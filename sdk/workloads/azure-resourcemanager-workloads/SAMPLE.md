# Code snippets and samples


## Monitors

- [Create](#monitors_create)
- [Delete](#monitors_delete)
- [GetByResourceGroup](#monitors_getbyresourcegroup)
- [List](#monitors_list)
- [ListByResourceGroup](#monitors_listbyresourcegroup)
- [Update](#monitors_update)

## Operations

- [List](#operations_list)

## ProviderInstances

- [Create](#providerinstances_create)
- [Delete](#providerinstances_delete)
- [Get](#providerinstances_get)
- [List](#providerinstances_list)

## ResourceProvider

- [SapAvailabilityZoneDetails](#resourceprovider_sapavailabilityzonedetails)
- [SapDiskConfigurations](#resourceprovider_sapdiskconfigurations)
- [SapSizingRecommendations](#resourceprovider_sapsizingrecommendations)
- [SapSupportedSku](#resourceprovider_sapsupportedsku)

## SapApplicationServerInstances

- [Create](#sapapplicationserverinstances_create)
- [Delete](#sapapplicationserverinstances_delete)
- [Get](#sapapplicationserverinstances_get)
- [List](#sapapplicationserverinstances_list)
- [StartInstance](#sapapplicationserverinstances_startinstance)
- [StopInstance](#sapapplicationserverinstances_stopinstance)
- [Update](#sapapplicationserverinstances_update)

## SapCentralInstances

- [Create](#sapcentralinstances_create)
- [Delete](#sapcentralinstances_delete)
- [Get](#sapcentralinstances_get)
- [List](#sapcentralinstances_list)
- [StartInstance](#sapcentralinstances_startinstance)
- [StopInstance](#sapcentralinstances_stopinstance)
- [Update](#sapcentralinstances_update)

## SapDatabaseInstances

- [Create](#sapdatabaseinstances_create)
- [Delete](#sapdatabaseinstances_delete)
- [Get](#sapdatabaseinstances_get)
- [List](#sapdatabaseinstances_list)
- [StartInstance](#sapdatabaseinstances_startinstance)
- [StopInstance](#sapdatabaseinstances_stopinstance)
- [Update](#sapdatabaseinstances_update)

## SapLandscapeMonitor

- [Create](#saplandscapemonitor_create)
- [Delete](#saplandscapemonitor_delete)
- [Get](#saplandscapemonitor_get)
- [List](#saplandscapemonitor_list)
- [Update](#saplandscapemonitor_update)

## SapVirtualInstances

- [Create](#sapvirtualinstances_create)
- [Delete](#sapvirtualinstances_delete)
- [GetByResourceGroup](#sapvirtualinstances_getbyresourcegroup)
- [List](#sapvirtualinstances_list)
- [ListByResourceGroup](#sapvirtualinstances_listbyresourcegroup)
- [Start](#sapvirtualinstances_start)
- [Stop](#sapvirtualinstances_stop)
- [Update](#sapvirtualinstances_update)
### Monitors_Create

```java
import com.azure.resourcemanager.workloads.models.ManagedRGConfiguration;
import com.azure.resourcemanager.workloads.models.RoutingPreference;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors Create. */
public final class MonitorsCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/monitors_Create.json
     */
    /**
     * Sample code: Create a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .monitors()
            .define("mySapMonitor")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key", "value"))
            .withAppLocation("westus")
            .withRoutingPreference(RoutingPreference.ROUTE_ALL)
            .withManagedResourceGroupConfiguration(new ManagedRGConfiguration().withName("myManagedRg"))
            .withLogAnalyticsWorkspaceArmId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.operationalinsights/workspaces/myWorkspace")
            .withMonitorSubnet(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/mySubnet")
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

### Monitors_Delete

```java
/** Samples for Monitors Delete. */
public final class MonitorsDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/monitors_Delete.json
     */
    /**
     * Sample code: Deletes a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deletesASAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.monitors().delete("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_GetByResourceGroup

```java
/** Samples for Monitors GetByResourceGroup. */
public final class MonitorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/monitors_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .monitors()
            .getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_List

```java
/** Samples for Monitors List. */
public final class MonitorsListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/monitors_List.json
     */
    /**
     * Sample code: List all SAP monitors in a subscription.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllSAPMonitorsInASubscription(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.monitors().list(com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
/** Samples for Monitors ListByResourceGroup. */
public final class MonitorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/monitors_ListByRG.json
     */
    /**
     * Sample code: List all SAP monitors in a resource group.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllSAPMonitorsInAResourceGroup(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.monitors().listByResourceGroup("example-rg", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.resourcemanager.workloads.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.workloads.models.Monitor;
import com.azure.resourcemanager.workloads.models.UserAssignedServiceIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors Update. */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/monitors_PatchTags_Delete.json
     */
    /**
     * Sample code: Delete Tags field of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deleteTagsFieldOfASAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        Monitor resource =
            manager
                .monitors()
                .getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withIdentity(new UserAssignedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
            .apply();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/monitors_PatchTags.json
     */
    /**
     * Sample code: Update Tags field of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void updateTagsFieldOfASAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        Monitor resource =
            manager
                .monitors()
                .getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("testkey", "testvalue"))
            .withIdentity(new UserAssignedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
            .apply();
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void operations(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProviderInstances_Create

```java
import com.azure.resourcemanager.workloads.models.DB2ProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.HanaDbProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.MsSqlServerProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.PrometheusHaClusterProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.PrometheusOSProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.SapNetWeaverProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.SslPreference;
import java.util.Arrays;

/** Samples for ProviderInstances Create. */
public final class ProviderInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/MsSqlServerProviderInstance_Create.json
     */
    /**
     * Sample code: Create a MsSqlServer provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAMsSqlServerProvider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new MsSqlServerProviderInstanceProperties()
                    .withHostname("hostname")
                    .withDbPort("5912")
                    .withDbUsername("user")
                    .withDbPassword("fakeTokenPlaceholder")
                    .withDbPasswordUri("fakeTokenPlaceholder")
                    .withSapSid("sid")
                    .withSslPreference(SslPreference.SERVER_CERTIFICATE)
                    .withSslCertificateUri("https://storageaccount.blob.core.windows.net/containername/filename"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/Db2ProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a Db2 provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createADb2ProviderWithRootCertificate(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new DB2ProviderInstanceProperties()
                    .withHostname("hostname")
                    .withDbName("dbName")
                    .withDbPort("dbPort")
                    .withDbUsername("username")
                    .withDbPassword("fakeTokenPlaceholder")
                    .withDbPasswordUri("fakeTokenPlaceholder")
                    .withSapSid("SID")
                    .withSslPreference(SslPreference.ROOT_CERTIFICATE))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/ProviderInstances_Create.json
     */
    /**
     * Sample code: Create a SAP monitor Hana provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorHanaProvider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new HanaDbProviderInstanceProperties()
                    .withHostname("name")
                    .withDbName("db")
                    .withSqlPort("0000")
                    .withInstanceNumber("00")
                    .withDbUsername("user")
                    .withDbPassword("fakeTokenPlaceholder")
                    .withDbPasswordUri("fakeTokenPlaceholder")
                    .withSslCertificateUri("https://storageaccount.blob.core.windows.net/containername/filename")
                    .withSslHostnameInCertificate("xyz.domain.com")
                    .withSslPreference(SslPreference.SERVER_CERTIFICATE)
                    .withSapSid("SID"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/NetWeaverProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a SAP monitor NetWeaver provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorNetWeaverProviderWithRootCertificate(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new SapNetWeaverProviderInstanceProperties()
                    .withSapSid("SID")
                    .withSapHostname("name")
                    .withSapInstanceNr("00")
                    .withSapHostFileEntries(Arrays.asList("127.0.0.1 name fqdn"))
                    .withSapUsername("username")
                    .withSapPassword("fakeTokenPlaceholder")
                    .withSapPasswordUri("fakeTokenPlaceholder")
                    .withSapClientId("111")
                    .withSapPortNumber("1234")
                    .withSslPreference(SslPreference.ROOT_CERTIFICATE))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/MsSqlServerProviderInstance_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a MsSqlServer provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAMsSqlServerProviderWithRootCertificate(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new MsSqlServerProviderInstanceProperties()
                    .withHostname("hostname")
                    .withDbPort("5912")
                    .withDbUsername("user")
                    .withDbPassword("fakeTokenPlaceholder")
                    .withDbPasswordUri("fakeTokenPlaceholder")
                    .withSapSid("sid")
                    .withSslPreference(SslPreference.ROOT_CERTIFICATE))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/PrometheusHaClusterProviderInstances_Create.json
     */
    /**
     * Sample code: Create a PrometheusHaCluster provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAPrometheusHaClusterProvider(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new PrometheusHaClusterProviderInstanceProperties()
                    .withPrometheusUrl("http://192.168.0.0:9090/metrics")
                    .withHostname("hostname")
                    .withSid("sid")
                    .withClusterName("clusterName")
                    .withSslPreference(SslPreference.SERVER_CERTIFICATE)
                    .withSslCertificateUri("https://storageaccount.blob.core.windows.net/containername/filename"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/PrometheusHaClusterProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a PrometheusHaCluster provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAPrometheusHaClusterProviderWithRootCertificate(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new PrometheusHaClusterProviderInstanceProperties()
                    .withPrometheusUrl("http://192.168.0.0:9090/metrics")
                    .withHostname("hostname")
                    .withSid("sid")
                    .withClusterName("clusterName")
                    .withSslPreference(SslPreference.ROOT_CERTIFICATE))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/Db2ProviderInstances_Create.json
     */
    /**
     * Sample code: Create a Db2 provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createADb2Provider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new DB2ProviderInstanceProperties()
                    .withHostname("hostname")
                    .withDbName("dbName")
                    .withDbPort("dbPort")
                    .withDbUsername("username")
                    .withDbPassword("fakeTokenPlaceholder")
                    .withDbPasswordUri("fakeTokenPlaceholder")
                    .withSapSid("SID")
                    .withSslPreference(SslPreference.SERVER_CERTIFICATE)
                    .withSslCertificateUri("https://storageaccount.blob.core.windows.net/containername/filename"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/PrometheusOSProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a OS provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAOSProviderWithRootCertificate(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new PrometheusOSProviderInstanceProperties()
                    .withPrometheusUrl("http://192.168.0.0:9090/metrics")
                    .withSslPreference(SslPreference.ROOT_CERTIFICATE)
                    .withSapSid("SID"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/PrometheusOSProviderInstances_Create.json
     */
    /**
     * Sample code: Create a OS provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAOSProvider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new PrometheusOSProviderInstanceProperties()
                    .withPrometheusUrl("http://192.168.0.0:9090/metrics")
                    .withSslPreference(SslPreference.SERVER_CERTIFICATE)
                    .withSslCertificateUri("https://storageaccount.blob.core.windows.net/containername/filename")
                    .withSapSid("SID"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/NetWeaverProviderInstances_Create.json
     */
    /**
     * Sample code: Create a SAP monitor NetWeaver provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorNetWeaverProvider(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new SapNetWeaverProviderInstanceProperties()
                    .withSapSid("SID")
                    .withSapHostname("name")
                    .withSapInstanceNr("00")
                    .withSapHostFileEntries(Arrays.asList("127.0.0.1 name fqdn"))
                    .withSapUsername("username")
                    .withSapPassword("fakeTokenPlaceholder")
                    .withSapPasswordUri("fakeTokenPlaceholder")
                    .withSapClientId("111")
                    .withSapPortNumber("1234")
                    .withSslCertificateUri("https://storageaccount.blob.core.windows.net/containername/filename")
                    .withSslPreference(SslPreference.SERVER_CERTIFICATE))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/ProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a SAP monitor Hana provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorHanaProviderWithRootCertificate(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingMonitor("myResourceGroup", "mySapMonitor")
            .withProviderSettings(
                new HanaDbProviderInstanceProperties()
                    .withHostname("name")
                    .withDbName("db")
                    .withSqlPort("0000")
                    .withInstanceNumber("00")
                    .withDbUsername("user")
                    .withDbPassword("fakeTokenPlaceholder")
                    .withDbPasswordUri("fakeTokenPlaceholder")
                    .withSslHostnameInCertificate("xyz.domain.com")
                    .withSslPreference(SslPreference.ROOT_CERTIFICATE)
                    .withSapSid("SID"))
            .create();
    }
}
```

### ProviderInstances_Delete

```java
/** Samples for ProviderInstances Delete. */
public final class ProviderInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/ProviderInstances_Delete.json
     */
    /**
     * Sample code: Deletes a SAP monitor provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deletesASAPMonitorProvider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .delete("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderInstances_Get

```java
/** Samples for ProviderInstances Get. */
public final class ProviderInstancesGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/ProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor Hana provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitorHanaProvider(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/PrometheusHaClusterProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a PrometheusHaCluster provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfAPrometheusHaClusterProvider(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/PrometheusOSProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a OS provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfAOSProvider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/Db2ProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a Db2 provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfADb2Provider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/MsSqlServerProviderInstance_Get.json
     */
    /**
     * Sample code: Get properties of a MsSqlServer provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfAMsSqlServerProvider(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/NetWeaverProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor NetWeaver provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitorNetWeaverProvider(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderInstances_List

```java
/** Samples for ProviderInstances List. */
public final class ProviderInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/ProviderInstances_List.json
     */
    /**
     * Sample code: List all SAP monitors providers in a subscription.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllSAPMonitorsProvidersInASubscription(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.providerInstances().list("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SapAvailabilityZoneDetails

```java
/** Samples for ResourceProvider SapAvailabilityZoneDetails. */
public final class ResourceProviderSapAvailabilityZoneDetailsSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPAvailabilityZoneDetails_northeurope.json
     */
    /**
     * Sample code: SAPAvailabilityZoneDetails_northeurope.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPAvailabilityZoneDetailsNortheurope(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .resourceProviders()
            .sapAvailabilityZoneDetailsWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPAvailabilityZoneDetails_eastus.json
     */
    /**
     * Sample code: SAPAvailabilityZoneDetails_eastus.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPAvailabilityZoneDetailsEastus(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .resourceProviders()
            .sapAvailabilityZoneDetailsWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SapDiskConfigurations

```java
/** Samples for ResourceProvider SapDiskConfigurations. */
public final class ResourceProviderSapDiskConfigurationsSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDiskConfigurations_NonProd.json
     */
    /**
     * Sample code: SAPDiskConfigurations_NonProd.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDiskConfigurationsNonProd(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .resourceProviders()
            .sapDiskConfigurationsWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDiskConfigurations_Prod.json
     */
    /**
     * Sample code: SAPDiskConfigurations_Prod.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDiskConfigurationsProd(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .resourceProviders()
            .sapDiskConfigurationsWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SapSizingRecommendations

```java
/** Samples for ResourceProvider SapSizingRecommendations. */
public final class ResourceProviderSapSizingRecommendationsSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_Distributed.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_Distributed.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSizingRecommendationsS4HANADistributed(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .resourceProviders()
            .sapSizingRecommendationsWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_SingleServer.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_SingleServer.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSizingRecommendationsS4HANASingleServer(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .resourceProviders()
            .sapSizingRecommendationsWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_HA_AvZone.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_DistributedHA_AvZone.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSizingRecommendationsS4HANADistributedHAAvZone(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .resourceProviders()
            .sapSizingRecommendationsWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_HA_AvSet.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_DistributedHA_AvSet.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSizingRecommendationsS4HANADistributedHAAvSet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .resourceProviders()
            .sapSizingRecommendationsWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SapSupportedSku

```java
/** Samples for ResourceProvider SapSupportedSku. */
public final class ResourceProviderSapSupportedSkuSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPSupportedSkus_SingleServer.json
     */
    /**
     * Sample code: SAPSupportedSkus_SingleServer.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSupportedSkusSingleServer(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPSupportedSkus_Distributed.json
     */
    /**
     * Sample code: SAPSupportedSkus_Distributed.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSupportedSkusDistributed(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPSupportedSkus_DistributedHA_AvZone.json
     */
    /**
     * Sample code: SAPSupportedSkus_DistributedHA_AvZone.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSupportedSkusDistributedHAAvZone(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPSupportedSkus_DistributedHA_AvSet.json
     */
    /**
     * Sample code: SAPSupportedSkus_DistributedHA_AvSet.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSupportedSkusDistributedHAAvSet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus", null, com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_Create

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for SapApplicationServerInstances Create. */
public final class SapApplicationServerInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_Create.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Create.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPApplicationServerInstancesCreate(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapApplicationServerInstances()
            .define("app01")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_Create_HA_AvSet.json
     */
    /**
     * Sample code: Create SAP Application Server Instances for HA System with Availability Set.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createSAPApplicationServerInstancesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapApplicationServerInstances()
            .define("app01")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
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

### SapApplicationServerInstances_Delete

```java
/** Samples for SapApplicationServerInstances Delete. */
public final class SapApplicationServerInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_Delete.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Delete.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPApplicationServerInstancesDelete(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapApplicationServerInstances().delete("test-rg", "X00", "app01", com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_Get

```java
/** Samples for SapApplicationServerInstances Get. */
public final class SapApplicationServerInstancesGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_Get.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Get.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPApplicationServerInstancesGet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapApplicationServerInstances()
            .getWithResponse("test-rg", "X00", "app01", com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_List

```java
/** Samples for SapApplicationServerInstances List. */
public final class SapApplicationServerInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_List.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_List.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPApplicationServerInstancesList(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapApplicationServerInstances().list("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_StartInstance

```java
/** Samples for SapApplicationServerInstances StartInstance. */
public final class SapApplicationServerInstancesStartInstanceSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_StartInstance.json
     */
    /**
     * Sample code: Start the SAP Application Server Instance.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void startTheSAPApplicationServerInstance(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapApplicationServerInstances()
            .startInstance("test-rg", "X00", "app01", com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_StopInstance

```java
import com.azure.resourcemanager.workloads.models.StopRequest;

/** Samples for SapApplicationServerInstances StopInstance. */
public final class SapApplicationServerInstancesStopInstanceSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_StopInstance.json
     */
    /**
     * Sample code: Stop the SAP Application Server Instance.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void stopTheSAPApplicationServerInstance(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapApplicationServerInstances()
            .stopInstance(
                "test-rg",
                "X00",
                "app01",
                new StopRequest().withSoftStopTimeoutSeconds(0L),
                com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_Update

```java
import com.azure.resourcemanager.workloads.models.SapApplicationServerInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapApplicationServerInstances Update. */
public final class SapApplicationServerInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_Update.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Update.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPApplicationServerInstancesUpdate(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        SapApplicationServerInstance resource =
            manager
                .sapApplicationServerInstances()
                .getWithResponse("test-rg", "X00", "app01", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1")).apply();
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

### SapCentralInstances_Create

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for SapCentralInstances Create. */
public final class SapCentralInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPCentralInstances_Create_HA_AvSet.json
     */
    /**
     * Sample code: Create SAP Central Instances for HA System with Availability Set.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createSAPCentralInstancesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapCentralInstances()
            .define("centralServer")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPCentralInstances_Create.json
     */
    /**
     * Sample code: SAPCentralInstances_Create.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPCentralInstancesCreate(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapCentralInstances()
            .define("centralServer")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
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

### SapCentralInstances_Delete

```java
/** Samples for SapCentralInstances Delete. */
public final class SapCentralInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPCentralInstances_Delete.json
     */
    /**
     * Sample code: SAPCentralInstances_Delete.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPCentralInstancesDelete(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapCentralInstances().delete("test-rg", "X00", "centralServer", com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_Get

```java
/** Samples for SapCentralInstances Get. */
public final class SapCentralInstancesGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPCentralInstances_Get.json
     */
    /**
     * Sample code: SAPCentralInstances_Get.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPCentralInstancesGet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapCentralInstances()
            .getWithResponse("test-rg", "X00", "centralServer", com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_List

```java
/** Samples for SapCentralInstances List. */
public final class SapCentralInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPCentralInstances_List.json
     */
    /**
     * Sample code: SAPCentralInstances_List.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPCentralInstancesList(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapCentralInstances().list("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_StartInstance

```java
/** Samples for SapCentralInstances StartInstance. */
public final class SapCentralInstancesStartInstanceSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPCentralInstances_StartInstance.json
     */
    /**
     * Sample code: Start the SAP Central Services Instance.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void startTheSAPCentralServicesInstance(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapCentralInstances()
            .startInstance("test-rg", "X00", "centralServer", com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_StopInstance

```java
import com.azure.resourcemanager.workloads.models.StopRequest;

/** Samples for SapCentralInstances StopInstance. */
public final class SapCentralInstancesStopInstanceSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPCentralInstances_StopInstance.json
     */
    /**
     * Sample code: Stop the SAP Central Services Instance.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void stopTheSAPCentralServicesInstance(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapCentralInstances()
            .stopInstance(
                "test-rg",
                "X00",
                "centralServer",
                new StopRequest().withSoftStopTimeoutSeconds(1200L),
                com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_Update

```java
import com.azure.resourcemanager.workloads.models.SapCentralServerInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapCentralInstances Update. */
public final class SapCentralInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPCentralInstances_Update.json
     */
    /**
     * Sample code: SAPCentralInstances_Update.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPCentralInstancesUpdate(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        SapCentralServerInstance resource =
            manager
                .sapCentralInstances()
                .getWithResponse("test-rg", "X00", "centralServer", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1")).apply();
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

### SapDatabaseInstances_Create

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for SapDatabaseInstances Create. */
public final class SapDatabaseInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_Create_HA_AvSet.json
     */
    /**
     * Sample code: Create SAP Database Instances for HA System with Availability Set.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createSAPDatabaseInstancesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapDatabaseInstances()
            .define("databaseServer")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_Create.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Create.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDatabaseInstancesCreate(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapDatabaseInstances()
            .define("databaseServer")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
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

### SapDatabaseInstances_Delete

```java
/** Samples for SapDatabaseInstances Delete. */
public final class SapDatabaseInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_Delete.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Delete.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDatabaseInstancesDelete(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapDatabaseInstances().delete("test-rg", "X00", "databaseServer", com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_Get

```java
/** Samples for SapDatabaseInstances Get. */
public final class SapDatabaseInstancesGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_Get.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Get.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDatabaseInstancesGet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapDatabaseInstances()
            .getWithResponse("test-rg", "X00", "databaseServer", com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_List

```java
/** Samples for SapDatabaseInstances List. */
public final class SapDatabaseInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_List.json
     */
    /**
     * Sample code: SAPDatabaseInstances_List.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDatabaseInstancesList(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapDatabaseInstances().list("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_StartInstance

```java
/** Samples for SapDatabaseInstances StartInstance. */
public final class SapDatabaseInstancesStartInstanceSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_StartInstance.json
     */
    /**
     * Sample code: Start the database instance of the SAP system.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void startTheDatabaseInstanceOfTheSAPSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapDatabaseInstances().startInstance("test-rg", "X00", "db0", com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_StopInstance

```java
import com.azure.resourcemanager.workloads.models.StopRequest;

/** Samples for SapDatabaseInstances StopInstance. */
public final class SapDatabaseInstancesStopInstanceSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_StopInstance.json
     */
    /**
     * Sample code: Stop the database instance of the SAP system.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void stopTheDatabaseInstanceOfTheSAPSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapDatabaseInstances()
            .stopInstance(
                "test-rg",
                "X00",
                "db0",
                new StopRequest().withSoftStopTimeoutSeconds(0L),
                com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_Update

```java
import com.azure.resourcemanager.workloads.models.SapDatabaseInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapDatabaseInstances Update. */
public final class SapDatabaseInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_Update.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Update.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDatabaseInstancesUpdate(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        SapDatabaseInstance resource =
            manager
                .sapDatabaseInstances()
                .getWithResponse("test-rg", "X00", "databaseServer", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1")).apply();
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

### SapLandscapeMonitor_Create

```java
import com.azure.resourcemanager.workloads.fluent.models.SapLandscapeMonitorInner;
import com.azure.resourcemanager.workloads.models.SapLandscapeMonitorMetricThresholds;
import com.azure.resourcemanager.workloads.models.SapLandscapeMonitorPropertiesGrouping;
import com.azure.resourcemanager.workloads.models.SapLandscapeMonitorSidMapping;
import java.util.Arrays;

/** Samples for SapLandscapeMonitor Create. */
public final class SapLandscapeMonitorCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/SapLandscapeMonitor_Create.json
     */
    /**
     * Sample code: Create for SAP Landscape monitor Dashboard.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createForSAPLandscapeMonitorDashboard(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapLandscapeMonitors()
            .createWithResponse(
                "myResourceGroup",
                "mySapMonitor",
                new SapLandscapeMonitorInner()
                    .withGrouping(
                        new SapLandscapeMonitorPropertiesGrouping()
                            .withLandscape(
                                Arrays
                                    .asList(
                                        new SapLandscapeMonitorSidMapping()
                                            .withName("Prod")
                                            .withTopSid(Arrays.asList("SID1", "SID2"))))
                            .withSapApplication(
                                Arrays
                                    .asList(
                                        new SapLandscapeMonitorSidMapping()
                                            .withName("ERP1")
                                            .withTopSid(Arrays.asList("SID1", "SID2")))))
                    .withTopMetricsThresholds(
                        Arrays
                            .asList(
                                new SapLandscapeMonitorMetricThresholds()
                                    .withName("Instance Availability")
                                    .withGreen(90.0F)
                                    .withYellow(75.0F)
                                    .withRed(50.0F))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SapLandscapeMonitor_Delete

```java
/** Samples for SapLandscapeMonitor Delete. */
public final class SapLandscapeMonitorDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/SapLandscapeMonitor_Delete.json
     */
    /**
     * Sample code: Deletes SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deletesSAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapLandscapeMonitors()
            .deleteByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### SapLandscapeMonitor_Get

```java
/** Samples for SapLandscapeMonitor Get. */
public final class SapLandscapeMonitorGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/SapLandscapeMonitor_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapLandscapeMonitors()
            .getWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### SapLandscapeMonitor_List

```java
/** Samples for SapLandscapeMonitor List. */
public final class SapLandscapeMonitorListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/SapLandscapeMonitor_List.json
     */
    /**
     * Sample code: Get properties of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapLandscapeMonitors()
            .listWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### SapLandscapeMonitor_Update

```java
import com.azure.resourcemanager.workloads.fluent.models.SapLandscapeMonitorInner;
import com.azure.resourcemanager.workloads.models.SapLandscapeMonitorMetricThresholds;
import com.azure.resourcemanager.workloads.models.SapLandscapeMonitorPropertiesGrouping;
import com.azure.resourcemanager.workloads.models.SapLandscapeMonitorSidMapping;
import java.util.Arrays;

/** Samples for SapLandscapeMonitor Update. */
public final class SapLandscapeMonitorUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/workloadmonitor/SapLandscapeMonitor_Update.json
     */
    /**
     * Sample code: Update SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void updateSAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapLandscapeMonitors()
            .updateWithResponse(
                "myResourceGroup",
                "mySapMonitor",
                new SapLandscapeMonitorInner()
                    .withGrouping(
                        new SapLandscapeMonitorPropertiesGrouping()
                            .withLandscape(
                                Arrays
                                    .asList(
                                        new SapLandscapeMonitorSidMapping()
                                            .withName("Prod")
                                            .withTopSid(Arrays.asList("SID1", "SID2"))))
                            .withSapApplication(
                                Arrays
                                    .asList(
                                        new SapLandscapeMonitorSidMapping()
                                            .withName("ERP1")
                                            .withTopSid(Arrays.asList("SID1", "SID2")))))
                    .withTopMetricsThresholds(
                        Arrays
                            .asList(
                                new SapLandscapeMonitorMetricThresholds()
                                    .withName("Instance Availability")
                                    .withGreen(90.0F)
                                    .withYellow(75.0F)
                                    .withRed(50.0F))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_Create

```java
import com.azure.resourcemanager.workloads.models.ApplicationServerConfiguration;
import com.azure.resourcemanager.workloads.models.ApplicationServerFullResourceNames;
import com.azure.resourcemanager.workloads.models.CentralServerConfiguration;
import com.azure.resourcemanager.workloads.models.CentralServerFullResourceNames;
import com.azure.resourcemanager.workloads.models.CreateAndMountFileShareConfiguration;
import com.azure.resourcemanager.workloads.models.DatabaseConfiguration;
import com.azure.resourcemanager.workloads.models.DatabaseServerFullResourceNames;
import com.azure.resourcemanager.workloads.models.DeploymentConfiguration;
import com.azure.resourcemanager.workloads.models.DeploymentWithOSConfiguration;
import com.azure.resourcemanager.workloads.models.DiscoveryConfiguration;
import com.azure.resourcemanager.workloads.models.DiskConfiguration;
import com.azure.resourcemanager.workloads.models.DiskSku;
import com.azure.resourcemanager.workloads.models.DiskSkuName;
import com.azure.resourcemanager.workloads.models.DiskVolumeConfiguration;
import com.azure.resourcemanager.workloads.models.ExternalInstallationSoftwareConfiguration;
import com.azure.resourcemanager.workloads.models.HighAvailabilityConfiguration;
import com.azure.resourcemanager.workloads.models.ImageReference;
import com.azure.resourcemanager.workloads.models.LinuxConfiguration;
import com.azure.resourcemanager.workloads.models.LoadBalancerResourceNames;
import com.azure.resourcemanager.workloads.models.MountFileShareConfiguration;
import com.azure.resourcemanager.workloads.models.NetworkConfiguration;
import com.azure.resourcemanager.workloads.models.NetworkInterfaceResourceNames;
import com.azure.resourcemanager.workloads.models.OSProfile;
import com.azure.resourcemanager.workloads.models.OsSapConfiguration;
import com.azure.resourcemanager.workloads.models.SapDatabaseType;
import com.azure.resourcemanager.workloads.models.SapEnvironmentType;
import com.azure.resourcemanager.workloads.models.SapHighAvailabilityType;
import com.azure.resourcemanager.workloads.models.SapInstallWithoutOSConfigSoftwareConfiguration;
import com.azure.resourcemanager.workloads.models.SapProductType;
import com.azure.resourcemanager.workloads.models.SharedStorageResourceNames;
import com.azure.resourcemanager.workloads.models.SingleServerConfiguration;
import com.azure.resourcemanager.workloads.models.SkipFileShareConfiguration;
import com.azure.resourcemanager.workloads.models.SshConfiguration;
import com.azure.resourcemanager.workloads.models.SshKeyPair;
import com.azure.resourcemanager.workloads.models.SshPublicKey;
import com.azure.resourcemanager.workloads.models.StorageConfiguration;
import com.azure.resourcemanager.workloads.models.ThreeTierConfiguration;
import com.azure.resourcemanager.workloads.models.ThreeTierFullResourceNames;
import com.azure.resourcemanager.workloads.models.VirtualMachineConfiguration;
import com.azure.resourcemanager.workloads.models.VirtualMachineResourceNames;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapVirtualInstances Create. */
public final class SapVirtualInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Install_Distributed.json
     */
    /**
     * Sample code: Install SAP Software on Distributed System.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void installSAPSoftwareOnDistributedSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("eastus2")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("{{resourcegrp}}")
                            .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E4ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("8.2")
                                                    .withVersion("8.2.2021091201"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("azureuser")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E4ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("8.2")
                                                    .withVersion("8.2.2021091201"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("azureuser")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("8.2")
                                                    .withVersion("8.2.2021091201"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("azureuser")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L)))
                    .withSoftwareConfiguration(
                        new SapInstallWithoutOSConfigSoftwareConfiguration()
                            .withBomUrl(
                                "https://teststorageaccount.blob.core.windows.net/sapbits/sapfiles/boms/S41909SPS03_v0011ms/S41909SPS03_v0011ms.yaml")
                            .withSapBitsStorageAccountId(
                                "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Storage/storageAccounts/teststorageaccount")
                            .withSoftwareVersion("SAP S/4HANA 1909 SPS 03"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.bpaas.com")))
            .withTags(mapOf("created by", "azureuser"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_HA_AvZone.json
     */
    /**
     * Sample code: Create Infrastructure only for HA System with Availability Zone.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureOnlyForHASystemWithAvailabilityZone(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "fakeTokenPlaceholder")))))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "fakeTokenPlaceholder")))))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "fakeTokenPlaceholder")))))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE))))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_Distributed.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for Distributed System (Recommended).
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithOSConfigurationForDistributedSystemRecommended(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_DetectInstallation_SingleServer.json
     */
    /**
     * Sample code: Detect SAP Software Installation on a Single Server System.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void detectSAPSoftwareInstallationOnASingleServerSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.NON_PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new SingleServerConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                            .withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration()
                                    .withVmSize("Standard_E32ds_v4")
                                    .withImageReference(
                                        new ImageReference()
                                            .withPublisher("RedHat")
                                            .withOffer("RHEL-SAP-HA")
                                            .withSku("84sapha-gen2")
                                            .withVersion("8.4.2021091202"))
                                    .withOsProfile(
                                        new OSProfile()
                                            .withAdminUsername("{your-username}")
                                            .withOsConfiguration(
                                                new LinuxConfiguration()
                                                    .withDisablePasswordAuthentication(true)
                                                    .withSshKeyPair(
                                                        new SshKeyPair()
                                                            .withPublicKey("fakeTokenPlaceholder")
                                                            .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withSoftwareConfiguration(
                        new ExternalInstallationSoftwareConfiguration()
                            .withCentralServerVmId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_DiskDetails_HA_AvSet.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configuration for HA System with Availability Set
     * (Recommended).
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationForHASystemWithAvailabilitySetRecommended(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L)
                                    .withDiskConfiguration(
                                        new DiskConfiguration()
                                            .withDiskVolumeConfigurations(
                                                mapOf(
                                                    "backup",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(2L)
                                                        .withSizeGB(256L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                                    "hana/data",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(4L)
                                                        .withSizeGB(128L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                                    "hana/log",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(3L)
                                                        .withSizeGB(128L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                                    "hana/shared",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(1L)
                                                        .withSizeGB(256L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                                    "os",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(1L)
                                                        .withSizeGB(64L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                                    "usr/sap",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(1L)
                                                        .withSizeGB(128L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS))))))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_Distributed_CreateTrans.json
     */
    /**
     * Sample code: Create Infrastructure with a new SAP Trans Fileshare.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithANewSAPTransFileshare(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withStorageConfiguration(
                                new StorageConfiguration()
                                    .withTransportFileShareConfiguration(
                                        new CreateAndMountFileShareConfiguration()
                                            .withResourceGroup("test-rg")
                                            .withStorageAccountName("input-sa-name"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_Distributed_MountTrans.json
     */
    /**
     * Sample code: Create Infrastructure with an existing SAP Trans Fileshare.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithAnExistingSAPTransFileshare(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withStorageConfiguration(
                                new StorageConfiguration()
                                    .withTransportFileShareConfiguration(
                                        new MountFileShareConfiguration()
                                            .withId("fileshareID")
                                            .withPrivateEndpointId("pe-arm-id"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_HA_AvSet.json
     */
    /**
     * Sample code: Create Infrastructure only for HA System with Availability Set.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureOnlyForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "fakeTokenPlaceholder")))))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "fakeTokenPlaceholder")))))))
                                    .withInstanceCount(5L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "fakeTokenPlaceholder")))))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET))))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_DetectInstallation_HA_AvZone.json
     */
    /**
     * Sample code: Detect SAP Software Installation on an HA System with Availability Zone.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void detectSAPSoftwareInstallationOnAnHASystemWithAvailabilityZone(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("84sapha-gen2")
                                                    .withVersion("8.4.2021091202"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("84sapha-gen2")
                                                    .withVersion("8.4.2021091202"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("84sapha-gen2")
                                                    .withVersion("8.4.2021091202"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))
                    .withSoftwareConfiguration(
                        new ExternalInstallationSoftwareConfiguration()
                            .withCentralServerVmId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_SingleServer.json
     */
    /**
     * Sample code: Create Infrastructure only for Single Server System.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureOnlyForSingleServerSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.NON_PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new SingleServerConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                            .withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration()
                                    .withVmSize("Standard_E32ds_v4")
                                    .withImageReference(
                                        new ImageReference()
                                            .withPublisher("RedHat")
                                            .withOffer("RHEL-SAP")
                                            .withSku("7.4")
                                            .withVersion("7.4.2019062505"))
                                    .withOsProfile(
                                        new OSProfile()
                                            .withAdminUsername("{your-username}")
                                            .withOsConfiguration(
                                                new LinuxConfiguration()
                                                    .withDisablePasswordAuthentication(true)
                                                    .withSsh(
                                                        new SshConfiguration()
                                                            .withPublicKeys(
                                                                Arrays
                                                                    .asList(
                                                                        new SshPublicKey()
                                                                            .withKeyData("fakeTokenPlaceholder")))))))))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_Discover.json
     */
    /**
     * Sample code: Register existing SAP system as Virtual Instance for SAP solutions.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void registerExistingSAPSystemAsVirtualInstanceForSAPSolutions(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("northeurope")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.NON_PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DiscoveryConfiguration()
                    .withCentralServerVmId(
                        "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
            .withTags(mapOf("createdby", "abc@microsoft.com", "test", "abc"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_CustomFullResourceNames_Distributed.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for Distributed System.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForDistributedSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withCustomResourceNames(
                                new ThreeTierFullResourceNames()
                                    .withCentralServer(
                                        new CentralServerFullResourceNames()
                                            .withVirtualMachines(
                                                Arrays
                                                    .asList(
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("ascsvm")
                                                            .withHostname("ascshostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("ascsnic")))
                                                            .withOsDiskName("ascsosdisk")
                                                            .withDataDiskNames(
                                                                mapOf("default", Arrays.asList("ascsdisk0"))))))
                                    .withApplicationServer(
                                        new ApplicationServerFullResourceNames()
                                            .withVirtualMachines(
                                                Arrays
                                                    .asList(
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("appvm0")
                                                            .withHostname("apphostName0")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("appnic0")))
                                                            .withOsDiskName("app0osdisk")
                                                            .withDataDiskNames(
                                                                mapOf("default", Arrays.asList("app0disk0"))),
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("appvm1")
                                                            .withHostname("apphostName1")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("appnic1")))
                                                            .withOsDiskName("app1osdisk")
                                                            .withDataDiskNames(
                                                                mapOf("default", Arrays.asList("app1disk0")))))
                                            .withAvailabilitySetName("appAvSet"))
                                    .withDatabaseServer(
                                        new DatabaseServerFullResourceNames()
                                            .withVirtualMachines(
                                                Arrays
                                                    .asList(
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("dbvm")
                                                            .withHostname("dbhostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("dbnic")))
                                                            .withOsDiskName("dbosdisk")
                                                            .withDataDiskNames(
                                                                mapOf(
                                                                    "hanaData",
                                                                    Arrays.asList("hanadata0", "hanadata1"),
                                                                    "hanaLog",
                                                                    Arrays.asList("hanalog0", "hanalog1", "hanalog2"),
                                                                    "hanaShared",
                                                                    Arrays.asList("hanashared0", "hanashared1"),
                                                                    "usrSap",
                                                                    Arrays.asList("usrsap0"))))))
                                    .withSharedStorage(
                                        new SharedStorageResourceNames()
                                            .withSharedStorageAccountName("storageacc")
                                            .withSharedStorageAccountPrivateEndPointName("peForxNFS"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_CustomFullResourceNames_SingleServer.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for Single Server System.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForSingleServerSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.NON_PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new SingleServerConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                            .withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration()
                                    .withVmSize("Standard_E32ds_v4")
                                    .withImageReference(
                                        new ImageReference()
                                            .withPublisher("RedHat")
                                            .withOffer("RHEL-SAP")
                                            .withSku("7.4")
                                            .withVersion("7.4.2019062505"))
                                    .withOsProfile(
                                        new OSProfile()
                                            .withAdminUsername("{your-username}")
                                            .withOsConfiguration(
                                                new LinuxConfiguration()
                                                    .withDisablePasswordAuthentication(true)
                                                    .withSshKeyPair(
                                                        new SshKeyPair()
                                                            .withPublicKey("fakeTokenPlaceholder")
                                                            .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_CustomFullResourceNames_HA_AvZone.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for HA system with
     * Availability Zone.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForHASystemWithAvailabilityZone(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE))
                            .withCustomResourceNames(
                                new ThreeTierFullResourceNames()
                                    .withCentralServer(
                                        new CentralServerFullResourceNames()
                                            .withVirtualMachines(
                                                Arrays
                                                    .asList(
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("ascsvm")
                                                            .withHostname("ascshostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("ascsnic")))
                                                            .withOsDiskName("ascsosdisk"),
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("ersvm")
                                                            .withHostname("ershostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("ersnic")))
                                                            .withOsDiskName("ersosdisk")))
                                            .withLoadBalancer(
                                                new LoadBalancerResourceNames()
                                                    .withLoadBalancerName("ascslb")
                                                    .withFrontendIpConfigurationNames(
                                                        Arrays.asList("ascsip0", "ersip0"))
                                                    .withBackendPoolNames(Arrays.asList("ascsBackendPool"))
                                                    .withHealthProbeNames(
                                                        Arrays.asList("ascsHealthProbe", "ersHealthProbe"))))
                                    .withApplicationServer(
                                        new ApplicationServerFullResourceNames()
                                            .withVirtualMachines(
                                                Arrays
                                                    .asList(
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("appvm0")
                                                            .withHostname("apphostName0")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("appnic0")))
                                                            .withOsDiskName("app0osdisk")
                                                            .withDataDiskNames(
                                                                mapOf("default", Arrays.asList("app0disk0"))),
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("appvm1")
                                                            .withHostname("apphostName1")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("appnic1")))
                                                            .withOsDiskName("app1osdisk")
                                                            .withDataDiskNames(
                                                                mapOf("default", Arrays.asList("app1disk0"))))))
                                    .withDatabaseServer(
                                        new DatabaseServerFullResourceNames()
                                            .withVirtualMachines(
                                                Arrays
                                                    .asList(
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("dbvmpr")
                                                            .withHostname("dbprhostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("dbprnic")))
                                                            .withOsDiskName("dbprosdisk")
                                                            .withDataDiskNames(
                                                                mapOf(
                                                                    "hanaData",
                                                                    Arrays.asList("hanadatapr0", "hanadatapr1"),
                                                                    "hanaLog",
                                                                    Arrays
                                                                        .asList(
                                                                            "hanalogpr0", "hanalogpr1", "hanalogpr2"),
                                                                    "hanaShared",
                                                                    Arrays.asList("hanasharedpr0", "hanasharedpr1"),
                                                                    "usrSap",
                                                                    Arrays.asList("usrsappr0"))),
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("dbvmsr")
                                                            .withHostname("dbsrhostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("dbsrnic")))
                                                            .withOsDiskName("dbsrosdisk")
                                                            .withDataDiskNames(
                                                                mapOf(
                                                                    "hanaData",
                                                                    Arrays.asList("hanadatasr0", "hanadatasr1"),
                                                                    "hanaLog",
                                                                    Arrays
                                                                        .asList(
                                                                            "hanalogsr0", "hanalogsr1", "hanalogsr2"),
                                                                    "hanaShared",
                                                                    Arrays.asList("hanasharedsr0", "hanasharedsr1"),
                                                                    "usrSap",
                                                                    Arrays.asList("usrsapsr0")))))
                                            .withLoadBalancer(
                                                new LoadBalancerResourceNames()
                                                    .withLoadBalancerName("dblb")
                                                    .withFrontendIpConfigurationNames(Arrays.asList("dbip"))
                                                    .withBackendPoolNames(Arrays.asList("dbBackendPool"))
                                                    .withHealthProbeNames(Arrays.asList("dbHealthProbe"))))
                                    .withSharedStorage(
                                        new SharedStorageResourceNames()
                                            .withSharedStorageAccountName("storageacc")
                                            .withSharedStorageAccountPrivateEndPointName("peForxNFS"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_CustomFullResourceNames_HA_AvSet.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for HA System with
     * Availability Set.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET))
                            .withCustomResourceNames(
                                new ThreeTierFullResourceNames()
                                    .withCentralServer(
                                        new CentralServerFullResourceNames()
                                            .withVirtualMachines(
                                                Arrays
                                                    .asList(
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("ascsvm")
                                                            .withHostname("ascshostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("ascsnic")))
                                                            .withOsDiskName("ascsosdisk"),
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("ersvm")
                                                            .withHostname("ershostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("ersnic")))
                                                            .withOsDiskName("ersosdisk")))
                                            .withAvailabilitySetName("csAvSet")
                                            .withLoadBalancer(
                                                new LoadBalancerResourceNames()
                                                    .withLoadBalancerName("ascslb")
                                                    .withFrontendIpConfigurationNames(
                                                        Arrays.asList("ascsip0", "ersip0"))
                                                    .withBackendPoolNames(Arrays.asList("ascsBackendPool"))
                                                    .withHealthProbeNames(
                                                        Arrays.asList("ascsHealthProbe", "ersHealthProbe"))))
                                    .withApplicationServer(
                                        new ApplicationServerFullResourceNames()
                                            .withVirtualMachines(
                                                Arrays
                                                    .asList(
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("appvm0")
                                                            .withHostname("apphostName0")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("appnic0")))
                                                            .withOsDiskName("app0osdisk")
                                                            .withDataDiskNames(
                                                                mapOf("default", Arrays.asList("app0disk0"))),
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("appvm1")
                                                            .withHostname("apphostName1")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("appnic1")))
                                                            .withOsDiskName("app1osdisk")
                                                            .withDataDiskNames(
                                                                mapOf("default", Arrays.asList("app1disk0")))))
                                            .withAvailabilitySetName("appAvSet"))
                                    .withDatabaseServer(
                                        new DatabaseServerFullResourceNames()
                                            .withVirtualMachines(
                                                Arrays
                                                    .asList(
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("dbvmpr")
                                                            .withHostname("dbprhostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("dbprnic")))
                                                            .withOsDiskName("dbprosdisk")
                                                            .withDataDiskNames(
                                                                mapOf(
                                                                    "hanaData",
                                                                    Arrays.asList("hanadatapr0", "hanadatapr1"),
                                                                    "hanaLog",
                                                                    Arrays
                                                                        .asList(
                                                                            "hanalogpr0", "hanalogpr1", "hanalogpr2"),
                                                                    "hanaShared",
                                                                    Arrays.asList("hanasharedpr0", "hanasharedpr1"),
                                                                    "usrSap",
                                                                    Arrays.asList("usrsappr0"))),
                                                        new VirtualMachineResourceNames()
                                                            .withVmName("dbvmsr")
                                                            .withHostname("dbsrhostName")
                                                            .withNetworkInterfaces(
                                                                Arrays
                                                                    .asList(
                                                                        new NetworkInterfaceResourceNames()
                                                                            .withNetworkInterfaceName("dbsrnic")))
                                                            .withOsDiskName("dbsrosdisk")
                                                            .withDataDiskNames(
                                                                mapOf(
                                                                    "hanaData",
                                                                    Arrays.asList("hanadatasr0", "hanadatasr1"),
                                                                    "hanaLog",
                                                                    Arrays
                                                                        .asList(
                                                                            "hanalogsr0", "hanalogsr1", "hanalogsr2"),
                                                                    "hanaShared",
                                                                    Arrays.asList("hanasharedsr0", "hanasharedsr1"),
                                                                    "usrSap",
                                                                    Arrays.asList("usrsapsr0")))))
                                            .withAvailabilitySetName("dbAvSet")
                                            .withLoadBalancer(
                                                new LoadBalancerResourceNames()
                                                    .withLoadBalancerName("dblb")
                                                    .withFrontendIpConfigurationNames(Arrays.asList("dbip"))
                                                    .withBackendPoolNames(Arrays.asList("dbBackendPool"))
                                                    .withHealthProbeNames(Arrays.asList("dbHealthProbe"))))
                                    .withSharedStorage(
                                        new SharedStorageResourceNames()
                                            .withSharedStorageAccountName("storageacc")
                                            .withSharedStorageAccountPrivateEndPointName("peForxNFS"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_HA_AvSet.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for HA System with Availability Set (Recommended).
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithOSConfigurationForHASystemWithAvailabilitySetRecommended(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_Distributed_SkipTransMount.json
     */
    /**
     * Sample code: Create Infrastructure without SAP Trans Fileshare.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithoutSAPTransFileshare(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withStorageConfiguration(
                                new StorageConfiguration()
                                    .withTransportFileShareConfiguration(new SkipFileShareConfiguration())))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_DiskDetails_Distributed.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configuration for Distributed System (Recommended).
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationForDistributedSystemRecommended(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L)
                                    .withDiskConfiguration(
                                        new DiskConfiguration()
                                            .withDiskVolumeConfigurations(
                                                mapOf(
                                                    "backup",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(2L)
                                                        .withSizeGB(256L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                                    "hana/data",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(4L)
                                                        .withSizeGB(128L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                                    "hana/log",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(3L)
                                                        .withSizeGB(128L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                                    "hana/shared",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(1L)
                                                        .withSizeGB(256L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                                    "os",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(1L)
                                                        .withSizeGB(64L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                                    "usr/sap",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(1L)
                                                        .withSizeGB(128L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_DetectInstallation_Distributed.json
     */
    /**
     * Sample code: Detect SAP Software Installation on a Distributed System.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void detectSAPSoftwareInstallationOnADistributedSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("eastus2")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("{{resourcegrp}}")
                            .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E4ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("84sapha-gen2")
                                                    .withVersion("8.4.2021091202"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("azureuser")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E4ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("84sapha-gen2")
                                                    .withVersion("8.4.2021091202"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("azureuser")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("84sapha-gen2")
                                                    .withVersion("8.4.2021091202"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("azureuser")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(1L)))
                    .withSoftwareConfiguration(
                        new ExternalInstallationSoftwareConfiguration()
                            .withCentralServerVmId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.bpaas.com")))
            .withTags(mapOf("created by", "azureuser"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_DetectInstallation_HA_AvSet.json
     */
    /**
     * Sample code: Detect SAP Software Installation on an HA System with Availability Set.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void detectSAPSoftwareInstallationOnAnHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("84sapha-gen2")
                                                    .withVersion("8.4.2021091202"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("84sapha-gen2")
                                                    .withVersion("8.4.2021091202"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP-HA")
                                                    .withSku("84sapha-gen2")
                                                    .withVersion("8.4.2021091202"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))
                    .withSoftwareConfiguration(
                        new ExternalInstallationSoftwareConfiguration()
                            .withCentralServerVmId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Install_SingleServer.json
     */
    /**
     * Sample code: Install SAP Software on Single Server System.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void installSAPSoftwareOnSingleServerSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("eastus2")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.NON_PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new SingleServerConfiguration()
                            .withAppResourceGroup("test-rg")
                            .withSubnetId(
                                "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/testsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration()
                                    .withVmSize("Standard_E32ds_v4")
                                    .withImageReference(
                                        new ImageReference()
                                            .withPublisher("SUSE")
                                            .withOffer("SLES-SAP")
                                            .withSku("12-sp4-gen2")
                                            .withVersion("2022.02.01"))
                                    .withOsProfile(
                                        new OSProfile()
                                            .withAdminUsername("azureappadmin")
                                            .withOsConfiguration(
                                                new LinuxConfiguration()
                                                    .withDisablePasswordAuthentication(true)
                                                    .withSshKeyPair(
                                                        new SshKeyPair()
                                                            .withPublicKey("fakeTokenPlaceholder")
                                                            .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withSoftwareConfiguration(
                        new SapInstallWithoutOSConfigSoftwareConfiguration()
                            .withBomUrl(
                                "https://teststorageaccount.blob.core.windows.net/sapbits/sapfiles/boms/S41909SPS03_v0011ms/S41909SPS03_v0011ms.yaml")
                            .withSapBitsStorageAccountId(
                                "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Storage/storageAccounts/teststorageaccount")
                            .withSoftwareVersion("SAP S/4HANA 1909 SPS 03"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.bpaas.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_Distributed.json
     */
    /**
     * Sample code: Create Infrastructure only for Distributed System.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureOnlyForDistributedSystem(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "fakeTokenPlaceholder")))))))
                                    .withInstanceCount(1L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "fakeTokenPlaceholder")))))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "fakeTokenPlaceholder")))))))
                                    .withInstanceCount(1L))))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_Discover_CustomMrgStorageAccountName.json
     */
    /**
     * Sample code: Register existing SAP system as Virtual Instance for SAP solutions with optional customizations.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void registerExistingSAPSystemAsVirtualInstanceForSAPSolutionsWithOptionalCustomizations(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("northeurope")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.NON_PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DiscoveryConfiguration()
                    .withCentralServerVmId(
                        "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0")
                    .withManagedRgStorageAccountName("q20saacssgrs"))
            .withTags(mapOf("createdby", "abc@microsoft.com", "test", "abc"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_SingleServer.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for Single Server System (Recommended).
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithOSConfigurationForSingleServerSystemRecommended(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.NON_PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new SingleServerConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                            .withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration()
                                    .withVmSize("Standard_E32ds_v4")
                                    .withImageReference(
                                        new ImageReference()
                                            .withPublisher("RedHat")
                                            .withOffer("RHEL-SAP")
                                            .withSku("7.4")
                                            .withVersion("7.4.2019062505"))
                                    .withOsProfile(
                                        new OSProfile()
                                            .withAdminUsername("{your-username}")
                                            .withOsConfiguration(
                                                new LinuxConfiguration()
                                                    .withDisablePasswordAuthentication(true)
                                                    .withSshKeyPair(
                                                        new SshKeyPair()
                                                            .withPublicKey("fakeTokenPlaceholder")
                                                            .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_DiskDetails_HA_AvZone.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configuration for HA System with Availability Zone
     * (Recommended).
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationForHASystemWithAvailabilityZoneRecommended(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L)
                                    .withDiskConfiguration(
                                        new DiskConfiguration()
                                            .withDiskVolumeConfigurations(
                                                mapOf(
                                                    "backup",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(2L)
                                                        .withSizeGB(256L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                                    "hana/data",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(4L)
                                                        .withSizeGB(128L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                                    "hana/log",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(3L)
                                                        .withSizeGB(128L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                                    "hana/shared",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(1L)
                                                        .withSizeGB(256L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                                    "os",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(1L)
                                                        .withSizeGB(64L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                                    "usr/sap",
                                                    new DiskVolumeConfiguration()
                                                        .withCount(1L)
                                                        .withSizeGB(128L)
                                                        .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS))))))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_HA_AvZone.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for HA System with Availability Zone (Recommended).
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithOSConfigurationForHASystemWithAvailabilityZoneRecommended(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new ThreeTierConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withCentralServer(
                                new CentralServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E16ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withApplicationServer(
                                new ApplicationServerConfiguration()
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_E32ds_v4")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(6L))
                            .withDatabaseServer(
                                new DatabaseConfiguration()
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                                    .withVirtualMachineConfiguration(
                                        new VirtualMachineConfiguration()
                                            .withVmSize("Standard_M32ts")
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("RedHat")
                                                    .withOffer("RHEL-SAP")
                                                    .withSku("7.4")
                                                    .withVersion("7.4.2019062505"))
                                            .withOsProfile(
                                                new OSProfile()
                                                    .withAdminUsername("{your-username}")
                                                    .withOsConfiguration(
                                                        new LinuxConfiguration()
                                                            .withDisablePasswordAuthentication(true)
                                                            .withSshKeyPair(
                                                                new SshKeyPair()
                                                                    .withPublicKey("fakeTokenPlaceholder")
                                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_DiskDetails_SingleServer.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configurations for Single Server System (Recommended).
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationsForSingleServerSystemRecommended(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withEnvironment(SapEnvironmentType.NON_PROD)
            .withSapProduct(SapProductType.S4HANA)
            .withConfiguration(
                new DeploymentWithOSConfiguration()
                    .withAppLocation("eastus")
                    .withInfrastructureConfiguration(
                        new SingleServerConfiguration()
                            .withAppResourceGroup("X00-RG")
                            .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                            .withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration()
                                    .withVmSize("Standard_E32ds_v4")
                                    .withImageReference(
                                        new ImageReference()
                                            .withPublisher("RedHat")
                                            .withOffer("RHEL-SAP")
                                            .withSku("7.4")
                                            .withVersion("7.4.2019062505"))
                                    .withOsProfile(
                                        new OSProfile()
                                            .withAdminUsername("{your-username}")
                                            .withOsConfiguration(
                                                new LinuxConfiguration()
                                                    .withDisablePasswordAuthentication(true)
                                                    .withSshKeyPair(
                                                        new SshKeyPair()
                                                            .withPublicKey("fakeTokenPlaceholder")
                                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withDbDiskConfiguration(
                                new DiskConfiguration()
                                    .withDiskVolumeConfigurations(
                                        mapOf(
                                            "backup",
                                            new DiskVolumeConfiguration()
                                                .withCount(2L)
                                                .withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "hana/data",
                                            new DiskVolumeConfiguration()
                                                .withCount(4L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/log",
                                            new DiskVolumeConfiguration()
                                                .withCount(3L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/shared",
                                            new DiskVolumeConfiguration()
                                                .withCount(1L)
                                                .withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "os",
                                            new DiskVolumeConfiguration()
                                                .withCount(1L)
                                                .withSizeGB(64L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "usr/sap",
                                            new DiskVolumeConfiguration()
                                                .withCount(1L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
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

### SapVirtualInstances_Delete

```java
/** Samples for SapVirtualInstances Delete. */
public final class SapVirtualInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Delete.json
     */
    /**
     * Sample code: SAPVirtualInstances_Delete.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesDelete(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().delete("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_GetByResourceGroup

```java
/** Samples for SapVirtualInstances GetByResourceGroup. */
public final class SapVirtualInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Get.json
     */
    /**
     * Sample code: SAPVirtualInstances_Get.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesGet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .getByResourceGroupWithResponse("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_List

```java
/** Samples for SapVirtualInstances List. */
public final class SapVirtualInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_ListBySubscription.json
     */
    /**
     * Sample code: SAPVirtualInstances_ListBySubscription.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesListBySubscription(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_ListByResourceGroup

```java
/** Samples for SapVirtualInstances ListByResourceGroup. */
public final class SapVirtualInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_ListByResourceGroup.json
     */
    /**
     * Sample code: SAPVirtualInstances_ListByResourceGroup.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesListByResourceGroup(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_Start

```java
/** Samples for SapVirtualInstances Start. */
public final class SapVirtualInstancesStartSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Start.json
     */
    /**
     * Sample code: SAPVirtualInstances_Start.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesStart(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().start("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_Stop

```java
import com.azure.resourcemanager.workloads.models.StopRequest;

/** Samples for SapVirtualInstances Stop. */
public final class SapVirtualInstancesStopSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Stop.json
     */
    /**
     * Sample code: SAPVirtualInstances_Stop.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesStop(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .sapVirtualInstances()
            .stop("test-rg", "X00", new StopRequest().withSoftStopTimeoutSeconds(0L), com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_Update

```java
import com.azure.resourcemanager.workloads.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.workloads.models.SapVirtualInstance;
import com.azure.resourcemanager.workloads.models.UserAssignedServiceIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapVirtualInstances Update. */
public final class SapVirtualInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2022-11-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Update.json
     */
    /**
     * Sample code: SAPVirtualInstances_Update.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesUpdate(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        SapVirtualInstance resource =
            manager
                .sapVirtualInstances()
                .getByResourceGroupWithResponse("test-rg", "X00", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "svi1"))
            .withIdentity(new UserAssignedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
            .apply();
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

