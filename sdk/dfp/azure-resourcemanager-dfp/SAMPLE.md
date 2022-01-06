# Code snippets and samples


## Instances

- [CheckNameAvailability](#instances_checknameavailability)
- [Create](#instances_create)
- [Delete](#instances_delete)
- [GetByResourceGroup](#instances_getbyresourcegroup)
- [List](#instances_list)
- [ListByResourceGroup](#instances_listbyresourcegroup)
- [Update](#instances_update)

## ResourceProvider

- [ListOperations](#resourceprovider_listoperations)
### Instances_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dfp.models.CheckInstanceNameAvailabilityParameters;

/** Samples for Instances CheckNameAvailability. */
public final class InstancesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/dfp/resource-manager/Microsoft.Dynamics365Fraudprotection/preview/2021-02-01-preview/examples/checkNameAvailability.json
     */
    /**
     * Sample code: Check name availability of an instance.
     *
     * @param manager Entry point to DfpManager.
     */
    public static void checkNameAvailabilityOfAnInstance(com.azure.resourcemanager.dfp.DfpManager manager) {
        manager
            .instances()
            .checkNameAvailabilityWithResponse(
                "West US",
                new CheckInstanceNameAvailabilityParameters()
                    .withName("azsdktest")
                    .withType("Microsoft.Dynamics365Fraudprotection/instances"),
                Context.NONE);
    }
}
```

### Instances_Create

```java
import com.azure.resourcemanager.dfp.models.DfpInstanceAdministrators;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Instances Create. */
public final class InstancesCreateSamples {
    /*
     * x-ms-original-file: specification/dfp/resource-manager/Microsoft.Dynamics365Fraudprotection/preview/2021-02-01-preview/examples/createInstance.json
     */
    /**
     * Sample code: Create instance.
     *
     * @param manager Entry point to DfpManager.
     */
    public static void createInstance(com.azure.resourcemanager.dfp.DfpManager manager) {
        manager
            .instances()
            .define("azsdktest")
            .withRegion("West US")
            .withExistingResourceGroup("TestRG")
            .withTags(mapOf("testKey", "testValue"))
            .withAdministration(
                new DfpInstanceAdministrators()
                    .withMembers(Arrays.asList("azsdktest@microsoft.com", "azsdktest2@microsoft.com")))
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

### Instances_Delete

```java
import com.azure.core.util.Context;

/** Samples for Instances Delete. */
public final class InstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/dfp/resource-manager/Microsoft.Dynamics365Fraudprotection/preview/2021-02-01-preview/examples/deleteInstance.json
     */
    /**
     * Sample code: Get details of an instance.
     *
     * @param manager Entry point to DfpManager.
     */
    public static void getDetailsOfAnInstance(com.azure.resourcemanager.dfp.DfpManager manager) {
        manager.instances().delete("TestRG", "azsdktest", Context.NONE);
    }
}
```

### Instances_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Instances GetByResourceGroup. */
public final class InstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dfp/resource-manager/Microsoft.Dynamics365Fraudprotection/preview/2021-02-01-preview/examples/getInstance.json
     */
    /**
     * Sample code: Get details of an instance.
     *
     * @param manager Entry point to DfpManager.
     */
    public static void getDetailsOfAnInstance(com.azure.resourcemanager.dfp.DfpManager manager) {
        manager.instances().getByResourceGroupWithResponse("TestRG", "azsdktest", Context.NONE);
    }
}
```

### Instances_List

```java
import com.azure.core.util.Context;

/** Samples for Instances List. */
public final class InstancesListSamples {
    /*
     * x-ms-original-file: specification/dfp/resource-manager/Microsoft.Dynamics365Fraudprotection/preview/2021-02-01-preview/examples/listInstancesInSubscription.json
     */
    /**
     * Sample code: Get details of a instance.
     *
     * @param manager Entry point to DfpManager.
     */
    public static void getDetailsOfAInstance(com.azure.resourcemanager.dfp.DfpManager manager) {
        manager.instances().list(Context.NONE);
    }
}
```

### Instances_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Instances ListByResourceGroup. */
public final class InstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dfp/resource-manager/Microsoft.Dynamics365Fraudprotection/preview/2021-02-01-preview/examples/listInstancesInResourceGroup.json
     */
    /**
     * Sample code: List DFP instances in resource group.
     *
     * @param manager Entry point to DfpManager.
     */
    public static void listDFPInstancesInResourceGroup(com.azure.resourcemanager.dfp.DfpManager manager) {
        manager.instances().listByResourceGroup("TestRG", Context.NONE);
    }
}
```

### Instances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dfp.models.DfpInstance;
import com.azure.resourcemanager.dfp.models.DfpInstanceAdministrators;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Instances Update. */
public final class InstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/dfp/resource-manager/Microsoft.Dynamics365Fraudprotection/preview/2021-02-01-preview/examples/updateInstance.json
     */
    /**
     * Sample code: Update instance parameters.
     *
     * @param manager Entry point to DfpManager.
     */
    public static void updateInstanceParameters(com.azure.resourcemanager.dfp.DfpManager manager) {
        DfpInstance resource =
            manager.instances().getByResourceGroupWithResponse("TestRG", "azsdktest", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("testKey", "testValue"))
            .withAdministration(
                new DfpInstanceAdministrators()
                    .withMembers(Arrays.asList("azsdktest@microsoft.com", "azsdktest2@microsoft.com")))
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

### ResourceProvider_ListOperations

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ListOperations. */
public final class ResourceProviderListOperationsSamples {
    /*
     * x-ms-original-file: specification/dfp/resource-manager/Microsoft.Dynamics365Fraudprotection/preview/2021-02-01-preview/examples/ListOperations.json
     */
    /**
     * Sample code: Get a list of operations supported by Microsoft.Dynamics365FraudProtection resource provider.
     *
     * @param manager Entry point to DfpManager.
     */
    public static void getAListOfOperationsSupportedByMicrosoftDynamics365FraudProtectionResourceProvider(
        com.azure.resourcemanager.dfp.DfpManager manager) {
        manager.resourceProviders().listOperations(Context.NONE);
    }
}
```

