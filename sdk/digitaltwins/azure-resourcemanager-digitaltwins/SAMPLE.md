# Code snippets and samples


## DigitalTwins

- [CheckNameAvailability](#digitaltwins_checknameavailability)
- [CreateOrUpdate](#digitaltwins_createorupdate)
- [Delete](#digitaltwins_delete)
- [GetByResourceGroup](#digitaltwins_getbyresourcegroup)
- [List](#digitaltwins_list)
- [ListByResourceGroup](#digitaltwins_listbyresourcegroup)
- [Update](#digitaltwins_update)

## DigitalTwinsEndpoint

- [CreateOrUpdate](#digitaltwinsendpoint_createorupdate)
- [Delete](#digitaltwinsendpoint_delete)
- [Get](#digitaltwinsendpoint_get)
- [List](#digitaltwinsendpoint_list)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)
### DigitalTwins_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.digitaltwins.models.CheckNameRequest;

/** Samples for DigitalTwins CheckNameAvailability. */
public final class DigitalTwinsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsCheckNameAvailability_example.json
     */
    /**
     * Sample code: Check name Availability.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager
            .digitalTwins()
            .checkNameAvailabilityWithResponse(
                "WestUS2", new CheckNameRequest().withName("myadtinstance"), Context.NONE);
    }
}
```

### DigitalTwins_CreateOrUpdate

```java
import com.azure.resourcemanager.digitaltwins.models.DigitalTwinsIdentity;
import com.azure.resourcemanager.digitaltwins.models.DigitalTwinsIdentityType;
import com.azure.resourcemanager.digitaltwins.models.PublicNetworkAccess;

/** Samples for DigitalTwins CreateOrUpdate. */
public final class DigitalTwinsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsPut_example.json
     */
    /**
     * Sample code: Put a DigitalTwinsInstance resource.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void putADigitalTwinsInstanceResource(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager
            .digitalTwins()
            .define("myDigitalTwinsService")
            .withRegion("WestUS2")
            .withExistingResourceGroup("resRg")
            .create();
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsPut_WithPublicNetworkAccess.json
     */
    /**
     * Sample code: Put a DigitalTwinsInstance resource with publicNetworkAccess property.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void putADigitalTwinsInstanceResourceWithPublicNetworkAccessProperty(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager
            .digitalTwins()
            .define("myDigitalTwinsService")
            .withRegion("WestUS2")
            .withExistingResourceGroup("resRg")
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .create();
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsPut_WithIdentity_example.json
     */
    /**
     * Sample code: Put a DigitalTwinsInstance resource with identity.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void putADigitalTwinsInstanceResourceWithIdentity(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager
            .digitalTwins()
            .define("myDigitalTwinsService")
            .withRegion("WestUS2")
            .withExistingResourceGroup("resRg")
            .withIdentity(new DigitalTwinsIdentity().withType(DigitalTwinsIdentityType.SYSTEM_ASSIGNED))
            .create();
    }
}
```

### DigitalTwins_Delete

```java
import com.azure.core.util.Context;

/** Samples for DigitalTwins Delete. */
public final class DigitalTwinsDeleteSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsDelete_example.json
     */
    /**
     * Sample code: Delete a DigitalTwinsInstance resource.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void deleteADigitalTwinsInstanceResource(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwins().delete("resRg", "myDigitalTwinsService", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsDelete_WithIdentity_example.json
     */
    /**
     * Sample code: Delete a DigitalTwinsInstance resource with identity.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void deleteADigitalTwinsInstanceResourceWithIdentity(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwins().delete("resRg", "myDigitalTwinsService", Context.NONE);
    }
}
```

### DigitalTwins_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DigitalTwins GetByResourceGroup. */
public final class DigitalTwinsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsGet_WithIdentity_example.json
     */
    /**
     * Sample code: Get a DigitalTwinsInstance resource with identity.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getADigitalTwinsInstanceResourceWithIdentity(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwins().getByResourceGroupWithResponse("resRg", "myDigitalTwinsService", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsGet_WithPrivateEndpointConnection_example.json
     */
    /**
     * Sample code: Get a DigitalTwinsInstance resource with a private endpoint connection.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getADigitalTwinsInstanceResourceWithAPrivateEndpointConnection(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwins().getByResourceGroupWithResponse("resRg", "myDigitalTwinsService", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsGet_example.json
     */
    /**
     * Sample code: Get a DigitalTwinsInstance resource.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getADigitalTwinsInstanceResource(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwins().getByResourceGroupWithResponse("resRg", "myDigitalTwinsService", Context.NONE);
    }
}
```

### DigitalTwins_List

```java
import com.azure.core.util.Context;

/** Samples for DigitalTwins List. */
public final class DigitalTwinsListSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsList_example.json
     */
    /**
     * Sample code: Get DigitalTwinsInstance resources by subscription.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getDigitalTwinsInstanceResourcesBySubscription(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwins().list(Context.NONE);
    }
}
```

### DigitalTwins_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DigitalTwins ListByResourceGroup. */
public final class DigitalTwinsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsListByResourceGroup_example.json
     */
    /**
     * Sample code: Get DigitalTwinsInstance resources by resource group.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getDigitalTwinsInstanceResourcesByResourceGroup(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwins().listByResourceGroup("resRg", Context.NONE);
    }
}
```

### DigitalTwins_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.digitaltwins.models.DigitalTwinsDescription;
import com.azure.resourcemanager.digitaltwins.models.DigitalTwinsIdentity;
import com.azure.resourcemanager.digitaltwins.models.DigitalTwinsIdentityType;
import com.azure.resourcemanager.digitaltwins.models.DigitalTwinsPatchProperties;
import com.azure.resourcemanager.digitaltwins.models.PublicNetworkAccess;
import java.util.HashMap;
import java.util.Map;

/** Samples for DigitalTwins Update. */
public final class DigitalTwinsUpdateSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsPatch_example.json
     */
    /**
     * Sample code: Patch a DigitalTwinsInstance resource.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void patchADigitalTwinsInstanceResource(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        DigitalTwinsDescription resource =
            manager
                .digitalTwins()
                .getByResourceGroupWithResponse("resRg", "myDigitalTwinsService", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("purpose", "dev")).apply();
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsPatch_WithPublicNetworkAccess.json
     */
    /**
     * Sample code: Patch a DigitalTwinsInstance resource with publicNetworkAccess property.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void patchADigitalTwinsInstanceResourceWithPublicNetworkAccessProperty(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        DigitalTwinsDescription resource =
            manager
                .digitalTwins()
                .getByResourceGroupWithResponse("resRg", "myDigitalTwinsService", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(new DigitalTwinsPatchProperties().withPublicNetworkAccess(PublicNetworkAccess.DISABLED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsPatch_WithIdentity_example.json
     */
    /**
     * Sample code: Patch a DigitalTwinsInstance resource with identity.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void patchADigitalTwinsInstanceResourceWithIdentity(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        DigitalTwinsDescription resource =
            manager
                .digitalTwins()
                .getByResourceGroupWithResponse("resRg", "myDigitalTwinsService", Context.NONE)
                .getValue();
        resource.update().withIdentity(new DigitalTwinsIdentity().withType(DigitalTwinsIdentityType.NONE)).apply();
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

### DigitalTwinsEndpoint_CreateOrUpdate

```java
import com.azure.resourcemanager.digitaltwins.models.AuthenticationType;
import com.azure.resourcemanager.digitaltwins.models.ServiceBus;

/** Samples for DigitalTwinsEndpoint CreateOrUpdate. */
public final class DigitalTwinsEndpointCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsEndpointPut_example.json
     */
    /**
     * Sample code: Put a DigitalTwinsInstance resource.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void putADigitalTwinsInstanceResource(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager
            .digitalTwinsEndpoints()
            .define("myServiceBus")
            .withExistingDigitalTwinsInstance("resRg", "myDigitalTwinsService")
            .withProperties(
                new ServiceBus()
                    .withAuthenticationType(AuthenticationType.KEY_BASED)
                    .withPrimaryConnectionString(
                        "Endpoint=sb://mysb.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=xyzxyzoX4=;EntityPath=abcabc")
                    .withSecondaryConnectionString(
                        "Endpoint=sb://mysb.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=xyzxyzoX4=;EntityPath=abcabc"))
            .create();
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsEndpointPut_WithIdentity_example.json
     */
    /**
     * Sample code: Put a DigitalTwinsInstance resource with identity.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void putADigitalTwinsInstanceResourceWithIdentity(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager
            .digitalTwinsEndpoints()
            .define("myServiceBus")
            .withExistingDigitalTwinsInstance("resRg", "myDigitalTwinsService")
            .withProperties(
                new ServiceBus()
                    .withAuthenticationType(AuthenticationType.IDENTITY_BASED)
                    .withEndpointUri("sb://mysb.servicebus.windows.net/")
                    .withEntityPath("mysbtopic"))
            .create();
    }
}
```

### DigitalTwinsEndpoint_Delete

```java
import com.azure.core.util.Context;

/** Samples for DigitalTwinsEndpoint Delete. */
public final class DigitalTwinsEndpointDeleteSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsEndpointDelete_example.json
     */
    /**
     * Sample code: Delete a DigitalTwinsInstance endpoint.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void deleteADigitalTwinsInstanceEndpoint(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwinsEndpoints().delete("resRg", "myDigitalTwinsService", "myendpoint", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsEndpointDelete_WithIdentity_example.json
     */
    /**
     * Sample code: Delete a DigitalTwinsInstance endpoint with identity.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void deleteADigitalTwinsInstanceEndpointWithIdentity(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwinsEndpoints().delete("resRg", "myDigitalTwinsService", "myendpoint", Context.NONE);
    }
}
```

### DigitalTwinsEndpoint_Get

```java
import com.azure.core.util.Context;

/** Samples for DigitalTwinsEndpoint Get. */
public final class DigitalTwinsEndpointGetSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsEndpointGet_example.json
     */
    /**
     * Sample code: Get a DigitalTwinsInstance endpoint.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getADigitalTwinsInstanceEndpoint(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwinsEndpoints().getWithResponse("resRg", "myDigitalTwinsService", "myServiceBus", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsEndpointGet_WithIdentity_example.json
     */
    /**
     * Sample code: Get a DigitalTwinsInstance endpoint with identity.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getADigitalTwinsInstanceEndpointWithIdentity(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwinsEndpoints().getWithResponse("resRg", "myDigitalTwinsService", "myServiceBus", Context.NONE);
    }
}
```

### DigitalTwinsEndpoint_List

```java
import com.azure.core.util.Context;

/** Samples for DigitalTwinsEndpoint List. */
public final class DigitalTwinsEndpointListSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsEndpointsGet_example.json
     */
    /**
     * Sample code: Get a DigitalTwinsInstance endpoints.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getADigitalTwinsInstanceEndpoints(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwinsEndpoints().list("resRg", "myDigitalTwinsService", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsEndpointsGet_WithIdentity_example.json
     */
    /**
     * Sample code: Get a DigitalTwinsInstance endpoints with identity.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getADigitalTwinsInstanceEndpointsWithIdentity(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.digitalTwinsEndpoints().list("resRg", "myDigitalTwinsService", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/DigitalTwinsOperationsList_example.json
     */
    /**
     * Sample code: Get available operations.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getAvailableOperations(com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.digitaltwins.models.ConnectionPropertiesPrivateLinkServiceConnectionState;
import com.azure.resourcemanager.digitaltwins.models.PrivateEndpointConnection;
import com.azure.resourcemanager.digitaltwins.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.digitaltwins.models.PrivateLinkServiceConnectionStatus;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/PrivateEndpointConnectionPut_example.json
     */
    /**
     * Sample code: Update the status of a private endpoint connection with the given name.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void updateTheStatusOfAPrivateEndpointConnectionWithTheGivenName(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        PrivateEndpointConnection resource =
            manager
                .privateEndpointConnections()
                .getWithResponse("resRg", "myDigitalTwinsService", "myPrivateConnection", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new PrivateEndpointConnectionProperties()
                    .withPrivateLinkServiceConnectionState(
                        new ConnectionPropertiesPrivateLinkServiceConnectionState()
                            .withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                            .withDescription("Approved by johndoe@company.com.")))
            .apply();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/PrivateEndpointConnectionDelete_example.json
     */
    /**
     * Sample code: Delete private endpoint connection with the specified name.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void deletePrivateEndpointConnectionWithTheSpecifiedName(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager
            .privateEndpointConnections()
            .delete("resRg", "myDigitalTwinsService", "myPrivateConnection", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/PrivateEndpointConnectionByConnectionName_example.json
     */
    /**
     * Sample code: Get private endpoint connection properties for the given private endpoint.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getPrivateEndpointConnectionPropertiesForTheGivenPrivateEndpoint(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("resRg", "myDigitalTwinsService", "myPrivateConnection", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/PrivateEndpointConnectionsList_example.json
     */
    /**
     * Sample code: List private endpoint connection properties.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void listPrivateEndpointConnectionProperties(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.privateEndpointConnections().listWithResponse("resRg", "myDigitalTwinsService", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/PrivateLinkResourcesByGroupId_example.json
     */
    /**
     * Sample code: Get the specified private link resource for the given Digital Twin.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void getTheSpecifiedPrivateLinkResourceForTheGivenDigitalTwin(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.privateLinkResources().getWithResponse("resRg", "myDigitalTwinsService", "subResource", Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/digitaltwins/resource-manager/Microsoft.DigitalTwins/stable/2020-12-01/examples/PrivateLinkResourcesList_example.json
     */
    /**
     * Sample code: List private link resources for given Digital Twin.
     *
     * @param manager Entry point to AzureDigitalTwinsManager.
     */
    public static void listPrivateLinkResourcesForGivenDigitalTwin(
        com.azure.resourcemanager.digitaltwins.AzureDigitalTwinsManager manager) {
        manager.privateLinkResources().listWithResponse("resRg", "myDigitalTwinsService", Context.NONE);
    }
}
```

