# Code snippets and samples


## NetworkFunctions

- [CreateOrUpdate](#networkfunctions_createorupdate)
- [Delete](#networkfunctions_delete)
- [GetByResourceGroup](#networkfunctions_getbyresourcegroup)
- [List](#networkfunctions_list)
- [ListByResourceGroup](#networkfunctions_listbyresourcegroup)
- [UpdateTags](#networkfunctions_updatetags)

## Operations

- [List](#operations_list)
### NetworkFunctions_CreateOrUpdate

```java
import com.azure.resourcemanager.mpcnetworkfunction.models.NetworkFunctionAdministrativeState;
import com.azure.resourcemanager.mpcnetworkfunction.models.NetworkFunctionType;
import com.azure.resourcemanager.mpcnetworkfunction.models.SkuDefinitions;

/** Samples for NetworkFunctions CreateOrUpdate. */
public final class NetworkFunctionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mpcnetworkfunction/resource-manager/Microsoft.MobilePacketCore/preview/2023-05-15-preview/examples/NetworkFunctions_CreateOrUpdate.json
     */
    /**
     * Sample code: NetworkFunctions_CreateOrUpdate.
     *
     * @param manager Entry point to MpcnetworkfunctionManager.
     */
    public static void networkFunctionsCreateOrUpdate(
        com.azure.resourcemanager.mpcnetworkfunction.MpcnetworkfunctionManager manager) {
        manager
            .networkFunctions()
            .define("nf1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg1")
            .withSku(SkuDefinitions.NEXUS_PRODUCTION)
            .withNetworkFunctionType(NetworkFunctionType.SMF)
            .withNetworkFunctionAdministrativeState(NetworkFunctionAdministrativeState.COMMISSIONED)
            .withCapacity(100000)
            .withUserDescription("string")
            .withDeploymentNotes("string")
            .create();
    }
}
```

### NetworkFunctions_Delete

```java
/** Samples for NetworkFunctions Delete. */
public final class NetworkFunctionsDeleteSamples {
    /*
     * x-ms-original-file: specification/mpcnetworkfunction/resource-manager/Microsoft.MobilePacketCore/preview/2023-05-15-preview/examples/NetworkFunctions_Delete.json
     */
    /**
     * Sample code: NetworkFunctions_Delete.
     *
     * @param manager Entry point to MpcnetworkfunctionManager.
     */
    public static void networkFunctionsDelete(
        com.azure.resourcemanager.mpcnetworkfunction.MpcnetworkfunctionManager manager) {
        manager.networkFunctions().deleteByResourceGroupWithResponse("rg1", "nf1", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_GetByResourceGroup

```java
/** Samples for NetworkFunctions GetByResourceGroup. */
public final class NetworkFunctionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mpcnetworkfunction/resource-manager/Microsoft.MobilePacketCore/preview/2023-05-15-preview/examples/NetworkFunctions_Get.json
     */
    /**
     * Sample code: NetworkFunctions_Get.
     *
     * @param manager Entry point to MpcnetworkfunctionManager.
     */
    public static void networkFunctionsGet(
        com.azure.resourcemanager.mpcnetworkfunction.MpcnetworkfunctionManager manager) {
        manager.networkFunctions().getByResourceGroupWithResponse("rg1", "nf1", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_List

```java
/** Samples for NetworkFunctions List. */
public final class NetworkFunctionsListSamples {
    /*
     * x-ms-original-file: specification/mpcnetworkfunction/resource-manager/Microsoft.MobilePacketCore/preview/2023-05-15-preview/examples/NetworkFunctions_ListBySubscription.json
     */
    /**
     * Sample code: NetworkFunctions_ListBySubscription.
     *
     * @param manager Entry point to MpcnetworkfunctionManager.
     */
    public static void networkFunctionsListBySubscription(
        com.azure.resourcemanager.mpcnetworkfunction.MpcnetworkfunctionManager manager) {
        manager.networkFunctions().list(com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_ListByResourceGroup

```java
/** Samples for NetworkFunctions ListByResourceGroup. */
public final class NetworkFunctionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mpcnetworkfunction/resource-manager/Microsoft.MobilePacketCore/preview/2023-05-15-preview/examples/NetworkFunctions_ListByResourceGroup.json
     */
    /**
     * Sample code: NetworkFunctions_ListByResourceGroup.
     *
     * @param manager Entry point to MpcnetworkfunctionManager.
     */
    public static void networkFunctionsListByResourceGroup(
        com.azure.resourcemanager.mpcnetworkfunction.MpcnetworkfunctionManager manager) {
        manager.networkFunctions().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_UpdateTags

```java
import com.azure.resourcemanager.mpcnetworkfunction.models.NetworkFunctionResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for NetworkFunctions UpdateTags. */
public final class NetworkFunctionsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mpcnetworkfunction/resource-manager/Microsoft.MobilePacketCore/preview/2023-05-15-preview/examples/NetworkFunctions_UpdateTags.json
     */
    /**
     * Sample code: NetworkFunctions_UpdateTags.
     *
     * @param manager Entry point to MpcnetworkfunctionManager.
     */
    public static void networkFunctionsUpdateTags(
        com.azure.resourcemanager.mpcnetworkfunction.MpcnetworkfunctionManager manager) {
        NetworkFunctionResource resource =
            manager
                .networkFunctions()
                .getByResourceGroupWithResponse("rg1", "nf1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/mpcnetworkfunction/resource-manager/Microsoft.MobilePacketCore/preview/2023-05-15-preview/examples/OperationList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to MpcnetworkfunctionManager.
     */
    public static void operationsList(com.azure.resourcemanager.mpcnetworkfunction.MpcnetworkfunctionManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

