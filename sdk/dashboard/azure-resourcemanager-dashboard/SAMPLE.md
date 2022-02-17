# Code snippets and samples


## Grafana

- [Create](#grafana_create)
- [Delete](#grafana_delete)
- [GetByResourceGroup](#grafana_getbyresourcegroup)
- [List](#grafana_list)
- [ListByResourceGroup](#grafana_listbyresourcegroup)
- [Update](#grafana_update)

## Operations

- [List](#operations_list)
### Grafana_Create

```java
/** Samples for Grafana Create. */
public final class GrafanaCreateSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/preview/2021-09-01-preview/examples/Grafana_Create.json
     */
    /**
     * Sample code: Grafana_Create.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaCreate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager
            .grafanas()
            .define("myWorkspace")
            .withRegion((String) null)
            .withExistingResourceGroup("00000000-0000-0000-0000-000000000000", "myResourceGroup")
            .create();
    }
}
```

### Grafana_Delete

```java
import com.azure.core.util.Context;

/** Samples for Grafana Delete. */
public final class GrafanaDeleteSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/preview/2021-09-01-preview/examples/Grafana_Delete.json
     */
    /**
     * Sample code: Grafana_Delete.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager
            .grafanas()
            .delete("00000000-0000-0000-0000-000000000000", "myResourceGroup", "myWorkspace", Context.NONE);
    }
}
```

### Grafana_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Grafana GetByResourceGroup. */
public final class GrafanaGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/preview/2021-09-01-preview/examples/Grafana_Get.json
     */
    /**
     * Sample code: Grafana_Get.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager
            .grafanas()
            .getByResourceGroupWithResponse(
                "00000000-0000-0000-0000-000000000000", "myResourceGroup", "myWorkspace", Context.NONE);
    }
}
```

### Grafana_List

```java
import com.azure.core.util.Context;

/** Samples for Grafana List. */
public final class GrafanaListSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/preview/2021-09-01-preview/examples/Grafana_List.json
     */
    /**
     * Sample code: Grafana_List.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.grafanas().list("00000000-0000-0000-0000-000000000000", Context.NONE);
    }
}
```

### Grafana_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Grafana ListByResourceGroup. */
public final class GrafanaListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/preview/2021-09-01-preview/examples/Grafana_ListByResourceGroup.json
     */
    /**
     * Sample code: Grafana_ListByResourceGroup.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaListByResourceGroup(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.grafanas().listByResourceGroup("00000000-0000-0000-0000-000000000000", "myResourceGroup", Context.NONE);
    }
}
```

### Grafana_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dashboard.models.GrafanaResource;

/** Samples for Grafana Update. */
public final class GrafanaUpdateSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/preview/2021-09-01-preview/examples/Grafana_Update.json
     */
    /**
     * Sample code: Grafana_Update.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaUpdate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        GrafanaResource resource =
            manager
                .grafanas()
                .getByResourceGroupWithResponse(
                    "00000000-0000-0000-0000-000000000000", "myResourceGroup", "myWorkspace", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/preview/2021-09-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void operationsList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

