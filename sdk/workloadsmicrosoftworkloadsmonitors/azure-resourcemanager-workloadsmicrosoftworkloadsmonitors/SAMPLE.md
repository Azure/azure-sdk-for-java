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

## SapLandscapeMonitor

- [Create](#saplandscapemonitor_create)
- [Delete](#saplandscapemonitor_delete)
- [Get](#saplandscapemonitor_get)
- [List](#saplandscapemonitor_list)
- [Update](#saplandscapemonitor_update)
### Monitors_Create

```java
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.ManagedRGConfiguration;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.ManagedServiceIdentity;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.RoutingPreference;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors Create. */
public final class MonitorsCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Monitors/monitors_Create_UserAssignedIdentity.json
     */
    /**
     * Sample code: Create a SAP monitor with user assigned identity.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorWithUserAssignedIdentity(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .monitors()
            .define("mySapMonitor")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/userassignedmsi",
                            new UserAssignedIdentity()))
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED))
            .withAppLocation("westus")
            .withRoutingPreference(RoutingPreference.ROUTE_ALL)
            .withManagedResourceGroupConfiguration(new ManagedRGConfiguration().withName("myManagedRg"))
            .withLogAnalyticsWorkspaceArmId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.operationalinsights/workspaces/myWorkspace")
            .withMonitorSubnet(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/mySubnet")
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Monitors/monitors_Create.json
     */
    /**
     * Sample code: Create a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitor(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .monitors()
            .define("mySapMonitor")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withAppLocation("westus")
            .withRoutingPreference(RoutingPreference.ROUTE_ALL)
            .withManagedResourceGroupConfiguration(new ManagedRGConfiguration().withName("myManagedRg"))
            .withLogAnalyticsWorkspaceArmId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.operationalinsights/workspaces/myWorkspace")
            .withMonitorSubnet(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/mySubnet")
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Monitors/monitors_Create_Identity.json
     */
    /**
     * Sample code: Create a SAP monitor with system assigned identity.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorWithSystemAssignedIdentity(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .monitors()
            .define("mySapMonitor")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .withAppLocation("westus")
            .withRoutingPreference(RoutingPreference.ROUTE_ALL)
            .withManagedResourceGroupConfiguration(new ManagedRGConfiguration().withName("myManagedRg"))
            .withLogAnalyticsWorkspaceArmId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.operationalinsights/workspaces/myWorkspace")
            .withMonitorSubnet(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/mySubnet")
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

### Monitors_Delete

```java
/** Samples for Monitors Delete. */
public final class MonitorsDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Monitors/monitors_Delete.json
     */
    /**
     * Sample code: Deletes a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deletesASAPMonitor(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager.monitors().delete("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_GetByResourceGroup

```java
/** Samples for Monitors GetByResourceGroup. */
public final class MonitorsGetByResourceGroup {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Monitors/monitors_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitor(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Monitors/monitors_List.json
     */
    /**
     * Sample code: List all SAP monitors in a subscription.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllSAPMonitorsInASubscription(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager.monitors().list(com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
/** Samples for Monitors ListByResourceGroup. */
public final class MonitorsListByResourceGrou {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Monitors/monitors_ListByRG.json
     */
    /**
     * Sample code: List all SAP monitors in a resource group.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllSAPMonitorsInAResourceGroup(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager.monitors().listByResourceGroup("example-rg", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.ManagedServiceIdentity;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.Monitor;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors Update. */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Monitors/monitors_PatchTags_Delete.json
     */
    /**
     * Sample code: Delete Tags field of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deleteTagsFieldOfASAPMonitor(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        Monitor resource =
            manager
                .monitors()
                .getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
            .apply();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Monitors/monitors_PatchTags.json
     */
    /**
     * Sample code: Update Tags field of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void updateTagsFieldOfASAPMonitor(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        Monitor resource =
            manager
                .monitors()
                .getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/operations/preview/2023-10-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void operations(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProviderInstances_Create

```java
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.DB2ProviderInstanceProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.HanaDbProviderInstanceProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.MsSqlServerProviderInstanceProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.PrometheusHaClusterProviderInstanceProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.PrometheusOSProviderInstanceProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.SapNetWeaverProviderInstanceProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.SslPreference;
import java.util.Arrays;

/** Samples for ProviderInstances Create. */
public final class ProviderInstancesCreateSam {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/MsSqlServerProviderInstance/MsSqlServerProviderInstance_Create.json
     */
    /**
     * Sample code: Create a MsSqlServer provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAMsSqlServerProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Db2ProviderInstances/Db2ProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a Db2 provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createADb2ProviderWithRootCertificate(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/ProviderInstances/ProviderInstances_Create.json
     */
    /**
     * Sample code: Create a SAP monitor Hana provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorHanaProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/NetWeaverProviderInstances/NetWeaverProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a SAP monitor NetWeaver provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorNetWeaverProviderWithRootCertificate(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/MsSqlServerProviderInstance/MsSqlServerProviderInstance_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a MsSqlServer provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAMsSqlServerProviderWithRootCertificate(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/PrometheusHaClusterProviderInstances/PrometheusHaClusterProviderInstances_Create.json
     */
    /**
     * Sample code: Create a PrometheusHaCluster provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAPrometheusHaClusterProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/PrometheusHaClusterProviderInstances/PrometheusHaClusterProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a PrometheusHaCluster provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAPrometheusHaClusterProviderWithRootCertificate(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Db2ProviderInstances/Db2ProviderInstances_Create.json
     */
    /**
     * Sample code: Create a Db2 provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createADb2Provider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/PrometheusOSProviderInstances/PrometheusOSProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a OS provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAOSProviderWithRootCertificate(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/PrometheusOSProviderInstances/PrometheusOSProviderInstances_Create.json
     */
    /**
     * Sample code: Create a OS provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAOSProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/NetWeaverProviderInstances/NetWeaverProviderInstances_Create.json
     */
    /**
     * Sample code: Create a SAP monitor NetWeaver provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorNetWeaverProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/ProviderInstances/ProviderInstances_Create_Root_Certificate.json
     */
    /**
     * Sample code: Create a SAP monitor Hana provider with Root Certificate.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASAPMonitorHanaProviderWithRootCertificate(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
public final class ProviderInstancesDeleteSam {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/ProviderInstances/ProviderInstances_Delete.json
     */
    /**
     * Sample code: Deletes a SAP monitor provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deletesASAPMonitorProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/ProviderInstances/ProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor Hana provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitorHanaProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/PrometheusHaClusterProviderInstances/PrometheusHaClusterProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a PrometheusHaCluster provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfAPrometheusHaClusterProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/PrometheusOSProviderInstances/PrometheusOSProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a OS provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfAOSProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/Db2ProviderInstances/Db2ProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a Db2 provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfADb2Provider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/MsSqlServerProviderInstance/MsSqlServerProviderInstance_Get.json
     */
    /**
     * Sample code: Get properties of a MsSqlServer provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfAMsSqlServerProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/NetWeaverProviderInstances/NetWeaverProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor NetWeaver provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitorNetWeaverProvider(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderInstances_List

```java
/** Samples for ProviderInstances List. */
public final class ProviderInstancesListSampl {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/ProviderInstances/ProviderInstances_List.json
     */
    /**
     * Sample code: List all SAP monitors providers in a subscription.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllSAPMonitorsProvidersInASubscription(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager.providerInstances().list("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### SapLandscapeMonitor_Create

```java
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.fluent.models.SapLandscapeMonitorInner;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.SapLandscapeMonitorMetricThresholds;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.SapLandscapeMonitorPropertiesGrouping;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.SapLandscapeMonitorSidMapping;
import java.util.Arrays;

/** Samples for SapLandscapeMonitor Create. */
public final class SapLandscapeMonitorCreateS {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/SapLandscapeMonitor/SapLandscapeMonitor_Create.json
     */
    /**
     * Sample code: Create for SAP Landscape monitor Dashboard.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createForSAPLandscapeMonitorDashboard(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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
public final class SapLandscapeMonitorDeleteS {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/SapLandscapeMonitor/SapLandscapeMonitor_Delete.json
     */
    /**
     * Sample code: Deletes SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deletesSAPMonitor(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .sapLandscapeMonitors()
            .deleteByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### SapLandscapeMonitor_Get

```java
/** Samples for SapLandscapeMonitor Get. */
public final class SapLandscapeMonitorGetSamp {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/SapLandscapeMonitor/SapLandscapeMonitor_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitor(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .sapLandscapeMonitors()
            .getWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### SapLandscapeMonitor_List

```java
/** Samples for SapLandscapeMonitor List. */
public final class SapLandscapeMonitorListSam {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/SapLandscapeMonitor/SapLandscapeMonitor_List.json
     */
    /**
     * Sample code: Get properties of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitor(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
        manager
            .sapLandscapeMonitors()
            .listWithResponse("myResourceGroup", "mySapMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### SapLandscapeMonitor_Update

```java
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.fluent.models.SapLandscapeMonitorInner;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.SapLandscapeMonitorMetricThresholds;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.SapLandscapeMonitorPropertiesGrouping;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.models.SapLandscapeMonitorSidMapping;
import java.util.Arrays;

/** Samples for SapLandscapeMonitor Update. */
public final class SapLandscapeMonitorUpdateS {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/monitors/preview/2023-10-01-preview/examples/SapLandscapeMonitor/SapLandscapeMonitor_Update.json
     */
    /**
     * Sample code: Update SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void updateSAPMonitor(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsmonitors.WorkloadsManager manager) {
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

