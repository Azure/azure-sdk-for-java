# Code snippets and samples


## AzureBareMetalInstances

- [GetByResourceGroup](#azurebaremetalinstances_getbyresourcegroup)
- [List](#azurebaremetalinstances_list)
- [ListByResourceGroup](#azurebaremetalinstances_listbyresourcegroup)
- [Update](#azurebaremetalinstances_update)

## Operations

- [List](#operations_list)
### AzureBareMetalInstances_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AzureBareMetalInstances GetByResourceGroup. */
public final class AzureBareMetalInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/stable/2021-08-09/examples/AzureBareMetalInstances_Get.json
     */
    /**
     * Sample code: Get an AzureBareMetal instance.
     *
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void getAnAzureBareMetalInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager
            .azureBareMetalInstances()
            .getByResourceGroupWithResponse("myResourceGroup", "myAzureBareMetalInstance", Context.NONE);
    }
}
```

### AzureBareMetalInstances_List

```java
import com.azure.core.util.Context;

/** Samples for AzureBareMetalInstances List. */
public final class AzureBareMetalInstancesListSamples {
    /*
     * x-ms-original-file: specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/stable/2021-08-09/examples/AzureBareMetalInstances_ListBySubscription.json
     */
    /**
     * Sample code: List all AzureBareMetal instances in a subscription.
     *
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void listAllAzureBareMetalInstancesInASubscription(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances().list(Context.NONE);
    }
}
```

### AzureBareMetalInstances_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AzureBareMetalInstances ListByResourceGroup. */
public final class AzureBareMetalInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/stable/2021-08-09/examples/AzureBareMetalInstances_ListByResourceGroup.json
     */
    /**
     * Sample code: List all AzureBareMetal instances in a resource group.
     *
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void listAllAzureBareMetalInstancesInAResourceGroup(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### AzureBareMetalInstances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.baremetalinfrastructure.models.Tags;
import java.util.HashMap;
import java.util.Map;

/** Samples for AzureBareMetalInstances Update. */
public final class AzureBareMetalInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/stable/2021-08-09/examples/AzureBareMetalInstances_PatchTags.json
     */
    /**
     * Sample code: Update Tags field of an AzureBareMetal instance.
     *
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void updateTagsFieldOfAnAzureBareMetalInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager
            .azureBareMetalInstances()
            .updateWithResponse(
                "myResourceGroup", "myABMInstance", new Tags().withTags(mapOf("testkey", "testvalue")), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/stable/2021-08-09/examples/AzureBareMetalInstances_PatchTags_Delete.json
     */
    /**
     * Sample code: Delete Tags field of an AzureBareMetal instance.
     *
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void deleteTagsFieldOfAnAzureBareMetalInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager
            .azureBareMetalInstances()
            .updateWithResponse("myResourceGroup", "myABMInstance", new Tags().withTags(mapOf()), Context.NONE);
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
     * x-ms-original-file: specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/stable/2021-08-09/examples/AzureBareMetalOperations_List.json
     */
    /**
     * Sample code: List all management operations supported by the AzureBareMetal RP.
     *
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void listAllManagementOperationsSupportedByTheAzureBareMetalRP(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

