// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.models.CustomMatcher;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.security.keyvault.keys.models.KeyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;

public class DiskEncryptionSetTests extends ComputeManagementTest {
    String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        if (rgName != null) {
            resourceManager.resourceGroups().deleteByName(rgName);
        }
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();

        if (interceptorManager.isPlaybackMode()) {
            if (!testContextManager.doNotRecordTest()) {
                // don't match api-version when matching url
                interceptorManager.addMatchers(new CustomMatcher()
                    .setHeadersKeyOnlyMatch(Collections.singletonList("Accept"))
                    .setExcludedHeaders(Collections.singletonList("Accept-Language"))
                    .setIgnoredQueryParameters(Collections.singletonList("api-version")));
            }
        }
    }

    @Test
    public void canCRUDDiskEncryptionSet() {
        Region region = Region.US_EAST;
        VaultAndKey vaultAndKey = createVaultAndKey(region, rgName, generateRandomResourceName("kv", 15),
            azureCliSignedInUser().userPrincipalName());

        String name = generateRandomResourceName("des", 15);
        DiskEncryptionSet diskEncryptionSet = computeManager.diskEncryptionSets()
            .define(name)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withEncryptionType(DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY)
            .withExistingKeyVault(vaultAndKey.vault.id())
            .withExistingKey(vaultAndKey.key.id())
            .withSystemAssignedManagedServiceIdentity()
            .withRoleBasedAccessToCurrentKeyVault()
            .withAutomaticKeyRotation()
            .create();

        Assertions.assertEquals(vaultAndKey.vault.id(), diskEncryptionSet.keyVaultId());
        Assertions.assertEquals(vaultAndKey.key.id(), diskEncryptionSet.encryptionKeyId());
        Assertions.assertNotNull(diskEncryptionSet.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertTrue(diskEncryptionSet.isAutomaticKeyRotationEnabled());
        Assertions.assertEquals(DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY, diskEncryptionSet.encryptionType());

        diskEncryptionSet.update()
            .withoutSystemAssignedManagedServiceIdentity()
            .withoutAutomaticKeyRotation()
            .apply();
        Assertions.assertNull(diskEncryptionSet.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertFalse(diskEncryptionSet.isAutomaticKeyRotationEnabled());
    }

    static final class VaultAndKey {
        private final Vault vault;
        private final Key key;

        private VaultAndKey(Vault vault, Key key) {
            this.vault = vault;
            this.key = key;
        }
    }

    VaultAndKey createVaultAndKey(Region region, String rgName, String name, String signedInUser) {
        // create vault
        Vault vault = keyVaultManager.vaults().define(name)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withRoleBasedAccessControl()
            .withPurgeProtectionEnabled()
            .create();

        // RBAC for this app
        String rbacName = generateRandomUuid();
        authorizationManager.roleAssignments().define(rbacName)
            .forUser(signedInUser)
            .withBuiltInRole(BuiltInRole.KEY_VAULT_ADMINISTRATOR)
            .withResourceScope(vault)
            .create();
        // wait for propagation time
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));

        // create key
        Key key = createKey(vault);

        return new VaultAndKey(vault, key);
    }

    Key createKey(Vault vault) {
        return vault.keys().define("key1")
            .withKeyTypeToCreate(KeyType.RSA)
            .withKeySize(4096)
            .create();
    }
}
