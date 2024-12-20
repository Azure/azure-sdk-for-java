# Code snippets and samples


## LoadTests

- [CreateOrUpdate](#loadtests_createorupdate)
- [Delete](#loadtests_delete)
- [GetByResourceGroup](#loadtests_getbyresourcegroup)
- [List](#loadtests_list)
- [ListByResourceGroup](#loadtests_listbyresourcegroup)
- [ListOutboundNetworkDependenciesEndpoints](#loadtests_listoutboundnetworkdependenciesendpoints)
- [Update](#loadtests_update)

## Operations

- [List](#operations_list)

## Quotas

- [CheckAvailability](#quotas_checkavailability)
- [Get](#quotas_get)
- [List](#quotas_list)
### LoadTests_CreateOrUpdate

```java
/**
 * Samples for LoadTests CreateOrUpdate.
 */
public final class LoadTestsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/
     * LoadTests_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a LoadTestResource.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void createALoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests()
            .define("myLoadTest")
            .withRegion((String) null)
            .withExistingResourceGroup("dummyrg")
            .create();
    }
}
```

### LoadTests_Delete

```java
/**
 * Samples for LoadTests Delete.
 */
public final class LoadTestsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/
     * LoadTests_Delete.json
     */
    /**
     * Sample code: Delete a LoadTestResource.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void deleteALoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests().delete("dummyrg", "myLoadTest", com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_GetByResourceGroup

```java
/**
 * Samples for LoadTests GetByResourceGroup.
 */
public final class LoadTestsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/LoadTests_Get
     * .json
     */
    /**
     * Sample code: Get a LoadTestResource.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void getALoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests().getByResourceGroupWithResponse("dummyrg", "myLoadTest", com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_List

```java
/**
 * Samples for LoadTests List.
 */
public final class LoadTestsListSamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/
     * LoadTests_ListBySubscription.json
     */
    /**
     * Sample code: List LoadTestResource resources by subscription ID.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void
        listLoadTestResourceResourcesBySubscriptionID(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests().list(com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_ListByResourceGroup

```java
/**
 * Samples for LoadTests ListByResourceGroup.
 */
public final class LoadTestsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/
     * LoadTests_ListByResourceGroup.json
     */
    /**
     * Sample code: List LoadTestResource resources by resource group.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void
        listLoadTestResourceResourcesByResourceGroup(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests().listByResourceGroup("dummyrg", com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_ListOutboundNetworkDependenciesEndpoints

```java
/**
 * Samples for LoadTests ListOutboundNetworkDependenciesEndpoints.
 */
public final class LoadTestsListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/
     * LoadTests_ListOutboundNetworkDependenciesEndpoints.json
     */
    /**
     * Sample code: Lists the endpoints that agents may call as part of load testing.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void listsTheEndpointsThatAgentsMayCallAsPartOfLoadTesting(
        com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests()
            .listOutboundNetworkDependenciesEndpoints("default-azureloadtest-japaneast", "sampleloadtest",
                com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_Update

```java
import com.azure.resourcemanager.loadtesting.models.LoadTestResource;

/**
 * Samples for LoadTests Update.
 */
public final class LoadTestsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/
     * LoadTests_Update.json
     */
    /**
     * Sample code: Update a LoadTestResource.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void updateALoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        LoadTestResource resource = manager.loadTests()
            .getByResourceGroupWithResponse("dummyrg", "myLoadTest", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
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
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/
     * Operations_List.json
     */
    /**
     * Sample code: List the operations for the provider.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void listTheOperationsForTheProvider(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Quotas_CheckAvailability

```java

/**
 * Samples for Quotas CheckAvailability.
 */
public final class QuotasCheckAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/
     * Quotas_CheckAvailability.json
     */
    /**
     * Sample code: Check Quota Availability on quota bucket per region per subscription.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void checkQuotaAvailabilityOnQuotaBucketPerRegionPerSubscription(
        com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.quotas()
            .checkAvailabilityWithResponse("westus", "testQuotaBucket", null, com.azure.core.util.Context.NONE);
    }
}
```

### Quotas_Get

```java
/**
 * Samples for Quotas Get.
 */
public final class QuotasGetSamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/Quotas_Get.
     * json
     */
    /**
     * Sample code: Get the available quota for a quota bucket per region per subscription.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void getTheAvailableQuotaForAQuotaBucketPerRegionPerSubscription(
        com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.quotas().getWithResponse("westus", "testQuotaBucket", com.azure.core.util.Context.NONE);
    }
}
```

### Quotas_List

```java
/**
 * Samples for Quotas List.
 */
public final class QuotasListSamples {
    /*
     * x-ms-original-file:
     * specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/Quotas_List.
     * json
     */
    /**
     * Sample code: List quotas for a given subscription Id.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void
        listQuotasForAGivenSubscriptionId(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.quotas().list("westus", com.azure.core.util.Context.NONE);
    }
}
```

