/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.hdinsight.v2018_06_01_preview.utilities;

import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.requests.CreateKeyRequest;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;
import com.microsoft.azure.management.keyvault.*;
import com.microsoft.azure.management.keyvault.implementation.KeyVaultManager;
import com.microsoft.azure.management.keyvault.implementation.VaultInner;
import com.microsoft.azure.management.msi.implementation.IdentityInner;
import com.microsoft.azure.management.msi.implementation.MSIManager;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.Kind;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountSkuType;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestClient;
import org.assertj.core.util.Lists;

import java.util.List;
import java.util.UUID;

import static com.microsoft.azure.arm.core.TestBase.generateRandomResourceName;
import static com.microsoft.azure.arm.core.TestBase.isRecordMode;
import static com.microsoft.azure.management.hdinsight.v2018_06_01_preview.scenariotests.HDInsightManagementTestBase.*;
import static com.microsoft.azure.management.storage.Kind.STORAGE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Manage Azure resources used during test execution
 */
public class HDInsightTestResourceManager {

    /**
     * Default key vault permissions. Give the service principal all permissions
     */
    private static final Permissions DEFAULT_PERMISSIONS = new Permissions()
        .withKeys(Lists.newArrayList(KeyPermissions.values()))
        .withSecrets(Lists.newArrayList(SecretPermissions.values()))
        .withCertificates(Lists.newArrayList(CertificatePermissions.values()))
        .withStorage(Lists.newArrayList(StoragePermissions.values()));

    private static final int AZURE_RESOURCE_NAME_MAX_LEN = 24;

    /**
     * Entry point to Azure Resources resource management.
     */
    private final ResourceManager resourceManager;

    /**
     * Entry point to Azure Storage resource management.
     */
    private final StorageManager storageManager;

    /**
     * Entry point to Azure Managed Service Identity (MSI) resource management.
     */
    private final MSIManager msiManager;

    /**
     * Entry point to Azure KeyVault resource management.
     */
    private final KeyVaultManager keyVaultManager;

    /**
     * Key vault client.
     */
    private final KeyVaultClient keyVaultClient;

    private final String tenantId;

    public HDInsightTestResourceManager(RestClient restClient, String subscription, String tenant) {
        resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscription);
        storageManager = StorageManager.authenticate(restClient, subscription);
        msiManager = MSIManager.authenticate(restClient, subscription);
        keyVaultManager = KeyVaultManager.authenticate(restClient, tenant, subscription);
        keyVaultClient = new KeyVaultClient(createKeyVaultRestClient(restClient));
        tenantId = tenant;
    }

    /**
     * Create REST client for Key vault client
     */
    private RestClient createKeyVaultRestClient(RestClient restClient) {
        if (isRecordMode()) {
            return restClient.newBuilder().withBaseUrl("https://{vaultBaseUrl}").build();
        } else {
            return restClient;
        }
    }

    /**
     * Create a resource group
     */
    public ResourceGroup createResourceGroup() {
        return createResourceGroup(RESOURCE_GROUP_NAME_PREFIX, REGION);
    }

    public ResourceGroup createResourceGroup(String namePrefix, String region) {
        String resourceGroupName = generateRandomResourceName(namePrefix, AZURE_RESOURCE_NAME_MAX_LEN);

        // Here we assume that the resource group name is available.
        // Considering that deletion is a dangerous operation, we just report error back to the tester
        assertThat(resourceManager.resourceGroups().checkExistence(resourceGroupName)).isFalse();

        return resourceManager.resourceGroups().define(resourceGroupName)
            .withRegion(region)
            .create();
    }

    /**
     * delete a resource group
     */
    public void deleteResourceGroup(String resourceGroupName) {
        resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
    }

    /**
     * Create a storage account.
     */
    public StorageAccount createStorageAccount(String resourceGroupName) {
        return createStorageAccount(resourceGroupName, STORAGE_ACCOUNT_NAME_PREFIX);
    }

    public StorageAccount createStorageAccount(String resourceGroupName, String namePrefix) {
        return createStorageAccount(resourceGroupName, namePrefix, STORAGE);
    }

    public StorageAccount createStorageAccount(String resourceGroupName, String namePrefix, Kind kind) {
        return createStorageAccount(
            resourceGroupName,
            namePrefix,
            StorageAccountSkuType.STANDARD_LRS,
            REGION,
            kind
        );
    }

    public StorageAccount createStorageAccount(String resourceGroupName,
                                               String namePrefix,
                                               StorageAccountSkuType sku,
                                               String region,
                                               Kind kind) {
        String storageAccountName = generateRandomResourceName(namePrefix, AZURE_RESOURCE_NAME_MAX_LEN);
        StorageAccount.DefinitionStages.WithCreate builder = storageManager.storageAccounts().define(storageAccountName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroupName)
            .withSku(sku);

        switch (kind) {
            case BLOB_STORAGE:
                builder = builder.withBlobStorageAccountKind();
                break;
            case STORAGE:
                builder = builder.withGeneralPurposeAccountKind();
                break;
            case STORAGE_V2:
                builder = builder.withGeneralPurposeAccountKindV2();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported storage kind: " + kind);
        }

        return builder.create();
    }

    /**
     * Retrieve a storage account key.
     */
    public String generateKey(StorageAccount storageAccount) {
        return storageManager.storageAccounts()
            .inner()
            .listKeys(storageAccount.resourceGroupName(), storageAccount.name())
            .keys()
            .get(0)
            .value();
    }

    /**
     * Create a Managed Service Identity
     */
    public IdentityInner createManagedIdentity(String resourceGroupName) {
        return createManagedIdentity(resourceGroupName, MANAGED_IDENTITY_NAME_PREFIX, REGION);
    }

    public IdentityInner createManagedIdentity(String resourceGroupName, String namePrefix, String region) {
        String msiName = generateRandomResourceName(namePrefix, AZURE_RESOURCE_NAME_MAX_LEN);
        IdentityInner createParams = (IdentityInner) new IdentityInner().withLocation(region);
        return msiManager.identities().inner().createOrUpdate(resourceGroupName, msiName, createParams);
    }

    /**
     * Create a vault
     */
    public VaultInner createVault(String resourceGroupName, Boolean enableSoftDelete) {
        return createVault(
            resourceGroupName,
            SkuName.PREMIUM,
            DEFAULT_PERMISSIONS,
            true,
            true,
            true,
            enableSoftDelete,
            VAULT_NAME_PREFIX,
            REGION);
    }

    public VaultInner createVault(String resourceGroupName,
                                  SkuName sku,
                                  Permissions permissions,
                                  Boolean enabledForDeployment,
                                  Boolean enabledForDiskEncryption,
                                  Boolean enabledForTemplateDeployment,
                                  Boolean enableSoftDelete,
                                  String namePrefix,
                                  String region) {
        String vaultName = generateRandomResourceName(namePrefix, AZURE_RESOURCE_NAME_MAX_LEN);
        List<AccessPolicyEntry> accessPolicies = Lists.newArrayList(
            new AccessPolicyEntry().withTenantId(UUID.fromString(tenantId))
                .withObjectId(CLIENT_OID)
                .withPermissions(permissions)
        );

        VaultProperties properties = new VaultProperties()
            .withTenantId(UUID.fromString(tenantId))
            .withSku(new Sku().withName(sku))
            .withAccessPolicies(accessPolicies)
            .withEnabledForDeployment(enabledForDeployment)
            .withEnabledForDiskEncryption(enabledForDiskEncryption)
            .withEnabledForTemplateDeployment(enabledForTemplateDeployment)
            .withEnableSoftDelete(enableSoftDelete);
        VaultCreateOrUpdateParameters parameters = new VaultCreateOrUpdateParameters().withLocation(region).withProperties(properties);
        return keyVaultManager.vaults().inner().createOrUpdate(resourceGroupName, vaultName, parameters);
    }

    /**
     * Update vault to add access policy entry to some object, service principal or MSI, etc.
     *
     * @param vault             vault to update
     * @param resourceGroupName name of resource group where vault locates in
     * @param objectId          object ID of some Azure resource
     * @param permissions       permissions to be assigned to the specific Azure resource
     */
    public VaultInner setVaultPermissions(VaultInner vault, String resourceGroupName, String objectId, Permissions permissions) {
        vault.properties().accessPolicies().add(
            new AccessPolicyEntry().withTenantId(UUID.fromString(tenantId))
                .withObjectId(objectId)
                .withPermissions(permissions)
        );

        VaultCreateOrUpdateParameters updateParams = new VaultCreateOrUpdateParameters()
            .withLocation(vault.location())
            .withProperties(vault.properties());
        return keyVaultManager.vaults().inner().createOrUpdate(resourceGroupName, vault.name(), updateParams);
    }

    /**
     * Generate a vault key identifier
     */
    public KeyIdentifier generateVaultKey(VaultInner vault, String namePrefix) {
        String vaultUri = vault.properties().vaultUri();
        String keyName = generateRandomResourceName(namePrefix, AZURE_RESOURCE_NAME_MAX_LEN);
        KeyBundle createdBundle = keyVaultClient.createKey(new CreateKeyRequest.Builder(vaultUri, keyName, JsonWebKeyType.RSA).build());
        return new KeyIdentifier(createdBundle.key().kid());
    }

    /**
     * Delete a vault
     */
    public void deleteVault(String resourceGroupName, String vaultName) {
        keyVaultManager.vaults().deleteByResourceGroup(resourceGroupName, vaultName);
    }

    /**
     * Purge a deleted vault
     */
    public void purgeDeletedVault(String vaultName, String location) {
        keyVaultManager.vaults().purgeDeleted(vaultName, location);
    }
}
