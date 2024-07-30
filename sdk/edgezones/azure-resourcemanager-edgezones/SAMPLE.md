# Code snippets and samples


## ExtendedZones

- [Get](#extendedzones_get)
- [List](#extendedzones_list)
- [Register](#extendedzones_register)
- [Unregister](#extendedzones_unregister)

## Operations

- [List](#operations_list)
### ExtendedZones_Get

```java
/**
 * Samples for ExtendedZones Get.
 */
public final class ExtendedZonesGetSamples {
    /*
     * x-ms-original-file: specification/edgezones/resource-manager/Microsoft.EdgeZones/preview/2024-04-01-preview/examples/ExtendedZones_Get.json
     */
    /**
     * Sample code: GetExtendedZone.
     * 
     * @param manager Entry point to EdgeZonesManager.
     */
    public static void getExtendedZone(com.azure.resourcemanager.edgezones.EdgeZonesManager manager) {
        manager.extendedZones().getWithResponse("losangeles", com.azure.core.util.Context.NONE);
    }
}
```

### ExtendedZones_List

```java
/**
 * Samples for ExtendedZones List.
 */
public final class ExtendedZonesListSamples {
    /*
     * x-ms-original-file: specification/edgezones/resource-manager/Microsoft.EdgeZones/preview/2024-04-01-preview/examples/ExtendedZones_ListBySubscription.json
     */
    /**
     * Sample code: ListExtendedZones.
     * 
     * @param manager Entry point to EdgeZonesManager.
     */
    public static void listExtendedZones(com.azure.resourcemanager.edgezones.EdgeZonesManager manager) {
        manager.extendedZones().list(com.azure.core.util.Context.NONE);
    }
}
```

### ExtendedZones_Register

```java
/**
 * Samples for ExtendedZones Register.
 */
public final class ExtendedZonesRegisterSamples {
    /*
     * x-ms-original-file: specification/edgezones/resource-manager/Microsoft.EdgeZones/preview/2024-04-01-preview/examples/ExtendedZones_Register.json
     */
    /**
     * Sample code: RegisterExtendedZone.
     * 
     * @param manager Entry point to EdgeZonesManager.
     */
    public static void registerExtendedZone(com.azure.resourcemanager.edgezones.EdgeZonesManager manager) {
        manager.extendedZones().registerWithResponse("losangeles", com.azure.core.util.Context.NONE);
    }
}
```

### ExtendedZones_Unregister

```java
/**
 * Samples for ExtendedZones Unregister.
 */
public final class ExtendedZonesUnregisterSamples {
    /*
     * x-ms-original-file: specification/edgezones/resource-manager/Microsoft.EdgeZones/preview/2024-04-01-preview/examples/ExtendedZones_Unregister.json
     */
    /**
     * Sample code: UnregisterExtendedZone.
     * 
     * @param manager Entry point to EdgeZonesManager.
     */
    public static void unregisterExtendedZone(com.azure.resourcemanager.edgezones.EdgeZonesManager manager) {
        manager.extendedZones().unregisterWithResponse("losangeles", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/edgezones/resource-manager/Microsoft.EdgeZones/preview/2024-04-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: ListOperations.
     * 
     * @param manager Entry point to EdgeZonesManager.
     */
    public static void listOperations(com.azure.resourcemanager.edgezones.EdgeZonesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

