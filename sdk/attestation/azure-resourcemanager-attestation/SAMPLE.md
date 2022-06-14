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
### AttestationProviders_Create

```java
import com.azure.resourcemanager.attestation.models.AttestationServiceCreationSpecificParams;

/** Samples for AttestationProviders Create. */
public final class AttestationProvidersCreateSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/Create_AttestationProvider.json
     */
    /**
     * Sample code: AttestationProviders_Create.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersCreate(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager
            .attestationProviders()
            .define("myattestationprovider")
            .withRegion((String) null)
            .withExistingResourceGroup("MyResourceGroup")
            .withProperties((AttestationServiceCreationSpecificParams) null)
            .create();
    }
}
```

### AttestationProviders_Delete

```java
import com.azure.core.util.Context;

/** Samples for AttestationProviders Delete. */
public final class AttestationProvidersDeleteSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/Delete_AttestationProvider.json
     */
    /**
     * Sample code: AttestationProviders_Delete.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersDelete(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager
            .attestationProviders()
            .deleteWithResponse("sample-resource-group", "myattestationprovider", Context.NONE);
    }
}
```

### AttestationProviders_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AttestationProviders GetByResourceGroup. */
public final class AttestationProvidersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/Get_AttestationProvider.json
     */
    /**
     * Sample code: AttestationProviders_Get.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersGet(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager
            .attestationProviders()
            .getByResourceGroupWithResponse("MyResourceGroup", "myattestationprovider", Context.NONE);
    }
}
```

### AttestationProviders_GetDefaultByLocation

```java
import com.azure.core.util.Context;

/** Samples for AttestationProviders GetDefaultByLocation. */
public final class AttestationProvidersGetDefaultByLocationSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/Get_DefaultProviderByLocation.json
     */
    /**
     * Sample code: AttestationProviders_GetDefaultWithLocation.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersGetDefaultWithLocation(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders().getDefaultByLocationWithResponse("Central US", Context.NONE);
    }
}
```

### AttestationProviders_List

```java
import com.azure.core.util.Context;

/** Samples for AttestationProviders List. */
public final class AttestationProvidersListSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/Get_AttestationProvidersList.json
     */
    /**
     * Sample code: AttestationProviders_List.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersList(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders().listWithResponse(Context.NONE);
    }
}
```

### AttestationProviders_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AttestationProviders ListByResourceGroup. */
public final class AttestationProvidersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/Get_AttestationProvidersListByResourceGroup.json
     */
    /**
     * Sample code: AttestationProviders_ListByResourceGroup.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersListByResourceGroup(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders().listByResourceGroupWithResponse("testrg1", Context.NONE);
    }
}
```

### AttestationProviders_ListDefault

```java
import com.azure.core.util.Context;

/** Samples for AttestationProviders ListDefault. */
public final class AttestationProvidersListDefaultSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/Get_DefaultProviders.json
     */
    /**
     * Sample code: AttestationProviders_GetDefault.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersGetDefault(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.attestationProviders().listDefaultWithResponse(Context.NONE);
    }
}
```

### AttestationProviders_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.attestation.models.AttestationProvider;
import java.util.HashMap;
import java.util.Map;

/** Samples for AttestationProviders Update. */
public final class AttestationProvidersUpdateSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/Update_AttestationProvider.json
     */
    /**
     * Sample code: AttestationProviders_Update.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProvidersUpdate(com.azure.resourcemanager.attestation.AttestationManager manager) {
        AttestationProvider resource =
            manager
                .attestationProviders()
                .getByResourceGroupWithResponse("MyResourceGroup", "myattestationprovider", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("Property1", "Value1", "Property2", "Value2", "Property3", "Value3")).apply();
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
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void operationsList(com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.attestation.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.attestation.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections Create. */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/AttestationProviderPutPrivateEndpointConnection.json
     */
    /**
     * Sample code: AttestationProviderPutPrivateEndpointConnection.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProviderPutPrivateEndpointConnection(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager
            .privateEndpointConnections()
            .define("{privateEndpointConnectionName}")
            .withExistingAttestationProvider("res7687", "sto9699")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
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
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/AttestationProviderDeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: AttestationProviderDeletePrivateEndpointConnection.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProviderDeletePrivateEndpointConnection(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager
            .privateEndpointConnections()
            .deleteWithResponse("res6977", "sto2527", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/AttestationProviderGetPrivateEndpointConnection.json
     */
    /**
     * Sample code: AttestationProviderGetPrivateEndpointConnection.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProviderGetPrivateEndpointConnection(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("res6977", "sto2527", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/attestation/resource-manager/Microsoft.Attestation/stable/2020-10-01/examples/AttestationProviderListPrivateEndpointConnections.json
     */
    /**
     * Sample code: AttestationProviderListPrivateEndpointConnections.
     *
     * @param manager Entry point to AttestationManager.
     */
    public static void attestationProviderListPrivateEndpointConnections(
        com.azure.resourcemanager.attestation.AttestationManager manager) {
        manager.privateEndpointConnections().list("res6977", "sto2527", Context.NONE);
    }
}
```

