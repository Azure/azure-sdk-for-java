# Code snippets and samples


## Operations

- [List](#operations_list)

## StorageTaskAssignment

- [List](#storagetaskassignment_list)

## StorageTasks

- [Create](#storagetasks_create)
- [Delete](#storagetasks_delete)
- [GetByResourceGroup](#storagetasks_getbyresourcegroup)
- [List](#storagetasks_list)
- [ListByResourceGroup](#storagetasks_listbyresourcegroup)
- [PreviewActions](#storagetasks_previewactions)
- [Update](#storagetasks_update)

## StorageTasksReport

- [List](#storagetasksreport_list)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2023-01-01/misc/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void operationsList(com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageTaskAssignment_List

```java
/**
 * Samples for StorageTaskAssignment List.
 */
public final class StorageTaskAssignmentListSamples {
    /*
     * x-ms-original-file: 2023-01-01/storageTasksList/ListStorageTaskAssignmentIds.json
     */
    /**
     * Sample code: ListStorageTaskAssignmentsByResourceGroup.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void listStorageTaskAssignmentsByResourceGroup(
        com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        manager.storageTaskAssignments().list("rgroup1", "mytask1", null, com.azure.core.util.Context.NONE);
    }
}
```

### StorageTasks_Create

```java
import com.azure.resourcemanager.storageactions.models.ElseCondition;
import com.azure.resourcemanager.storageactions.models.IfCondition;
import com.azure.resourcemanager.storageactions.models.ManagedServiceIdentity;
import com.azure.resourcemanager.storageactions.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.storageactions.models.OnFailure;
import com.azure.resourcemanager.storageactions.models.OnSuccess;
import com.azure.resourcemanager.storageactions.models.StorageTaskAction;
import com.azure.resourcemanager.storageactions.models.StorageTaskOperation;
import com.azure.resourcemanager.storageactions.models.StorageTaskOperationName;
import com.azure.resourcemanager.storageactions.models.StorageTaskProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageTasks Create.
 */
public final class StorageTasksCreateSamples {
    /*
     * x-ms-original-file: 2023-01-01/storageTasksCrud/PutStorageTask.json
     */
    /**
     * Sample code: PutStorageTask.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void putStorageTask(com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        manager.storageTasks()
            .define("mytask1")
            .withRegion("westus")
            .withExistingResourceGroup("res4228")
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .withProperties(
                new StorageTaskProperties().withEnabled(true)
                    .withDescription("My Storage task")
                    .withAction(new StorageTaskAction()
                        .withIfProperty(new IfCondition().withCondition("[[equals(AccessTier, 'Cool')]]")
                            .withOperations(Arrays
                                .asList(new StorageTaskOperation().withName(StorageTaskOperationName.SET_BLOB_TIER)
                                    .withParameters(mapOf("tier", "Hot"))
                                    .withOnSuccess(OnSuccess.CONTINUE)
                                    .withOnFailure(OnFailure.BREAK))))
                        .withElseProperty(new ElseCondition().withOperations(
                            Arrays.asList(new StorageTaskOperation().withName(StorageTaskOperationName.DELETE_BLOB)
                                .withOnSuccess(OnSuccess.CONTINUE)
                                .withOnFailure(OnFailure.BREAK))))))
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

### StorageTasks_Delete

```java
/**
 * Samples for StorageTasks Delete.
 */
public final class StorageTasksDeleteSamples {
    /*
     * x-ms-original-file: 2023-01-01/storageTasksCrud/DeleteStorageTask.json
     */
    /**
     * Sample code: DeleteStorageTask.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void deleteStorageTask(com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        manager.storageTasks().delete("res4228", "mytask1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTasks_GetByResourceGroup

```java
/**
 * Samples for StorageTasks GetByResourceGroup.
 */
public final class StorageTasksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-01-01/storageTasksCrud/GetStorageTask.json
     */
    /**
     * Sample code: GetStorageTask.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void getStorageTask(com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        manager.storageTasks().getByResourceGroupWithResponse("res4228", "mytask1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTasks_List

```java
/**
 * Samples for StorageTasks List.
 */
public final class StorageTasksListSamples {
    /*
     * x-ms-original-file: 2023-01-01/storageTasksList/ListStorageTasksBySubscription.json
     */
    /**
     * Sample code: ListStorageTasksBySubscription.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void
        listStorageTasksBySubscription(com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        manager.storageTasks().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageTasks_ListByResourceGroup

```java
/**
 * Samples for StorageTasks ListByResourceGroup.
 */
public final class StorageTasksListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-01-01/storageTasksList/ListStorageTasksByResourceGroup.json
     */
    /**
     * Sample code: ListStorageTasksByResourceGroup.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void
        listStorageTasksByResourceGroup(com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        manager.storageTasks().listByResourceGroup("res6117", com.azure.core.util.Context.NONE);
    }
}
```

### StorageTasks_PreviewActions

```java
import com.azure.resourcemanager.storageactions.fluent.models.StorageTaskPreviewActionInner;
import com.azure.resourcemanager.storageactions.models.StorageTaskPreviewActionCondition;
import com.azure.resourcemanager.storageactions.models.StorageTaskPreviewActionIfCondition;
import com.azure.resourcemanager.storageactions.models.StorageTaskPreviewActionProperties;
import com.azure.resourcemanager.storageactions.models.StorageTaskPreviewBlobProperties;
import com.azure.resourcemanager.storageactions.models.StorageTaskPreviewContainerProperties;
import com.azure.resourcemanager.storageactions.models.StorageTaskPreviewKeyValueProperties;
import java.util.Arrays;

/**
 * Samples for StorageTasks PreviewActions.
 */
public final class StorageTasksPreviewActionsSamples {
    /*
     * x-ms-original-file: 2023-01-01/misc/PerformStorageTaskActionsPreview.json
     */
    /**
     * Sample code: PerformStorageTaskActionsPreview.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void
        performStorageTaskActionsPreview(com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        manager.storageTasks()
            .previewActionsWithResponse("eastus",
                new StorageTaskPreviewActionInner()
                    .withProperties(
                        new StorageTaskPreviewActionProperties()
                            .withContainer(
                                new StorageTaskPreviewContainerProperties().withName("firstContainer")
                                    .withMetadata(Arrays.asList(new StorageTaskPreviewKeyValueProperties()
                                        .withKey("fakeTokenPlaceholder")
                                        .withValue("mContainerValue1"))))
                            .withBlobs(Arrays.asList(new StorageTaskPreviewBlobProperties()
                                .withName("folder1/file1.txt")
                                .withProperties(Arrays.asList(new StorageTaskPreviewKeyValueProperties()
                                    .withKey("fakeTokenPlaceholder")
                                    .withValue("Wed, 07 Jun 2023 05:23:29 GMT"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("Wed, 07 Jun 2023 05:23:29 GMT"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("0x8DB67175454D36D"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("38619"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("text/xml"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue(""),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue(""),
                                    new StorageTaskPreviewKeyValueProperties()
                                        .withKey("fakeTokenPlaceholder")
                                        .withValue(""),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("njr6iDrmU9+FC89WMK22EA=="),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue(""),
                                    new StorageTaskPreviewKeyValueProperties()
                                        .withKey("fakeTokenPlaceholder")
                                        .withValue(""),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("BlockBlob"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("Hot"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("true"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("unlocked"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("available"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("true"),
                                    new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("1")))
                                .withMetadata(Arrays
                                    .asList(new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("mValue1")))
                                .withTags(Arrays
                                    .asList(new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                        .withValue("tValue1"))),
                                new StorageTaskPreviewBlobProperties()
                                    .withName("folder2/file1.txt")
                                    .withProperties(Arrays.asList(
                                        new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                            .withValue("Wed, 06 Jun 2023 05:23:29 GMT"),
                                        new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                            .withValue("Wed, 06 Jun 2023 05:23:29 GMT"),
                                        new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                            .withValue("0x6FB67175454D36D")))
                                    .withMetadata(Arrays.asList(
                                        new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                            .withValue("mValue2")))
                                    .withTags(Arrays.asList(
                                        new StorageTaskPreviewKeyValueProperties().withKey("fakeTokenPlaceholder")
                                            .withValue("tValue2")))))
                            .withAction(new StorageTaskPreviewActionCondition()
                                .withIfProperty(new StorageTaskPreviewActionIfCondition()
                                    .withCondition("[[equals(AccessTier, 'Hot')]]"))
                                .withElseBlockExists(true))),
                com.azure.core.util.Context.NONE);
    }
}
```

### StorageTasks_Update

```java
import com.azure.resourcemanager.storageactions.models.ElseCondition;
import com.azure.resourcemanager.storageactions.models.IfCondition;
import com.azure.resourcemanager.storageactions.models.ManagedServiceIdentity;
import com.azure.resourcemanager.storageactions.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.storageactions.models.OnFailure;
import com.azure.resourcemanager.storageactions.models.OnSuccess;
import com.azure.resourcemanager.storageactions.models.StorageTask;
import com.azure.resourcemanager.storageactions.models.StorageTaskAction;
import com.azure.resourcemanager.storageactions.models.StorageTaskOperation;
import com.azure.resourcemanager.storageactions.models.StorageTaskOperationName;
import com.azure.resourcemanager.storageactions.models.StorageTaskUpdateProperties;
import com.azure.resourcemanager.storageactions.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageTasks Update.
 */
public final class StorageTasksUpdateSamples {
    /*
     * x-ms-original-file: 2023-01-01/storageTasksCrud/PatchStorageTask.json
     */
    /**
     * Sample code: PatchStorageTask.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void patchStorageTask(com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        StorageTask resource = manager.storageTasks()
            .getByResourceGroupWithResponse("res4228", "mytask1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/1f31ba14-ce16-4281-b9b4-3e78da6e1616/resourceGroups/res4228/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myUserAssignedIdentity",
                    new UserAssignedIdentity())))
            .withProperties(
                new StorageTaskUpdateProperties().withEnabled(true)
                    .withAction(new StorageTaskAction()
                        .withIfProperty(new IfCondition().withCondition("[[equals(AccessTier, 'Cool')]]")
                            .withOperations(Arrays
                                .asList(new StorageTaskOperation().withName(StorageTaskOperationName.SET_BLOB_TIER)
                                    .withParameters(mapOf("tier", "Hot"))
                                    .withOnSuccess(OnSuccess.CONTINUE)
                                    .withOnFailure(OnFailure.BREAK))))
                        .withElseProperty(new ElseCondition().withOperations(
                            Arrays.asList(new StorageTaskOperation().withName(StorageTaskOperationName.DELETE_BLOB)
                                .withOnSuccess(OnSuccess.CONTINUE)
                                .withOnFailure(OnFailure.BREAK))))))
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

### StorageTasksReport_List

```java
/**
 * Samples for StorageTasksReport List.
 */
public final class StorageTasksReportListSamples {
    /*
     * x-ms-original-file: 2023-01-01/storageTasksList/ListStorageTasksRunReportSummary.json
     */
    /**
     * Sample code: ListStorageTasksByResourceGroup.
     * 
     * @param manager Entry point to StorageActionsManager.
     */
    public static void
        listStorageTasksByResourceGroup(com.azure.resourcemanager.storageactions.StorageActionsManager manager) {
        manager.storageTasksReports().list("rgroup1", "mytask1", null, null, com.azure.core.util.Context.NONE);
    }
}
```

