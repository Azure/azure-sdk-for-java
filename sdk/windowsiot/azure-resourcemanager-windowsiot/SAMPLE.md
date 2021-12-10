# Code snippets and samples


## Operations

- [List](#operations_list)

## Services

- [CheckDeviceServiceNameAvailability](#services_checkdeviceservicenameavailability)
- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [GetByResourceGroup](#services_getbyresourcegroup)
- [List](#services_list)
- [ListByResourceGroup](#services_listbyresourcegroup)
- [Update](#services_update)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/windowsiot/resource-manager/Microsoft.WindowsIoT/stable/2019-06-01/examples/OperationsList.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to WindowsiotManager.
     */
    public static void operationsList(com.azure.resourcemanager.windowsiot.WindowsiotManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Services_CheckDeviceServiceNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.windowsiot.models.DeviceServiceCheckNameAvailabilityParameters;

/** Samples for Services CheckDeviceServiceNameAvailability. */
public final class ServicesCheckDeviceServiceNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/windowsiot/resource-manager/Microsoft.WindowsIoT/stable/2019-06-01/examples/Service_CheckNameAvailability.json
     */
    /**
     * Sample code: Service_CheckNameAvailability.
     *
     * @param manager Entry point to WindowsiotManager.
     */
    public static void serviceCheckNameAvailability(com.azure.resourcemanager.windowsiot.WindowsiotManager manager) {
        manager
            .services()
            .checkDeviceServiceNameAvailabilityWithResponse(
                new DeviceServiceCheckNameAvailabilityParameters().withName("service3363"), Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
/** Samples for Services CreateOrUpdate. */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/windowsiot/resource-manager/Microsoft.WindowsIoT/stable/2019-06-01/examples/Service_Create.json
     */
    /**
     * Sample code: Service_Create.
     *
     * @param manager Entry point to WindowsiotManager.
     */
    public static void serviceCreate(com.azure.resourcemanager.windowsiot.WindowsiotManager manager) {
        manager
            .services()
            .define("service4445")
            .withRegion("East US")
            .withExistingResourceGroup("res9101")
            .withNotes("blah")
            .withQuantity(1000000L)
            .withBillingDomainName("a.b.c")
            .withAdminDomainName("d.e.f")
            .create();
    }
}
```

### Services_Delete

```java
import com.azure.core.util.Context;

/** Samples for Services Delete. */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/windowsiot/resource-manager/Microsoft.WindowsIoT/stable/2019-06-01/examples/Service_Delete.json
     */
    /**
     * Sample code: Service_Delete.
     *
     * @param manager Entry point to WindowsiotManager.
     */
    public static void serviceDelete(com.azure.resourcemanager.windowsiot.WindowsiotManager manager) {
        manager.services().deleteWithResponse("res4228", "service2434", Context.NONE);
    }
}
```

### Services_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Services GetByResourceGroup. */
public final class ServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/windowsiot/resource-manager/Microsoft.WindowsIoT/stable/2019-06-01/examples/Service_GetProperties.json
     */
    /**
     * Sample code: Services_GetProperties.
     *
     * @param manager Entry point to WindowsiotManager.
     */
    public static void servicesGetProperties(com.azure.resourcemanager.windowsiot.WindowsiotManager manager) {
        manager.services().getByResourceGroupWithResponse("res9407", "service8596", Context.NONE);
    }
}
```

### Services_List

```java
import com.azure.core.util.Context;

/** Samples for Services List. */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/windowsiot/resource-manager/Microsoft.WindowsIoT/stable/2019-06-01/examples/Service_List.json
     */
    /**
     * Sample code: Service_List.
     *
     * @param manager Entry point to WindowsiotManager.
     */
    public static void serviceList(com.azure.resourcemanager.windowsiot.WindowsiotManager manager) {
        manager.services().list(Context.NONE);
    }
}
```

### Services_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Services ListByResourceGroup. */
public final class ServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/windowsiot/resource-manager/Microsoft.WindowsIoT/stable/2019-06-01/examples/Service_ListByResourceGroup.json
     */
    /**
     * Sample code: Service_ListByResourceGroup.
     *
     * @param manager Entry point to WindowsiotManager.
     */
    public static void serviceListByResourceGroup(com.azure.resourcemanager.windowsiot.WindowsiotManager manager) {
        manager.services().listByResourceGroup("res6117", Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.windowsiot.models.DeviceService;

/** Samples for Services Update. */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/windowsiot/resource-manager/Microsoft.WindowsIoT/stable/2019-06-01/examples/Service_Update.json
     */
    /**
     * Sample code: Service_Update.
     *
     * @param manager Entry point to WindowsiotManager.
     */
    public static void serviceUpdate(com.azure.resourcemanager.windowsiot.WindowsiotManager manager) {
        DeviceService resource =
            manager.services().getByResourceGroupWithResponse("res9407", "service8596", Context.NONE).getValue();
        resource
            .update()
            .withNotes("blah")
            .withQuantity(1000000L)
            .withBillingDomainName("a.b.c")
            .withAdminDomainName("d.e.f")
            .apply();
    }
}
```

