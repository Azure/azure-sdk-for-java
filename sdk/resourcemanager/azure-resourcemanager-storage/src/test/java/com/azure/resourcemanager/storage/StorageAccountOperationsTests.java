// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.models.IdentityType;
import com.azure.resourcemanager.storage.models.Kind;
import com.azure.resourcemanager.storage.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.models.PublicNetworkAccess;
import com.azure.resourcemanager.storage.models.SkuName;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionStatus;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.azure.resourcemanager.storage.models.StorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class StorageAccountOperationsTests extends StorageManagementTest {
    private String rgName = "";
    private String saName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        saName = generateRandomResourceName("javacsmsa", 15);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCRUDStorageAccount() throws Exception {
        // Name available
        // Skipping checking name availability for now because of 503 error 'The service is not yet ready to process any
        // requests. Please retry in a few moments.'
        //        CheckNameAvailabilityResult result = storageManager.storageAccounts()
        //                .checkNameAvailability(SA_NAME);
        //        Assertions.assertEquals(true, result.isAvailable());
        // Create
        Mono<StorageAccount> resourceStream =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup(rgName)
                .withGeneralPurposeAccountKindV2()
                .withTag("tag1", "value1")
                .withHnsEnabled(true)
                .withAzureFilesAadIntegrationEnabled(false)
                .withInfrastructureEncryption()
                .createAsync();
        StorageAccount storageAccount = resourceStream.block();
        Assertions.assertEquals(rgName, storageAccount.resourceGroupName());
        Assertions.assertEquals(SkuName.STANDARD_RAGRS, storageAccount.skuType().name());
        Assertions.assertTrue(storageAccount.isHnsEnabled());
        Assertions.assertTrue(storageAccount.infrastructureEncryptionEnabled());
        // Assertions.assertFalse(storageAccount.isAzureFilesAadIntegrationEnabled());
        // List
        PagedIterable<StorageAccount> accounts = storageManager.storageAccounts().listByResourceGroup(rgName);
        boolean found = false;
        for (StorageAccount account : accounts) {
            if (account.name().equals(saName)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
        Assertions.assertEquals(1, storageAccount.tags().size());

        // Get
        storageAccount = storageManager.storageAccounts().getByResourceGroup(rgName, saName);
        Assertions.assertNotNull(storageAccount);

        // Get Keys
        List<StorageAccountKey> keys = storageAccount.getKeys();
        Assertions.assertTrue(keys.size() > 0);

        // Regen key
        StorageAccountKey oldKey = keys.get(0);
        List<StorageAccountKey> updatedKeys = storageAccount.regenerateKey(oldKey.keyName());
        Assertions.assertTrue(updatedKeys.size() > 0);
        for (StorageAccountKey updatedKey : updatedKeys) {
            if (updatedKey.keyName().equalsIgnoreCase(oldKey.keyName())) {
                if (!isPlaybackMode()) {
                    Assertions.assertNotEquals(oldKey.value(), updatedKey.value());
                }
                break;
            }
        }

        Assertions.assertTrue(storageAccount.infrastructureEncryptionEnabled());

        Map<StorageService, StorageAccountEncryptionStatus> statuses = storageAccount.encryptionStatuses();
        Assertions.assertNotNull(statuses);
        Assertions.assertTrue(statuses.size() > 0);

        Assertions.assertTrue(statuses.containsKey(StorageService.BLOB));
        StorageAccountEncryptionStatus blobServiceEncryptionStatus = statuses.get(StorageService.BLOB);
        Assertions.assertNotNull(blobServiceEncryptionStatus);
        Assertions.assertTrue(blobServiceEncryptionStatus.isEnabled()); // Service will enable this by default

        Assertions.assertTrue(statuses.containsKey(StorageService.FILE));
        StorageAccountEncryptionStatus fileServiceEncryptionStatus = statuses.get(StorageService.FILE);
        Assertions.assertNotNull(fileServiceEncryptionStatus);
        Assertions.assertTrue(fileServiceEncryptionStatus.isEnabled()); // Service will enable this by default

        // Update
        storageAccount = storageAccount.update()
            .withSku(StorageAccountSkuType.STANDARD_LRS).withTag("tag2", "value2").apply();
        Assertions.assertEquals(SkuName.STANDARD_LRS, storageAccount.skuType().name());
        Assertions.assertEquals(2, storageAccount.tags().size());
    }

    @Test
    public void canEnableLargeFileSharesOnStorageAccount() throws Exception {
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .withLargeFileShares(true)
                .create();

        Assertions.assertTrue(storageAccount.isLargeFileSharesEnabled());
    }

    @Test
    public void storageAccountDefault() {
        String saName2 = generateRandomResourceName("javacsmsa", 15);

        // default
        StorageAccount storageAccountDefault = storageManager.storageAccounts().define(saName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .create();

        Assertions.assertEquals(Kind.STORAGE_V2, storageAccountDefault.kind());
        Assertions.assertEquals(SkuName.STANDARD_RAGRS, storageAccountDefault.skuType().name());
        Assertions.assertTrue(storageAccountDefault.isHttpsTrafficOnly());
        Assertions.assertEquals(MinimumTlsVersion.TLS1_2, storageAccountDefault.minimumTlsVersion());
        Assertions.assertTrue(storageAccountDefault.isBlobPublicAccessAllowed());
        Assertions.assertTrue(storageAccountDefault.isSharedKeyAccessAllowed());

        // update to non-default
        StorageAccount storageAccount = storageAccountDefault.update()
            .withHttpAndHttpsTraffic()
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_1)
            .disableBlobPublicAccess()
            .disableSharedKeyAccess()
            .apply();

        Assertions.assertFalse(storageAccount.isHttpsTrafficOnly());
        Assertions.assertEquals(MinimumTlsVersion.TLS1_1, storageAccount.minimumTlsVersion());
        Assertions.assertFalse(storageAccount.isBlobPublicAccessAllowed());
        Assertions.assertFalse(storageAccount.isSharedKeyAccessAllowed());

        // new storage account configured as non-default
        storageAccount = storageManager.storageAccounts().define(saName2)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withSku(StorageAccountSkuType.STANDARD_LRS)
            .withGeneralPurposeAccountKind()
            .withHttpAndHttpsTraffic()
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_1)
            .disableBlobPublicAccess()
            .disableSharedKeyAccess()
            .create();

        Assertions.assertEquals(Kind.STORAGE, storageAccount.kind());
        Assertions.assertEquals(SkuName.STANDARD_LRS, storageAccount.skuType().name());
        Assertions.assertFalse(storageAccount.isHttpsTrafficOnly());
        Assertions.assertEquals(MinimumTlsVersion.TLS1_1, storageAccount.minimumTlsVersion());
        Assertions.assertFalse(storageAccount.isBlobPublicAccessAllowed());
        Assertions.assertFalse(storageAccount.isSharedKeyAccessAllowed());
    }

    @Test
    public void canAllowCrossTenantReplicationOnStorageAccount() {
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .create();

        Assertions.assertFalse(storageAccount.isAllowCrossTenantReplication());

        storageAccount.update()
            .allowCrossTenantReplication()
            .apply();

        Assertions.assertTrue(storageAccount.isAllowCrossTenantReplication());
    }

    @Test
    public void canDisallowCrossTenantReplicationOnStorageAccount() {
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .allowCrossTenantReplication()
                .create();

        Assertions.assertTrue(storageAccount.isAllowCrossTenantReplication());

        storageAccount.update()
            .disallowCrossTenantReplication()
            .apply();

        Assertions.assertFalse(storageAccount.isAllowCrossTenantReplication());
    }

    @Test
    public void canEnableDefaultToOAuthAuthenticationOnStorageAccount() {
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .create();

        Assertions.assertFalse(storageAccount.isDefaultToOAuthAuthentication());

        storageAccount.update()
            .enableDefaultToOAuthAuthentication()
            .apply();

        Assertions.assertTrue(storageAccount.isDefaultToOAuthAuthentication());
    }

    @Test
    public void canDisableDefaultToOAuthAuthenticationOnStorageAccount() {
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .enableDefaultToOAuthAuthentication()
                .create();

        Assertions.assertTrue(storageAccount.isDefaultToOAuthAuthentication());

        storageAccount.update()
            .disableDefaultToOAuthAuthentication()
            .apply();

        Assertions.assertFalse(storageAccount.isDefaultToOAuthAuthentication());
    }

    @Test
    public void createStorageAccountWithSystemAssigned() {

        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withSystemAssignedManagedServiceIdentity()
            .create();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateStorageAccountWithSystemAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .create();
        Assertions.assertNull(storageAccount.innerModel().identity());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update().withSystemAssignedManagedServiceIdentity().apply();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }


    @Test
    public void updateStorageAccountWithoutSystemAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withSystemAssignedManagedServiceIdentity()
            .create();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update().withoutSystemAssignedManagedServiceIdentity().apply();
        Assertions.assertEquals(IdentityType.NONE, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void createStorageAccountWithNewUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();

        Creatable<com.azure.resourcemanager.msi.models.Identity> identityCreatable =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName);

        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withNewUserAssignedManagedServiceIdentity(identityCreatable)
            .create();

        Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateStorageAccountWithNewUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();

        Creatable<com.azure.resourcemanager.msi.models.Identity> identityCreatable =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName);

        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .create();

        Assertions.assertNull(storageAccount.innerModel().identity());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update()
            .withNewUserAssignedManagedServiceIdentity(identityCreatable).apply();

        Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void createStorageAccountWithExistUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();

        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity)
            .create();

        Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateStorageAccountWithExistUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();

        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .create();

        Assertions.assertNull(storageAccount.innerModel().identity());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update()
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity).apply();

        Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateStorageAccountWithoutUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();

        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity)
            .create();
        Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update().withoutUserAssignedManagedServiceIdentity(defaultIdentity.id()).apply();
        Assertions.assertEquals(IdentityType.NONE, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateIdentityFromSystemUserAssignedToSystemAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withSystemAssignedManagedServiceIdentity()
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity)
            .create();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update().withoutUserAssignedManagedServiceIdentity(defaultIdentity.id()).apply();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateIdentityFromSystemUserAssignedToUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withSystemAssignedManagedServiceIdentity()
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity)
            .create();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update().withoutSystemAssignedManagedServiceIdentity().apply();
        Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateIdentityFromSystemUserAssignedToNone() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withSystemAssignedManagedServiceIdentity()
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity)
            .create();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update().withoutSystemAssignedManagedServiceIdentity()
            .withoutUserAssignedManagedServiceIdentity(defaultIdentity.id()).apply();
        Assertions.assertEquals(IdentityType.NONE, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateIdentityFromSystemAssignedToUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withSystemAssignedManagedServiceIdentity()
            .create();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update()
            .withoutSystemAssignedManagedServiceIdentity()
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity)
            .apply();
        Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateIdentityFromUserAssignedToSystemAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity)
            .create();
        Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update()
            .withSystemAssignedManagedServiceIdentity()
            .withoutUserAssignedManagedServiceIdentity(defaultIdentity.id())
            .apply();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateIdentityFromUserAssignedToSystemUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity)
            .create();
        Assertions.assertEquals(IdentityType.USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update().withSystemAssignedManagedServiceIdentity().apply();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateIdentityFromSystemAssignedToSystemUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withSystemAssignedManagedServiceIdentity()
            .create();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertTrue(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());

        storageAccount.update().withExistingUserAssignedManagedServiceIdentity(defaultIdentity).apply();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void updateIdentityFromNoneToSystemUserAssigned() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        com.azure.resourcemanager.msi.models.Identity defaultIdentity =
            msiManager
                .identities()
                .define(generateRandomResourceName("javacsmmsi", 15))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .create();

        storageAccount.update()
            .withSystemAssignedManagedServiceIdentity()
            .withExistingUserAssignedManagedServiceIdentity(defaultIdentity)
            .apply();
        Assertions.assertEquals(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED, storageAccount.innerModel().identity().type());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(storageAccount.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertFalse(storageAccount.userAssignedManagedServiceIdentityIds().isEmpty());
    }

    @Test
    public void canCreateStorageAccountWithDisabledPublicNetworkAccess() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withSystemAssignedManagedServiceIdentity()
            .disablePublicNetworkAccess()
            .create();
        Assertions.assertEquals(PublicNetworkAccess.DISABLED, storageAccount.publicNetworkAccess());
    }

    @Test
    public void canUpdatePublicNetworkAccess() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        StorageAccount storageAccount = storageManager
            .storageAccounts()
            .define(saName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withSystemAssignedManagedServiceIdentity()
            .create();
        storageAccount.update().disablePublicNetworkAccess().apply();
        Assertions.assertEquals(PublicNetworkAccess.DISABLED, storageAccount.publicNetworkAccess());

        storageAccount.update().enablePublicNetworkAccess().apply();
        Assertions.assertEquals(PublicNetworkAccess.ENABLED, storageAccount.publicNetworkAccess());
    }
}
