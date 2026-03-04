# Code snippets and samples


## AvailabilityGroupListeners

- [CreateOrUpdate](#availabilitygrouplisteners_createorupdate)
- [Delete](#availabilitygrouplisteners_delete)
- [Get](#availabilitygrouplisteners_get)
- [ListByGroup](#availabilitygrouplisteners_listbygroup)

## Operations

- [List](#operations_list)

## SqlVirtualMachineGroups

- [CreateOrUpdate](#sqlvirtualmachinegroups_createorupdate)
- [Delete](#sqlvirtualmachinegroups_delete)
- [GetByResourceGroup](#sqlvirtualmachinegroups_getbyresourcegroup)
- [List](#sqlvirtualmachinegroups_list)
- [ListByResourceGroup](#sqlvirtualmachinegroups_listbyresourcegroup)
- [Update](#sqlvirtualmachinegroups_update)

## SqlVirtualMachineTroubleshoot

- [Troubleshoot](#sqlvirtualmachinetroubleshoot_troubleshoot)

## SqlVirtualMachines

- [CreateOrUpdate](#sqlvirtualmachines_createorupdate)
- [Delete](#sqlvirtualmachines_delete)
- [FetchDCAssessment](#sqlvirtualmachines_fetchdcassessment)
- [GetByResourceGroup](#sqlvirtualmachines_getbyresourcegroup)
- [List](#sqlvirtualmachines_list)
- [ListByResourceGroup](#sqlvirtualmachines_listbyresourcegroup)
- [ListBySqlVmGroup](#sqlvirtualmachines_listbysqlvmgroup)
- [Redeploy](#sqlvirtualmachines_redeploy)
- [StartAssessment](#sqlvirtualmachines_startassessment)
- [Update](#sqlvirtualmachines_update)
### AvailabilityGroupListeners_CreateOrUpdate

```java
import com.azure.resourcemanager.sqlvirtualmachine.models.LoadBalancerConfiguration;
import com.azure.resourcemanager.sqlvirtualmachine.models.MultiSubnetIpConfiguration;
import com.azure.resourcemanager.sqlvirtualmachine.models.PrivateIpAddress;
import java.util.Arrays;

/**
 * Samples for AvailabilityGroupListeners CreateOrUpdate.
 */
public final class AvailabilityGroupListenersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateAvailabilityGroupListener.json
     */
    /**
     * Sample code: Creates or updates an availability group listener using load balancer. This is used for VMs present
     * in single subnet.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        createsOrUpdatesAnAvailabilityGroupListenerUsingLoadBalancerThisIsUsedForVMsPresentInSingleSubnet(
            com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.availabilityGroupListeners()
            .define("agl-test")
            .withExistingSqlVirtualMachineGroup("testrg", "testvmgroup")
            .withAvailabilityGroupName("ag-test")
            .withLoadBalancerConfigurations(Arrays.asList(new LoadBalancerConfiguration()
                .withPrivateIpAddress(new PrivateIpAddress().withIpAddress("10.1.0.112")
                    .withSubnetResourceId(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/default"))
                .withLoadBalancerResourceId(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Network/loadBalancers/lb-test")
                .withProbePort(59983)
                .withSqlVirtualMachineInstances(Arrays.asList(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.SqlVirtualMachine/sqlVirtualMachines/testvm2",
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.SqlVirtualMachine/sqlVirtualMachines/testvm3"))))
            .withPort(1433)
            .create();
    }

    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateAvailabilityGroupListenerWithMultiSubnet.json
     */
    /**
     * Sample code: Creates or updates an availability group listener. This is used for VMs present in multi subnet.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesAnAvailabilityGroupListenerThisIsUsedForVMsPresentInMultiSubnet(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.availabilityGroupListeners()
            .define("agl-test")
            .withExistingSqlVirtualMachineGroup("testrg", "testvmgroup")
            .withAvailabilityGroupName("ag-test")
            .withMultiSubnetIpConfigurations(Arrays.asList(new MultiSubnetIpConfiguration()
                .withPrivateIpAddress(new PrivateIpAddress().withIpAddress("10.0.0.112")
                    .withSubnetResourceId(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/default"))
                .withSqlVirtualMachineInstance(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.SqlVirtualMachine/sqlVirtualMachines/testvm2"),
                new MultiSubnetIpConfiguration().withPrivateIpAddress(new PrivateIpAddress().withIpAddress("10.0.1.112")
                    .withSubnetResourceId(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/alternate"))
                    .withSqlVirtualMachineInstance(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.SqlVirtualMachine/sqlVirtualMachines/testvm1")))
            .withPort(1433)
            .create();
    }
}
```

### AvailabilityGroupListeners_Delete

```java
/**
 * Samples for AvailabilityGroupListeners Delete.
 */
public final class AvailabilityGroupListenersDeleteSamples {
    /*
     * x-ms-original-file: 2023-10-01/DeleteAvailabilityGroupListener.json
     */
    /**
     * Sample code: Deletes an availability group listener.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void deletesAnAvailabilityGroupListener(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.availabilityGroupListeners()
            .delete("testrg", "testvmgroup", "agl-test", com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilityGroupListeners_Get

```java
/**
 * Samples for AvailabilityGroupListeners Get.
 */
public final class AvailabilityGroupListenersGetSamples {
    /*
     * x-ms-original-file: 2023-10-01/GetAvailabilityGroupListener.json
     */
    /**
     * Sample code: Gets an availability group listener.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        getsAnAvailabilityGroupListener(com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.availabilityGroupListeners()
            .getWithResponse("testrg", "testvmgroup", "agl-test", null, com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilityGroupListeners_ListByGroup

```java
/**
 * Samples for AvailabilityGroupListeners ListByGroup.
 */
public final class AvailabilityGroupListenersListByGroupSamples {
    /*
     * x-ms-original-file: 2023-10-01/ListByGroupAvailabilityGroupListener.json
     */
    /**
     * Sample code: Lists all availability group listeners in a SQL virtual machine group.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void listsAllAvailabilityGroupListenersInASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.availabilityGroupListeners().listByGroup("testrg", "testvmgroup", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2023-10-01/ListOperation.json
     */
    /**
     * Sample code: Lists all of the available SQL Virtual Machine Rest API operations.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void listsAllOfTheAvailableSQLVirtualMachineRestAPIOperations(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachineGroups_CreateOrUpdate

```java
import com.azure.resourcemanager.sqlvirtualmachine.models.ClusterSubnetType;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlVmGroupImageSku;
import com.azure.resourcemanager.sqlvirtualmachine.models.WsfcDomainProfile;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlVirtualMachineGroups CreateOrUpdate.
 */
public final class SqlVirtualMachineGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine group.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineGroups()
            .define("testvmgroup")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("mytag", "myval"))
            .withSqlImageOffer("SQL2016-WS2016")
            .withSqlImageSku(SqlVmGroupImageSku.ENTERPRISE)
            .withWsfcDomainProfile(new WsfcDomainProfile().withDomainFqdn("testdomain.com")
                .withOuPath("OU=WSCluster,DC=testdomain,DC=com")
                .withClusterBootstrapAccount("testrpadmin")
                .withClusterOperatorAccount("testrp@testdomain.com")
                .withSqlServiceAccount("sqlservice@testdomain.com")
                .withIsSqlServiceAccountGmsa(false)
                .withStorageAccountUrl("https://storgact.blob.core.windows.net/")
                .withStorageAccountPrimaryKey("fakeTokenPlaceholder")
                .withClusterSubnetType(ClusterSubnetType.MULTI_SUBNET))
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

### SqlVirtualMachineGroups_Delete

```java
/**
 * Samples for SqlVirtualMachineGroups Delete.
 */
public final class SqlVirtualMachineGroupsDeleteSamples {
    /*
     * x-ms-original-file: 2023-10-01/DeleteSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Deletes a SQL virtual machine group.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        deletesASQLVirtualMachineGroup(com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineGroups().delete("testrg", "testvmgroup", com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachineGroups_GetByResourceGroup

```java
/**
 * Samples for SqlVirtualMachineGroups GetByResourceGroup.
 */
public final class SqlVirtualMachineGroupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-10-01/GetSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Gets a SQL virtual machine group.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        getsASQLVirtualMachineGroup(com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineGroups()
            .getByResourceGroupWithResponse("testrg", "testvmgroup", com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachineGroups_List

```java
/**
 * Samples for SqlVirtualMachineGroups List.
 */
public final class SqlVirtualMachineGroupsListSamples {
    /*
     * x-ms-original-file: 2023-10-01/ListSubscriptionSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Gets all SQL virtual machine groups in a subscription.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsAllSQLVirtualMachineGroupsInASubscription(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineGroups().list(com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachineGroups_ListByResourceGroup

```java
/**
 * Samples for SqlVirtualMachineGroups ListByResourceGroup.
 */
public final class SqlVirtualMachineGroupsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-10-01/ListByResourceGroupSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Gets all SQL virtual machine groups in a resource group.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsAllSQLVirtualMachineGroupsInAResourceGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineGroups().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachineGroups_Update

```java
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlVirtualMachineGroup;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlVirtualMachineGroups Update.
 */
public final class SqlVirtualMachineGroupsUpdateSamples {
    /*
     * x-ms-original-file: 2023-10-01/UpdateSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Updates a SQL virtual machine group tags.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void updatesASQLVirtualMachineGroupTags(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        SqlVirtualMachineGroup resource = manager.sqlVirtualMachineGroups()
            .getByResourceGroupWithResponse("testrg", "testvmgroup", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlVirtualMachineTroubleshoot_Troubleshoot

```java
import com.azure.resourcemanager.sqlvirtualmachine.fluent.models.SqlVmTroubleshootingInner;
import com.azure.resourcemanager.sqlvirtualmachine.models.TroubleshootingAdditionalProperties;
import com.azure.resourcemanager.sqlvirtualmachine.models.TroubleshootingScenario;
import com.azure.resourcemanager.sqlvirtualmachine.models.UnhealthyReplicaInfo;
import java.time.OffsetDateTime;

/**
 * Samples for SqlVirtualMachineTroubleshoot Troubleshoot.
 */
public final class SqlVirtualMachineTroubleshootTroubleshootSamples {
    /*
     * x-ms-original-file: 2023-10-01/TroubleshootSqlVirtualMachine.json
     */
    /**
     * Sample code: Start SQL virtual machine troubleshooting operation.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void startSQLVirtualMachineTroubleshootingOperation(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineTroubleshoots()
            .troubleshoot("testrg", "testvm",
                new SqlVmTroubleshootingInner().withStartTimeUtc(OffsetDateTime.parse("2023-07-09T17:10:00Z"))
                    .withEndTimeUtc(OffsetDateTime.parse("2023-07-09T22:10:00Z"))
                    .withTroubleshootingScenario(TroubleshootingScenario.UNHEALTHY_REPLICA)
                    .withProperties(new TroubleshootingAdditionalProperties()
                        .withUnhealthyReplicaInfo(new UnhealthyReplicaInfo().withAvailabilityGroupName("AG1"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachines_CreateOrUpdate

```java
import com.azure.resourcemanager.sqlvirtualmachine.models.AadAuthenticationSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.AdditionalFeaturesServerConfigurations;
import com.azure.resourcemanager.sqlvirtualmachine.models.AssessmentDayOfWeek;
import com.azure.resourcemanager.sqlvirtualmachine.models.AssessmentSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.AutoBackupDaysOfWeek;
import com.azure.resourcemanager.sqlvirtualmachine.models.AutoBackupSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.AutoPatchingSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.BackupScheduleType;
import com.azure.resourcemanager.sqlvirtualmachine.models.ConnectivityType;
import com.azure.resourcemanager.sqlvirtualmachine.models.DayOfWeek;
import com.azure.resourcemanager.sqlvirtualmachine.models.DiskConfigurationType;
import com.azure.resourcemanager.sqlvirtualmachine.models.FullBackupFrequencyType;
import com.azure.resourcemanager.sqlvirtualmachine.models.KeyVaultCredentialSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.LeastPrivilegeMode;
import com.azure.resourcemanager.sqlvirtualmachine.models.Schedule;
import com.azure.resourcemanager.sqlvirtualmachine.models.ServerConfigurationsManagementSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlConnectivityUpdateSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlImageSku;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlInstanceSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlServerLicenseType;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlStorageSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlStorageUpdateSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlTempDbSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlWorkloadType;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlWorkloadTypeUpdateSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.StorageConfigurationSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.StorageWorkloadType;
import com.azure.resourcemanager.sqlvirtualmachine.models.VirtualMachineIdentity;
import com.azure.resourcemanager.sqlvirtualmachine.models.VmIdentityType;
import com.azure.resourcemanager.sqlvirtualmachine.models.WsfcDomainCredentials;
import java.util.Arrays;

/**
 * Samples for SqlVirtualMachines CreateOrUpdate.
 */
public final class SqlVirtualMachinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateSqlVirtualMachineStorageConfigurationEXTEND.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine for Storage Configuration Settings to EXTEND Data, Log or
     * TempDB storage pool.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        createsOrUpdatesASQLVirtualMachineForStorageConfigurationSettingsToEXTENDDataLogOrTempDBStoragePool(
            com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .withStorageConfigurationSettings(new StorageConfigurationSettings()
                .withSqlDataSettings(new SqlStorageSettings().withLuns(Arrays.asList(2)))
                .withDiskConfigurationType(DiskConfigurationType.EXTEND))
            .create();
    }

    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateVirtualMachineWithVMGroup.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine and joins it to a SQL virtual machine group.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesASQLVirtualMachineAndJoinsItToASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm2")
            .withWsfcDomainCredentials(
                new WsfcDomainCredentials().withClusterBootstrapAccountPassword("fakeTokenPlaceholder")
                    .withClusterOperatorAccountPassword("fakeTokenPlaceholder")
                    .withSqlServiceAccountPassword("fakeTokenPlaceholder"))
            .withWsfcStaticIp("10.0.0.7")
            .create();
    }

    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateSqlVirtualMachineAutomatedBackupWeekly.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine for Automated Back up Settings with Weekly and Days of the
     * week to run the back up.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        createsOrUpdatesASQLVirtualMachineForAutomatedBackUpSettingsWithWeeklyAndDaysOfTheWeekToRunTheBackUp(
            com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .withSqlServerLicenseType(SqlServerLicenseType.PAYG)
            .withSqlImageSku(SqlImageSku.ENTERPRISE)
            .withAutoPatchingSettings(new AutoPatchingSettings().withEnable(true)
                .withDayOfWeek(DayOfWeek.SUNDAY)
                .withMaintenanceWindowStartingHour(2)
                .withMaintenanceWindowDuration(60))
            .withAutoBackupSettings(new AutoBackupSettings().withEnable(true)
                .withEnableEncryption(true)
                .withRetentionPeriod(17)
                .withStorageAccountUrl("https://teststorage.blob.core.windows.net/")
                .withStorageContainerName("testcontainer")
                .withStorageAccessKey("fakeTokenPlaceholder")
                .withPassword("fakeTokenPlaceholder")
                .withBackupSystemDbs(true)
                .withBackupScheduleType(BackupScheduleType.MANUAL)
                .withFullBackupFrequency(FullBackupFrequencyType.WEEKLY)
                .withDaysOfWeek(Arrays.asList(AutoBackupDaysOfWeek.MONDAY, AutoBackupDaysOfWeek.FRIDAY))
                .withFullBackupStartTime(6)
                .withFullBackupWindowHours(11)
                .withLogBackupFrequency(10))
            .withKeyVaultCredentialSettings(new KeyVaultCredentialSettings().withEnable(false))
            .withServerConfigurationsManagementSettings(new ServerConfigurationsManagementSettings()
                .withSqlConnectivityUpdateSettings(
                    new SqlConnectivityUpdateSettings().withConnectivityType(ConnectivityType.PRIVATE)
                        .withPort(1433)
                        .withSqlAuthUpdateUsername("sqllogin")
                        .withSqlAuthUpdatePassword("fakeTokenPlaceholder"))
                .withSqlWorkloadTypeUpdateSettings(
                    new SqlWorkloadTypeUpdateSettings().withSqlWorkloadType(SqlWorkloadType.OLTP))
                .withSqlStorageUpdateSettings(new SqlStorageUpdateSettings().withDiskCount(1)
                    .withStartingDeviceId(2)
                    .withDiskConfigurationType(DiskConfigurationType.NEW))
                .withAdditionalFeaturesServerConfigurations(
                    new AdditionalFeaturesServerConfigurations().withIsRServicesEnabled(false)))
            .create();
    }

    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateSqlVirtualMachineStorageConfigurationNEW.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine for Storage Configuration Settings to NEW Data, Log and
     * TempDB storage pool.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        createsOrUpdatesASQLVirtualMachineForStorageConfigurationSettingsToNEWDataLogAndTempDBStoragePool(
            com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .withStorageConfigurationSettings(new StorageConfigurationSettings()
                .withSqlDataSettings(
                    new SqlStorageSettings().withLuns(Arrays.asList(0)).withDefaultFilePath("F:\\folderpath\\"))
                .withSqlLogSettings(
                    new SqlStorageSettings().withLuns(Arrays.asList(1)).withDefaultFilePath("G:\\folderpath\\"))
                .withSqlTempDbSettings(new SqlTempDbSettings().withDataFileSize(256)
                    .withDataGrowth(512)
                    .withLogFileSize(256)
                    .withLogGrowth(512)
                    .withDataFileCount(8)
                    .withDefaultFilePath("D:\\TEMP"))
                .withSqlSystemDbOnDataDisk(true)
                .withDiskConfigurationType(DiskConfigurationType.NEW)
                .withStorageWorkloadType(StorageWorkloadType.OLTP))
            .create();
    }

    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateSqlVirtualMachineVmIdentitySettings.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine to enable the usage of Virtual Machine managed identity.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesASQLVirtualMachineToEnableTheUsageOfVirtualMachineManagedIdentity(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .withVirtualMachineIdentitySettings(new VirtualMachineIdentity().withType(VmIdentityType.USER_ASSIGNED)
                .withResourceId(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourcegroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testvmidentity"))
            .create();
    }

    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateSqlVirtualMachineMAX.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine with max parameters.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesASQLVirtualMachineWithMaxParameters(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .withSqlServerLicenseType(SqlServerLicenseType.PAYG)
            .withLeastPrivilegeMode(LeastPrivilegeMode.ENABLED)
            .withSqlImageSku(SqlImageSku.ENTERPRISE)
            .withAutoPatchingSettings(new AutoPatchingSettings().withEnable(true)
                .withDayOfWeek(DayOfWeek.SUNDAY)
                .withMaintenanceWindowStartingHour(2)
                .withMaintenanceWindowDuration(60))
            .withAutoBackupSettings(new AutoBackupSettings().withEnable(true)
                .withEnableEncryption(true)
                .withRetentionPeriod(17)
                .withStorageAccountUrl("https://teststorage.blob.core.windows.net/")
                .withStorageContainerName("testcontainer")
                .withStorageAccessKey("fakeTokenPlaceholder")
                .withPassword("fakeTokenPlaceholder")
                .withBackupSystemDbs(true)
                .withBackupScheduleType(BackupScheduleType.MANUAL)
                .withFullBackupFrequency(FullBackupFrequencyType.DAILY)
                .withFullBackupStartTime(6)
                .withFullBackupWindowHours(11)
                .withLogBackupFrequency(10))
            .withKeyVaultCredentialSettings(new KeyVaultCredentialSettings().withEnable(false))
            .withServerConfigurationsManagementSettings(new ServerConfigurationsManagementSettings()
                .withSqlConnectivityUpdateSettings(
                    new SqlConnectivityUpdateSettings().withConnectivityType(ConnectivityType.PRIVATE)
                        .withPort(1433)
                        .withSqlAuthUpdateUsername("sqllogin")
                        .withSqlAuthUpdatePassword("fakeTokenPlaceholder"))
                .withSqlWorkloadTypeUpdateSettings(
                    new SqlWorkloadTypeUpdateSettings().withSqlWorkloadType(SqlWorkloadType.OLTP))
                .withSqlStorageUpdateSettings(new SqlStorageUpdateSettings().withDiskCount(1)
                    .withStartingDeviceId(2)
                    .withDiskConfigurationType(DiskConfigurationType.NEW))
                .withAdditionalFeaturesServerConfigurations(
                    new AdditionalFeaturesServerConfigurations().withIsRServicesEnabled(false))
                .withSqlInstanceSettings(new SqlInstanceSettings().withCollation("SQL_Latin1_General_CP1_CI_AS")
                    .withMaxDop(8)
                    .withIsOptimizeForAdHocWorkloadsEnabled(true)
                    .withMinServerMemoryMB(0)
                    .withMaxServerMemoryMB(128)
                    .withIsLpimEnabled(true)
                    .withIsIfiEnabled(true))
                .withAzureAdAuthenticationSettings(
                    new AadAuthenticationSettings().withClientId("11111111-2222-3333-4444-555555555555")))
            .withStorageConfigurationSettings(new StorageConfigurationSettings()
                .withSqlDataSettings(new SqlStorageSettings().withLuns(Arrays.asList(0))
                    .withDefaultFilePath("F:\\folderpath\\")
                    .withUseStoragePool(false))
                .withSqlLogSettings(new SqlStorageSettings().withLuns(Arrays.asList(1))
                    .withDefaultFilePath("G:\\folderpath\\")
                    .withUseStoragePool(false))
                .withSqlTempDbSettings(new SqlTempDbSettings().withDataFileSize(256)
                    .withDataGrowth(512)
                    .withLogFileSize(256)
                    .withLogGrowth(512)
                    .withDataFileCount(8)
                    .withLuns(Arrays.asList(2))
                    .withDefaultFilePath("D:\\TEMP")
                    .withUseStoragePool(false))
                .withSqlSystemDbOnDataDisk(true)
                .withDiskConfigurationType(DiskConfigurationType.NEW)
                .withStorageWorkloadType(StorageWorkloadType.OLTP)
                .withEnableStorageConfigBlade(true))
            .withAssessmentSettings(new AssessmentSettings().withEnable(true)
                .withRunImmediately(true)
                .withSchedule(new Schedule().withEnable(true)
                    .withWeeklyInterval(1)
                    .withDayOfWeek(AssessmentDayOfWeek.SUNDAY)
                    .withStartTime("23:17")))
            .withEnableAutomaticUpgrade(true)
            .create();
    }

    /*
     * x-ms-original-file: 2023-10-01/CreateOrUpdateSqlVirtualMachineMIN.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine with min parameters.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesASQLVirtualMachineWithMinParameters(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .create();
    }
}
```

### SqlVirtualMachines_Delete

```java
/**
 * Samples for SqlVirtualMachines Delete.
 */
public final class SqlVirtualMachinesDeleteSamples {
    /*
     * x-ms-original-file: 2023-10-01/DeleteSqlVirtualMachine.json
     */
    /**
     * Sample code: Deletes a SQL virtual machine.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        deletesASQLVirtualMachine(com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().delete("testrg", "testvm1", com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachines_FetchDCAssessment

```java
import com.azure.resourcemanager.sqlvirtualmachine.models.DiskConfigAssessmentRequest;

/**
 * Samples for SqlVirtualMachines FetchDCAssessment.
 */
public final class SqlVirtualMachinesFetchDCAssessmentSamples {
    /*
     * x-ms-original-file: 2023-10-01/StartDiskConfigAssessmentOnSqlVirtualMachine.json
     */
    /**
     * Sample code: Starts SQL best practices Assessment with Disk Config rules on SQL virtual machine.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void startsSQLBestPracticesAssessmentWithDiskConfigRulesOnSQLVirtualMachine(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines()
            .fetchDCAssessment("testrg", "testvm", new DiskConfigAssessmentRequest().withRunDiskConfigRules(false),
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachines_GetByResourceGroup

```java
/**
 * Samples for SqlVirtualMachines GetByResourceGroup.
 */
public final class SqlVirtualMachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-10-01/GetSqlVirtualMachine.json
     */
    /**
     * Sample code: Gets a SQL virtual machine.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        getsASQLVirtualMachine(com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines()
            .getByResourceGroupWithResponse("testrg", "testvm", null, com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachines_List

```java
/**
 * Samples for SqlVirtualMachines List.
 */
public final class SqlVirtualMachinesListSamples {
    /*
     * x-ms-original-file: 2023-10-01/ListSubscriptionSqlVirtualMachine.json
     */
    /**
     * Sample code: Gets all SQL virtual machines in a subscription.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsAllSQLVirtualMachinesInASubscription(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().list(com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachines_ListByResourceGroup

```java
/**
 * Samples for SqlVirtualMachines ListByResourceGroup.
 */
public final class SqlVirtualMachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-10-01/ListByResourceGroupSqlVirtualMachine.json
     */
    /**
     * Sample code: Gets all SQL virtual machines in a resource group.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsAllSQLVirtualMachinesInAResourceGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachines_ListBySqlVmGroup

```java
/**
 * Samples for SqlVirtualMachines ListBySqlVmGroup.
 */
public final class SqlVirtualMachinesListBySqlVmGroupSamples {
    /*
     * x-ms-original-file: 2023-10-01/ListBySqlVirtualMachineGroupSqlVirtualMachine.json
     */
    /**
     * Sample code: Gets the list of sql virtual machines in a SQL virtual machine group.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsTheListOfSqlVirtualMachinesInASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().listBySqlVmGroup("testrg", "testvm", com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachines_Redeploy

```java
/**
 * Samples for SqlVirtualMachines Redeploy.
 */
public final class SqlVirtualMachinesRedeploySamples {
    /*
     * x-ms-original-file: 2023-10-01/RedeploySqlVirtualMachine.json
     */
    /**
     * Sample code: Uninstalls and reinstalls the SQL IaaS Extension.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void uninstallsAndReinstallsTheSQLIaaSExtension(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().redeploy("testrg", "testvm", com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachines_StartAssessment

```java
/**
 * Samples for SqlVirtualMachines StartAssessment.
 */
public final class SqlVirtualMachinesStartAssessmentSamples {
    /*
     * x-ms-original-file: 2023-10-01/StartAssessmentOnSqlVirtualMachine.json
     */
    /**
     * Sample code: Starts SQL best practices Assessment on SQL virtual machine.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void startsSQLBestPracticesAssessmentOnSQLVirtualMachine(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().startAssessment("testrg", "testvm", com.azure.core.util.Context.NONE);
    }
}
```

### SqlVirtualMachines_Update

```java
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlVirtualMachine;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlVirtualMachines Update.
 */
public final class SqlVirtualMachinesUpdateSamples {
    /*
     * x-ms-original-file: 2023-10-01/UpdateSqlVirtualMachine.json
     */
    /**
     * Sample code: Updates a SQL virtual machine tags.
     * 
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void
        updatesASQLVirtualMachineTags(com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        SqlVirtualMachine resource = manager.sqlVirtualMachines()
            .getByResourceGroupWithResponse("testrg", "testvm", null, com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

