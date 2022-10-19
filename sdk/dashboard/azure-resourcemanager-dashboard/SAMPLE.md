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

## PrivateEndpointConnections

- [Approve](#privateendpointconnections_approve)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)
### Grafana_Create

```java
import com.azure.resourcemanager.dashboard.models.ApiKey;
import com.azure.resourcemanager.dashboard.models.AzureMonitorWorkspaceIntegration;
import com.azure.resourcemanager.dashboard.models.DeterministicOutboundIp;
import com.azure.resourcemanager.dashboard.models.GrafanaIntegrations;
import com.azure.resourcemanager.dashboard.models.ManagedGrafanaProperties;
import com.azure.resourcemanager.dashboard.models.ManagedServiceIdentity;
import com.azure.resourcemanager.dashboard.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.dashboard.models.PublicNetworkAccess;
import com.azure.resourcemanager.dashboard.models.ResourceSku;
import com.azure.resourcemanager.dashboard.models.ZoneRedundancy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Grafana Create. */
public final class GrafanaCreateSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/Grafana_Create.json
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
                    .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                    .withZoneRedundancy(ZoneRedundancy.ENABLED)
                    .withApiKey(ApiKey.ENABLED)
                    .withDeterministicOutboundIp(DeterministicOutboundIp.ENABLED)
                    .withGrafanaIntegrations(
                        new GrafanaIntegrations()
                            .withAzureMonitorWorkspaceIntegrations(
                                Arrays
                                    .asList(
                                        new AzureMonitorWorkspaceIntegration()
                                            .withAzureMonitorWorkspaceResourceId(
                                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.monitor/accounts/myAzureMonitorWorkspace")))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/Grafana_Delete.json
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
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/Grafana_Get.json
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
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/Grafana_List.json
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
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/Grafana_ListByResourceGroup.json
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
import com.azure.resourcemanager.dashboard.models.ApiKey;
import com.azure.resourcemanager.dashboard.models.AzureMonitorWorkspaceIntegration;
import com.azure.resourcemanager.dashboard.models.DeterministicOutboundIp;
import com.azure.resourcemanager.dashboard.models.GrafanaIntegrations;
import com.azure.resourcemanager.dashboard.models.ManagedGrafana;
import com.azure.resourcemanager.dashboard.models.ManagedGrafanaPropertiesUpdateParameters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Grafana Update. */
public final class GrafanaUpdateSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/Grafana_Update.json
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
        resource
            .update()
            .withTags(mapOf("Environment", "Dev 2"))
            .withProperties(
                new ManagedGrafanaPropertiesUpdateParameters()
                    .withApiKey(ApiKey.ENABLED)
                    .withDeterministicOutboundIp(DeterministicOutboundIp.ENABLED)
                    .withGrafanaIntegrations(
                        new GrafanaIntegrations()
                            .withAzureMonitorWorkspaceIntegrations(
                                Arrays
                                    .asList(
                                        new AzureMonitorWorkspaceIntegration()
                                            .withAzureMonitorWorkspaceResourceId(
                                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.monitor/accounts/myAzureMonitorWorkspace")))))
            .apply();
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
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/Operations_List.json
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

### PrivateEndpointConnections_Approve

```java
/** Samples for PrivateEndpointConnections Approve. */
public final class PrivateEndpointConnectionsApproveSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/PrivateEndpointConnections_Approve.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Approve.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsApprove(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager
            .privateEndpointConnections()
            .define("myConnection")
            .withExistingGrafana("myResourceGroup", "myWorkspace")
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateEndpointConnections().delete("myResourceGroup", "myWorkspace", "myConnection", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("myResourceGroup", "myWorkspace", "myConnection", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/PrivateEndpointConnections_List.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateEndpointConnections().list("myResourceGroup", "myWorkspace", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: PrivateLinkResources_Get.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void privateLinkResourcesGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateLinkResources().getWithResponse("myResourceGroup", "myWorkspace", "grafana", Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2022-08-01/examples/PrivateLinkResources_List.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     *
     * @param manager Entry point to DashboardManager.
     */
    public static void privateLinkResourcesList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateLinkResources().list("myResourceGroup", "myWorkspace", Context.NONE);
    }
}
```

