# Code snippets and samples


## Labels

- [CreateAndUpdate](#labels_createandupdate)
- [Delete](#labels_delete)
- [GetByWorkspace](#labels_getbyworkspace)
- [ListByWorkspace](#labels_listbyworkspace)
- [Update](#labels_update)

## Operations

- [List](#operations_list)

## Workspaces

- [CreateAndUpdate](#workspaces_createandupdate)
- [Delete](#workspaces_delete)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [Update](#workspaces_update)
### Labels_CreateAndUpdate

```java
import com.azure.core.util.Context;

/** Samples for Labels CreateAndUpdate. */
public final class LabelsCreateAndUpdateSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Labels_CreateAndUpdate.json
     */
    /**
     * Sample code: Labels.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void labels(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.labels().createAndUpdate("dummyrg", "ThisisaWorkspace", "ThisisaLabel", null, Context.NONE);
    }
}
```

### Labels_Delete

```java
import com.azure.core.util.Context;

/** Samples for Labels Delete. */
public final class LabelsDeleteSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Labels_Delete.json
     */
    /**
     * Sample code: Labels.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void labels(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.labels().delete("dummyrg", "ThisisaWorkspace", "ThisisaLabel", Context.NONE);
    }
}
```

### Labels_GetByWorkspace

```java
import com.azure.core.util.Context;

/** Samples for Labels GetByWorkspace. */
public final class LabelsGetByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Labels_GetByWorkspace.json
     */
    /**
     * Sample code: Labels.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void labels(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.labels().getByWorkspaceWithResponse("dummyrg", "ThisisaWorkspace", "ThisisaLabel", Context.NONE);
    }
}
```

### Labels_ListByWorkspace

```java
import com.azure.core.util.Context;

/** Samples for Labels ListByWorkspace. */
public final class LabelsListByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Labels_ListByWorkspace.json
     */
    /**
     * Sample code: Labels.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void labels(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.labels().listByWorkspace("dummyrg", "ThisisaWorkspace", Context.NONE);
    }
}
```

### Labels_Update

```java
import com.azure.core.util.Context;

/** Samples for Labels Update. */
public final class LabelsUpdateSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Labels_Update.json
     */
    /**
     * Sample code: Labels.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void labels(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.labels().updateWithResponse("dummyrg", "ThisisaWorkspace", "ThisisaLabel", null, Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void operations(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Workspaces_CreateAndUpdate

```java
/** Samples for Workspaces CreateAndUpdate. */
public final class WorkspacesCreateAndUpdateSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Workspaces_CreateAndUpdate.json
     */
    /**
     * Sample code: Workspaces.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void workspaces(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager
            .workspaces()
            .define("ThisisaWorkspace")
            .withRegion("West US")
            .withExistingResourceGroup("dummyrg")
            .create();
    }
}
```

### Workspaces_Delete

```java
import com.azure.core.util.Context;

/** Samples for Workspaces Delete. */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Workspaces_Delete.json
     */
    /**
     * Sample code: Workspaces.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void workspaces(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.workspaces().delete("dummyrg", "ThisisaWorkspace", Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Workspaces GetByResourceGroup. */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Workspaces_Get.json
     */
    /**
     * Sample code: Workspaces.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void workspaces(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("dummyrg", "ThisisaWorkspace", Context.NONE);
    }
}
```

### Workspaces_List

```java
import com.azure.core.util.Context;

/** Samples for Workspaces List. */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Workspaces_ListBySubscription.json
     */
    /**
     * Sample code: Workspaces.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void workspaces(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.workspaces().list(Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListByResourceGroup. */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Workspaces_ListByResourceGroup.json
     */
    /**
     * Sample code: Workspaces.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void workspaces(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        manager.workspaces().listByResourceGroup("dummyrg", Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.riskiq.models.WorkspaceResource;

/** Samples for Workspaces Update. */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: specification/riskiq/resource-manager/Microsoft.Easm/preview/2022-04-01-preview/examples/Workspaces_Update.json
     */
    /**
     * Sample code: Workspaces.
     *
     * @param manager Entry point to RiskiqManager.
     */
    public static void workspaces(com.azure.resourcemanager.riskiq.RiskiqManager manager) {
        WorkspaceResource resource =
            manager.workspaces().getByResourceGroupWithResponse("dummyrg", "ThisisaWorkspace", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

