# Code snippets and samples


## BgpPeers

- [CreateOrUpdate](#bgppeers_createorupdate)
- [Delete](#bgppeers_delete)
- [Get](#bgppeers_get)
- [List](#bgppeers_list)

## LoadBalancers

- [CreateOrUpdate](#loadbalancers_createorupdate)
- [Delete](#loadbalancers_delete)
- [Get](#loadbalancers_get)
- [List](#loadbalancers_list)

## Operations

- [List](#operations_list)

## Services

- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [Get](#services_get)
- [List](#services_list)

## StorageClass

- [CreateOrUpdate](#storageclass_createorupdate)
- [Delete](#storageclass_delete)
- [Get](#storageclass_get)
- [List](#storageclass_list)
- [Update](#storageclass_update)
### BgpPeers_CreateOrUpdate

```java
import com.azure.resourcemanager.containerorchestratorruntime.models.BgpPeerProperties;

/**
 * Samples for BgpPeers CreateOrUpdate.
 */
public final class BgpPeersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-03-01/BgpPeers_CreateOrUpdate.json
     */
    /**
     * Sample code: BgpPeers_CreateOrUpdate.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void bgpPeersCreateOrUpdate(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.bgpPeers()
            .define("testpeer")
            .withExistingResourceUri(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1")
            .withProperties(new BgpPeerProperties().withMyAsn(64500).withPeerAsn(64501).withPeerAddress("10.0.0.1"))
            .create();
    }
}
```

### BgpPeers_Delete

```java
/**
 * Samples for BgpPeers Delete.
 */
public final class BgpPeersDeleteSamples {
    /*
     * x-ms-original-file: 2024-03-01/BgpPeers_Delete.json
     */
    /**
     * Sample code: BgpPeers_Delete.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void bgpPeersDelete(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.bgpPeers()
            .deleteByResourceGroupWithResponse(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                "testpeer", com.azure.core.util.Context.NONE);
    }
}
```

### BgpPeers_Get

```java
/**
 * Samples for BgpPeers Get.
 */
public final class BgpPeersGetSamples {
    /*
     * x-ms-original-file: 2024-03-01/BgpPeers_Get.json
     */
    /**
     * Sample code: BgpPeers_Get.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void bgpPeersGet(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.bgpPeers()
            .getWithResponse(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                "testpeer", com.azure.core.util.Context.NONE);
    }
}
```

### BgpPeers_List

```java
/**
 * Samples for BgpPeers List.
 */
public final class BgpPeersListSamples {
    /*
     * x-ms-original-file: 2024-03-01/BgpPeers_List.json
     */
    /**
     * Sample code: BgpPeers_List.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void bgpPeersList(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.bgpPeers()
            .list(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                com.azure.core.util.Context.NONE);
    }
}
```

### LoadBalancers_CreateOrUpdate

```java
import com.azure.resourcemanager.containerorchestratorruntime.models.AdvertiseMode;
import com.azure.resourcemanager.containerorchestratorruntime.models.LoadBalancerProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LoadBalancers CreateOrUpdate.
 */
public final class LoadBalancersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-03-01/LoadBalancers_CreateOrUpdate.json
     */
    /**
     * Sample code: LoadBalancers_CreateOrUpdate.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void loadBalancersCreateOrUpdate(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.loadBalancers()
            .define("testlb")
            .withExistingResourceUri(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1")
            .withProperties(new LoadBalancerProperties()
                .withAddresses(Arrays.asList("192.168.50.1/24", "192.168.51.2-192.168.51.10"))
                .withServiceSelector(mapOf("app", "frontend"))
                .withAdvertiseMode(AdvertiseMode.ARP))
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

### LoadBalancers_Delete

```java
/**
 * Samples for LoadBalancers Delete.
 */
public final class LoadBalancersDeleteSamples {
    /*
     * x-ms-original-file: 2024-03-01/LoadBalancers_Delete.json
     */
    /**
     * Sample code: LoadBalancers_Delete.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void loadBalancersDelete(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.loadBalancers()
            .deleteByResourceGroupWithResponse(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                "testlb", com.azure.core.util.Context.NONE);
    }
}
```

### LoadBalancers_Get

```java
/**
 * Samples for LoadBalancers Get.
 */
public final class LoadBalancersGetSamples {
    /*
     * x-ms-original-file: 2024-03-01/LoadBalancers_Get.json
     */
    /**
     * Sample code: LoadBalancers_Get.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void loadBalancersGet(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.loadBalancers()
            .getWithResponse(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                "testlb", com.azure.core.util.Context.NONE);
    }
}
```

### LoadBalancers_List

```java
/**
 * Samples for LoadBalancers List.
 */
public final class LoadBalancersListSamples {
    /*
     * x-ms-original-file: 2024-03-01/LoadBalancers_List.json
     */
    /**
     * Sample code: LoadBalancers_List.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void loadBalancersList(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.loadBalancers()
            .list(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-03-01/Operations_List.json
     */
    /**
     * Sample code: Operations_List_0.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void operationsList0(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.containerorchestratorruntime.models.ServiceProperties;

/**
 * Samples for Services CreateOrUpdate.
 */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-03-01/Services_CreateOrUpdate.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void servicesCreateOrUpdate(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.services()
            .define("storageclass")
            .withExistingResourceUri(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1")
            .withProperties(new ServiceProperties())
            .create();
    }
}
```

### Services_Delete

```java
/**
 * Samples for Services Delete.
 */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: 2024-03-01/Services_Delete.json
     */
    /**
     * Sample code: Services_Delete.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void servicesDelete(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.services()
            .deleteByResourceGroupWithResponse(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                "storageclass", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Get

```java
/**
 * Samples for Services Get.
 */
public final class ServicesGetSamples {
    /*
     * x-ms-original-file: 2024-03-01/Services_Get.json
     */
    /**
     * Sample code: Services_Get.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void servicesGet(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.services()
            .getWithResponse(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                "storageclass", com.azure.core.util.Context.NONE);
    }
}
```

### Services_List

```java
/**
 * Samples for Services List.
 */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: 2024-03-01/Services_List.json
     */
    /**
     * Sample code: Services_List.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void servicesList(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.services()
            .list(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                com.azure.core.util.Context.NONE);
    }
}
```

### StorageClass_CreateOrUpdate

```java
import com.azure.resourcemanager.containerorchestratorruntime.models.RwxStorageClassTypeProperties;
import com.azure.resourcemanager.containerorchestratorruntime.models.StorageClassProperties;

/**
 * Samples for StorageClass CreateOrUpdate.
 */
public final class StorageClassCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-03-01/StorageClass_CreateOrUpdate.json
     */
    /**
     * Sample code: StorageClass_CreateOrUpdate_0.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void storageClassCreateOrUpdate0(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.storageClass()
            .define("testrwx")
            .withExistingResourceUri(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1")
            .withProperties(new StorageClassProperties()
                .withTypeProperties(new RwxStorageClassTypeProperties().withBackingStorageClassName("default")))
            .create();
    }
}
```

### StorageClass_Delete

```java
/**
 * Samples for StorageClass Delete.
 */
public final class StorageClassDeleteSamples {
    /*
     * x-ms-original-file: 2024-03-01/StorageClass_Delete.json
     */
    /**
     * Sample code: StorageClass_Delete_0.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void storageClassDelete0(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.storageClass()
            .delete(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                "testrwx", com.azure.core.util.Context.NONE);
    }
}
```

### StorageClass_Get

```java
/**
 * Samples for StorageClass Get.
 */
public final class StorageClassGetSamples {
    /*
     * x-ms-original-file: 2024-03-01/StorageClass_Get.json
     */
    /**
     * Sample code: StorageClass_Get_0.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void storageClassGet0(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.storageClass()
            .getWithResponse(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                "testrwx", com.azure.core.util.Context.NONE);
    }
}
```

### StorageClass_List

```java
/**
 * Samples for StorageClass List.
 */
public final class StorageClassListSamples {
    /*
     * x-ms-original-file: 2024-03-01/StorageClass_List.json
     */
    /**
     * Sample code: StorageClass_List_0.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void storageClassList0(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        manager.storageClass()
            .list(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                com.azure.core.util.Context.NONE);
    }
}
```

### StorageClass_Update

```java
import com.azure.resourcemanager.containerorchestratorruntime.models.StorageClassPropertiesUpdate;
import com.azure.resourcemanager.containerorchestratorruntime.models.StorageClassResource;
import com.azure.resourcemanager.containerorchestratorruntime.models.StorageClassTypePropertiesUpdate;

/**
 * Samples for StorageClass Update.
 */
public final class StorageClassUpdateSamples {
    /*
     * x-ms-original-file: 2024-03-01/StorageClass_Update.json
     */
    /**
     * Sample code: StorageClass_Update_0.
     * 
     * @param manager Entry point to ContainerOrchestratorRuntimeManager.
     */
    public static void storageClassUpdate0(
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager) {
        StorageClassResource resource = manager.storageClass()
            .getWithResponse(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/example/providers/Microsoft.Kubernetes/connectedClusters/cluster1",
                "testrwx", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new StorageClassPropertiesUpdate()
                .withTypeProperties(new StorageClassTypePropertiesUpdate().withBackingStorageClassName("default")))
            .apply();
    }
}
```

