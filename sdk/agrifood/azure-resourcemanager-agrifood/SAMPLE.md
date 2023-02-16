# Code snippets and samples


## Extensions

- [Create](#extensions_create)
- [Delete](#extensions_delete)
- [Get](#extensions_get)
- [ListByFarmBeats](#extensions_listbyfarmbeats)
- [Update](#extensions_update)

## FarmBeatsExtensions

- [Get](#farmbeatsextensions_get)
- [List](#farmbeatsextensions_list)

## FarmBeatsModels

- [CreateOrUpdate](#farmbeatsmodels_createorupdate)
- [Delete](#farmbeatsmodels_delete)
- [GetByResourceGroup](#farmbeatsmodels_getbyresourcegroup)
- [GetOperationResult](#farmbeatsmodels_getoperationresult)
- [List](#farmbeatsmodels_list)
- [ListByResourceGroup](#farmbeatsmodels_listbyresourcegroup)
- [Update](#farmbeatsmodels_update)

## Locations

- [CheckNameAvailability](#locations_checknameavailability)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByResource](#privateendpointconnections_listbyresource)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByResource](#privatelinkresources_listbyresource)
### Extensions_Create

```java
import com.azure.core.util.Context;

/** Samples for Extensions Create. */
public final class ExtensionsCreateSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/Extensions_Create.json
     */
    /**
     * Sample code: Extensions_Create.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void extensionsCreate(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .extensions()
            .createWithResponse("examples-rg", "examples-farmbeatsResourceName", "provider.extension", Context.NONE);
    }
}
```

### Extensions_Delete

```java
import com.azure.core.util.Context;

/** Samples for Extensions Delete. */
public final class ExtensionsDeleteSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/Extensions_Delete.json
     */
    /**
     * Sample code: Extensions_Delete.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void extensionsDelete(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .extensions()
            .deleteWithResponse("examples-rg", "examples-farmbeatsResourceName", "provider.extension", Context.NONE);
    }
}
```

### Extensions_Get

```java
import com.azure.core.util.Context;

/** Samples for Extensions Get. */
public final class ExtensionsGetSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/Extensions_Get.json
     */
    /**
     * Sample code: Extensions_Get.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void extensionsGet(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .extensions()
            .getWithResponse("examples-rg", "examples-farmbeatsResourceName", "provider.extension", Context.NONE);
    }
}
```

### Extensions_ListByFarmBeats

```java
import com.azure.core.util.Context;

/** Samples for Extensions ListByFarmBeats. */
public final class ExtensionsListByFarmBeatsSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/Extensions_ListByFarmBeats.json
     */
    /**
     * Sample code: Extensions_ListByFarmBeats.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void extensionsListByFarmBeats(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .extensions()
            .listByFarmBeats("examples-rg", "examples-farmbeatsResourceName", null, null, null, null, Context.NONE);
    }
}
```

### Extensions_Update

```java
import com.azure.core.util.Context;

/** Samples for Extensions Update. */
public final class ExtensionsUpdateSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/Extensions_Update.json
     */
    /**
     * Sample code: Extensions_Update.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void extensionsUpdate(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .extensions()
            .updateWithResponse("examples-rg", "examples-farmbeatsResourceName", "provider.extension", Context.NONE);
    }
}
```

### FarmBeatsExtensions_Get

```java
import com.azure.core.util.Context;

/** Samples for FarmBeatsExtensions Get. */
public final class FarmBeatsExtensionsGetSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsExtensions_Get.json
     */
    /**
     * Sample code: FarmBeatsExtensions_Get.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsExtensionsGet(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager.farmBeatsExtensions().getWithResponse("DTN.ContentServices", Context.NONE);
    }
}
```

### FarmBeatsExtensions_List

```java
import com.azure.core.util.Context;

/** Samples for FarmBeatsExtensions List. */
public final class FarmBeatsExtensionsListSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsExtensions_List.json
     */
    /**
     * Sample code: FarmBeatsExtensions_List.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsExtensionsList(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager.farmBeatsExtensions().list(null, null, null, null, null, Context.NONE);
    }
}
```

### FarmBeatsModels_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for FarmBeatsModels CreateOrUpdate. */
public final class FarmBeatsModelsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsModels_CreateOrUpdate.json
     */
    /**
     * Sample code: FarmBeatsModels_CreateOrUpdate.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsModelsCreateOrUpdate(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .farmBeatsModels()
            .define("examples-farmbeatsResourceName")
            .withRegion("eastus2")
            .withExistingResourceGroup("examples-rg")
            .withTags(mapOf("key1", "value1", "key2", "value2"))
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

### FarmBeatsModels_Delete

```java
import com.azure.core.util.Context;

/** Samples for FarmBeatsModels Delete. */
public final class FarmBeatsModelsDeleteSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsModels_Delete.json
     */
    /**
     * Sample code: FarmBeatsModels_Delete.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsModelsDelete(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager.farmBeatsModels().deleteWithResponse("examples-rg", "examples-farmBeatsResourceName", Context.NONE);
    }
}
```

### FarmBeatsModels_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for FarmBeatsModels GetByResourceGroup. */
public final class FarmBeatsModelsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsModels_Get.json
     */
    /**
     * Sample code: FarmBeatsModels_Get.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsModelsGet(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .farmBeatsModels()
            .getByResourceGroupWithResponse("examples-rg", "examples-farmBeatsResourceName", Context.NONE);
    }
}
```

### FarmBeatsModels_GetOperationResult

```java
import com.azure.core.util.Context;

/** Samples for FarmBeatsModels GetOperationResult. */
public final class FarmBeatsModelsGetOperationResultSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsModels_GetOperationResult.json
     */
    /**
     * Sample code: FarmBeatsModels_GetOperationResult.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsModelsGetOperationResult(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .farmBeatsModels()
            .getOperationResultWithResponse(
                "examples-rg",
                "examples-farmBeatsResourceName",
                "resource-provisioning-id-farmBeatsResourceName",
                Context.NONE);
    }
}
```

### FarmBeatsModels_List

```java
import com.azure.core.util.Context;

/** Samples for FarmBeatsModels List. */
public final class FarmBeatsModelsListSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsModels_ListBySubscription.json
     */
    /**
     * Sample code: FarmBeatsModels_ListBySubscription.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsModelsListBySubscription(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager.farmBeatsModels().list(null, null, Context.NONE);
    }
}
```

### FarmBeatsModels_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for FarmBeatsModels ListByResourceGroup. */
public final class FarmBeatsModelsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsModels_ListByResourceGroup.json
     */
    /**
     * Sample code: FarmBeatsModels_ListByResourceGroup.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsModelsListByResourceGroup(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager.farmBeatsModels().listByResourceGroup("examples-rg", null, null, Context.NONE);
    }
}
```

### FarmBeatsModels_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.agrifood.models.FarmBeats;
import com.azure.resourcemanager.agrifood.models.FarmBeatsUpdateProperties;
import com.azure.resourcemanager.agrifood.models.Identity;
import com.azure.resourcemanager.agrifood.models.ResourceIdentityType;
import com.azure.resourcemanager.agrifood.models.SensorIntegration;
import java.util.HashMap;
import java.util.Map;

/** Samples for FarmBeatsModels Update. */
public final class FarmBeatsModelsUpdateSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsModels_Update.json
     */
    /**
     * Sample code: FarmBeatsModels_Update.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsModelsUpdate(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        FarmBeats resource =
            manager
                .farmBeatsModels()
                .getByResourceGroupWithResponse("examples-rg", "examples-farmBeatsResourceName", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1", "key2", "value2")).apply();
    }

    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/FarmBeatsModels_UpdateWithSensor.json
     */
    /**
     * Sample code: FarmBeatsModels_UpdateWithSensor.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void farmBeatsModelsUpdateWithSensor(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        FarmBeats resource =
            manager
                .farmBeatsModels()
                .getByResourceGroupWithResponse("examples-rg", "examples-farmBeatsResourceName", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withProperties(
                new FarmBeatsUpdateProperties().withSensorIntegration(new SensorIntegration().withEnabled("True")))
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

### Locations_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.agrifood.models.CheckNameAvailabilityRequest;

/** Samples for Locations CheckNameAvailability. */
public final class LocationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/Locations_CheckNameAvailability_Available.json
     */
    /**
     * Sample code: Locations_CheckNameAvailability_Available.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void locationsCheckNameAvailabilityAvailable(
        com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .locations()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest()
                    .withName("newaccountname")
                    .withType("Microsoft.AgFoodPlatform/farmBeats"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/Locations_CheckNameAvailability_AlreadyExists.json
     */
    /**
     * Sample code: Locations_CheckNameAvailability_AlreadyExists.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void locationsCheckNameAvailabilityAlreadyExists(
        com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .locations()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest()
                    .withName("existingaccountname")
                    .withType("Microsoft.AgFoodPlatform/farmBeats"),
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void operationsList(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.agrifood.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.agrifood.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/PrivateEndpointConnections_CreateOrUpdate.json
     */
    /**
     * Sample code: PrivateEndpointConnections_CreateOrUpdate.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void privateEndpointConnectionsCreateOrUpdate(
        com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .privateEndpointConnections()
            .define("privateEndpointConnectionName")
            .withExistingFarmBeat("examples-rg", "examples-farmbeatsResourceName")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Approved by johndoe@contoso.com"))
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
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .privateEndpointConnections()
            .delete("examples-rg", "examples-farmbeatsResourceName", "privateEndpointConnectionName", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "examples-rg", "examples-farmbeatsResourceName", "privateEndpointConnectionName", Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByResource

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections ListByResource. */
public final class PrivateEndpointConnectionsListByResourceSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/PrivateEndpointConnections_ListByResource.json
     */
    /**
     * Sample code: PrivateEndpointConnections_ListByResource.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void privateEndpointConnectionsListByResource(
        com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .privateEndpointConnections()
            .listByResource("examples-rg", "examples-farmbeatsResourceName", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: PrivateLinkResources_Get.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void privateLinkResourcesGet(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager
            .privateLinkResources()
            .getWithResponse("examples-rg", "examples-farmbeatsResourceName", "farmbeats", Context.NONE);
    }
}
```

### PrivateLinkResources_ListByResource

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByResource. */
public final class PrivateLinkResourcesListByResourceSamples {
    /*
     * x-ms-original-file: specification/agrifood/resource-manager/Microsoft.AgFoodPlatform/preview/2021-09-01-preview/examples/PrivateLinkResources_ListByResource.json
     */
    /**
     * Sample code: PrivateLinkResources_ListByResource.
     *
     * @param manager Entry point to AgriFoodManager.
     */
    public static void privateLinkResourcesListByResource(com.azure.resourcemanager.agrifood.AgriFoodManager manager) {
        manager.privateLinkResources().listByResource("examples-rg", "examples-farmbeatsResourceName", Context.NONE);
    }
}
```

