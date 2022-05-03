# Code snippets and samples


## FluidRelayOperations

- [List](#fluidrelayoperations_list)

## FluidRelayServers

- [CreateOrUpdate](#fluidrelayservers_createorupdate)
- [Delete](#fluidrelayservers_delete)
- [GetByResourceGroup](#fluidrelayservers_getbyresourcegroup)
- [GetKeys](#fluidrelayservers_getkeys)
- [List](#fluidrelayservers_list)
- [ListByResourceGroup](#fluidrelayservers_listbyresourcegroup)
- [RegenerateKey](#fluidrelayservers_regeneratekey)
- [Update](#fluidrelayservers_update)
### FluidRelayOperations_List

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayOperations List. */
public final class FluidRelayOperationsListSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/preview/2021-06-15-preview/examples/FluidRelayServerOperations.json
     */
    /**
     * Sample code: List Fluid Relay server operations.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void listFluidRelayServerOperations(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayOperations().list(Context.NONE);
    }
}
```

### FluidRelayServers_CreateOrUpdate

```java
import com.azure.resourcemanager.fluidrelay.models.Identity;
import com.azure.resourcemanager.fluidrelay.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for FluidRelayServers CreateOrUpdate. */
public final class FluidRelayServersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/preview/2021-06-15-preview/examples/FluidRelayServers_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a Fluid Relay server.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void createAFluidRelayServer(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager
            .fluidRelayServers()
            .define("myFluidRelayServer")
            .withRegion("west-us")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Category", "sales"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
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

### FluidRelayServers_Delete

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayServers Delete. */
public final class FluidRelayServersDeleteSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/preview/2021-06-15-preview/examples/FluidRelayServers_Delete.json
     */
    /**
     * Sample code: Delete a Fluid Relay server.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void deleteAFluidRelayServer(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayServers().deleteWithResponse("myResourceGroup", "myFluidRelayServer", Context.NONE);
    }
}
```

### FluidRelayServers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayServers GetByResourceGroup. */
public final class FluidRelayServersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/preview/2021-06-15-preview/examples/FluidRelayServers_Get.json
     */
    /**
     * Sample code: Get Fluid Relay server details.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void getFluidRelayServerDetails(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager
            .fluidRelayServers()
            .getByResourceGroupWithResponse("myResourceGroup", "myFluidRelayServer", Context.NONE);
    }
}
```

### FluidRelayServers_GetKeys

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayServers GetKeys. */
public final class FluidRelayServersGetKeysSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/preview/2021-06-15-preview/examples/FluidRelayServers_GetKeys.json
     */
    /**
     * Sample code: Get keys for a Fluid Relay server.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void getKeysForAFluidRelayServer(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayServers().getKeysWithResponse("myResourceGroup", "myFluidRelayServer", Context.NONE);
    }
}
```

### FluidRelayServers_List

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayServers List. */
public final class FluidRelayServersListSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/preview/2021-06-15-preview/examples/FluidRelayServers_ListBySubscription.json
     */
    /**
     * Sample code: List all Fluid Relay servers in a subscription.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void listAllFluidRelayServersInASubscription(
        com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayServers().list(Context.NONE);
    }
}
```

### FluidRelayServers_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayServers ListByResourceGroup. */
public final class FluidRelayServersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/preview/2021-06-15-preview/examples/FluidRelayServers_ListByResourceGroup.json
     */
    /**
     * Sample code: List all Fluid Relay servers in a resource group.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void listAllFluidRelayServersInAResourceGroup(
        com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayServers().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### FluidRelayServers_RegenerateKey

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.fluidrelay.models.KeyName;
import com.azure.resourcemanager.fluidrelay.models.RegenerateKeyRequest;

/** Samples for FluidRelayServers RegenerateKey. */
public final class FluidRelayServersRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/preview/2021-06-15-preview/examples/FluidRelayServers_RegenerateKeys.json
     */
    /**
     * Sample code: Regenerate keys for a Fluid Relay server.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void regenerateKeysForAFluidRelayServer(
        com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager
            .fluidRelayServers()
            .regenerateKeyWithResponse(
                "myResourceGroup",
                "myFluidRelayServer",
                new RegenerateKeyRequest().withKeyName(KeyName.KEY1),
                Context.NONE);
    }
}
```

### FluidRelayServers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.fluidrelay.models.FluidRelayServer;
import java.util.HashMap;
import java.util.Map;

/** Samples for FluidRelayServers Update. */
public final class FluidRelayServersUpdateSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/preview/2021-06-15-preview/examples/FluidRelayServers_Update.json
     */
    /**
     * Sample code: Create a Fluid Relay server.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void createAFluidRelayServer(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        FluidRelayServer resource =
            manager
                .fluidRelayServers()
                .getByResourceGroupWithResponse("myResourceGroup", "myFluidRelayServer", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("Category", "sales")).apply();
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

