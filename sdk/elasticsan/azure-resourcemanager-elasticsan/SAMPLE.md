# Code snippets and samples


## ElasticSans

- [Create](#elasticsans_create)
- [Delete](#elasticsans_delete)
- [GetByResourceGroup](#elasticsans_getbyresourcegroup)
- [List](#elasticsans_list)
- [ListByResourceGroup](#elasticsans_listbyresourcegroup)
- [Update](#elasticsans_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [ListByElasticSan](#privatelinkresources_listbyelasticsan)

## ResourceProvider

- [RestoreVolume](#resourceprovider_restorevolume)

## Skus

- [List](#skus_list)

## VolumeGroups

- [Create](#volumegroups_create)
- [Delete](#volumegroups_delete)
- [Get](#volumegroups_get)
- [ListByElasticSan](#volumegroups_listbyelasticsan)
- [Update](#volumegroups_update)

## VolumeSnapshots

- [Create](#volumesnapshots_create)
- [Delete](#volumesnapshots_delete)
- [Get](#volumesnapshots_get)
- [ListByVolumeGroup](#volumesnapshots_listbyvolumegroup)

## Volumes

- [Create](#volumes_create)
- [Delete](#volumes_delete)
- [Get](#volumes_get)
- [ListByVolumeGroup](#volumes_listbyvolumegroup)
- [PreBackup](#volumes_prebackup)
- [PreRestore](#volumes_prerestore)
- [Update](#volumes_update)
### ElasticSans_Create

```java
/**
 * Samples for PrivateEndpointConnections List.
 */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateEndpointConnections_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        privateEndpointConnectionsListMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateEndpointConnections()
            .list("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateEndpointConnections_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        privateEndpointConnectionsListMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateEndpointConnections()
            .list("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE);
    }
}
```

### ElasticSans_Delete

```java
/**
 * Samples for VolumeGroups Delete.
 */
public final class VolumeGroupsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeGroupsDeleteMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeGroups()
            .delete("resourcegroupname", "elasticsanname", "volumegroupname", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeGroupsDeleteMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeGroups()
            .delete("resourcegroupname", "elasticsanname", "volumegroupname", com.azure.core.util.Context.NONE);
    }
}
```

### ElasticSans_GetByResourceGroup

```java
import com.azure.resourcemanager.elasticsan.models.DiskSnapshotList;
import java.util.Arrays;

/**
 * Samples for Volumes PreRestore.
 */
public final class VolumesPreRestoreSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_PreRestore_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_PreRestore_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeGroupsPreRestoreMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .preRestore("resourcegroupname", "elasticsanname", "volumegroupname",
                new DiskSnapshotList().withDiskSnapshotIds(Arrays.asList(
                    "/subscriptions/{subscriptionid}/resourceGroups/{resourcegroupname}/providers/Microsoft.Compute/snapshots/disksnapshot1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ElasticSans_List

```java
import com.azure.resourcemanager.elasticsan.models.AutoScalePolicyEnforcement;
import com.azure.resourcemanager.elasticsan.models.AutoScaleProperties;
import com.azure.resourcemanager.elasticsan.models.PublicNetworkAccess;
import com.azure.resourcemanager.elasticsan.models.ScaleUpProperties;
import com.azure.resourcemanager.elasticsan.models.Sku;
import com.azure.resourcemanager.elasticsan.models.SkuName;
import com.azure.resourcemanager.elasticsan.models.SkuTier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ElasticSans Create.
 */
public final class ElasticSansCreateSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void elasticSansCreateMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans()
            .define("elasticsanname")
            .withRegion("France Central")
            .withExistingResourceGroup("resourcegroupname")
            .withSku(new Sku().withName(SkuName.PREMIUM_LRS).withTier(SkuTier.PREMIUM))
            .withBaseSizeTiB(5L)
            .withExtendedCapacitySizeTiB(25L)
            .withTags(mapOf("key9316", "fakeTokenPlaceholder"))
            .withAvailabilityZones(Arrays.asList("1"))
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .withAutoScaleProperties(
                new AutoScaleProperties().withScaleUpProperties(new ScaleUpProperties().withUnusedSizeTiB(24L)
                    .withIncreaseCapacityUnitByTiB(4L)
                    .withCapacityUnitScaleUpLimitTiB(17L)
                    .withAutoScalePolicyEnforcement(AutoScalePolicyEnforcement.NONE)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_Create_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void elasticSansCreateMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans()
            .define("elasticsanname")
            .withRegion("France Central")
            .withExistingResourceGroup("resourcegroupname")
            .withSku(new Sku().withName(SkuName.PREMIUM_LRS))
            .withBaseSizeTiB(15L)
            .withExtendedCapacitySizeTiB(27L)
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

### ElasticSans_ListByResourceGroup

```java
import com.azure.resourcemanager.elasticsan.models.XMsAccessSoftDeletedResources;

/**
 * Samples for Volumes ListByVolumeGroup.
 */
public final class VolumesListByVolumeGroupSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_ListByVolumeGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_ListByVolumeGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumesListByVolumeGroupMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .listByVolumeGroup("resourcegroupname", "elasticsanname", "volumegroupname",
                XMsAccessSoftDeletedResources.TRUE, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_ListByVolumeGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: Volumes_ListByVolumeGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumesListByVolumeGroupMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .listByVolumeGroup("resourcegroupname", "elasticsanname", "volumegroupname",
                XMsAccessSoftDeletedResources.TRUE, com.azure.core.util.Context.NONE);
    }
}
```

### ElasticSans_Update

```java
/**
 * Samples for ElasticSans ListByResourceGroup.
 */
public final class ElasticSansListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_ListByResourceGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        elasticSansListByResourceGroupMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans().listByResourceGroup("resourcegroupname", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        elasticSansListByResourceGroupMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans().listByResourceGroup("resourcegroupname", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/**
 * Samples for VolumeSnapshots ListByVolumeGroup.
 */
public final class VolumeSnapshotsListByVolumeGroupSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeSnapshots_ListByVolumeGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: VolumeSnapshots_ListByVolumeGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeSnapshotsListByVolumeGroupMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeSnapshots()
            .listByVolumeGroup("resourcegroupname", "elasticsanname", "volumegroupname", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeSnapshots_ListByVolumeGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeSnapshots_ListByVolumeGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeSnapshotsListByVolumeGroupMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeSnapshots()
            .listByVolumeGroup("resourcegroupname", "elasticsanname", "volumegroupname", "volumeName eq <volume name>",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.elasticsan.models.DeleteType;
import com.azure.resourcemanager.elasticsan.models.XMsDeleteSnapshots;
import com.azure.resourcemanager.elasticsan.models.XMsForceDelete;

/**
 * Samples for Volumes Delete.
 */
public final class VolumesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumesDeleteMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .delete("resourcegroupname", "elasticsanname", "volumegroupname", "volumename", null, null,
                DeleteType.PERMANENT, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumesDeleteMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .delete("resourcegroupname", "elasticsanname", "volumegroupname", "volumename", XMsDeleteSnapshots.TRUE,
                XMsForceDelete.TRUE, DeleteType.PERMANENT, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateEndpointConnections_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        privateEndpointConnectionsGetMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("resourcegroupname", "elasticsanname", "privateendpointconnectionname",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateEndpointConnections_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        privateEndpointConnectionsGetMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("resourcegroupname", "elasticsanname", "privateendpointconnectionname",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for VolumeGroups Get.
 */
public final class VolumeGroupsGetSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeGroupsGetMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeGroups()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeGroupsGetMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeGroups()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
/**
 * Samples for ElasticSans Delete.
 */
public final class ElasticSansDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void elasticSansDeleteMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans().delete("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void elasticSansDeleteMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans().delete("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByElasticSan

```java
/**
 * Samples for VolumeSnapshots Get.
 */
public final class VolumeSnapshotsGetSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeSnapshots_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeSnapshots_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeSnapshotsGetMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeSnapshots()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname", "snapshotname",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeSnapshots_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: VolumeSnapshots_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeSnapshotsGetMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeSnapshots()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname", "snapshotname",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_RestoreVolume

```java
import com.azure.resourcemanager.elasticsan.models.ManagedByInfo;
import com.azure.resourcemanager.elasticsan.models.Volume;

/**
 * Samples for Volumes Update.
 */
public final class VolumesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumesUpdateMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        Volume resource = manager.volumes()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname", "volumename",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumesUpdateMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        Volume resource = manager.volumes()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname", "volumename",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withSizeGiB(7L)
            .withManagedBy(new ManagedByInfo().withResourceId("pclpkrpkpmvcsegcubrakcoodrubo"))
            .apply();
    }
}
```

### Skus_List

```java
/**
 * Samples for ElasticSans GetByResourceGroup.
 */
public final class ElasticSansGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void elasticSansGetMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans()
            .getByResourceGroupWithResponse("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void elasticSansGetMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans()
            .getByResourceGroupWithResponse("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_Create

```java
import com.azure.resourcemanager.elasticsan.models.XMsAccessSoftDeletedResources;

/**
 * Samples for VolumeGroups ListByElasticSan.
 */
public final class VolumeGroupsListByElasticSanSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_ListByElasticSan_MinimumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_ListByElasticSan_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeGroupsListByElasticSanMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeGroups()
            .listByElasticSan("resourcegroupname", "elasticsanname", XMsAccessSoftDeletedResources.TRUE,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_ListByElasticSan_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_ListByElasticSan_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeGroupsListByElasticSanMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeGroups()
            .listByElasticSan("resourcegroupname", "elasticsanname", XMsAccessSoftDeletedResources.TRUE,
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_Delete

```java
import com.azure.resourcemanager.elasticsan.models.Action;
import com.azure.resourcemanager.elasticsan.models.DeleteRetentionPolicy;
import com.azure.resourcemanager.elasticsan.models.EncryptionIdentity;
import com.azure.resourcemanager.elasticsan.models.EncryptionProperties;
import com.azure.resourcemanager.elasticsan.models.EncryptionType;
import com.azure.resourcemanager.elasticsan.models.Identity;
import com.azure.resourcemanager.elasticsan.models.IdentityType;
import com.azure.resourcemanager.elasticsan.models.KeyVaultProperties;
import com.azure.resourcemanager.elasticsan.models.NetworkRuleSet;
import com.azure.resourcemanager.elasticsan.models.PolicyState;
import com.azure.resourcemanager.elasticsan.models.StorageTargetType;
import com.azure.resourcemanager.elasticsan.models.UserAssignedIdentity;
import com.azure.resourcemanager.elasticsan.models.VirtualNetworkRule;
import com.azure.resourcemanager.elasticsan.models.VolumeGroup;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VolumeGroups Update.
 */
public final class VolumeGroupsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeGroupsUpdateMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        VolumeGroup resource = manager.volumeGroups()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withIdentity(new Identity().withType(IdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key2350", new UserAssignedIdentity())))
            .withProtocolType(StorageTargetType.ISCSI)
            .withEncryption(EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_KEY)
            .withEncryptionProperties(new EncryptionProperties()
                .withKeyVaultProperties(new KeyVaultProperties().withKeyName("fakeTokenPlaceholder")
                    .withKeyVersion("fakeTokenPlaceholder")
                    .withKeyVaultUri("fakeTokenPlaceholder"))
                .withEncryptionIdentity(new EncryptionIdentity().withEncryptionUserAssignedIdentity("vgbeephfgecgg")))
            .withNetworkAcls(new NetworkRuleSet().withVirtualNetworkRules(Arrays
                .asList(new VirtualNetworkRule().withVirtualNetworkResourceId("fhhawhc").withAction(Action.ALLOW))))
            .withEnforceDataIntegrityCheckForIscsi(true)
            .withDeleteRetentionPolicy(
                new DeleteRetentionPolicy().withPolicyState(PolicyState.ENABLED).withRetentionPeriodDays(14))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeGroupsUpdateMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        VolumeGroup resource = manager.volumeGroups()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
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

### VolumeGroups_Get

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateEndpointConnections_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        privateEndpointConnectionsDeleteMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateEndpointConnections()
            .delete("resourcegroupname", "elasticsanname", "privateendpointconnectionname",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateEndpointConnections_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        privateEndpointConnectionsDeleteMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateEndpointConnections()
            .delete("resourcegroupname", "elasticsanname", "privateendpointconnectionname",
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_ListByElasticSan

```java
import com.azure.resourcemanager.elasticsan.models.AutoScalePolicyEnforcement;
import com.azure.resourcemanager.elasticsan.models.AutoScaleProperties;
import com.azure.resourcemanager.elasticsan.models.ElasticSan;
import com.azure.resourcemanager.elasticsan.models.PublicNetworkAccess;
import com.azure.resourcemanager.elasticsan.models.ScaleUpProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ElasticSans Update.
 */
public final class ElasticSansUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void elasticSansUpdateMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        ElasticSan resource = manager.elasticSans()
            .getByResourceGroupWithResponse("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void elasticSansUpdateMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        ElasticSan resource = manager.elasticSans()
            .getByResourceGroupWithResponse("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1931", "fakeTokenPlaceholder"))
            .withBaseSizeTiB(13L)
            .withExtendedCapacitySizeTiB(29L)
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .withAutoScaleProperties(
                new AutoScaleProperties().withScaleUpProperties(new ScaleUpProperties().withUnusedSizeTiB(24L)
                    .withIncreaseCapacityUnitByTiB(4L)
                    .withCapacityUnitScaleUpLimitTiB(17L)
                    .withAutoScalePolicyEnforcement(AutoScalePolicyEnforcement.NONE)))
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

### VolumeGroups_Update

```java
/**
 * Samples for VolumeSnapshots Delete.
 */
public final class VolumeSnapshotsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeSnapshots_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeSnapshots_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeSnapshotsDeleteMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeSnapshots()
            .delete("resourcegroupname", "elasticsanname", "volumegroupname", "snapshotname",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeSnapshots_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: VolumeSnapshots_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeSnapshotsDeleteMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeSnapshots()
            .delete("resourcegroupname", "elasticsanname", "volumegroupname", "snapshotname",
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeSnapshots_Create

```java
/**
 * Samples for PrivateLinkResources ListByElasticSan.
 */
public final class PrivateLinkResourcesListByElasticSanSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateLinkResources_ListByElasticSan_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrivateLinkResources_ListByElasticSan_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void privateLinkResourcesListByElasticSanMinimumSetGen(
        com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateLinkResources()
            .listByElasticSanWithResponse("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateLinkResources_ListByElasticSan_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateLinkResources_ListByElasticSan_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void privateLinkResourcesListByElasticSanMaximumSetGen(
        com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateLinkResources()
            .listByElasticSanWithResponse("resourcegroupname", "elasticsanname", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeSnapshots_Delete

```java
/**
 * Samples for ResourceProvider RestoreVolume.
 */
public final class ResourceProviderRestoreVolumeSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * RestoreVolume_MaximumSet_Gen.json
     */
    /**
     * Sample code: RestoreVolume_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void restoreVolumeMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.resourceProviders()
            .restoreVolume("resourcegroupname", "elasticsanname", "volumegroupname", "volumename-1741526907",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * RestoreVolume_MinimumSet_Gen.json
     */
    /**
     * Sample code: RestoreVolume_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void restoreVolumeMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.resourceProviders()
            .restoreVolume("resourcegroupname", "elasticsanname", "volumegroupname", "volumename-1741526907",
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeSnapshots_Get

```java
/**
 * Samples for Skus List.
 */
public final class SkusListSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Skus_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Skus_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void skusListMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.skus().list(null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Skus_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Skus_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void skusListMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.skus().list("obwwdrkq", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeSnapshots_ListByVolumeGroup

```java
import com.azure.resourcemanager.elasticsan.models.VolumeNameList;
import java.util.Arrays;

/**
 * Samples for Volumes PreBackup.
 */
public final class VolumesPreBackupSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_PreBackup_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_PreBackup_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeGroupsPreBackupMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .preBackup("resourcegroupname", "elasticsanname", "volumegroupname",
                new VolumeNameList().withVolumeNames(Arrays.asList("volumename")), com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Create

```java
/**
 * Samples for ElasticSans List.
 */
public final class ElasticSansListSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        elasticSansListBySubscriptionMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * ElasticSans_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: ElasticSans_ListBySubscription_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        elasticSansListBySubscriptionMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.elasticSans().list(com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Delete

```java
import com.azure.resourcemanager.elasticsan.models.Action;
import com.azure.resourcemanager.elasticsan.models.DeleteRetentionPolicy;
import com.azure.resourcemanager.elasticsan.models.EncryptionIdentity;
import com.azure.resourcemanager.elasticsan.models.EncryptionProperties;
import com.azure.resourcemanager.elasticsan.models.EncryptionType;
import com.azure.resourcemanager.elasticsan.models.Identity;
import com.azure.resourcemanager.elasticsan.models.IdentityType;
import com.azure.resourcemanager.elasticsan.models.KeyVaultProperties;
import com.azure.resourcemanager.elasticsan.models.NetworkRuleSet;
import com.azure.resourcemanager.elasticsan.models.PolicyState;
import com.azure.resourcemanager.elasticsan.models.StorageTargetType;
import com.azure.resourcemanager.elasticsan.models.UserAssignedIdentity;
import com.azure.resourcemanager.elasticsan.models.VirtualNetworkRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VolumeGroups Create.
 */
public final class VolumeGroupsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Create_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeGroupsCreateMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeGroups()
            .define("volumegroupname")
            .withExistingElasticSan("resourcegroupname", "elasticsanname")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeGroups_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumeGroupsCreateMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeGroups()
            .define("volumegroupname")
            .withExistingElasticSan("resourcegroupname", "elasticsanname")
            .withIdentity(new Identity().withType(IdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key2350", new UserAssignedIdentity())))
            .withProtocolType(StorageTargetType.ISCSI)
            .withEncryption(EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_KEY)
            .withEncryptionProperties(new EncryptionProperties()
                .withKeyVaultProperties(new KeyVaultProperties().withKeyName("fakeTokenPlaceholder")
                    .withKeyVersion("fakeTokenPlaceholder")
                    .withKeyVaultUri("fakeTokenPlaceholder"))
                .withEncryptionIdentity(new EncryptionIdentity().withEncryptionUserAssignedIdentity("vgbeephfgecgg")))
            .withNetworkAcls(new NetworkRuleSet().withVirtualNetworkRules(Arrays
                .asList(new VirtualNetworkRule().withVirtualNetworkResourceId("fhhawhc").withAction(Action.ALLOW))))
            .withEnforceDataIntegrityCheckForIscsi(true)
            .withDeleteRetentionPolicy(
                new DeleteRetentionPolicy().withPolicyState(PolicyState.ENABLED).withRetentionPeriodDays(14))
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

### Volumes_Get

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void operationsListMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void operationsListMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ListByVolumeGroup

```java
import com.azure.resourcemanager.elasticsan.models.PrivateEndpoint;
import com.azure.resourcemanager.elasticsan.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.elasticsan.models.PrivateLinkServiceConnectionState;
import java.util.Arrays;

/**
 * Samples for PrivateEndpointConnections Create.
 */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateEndpointConnections_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        privateEndpointConnectionsCreateMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateEndpointConnections()
            .define("privateendpointconnectionname")
            .withExistingElasticSan("resourcegroupname", "elasticsanname")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.PENDING)
                    .withDescription("dxl")
                    .withActionsRequired("jhjdpwvyzipggtn"))
            .withPrivateEndpoint(new PrivateEndpoint())
            .withGroupIds(Arrays.asList("jdwrzpemdjrpiwzvy"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * PrivateEndpointConnections_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Create_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        privateEndpointConnectionsCreateMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.privateEndpointConnections()
            .define("privateendpointconnectionname")
            .withExistingElasticSan("resourcegroupname", "elasticsanname")
            .withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState())
            .create();
    }
}
```

### Volumes_PreBackup

```java
/**
 * Samples for Volumes Get.
 */
public final class VolumesGetSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumesGetMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname", "volumename",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumesGetMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .getWithResponse("resourcegroupname", "elasticsanname", "volumegroupname", "volumename",
                com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_PreRestore

```java
import com.azure.resourcemanager.elasticsan.models.SnapshotCreationData;

/**
 * Samples for VolumeSnapshots Create.
 */
public final class VolumeSnapshotsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeSnapshots_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeSnapshots_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeSnapshotsCreateMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeSnapshots()
            .define("snapshotname")
            .withExistingVolumegroup("resourcegroupname", "elasticsanname", "volumegroupname")
            .withCreationData(new SnapshotCreationData().withSourceId(
                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ElasticSan/elasticSans/{elasticSanName}/volumegroups/{volumeGroupName}/volumes/{volumeName}"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * VolumeSnapshots_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: VolumeSnapshots_Create_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void
        volumeSnapshotsCreateMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumeSnapshots()
            .define("snapshotname")
            .withExistingVolumegroup("resourcegroupname", "elasticsanname", "volumegroupname")
            .withCreationData(new SnapshotCreationData().withSourceId(
                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ElasticSan/elasticSans/{elasticSanName}/volumegroups/{volumeGroupName}/volumes/{volumeName}"))
            .create();
    }
}
```

### Volumes_Update

```java
import com.azure.resourcemanager.elasticsan.models.ManagedByInfo;
import com.azure.resourcemanager.elasticsan.models.SourceCreationData;
import com.azure.resourcemanager.elasticsan.models.VolumeCreateOption;

/**
 * Samples for Volumes Create.
 */
public final class VolumesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumesCreateMaximumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .define("volumename")
            .withExistingVolumegroup("resourcegroupname", "elasticsanname", "volumegroupname")
            .withSizeGiB(23L)
            .withCreationData(
                new SourceCreationData().withCreateSource(VolumeCreateOption.NONE).withSourceId("mdonegivjquite"))
            .withManagedBy(new ManagedByInfo().withResourceId("pclpkrpkpmvcsegcubrakcoodrubo"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/elasticsan/resource-manager/Microsoft.ElasticSan/preview/2024-07-01-preview/examples/
     * Volumes_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Create_MinimumSet_Gen.
     * 
     * @param manager Entry point to ElasticSanManager.
     */
    public static void volumesCreateMinimumSetGen(com.azure.resourcemanager.elasticsan.ElasticSanManager manager) {
        manager.volumes()
            .define("volumename")
            .withExistingVolumegroup("resourcegroupname", "elasticsanname", "volumegroupname")
            .withSizeGiB(9L)
            .create();
    }
}
```

