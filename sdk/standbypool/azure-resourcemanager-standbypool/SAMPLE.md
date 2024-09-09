# Code snippets and samples


## Operations

- [List](#operations_list)

## StandbyContainerGroupPoolRuntimeViews

- [Get](#standbycontainergrouppoolruntimeviews_get)
- [ListByStandbyPool](#standbycontainergrouppoolruntimeviews_listbystandbypool)

## StandbyContainerGroupPools

- [CreateOrUpdate](#standbycontainergrouppools_createorupdate)
- [Delete](#standbycontainergrouppools_delete)
- [GetByResourceGroup](#standbycontainergrouppools_getbyresourcegroup)
- [List](#standbycontainergrouppools_list)
- [ListByResourceGroup](#standbycontainergrouppools_listbyresourcegroup)
- [Update](#standbycontainergrouppools_update)

## StandbyVirtualMachinePoolRuntimeViews

- [Get](#standbyvirtualmachinepoolruntimeviews_get)
- [ListByStandbyPool](#standbyvirtualmachinepoolruntimeviews_listbystandbypool)

## StandbyVirtualMachinePools

- [CreateOrUpdate](#standbyvirtualmachinepools_createorupdate)
- [Delete](#standbyvirtualmachinepools_delete)
- [GetByResourceGroup](#standbyvirtualmachinepools_getbyresourcegroup)
- [List](#standbyvirtualmachinepools_list)
- [ListByResourceGroup](#standbyvirtualmachinepools_listbyresourcegroup)
- [Update](#standbyvirtualmachinepools_update)

## StandbyVirtualMachines

- [Get](#standbyvirtualmachines_get)
- [ListByStandbyVirtualMachinePoolResource](#standbyvirtualmachines_listbystandbyvirtualmachinepoolresource)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void operationsList(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### StandbyContainerGroupPoolRuntimeViews_Get

```java
/**
 * Samples for StandbyContainerGroupPoolRuntimeViews Get.
 */
public final class StandbyContainerGroupPoolRuntimeViewsGetSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyContainerGroupPoolRuntimeViews_Get.json
     */
    /**
     * Sample code: StandbyContainerGroupPoolRuntimeViews_Get.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyContainerGroupPoolRuntimeViewsGet(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyContainerGroupPoolRuntimeViews()
            .getWithResponse("rgstandbypool", "pool", "latest", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyContainerGroupPoolRuntimeViews_ListByStandbyPool

```java
/**
 * Samples for StandbyContainerGroupPoolRuntimeViews ListByStandbyPool.
 */
public final class StandbyContainerGroupPoolRuntimeViewsListByStandbyPoolSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyContainerGroupPoolRuntimeViews_ListByStandbyPool.json
     */
    /**
     * Sample code: StandbyContainerGroupPoolRuntimeViews_ListByStandbyPool.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void standbyContainerGroupPoolRuntimeViewsListByStandbyPool(
        com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyContainerGroupPoolRuntimeViews()
            .listByStandbyPool("rgstandbypool", "pool", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyContainerGroupPools_CreateOrUpdate

```java
import com.azure.resourcemanager.standbypool.models.ContainerGroupProfile;
import com.azure.resourcemanager.standbypool.models.ContainerGroupProperties;
import com.azure.resourcemanager.standbypool.models.RefillPolicy;
import com.azure.resourcemanager.standbypool.models.StandbyContainerGroupPoolElasticityProfile;
import com.azure.resourcemanager.standbypool.models.StandbyContainerGroupPoolResourceProperties;
import com.azure.resourcemanager.standbypool.models.Subnet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StandbyContainerGroupPools CreateOrUpdate.
 */
public final class StandbyContainerGroupPoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyContainerGroupPools_CreateOrUpdate.json
     */
    /**
     * Sample code: StandbyContainerGroupPools_CreateOrUpdate.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyContainerGroupPoolsCreateOrUpdate(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyContainerGroupPools()
            .define("pool")
            .withRegion("West US")
            .withExistingResourceGroup("rgstandbypool")
            .withTags(mapOf())
            .withProperties(new StandbyContainerGroupPoolResourceProperties()
                .withElasticityProfile(new StandbyContainerGroupPoolElasticityProfile().withMaxReadyCapacity(688L)
                    .withRefillPolicy(RefillPolicy.ALWAYS))
                .withContainerGroupProperties(new ContainerGroupProperties()
                    .withContainerGroupProfile(new ContainerGroupProfile().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000009/resourceGroups/rgstandbypool/providers/Microsoft.ContainerInstance/containerGroupProfiles/cgProfile")
                        .withRevision(1L))
                    .withSubnetIds(Arrays.asList(new Subnet().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000009/resourceGroups/rgstandbypool/providers/Microsoft.Network/virtualNetworks/cgSubnet/subnets/cgSubnet")))))
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

### StandbyContainerGroupPools_Delete

```java
/**
 * Samples for StandbyContainerGroupPools Delete.
 */
public final class StandbyContainerGroupPoolsDeleteSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyContainerGroupPools_Delete.json
     */
    /**
     * Sample code: StandbyContainerGroupPools_Delete.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyContainerGroupPoolsDelete(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyContainerGroupPools().delete("rgstandbypool", "pool", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyContainerGroupPools_GetByResourceGroup

```java
/**
 * Samples for StandbyContainerGroupPools GetByResourceGroup.
 */
public final class StandbyContainerGroupPoolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyContainerGroupPools_Get.json
     */
    /**
     * Sample code: StandbyContainerGroupPools_Get.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void standbyContainerGroupPoolsGet(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyContainerGroupPools()
            .getByResourceGroupWithResponse("rgstandbypool", "pool", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyContainerGroupPools_List

```java
/**
 * Samples for StandbyContainerGroupPools List.
 */
public final class StandbyContainerGroupPoolsListSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyContainerGroupPools_ListBySubscription.json
     */
    /**
     * Sample code: StandbyContainerGroupPools_ListBySubscription.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyContainerGroupPoolsListBySubscription(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyContainerGroupPools().list(com.azure.core.util.Context.NONE);
    }
}
```

### StandbyContainerGroupPools_ListByResourceGroup

```java
/**
 * Samples for StandbyContainerGroupPools ListByResourceGroup.
 */
public final class StandbyContainerGroupPoolsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyContainerGroupPools_ListByResourceGroup.json
     */
    /**
     * Sample code: StandbyContainerGroupPools_ListByResourceGroup.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void standbyContainerGroupPoolsListByResourceGroup(
        com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyContainerGroupPools().listByResourceGroup("rgstandbypool", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyContainerGroupPools_Update

```java
import com.azure.resourcemanager.standbypool.models.ContainerGroupProfile;
import com.azure.resourcemanager.standbypool.models.ContainerGroupProperties;
import com.azure.resourcemanager.standbypool.models.RefillPolicy;
import com.azure.resourcemanager.standbypool.models.StandbyContainerGroupPoolElasticityProfile;
import com.azure.resourcemanager.standbypool.models.StandbyContainerGroupPoolResource;
import com.azure.resourcemanager.standbypool.models.StandbyContainerGroupPoolResourceUpdateProperties;
import com.azure.resourcemanager.standbypool.models.Subnet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StandbyContainerGroupPools Update.
 */
public final class StandbyContainerGroupPoolsUpdateSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyContainerGroupPools_Update.json
     */
    /**
     * Sample code: StandbyContainerGroupPools_Update.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyContainerGroupPoolsUpdate(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        StandbyContainerGroupPoolResource resource = manager.standbyContainerGroupPools()
            .getByResourceGroupWithResponse("rgstandbypool", "pool", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf())
            .withProperties(new StandbyContainerGroupPoolResourceUpdateProperties()
                .withElasticityProfile(new StandbyContainerGroupPoolElasticityProfile().withMaxReadyCapacity(1743L)
                    .withRefillPolicy(RefillPolicy.ALWAYS))
                .withContainerGroupProperties(new ContainerGroupProperties()
                    .withContainerGroupProfile(new ContainerGroupProfile().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000009/resourceGroups/rgstandbypool/providers/Microsoft.ContainerInstance/containerGroupProfiles/cgProfile")
                        .withRevision(2L))
                    .withSubnetIds(Arrays.asList(new Subnet().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000009/resourceGroups/rgstandbypool/providers/Microsoft.Network/virtualNetworks/cgSubnet/subnets/cgSubnet")))))
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

### StandbyVirtualMachinePoolRuntimeViews_Get

```java
/**
 * Samples for StandbyVirtualMachinePoolRuntimeViews Get.
 */
public final class StandbyVirtualMachinePoolRuntimeViewsGetSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachinePoolRuntimeViews_Get.json
     */
    /**
     * Sample code: StandbyVirtualMachinePoolRuntimeViews_Get.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyVirtualMachinePoolRuntimeViewsGet(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyVirtualMachinePoolRuntimeViews()
            .getWithResponse("rgstandbypool", "pool", "latest", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyVirtualMachinePoolRuntimeViews_ListByStandbyPool

```java
/**
 * Samples for StandbyVirtualMachinePoolRuntimeViews ListByStandbyPool.
 */
public final class StandbyVirtualMachinePoolRuntimeViewsListByStandbyPoolSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachinePoolRuntimeViews_ListByStandbyPool.json
     */
    /**
     * Sample code: StandbyVirtualMachinePoolRuntimeViews_ListByStandbyPool.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void standbyVirtualMachinePoolRuntimeViewsListByStandbyPool(
        com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyVirtualMachinePoolRuntimeViews()
            .listByStandbyPool("rgstandbypool", "pool", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyVirtualMachinePools_CreateOrUpdate

```java
import com.azure.resourcemanager.standbypool.models.StandbyVirtualMachinePoolElasticityProfile;
import com.azure.resourcemanager.standbypool.models.StandbyVirtualMachinePoolResourceProperties;
import com.azure.resourcemanager.standbypool.models.VirtualMachineState;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StandbyVirtualMachinePools CreateOrUpdate.
 */
public final class StandbyVirtualMachinePoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachinePools_CreateOrUpdate.json
     */
    /**
     * Sample code: StandbyVirtualMachinePools_CreateOrUpdate.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyVirtualMachinePoolsCreateOrUpdate(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyVirtualMachinePools()
            .define("pool")
            .withRegion("West US")
            .withExistingResourceGroup("rgstandbypool")
            .withTags(mapOf())
            .withProperties(new StandbyVirtualMachinePoolResourceProperties()
                .withElasticityProfile(new StandbyVirtualMachinePoolElasticityProfile().withMaxReadyCapacity(304L)
                    .withMinReadyCapacity(300L))
                .withVirtualMachineState(VirtualMachineState.RUNNING)
                .withAttachedVirtualMachineScaleSetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000009/resourceGroups/rgstandbypool/providers/Microsoft.Compute/virtualMachineScaleSets/myVmss"))
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

### StandbyVirtualMachinePools_Delete

```java
/**
 * Samples for StandbyVirtualMachinePools Delete.
 */
public final class StandbyVirtualMachinePoolsDeleteSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachinePools_Delete.json
     */
    /**
     * Sample code: StandbyVirtualMachinePools_Delete.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyVirtualMachinePoolsDelete(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyVirtualMachinePools().delete("rgstandbypool", "pool", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyVirtualMachinePools_GetByResourceGroup

```java
/**
 * Samples for StandbyVirtualMachinePools GetByResourceGroup.
 */
public final class StandbyVirtualMachinePoolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachinePools_Get.json
     */
    /**
     * Sample code: StandbyVirtualMachinePools_Get.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void standbyVirtualMachinePoolsGet(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyVirtualMachinePools()
            .getByResourceGroupWithResponse("rgstandbypool", "pool", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyVirtualMachinePools_List

```java
/**
 * Samples for StandbyVirtualMachinePools List.
 */
public final class StandbyVirtualMachinePoolsListSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachinePools_ListBySubscription.json
     */
    /**
     * Sample code: StandbyVirtualMachinePools_ListBySubscription.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyVirtualMachinePoolsListBySubscription(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyVirtualMachinePools().list(com.azure.core.util.Context.NONE);
    }
}
```

### StandbyVirtualMachinePools_ListByResourceGroup

```java
/**
 * Samples for StandbyVirtualMachinePools ListByResourceGroup.
 */
public final class StandbyVirtualMachinePoolsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachinePools_ListByResourceGroup.json
     */
    /**
     * Sample code: StandbyVirtualMachinePools_ListByResourceGroup.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void standbyVirtualMachinePoolsListByResourceGroup(
        com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyVirtualMachinePools().listByResourceGroup("rgstandbypool", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyVirtualMachinePools_Update

```java
import com.azure.resourcemanager.standbypool.models.StandbyVirtualMachinePoolElasticityProfile;
import com.azure.resourcemanager.standbypool.models.StandbyVirtualMachinePoolResource;
import com.azure.resourcemanager.standbypool.models.StandbyVirtualMachinePoolResourceUpdateProperties;
import com.azure.resourcemanager.standbypool.models.VirtualMachineState;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StandbyVirtualMachinePools Update.
 */
public final class StandbyVirtualMachinePoolsUpdateSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachinePools_Update.json
     */
    /**
     * Sample code: StandbyVirtualMachinePools_Update.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void
        standbyVirtualMachinePoolsUpdate(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        StandbyVirtualMachinePoolResource resource = manager.standbyVirtualMachinePools()
            .getByResourceGroupWithResponse("rgstandbypool", "pool", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf())
            .withProperties(new StandbyVirtualMachinePoolResourceUpdateProperties()
                .withElasticityProfile(new StandbyVirtualMachinePoolElasticityProfile().withMaxReadyCapacity(304L)
                    .withMinReadyCapacity(300L))
                .withVirtualMachineState(VirtualMachineState.RUNNING)
                .withAttachedVirtualMachineScaleSetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000009/resourceGroups/rgstandbypool/providers/Microsoft.Compute/virtualMachineScaleSets/myVmss"))
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

### StandbyVirtualMachines_Get

```java
/**
 * Samples for StandbyVirtualMachines Get.
 */
public final class StandbyVirtualMachinesGetSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachines_Get.json
     */
    /**
     * Sample code: StandbyVirtualMachines_Get.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void standbyVirtualMachinesGet(com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyVirtualMachines()
            .getWithResponse("rgstandbypool", "pool", "virtualMachine", com.azure.core.util.Context.NONE);
    }
}
```

### StandbyVirtualMachines_ListByStandbyVirtualMachinePoolResource

```java
/**
 * Samples for StandbyVirtualMachines ListByStandbyVirtualMachinePoolResource.
 */
public final class StandbyVirtualMachinesListByStandbyVirtualMachinePoolResourceSamples {
    /*
     * x-ms-original-file: 2024-03-01-preview/StandbyVirtualMachines_ListByStandbyVirtualMachinePoolResource.json
     */
    /**
     * Sample code: StandbyVirtualMachines_ListByStandbyVirtualMachinePoolResource.
     * 
     * @param manager Entry point to StandbyPoolManager.
     */
    public static void standbyVirtualMachinesListByStandbyVirtualMachinePoolResource(
        com.azure.resourcemanager.standbypool.StandbyPoolManager manager) {
        manager.standbyVirtualMachines()
            .listByStandbyVirtualMachinePoolResource("rgstandbypool", "pool", com.azure.core.util.Context.NONE);
    }
}
```

