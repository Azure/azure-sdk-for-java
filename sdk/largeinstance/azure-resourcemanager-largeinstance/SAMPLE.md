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
/**
 * Samples for AzureLargeInstance GetByResourceGroup.
 */
public final class AzureLargeInstanceGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeInstance_Get.json
     */
    /**
     * Sample code: AzureLargeInstance_Get.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void azureLargeInstanceGet(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeInstances().getByResourceGroupWithResponse("myResourceGroup", "myAzureLargeInstance",
            com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_List

```java
/**
 * Samples for AzureLargeInstance List.
 */
public final class AzureLargeInstanceListSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeInstance_ListBySubscription.json
     */
    /**
     * Sample code: AzureLargeInstance_ListBySubscription.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void
        azureLargeInstanceListBySubscription(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_ListByResourceGroup

```java
/**
 * Samples for AzureLargeInstance ListByResourceGroup.
 */
public final class AzureLargeInstanceListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeInstance_ListByResourceGroup.json
     */
    /**
     * Sample code: AzureLargeInstance_ListByResourceGroup.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void
        azureLargeInstanceListByResourceGroup(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeInstances().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_Restart

```java

/**
 * Samples for AzureLargeInstance Restart.
 */
public final class AzureLargeInstanceRestartSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeInstance_Restart.json
     */
    /**
     * Sample code: AzureLargeInstance_Restart.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void azureLargeInstanceRestart(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeInstances().restart("myResourceGroup", "myALInstance", null,
            com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_Shutdown

```java
/**
 * Samples for AzureLargeInstance Shutdown.
 */
public final class AzureLargeInstanceShutdownSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeInstance_Shutdown.json
     */
    /**
     * Sample code: AzureLargeInstance_Shutdown.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void
        azureLargeInstanceShutdown(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeInstances().shutdown("myResourceGroup", "myALInstance", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_Start

```java
/**
 * Samples for AzureLargeInstance Start.
 */
public final class AzureLargeInstanceStartSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeInstance_Start.json
     */
    /**
     * Sample code: AzureLargeInstance_Start.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void azureLargeInstanceStart(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeInstances().start("myResourceGroup", "myALInstance", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeInstance_Update

```java

/**
 * Samples for AzureLargeInstance Update.
 */
public final class AzureLargeInstanceUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeInstance_PatchTags.json
     */
    /**
     * Sample code: AzureLargeInstance_Update_Tag.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void
        azureLargeInstanceUpdateTag(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeInstances().updateWithResponse("myResourceGroup", "myALInstance", null,
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeInstance_PatchTags_Delete.json
     */
    /**
     * Sample code: AzureLargeInstance_Delete_Tag.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void
        azureLargeInstanceDeleteTag(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeInstances().updateWithResponse("myResourceGroup", "myALInstance", null,
            com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeStorageInstance_GetByResourceGroup

```java
/**
 * Samples for AzureLargeStorageInstance GetByResourceGroup.
 */
public final class AzureLargeStorageInstanceGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeStorageInstance_Get.json
     */
    /**
     * Sample code: AzureLargeStorageInstance_Get.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void
        azureLargeStorageInstanceGet(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeStorageInstances().getByResourceGroupWithResponse("myResourceGroup",
            "myAzureLargeStorageInstance", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeStorageInstance_List

```java
/**
 * Samples for AzureLargeStorageInstance List.
 */
public final class AzureLargeStorageInstanceListSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeStorageInstance_ListBySubscription.json
     */
    /**
     * Sample code: AzureLargeStorageInstance_ListBySubscription.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void azureLargeStorageInstanceListBySubscription(
        com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeStorageInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeStorageInstance_ListByResourceGroup

```java
/**
 * Samples for AzureLargeStorageInstance ListByResourceGroup.
 */
public final class AzureLargeStorageInstanceListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeStorageInstance_ListByResourceGroup.json
     */
    /**
     * Sample code: AzureLargeStorageInstance_ListByResourceGroup.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void azureLargeStorageInstanceListByResourceGroup(
        com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeStorageInstances().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### AzureLargeStorageInstance_Update

```java

/**
 * Samples for AzureLargeStorageInstance Update.
 */
public final class AzureLargeStorageInstanceUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeStorageInstance_PatchTags_Delete.json
     */
    /**
     * Sample code: AzureLargeStorageInstance_Delete_Tag.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void
        azureLargeStorageInstanceDeleteTag(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeStorageInstances().updateWithResponse("myResourceGroup", "myALSInstance", null,
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeStorageInstance_PatchTags.json
     */
    /**
     * Sample code: AzureLargeStorageInstance_Update_Tag.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void
        azureLargeStorageInstanceUpdateTag(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.azureLargeStorageInstances().updateWithResponse("myResourceGroup", "myALSInstance", null,
            com.azure.core.util.Context.NONE);
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
     * specification/azurelargeinstance/resource-manager/Microsoft.AzureLargeInstance/preview/2023-07-20-preview/
     * examples/AzureLargeInstanceOperations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to LargeInstanceManager.
     */
    public static void operationsList(com.azure.resourcemanager.largeinstance.LargeInstanceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

