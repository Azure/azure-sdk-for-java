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

## SqlVirtualMachines

- [CreateOrUpdate](#sqlvirtualmachines_createorupdate)
- [Delete](#sqlvirtualmachines_delete)
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

/** Samples for AvailabilityGroupListeners CreateOrUpdate. */
public final class AvailabilityGroupListenersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/CreateOrUpdateAvailabilityGroupListener.json
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
        manager
            .availabilityGroupListeners()
            .define("agl-test")
            .withExistingSqlVirtualMachineGroup("testrg", "testvmgroup")
            .withAvailabilityGroupName("ag-test")
            .withLoadBalancerConfigurations(
                Arrays
                    .asList(
                        new LoadBalancerConfiguration()
                            .withPrivateIpAddress(
                                new PrivateIpAddress()
                                    .withIpAddress("10.1.0.112")
                                    .withSubnetResourceId(
                                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/default"))
                            .withLoadBalancerResourceId(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Network/loadBalancers/lb-test")
                            .withProbePort(59983)
                            .withSqlVirtualMachineInstances(
                                Arrays
                                    .asList(
                                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.SqlVirtualMachine/sqlVirtualMachines/testvm2",
                                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.SqlVirtualMachine/sqlVirtualMachines/testvm3"))))
            .withPort(1433)
            .create();
    }

    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/CreateOrUpdateAvailabilityGroupListenerWithMultiSubnet.json
     */
    /**
     * Sample code: Creates or updates an availability group listener. This is used for VMs present in multi subnet.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesAnAvailabilityGroupListenerThisIsUsedForVMsPresentInMultiSubnet(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager
            .availabilityGroupListeners()
            .define("agl-test")
            .withExistingSqlVirtualMachineGroup("testrg", "testvmgroup")
            .withAvailabilityGroupName("ag-test")
            .withMultiSubnetIpConfigurations(
                Arrays
                    .asList(
                        new MultiSubnetIpConfiguration()
                            .withPrivateIpAddress(
                                new PrivateIpAddress()
                                    .withIpAddress("10.0.0.112")
                                    .withSubnetResourceId(
                                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/default"))
                            .withSqlVirtualMachineInstance(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.SqlVirtualMachine/sqlVirtualMachines/testvm2"),
                        new MultiSubnetIpConfiguration()
                            .withPrivateIpAddress(
                                new PrivateIpAddress()
                                    .withIpAddress("10.0.1.112")
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
import com.azure.core.util.Context;

/** Samples for AvailabilityGroupListeners Delete. */
public final class AvailabilityGroupListenersDeleteSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/DeleteAvailabilityGroupListener.json
     */
    /**
     * Sample code: Deletes an availability group listener.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void deletesAnAvailabilityGroupListener(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.availabilityGroupListeners().delete("testrg", "testvmgroup", "agl-test", Context.NONE);
    }
}
```

### AvailabilityGroupListeners_Get

```java
import com.azure.core.util.Context;

/** Samples for AvailabilityGroupListeners Get. */
public final class AvailabilityGroupListenersGetSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/GetAvailabilityGroupListener.json
     */
    /**
     * Sample code: Gets an availability group listener.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsAnAvailabilityGroupListener(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.availabilityGroupListeners().getWithResponse("testrg", "testvmgroup", "agl-test", null, Context.NONE);
    }
}
```

### AvailabilityGroupListeners_ListByGroup

```java
import com.azure.core.util.Context;

/** Samples for AvailabilityGroupListeners ListByGroup. */
public final class AvailabilityGroupListenersListByGroupSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/ListByGroupAvailabilityGroupListener.json
     */
    /**
     * Sample code: Lists all availability group listeners in a SQL virtual machine group.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void listsAllAvailabilityGroupListenersInASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.availabilityGroupListeners().listByGroup("testrg", "testvmgroup", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/ListOperation.json
     */
    /**
     * Sample code: Lists all of the available SQL Virtual Machine Rest API operations.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void listsAllOfTheAvailableSQLVirtualMachineRestAPIOperations(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.operations().list(Context.NONE);
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

/** Samples for SqlVirtualMachineGroups CreateOrUpdate. */
public final class SqlVirtualMachineGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/CreateOrUpdateSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine group.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager
            .sqlVirtualMachineGroups()
            .define("testvmgroup")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("mytag", "myval"))
            .withSqlImageOffer("SQL2016-WS2016")
            .withSqlImageSku(SqlVmGroupImageSku.ENTERPRISE)
            .withWsfcDomainProfile(
                new WsfcDomainProfile()
                    .withDomainFqdn("testdomain.com")
                    .withOuPath("OU=WSCluster,DC=testdomain,DC=com")
                    .withClusterBootstrapAccount("testrpadmin")
                    .withClusterOperatorAccount("testrp@testdomain.com")
                    .withSqlServiceAccount("sqlservice@testdomain.com")
                    .withStorageAccountUrl("https://storgact.blob.core.windows.net/")
                    .withStorageAccountPrimaryKey("<primary storage access key>")
                    .withClusterSubnetType(ClusterSubnetType.MULTI_SUBNET))
            .create();
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

### SqlVirtualMachineGroups_Delete

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachineGroups Delete. */
public final class SqlVirtualMachineGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/DeleteSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Deletes a SQL virtual machine group.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void deletesASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineGroups().delete("testrg", "testvmgroup", Context.NONE);
    }
}
```

### SqlVirtualMachineGroups_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachineGroups GetByResourceGroup. */
public final class SqlVirtualMachineGroupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/GetSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Gets a SQL virtual machine group.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineGroups().getByResourceGroupWithResponse("testrg", "testvmgroup", Context.NONE);
    }
}
```

### SqlVirtualMachineGroups_List

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachineGroups List. */
public final class SqlVirtualMachineGroupsListSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/ListSubscriptionSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Gets all SQL virtual machine groups in a subscription.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsAllSQLVirtualMachineGroupsInASubscription(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineGroups().list(Context.NONE);
    }
}
```

### SqlVirtualMachineGroups_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachineGroups ListByResourceGroup. */
public final class SqlVirtualMachineGroupsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/ListByResourceGroupSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Gets all SQL virtual machine groups in a resource group.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsAllSQLVirtualMachineGroupsInAResourceGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachineGroups().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### SqlVirtualMachineGroups_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlVirtualMachineGroup;
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlVirtualMachineGroups Update. */
public final class SqlVirtualMachineGroupsUpdateSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/UpdateSqlVirtualMachineGroup.json
     */
    /**
     * Sample code: Updates a SQL virtual machine group tags.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void updatesASQLVirtualMachineGroupTags(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        SqlVirtualMachineGroup resource =
            manager
                .sqlVirtualMachineGroups()
                .getByResourceGroupWithResponse("testrg", "testvmgroup", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlVirtualMachines_CreateOrUpdate

```java
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
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlManagementMode;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlServerLicenseType;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlStorageSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlStorageUpdateSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlTempDbSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlWorkloadType;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlWorkloadTypeUpdateSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.StorageConfigurationSettings;
import com.azure.resourcemanager.sqlvirtualmachine.models.StorageWorkloadType;
import com.azure.resourcemanager.sqlvirtualmachine.models.WsfcDomainCredentials;
import java.util.Arrays;

/** Samples for SqlVirtualMachines CreateOrUpdate. */
public final class SqlVirtualMachinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/CreateOrUpdateSqlVirtualMachineStorageConfigurationEXTEND.json
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
        manager
            .sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .withStorageConfigurationSettings(
                new StorageConfigurationSettings()
                    .withSqlDataSettings(new SqlStorageSettings().withLuns(Arrays.asList(2)))
                    .withDiskConfigurationType(DiskConfigurationType.EXTEND))
            .create();
    }

    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/CreateOrUpdateVirtualMachineWithVMGroup.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine and joins it to a SQL virtual machine group.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesASQLVirtualMachineAndJoinsItToASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager
            .sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm2")
            .withWsfcDomainCredentials(
                new WsfcDomainCredentials()
                    .withClusterBootstrapAccountPassword("<Password>")
                    .withClusterOperatorAccountPassword("<Password>")
                    .withSqlServiceAccountPassword("<Password>"))
            .withWsfcStaticIp("10.0.0.7")
            .create();
    }

    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/CreateOrUpdateSqlVirtualMachineAutomatedBackupWeekly.json
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
        manager
            .sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .withSqlServerLicenseType(SqlServerLicenseType.PAYG)
            .withSqlManagement(SqlManagementMode.FULL)
            .withSqlImageSku(SqlImageSku.ENTERPRISE)
            .withAutoPatchingSettings(
                new AutoPatchingSettings()
                    .withEnable(true)
                    .withDayOfWeek(DayOfWeek.SUNDAY)
                    .withMaintenanceWindowStartingHour(2)
                    .withMaintenanceWindowDuration(60))
            .withAutoBackupSettings(
                new AutoBackupSettings()
                    .withEnable(true)
                    .withEnableEncryption(true)
                    .withRetentionPeriod(17)
                    .withStorageAccountUrl("https://teststorage.blob.core.windows.net/")
                    .withStorageContainerName("testcontainer")
                    .withStorageAccessKey("<primary storage access key>")
                    .withPassword("<Password>")
                    .withBackupSystemDbs(true)
                    .withBackupScheduleType(BackupScheduleType.MANUAL)
                    .withFullBackupFrequency(FullBackupFrequencyType.WEEKLY)
                    .withDaysOfWeek(Arrays.asList(AutoBackupDaysOfWeek.MONDAY, AutoBackupDaysOfWeek.FRIDAY))
                    .withFullBackupStartTime(6)
                    .withFullBackupWindowHours(11)
                    .withLogBackupFrequency(10))
            .withKeyVaultCredentialSettings(new KeyVaultCredentialSettings().withEnable(false))
            .withServerConfigurationsManagementSettings(
                new ServerConfigurationsManagementSettings()
                    .withSqlConnectivityUpdateSettings(
                        new SqlConnectivityUpdateSettings()
                            .withConnectivityType(ConnectivityType.PRIVATE)
                            .withPort(1433)
                            .withSqlAuthUpdateUsername("sqllogin")
                            .withSqlAuthUpdatePassword("<password>"))
                    .withSqlWorkloadTypeUpdateSettings(
                        new SqlWorkloadTypeUpdateSettings().withSqlWorkloadType(SqlWorkloadType.OLTP))
                    .withSqlStorageUpdateSettings(
                        new SqlStorageUpdateSettings()
                            .withDiskCount(1)
                            .withStartingDeviceId(2)
                            .withDiskConfigurationType(DiskConfigurationType.NEW))
                    .withAdditionalFeaturesServerConfigurations(
                        new AdditionalFeaturesServerConfigurations().withIsRServicesEnabled(false)))
            .create();
    }

    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/CreateOrUpdateSqlVirtualMachineStorageConfigurationNEW.json
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
        manager
            .sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .withStorageConfigurationSettings(
                new StorageConfigurationSettings()
                    .withSqlDataSettings(
                        new SqlStorageSettings().withLuns(Arrays.asList(0)).withDefaultFilePath("F:\\folderpath\\"))
                    .withSqlLogSettings(
                        new SqlStorageSettings().withLuns(Arrays.asList(1)).withDefaultFilePath("G:\\folderpath\\"))
                    .withSqlTempDbSettings(
                        new SqlTempDbSettings()
                            .withDataFileSize(256)
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
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/CreateOrUpdateSqlVirtualMachineMAX.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine with max parameters.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesASQLVirtualMachineWithMaxParameters(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager
            .sqlVirtualMachines()
            .define("testvm")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withVirtualMachineResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Compute/virtualMachines/testvm")
            .withSqlServerLicenseType(SqlServerLicenseType.PAYG)
            .withSqlManagement(SqlManagementMode.FULL)
            .withLeastPrivilegeMode(LeastPrivilegeMode.ENABLED)
            .withSqlImageSku(SqlImageSku.ENTERPRISE)
            .withAutoPatchingSettings(
                new AutoPatchingSettings()
                    .withEnable(true)
                    .withDayOfWeek(DayOfWeek.SUNDAY)
                    .withMaintenanceWindowStartingHour(2)
                    .withMaintenanceWindowDuration(60))
            .withAutoBackupSettings(
                new AutoBackupSettings()
                    .withEnable(true)
                    .withEnableEncryption(true)
                    .withRetentionPeriod(17)
                    .withStorageAccountUrl("https://teststorage.blob.core.windows.net/")
                    .withStorageContainerName("testcontainer")
                    .withStorageAccessKey("<primary storage access key>")
                    .withPassword("<Password>")
                    .withBackupSystemDbs(true)
                    .withBackupScheduleType(BackupScheduleType.MANUAL)
                    .withFullBackupFrequency(FullBackupFrequencyType.DAILY)
                    .withFullBackupStartTime(6)
                    .withFullBackupWindowHours(11)
                    .withLogBackupFrequency(10))
            .withKeyVaultCredentialSettings(new KeyVaultCredentialSettings().withEnable(false))
            .withServerConfigurationsManagementSettings(
                new ServerConfigurationsManagementSettings()
                    .withSqlConnectivityUpdateSettings(
                        new SqlConnectivityUpdateSettings()
                            .withConnectivityType(ConnectivityType.PRIVATE)
                            .withPort(1433)
                            .withSqlAuthUpdateUsername("sqllogin")
                            .withSqlAuthUpdatePassword("<password>"))
                    .withSqlWorkloadTypeUpdateSettings(
                        new SqlWorkloadTypeUpdateSettings().withSqlWorkloadType(SqlWorkloadType.OLTP))
                    .withSqlStorageUpdateSettings(
                        new SqlStorageUpdateSettings()
                            .withDiskCount(1)
                            .withStartingDeviceId(2)
                            .withDiskConfigurationType(DiskConfigurationType.NEW))
                    .withAdditionalFeaturesServerConfigurations(
                        new AdditionalFeaturesServerConfigurations().withIsRServicesEnabled(false))
                    .withSqlInstanceSettings(
                        new SqlInstanceSettings()
                            .withCollation("SQL_Latin1_General_CP1_CI_AS")
                            .withMaxDop(8)
                            .withIsOptimizeForAdHocWorkloadsEnabled(true)
                            .withMinServerMemoryMB(0)
                            .withMaxServerMemoryMB(128)
                            .withIsLpimEnabled(true)
                            .withIsIfiEnabled(true)))
            .withAssessmentSettings(
                new AssessmentSettings()
                    .withEnable(true)
                    .withRunImmediately(true)
                    .withSchedule(
                        new Schedule()
                            .withEnable(true)
                            .withWeeklyInterval(1)
                            .withDayOfWeek(AssessmentDayOfWeek.SUNDAY)
                            .withStartTime("23:17")))
            .withEnableAutomaticUpgrade(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/CreateOrUpdateSqlVirtualMachineMIN.json
     */
    /**
     * Sample code: Creates or updates a SQL virtual machine with min parameters.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void createsOrUpdatesASQLVirtualMachineWithMinParameters(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager
            .sqlVirtualMachines()
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
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachines Delete. */
public final class SqlVirtualMachinesDeleteSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/DeleteSqlVirtualMachine.json
     */
    /**
     * Sample code: Deletes a SQL virtual machine.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void deletesASQLVirtualMachine(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().delete("testrg", "testvm1", Context.NONE);
    }
}
```

### SqlVirtualMachines_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachines GetByResourceGroup. */
public final class SqlVirtualMachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/GetSqlVirtualMachine.json
     */
    /**
     * Sample code: Gets a SQL virtual machine.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsASQLVirtualMachine(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().getByResourceGroupWithResponse("testrg", "testvm", null, Context.NONE);
    }
}
```

### SqlVirtualMachines_List

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachines List. */
public final class SqlVirtualMachinesListSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/ListSubscriptionSqlVirtualMachine.json
     */
    /**
     * Sample code: Gets all SQL virtual machines in a subscription.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsAllSQLVirtualMachinesInASubscription(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().list(Context.NONE);
    }
}
```

### SqlVirtualMachines_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachines ListByResourceGroup. */
public final class SqlVirtualMachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/ListByResourceGroupSqlVirtualMachine.json
     */
    /**
     * Sample code: Gets all SQL virtual machines in a resource group.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsAllSQLVirtualMachinesInAResourceGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### SqlVirtualMachines_ListBySqlVmGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachines ListBySqlVmGroup. */
public final class SqlVirtualMachinesListBySqlVmGroupSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/ListBySqlVirtualMachineGroupSqlVirtualMachine.json
     */
    /**
     * Sample code: Gets the list of sql virtual machines in a SQL virtual machine group.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void getsTheListOfSqlVirtualMachinesInASQLVirtualMachineGroup(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().listBySqlVmGroup("testrg", "testvm", Context.NONE);
    }
}
```

### SqlVirtualMachines_Redeploy

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachines Redeploy. */
public final class SqlVirtualMachinesRedeploySamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/RedeploySqlVirtualMachine.json
     */
    /**
     * Sample code: Uninstalls and reinstalls the SQL Iaas Extension.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void uninstallsAndReinstallsTheSQLIaasExtension(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().redeploy("testrg", "testvm", Context.NONE);
    }
}
```

### SqlVirtualMachines_StartAssessment

```java
import com.azure.core.util.Context;

/** Samples for SqlVirtualMachines StartAssessment. */
public final class SqlVirtualMachinesStartAssessmentSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/StartAssessmentOnSqlVirtualMachine.json
     */
    /**
     * Sample code: Starts Assessment on SQL virtual machine.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void startsAssessmentOnSQLVirtualMachine(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        manager.sqlVirtualMachines().startAssessment("testrg", "testvm", Context.NONE);
    }
}
```

### SqlVirtualMachines_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.sqlvirtualmachine.models.SqlVirtualMachine;
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlVirtualMachines Update. */
public final class SqlVirtualMachinesUpdateSamples {
    /*
     * x-ms-original-file: specification/sqlvirtualmachine/resource-manager/Microsoft.SqlVirtualMachine/preview/2022-07-01-preview/examples/UpdateSqlVirtualMachine.json
     */
    /**
     * Sample code: Updates a SQL virtual machine tags.
     *
     * @param manager Entry point to SqlVirtualMachineManager.
     */
    public static void updatesASQLVirtualMachineTags(
        com.azure.resourcemanager.sqlvirtualmachine.SqlVirtualMachineManager manager) {
        SqlVirtualMachine resource =
            manager
                .sqlVirtualMachines()
                .getByResourceGroupWithResponse("testrg", "testvm", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

