# Code snippets and samples


## NodeCustomizations

- [CreateOrUpdate](#nodecustomizations_createorupdate)
- [Delete](#nodecustomizations_delete)
- [DeleteVersion](#nodecustomizations_deleteversion)
- [GetByResourceGroup](#nodecustomizations_getbyresourcegroup)
- [GetVersion](#nodecustomizations_getversion)
- [List](#nodecustomizations_list)
- [ListByResourceGroup](#nodecustomizations_listbyresourcegroup)
- [ListVersions](#nodecustomizations_listversions)
- [Update](#nodecustomizations_update)

## Operations

- [List](#operations_list)
### NodeCustomizations_CreateOrUpdate

```java
import com.azure.resourcemanager.containerservicenodecustomization.models.ExecutionPoint;
import com.azure.resourcemanager.containerservicenodecustomization.models.NodeCustomizationProperties;
import com.azure.resourcemanager.containerservicenodecustomization.models.NodeCustomizationScript;
import com.azure.resourcemanager.containerservicenodecustomization.models.ScriptType;
import com.azure.resourcemanager.containerservicenodecustomization.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NodeCustomizations CreateOrUpdate.
 */
public final class NodeCustomizationsCreateOrUpdSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/NodeCustomizations_CreateOrUpdate.json
     */
    /**
     * Sample code: NodeCustomizations_CreateOrUpdate.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void nodeCustomizationsCreateOrUpdate(
        com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        manager.nodeCustomizations()
            .define("my-node-customization")
            .withRegion("westus2")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("team", "blue"))
            .withProperties(new NodeCustomizationProperties().withContainerImages(Arrays.asList("redis:8.0.0"))
                .withIdentityProfile(new UserAssignedIdentity())
                .withVersion("1.0.0")
                .withCustomizationScripts(Arrays.asList(new NodeCustomizationScript().withName("initialize-node")
                    .withExecutionPoint(ExecutionPoint.NODE_IMAGE_BUILD_TIME)
                    .withScriptType(ScriptType.BASH)
                    .withScript("echo \"test node customization\" > /var/log/test-node-customization.txt"))))
            .create();
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

### NodeCustomizations_Delete

```java
/**
 * Samples for NodeCustomizations Delete.
 */
public final class NodeCustomizationsDeleteSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/NodeCustomizations_Delete.json
     */
    /**
     * Sample code: NodeCustomizations_Delete.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void nodeCustomizationsDelete(
        com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        manager.nodeCustomizations().delete("rg1", "my-node-customization", null, com.azure.core.util.Context.NONE);
    }
}
```

### NodeCustomizations_DeleteVersion

```java
/**
 * Samples for NodeCustomizations DeleteVersion.
 */
public final class NodeCustomizationsDeleteVersiSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/NodeCustomizations_DeleteVersion.json
     */
    /**
     * Sample code: NodeCustomizations_DeleteVersion.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void nodeCustomizationsDeleteVersion(
        com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        manager.nodeCustomizations()
            .deleteVersion("rg1", "my-node-customization", "20250101-abcd1234", null, com.azure.core.util.Context.NONE);
    }
}
```

### NodeCustomizations_GetByResourceGroup

```java
/**
 * Samples for NodeCustomizations GetByResourceGroup.
 */
public final class NodeCustomizationsGetByResourSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/NodeCustomizations_Get.json
     */
    /**
     * Sample code: NodeCustomizations_Get.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void nodeCustomizationsGet(
        com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        manager.nodeCustomizations()
            .getByResourceGroupWithResponse("rg1", "my-node-customization", com.azure.core.util.Context.NONE);
    }
}
```

### NodeCustomizations_GetVersion

```java
/**
 * Samples for NodeCustomizations GetVersion.
 */
public final class NodeCustomizationsGetVersionSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/NodeCustomizations_GetVersion.json
     */
    /**
     * Sample code: NodeCustomizations_GetVersion.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void nodeCustomizationsGetVersion(
        com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        manager.nodeCustomizations()
            .getVersionWithResponse("rg1", "my-node-customization", "20250101-abcd1234",
                com.azure.core.util.Context.NONE);
    }
}
```

### NodeCustomizations_List

```java
/**
 * Samples for NodeCustomizations List.
 */
public final class NodeCustomizationsListSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/NodeCustomizations_ListBySubscription.json
     */
    /**
     * Sample code: NodeCustomizations_ListBySubscription.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void nodeCustomizationsListBySubscription(
        com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        manager.nodeCustomizations().list(com.azure.core.util.Context.NONE);
    }
}
```

### NodeCustomizations_ListByResourceGroup

```java
/**
 * Samples for NodeCustomizations ListByResourceGroup.
 */
public final class NodeCustomizationsListByResouSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/NodeCustomizations_ListByResourceGroup.json
     */
    /**
     * Sample code: NodeCustomizations_ListByResourceGroup.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void nodeCustomizationsListByResourceGroup(
        com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        manager.nodeCustomizations().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### NodeCustomizations_ListVersions

```java
/**
 * Samples for NodeCustomizations ListVersions.
 */
public final class NodeCustomizationsListVersionSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/NodeCustomizations_ListVersions.json
     */
    /**
     * Sample code: NodeCustomizations_ListVersions.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void nodeCustomizationsListVersions(
        com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        manager.nodeCustomizations().listVersions("rg1", "my-node-customization", com.azure.core.util.Context.NONE);
    }
}
```

### NodeCustomizations_Update

```java
import com.azure.resourcemanager.containerservicenodecustomization.models.NodeCustomization;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NodeCustomizations Update.
 */
public final class NodeCustomizationsUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/NodeCustomizations_Update.json
     */
    /**
     * Sample code: NodeCustomizations_Update.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void nodeCustomizationsUpdate(
        com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        NodeCustomization resource = manager.nodeCustomizations()
            .getByResourceGroupWithResponse("rg1", "my-node-customization", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key5558", "fakeTokenPlaceholder")).apply();
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
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-09-02-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to NodeCustomizationManager.
     */
    public static void
        operationsList(com.azure.resourcemanager.containerservicenodecustomization.NodeCustomizationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

