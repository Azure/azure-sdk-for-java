# Code snippets and samples


## EnergyServices

- [Create](#energyservices_create)
- [Delete](#energyservices_delete)
- [GetByResourceGroup](#energyservices_getbyresourcegroup)
- [List](#energyservices_list)
- [ListByResourceGroup](#energyservices_listbyresourcegroup)
- [Update](#energyservices_update)

## Locations

- [CheckNameAvailability](#locations_checknameavailability)

## Operations

- [List](#operations_list)
### EnergyServices_Create

```java
/** Samples for EnergyServices Create. */
public final class EnergyServicesCreateSamples {
    /*
     * x-ms-original-file: specification/oep/resource-manager/Microsoft.OpenEnergyPlatform/preview/2021-06-01-preview/examples/OepResource_Create.json
     */
    /**
     * Sample code: OepResource_Create.
     *
     * @param manager Entry point to OepManager.
     */
    public static void oepResourceCreate(com.azure.resourcemanager.oep.OepManager manager) {
        manager
            .energyServices()
            .define("DummyResourceName")
            .withRegion((String) null)
            .withExistingResourceGroup("DummyResourceGroupName")
            .create();
    }
}
```

### EnergyServices_Delete

```java
import com.azure.core.util.Context;

/** Samples for EnergyServices Delete. */
public final class EnergyServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/oep/resource-manager/Microsoft.OpenEnergyPlatform/preview/2021-06-01-preview/examples/OepResource_Delete.json
     */
    /**
     * Sample code: OepResource_Delete.
     *
     * @param manager Entry point to OepManager.
     */
    public static void oepResourceDelete(com.azure.resourcemanager.oep.OepManager manager) {
        manager.energyServices().delete("DummyResourceGroupName", "DummyResourceName", Context.NONE);
    }
}
```

### EnergyServices_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for EnergyServices GetByResourceGroup. */
public final class EnergyServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/oep/resource-manager/Microsoft.OpenEnergyPlatform/preview/2021-06-01-preview/examples/OepResource_Get.json
     */
    /**
     * Sample code: OepResource_Get.
     *
     * @param manager Entry point to OepManager.
     */
    public static void oepResourceGet(com.azure.resourcemanager.oep.OepManager manager) {
        manager
            .energyServices()
            .getByResourceGroupWithResponse("DummyResourceGroupName", "DummyResourceName", Context.NONE);
    }
}
```

### EnergyServices_List

```java
import com.azure.core.util.Context;

/** Samples for EnergyServices List. */
public final class EnergyServicesListSamples {
    /*
     * x-ms-original-file: specification/oep/resource-manager/Microsoft.OpenEnergyPlatform/preview/2021-06-01-preview/examples/OepResource_ListBySubscriptionId.json
     */
    /**
     * Sample code: OepResource_ListBySubscriptionId.
     *
     * @param manager Entry point to OepManager.
     */
    public static void oepResourceListBySubscriptionId(com.azure.resourcemanager.oep.OepManager manager) {
        manager.energyServices().list(Context.NONE);
    }
}
```

### EnergyServices_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for EnergyServices ListByResourceGroup. */
public final class EnergyServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/oep/resource-manager/Microsoft.OpenEnergyPlatform/preview/2021-06-01-preview/examples/OepResource_ListByResourceGroup.json
     */
    /**
     * Sample code: OepResource_ListByResourceGroup.
     *
     * @param manager Entry point to OepManager.
     */
    public static void oepResourceListByResourceGroup(com.azure.resourcemanager.oep.OepManager manager) {
        manager.energyServices().listByResourceGroup("DummyResourceGroupName", Context.NONE);
    }
}
```

### EnergyServices_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.oep.models.EnergyService;

/** Samples for EnergyServices Update. */
public final class EnergyServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/oep/resource-manager/Microsoft.OpenEnergyPlatform/preview/2021-06-01-preview/examples/OepResource_Update.json
     */
    /**
     * Sample code: OepResource_Update.
     *
     * @param manager Entry point to OepManager.
     */
    public static void oepResourceUpdate(com.azure.resourcemanager.oep.OepManager manager) {
        EnergyService resource =
            manager
                .energyServices()
                .getByResourceGroupWithResponse("DummyResourceGroupName", "DummyResourceName", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Locations_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.oep.models.CheckNameAvailabilityRequest;

/** Samples for Locations CheckNameAvailability. */
public final class LocationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/oep/resource-manager/Microsoft.OpenEnergyPlatform/preview/2021-06-01-preview/examples/Locations_CheckNameAvailability.json
     */
    /**
     * Sample code: Locations_CheckNameAvailability.
     *
     * @param manager Entry point to OepManager.
     */
    public static void locationsCheckNameAvailability(com.azure.resourcemanager.oep.OepManager manager) {
        manager
            .locations()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest().withName("sample-name").withType("Microsoft.OEP/oepResource"),
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
     * x-ms-original-file: specification/oep/resource-manager/Microsoft.OpenEnergyPlatform/preview/2021-06-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to OepManager.
     */
    public static void operationsList(com.azure.resourcemanager.oep.OepManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

