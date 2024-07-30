# Code snippets and samples


## AmlFilesystems

- [Archive](#amlfilesystems_archive)
- [CancelArchive](#amlfilesystems_cancelarchive)
- [CreateOrUpdate](#amlfilesystems_createorupdate)
- [Delete](#amlfilesystems_delete)
- [GetByResourceGroup](#amlfilesystems_getbyresourcegroup)
- [List](#amlfilesystems_list)
- [ListByResourceGroup](#amlfilesystems_listbyresourcegroup)
- [Update](#amlfilesystems_update)

## AscOperations

- [Get](#ascoperations_get)

## AscUsages

- [List](#ascusages_list)

## Caches

- [CreateOrUpdate](#caches_createorupdate)
- [DebugInfo](#caches_debuginfo)
- [Delete](#caches_delete)
- [Flush](#caches_flush)
- [GetByResourceGroup](#caches_getbyresourcegroup)
- [List](#caches_list)
- [ListByResourceGroup](#caches_listbyresourcegroup)
- [PausePrimingJob](#caches_pauseprimingjob)
- [ResumePrimingJob](#caches_resumeprimingjob)
- [SpaceAllocation](#caches_spaceallocation)
- [Start](#caches_start)
- [StartPrimingJob](#caches_startprimingjob)
- [Stop](#caches_stop)
- [StopPrimingJob](#caches_stopprimingjob)
- [Update](#caches_update)
- [UpgradeFirmware](#caches_upgradefirmware)

## ImportJobs

- [CreateOrUpdate](#importjobs_createorupdate)
- [Delete](#importjobs_delete)
- [Get](#importjobs_get)
- [ListByAmlFilesystem](#importjobs_listbyamlfilesystem)
- [Update](#importjobs_update)

## Operations

- [List](#operations_list)

## ResourceProvider

- [CheckAmlFSSubnets](#resourceprovider_checkamlfssubnets)
- [GetRequiredAmlFSSubnetsSize](#resourceprovider_getrequiredamlfssubnetssize)

## Skus

- [List](#skus_list)

## StorageTargetOperation

- [Flush](#storagetargetoperation_flush)
- [Invalidate](#storagetargetoperation_invalidate)
- [Resume](#storagetargetoperation_resume)
- [Suspend](#storagetargetoperation_suspend)

## StorageTargets

- [CreateOrUpdate](#storagetargets_createorupdate)
- [Delete](#storagetargets_delete)
- [DnsRefresh](#storagetargets_dnsrefresh)
- [Get](#storagetargets_get)
- [ListByCache](#storagetargets_listbycache)
- [RestoreDefaults](#storagetargets_restoredefaults)

## UsageModels

- [List](#usagemodels_list)
### AmlFilesystems_Archive

```java
import com.azure.resourcemanager.storagecache.models.AmlFilesystemArchiveInfo;

/**
 * Samples for AmlFilesystems Archive.
 */
public final class AmlFilesystemsArchiveSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/amlFilesystems_Archive.json
     */
    /**
     * Sample code: amlFilesystems_Archive.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void amlFilesystemsArchive(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.amlFilesystems()
            .archiveWithResponse("scgroup", "sc", new AmlFilesystemArchiveInfo().withFilesystemPath("/"),
                com.azure.core.util.Context.NONE);
    }
}
```

### AmlFilesystems_CancelArchive

```java
/**
 * Samples for AmlFilesystems CancelArchive.
 */
public final class AmlFilesystemsCancelArchiveSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/amlFilesystems_CancelArchive.json
     */
    /**
     * Sample code: amlFilesystems_cancelArchive.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void amlFilesystemsCancelArchive(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.amlFilesystems().cancelArchiveWithResponse("scgroup", "sc", com.azure.core.util.Context.NONE);
    }
}
```

### AmlFilesystems_CreateOrUpdate

```java
import com.azure.resourcemanager.storagecache.models.AmlFilesystemEncryptionSettings;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemHsmSettings;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemIdentity;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemIdentityType;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemPropertiesHsm;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemPropertiesMaintenanceWindow;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemRootSquashSettings;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemSquashMode;
import com.azure.resourcemanager.storagecache.models.KeyVaultKeyReference;
import com.azure.resourcemanager.storagecache.models.KeyVaultKeyReferenceSourceVault;
import com.azure.resourcemanager.storagecache.models.MaintenanceDayOfWeekType;
import com.azure.resourcemanager.storagecache.models.SkuName;
import com.azure.resourcemanager.storagecache.models.UserAssignedIdentitiesValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AmlFilesystems CreateOrUpdate.
 */
public final class AmlFilesystemsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/amlFilesystems_CreateOrUpdate.json
     */
    /**
     * Sample code: amlFilesystems_CreateOrUpdate.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void
        amlFilesystemsCreateOrUpdate(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.amlFilesystems()
            .define("fs1")
            .withRegion("eastus")
            .withExistingResourceGroup("scgroup")
            .withTags(mapOf("Dept", "ContosoAds"))
            .withIdentity(new AmlFilesystemIdentity().withType(AmlFilesystemIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity1",
                    new UserAssignedIdentitiesValue())))
            .withSku(new SkuName().withName("AMLFS-Durable-Premium-250"))
            .withZones(Arrays.asList("1"))
            .withStorageCapacityTiB(16f)
            .withFilesystemSubnet(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.Network/virtualNetworks/scvnet/subnets/fsSub")
            .withEncryptionSettings(new AmlFilesystemEncryptionSettings()
                .withKeyEncryptionKey(new KeyVaultKeyReference().withKeyUrl("fakeTokenPlaceholder")
                    .withSourceVault(new KeyVaultKeyReferenceSourceVault().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.KeyVault/vaults/keyvault-cmk"))))
            .withMaintenanceWindow(
                new AmlFilesystemPropertiesMaintenanceWindow().withDayOfWeek(MaintenanceDayOfWeekType.FRIDAY)
                    .withTimeOfDayUtc("22:00"))
            .withHsm(new AmlFilesystemPropertiesHsm().withSettings(new AmlFilesystemHsmSettings().withContainer(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.Storage/storageAccounts/storageaccountname/blobServices/default/containers/containername")
                .withLoggingContainer(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.Storage/storageAccounts/storageaccountname/blobServices/default/containers/loggingcontainername")
                .withImportPrefixesInitial(Arrays.asList("/"))))
            .withRootSquashSettings(new AmlFilesystemRootSquashSettings().withMode(AmlFilesystemSquashMode.ALL)
                .withNoSquashNidLists("10.0.0.[5-6]@tcp;10.0.1.2@tcp")
                .withSquashUid(99L)
                .withSquashGid(99L))
            .create();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### AmlFilesystems_Delete

```java
/**
 * Samples for AmlFilesystems Delete.
 */
public final class AmlFilesystemsDeleteSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/amlFilesystems_Delete.json
     */
    /**
     * Sample code: amlFilesystems_Delete.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void amlFilesystemsDelete(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.amlFilesystems().delete("scgroup", "fs1", com.azure.core.util.Context.NONE);
    }
}
```

### AmlFilesystems_GetByResourceGroup

```java
/**
 * Samples for AmlFilesystems GetByResourceGroup.
 */
public final class AmlFilesystemsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/amlFilesystems_Get.json
     */
    /**
     * Sample code: amlFilesystems_Get.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void amlFilesystemsGet(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.amlFilesystems().getByResourceGroupWithResponse("scgroup", "fs1", com.azure.core.util.Context.NONE);
    }
}
```

### AmlFilesystems_List

```java
/**
 * Samples for AmlFilesystems List.
 */
public final class AmlFilesystemsListSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/amlFilesystems_List.json
     */
    /**
     * Sample code: amlFilesystems_List.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void amlFilesystemsList(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.amlFilesystems().list(com.azure.core.util.Context.NONE);
    }
}
```

### AmlFilesystems_ListByResourceGroup

```java
/**
 * Samples for AmlFilesystems ListByResourceGroup.
 */
public final class AmlFilesystemsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/amlFilesystems_ListByResourceGroup.json
     */
    /**
     * Sample code: amlFilesystems_ListByResourceGroup.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void
        amlFilesystemsListByResourceGroup(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.amlFilesystems().listByResourceGroup("scgroup", com.azure.core.util.Context.NONE);
    }
}
```

### AmlFilesystems_Update

```java
import com.azure.resourcemanager.storagecache.models.AmlFilesystem;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemEncryptionSettings;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemRootSquashSettings;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemSquashMode;
import com.azure.resourcemanager.storagecache.models.AmlFilesystemUpdatePropertiesMaintenanceWindow;
import com.azure.resourcemanager.storagecache.models.KeyVaultKeyReference;
import com.azure.resourcemanager.storagecache.models.KeyVaultKeyReferenceSourceVault;
import com.azure.resourcemanager.storagecache.models.MaintenanceDayOfWeekType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AmlFilesystems Update.
 */
public final class AmlFilesystemsUpdateSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/amlFilesystems_Update.json
     */
    /**
     * Sample code: amlFilesystems_Update.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void amlFilesystemsUpdate(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        AmlFilesystem resource = manager.amlFilesystems()
            .getByResourceGroupWithResponse("scgroup", "fs1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Dept", "ContosoAds"))
            .withEncryptionSettings(new AmlFilesystemEncryptionSettings()
                .withKeyEncryptionKey(new KeyVaultKeyReference().withKeyUrl("fakeTokenPlaceholder")
                    .withSourceVault(new KeyVaultKeyReferenceSourceVault().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.KeyVault/vaults/keyvault-cmk"))))
            .withMaintenanceWindow(
                new AmlFilesystemUpdatePropertiesMaintenanceWindow().withDayOfWeek(MaintenanceDayOfWeekType.FRIDAY)
                    .withTimeOfDayUtc("22:00"))
            .withRootSquashSettings(new AmlFilesystemRootSquashSettings().withMode(AmlFilesystemSquashMode.ALL)
                .withNoSquashNidLists("10.0.0.[5-6]@tcp;10.0.1.2@tcp")
                .withSquashUid(99L)
                .withSquashGid(99L))
            .apply();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### AscOperations_Get

```java
/**
 * Samples for AscOperations Get.
 */
public final class AscOperationsGetSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/AscOperations_Get.json
     */
    /**
     * Sample code: AscOperations_Get.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void ascOperationsGet(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.ascOperations().getWithResponse("westus", "testoperationid", com.azure.core.util.Context.NONE);
    }
}
```

### AscUsages_List

```java
/**
 * Samples for AscUsages List.
 */
public final class AscUsagesListSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/AscResourceUsages_Get.json
     */
    /**
     * Sample code: AscUsages_List.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void ascUsagesList(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.ascUsages().list("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### Caches_CreateOrUpdate

```java
import com.azure.resourcemanager.storagecache.models.CacheActiveDirectorySettings;
import com.azure.resourcemanager.storagecache.models.CacheActiveDirectorySettingsCredentials;
import com.azure.resourcemanager.storagecache.models.CacheDirectorySettings;
import com.azure.resourcemanager.storagecache.models.CacheEncryptionSettings;
import com.azure.resourcemanager.storagecache.models.CacheIdentity;
import com.azure.resourcemanager.storagecache.models.CacheIdentityType;
import com.azure.resourcemanager.storagecache.models.CacheSecuritySettings;
import com.azure.resourcemanager.storagecache.models.CacheSku;
import com.azure.resourcemanager.storagecache.models.CacheUpgradeSettings;
import com.azure.resourcemanager.storagecache.models.CacheUsernameDownloadSettings;
import com.azure.resourcemanager.storagecache.models.CacheUsernameDownloadSettingsCredentials;
import com.azure.resourcemanager.storagecache.models.KeyVaultKeyReference;
import com.azure.resourcemanager.storagecache.models.KeyVaultKeyReferenceSourceVault;
import com.azure.resourcemanager.storagecache.models.NfsAccessPolicy;
import com.azure.resourcemanager.storagecache.models.NfsAccessRule;
import com.azure.resourcemanager.storagecache.models.NfsAccessRuleAccess;
import com.azure.resourcemanager.storagecache.models.NfsAccessRuleScope;
import com.azure.resourcemanager.storagecache.models.UserAssignedIdentitiesValueAutoGenerated;
import com.azure.resourcemanager.storagecache.models.UsernameSource;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Caches CreateOrUpdate.
 */
public final class CachesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_CreateOrUpdate_ldap_only.json
     */
    /**
     * Sample code: Caches_CreateOrUpdate_ldap_only.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void
        cachesCreateOrUpdateLdapOnly(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches()
            .define("sc1")
            .withRegion("westus")
            .withExistingResourceGroup("scgroup")
            .withTags(mapOf("Dept", "Contoso"))
            .withSku(new CacheSku().withName("Standard_2G"))
            .withCacheSizeGB(3072)
            .withSubnet(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.Network/virtualNetworks/scvnet/subnets/sub1")
            .withUpgradeSettings(new CacheUpgradeSettings().withUpgradeScheduleEnabled(true)
                .withScheduledTime(OffsetDateTime.parse("2022-04-26T18:25:43.511Z")))
            .withEncryptionSettings(new CacheEncryptionSettings().withKeyEncryptionKey(new KeyVaultKeyReference()
                .withKeyUrl("fakeTokenPlaceholder")
                .withSourceVault(new KeyVaultKeyReferenceSourceVault().withId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.KeyVault/vaults/keyvault-cmk"))))
            .withSecuritySettings(
                new CacheSecuritySettings().withAccessPolicies(Arrays.asList(new NfsAccessPolicy().withName("default")
                    .withAccessRules(Arrays.asList(new NfsAccessRule().withScope(NfsAccessRuleScope.DEFAULT)
                        .withAccess(NfsAccessRuleAccess.RW)
                        .withSuid(false)
                        .withSubmountAccess(true)
                        .withRootSquash(false))))))
            .withDirectoryServicesSettings(new CacheDirectorySettings()
                .withUsernameDownload(new CacheUsernameDownloadSettings().withExtendedGroups(true)
                    .withUsernameSource(UsernameSource.LDAP)
                    .withLdapServer("192.0.2.12")
                    .withLdapBaseDN("dc=contosoad,dc=contoso,dc=local")
                    .withCredentials(new CacheUsernameDownloadSettingsCredentials()
                        .withBindDn("cn=ldapadmin,dc=contosoad,dc=contoso,dc=local")
                        .withBindPassword("fakeTokenPlaceholder"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_CreateOrUpdate.json
     */
    /**
     * Sample code: Caches_CreateOrUpdate.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesCreateOrUpdate(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches()
            .define("sc1")
            .withRegion("westus")
            .withExistingResourceGroup("scgroup")
            .withTags(mapOf("Dept", "Contoso"))
            .withIdentity(new CacheIdentity().withType(CacheIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity1",
                    new UserAssignedIdentitiesValueAutoGenerated())))
            .withSku(new CacheSku().withName("Standard_2G"))
            .withCacheSizeGB(3072)
            .withSubnet(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.Network/virtualNetworks/scvnet/subnets/sub1")
            .withUpgradeSettings(new CacheUpgradeSettings().withUpgradeScheduleEnabled(true)
                .withScheduledTime(OffsetDateTime.parse("2022-04-26T18:25:43.511Z")))
            .withEncryptionSettings(new CacheEncryptionSettings().withKeyEncryptionKey(new KeyVaultKeyReference()
                .withKeyUrl("fakeTokenPlaceholder")
                .withSourceVault(new KeyVaultKeyReferenceSourceVault().withId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.KeyVault/vaults/keyvault-cmk"))))
            .withSecuritySettings(
                new CacheSecuritySettings().withAccessPolicies(Arrays.asList(new NfsAccessPolicy().withName("default")
                    .withAccessRules(Arrays.asList(new NfsAccessRule().withScope(NfsAccessRuleScope.DEFAULT)
                        .withAccess(NfsAccessRuleAccess.RW)
                        .withSuid(false)
                        .withSubmountAccess(true)
                        .withRootSquash(false))))))
            .withDirectoryServicesSettings(new CacheDirectorySettings()
                .withActiveDirectory(new CacheActiveDirectorySettings().withPrimaryDnsIpAddress("192.0.2.10")
                    .withSecondaryDnsIpAddress("192.0.2.11")
                    .withDomainName("contosoAd.contoso.local")
                    .withDomainNetBiosName("contosoAd")
                    .withCacheNetBiosName("contosoSmb")
                    .withCredentials(new CacheActiveDirectorySettingsCredentials().withUsername("consotoAdmin")
                        .withPassword("fakeTokenPlaceholder")))
                .withUsernameDownload(new CacheUsernameDownloadSettings().withExtendedGroups(true)
                    .withUsernameSource(UsernameSource.LDAP)
                    .withLdapServer("192.0.2.12")
                    .withLdapBaseDN("dc=contosoad,dc=contoso,dc=local")
                    .withCredentials(new CacheUsernameDownloadSettingsCredentials()
                        .withBindDn("cn=ldapadmin,dc=contosoad,dc=contoso,dc=local")
                        .withBindPassword("fakeTokenPlaceholder"))))
            .create();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Caches_DebugInfo

```java
/**
 * Samples for Caches DebugInfo.
 */
public final class CachesDebugInfoSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_DebugInfo.json
     */
    /**
     * Sample code: Caches_DebugInfo.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesDebugInfo(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches().debugInfo("scgroup", "sc", com.azure.core.util.Context.NONE);
    }
}
```

### Caches_Delete

```java
/**
 * Samples for Caches Delete.
 */
public final class CachesDeleteSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_Delete.json
     */
    /**
     * Sample code: Caches_Delete.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesDelete(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches().delete("scgroup", "sc", com.azure.core.util.Context.NONE);
    }
}
```

### Caches_Flush

```java
/**
 * Samples for Caches Flush.
 */
public final class CachesFlushSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_Flush.json
     */
    /**
     * Sample code: Caches_Flush.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesFlush(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches().flush("scgroup", "sc", com.azure.core.util.Context.NONE);
    }
}
```

### Caches_GetByResourceGroup

```java
/**
 * Samples for Caches GetByResourceGroup.
 */
public final class CachesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_Get.json
     */
    /**
     * Sample code: Caches_Get.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesGet(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches().getByResourceGroupWithResponse("scgroup", "sc1", com.azure.core.util.Context.NONE);
    }
}
```

### Caches_List

```java
/**
 * Samples for Caches List.
 */
public final class CachesListSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_List.json
     */
    /**
     * Sample code: Caches_List.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesList(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches().list(com.azure.core.util.Context.NONE);
    }
}
```

### Caches_ListByResourceGroup

```java
/**
 * Samples for Caches ListByResourceGroup.
 */
public final class CachesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_ListByResourceGroup.json
     */
    /**
     * Sample code: Caches_ListByResourceGroup.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesListByResourceGroup(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches().listByResourceGroup("scgroup", com.azure.core.util.Context.NONE);
    }
}
```

### Caches_PausePrimingJob

```java
import com.azure.resourcemanager.storagecache.models.PrimingJobIdParameter;

/**
 * Samples for Caches PausePrimingJob.
 */
public final class CachesPausePrimingJobSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/PausePrimingJob.json
     */
    /**
     * Sample code: PausePrimingJob.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void pausePrimingJob(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches()
            .pausePrimingJob("scgroup", "sc1", new PrimingJobIdParameter().withPrimingJobId("00000000000_0000000000"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Caches_ResumePrimingJob

```java
import com.azure.resourcemanager.storagecache.models.PrimingJobIdParameter;

/**
 * Samples for Caches ResumePrimingJob.
 */
public final class CachesResumePrimingJobSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/ResumePrimingJob.json
     */
    /**
     * Sample code: ResumePrimingJob.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void resumePrimingJob(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches()
            .resumePrimingJob("scgroup", "sc1", new PrimingJobIdParameter().withPrimingJobId("00000000000_0000000000"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Caches_SpaceAllocation

```java
import com.azure.resourcemanager.storagecache.models.StorageTargetSpaceAllocation;
import java.util.Arrays;

/**
 * Samples for Caches SpaceAllocation.
 */
public final class CachesSpaceAllocationSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/SpaceAllocation_Post.json
     */
    /**
     * Sample code: SpaceAllocation_Post.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void spaceAllocationPost(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches()
            .spaceAllocation("scgroup", "sc1",
                Arrays.asList(new StorageTargetSpaceAllocation().withName("st1").withAllocationPercentage(25),
                    new StorageTargetSpaceAllocation().withName("st2").withAllocationPercentage(50),
                    new StorageTargetSpaceAllocation().withName("st3").withAllocationPercentage(25)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Caches_Start

```java
/**
 * Samples for Caches Start.
 */
public final class CachesStartSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_Start.json
     */
    /**
     * Sample code: Caches_Start.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesStart(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches().start("scgroup", "sc", com.azure.core.util.Context.NONE);
    }
}
```

### Caches_StartPrimingJob

```java
import com.azure.resourcemanager.storagecache.models.PrimingJob;

/**
 * Samples for Caches StartPrimingJob.
 */
public final class CachesStartPrimingJobSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StartPrimingJob.json
     */
    /**
     * Sample code: StartPrimingJob.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void startPrimingJob(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches()
            .startPrimingJob("scgroup", "sc1", new PrimingJob().withPrimingJobName("contosoJob")
                .withPrimingManifestUrl(
                    "https://contosostorage.blob.core.windows.net/contosoblob/00000000_00000000000000000000000000000000.00000000000.FFFFFFFF.00000000?sp=r&st=2021-08-11T19:33:35Z&se=2021-08-12T03:33:35Z&spr=https&sv=2020-08-04&sr=b&sig=<secret-value-from-key>"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Caches_Stop

```java
/**
 * Samples for Caches Stop.
 */
public final class CachesStopSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_Stop.json
     */
    /**
     * Sample code: Caches_Stop.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesStop(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches().stop("scgroup", "sc", com.azure.core.util.Context.NONE);
    }
}
```

### Caches_StopPrimingJob

```java
import com.azure.resourcemanager.storagecache.models.PrimingJobIdParameter;

/**
 * Samples for Caches StopPrimingJob.
 */
public final class CachesStopPrimingJobSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StopPrimingJob.json
     */
    /**
     * Sample code: StopPrimingJob.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void stopPrimingJob(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches()
            .stopPrimingJob("scgroup", "sc1", new PrimingJobIdParameter().withPrimingJobId("00000000000_0000000000"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Caches_Update

```java
import com.azure.resourcemanager.storagecache.models.Cache;
import com.azure.resourcemanager.storagecache.models.CacheActiveDirectorySettings;
import com.azure.resourcemanager.storagecache.models.CacheDirectorySettings;
import com.azure.resourcemanager.storagecache.models.CacheNetworkSettings;
import com.azure.resourcemanager.storagecache.models.CacheSecuritySettings;
import com.azure.resourcemanager.storagecache.models.CacheUpgradeSettings;
import com.azure.resourcemanager.storagecache.models.CacheUsernameDownloadSettings;
import com.azure.resourcemanager.storagecache.models.CacheUsernameDownloadSettingsCredentials;
import com.azure.resourcemanager.storagecache.models.NfsAccessPolicy;
import com.azure.resourcemanager.storagecache.models.NfsAccessRule;
import com.azure.resourcemanager.storagecache.models.NfsAccessRuleAccess;
import com.azure.resourcemanager.storagecache.models.NfsAccessRuleScope;
import com.azure.resourcemanager.storagecache.models.UsernameSource;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Caches Update.
 */
public final class CachesUpdateSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_Update_ldap_only.json
     */
    /**
     * Sample code: Caches_Update_ldap_only.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesUpdateLdapOnly(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        Cache resource = manager.caches()
            .getByResourceGroupWithResponse("scgroup", "sc1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Dept", "Contoso"))
            .withUpgradeSettings(new CacheUpgradeSettings().withUpgradeScheduleEnabled(true)
                .withScheduledTime(OffsetDateTime.parse("2022-04-26T18:25:43.511Z")))
            .withNetworkSettings(new CacheNetworkSettings().withMtu(1500)
                .withDnsServers(Arrays.asList("10.1.22.33", "10.1.12.33"))
                .withDnsSearchDomain("contoso.com")
                .withNtpServer("time.contoso.com"))
            .withSecuritySettings(new CacheSecuritySettings().withAccessPolicies(Arrays.asList(
                new NfsAccessPolicy().withName("default")
                    .withAccessRules(Arrays.asList(new NfsAccessRule().withScope(NfsAccessRuleScope.DEFAULT)
                        .withAccess(NfsAccessRuleAccess.RW)
                        .withSuid(false)
                        .withSubmountAccess(true)
                        .withRootSquash(false))),
                new NfsAccessPolicy().withName("restrictive")
                    .withAccessRules(Arrays.asList(
                        new NfsAccessRule().withScope(NfsAccessRuleScope.HOST)
                            .withFilter("10.99.3.145")
                            .withAccess(NfsAccessRuleAccess.RW)
                            .withSuid(true)
                            .withSubmountAccess(true)
                            .withRootSquash(false),
                        new NfsAccessRule().withScope(NfsAccessRuleScope.NETWORK)
                            .withFilter("10.99.1.0/24")
                            .withAccess(NfsAccessRuleAccess.RW)
                            .withSuid(true)
                            .withSubmountAccess(true)
                            .withRootSquash(false),
                        new NfsAccessRule().withScope(NfsAccessRuleScope.DEFAULT)
                            .withAccess(NfsAccessRuleAccess.NO)
                            .withSuid(false)
                            .withSubmountAccess(true)
                            .withRootSquash(true)
                            .withAnonymousUid("65534")
                            .withAnonymousGid("65534"))))))
            .withDirectoryServicesSettings(new CacheDirectorySettings()
                .withUsernameDownload(new CacheUsernameDownloadSettings().withExtendedGroups(true)
                    .withUsernameSource(UsernameSource.LDAP)
                    .withLdapServer("192.0.2.12")
                    .withLdapBaseDN("dc=contosoad,dc=contoso,dc=local")
                    .withCredentials(new CacheUsernameDownloadSettingsCredentials()
                        .withBindDn("cn=ldapadmin,dc=contosoad,dc=contoso,dc=local")
                        .withBindPassword("fakeTokenPlaceholder"))))
            .apply();
    }

    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_Update.json
     */
    /**
     * Sample code: Caches_Update.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesUpdate(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        Cache resource = manager.caches()
            .getByResourceGroupWithResponse("scgroup", "sc1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Dept", "Contoso"))
            .withUpgradeSettings(new CacheUpgradeSettings().withUpgradeScheduleEnabled(true)
                .withScheduledTime(OffsetDateTime.parse("2022-04-26T18:25:43.511Z")))
            .withNetworkSettings(new CacheNetworkSettings().withMtu(1500)
                .withDnsServers(Arrays.asList("10.1.22.33", "10.1.12.33"))
                .withDnsSearchDomain("contoso.com")
                .withNtpServer("time.contoso.com"))
            .withSecuritySettings(new CacheSecuritySettings().withAccessPolicies(Arrays.asList(
                new NfsAccessPolicy().withName("default")
                    .withAccessRules(Arrays.asList(new NfsAccessRule().withScope(NfsAccessRuleScope.DEFAULT)
                        .withAccess(NfsAccessRuleAccess.RW)
                        .withSuid(false)
                        .withSubmountAccess(true)
                        .withRootSquash(false))),
                new NfsAccessPolicy().withName("restrictive")
                    .withAccessRules(Arrays.asList(
                        new NfsAccessRule().withScope(NfsAccessRuleScope.HOST)
                            .withFilter("10.99.3.145")
                            .withAccess(NfsAccessRuleAccess.RW)
                            .withSuid(true)
                            .withSubmountAccess(true)
                            .withRootSquash(false),
                        new NfsAccessRule().withScope(NfsAccessRuleScope.NETWORK)
                            .withFilter("10.99.1.0/24")
                            .withAccess(NfsAccessRuleAccess.RW)
                            .withSuid(true)
                            .withSubmountAccess(true)
                            .withRootSquash(false),
                        new NfsAccessRule().withScope(NfsAccessRuleScope.DEFAULT)
                            .withAccess(NfsAccessRuleAccess.NO)
                            .withSuid(false)
                            .withSubmountAccess(true)
                            .withRootSquash(true)
                            .withAnonymousUid("65534")
                            .withAnonymousGid("65534"))))))
            .withDirectoryServicesSettings(new CacheDirectorySettings()
                .withActiveDirectory(new CacheActiveDirectorySettings().withPrimaryDnsIpAddress("192.0.2.10")
                    .withSecondaryDnsIpAddress("192.0.2.11")
                    .withDomainName("contosoAd.contoso.local")
                    .withDomainNetBiosName("contosoAd")
                    .withCacheNetBiosName("contosoSmb"))
                .withUsernameDownload(
                    new CacheUsernameDownloadSettings().withExtendedGroups(true).withUsernameSource(UsernameSource.AD)))
            .apply();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Caches_UpgradeFirmware

```java
/**
 * Samples for Caches UpgradeFirmware.
 */
public final class CachesUpgradeFirmwareSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Caches_UpgradeFirmware.json
     */
    /**
     * Sample code: Caches_UpgradeFirmware.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesUpgradeFirmware(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.caches().upgradeFirmware("scgroup", "sc1", com.azure.core.util.Context.NONE);
    }
}
```

### ImportJobs_CreateOrUpdate

```java
import com.azure.resourcemanager.storagecache.models.ConflictResolutionMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ImportJobs CreateOrUpdate.
 */
public final class ImportJobsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/importJobs_CreateOrUpdate.json
     */
    /**
     * Sample code: importJobs_CreateOrUpdate.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void importJobsCreateOrUpdate(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.importJobs()
            .define("job1")
            .withRegion("eastus")
            .withExistingAmlFilesystem("scgroup", "fs1")
            .withTags(mapOf("Dept", "ContosoAds"))
            .withImportPrefixes(Arrays.asList("/"))
            .withConflictResolutionMode(ConflictResolutionMode.OVERWRITE_ALWAYS)
            .withMaximumErrors(0)
            .create();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### ImportJobs_Delete

```java
/**
 * Samples for ImportJobs Delete.
 */
public final class ImportJobsDeleteSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/importJobs_Delete.json
     */
    /**
     * Sample code: importJobs_Delete.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void importJobsDelete(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.importJobs().delete("scgroup", "fs1", "job1", com.azure.core.util.Context.NONE);
    }
}
```

### ImportJobs_Get

```java
/**
 * Samples for ImportJobs Get.
 */
public final class ImportJobsGetSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/importJobs_Get.json
     */
    /**
     * Sample code: importJobs_Get.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void importJobsGet(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.importJobs().getWithResponse("scgroup", "fs1", "job1", com.azure.core.util.Context.NONE);
    }
}
```

### ImportJobs_ListByAmlFilesystem

```java
/**
 * Samples for ImportJobs ListByAmlFilesystem.
 */
public final class ImportJobsListByAmlFilesystemSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/importJobs_ListByAmlFilesystem.json
     */
    /**
     * Sample code: importJobs_ListByAmlFilesystem.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void
        importJobsListByAmlFilesystem(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.importJobs().listByAmlFilesystem("scgroup", "fs1", com.azure.core.util.Context.NONE);
    }
}
```

### ImportJobs_Update

```java
import com.azure.resourcemanager.storagecache.models.ImportJob;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ImportJobs Update.
 */
public final class ImportJobsUpdateSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/importJob_Update.json
     */
    /**
     * Sample code: importJobs_Update.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void importJobsUpdate(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        ImportJob resource = manager.importJobs()
            .getWithResponse("scgroup", "fs1", "job1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Dept", "ContosoAds")).apply();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void operationsList(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_CheckAmlFSSubnets

```java
import com.azure.resourcemanager.storagecache.models.AmlFilesystemSubnetInfo;
import com.azure.resourcemanager.storagecache.models.SkuName;

/**
 * Samples for ResourceProvider CheckAmlFSSubnets.
 */
public final class ResourceProviderCheckAmlFSSubnetsSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/checkAmlFSSubnets.json
     */
    /**
     * Sample code: checkAmlFSSubnets.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void checkAmlFSSubnets(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.resourceProviders()
            .checkAmlFSSubnetsWithResponse(new AmlFilesystemSubnetInfo().withFilesystemSubnet(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.Network/virtualNetworks/scvnet/subnets/fsSub")
                .withStorageCapacityTiB(16.0F)
                .withSku(new SkuName().withName("AMLFS-Durable-Premium-125")), com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetRequiredAmlFSSubnetsSize

```java

/**
 * Samples for ResourceProvider GetRequiredAmlFSSubnetsSize.
 */
public final class ResourceProviderGetRequiredAmlFSSubnetsSizeSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/getRequiredAmlFSSubnetsSize.json
     */
    /**
     * Sample code: getRequiredAmlFilesystemSubnetsSize.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void
        getRequiredAmlFilesystemSubnetsSize(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.resourceProviders().getRequiredAmlFSSubnetsSizeWithResponse(null, com.azure.core.util.Context.NONE);
    }
}
```

### Skus_List

```java
/**
 * Samples for Skus List.
 */
public final class SkusListSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/Skus_List.json
     */
    /**
     * Sample code: Skus_List.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void skusList(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.skus().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageTargetOperation_Flush

```java
/**
 * Samples for StorageTargetOperation Flush.
 */
public final class StorageTargetOperationFlushSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_Flush.json
     */
    /**
     * Sample code: StorageTargets_Flush.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void storageTargetsFlush(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargetOperations().flush("scgroup", "sc", "st1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTargetOperation_Invalidate

```java
/**
 * Samples for StorageTargetOperation Invalidate.
 */
public final class StorageTargetOperationInvalidateSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_Invalidate.json
     */
    /**
     * Sample code: StorageTargets_Invalidate.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void storageTargetsInvalidate(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargetOperations().invalidate("scgroup", "sc", "st1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTargetOperation_Resume

```java
/**
 * Samples for StorageTargetOperation Resume.
 */
public final class StorageTargetOperationResumeSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_Resume.json
     */
    /**
     * Sample code: StorageTargets_Resume.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void storageTargetsResume(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargetOperations().resume("scgroup", "sc", "st1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTargetOperation_Suspend

```java
/**
 * Samples for StorageTargetOperation Suspend.
 */
public final class StorageTargetOperationSuspendSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_Suspend.json
     */
    /**
     * Sample code: StorageTargets_Suspend.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void storageTargetsSuspend(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargetOperations().suspend("scgroup", "sc", "st1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTargets_CreateOrUpdate

```java
import com.azure.resourcemanager.storagecache.models.BlobNfsTarget;
import com.azure.resourcemanager.storagecache.models.NamespaceJunction;
import com.azure.resourcemanager.storagecache.models.Nfs3Target;
import com.azure.resourcemanager.storagecache.models.StorageTargetType;
import java.util.Arrays;

/**
 * Samples for StorageTargets CreateOrUpdate.
 */
public final class StorageTargetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_CreateOrUpdate.json
     */
    /**
     * Sample code: StorageTargets_CreateOrUpdate.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void
        storageTargetsCreateOrUpdate(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargets()
            .define("st1")
            .withExistingCache("scgroup", "sc1")
            .withJunctions(Arrays.asList(
                new NamespaceJunction().withNamespacePath("/path/on/cache")
                    .withTargetPath("/path/on/exp1")
                    .withNfsExport("exp1")
                    .withNfsAccessPolicy("default"),
                new NamespaceJunction().withNamespacePath("/path2/on/cache")
                    .withTargetPath("/path2/on/exp2")
                    .withNfsExport("exp2")
                    .withNfsAccessPolicy("rootSquash")))
            .withTargetType(StorageTargetType.NFS3)
            .withNfs3(new Nfs3Target().withTarget("10.0.44.44").withUsageModel("READ_ONLY").withVerificationTimer(30))
            .create();
    }

    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_CreateOrUpdate_BlobNfs.json
     */
    /**
     * Sample code: StorageTargets_CreateOrUpdate_BlobNfs.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void
        storageTargetsCreateOrUpdateBlobNfs(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargets()
            .define("st1")
            .withExistingCache("scgroup", "sc1")
            .withJunctions(Arrays.asList(new NamespaceJunction().withNamespacePath("/blobnfs")))
            .withTargetType(StorageTargetType.BLOB_NFS)
            .withBlobNfs(new BlobNfsTarget().withTarget(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/scgroup/providers/Microsoft.Storage/storageAccounts/blofnfs/blobServices/default/containers/blobnfs")
                .withUsageModel("READ_WRITE")
                .withVerificationTimer(28800)
                .withWriteBackTimer(3600))
            .create();
    }

    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_CreateOrUpdate_NoJunctions.json
     */
    /**
     * Sample code: StorageTargets_CreateOrUpdate_NoJunctions.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void
        storageTargetsCreateOrUpdateNoJunctions(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargets()
            .define("st1")
            .withExistingCache("scgroup", "sc1")
            .withTargetType(StorageTargetType.NFS3)
            .withNfs3(new Nfs3Target().withTarget("10.0.44.44").withUsageModel("READ_ONLY").withVerificationTimer(30))
            .create();
    }
}
```

### StorageTargets_Delete

```java
/**
 * Samples for StorageTargets Delete.
 */
public final class StorageTargetsDeleteSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_Delete.json
     */
    /**
     * Sample code: StorageTargets_Delete.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void storageTargetsDelete(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargets().delete("scgroup", "sc1", "st1", null, com.azure.core.util.Context.NONE);
    }
}
```

### StorageTargets_DnsRefresh

```java
/**
 * Samples for StorageTargets DnsRefresh.
 */
public final class StorageTargetsDnsRefreshSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_DnsRefresh.json
     */
    /**
     * Sample code: Caches_DnsRefresh.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void cachesDnsRefresh(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargets().dnsRefresh("scgroup", "sc", "st1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTargets_Get

```java
/**
 * Samples for StorageTargets Get.
 */
public final class StorageTargetsGetSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_Get.json
     */
    /**
     * Sample code: StorageTargets_Get.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void storageTargetsGet(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargets().getWithResponse("scgroup", "sc1", "st1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTargets_ListByCache

```java
/**
 * Samples for StorageTargets ListByCache.
 */
public final class StorageTargetsListByCacheSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_ListByCache.json
     */
    /**
     * Sample code: StorageTargets_List.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void storageTargetsList(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargets().listByCache("scgroup", "sc1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTargets_RestoreDefaults

```java
/**
 * Samples for StorageTargets RestoreDefaults.
 */
public final class StorageTargetsRestoreDefaultsSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/StorageTargets_RestoreDefaults.json
     */
    /**
     * Sample code: StorageTargets_RestoreDefaults.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void
        storageTargetsRestoreDefaults(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.storageTargets().restoreDefaults("scgroup", "sc", "st1", com.azure.core.util.Context.NONE);
    }
}
```

### UsageModels_List

```java
/**
 * Samples for UsageModels List.
 */
public final class UsageModelsListSamples {
    /*
     * x-ms-original-file: specification/storagecache/resource-manager/Microsoft.StorageCache/stable/2024-03-01/examples/UsageModels_List.json
     */
    /**
     * Sample code: UsageModels_List.
     * 
     * @param manager Entry point to StorageCacheManager.
     */
    public static void usageModelsList(com.azure.resourcemanager.storagecache.StorageCacheManager manager) {
        manager.usageModels().list(com.azure.core.util.Context.NONE);
    }
}
```

