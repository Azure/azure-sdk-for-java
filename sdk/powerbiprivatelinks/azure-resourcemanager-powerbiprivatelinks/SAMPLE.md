# Code snippets and samples


## Operations

- [List](#operations_list)

## PowerBIResources

- [Create](#powerbiresources_create)
- [Delete](#powerbiresources_delete)
- [GetByResourceGroup](#powerbiresources_getbyresourcegroup)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByResource](#privateendpointconnections_listbyresource)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByResource](#privatelinkresources_listbyresource)

## PrivateLinkServiceResourceOperationResults

- [Get](#privatelinkserviceresourceoperationresults_get)

## PrivateLinkServices

- [ListByResourceGroup](#privatelinkservices_listbyresourcegroup)

## PrivateLinkServicesForPowerBI

- [ListBySubscriptionId](#privatelinkservicesforpowerbi_listbysubscriptionid)
### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/Operations_List.json
     */
    /**
     * Sample code: Lists all of the available Power BI RP operations.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void listsAllOfTheAvailablePowerBIRPOperations(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PowerBIResources_Create

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for PowerBIResources Create. */
public final class PowerBIResourcesCreateSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PowerBIResources_Create.json
     */
    /**
     * Sample code: Creates or updates private link service resource.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void createsOrUpdatesPrivateLinkServiceResource(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .powerBIResources()
            .define("azureResourceName")
            .withRegion("global")
            .withExistingResourceGroup("resourceGroup")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withTenantId("ac2bc297-8a3e-46f3-972d-87c2b4ae6e2f")
            .withClientTenantId("ac2bc297-8a3e-46f3-972d-87c2b4ae6e2f")
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

### PowerBIResources_Delete

```java
/** Samples for PowerBIResources Delete. */
public final class PowerBIResourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PowerBIResources_Delete.json
     */
    /**
     * Sample code: Deletes private link service resource.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void deletesPrivateLinkServiceResource(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .powerBIResources()
            .deleteByResourceGroupWithResponse("resourceGroup", "azureResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### PowerBIResources_GetByResourceGroup

```java
/** Samples for PowerBIResources GetByResourceGroup. */
public final class PowerBIResourcesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PowerBIResources_ListByResourceName.json
     */
    /**
     * Sample code: List private link resources in a Azure resource.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void listPrivateLinkResourcesInAAzureResource(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .powerBIResources()
            .getByResourceGroupWithResponse("resourceGroup", "azureResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.powerbiprivatelinks.models.ConnectionState;
import com.azure.resourcemanager.powerbiprivatelinks.models.PersistedConnectionStatus;
import com.azure.resourcemanager.powerbiprivatelinks.models.PrivateEndpoint;

/** Samples for PrivateEndpointConnections Create. */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PrivateEndpointConnections_Create.json
     */
    /**
     * Sample code: Updates status of private endpoint connection.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void updatesStatusOfPrivateEndpointConnection(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .privateEndpointConnections()
            .define("myPrivateEndpointName")
            .withExistingPrivateLinkServicesForPowerBI("resourceGroup", "azureResourceName")
            .withPrivateEndpoint(
                new PrivateEndpoint()
                    .withId(
                        "/subscriptions/a0020869-4d28-422a-89f4-c2413130d73c/resourceGroups/resourceGroup/providers/Microsoft.Network/privateEndpoints/myPrivateEndpointName"))
            .withPrivateLinkServiceConnectionState(
                new ConnectionState()
                    .withStatus(PersistedConnectionStatus.fromString("Approved "))
                    .withDescription("")
                    .withActionsRequired("None"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: Deletes private endpoint connection.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void deletesPrivateEndpointConnection(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .privateEndpointConnections()
            .delete("resourceGroup", "azureResourceName", "myPrivateEndpointName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "resourceGroup", "azureResourceName", "myPrivateEndpointName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByResource

```java
/** Samples for PrivateEndpointConnections ListByResource. */
public final class PrivateEndpointConnectionsListByResourceSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PrivateEndpointConnections_ListByResource.json
     */
    /**
     * Sample code: Gets private endpoint connections.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void getsPrivateEndpointConnections(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .privateEndpointConnections()
            .listByResource("resourceGroup", "azureResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: Gets a private link resource.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void getsAPrivateLinkResource(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .privateLinkResources()
            .getWithResponse("resourceGroup", "azureResourceName", "tenant", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByResource

```java
/** Samples for PrivateLinkResources ListByResource. */
public final class PrivateLinkResourcesListByResourceSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PrivateLinkResources_ListByResource.json
     */
    /**
     * Sample code: Gets private link resources in a Azure resource.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void getsPrivateLinkResourcesInAAzureResource(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .privateLinkResources()
            .listByResource("resourceGroup", "azureResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkServiceResourceOperationResults_Get

```java
/** Samples for PrivateLinkServiceResourceOperationResults Get. */
public final class PrivateLinkServiceResourceOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PrivateLinkServiceResourceOperationResults_Get.json
     */
    /**
     * Sample code: Result of operation on private link resources.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void resultOfOperationOnPrivateLinkResources(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .privateLinkServiceResourceOperationResults()
            .get("9a062a88-e463-4697-bef2-fe039df73a02", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkServices_ListByResourceGroup

```java
/** Samples for PrivateLinkServices ListByResourceGroup. */
public final class PrivateLinkServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PrivateLinkServices_ListByResourceGroup.json
     */
    /**
     * Sample code: List private link resources in a resource group.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void listPrivateLinkResourcesInAResourceGroup(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager
            .privateLinkServices()
            .listByResourceGroupWithResponse("resourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkServicesForPowerBI_ListBySubscriptionId

```java
/** Samples for PrivateLinkServicesForPowerBI ListBySubscriptionId. */
public final class PrivateLinkServicesForPowerBIListBySubscriptionIdSamples {
    /*
     * x-ms-original-file: specification/powerbiprivatelinks/resource-manager/Microsoft.PowerBI/stable/2020-06-01/examples/PrivateLinkServices_ListBySubscriptionId.json
     */
    /**
     * Sample code: List private link resources in a subscription.
     *
     * @param manager Entry point to PrivateLinkServicesForPowerBIManager.
     */
    public static void listPrivateLinkResourcesInASubscription(
        com.azure.resourcemanager.powerbiprivatelinks.PrivateLinkServicesForPowerBIManager manager) {
        manager.privateLinkServicesForPowerBIs().listBySubscriptionIdWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

