# Code snippets and samples


## DiskPoolZones

- [List](#diskpoolzones_list)

## DiskPools

- [CreateOrUpdate](#diskpools_createorupdate)
- [Deallocate](#diskpools_deallocate)
- [Delete](#diskpools_delete)
- [GetByResourceGroup](#diskpools_getbyresourcegroup)
- [List](#diskpools_list)
- [ListByResourceGroup](#diskpools_listbyresourcegroup)
- [ListOutboundNetworkDependenciesEndpoints](#diskpools_listoutboundnetworkdependenciesendpoints)
- [Start](#diskpools_start)
- [Update](#diskpools_update)
- [Upgrade](#diskpools_upgrade)

## IscsiTargets

- [CreateOrUpdate](#iscsitargets_createorupdate)
- [Delete](#iscsitargets_delete)
- [Get](#iscsitargets_get)
- [ListByDiskPool](#iscsitargets_listbydiskpool)
- [Update](#iscsitargets_update)

## Operations

- [List](#operations_list)

## ResourceSkus

- [List](#resourceskus_list)
### DiskPoolZones_List

```java
import com.azure.core.util.Context;

/** Samples for DiskPoolZones List. */
public final class DiskPoolZonesListSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPoolZones_List.json
     */
    /**
     * Sample code: List Disk Pool Zones.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void listDiskPoolZones(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.diskPoolZones().list("eastus", Context.NONE);
    }
}
```

### DiskPools_CreateOrUpdate

```java
import com.azure.resourcemanager.storagepool.fluent.models.Sku;
import com.azure.resourcemanager.storagepool.models.Disk;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for DiskPools CreateOrUpdate. */
public final class DiskPoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_Put.json
     */
    /**
     * Sample code: Create or Update Disk pool.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void createOrUpdateDiskPool(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager
            .diskPools()
            .define("myDiskPool")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName("Basic_V1").withTier("Basic"))
            .withSubnetId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myvnet/subnets/mysubnet")
            .withTags(mapOf("key", "value"))
            .withAvailabilityZones(Arrays.asList("1"))
            .withDisks(
                Arrays
                    .asList(
                        new Disk()
                            .withId(
                                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myResourceGroup/providers/Microsoft.Compute/disks/vm-name_DataDisk_0"),
                        new Disk()
                            .withId(
                                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myResourceGroup/providers/Microsoft.Compute/disks/vm-name_DataDisk_1")))
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

### DiskPools_Deallocate

```java
import com.azure.core.util.Context;

/** Samples for DiskPools Deallocate. */
public final class DiskPoolsDeallocateSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_Deallocate.json
     */
    /**
     * Sample code: Deallocate Disk Pool.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void deallocateDiskPool(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.diskPools().deallocate("myResourceGroup", "myDiskPool", Context.NONE);
    }
}
```

### DiskPools_Delete

```java
import com.azure.core.util.Context;

/** Samples for DiskPools Delete. */
public final class DiskPoolsDeleteSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_Delete.json
     */
    /**
     * Sample code: Delete Disk pool.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void deleteDiskPool(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.diskPools().delete("myResourceGroup", "myDiskPool", Context.NONE);
    }
}
```

### DiskPools_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DiskPools GetByResourceGroup. */
public final class DiskPoolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_Get.json
     */
    /**
     * Sample code: Get Disk pool.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void getDiskPool(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.diskPools().getByResourceGroupWithResponse("myResourceGroup", "myDiskPool", Context.NONE);
    }
}
```

### DiskPools_List

```java
import com.azure.core.util.Context;

/** Samples for DiskPools List. */
public final class DiskPoolsListSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_ListBySubscription.json
     */
    /**
     * Sample code: List Disk Pools by subscription.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void listDiskPoolsBySubscription(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.diskPools().list(Context.NONE);
    }
}
```

### DiskPools_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DiskPools ListByResourceGroup. */
public final class DiskPoolsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_ListByResourceGroup.json
     */
    /**
     * Sample code: List Disk Pools.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void listDiskPools(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.diskPools().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### DiskPools_ListOutboundNetworkDependenciesEndpoints

```java
import com.azure.core.util.Context;

/** Samples for DiskPools ListOutboundNetworkDependenciesEndpoints. */
public final class DiskPoolsListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_GetOutboundNetworkDependencies.json
     */
    /**
     * Sample code: Get Disk Pool outbound network dependencies.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void getDiskPoolOutboundNetworkDependencies(
        com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager
            .diskPools()
            .listOutboundNetworkDependenciesEndpoints("Sample-WestUSResourceGroup", "SampleAse", Context.NONE);
    }
}
```

### DiskPools_Start

```java
import com.azure.core.util.Context;

/** Samples for DiskPools Start. */
public final class DiskPoolsStartSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_Start.json
     */
    /**
     * Sample code: Start Disk Pool.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void startDiskPool(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.diskPools().start("myResourceGroup", "myDiskPool", Context.NONE);
    }
}
```

### DiskPools_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storagepool.fluent.models.Sku;
import com.azure.resourcemanager.storagepool.models.Disk;
import com.azure.resourcemanager.storagepool.models.DiskPool;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for DiskPools Update. */
public final class DiskPoolsUpdateSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_Patch.json
     */
    /**
     * Sample code: Update Disk pool.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void updateDiskPool(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        DiskPool resource =
            manager
                .diskPools()
                .getByResourceGroupWithResponse("myResourceGroup", "myDiskPool", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key", "value"))
            .withSku(new Sku().withName("Basic_B1").withTier("Basic"))
            .withDisks(
                Arrays
                    .asList(
                        new Disk()
                            .withId(
                                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myResourceGroup/providers/Microsoft.Compute/disks/vm-name_DataDisk_0"),
                        new Disk()
                            .withId(
                                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myResourceGroup/providers/Microsoft.Compute/disks/vm-name_DataDisk_1")))
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

### DiskPools_Upgrade

```java
import com.azure.core.util.Context;

/** Samples for DiskPools Upgrade. */
public final class DiskPoolsUpgradeSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/DiskPools_Upgrade.json
     */
    /**
     * Sample code: Upgrade Disk Pool.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void upgradeDiskPool(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.diskPools().upgrade("myResourceGroup", "myDiskPool", Context.NONE);
    }
}
```

### IscsiTargets_CreateOrUpdate

```java
import com.azure.resourcemanager.storagepool.models.IscsiLun;
import com.azure.resourcemanager.storagepool.models.IscsiTargetAclMode;
import java.util.Arrays;

/** Samples for IscsiTargets CreateOrUpdate. */
public final class IscsiTargetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/IscsiTargets_Put.json
     */
    /**
     * Sample code: Create or Update iSCSI Target.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void createOrUpdateISCSITarget(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager
            .iscsiTargets()
            .define("myIscsiTarget")
            .withExistingDiskPool("myResourceGroup", "myDiskPool")
            .withAclMode(IscsiTargetAclMode.DYNAMIC)
            .withTargetIqn("iqn.2005-03.org.iscsi:server1")
            .withLuns(
                Arrays
                    .asList(
                        new IscsiLun()
                            .withName("lun0")
                            .withManagedDiskAzureResourceId(
                                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myResourceGroup/providers/Microsoft.Compute/disks/vm-name_DataDisk_1")))
            .create();
    }
}
```

### IscsiTargets_Delete

```java
import com.azure.core.util.Context;

/** Samples for IscsiTargets Delete. */
public final class IscsiTargetsDeleteSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/IscsiTargets_Delete.json
     */
    /**
     * Sample code: Delete iSCSI Target.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void deleteISCSITarget(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.iscsiTargets().delete("myResourceGroup", "myDiskPool", "myIscsiTarget", Context.NONE);
    }
}
```

### IscsiTargets_Get

```java
import com.azure.core.util.Context;

/** Samples for IscsiTargets Get. */
public final class IscsiTargetsGetSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/IscsiTargets_Get.json
     */
    /**
     * Sample code: Get iSCSI Target.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void getISCSITarget(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.iscsiTargets().getWithResponse("myResourceGroup", "myDiskPool", "myIscsiTarget", Context.NONE);
    }
}
```

### IscsiTargets_ListByDiskPool

```java
import com.azure.core.util.Context;

/** Samples for IscsiTargets ListByDiskPool. */
public final class IscsiTargetsListByDiskPoolSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/IscsiTargets_ListByDiskPool.json
     */
    /**
     * Sample code: List Disk Pools by Resource Group.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void listDiskPoolsByResourceGroup(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.iscsiTargets().listByDiskPool("myResourceGroup", "myDiskPool", Context.NONE);
    }
}
```

### IscsiTargets_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storagepool.models.Acl;
import com.azure.resourcemanager.storagepool.models.IscsiLun;
import com.azure.resourcemanager.storagepool.models.IscsiTarget;
import java.util.Arrays;

/** Samples for IscsiTargets Update. */
public final class IscsiTargetsUpdateSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/IscsiTargets_Patch.json
     */
    /**
     * Sample code: Update iSCSI Target.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void updateISCSITarget(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        IscsiTarget resource =
            manager
                .iscsiTargets()
                .getWithResponse("myResourceGroup", "myDiskPool", "myIscsiTarget", Context.NONE)
                .getValue();
        resource
            .update()
            .withStaticAcls(
                Arrays
                    .asList(
                        new Acl()
                            .withInitiatorIqn("iqn.2005-03.org.iscsi:client")
                            .withMappedLuns(Arrays.asList("lun0"))))
            .withLuns(
                Arrays
                    .asList(
                        new IscsiLun()
                            .withName("lun0")
                            .withManagedDiskAzureResourceId(
                                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myResourceGroup/providers/Microsoft.Compute/disks/vm-name_DataDisk_1")))
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
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/Operations_List.json
     */
    /**
     * Sample code: List operations.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void listOperations(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ResourceSkus_List

```java
import com.azure.core.util.Context;

/** Samples for ResourceSkus List. */
public final class ResourceSkusListSamples {
    /*
     * x-ms-original-file: specification/storagepool/resource-manager/Microsoft.StoragePool/stable/2021-08-01/examples/Skus_List.json
     */
    /**
     * Sample code: List Disk Pool Skus.
     *
     * @param manager Entry point to StoragePoolManager.
     */
    public static void listDiskPoolSkus(com.azure.resourcemanager.storagepool.StoragePoolManager manager) {
        manager.resourceSkus().list("eastus", Context.NONE);
    }
}
```

