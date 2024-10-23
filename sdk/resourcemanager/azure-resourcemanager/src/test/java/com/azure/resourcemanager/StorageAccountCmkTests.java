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
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.IdentityType;
import com.azure.resourcemanager.storage.models.KeySource;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import com.azure.security.keyvault.keys.models.KeyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class StorageAccountCmkTests extends ResourceManagerTestProxyTestBase {
    private AzureResourceManager azureResourceManager;

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential,
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
    }

    @Override
    protected void cleanUpResources() {}

    @Test
    // involves key vault key
    @DoNotRecord(skipInPlayback = true)
    public void testCmkAndMmk() {
        String vaultName = generateRandomResourceName("vt", 15);
        String msiName1 = generateRandomResourceName("msi", 15);
        String msiName2 = generateRandomResourceName("msi", 15);
        String saName = generateRandomResourceName("sa", 15);
        String rgName = generateRandomResourceName("jvmgmt", 15);
        Region region = Region.US_EAST;
        try {
            // create key vault with access policy
            Vault vault = azureResourceManager.vaults()
                .define(vaultName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withEmptyAccessPolicy()
                .withPurgeProtectionEnabled() // CMK requires purge protection enabled
                .create();
            // create two MSIs
            Identity identity1 = azureResourceManager.identities()
                .define(msiName1)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .create();
            Identity identity2 = azureResourceManager.identities()
                .define(msiName2)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .create();

            // create Storage Account with default MMK
            StorageAccount storageAccount = azureResourceManager.storageAccounts()
                .define(saName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withGeneralPurposeAccountKindV2()
                .withSku(StorageAccountSkuType.STANDARD_RAGRS)
                .withSystemAssignedManagedServiceIdentity()
                .create();

            Assertions.assertNull(storageAccount.identityTypeForCustomerEncryptionKey());
            Assertions.assertNull(storageAccount.userAssignedIdentityIdForCustomerEncryptionKey());

            Assertions.assertEquals(KeySource.MICROSOFT_STORAGE.toString(), storageAccount.encryptionKeySource().toString());

            // assign access policy to the three MSIs
            vault.update()
                .defineAccessPolicy()   // access policy for this sample client to generate key
                    .forUser(azureCliSignedInUser().userPrincipalName())
                    .allowKeyAllPermissions()
                    .attach()
                .defineAccessPolicy()
                    .forObjectId(storageAccount.systemAssignedManagedServiceIdentityPrincipalId())
                    .allowKeyPermissions(KeyPermissions.GET, KeyPermissions.WRAP_KEY, KeyPermissions.UNWRAP_KEY)
                    .attach()
                .defineAccessPolicy()
                    .forObjectId(identity1.principalId())
                    .allowKeyPermissions(KeyPermissions.GET, KeyPermissions.WRAP_KEY, KeyPermissions.UNWRAP_KEY)
                    .attach()
                .defineAccessPolicy()
                    .forObjectId(identity2.principalId())
                    .allowKeyPermissions(KeyPermissions.GET, KeyPermissions.WRAP_KEY, KeyPermissions.UNWRAP_KEY)
                    .attach()
                .apply();

            // create key vault key
            Key key = vault.keys().define("key1")
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeySize(4096)
                .create();

            // update Storage Account to CMK with system-assigned MSI
            storageAccount.update()
                .withEncryptionKeyFromKeyVault(vault.vaultUri(), key.name(), "")
                .apply();
            Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED, storageAccount.identityTypeForCustomerEncryptionKey());
            Assertions.assertEquals(KeySource.MICROSOFT_KEYVAULT.toString(), storageAccount.encryptionKeySource().toString());

            // update Storage Account to CMK with user-assigned MSI
            storageAccount.update()
                .withExistingUserAssignedManagedServiceIdentity(identity1.id())
                .withEncryptionKeyFromKeyVault(vault.vaultUri(), key.name(), "", identity1.id())
                .apply();

            Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.identityTypeForCustomerEncryptionKey());

            // update Storage Account to CMK with another user-assigned MSI
            storageAccount.update()
                .withoutUserAssignedManagedServiceIdentity(identity1.id())
                .withExistingUserAssignedManagedServiceIdentity(identity2.id())
                .withEncryptionKeyFromKeyVault(vault.vaultUri(), key.name(), "", identity2.id())
                .apply();

            Assertions.assertEquals(identity2.id(), storageAccount.userAssignedIdentityIdForCustomerEncryptionKey());

            // update Storage Account to CMK with system-assigned MSI
            storageAccount.update()
                .withEncryptionKeyFromKeyVault(vault.vaultUri(), key.name(), "")
                .apply();
            Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED, storageAccount.identityTypeForCustomerEncryptionKey());
            Assertions.assertNull(storageAccount.userAssignedIdentityIdForCustomerEncryptionKey());

            // update Storage Account to MMK
            storageAccount.update()
                .withMicrosoftManagedEncryptionKey()
                .apply();
            Assertions.assertEquals(KeySource.MICROSOFT_STORAGE.toString(), storageAccount.encryptionKeySource().toString());
            Assertions.assertNull(storageAccount.identityTypeForCustomerEncryptionKey());
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }
}
