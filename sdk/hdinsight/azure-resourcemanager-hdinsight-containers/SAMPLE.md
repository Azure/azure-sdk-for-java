# Code snippets and samples


## AvailableClusterPoolVersions

- [ListByLocation](#availableclusterpoolversions_listbylocation)

## AvailableClusterVersions

- [ListByLocation](#availableclusterversions_listbylocation)

## ClusterAvailableUpgrades

- [List](#clusteravailableupgrades_list)

## ClusterJobs

- [List](#clusterjobs_list)
- [RunJob](#clusterjobs_runjob)

## ClusterLibraries

- [List](#clusterlibraries_list)
- [ManageLibraries](#clusterlibraries_managelibraries)

## ClusterPoolAvailableUpgrades

- [List](#clusterpoolavailableupgrades_list)

## ClusterPoolUpgradeHistories

- [List](#clusterpoolupgradehistories_list)

## ClusterPools

- [CreateOrUpdate](#clusterpools_createorupdate)
- [Delete](#clusterpools_delete)
- [GetByResourceGroup](#clusterpools_getbyresourcegroup)
- [List](#clusterpools_list)
- [ListByResourceGroup](#clusterpools_listbyresourcegroup)
- [UpdateTags](#clusterpools_updatetags)
- [Upgrade](#clusterpools_upgrade)

## ClusterUpgradeHistories

- [List](#clusterupgradehistories_list)

## Clusters

- [Create](#clusters_create)
- [Delete](#clusters_delete)
- [Get](#clusters_get)
- [GetInstanceView](#clusters_getinstanceview)
- [ListByClusterPoolName](#clusters_listbyclusterpoolname)
- [ListInstanceViews](#clusters_listinstanceviews)
- [ListServiceConfigs](#clusters_listserviceconfigs)
- [Resize](#clusters_resize)
- [Update](#clusters_update)
- [Upgrade](#clusters_upgrade)
- [UpgradeManualRollback](#clusters_upgrademanualrollback)

## Locations

- [CheckNameAvailability](#locations_checknameavailability)

## Operations

- [List](#operations_list)
### AvailableClusterPoolVersions_ListByLocation

```java
/**
 * Samples for AvailableClusterPoolVersions ListByLocation.
 */
public final class AvailableClusterPoolVersionsListByLocationSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListAvailableClusterPoolVersions.json
     */
    /**
     * Sample code: ClusterPoolVersionListResult.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolVersionListResult(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.availableClusterPoolVersions().listByLocation("westus2", com.azure.core.util.Context.NONE);
    }
}
```

### AvailableClusterVersions_ListByLocation

```java
/**
 * Samples for AvailableClusterVersions ListByLocation.
 */
public final class AvailableClusterVersionsListByLocationSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListAvailableClusterVersions.json
     */
    /**
     * Sample code: ClusterVersionListResult.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clusterVersionListResult(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.availableClusterVersions().listByLocation("westus2", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterAvailableUpgrades_List

```java
/**
 * Samples for ClusterAvailableUpgrades List.
 */
public final class ClusterAvailableUpgradesListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClusterAvailableUpgrades.json
     */
    /**
     * Sample code: GetClusterAvailableUpgrade.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        getClusterAvailableUpgrade(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterAvailableUpgrades()
            .list("hiloResourcegroup", "clusterpool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterJobs_List

```java
/**
 * Samples for ClusterJobs List.
 */
public final class ClusterJobsListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClusterJobs.json
     */
    /**
     * Sample code: ListClusterJobs.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        listClusterJobs(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterJobs()
            .list("hiloResourcegroup", "clusterPool1", "cluster1", null, com.azure.core.util.Context.NONE);
    }
}
```

### ClusterJobs_RunJob

```java
import com.azure.resourcemanager.hdinsight.containers.fluent.models.ClusterJobInner;
import com.azure.resourcemanager.hdinsight.containers.models.Action;
import com.azure.resourcemanager.hdinsight.containers.models.FlinkJobProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ClusterJobs RunJob.
 */
public final class ClusterJobsRunJobSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * RunClusterJob.json
     */
    /**
     * Sample code: RunClusterJob.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        runClusterJob(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterJobs()
            .runJob("hiloResourcegroup", "clusterpool1", "cluster1",
                new ClusterJobInner()
                    .withProperties(new FlinkJobProperties().withJobName("flink-job-name")
                        .withJobJarDirectory("abfs://flinkjob@hilosa.dfs.core.windows.net/jars")
                        .withJarName("flink-sleep-job-0.0.1-SNAPSHOT.jar")
                        .withEntryClass("com.microsoft.hilo.flink.job.streaming.SleepJob")
                        .withAction(Action.START)
                        .withFlinkConfiguration(mapOf("parallelism", "1", "savepoint.directory",
                            "abfs://flinkjob@hilosa.dfs.core.windows.net/savepoint"))),
                com.azure.core.util.Context.NONE);
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

### ClusterLibraries_List

```java
import com.azure.resourcemanager.hdinsight.containers.models.Category;

/**
 * Samples for ClusterLibraries List.
 */
public final class ClusterLibrariesListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListPredefinedClusterLibraries.json
     */
    /**
     * Sample code: ListPredefinedClusterLibraries.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void listPredefinedClusterLibraries(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterLibraries()
            .list("hiloResourceGroup", "clusterPool", "cluster", Category.PREDEFINED, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListUserCustomClusterLibraries.json
     */
    /**
     * Sample code: ListUserCustomClusterLibraries.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void listUserCustomClusterLibraries(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterLibraries()
            .list("hiloResourceGroup", "clusterPool", "cluster", Category.CUSTOM, com.azure.core.util.Context.NONE);
    }
}
```

### ClusterLibraries_ManageLibraries

```java
import com.azure.resourcemanager.hdinsight.containers.fluent.models.ClusterLibraryInner;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterLibraryManagementOperation;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterLibraryManagementOperationProperties;
import com.azure.resourcemanager.hdinsight.containers.models.LibraryManagementAction;
import com.azure.resourcemanager.hdinsight.containers.models.MavenLibraryProperties;
import com.azure.resourcemanager.hdinsight.containers.models.PyPiLibraryProperties;
import java.util.Arrays;

/**
 * Samples for ClusterLibraries ManageLibraries.
 */
public final class ClusterLibrariesManageLibrariesSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * UninstallExistingClusterLibraries.json
     */
    /**
     * Sample code: UninstallExistingClusterLibraries.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void uninstallExistingClusterLibraries(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterLibraries()
            .manageLibraries("hiloResourceGroup", "clusterPool", "cluster",
                new ClusterLibraryManagementOperation().withProperties(new ClusterLibraryManagementOperationProperties()
                    .withAction(LibraryManagementAction.UNINSTALL)
                    .withLibraries(Arrays.asList(
                        new ClusterLibraryInner().withProperties(new PyPiLibraryProperties().withName("tensorflow")),
                        new ClusterLibraryInner()
                            .withProperties(new MavenLibraryProperties().withGroupId("org.apache.flink")
                                .withName("flink-connector-hudi"))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * InstallNewClusterLibraries.json
     */
    /**
     * Sample code: InstallNewClusterLibraries.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        installNewClusterLibraries(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterLibraries()
            .manageLibraries("hiloResourceGroup", "clusterPool", "cluster",
                new ClusterLibraryManagementOperation().withProperties(
                    new ClusterLibraryManagementOperationProperties().withAction(LibraryManagementAction.INSTALL)
                        .withLibraries(Arrays.asList(
                            new ClusterLibraryInner()
                                .withProperties(new PyPiLibraryProperties().withRemarks("PyPi packages.")
                                    .withName("requests")
                                    .withVersion("2.31.0")),
                            new ClusterLibraryInner()
                                .withProperties(new MavenLibraryProperties().withRemarks("Maven packages.")
                                    .withGroupId("org.apache.flink")
                                    .withName("flink-connector-kafka")
                                    .withVersion("3.0.2-1.18"))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ClusterPoolAvailableUpgrades_List

```java
/**
 * Samples for ClusterPoolAvailableUpgrades List.
 */
public final class ClusterPoolAvailableUpgradesListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClusterPoolAvailableUpgrades.json
     */
    /**
     * Sample code: GetClusterPoolAvailableUpgrade.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void getClusterPoolAvailableUpgrade(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPoolAvailableUpgrades()
            .list("hiloResourcegroup", "clusterpool1", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterPoolUpgradeHistories_List

```java
/**
 * Samples for ClusterPoolUpgradeHistories List.
 */
public final class ClusterPoolUpgradeHistoriesListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClusterPoolUpgradeHistory.json
     */
    /**
     * Sample code: ClusterPoolUpgradeHistoriesListResult.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolUpgradeHistoriesListResult(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPoolUpgradeHistories()
            .list("hiloResourcegroup", "clusterpool1", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterPools_CreateOrUpdate

```java
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPoolResourceProperties;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPoolResourcePropertiesClusterPoolProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPoolResourcePropertiesComputeProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPoolResourcePropertiesNetworkProfile;
import com.azure.resourcemanager.hdinsight.containers.models.OutboundType;
import java.util.Arrays;

/**
 * Samples for ClusterPools CreateOrUpdate.
 */
public final class ClusterPoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * CreateClusterPoolWithPrivateAks.json
     */
    /**
     * Sample code: ClusterPoolPutWithPrivateAks.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolPutWithPrivateAks(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools()
            .define("clusterpool1")
            .withRegion("West US 2")
            .withExistingResourceGroup("hiloResourcegroup")
            .withProperties(new ClusterPoolResourceProperties()
                .withClusterPoolProfile(
                    new ClusterPoolResourcePropertiesClusterPoolProfile().withClusterPoolVersion("1.2"))
                .withComputeProfile(new ClusterPoolResourcePropertiesComputeProfile().withVmSize("Standard_D3_v2")
                    .withAvailabilityZones(Arrays.asList("1", "2", "3")))
                .withNetworkProfile(new ClusterPoolResourcePropertiesNetworkProfile().withSubnetId(
                    "/subscriptions/subid/resourceGroups/hiloResourcegroup/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1")
                    .withEnablePrivateApiServer(true)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * CreateClusterPoolWithUDRAks.json
     */
    /**
     * Sample code: ClusterPoolPutWithUDRAks.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clusterPoolPutWithUDRAks(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools()
            .define("clusterpool1")
            .withRegion("West US 2")
            .withExistingResourceGroup("hiloResourcegroup")
            .withProperties(new ClusterPoolResourceProperties()
                .withClusterPoolProfile(
                    new ClusterPoolResourcePropertiesClusterPoolProfile().withClusterPoolVersion("1.2"))
                .withComputeProfile(new ClusterPoolResourcePropertiesComputeProfile().withVmSize("Standard_D3_v2")
                    .withAvailabilityZones(Arrays.asList("1", "2", "3")))
                .withNetworkProfile(new ClusterPoolResourcePropertiesNetworkProfile().withSubnetId(
                    "/subscriptions/subid/resourceGroups/hiloResourcegroup/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1")
                    .withOutboundType(OutboundType.USER_DEFINED_ROUTING)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * CreateClusterPool.json
     */
    /**
     * Sample code: ClusterPoolPut.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clusterPoolPut(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools()
            .define("clusterpool1")
            .withRegion("West US 2")
            .withExistingResourceGroup("hiloResourcegroup")
            .withProperties(new ClusterPoolResourceProperties()
                .withClusterPoolProfile(
                    new ClusterPoolResourcePropertiesClusterPoolProfile().withClusterPoolVersion("1.2"))
                .withComputeProfile(new ClusterPoolResourcePropertiesComputeProfile().withVmSize("Standard_D3_v2")
                    .withAvailabilityZones(Arrays.asList("1", "2", "3"))))
            .create();
    }
}
```

### ClusterPools_Delete

```java
/**
 * Samples for ClusterPools Delete.
 */
public final class ClusterPoolsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * DeleteClusterPool.json
     */
    /**
     * Sample code: ClusterPoolDelete.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clusterPoolDelete(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools().delete("rg1", "clusterpool1", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterPools_GetByResourceGroup

```java
/**
 * Samples for ClusterPools GetByResourceGroup.
 */
public final class ClusterPoolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * GetClusterPool.json
     */
    /**
     * Sample code: ClusterPoolGet.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clusterPoolGet(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools()
            .getByResourceGroupWithResponse("hiloResourcegroup", "clusterpool1", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterPools_List

```java
/**
 * Samples for ClusterPools List.
 */
public final class ClusterPoolsListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClusterPoolsSubscription.json
     */
    /**
     * Sample code: ClusterPoolsListBySubscription.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolsListBySubscription(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools().list(com.azure.core.util.Context.NONE);
    }
}
```

### ClusterPools_ListByResourceGroup

```java
/**
 * Samples for ClusterPools ListByResourceGroup.
 */
public final class ClusterPoolsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClusterPools.json
     */
    /**
     * Sample code: ClusterPoolsListByResourceGroup.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolsListByResourceGroup(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools().listByResourceGroup("hiloResourcegroup", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterPools_UpdateTags

```java
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPool;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ClusterPools UpdateTags.
 */
public final class ClusterPoolsUpdateTagsSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * PatchClusterPool.json
     */
    /**
     * Sample code: ClusterPoolsPatchTags.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clusterPoolsPatchTags(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        ClusterPool resource = manager.clusterPools()
            .getByResourceGroupWithResponse("hiloResourcegroup", "clusterpool1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### ClusterPools_Upgrade

```java
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPoolAksPatchVersionUpgradeProperties;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPoolNodeOsImageUpdateProperties;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPoolUpgrade;

/**
 * Samples for ClusterPools Upgrade.
 */
public final class ClusterPoolsUpgradeSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * UpgradeNodeOsForClusterPool.json
     */
    /**
     * Sample code: ClusterPoolsUpgradeNodeOs.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clusterPoolsUpgradeNodeOs(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools()
            .upgrade("hiloResourcegroup", "clusterpool1",
                new ClusterPoolUpgrade().withProperties(new ClusterPoolNodeOsImageUpdateProperties()),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * UpgradeAKSPatchVersionForClusterPool.json
     */
    /**
     * Sample code: ClusterPoolsUpgradeAKSPatchVersion.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolsUpgradeAKSPatchVersion(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools()
            .upgrade("hiloResourcegroup", "clusterpool1",
                new ClusterPoolUpgrade()
                    .withProperties(new ClusterPoolAksPatchVersionUpgradeProperties().withUpgradeClusterPool(true)
                        .withUpgradeAllClusterNodes(false)),
                com.azure.core.util.Context.NONE);
    }
}
```

### ClusterUpgradeHistories_List

```java
/**
 * Samples for ClusterUpgradeHistories List.
 */
public final class ClusterUpgradeHistoriesListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClusterUpgradeHistory.json
     */
    /**
     * Sample code: ClusterUpgradeHistoriesListResult.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterUpgradeHistoriesListResult(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterUpgradeHistories()
            .list("hiloResourcegroup", "clusterpool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Create

```java
import com.azure.resourcemanager.hdinsight.containers.models.AuthorizationProfile;
import com.azure.resourcemanager.hdinsight.containers.models.AutoscaleProfile;
import com.azure.resourcemanager.hdinsight.containers.models.AutoscaleType;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterAccessProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterConfigFile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterResourceProperties;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterServiceConfig;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterServiceConfigsProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ComparisonOperator;
import com.azure.resourcemanager.hdinsight.containers.models.ComparisonRule;
import com.azure.resourcemanager.hdinsight.containers.models.ComputeProfile;
import com.azure.resourcemanager.hdinsight.containers.models.LoadBasedConfig;
import com.azure.resourcemanager.hdinsight.containers.models.ManagedIdentityProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ManagedIdentitySpec;
import com.azure.resourcemanager.hdinsight.containers.models.ManagedIdentityType;
import com.azure.resourcemanager.hdinsight.containers.models.NodeProfile;
import com.azure.resourcemanager.hdinsight.containers.models.RangerAdminSpec;
import com.azure.resourcemanager.hdinsight.containers.models.RangerAdminSpecDatabase;
import com.azure.resourcemanager.hdinsight.containers.models.RangerAuditSpec;
import com.azure.resourcemanager.hdinsight.containers.models.RangerProfile;
import com.azure.resourcemanager.hdinsight.containers.models.RangerUsersyncMode;
import com.azure.resourcemanager.hdinsight.containers.models.RangerUsersyncSpec;
import com.azure.resourcemanager.hdinsight.containers.models.ScaleActionType;
import com.azure.resourcemanager.hdinsight.containers.models.ScalingRule;
import com.azure.resourcemanager.hdinsight.containers.models.Schedule;
import com.azure.resourcemanager.hdinsight.containers.models.ScheduleBasedConfig;
import com.azure.resourcemanager.hdinsight.containers.models.ScheduleDay;
import com.azure.resourcemanager.hdinsight.containers.models.SparkProfile;
import com.azure.resourcemanager.hdinsight.containers.models.SshProfile;
import com.azure.resourcemanager.hdinsight.containers.models.TrinoProfile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clusters Create.
 */
public final class ClustersCreateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * CreateSparkCluster.json
     */
    /**
     * Sample code: HDInsightSparkClusterPut.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        hDInsightSparkClusterPut(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .define("cluster1")
            .withRegion("West US 2")
            .withExistingClusterpool("hiloResourcegroup", "clusterpool1")
            .withProperties(new ClusterResourceProperties().withClusterType("spark")
                .withComputeProfile(new ComputeProfile()
                    .withNodes(
                        Arrays.asList(new NodeProfile().withType("worker").withVmSize("Standard_D3_v2").withCount(4)))
                    .withAvailabilityZones(Arrays.asList("1", "2", "3")))
                .withClusterProfile(new ClusterProfile().withClusterVersion("0.0.1")
                    .withOssVersion("2.2.3")
                    .withManagedIdentityProfile(new ManagedIdentityProfile()
                        .withIdentityList(Arrays.asList(new ManagedIdentitySpec().withType(ManagedIdentityType.CLUSTER)
                            .withResourceId(
                                "/subscriptions/subid/resourceGroups/hiloResourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-msi")
                            .withClientId("de91f1d8-767f-460a-ac11-3cf103f74b34")
                            .withObjectId("40491351-c240-4042-91e0-f644a1d2b441"))))
                    .withAuthorizationProfile(
                        new AuthorizationProfile().withUserIds(Arrays.asList("testuser1", "testuser2")))
                    .withServiceConfigsProfiles(Arrays.asList(
                        new ClusterServiceConfigsProfile().withServiceName("spark-service")
                            .withConfigs(Arrays.asList(new ClusterServiceConfig().withComponent("spark-config")
                                .withFiles(Arrays.asList(new ClusterConfigFile().withFileName("spark-defaults.conf")
                                    .withValues(mapOf("spark.eventLog.enabled", "true")))))),
                        new ClusterServiceConfigsProfile().withServiceName("yarn-service")
                            .withConfigs(Arrays.asList(new ClusterServiceConfig().withComponent("yarn-config")
                                .withFiles(Arrays.asList(
                                    new ClusterConfigFile().withFileName("core-site.xml")
                                        .withValues(mapOf("fs.defaultFS",
                                            "wasb://testcontainer@teststorage.dfs.core.windows.net/",
                                            "storage.container", "testcontainer", "storage.key", "fakeTokenPlaceholder",
                                            "storage.name", "teststorage", "storage.protocol", "wasb")),
                                    new ClusterConfigFile().withFileName("yarn-site.xml")
                                        .withValues(mapOf("yarn.webapp.ui2.enable", "false"))))))))
                    .withSshProfile(new SshProfile().withCount(2).withVmSize("Standard_D3_v2"))
                    .withSparkProfile(new SparkProfile())))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * CreateSparkClusterWithInternalIngress.json
     */
    /**
     * Sample code: HDInsightSparkClusterPutWithInternalIngress.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightSparkClusterPutWithInternalIngress(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .define("cluster1")
            .withRegion("West US 2")
            .withExistingClusterpool("hiloResourcegroup", "clusterpool1")
            .withProperties(new ClusterResourceProperties().withClusterType("spark")
                .withComputeProfile(new ComputeProfile()
                    .withNodes(
                        Arrays.asList(new NodeProfile().withType("worker").withVmSize("Standard_D3_v2").withCount(4)))
                    .withAvailabilityZones(Arrays.asList("1", "2", "3")))
                .withClusterProfile(new ClusterProfile().withClusterVersion("0.0.1")
                    .withOssVersion("2.2.3")
                    .withManagedIdentityProfile(new ManagedIdentityProfile()
                        .withIdentityList(Arrays.asList(new ManagedIdentitySpec().withType(ManagedIdentityType.CLUSTER)
                            .withResourceId(
                                "/subscriptions/subid/resourceGroups/hiloResourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-msi")
                            .withClientId("de91f1d8-767f-460a-ac11-3cf103f74b34")
                            .withObjectId("40491351-c240-4042-91e0-f644a1d2b441"))))
                    .withAuthorizationProfile(
                        new AuthorizationProfile().withUserIds(Arrays.asList("testuser1", "testuser2")))
                    .withServiceConfigsProfiles(Arrays.asList(
                        new ClusterServiceConfigsProfile().withServiceName("spark-service")
                            .withConfigs(Arrays.asList(new ClusterServiceConfig().withComponent("spark-config")
                                .withFiles(Arrays.asList(new ClusterConfigFile().withFileName("spark-defaults.conf")
                                    .withValues(mapOf("spark.eventLog.enabled", "true")))))),
                        new ClusterServiceConfigsProfile().withServiceName("yarn-service")
                            .withConfigs(Arrays.asList(new ClusterServiceConfig().withComponent("yarn-config")
                                .withFiles(Arrays.asList(
                                    new ClusterConfigFile().withFileName("core-site.xml")
                                        .withValues(mapOf("fs.defaultFS",
                                            "wasb://testcontainer@teststorage.dfs.core.windows.net/",
                                            "storage.container", "testcontainer", "storage.key", "fakeTokenPlaceholder",
                                            "storage.name", "teststorage", "storage.protocol", "wasb")),
                                    new ClusterConfigFile().withFileName("yarn-site.xml")
                                        .withValues(mapOf("yarn.webapp.ui2.enable", "false"))))))))
                    .withClusterAccessProfile(new ClusterAccessProfile().withEnableInternalIngress(true))
                    .withSshProfile(new SshProfile().withCount(2).withVmSize("Standard_D3_v2"))
                    .withSparkProfile(new SparkProfile())))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * CreateAutoscaleCluster.json
     */
    /**
     * Sample code: HDInsightClusterPut.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        hDInsightClusterPut(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .define("cluster1")
            .withRegion("West US 2")
            .withExistingClusterpool("hiloResourcegroup", "clusterpool1")
            .withProperties(new ClusterResourceProperties().withClusterType("Trino")
                .withComputeProfile(new ComputeProfile()
                    .withNodes(
                        Arrays.asList(new NodeProfile().withType("Head").withVmSize("Standard_E8as_v5").withCount(2),
                            new NodeProfile().withType("Worker").withVmSize("Standard_E8as_v5").withCount(3)))
                    .withAvailabilityZones(Arrays.asList("1", "2", "3")))
                .withClusterProfile(new ClusterProfile().withClusterVersion("1.0.6")
                    .withOssVersion("0.410.0")
                    .withManagedIdentityProfile(new ManagedIdentityProfile()
                        .withIdentityList(Arrays.asList(new ManagedIdentitySpec().withType(ManagedIdentityType.CLUSTER)
                            .withResourceId(
                                "/subscriptions/subid/resourceGroups/hiloResourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-msi")
                            .withClientId("de91f1d8-767f-460a-ac11-3cf103f74b34")
                            .withObjectId("40491351-c240-4042-91e0-f644a1d2b441"))))
                    .withAuthorizationProfile(
                        new AuthorizationProfile().withUserIds(Arrays.asList("testuser1", "testuser2")))
                    .withSshProfile(new SshProfile().withCount(2).withVmSize("Standard_E8as_v5"))
                    .withAutoscaleProfile(new AutoscaleProfile().withEnabled(true)
                        .withGracefulDecommissionTimeout(3600)
                        .withAutoscaleType(AutoscaleType.SCHEDULE_BASED)
                        .withScheduleBasedConfig(new ScheduleBasedConfig().withTimeZone("Cen. Australia Standard Time")
                            .withDefaultCount(10)
                            .withSchedules(Arrays.asList(
                                new Schedule().withStartTime("00:00")
                                    .withEndTime("12:00")
                                    .withCount(20)
                                    .withDays(Arrays.asList(ScheduleDay.MONDAY)),
                                new Schedule().withStartTime("00:00")
                                    .withEndTime("12:00")
                                    .withCount(25)
                                    .withDays(Arrays.asList(ScheduleDay.SUNDAY)))))
                        .withLoadBasedConfig(new LoadBasedConfig().withMinNodes(10)
                            .withMaxNodes(20)
                            .withPollInterval(60)
                            .withCooldownPeriod(300)
                            .withScalingRules(Arrays.asList(new ScalingRule().withActionType(ScaleActionType.SCALEUP)
                                .withEvaluationCount(3)
                                .withScalingMetric("cpu")
                                .withComparisonRule(new ComparisonRule().withOperator(ComparisonOperator.GREATER_THAN)
                                    .withThreshold(90f)),
                                new ScalingRule().withActionType(ScaleActionType.SCALEDOWN)
                                    .withEvaluationCount(3)
                                    .withScalingMetric("cpu")
                                    .withComparisonRule(new ComparisonRule().withOperator(ComparisonOperator.LESS_THAN)
                                        .withThreshold(20f))))))
                    .withTrinoProfile(new TrinoProfile())))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * CreateRangerCluster.json
     */
    /**
     * Sample code: HDInsightRangerClusterPut.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        hDInsightRangerClusterPut(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .define("cluster1")
            .withRegion("West US 2")
            .withExistingClusterpool("hiloResourcegroup", "clusterpool1")
            .withProperties(new ClusterResourceProperties().withClusterType("ranger")
                .withComputeProfile(new ComputeProfile()
                    .withNodes(
                        Arrays.asList(new NodeProfile().withType("head").withVmSize("Standard_D3_v2").withCount(2)))
                    .withAvailabilityZones(Arrays.asList("1", "2", "3")))
                .withClusterProfile(new ClusterProfile().withClusterVersion("0.0.1")
                    .withOssVersion("2.2.3")
                    .withManagedIdentityProfile(new ManagedIdentityProfile()
                        .withIdentityList(Arrays.asList(new ManagedIdentitySpec().withType(ManagedIdentityType.CLUSTER)
                            .withResourceId(
                                "/subscriptions/subid/resourceGroups/hiloResourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-msi")
                            .withClientId("de91f1d8-767f-460a-ac11-3cf103f74b34")
                            .withObjectId("40491351-c240-4042-91e0-f644a1d2b441"))))
                    .withAuthorizationProfile(
                        new AuthorizationProfile().withUserIds(Arrays.asList("testuser1", "testuser2")))
                    .withRangerProfile(new RangerProfile()
                        .withRangerAdmin(new RangerAdminSpec()
                            .withAdmins(Arrays.asList("testuser1@contoso.com", "testuser2@contoso.com"))
                            .withDatabase(new RangerAdminSpecDatabase().withHost("testsqlserver.database.windows.net")
                                .withName("testdb")
                                .withPasswordSecretRef("fakeTokenPlaceholder")
                                .withUsername("admin")))
                        .withRangerAudit(new RangerAuditSpec()
                            .withStorageAccount("https://teststorage.blob.core.windows.net/testblob"))
                        .withRangerUsersync(new RangerUsersyncSpec().withEnabled(true)
                            .withGroups(Arrays.asList("0a53828f-36c9-44c3-be3d-99a7fce977ad",
                                "13be6971-79db-4f33-9d41-b25589ca25ac"))
                            .withMode(RangerUsersyncMode.AUTOMATIC)
                            .withUsers(Arrays.asList("testuser1@contoso.com", "testuser2@contoso.com"))))))
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

### Clusters_Delete

```java
/**
 * Samples for Clusters Delete.
 */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * DeleteCluster.json
     */
    /**
     * Sample code: HDInsightClustersDelete.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        hDInsightClustersDelete(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters().delete("rg1", "clusterpool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Get

```java
/**
 * Samples for Clusters Get.
 */
public final class ClustersGetSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * GetCluster.json
     */
    /**
     * Sample code: HDInsightClusterGet.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        hDInsightClusterGet(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .getWithResponse("hiloResourcegroup", "clusterpool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetInstanceView

```java
/**
 * Samples for Clusters GetInstanceView.
 */
public final class ClustersGetInstanceViewSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * GetClusterInstanceView.json
     */
    /**
     * Sample code: HDInsightClusterGetInstanceView.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClusterGetInstanceView(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .getInstanceViewWithResponse("rg1", "clusterPool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByClusterPoolName

```java
/**
 * Samples for Clusters ListByClusterPoolName.
 */
public final class ClustersListByClusterPoolNameSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClustersByClusterPoolName.json
     */
    /**
     * Sample code: HDInsightClustersListByClusterPoolName.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClustersListByClusterPoolName(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters().listByClusterPoolName("hiloResourcegroup", "clusterpool1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListInstanceViews

```java
/**
 * Samples for Clusters ListInstanceViews.
 */
public final class ClustersListInstanceViewsSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClusterInstanceViews.json
     */
    /**
     * Sample code: HDInsightClusterGetInstanceViews.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClusterGetInstanceViews(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters().listInstanceViews("rg1", "clusterPool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListServiceConfigs

```java
/**
 * Samples for Clusters ListServiceConfigs.
 */
public final class ClustersListServiceConfigsSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ListClusterServiceConfigs.json
     */
    /**
     * Sample code: HDInsightClusterGetServiceConfigs.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClusterGetServiceConfigs(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters().listServiceConfigs("rg1", "clusterPool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Resize

```java
import com.azure.resourcemanager.hdinsight.containers.models.ClusterResizeData;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterResizeProperties;

/**
 * Samples for Clusters Resize.
 */
public final class ClustersResizeSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ResizeCluster.json
     */
    /**
     * Sample code: HDInsightClusterResize.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        hDInsightClusterResize(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .resize("hiloResourcegroup", "clusterpool1", "cluster1",
                new ClusterResizeData().withLocation("West US 2")
                    .withProperties(new ClusterResizeProperties().withTargetWorkerNodeCount(5)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.resourcemanager.hdinsight.containers.models.AuthorizationProfile;
import com.azure.resourcemanager.hdinsight.containers.models.AutoscaleProfile;
import com.azure.resourcemanager.hdinsight.containers.models.AutoscaleType;
import com.azure.resourcemanager.hdinsight.containers.models.Cluster;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterConfigFile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterLogAnalyticsApplicationLogs;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterLogAnalyticsProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPatchProperties;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterServiceConfig;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterServiceConfigsProfile;
import com.azure.resourcemanager.hdinsight.containers.models.RangerAdminSpec;
import com.azure.resourcemanager.hdinsight.containers.models.RangerAdminSpecDatabase;
import com.azure.resourcemanager.hdinsight.containers.models.RangerAuditSpec;
import com.azure.resourcemanager.hdinsight.containers.models.RangerProfile;
import com.azure.resourcemanager.hdinsight.containers.models.RangerUsersyncMode;
import com.azure.resourcemanager.hdinsight.containers.models.RangerUsersyncSpec;
import com.azure.resourcemanager.hdinsight.containers.models.Schedule;
import com.azure.resourcemanager.hdinsight.containers.models.ScheduleBasedConfig;
import com.azure.resourcemanager.hdinsight.containers.models.ScheduleDay;
import com.azure.resourcemanager.hdinsight.containers.models.SshProfile;
import com.azure.resourcemanager.hdinsight.containers.models.UpdatableClusterProfile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clusters Update.
 */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * PatchRangerCluster.json
     */
    /**
     * Sample code: HDInsightRangerClusterPatchTags.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightRangerClusterPatchTags(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        Cluster resource = manager.clusters()
            .getWithResponse("hiloResourcegroup", "clusterpool1", "cluster1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ClusterPatchProperties()
                .withClusterProfile(new UpdatableClusterProfile().withRangerProfile(new RangerProfile()
                    .withRangerAdmin(new RangerAdminSpec()
                        .withAdmins(Arrays.asList("testuser1@contoso.com", "testuser2@contoso.com"))
                        .withDatabase(new RangerAdminSpecDatabase().withHost("testsqlserver.database.windows.net")
                            .withName("testdb")
                            .withPasswordSecretRef("fakeTokenPlaceholder")
                            .withUsername("admin")))
                    .withRangerAudit(
                        new RangerAuditSpec().withStorageAccount("https://teststorage.blob.core.windows.net/testblob"))
                    .withRangerUsersync(new RangerUsersyncSpec().withEnabled(true)
                        .withGroups(Arrays.asList("0a53828f-36c9-44c3-be3d-99a7fce977ad",
                            "13be6971-79db-4f33-9d41-b25589ca25ac"))
                        .withMode(RangerUsersyncMode.AUTOMATIC)
                        .withUsers(Arrays.asList("testuser1@contoso.com", "testuser2@contoso.com"))))))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * PatchCluster.json
     */
    /**
     * Sample code: HDInsightClustersPatchTags.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        hDInsightClustersPatchTags(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        Cluster resource = manager.clusters()
            .getWithResponse("hiloResourcegroup", "clusterpool1", "cluster1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new ClusterPatchProperties()
                    .withClusterProfile(
                        new UpdatableClusterProfile()
                            .withServiceConfigsProfiles(
                                Arrays
                                    .asList(
                                        new ClusterServiceConfigsProfile().withServiceName("TestService1")
                                            .withConfigs(
                                                Arrays
                                                    .asList(
                                                        new ClusterServiceConfig().withComponent("TestComp1")
                                                            .withFiles(
                                                                Arrays.asList(
                                                                    new ClusterConfigFile().withFileName("TestFile1")
                                                                        .withValues(mapOf("Test.config.1", "1",
                                                                            "Test.config.2", "2")),
                                                                    new ClusterConfigFile().withFileName("TestFile2")
                                                                        .withValues(mapOf("Test.config.3", "3",
                                                                            "Test.config.4", "4")))),
                                                        new ClusterServiceConfig().withComponent("TestComp2")
                                                            .withFiles(Arrays
                                                                .asList(
                                                                    new ClusterConfigFile().withFileName("TestFile3")
                                                                        .withContent("TestContent")
                                                                        .withPath("TestPath"),
                                                                    new ClusterConfigFile()
                                                                        .withFileName("TestFile4")
                                                                        .withValues(mapOf("Test.config.7", "7",
                                                                            "Test.config.8", "8")))))),
                                        new ClusterServiceConfigsProfile().withServiceName("TestService2")
                                            .withConfigs(
                                                Arrays
                                                    .asList(new ClusterServiceConfig().withComponent("TestComp3")
                                                        .withFiles(Arrays
                                                            .asList(new ClusterConfigFile().withFileName("TestFile5")
                                                                .withValues(mapOf("Test.config.9", "9"))))))))
                            .withSshProfile(new SshProfile().withCount(2))
                            .withAutoscaleProfile(
                                new AutoscaleProfile().withEnabled(true)
                                    .withGracefulDecommissionTimeout(-1)
                                    .withAutoscaleType(AutoscaleType.SCHEDULE_BASED)
                                    .withScheduleBasedConfig(new ScheduleBasedConfig()
                                        .withTimeZone("Cen. Australia Standard Time")
                                        .withDefaultCount(3)
                                        .withSchedules(Arrays.asList(
                                            new Schedule().withStartTime("00:00")
                                                .withEndTime("12:00")
                                                .withCount(3)
                                                .withDays(Arrays
                                                    .asList(ScheduleDay.fromString("Monday, Tuesday, Wednesday"))),
                                            new Schedule().withStartTime("00:00")
                                                .withEndTime("12:00")
                                                .withCount(3)
                                                .withDays(Arrays.asList(ScheduleDay.SUNDAY))))))
                            .withAuthorizationProfile(
                                new AuthorizationProfile().withUserIds(Arrays.asList("Testuser1", "Testuser2")))
                            .withLogAnalyticsProfile(new ClusterLogAnalyticsProfile().withEnabled(true)
                                .withApplicationLogs(new ClusterLogAnalyticsApplicationLogs().withStdOutEnabled(true)
                                    .withStdErrorEnabled(true))
                                .withMetricsEnabled(true))))
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

### Clusters_Upgrade

```java
import com.azure.resourcemanager.hdinsight.containers.models.ClusterAksPatchVersionUpgradeProperties;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterHotfixUpgradeProperties;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterUpgrade;

/**
 * Samples for Clusters Upgrade.
 */
public final class ClustersUpgradeSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * UpgradeAKSPatchVersionForCluster.json
     */
    /**
     * Sample code: ClustersUpgradeAKSPatchVersion.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clustersUpgradeAKSPatchVersion(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .upgrade("hiloResourcegroup", "clusterpool1", "cluster1",
                new ClusterUpgrade().withProperties(new ClusterAksPatchVersionUpgradeProperties()),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * UpgradeHotfixForCluster.json
     */
    /**
     * Sample code: ClustersUpgradeHotfix.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clustersUpgradeHotfix(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .upgrade("hiloResourcegroup", "clusterpool1", "cluster1",
                new ClusterUpgrade().withProperties(new ClusterHotfixUpgradeProperties().withTargetOssVersion("1.16.0")
                    .withTargetClusterVersion("1.0.6")
                    .withTargetBuildNumber("3")
                    .withComponentName("historyserver")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_UpgradeManualRollback

```java
import com.azure.resourcemanager.hdinsight.containers.models.ClusterUpgradeRollback;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterUpgradeRollbackProperties;

/**
 * Samples for Clusters UpgradeManualRollback.
 */
public final class ClustersUpgradeManualRollbackSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * ClusterUpgradeRollback.json
     */
    /**
     * Sample code: ClusterUpgradeRollback.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clusterUpgradeRollback(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .upgradeManualRollback("hiloResourcegroup", "clusterpool1", "cluster1",
                new ClusterUpgradeRollback().withProperties(new ClusterUpgradeRollbackProperties().withUpgradeHistory(
                    "/subscriptions/10e32bab-26da-4cc4-a441-52b318f824e6/resourceGroups/hiloResourcegroup/providers/Microsoft.HDInsight/clusterpools/clusterpool1/clusters/cluster1/upgradeHistories/01_11_2024_02_35_03_AM-HotfixUpgrade")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Locations_CheckNameAvailability

```java
import com.azure.resourcemanager.hdinsight.containers.models.NameAvailabilityParameters;

/**
 * Samples for Locations CheckNameAvailability.
 */
public final class LocationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * LocationsNameAvailability.json
     */
    /**
     * Sample code: LocationsNameAvailability.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        locationsNameAvailability(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.locations()
            .checkNameAvailabilityWithResponse("southeastasia",
                new NameAvailabilityParameters().withName("contosemember1")
                    .withType("Microsoft.HDInsight/clusterPools/clusters"),
                com.azure.core.util.Context.NONE);
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
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * GetOperations.json
     */
    /**
     * Sample code: ListOperations.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        listOperations(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

