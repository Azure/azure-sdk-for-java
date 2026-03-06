# Code snippets and samples


## AttestationProviders

- [Create](#attestationproviders_create)
- [Delete](#attestationproviders_delete)
- [GetByResourceGroup](#attestationproviders_getbyresourcegroup)
- [GetDefaultByLocation](#attestationproviders_getdefaultbylocation)
- [List](#attestationproviders_list)
- [ListByResourceGroup](#attestationproviders_listbyresourcegroup)
- [ListDefault](#attestationproviders_listdefault)
- [Update](#attestationproviders_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [ListByProvider](#privatelinkresources_listbyprovider)
### AttestationProviders_Create

```java
import com.azure.resourcemanager.attestation.models.AttestationServiceCreationSpecificParams;
import com.azure.resourcemanager.attestation.models.PublicNetworkAccessType;
import com.azure.resourcemanager.attestation.models.TpmAttestationAuthenticationType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AttestationProviders Create.
 */
public final class AttestationProvidersCreateSamples {
    /*
     * x-ms-original-file: 2021-06-01/Create_AttestationProvider.json
     */
    /**
     * Sample code: AttestationProviders_Create.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersCreate(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders()
            .define("myattestationprovider")
            .withRegion("East US")
            .withExistingResourceGroup("MyResourceGroup")
            .withProperties(
                new AttestationServiceCreationSpecificParams().withPublicNetworkAccess(PublicNetworkAccessType.ENABLED)
                    .withTpmAttestationAuthentication(TpmAttestationAuthenticationType.ENABLED))
            .withTags(mapOf("Property1", "Value1", "Property2", "Value2", "Property3", "Value3"))
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

### AttestationProviders_Delete

```java
/**
 * Samples for AttestationProviders Delete.
 */
public final class AttestationProvidersDeleteSamples {
    /*
     * x-ms-original-file: 2021-06-01/Delete_AttestationProvider.json
     */
    /**
     * Sample code: AttestationProviders_Delete.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersDelete(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders()
            .deleteByResourceGroupWithResponse("sample-resource-group", "myattestationprovider",
                com.azure.core.util.Context.NONE);
    }
}
```

### AttestationProviders_GetByResourceGroup

```java
/**
 * Samples for AttestationProviders GetByResourceGroup.
 */
public final class AttestationProvidersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2021-06-01/Get_AttestationProvider.json
     */
    /**
     * Sample code: AttestationProviders_Get.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersGet(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders()
            .getByResourceGroupWithResponse("MyResourceGroup", "myattestationprovider",
                com.azure.core.util.Context.NONE);
    }
}
```

### AttestationProviders_GetDefaultByLocation

```java
/**
 * Samples for AttestationProviders GetDefaultByLocation.
 */
public final class AttestationProvidersGetDefaultByLocationSamples {
    /*
     * x-ms-original-file: 2021-06-01/Get_DefaultProviderByLocation.json
     */
    /**
     * Sample code: AttestationProviders_GetDefaultWithLocation.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void
        attestationProvidersGetDefaultWithLocation(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders().getDefaultByLocationWithResponse("Central US", com.azure.core.util.Context.NONE);
    }
}
```

### AttestationProviders_List

```java
/**
 * Samples for AttestationProviders List.
 */
public final class AttestationProvidersListSamples {
    /*
     * x-ms-original-file: 2021-06-01/Get_AttestationProvidersList.json
     */
    /**
     * Sample code: AttestationProviders_List.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersList(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### AttestationProviders_ListByResourceGroup

```java
/**
 * Samples for AttestationProviders ListByResourceGroup.
 */
public final class AttestationProvidersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2021-06-01/Get_AttestationProvidersListByResourceGroup.json
     */
    /**
     * Sample code: AttestationProviders_ListByResourceGroup.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void
        attestationProvidersListByResourceGroup(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders().listByResourceGroupWithResponse("testrg1", com.azure.core.util.Context.NONE);
    }
}
```

### AttestationProviders_ListDefault

```java
/**
 * Samples for AttestationProviders ListDefault.
 */
public final class AttestationProvidersListDefaultSamples {
    /*
     * x-ms-original-file: 2021-06-01/Get_DefaultProviders.json
     */
    /**
     * Sample code: AttestationProviders_GetDefault.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void
        attestationProvidersGetDefault(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders().listDefaultWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### AttestationProviders_Update

```java
import com.azure.resourcemanager.attestation.models.AttestationProvider;
import com.azure.resourcemanager.attestation.models.AttestationServicePatchSpecificParams;
import com.azure.resourcemanager.attestation.models.PublicNetworkAccessType;
import com.azure.resourcemanager.attestation.models.TpmAttestationAuthenticationType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AttestationProviders Update.
 */
public final class AttestationProvidersUpdateSamples {
    /*
     * x-ms-original-file: 2021-06-01/Update_AttestationProvider.json
     */
    /**
     * Sample code: AttestationProviders_Update.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersUpdate(com.azure.resourcemanager.attestation.AttestationManager manager) {
        AttestationProvider resource = manager.attestationProviders()
            .getByResourceGroupWithResponse("MyResourceGroup", "myattestationprovider",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Property1", "Value1", "Property2", "Value2", "Property3", "Value3"))
            .withProperties(
                new AttestationServicePatchSpecificParams().withPublicNetworkAccess(PublicNetworkAccessType.DISABLED)
                    .withTpmAttestationAuthentication(TpmAttestationAuthenticationType.DISABLED))
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2021-06-01/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void operationsList(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.attestation.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.attestation.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections Create.
 */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: 2021-06-01/AttestationProviderPutPrivateEndpointConnection.json
     */
    /**
     * Sample code: AttestationProviderPutPrivateEndpointConnection.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProviderPutPrivateEndpointConnection(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.privateEndpointConnections()
            .define("{privateEndpointConnectionName}")
            .withExistingAttestationProvider("res7687", "sto9699")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
            .create();
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
     * x-ms-original-file: 2021-06-01/AttestationProviderDeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: AttestationProviderDeletePrivateEndpointConnection.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProviderDeletePrivateEndpointConnection(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.privateEndpointConnections()
            .deleteWithResponse("res6977", "sto2527", "{privateEndpointConnectionName}",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2021-06-01/AttestationProviderGetPrivateEndpointConnection.json
     */
    /**
     * Sample code: AttestationProviderGetPrivateEndpointConnection.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProviderGetPrivateEndpointConnection(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("res6977", "sto2527", "{privateEndpointConnectionName}", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2021-06-01/AttestationProviderListPrivateEndpointConnections.json
     */
    /**
     * Sample code: AttestationProviderListPrivateEndpointConnections.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProviderListPrivateEndpointConnections(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.privateEndpointConnections().list("res6977", "sto2527", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByProvider

```java
/**
 * Samples for PrivateLinkResources ListByProvider.
 */
public final class PrivateLinkResourcesListByProviderSamples {
    /*
     * x-ms-original-file: 2021-06-01/AttestationProviderListPrivateLinkResources.json
     */
    /**
     * Sample code: AttestationProviderListPrivateLinkResources.
     * 
     * @param manager Entry point to AttestationManager.
     */
    public static void
        attestationProviderListPrivateLinkResources(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.privateLinkResources()
            .listByProviderWithResponse("MyResourceGroup", "myattestationprovider", com.azure.core.util.Context.NONE);
    }
}
```

