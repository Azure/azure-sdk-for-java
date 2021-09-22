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

## NetAppResourceQuotaLimits

- [Get](#netappresourcequotalimits_get)
- [List](#netappresourcequotalimits_list)

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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Backups_Account_Delete.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Backups_Account_Get.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Backups_Account_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Accounts_CreateOrUpdate.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Accounts_Delete.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Accounts_Get.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Accounts_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Accounts_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Accounts_Update.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/BackupPolicies_Create.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/BackupPolicies_Delete.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/BackupPolicies_Get.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/BackupPolicies_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/BackupPolicies_Update.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Backups_Create.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Backups_Delete.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Backups_Get.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_BackupStatus.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_RestoreStatus.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Backups_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Backups_Update.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/CheckFilePathAvailability.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/CheckNameAvailability.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/CheckQuotaAvailability.json
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

### NetAppResourceQuotaLimits_Get

```java
import com.azure.core.util.Context;

/** Samples for NetAppResourceQuotaLimits Get. */
public final class NetAppResourceQuotaLimitsGetSamples {
    /*
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/QuotaLimits_Get.json
     */
    /**
     * Sample code: QuotaLimits.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void quotaLimits(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager
            .netAppResourceQuotaLimits()
            .getWithResponse("eastus", "totalCoolAccessVolumesPerSubscription", Context.NONE);
    }
}
```

### NetAppResourceQuotaLimits_List

```java
import com.azure.core.util.Context;

/** Samples for NetAppResourceQuotaLimits List. */
public final class NetAppResourceQuotaLimitsListSamples {
    /*
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/QuotaLimits_List.json
     */
    /**
     * Sample code: QuotaLimits.
     *
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void quotaLimits(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResourceQuotaLimits().list("eastus", Context.NONE);
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Pools_CreateOrUpdate.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Pools_Delete.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Pools_Get.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Pools_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Pools_Update.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/SnapshotPolicies_Create.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/SnapshotPolicies_Delete.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/SnapshotPolicies_Get.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/SnapshotPolicies_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/SnapshotPolicies_ListVolumes.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/SnapshotPolicies_Update.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Snapshots_Create.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Snapshots_Delete.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Snapshots_Get.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Snapshots_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Snapshots_Update.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Vaults_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_AuthorizeReplication.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_BreakReplication.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_CreateOrUpdate.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_Delete.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_DeleteReplication.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_Get.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_List.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_PoolChange.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_ReInitializeReplication.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_ReplicationStatus.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_ResyncReplication.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_Revert.json
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
     * x-ms-original-file: specification/netapp/resource-manager/Microsoft.NetApp/stable/2021-06-01/examples/Volumes_Update.json
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

