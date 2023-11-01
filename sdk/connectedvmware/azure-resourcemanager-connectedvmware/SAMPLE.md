# Code snippets and samples


## Clusters

- [Create](#clusters_create)
- [Delete](#clusters_delete)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [Update](#clusters_update)

## Datastores

- [Create](#datastores_create)
- [Delete](#datastores_delete)
- [GetByResourceGroup](#datastores_getbyresourcegroup)
- [List](#datastores_list)
- [ListByResourceGroup](#datastores_listbyresourcegroup)
- [Update](#datastores_update)

## Hosts

- [Create](#hosts_create)
- [Delete](#hosts_delete)
- [GetByResourceGroup](#hosts_getbyresourcegroup)
- [List](#hosts_list)
- [ListByResourceGroup](#hosts_listbyresourcegroup)
- [Update](#hosts_update)

## InventoryItems

- [Create](#inventoryitems_create)
- [Delete](#inventoryitems_delete)
- [Get](#inventoryitems_get)
- [ListByVCenter](#inventoryitems_listbyvcenter)

## Operations

- [List](#operations_list)

## ResourcePools

- [Create](#resourcepools_create)
- [Delete](#resourcepools_delete)
- [GetByResourceGroup](#resourcepools_getbyresourcegroup)
- [List](#resourcepools_list)
- [ListByResourceGroup](#resourcepools_listbyresourcegroup)
- [Update](#resourcepools_update)

## VCenters

- [Create](#vcenters_create)
- [Delete](#vcenters_delete)
- [GetByResourceGroup](#vcenters_getbyresourcegroup)
- [List](#vcenters_list)
- [ListByResourceGroup](#vcenters_listbyresourcegroup)
- [Update](#vcenters_update)

## VMInstanceGuestAgents

- [Create](#vminstanceguestagents_create)
- [Delete](#vminstanceguestagents_delete)
- [Get](#vminstanceguestagents_get)
- [List](#vminstanceguestagents_list)

## VirtualMachineInstances

- [CreateOrUpdate](#virtualmachineinstances_createorupdate)
- [Delete](#virtualmachineinstances_delete)
- [Get](#virtualmachineinstances_get)
- [List](#virtualmachineinstances_list)
- [Restart](#virtualmachineinstances_restart)
- [Start](#virtualmachineinstances_start)
- [Stop](#virtualmachineinstances_stop)
- [Update](#virtualmachineinstances_update)

## VirtualMachineTemplates

- [Create](#virtualmachinetemplates_create)
- [Delete](#virtualmachinetemplates_delete)
- [GetByResourceGroup](#virtualmachinetemplates_getbyresourcegroup)
- [List](#virtualmachinetemplates_list)
- [ListByResourceGroup](#virtualmachinetemplates_listbyresourcegroup)
- [Update](#virtualmachinetemplates_update)

## VirtualNetworks

- [Create](#virtualnetworks_create)
- [Delete](#virtualnetworks_delete)
- [GetByResourceGroup](#virtualnetworks_getbyresourcegroup)
- [List](#virtualnetworks_list)
- [ListByResourceGroup](#virtualnetworks_listbyresourcegroup)
- [Update](#virtualnetworks_update)

## VmInstanceHybridIdentityMetadata

- [Get](#vminstancehybrididentitymetadata_get)
- [List](#vminstancehybrididentitymetadata_list)
### Clusters_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for Clusters Create. */
public final class ClustersCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateCluster.json
     */
    /**
     * Sample code: CreateCluster.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createCluster(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .clusters()
            .define("HRCluster")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
            .withVCenterId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VCenters/ContosoVCenter")
            .withMoRefId("aaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .create();
    }
}
```

### Clusters_Delete

```java
/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteCluster.json
     */
    /**
     * Sample code: DeleteCluster.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteCluster(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.clusters().delete("testrg", "HRCluster", null, com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetCluster.json
     */
    /**
     * Sample code: GetCluster.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getCluster(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.clusters().getByResourceGroupWithResponse("testrg", "HRCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_List

```java
/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListClusters.json
     */
    /**
     * Sample code: ListClusters.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listClusters(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.clusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
/** Samples for Clusters ListByResourceGroup. */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListClustersByResourceGroup.json
     */
    /**
     * Sample code: ListClustersByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listClustersByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.clusters().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.resourcemanager.connectedvmware.models.Cluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/UpdateCluster.json
     */
    /**
     * Sample code: UpdateCluster.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateCluster(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("testrg", "HRCluster", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### Datastores_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for Datastores Create. */
public final class DatastoresCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateDatastore.json
     */
    /**
     * Sample code: CreateDatastore.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createDatastore(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .datastores()
            .define("HRDatastore")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
            .withVCenterId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VCenters/ContosoVCenter")
            .withMoRefId("aaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .create();
    }
}
```

### Datastores_Delete

```java
/** Samples for Datastores Delete. */
public final class DatastoresDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteDatastore.json
     */
    /**
     * Sample code: DeleteDatastore.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteDatastore(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.datastores().delete("testrg", "HRDatastore", null, com.azure.core.util.Context.NONE);
    }
}
```

### Datastores_GetByResourceGroup

```java
/** Samples for Datastores GetByResourceGroup. */
public final class DatastoresGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetDatastore.json
     */
    /**
     * Sample code: GetDatastore.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getDatastore(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.datastores().getByResourceGroupWithResponse("testrg", "HRDatastore", com.azure.core.util.Context.NONE);
    }
}
```

### Datastores_List

```java
/** Samples for Datastores List. */
public final class DatastoresListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListDatastores.json
     */
    /**
     * Sample code: ListDatastores.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listDatastores(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.datastores().list(com.azure.core.util.Context.NONE);
    }
}
```

### Datastores_ListByResourceGroup

```java
/** Samples for Datastores ListByResourceGroup. */
public final class DatastoresListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListDatastoresByResourceGroup.json
     */
    /**
     * Sample code: ListDatastoresByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listDatastoresByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.datastores().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### Datastores_Update

```java
import com.azure.resourcemanager.connectedvmware.models.Datastore;
import java.util.HashMap;
import java.util.Map;

/** Samples for Datastores Update. */
public final class DatastoresUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/UpdateDatastore.json
     */
    /**
     * Sample code: UpdateDatastore.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateDatastore(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        Datastore resource =
            manager
                .datastores()
                .getByResourceGroupWithResponse("testrg", "HRDatastore", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### Hosts_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for Hosts Create. */
public final class HostsCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateHost.json
     */
    /**
     * Sample code: CreateHost.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createHost(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .hosts()
            .define("HRHost")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
            .withVCenterId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VCenters/ContosoVCenter")
            .withMoRefId("aaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .create();
    }
}
```

### Hosts_Delete

```java
/** Samples for Hosts Delete. */
public final class HostsDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteHost.json
     */
    /**
     * Sample code: DeleteHost.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteHost(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hosts().delete("testrg", "HRHost", null, com.azure.core.util.Context.NONE);
    }
}
```

### Hosts_GetByResourceGroup

```java
/** Samples for Hosts GetByResourceGroup. */
public final class HostsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetHost.json
     */
    /**
     * Sample code: GetHost.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getHost(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hosts().getByResourceGroupWithResponse("testrg", "HRHost", com.azure.core.util.Context.NONE);
    }
}
```

### Hosts_List

```java
/** Samples for Hosts List. */
public final class HostsListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListHosts.json
     */
    /**
     * Sample code: ListHosts.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listHosts(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hosts().list(com.azure.core.util.Context.NONE);
    }
}
```

### Hosts_ListByResourceGroup

```java
/** Samples for Hosts ListByResourceGroup. */
public final class HostsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListHostsByResourceGroup.json
     */
    /**
     * Sample code: ListHostsByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listHostsByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hosts().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### Hosts_Update

```java
import com.azure.resourcemanager.connectedvmware.models.HostModel;
import java.util.HashMap;
import java.util.Map;

/** Samples for Hosts Update. */
public final class HostsUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/UpdateHost.json
     */
    /**
     * Sample code: UpdateHost.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateHost(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        HostModel resource =
            manager
                .hosts()
                .getByResourceGroupWithResponse("testrg", "HRHost", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### InventoryItems_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ResourcePoolInventoryItem;

/** Samples for InventoryItems Create. */
public final class InventoryItemsCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateInventoryItem.json
     */
    /**
     * Sample code: CreateInventoryItem.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createInventoryItem(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .inventoryItems()
            .define("testItem")
            .withExistingVcenter("testrg", "ContosoVCenter")
            .withProperties(new ResourcePoolInventoryItem())
            .create();
    }
}
```

### InventoryItems_Delete

```java
/** Samples for InventoryItems Delete. */
public final class InventoryItemsDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteInventoryItem.json
     */
    /**
     * Sample code: DeleteInventoryItem.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteInventoryItem(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .inventoryItems()
            .deleteWithResponse("testrg", "ContosoVCenter", "testItem", com.azure.core.util.Context.NONE);
    }
}
```

### InventoryItems_Get

```java
/** Samples for InventoryItems Get. */
public final class InventoryItemsGetSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetInventoryItem.json
     */
    /**
     * Sample code: GetInventoryItem.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getInventoryItem(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .inventoryItems()
            .getWithResponse("testrg", "ContosoVCenter", "testItem", com.azure.core.util.Context.NONE);
    }
}
```

### InventoryItems_ListByVCenter

```java
/** Samples for InventoryItems ListByVCenter. */
public final class InventoryItemsListByVCenterSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/InventoryItems_ListByVCenter.json
     */
    /**
     * Sample code: InventoryItemsListByVCenter.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void inventoryItemsListByVCenter(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.inventoryItems().listByVCenter("testrg", "ContosoVCenter", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listOperations(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourcePools_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for ResourcePools Create. */
public final class ResourcePoolsCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateResourcePool.json
     */
    /**
     * Sample code: CreateResourcePool.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createResourcePool(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .resourcePools()
            .define("HRPool")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
            .withVCenterId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VCenters/ContosoVCenter")
            .withMoRefId("aaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .create();
    }
}
```

### ResourcePools_Delete

```java
/** Samples for ResourcePools Delete. */
public final class ResourcePoolsDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteResourcePool.json
     */
    /**
     * Sample code: DeleteResourcePool.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteResourcePool(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.resourcePools().delete("testrg", "HRPool", null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourcePools_GetByResourceGroup

```java
/** Samples for ResourcePools GetByResourceGroup. */
public final class ResourcePoolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetResourcePool.json
     */
    /**
     * Sample code: GetResourcePool.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getResourcePool(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.resourcePools().getByResourceGroupWithResponse("testrg", "HRPool", com.azure.core.util.Context.NONE);
    }
}
```

### ResourcePools_List

```java
/** Samples for ResourcePools List. */
public final class ResourcePoolsListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListResourcePools.json
     */
    /**
     * Sample code: ListResourcePools.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listResourcePools(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.resourcePools().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourcePools_ListByResourceGroup

```java
/** Samples for ResourcePools ListByResourceGroup. */
public final class ResourcePoolsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListResourcePoolsByResourceGroup.json
     */
    /**
     * Sample code: ListResourcePoolsByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listResourcePoolsByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.resourcePools().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### ResourcePools_Update

```java
import com.azure.resourcemanager.connectedvmware.models.ResourcePool;
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourcePools Update. */
public final class ResourcePoolsUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/UpdateResourcePool.json
     */
    /**
     * Sample code: UpdateResourcePool.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateResourcePool(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        ResourcePool resource =
            manager
                .resourcePools()
                .getByResourceGroupWithResponse("testrg", "HRPool", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VCenters_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;
import com.azure.resourcemanager.connectedvmware.models.VICredential;

/** Samples for VCenters Create. */
public final class VCentersCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateVCenter.json
     */
    /**
     * Sample code: CreateVCenter.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createVCenter(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .vCenters()
            .define("ContosoVCenter")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withFqdn("ContosoVMware.contoso.com")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
            .withPort(1234)
            .withCredentials(new VICredential().withUsername("tempuser").withPassword("fakeTokenPlaceholder"))
            .create();
    }
}
```

### VCenters_Delete

```java
/** Samples for VCenters Delete. */
public final class VCentersDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteVCenter.json
     */
    /**
     * Sample code: DeleteVCenter.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteVCenter(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.vCenters().delete("testrg", "ContosoVCenter", null, com.azure.core.util.Context.NONE);
    }
}
```

### VCenters_GetByResourceGroup

```java
/** Samples for VCenters GetByResourceGroup. */
public final class VCentersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetVCenter.json
     */
    /**
     * Sample code: GetVCenter.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getVCenter(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.vCenters().getByResourceGroupWithResponse("testrg", "ContosoVCenter", com.azure.core.util.Context.NONE);
    }
}
```

### VCenters_List

```java
/** Samples for VCenters List. */
public final class VCentersListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListVCenters.json
     */
    /**
     * Sample code: ListVCenters.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVCenters(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.vCenters().list(com.azure.core.util.Context.NONE);
    }
}
```

### VCenters_ListByResourceGroup

```java
/** Samples for VCenters ListByResourceGroup. */
public final class VCentersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListVCentersByResourceGroup.json
     */
    /**
     * Sample code: ListVCentersByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVCentersByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.vCenters().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### VCenters_Update

```java
import com.azure.resourcemanager.connectedvmware.models.VCenter;
import java.util.HashMap;
import java.util.Map;

/** Samples for VCenters Update. */
public final class VCentersUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/UpdateVCenter.json
     */
    /**
     * Sample code: UpdateVCenter.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateVCenter(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        VCenter resource =
            manager
                .vCenters()
                .getByResourceGroupWithResponse("testrg", "ContosoVCenter", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VMInstanceGuestAgents_Create

```java
import com.azure.resourcemanager.connectedvmware.fluent.models.GuestAgentInner;
import com.azure.resourcemanager.connectedvmware.models.GuestCredential;
import com.azure.resourcemanager.connectedvmware.models.HttpProxyConfiguration;
import com.azure.resourcemanager.connectedvmware.models.ProvisioningAction;

/** Samples for VMInstanceGuestAgents Create. */
public final class VMInstanceGuestAgentsCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateVMInstanceGuestAgent.json
     */
    /**
     * Sample code: CreateGuestAgent.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createGuestAgent(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .vMInstanceGuestAgents()
            .create(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new GuestAgentInner()
                    .withCredentials(
                        new GuestCredential().withUsername("tempuser").withPassword("fakeTokenPlaceholder"))
                    .withPrivateLinkScopeResourceId(
                        "/subscriptions/{subscriptionId}/resourceGroups/myResourceGroup/providers/Microsoft.HybridCompute/privateLinkScopes/privateLinkScopeName")
                    .withHttpProxyConfig(new HttpProxyConfiguration().withHttpsProxy("http://192.1.2.3:8080"))
                    .withProvisioningAction(ProvisioningAction.INSTALL),
                com.azure.core.util.Context.NONE);
    }
}
```

### VMInstanceGuestAgents_Delete

```java
/** Samples for VMInstanceGuestAgents Delete. */
public final class VMInstanceGuestAgentsDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteVMInstanceGuestAgent.json
     */
    /**
     * Sample code: DeleteGuestAgent.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteGuestAgent(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .vMInstanceGuestAgents()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VMInstanceGuestAgents_Get

```java
/** Samples for VMInstanceGuestAgents Get. */
public final class VMInstanceGuestAgentsGetSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetVMInstanceGuestAgent.json
     */
    /**
     * Sample code: GetGuestAgent.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getGuestAgent(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .vMInstanceGuestAgents()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VMInstanceGuestAgents_List

```java
/** Samples for VMInstanceGuestAgents List. */
public final class VMInstanceGuestAgentsListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/VMInstanceGuestAgent_ListByVm.json
     */
    /**
     * Sample code: GuestAgentListByVm.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void guestAgentListByVm(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .vMInstanceGuestAgents()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_CreateOrUpdate

```java
import com.azure.resourcemanager.connectedvmware.fluent.models.VirtualMachineInstanceInner;
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;
import com.azure.resourcemanager.connectedvmware.models.HardwareProfile;
import com.azure.resourcemanager.connectedvmware.models.InfrastructureProfile;
import com.azure.resourcemanager.connectedvmware.models.PlacementProfile;

/** Samples for VirtualMachineInstances CreateOrUpdate. */
public final class VirtualMachineInstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateVirtualMachineInstance.json
     */
    /**
     * Sample code: CreateVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner()
                    .withExtendedLocation(
                        new ExtendedLocation()
                            .withType("customLocation")
                            .withName(
                                "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
                    .withPlacementProfile(
                        new PlacementProfile()
                            .withResourcePoolId(
                                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/ResourcePools/HRPool"))
                    .withHardwareProfile(new HardwareProfile().withMemorySizeMB(4196).withNumCPUs(4))
                    .withInfrastructureProfile(
                        new InfrastructureProfile()
                            .withTemplateId(
                                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VirtualMachineTemplates/WebFrontEndTemplate")
                            .withVCenterId(
                                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VCenters/ContosoVCenter")),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Delete

```java
/** Samples for VirtualMachineInstances Delete. */
public final class VirtualMachineInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteVirtualMachineInstance.json
     */
    /**
     * Sample code: DeleteVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineInstances()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Get

```java
/** Samples for VirtualMachineInstances Get. */
public final class VirtualMachineInstancesGetSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetVirtualMachineInstance.json
     */
    /**
     * Sample code: GetVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineInstances()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_List

```java
/** Samples for VirtualMachineInstances List. */
public final class VirtualMachineInstancesListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListVirtualMachineInstances.json
     */
    /**
     * Sample code: GetVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineInstances()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Restart

```java
/** Samples for VirtualMachineInstances Restart. */
public final class VirtualMachineInstancesRestartSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/RestartVirtualMachineInstance.json
     */
    /**
     * Sample code: RestartVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void restartVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineInstances()
            .restart(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Start

```java
/** Samples for VirtualMachineInstances Start. */
public final class VirtualMachineInstancesStartSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/StartVirtualMachineInstance.json
     */
    /**
     * Sample code: StartVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineInstances()
            .start(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Stop

```java
import com.azure.resourcemanager.connectedvmware.models.StopVirtualMachineOptions;

/** Samples for VirtualMachineInstances Stop. */
public final class VirtualMachineInstancesStopSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/StopVirtualMachineInstance.json
     */
    /**
     * Sample code: StopVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void stopVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineInstances()
            .stop(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new StopVirtualMachineOptions().withSkipShutdown(true),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Update

```java
import com.azure.resourcemanager.connectedvmware.models.HardwareProfile;
import com.azure.resourcemanager.connectedvmware.models.VirtualMachineInstanceUpdate;

/** Samples for VirtualMachineInstances Update. */
public final class VirtualMachineInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/UpdateVirtualMachineInstance.json
     */
    /**
     * Sample code: UpdateVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineInstances()
            .update(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceUpdate()
                    .withHardwareProfile(new HardwareProfile().withMemorySizeMB(4196).withNumCPUs(4)),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for VirtualMachineTemplates Create. */
public final class VirtualMachineTemplatesCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateVirtualMachineTemplate.json
     */
    /**
     * Sample code: CreateVirtualMachineTemplate.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createVirtualMachineTemplate(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineTemplates()
            .define("WebFrontEndTemplate")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
            .withVCenterId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VCenters/ContosoVCenter")
            .withMoRefId("aaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .create();
    }
}
```

### VirtualMachineTemplates_Delete

```java
/** Samples for VirtualMachineTemplates Delete. */
public final class VirtualMachineTemplatesDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteVirtualMachineTemplate.json
     */
    /**
     * Sample code: DeleteVirtualMachineTemplate.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteVirtualMachineTemplate(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineTemplates()
            .delete("testrg", "WebFrontEndTemplate", null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_GetByResourceGroup

```java
/** Samples for VirtualMachineTemplates GetByResourceGroup. */
public final class VirtualMachineTemplatesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetVirtualMachineTemplate.json
     */
    /**
     * Sample code: GetVirtualMachineTemplate.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getVirtualMachineTemplate(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachineTemplates()
            .getByResourceGroupWithResponse("testrg", "WebFrontEndTemplate", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_List

```java
/** Samples for VirtualMachineTemplates List. */
public final class VirtualMachineTemplatesListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListVirtualMachineTemplates.json
     */
    /**
     * Sample code: ListVirtualMachineTemplates.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualMachineTemplates(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachineTemplates().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_ListByResourceGroup

```java
/** Samples for VirtualMachineTemplates ListByResourceGroup. */
public final class VirtualMachineTemplatesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListVirtualMachineTemplatesByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualMachineTemplatesByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualMachineTemplatesByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachineTemplates().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_Update

```java
import com.azure.resourcemanager.connectedvmware.models.VirtualMachineTemplate;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachineTemplates Update. */
public final class VirtualMachineTemplatesUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/UpdateVirtualMachineTemplate.json
     */
    /**
     * Sample code: UpdateVirtualMachineTemplate.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateVirtualMachineTemplate(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        VirtualMachineTemplate resource =
            manager
                .virtualMachineTemplates()
                .getByResourceGroupWithResponse("testrg", "WebFrontEndTemplate", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VirtualNetworks_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for VirtualNetworks Create. */
public final class VirtualNetworksCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/CreateVirtualNetwork.json
     */
    /**
     * Sample code: CreateVirtualNetwork.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createVirtualNetwork(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualNetworks()
            .define("ProdNetwork")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
            .withVCenterId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VCenters/ContosoVCenter")
            .withMoRefId("aaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .create();
    }
}
```

### VirtualNetworks_Delete

```java
/** Samples for VirtualNetworks Delete. */
public final class VirtualNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/DeleteVirtualNetwork.json
     */
    /**
     * Sample code: DeleteVirtualNetwork.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteVirtualNetwork(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualNetworks().delete("testrg", "ProdNetwork", null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_GetByResourceGroup

```java
/** Samples for VirtualNetworks GetByResourceGroup. */
public final class VirtualNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetVirtualNetwork.json
     */
    /**
     * Sample code: GetVirtualNetwork.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getVirtualNetwork(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualNetworks()
            .getByResourceGroupWithResponse("testrg", "ProdNetwork", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_List

```java
/** Samples for VirtualNetworks List. */
public final class VirtualNetworksListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListVirtualNetworks.json
     */
    /**
     * Sample code: ListVirtualNetworks.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualNetworks(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualNetworks().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_ListByResourceGroup

```java
/** Samples for VirtualNetworks ListByResourceGroup. */
public final class VirtualNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/ListVirtualNetworksByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualNetworksByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualNetworksByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualNetworks().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_Update

```java
import com.azure.resourcemanager.connectedvmware.models.VirtualNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworks Update. */
public final class VirtualNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/UpdateVirtualNetwork.json
     */
    /**
     * Sample code: UpdateVirtualNetwork.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateVirtualNetwork(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        VirtualNetwork resource =
            manager
                .virtualNetworks()
                .getByResourceGroupWithResponse("testrg", "ProdNetwork", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VmInstanceHybridIdentityMetadata_Get

```java
/** Samples for VmInstanceHybridIdentityMetadata Get. */
public final class VmInstanceHybridIdentityMetadataGetSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/GetVmInstanceHybridIdentityMetadata.json
     */
    /**
     * Sample code: GetHybridIdentityMetadata.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getHybridIdentityMetadata(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .vmInstanceHybridIdentityMetadatas()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VmInstanceHybridIdentityMetadata_List

```java
/** Samples for VmInstanceHybridIdentityMetadata List. */
public final class VmInstanceHybridIdentityMetadataListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/stable/2023-10-01/examples/HybridIdentityMetadata_ListByVmInstance.json
     */
    /**
     * Sample code: HybridIdentityMetadataListByVm.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void hybridIdentityMetadataListByVm(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .vmInstanceHybridIdentityMetadatas()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

