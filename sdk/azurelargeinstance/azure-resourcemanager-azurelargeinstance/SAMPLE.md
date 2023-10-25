# Code snippets and samples


## AzureLargeInstance

- [GetByResourceGroup](#azurelargeinstance_getbyresourcegroup)
- [List](#azurelargeinstance_list)
- [ListByResourceGroup](#azurelargeinstance_listbyresourcegroup)
- [Restart](#azurelargeinstance_restart)
- [Shutdown](#azurelargeinstance_shutdown)
- [Start](#azurelargeinstance_start)
- [Update](#azurelargeinstance_update)

## AzureLargeStorageInstance

- [GetByResourceGroup](#azurelargestorageinstance_getbyresourcegroup)
- [List](#azurelargestorageinstance_list)
- [ListByResourceGroup](#azurelargestorageinstance_listbyresourcegroup)
- [Update](#azurelargestorageinstance_update)

## Operations

- [List](#operations_list)
### AzureLargeInstance_GetByResourceGroup

```java
/** Samples for AzureLargeInstance GetByResourceGroup. */
public final class AzureLargeInstanceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeInstance_Get.json
     */
    /**
     * Sample code: Get an Azure Large Instance.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void getAnAzureLargeInstance(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager
            .azureLargeInstances()
            .getByResourceGroupWithResponse(
                "myResourceGroup", "myAzureLargeInstance", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_List

```java
/** Samples for AzureLargeInstance List. */
public final class AzureLargeInstanceListSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeInstance_ListBySubscription.json
     */
    /**
     * Sample code: List all Azure Large Instances in a subscription.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void listAllAzureLargeInstancesInASubscription(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager.azureLargeInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_ListByResourceGroup

```java
/** Samples for AzureLargeInstance ListByResourceGroup. */
public final class AzureLargeInstanceListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeInstance_ListByResourceGroup.json
     */
    /**
     * Sample code: List all Azure Large Instances in a resource group.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void listAllAzureLargeInstancesInAResourceGroup(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager.azureLargeInstances().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_Restart

```java
/** Samples for AzureLargeInstance Restart. */
public final class AzureLargeInstanceRestartSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeInstance_Restart.json
     */
    /**
     * Sample code: Restart an Azure Large Instance.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void restartAnAzureLargeInstance(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager
            .azureLargeInstances()
            .restart("myResourceGroup", "myALInstance", null, com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_Shutdown

```java
/** Samples for AzureLargeInstance Shutdown. */
public final class AzureLargeInstanceShutdownSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeInstance_Shutdown.json
     */
    /**
     * Sample code: Shutdown an AzureLarge instance.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void shutdownAnAzureLargeInstance(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager.azureLargeInstances().shutdown("myResourceGroup", "myALInstance", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_Start

```java
/** Samples for AzureLargeInstance Start. */
public final class AzureLargeInstanceStartSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeInstance_Start.json
     */
    /**
     * Sample code: Start an Azure Large Instance.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void startAnAzureLargeInstance(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager.azureLargeInstances().start("myResourceGroup", "myALInstance", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_Update

```java
import com.azure.resourcemanager.azurelargeinstance.models.Tags;
import java.util.HashMap;
import java.util.Map;

/** Samples for AzureLargeInstance Update. */
public final class AzureLargeInstanceUpdateSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeInstance_PatchTags.json
     */
    /**
     * Sample code: Update Tags field of an Azure Large Instance.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void updateTagsFieldOfAnAzureLargeInstance(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager
            .azureLargeInstances()
            .updateWithResponse(
                "myResourceGroup",
                "myALInstance",
                new Tags().withTags(mapOf("testkey", "fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeInstance_PatchTags_Delete.json
     */
    /**
     * Sample code: Delete Tags field of an Azure Large Instance.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void deleteTagsFieldOfAnAzureLargeInstance(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager
            .azureLargeInstances()
            .updateWithResponse(
                "myResourceGroup", "myALInstance", new Tags().withTags(mapOf()), com.azure.core.util.Context.NONE);
    }

    // Use "Map.of" if available
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

### AzureLargeStorageInstance_GetByResourceGroup

```java
/** Samples for AzureLargeStorageInstance GetByResourceGroup. */
public final class AzureLargeStorageInstanceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeStorageInstance_Get.json
     */
    /**
     * Sample code: Get an AzureLargeStorageStorage instance.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void getAnAzureLargeStorageStorageInstance(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager
            .azureLargeStorageInstances()
            .getByResourceGroupWithResponse(
                "myResourceGroup", "myAzureLargeStorageInstance", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeStorageInstance_List

```java
/** Samples for AzureLargeStorageInstance List. */
public final class AzureLargeStorageInstanceListSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeStorageInstance_ListBySubscription.json
     */
    /**
     * Sample code: List all AzureLargeStorageInstances in a subscription.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void listAllAzureLargeStorageInstancesInASubscription(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager.azureLargeStorageInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeStorageInstance_ListByResourceGroup

```java
/** Samples for AzureLargeStorageInstance ListByResourceGroup. */
public final class AzureLargeStorageInstanceListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeStorageInstance_ListByResourceGroup.json
     */
    /**
     * Sample code: List all AzureLargeStorageInstances in a resource group.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void listAllAzureLargeStorageInstancesInAResourceGroup(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager.azureLargeStorageInstances().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeStorageInstance_Update

```java
import com.azure.resourcemanager.azurelargeinstance.models.Tags;
import java.util.HashMap;
import java.util.Map;

/** Samples for AzureLargeStorageInstance Update. */
public final class AzureLargeStorageInstanceUpdateSamples {
    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeStorageInstance_PatchTags.json
     */
    /**
     * Sample code: Update Tags field of an AzureLargeStorageStorage instance.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void updateTagsFieldOfAnAzureLargeStorageStorageInstance(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager
            .azureLargeStorageInstances()
            .updateWithResponse(
                "myResourceGroup",
                "myALSInstance",
                new Tags().withTags(mapOf("testkey", "fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeStorageInstance_PatchTags_Delete.json
     */
    /**
     * Sample code: Delete Tags field of an AzureLargeStorageStorage instance.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void deleteTagsFieldOfAnAzureLargeStorageStorageInstance(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager
            .azureLargeStorageInstances()
            .updateWithResponse(
                "myResourceGroup", "myALSInstance", new Tags().withTags(mapOf()), com.azure.core.util.Context.NONE);
    }

    // Use "Map.of" if available
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
     * x-ms-original-file: specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/examples/AzureLargeInstanceOperations_List.json
     */
    /**
     * Sample code: List all management operations supported by the AzureLargeInstance RP.
     *
     * @param manager Entry point to AzureLargeInstanceManager.
     */
    public static void listAllManagementOperationsSupportedByTheAzureLargeInstanceRP(
        com.azure.resourcemanager.azurelargeinstance.AzureLargeInstanceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

