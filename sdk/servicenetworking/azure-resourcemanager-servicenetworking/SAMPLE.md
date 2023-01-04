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
            .define("associatedvnet-1")
            .withRegion("West US")
            .withExistingTrafficController("rg1", "TC1")
            .withAssociationType(AssociationType.SUBNETS)
            .withSubnet(new AssociationSubnet().withId("subnetFullRef"))
            .create();
    }
}
```

### AssociationsInterface_Delete

```java
import com.azure.core.util.Context;

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
        manager.associationsInterfaces().delete("rg1", "TC1", "associatedvnet-2", Context.NONE);
    }
}
```

### AssociationsInterface_Get

```java
import com.azure.core.util.Context;

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
        manager.associationsInterfaces().getWithResponse("rg1", "TC1", "associatedvnet-2", Context.NONE);
    }
}
```

### AssociationsInterface_ListByTrafficController

```java
import com.azure.core.util.Context;

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
        manager.associationsInterfaces().listByTrafficController("rg1", "TC1", Context.NONE);
    }
}
```

### AssociationsInterface_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicenetworking.models.Association;
import com.azure.resourcemanager.servicenetworking.models.AssociationSubnet;
import com.azure.resourcemanager.servicenetworking.models.AssociationType;
import com.azure.resourcemanager.servicenetworking.models.AssociationUpdateProperties;

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
            manager.associationsInterfaces().getWithResponse("rg1", "TC1", "associatedvnet-1", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new AssociationUpdateProperties()
                    .withAssociationType(AssociationType.SUBNETS)
                    .withSubnet(new AssociationSubnet().withId("subnetFullRef")))
            .apply();
    }
}
```

### FrontendsInterface_CreateOrUpdate

```java
import com.azure.resourcemanager.servicenetworking.models.FrontendIpAddressVersion;
import com.azure.resourcemanager.servicenetworking.models.FrontendMode;
import com.azure.resourcemanager.servicenetworking.models.FrontendPropertiesIpAddress;

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
            .define("publicIp1")
            .withRegion("West US")
            .withExistingTrafficController("rg1", "TC1")
            .withMode(FrontendMode.PUBLIC)
            .withIpAddressVersion(FrontendIpAddressVersion.IPV4)
            .withPublicIpAddress(new FrontendPropertiesIpAddress().withId("resourceUriAsString"))
            .create();
    }
}
```

### FrontendsInterface_Delete

```java
import com.azure.core.util.Context;

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
        manager.frontendsInterfaces().delete("rg1", "TC1", "publicIp1", Context.NONE);
    }
}
```

### FrontendsInterface_Get

```java
import com.azure.core.util.Context;

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
        manager.frontendsInterfaces().getWithResponse("rg1", "TC1", "publicIp1", Context.NONE);
    }
}
```

### FrontendsInterface_ListByTrafficController

```java
import com.azure.core.util.Context;

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
        manager.frontendsInterfaces().listByTrafficController("rg1", "TC1", Context.NONE);
    }
}
```

### FrontendsInterface_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicenetworking.models.Frontend;
import com.azure.resourcemanager.servicenetworking.models.FrontendIpAddressVersion;
import com.azure.resourcemanager.servicenetworking.models.FrontendMode;
import com.azure.resourcemanager.servicenetworking.models.FrontendPropertiesIpAddress;
import com.azure.resourcemanager.servicenetworking.models.FrontendUpdateProperties;

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
            manager.frontendsInterfaces().getWithResponse("rg1", "TC1", "publicIp1", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new FrontendUpdateProperties()
                    .withMode(FrontendMode.PUBLIC)
                    .withIpAddressVersion(FrontendIpAddressVersion.IPV4)
                    .withPublicIpAddress(new FrontendPropertiesIpAddress().withId("resourceUriAsString")))
            .apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

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
        manager.operations().list(Context.NONE);
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
            .define("TC1")
            .withRegion("West US")
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
import com.azure.core.util.Context;

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
        manager.trafficControllerInterfaces().delete("rg1", "TC1", Context.NONE);
    }
}
```

### TrafficControllerInterface_GetByResourceGroup

```java
import com.azure.core.util.Context;

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
        manager.trafficControllerInterfaces().getByResourceGroupWithResponse("rg1", "TC1", Context.NONE);
    }
}
```

### TrafficControllerInterface_List

```java
import com.azure.core.util.Context;

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
        manager.trafficControllerInterfaces().list(Context.NONE);
    }
}
```

### TrafficControllerInterface_ListByResourceGroup

```java
import com.azure.core.util.Context;

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
        manager.trafficControllerInterfaces().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### TrafficControllerInterface_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.servicenetworking.models.TrafficController;
import java.io.IOException;

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
        com.azure.resourcemanager.servicenetworking.TrafficControllerManager manager) throws IOException {
        TrafficController resource =
            manager.trafficControllerInterfaces().getByResourceGroupWithResponse("rg1", "TC1", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"configurationEndpoints\":[\"abc.eastus.trafficcontroller.azure.net\"]}",
                        Object.class,
                        SerializerEncoding.JSON))
            .apply();
    }
}
```

