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

## GuestAgents

- [Create](#guestagents_create)
- [Delete](#guestagents_delete)
- [Get](#guestagents_get)
- [ListByVm](#guestagents_listbyvm)

## Hosts

- [Create](#hosts_create)
- [Delete](#hosts_delete)
- [GetByResourceGroup](#hosts_getbyresourcegroup)
- [List](#hosts_list)
- [ListByResourceGroup](#hosts_listbyresourcegroup)
- [Update](#hosts_update)

## HybridIdentityMetadata

- [Create](#hybrididentitymetadata_create)
- [Delete](#hybrididentitymetadata_delete)
- [Get](#hybrididentitymetadata_get)
- [ListByVm](#hybrididentitymetadata_listbyvm)

## InventoryItems

- [Create](#inventoryitems_create)
- [Delete](#inventoryitems_delete)
- [Get](#inventoryitems_get)
- [ListByVCenter](#inventoryitems_listbyvcenter)

## MachineExtensions

- [CreateOrUpdate](#machineextensions_createorupdate)
- [Delete](#machineextensions_delete)
- [Get](#machineextensions_get)
- [List](#machineextensions_list)
- [Update](#machineextensions_update)

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

## VirtualMachineTemplates

- [Create](#virtualmachinetemplates_create)
- [Delete](#virtualmachinetemplates_delete)
- [GetByResourceGroup](#virtualmachinetemplates_getbyresourcegroup)
- [List](#virtualmachinetemplates_list)
- [ListByResourceGroup](#virtualmachinetemplates_listbyresourcegroup)
- [Update](#virtualmachinetemplates_update)

## VirtualMachines

- [AssessPatches](#virtualmachines_assesspatches)
- [Create](#virtualmachines_create)
- [Delete](#virtualmachines_delete)
- [GetByResourceGroup](#virtualmachines_getbyresourcegroup)
- [InstallPatches](#virtualmachines_installpatches)
- [List](#virtualmachines_list)
- [ListByResourceGroup](#virtualmachines_listbyresourcegroup)
- [Restart](#virtualmachines_restart)
- [Start](#virtualmachines_start)
- [Stop](#virtualmachines_stop)
- [Update](#virtualmachines_update)

## VirtualNetworks

- [Create](#virtualnetworks_create)
- [Delete](#virtualnetworks_delete)
- [GetByResourceGroup](#virtualnetworks_getbyresourcegroup)
- [List](#virtualnetworks_list)
- [ListByResourceGroup](#virtualnetworks_listbyresourcegroup)
- [Update](#virtualnetworks_update)
### Clusters_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for Clusters Create. */
public final class ClustersCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateCluster.json
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
import com.azure.core.util.Context;

/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteCluster.json
     */
    /**
     * Sample code: DeleteCluster.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteCluster(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.clusters().delete("testrg", "HRCluster", null, Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetCluster.json
     */
    /**
     * Sample code: GetCluster.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getCluster(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.clusters().getByResourceGroupWithResponse("testrg", "HRCluster", Context.NONE);
    }
}
```

### Clusters_List

```java
import com.azure.core.util.Context;

/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListClusters.json
     */
    /**
     * Sample code: ListClusters.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listClusters(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.clusters().list(Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListByResourceGroup. */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListClustersByResourceGroup.json
     */
    /**
     * Sample code: ListClustersByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listClustersByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.clusters().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.Cluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/UpdateCluster.json
     */
    /**
     * Sample code: UpdateCluster.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateCluster(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        Cluster resource =
            manager.clusters().getByResourceGroupWithResponse("testrg", "HRCluster", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### Datastores_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for Datastores Create. */
public final class DatastoresCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateDatastore.json
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
import com.azure.core.util.Context;

/** Samples for Datastores Delete. */
public final class DatastoresDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteDatastore.json
     */
    /**
     * Sample code: DeleteDatastore.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteDatastore(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.datastores().delete("testrg", "HRDatastore", null, Context.NONE);
    }
}
```

### Datastores_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Datastores GetByResourceGroup. */
public final class DatastoresGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetDatastore.json
     */
    /**
     * Sample code: GetDatastore.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getDatastore(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.datastores().getByResourceGroupWithResponse("testrg", "HRDatastore", Context.NONE);
    }
}
```

### Datastores_List

```java
import com.azure.core.util.Context;

/** Samples for Datastores List. */
public final class DatastoresListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListDatastores.json
     */
    /**
     * Sample code: ListDatastores.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listDatastores(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.datastores().list(Context.NONE);
    }
}
```

### Datastores_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Datastores ListByResourceGroup. */
public final class DatastoresListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListDatastoresByResourceGroup.json
     */
    /**
     * Sample code: ListDatastoresByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listDatastoresByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.datastores().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### Datastores_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.Datastore;
import java.util.HashMap;
import java.util.Map;

/** Samples for Datastores Update. */
public final class DatastoresUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/UpdateDatastore.json
     */
    /**
     * Sample code: UpdateDatastore.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateDatastore(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        Datastore resource =
            manager.datastores().getByResourceGroupWithResponse("testrg", "HRDatastore", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### GuestAgents_Create

```java
import com.azure.resourcemanager.connectedvmware.models.GuestCredential;
import com.azure.resourcemanager.connectedvmware.models.HttpProxyConfiguration;
import com.azure.resourcemanager.connectedvmware.models.ProvisioningAction;

/** Samples for GuestAgents Create. */
public final class GuestAgentsCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateGuestAgent.json
     */
    /**
     * Sample code: CreateGuestAgent.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createGuestAgent(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .guestAgents()
            .define("default")
            .withExistingVirtualMachine("testrg", "ContosoVm")
            .withCredentials(new GuestCredential().withUsername("tempuser").withPassword("<password>"))
            .withHttpProxyConfig(new HttpProxyConfiguration().withHttpsProxy("http://192.1.2.3:8080"))
            .withProvisioningAction(ProvisioningAction.INSTALL)
            .create();
    }
}
```

### GuestAgents_Delete

```java
import com.azure.core.util.Context;

/** Samples for GuestAgents Delete. */
public final class GuestAgentsDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteGuestAgent.json
     */
    /**
     * Sample code: DeleteGuestAgent.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteGuestAgent(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.guestAgents().delete("testrg", "ContosoVm", "default", Context.NONE);
    }
}
```

### GuestAgents_Get

```java
import com.azure.core.util.Context;

/** Samples for GuestAgents Get. */
public final class GuestAgentsGetSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetGuestAgent.json
     */
    /**
     * Sample code: GetGuestAgent.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getGuestAgent(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.guestAgents().getWithResponse("testrg", "ContosoVm", "default", Context.NONE);
    }
}
```

### GuestAgents_ListByVm

```java
import com.azure.core.util.Context;

/** Samples for GuestAgents ListByVm. */
public final class GuestAgentsListByVmSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GuestAgent_ListByVm.json
     */
    /**
     * Sample code: GuestAgentListByVm.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void guestAgentListByVm(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.guestAgents().listByVm("testrg", "ContosoVm", Context.NONE);
    }
}
```

### Hosts_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for Hosts Create. */
public final class HostsCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateHost.json
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
import com.azure.core.util.Context;

/** Samples for Hosts Delete. */
public final class HostsDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteHost.json
     */
    /**
     * Sample code: DeleteHost.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteHost(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hosts().delete("testrg", "HRHost", null, Context.NONE);
    }
}
```

### Hosts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Hosts GetByResourceGroup. */
public final class HostsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetHost.json
     */
    /**
     * Sample code: GetHost.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getHost(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hosts().getByResourceGroupWithResponse("testrg", "HRHost", Context.NONE);
    }
}
```

### Hosts_List

```java
import com.azure.core.util.Context;

/** Samples for Hosts List. */
public final class HostsListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListHosts.json
     */
    /**
     * Sample code: ListHosts.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listHosts(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hosts().list(Context.NONE);
    }
}
```

### Hosts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Hosts ListByResourceGroup. */
public final class HostsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListHostsByResourceGroup.json
     */
    /**
     * Sample code: ListHostsByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listHostsByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hosts().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### Hosts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.HostModel;
import java.util.HashMap;
import java.util.Map;

/** Samples for Hosts Update. */
public final class HostsUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/UpdateHost.json
     */
    /**
     * Sample code: UpdateHost.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateHost(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        HostModel resource =
            manager.hosts().getByResourceGroupWithResponse("testrg", "HRHost", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### HybridIdentityMetadata_Create

```java
/** Samples for HybridIdentityMetadata Create. */
public final class HybridIdentityMetadataCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateHybridIdentityMetadata.json
     */
    /**
     * Sample code: CreateHybridIdentityMetadata.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createHybridIdentityMetadata(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .hybridIdentityMetadatas()
            .define("default")
            .withExistingVirtualMachine("testrg", "ContosoVm")
            .withVmId("f8b82dff-38ef-4220-99ef-d3a3f86ddc6c")
            .withPublicKey("8ec7d60c-9700-40b1-8e6e-e5b2f6f477f2")
            .create();
    }
}
```

### HybridIdentityMetadata_Delete

```java
import com.azure.core.util.Context;

/** Samples for HybridIdentityMetadata Delete. */
public final class HybridIdentityMetadataDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteHybridIdentityMetadata.json
     */
    /**
     * Sample code: DeleteHybridIdentityMetadata.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteHybridIdentityMetadata(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hybridIdentityMetadatas().deleteWithResponse("testrg", "ContosoVm", "default", Context.NONE);
    }
}
```

### HybridIdentityMetadata_Get

```java
import com.azure.core.util.Context;

/** Samples for HybridIdentityMetadata Get. */
public final class HybridIdentityMetadataGetSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetHybridIdentityMetadata.json
     */
    /**
     * Sample code: GetHybridIdentityMetadata.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getHybridIdentityMetadata(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hybridIdentityMetadatas().getWithResponse("testrg", "ContosoVm", "default", Context.NONE);
    }
}
```

### HybridIdentityMetadata_ListByVm

```java
import com.azure.core.util.Context;

/** Samples for HybridIdentityMetadata ListByVm. */
public final class HybridIdentityMetadataListByVmSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/HybridIdentityMetadata_ListByVm.json
     */
    /**
     * Sample code: HybridIdentityMetadataListByVm.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void hybridIdentityMetadataListByVm(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.hybridIdentityMetadatas().listByVm("testrg", "ContosoVm", Context.NONE);
    }
}
```

### InventoryItems_Create

```java
/** Samples for InventoryItems Create. */
public final class InventoryItemsCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateInventoryItem.json
     */
    /**
     * Sample code: CreateInventoryItem.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createInventoryItem(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.inventoryItems().define("testItem").withExistingVcenter("testrg", "ContosoVCenter").create();
    }
}
```

### InventoryItems_Delete

```java
import com.azure.core.util.Context;

/** Samples for InventoryItems Delete. */
public final class InventoryItemsDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteInventoryItem.json
     */
    /**
     * Sample code: DeleteInventoryItem.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteInventoryItem(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.inventoryItems().deleteWithResponse("testrg", "ContosoVCenter", "testItem", Context.NONE);
    }
}
```

### InventoryItems_Get

```java
import com.azure.core.util.Context;

/** Samples for InventoryItems Get. */
public final class InventoryItemsGetSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetInventoryItem.json
     */
    /**
     * Sample code: GetInventoryItem.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getInventoryItem(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.inventoryItems().getWithResponse("testrg", "ContosoVCenter", "testItem", Context.NONE);
    }
}
```

### InventoryItems_ListByVCenter

```java
import com.azure.core.util.Context;

/** Samples for InventoryItems ListByVCenter. */
public final class InventoryItemsListByVCenterSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/InventoryItems_ListByVCenter.json
     */
    /**
     * Sample code: InventoryItemsListByVCenter.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void inventoryItemsListByVCenter(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.inventoryItems().listByVCenter("testrg", "ContosoVCenter", Context.NONE);
    }
}
```

### MachineExtensions_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/** Samples for MachineExtensions CreateOrUpdate. */
public final class MachineExtensionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/PUTExtension.json
     */
    /**
     * Sample code: Create or Update a Machine Extension (PUT).
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createOrUpdateAMachineExtensionPUT(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) throws IOException {
        manager
            .machineExtensions()
            .define("CustomScriptExtension")
            .withRegion("eastus2euap")
            .withExistingVirtualMachine("myResourceGroup", "myMachine")
            .withPublisher("Microsoft.Compute")
            .withTypePropertiesType("CustomScriptExtension")
            .withTypeHandlerVersion("1.10")
            .withSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"commandToExecute\":\"powershell.exe -c \\\"Get-Process | Where-Object { $_.CPU -gt 10000"
                            + " }\\\"\"}",
                        Object.class,
                        SerializerEncoding.JSON))
            .create();
    }
}
```

### MachineExtensions_Delete

```java
import com.azure.core.util.Context;

/** Samples for MachineExtensions Delete. */
public final class MachineExtensionsDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DELETEExtension.json
     */
    /**
     * Sample code: Delete a Machine Extension.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteAMachineExtension(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.machineExtensions().delete("myResourceGroup", "myMachine", "MMA", Context.NONE);
    }
}
```

### MachineExtensions_Get

```java
import com.azure.core.util.Context;

/** Samples for MachineExtensions Get. */
public final class MachineExtensionsGetSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GETExtension.json
     */
    /**
     * Sample code: Get Machine Extension.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getMachineExtension(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .machineExtensions()
            .getWithResponse("myResourceGroup", "myMachine", "CustomScriptExtension", Context.NONE);
    }
}
```

### MachineExtensions_List

```java
import com.azure.core.util.Context;

/** Samples for MachineExtensions List. */
public final class MachineExtensionsListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/LISTExtension.json
     */
    /**
     * Sample code: Get all Machine Extensions.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getAllMachineExtensions(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.machineExtensions().list("myResourceGroup", "myMachine", null, Context.NONE);
    }
}
```

### MachineExtensions_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.connectedvmware.models.MachineExtension;
import java.io.IOException;

/** Samples for MachineExtensions Update. */
public final class MachineExtensionsUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/UpdateExtension.json
     */
    /**
     * Sample code: Create or Update a Machine Extension (PATCH).
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createOrUpdateAMachineExtensionPATCH(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) throws IOException {
        MachineExtension resource =
            manager
                .machineExtensions()
                .getWithResponse("myResourceGroup", "myMachine", "CustomScriptExtension", Context.NONE)
                .getValue();
        resource
            .update()
            .withPublisher("Microsoft.Compute")
            .withType("CustomScriptExtension")
            .withTypeHandlerVersion("1.10")
            .withSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"commandToExecute\":\"powershell.exe -c \\\"Get-Process | Where-Object { $_.CPU -lt 100"
                            + " }\\\"\"}",
                        Object.class,
                        SerializerEncoding.JSON))
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
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listOperations(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ResourcePools_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for ResourcePools Create. */
public final class ResourcePoolsCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateResourcePool.json
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
import com.azure.core.util.Context;

/** Samples for ResourcePools Delete. */
public final class ResourcePoolsDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteResourcePool.json
     */
    /**
     * Sample code: DeleteResourcePool.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteResourcePool(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.resourcePools().delete("testrg", "HRPool", null, Context.NONE);
    }
}
```

### ResourcePools_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ResourcePools GetByResourceGroup. */
public final class ResourcePoolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetResourcePool.json
     */
    /**
     * Sample code: GetResourcePool.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getResourcePool(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.resourcePools().getByResourceGroupWithResponse("testrg", "HRPool", Context.NONE);
    }
}
```

### ResourcePools_List

```java
import com.azure.core.util.Context;

/** Samples for ResourcePools List. */
public final class ResourcePoolsListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListResourcePools.json
     */
    /**
     * Sample code: ListResourcePools.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listResourcePools(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.resourcePools().list(Context.NONE);
    }
}
```

### ResourcePools_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ResourcePools ListByResourceGroup. */
public final class ResourcePoolsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListResourcePoolsByResourceGroup.json
     */
    /**
     * Sample code: ListResourcePoolsByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listResourcePoolsByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.resourcePools().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### ResourcePools_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.ResourcePool;
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourcePools Update. */
public final class ResourcePoolsUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/UpdateResourcePool.json
     */
    /**
     * Sample code: UpdateResourcePool.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateResourcePool(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        ResourcePool resource =
            manager.resourcePools().getByResourceGroupWithResponse("testrg", "HRPool", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VCenters_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;
import com.azure.resourcemanager.connectedvmware.models.VICredential;

/** Samples for VCenters Create. */
public final class VCentersCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateVCenter.json
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
            .withCredentials(new VICredential().withUsername("tempuser").withPassword("<password>"))
            .create();
    }
}
```

### VCenters_Delete

```java
import com.azure.core.util.Context;

/** Samples for VCenters Delete. */
public final class VCentersDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteVCenter.json
     */
    /**
     * Sample code: DeleteVCenter.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteVCenter(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.vCenters().delete("testrg", "ContosoVCenter", null, Context.NONE);
    }
}
```

### VCenters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VCenters GetByResourceGroup. */
public final class VCentersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetVCenter.json
     */
    /**
     * Sample code: GetVCenter.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getVCenter(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.vCenters().getByResourceGroupWithResponse("testrg", "ContosoVCenter", Context.NONE);
    }
}
```

### VCenters_List

```java
import com.azure.core.util.Context;

/** Samples for VCenters List. */
public final class VCentersListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListVCenters.json
     */
    /**
     * Sample code: ListVCenters.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVCenters(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.vCenters().list(Context.NONE);
    }
}
```

### VCenters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VCenters ListByResourceGroup. */
public final class VCentersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListVCentersByResourceGroup.json
     */
    /**
     * Sample code: ListVCentersByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVCentersByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.vCenters().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### VCenters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.VCenter;
import java.util.HashMap;
import java.util.Map;

/** Samples for VCenters Update. */
public final class VCentersUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/UpdateVCenter.json
     */
    /**
     * Sample code: UpdateVCenter.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateVCenter(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        VCenter resource =
            manager.vCenters().getByResourceGroupWithResponse("testrg", "ContosoVCenter", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VirtualMachineTemplates_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for VirtualMachineTemplates Create. */
public final class VirtualMachineTemplatesCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateVirtualMachineTemplate.json
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
import com.azure.core.util.Context;

/** Samples for VirtualMachineTemplates Delete. */
public final class VirtualMachineTemplatesDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteVirtualMachineTemplate.json
     */
    /**
     * Sample code: DeleteVirtualMachineTemplate.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteVirtualMachineTemplate(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachineTemplates().delete("testrg", "WebFrontEndTemplate", null, Context.NONE);
    }
}
```

### VirtualMachineTemplates_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineTemplates GetByResourceGroup. */
public final class VirtualMachineTemplatesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetVirtualMachineTemplate.json
     */
    /**
     * Sample code: GetVirtualMachineTemplate.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getVirtualMachineTemplate(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachineTemplates().getByResourceGroupWithResponse("testrg", "WebFrontEndTemplate", Context.NONE);
    }
}
```

### VirtualMachineTemplates_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineTemplates List. */
public final class VirtualMachineTemplatesListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListVirtualMachineTemplates.json
     */
    /**
     * Sample code: ListVirtualMachineTemplates.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualMachineTemplates(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachineTemplates().list(Context.NONE);
    }
}
```

### VirtualMachineTemplates_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineTemplates ListByResourceGroup. */
public final class VirtualMachineTemplatesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListVirtualMachineTemplatesByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualMachineTemplatesByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualMachineTemplatesByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachineTemplates().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### VirtualMachineTemplates_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.VirtualMachineTemplate;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachineTemplates Update. */
public final class VirtualMachineTemplatesUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/UpdateVirtualMachineTemplate.json
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
                .getByResourceGroupWithResponse("testrg", "WebFrontEndTemplate", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VirtualMachines_AssessPatches

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines AssessPatches. */
public final class VirtualMachinesAssessPatchesSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/VirtualMachineAssessPatches.json
     */
    /**
     * Sample code: Assess patch state of a machine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void assessPatchStateOfAMachine(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachines().assessPatches("myResourceGroupName", "myMachineName", Context.NONE);
    }
}
```

### VirtualMachines_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;
import com.azure.resourcemanager.connectedvmware.models.HardwareProfile;

/** Samples for VirtualMachines Create. */
public final class VirtualMachinesCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateVirtualMachine.json
     */
    /**
     * Sample code: CreateVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void createVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachines()
            .define("DemoVM")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
            .withResourcePoolId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/ResourcePools/HRPool")
            .withTemplateId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VirtualMachineTemplates/WebFrontEndTemplate")
            .withVCenterId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ConnectedVMwarevSphere/VCenters/ContosoVCenter")
            .withHardwareProfile(new HardwareProfile().withMemorySizeMB(4196).withNumCPUs(4))
            .create();
    }
}
```

### VirtualMachines_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Delete. */
public final class VirtualMachinesDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteVirtualMachine.json
     */
    /**
     * Sample code: DeleteVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachines().delete("testrg", "DemoVM", null, null, Context.NONE);
    }
}
```

### VirtualMachines_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines GetByResourceGroup. */
public final class VirtualMachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetVirtualMachine.json
     */
    /**
     * Sample code: GetVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachines().getByResourceGroupWithResponse("testrg", "DemoVM", Context.NONE);
    }
}
```

### VirtualMachines_InstallPatches

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.VMGuestPatchClassificationWindows;
import com.azure.resourcemanager.connectedvmware.models.VMGuestPatchRebootSetting;
import com.azure.resourcemanager.connectedvmware.models.VirtualMachineInstallPatchesParameters;
import com.azure.resourcemanager.connectedvmware.models.WindowsParameters;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for VirtualMachines InstallPatches. */
public final class VirtualMachinesInstallPatchesSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/VirtualMachineInstallPatches.json
     */
    /**
     * Sample code: Install patch state of a machine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void installPatchStateOfAMachine(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachines()
            .installPatches(
                "myResourceGroupName",
                "myMachineName",
                new VirtualMachineInstallPatchesParameters()
                    .withMaximumDuration("PT3H")
                    .withRebootSetting(VMGuestPatchRebootSetting.IF_REQUIRED)
                    .withWindowsParameters(
                        new WindowsParameters()
                            .withClassificationsToInclude(
                                Arrays
                                    .asList(
                                        VMGuestPatchClassificationWindows.CRITICAL,
                                        VMGuestPatchClassificationWindows.SECURITY))
                            .withMaxPatchPublishDate(OffsetDateTime.parse("2022-01-15T02:36:43.0539904+00:00"))),
                Context.NONE);
    }
}
```

### VirtualMachines_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines List. */
public final class VirtualMachinesListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListVirtualMachines.json
     */
    /**
     * Sample code: ListVirtualMachines.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualMachines(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachines().list(Context.NONE);
    }
}
```

### VirtualMachines_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines ListByResourceGroup. */
public final class VirtualMachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListVirtualMachinesByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualMachinesByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualMachinesByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachines().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### VirtualMachines_Restart

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Restart. */
public final class VirtualMachinesRestartSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/RestartVirtualMachine.json
     */
    /**
     * Sample code: RestartVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void restartVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachines().restart("testrg", "DemoVM", Context.NONE);
    }
}
```

### VirtualMachines_Start

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Start. */
public final class VirtualMachinesStartSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/StartVirtualMachine.json
     */
    /**
     * Sample code: StartVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualMachines().start("testrg", "DemoVM", Context.NONE);
    }
}
```

### VirtualMachines_Stop

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.StopVirtualMachineOptions;

/** Samples for VirtualMachines Stop. */
public final class VirtualMachinesStopSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/StopVirtualMachine.json
     */
    /**
     * Sample code: StopVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void stopVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager
            .virtualMachines()
            .stop("testrg", "DemoVM", new StopVirtualMachineOptions().withSkipShutdown(true), Context.NONE);
    }
}
```

### VirtualMachines_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.VirtualMachine;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachines Update. */
public final class VirtualMachinesUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/UpdateVirtualMachine.json
     */
    /**
     * Sample code: UpdateVirtualMachine.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateVirtualMachine(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        VirtualMachine resource =
            manager.virtualMachines().getByResourceGroupWithResponse("testrg", "DemoVM", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VirtualNetworks_Create

```java
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;

/** Samples for VirtualNetworks Create. */
public final class VirtualNetworksCreateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/CreateVirtualNetwork.json
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
import com.azure.core.util.Context;

/** Samples for VirtualNetworks Delete. */
public final class VirtualNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/DeleteVirtualNetwork.json
     */
    /**
     * Sample code: DeleteVirtualNetwork.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void deleteVirtualNetwork(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualNetworks().delete("testrg", "ProdNetwork", null, Context.NONE);
    }
}
```

### VirtualNetworks_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks GetByResourceGroup. */
public final class VirtualNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/GetVirtualNetwork.json
     */
    /**
     * Sample code: GetVirtualNetwork.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void getVirtualNetwork(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualNetworks().getByResourceGroupWithResponse("testrg", "ProdNetwork", Context.NONE);
    }
}
```

### VirtualNetworks_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks List. */
public final class VirtualNetworksListSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListVirtualNetworks.json
     */
    /**
     * Sample code: ListVirtualNetworks.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualNetworks(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualNetworks().list(Context.NONE);
    }
}
```

### VirtualNetworks_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks ListByResourceGroup. */
public final class VirtualNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/ListVirtualNetworksByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualNetworksByResourceGroup.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void listVirtualNetworksByResourceGroup(
        com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        manager.virtualNetworks().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### VirtualNetworks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.connectedvmware.models.VirtualNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworks Update. */
public final class VirtualNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/connectedvmware/resource-manager/Microsoft.ConnectedVMwarevSphere/preview/2022-01-10-preview/examples/UpdateVirtualNetwork.json
     */
    /**
     * Sample code: UpdateVirtualNetwork.
     *
     * @param manager Entry point to ConnectedVMwareManager.
     */
    public static void updateVirtualNetwork(com.azure.resourcemanager.connectedvmware.ConnectedVMwareManager manager) {
        VirtualNetwork resource =
            manager.virtualNetworks().getByResourceGroupWithResponse("testrg", "ProdNetwork", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

