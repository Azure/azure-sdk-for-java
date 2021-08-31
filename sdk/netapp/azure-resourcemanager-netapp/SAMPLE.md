# Code snippets and samples


## AccountBackups

- [Delete](#accountbackups_delete)
- [Get](#accountbackups_get)
- [List](#accountbackups_list)

## Accounts

- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## BackupPolicies

- [Create](#backuppolicies_create)
- [Delete](#backuppolicies_delete)
- [Get](#backuppolicies_get)
- [List](#backuppolicies_list)
- [Update](#backuppolicies_update)

## Backups

- [Create](#backups_create)
- [Delete](#backups_delete)
- [Get](#backups_get)
- [GetStatus](#backups_getstatus)
- [GetVolumeRestoreStatus](#backups_getvolumerestorestatus)
- [List](#backups_list)
- [Update](#backups_update)

## NetAppResource

- [CheckFilePathAvailability](#netappresource_checkfilepathavailability)
- [CheckNameAvailability](#netappresource_checknameavailability)
- [CheckQuotaAvailability](#netappresource_checkquotaavailability)

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
- [Update](#snapshots_update)

## Vaults

- [List](#vaults_list)

## Volumes

- [AuthorizeReplication](#volumes_authorizereplication)
- [BreakReplication](#volumes_breakreplication)
- [CreateOrUpdate](#volumes_createorupdate)
- [Delete](#volumes_delete)
- [DeleteReplication](#volumes_deletereplication)
- [Get](#volumes_get)
- [List](#volumes_list)
- [PoolChange](#volumes_poolchange)
- [ReInitializeReplication](#volumes_reinitializereplication)
- [ReplicationStatus](#volumes_replicationstatus)
- [ResyncReplication](#volumes_resyncreplication)
- [Revert](#volumes_revert)
- [Update](#volumes_update)
### AccountBackups_Delete

```java
import com.azure.core.util.Context;

/** Samples for AccountBackups Delete. */
public final class AccountBackupsDeleteSamples {
    /*
     * operationId: AccountBackups_Delete
     * api-version: 2021-06-01
     * x-ms-examples: AccountBackups_Delete
     */
    /**
     * Sample code: AccountBackups_Delete.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountBackupsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accountBackups().delete("resourceGroup", "accountName", "backupName", Context.NONE);
    }
}
```

### AccountBackups_Get

```java
import com.azure.core.util.Context;

/** Samples for AccountBackups Get. */
public final class AccountBackupsGetSamples {
    /*
     * operationId: AccountBackups_Get
     * api-version: 2021-06-01
     * x-ms-examples: AccountBackups_Get
     */
    /**
     * Sample code: AccountBackups_Get.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountBackupsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accountBackups().getWithResponse("myRG", "account1", "backup1", Context.NONE);
    }
}
```

### AccountBackups_List

```java
import com.azure.core.util.Context;

/** Samples for AccountBackups List. */
public final class AccountBackupsListSamples {
    /*
     * operationId: AccountBackups_List
     * api-version: 2021-06-01
     * x-ms-examples: AccountBackups_List
     */
    /**
     * Sample code: AccountBackups_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountBackupsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accountBackups().list("myRG", "account1", Context.NONE);
    }
}
```

### Accounts_CreateOrUpdate

```java
import com.azure.resourcemanager.netapp.models.ActiveDirectory;
import java.util.Arrays;

/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
    /*
     * operationId: Accounts_CreateOrUpdate
     * api-version: 2021-06-01
     * x-ms-examples: Accounts_CreateOrUpdate
     */
    /**
     * Sample code: Accounts_CreateOrUpdate.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsCreateOrUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .accounts()
            .define("account1")
            .withRegion("eastus")
            .withExistingResourceGroup("myRG")
            .withActiveDirectories(
                Arrays
                    .asList(
                        new ActiveDirectory()
                            .withUsername("ad_user_name")
                            .withPassword("ad_password")
                            .withDomain("10.10.10.3")
                            .withDns("10.10.10.3, 10.10.10.4")
                            .withSmbServerName("SMBServer")
                            .withOrganizationalUnit("Engineering")
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
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * operationId: Accounts_Delete
     * api-version: 2021-06-01
     * x-ms-examples: Accounts_Delete
     */
    /**
     * Sample code: Accounts_Delete.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().delete("myRG", "account1", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * operationId: Accounts_Get
     * api-version: 2021-06-01
     * x-ms-examples: Accounts_Get
     */
    /**
     * Sample code: Accounts_Get.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().getByResourceGroupWithResponse("myRG", "account1", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * operationId: Accounts_ListBySubscription
     * api-version: 2021-06-01
     * x-ms-examples: Accounts_List
     */
    /**
     * Sample code: Accounts_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().list(Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * operationId: Accounts_List
     * api-version: 2021-06-01
     * x-ms-examples: Accounts_List
     */
    /**
     * Sample code: Accounts_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.accounts().listByResourceGroup("myRG", Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.NetAppAccount;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * operationId: Accounts_Update
     * api-version: 2021-06-01
     * x-ms-examples: Accounts_Update
     */
    /**
     * Sample code: Accounts_Update.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void accountsUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        NetAppAccount resource =
            manager.accounts().getByResourceGroupWithResponse("myRG", "account1", Context.NONE).getValue();
        resource.update().withTags(mapOf("Tag1", "Value1")).apply();
    }

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
/** Samples for BackupPolicies Create. */
public final class BackupPoliciesCreateSamples {
    /*
     * operationId: BackupPolicies_Create
     * api-version: 2021-06-01
     * x-ms-examples: BackupPolicies_Create
     */
    /**
     * Sample code: BackupPolicies_Create.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupPoliciesCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .backupPolicies()
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
import com.azure.core.util.Context;

/** Samples for BackupPolicies Delete. */
public final class BackupPoliciesDeleteSamples {
    /*
     * operationId: BackupPolicies_Delete
     * api-version: 2021-06-01
     * x-ms-examples: Backups_Delete
     */
    /**
     * Sample code: Backups_Delete.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupPolicies().delete("resourceGroup", "accountName", "backupPolicyName", Context.NONE);
    }
}
```

### BackupPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for BackupPolicies Get. */
public final class BackupPoliciesGetSamples {
    /*
     * operationId: BackupPolicies_Get
     * api-version: 2021-06-01
     * x-ms-examples: Backups_Get
     */
    /**
     * Sample code: Backups_Get.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupPolicies().getWithResponse("myRG", "account1", "backupPolicyName", Context.NONE);
    }
}
```

### BackupPolicies_List

```java
import com.azure.core.util.Context;

/** Samples for BackupPolicies List. */
public final class BackupPoliciesListSamples {
    /*
     * operationId: BackupPolicies_List
     * api-version: 2021-06-01
     * x-ms-examples: Backups_List
     */
    /**
     * Sample code: Backups_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backupPolicies().list("myRG", "account1", Context.NONE);
    }
}
```

### BackupPolicies_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.BackupPolicy;

/** Samples for BackupPolicies Update. */
public final class BackupPoliciesUpdateSamples {
    /*
     * operationId: BackupPolicies_Update
     * api-version: 2021-06-01
     * x-ms-examples: BackupPolicies_Update
     */
    /**
     * Sample code: BackupPolicies_Update.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupPoliciesUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        BackupPolicy resource =
            manager.backupPolicies().getWithResponse("myRG", "account1", "backupPolicyName", Context.NONE).getValue();
        resource
            .update()
            .withDailyBackupsToKeep(5)
            .withWeeklyBackupsToKeep(10)
            .withMonthlyBackupsToKeep(10)
            .withEnabled(false)
            .apply();
    }
}
```

### Backups_Create

```java
/** Samples for Backups Create. */
public final class BackupsCreateSamples {
    /*
     * operationId: Backups_Create
     * api-version: 2021-06-01
     * x-ms-examples: Backups_Create
     */
    /**
     * Sample code: Backups_Create.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .backups()
            .define("backup1")
            .withRegion("eastus")
            .withExistingVolume("myRG", "account1", "pool1", "volume1")
            .withLabel("myLabel")
            .create();
    }
}
```

### Backups_Delete

```java
import com.azure.core.util.Context;

/** Samples for Backups Delete. */
public final class BackupsDeleteSamples {
    /*
     * operationId: Backups_Delete
     * api-version: 2021-06-01
     * x-ms-examples: Backups_Delete
     */
    /**
     * Sample code: Backups_Delete.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups().delete("resourceGroup", "accountName", "poolName", "volumeName", "backupName", Context.NONE);
    }
}
```

### Backups_Get

```java
import com.azure.core.util.Context;

/** Samples for Backups Get. */
public final class BackupsGetSamples {
    /*
     * operationId: Backups_Get
     * api-version: 2021-06-01
     * x-ms-examples: Backups_Get
     */
    /**
     * Sample code: Backups_Get.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups().getWithResponse("myRG", "account1", "pool1", "volume1", "backup1", Context.NONE);
    }
}
```

### Backups_GetStatus

```java
import com.azure.core.util.Context;

/** Samples for Backups GetStatus. */
public final class BackupsGetStatusSamples {
    /*
     * operationId: Backups_GetStatus
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_BackupStatus
     */
    /**
     * Sample code: Volumes_BackupStatus.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesBackupStatus(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups().getStatusWithResponse("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Backups_GetVolumeRestoreStatus

```java
import com.azure.core.util.Context;

/** Samples for Backups GetVolumeRestoreStatus. */
public final class BackupsGetVolumeRestoreStatusSamples {
    /*
     * operationId: Backups_GetVolumeRestoreStatus
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_RestoreStatus
     */
    /**
     * Sample code: Volumes_RestoreStatus.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesRestoreStatus(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups().getVolumeRestoreStatusWithResponse("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Backups_List

```java
import com.azure.core.util.Context;

/** Samples for Backups List. */
public final class BackupsListSamples {
    /*
     * operationId: Backups_List
     * api-version: 2021-06-01
     * x-ms-examples: Backups_List
     */
    /**
     * Sample code: Backups_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.backups().list("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Backups_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.Backup;

/** Samples for Backups Update. */
public final class BackupsUpdateSamples {
    /*
     * operationId: Backups_Update
     * api-version: 2021-06-01
     * x-ms-examples: Backups_Update
     */
    /**
     * Sample code: Backups_Update.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void backupsUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        Backup resource =
            manager
                .backups()
                .getWithResponse("myRG", "account1", "pool1", "volume1", "backup1", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### NetAppResource_CheckFilePathAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.FilePathAvailabilityRequest;

/** Samples for NetAppResource CheckFilePathAvailability. */
public final class NetAppResourceCheckFilePathAvailabilitySamples {
    /*
     * operationId: NetAppResource_CheckFilePathAvailability
     * api-version: 2021-06-01
     * x-ms-examples: CheckFilePathAvailability
     */
    /**
     * Sample code: CheckFilePathAvailability.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void checkFilePathAvailability(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .netAppResources()
            .checkFilePathAvailabilityWithResponse(
                "eastus",
                new FilePathAvailabilityRequest()
                    .withName("my-exact-filepth")
                    .withSubnetId(
                        "/subscriptions/9760acf5-4638-11e7-9bdb-020073ca7778/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3"),
                Context.NONE);
    }
}
```

### NetAppResource_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.CheckNameResourceTypes;
import com.azure.resourcemanager.netapp.models.ResourceNameAvailabilityRequest;

/** Samples for NetAppResource CheckNameAvailability. */
public final class NetAppResourceCheckNameAvailabilitySamples {
    /*
     * operationId: NetAppResource_CheckNameAvailability
     * api-version: 2021-06-01
     * x-ms-examples: CheckNameAvailability
     */
    /**
     * Sample code: CheckNameAvailability.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .netAppResources()
            .checkNameAvailabilityWithResponse(
                "eastus",
                new ResourceNameAvailabilityRequest()
                    .withName("accName")
                    .withType(CheckNameResourceTypes.fromString("netAppAccount"))
                    .withResourceGroup("myRG"),
                Context.NONE);
    }
}
```

### NetAppResource_CheckQuotaAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.CheckQuotaNameResourceTypes;
import com.azure.resourcemanager.netapp.models.QuotaAvailabilityRequest;

/** Samples for NetAppResource CheckQuotaAvailability. */
public final class NetAppResourceCheckQuotaAvailabilitySamples {
    /*
     * operationId: NetAppResource_CheckQuotaAvailability
     * api-version: 2021-06-01
     * x-ms-examples: CheckQuotaAvailability
     */
    /**
     * Sample code: CheckQuotaAvailability.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void checkQuotaAvailability(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .netAppResources()
            .checkQuotaAvailabilityWithResponse(
                "eastus",
                new QuotaAvailabilityRequest()
                    .withName("resource1")
                    .withType(CheckQuotaNameResourceTypes.MICROSOFT_NET_APP_NET_APP_ACCOUNTS)
                    .withResourceGroup("myRG"),
                Context.NONE);
    }
}
```

### Pools_CreateOrUpdate

```java
import com.azure.resourcemanager.netapp.models.QosType;
import com.azure.resourcemanager.netapp.models.ServiceLevel;

/** Samples for Pools CreateOrUpdate. */
public final class PoolsCreateOrUpdateSamples {
    /*
     * operationId: Pools_CreateOrUpdate
     * api-version: 2021-06-01
     * x-ms-examples: Pools_CreateOrUpdate
     */
    /**
     * Sample code: Pools_CreateOrUpdate.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsCreateOrUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .pools()
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
import com.azure.core.util.Context;

/** Samples for Pools Delete. */
public final class PoolsDeleteSamples {
    /*
     * operationId: Pools_Delete
     * api-version: 2021-06-01
     * x-ms-examples: Pools_Delete
     */
    /**
     * Sample code: Pools_Delete.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.pools().delete("myRG", "account1", "pool1", Context.NONE);
    }
}
```

### Pools_Get

```java
import com.azure.core.util.Context;

/** Samples for Pools Get. */
public final class PoolsGetSamples {
    /*
     * operationId: Pools_Get
     * api-version: 2021-06-01
     * x-ms-examples: Pools_Get
     */
    /**
     * Sample code: Pools_Get.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.pools().getWithResponse("myRG", "account1", "pool1", Context.NONE);
    }
}
```

### Pools_List

```java
import com.azure.core.util.Context;

/** Samples for Pools List. */
public final class PoolsListSamples {
    /*
     * operationId: Pools_List
     * api-version: 2021-06-01
     * x-ms-examples: Pools_List
     */
    /**
     * Sample code: Pools_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.pools().list("myRG", "account1", Context.NONE);
    }
}
```

### Pools_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.CapacityPool;

/** Samples for Pools Update. */
public final class PoolsUpdateSamples {
    /*
     * operationId: Pools_Update
     * api-version: 2021-06-01
     * x-ms-examples: Pools_Update
     */
    /**
     * Sample code: Pools_Update.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void poolsUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        CapacityPool resource = manager.pools().getWithResponse("myRG", "account1", "pool1", Context.NONE).getValue();
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

/** Samples for SnapshotPolicies Create. */
public final class SnapshotPoliciesCreateSamples {
    /*
     * operationId: SnapshotPolicies_Create
     * api-version: 2021-06-01
     * x-ms-examples: SnapshotPolicies_Create
     */
    /**
     * Sample code: SnapshotPolicies_Create.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .snapshotPolicies()
            .define("snapshotPolicyName")
            .withRegion("eastus")
            .withExistingNetAppAccount("myRG", "account1")
            .withHourlySchedule(new HourlySchedule().withSnapshotsToKeep(2).withMinute(50))
            .withDailySchedule(new DailySchedule().withSnapshotsToKeep(4).withHour(14).withMinute(30))
            .withWeeklySchedule(
                new WeeklySchedule().withSnapshotsToKeep(3).withDay("Wednesday").withHour(14).withMinute(45))
            .withMonthlySchedule(
                new MonthlySchedule().withSnapshotsToKeep(5).withDaysOfMonth("10,11,12").withHour(14).withMinute(15))
            .create();
    }
}
```

### SnapshotPolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for SnapshotPolicies Delete. */
public final class SnapshotPoliciesDeleteSamples {
    /*
     * operationId: SnapshotPolicies_Delete
     * api-version: 2021-06-01
     * x-ms-examples: SnapshotPolicies_Delete
     */
    /**
     * Sample code: SnapshotPolicies_Delete.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshotPolicies().delete("resourceGroup", "accountName", "snapshotPolicyName", Context.NONE);
    }
}
```

### SnapshotPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for SnapshotPolicies Get. */
public final class SnapshotPoliciesGetSamples {
    /*
     * operationId: SnapshotPolicies_Get
     * api-version: 2021-06-01
     * x-ms-examples: SnapshotPolicies_Get
     */
    /**
     * Sample code: SnapshotPolicies_Get.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshotPolicies().getWithResponse("myRG", "account1", "snapshotPolicyName", Context.NONE);
    }
}
```

### SnapshotPolicies_List

```java
import com.azure.core.util.Context;

/** Samples for SnapshotPolicies List. */
public final class SnapshotPoliciesListSamples {
    /*
     * operationId: SnapshotPolicies_List
     * api-version: 2021-06-01
     * x-ms-examples: SnapshotPolicies_List
     */
    /**
     * Sample code: SnapshotPolicies_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshotPolicies().list("myRG", "account1", Context.NONE);
    }
}
```

### SnapshotPolicies_ListVolumes

```java
import com.azure.core.util.Context;

/** Samples for SnapshotPolicies ListVolumes. */
public final class SnapshotPoliciesListVolumesSamples {
    /*
     * operationId: SnapshotPolicies_ListVolumes
     * api-version: 2021-06-01
     * x-ms-examples: SnapshotPolicies_ListVolumes
     */
    /**
     * Sample code: SnapshotPolicies_ListVolumes.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesListVolumes(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshotPolicies().listVolumesWithResponse("myRG", "account1", "snapshotPolicyName", Context.NONE);
    }
}
```

### SnapshotPolicies_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.DailySchedule;
import com.azure.resourcemanager.netapp.models.HourlySchedule;
import com.azure.resourcemanager.netapp.models.MonthlySchedule;
import com.azure.resourcemanager.netapp.models.SnapshotPolicy;
import com.azure.resourcemanager.netapp.models.WeeklySchedule;

/** Samples for SnapshotPolicies Update. */
public final class SnapshotPoliciesUpdateSamples {
    /*
     * operationId: SnapshotPolicies_Update
     * api-version: 2021-06-01
     * x-ms-examples: SnapshotPolicies_Update
     */
    /**
     * Sample code: SnapshotPolicies_Update.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotPoliciesUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        SnapshotPolicy resource =
            manager
                .snapshotPolicies()
                .getWithResponse("myRG", "account1", "snapshotPolicyName", Context.NONE)
                .getValue();
        resource
            .update()
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
/** Samples for Snapshots Create. */
public final class SnapshotsCreateSamples {
    /*
     * operationId: Snapshots_Create
     * api-version: 2021-06-01
     * x-ms-examples: Snapshots_Create
     */
    /**
     * Sample code: Snapshots_Create.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsCreate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .snapshots()
            .define("snapshot1")
            .withRegion("eastus")
            .withExistingVolume("myRG", "account1", "pool1", "volume1")
            .create();
    }
}
```

### Snapshots_Delete

```java
import com.azure.core.util.Context;

/** Samples for Snapshots Delete. */
public final class SnapshotsDeleteSamples {
    /*
     * operationId: Snapshots_Delete
     * api-version: 2021-06-01
     * x-ms-examples: Snapshots_Delete
     */
    /**
     * Sample code: Snapshots_Delete.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshots().delete("myRG", "account1", "pool1", "volume1", "snapshot1", Context.NONE);
    }
}
```

### Snapshots_Get

```java
import com.azure.core.util.Context;

/** Samples for Snapshots Get. */
public final class SnapshotsGetSamples {
    /*
     * operationId: Snapshots_Get
     * api-version: 2021-06-01
     * x-ms-examples: Snapshots_Get
     */
    /**
     * Sample code: Snapshots_Get.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshots().getWithResponse("myRG", "account1", "pool1", "volume1", "snapshot1", Context.NONE);
    }
}
```

### Snapshots_List

```java
import com.azure.core.util.Context;

/** Samples for Snapshots List. */
public final class SnapshotsListSamples {
    /*
     * operationId: Snapshots_List
     * api-version: 2021-06-01
     * x-ms-examples: Snapshots_List
     */
    /**
     * Sample code: Snapshots_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.snapshots().list("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Snapshots_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/** Samples for Snapshots Update. */
public final class SnapshotsUpdateSamples {
    /*
     * operationId: Snapshots_Update
     * api-version: 2021-06-01
     * x-ms-examples: Snapshots_Update
     */
    /**
     * Sample code: Snapshots_Update.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void snapshotsUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) throws IOException {
        manager
            .snapshots()
            .update(
                "myRG",
                "account1",
                "pool1",
                "volume1",
                "snapshot1",
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON),
                Context.NONE);
    }
}
```

### Vaults_List

```java
import com.azure.core.util.Context;

/** Samples for Vaults List. */
public final class VaultsListSamples {
    /*
     * operationId: Vaults_List
     * api-version: 2021-06-01
     * x-ms-examples: Vaults_List
     */
    /**
     * Sample code: Vaults_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void vaultsList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.vaults().list("myRG", "account1", Context.NONE);
    }
}
```

### Volumes_AuthorizeReplication

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.AuthorizeRequest;

/** Samples for Volumes AuthorizeReplication. */
public final class VolumesAuthorizeReplicationSamples {
    /*
     * operationId: Volumes_AuthorizeReplication
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_AuthorizeReplication
     */
    /**
     * Sample code: Volumes_AuthorizeReplication.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesAuthorizeReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .volumes()
            .authorizeReplication(
                "myRG",
                "account1",
                "pool1",
                "volume1",
                new AuthorizeRequest()
                    .withRemoteVolumeResourceId(
                        "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRemoteRG/providers/Microsoft.NetApp/netAppAccounts/remoteAccount1/capacityPools/remotePool1/volumes/remoteVolume1"),
                Context.NONE);
    }
}
```

### Volumes_BreakReplication

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.BreakReplicationRequest;

/** Samples for Volumes BreakReplication. */
public final class VolumesBreakReplicationSamples {
    /*
     * operationId: Volumes_BreakReplication
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_BreakReplication
     */
    /**
     * Sample code: Volumes_BreakReplication.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesBreakReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .volumes()
            .breakReplication(
                "myRG",
                "account1",
                "pool1",
                "volume1",
                new BreakReplicationRequest().withForceBreakReplication(false),
                Context.NONE);
    }
}
```

### Volumes_CreateOrUpdate

```java
import com.azure.resourcemanager.netapp.models.ServiceLevel;

/** Samples for Volumes CreateOrUpdate. */
public final class VolumesCreateOrUpdateSamples {
    /*
     * operationId: Volumes_CreateOrUpdate
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_CreateOrUpdate
     */
    /**
     * Sample code: Volumes_CreateOrUpdate.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesCreateOrUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .volumes()
            .define("volume1")
            .withRegion("eastus")
            .withExistingCapacityPool("myRG", "account1", "pool1")
            .withCreationToken("my-unique-file-path")
            .withUsageThreshold(107374182400L)
            .withSubnetId(
                "/subscriptions/9760acf5-4638-11e7-9bdb-020073ca7778/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")
            .withServiceLevel(ServiceLevel.PREMIUM)
            .withThroughputMibps(128.0f)
            .withEncryptionKeySource("Microsoft.KeyVault")
            .create();
    }
}
```

### Volumes_Delete

```java
import com.azure.core.util.Context;

/** Samples for Volumes Delete. */
public final class VolumesDeleteSamples {
    /*
     * operationId: Volumes_Delete
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_Delete
     */
    /**
     * Sample code: Volumes_Delete.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesDelete(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().delete("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Volumes_DeleteReplication

```java
import com.azure.core.util.Context;

/** Samples for Volumes DeleteReplication. */
public final class VolumesDeleteReplicationSamples {
    /*
     * operationId: Volumes_DeleteReplication
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_DeleteReplication
     */
    /**
     * Sample code: Volumes_DeleteReplication.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesDeleteReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().deleteReplication("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Volumes_Get

```java
import com.azure.core.util.Context;

/** Samples for Volumes Get. */
public final class VolumesGetSamples {
    /*
     * operationId: Volumes_Get
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_Get
     */
    /**
     * Sample code: Volumes_Get.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().getWithResponse("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Volumes_List

```java
import com.azure.core.util.Context;

/** Samples for Volumes List. */
public final class VolumesListSamples {
    /*
     * operationId: Volumes_List
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_List
     */
    /**
     * Sample code: Volumes_List.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesList(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().list("myRG", "account1", "pool1", Context.NONE);
    }
}
```

### Volumes_PoolChange

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.PoolChangeRequest;

/** Samples for Volumes PoolChange. */
public final class VolumesPoolChangeSamples {
    /*
     * operationId: Volumes_PoolChange
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_AuthorizeReplication
     */
    /**
     * Sample code: Volumes_AuthorizeReplication.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesAuthorizeReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .volumes()
            .poolChange(
                "myRG",
                "account1",
                "pool1",
                "volume1",
                new PoolChangeRequest()
                    .withNewPoolResourceId(
                        "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1"),
                Context.NONE);
    }
}
```

### Volumes_ReInitializeReplication

```java
import com.azure.core.util.Context;

/** Samples for Volumes ReInitializeReplication. */
public final class VolumesReInitializeReplicationSamples {
    /*
     * operationId: Volumes_ReInitializeReplication
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_ReInitializeReplication
     */
    /**
     * Sample code: Volumes_ReInitializeReplication.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesReInitializeReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().reInitializeReplication("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Volumes_ReplicationStatus

```java
import com.azure.core.util.Context;

/** Samples for Volumes ReplicationStatus. */
public final class VolumesReplicationStatusSamples {
    /*
     * operationId: Volumes_ReplicationStatus
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_ReplicationStatus
     */
    /**
     * Sample code: Volumes_ReplicationStatus.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesReplicationStatus(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().replicationStatusWithResponse("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Volumes_ResyncReplication

```java
import com.azure.core.util.Context;

/** Samples for Volumes ResyncReplication. */
public final class VolumesResyncReplicationSamples {
    /*
     * operationId: Volumes_ResyncReplication
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_ResyncReplication
     */
    /**
     * Sample code: Volumes_ResyncReplication.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesResyncReplication(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.volumes().resyncReplication("myRG", "account1", "pool1", "volume1", Context.NONE);
    }
}
```

### Volumes_Revert

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.VolumeRevert;

/** Samples for Volumes Revert. */
public final class VolumesRevertSamples {
    /*
     * operationId: Volumes_Revert
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_Revert
     */
    /**
     * Sample code: Volumes_Revert.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesRevert(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .volumes()
            .revert(
                "myRG",
                "account1",
                "pool1",
                "volume1",
                new VolumeRevert()
                    .withSnapshotId(
                        "/subscriptions/D633CC2E-722B-4AE1-B636-BBD9E4C60ED9/resourceGroups/myRG/providers/Microsoft.NetApp/netAppAccounts/account1/capacityPools/pool1/volumes/volume1/snapshots/snapshot1"),
                Context.NONE);
    }
}
```

### Volumes_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.netapp.models.Volume;

/** Samples for Volumes Update. */
public final class VolumesUpdateSamples {
    /*
     * operationId: Volumes_Update
     * api-version: 2021-06-01
     * x-ms-examples: Volumes_Update
     */
    /**
     * Sample code: Volumes_Update.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void volumesUpdate(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        Volume resource =
            manager.volumes().getWithResponse("myRG", "account1", "pool1", "volume1", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

