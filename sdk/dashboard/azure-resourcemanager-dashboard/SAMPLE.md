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
import com.azure.resourcemanager.dashboard.models.IdentityType;
import com.azure.resourcemanager.dashboard.models.ManagedGrafanaProperties;
import com.azure.resourcemanager.dashboard.models.ManagedIdentity;
import com.azure.resourcemanager.dashboard.models.ProvisioningState;
import com.azure.resourcemanager.dashboard.models.ResourceSku;
import com.azure.resourcemanager.dashboard.models.ZoneRedundancy;
import java.util.HashMap;
import java.util.Map;

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
            .withRegion("West US")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Environment", "Dev"))
            .withSku(new ResourceSku().withName("Standard"))
            .withProperties(
                new ManagedGrafanaProperties()
                    .withProvisioningState(ProvisioningState.ACCEPTED)
                    .withZoneRedundancy(ZoneRedundancy.ENABLED))
            .withIdentity(new ManagedIdentity().withType(IdentityType.SYSTEM_ASSIGNED))
            .create();
    }

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
        manager.grafanas().delete("myResourceGroup", "myWorkspace", Context.NONE);
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
        manager.grafanas().getByResourceGroupWithResponse("myResourceGroup", "myWorkspace", Context.NONE);
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
        manager.grafanas().list(Context.NONE);
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
        manager.grafanas().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### Grafana_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dashboard.models.ManagedGrafana;
import java.util.HashMap;
import java.util.Map;

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
        ManagedGrafana resource =
            manager
                .grafanas()
                .getByResourceGroupWithResponse("myResourceGroup", "myWorkspace", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("Environment", "Dev 2")).apply();
    }

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

