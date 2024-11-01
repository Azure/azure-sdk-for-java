# Code snippets and samples


## Labels

- [CreateAndUpdate](#labels_createandupdate)
- [Delete](#labels_delete)
- [GetByWorkspace](#labels_getbyworkspace)
- [ListByWorkspace](#labels_listbyworkspace)
- [Update](#labels_update)

## Operations

- [List](#operations_list)

## Tasks

- [GetByWorkspace](#tasks_getbyworkspace)

## Workspaces

- [CreateAndUpdate](#workspaces_createandupdate)
- [Delete](#workspaces_delete)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [Update](#workspaces_update)
### Labels_CreateAndUpdate

```java

/**
 * Samples for Labels CreateAndUpdate.
 */
public final class LabelsCreateAndUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Labels_CreateAndUpdate.
     * json
     */
    /**
     * Sample code: Labels.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void labels(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.labels()
            .createAndUpdate("dummyrg", "ThisisaWorkspace", "ThisisaLabel", null, com.azure.core.util.Context.NONE);
    }
}
```

### Labels_Delete

```java
/**
 * Samples for Labels Delete.
 */
public final class LabelsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Labels_Delete.json
     */
    /**
     * Sample code: Labels.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void labels(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.labels().delete("dummyrg", "ThisisaWorkspace", "ThisisaLabel", com.azure.core.util.Context.NONE);
    }
}
```

### Labels_GetByWorkspace

```java
/**
 * Samples for Labels GetByWorkspace.
 */
public final class LabelsGetByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Labels_GetByWorkspace.
     * json
     */
    /**
     * Sample code: Labels.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void labels(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.labels()
            .getByWorkspaceWithResponse("dummyrg", "ThisisaWorkspace", "ThisisaLabel",
                com.azure.core.util.Context.NONE);
    }
}
```

### Labels_ListByWorkspace

```java
/**
 * Samples for Labels ListByWorkspace.
 */
public final class LabelsListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Labels_ListByWorkspace.
     * json
     */
    /**
     * Sample code: Labels.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void labels(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.labels().listByWorkspace("dummyrg", "ThisisaWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Labels_Update

```java

/**
 * Samples for Labels Update.
 */
public final class LabelsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Labels_Update.json
     */
    /**
     * Sample code: Labels.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void labels(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.labels()
            .updateWithResponse("dummyrg", "ThisisaWorkspace", "ThisisaLabel", null, com.azure.core.util.Context.NONE);
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
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void operations(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_GetByWorkspace

```java
/**
 * Samples for Tasks GetByWorkspace.
 */
public final class TasksGetByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Tasks_GetByWorkspace.
     * json
     */
    /**
     * Sample code: Tasks.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void tasks(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.tasks()
            .getByWorkspaceWithResponse("dummyrg", "ThisisaWorkspace", "ThisisaTaskId",
                com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateAndUpdate

```java
/**
 * Samples for Workspaces CreateAndUpdate.
 */
public final class WorkspacesCreateAndUpdateSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/
     * Workspaces_CreateAndUpdate.json
     */
    /**
     * Sample code: Workspaces.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void workspaces(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.workspaces()
            .define("ThisisaWorkspace")
            .withRegion("West US")
            .withExistingResourceGroup("dummyrg")
            .create();
    }
}
```

### Workspaces_Delete

```java
/**
 * Samples for Workspaces Delete.
 */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Workspaces_Delete.json
     */
    /**
     * Sample code: Workspaces.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void workspaces(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.workspaces().delete("dummyrg", "ThisisaWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/**
 * Samples for Workspaces GetByResourceGroup.
 */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Workspaces_Get.json
     */
    /**
     * Sample code: Workspaces.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void workspaces(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.workspaces()
            .getByResourceGroupWithResponse("dummyrg", "ThisisaWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/**
 * Samples for Workspaces List.
 */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/
     * Workspaces_ListBySubscription.json
     */
    /**
     * Sample code: Workspaces.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void workspaces(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.workspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
/**
 * Samples for Workspaces ListByResourceGroup.
 */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/
     * Workspaces_ListByResourceGroup.json
     */
    /**
     * Sample code: Workspaces.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void workspaces(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        manager.workspaces().listByResourceGroup("dummyrg", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.resourcemanager.defendereasm.models.WorkspaceResource;

/**
 * Samples for Workspaces Update.
 */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/riskiq/resource-manager/Microsoft.Easm/preview/2023-04-01-preview/examples/Workspaces_Update.json
     */
    /**
     * Sample code: Workspaces.
     * 
     * @param manager Entry point to EasmManager.
     */
    public static void workspaces(com.azure.resourcemanager.defendereasm.EasmManager manager) {
        WorkspaceResource resource = manager.workspaces()
            .getByResourceGroupWithResponse("dummyrg", "ThisisaWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

