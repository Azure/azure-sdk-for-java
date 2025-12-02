# Code snippets and samples


## Operations

- [List](#operations_list)

## StorageDiscoveryWorkspaces

- [CreateOrUpdate](#storagediscoveryworkspaces_createorupdate)
- [Delete](#storagediscoveryworkspaces_delete)
- [GetByResourceGroup](#storagediscoveryworkspaces_getbyresourcegroup)
- [List](#storagediscoveryworkspaces_list)
- [ListByResourceGroup](#storagediscoveryworkspaces_listbyresourcegroup)
- [Update](#storagediscoveryworkspaces_update)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-09-01/Operation_List.json
     */
    /**
     * Sample code: List all provider operations.
     * 
     * @param manager Entry point to StorageDiscoveryManager.
     */
    public static void
        listAllProviderOperations(com.azure.resourcemanager.storagediscovery.StorageDiscoveryManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageDiscoveryWorkspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.storagediscovery.models.StorageDiscoveryResourceType;
import com.azure.resourcemanager.storagediscovery.models.StorageDiscoveryScope;
import com.azure.resourcemanager.storagediscovery.models.StorageDiscoveryWorkspaceProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageDiscoveryWorkspaces CreateOrUpdate.
 */
public final class StorageDiscoveryWorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-01/StorageDiscoveryWorkspaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update a StorageDiscoveryWorkspace.
     * 
     * @param manager Entry point to StorageDiscoveryManager.
     */
    public static void createOrUpdateAStorageDiscoveryWorkspace(
        com.azure.resourcemanager.storagediscovery.StorageDiscoveryManager manager) {
        manager.storageDiscoveryWorkspaces()
            .define("Sample-Storage-Workspace")
            .withRegion("westeurope")
            .withExistingResourceGroup("sample-rg")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withProperties(new StorageDiscoveryWorkspaceProperties()
                .withDescription("Sample Storage Discovery Workspace")
                .withWorkspaceRoots(Arrays.asList("/subscriptions/b79cb3ba-745e-5d9a-8903-4a02327a7e09"))
                .withScopes(Arrays.asList(new StorageDiscoveryScope().withDisplayName("Sample-Collection")
                    .withResourceTypes(Arrays.asList(StorageDiscoveryResourceType.fromString(
                        "/subscriptions/b79cb3ba-745e-5d9a-8903-4a02327a7e09/resourceGroups/sample-rg/providers/Microsoft.Storage/storageAccounts/sample-storageAccount")))
                    .withTagKeysOnly(Arrays.asList("filterTag1", "filterTag2"))
                    .withTags(mapOf("filterTag3", "value3", "filterTag4", "value4")),
                    new StorageDiscoveryScope().withDisplayName("Sample-Collection-2")
                        .withResourceTypes(Arrays.asList(StorageDiscoveryResourceType.fromString(
                            "/subscriptions/b79cb3ba-745e-5d9a-8903-4a02327a7e09/resourceGroups/sample-rg/providers/Microsoft.Storage/storageAccounts/sample-storageAccount")))
                        .withTagKeysOnly(Arrays.asList("filterTag5"))
                        .withTags(mapOf("filterTag6", "value6")))))
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

### StorageDiscoveryWorkspaces_Delete

```java
/**
 * Samples for StorageDiscoveryWorkspaces Delete.
 */
public final class StorageDiscoveryWorkspacesDeleteSamples {
    /*
     * x-ms-original-file: 2025-09-01/StorageDiscoveryWorkspaces_Delete.json
     */
    /**
     * Sample code: Delete a StorageDiscoveryWorkspace.
     * 
     * @param manager Entry point to StorageDiscoveryManager.
     */
    public static void
        deleteAStorageDiscoveryWorkspace(com.azure.resourcemanager.storagediscovery.StorageDiscoveryManager manager) {
        manager.storageDiscoveryWorkspaces()
            .deleteByResourceGroupWithResponse("sample-rg", "sampleworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### StorageDiscoveryWorkspaces_GetByResourceGroup

```java
/**
 * Samples for StorageDiscoveryWorkspaces GetByResourceGroup.
 */
public final class StorageDiscoveryWorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-09-01/StorageDiscoveryWorkspaces_Get.json
     */
    /**
     * Sample code: Get a StorageDiscoveryWorkspace.
     * 
     * @param manager Entry point to StorageDiscoveryManager.
     */
    public static void
        getAStorageDiscoveryWorkspace(com.azure.resourcemanager.storagediscovery.StorageDiscoveryManager manager) {
        manager.storageDiscoveryWorkspaces()
            .getByResourceGroupWithResponse("sample-rg", "Sample-Storage-Workspace", com.azure.core.util.Context.NONE);
    }
}
```

### StorageDiscoveryWorkspaces_List

```java
/**
 * Samples for StorageDiscoveryWorkspaces List.
 */
public final class StorageDiscoveryWorkspacesListSamples {
    /*
     * x-ms-original-file: 2025-09-01/StorageDiscoveryWorkspaces_ListBySubscription.json
     */
    /**
     * Sample code: List StorageDiscoveryWorkspaces by Subscription.
     * 
     * @param manager Entry point to StorageDiscoveryManager.
     */
    public static void listStorageDiscoveryWorkspacesBySubscription(
        com.azure.resourcemanager.storagediscovery.StorageDiscoveryManager manager) {
        manager.storageDiscoveryWorkspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageDiscoveryWorkspaces_ListByResourceGroup

```java
/**
 * Samples for StorageDiscoveryWorkspaces ListByResourceGroup.
 */
public final class StorageDiscoveryWorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-09-01/StorageDiscoveryWorkspaces_ListByResourceGroup.json
     */
    /**
     * Sample code: List StorageDiscoveryWorkspaces by Resource Group.
     * 
     * @param manager Entry point to StorageDiscoveryManager.
     */
    public static void listStorageDiscoveryWorkspacesByResourceGroup(
        com.azure.resourcemanager.storagediscovery.StorageDiscoveryManager manager) {
        manager.storageDiscoveryWorkspaces().listByResourceGroup("sample-rg", com.azure.core.util.Context.NONE);
    }
}
```

### StorageDiscoveryWorkspaces_Update

```java
import com.azure.resourcemanager.storagediscovery.models.StorageDiscoveryResourceType;
import com.azure.resourcemanager.storagediscovery.models.StorageDiscoveryScope;
import com.azure.resourcemanager.storagediscovery.models.StorageDiscoverySku;
import com.azure.resourcemanager.storagediscovery.models.StorageDiscoveryWorkspace;
import com.azure.resourcemanager.storagediscovery.models.StorageDiscoveryWorkspacePropertiesUpdate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageDiscoveryWorkspaces Update.
 */
public final class StorageDiscoveryWorkspacesUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-01/StorageDiscoveryWorkspaces_Update.json
     */
    /**
     * Sample code: Update a StorageDiscoveryWorkspace.
     * 
     * @param manager Entry point to StorageDiscoveryManager.
     */
    public static void
        updateAStorageDiscoveryWorkspace(com.azure.resourcemanager.storagediscovery.StorageDiscoveryManager manager) {
        StorageDiscoveryWorkspace resource = manager.storageDiscoveryWorkspaces()
            .getByResourceGroupWithResponse("sample-rg", "Sample-Storage-Workspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new StorageDiscoveryWorkspacePropertiesUpdate().withSku(StorageDiscoverySku.FREE)
                .withDescription("Updated Sample Storage Discovery Workspace")
                .withWorkspaceRoots(Arrays.asList("/subscriptions/b79cb3ba-745e-5d9a-8903-4a02327a7e09"))
                .withScopes(Arrays.asList(new StorageDiscoveryScope().withDisplayName("Updated-Sample-Collection")
                    .withResourceTypes(Arrays.asList(StorageDiscoveryResourceType.fromString(
                        "/subscriptions/b79cb3ba-745e-5d9a-8903-4a02327a7e09/resourceGroups/sample-rg/providers/Microsoft.Storage/storageAccounts/updated-sample-storageAccount")))
                    .withTagKeysOnly(Arrays.asList("updated-filtertag1", "updated-filtertag2"))
                    .withTags(mapOf("updated-filtertag3", "updated-value3", "updated-filtertag4", "updated-value4")))))
            .apply();
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

