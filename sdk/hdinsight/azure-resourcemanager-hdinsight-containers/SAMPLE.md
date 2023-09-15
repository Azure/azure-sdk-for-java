# Code snippets and samples


## AvailableClusterPoolVersions

- [ListByLocation](#availableclusterpoolversions_listbylocation)

## AvailableClusterVersions

- [ListByLocation](#availableclusterversions_listbylocation)

## ClusterJobs

- [List](#clusterjobs_list)
- [RunJob](#clusterjobs_runjob)

## ClusterPools

- [CreateOrUpdate](#clusterpools_createorupdate)
- [Delete](#clusterpools_delete)
- [GetByResourceGroup](#clusterpools_getbyresourcegroup)
- [List](#clusterpools_list)
- [ListByResourceGroup](#clusterpools_listbyresourcegroup)
- [UpdateTags](#clusterpools_updatetags)

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

## Locations

- [CheckNameAvailability](#locations_checknameavailability)

## Operations

- [List](#operations_list)
### AvailableClusterPoolVersions_ListByLocation

```java
/** Samples for AvailableClusterPoolVersions ListByLocation. */
public final class AvailableClusterPoolVersionsListByLocationSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/ListAvailableClusterPoolVersions.json
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
/** Samples for AvailableClusterVersions ListByLocation. */
public final class AvailableClusterVersionsListByLocationSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/ListAvailableClusterVersions.json
     */
    /**
     * Sample code: ClusterVersionListResult.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterVersionListResult(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.availableClusterVersions().listByLocation("westus2", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterJobs_List

```java
/** Samples for ClusterJobs List. */
public final class ClusterJobsListSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/ListClusterJobs.json
     */
    /**
     * Sample code: ListClusterJobs.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void listClusterJobs(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterJobs().list("hiloResourcegroup", "clusterPool1", "cluster1", com.azure.core.util.Context.NONE);
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

/** Samples for ClusterJobs RunJob. */
public final class ClusterJobsRunJobSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/RunClusterJob.json
     */
    /**
     * Sample code: RunClusterJob.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void runClusterJob(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager
            .clusterJobs()
            .runJob(
                "hiloResourcegroup",
                "clusterpool1",
                "cluster1",
                new ClusterJobInner()
                    .withProperties(
                        new FlinkJobProperties()
                            .withJobName("flink-job-name")
                            .withJobJarDirectory("abfs://flinkjob@hilosa.dfs.core.windows.net/jars")
                            .withJarName("flink-sleep-job-0.0.1-SNAPSHOT.jar")
                            .withEntryClass("com.microsoft.hilo.flink.job.streaming.SleepJob")
                            .withAction(Action.START)
                            .withFlinkConfiguration(
                                mapOf(
                                    "parallelism",
                                    "1",
                                    "savepoint.directory",
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

### ClusterPools_CreateOrUpdate

```java
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPoolResourcePropertiesClusterPoolProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterPoolResourcePropertiesComputeProfile;

/** Samples for ClusterPools CreateOrUpdate. */
public final class ClusterPoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/CreateClusterPool.json
     */
    /**
     * Sample code: ClusterPoolPut.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolPut(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager
            .clusterPools()
            .define("clusterpool1")
            .withRegion("West US 2")
            .withExistingResourceGroup("hiloResourcegroup")
            .withClusterPoolProfile(new ClusterPoolResourcePropertiesClusterPoolProfile().withClusterPoolVersion("1.2"))
            .withComputeProfile(new ClusterPoolResourcePropertiesComputeProfile().withVmSize("Standard_D3_v2"))
            .create();
    }
}
```

### ClusterPools_Delete

```java
/** Samples for ClusterPools Delete. */
public final class ClusterPoolsDeleteSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/DeleteClusterPool.json
     */
    /**
     * Sample code: ClusterPoolDelete.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolDelete(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusterPools().delete("rg1", "clusterpool1", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterPools_GetByResourceGroup

```java
/** Samples for ClusterPools GetByResourceGroup. */
public final class ClusterPoolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/GetClusterPool.json
     */
    /**
     * Sample code: ClusterPoolGet.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolGet(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager
            .clusterPools()
            .getByResourceGroupWithResponse("hiloResourcegroup", "clusterpool1", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterPools_List

```java
/** Samples for ClusterPools List. */
public final class ClusterPoolsListSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/ListClusterPoolsSubscription.json
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
/** Samples for ClusterPools ListByResourceGroup. */
public final class ClusterPoolsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/ListClusterPools.json
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

/** Samples for ClusterPools UpdateTags. */
public final class ClusterPoolsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/PatchClusterPool.json
     */
    /**
     * Sample code: ClusterPoolsPatchTags.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clusterPoolsPatchTags(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        ClusterPool resource =
            manager
                .clusterPools()
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

### Clusters_Create

```java
import com.azure.resourcemanager.hdinsight.containers.models.AuthorizationProfile;
import com.azure.resourcemanager.hdinsight.containers.models.AutoscaleProfile;
import com.azure.resourcemanager.hdinsight.containers.models.AutoscaleType;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterConfigFile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterServiceConfig;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterServiceConfigsProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ComparisonOperator;
import com.azure.resourcemanager.hdinsight.containers.models.ComparisonRule;
import com.azure.resourcemanager.hdinsight.containers.models.ComputeProfile;
import com.azure.resourcemanager.hdinsight.containers.models.IdentityProfile;
import com.azure.resourcemanager.hdinsight.containers.models.LoadBasedConfig;
import com.azure.resourcemanager.hdinsight.containers.models.NodeProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ScaleActionType;
import com.azure.resourcemanager.hdinsight.containers.models.ScalingRule;
import com.azure.resourcemanager.hdinsight.containers.models.Schedule;
import com.azure.resourcemanager.hdinsight.containers.models.ScheduleBasedConfig;
import com.azure.resourcemanager.hdinsight.containers.models.ScheduleDay;
import com.azure.resourcemanager.hdinsight.containers.models.SparkProfile;
import com.azure.resourcemanager.hdinsight.containers.models.SshProfile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Create. */
public final class ClustersCreateSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/CreateSparkCluster.json
     */
    /**
     * Sample code: HDInsightSparkClusterPut.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightSparkClusterPut(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager
            .clusters()
            .define("cluster1")
            .withRegion("West US 2")
            .withExistingClusterpool("hiloResourcegroup", "clusterpool1")
            .withClusterType("spark")
            .withComputeProfile(
                new ComputeProfile()
                    .withNodes(
                        Arrays.asList(new NodeProfile().withType("worker").withVmSize("Standard_D3_v2").withCount(4))))
            .withClusterProfile(
                new ClusterProfile()
                    .withClusterVersion("0.0.1")
                    .withOssVersion("2.2.3")
                    .withIdentityProfile(
                        new IdentityProfile()
                            .withMsiResourceId(
                                "/subscriptions/subid/resourceGroups/hiloResourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-msi")
                            .withMsiClientId("de91f1d8-767f-460a-ac11-3cf103f74b34")
                            .withMsiObjectId("40491351-c240-4042-91e0-f644a1d2b441"))
                    .withAuthorizationProfile(
                        new AuthorizationProfile().withUserIds(Arrays.asList("testuser1", "testuser2")))
                    .withServiceConfigsProfiles(
                        Arrays
                            .asList(
                                new ClusterServiceConfigsProfile()
                                    .withServiceName("spark-service")
                                    .withConfigs(
                                        Arrays
                                            .asList(
                                                new ClusterServiceConfig()
                                                    .withComponent("spark-config")
                                                    .withFiles(
                                                        Arrays
                                                            .asList(
                                                                new ClusterConfigFile()
                                                                    .withFileName("spark-defaults.conf")
                                                                    .withValues(
                                                                        mapOf("spark.eventLog.enabled", "true")))))),
                                new ClusterServiceConfigsProfile()
                                    .withServiceName("yarn-service")
                                    .withConfigs(
                                        Arrays
                                            .asList(
                                                new ClusterServiceConfig()
                                                    .withComponent("yarn-config")
                                                    .withFiles(
                                                        Arrays
                                                            .asList(
                                                                new ClusterConfigFile()
                                                                    .withFileName("core-site.xml")
                                                                    .withValues(
                                                                        mapOf(
                                                                            "fs.defaultFS",
                                                                            "wasb://testcontainer@teststorage.dfs.core.windows.net/",
                                                                            "storage.container",
                                                                            "testcontainer",
                                                                            "storage.key",
                                                                            "fakeTokenPlaceholder",
                                                                            "storage.name",
                                                                            "teststorage",
                                                                            "storage.protocol",
                                                                            "wasb")),
                                                                new ClusterConfigFile()
                                                                    .withFileName("yarn-site.xml")
                                                                    .withValues(
                                                                        mapOf("yarn.webapp.ui2.enable", "false"))))))))
                    .withSshProfile(new SshProfile().withCount(2))
                    .withSparkProfile(new SparkProfile()))
            .create();
    }

    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/CreateAutoscaleCluster.json
     */
    /**
     * Sample code: HDInsightClusterPut.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClusterPut(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager
            .clusters()
            .define("cluster1")
            .withRegion("West US 2")
            .withExistingClusterpool("hiloResourcegroup", "clusterpool1")
            .withClusterType("kafka")
            .withComputeProfile(
                new ComputeProfile()
                    .withNodes(
                        Arrays.asList(new NodeProfile().withType("worker").withVmSize("Standard_D3_v2").withCount(4))))
            .withClusterProfile(
                new ClusterProfile()
                    .withClusterVersion("1.0.1")
                    .withOssVersion("2.4.1")
                    .withIdentityProfile(
                        new IdentityProfile()
                            .withMsiResourceId(
                                "/subscriptions/subid/resourceGroups/hiloResourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-msi")
                            .withMsiClientId("de91f1d8-767f-460a-ac11-3cf103f74b34")
                            .withMsiObjectId("40491351-c240-4042-91e0-f644a1d2b441"))
                    .withAuthorizationProfile(
                        new AuthorizationProfile().withUserIds(Arrays.asList("testuser1", "testuser2")))
                    .withSshProfile(new SshProfile().withCount(2))
                    .withAutoscaleProfile(
                        new AutoscaleProfile()
                            .withEnabled(true)
                            .withGracefulDecommissionTimeout(3600)
                            .withAutoscaleType(AutoscaleType.SCHEDULE_BASED)
                            .withScheduleBasedConfig(
                                new ScheduleBasedConfig()
                                    .withTimeZone("Cen. Australia Standard Time")
                                    .withDefaultCount(10)
                                    .withSchedules(
                                        Arrays
                                            .asList(
                                                new Schedule()
                                                    .withStartTime("00:00")
                                                    .withEndTime("12:00")
                                                    .withCount(20)
                                                    .withDays(Arrays.asList(ScheduleDay.MONDAY)),
                                                new Schedule()
                                                    .withStartTime("00:00")
                                                    .withEndTime("12:00")
                                                    .withCount(25)
                                                    .withDays(Arrays.asList(ScheduleDay.SUNDAY)))))
                            .withLoadBasedConfig(
                                new LoadBasedConfig()
                                    .withMinNodes(10)
                                    .withMaxNodes(20)
                                    .withPollInterval(60)
                                    .withCooldownPeriod(300)
                                    .withScalingRules(
                                        Arrays
                                            .asList(
                                                new ScalingRule()
                                                    .withActionType(ScaleActionType.SCALEUP)
                                                    .withEvaluationCount(3)
                                                    .withScalingMetric("cpu")
                                                    .withComparisonRule(
                                                        new ComparisonRule()
                                                            .withOperator(ComparisonOperator.GREATER_THAN)
                                                            .withThreshold(90f)),
                                                new ScalingRule()
                                                    .withActionType(ScaleActionType.SCALEDOWN)
                                                    .withEvaluationCount(3)
                                                    .withScalingMetric("cpu")
                                                    .withComparisonRule(
                                                        new ComparisonRule()
                                                            .withOperator(ComparisonOperator.LESS_THAN)
                                                            .withThreshold(20f))))))
                    .withKafkaProfile(mapOf()))
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
/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/DeleteCluster.json
     */
    /**
     * Sample code: HDInsightClustersDelete.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClustersDelete(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters().delete("rg1", "clusterpool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Get

```java
/** Samples for Clusters Get. */
public final class ClustersGetSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/GetCluster.json
     */
    /**
     * Sample code: HDInsightClusterGet.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClusterGet(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager
            .clusters()
            .getWithResponse("hiloResourcegroup", "clusterpool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetInstanceView

```java
/** Samples for Clusters GetInstanceView. */
public final class ClustersGetInstanceViewSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/GetClusterInstanceView.json
     */
    /**
     * Sample code: HDInsightClusterGetInstanceView.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClusterGetInstanceView(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager
            .clusters()
            .getInstanceViewWithResponse("rg1", "clusterPool1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByClusterPoolName

```java
/** Samples for Clusters ListByClusterPoolName. */
public final class ClustersListByClusterPoolNameSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/ListClustersByClusterPoolName.json
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
/** Samples for Clusters ListInstanceViews. */
public final class ClustersListInstanceViewsSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/ListClusterInstanceViews.json
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
/** Samples for Clusters ListServiceConfigs. */
public final class ClustersListServiceConfigsSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/ListClusterServiceConfigs.json
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

/** Samples for Clusters Resize. */
public final class ClustersResizeSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/ResizeCluster.json
     */
    /**
     * Sample code: HDInsightClusterResize.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClusterResize(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager
            .clusters()
            .resize(
                "hiloResourcegroup",
                "clusterpool1",
                "cluster1",
                new ClusterResizeData().withLocation("West US 2").withTargetWorkerNodeCount(5),
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
import com.azure.resourcemanager.hdinsight.containers.models.ClusterServiceConfig;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterServiceConfigsProfile;
import com.azure.resourcemanager.hdinsight.containers.models.Schedule;
import com.azure.resourcemanager.hdinsight.containers.models.ScheduleBasedConfig;
import com.azure.resourcemanager.hdinsight.containers.models.ScheduleDay;
import com.azure.resourcemanager.hdinsight.containers.models.SshProfile;
import com.azure.resourcemanager.hdinsight.containers.models.UpdatableClusterProfile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/PatchCluster.json
     */
    /**
     * Sample code: HDInsightClustersPatchTags.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void hDInsightClustersPatchTags(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getWithResponse("hiloResourcegroup", "clusterpool1", "cluster1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withClusterProfile(
                new UpdatableClusterProfile()
                    .withServiceConfigsProfiles(
                        Arrays
                            .asList(
                                new ClusterServiceConfigsProfile()
                                    .withServiceName("TestService1")
                                    .withConfigs(
                                        Arrays
                                            .asList(
                                                new ClusterServiceConfig()
                                                    .withComponent("TestComp1")
                                                    .withFiles(
                                                        Arrays
                                                            .asList(
                                                                new ClusterConfigFile()
                                                                    .withFileName("TestFile1")
                                                                    .withValues(
                                                                        mapOf(
                                                                            "Test.config.1",
                                                                            "1",
                                                                            "Test.config.2",
                                                                            "2")),
                                                                new ClusterConfigFile()
                                                                    .withFileName("TestFile2")
                                                                    .withValues(
                                                                        mapOf(
                                                                            "Test.config.3",
                                                                            "3",
                                                                            "Test.config.4",
                                                                            "4")))),
                                                new ClusterServiceConfig()
                                                    .withComponent("TestComp2")
                                                    .withFiles(
                                                        Arrays
                                                            .asList(
                                                                new ClusterConfigFile()
                                                                    .withFileName("TestFile3")
                                                                    .withContent("TestContent")
                                                                    .withPath("TestPath"),
                                                                new ClusterConfigFile()
                                                                    .withFileName("TestFile4")
                                                                    .withValues(
                                                                        mapOf(
                                                                            "Test.config.7",
                                                                            "7",
                                                                            "Test.config.8",
                                                                            "8")))))),
                                new ClusterServiceConfigsProfile()
                                    .withServiceName("TestService2")
                                    .withConfigs(
                                        Arrays
                                            .asList(
                                                new ClusterServiceConfig()
                                                    .withComponent("TestComp3")
                                                    .withFiles(
                                                        Arrays
                                                            .asList(
                                                                new ClusterConfigFile()
                                                                    .withFileName("TestFile5")
                                                                    .withValues(mapOf("Test.config.9", "9"))))))))
                    .withSshProfile(new SshProfile().withCount(2))
                    .withAutoscaleProfile(
                        new AutoscaleProfile()
                            .withEnabled(true)
                            .withGracefulDecommissionTimeout(-1)
                            .withAutoscaleType(AutoscaleType.SCHEDULE_BASED)
                            .withScheduleBasedConfig(
                                new ScheduleBasedConfig()
                                    .withTimeZone("Cen. Australia Standard Time")
                                    .withDefaultCount(3)
                                    .withSchedules(
                                        Arrays
                                            .asList(
                                                new Schedule()
                                                    .withStartTime("00:00")
                                                    .withEndTime("12:00")
                                                    .withCount(3)
                                                    .withDays(
                                                        Arrays
                                                            .asList(
                                                                ScheduleDay.fromString("Monday, Tuesday, Wednesday"))),
                                                new Schedule()
                                                    .withStartTime("00:00")
                                                    .withEndTime("12:00")
                                                    .withCount(3)
                                                    .withDays(Arrays.asList(ScheduleDay.SUNDAY))))))
                    .withAuthorizationProfile(
                        new AuthorizationProfile().withUserIds(Arrays.asList("Testuser1", "Testuser2")))
                    .withLogAnalyticsProfile(
                        new ClusterLogAnalyticsProfile()
                            .withEnabled(true)
                            .withApplicationLogs(
                                new ClusterLogAnalyticsApplicationLogs()
                                    .withStdOutEnabled(true)
                                    .withStdErrorEnabled(true))
                            .withMetricsEnabled(true)))
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

### Locations_CheckNameAvailability

```java
import com.azure.resourcemanager.hdinsight.containers.models.NameAvailabilityParameters;

/** Samples for Locations CheckNameAvailability. */
public final class LocationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/LocationsNameAvailability.json
     */
    /**
     * Sample code: LocationsNameAvailability.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void locationsNameAvailability(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager
            .locations()
            .checkNameAvailabilityWithResponse(
                "southeastasia",
                new NameAvailabilityParameters()
                    .withName("contosemember1")
                    .withType("Microsoft.HDInsight/clusterPools/clusters"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2023-06-01-preview/examples/GetOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void listOperations(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

