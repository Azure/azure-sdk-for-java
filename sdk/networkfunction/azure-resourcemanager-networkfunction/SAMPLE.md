# Code snippets and samples


## AzureTrafficCollectors

- [CreateOrUpdate](#azuretrafficcollectors_createorupdate)
- [Delete](#azuretrafficcollectors_delete)
- [GetByResourceGroup](#azuretrafficcollectors_getbyresourcegroup)
- [UpdateTags](#azuretrafficcollectors_updatetags)

## AzureTrafficCollectorsByResourceGroup

- [ListByResourceGroup](#azuretrafficcollectorsbyresourcegroup_listbyresourcegroup)

## AzureTrafficCollectorsBySubscription

- [List](#azuretrafficcollectorsbysubscription_list)

## CollectorPolicies

- [CreateOrUpdate](#collectorpolicies_createorupdate)
- [Delete](#collectorpolicies_delete)
- [Get](#collectorpolicies_get)
- [List](#collectorpolicies_list)

## NetworkFunction

- [ListOperations](#networkfunction_listoperations)
### AzureTrafficCollectors_CreateOrUpdate

```java
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for AzureTrafficCollectors CreateOrUpdate. */
public final class AzureTrafficCollectorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/AzureTrafficCollectorCreate.json
     */
    /**
     * Sample code: Create a traffic collector.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void createATrafficCollector(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager
            .azureTrafficCollectors()
            .define("atc")
            .withRegion("West US")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "value1"))
            .withCollectorPolicies(Arrays.asList())
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

### AzureTrafficCollectors_Delete

```java
import com.azure.core.util.Context;

/** Samples for AzureTrafficCollectors Delete. */
public final class AzureTrafficCollectorsDeleteSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/AzureTrafficCollectorDelete.json
     */
    /**
     * Sample code: Delete Traffic Collector.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void deleteTrafficCollector(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager.azureTrafficCollectors().delete("rg1", "atc", Context.NONE);
    }
}
```

### AzureTrafficCollectors_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AzureTrafficCollectors GetByResourceGroup. */
public final class AzureTrafficCollectorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/AzureTrafficCollectorGet.json
     */
    /**
     * Sample code: Get Traffic Collector.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void getTrafficCollector(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager.azureTrafficCollectors().getByResourceGroupWithResponse("rg1", "atc", Context.NONE);
    }
}
```

### AzureTrafficCollectors_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.networkfunction.models.AzureTrafficCollector;
import java.util.HashMap;
import java.util.Map;

/** Samples for AzureTrafficCollectors UpdateTags. */
public final class AzureTrafficCollectorsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/AzureTrafficCollectorUpdateTags.json
     */
    /**
     * Sample code: Update Traffic Collector tags.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void updateTrafficCollectorTags(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        AzureTrafficCollector resource =
            manager.azureTrafficCollectors().getByResourceGroupWithResponse("rg1", "atc", Context.NONE).getValue();
        resource.update().withTags(mapOf("key1", "value1", "key2", "value2")).apply();
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

### AzureTrafficCollectorsByResourceGroup_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AzureTrafficCollectorsByResourceGroup ListByResourceGroup. */
public final class AzureTrafficCollectorsByResourceGroupListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/AzureTrafficCollectorsByResourceGroupList.json
     */
    /**
     * Sample code: List of Traffic Collectors by ResourceGroup.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void listOfTrafficCollectorsByResourceGroup(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager.azureTrafficCollectorsByResourceGroups().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### AzureTrafficCollectorsBySubscription_List

```java
import com.azure.core.util.Context;

/** Samples for AzureTrafficCollectorsBySubscription List. */
public final class AzureTrafficCollectorsBySubscriptionListSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/AzureTrafficCollectorsBySubscriptionList.json
     */
    /**
     * Sample code: List of Traffic Collectors by Subscription.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void listOfTrafficCollectorsBySubscription(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager.azureTrafficCollectorsBySubscriptions().list(Context.NONE);
    }
}
```

### CollectorPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.networkfunction.models.DestinationType;
import com.azure.resourcemanager.networkfunction.models.EmissionPoliciesPropertiesFormat;
import com.azure.resourcemanager.networkfunction.models.EmissionPolicyDestination;
import com.azure.resourcemanager.networkfunction.models.EmissionType;
import com.azure.resourcemanager.networkfunction.models.IngestionPolicyPropertiesFormat;
import com.azure.resourcemanager.networkfunction.models.IngestionSourcesPropertiesFormat;
import com.azure.resourcemanager.networkfunction.models.IngestionType;
import com.azure.resourcemanager.networkfunction.models.SourceType;
import java.util.Arrays;

/** Samples for CollectorPolicies CreateOrUpdate. */
public final class CollectorPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/CollectorPolicyCreate.json
     */
    /**
     * Sample code: Create a collection policy.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void createACollectionPolicy(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager
            .collectorPolicies()
            .define("cp1")
            .withExistingAzureTrafficCollector("rg1", "atc")
            .withIngestionPolicy(
                new IngestionPolicyPropertiesFormat()
                    .withIngestionType(IngestionType.IPFIX)
                    .withIngestionSources(
                        Arrays
                            .asList(
                                new IngestionSourcesPropertiesFormat()
                                    .withSourceType(SourceType.RESOURCE)
                                    .withResourceId(
                                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/expressRouteCircuits/circuitName"))))
            .withEmissionPolicies(
                Arrays
                    .asList(
                        new EmissionPoliciesPropertiesFormat()
                            .withEmissionType(EmissionType.IPFIX)
                            .withEmissionDestinations(
                                Arrays
                                    .asList(
                                        new EmissionPolicyDestination()
                                            .withDestinationType(DestinationType.AZURE_MONITOR)))))
            .create();
    }
}
```

### CollectorPolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for CollectorPolicies Delete. */
public final class CollectorPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/CollectorPolicyDelete.json
     */
    /**
     * Sample code: Delete Collection Policy.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void deleteCollectionPolicy(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager.collectorPolicies().delete("rg1", "atc", "cp1", Context.NONE);
    }
}
```

### CollectorPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for CollectorPolicies Get. */
public final class CollectorPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/CollectorPolicyGet.json
     */
    /**
     * Sample code: Get Collection Policy.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void getCollectionPolicy(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager.collectorPolicies().getWithResponse("rg1", "atc", "cp1", Context.NONE);
    }
}
```

### CollectorPolicies_List

```java
import com.azure.core.util.Context;

/** Samples for CollectorPolicies List. */
public final class CollectorPoliciesListSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/CollectorPoliciesList.json
     */
    /**
     * Sample code: List of Collection Policies.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void listOfCollectionPolicies(
        com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager.collectorPolicies().list("rg1", "atc", Context.NONE);
    }
}
```

### NetworkFunction_ListOperations

```java
import com.azure.core.util.Context;

/** Samples for NetworkFunction ListOperations. */
public final class NetworkFunctionListOperationsSamples {
    /*
     * x-ms-original-file: specification/networkfunction/resource-manager/Microsoft.NetworkFunction/stable/2022-05-01/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to AzureTrafficCollectorManager.
     */
    public static void operationsList(com.azure.resourcemanager.networkfunction.AzureTrafficCollectorManager manager) {
        manager.networkFunctions().listOperations(Context.NONE);
    }
}
```

