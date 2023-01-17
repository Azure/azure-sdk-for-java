# Code snippets and samples


## HanaInstances

- [Create](#hanainstances_create)
- [Delete](#hanainstances_delete)
- [GetByResourceGroup](#hanainstances_getbyresourcegroup)
- [List](#hanainstances_list)
- [ListByResourceGroup](#hanainstances_listbyresourcegroup)
- [Restart](#hanainstances_restart)
- [Shutdown](#hanainstances_shutdown)
- [Start](#hanainstances_start)
- [Update](#hanainstances_update)

## Operations

- [List](#operations_list)
### HanaInstances_Create

```java
import com.azure.resourcemanager.hanaonazure.models.IpAddress;
import com.azure.resourcemanager.hanaonazure.models.NetworkProfile;
import com.azure.resourcemanager.hanaonazure.models.OSProfile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for HanaInstances Create. */
public final class HanaInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_Create.json
     */
    /**
     * Sample code: Create a HANA instance.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void createAHANAInstance(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager
            .hanaInstances()
            .define("myHanaInstance")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key", "value"))
            .withOsProfile(new OSProfile().withComputerName("myComputerName").withSshPublicKey("fakeTokenPlaceholder"))
            .withNetworkProfile(
                new NetworkProfile()
                    .withNetworkInterfaces(Arrays.asList(new IpAddress().withIpAddress("100.100.100.100"))))
            .withPartnerNodeId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.HanaOnAzure/hanaInstances/myHanaInstance2")
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

### HanaInstances_Delete

```java
/** Samples for HanaInstances Delete. */
public final class HanaInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_Delete.json
     */
    /**
     * Sample code: Delete a HANA instance.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void deleteAHANAInstance(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.hanaInstances().delete("myResourceGroup", "myHanaInstance", com.azure.core.util.Context.NONE);
    }
}
```

### HanaInstances_GetByResourceGroup

```java
/** Samples for HanaInstances GetByResourceGroup. */
public final class HanaInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_Get.json
     */
    /**
     * Sample code: Get properties of a HANA instance.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void getPropertiesOfAHANAInstance(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager
            .hanaInstances()
            .getByResourceGroupWithResponse("myResourceGroup", "myHanaInstance", com.azure.core.util.Context.NONE);
    }
}
```

### HanaInstances_List

```java
/** Samples for HanaInstances List. */
public final class HanaInstancesListSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_List.json
     */
    /**
     * Sample code: List all HANA instances in a subscription.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void listAllHANAInstancesInASubscription(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.hanaInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### HanaInstances_ListByResourceGroup

```java
/** Samples for HanaInstances ListByResourceGroup. */
public final class HanaInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_ListByResourceGroup.json
     */
    /**
     * Sample code: List all HANA instances in a resource group.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void listAllHANAInstancesInAResourceGroup(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.hanaInstances().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### HanaInstances_Restart

```java
/** Samples for HanaInstances Restart. */
public final class HanaInstancesRestartSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_Restart.json
     */
    /**
     * Sample code: Restart a HANA instance.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void restartAHANAInstance(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.hanaInstances().restart("myResourceGroup", "myHanaInstance", com.azure.core.util.Context.NONE);
    }
}
```

### HanaInstances_Shutdown

```java
/** Samples for HanaInstances Shutdown. */
public final class HanaInstancesShutdownSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_Shutdown.json
     */
    /**
     * Sample code: Shutdown a HANA instance.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void shutdownAHANAInstance(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.hanaInstances().shutdown("myResourceGroup", "myHanaInstance", com.azure.core.util.Context.NONE);
    }
}
```

### HanaInstances_Start

```java
/** Samples for HanaInstances Start. */
public final class HanaInstancesStartSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_Start.json
     */
    /**
     * Sample code: Start a HANA instance.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void startAHANAInstance(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.hanaInstances().start("myResourceGroup", "myHanaInstance", com.azure.core.util.Context.NONE);
    }
}
```

### HanaInstances_Update

```java
import com.azure.resourcemanager.hanaonazure.models.HanaInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for HanaInstances Update. */
public final class HanaInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_PatchTags_Delete.json
     */
    /**
     * Sample code: Delete Tags field of a HANA instance.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void deleteTagsFieldOfAHANAInstance(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        HanaInstance resource =
            manager
                .hanaInstances()
                .getByResourceGroupWithResponse("myResourceGroup", "myHanaInstance", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf()).apply();
    }

    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaInstances_PatchTags.json
     */
    /**
     * Sample code: Update Tags field of a HANA instance.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void updateTagsFieldOfAHANAInstance(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        HanaInstance resource =
            manager
                .hanaInstances()
                .getByResourceGroupWithResponse("myResourceGroup", "myHanaInstance", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("testkey", "testvalue")).apply();
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
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2017-11-03-preview/examples/HanaOperations_List.json
     */
    /**
     * Sample code: List all HANA management operations supported by HANA RP.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void listAllHANAManagementOperationsSupportedByHANARP(
        com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

