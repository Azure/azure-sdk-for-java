# Code snippets and samples


## Operations

- [List](#operations_list)

## PreparedImageSpecifications

- [CreateOrUpdate](#preparedimagespecifications_createorupdate)
- [Delete](#preparedimagespecifications_delete)
- [DeleteVersion](#preparedimagespecifications_deleteversion)
- [GetByResourceGroup](#preparedimagespecifications_getbyresourcegroup)
- [GetVersion](#preparedimagespecifications_getversion)
- [List](#preparedimagespecifications_list)
- [ListByResourceGroup](#preparedimagespecifications_listbyresourcegroup)
- [ListVersions](#preparedimagespecifications_listversions)
- [Update](#preparedimagespecifications_update)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void operationsList(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PreparedImageSpecifications_CreateOrUpdate

```java
import com.azure.resourcemanager.containerservicepreparedimgspec.models.ExecutionPoint;
import com.azure.resourcemanager.containerservicepreparedimgspec.models.PreparedImageSpecificationManagedIdentityProfile;
import com.azure.resourcemanager.containerservicepreparedimgspec.models.PreparedImageSpecificationProperties;
import com.azure.resourcemanager.containerservicepreparedimgspec.models.PreparedImageSpecificationScript;
import com.azure.resourcemanager.containerservicepreparedimgspec.models.ScriptType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PreparedImageSpecifications CreateOrUpdate.
 */
public final class PreparedImageSpecificationsCreateOrSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/PreparedImageSpecifications_CreateOrUpdate.json
     */
    /**
     * Sample code: PreparedImageSpecifications_CreateOrUpdate.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void preparedImageSpecificationsCreateOrUpdate(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        manager.preparedImageSpecifications()
            .define("my-prepared-image-specification")
            .withRegion("westus2")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("team", "blue"))
            .withProperties(new PreparedImageSpecificationProperties().withContainerImages(Arrays.asList("redis:8.0.0"))
                .withIdentityProfile(new PreparedImageSpecificationManagedIdentityProfile().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity1"))
                .withVersion("20250101-abcd1234")
                .withCustomizationScripts(Arrays.asList(new PreparedImageSpecificationScript()
                    .withName("initialize-node")
                    .withExecutionPoint(ExecutionPoint.NODE_IMAGE_BUILD_TIME)
                    .withScriptType(ScriptType.BASH)
                    .withScript("echo \"test prepared image specification\" > /var/log/test-node-customization.txt"))))
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

### PreparedImageSpecifications_Delete

```java
/**
 * Samples for PreparedImageSpecifications Delete.
 */
public final class PreparedImageSpecificationsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/PreparedImageSpecifications_Delete.json
     */
    /**
     * Sample code: PreparedImageSpecifications_Delete.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void preparedImageSpecificationsDelete(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        manager.preparedImageSpecifications()
            .delete("rg1", "my-prepared-image-specification", null, com.azure.core.util.Context.NONE);
    }
}
```

### PreparedImageSpecifications_DeleteVersion

```java
/**
 * Samples for PreparedImageSpecifications DeleteVersion.
 */
public final class PreparedImageSpecificationsDeleteVeSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/PreparedImageSpecifications_DeleteVersion.json
     */
    /**
     * Sample code: PreparedImageSpecifications_DeleteVersion.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void preparedImageSpecificationsDeleteVersion(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        manager.preparedImageSpecifications()
            .deleteVersion("rg1", "my-prepared-image-specification", "20250101-abcd1234", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PreparedImageSpecifications_GetByResourceGroup

```java
/**
 * Samples for PreparedImageSpecifications GetByResourceGroup.
 */
public final class PreparedImageSpecificationsGetByResSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/PreparedImageSpecifications_Get.json
     */
    /**
     * Sample code: PreparedImageSpecifications_Get.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void preparedImageSpecificationsGet(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        manager.preparedImageSpecifications()
            .getByResourceGroupWithResponse("rg1", "my-prepared-image-specification", com.azure.core.util.Context.NONE);
    }
}
```

### PreparedImageSpecifications_GetVersion

```java
/**
 * Samples for PreparedImageSpecifications GetVersion.
 */
public final class PreparedImageSpecificationsGetVersiSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/PreparedImageSpecifications_GetVersion.json
     */
    /**
     * Sample code: PreparedImageSpecifications_GetVersion.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void preparedImageSpecificationsGetVersion(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        manager.preparedImageSpecifications()
            .getVersionWithResponse("rg1", "my-prepared-image-specification", "20250101-abcd1234",
                com.azure.core.util.Context.NONE);
    }
}
```

### PreparedImageSpecifications_List

```java
/**
 * Samples for PreparedImageSpecifications List.
 */
public final class PreparedImageSpecificationsListSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/PreparedImageSpecifications_ListBySubscription.json
     */
    /**
     * Sample code: PreparedImageSpecifications_ListBySubscription.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void preparedImageSpecificationsListBySubscription(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        manager.preparedImageSpecifications().list(com.azure.core.util.Context.NONE);
    }
}
```

### PreparedImageSpecifications_ListByResourceGroup

```java
/**
 * Samples for PreparedImageSpecifications ListByResourceGroup.
 */
public final class PreparedImageSpecificationsListByReSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/PreparedImageSpecifications_ListByResourceGroup.json
     */
    /**
     * Sample code: PreparedImageSpecifications_ListByResourceGroup.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void preparedImageSpecificationsListByResourceGroup(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        manager.preparedImageSpecifications().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### PreparedImageSpecifications_ListVersions

```java
/**
 * Samples for PreparedImageSpecifications ListVersions.
 */
public final class PreparedImageSpecificationsListVersSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/PreparedImageSpecifications_ListVersions.json
     */
    /**
     * Sample code: PreparedImageSpecifications_ListVersions.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void preparedImageSpecificationsListVersions(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        manager.preparedImageSpecifications()
            .listVersions("rg1", "my-prepared-image-specification", com.azure.core.util.Context.NONE);
    }
}
```

### PreparedImageSpecifications_Update

```java
import com.azure.resourcemanager.containerservicepreparedimgspec.models.PreparedImageSpecification;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PreparedImageSpecifications Update.
 */
public final class PreparedImageSpecificationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-02-preview/PreparedImageSpecifications_Update.json
     */
    /**
     * Sample code: PreparedImageSpecifications_Update.
     * 
     * @param manager Entry point to ContainerServicePreparedImageSpecificationManager.
     */
    public static void preparedImageSpecificationsUpdate(
        com.azure.resourcemanager.containerservicepreparedimgspec.ContainerServicePreparedImageSpecificationManager manager) {
        PreparedImageSpecification resource = manager.preparedImageSpecifications()
            .getByResourceGroupWithResponse("rg1", "my-prepared-image-specification", com.azure.core.util.Context.NONE)
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

