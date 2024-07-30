# Code snippets and samples


## Accounts

- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [RenewCredentials](#accounts_renewcredentials)
- [Update](#accounts_update)

## BackupPolicies

- [Create](#backuppolicies_create)
- [Delete](#backuppolicies_delete)
- [Get](#backuppolicies_get)
- [List](#backuppolicies_list)
- [Update](#backuppolicies_update)

## BackupVaults

- [CreateOrUpdate](#backupvaults_createorupdate)
- [Delete](#backupvaults_delete)
- [Get](#backupvaults_get)
- [ListByNetAppAccount](#backupvaults_listbynetappaccount)
- [Update](#backupvaults_update)

## Backups

- [Create](#backups_create)
- [Delete](#backups_delete)
- [Get](#backups_get)
- [GetLatestStatus](#backups_getlateststatus)
- [GetVolumeLatestRestoreStatus](#backups_getvolumelatestrestorestatus)
- [ListByVault](#backups_listbyvault)
- [Update](#backups_update)

## BackupsUnderAccount

- [MigrateBackups](#backupsunderaccount_migratebackups)

## BackupsUnderBackupVault

- [RestoreFiles](#backupsunderbackupvault_restorefiles)

## BackupsUnderVolume

- [MigrateBackups](#backupsundervolume_migratebackups)

## NetAppResource

- [CheckFilePathAvailability](#netappresource_checkfilepathavailability)
- [CheckNameAvailability](#netappresource_checknameavailability)
- [CheckQuotaAvailability](#netappresource_checkquotaavailability)
- [QueryNetworkSiblingSet](#netappresource_querynetworksiblingset)
- [QueryRegionInfo](#netappresource_queryregioninfo)
- [UpdateNetworkSiblingSet](#netappresource_updatenetworksiblingset)

## NetAppResourceQuotaLimits

- [Get](#netappresourcequotalimits_get)
- [List](#netappresourcequotalimits_list)

## NetAppResourceRegionInfos

- [Get](#netappresourceregioninfos_get)
- [List](#netappresourceregioninfos_list)

## Operations

- [List](#operations_list)

## Pools

- [CreateOrUpdate](#pools_createorupdate)
- [Delete](#pools_delete)
- [Get](#pools_get)
- [List](#pools_list)
- [Update](#pools_update)

## SnapshotPolicies

- [Create](#snapshotpolicies_create)
- [Delete](#snapshotpolicies_delete)
- [Get](#snapshotpolicies_get)
- [List](#snapshotpolicies_list)
- [ListVolumes](#snapshotpolicies_listvolumes)
- [Update](#snapshotpolicies_update)

## Snapshots

- [Create](#snapshots_create)
- [Delete](#snapshots_delete)
- [Get](#snapshots_get)
- [List](#snapshots_list)
- [RestoreFiles](#snapshots_restorefiles)
- [Update](#snapshots_update)

## Subvolumes

- [Create](#subvolumes_create)
- [Delete](#subvolumes_delete)
- [Get](#subvolumes_get)
- [GetMetadata](#subvolumes_getmetadata)
- [ListByVolume](#subvolumes_listbyvolume)
- [Update](#subvolumes_update)

## VolumeGroups

- [Create](#volumegroups_create)
- [Delete](#volumegroups_delete)
- [Get](#volumegroups_get)
- [ListByNetAppAccount](#volumegroups_listbynetappaccount)

## VolumeQuotaRules

- [Create](#volumequotarules_create)
- [Delete](#volumequotarules_delete)
- [Get](#volumequotarules_get)
- [ListByVolume](#volumequotarules_listbyvolume)
- [Update](#volumequotarules_update)

## Volumes

- [AuthorizeReplication](#volumes_authorizereplication)
- [BreakFileLocks](#volumes_breakfilelocks)
- [BreakReplication](#volumes_breakreplication)
- [CreateOrUpdate](#volumes_createorupdate)
- [Delete](#volumes_delete)
- [DeleteReplication](#volumes_deletereplication)
- [FinalizeRelocation](#volumes_finalizerelocation)
- [Get](#volumes_get)
- [List](#volumes_list)
- [ListGetGroupIdListForLdapUser](#volumes_listgetgroupidlistforldapuser)
- [ListReplications](#volumes_listreplications)
- [PoolChange](#volumes_poolchange)
- [PopulateAvailabilityZone](#volumes_populateavailabilityzone)
- [ReInitializeReplication](#volumes_reinitializereplication)
- [ReestablishReplication](#volumes_reestablishreplication)
- [Relocate](#volumes_relocate)
- [ReplicationStatus](#volumes_replicationstatus)
- [ResetCifsPassword](#volumes_resetcifspassword)
- [ResyncReplication](#volumes_resyncreplication)
- [Revert](#volumes_revert)
- [RevertRelocation](#volumes_revertrelocation)
- [Update](#volumes_update)
### Accounts_CreateOrUpdate

```java
import com.azure.resourcemanager.netapp.models.ActiveDirectory;
import java.util.Arrays;

/**
 * Samples for Accounts CreateOrUpdate.
 */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Accounts_CreateOrUpdate.json
     */
    /**
     * Sample code: Accounts_CreateOrUpdate.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsCreateOrUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().define("account1").withRegion("eastus").withExistingResourceGroup("myRG").create();
    }

    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Accounts_CreateOrUpdateAD.json
     */
    /**
     * Sample code: Accounts_CreateOrUpdateWithActiveDirectory.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void
        accountsCreateOrUpdateWithActiveDirectory(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts()
            .define("account1")
            .withRegion("eastus")
            .withExistingResourceGroup("myRG")
            .withActiveDirectories(Arrays.asList(new ActiveDirectory().withUsername("ad_user_name")
                .withPassword("fakeTokenPlaceholder")
                .withDomain("10.10.10.3")
                .withDns("10.10.10.3")
                .withSmbServerName("SMBServer")
                .withOrganizationalUnit("OU=Engineering")
                .withSite("SiteName")
                .withAesEncryption(true)
                .withLdapSigning(false)
                .withLdapOverTls(false)))
            .create();
    }
}
```

### Accounts_Delete

```java
/**
 * Samples for Accounts Delete.
 */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Accounts_Delete.json
     */
    /**
     * Sample code: Accounts_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().delete("myRG", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
/**
 * Samples for Accounts GetByResourceGroup.
 */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Accounts_Get.json
     */
    /**
     * Sample code: Accounts_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().getByResourceGroupWithResponse("myRG", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_List

```java
/**
 * Samples for Accounts List.
 */
public final class AccountsListSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Accounts_List.json
     */
    /**
     * Sample code: Accounts_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
/**
 * Samples for Accounts ListByResourceGroup.
 */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Accounts_List.json
     */
    /**
     * Sample code: Accounts_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().listByResourceGroup("myRG", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_RenewCredentials

```java
/**
 * Samples for Accounts RenewCredentials.
 */
public final class AccountsRenewCredentialsSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Accounts_RenewCredentials.json
     */
    /**
     * Sample code: Accounts_RenewCredentials.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsRenewCredentials(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().renewCredentials("myRG", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.resourcemanager.netapp.models.NetAppAccount;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Accounts Update.
 */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Accounts_Update.json
     */
    /**
     * Sample code: Accounts_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        NetAppAccount resource = manager.accounts()
            .getByResourceGroupWithResponse("myRG", "account1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Tag1", "Value1")).apply();
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

### BackupPolicies_Create

```java
/**
 * Samples for BackupPolicies Create.
 */
public final class BackupPoliciesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupPolicies_Create.json
     */
    /**
     * Sample code: BackupPolicies_Create.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupPoliciesCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupPolicies()
            .define("backupPolicyName")
            .withRegion("westus")
            .withExistingNetAppAccount("myRG", "account1")
            .withDailyBackupsToKeep(10)
            .withWeeklyBackupsToKeep(10)
            .withMonthlyBackupsToKeep(10)
            .withEnabled(true)
            .create();
    }
}
```

### BackupPolicies_Delete

```java
/**
 * Samples for BackupPolicies Delete.
 */
public final class BackupPoliciesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupPolicies_Delete.json
     */
    /**
     * Sample code: BackupPolicies_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupPoliciesDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupPolicies()
            .delete("resourceGroup", "accountName", "backupPolicyName", com.azure.core.util.Context.NONE);
    }
}
```

### BackupPolicies_Get

```java
/**
 * Samples for BackupPolicies Get.
 */
public final class BackupPoliciesGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupPolicies_Get.json
     */
    /**
     * Sample code: Backups_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupPolicies()
            .getWithResponse("myRG", "account1", "backupPolicyName", com.azure.core.util.Context.NONE);
    }
}
```

### BackupPolicies_List

```java
/**
 * Samples for BackupPolicies List.
 */
public final class BackupPoliciesListSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupPolicies_List.json
     */
    /**
     * Sample code: BackupPolicies_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupPoliciesList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupPolicies().list("myRG", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### BackupPolicies_Update

```java
import com.azure.resourcemanager.netapp.models.BackupPolicy;

/**
 * Samples for BackupPolicies Update.
 */
public final class BackupPoliciesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupPolicies_Update.json
     */
    /**
     * Sample code: BackupPolicies_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupPoliciesUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        BackupPolicy resource = manager.backupPolicies()
            .getWithResponse("myRG", "account1", "backupPolicyName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withDailyBackupsToKeep(5)
            .withWeeklyBackupsToKeep(10)
            .withMonthlyBackupsToKeep(10)
            .withEnabled(false)
            .apply();
    }
}
```

### BackupVaults_CreateOrUpdate

```java
/**
 * Samples for BackupVaults CreateOrUpdate.
 */
public final class BackupVaultsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupVaults_Create.json
     */
    /**
     * Sample code: BackupVault_CreateOrUpdate.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupVaultCreateOrUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupVaults()
            .define("backupVault1")
            .withRegion("eastus")
            .withExistingNetAppAccount("myRG", "account1")
            .create();
    }
}
```

### BackupVaults_Delete

```java
/**
 * Samples for BackupVaults Delete.
 */
public final class BackupVaultsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupVaults_Delete.json
     */
    /**
     * Sample code: BackupVaults_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupVaultsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupVaults().delete("resourceGroup", "account1", "backupVault1", com.azure.core.util.Context.NONE);
    }
}
```

### BackupVaults_Get

```java
/**
 * Samples for BackupVaults Get.
 */
public final class BackupVaultsGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupVaults_Get.json
     */
    /**
     * Sample code: BackupVaults_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupVaultsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupVaults().getWithResponse("myRG", "account1", "backupVault1", com.azure.core.util.Context.NONE);
    }
}
```

### BackupVaults_ListByNetAppAccount

```java
/**
 * Samples for BackupVaults ListByNetAppAccount.
 */
public final class BackupVaultsListByNetAppAccountSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupVaults_List.json
     */
    /**
     * Sample code: BackupVaults_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupVaultsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupVaults().listByNetAppAccount("myRG", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### BackupVaults_Update

```java
import com.azure.resourcemanager.netapp.models.BackupVault;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for BackupVaults Update.
 */
public final class BackupVaultsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupVaults_Update.json
     */
    /**
     * Sample code: BackupVaults_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupVaultsUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        BackupVault resource = manager.backupVaults()
            .getWithResponse("myRG", "account1", "backupVault1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Tag1", "Value1")).apply();
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

### Backups_Create

```java
/**
 * Samples for Backups Create.
 */
public final class BackupsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupsUnderBackupVault_Create.
     * json
     */
    /**
     * Sample code: BackupsUnderBackupVault_Create.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsUnderBackupVaultCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups()
            .define("backup1")
            .withExistingBackupVault("myRG", "account1", "backupVault1")
            .withVolumeResourceId(
                "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPool/pool1/volumes/volume1")
            .withLabel("myLabel")
            .create();
    }
}
```

### Backups_Delete

```java
/**
 * Samples for Backups Delete.
 */
public final class BackupsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupsUnderBackupVault_Delete.
     * json
     */
    /**
     * Sample code: BackupsUnderBackupVault_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsUnderBackupVaultDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups()
            .delete("resourceGroup", "account1", "backupVault1", "backup1", com.azure.core.util.Context.NONE);
    }
}
```

### Backups_Get

```java
/**
 * Samples for Backups Get.
 */
public final class BackupsGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupsUnderBackupVault_Get.
     * json
     */
    /**
     * Sample code: BackupsUnderBackupVault_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsUnderBackupVaultGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups()
            .getWithResponse("myRG", "account1", "backupVault1", "backup1", com.azure.core.util.Context.NONE);
    }
}
```

### Backups_GetLatestStatus

```java
/**
 * Samples for Backups GetLatestStatus.
 */
public final class BackupsGetLatestStatusSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_LatestBackupStatus.json
     */
    /**
     * Sample code: Volumes_BackupStatus.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesBackupStatus(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups()
            .getLatestStatusWithResponse("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Backups_GetVolumeLatestRestoreStatus

```java
/**
 * Samples for Backups GetVolumeLatestRestoreStatus.
 */
public final class BackupsGetVolumeLatestRestoreStatusSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_LatestRestoreStatus.
     * json
     */
    /**
     * Sample code: Volumes_RestoreStatus.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesRestoreStatus(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups()
            .getVolumeLatestRestoreStatusWithResponse("myRG", "account1", "pool1", "volume1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Backups_ListByVault

```java
/**
 * Samples for Backups ListByVault.
 */
public final class BackupsListByVaultSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupsUnderBackupVault_List.
     * json
     */
    /**
     * Sample code: Backups_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups().listByVault("myRG", "account1", "backupVault1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Backups_Update

```java
import com.azure.resourcemanager.netapp.models.Backup;

/**
 * Samples for Backups Update.
 */
public final class BackupsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupsUnderBackupVault_Update.
     * json
     */
    /**
     * Sample code: BackupsUnderBackupVault_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsUnderBackupVaultUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        Backup resource = manager.backups()
            .getWithResponse("myRG", "account1", "backupVault1", "backup1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### BackupsUnderAccount_MigrateBackups

```java
import com.azure.resourcemanager.netapp.models.BackupsMigrationRequest;

/**
 * Samples for BackupsUnderAccount MigrateBackups.
 */
public final class BackupsUnderAccountMigrateBackupsSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupsUnderAccount_Migrate.
     * json
     */
    /**
     * Sample code: BackupsUnderAccount_Migrate.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsUnderAccountMigrate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupsUnderAccounts()
            .migrateBackups("myRG", "account1", new BackupsMigrationRequest().withBackupVaultId(
                "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/backupVaults/backupVault1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupsUnderBackupVault_RestoreFiles

```java
import com.azure.resourcemanager.netapp.models.BackupRestoreFiles;
import java.util.Arrays;

/**
 * Samples for BackupsUnderBackupVault RestoreFiles.
 */
public final class BackupsUnderBackupVaultRestoreFilesSamples {
    /*
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/
     * BackupsUnderBackupVault_SingleFileRestore.json
     */
    /**
     * Sample code: Backups_SingleFileRestore.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsSingleFileRestore(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupsUnderBackupVaults()
            .restoreFiles("myRG", "account1", "backupVault1", "backup1", new BackupRestoreFiles()
                .withFileList(Arrays.asList("/dir1/customer1.db", "/dir1/customer2.db"))
                .withDestinationVolumeId(
                    "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1/volumes/volume1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupsUnderVolume_MigrateBackups

```java
import com.azure.resourcemanager.netapp.models.BackupsMigrationRequest;

/**
 * Samples for BackupsUnderVolume MigrateBackups.
 */
public final class BackupsUnderVolumeMigrateBackupsSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/BackupsUnderVolume_Migrate.json
     */
    /**
     * Sample code: BackupsUnderVolume_Migrate.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsUnderVolumeMigrate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupsUnderVolumes()
            .migrateBackups("myRG", "account1", "pool1", "volume1", new BackupsMigrationRequest().withBackupVaultId(
                "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/backupVaults/backupVault1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResource_CheckFilePathAvailability

```java
import com.azure.resourcemanager.netapp.models.FilePathAvailabilityRequest;

/**
 * Samples for NetAppResource CheckFilePathAvailability.
 */
public final class NetAppResourceCheckFilePathAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/CheckFilePathAvailability.json
     */
    /**
     * Sample code: CheckFilePathAvailability.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void checkFilePathAvailability(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResources()
            .checkFilePathAvailabilityWithResponse("eastus", new FilePathAvailabilityRequest()
                .withName("my-exact-filepth")
                .withSubnetId(
                    "/subscriptions/9760acf5-4638-11e7-9bdb-020073ca7778/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3"),
                com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResource_CheckNameAvailability

```java
import com.azure.resourcemanager.netapp.models.CheckNameResourceTypes;
import com.azure.resourcemanager.netapp.models.ResourceNameAvailabilityRequest;

/**
 * Samples for NetAppResource CheckNameAvailability.
 */
public final class NetAppResourceCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: CheckNameAvailability.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResources()
            .checkNameAvailabilityWithResponse("eastus",
                new ResourceNameAvailabilityRequest().withName("accName")
                    .withType(CheckNameResourceTypes.MICROSOFT_NET_APP_NET_APP_ACCOUNTS)
                    .withResourceGroup("myRG"),
                com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResource_CheckQuotaAvailability

```java
import com.azure.resourcemanager.netapp.models.CheckQuotaNameResourceTypes;
import com.azure.resourcemanager.netapp.models.QuotaAvailabilityRequest;

/**
 * Samples for NetAppResource CheckQuotaAvailability.
 */
public final class NetAppResourceCheckQuotaAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/CheckQuotaAvailability.json
     */
    /**
     * Sample code: CheckQuotaAvailability.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void checkQuotaAvailability(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResources()
            .checkQuotaAvailabilityWithResponse("eastus",
                new QuotaAvailabilityRequest().withName("resource1")
                    .withType(CheckQuotaNameResourceTypes.MICROSOFT_NET_APP_NET_APP_ACCOUNTS)
                    .withResourceGroup("myRG"),
                com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResource_QueryNetworkSiblingSet

```java
import com.azure.resourcemanager.netapp.models.QueryNetworkSiblingSetRequest;

/**
 * Samples for NetAppResource QueryNetworkSiblingSet.
 */
public final class NetAppResourceQueryNetworkSiblingSetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/NetworkSiblingSet_Query.json
     */
    /**
     * Sample code: NetworkSiblingSet_Query.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void networkSiblingSetQuery(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResources()
            .queryNetworkSiblingSetWithResponse("eastus", new QueryNetworkSiblingSetRequest()
                .withNetworkSiblingSetId("9760acf5-4638-11e7-9bdb-020073ca3333")
                .withSubnetId(
                    "/subscriptions/9760acf5-4638-11e7-9bdb-020073ca7778/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testVnet/subnets/testSubnet"),
                com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResource_QueryRegionInfo

```java
/**
 * Samples for NetAppResource QueryRegionInfo.
 */
public final class NetAppResourceQueryRegionInfoSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/RegionInfo.json
     */
    /**
     * Sample code: RegionInfo_Query.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void regionInfoQuery(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResources().queryRegionInfoWithResponse("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResource_UpdateNetworkSiblingSet

```java
import com.azure.resourcemanager.netapp.models.NetworkFeatures;
import com.azure.resourcemanager.netapp.models.UpdateNetworkSiblingSetRequest;

/**
 * Samples for NetAppResource UpdateNetworkSiblingSet.
 */
public final class NetAppResourceUpdateNetworkSiblingSetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/NetworkSiblingSet_Update.json
     */
    /**
     * Sample code: NetworkFeatures_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void networkFeaturesUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResources()
            .updateNetworkSiblingSet("eastus", new UpdateNetworkSiblingSetRequest()
                .withNetworkSiblingSetId("9760acf5-4638-11e7-9bdb-020073ca3333")
                .withSubnetId(
                    "/subscriptions/9760acf5-4638-11e7-9bdb-020073ca7778/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testVnet/subnets/testSubnet")
                .withNetworkSiblingSetStateId("12345_44420.8001578125")
                .withNetworkFeatures(NetworkFeatures.STANDARD), com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResourceQuotaLimits_Get

```java
/**
 * Samples for NetAppResourceQuotaLimits Get.
 */
public final class NetAppResourceQuotaLimitsGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/QuotaLimits_Get.json
     */
    /**
     * Sample code: QuotaLimits.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void quotaLimits(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResourceQuotaLimits()
            .getWithResponse("eastus", "totalCoolAccessVolumesPerSubscription", com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResourceQuotaLimits_List

```java
/**
 * Samples for NetAppResourceQuotaLimits List.
 */
public final class NetAppResourceQuotaLimitsListSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/QuotaLimits_List.json
     */
    /**
     * Sample code: QuotaLimits.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void quotaLimits(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResourceQuotaLimits().list("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResourceRegionInfos_Get

```java
/**
 * Samples for NetAppResourceRegionInfos Get.
 */
public final class NetAppResourceRegionInfosGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/RegionInfos_Get.json
     */
    /**
     * Sample code: RegionInfos_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void regionInfosGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResourceRegionInfos().getWithResponse("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### NetAppResourceRegionInfos_List

```java
/**
 * Samples for NetAppResourceRegionInfos List.
 */
public final class NetAppResourceRegionInfosListSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/RegionInfos_List.json
     */
    /**
     * Sample code: RegionInfos_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void regionInfosList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResourceRegionInfos().list("eastus", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/OperationList.json
     */
    /**
     * Sample code: OperationList.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void operationList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Pools_CreateOrUpdate

```java
import com.azure.resourcemanager.netapp.models.QosType;
import com.azure.resourcemanager.netapp.models.ServiceLevel;

/**
 * Samples for Pools CreateOrUpdate.
 */
public final class PoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Pools_CreateOrUpdate.json
     */
    /**
     * Sample code: Pools_CreateOrUpdate.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsCreateOrUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.pools()
            .define("pool1")
            .withRegion("eastus")
            .withExistingNetAppAccount("myRG", "account1")
            .withSize(4398046511104L)
            .withServiceLevel(ServiceLevel.PREMIUM)
            .withQosType(QosType.AUTO)
            .create();
    }
}
```

### Pools_Delete

```java
/**
 * Samples for Pools Delete.
 */
public final class PoolsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Pools_Delete.json
     */
    /**
     * Sample code: Pools_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.pools().delete("myRG", "account1", "pool1", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_Get

```java
/**
 * Samples for Pools Get.
 */
public final class PoolsGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Pools_Get.json
     */
    /**
     * Sample code: Pools_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.pools().getWithResponse("myRG", "account1", "pool1", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_List

```java
/**
 * Samples for Pools List.
 */
public final class PoolsListSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Pools_List.json
     */
    /**
     * Sample code: Pools_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.pools().list("myRG", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_Update

```java
import com.azure.resourcemanager.netapp.models.CapacityPool;

/**
 * Samples for Pools Update.
 */
public final class PoolsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Pools_Update.json
     */
    /**
     * Sample code: Pools_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        CapacityPool resource
            = manager.pools().getWithResponse("myRG", "account1", "pool1", com.azure.core.util.Context.NONE).getValue();
        resource.update().apply();
    }
}
```

### SnapshotPolicies_Create

```java
import com.azure.resourcemanager.netapp.models.DailySchedule;
import com.azure.resourcemanager.netapp.models.HourlySchedule;
import com.azure.resourcemanager.netapp.models.MonthlySchedule;
import com.azure.resourcemanager.netapp.models.WeeklySchedule;

/**
 * Samples for SnapshotPolicies Create.
 */
public final class SnapshotPoliciesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/SnapshotPolicies_Create.json
     */
    /**
     * Sample code: SnapshotPolicies_Create.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshotPolicies()
            .define("snapshotPolicyName")
            .withRegion("eastus")
            .withExistingNetAppAccount("myRG", "account1")
            .withHourlySchedule(new HourlySchedule().withSnapshotsToKeep(2).withMinute(50))
            .withDailySchedule(new DailySchedule().withSnapshotsToKeep(4).withHour(14).withMinute(30))
            .withWeeklySchedule(
                new WeeklySchedule().withSnapshotsToKeep(3).withDay("Wednesday").withHour(14).withMinute(45))
            .withMonthlySchedule(
                new MonthlySchedule().withSnapshotsToKeep(5).withDaysOfMonth("10,11,12").withHour(14).withMinute(15))
            .withEnabled(true)
            .create();
    }
}
```

### SnapshotPolicies_Delete

```java
/**
 * Samples for SnapshotPolicies Delete.
 */
public final class SnapshotPoliciesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/SnapshotPolicies_Delete.json
     */
    /**
     * Sample code: SnapshotPolicies_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshotPolicies()
            .delete("resourceGroup", "accountName", "snapshotPolicyName", com.azure.core.util.Context.NONE);
    }
}
```

### SnapshotPolicies_Get

```java
/**
 * Samples for SnapshotPolicies Get.
 */
public final class SnapshotPoliciesGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/SnapshotPolicies_Get.json
     */
    /**
     * Sample code: SnapshotPolicies_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshotPolicies()
            .getWithResponse("myRG", "account1", "snapshotPolicyName", com.azure.core.util.Context.NONE);
    }
}
```

### SnapshotPolicies_List

```java
/**
 * Samples for SnapshotPolicies List.
 */
public final class SnapshotPoliciesListSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/SnapshotPolicies_List.json
     */
    /**
     * Sample code: SnapshotPolicies_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshotPolicies().list("myRG", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### SnapshotPolicies_ListVolumes

```java
/**
 * Samples for SnapshotPolicies ListVolumes.
 */
public final class SnapshotPoliciesListVolumesSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/SnapshotPolicies_ListVolumes.
     * json
     */
    /**
     * Sample code: SnapshotPolicies_ListVolumes.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesListVolumes(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshotPolicies()
            .listVolumesWithResponse("myRG", "account1", "snapshotPolicyName", com.azure.core.util.Context.NONE);
    }
}
```

### SnapshotPolicies_Update

```java
import com.azure.resourcemanager.netapp.models.DailySchedule;
import com.azure.resourcemanager.netapp.models.HourlySchedule;
import com.azure.resourcemanager.netapp.models.MonthlySchedule;
import com.azure.resourcemanager.netapp.models.SnapshotPolicy;
import com.azure.resourcemanager.netapp.models.WeeklySchedule;

/**
 * Samples for SnapshotPolicies Update.
 */
public final class SnapshotPoliciesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/SnapshotPolicies_Update.json
     */
    /**
     * Sample code: SnapshotPolicies_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        SnapshotPolicy resource = manager.snapshotPolicies()
            .getWithResponse("myRG", "account1", "snapshotPolicyName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withHourlySchedule(new HourlySchedule().withSnapshotsToKeep(2).withMinute(50))
            .withDailySchedule(new DailySchedule().withSnapshotsToKeep(4).withHour(14).withMinute(30))
            .withWeeklySchedule(
                new WeeklySchedule().withSnapshotsToKeep(3).withDay("Wednesday").withHour(14).withMinute(45))
            .withMonthlySchedule(
                new MonthlySchedule().withSnapshotsToKeep(5).withDaysOfMonth("10,11,12").withHour(14).withMinute(15))
            .withEnabled(true)
            .apply();
    }
}
```

### Snapshots_Create

```java
/**
 * Samples for Snapshots Create.
 */
public final class SnapshotsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Snapshots_Create.json
     */
    /**
     * Sample code: Snapshots_Create.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshots()
            .define("snapshot1")
            .withRegion("eastus")
            .withExistingVolume("myRG", "account1", "pool1", "volume1")
            .create();
    }
}
```

### Snapshots_Delete

```java
/**
 * Samples for Snapshots Delete.
 */
public final class SnapshotsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Snapshots_Delete.json
     */
    /**
     * Sample code: Snapshots_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshots()
            .delete("myRG", "account1", "pool1", "volume1", "snapshot1", com.azure.core.util.Context.NONE);
    }
}
```

### Snapshots_Get

```java
/**
 * Samples for Snapshots Get.
 */
public final class SnapshotsGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Snapshots_Get.json
     */
    /**
     * Sample code: Snapshots_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshots()
            .getWithResponse("myRG", "account1", "pool1", "volume1", "snapshot1", com.azure.core.util.Context.NONE);
    }
}
```

### Snapshots_List

```java
/**
 * Samples for Snapshots List.
 */
public final class SnapshotsListSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Snapshots_List.json
     */
    /**
     * Sample code: Snapshots_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshots().list("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Snapshots_RestoreFiles

```java
import com.azure.resourcemanager.netapp.models.SnapshotRestoreFiles;
import java.util.Arrays;

/**
 * Samples for Snapshots RestoreFiles.
 */
public final class SnapshotsRestoreFilesSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Snapshots_SingleFileRestore.
     * json
     */
    /**
     * Sample code: Snapshots_SingleFileRestore.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsSingleFileRestore(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshots()
            .restoreFiles("myRG", "account1", "pool1", "volume1", "snapshot1",
                new SnapshotRestoreFiles().withFilePaths(Arrays.asList("/dir1/customer1.db", "/dir1/customer2.db")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Snapshots_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/**
 * Samples for Snapshots Update.
 */
public final class SnapshotsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Snapshots_Update.json
     */
    /**
     * Sample code: Snapshots_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) throws IOException {
        manager.snapshots()
            .update("myRG", "account1", "pool1", "volume1", "snapshot1",
                SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON),
                com.azure.core.util.Context.NONE);
    }
}
```

### Subvolumes_Create

```java
/**
 * Samples for Subvolumes Create.
 */
public final class SubvolumesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Subvolumes_Create.json
     */
    /**
     * Sample code: Subvolumes_Create.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void subvolumesCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.subvolumes()
            .define("subvolume1")
            .withExistingVolume("myRG", "account1", "pool1", "volume1")
            .withPath("/subvolumePath")
            .create();
    }
}
```

### Subvolumes_Delete

```java
/**
 * Samples for Subvolumes Delete.
 */
public final class SubvolumesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Subvolumes_Delete.json
     */
    /**
     * Sample code: Subvolumes_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void subvolumesDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.subvolumes()
            .delete("myRG", "account1", "pool1", "volume1", "subvolume1", com.azure.core.util.Context.NONE);
    }
}
```

### Subvolumes_Get

```java
/**
 * Samples for Subvolumes Get.
 */
public final class SubvolumesGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Subvolumes_Get.json
     */
    /**
     * Sample code: Subvolumes_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void subvolumesGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.subvolumes()
            .getWithResponse("myRG", "account1", "pool1", "volume1", "subvolume1", com.azure.core.util.Context.NONE);
    }
}
```

### Subvolumes_GetMetadata

```java
/**
 * Samples for Subvolumes GetMetadata.
 */
public final class SubvolumesGetMetadataSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Subvolumes_Metadata.json
     */
    /**
     * Sample code: Subvolumes_Metadata.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void subvolumesMetadata(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.subvolumes()
            .getMetadata("myRG", "account1", "pool1", "volume1", "subvolume1", com.azure.core.util.Context.NONE);
    }
}
```

### Subvolumes_ListByVolume

```java
/**
 * Samples for Subvolumes ListByVolume.
 */
public final class SubvolumesListByVolumeSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Subvolumes_List.json
     */
    /**
     * Sample code: Subvolumes_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void subvolumesList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.subvolumes().listByVolume("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Subvolumes_Update

```java
import com.azure.resourcemanager.netapp.models.SubvolumeInfo;

/**
 * Samples for Subvolumes Update.
 */
public final class SubvolumesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Subvolumes_Update.json
     */
    /**
     * Sample code: Subvolumes_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void subvolumesUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        SubvolumeInfo resource = manager.subvolumes()
            .getWithResponse("myRG", "account1", "pool1", "volume1", "subvolume1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withPath("/subvolumePath").apply();
    }
}
```

### VolumeGroups_Create

```java
import com.azure.resourcemanager.netapp.models.ApplicationType;
import com.azure.resourcemanager.netapp.models.ExportPolicyRule;
import com.azure.resourcemanager.netapp.models.ServiceLevel;
import com.azure.resourcemanager.netapp.models.VolumeGroupMetadata;
import com.azure.resourcemanager.netapp.models.VolumeGroupVolumeProperties;
import com.azure.resourcemanager.netapp.models.VolumePropertiesExportPolicy;
import java.util.Arrays;

/**
 * Samples for VolumeGroups Create.
 */
public final class VolumeGroupsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeGroups_Create_SapHana.
     * json
     */
    /**
     * Sample code: VolumeGroups_Create_SapHana.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeGroupsCreateSapHana(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeGroups()
            .define("group1")
            .withExistingNetAppAccount("myRG", "account1")
            .withRegion("westus")
            .withGroupMetadata(new VolumeGroupMetadata().withGroupDescription("Volume group")
                .withApplicationType(ApplicationType.SAP_HANA)
                .withApplicationIdentifier("SH9"))
            .withVolumes(Arrays.asList(
                new VolumeGroupVolumeProperties().withName("test-data-mnt00001")
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withProximityPlacementGroup(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/cys_sjain_fcp_rg/providers/Microsoft.Compute/proximityPlacementGroups/svlqa_sjain_multivolume_ppg")
                    .withVolumeSpecName("data"),
                new VolumeGroupVolumeProperties().withName("test-log-mnt00001")
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withProximityPlacementGroup(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/cys_sjain_fcp_rg/providers/Microsoft.Compute/proximityPlacementGroups/svlqa_sjain_multivolume_ppg")
                    .withVolumeSpecName("log"),
                new VolumeGroupVolumeProperties().withName("test-shared")
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withProximityPlacementGroup(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/cys_sjain_fcp_rg/providers/Microsoft.Compute/proximityPlacementGroups/svlqa_sjain_multivolume_ppg")
                    .withVolumeSpecName("shared"),
                new VolumeGroupVolumeProperties().withName("test-data-backup")
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withProximityPlacementGroup(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/cys_sjain_fcp_rg/providers/Microsoft.Compute/proximityPlacementGroups/svlqa_sjain_multivolume_ppg")
                    .withVolumeSpecName("data-backup"),
                new VolumeGroupVolumeProperties().withName("test-log-backup")
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withProximityPlacementGroup(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/cys_sjain_fcp_rg/providers/Microsoft.Compute/proximityPlacementGroups/svlqa_sjain_multivolume_ppg")
                    .withVolumeSpecName("log-backup")))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeGroups_Create_Oracle.json
     */
    /**
     * Sample code: VolumeGroups_Create_Oracle.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeGroupsCreateOracle(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeGroups()
            .define("group1")
            .withExistingNetAppAccount("myRG", "account1")
            .withRegion("westus")
            .withGroupMetadata(new VolumeGroupMetadata().withGroupDescription("Volume group")
                .withApplicationType(ApplicationType.ORACLE)
                .withApplicationIdentifier("OR2"))
            .withVolumes(Arrays.asList(
                new VolumeGroupVolumeProperties().withName("test-ora-data1")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-data1"),
                new VolumeGroupVolumeProperties().withName("test-ora-data2")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-data2"),
                new VolumeGroupVolumeProperties().withName("test-ora-data3")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-data3"),
                new VolumeGroupVolumeProperties().withName("test-ora-data4")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-data4"),
                new VolumeGroupVolumeProperties().withName("test-ora-data5")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-data5"),
                new VolumeGroupVolumeProperties().withName("test-ora-data6")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-data6"),
                new VolumeGroupVolumeProperties().withName("test-ora-data7")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-data7"),
                new VolumeGroupVolumeProperties().withName("test-ora-data8")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-data8"),
                new VolumeGroupVolumeProperties().withName("test-ora-log")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-log"),
                new VolumeGroupVolumeProperties().withName("test-ora-log-mirror")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-log-mirror"),
                new VolumeGroupVolumeProperties().withName("test-ora-binary")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-binary"),
                new VolumeGroupVolumeProperties().withName("test-ora-backup")
                    .withZones(Arrays.asList("1"))
                    .withCreationToken("fakeTokenPlaceholder")
                    .withServiceLevel(ServiceLevel.PREMIUM)
                    .withUsageThreshold(107374182400L)
                    .withExportPolicy(new VolumePropertiesExportPolicy()
                        .withRules(Arrays.asList(new ExportPolicyRule().withRuleIndex(1)
                            .withUnixReadOnly(true)
                            .withUnixReadWrite(true)
                            .withKerberos5ReadOnly(false)
                            .withKerberos5ReadWrite(false)
                            .withKerberos5IReadOnly(false)
                            .withKerberos5IReadWrite(false)
                            .withKerberos5PReadOnly(false)
                            .withKerberos5PReadWrite(false)
                            .withCifs(false)
                            .withNfsv3(false)
                            .withNfsv41(true)
                            .withAllowedClients("0.0.0.0/0")
                            .withHasRootAccess(true))))
                    .withProtocolTypes(Arrays.asList("NFSv4.1"))
                    .withSubnetId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
                    .withThroughputMibps(10.0F)
                    .withCapacityPoolResourceId(
                        "/subscriptions/d633cc2e-722b-4ae1-b636-bbd9e4c60ed9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1")
                    .withVolumeSpecName("ora-backup")))
            .create();
    }
}
```

### VolumeGroups_Delete

```java
/**
 * Samples for VolumeGroups Delete.
 */
public final class VolumeGroupsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeGroups_Delete.json
     */
    /**
     * Sample code: VolumeGroups_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeGroupsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeGroups().delete("myRG", "account1", "group1", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_Get

```java
/**
 * Samples for VolumeGroups Get.
 */
public final class VolumeGroupsGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeGroups_Get_SapHana.json
     */
    /**
     * Sample code: VolumeGroups_Get_SapHana.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeGroupsGetSapHana(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeGroups().getWithResponse("myRG", "account1", "group1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeGroups_Get_Oracle.json
     */
    /**
     * Sample code: VolumeGroups_Get_Oracle.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeGroupsGetOracle(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeGroups().getWithResponse("myRG", "account1", "group1", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_ListByNetAppAccount

```java
/**
 * Samples for VolumeGroups ListByNetAppAccount.
 */
public final class VolumeGroupsListByNetAppAccountSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeGroups_List_Oracle.json
     */
    /**
     * Sample code: VolumeGroups_List_Oracle.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeGroupsListOracle(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeGroups().listByNetAppAccount("myRG", "account1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeGroups_List_SapHana.json
     */
    /**
     * Sample code: VolumeGroups_List_SapHana.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeGroupsListSapHana(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeGroups().listByNetAppAccount("myRG", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeQuotaRules_Create

```java
import com.azure.resourcemanager.netapp.models.Type;

/**
 * Samples for VolumeQuotaRules Create.
 */
public final class VolumeQuotaRulesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeQuotaRules_Create.json
     */
    /**
     * Sample code: VolumeQuotaRules_Create.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeQuotaRulesCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeQuotaRules()
            .define("rule-0004")
            .withRegion("westus")
            .withExistingVolume("myRG", "account-9957", "pool-5210", "volume-6387")
            .withQuotaSizeInKiBs(100005L)
            .withQuotaType(Type.INDIVIDUAL_USER_QUOTA)
            .withQuotaTarget("1821")
            .create();
    }
}
```

### VolumeQuotaRules_Delete

```java
/**
 * Samples for VolumeQuotaRules Delete.
 */
public final class VolumeQuotaRulesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeQuotaRules_Delete.json
     */
    /**
     * Sample code: VolumeQuotaRules_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeQuotaRulesDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeQuotaRules()
            .delete("myRG", "account-9957", "pool-5210", "volume-6387", "rule-0004", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeQuotaRules_Get

```java
/**
 * Samples for VolumeQuotaRules Get.
 */
public final class VolumeQuotaRulesGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeQuotaRules_Get.json
     */
    /**
     * Sample code: VolumeQuotaRules_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeQuotaRulesGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeQuotaRules()
            .getWithResponse("myRG", "account-9957", "pool-5210", "volume-6387", "rule-0004",
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeQuotaRules_ListByVolume

```java
/**
 * Samples for VolumeQuotaRules ListByVolume.
 */
public final class VolumeQuotaRulesListByVolumeSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeQuotaRules_List.json
     */
    /**
     * Sample code: VolumeQuotaRules_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeQuotaRulesList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumeQuotaRules()
            .listByVolume("myRG", "account-9957", "pool-5210", "volume-6387", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeQuotaRules_Update

```java
import com.azure.resourcemanager.netapp.models.VolumeQuotaRule;

/**
 * Samples for VolumeQuotaRules Update.
 */
public final class VolumeQuotaRulesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/VolumeQuotaRules_Update.json
     */
    /**
     * Sample code: VolumeQuotaRules_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumeQuotaRulesUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        VolumeQuotaRule resource = manager.volumeQuotaRules()
            .getWithResponse("myRG", "account-9957", "pool-5210", "volume-6387", "rule-0004",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withQuotaSizeInKiBs(100009L).apply();
    }
}
```

### Volumes_AuthorizeReplication

```java
import com.azure.resourcemanager.netapp.models.AuthorizeRequest;

/**
 * Samples for Volumes AuthorizeReplication.
 */
public final class VolumesAuthorizeReplicationSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_AuthorizeReplication.
     * json
     */
    /**
     * Sample code: Volumes_AuthorizeReplication.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesAuthorizeReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .authorizeReplication("myRG", "account1", "pool1", "volume1",
                new AuthorizeRequest().withRemoteVolumeResourceId(
                    "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRemoteRG/providers/Microsoft.NetApp/netAppAccounts/remoteAccount1/capacityPools/remotePool1/volumes/remoteVolume1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_BreakFileLocks

```java
import com.azure.resourcemanager.netapp.models.BreakFileLocksRequest;

/**
 * Samples for Volumes BreakFileLocks.
 */
public final class VolumesBreakFileLocksSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_BreakFileLocks.json
     */
    /**
     * Sample code: Volumes_BreakFileLocks.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesBreakFileLocks(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .breakFileLocks("myRG", "account1", "pool1", "volume1",
                new BreakFileLocksRequest().withClientIp("101.102.103.104").withConfirmRunningDisruptiveOperation(true),
                com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_BreakReplication

```java
import com.azure.resourcemanager.netapp.models.BreakReplicationRequest;

/**
 * Samples for Volumes BreakReplication.
 */
public final class VolumesBreakReplicationSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_BreakReplication.json
     */
    /**
     * Sample code: Volumes_BreakReplication.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesBreakReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .breakReplication("myRG", "account1", "pool1", "volume1",
                new BreakReplicationRequest().withForceBreakReplication(false), com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_CreateOrUpdate

```java
import com.azure.resourcemanager.netapp.models.ServiceLevel;

/**
 * Samples for Volumes CreateOrUpdate.
 */
public final class VolumesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_CreateOrUpdate.json
     */
    /**
     * Sample code: Volumes_CreateOrUpdate.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesCreateOrUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .define("volume1")
            .withRegion("eastus")
            .withExistingCapacityPool("myRG", "account1", "pool1")
            .withCreationToken("my-unique-file-path")
            .withUsageThreshold(107374182400L)
            .withSubnetId(
                "/subscriptions/9760acf5-4638-11e7-9bdb-020073ca7778/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
            .withServiceLevel(ServiceLevel.PREMIUM)
            .create();
    }
}
```

### Volumes_Delete

```java
/**
 * Samples for Volumes Delete.
 */
public final class VolumesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_Delete.json
     */
    /**
     * Sample code: Volumes_Delete.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().delete("myRG", "account1", "pool1", "volume1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_DeleteReplication

```java
/**
 * Samples for Volumes DeleteReplication.
 */
public final class VolumesDeleteReplicationSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_DeleteReplication.json
     */
    /**
     * Sample code: Volumes_DeleteReplication.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesDeleteReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().deleteReplication("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_FinalizeRelocation

```java
/**
 * Samples for Volumes FinalizeRelocation.
 */
public final class VolumesFinalizeRelocationSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_FinalizeRelocation.json
     */
    /**
     * Sample code: Volumes_FinalizeRelocation.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesFinalizeRelocation(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().finalizeRelocation("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Get

```java
/**
 * Samples for Volumes Get.
 */
public final class VolumesGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_Get.json
     */
    /**
     * Sample code: Volumes_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().getWithResponse("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_List

```java
/**
 * Samples for Volumes List.
 */
public final class VolumesListSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_List.json
     */
    /**
     * Sample code: Volumes_List.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().list("myRG", "account1", "pool1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ListGetGroupIdListForLdapUser

```java
import com.azure.resourcemanager.netapp.models.GetGroupIdListForLdapUserRequest;

/**
 * Samples for Volumes ListGetGroupIdListForLdapUser.
 */
public final class VolumesListGetGroupIdListForLdapUserSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/GroupIdListForLDAPUser.json
     */
    /**
     * Sample code: GetGroupIdListForUser.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void getGroupIdListForUser(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .listGetGroupIdListForLdapUser("myRG", "account1", "pool1", "volume1",
                new GetGroupIdListForLdapUserRequest().withUsername("user1"), com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ListReplications

```java
/**
 * Samples for Volumes ListReplications.
 */
public final class VolumesListReplicationsSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_ListReplications.json
     */
    /**
     * Sample code: Volumes_ListReplications.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesListReplications(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().listReplications("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_PoolChange

```java
import com.azure.resourcemanager.netapp.models.PoolChangeRequest;

/**
 * Samples for Volumes PoolChange.
 */
public final class VolumesPoolChangeSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_PoolChange.json
     */
    /**
     * Sample code: Volumes_AuthorizeReplication.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesAuthorizeReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .poolChange("myRG", "account1", "pool1", "volume1", new PoolChangeRequest().withNewPoolResourceId(
                "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_PopulateAvailabilityZone

```java
/**
 * Samples for Volumes PopulateAvailabilityZone.
 */
public final class VolumesPopulateAvailabilityZoneSamples {
    /*
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/
     * Volumes_PopulateAvailabilityZones.json
     */
    /**
     * Sample code: Volumes_PopulateAvailabilityZones.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesPopulateAvailabilityZones(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .populateAvailabilityZone("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ReInitializeReplication

```java
/**
 * Samples for Volumes ReInitializeReplication.
 */
public final class VolumesReInitializeReplicationSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_ReInitializeReplication
     * .json
     */
    /**
     * Sample code: Volumes_ReInitializeReplication.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesReInitializeReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .reInitializeReplication("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ReestablishReplication

```java
import com.azure.resourcemanager.netapp.models.ReestablishReplicationRequest;

/**
 * Samples for Volumes ReestablishReplication.
 */
public final class VolumesReestablishReplicationSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_ReestablishReplication.
     * json
     */
    /**
     * Sample code: Volumes_ReestablishReplication.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesReestablishReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .reestablishReplication("myRG", "account1", "pool1", "volume1",
                new ReestablishReplicationRequest().withSourceVolumeId(
                    "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/mySourceRG/providers/Microsoft.NetApp/netAppAccounts/sourceAccount1/capacityPools/sourcePool1/volumes/sourceVolume1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Relocate

```java
import com.azure.resourcemanager.netapp.models.RelocateVolumeRequest;

/**
 * Samples for Volumes Relocate.
 */
public final class VolumesRelocateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_Relocate.json
     */
    /**
     * Sample code: Volumes_Relocate.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesRelocate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .relocate("myRG", "account1", "pool1", "volume1", new RelocateVolumeRequest(),
                com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ReplicationStatus

```java
/**
 * Samples for Volumes ReplicationStatus.
 */
public final class VolumesReplicationStatusSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_ReplicationStatus.json
     */
    /**
     * Sample code: Volumes_ReplicationStatus.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesReplicationStatus(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .replicationStatusWithResponse("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ResetCifsPassword

```java
/**
 * Samples for Volumes ResetCifsPassword.
 */
public final class VolumesResetCifsPasswordSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_ResetCifsPassword.json
     */
    /**
     * Sample code: Volumes_ResetCifsPassword.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesResetCifsPassword(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().resetCifsPassword("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ResyncReplication

```java
/**
 * Samples for Volumes ResyncReplication.
 */
public final class VolumesResyncReplicationSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_ResyncReplication.json
     */
    /**
     * Sample code: Volumes_ResyncReplication.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesResyncReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().resyncReplication("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Revert

```java
import com.azure.resourcemanager.netapp.models.VolumeRevert;

/**
 * Samples for Volumes Revert.
 */
public final class VolumesRevertSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_Revert.json
     */
    /**
     * Sample code: Volumes_Revert.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesRevert(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes()
            .revert("myRG", "account1", "pool1", "volume1", new VolumeRevert().withSnapshotId(
                "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1/volumes/volume1/snapshots/snapshot1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_RevertRelocation

```java
/**
 * Samples for Volumes RevertRelocation.
 */
public final class VolumesRevertRelocationSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_RevertRelocation.json
     */
    /**
     * Sample code: Volumes_RevertRelocation.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesRevertRelocation(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().revertRelocation("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Update

```java
import com.azure.resourcemanager.netapp.models.Volume;

/**
 * Samples for Volumes Update.
 */
public final class VolumesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2023-11-01/examples/Volumes_Update.json
     */
    /**
     * Sample code: Volumes_Update.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        Volume resource = manager.volumes()
            .getWithResponse("myRG", "account1", "pool1", "volume1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

