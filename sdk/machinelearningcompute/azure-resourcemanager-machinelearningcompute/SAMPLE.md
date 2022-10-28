# Code snippets and samples


## MachineLearningCompute

- [ListAvailableOperations](#machinelearningcompute_listavailableoperations)

## OperationalizationClusters

- [CheckSystemServicesUpdatesAvailable](#operationalizationclusters_checksystemservicesupdatesavailable)
- [CreateOrUpdate](#operationalizationclusters_createorupdate)
- [Delete](#operationalizationclusters_delete)
- [GetByResourceGroup](#operationalizationclusters_getbyresourcegroup)
- [List](#operationalizationclusters_list)
- [ListByResourceGroup](#operationalizationclusters_listbyresourcegroup)
- [ListKeys](#operationalizationclusters_listkeys)
- [Update](#operationalizationclusters_update)
- [UpdateSystemServices](#operationalizationclusters_updatesystemservices)
### MachineLearningCompute_ListAvailableOperations

```java
import com.azure.core.util.Context;

/** Samples for MachineLearningCompute ListAvailableOperations. */
public final class MachineLearningComputeListAvailableOperationsSamples {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/MachineLearningCompute_ListAvailableOperations.json
     */
    /**
     * Sample code: Machine Learning Compute List Available Operations.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void machineLearningComputeListAvailableOperations(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        manager.machineLearningComputes().listAvailableOperationsWithResponse(Context.NONE);
    }
}
```

### OperationalizationClusters_CheckSystemServicesUpdatesAvailable

```java
import com.azure.core.util.Context;

/** Samples for OperationalizationClusters CheckSystemServicesUpdatesAvailable. */
public final class OperationalizationClustersCheckSystemServicesUpdatesAvailableSamp {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/OperationalizationClusters_CheckSystemServicesUpdatesAvailable.json
     */
    /**
     * Sample code: Check Update for an Operationalization Cluster.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void checkUpdateForAnOperationalizationCluster(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        manager
            .operationalizationClusters()
            .checkSystemServicesUpdatesAvailableWithResponse("myResourceGroup", "myCluster", Context.NONE);
    }
}
```

### OperationalizationClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearningcompute.models.AcsClusterProperties;
import com.azure.resourcemanager.machinelearningcompute.models.ClusterType;
import com.azure.resourcemanager.machinelearningcompute.models.GlobalServiceConfiguration;
import com.azure.resourcemanager.machinelearningcompute.models.KubernetesClusterProperties;
import com.azure.resourcemanager.machinelearningcompute.models.OrchestratorType;
import com.azure.resourcemanager.machinelearningcompute.models.ServicePrincipalProperties;
import com.azure.resourcemanager.machinelearningcompute.models.SslConfiguration;
import com.azure.resourcemanager.machinelearningcompute.models.Status;
import java.util.HashMap;
import java.util.Map;

/** Samples for OperationalizationClusters CreateOrUpdate. */
public final class OperationalizationClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/OperationalizationClusters_CreateOrUpdate.json
     */
    /**
     * Sample code: PUT Operationalization Cluster.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void pUTOperationalizationCluster(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        manager
            .operationalizationClusters()
            .define("myCluster")
            .withRegion("West US")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key1", "alpha", "key2", "beta"))
            .withDescription("My Operationalization Cluster")
            .withClusterType(ClusterType.ACS)
            .withContainerService(
                new AcsClusterProperties()
                    .withOrchestratorType(OrchestratorType.KUBERNETES)
                    .withOrchestratorProperties(
                        new KubernetesClusterProperties()
                            .withServicePrincipal(
                                new ServicePrincipalProperties()
                                    .withClientId("abcdefghijklmnopqrt")
                                    .withSecret("<secret>"))))
            .withGlobalServiceConfiguration(
                new GlobalServiceConfiguration()
                    .withSsl(
                        new SslConfiguration()
                            .withStatus(Status.ENABLED)
                            .withCert("afjdklq2131casfakld=")
                            .withKey("flksdafkldsajf=")
                            .withCname("foo.bar.com"))
                    .withAdditionalProperties(mapOf()))
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

### OperationalizationClusters_Delete

```java
import com.azure.core.util.Context;

/** Samples for OperationalizationClusters Delete. */
public final class OperationalizationClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/OperationalizationClusters_Delete.json
     */
    /**
     * Sample code: DELETE Operationalization Cluster.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void dELETEOperationalizationCluster(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        manager.operationalizationClusters().delete("myResourceGroup", "myCluster", null, Context.NONE);
    }
}
```

### OperationalizationClusters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for OperationalizationClusters GetByResourceGroup. */
public final class OperationalizationClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/OperationalizationClusters_Get.json
     */
    /**
     * Sample code: GET Operationalization Cluster.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void gETOperationalizationCluster(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        manager
            .operationalizationClusters()
            .getByResourceGroupWithResponse("myResourceGroup", "myCluster", Context.NONE);
    }
}
```

### OperationalizationClusters_List

```java
import com.azure.core.util.Context;

/** Samples for OperationalizationClusters List. */
public final class OperationalizationClustersListSamples {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/OperationalizationClusters_ListBySubscription.json
     */
    /**
     * Sample code: List Operationalization Clusters by Subscription.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void listOperationalizationClustersBySubscription(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        manager.operationalizationClusters().list(null, Context.NONE);
    }
}
```

### OperationalizationClusters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for OperationalizationClusters ListByResourceGroup. */
public final class OperationalizationClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/OperationalizationClusters_ListByResourceGroup.json
     */
    /**
     * Sample code: List Operationalization Clusters by Resource Group.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void listOperationalizationClustersByResourceGroup(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        manager.operationalizationClusters().listByResourceGroup("myResourceGroup", null, Context.NONE);
    }
}
```

### OperationalizationClusters_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for OperationalizationClusters ListKeys. */
public final class OperationalizationClustersListKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/OperationalizationClusters_ListKeys.json
     */
    /**
     * Sample code: List Keys of an Operationalization Cluster.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void listKeysOfAnOperationalizationCluster(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        manager.operationalizationClusters().listKeysWithResponse("myResourceGroup", "myCluster", Context.NONE);
    }
}
```

### OperationalizationClusters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearningcompute.models.OperationalizationCluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for OperationalizationClusters Update. */
public final class OperationalizationClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/OperationalizationClusters_Update.json
     */
    /**
     * Sample code: PATCH Operationalization Cluster.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void pATCHOperationalizationCluster(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        OperationalizationCluster resource =
            manager
                .operationalizationClusters()
                .getByResourceGroupWithResponse("myResourceGroup", "myCluster", Context.NONE)
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

### OperationalizationClusters_UpdateSystemServices

```java
import com.azure.core.util.Context;

/** Samples for OperationalizationClusters UpdateSystemServices. */
public final class OperationalizationClustersUpdateSystemServicesSamples {
    /*
     * x-ms-original-file: specification/machinelearningcompute/resource-manager/Microsoft.MachineLearningCompute/preview/2017-08-01-preview/examples/OperationalizationClusters_UpdateSystemServices.json
     */
    /**
     * Sample code: Update System Services in an Operationalization Cluster.
     *
     * @param manager Entry point to MachineLearningComputeManager.
     */
    public static void updateSystemServicesInAnOperationalizationCluster(
        com.azure.resourcemanager.machinelearningcompute.MachineLearningComputeManager manager) {
        manager.operationalizationClusters().updateSystemServices("myResourceGroup", "myCluster", Context.NONE);
    }
}
```

