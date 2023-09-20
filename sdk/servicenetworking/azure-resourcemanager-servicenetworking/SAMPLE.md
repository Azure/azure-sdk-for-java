# Code snippets and samples


## AssociationsInterface

- [CreateOrUpdate](#associationsinterface_createorupdate)
- [Delete](#associationsinterface_delete)
- [Get](#associationsinterface_get)
- [ListByTrafficController](#associationsinterface_listbytrafficcontroller)
- [Update](#associationsinterface_update)

## FrontendsInterface

- [CreateOrUpdate](#frontendsinterface_createorupdate)
- [Delete](#frontendsinterface_delete)
- [Get](#frontendsinterface_get)
- [ListByTrafficController](#frontendsinterface_listbytrafficcontroller)
- [Update](#frontendsinterface_update)

## Operations

- [List](#operations_list)

## TrafficControllerInterface

- [CreateOrUpdate](#trafficcontrollerinterface_createorupdate)
- [Delete](#trafficcontrollerinterface_delete)
- [GetByResourceGroup](#trafficcontrollerinterface_getbyresourcegroup)
- [List](#trafficcontrollerinterface_list)
- [ListByResourceGroup](#trafficcontrollerinterface_listbyresourcegroup)
- [Update](#trafficcontrollerinterface_update)
### AssociationsInterface_CreateOrUpdate

```java
import com.azure.resourcemanager.servicenetworking.models.AssociationSubnet;
import com.azure.resourcemanager.servicenetworking.models.AssociationType;

/** Samples for AssociationsInterface CreateOrUpdate. */
public final class AssociationsInterfaceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/AssociationPut.json
     */
    /**
     * Sample code: Put Association.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void putAssociation(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager
            .associationsInterfaces()
            .define("as1")
            .withRegion("NorthCentralUS")
            .withExistingTrafficController("rg1", "tc1")
            .withAssociationType(AssociationType.SUBNETS)
            .withSubnet(
                new AssociationSubnet()
                    .withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/virtualNetworks/vnet-tc/subnets/tc-subnet"))
            .create();
    }
}
```

### AssociationsInterface_Delete

```java
/** Samples for AssociationsInterface Delete. */
public final class AssociationsInterfaceDeleteSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/AssociationDelete.json
     */
    /**
     * Sample code: Delete Association.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void deleteAssociation(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.associationsInterfaces().delete("rg1", "tc1", "as1", com.azure.core.util.Context.NONE);
    }
}
```

### AssociationsInterface_Get

```java
/** Samples for AssociationsInterface Get. */
public final class AssociationsInterfaceGetSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/AssociationGet.json
     */
    /**
     * Sample code: Get Association.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void getAssociation(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.associationsInterfaces().getWithResponse("rg1", "tc1", "as1", com.azure.core.util.Context.NONE);
    }
}
```

### AssociationsInterface_ListByTrafficController

```java
/** Samples for AssociationsInterface ListByTrafficController. */
public final class AssociationsInterfaceListByTrafficControllerSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/AssociationsGet.json
     */
    /**
     * Sample code: Get Associations.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void getAssociations(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.associationsInterfaces().listByTrafficController("rg1", "tc1", com.azure.core.util.Context.NONE);
    }
}
```

### AssociationsInterface_Update

```java
import com.azure.resourcemanager.servicenetworking.models.Association;
import com.azure.resourcemanager.servicenetworking.models.AssociationSubnetUpdate;
import com.azure.resourcemanager.servicenetworking.models.AssociationType;

/** Samples for AssociationsInterface Update. */
public final class AssociationsInterfaceUpdateSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/AssociationPatch.json
     */
    /**
     * Sample code: Update Association.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void updateAssociation(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        Association resource =
            manager
                .associationsInterfaces()
                .getWithResponse("rg1", "tc1", "as1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withAssociationType(AssociationType.SUBNETS)
            .withSubnet(
                new AssociationSubnetUpdate()
                    .withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/virtualNetworks/vnet-tc/subnets/tc-subnet"))
            .apply();
    }
}
```

### FrontendsInterface_CreateOrUpdate

```java
/** Samples for FrontendsInterface CreateOrUpdate. */
public final class FrontendsInterfaceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/FrontendPut.json
     */
    /**
     * Sample code: Put Frontend.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void putFrontend(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager
            .frontendsInterfaces()
            .define("fe1")
            .withRegion("NorthCentralUS")
            .withExistingTrafficController("rg1", "tc1")
            .create();
    }
}
```

### FrontendsInterface_Delete

```java
/** Samples for FrontendsInterface Delete. */
public final class FrontendsInterfaceDeleteSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/FrontendDelete.json
     */
    /**
     * Sample code: Delete Frontend.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void deleteFrontend(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.frontendsInterfaces().delete("rg1", "tc1", "fe1", com.azure.core.util.Context.NONE);
    }
}
```

### FrontendsInterface_Get

```java
/** Samples for FrontendsInterface Get. */
public final class FrontendsInterfaceGetSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/FrontendGet.json
     */
    /**
     * Sample code: Get Frontend.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void getFrontend(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.frontendsInterfaces().getWithResponse("rg1", "tc1", "fe1", com.azure.core.util.Context.NONE);
    }
}
```

### FrontendsInterface_ListByTrafficController

```java
/** Samples for FrontendsInterface ListByTrafficController. */
public final class FrontendsInterfaceListByTrafficControllerSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/FrontendsGet.json
     */
    /**
     * Sample code: Get Frontends.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void getFrontends(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.frontendsInterfaces().listByTrafficController("rg1", "tc1", com.azure.core.util.Context.NONE);
    }
}
```

### FrontendsInterface_Update

```java
import com.azure.resourcemanager.servicenetworking.models.Frontend;

/** Samples for FrontendsInterface Update. */
public final class FrontendsInterfaceUpdateSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/FrontendPatch.json
     */
    /**
     * Sample code: Update Frontend.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void updateFrontend(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        Frontend resource =
            manager
                .frontendsInterfaces()
                .getWithResponse("rg1", "tc1", "fe1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/OperationsList.json
     */
    /**
     * Sample code: Get Operations List.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void getOperationsList(com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### TrafficControllerInterface_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for TrafficControllerInterface CreateOrUpdate. */
public final class TrafficControllerInterfaceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/TrafficControllerPut.json
     */
    /**
     * Sample code: Put Traffic Controller.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void putTrafficController(
        com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager
            .trafficControllerInterfaces()
            .define("tc1")
            .withRegion("NorthCentralUS")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "value1"))
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

### TrafficControllerInterface_Delete

```java
/** Samples for TrafficControllerInterface Delete. */
public final class TrafficControllerInterfaceDeleteSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/TrafficControllerDelete.json
     */
    /**
     * Sample code: Delete Traffic Controller.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void deleteTrafficController(
        com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.trafficControllerInterfaces().delete("rg1", "tc1", com.azure.core.util.Context.NONE);
    }
}
```

### TrafficControllerInterface_GetByResourceGroup

```java
/** Samples for TrafficControllerInterface GetByResourceGroup. */
public final class TrafficControllerInterfaceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/TrafficControllerGet.json
     */
    /**
     * Sample code: Get Traffic Controller.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void getTrafficController(
        com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager
            .trafficControllerInterfaces()
            .getByResourceGroupWithResponse("rg1", "tc1", com.azure.core.util.Context.NONE);
    }
}
```

### TrafficControllerInterface_List

```java
/** Samples for TrafficControllerInterface List. */
public final class TrafficControllerInterfaceListSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/TrafficControllersGetList.json
     */
    /**
     * Sample code: Get Traffic Controllers List.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void getTrafficControllersList(
        com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.trafficControllerInterfaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### TrafficControllerInterface_ListByResourceGroup

```java
/** Samples for TrafficControllerInterface ListByResourceGroup. */
public final class TrafficControllerInterfaceListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/TrafficControllersGet.json
     */
    /**
     * Sample code: Get Traffic Controllers.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void getTrafficControllers(
        com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        manager.trafficControllerInterfaces().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### TrafficControllerInterface_Update

```java
import com.azure.resourcemanager.servicenetworking.models.TrafficController;
import java.util.HashMap;
import java.util.Map;

/** Samples for TrafficControllerInterface Update. */
public final class TrafficControllerInterfaceUpdateSamples {
    /*
     * x-ms-original-file: specification/servicenetworking/resource-manager/Microsoft.ServiceNetworking/cadl/examples/TrafficControllerPatch.json
     */
    /**
     * Sample code: Patch Traffic Controller.
     *
     * @param manager Entry point to TrafficControllerManager.
     */
    public static void patchTrafficController(
        com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) {
        TrafficController resource =
            manager
                .trafficControllerInterfaces()
                .getByResourceGroupWithResponse("rg1", "tc1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1")).apply();
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

