# Code snippets and samples


## IntegrationFabrics

- [Create](#integrationfabrics_create)
- [Delete](#integrationfabrics_delete)
- [Get](#integrationfabrics_get)
- [List](#integrationfabrics_list)
- [Update](#integrationfabrics_update)

## ManagedDashboards

- [Create](#manageddashboards_create)
- [Delete](#manageddashboards_delete)
- [GetByResourceGroup](#manageddashboards_getbyresourcegroup)
- [List](#manageddashboards_list)
- [ListByResourceGroup](#manageddashboards_listbyresourcegroup)
- [Update](#manageddashboards_update)

## ManagedGrafanas

- [CheckEnterpriseDetails](#managedgrafanas_checkenterprisedetails)
- [Create](#managedgrafanas_create)
- [Delete](#managedgrafanas_delete)
- [FetchAvailablePlugins](#managedgrafanas_fetchavailableplugins)
- [GetByResourceGroup](#managedgrafanas_getbyresourcegroup)
- [List](#managedgrafanas_list)
- [ListByResourceGroup](#managedgrafanas_listbyresourcegroup)
- [Refresh](#managedgrafanas_refresh)
- [Update](#managedgrafanas_update)

## ManagedPrivateEndpointModels

- [Create](#managedprivateendpointmodels_create)
- [Delete](#managedprivateendpointmodels_delete)
- [Get](#managedprivateendpointmodels_get)
- [List](#managedprivateendpointmodels_list)
- [Update](#managedprivateendpointmodels_update)

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
### IntegrationFabrics_Create

```java
/**
 * Samples for ManagedGrafanas Delete.
 */
public final class ManagedGrafanasDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Grafana_Delete.json
     */
    /**
     * Sample code: Grafana_Delete.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedGrafanas().delete("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationFabrics_Delete

```java
/**
 * Samples for ManagedDashboards ListByResourceGroup.
 */
public final class ManagedDashboardsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Dashboard_ListByResourceGroup.json
     */
    /**
     * Sample code: Dashboard_ListByResourceGroup.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void dashboardListByResourceGroup(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedDashboards().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationFabrics_Get

```java
/**
 * Samples for ManagedGrafanas List.
 */
public final class ManagedGrafanasListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Grafana_List.json
     */
    /**
     * Sample code: Grafana_List.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedGrafanas().list(com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationFabrics_List

```java
/**
 * Samples for PrivateEndpointConnections List.
 */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/PrivateEndpointConnections_List.json
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

### IntegrationFabrics_Update

```java
import com.azure.resourcemanager.dashboard.models.IntegrationFabric;
import com.azure.resourcemanager.dashboard.models.IntegrationFabricPropertiesUpdateParameters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for IntegrationFabrics Update.
 */
public final class IntegrationFabricsUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/IntegrationFabrics_Update.json
     */
    /**
     * Sample code: IntegrationFabrics_Update.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void integrationFabricsUpdate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        IntegrationFabric resource = manager.integrationFabrics()
            .getWithResponse("myResourceGroup", "myWorkspace", "sampleIntegration", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Environment", "Dev 2"))
            .withProperties(new IntegrationFabricPropertiesUpdateParameters().withScenarios(Arrays.asList("scenario1")))
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

### ManagedDashboards_Create

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: PrivateLinkResources_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateLinkResourcesGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateLinkResources()
            .getWithResponse("myResourceGroup", "myWorkspace", "grafana", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedDashboards_Delete

```java
/**
 * Samples for PrivateLinkResources List.
 */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/PrivateLinkResources_List.json
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

### ManagedDashboards_GetByResourceGroup

```java
/**
 * Samples for ManagedGrafanas Refresh.
 */
public final class ManagedGrafanasRefreshSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/ManagedPrivateEndpoints_Refresh.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_Refresh.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointRefresh(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedGrafanas().refresh("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedDashboards_List

```java
/**
 * Samples for IntegrationFabrics Delete.
 */
public final class IntegrationFabricsDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/IntegrationFabrics_Delete.json
     */
    /**
     * Sample code: IntegrationFabrics_Delete.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void integrationFabricsDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.integrationFabrics()
            .delete("myResourceGroup", "myWorkspace", "sampleIntegration", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedDashboards_ListByResourceGroup

```java
/**
 * Samples for IntegrationFabrics Get.
 */
public final class IntegrationFabricsGetSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/IntegrationFabrics_Get.json
     */
    /**
     * Sample code: IntegrationFabrics_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void integrationFabricsGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.integrationFabrics()
            .getWithResponse("myResourceGroup", "myWorkspace", "sampleIntegration", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedDashboards_Update

```java
/**
 * Samples for ManagedDashboards List.
 */
public final class ManagedDashboardsListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Dashboard_List.json
     */
    /**
     * Sample code: Dashboard_ListByResourceGroup.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void dashboardListByResourceGroup(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedDashboards().list(com.azure.core.util.Context.NONE);
    }
}
```

### ManagedGrafanas_CheckEnterpriseDetails

```java
/**
 * Samples for IntegrationFabrics List.
 */
public final class IntegrationFabricsListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/IntegrationFabrics_List.json
     */
    /**
     * Sample code: IntegrationFabrics_List.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void integrationFabricsList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.integrationFabrics().list("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedGrafanas_Create

```java
/**
 * Samples for ManagedGrafanas GetByResourceGroup.
 */
public final class ManagedGrafanasGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Grafana_Get.json
     */
    /**
     * Sample code: Grafana_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedGrafanas()
            .getByResourceGroupWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedGrafanas_Delete

```java
/**
 * Samples for ManagedGrafanas CheckEnterpriseDetails.
 */
public final class ManagedGrafanasCheckEnterpriseDetailsSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/EnterpriseDetails_Post.json
     */
    /**
     * Sample code: EnterpriseDetails_Post.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void enterpriseDetailsPost(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedGrafanas()
            .checkEnterpriseDetailsWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedGrafanas_FetchAvailablePlugins

```java
/**
 * Samples for ManagedPrivateEndpointModels Delete.
 */
public final class ManagedPrivateEndpointModelsDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/ManagedPrivateEndpoints_Delete.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_Delete.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedPrivateEndpointModels()
            .delete("myResourceGroup", "myWorkspace", "myMPEName", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedGrafanas_GetByResourceGroup

```java
import com.azure.resourcemanager.dashboard.models.ManagedDashboard;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedDashboards Update.
 */
public final class ManagedDashboardsUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Dashboard_Update.json
     */
    /**
     * Sample code: Dashboard_Update.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void dashboardUpdate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        ManagedDashboard resource = manager.managedDashboards()
            .getByResourceGroupWithResponse("myResourceGroup", "myDashboard", com.azure.core.util.Context.NONE)
            .getValue();
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

### ManagedGrafanas_List

```java
import java.util.Arrays;

/**
 * Samples for ManagedPrivateEndpointModels Create.
 */
public final class ManagedPrivateEndpointModelsCreateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/ManagedPrivateEndpoints_Create.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_Create.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointCreate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedPrivateEndpointModels()
            .define("myMPEName")
            .withRegion("West US")
            .withExistingGrafana("myResourceGroup", "myWorkspace")
            .withPrivateLinkResourceId(
                "/subscriptions/xxxxxxxx-xxxx-xxxx-xxxx-000000000000/resourceGroups/xx-rg/providers/Microsoft.Kusto/Clusters/sampleKustoResource")
            .withPrivateLinkResourceRegion("West US")
            .withGroupIds(Arrays.asList("grafana"))
            .withRequestMessage("Example Request Message")
            .withPrivateLinkServiceUrl("my-self-hosted-influxdb.westus.mydomain.com")
            .create();
    }
}
```

### ManagedGrafanas_ListByResourceGroup

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedDashboards Create.
 */
public final class ManagedDashboardsCreateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Dashboard_Create.json
     */
    /**
     * Sample code: Dashboard_Create.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void dashboardCreate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedDashboards()
            .define("myDashboard")
            .withRegion("West US")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Environment", "Dev"))
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

### ManagedGrafanas_Refresh

```java
import com.azure.resourcemanager.dashboard.models.IntegrationFabricProperties;
import java.util.Arrays;

/**
 * Samples for IntegrationFabrics Create.
 */
public final class IntegrationFabricsCreateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/IntegrationFabrics_Create.json
     */
    /**
     * Sample code: IntegrationFabrics_Create.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void integrationFabricsCreate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.integrationFabrics()
            .define("sampleIntegration")
            .withRegion("West US")
            .withExistingGrafana("myResourceGroup", "myWorkspace")
            .withProperties(new IntegrationFabricProperties().withTargetResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerService/managedClusters/myAks")
                .withDataSourceResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Monitor/accounts/myAmw")
                .withScenarios(Arrays.asList("scenario1", "scenario2")))
            .create();
    }
}
```

### ManagedGrafanas_Update

```java
import com.azure.resourcemanager.dashboard.models.ApiKey;
import com.azure.resourcemanager.dashboard.models.AzureMonitorWorkspaceIntegration;
import com.azure.resourcemanager.dashboard.models.DeterministicOutboundIP;
import com.azure.resourcemanager.dashboard.models.EnterpriseConfigurations;
import com.azure.resourcemanager.dashboard.models.GrafanaConfigurations;
import com.azure.resourcemanager.dashboard.models.GrafanaIntegrations;
import com.azure.resourcemanager.dashboard.models.GrafanaPlugin;
import com.azure.resourcemanager.dashboard.models.ManagedGrafana;
import com.azure.resourcemanager.dashboard.models.ManagedGrafanaPropertiesUpdateParameters;
import com.azure.resourcemanager.dashboard.models.MarketplaceAutoRenew;
import com.azure.resourcemanager.dashboard.models.ResourceSku;
import com.azure.resourcemanager.dashboard.models.Security;
import com.azure.resourcemanager.dashboard.models.Smtp;
import com.azure.resourcemanager.dashboard.models.Snapshots;
import com.azure.resourcemanager.dashboard.models.StartTLSPolicy;
import com.azure.resourcemanager.dashboard.models.UnifiedAlertingScreenshots;
import com.azure.resourcemanager.dashboard.models.Users;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedGrafanas Update.
 */
public final class ManagedGrafanasUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Grafana_Update.json
     */
    /**
     * Sample code: Grafana_Update.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaUpdate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        ManagedGrafana resource = manager.managedGrafanas()
            .getByResourceGroupWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Environment", "Dev 2"))
            .withSku(new ResourceSku().withName("Standard"))
            .withProperties(new ManagedGrafanaPropertiesUpdateParameters().withApiKey(ApiKey.ENABLED)
                .withDeterministicOutboundIP(DeterministicOutboundIP.ENABLED)
                .withGrafanaIntegrations(new GrafanaIntegrations().withAzureMonitorWorkspaceIntegrations(
                    Arrays.asList(new AzureMonitorWorkspaceIntegration().withAzureMonitorWorkspaceResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.monitor/accounts/myAzureMonitorWorkspace"))))
                .withEnterpriseConfigurations(new EnterpriseConfigurations().withMarketplacePlanId("myPlanId")
                    .withMarketplaceAutoRenew(MarketplaceAutoRenew.ENABLED))
                .withGrafanaConfigurations(new GrafanaConfigurations()
                    .withSmtp(new Smtp().withEnabled(true)
                        .withHost("smtp.sendemail.com:587")
                        .withUser("username")
                        .withPassword("fakeTokenPlaceholder")
                        .withFromAddress("test@sendemail.com")
                        .withFromName("emailsender")
                        .withStartTLSPolicy(StartTLSPolicy.OPPORTUNISTIC_START_TLS)
                        .withSkipVerify(true))
                    .withSnapshots(new Snapshots().withExternalEnabled(true))
                    .withUsers(new Users().withViewersCanEdit(true).withEditorsCanAdmin(true))
                    .withSecurity(new Security().withCsrfAlwaysCheck(false))
                    .withUnifiedAlertingScreenshots(new UnifiedAlertingScreenshots().withCaptureEnabled(false)))
                .withGrafanaPlugins(mapOf("sample-plugin-id", new GrafanaPlugin()))
                .withGrafanaMajorVersion("9"))
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

### ManagedPrivateEndpointModels_Create

```java
/**
 * Samples for ManagedPrivateEndpointModels Get.
 */
public final class ManagedPrivateEndpointModelsGetSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/ManagedPrivateEndpoints_Get.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedPrivateEndpointModels()
            .getWithResponse("myResourceGroup", "myWorkspace", "myMPEName", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpointModels_Delete

```java
/**
 * Samples for PrivateEndpointConnections Approve.
 */
public final class PrivateEndpointConnectionsApproveSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/PrivateEndpointConnections_Approve.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Approve.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsApprove(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateEndpointConnections()
            .define("myConnection")
            .withExistingGrafana("myResourceGroup", "myWorkspace")
            .create();
    }
}
```

### ManagedPrivateEndpointModels_Get

```java
/**
 * Samples for ManagedDashboards GetByResourceGroup.
 */
public final class ManagedDashboardsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Dashboard_Get.json
     */
    /**
     * Sample code: Dashboard_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void dashboardGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedDashboards()
            .getByResourceGroupWithResponse("myResourceGroup", "myDashboard", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpointModels_List

```java
/**
 * Samples for ManagedGrafanas FetchAvailablePlugins.
 */
public final class ManagedGrafanasFetchAvailablePluginsSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Grafana_FetchAvailablePlugins.json
     */
    /**
     * Sample code: Grafana_FetchAvailablePlugins.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaFetchAvailablePlugins(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedGrafanas()
            .fetchAvailablePluginsWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpointModels_Update

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("myResourceGroup", "myWorkspace", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.resourcemanager.dashboard.models.ApiKey;
import com.azure.resourcemanager.dashboard.models.AzureMonitorWorkspaceIntegration;
import com.azure.resourcemanager.dashboard.models.DeterministicOutboundIP;
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
import com.azure.resourcemanager.dashboard.models.Security;
import com.azure.resourcemanager.dashboard.models.Smtp;
import com.azure.resourcemanager.dashboard.models.Snapshots;
import com.azure.resourcemanager.dashboard.models.StartTLSPolicy;
import com.azure.resourcemanager.dashboard.models.UnifiedAlertingScreenshots;
import com.azure.resourcemanager.dashboard.models.Users;
import com.azure.resourcemanager.dashboard.models.ZoneRedundancy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedGrafanas Create.
 */
public final class ManagedGrafanasCreateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Grafana_Create.json
     */
    /**
     * Sample code: Grafana_Create.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaCreate(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedGrafanas()
            .define("myWorkspace")
            .withRegion("West US")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Environment", "Dev"))
            .withSku(new ResourceSku().withName("Standard"))
            .withProperties(new ManagedGrafanaProperties().withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                .withZoneRedundancy(ZoneRedundancy.ENABLED)
                .withApiKey(ApiKey.ENABLED)
                .withDeterministicOutboundIP(DeterministicOutboundIP.ENABLED)
                .withGrafanaIntegrations(new GrafanaIntegrations().withAzureMonitorWorkspaceIntegrations(
                    Arrays.asList(new AzureMonitorWorkspaceIntegration().withAzureMonitorWorkspaceResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.monitor/accounts/myAzureMonitorWorkspace"))))
                .withEnterpriseConfigurations(new EnterpriseConfigurations().withMarketplacePlanId("myPlanId")
                    .withMarketplaceAutoRenew(MarketplaceAutoRenew.ENABLED))
                .withGrafanaConfigurations(new GrafanaConfigurations()
                    .withSmtp(new Smtp().withEnabled(true)
                        .withHost("smtp.sendemail.com:587")
                        .withUser("username")
                        .withPassword("fakeTokenPlaceholder")
                        .withFromAddress("test@sendemail.com")
                        .withFromName("emailsender")
                        .withStartTLSPolicy(StartTLSPolicy.OPPORTUNISTIC_START_TLS)
                        .withSkipVerify(true))
                    .withSnapshots(new Snapshots().withExternalEnabled(true))
                    .withUsers(new Users().withViewersCanEdit(true).withEditorsCanAdmin(true))
                    .withSecurity(new Security().withCsrfAlwaysCheck(false))
                    .withUnifiedAlertingScreenshots(new UnifiedAlertingScreenshots().withCaptureEnabled(false)))
                .withGrafanaPlugins(mapOf("sample-plugin-id", new GrafanaPlugin()))
                .withGrafanaMajorVersion("9"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### PrivateEndpointConnections_Approve

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Operations_List.json
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

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for ManagedPrivateEndpointModels List.
 */
public final class ManagedPrivateEndpointModelsListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/ManagedPrivateEndpoints_List.json
     */
    /**
     * Sample code: ManagedPrivateEndpoint_List.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointList(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedPrivateEndpointModels().list("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.resourcemanager.dashboard.models.ManagedPrivateEndpointModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedPrivateEndpointModels Update.
 */
public final class ManagedPrivateEndpointModelsUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/ManagedPrivateEndpoints_Patch.json
     */
    /**
     * Sample code: ManagedPrivateEndpoints_Patch.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void managedPrivateEndpointsPatch(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        ManagedPrivateEndpointModel resource = manager.managedPrivateEndpointModels()
            .getWithResponse("myResourceGroup", "myWorkspace", "myMPEName", com.azure.core.util.Context.NONE)
            .getValue();
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

### PrivateEndpointConnections_List

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.privateEndpointConnections()
            .delete("myResourceGroup", "myWorkspace", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for ManagedDashboards Delete.
 */
public final class ManagedDashboardsDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Dashboard_Delete.json
     */
    /**
     * Sample code: Dashboard_Delete.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void dashboardDelete(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedDashboards()
            .deleteByResourceGroupWithResponse("myResourceGroup", "myDashboard", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
/**
 * Samples for ManagedGrafanas ListByResourceGroup.
 */
public final class ManagedGrafanasListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/Grafana_ListByResourceGroup.json
     */
    /**
     * Sample code: Grafana_ListByResourceGroup.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void grafanaListByResourceGroup(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.managedGrafanas().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

