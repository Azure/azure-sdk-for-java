# Code snippets and samples


## DeploymentSafeguards

- [Create](#deploymentsafeguards_create)
- [Delete](#deploymentsafeguards_delete)
- [Get](#deploymentsafeguards_get)
- [List](#deploymentsafeguards_list)

## Operations

- [List](#operations_list)
### DeploymentSafeguards_Create

```java
/**
 * Samples for DeploymentSafeguards Get.
 */
public final class DeploymentSafeguardsGetSamples {
    /*
     * x-ms-original-file: 2025-05-02-preview/DeploymentSafeguards_Get.json
     */
    /**
     * Sample code: Gets a DeploymentSafeguard resource.
     * 
     * @param manager Entry point to ContainerServiceSafeguardsManager.
     */
    public static void getsADeploymentSafeguardResource(
        com.azure.resourcemanager.containerservicesafeguards.ContainerServiceSafeguardsManager manager) {
        manager.deploymentSafeguards()
            .getWithResponse(
                "subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.ContainerService/managedClusters/cluster1",
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentSafeguards_Delete

```java
/**
 * Samples for DeploymentSafeguards List.
 */
public final class DeploymentSafeguardsListSamples {
    /*
     * x-ms-original-file: 2025-05-02-preview/DeploymentSafeguards_List.json
     */
    /**
     * Sample code: Lists DeploymentSafeguards by parent resource.
     * 
     * @param manager Entry point to ContainerServiceSafeguardsManager.
     */
    public static void listsDeploymentSafeguardsByParentResource(
        com.azure.resourcemanager.containerservicesafeguards.ContainerServiceSafeguardsManager manager) {
        manager.deploymentSafeguards()
            .list(
                "subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.ContainerService/managedClusters/cluster1",
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentSafeguards_Get

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-05-02-preview/Operations_List.json
     */
    /**
     * Sample code: List the operations for the provider.
     * 
     * @param manager Entry point to ContainerServiceSafeguardsManager.
     */
    public static void listTheOperationsForTheProvider(
        com.azure.resourcemanager.containerservicesafeguards.ContainerServiceSafeguardsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentSafeguards_List

```java
/**
 * Samples for DeploymentSafeguards Delete.
 */
public final class DeploymentSafeguardsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-02-preview/DeploymentSafeguards_Delete.json
     */
    /**
     * Sample code: Deletes a DeploymentSafeguard resource asynchronously with a long running operation.
     * 
     * @param manager Entry point to ContainerServiceSafeguardsManager.
     */
    public static void deletesADeploymentSafeguardResourceAsynchronouslyWithALongRunningOperation(
        com.azure.resourcemanager.containerservicesafeguards.ContainerServiceSafeguardsManager manager) {
        manager.deploymentSafeguards()
            .delete(
                "subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.ContainerService/managedClusters/cluster1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.resourcemanager.containerservicesafeguards.fluent.models.DeploymentSafeguardInner;
import com.azure.resourcemanager.containerservicesafeguards.models.DeploymentSafeguardsLevel;
import com.azure.resourcemanager.containerservicesafeguards.models.DeploymentSafeguardsProperties;
import com.azure.resourcemanager.containerservicesafeguards.models.PodSecurityStandardsLevel;

/**
 * Samples for DeploymentSafeguards Create.
 */
public final class DeploymentSafeguardsCreateSamples {
    /*
     * x-ms-original-file: 2025-05-02-preview/DeploymentSafeguards_Create.json
     */
    /**
     * Sample code: Creates a DeploymentSafeguards resource with a long running operation.
     * 
     * @param manager Entry point to ContainerServiceSafeguardsManager.
     */
    public static void createsADeploymentSafeguardsResourceWithALongRunningOperation(
        com.azure.resourcemanager.containerservicesafeguards.ContainerServiceSafeguardsManager manager) {
        manager.deploymentSafeguards()
            .create(
                "subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.ContainerService/managedClusters/cluster1",
                new DeploymentSafeguardInner()
                    .withProperties(new DeploymentSafeguardsProperties().withLevel(DeploymentSafeguardsLevel.WARN)
                        .withPodSecurityStandardsLevel(PodSecurityStandardsLevel.POD_SECURITY_STANDARDS_BASELINE)),
                com.azure.core.util.Context.NONE);
    }
}
```

