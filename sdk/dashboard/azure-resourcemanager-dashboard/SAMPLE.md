# Code snippets and samples


## Grafana

- [CheckEnterpriseDetails](#grafana_checkenterprisedetails)
- [Create](#grafana_create)
- [Delete](#grafana_delete)
- [FetchAvailablePlugins](#grafana_fetchavailableplugins)
- [GetByResourceGroup](#grafana_getbyresourcegroup)
- [List](#grafana_list)
- [ListByResourceGroup](#grafana_listbyresourcegroup)
- [Update](#grafana_update)

## ManagedPrivateEndpoints

- [Create](#managedprivateendpoints_create)
- [Delete](#managedprivateendpoints_delete)
- [Get](#managedprivateendpoints_get)
- [List](#managedprivateendpoints_list)
- [Refresh](#managedprivateendpoints_refresh)
- [Update](#managedprivateendpoints_update)

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
### Grafana_CheckEnterpriseDetails

```java
/**
 * Samples for Grafana CheckEnterpriseDetails.
 */
public final class GrafanaCheckEnterpriseDetailsSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/EnterpriseDetails_Post.json
     */
    /**
     * Sample code: EnterpriseDetails_Post.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void enterpriseDetailsPost(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.grafanas().checkEnterpriseDetailsWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Grafana_Create

```java
import com.azure.resourcemanager.dashboard.models.ApiKey;
import com.azure.resourcemanager.dashboard.models.AzureMonitorWorkspaceIntegration;
import com.azure.resourcemanager.dashboard.models.DeterministicOutboundIp;
import com.azure.resourcemanager.dashboard.models.EnterpriseConfigurations;
import com.azure.resourcemanager.dashboard.models.GrafanaConfigurations;
import com.azure.resourcemanager.dashboard.models.GrafanaIntegrations;
import com.azure.resourcemanager.dashboard.models.GrafanaPlugin;
import com.azure.resourcemanager.dashboard.models.ManagedGrafanaProperties;
import com.azure.resourcemanager.dashboard.models.ManagedServiceIdentity;
import com.azure.resourcemanager.dashboard.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.dashboard.models.MarketplaceAutoRenew;
import com.azure.resourcemanager.dashboard.models.PublicNetworkAccess;
import com.azure.resourcemanager.dashboard.models.ResourceSku;
import com.azure.resourcemanager.dashboard.models.Smtp;
import com.azure.resourcemanager.dashboard.models.StartTlsPolicy;
import com.azure.resourcemanager.dashboard.models.ZoneRedundancy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Samples for Grafana Create.
 */
public final class GrafanaCreateSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/Grafana_Create.json
     */
    /**
     * Sample code: Grafana_Create.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaCreate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.grafanas().define("myWorkspace").withRegion("West US").withExistingResourceGroup("myResourceGroup").withTags(mapOf("Environment", "Dev")).withSku(new ResourceSku().withName("Standard")).withProperties(new ManagedGrafanaProperties().withPublicNetworkAccess(PublicNetworkAccess.ENABLED).withZoneRedundancy(ZoneRedundancy.ENABLED).withApiKey(ApiKey.ENABLED).withDeterministicOutboundIp(DeterministicOutboundIp.ENABLED).withGrafanaIntegrations(new GrafanaIntegrations().withAzureMonitorWorkspaceIntegrations(Arrays.asList(new AzureMonitorWorkspaceIntegration().withAzureMonitorWorkspaceResourceId("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.monitor/accounts/myAzureMonitorWorkspace")))).withEnterpriseConfigurations(new EnterpriseConfigurations().withMarketplacePlanId("myPlanId").withMarketplaceAutoRenew(MarketplaceAutoRenew.ENABLED)).withGrafanaConfigurations(new GrafanaConfigurations().withSmtp(new Smtp().withEnabled(true).withHost("smtp.sendemail.com:587").withUser("username").withPassword("fakeTokenPlaceholder").withFromAddress("test@sendemail.com").withFromName("emailsender").withStartTlsPolicy(StartTlsPolicy.OPPORTUNISTIC_START_TLS).withSkipVerify(true))).withGrafanaPlugins(mapOf("sample-plugin-id", new GrafanaPlugin())).withGrafanaMajorVersion("9")).withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)).create();
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

### Grafana_Delete

```java
/**
 * Samples for Grafana Delete.
 */
public final class GrafanaDeleteSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/Grafana_Delete.json
     */
    /**
     * Sample code: Grafana_Delete.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.grafanas().delete("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Grafana_FetchAvailablePlugins

```java
/**
 * Samples for Grafana FetchAvailablePlugins.
 */
public final class GrafanaFetchAvailablePluginsSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/Grafana_FetchAvailablePlugins.json
     */
    /**
     * Sample code: Grafana_FetchAvailablePlugins.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaFetchAvailablePlugins(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.grafanas().fetchAvailablePluginsWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Grafana_GetByResourceGroup

```java
/**
 * Samples for Grafana GetByResourceGroup.
 */
public final class GrafanaGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/Grafana_Get.json
     */
    /**
     * Sample code: Grafana_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.grafanas().getByResourceGroupWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Grafana_List

```java
/**
 * Samples for Grafana List.
 */
public final class GrafanaListSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/Grafana_List.json
     */
    /**
     * Sample code: Grafana_List.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.grafanas().list(com.azure.core.util.Context.NONE);
    }
}
```

### Grafana_ListByResourceGroup

```java
/**
 * Samples for Grafana ListByResourceGroup.
 */
public final class GrafanaListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/Grafana_ListByResourceGroup.json
     */
    /**
     * Sample code: Grafana_ListByResourceGroup.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaListByResourceGroup(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.grafanas().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Grafana_Update

```java
import com.azure.resourcemanager.dashboard.models.ApiKey;
import com.azure.resourcemanager.dashboard.models.AzureMonitorWorkspaceIntegration;
import com.azure.resourcemanager.dashboard.models.DeterministicOutboundIp;
import com.azure.resourcemanager.dashboard.models.EnterpriseConfigurations;
import com.azure.resourcemanager.dashboard.models.GrafanaConfigurations;
import com.azure.resourcemanager.dashboard.models.GrafanaIntegrations;
import com.azure.resourcemanager.dashboard.models.GrafanaPlugin;
import com.azure.resourcemanager.dashboard.models.ManagedGrafana;
import com.azure.resourcemanager.dashboard.models.ManagedGrafanaPropertiesUpdateParameters;
import com.azure.resourcemanager.dashboard.models.MarketplaceAutoRenew;
import com.azure.resourcemanager.dashboard.models.ResourceSku;
import com.azure.resourcemanager.dashboard.models.Smtp;
import com.azure.resourcemanager.dashboard.models.StartTlsPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Samples for Grafana Update.
 */
public final class GrafanaUpdateSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/Grafana_Update.json
     */
    /**
     * Sample code: Grafana_Update.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaUpdate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        ManagedGrafana resource = manager.grafanas().getByResourceGroupWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("Environment", "Dev 2")).withSku(new ResourceSku().withName("Standard")).withProperties(new ManagedGrafanaPropertiesUpdateParameters().withApiKey(ApiKey.ENABLED).withDeterministicOutboundIp(DeterministicOutboundIp.ENABLED).withGrafanaIntegrations(new GrafanaIntegrations().withAzureMonitorWorkspaceIntegrations(Arrays.asList(new AzureMonitorWorkspaceIntegration().withAzureMonitorWorkspaceResourceId("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.monitor/accounts/myAzureMonitorWorkspace")))).withEnterpriseConfigurations(new EnterpriseConfigurations().withMarketplacePlanId("myPlanId").withMarketplaceAutoRenew(MarketplaceAutoRenew.ENABLED)).withGrafanaConfigurations(new GrafanaConfigurations().withSmtp(new Smtp().withEnabled(true).withHost("smtp.sendemail.com:587").withUser("username").withPassword("fakeTokenPlaceholder").withFromAddress("test@sendemail.com").withFromName("emailsender").withStartTlsPolicy(StartTlsPolicy.OPPORTUNISTIC_START_TLS).withSkipVerify(true))).withGrafanaPlugins(mapOf("sample-plugin-id", new GrafanaPlugin())).withGrafanaMajorVersion("9")).apply();
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

### ManagedPrivateEndpoints_Create

```java
import java.util.Arrays;

/**
 * Samples for ManagedPrivateEndpoints Create.
 */
public final class ManagedPrivateEndpointsCreateSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/ManagedPrivateEndpoints_Create.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_Create.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointCreate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedPrivateEndpoints().define("myMPEName").withRegion("West US").withExistingGrafana("myResourceGroup", "myWorkspace").withPrivateLinkResourceId("/subscriptions/xxxxxxxx-xxxx-xxxx-xxxx-000000000000/resourceGroups/xx-rg/providers/Microsoft.Kusto/Clusters/sampleKustoResource").withPrivateLinkResourceRegion("West US").withGroupIds(Arrays.asList("grafana")).withRequestMessage("Example Request Message").withPrivateLinkServiceUrl("my-self-hosted-influxdb.westus.mydomain.com").create();
    }
}
```

### ManagedPrivateEndpoints_Delete

```java
/**
 * Samples for ManagedPrivateEndpoints Delete.
 */
public final class ManagedPrivateEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/ManagedPrivateEndpoints_Delete.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_Delete.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedPrivateEndpoints().delete("myResourceGroup", "myWorkspace", "myMPEName", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_Get

```java
/**
 * Samples for ManagedPrivateEndpoints Get.
 */
public final class ManagedPrivateEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/ManagedPrivateEndpoints_Get.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedPrivateEndpoints().getWithResponse("myResourceGroup", "myWorkspace", "myMPEName", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_List

```java
/**
 * Samples for ManagedPrivateEndpoints List.
 */
public final class ManagedPrivateEndpointsListSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/ManagedPrivateEndpoints_List.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_List.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedPrivateEndpoints().list("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_Refresh

```java
/**
 * Samples for ManagedPrivateEndpoints Refresh.
 */
public final class ManagedPrivateEndpointsRefreshSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/ManagedPrivateEndpoints_Refresh.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_Refresh.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointRefresh(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedPrivateEndpoints().refresh("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_Update

```java
import com.azure.resourcemanager.dashboard.models.ManagedPrivateEndpointModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedPrivateEndpoints Update.
 */
public final class ManagedPrivateEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/ManagedPrivateEndpoints_Patch.json
     */
    /**
     * Sample code: ManagedPrivateEndpoints_Patch.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointsPatch(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        ManagedPrivateEndpointModel resource = manager.managedPrivateEndpoints().getWithResponse("myResourceGroup", "myWorkspace", "myMPEName", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("Environment", "Dev 2")).apply();
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
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void operationsList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Approve

```java
/**
 * Samples for PrivateEndpointConnections Approve.
 */
public final class PrivateEndpointConnectionsApproveSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/PrivateEndpointConnections_Approve.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Approve.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsApprove(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateEndpointConnections().define("myConnection").withExistingGrafana("myResourceGroup", "myWorkspace").create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateEndpointConnections().delete("myResourceGroup", "myWorkspace", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateEndpointConnections().getWithResponse("myResourceGroup", "myWorkspace", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
/**
 * Samples for PrivateEndpointConnections List.
 */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/PrivateEndpointConnections_List.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateEndpointConnections().list("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: PrivateLinkResources_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateLinkResourcesGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateLinkResources().getWithResponse("myResourceGroup", "myWorkspace", "grafana", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
/**
 * Samples for PrivateLinkResources List.
 */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/dashboard/resource-manager/Microsoft.Dashboard/stable/2023-09-01/examples/PrivateLinkResources_List.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateLinkResourcesList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateLinkResources().list("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

