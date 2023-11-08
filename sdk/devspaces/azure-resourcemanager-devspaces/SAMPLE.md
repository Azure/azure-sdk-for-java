# Code snippets and samples


## ContainerHostMappings

- [GetContainerHostMapping](#containerhostmappings_getcontainerhostmapping)

## Controllers

- [Create](#controllers_create)
- [Delete](#controllers_delete)
- [GetByResourceGroup](#controllers_getbyresourcegroup)
- [List](#controllers_list)
- [ListByResourceGroup](#controllers_listbyresourcegroup)
- [ListConnectionDetails](#controllers_listconnectiondetails)
- [Update](#controllers_update)
### ContainerHostMappings_GetContainerHostMapping

```java
import com.azure.resourcemanager.devspaces.fluent.models.ContainerHostMappingInner;

/** Samples for ContainerHostMappings GetContainerHostMapping. */
public final class ContainerHostMappingsGetContainerHostMappingSamples {
    /*
     * x-ms-original-file: specification/devspaces/resource-manager/Microsoft.DevSpaces/stable/2019-04-01/examples/ContainerHostMappingsGetContainerHostMapping_example.json
     */
    /**
     * Sample code: ContainerHostMappingsGetContainerHostMapping.
     *
     * @param manager Entry point to DevSpacesManager.
     */
    public static void containerHostMappingsGetContainerHostMapping(
        com.azure.resourcemanager.devspaces.DevSpacesManager manager) {
        manager
            .containerHostMappings()
            .getContainerHostMappingWithResponse(
                "myResourceGroup",
                "eastus",
                new ContainerHostMappingInner()
                    .withContainerHostResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerService/managedClusters/myCluster"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Controllers_Create

```java
import com.azure.resourcemanager.devspaces.models.Sku;
import com.azure.resourcemanager.devspaces.models.SkuName;
import com.azure.resourcemanager.devspaces.models.SkuTier;
import java.util.HashMap;
import java.util.Map;

/** Samples for Controllers Create. */
public final class ControllersCreateSamples {
    /*
     * x-ms-original-file: specification/devspaces/resource-manager/Microsoft.DevSpaces/stable/2019-04-01/examples/ControllersCreate_example.json
     */
    /**
     * Sample code: ControllersCreate.
     *
     * @param manager Entry point to DevSpacesManager.
     */
    public static void controllersCreate(com.azure.resourcemanager.devspaces.DevSpacesManager manager) {
        manager
            .controllers()
            .define("myControllerResource")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(SkuName.S1).withTier(SkuTier.STANDARD))
            .withTargetContainerHostResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerService/managedClusters/myCluster")
            .withTargetContainerHostCredentialsBase64("QmFzZTY0IEVuY29kZWQgVmFsdWUK")
            .withTags(mapOf())
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

### Controllers_Delete

```java
/** Samples for Controllers Delete. */
public final class ControllersDeleteSamples {
    /*
     * x-ms-original-file: specification/devspaces/resource-manager/Microsoft.DevSpaces/stable/2019-04-01/examples/ControllersDelete_example.json
     */
    /**
     * Sample code: ControllersDelete.
     *
     * @param manager Entry point to DevSpacesManager.
     */
    public static void controllersDelete(com.azure.resourcemanager.devspaces.DevSpacesManager manager) {
        manager.controllers().delete("myResourceGroup", "myControllerResource", com.azure.core.util.Context.NONE);
    }
}
```

### Controllers_GetByResourceGroup

```java
/** Samples for Controllers GetByResourceGroup. */
public final class ControllersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devspaces/resource-manager/Microsoft.DevSpaces/stable/2019-04-01/examples/ControllersGet_example.json
     */
    /**
     * Sample code: ControllersGet.
     *
     * @param manager Entry point to DevSpacesManager.
     */
    public static void controllersGet(com.azure.resourcemanager.devspaces.DevSpacesManager manager) {
        manager
            .controllers()
            .getByResourceGroupWithResponse(
                "myResourceGroup", "myControllerResource", com.azure.core.util.Context.NONE);
    }
}
```

### Controllers_List

```java
/** Samples for Controllers List. */
public final class ControllersListSamples {
    /*
     * x-ms-original-file: specification/devspaces/resource-manager/Microsoft.DevSpaces/stable/2019-04-01/examples/ControllersList_example.json
     */
    /**
     * Sample code: ControllersList.
     *
     * @param manager Entry point to DevSpacesManager.
     */
    public static void controllersList(com.azure.resourcemanager.devspaces.DevSpacesManager manager) {
        manager.controllers().list(com.azure.core.util.Context.NONE);
    }
}
```

### Controllers_ListByResourceGroup

```java
/** Samples for Controllers ListByResourceGroup. */
public final class ControllersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devspaces/resource-manager/Microsoft.DevSpaces/stable/2019-04-01/examples/ControllersListByResourceGroup_example.json
     */
    /**
     * Sample code: ControllersListByResourceGroup.
     *
     * @param manager Entry point to DevSpacesManager.
     */
    public static void controllersListByResourceGroup(com.azure.resourcemanager.devspaces.DevSpacesManager manager) {
        manager.controllers().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Controllers_ListConnectionDetails

```java
import com.azure.resourcemanager.devspaces.models.ListConnectionDetailsParameters;

/** Samples for Controllers ListConnectionDetails. */
public final class ControllersListConnectionDetailsSamples {
    /*
     * x-ms-original-file: specification/devspaces/resource-manager/Microsoft.DevSpaces/stable/2019-04-01/examples/ControllersListConnectionDetails_example.json
     */
    /**
     * Sample code: ControllersListConnectionDetails.
     *
     * @param manager Entry point to DevSpacesManager.
     */
    public static void controllersListConnectionDetails(com.azure.resourcemanager.devspaces.DevSpacesManager manager) {
        manager
            .controllers()
            .listConnectionDetailsWithResponse(
                "myResourceGroup",
                "myControllerResource",
                new ListConnectionDetailsParameters()
                    .withTargetContainerHostResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerService/managedClusters/myCluster"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Controllers_Update

```java
import com.azure.resourcemanager.devspaces.models.Controller;
import java.util.HashMap;
import java.util.Map;

/** Samples for Controllers Update. */
public final class ControllersUpdateSamples {
    /*
     * x-ms-original-file: specification/devspaces/resource-manager/Microsoft.DevSpaces/stable/2019-04-01/examples/ControllersUpdate_example.json
     */
    /**
     * Sample code: ControllersUpdate.
     *
     * @param manager Entry point to DevSpacesManager.
     */
    public static void controllersUpdate(com.azure.resourcemanager.devspaces.DevSpacesManager manager) {
        Controller resource =
            manager
                .controllers()
                .getByResourceGroupWithResponse(
                    "myResourceGroup", "myControllerResource", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key", "value"))
            .withTargetContainerHostCredentialsBase64("QmFzZTY0IEVuY29kZWQgVmFsdWUK")
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

