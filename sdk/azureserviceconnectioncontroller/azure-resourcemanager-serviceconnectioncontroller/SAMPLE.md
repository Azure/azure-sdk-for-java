# Code snippets and samples


## AzureServiceConnectionControllers

- [CreateOrUpdate](#azureserviceconnectioncontrollers_createorupdate)
- [Delete](#azureserviceconnectioncontrollers_delete)
- [GetByResourceGroup](#azureserviceconnectioncontrollers_getbyresourcegroup)
- [List](#azureserviceconnectioncontrollers_list)
- [ListByResourceGroup](#azureserviceconnectioncontrollers_listbyresourcegroup)
- [Update](#azureserviceconnectioncontrollers_update)

## Operations

- [List](#operations_list)
### AzureServiceConnectionControllers_CreateOrUpdate

```java
import com.azure.resourcemanager.serviceconnectioncontroller.models.AzureServiceConnectionControllerProperties;
import com.azure.resourcemanager.serviceconnectioncontroller.models.IPAllocationMethod;
import com.azure.resourcemanager.serviceconnectioncontroller.models.IpConfiguration;
import com.azure.resourcemanager.serviceconnectioncontroller.models.ProviderConnectionProperties;
import java.util.Arrays;

/**
 * Samples for AzureServiceConnectionControllers CreateOrUpdate.
 */
public final class AzureServiceConnectionControllersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/AzureServiceConnectionControllers_CreateOrUpdate.json
     */
    /**
     * Sample code: AzureServiceConnectionControllers_CreateOrUpdate.
     * 
     * @param manager Entry point to AzureServiceConnectionControllerManager.
     */
    public static void azureServiceConnectionControllersCreateOrUpdate(
        com.azure.resourcemanager.serviceconnectioncontroller.AzureServiceConnectionControllerManager manager) {
        manager.azureServiceConnectionControllers()
            .define("pg1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg1")
            .withProperties(new AzureServiceConnectionControllerProperties().withSubnetId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1")
                .withIpConfigurations(Arrays.asList(new IpConfiguration().withPrivateIpV4Address("10.0.0.10")
                    .withIpAllocationMethod(IPAllocationMethod.STATIC)))
                .withProviderConnections(Arrays.asList(new ProviderConnectionProperties()
                    .withName("provider-connection")
                    .withSubnetId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet2")
                    .withIpConfiguration(new IpConfiguration().withPrivateIpV4Address("10.0.0.11")
                        .withIpAllocationMethod(IPAllocationMethod.STATIC))
                    .withPrivateEndpointResourceGroup(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/pe-rg1"))))
            .withManagedBy(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.Network/probingGateways/pg1")
            .create();
    }
}
```

### AzureServiceConnectionControllers_Delete

```java
/**
 * Samples for AzureServiceConnectionControllers Delete.
 */
public final class AzureServiceConnectionControllersDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/AzureServiceConnectionControllers_Delete.json
     */
    /**
     * Sample code: AzureServiceConnectionControllers_Delete.
     * 
     * @param manager Entry point to AzureServiceConnectionControllerManager.
     */
    public static void azureServiceConnectionControllersDelete(
        com.azure.resourcemanager.serviceconnectioncontroller.AzureServiceConnectionControllerManager manager) {
        manager.azureServiceConnectionControllers().delete("rg1", "pg1", com.azure.core.util.Context.NONE);
    }
}
```

### AzureServiceConnectionControllers_GetByResourceGroup

```java
/**
 * Samples for AzureServiceConnectionControllers GetByResourceGroup.
 */
public final class AzureServiceConnectionControllersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/AzureServiceConnectionControllers_Get.json
     */
    /**
     * Sample code: AzureServiceConnectionControllers_Get.
     * 
     * @param manager Entry point to AzureServiceConnectionControllerManager.
     */
    public static void azureServiceConnectionControllersGet(
        com.azure.resourcemanager.serviceconnectioncontroller.AzureServiceConnectionControllerManager manager) {
        manager.azureServiceConnectionControllers()
            .getByResourceGroupWithResponse("rg1", "pg1", com.azure.core.util.Context.NONE);
    }
}
```

### AzureServiceConnectionControllers_List

```java
/**
 * Samples for AzureServiceConnectionControllers List.
 */
public final class AzureServiceConnectionControllersListSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/AzureServiceConnectionControllers_ListBySubscription.json
     */
    /**
     * Sample code: AzureServiceConnectionControllers_ListBySubscription.
     * 
     * @param manager Entry point to AzureServiceConnectionControllerManager.
     */
    public static void azureServiceConnectionControllersListBySubscription(
        com.azure.resourcemanager.serviceconnectioncontroller.AzureServiceConnectionControllerManager manager) {
        manager.azureServiceConnectionControllers().list(com.azure.core.util.Context.NONE);
    }
}
```

### AzureServiceConnectionControllers_ListByResourceGroup

```java
/**
 * Samples for AzureServiceConnectionControllers ListByResourceGroup.
 */
public final class AzureServiceConnectionControllersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/AzureServiceConnectionControllers_ListByResourceGroup.json
     */
    /**
     * Sample code: AzureServiceConnectionControllers_ListByResourceGroup.
     * 
     * @param manager Entry point to AzureServiceConnectionControllerManager.
     */
    public static void azureServiceConnectionControllersListByResourceGroup(
        com.azure.resourcemanager.serviceconnectioncontroller.AzureServiceConnectionControllerManager manager) {
        manager.azureServiceConnectionControllers().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### AzureServiceConnectionControllers_Update

```java
import com.azure.resourcemanager.serviceconnectioncontroller.models.AzureServiceConnectionController;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AzureServiceConnectionControllers Update.
 */
public final class AzureServiceConnectionControllersUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/AzureServiceConnectionControllers_Update.json
     */
    /**
     * Sample code: AzureServiceConnectionControllers_Update.
     * 
     * @param manager Entry point to AzureServiceConnectionControllerManager.
     */
    public static void azureServiceConnectionControllersUpdate(
        com.azure.resourcemanager.serviceconnectioncontroller.AzureServiceConnectionControllerManager manager) {
        AzureServiceConnectionController resource = manager.azureServiceConnectionControllers()
            .getByResourceGroupWithResponse("rg1", "pg1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("environment", "production")).apply();
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
     * x-ms-original-file: 2026-02-09-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to AzureServiceConnectionControllerManager.
     */
    public static void operationsList(
        com.azure.resourcemanager.serviceconnectioncontroller.AzureServiceConnectionControllerManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

