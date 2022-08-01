# Code snippets and samples


## FluidRelayContainers

- [Delete](#fluidrelaycontainers_delete)
- [Get](#fluidrelaycontainers_get)
- [ListByFluidRelayServers](#fluidrelaycontainers_listbyfluidrelayservers)

## FluidRelayOperations

- [List](#fluidrelayoperations_list)

## FluidRelayServers

- [CreateOrUpdate](#fluidrelayservers_createorupdate)
- [Delete](#fluidrelayservers_delete)
- [GetByResourceGroup](#fluidrelayservers_getbyresourcegroup)
- [List](#fluidrelayservers_list)
- [ListByResourceGroup](#fluidrelayservers_listbyresourcegroup)
- [ListKeys](#fluidrelayservers_listkeys)
- [RegenerateKey](#fluidrelayservers_regeneratekey)
- [Update](#fluidrelayservers_update)
### FluidRelayContainers_Delete

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayContainers Delete. */
public final class FluidRelayContainersDeleteSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayContainers_Delete.json
     */
    /**
     * Sample code: Delete a Fluid Relay container.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void deleteAFluidRelayContainer(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager
            .fluidRelayContainers()
            .deleteWithResponse("myResourceGroup", "myFluidRelayServer", "myFluidRelayContainer", Context.NONE);
    }
}
```

### FluidRelayContainers_Get

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayContainers Get. */
public final class FluidRelayContainersGetSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayContainers_Get.json
     */
    /**
     * Sample code: Get Fluid Relay container details.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void getFluidRelayContainerDetails(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager
            .fluidRelayContainers()
            .getWithResponse("myResourceGroup", "myFluidRelayServer", "myFluidRelayContainer", Context.NONE);
    }
}
```

### FluidRelayContainers_ListByFluidRelayServers

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayContainers ListByFluidRelayServers. */
public final class FluidRelayContainersListByFluidRelayServersSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayContainers_ListByFluidRelayServer.json
     */
    /**
     * Sample code: List all Fluid Relay containers in a Fluid Relay server.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void listAllFluidRelayContainersInAFluidRelayServer(
        com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayContainers().listByFluidRelayServers("myResourceGroup", "myFluidRelayServer", Context.NONE);
    }
}
```

### FluidRelayOperations_List

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayOperations List. */
public final class FluidRelayOperationsListSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServerOperations.json
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
import com.azure.resourcemanager.fluidrelay.models.CmkIdentityType;
import com.azure.resourcemanager.fluidrelay.models.CustomerManagedKeyEncryptionProperties;
import com.azure.resourcemanager.fluidrelay.models.CustomerManagedKeyEncryptionPropertiesKeyEncryptionKeyIdentity;
import com.azure.resourcemanager.fluidrelay.models.EncryptionProperties;
import com.azure.resourcemanager.fluidrelay.models.Identity;
import com.azure.resourcemanager.fluidrelay.models.ResourceIdentityType;
import com.azure.resourcemanager.fluidrelay.models.StorageSku;
import com.azure.resourcemanager.fluidrelay.models.UserAssignedIdentitiesValue;
import java.util.HashMap;
import java.util.Map;

/** Samples for FluidRelayServers CreateOrUpdate. */
public final class FluidRelayServersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_CreateOrUpdate.json
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
            .withStoragesku(StorageSku.BASIC)
            .create();
    }

    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_CreateWithAmi.json
     */
    /**
     * Sample code: Create a Fluid Relay server with AMI.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void createAFluidRelayServerWithAMI(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager
            .fluidRelayServers()
            .define("myFluidRelayServer")
            .withRegion("west-us")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Category", "sales"))
            .withIdentity(
                new Identity()
                    .withType(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxxx-xxxx-xxxx-xxxx/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                            new UserAssignedIdentitiesValue(),
                            "/subscriptions/xxxx-xxxx-xxxx-xxxx/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                            new UserAssignedIdentitiesValue())))
            .withStoragesku(StorageSku.BASIC)
            .create();
    }

    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_CreateWithCmk.json
     */
    /**
     * Sample code: Create a Fluid Relay server with CMK.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void createAFluidRelayServerWithCMK(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager
            .fluidRelayServers()
            .define("myFluidRelayServer")
            .withRegion("west-us")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Category", "sales"))
            .withIdentity(
                new Identity()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxxx-xxxx-xxxx-xxxx/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityForCMK",
                            new UserAssignedIdentitiesValue())))
            .withEncryption(
                new EncryptionProperties()
                    .withCustomerManagedKeyEncryption(
                        new CustomerManagedKeyEncryptionProperties()
                            .withKeyEncryptionKeyIdentity(
                                new CustomerManagedKeyEncryptionPropertiesKeyEncryptionKeyIdentity()
                                    .withIdentityType(CmkIdentityType.USER_ASSIGNED)
                                    .withUserAssignedIdentityResourceId(
                                        "/subscriptions/xxxx-xxxx-xxxx-xxxx/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityForCMK"))
                            .withKeyEncryptionKeyUrl("https://contosovault.vault.azure.net/keys/contosokek")))
            .withStoragesku(StorageSku.BASIC)
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
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_Delete.json
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
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_Get.json
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

### FluidRelayServers_List

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayServers List. */
public final class FluidRelayServersListSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_ListBySubscription.json
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
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_ListByResourceGroup.json
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

### FluidRelayServers_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for FluidRelayServers ListKeys. */
public final class FluidRelayServersListKeysSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_ListKeys.json
     */
    /**
     * Sample code: Get keys for a Fluid Relay server.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void getKeysForAFluidRelayServer(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayServers().listKeysWithResponse("myResourceGroup", "myFluidRelayServer", Context.NONE);
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
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_RegenerateKeys.json
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
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/FluidRelayServers_Update.json
     */
    /**
     * Sample code: Update a Fluid Relay server.
     *
     * @param manager Entry point to FluidRelayManager.
     */
    public static void updateAFluidRelayServer(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
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

