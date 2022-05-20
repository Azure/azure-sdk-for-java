// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import com.azure.security.keyvault.keys.models.KeyType;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DiskEncryptionTestBase extends ResourceManagerTestBase {

    protected AzureResourceManager azureResourceManager;

    protected String rgName = "";
    protected final Region region = Region.US_EAST;

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        azureResourceManager = buildManager(AzureResourceManager.class, httpPipeline, profile);
        setInternalContext(internalContext, azureResourceManager);

        rgName = generateRandomResourceName("javacsmrg", 15);
    }

    @Override
    protected void cleanUpResources() {
        try {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) {
        }
    }

    protected static final class VaultAndKey {
        private final Vault vault;
        private final Key key;

        private VaultAndKey(Vault vault, Key key) {
            this.vault = vault;
            this.key = key;
        }
    }

    protected VaultAndKey createVaultAndKey(String name, String clientId) {
        // create vault
        Vault vault = azureResourceManager.vaults().define(name)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withRoleBasedAccessControl()
            .withPurgeProtectionEnabled()
            .create();

        // RBAC for this app
        String rbacName = generateRandomUuid();
        azureResourceManager.accessManagement().roleAssignments().define(rbacName)
            .forServicePrincipal(clientId)
            .withBuiltInRole(BuiltInRole.KEY_VAULT_ADMINISTRATOR)
            .withResourceScope(vault)
            .create();
        // wait for propagation time
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));

        // create key
        Key key = vault.keys().define("key1")
            .withKeyTypeToCreate(KeyType.RSA)
            .withKeySize(4096)
            .create();

        return new VaultAndKey(vault, key);
    }

    protected DiskEncryptionSet createDiskEncryptionSet(String name, DiskEncryptionSetType type, VaultAndKey vaultAndKey) {
        return azureResourceManager.diskEncryptionSets()
            .define(name)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withEncryptionType(type)
            .withExistingKeyVault(vaultAndKey.vault.id())
            .withExistingKey(vaultAndKey.key.id())
            .withSystemAssignedManagedServiceIdentity()
            .withRBACBasedAccessToCurrentKeyVault(BuiltInRole.KEY_VAULT_CRYPTO_SERVICE_ENCRYPTION_USER)
            .create();
    }
}
