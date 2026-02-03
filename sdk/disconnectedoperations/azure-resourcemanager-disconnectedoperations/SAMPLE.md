# Code snippets and samples


## Artifacts

- [Get](#artifacts_get)
- [ListByParent](#artifacts_listbyparent)
- [ListDownloadUri](#artifacts_listdownloaduri)

## DisconnectedOperations

- [CreateOrUpdate](#disconnectedoperations_createorupdate)
- [Delete](#disconnectedoperations_delete)
- [GetByResourceGroup](#disconnectedoperations_getbyresourcegroup)
- [ListDeploymentManifest](#disconnectedoperations_listdeploymentmanifest)

## Images

- [Get](#images_get)
- [ListByDisconnectedOperation](#images_listbydisconnectedoperation)
- [ListDownloadUri](#images_listdownloaduri)
### Artifacts_Get

```java
/**
 * Samples for Artifacts Get.
 */
public final class ArtifactsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/Artifacts_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Artifacts_Get.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void
        artifactsGet(com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.artifacts()
            .getWithResponse("rgdisconnectedoperations", "J_3-_S--_-UM_-_7w11", "PMY-", "-8Y-Us1BNNG6-H5w6-2--RP",
                com.azure.core.util.Context.NONE);
    }
}
```

### Artifacts_ListByParent

```java
/**
 * Samples for Artifacts ListByParent.
 */
public final class ArtifactsListByParentSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/Artifact_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: Artifacts_ListByParent.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void
        artifactsListByParent(com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.artifacts()
            .listByParent("rgdisconnectedoperations", "XOn_Y-7_M-46E-Y", "2v5Q3mNihPV88C882LnbQO8",
                com.azure.core.util.Context.NONE);
    }
}
```

### Artifacts_ListDownloadUri

```java
/**
 * Samples for Artifacts ListDownloadUri.
 */
public final class ArtifactsListDownloadUriSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/Artifact_ListDownloadUri_MaximumSet_Gen.json
     */
    /**
     * Sample code: Artifacts_ListDownloadUri.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void artifactsListDownloadUri(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.artifacts()
            .listDownloadUriWithResponse("rgdisconnectedoperations", "L4z_-S", "B-Ra--W0", "artifact1",
                com.azure.core.util.Context.NONE);
    }
}
```

### DisconnectedOperations_CreateOrUpdate

```java
import com.azure.resourcemanager.disconnectedoperations.models.ConnectionIntent;
import com.azure.resourcemanager.disconnectedoperations.models.DisconnectedOperationProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DisconnectedOperations CreateOrUpdate.
 */
public final class DisconnectedOperationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/DisconnectedOperations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_CreateOrUpdate.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsCreateOrUpdate(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations()
            .define("demo-resource")
            .withRegion("eastus")
            .withExistingResourceGroup("rgdisconnectedOperations")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withProperties(new DisconnectedOperationProperties().withConnectionIntent(ConnectionIntent.DISCONNECTED))
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

### DisconnectedOperations_Delete

```java
/**
 * Samples for DisconnectedOperations Delete.
 */
public final class DisconnectedOperationsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/DisconnectedOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_Delete.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsDelete(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations()
            .delete("rgdisconnectedoperations", "demo-resource", com.azure.core.util.Context.NONE);
    }
}
```

### DisconnectedOperations_GetByResourceGroup

```java
/**
 * Samples for DisconnectedOperations GetByResourceGroup.
 */
public final class DisconnectedOperationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/DisconnectedOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_Get.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsGet(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations()
            .getByResourceGroupWithResponse("rgdisconnectedoperations", "demo-resource",
                com.azure.core.util.Context.NONE);
    }
}
```

### DisconnectedOperations_ListDeploymentManifest

```java
/**
 * Samples for DisconnectedOperations ListDeploymentManifest.
 */
public final class DisconnectedOperationsListDeploymentManifestSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/DisconnectedOperations_ListDeploymentManifest_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_ListDeploymentManifest.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsListDeploymentManifest(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations()
            .listDeploymentManifestWithResponse("rgdisconnectedoperations", "demo-resource",
                com.azure.core.util.Context.NONE);
    }
}
```

### Images_Get

```java
/**
 * Samples for Images Get.
 */
public final class ImagesGetSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/Images_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Images_Get.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void
        imagesGet(com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.images()
            .getWithResponse("rgdisconnectedoperations", "bT62l-KS7g1-uh", "2P6", com.azure.core.util.Context.NONE);
    }
}
```

### Images_ListByDisconnectedOperation

```java
/**
 * Samples for Images ListByDisconnectedOperation.
 */
public final class ImagesListByDisconnectedOperationSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/Images_ListByDisconnectedOperation_MaximumSet_Gen.json
     */
    /**
     * Sample code: Images_ListByDisconnectedOperation.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void imagesListByDisconnectedOperation(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.images()
            .listByDisconnectedOperation("rgdisconnectedoperations", "w_-EG-3-euL7K3-E", "toynendoobwkrcwmfdfup", 20, 3,
                com.azure.core.util.Context.NONE);
    }
}
```

### Images_ListDownloadUri

```java
/**
 * Samples for Images ListDownloadUri.
 */
public final class ImagesListDownloadUriSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/Images_ListDownloadUri_MaximumSet_Gen.json
     */
    /**
     * Sample code: Images_ListDownloadUri.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void
        imagesListDownloadUri(com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.images()
            .listDownloadUriWithResponse("rgdisconnectedOperations", "g_-5-160", "1Q6lGV4V65j-1",
                com.azure.core.util.Context.NONE);
    }
}
```

