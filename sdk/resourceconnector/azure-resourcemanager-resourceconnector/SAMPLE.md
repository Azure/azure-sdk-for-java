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
import com.azure.resourcemanager.resourceconnector.models.Provider;

/** Samples for Appliances CreateOrUpdate. */
public final class AppliancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/AppliancesCreate_Update.json
     */
    /**
     * Sample code: Create/Update Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void createUpdateAppliance(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager
            .appliances()
            .define("appliance01")
            .withRegion("West US")
            .withExistingResourceGroup("testresourcegroup")
            .withDistro(Distro.AKSEDGE)
            .withInfrastructureConfig(new AppliancePropertiesInfrastructureConfig().withProvider(Provider.VMWARE))
            .create();
    }
}
```

### Appliances_Delete

```java
/** Samples for Appliances Delete. */
public final class AppliancesDeleteSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/AppliancesDelete.json
     */
    /**
     * Sample code: Delete Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void deleteAppliance(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().delete("testresourcegroup", "appliance01", com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_GetByResourceGroup

```java
/** Samples for Appliances GetByResourceGroup. */
public final class AppliancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/AppliancesGet.json
     */
    /**
     * Sample code: Get Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void getAppliance(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager
            .appliances()
            .getByResourceGroupWithResponse("testresourcegroup", "appliance01", com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_GetTelemetryConfig

```java
/** Samples for Appliances GetTelemetryConfig. */
public final class AppliancesGetTelemetryConfigSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/TelemetryConfig.json
     */
    /**
     * Sample code: GetTelemetryConfig Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void getTelemetryConfigAppliance(
        com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().getTelemetryConfigWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_GetUpgradeGraph

```java
/** Samples for Appliances GetUpgradeGraph. */
public final class AppliancesGetUpgradeGraphSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/UpgradeGraph.json
     */
    /**
     * Sample code: Get Appliance Upgrade Graph.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void getApplianceUpgradeGraph(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager
            .appliances()
            .getUpgradeGraphWithResponse(
                "testresourcegroup", "appliance01", "stable", com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_List

```java
/** Samples for Appliances List. */
public final class AppliancesListSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/AppliancesListBySubscription.json
     */
    /**
     * Sample code: List Appliances by subscription.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void listAppliancesBySubscription(
        com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().list(com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_ListByResourceGroup

```java
/** Samples for Appliances ListByResourceGroup. */
public final class AppliancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/AppliancesListByResourceGroup.json
     */
    /**
     * Sample code: List Appliances by resource group.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void listAppliancesByResourceGroup(
        com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().listByResourceGroup("testresourcegroup", com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_ListClusterUserCredential

```java
/** Samples for Appliances ListClusterUserCredential. */
public final class AppliancesListClusterUserCredentialSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/AppliancesListClusterUserCredential.json
     */
    /**
     * Sample code: ListClusterUserCredentialAppliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void listClusterUserCredentialAppliance(
        com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager
            .appliances()
            .listClusterUserCredentialWithResponse(
                "testresourcegroup", "appliance01", com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_ListKeys

```java
/** Samples for Appliances ListKeys. */
public final class AppliancesListKeysSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/AppliancesListKeys.json
     */
    /**
     * Sample code: ListKeys Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void listKeysAppliance(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().listKeysWithResponse("testresourcegroup", "appliance01", com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_ListOperations

```java
/** Samples for Appliances ListOperations. */
public final class AppliancesListOperationsSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/AppliancesListOperations.json
     */
    /**
     * Sample code: List Appliances operations.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void listAppliancesOperations(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().listOperations(com.azure.core.util.Context.NONE);
    }
}
```

### Appliances_Update

```java
import com.azure.resourcemanager.resourceconnector.models.Appliance;
import java.util.HashMap;
import java.util.Map;

/** Samples for Appliances Update. */
public final class AppliancesUpdateSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/stable/2022-10-27/examples/AppliancesPatch.json
     */
    /**
     * Sample code: Update Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void updateAppliance(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        Appliance resource =
            manager
                .appliances()
                .getByResourceGroupWithResponse("testresourcegroup", "appliance01", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key", "value")).apply();
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

