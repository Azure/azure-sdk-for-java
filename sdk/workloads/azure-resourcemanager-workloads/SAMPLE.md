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

## PhpWorkloads

- [CreateOrUpdate](#phpworkloads_createorupdate)
- [Delete](#phpworkloads_delete)
- [GetByResourceGroup](#phpworkloads_getbyresourcegroup)
- [List](#phpworkloads_list)
- [ListByResourceGroup](#phpworkloads_listbyresourcegroup)
- [Update](#phpworkloads_update)

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
- [Update](#sapapplicationserverinstances_update)

## SapCentralInstances

- [Create](#sapcentralinstances_create)
- [Delete](#sapcentralinstances_delete)
- [Get](#sapcentralinstances_get)
- [List](#sapcentralinstances_list)
- [Update](#sapcentralinstances_update)

## SapDatabaseInstances

- [Create](#sapdatabaseinstances_create)
- [Delete](#sapdatabaseinstances_delete)
- [Get](#sapdatabaseinstances_get)
- [List](#sapdatabaseinstances_list)
- [Update](#sapdatabaseinstances_update)

## SapVirtualInstances

- [Create](#sapvirtualinstances_create)
- [Delete](#sapvirtualinstances_delete)
- [GetByResourceGroup](#sapvirtualinstances_getbyresourcegroup)
- [List](#sapvirtualinstances_list)
- [ListByResourceGroup](#sapvirtualinstances_listbyresourcegroup)
- [Start](#sapvirtualinstances_start)
- [Stop](#sapvirtualinstances_stop)
- [Update](#sapvirtualinstances_update)

## Skus

- [List](#skus_list)

## WordpressInstances

- [CreateOrUpdate](#wordpressinstances_createorupdate)
- [Delete](#wordpressinstances_delete)
- [Get](#wordpressinstances_get)
- [List](#wordpressinstances_list)
### Monitors_Create

```java
import com.azure.resourcemanager.workloads.models.ManagedRGConfiguration;
import com.azure.resourcemanager.workloads.models.RoutingPreference;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors Create. */
public final class MonitorsCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/monitors_Create.json
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
import com.azure.core.util.Context;

/** Samples for Monitors Delete. */
public final class MonitorsDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/monitors_Delete.json
     */
    /**
     * Sample code: Deletes a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deletesASAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.monitors().delete("myResourceGroup", "mySapMonitor", Context.NONE);
    }
}
```

### Monitors_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Monitors GetByResourceGroup. */
public final class MonitorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/monitors_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfASAPMonitor(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", Context.NONE);
    }
}
```

### Monitors_List

```java
import com.azure.core.util.Context;

/** Samples for Monitors List. */
public final class MonitorsListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/monitors_List.json
     */
    /**
     * Sample code: List all SAP monitors in a subscription.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllSAPMonitorsInASubscription(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.monitors().list(Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListByResourceGroup. */
public final class MonitorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/monitors_ListByRG.json
     */
    /**
     * Sample code: List all SAP monitors in a resource group.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllSAPMonitorsInAResourceGroup(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.monitors().listByResourceGroup("example-rg", Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.workloads.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.workloads.models.Monitor;
import com.azure.resourcemanager.workloads.models.UserAssignedServiceIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors Update. */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/monitors_PatchTags_Delete.json
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
                .getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withIdentity(new UserAssignedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
            .apply();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/monitors_PatchTags.json
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
                .getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", Context.NONE)
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
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void operations(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PhpWorkloads_CreateOrUpdate

```java
import com.azure.resourcemanager.workloads.models.AzureFrontDoorEnabled;
import com.azure.resourcemanager.workloads.models.BackupProfile;
import com.azure.resourcemanager.workloads.models.CacheProfile;
import com.azure.resourcemanager.workloads.models.DatabaseProfile;
import com.azure.resourcemanager.workloads.models.DatabaseTier;
import com.azure.resourcemanager.workloads.models.DatabaseType;
import com.azure.resourcemanager.workloads.models.DiskInfo;
import com.azure.resourcemanager.workloads.models.DiskStorageType;
import com.azure.resourcemanager.workloads.models.EnableBackup;
import com.azure.resourcemanager.workloads.models.EnableSslEnforcement;
import com.azure.resourcemanager.workloads.models.FileShareStorageType;
import com.azure.resourcemanager.workloads.models.FileShareType;
import com.azure.resourcemanager.workloads.models.FileshareProfile;
import com.azure.resourcemanager.workloads.models.HAEnabled;
import com.azure.resourcemanager.workloads.models.LoadBalancerType;
import com.azure.resourcemanager.workloads.models.ManagedRGConfiguration;
import com.azure.resourcemanager.workloads.models.NetworkProfile;
import com.azure.resourcemanager.workloads.models.NodeProfile;
import com.azure.resourcemanager.workloads.models.OSImageOffer;
import com.azure.resourcemanager.workloads.models.OSImagePublisher;
import com.azure.resourcemanager.workloads.models.OSImageSku;
import com.azure.resourcemanager.workloads.models.OSImageVersion;
import com.azure.resourcemanager.workloads.models.OsImageProfile;
import com.azure.resourcemanager.workloads.models.PhpProfile;
import com.azure.resourcemanager.workloads.models.PhpVersion;
import com.azure.resourcemanager.workloads.models.RedisCacheFamily;
import com.azure.resourcemanager.workloads.models.SearchProfile;
import com.azure.resourcemanager.workloads.models.SearchType;
import com.azure.resourcemanager.workloads.models.SiteProfile;
import com.azure.resourcemanager.workloads.models.Sku;
import com.azure.resourcemanager.workloads.models.UserProfile;
import com.azure.resourcemanager.workloads.models.VmssNodesProfile;
import com.azure.resourcemanager.workloads.models.WorkloadKind;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for PhpWorkloads CreateOrUpdate. */
public final class PhpWorkloadsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/PhpWorkloads_CreateOrUpdate.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .phpWorkloads()
            .define("wp39")
            .withRegion("eastus2")
            .withExistingResourceGroup("test-rg")
            .withKind(WorkloadKind.WORD_PRESS)
            .withTags(mapOf())
            .withSku(new Sku().withName("Large"))
            .withAppLocation("eastus")
            .withManagedResourceGroupConfiguration(new ManagedRGConfiguration().withName("php-mrg-wp39"))
            .withAdminUserProfile(new UserProfile().withUsername("wpadmin").withSshPublicKey("===SSH=PUBLIC=KEY==="))
            .withWebNodesProfile(
                new VmssNodesProfile()
                    .withName("web-server")
                    .withNodeSku("Standard_DS2_v2")
                    .withOsImage(
                        new OsImageProfile()
                            .withPublisher(OSImagePublisher.CANONICAL)
                            .withOffer(OSImageOffer.UBUNTU_SERVER)
                            .withSku(OSImageSku.fromString("18.0-LTS"))
                            .withVersion(OSImageVersion.LATEST))
                    .withOsDisk(new DiskInfo().withStorageType(DiskStorageType.PREMIUM_LRS))
                    .withAutoScaleMinCount(1)
                    .withAutoScaleMaxCount(1))
            .withControllerProfile(
                new NodeProfile()
                    .withName("contoller-vm")
                    .withNodeSku("Standard_DS2_v2")
                    .withOsImage(
                        new OsImageProfile()
                            .withPublisher(OSImagePublisher.CANONICAL)
                            .withOffer(OSImageOffer.UBUNTU_SERVER)
                            .withSku(OSImageSku.fromString("18.0-LTS"))
                            .withVersion(OSImageVersion.LATEST))
                    .withOsDisk(new DiskInfo().withStorageType(DiskStorageType.PREMIUM_LRS))
                    .withDataDisks(
                        Arrays.asList(new DiskInfo().withStorageType(DiskStorageType.PREMIUM_LRS).withSizeInGB(100L))))
            .withNetworkProfile(
                new NetworkProfile()
                    .withLoadBalancerType(LoadBalancerType.LOAD_BALANCER)
                    .withLoadBalancerSku("Standard")
                    .withAzureFrontDoorEnabled(AzureFrontDoorEnabled.ENABLED))
            .withDatabaseProfile(
                new DatabaseProfile()
                    .withType(DatabaseType.MY_SQL)
                    .withServerName("wp-db-server")
                    .withVersion("5.7")
                    .withSku("Standard_D32s_v4")
                    .withTier(DatabaseTier.GENERAL_PURPOSE)
                    .withHaEnabled(HAEnabled.DISABLED)
                    .withStorageSku("Premium_LRS")
                    .withStorageInGB(128L)
                    .withStorageIops(200L)
                    .withBackupRetentionDays(7)
                    .withSslEnforcementEnabled(EnableSslEnforcement.ENABLED))
            .withSiteProfile(new SiteProfile().withDomainName("www.example.com"))
            .withFileshareProfile(
                new FileshareProfile()
                    .withShareType(FileShareType.AZURE_FILES)
                    .withStorageType(FileShareStorageType.PREMIUM_LRS)
                    .withShareSizeInGB(100L))
            .withPhpProfile(new PhpProfile().withVersion(PhpVersion.SEVEN_THREE))
            .withSearchProfile(
                new SearchProfile()
                    .withNodeSku("Standard_DS2_v2")
                    .withOsImage(
                        new OsImageProfile()
                            .withPublisher(OSImagePublisher.CANONICAL)
                            .withOffer(OSImageOffer.UBUNTU_SERVER)
                            .withSku(OSImageSku.fromString("18.0-LTS"))
                            .withVersion(OSImageVersion.LATEST))
                    .withOsDisk(new DiskInfo().withStorageType(DiskStorageType.PREMIUM_LRS))
                    .withSearchType(SearchType.ELASTIC))
            .withCacheProfile(
                new CacheProfile()
                    .withName("wp-cache")
                    .withSkuName("Basic")
                    .withFamily(RedisCacheFamily.C)
                    .withCapacity(0L))
            .withBackupProfile(new BackupProfile().withBackupEnabled(EnableBackup.DISABLED))
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

### PhpWorkloads_Delete

```java
import com.azure.core.util.Context;

/** Samples for PhpWorkloads Delete. */
public final class PhpWorkloadsDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/PhpWorkloads_Delete.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.phpWorkloads().delete("test-rg", "wp39", "false", Context.NONE);
    }
}
```

### PhpWorkloads_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PhpWorkloads GetByResourceGroup. */
public final class PhpWorkloadsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/PhpWorkloads_Get.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.phpWorkloads().getByResourceGroupWithResponse("test-rg", "wp39", Context.NONE);
    }
}
```

### PhpWorkloads_List

```java
import com.azure.core.util.Context;

/** Samples for PhpWorkloads List. */
public final class PhpWorkloadsListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/PhpWorkloads_ListBySubscription.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.phpWorkloads().list(Context.NONE);
    }
}
```

### PhpWorkloads_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PhpWorkloads ListByResourceGroup. */
public final class PhpWorkloadsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/PhpWorkloads_ListByResourceGroup.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.phpWorkloads().listByResourceGroup("test-rg", Context.NONE);
    }
}
```

### PhpWorkloads_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.workloads.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.workloads.models.PatchResourceRequestBodyIdentity;
import com.azure.resourcemanager.workloads.models.PhpWorkloadResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for PhpWorkloads Update. */
public final class PhpWorkloadsUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/PhpWorkloads_Update.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        PhpWorkloadResource resource =
            manager.phpWorkloads().getByResourceGroupWithResponse("test-rg", "wp39", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("tag_name", "tag_value"))
            .withIdentity(new PatchResourceRequestBodyIdentity().withType(ManagedServiceIdentityType.NONE))
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

### ProviderInstances_Create

```java
import com.azure.resourcemanager.workloads.models.DB2ProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.HanaDbProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.MsSqlServerProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.PrometheusHaClusterProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.PrometheusOSProviderInstanceProperties;
import com.azure.resourcemanager.workloads.models.SapNetWeaverProviderInstanceProperties;
import java.util.Arrays;

/** Samples for ProviderInstances Create. */
public final class ProviderInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/MsSqlServerProviderInstance_Create.json
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
                    .withDbPassword("****")
                    .withDbPasswordUri("")
                    .withSapSid("sid"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/ProviderInstances_Create.json
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
                    .withDbPassword("****")
                    .withDbPasswordUri("")
                    .withDbSslCertificateUri("https://storageaccount.blob.core.windows.net/containername/filename")
                    .withSslHostnameInCertificate("xyz.domain.com"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/PrometheusHaClusterProviderInstances_Create.json
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
                    .withClusterName("clusterName"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/Db2ProviderInstances_Create.json
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
                    .withDbPassword("password")
                    .withDbPasswordUri("")
                    .withSapSid("SID"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/PrometheusOSProviderInstances_Create.json
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
                new PrometheusOSProviderInstanceProperties().withPrometheusUrl("http://192.168.0.0:9090/metrics"))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/NetWeaverProviderInstances_Create.json
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
                    .withSapPassword("****")
                    .withSapPasswordUri("")
                    .withSapClientId("111")
                    .withSapPortNumber("1234")
                    .withSapSslCertificateUri("https://storageaccount.blob.core.windows.net/containername/filename"))
            .create();
    }
}
```

### ProviderInstances_Delete

```java
import com.azure.core.util.Context;

/** Samples for ProviderInstances Delete. */
public final class ProviderInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/ProviderInstances_Delete.json
     */
    /**
     * Sample code: Deletes a SAP monitor provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deletesASAPMonitorProvider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.providerInstances().delete("myResourceGroup", "mySapMonitor", "myProviderInstance", Context.NONE);
    }
}
```

### ProviderInstances_Get

```java
import com.azure.core.util.Context;

/** Samples for ProviderInstances Get. */
public final class ProviderInstancesGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/ProviderInstances_Get.json
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
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/PrometheusHaClusterProviderInstances_Get.json
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
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/PrometheusOSProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a OS provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfAOSProvider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/Db2ProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a Db2 provider.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getPropertiesOfADb2Provider(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/MsSqlServerProviderInstance_Get.json
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
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/NetWeaverProviderInstances_Get.json
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
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", Context.NONE);
    }
}
```

### ProviderInstances_List

```java
import com.azure.core.util.Context;

/** Samples for ProviderInstances List. */
public final class ProviderInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/workloadmonitor/ProviderInstances_List.json
     */
    /**
     * Sample code: List all SAP monitors providers in a subscription.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllSAPMonitorsProvidersInASubscription(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.providerInstances().list("myResourceGroup", "mySapMonitor", Context.NONE);
    }
}
```

### ResourceProvider_SapAvailabilityZoneDetails

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider SapAvailabilityZoneDetails. */
public final class ResourceProviderSapAvailabilityZoneDetailsSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPAvailabilityZoneDetails_northeurope.json
     */
    /**
     * Sample code: SAPAvailabilityZoneDetails_northeurope.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPAvailabilityZoneDetailsNortheurope(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapAvailabilityZoneDetailsWithResponse("centralus", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPAvailabilityZoneDetails_eastus.json
     */
    /**
     * Sample code: SAPAvailabilityZoneDetails_eastus.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPAvailabilityZoneDetailsEastus(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapAvailabilityZoneDetailsWithResponse("centralus", null, Context.NONE);
    }
}
```

### ResourceProvider_SapDiskConfigurations

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider SapDiskConfigurations. */
public final class ResourceProviderSapDiskConfigurationsSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPDiskConfigurations_NonProd.json
     */
    /**
     * Sample code: SAPDiskConfigurations_NonProd.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDiskConfigurationsNonProd(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapDiskConfigurationsWithResponse("centralus", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPDiskConfigurations_Prod.json
     */
    /**
     * Sample code: SAPDiskConfigurations_Prod.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDiskConfigurationsProd(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapDiskConfigurationsWithResponse("centralus", null, Context.NONE);
    }
}
```

### ResourceProvider_SapSizingRecommendations

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider SapSizingRecommendations. */
public final class ResourceProviderSapSizingRecommendationsSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_Distributed.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_Distributed.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSizingRecommendationsS4HANADistributed(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSizingRecommendationsWithResponse("centralus", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_SingleServer.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_SingleServer.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSizingRecommendationsS4HANASingleServer(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSizingRecommendationsWithResponse("centralus", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_HA_AvZone.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_DistributedHA_AvZone.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSizingRecommendationsS4HANADistributedHAAvZone(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSizingRecommendationsWithResponse("centralus", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_HA_AvSet.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_DistributedHA_AvSet.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSizingRecommendationsS4HANADistributedHAAvSet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSizingRecommendationsWithResponse("centralus", null, Context.NONE);
    }
}
```

### ResourceProvider_SapSupportedSku

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider SapSupportedSku. */
public final class ResourceProviderSapSupportedSkuSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPSupportedSkus_SingleServer.json
     */
    /**
     * Sample code: SAPSupportedSkus_SingleServer.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSupportedSkusSingleServer(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPSupportedSkus_Distributed.json
     */
    /**
     * Sample code: SAPSupportedSkus_Distributed.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSupportedSkusDistributed(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPSupportedSkus_DistributedHA_AvZone.json
     */
    /**
     * Sample code: SAPSupportedSkus_DistributedHA_AvZone.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSupportedSkusDistributedHAAvZone(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPSupportedSkus_DistributedHA_AvSet.json
     */
    /**
     * Sample code: SAPSupportedSkus_DistributedHA_AvSet.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPSupportedSkusDistributedHAAvSet(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus", null, Context.NONE);
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_Create.json
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
import com.azure.core.util.Context;

/** Samples for SapApplicationServerInstances Delete. */
public final class SapApplicationServerInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_Delete.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Delete.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPApplicationServerInstancesDelete(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapApplicationServerInstances().delete("test-rg", "X00", "app01", Context.NONE);
    }
}
```

### SapApplicationServerInstances_Get

```java
import com.azure.core.util.Context;

/** Samples for SapApplicationServerInstances Get. */
public final class SapApplicationServerInstancesGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_Get.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Get.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPApplicationServerInstancesGet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapApplicationServerInstances().getWithResponse("test-rg", "X00", "app01", Context.NONE);
    }
}
```

### SapApplicationServerInstances_List

```java
import com.azure.core.util.Context;

/** Samples for SapApplicationServerInstances List. */
public final class SapApplicationServerInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_List.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_List.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPApplicationServerInstancesList(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapApplicationServerInstances().list("test-rg", "X00", Context.NONE);
    }
}
```

### SapApplicationServerInstances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.workloads.models.SapApplicationServerInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapApplicationServerInstances Update. */
public final class SapApplicationServerInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPApplicationServerInstances_Update.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Update.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPApplicationServerInstancesUpdate(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        SapApplicationServerInstance resource =
            manager.sapApplicationServerInstances().getWithResponse("test-rg", "X00", "app01", Context.NONE).getValue();
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPCentralInstances_Create.json
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
import com.azure.core.util.Context;

/** Samples for SapCentralInstances Delete. */
public final class SapCentralInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPCentralInstances_Delete.json
     */
    /**
     * Sample code: SAPCentralInstances_Delete.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPCentralInstancesDelete(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapCentralInstances().delete("test-rg", "X00", "centralServer", Context.NONE);
    }
}
```

### SapCentralInstances_Get

```java
import com.azure.core.util.Context;

/** Samples for SapCentralInstances Get. */
public final class SapCentralInstancesGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPCentralInstances_Get.json
     */
    /**
     * Sample code: SAPCentralInstances_Get.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPCentralInstancesGet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapCentralInstances().getWithResponse("test-rg", "X00", "centralServer", Context.NONE);
    }
}
```

### SapCentralInstances_List

```java
import com.azure.core.util.Context;

/** Samples for SapCentralInstances List. */
public final class SapCentralInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPCentralInstances_List.json
     */
    /**
     * Sample code: SAPCentralInstances_List.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPCentralInstancesList(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapCentralInstances().list("test-rg", "X00", Context.NONE);
    }
}
```

### SapCentralInstances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.workloads.models.SapCentralServerInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapCentralInstances Update. */
public final class SapCentralInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPCentralInstances_Update.json
     */
    /**
     * Sample code: SAPCentralInstances_Update.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPCentralInstancesUpdate(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        SapCentralServerInstance resource =
            manager.sapCentralInstances().getWithResponse("test-rg", "X00", "centralServer", Context.NONE).getValue();
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
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_Create.json
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
import com.azure.core.util.Context;

/** Samples for SapDatabaseInstances Delete. */
public final class SapDatabaseInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_Delete.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Delete.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDatabaseInstancesDelete(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapDatabaseInstances().delete("test-rg", "X00", "databaseServer", Context.NONE);
    }
}
```

### SapDatabaseInstances_Get

```java
import com.azure.core.util.Context;

/** Samples for SapDatabaseInstances Get. */
public final class SapDatabaseInstancesGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_Get.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Get.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDatabaseInstancesGet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapDatabaseInstances().getWithResponse("test-rg", "X00", "databaseServer", Context.NONE);
    }
}
```

### SapDatabaseInstances_List

```java
import com.azure.core.util.Context;

/** Samples for SapDatabaseInstances List. */
public final class SapDatabaseInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_List.json
     */
    /**
     * Sample code: SAPDatabaseInstances_List.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDatabaseInstancesList(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapDatabaseInstances().list("test-rg", "X00", Context.NONE);
    }
}
```

### SapDatabaseInstances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.workloads.models.SapDatabaseInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapDatabaseInstances Update. */
public final class SapDatabaseInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPDatabaseInstances_Update.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Update.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPDatabaseInstancesUpdate(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        SapDatabaseInstance resource =
            manager.sapDatabaseInstances().getWithResponse("test-rg", "X00", "databaseServer", Context.NONE).getValue();
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

### SapVirtualInstances_Create

```java
import com.azure.resourcemanager.workloads.models.ApplicationServerConfiguration;
import com.azure.resourcemanager.workloads.models.CentralServerConfiguration;
import com.azure.resourcemanager.workloads.models.DatabaseConfiguration;
import com.azure.resourcemanager.workloads.models.DeployerVmPackages;
import com.azure.resourcemanager.workloads.models.DeploymentConfiguration;
import com.azure.resourcemanager.workloads.models.DeploymentWithOSConfiguration;
import com.azure.resourcemanager.workloads.models.HighAvailabilityConfiguration;
import com.azure.resourcemanager.workloads.models.ImageReference;
import com.azure.resourcemanager.workloads.models.LinuxConfiguration;
import com.azure.resourcemanager.workloads.models.NetworkConfiguration;
import com.azure.resourcemanager.workloads.models.OSProfile;
import com.azure.resourcemanager.workloads.models.OsSapConfiguration;
import com.azure.resourcemanager.workloads.models.SapDatabaseType;
import com.azure.resourcemanager.workloads.models.SapEnvironmentType;
import com.azure.resourcemanager.workloads.models.SapHighAvailabilityType;
import com.azure.resourcemanager.workloads.models.SapProductType;
import com.azure.resourcemanager.workloads.models.SingleServerConfiguration;
import com.azure.resourcemanager.workloads.models.SshConfiguration;
import com.azure.resourcemanager.workloads.models.SshKeyPair;
import com.azure.resourcemanager.workloads.models.SshPublicKey;
import com.azure.resourcemanager.workloads.models.ThreeTierConfiguration;
import com.azure.resourcemanager.workloads.models.VirtualMachineConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapVirtualInstances Create. */
public final class SapVirtualInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_HA_AvSet.json
     */
    /**
     * Sample code: SAPVirtualInstances_Create_WithOSConfig_HA_AvSet.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesCreateWithOSConfigHAAvSet(
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
                                                                    .withPublicKey("abc")
                                                                    .withPrivateKey("xyz")))))
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
                                                                    .withPublicKey("abc")
                                                                    .withPrivateKey("xyz")))))
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
                                                                    .withPublicKey("abc")
                                                                    .withPrivateKey("xyz")))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))
                    .withOsSapConfiguration(
                        new OsSapConfiguration()
                            .withDeployerVmPackages(
                                new DeployerVmPackages()
                                    .withUrl(
                                        "https://ybteststorageaccount.blob.core.windows.net/sapbits/deployervmpackages/DeployerVMPackages.zip")
                                    .withStorageAccountId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/yb-SapInstall/providers/Microsoft.Storage/storageAccounts/ybteststorageaccount"))
                            .withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_HA_AvZone.json
     */
    /**
     * Sample code: SAPVirtualInstances_Create_WithOSConfig_HA_AvZone.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesCreateWithOSConfigHAAvZone(
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
                                                                    .withPublicKey("abc")
                                                                    .withPrivateKey("xyz")))))
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
                                                                    .withPublicKey("abc")
                                                                    .withPrivateKey("xyz")))))
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
                                                                    .withPublicKey("abc")
                                                                    .withPrivateKey("xyz")))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))
                    .withOsSapConfiguration(
                        new OsSapConfiguration()
                            .withDeployerVmPackages(
                                new DeployerVmPackages()
                                    .withUrl(
                                        "https://ybteststorageaccount.blob.core.windows.net/sapbits/deployervmpackages/DeployerVMPackages.zip")
                                    .withStorageAccountId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/yb-SapInstall/providers/Microsoft.Storage/storageAccounts/ybteststorageaccount"))
                            .withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_Distributed.json
     */
    /**
     * Sample code: SAPVirtualInstances_Create_Distributed.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesCreateDistributed(
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
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "ssh-rsa public key")))))))
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
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "ssh-rsa public key")))))))
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
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "ssh-rsa public key")))))))
                                    .withInstanceCount(1L))))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_HA_AvZone.json
     */
    /**
     * Sample code: SAPVirtualInstances_Create_HA_AvZone.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesCreateHAAvZone(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
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
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "ssh-rsa public key")))))))
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
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "ssh-rsa public key")))))))
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
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "ssh-rsa public key")))))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE))))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_SingleServer.json
     */
    /**
     * Sample code: SAPVirtualInstances_Create_WithOSConfig_SingleServer.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesCreateWithOSConfigSingleServer(
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
                                                        new SshKeyPair().withPublicKey("abc").withPrivateKey("xyz"))))))
                    .withOsSapConfiguration(
                        new OsSapConfiguration()
                            .withDeployerVmPackages(
                                new DeployerVmPackages()
                                    .withUrl(
                                        "https://ybteststorageaccount.blob.core.windows.net/sapbits/deployervmpackages/DeployerVMPackages.zip")
                                    .withStorageAccountId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/yb-SapInstall/providers/Microsoft.Storage/storageAccounts/ybteststorageaccount"))
                            .withSapFqdn("xyz.test.com")))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_SingleServer.json
     */
    /**
     * Sample code: SAPVirtualInstances_Create_SingleServer.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesCreateSingleServer(
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
                                                    .withSsh(
                                                        new SshConfiguration()
                                                            .withPublicKeys(
                                                                Arrays
                                                                    .asList(
                                                                        new SshPublicKey()
                                                                            .withKeyData("ssh-rsa public key")))))))))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_HA_AvSet.json
     */
    /**
     * Sample code: SAPVirtualInstances_Create_HA_AvSet.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesCreateHAAvSet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
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
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "ssh-rsa public key")))))))
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
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "ssh-rsa public key")))))))
                                    .withInstanceCount(5L))
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
                                                            .withSsh(
                                                                new SshConfiguration()
                                                                    .withPublicKeys(
                                                                        Arrays
                                                                            .asList(
                                                                                new SshPublicKey()
                                                                                    .withKeyData(
                                                                                        "ssh-rsa public key")))))))
                                    .withInstanceCount(2L))
                            .withHighAvailabilityConfig(
                                new HighAvailabilityConfiguration()
                                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET))))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_Distributed.json
     */
    /**
     * Sample code: SAPVirtualInstances_Create_WithOSConfig_Distributed.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesCreateWithOSConfigDistributed(
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
                                                                    .withPublicKey("abc")
                                                                    .withPrivateKey("xyz")))))
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
                                                                    .withPublicKey("abc")
                                                                    .withPrivateKey("xyz")))))
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
                                                                    .withPublicKey("abc")
                                                                    .withPrivateKey("xyz")))))
                                    .withInstanceCount(1L)))
                    .withOsSapConfiguration(
                        new OsSapConfiguration()
                            .withDeployerVmPackages(
                                new DeployerVmPackages()
                                    .withUrl(
                                        "https://ybteststorageaccount.blob.core.windows.net/sapbits/deployervmpackages/DeployerVMPackages.zip")
                                    .withStorageAccountId(
                                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/yb-SapInstall/providers/Microsoft.Storage/storageAccounts/ybteststorageaccount"))
                            .withSapFqdn("xyz.test.com")))
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
import com.azure.core.util.Context;

/** Samples for SapVirtualInstances Delete. */
public final class SapVirtualInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Delete.json
     */
    /**
     * Sample code: SAPVirtualInstances_Delete.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesDelete(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().delete("test-rg", "X00", Context.NONE);
    }
}
```

### SapVirtualInstances_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SapVirtualInstances GetByResourceGroup. */
public final class SapVirtualInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Get.json
     */
    /**
     * Sample code: SAPVirtualInstances_Get.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesGet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().getByResourceGroupWithResponse("test-rg", "X00", Context.NONE);
    }
}
```

### SapVirtualInstances_List

```java
import com.azure.core.util.Context;

/** Samples for SapVirtualInstances List. */
public final class SapVirtualInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_ListBySubscription.json
     */
    /**
     * Sample code: SAPVirtualInstances_ListBySubscription.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesListBySubscription(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().list(Context.NONE);
    }
}
```

### SapVirtualInstances_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SapVirtualInstances ListByResourceGroup. */
public final class SapVirtualInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_ListByResourceGroup.json
     */
    /**
     * Sample code: SAPVirtualInstances_ListByResourceGroup.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesListByResourceGroup(
        com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().listByResourceGroup("test-rg", Context.NONE);
    }
}
```

### SapVirtualInstances_Start

```java
import com.azure.core.util.Context;

/** Samples for SapVirtualInstances Start. */
public final class SapVirtualInstancesStartSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Start.json
     */
    /**
     * Sample code: SAPVirtualInstances_Start.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesStart(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().start("test-rg", "X00", Context.NONE);
    }
}
```

### SapVirtualInstances_Stop

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.workloads.models.StopRequest;

/** Samples for SapVirtualInstances Stop. */
public final class SapVirtualInstancesStopSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Stop.json
     */
    /**
     * Sample code: SAPVirtualInstances_Stop.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesStop(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances().stop("test-rg", "X00", new StopRequest().withHardStop(false), Context.NONE);
    }
}
```

### SapVirtualInstances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.workloads.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.workloads.models.SapVirtualInstance;
import com.azure.resourcemanager.workloads.models.UserAssignedServiceIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapVirtualInstances Update. */
public final class SapVirtualInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/sapvirtualinstances/SAPVirtualInstances_Update.json
     */
    /**
     * Sample code: SAPVirtualInstances_Update.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesUpdate(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        SapVirtualInstance resource =
            manager.sapVirtualInstances().getByResourceGroupWithResponse("test-rg", "X00", Context.NONE).getValue();
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

### Skus_List

```java
import com.azure.core.util.Context;

/** Samples for Skus List. */
public final class SkusListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/Skus_List.json
     */
    /**
     * Sample code: Skus.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void skus(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.skus().list(Context.NONE);
    }
}
```

### WordpressInstances_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.workloads.fluent.models.WordpressInstanceResourceInner;
import com.azure.resourcemanager.workloads.models.WordpressVersions;

/** Samples for WordpressInstances CreateOrUpdate. */
public final class WordpressInstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/WordpressInstances_CreateOrUpdate.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager
            .wordpressInstances()
            .createOrUpdate(
                "test-rg",
                "wp39",
                new WordpressInstanceResourceInner()
                    .withVersion(WordpressVersions.FIVE_FOUR_TWO)
                    .withDatabaseName("wpdb")
                    .withDatabaseUser("wpuser"),
                Context.NONE);
    }
}
```

### WordpressInstances_Delete

```java
import com.azure.core.util.Context;

/** Samples for WordpressInstances Delete. */
public final class WordpressInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/WordpressInstances_Delete.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.wordpressInstances().deleteWithResponse("test-rg", "wp39", Context.NONE);
    }
}
```

### WordpressInstances_Get

```java
import com.azure.core.util.Context;

/** Samples for WordpressInstances Get. */
public final class WordpressInstancesGetSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/WordpressInstances_Get.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.wordpressInstances().getWithResponse("test-rg", "wp39", Context.NONE);
    }
}
```

### WordpressInstances_List

```java
import com.azure.core.util.Context;

/** Samples for WordpressInstances List. */
public final class WordpressInstancesListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/preview/2021-12-01-preview/examples/phpworkloads/WordpressInstances_List.json
     */
    /**
     * Sample code: Workloads.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void workloads(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.wordpressInstances().list("test-rg", "wp39", Context.NONE);
    }
}
```

