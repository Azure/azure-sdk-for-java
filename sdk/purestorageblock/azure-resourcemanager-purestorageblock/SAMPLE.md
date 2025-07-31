# Code snippets and samples


## AvsStorageContainerVolumes

- [Delete](#avsstoragecontainervolumes_delete)
- [Get](#avsstoragecontainervolumes_get)
- [ListByAvsStorageContainer](#avsstoragecontainervolumes_listbyavsstoragecontainer)
- [Update](#avsstoragecontainervolumes_update)

## AvsStorageContainers

- [Delete](#avsstoragecontainers_delete)
- [Get](#avsstoragecontainers_get)
- [ListByStoragePool](#avsstoragecontainers_listbystoragepool)

## AvsVmVolumes

- [Delete](#avsvmvolumes_delete)
- [Get](#avsvmvolumes_get)
- [ListByAvsVm](#avsvmvolumes_listbyavsvm)
- [Update](#avsvmvolumes_update)

## AvsVms

- [Delete](#avsvms_delete)
- [Get](#avsvms_get)
- [ListByStoragePool](#avsvms_listbystoragepool)
- [Update](#avsvms_update)

## Operations

- [List](#operations_list)

## Reservations

- [GetBillingReport](#reservations_getbillingreport)
- [GetBillingStatus](#reservations_getbillingstatus)
- [GetResourceLimits](#reservations_getresourcelimits)

## StoragePools

- [Create](#storagepools_create)
- [Delete](#storagepools_delete)
- [DisableAvsConnection](#storagepools_disableavsconnection)
- [EnableAvsConnection](#storagepools_enableavsconnection)
- [FinalizeAvsConnection](#storagepools_finalizeavsconnection)
- [GetAvsConnection](#storagepools_getavsconnection)
- [GetAvsStatus](#storagepools_getavsstatus)
- [GetByResourceGroup](#storagepools_getbyresourcegroup)
- [GetHealthStatus](#storagepools_gethealthstatus)
- [List](#storagepools_list)
- [ListByResourceGroup](#storagepools_listbyresourcegroup)
- [RepairAvsConnection](#storagepools_repairavsconnection)
- [Update](#storagepools_update)
### AvsStorageContainerVolumes_Delete

```java
/**
 * Samples for AvsStorageContainerVolumes Delete.
 */
public final class AvsStorageContainerVolumesDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsStorageContainerVolumes_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainerVolumes_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainerVolumesDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainerVolumes()
            .delete("rgpurestorage", "storagePoolname", "name", "cbdec-ddbb", com.azure.core.util.Context.NONE);
    }
}
```

### AvsStorageContainerVolumes_Get

```java
/**
 * Samples for AvsStorageContainerVolumes Get.
 */
public final class AvsStorageContainerVolumesGetSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsStorageContainerVolumes_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainerVolumes_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainerVolumesGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainerVolumes()
            .getWithResponse("rgpurestorage", "storagePoolname", "name", "cbdec-ddbb",
                com.azure.core.util.Context.NONE);
    }
}
```

### AvsStorageContainerVolumes_ListByAvsStorageContainer

```java
/**
 * Samples for AvsStorageContainerVolumes ListByAvsStorageContainer.
 */
public final class AvsStorageContainerVolumesListByAvsStorageContainerSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsStorageContainerVolumes_ListByAvsStorageContainer_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainerVolumes_ListByAvsStorageContainer.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsStorageContainerVolumesListByAvsStorageContainer(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainerVolumes()
            .listByAvsStorageContainer("rgpurestorage", "storagePoolname", "name", com.azure.core.util.Context.NONE);
    }
}
```

### AvsStorageContainerVolumes_Update

```java
import com.azure.resourcemanager.purestorageblock.models.AvsStorageContainerVolumeUpdate;
import com.azure.resourcemanager.purestorageblock.models.AvsStorageContainerVolumeUpdateProperties;
import com.azure.resourcemanager.purestorageblock.models.SoftDeletion;

/**
 * Samples for AvsStorageContainerVolumes Update.
 */
public final class AvsStorageContainerVolumesUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsStorageContainerVolumes_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainerVolumes_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainerVolumesUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainerVolumes()
            .update("rgpurestorage", "storagePoolname", "name", "cbdec-ddbb",
                new AvsStorageContainerVolumeUpdate().withProperties(new AvsStorageContainerVolumeUpdateProperties()
                    .withSoftDeletion(new SoftDeletion().withDestroyed(true))),
                com.azure.core.util.Context.NONE);
    }
}
```

### AvsStorageContainers_Delete

```java
/**
 * Samples for AvsStorageContainers Delete.
 */
public final class AvsStorageContainersDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsStorageContainers_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainers_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainersDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainers()
            .delete("rgpurestorage", "storagePoolName", "storageContainerName", com.azure.core.util.Context.NONE);
    }
}
```

### AvsStorageContainers_Get

```java
/**
 * Samples for AvsStorageContainers Get.
 */
public final class AvsStorageContainersGetSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsStorageContainers_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainers_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainersGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainers()
            .getWithResponse("rgpurestorage", "storagePoolName", "storageContainerName",
                com.azure.core.util.Context.NONE);
    }
}
```

### AvsStorageContainers_ListByStoragePool

```java
/**
 * Samples for AvsStorageContainers ListByStoragePool.
 */
public final class AvsStorageContainersListByStoragePoolSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsStorageContainers_ListByStoragePool_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainers_ListByStoragePool.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsStorageContainersListByStoragePool(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainers().listByStoragePool("rgpurestorage", "spName", com.azure.core.util.Context.NONE);
    }
}
```

### AvsVmVolumes_Delete

```java
/**
 * Samples for AvsVmVolumes Delete.
 */
public final class AvsVmVolumesDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsVmVolumes_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVmVolumes_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmVolumesDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVmVolumes()
            .delete("rgpurestorage", "storagePoolname", "cbdec-ddbb", "cbdec-ddbb", com.azure.core.util.Context.NONE);
    }
}
```

### AvsVmVolumes_Get

```java
/**
 * Samples for AvsVmVolumes Get.
 */
public final class AvsVmVolumesGetSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsVmVolumes_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVmVolumes_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmVolumesGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVmVolumes()
            .getWithResponse("rgpurestorage", "storagePoolname", "cbdec-ddbb", "cbdec-ddbb",
                com.azure.core.util.Context.NONE);
    }
}
```

### AvsVmVolumes_ListByAvsVm

```java
/**
 * Samples for AvsVmVolumes ListByAvsVm.
 */
public final class AvsVmVolumesListByAvsVmSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsVmVolumes_ListByAvsVm_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVmVolumes_ListByAvsVm.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsVmVolumesListByAvsVm(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVmVolumes()
            .listByAvsVm("rgpurestorage", "storagePoolname", "cbdec-ddbb", com.azure.core.util.Context.NONE);
    }
}
```

### AvsVmVolumes_Update

```java
import com.azure.resourcemanager.purestorageblock.models.AvsVmVolumeUpdate;
import com.azure.resourcemanager.purestorageblock.models.AvsVmVolumeUpdateProperties;
import com.azure.resourcemanager.purestorageblock.models.SoftDeletion;

/**
 * Samples for AvsVmVolumes Update.
 */
public final class AvsVmVolumesUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsVmVolumes_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVmVolumes_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmVolumesUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVmVolumes()
            .update("rgpurestorage", "storagePoolname", "cbdec-ddbb", "cbdec-ddbb",
                new AvsVmVolumeUpdate().withProperties(
                    new AvsVmVolumeUpdateProperties().withSoftDeletion(new SoftDeletion().withDestroyed(true))),
                com.azure.core.util.Context.NONE);
    }
}
```

### AvsVms_Delete

```java
/**
 * Samples for AvsVms Delete.
 */
public final class AvsVmsDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsVms_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVms_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmsDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVms().delete("rgpurestorage", "storagePoolname", "cbdec-ddbb", com.azure.core.util.Context.NONE);
    }
}
```

### AvsVms_Get

```java
/**
 * Samples for AvsVms Get.
 */
public final class AvsVmsGetSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsVms_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVms_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmsGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVms()
            .getWithResponse("rgpurestorage", "storagePoolname", "cbdec-ddbb", com.azure.core.util.Context.NONE);
    }
}
```

### AvsVms_ListByStoragePool

```java
/**
 * Samples for AvsVms ListByStoragePool.
 */
public final class AvsVmsListByStoragePoolSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsVms_ListByStoragePool_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVms_ListByStoragePool.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsVmsListByStoragePool(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVms().listByStoragePool("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE);
    }
}
```

### AvsVms_Update

```java
import com.azure.resourcemanager.purestorageblock.models.AvsVmUpdate;
import com.azure.resourcemanager.purestorageblock.models.AvsVmUpdateProperties;
import com.azure.resourcemanager.purestorageblock.models.SoftDeletion;

/**
 * Samples for AvsVms Update.
 */
public final class AvsVmsUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01/AvsVms_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVms_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmsUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVms()
            .update("rgpurestorage", "storagePoolname", "cbdec-ddbb",
                new AvsVmUpdate().withProperties(
                    new AvsVmUpdateProperties().withSoftDeletion(new SoftDeletion().withDestroyed(true))),
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
     * x-ms-original-file: 2024-11-01/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void operationsList(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-11-01/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumGen_Set.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        operationsListMinimumGenSet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_GetBillingReport

```java
/**
 * Samples for Reservations GetBillingReport.
 */
public final class ReservationsGetBillingReportSamples {
    /*
     * x-ms-original-file: 2024-11-01/Reservations_GetBillingReport_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_GetBillingReport_MaximumSet.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void reservationsGetBillingReportMaximumSet(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations()
            .getBillingReportWithResponse("rgpurestorage", "reservationname", com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_GetBillingStatus

```java
/**
 * Samples for Reservations GetBillingStatus.
 */
public final class ReservationsGetBillingStatusSamples {
    /*
     * x-ms-original-file: 2024-11-01/Reservations_GetBillingStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_GetBillingStatus_MaximumSet.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void reservationsGetBillingStatusMaximumSet(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations()
            .getBillingStatusWithResponse("rgpurestorage", "reservationname", com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_GetResourceLimits

```java
/**
 * Samples for Reservations GetResourceLimits.
 */
public final class ReservationsGetResourceLimitsSamples {
    /*
     * x-ms-original-file: 2024-11-01/Reservations_GetResourceLimits_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_GetResourceLimits.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        reservationsGetResourceLimits(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations()
            .getResourceLimitsWithResponse("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_Create

```java
import com.azure.resourcemanager.purestorageblock.models.ManagedServiceIdentity;
import com.azure.resourcemanager.purestorageblock.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.purestorageblock.models.StoragePoolProperties;
import com.azure.resourcemanager.purestorageblock.models.UserAssignedIdentity;
import com.azure.resourcemanager.purestorageblock.models.VnetInjection;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StoragePools Create.
 */
public final class StoragePoolsCreateSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_Create.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsCreate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .define("storagePoolname")
            .withRegion("lonlc")
            .withExistingResourceGroup("rgpurestorage")
            .withTags(mapOf("key7593", "fakeTokenPlaceholder"))
            .withProperties(new StoragePoolProperties().withAvailabilityZone("vknyl")
                .withVnetInjection(new VnetInjection().withSubnetId("tnlctolrxdvnkjiphlrdxq")
                    .withVnetId("zbumtytyqwewjcyckwqchiypshv"))
                .withProvisionedBandwidthMbPerSec(17L)
                .withReservationResourceId("xiowoxnbtcotutcmmrofvgdi"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key4211", new UserAssignedIdentity())))
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

### StoragePools_Delete

```java
/**
 * Samples for StoragePools Delete.
 */
public final class StoragePoolsDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools().delete("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_DisableAvsConnection

```java
/**
 * Samples for StoragePools DisableAvsConnection.
 */
public final class StoragePoolsDisableAvsConnectionSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_DisableAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_DisableAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsDisableAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .disableAvsConnection("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_EnableAvsConnection

```java
import com.azure.resourcemanager.purestorageblock.models.StoragePoolEnableAvsConnectionPost;

/**
 * Samples for StoragePools EnableAvsConnection.
 */
public final class StoragePoolsEnableAvsConnectionSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_EnableAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_EnableAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsEnableAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .enableAvsConnection("rgpurestorage", "storagePoolname",
                new StoragePoolEnableAvsConnectionPost().withClusterResourceId("tghkgktlddwlszbeh"),
                com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_FinalizeAvsConnection

```java
import com.azure.resourcemanager.purestorageblock.models.ServiceInitializationInfo;
import com.azure.resourcemanager.purestorageblock.models.StoragePoolFinalizeAvsConnectionPost;

/**
 * Samples for StoragePools FinalizeAvsConnection.
 */
public final class StoragePoolsFinalizeAvsConnectionSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_FinalizeAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_FinalizeAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsFinalizeAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .finalizeAvsConnection("rgpurestorage", "storagePoolname",
                new StoragePoolFinalizeAvsConnectionPost().withServiceInitializationDataEnc("hlgzaxrohv")
                    .withServiceInitializationData(new ServiceInitializationInfo().withServiceAccountUsername("axchgm")
                        .withServiceAccountPassword("fakeTokenPlaceholder")
                        .withVSphereIp("lhbajnykbznxnxpxozyfdjaciennks")
                        .withVSphereCertificate("s")),
                com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_GetAvsConnection

```java
/**
 * Samples for StoragePools GetAvsConnection.
 */
public final class StoragePoolsGetAvsConnectionSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_GetAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_GetAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsGetAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .getAvsConnectionWithResponse("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_GetAvsStatus

```java
/**
 * Samples for StoragePools GetAvsStatus.
 */
public final class StoragePoolsGetAvsStatusSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_GetAvsStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_GetAvsStatus.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsGetAvsStatus(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .getAvsStatusWithResponse("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_GetByResourceGroup

```java
/**
 * Samples for StoragePools GetByResourceGroup.
 */
public final class StoragePoolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .getByResourceGroupWithResponse("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_GetHealthStatus

```java
/**
 * Samples for StoragePools GetHealthStatus.
 */
public final class StoragePoolsGetHealthStatusSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_GetHealthStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_GetHealthStatus.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsGetHealthStatus(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .getHealthStatusWithResponse("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_List

```java
/**
 * Samples for StoragePools List.
 */
public final class StoragePoolsListSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_ListBySubscription.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsListBySubscription(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools().list(com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_ListByResourceGroup

```java
/**
 * Samples for StoragePools ListByResourceGroup.
 */
public final class StoragePoolsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_ListByResourceGroup.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsListByResourceGroup(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools().listByResourceGroup("rgpurestorage", com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_RepairAvsConnection

```java
/**
 * Samples for StoragePools RepairAvsConnection.
 */
public final class StoragePoolsRepairAvsConnectionSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_RepairAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_RepairAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsRepairAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .repairAvsConnection("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_Update

```java
import com.azure.resourcemanager.purestorageblock.models.ManagedServiceIdentity;
import com.azure.resourcemanager.purestorageblock.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.purestorageblock.models.StoragePool;
import com.azure.resourcemanager.purestorageblock.models.StoragePoolUpdateProperties;
import com.azure.resourcemanager.purestorageblock.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StoragePools Update.
 */
public final class StoragePoolsUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01/StoragePools_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        StoragePool resource = manager.storagePools()
            .getByResourceGroupWithResponse("rgpurestorage", "storagePoolname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key9065", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key4211", new UserAssignedIdentity())))
            .withProperties(new StoragePoolUpdateProperties().withProvisionedBandwidthMbPerSec(23L))
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

