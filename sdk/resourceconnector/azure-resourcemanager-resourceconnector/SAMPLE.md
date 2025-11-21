# Code snippets and samples


## Appliances

- [CreateOrUpdate](#appliances_createorupdate)
- [Delete](#appliances_delete)
- [GetByResourceGroup](#appliances_getbyresourcegroup)
- [GetTelemetryConfig](#appliances_gettelemetryconfig)
- [GetUpgradeGraph](#appliances_getupgradegraph)
- [List](#appliances_list)
- [ListByResourceGroup](#appliances_listbyresourcegroup)
- [ListClusterUserCredential](#appliances_listclusterusercredential)
- [ListKeys](#appliances_listkeys)
- [ListOperations](#appliances_listoperations)
- [Update](#appliances_update)
### Appliances_CreateOrUpdate

```java
import com.azure.resourcemanager.resourceconnector.models.AppliancePropertiesInfrastructureConfig;
import com.azure.resourcemanager.resourceconnector.models.Distro;
import com.azure.resourcemanager.resourceconnector.models.NetworkProfile;
import com.azure.resourcemanager.resourceconnector.models.Provider;
import com.azure.resourcemanager.resourceconnector.models.ProxyConfiguration;

/**
 * Samples for Appliances CreateOrUpdate.
 */
public final class AppliancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesCreate_Update.json
     */
    /**
     * Sample code: Create/Update Appliance.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void
        createUpdateAppliance(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances()
            .define("appliance01")
            .withRegion("West US")
            .withExistingResourceGroup("testresourcegroup")
            .withDistro(Distro.AKSEDGE)
            .withInfrastructureConfig(new AppliancePropertiesInfrastructureConfig().withProvider(Provider.VMWARE))
            .create();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesUpdateProxy.json
     */
    /**
     * Sample code: Update Appliance Proxy Configuration.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void updateApplianceProxyConfiguration(
        com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances()
            .define("appliance01")
            .withRegion("West US")
            .withExistingResourceGroup("testresourcegroup")
            .withDistro(Distro.AKSEDGE)
            .withInfrastructureConfig(new AppliancePropertiesInfrastructureConfig().withProvider(Provider.VMWARE))
            .withPublicKey("xxxxxxxx")
            .withNetworkProfile(
                new NetworkProfile().withProxyConfiguration(new ProxyConfiguration().withVersion("latest")))
            .create();
    }
}
```

### Appliances_Delete

```java
/**
 * Samples for Appliances Delete.
 */
public final class AppliancesDeleteSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesDelete.json
     */
    /**
     * Sample code: Delete Appliance.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void deleteAppliance(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances().delete("testresourcegroup", "appliance01", com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_GetByResourceGroup

```java
/**
 * Samples for Appliances GetByResourceGroup.
 */
public final class AppliancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesGet.json
     */
    /**
     * Sample code: Get Appliance.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void getAppliance(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances()
            .getByResourceGroupWithResponse("testresourcegroup", "appliance01", com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_GetTelemetryConfig

```java
/**
 * Samples for Appliances GetTelemetryConfig.
 */
public final class AppliancesGetTelemetryConfigSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TelemetryConfig.json
     */
    /**
     * Sample code: GetTelemetryConfig Appliance.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void
        getTelemetryConfigAppliance(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances().getTelemetryConfigWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_GetUpgradeGraph

```java
/**
 * Samples for Appliances GetUpgradeGraph.
 */
public final class AppliancesGetUpgradeGraphSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/UpgradeGraph.json
     */
    /**
     * Sample code: Get Appliance Upgrade Graph.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void
        getApplianceUpgradeGraph(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances()
            .getUpgradeGraphWithResponse("testresourcegroup", "appliance01", "stable",
                com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_List

```java
/**
 * Samples for Appliances List.
 */
public final class AppliancesListSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesListBySubscription.json
     */
    /**
     * Sample code: List Appliances by subscription.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void
        listAppliancesBySubscription(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances().list(com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_ListByResourceGroup

```java
/**
 * Samples for Appliances ListByResourceGroup.
 */
public final class AppliancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesListByResourceGroup.json
     */
    /**
     * Sample code: List Appliances by resource group.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void
        listAppliancesByResourceGroup(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances().listByResourceGroup("testresourcegroup", com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_ListClusterUserCredential

```java
/**
 * Samples for Appliances ListClusterUserCredential.
 */
public final class AppliancesListClusterUserCredentialSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesListClusterUserCredential.json
     */
    /**
     * Sample code: ListClusterUserCredentialAppliance.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void listClusterUserCredentialAppliance(
        com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances()
            .listClusterUserCredentialWithResponse("testresourcegroup", "appliance01",
                com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_ListKeys

```java
/**
 * Samples for Appliances ListKeys.
 */
public final class AppliancesListKeysSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesListKeys.json
     */
    /**
     * Sample code: ListKeys Appliance.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void listKeysAppliance(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances()
            .listKeysWithResponse("testresourcegroup", "appliance01", null, com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_ListOperations

```java
/**
 * Samples for Appliances ListOperations.
 */
public final class AppliancesListOperationsSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesListOperations.json
     */
    /**
     * Sample code: List Appliances operations.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void
        listAppliancesOperations(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        manager.appliances().listOperations(com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_Update

```java
import com.azure.resourcemanager.resourceconnector.models.Appliance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Appliances Update.
 */
public final class AppliancesUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AppliancesPatch.json
     */
    /**
     * Sample code: Update Appliance.
     * 
     * @param manager Entry point to ResourceConnectorManager.
     */
    public static void updateAppliance(com.azure.resourcemanager.resourceconnector.ResourceConnectorManager manager) {
        Appliance resource = manager.appliances()
            .getByResourceGroupWithResponse("testresourcegroup", "appliance01", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key", "fakeTokenPlaceholder")).apply();
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

