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

## RecoverableVolumeGroups

- [Delete](#recoverablevolumegroups_delete)
- [Get](#recoverablevolumegroups_get)
- [ListByStoragePool](#recoverablevolumegroups_listbystoragepool)

## Reservations

- [Create](#reservations_create)
- [Delete](#reservations_delete)
- [GetBillingReport](#reservations_getbillingreport)
- [GetBillingStatus](#reservations_getbillingstatus)
- [GetByResourceGroup](#reservations_getbyresourcegroup)
- [GetResourceLimits](#reservations_getresourcelimits)
- [LatestLinkedSaaS](#reservations_latestlinkedsaas)
- [LinkSaaS](#reservations_linksaas)
- [List](#reservations_list)
- [ListByResourceGroup](#reservations_listbyresourcegroup)
- [Update](#reservations_update)

## SaaSOperationGroup

- [ActivateResource](#saasoperationgroup_activateresource)

## StoragePools

- [ConfigurePlatformConsoleAuth](#storagepools_configureplatformconsoleauth)
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
- [ListPlatformConsoleActivationCode](#storagepools_listplatformconsoleactivationcode)
- [RepairAvsConnection](#storagepools_repairavsconnection)
- [Update](#storagepools_update)

## VolumeGroupSnapshots

- [Create](#volumegroupsnapshots_create)
- [Delete](#volumegroupsnapshots_delete)
- [Get](#volumegroupsnapshots_get)
- [ListByVolumeGroup](#volumegroupsnapshots_listbyvolumegroup)
- [ListSnapshots](#volumegroupsnapshots_listsnapshots)

## VolumeGroups

- [Create](#volumegroups_create)
- [Delete](#volumegroups_delete)
- [Get](#volumegroups_get)
- [GetStatus](#volumegroups_getstatus)
- [ListByStoragePool](#volumegroups_listbystoragepool)
- [ListConnectionParameters](#volumegroups_listconnectionparameters)
- [Overwrite](#volumegroups_overwrite)
- [Update](#volumegroups_update)

## Volumes

- [Create](#volumes_create)
- [Delete](#volumes_delete)
- [Get](#volumes_get)
- [ListByVolumeGroup](#volumes_listbyvolumegroup)
- [Overwrite](#volumes_overwrite)
- [Update](#volumes_update)
### AvsStorageContainerVolumes_Delete

```java
/**
 * Samples for AvsStorageContainerVolumes Delete.
 */
public final class AvsStorageContainerVolumesDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/AvsStorageContainerVolumes_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainerVolumes_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainerVolumesDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainerVolumes()
            .delete("rgpurestorage", "storagepool-01", "container-01", "a1b2c3d4-e5f6",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AvsStorageContainerVolumes_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainerVolumes_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainerVolumesGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainerVolumes()
            .getWithResponse("rgpurestorage", "storagepool-01", "container-01", "a1b2c3d4-e5f6",
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
     * x-ms-original-file: 2026-05-01-preview/AvsStorageContainerVolumes_ListByAvsStorageContainer_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainerVolumes_ListByAvsStorageContainer.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsStorageContainerVolumesListByAvsStorageContainer(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainerVolumes()
            .listByAvsStorageContainer("rgpurestorage", "storagepool-01", "container-01",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AvsStorageContainerVolumes_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainerVolumes_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainerVolumesUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainerVolumes()
            .update("rgpurestorage", "storagepool-01", "container-01", "a1b2c3d4-e5f6",
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
     * x-ms-original-file: 2026-05-01-preview/AvsStorageContainers_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainers_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainersDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainers()
            .delete("rgpurestorage", "storagepool-01", "container-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AvsStorageContainers_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsStorageContainers_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsStorageContainersGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsStorageContainers()
            .getWithResponse("rgpurestorage", "storagepool-01", "container-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AvsStorageContainers_ListByStoragePool_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-05-01-preview/AvsVmVolumes_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVmVolumes_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmVolumesDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVmVolumes()
            .delete("rgpurestorage", "storagepool-01", "abc123def456", "a1b2c3d4-e5f6",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AvsVmVolumes_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVmVolumes_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmVolumesGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVmVolumes()
            .getWithResponse("rgpurestorage", "storagepool-01", "abc123def456", "a1b2c3d4-e5f6",
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
     * x-ms-original-file: 2026-05-01-preview/AvsVmVolumes_ListByAvsVm_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVmVolumes_ListByAvsVm.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsVmVolumesListByAvsVm(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVmVolumes()
            .listByAvsVm("rgpurestorage", "storagepool-01", "abc123def456", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AvsVmVolumes_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVmVolumes_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmVolumesUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVmVolumes()
            .update("rgpurestorage", "storagepool-01", "abc123def456", "a1b2c3d4-e5f6",
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
     * x-ms-original-file: 2026-05-01-preview/AvsVms_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVms_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmsDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVms().delete("rgpurestorage", "storagepool-01", "abc123def456", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AvsVms_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVms_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmsGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVms()
            .getWithResponse("rgpurestorage", "storagepool-01", "abc123def456", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AvsVms_ListByStoragePool_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVms_ListByStoragePool.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        avsVmsListByStoragePool(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVms().listByStoragePool("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AvsVms_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsVms_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void avsVmsUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.avsVms()
            .update("rgpurestorage", "storagepool-01", "abc123def456",
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
     * x-ms-original-file: 2026-05-01-preview/Operations_List_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-05-01-preview/Operations_List_MinimumSet_Gen.json
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

### RecoverableVolumeGroups_Delete

```java
/**
 * Samples for RecoverableVolumeGroups Delete.
 */
public final class RecoverableVolumeGroupsDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/RecoverableVolumeGroups_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoverableVolumeGroups_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        recoverableVolumeGroupsDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.recoverableVolumeGroups()
            .delete("rgpurestorage", "storagepool-01", "volumegroup-01", com.azure.core.util.Context.NONE);
    }
}
```

### RecoverableVolumeGroups_Get

```java
/**
 * Samples for RecoverableVolumeGroups Get.
 */
public final class RecoverableVolumeGroupsGetSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/RecoverableVolumeGroups_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoverableVolumeGroups_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        recoverableVolumeGroupsGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.recoverableVolumeGroups()
            .getWithResponse("rgpurestorage", "storagepool-01", "volumegroup-01", com.azure.core.util.Context.NONE);
    }
}
```

### RecoverableVolumeGroups_ListByStoragePool

```java
/**
 * Samples for RecoverableVolumeGroups ListByStoragePool.
 */
public final class RecoverableVolumeGroupsListByStoragePoolSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/RecoverableVolumeGroups_ListByStoragePool_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoverableVolumeGroups_ListByStoragePool.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void recoverableVolumeGroupsListByStoragePool(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.recoverableVolumeGroups()
            .listByStoragePool("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_Create

```java
import com.azure.resourcemanager.purestorageblock.models.Address;
import com.azure.resourcemanager.purestorageblock.models.CompanyDetails;
import com.azure.resourcemanager.purestorageblock.models.MarketplaceDetails;
import com.azure.resourcemanager.purestorageblock.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.purestorageblock.models.OfferDetails;
import com.azure.resourcemanager.purestorageblock.models.ReservationPropertiesBaseResourceProperties;
import com.azure.resourcemanager.purestorageblock.models.UserDetails;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Reservations Create.
 */
public final class ReservationsCreateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Reservations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_Create.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void reservationsCreate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations()
            .define("reservation-01")
            .withRegion("eastus")
            .withExistingResourceGroup("rgpurestorage")
            .withTags(mapOf("environment", "production"))
            .withProperties(new ReservationPropertiesBaseResourceProperties()
                .withMarketplace(new MarketplaceDetails()
                    .withSubscriptionStatus(MarketplaceSubscriptionStatus.PENDING_FULFILLMENT_START)
                    .withOfferDetails(new OfferDetails().withPublisherId("pure_storage")
                        .withOfferId("purestorage-block-offer")
                        .withPlanId("standard-plan")
                        .withPlanName("Standard Plan")
                        .withTermUnit("month")
                        .withTermId("12-month-term")))
                .withUser(new UserDetails().withFirstName("John")
                    .withLastName("Doe")
                    .withEmailAddress("john.doe@contoso.com")
                    .withUpn("john.doe@contoso.com")
                    .withPhoneNumber("+1-425-555-1234")
                    .withCompanyDetails(new CompanyDetails().withCompanyName("Contoso Ltd.")
                        .withAddress(new Address().withAddressLine1("1 Microsoft Way")
                            .withAddressLine2("Suite 100")
                            .withCity("Redmond")
                            .withState("Washington")
                            .withCountry("United States")
                            .withPostalCode("fakeTokenPlaceholder")))))
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

### Reservations_Delete

```java
/**
 * Samples for Reservations Delete.
 */
public final class ReservationsDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Reservations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void reservationsDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations().delete("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/Reservations_GetBillingReport_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-05-01-preview/Reservations_GetBillingStatus_MaximumSet_Gen.json
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

### Reservations_GetByResourceGroup

```java
/**
 * Samples for Reservations GetByResourceGroup.
 */
public final class ReservationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Reservations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void reservationsGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations()
            .getByResourceGroupWithResponse("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/Reservations_GetResourceLimits_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_GetResourceLimits.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        reservationsGetResourceLimits(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations()
            .getResourceLimitsWithResponse("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_LatestLinkedSaaS

```java
/**
 * Samples for Reservations LatestLinkedSaaS.
 */
public final class ReservationsLatestLinkedSaaSSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Reservations_LatestLinkedSaaS_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_LatestLinkedSaaS.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        reservationsLatestLinkedSaaS(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations()
            .latestLinkedSaaSWithResponse("rgpurestorage", "reservation-01", com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_LinkSaaS

```java
import com.azure.resourcemanager.purestorageblock.models.LinkSaaSRequest;

/**
 * Samples for Reservations LinkSaaS.
 */
public final class ReservationsLinkSaaSSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Reservations_LinkSaaS_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_LinkSaaS.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        reservationsLinkSaaS(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations()
            .linkSaaS("rgpurestorage", "reservation-01", new LinkSaaSRequest().withSaaSResourceId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/Microsoft.SaaS/resources/saas-resource-01"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_List

```java
/**
 * Samples for Reservations List.
 */
public final class ReservationsListSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Reservations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_ListBySubscription.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        reservationsListBySubscription(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_ListByResourceGroup

```java
/**
 * Samples for Reservations ListByResourceGroup.
 */
public final class ReservationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Reservations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_ListByResourceGroup.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        reservationsListByResourceGroup(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.reservations().listByResourceGroup("rgpurestorage", com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_Update

```java
import com.azure.resourcemanager.purestorageblock.models.Address;
import com.azure.resourcemanager.purestorageblock.models.CompanyDetails;
import com.azure.resourcemanager.purestorageblock.models.Reservation;
import com.azure.resourcemanager.purestorageblock.models.ReservationUpdateProperties;
import com.azure.resourcemanager.purestorageblock.models.UserDetails;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Reservations Update.
 */
public final class ReservationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Reservations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Reservations_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void reservationsUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        Reservation resource = manager.reservations()
            .getByResourceGroupWithResponse("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key8751", "fakeTokenPlaceholder"))
            .withProperties(
                new ReservationUpdateProperties().withUser(new UserDetails().withFirstName("sjzquetrvxcrajxdfwfeuro")
                    .withLastName("qimvqxnlbclfouwzfk")
                    .withEmailAddress("john.doe@contoso.com")
                    .withUpn("pvafwnbigmhuigxfu")
                    .withPhoneNumber("jfljnoxsfsplwczwgvmlurfnorimvl")
                    .withCompanyDetails(new CompanyDetails().withCompanyName("uleytbkckdhaiykwjjcjqmnlik")
                        .withAddress(new Address().withAddressLine1("ryaasdffnhwialrgmukpiwtcjgbb")
                            .withAddressLine2("cvyuuqnvuqfrpkoplfzmhnwrqsbsgn")
                            .withCity("kdpzfxfbgozxwunkkhjthqdsnmce")
                            .withState("fygrbnektar")
                            .withCountry("trmpjpxsfmxprlnv")
                            .withPostalCode("fakeTokenPlaceholder")))))
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

### SaaSOperationGroup_ActivateResource

```java
import com.azure.resourcemanager.purestorageblock.models.ActivateSaaSRequest;

/**
 * Samples for SaaSOperationGroup ActivateResource.
 */
public final class SaaSOperationGroupActivateResourceSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/SaaSOperationGroup_ActivateSaaS_MaximumSet.json
     */
    /**
     * Sample code: SaaSOperationGroup_ActivateResource.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        saaSOperationGroupActivateResource(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.saaSOperationGroups()
            .activateResource(new ActivateSaaSRequest().withSaasGuid("12345678-1234-5678-1234-567812345678")
                .withPublisherId("purestorage1234567890"), com.azure.core.util.Context.NONE);
    }
}
```

### StoragePools_ConfigurePlatformConsoleAuth

```java
import com.azure.resourcemanager.purestorageblock.models.PlatformConsoleRole;
import com.azure.resourcemanager.purestorageblock.models.SshPlatformConsoleAuthConfig;

/**
 * Samples for StoragePools ConfigurePlatformConsoleAuth.
 */
public final class StoragePoolsConfigurePlatformConsoleAuthSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/StoragePools_ConfigurePlatformConsoleAuth_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_ConfigurePlatformConsoleAuth.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsConfigurePlatformConsoleAuth(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .configurePlatformConsoleAuthWithResponse("rgpurestorage", "storagepool-01",
                new SshPlatformConsoleAuthConfig().withUsername("alice")
                    .withPublicKey("fakeTokenPlaceholder")
                    .withRole(PlatformConsoleRole.STORAGE_ADMIN),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_Create.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsCreate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .define("storagepool-01")
            .withRegion("eastus")
            .withExistingResourceGroup("rgpurestorage")
            .withTags(mapOf("environment", "production"))
            .withProperties(new StoragePoolProperties().withAvailabilityZone("1")
                .withVnetInjection(new VnetInjection().withSubnetId(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/Microsoft.Network/virtualNetworks/vnet-01/subnets/subnet-01")
                    .withVnetId(
                        "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/Microsoft.Network/virtualNetworks/vnet-01"))
                .withProvisionedBandwidthMbPerSec(17L)
                .withReservationResourceId(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/PureStorage.Block/reservations/reservation-01"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("identity-01", new UserAssignedIdentity())))
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools().delete("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_DisableAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_DisableAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsDisableAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .disableAvsConnection("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_EnableAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_EnableAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsEnableAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .enableAvsConnection("rgpurestorage", "storagepool-01",
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_FinalizeAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_FinalizeAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsFinalizeAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .finalizeAvsConnection("rgpurestorage", "storagepool-01",
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_GetAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_GetAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsGetAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .getAvsConnectionWithResponse("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_GetAvsStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_GetAvsStatus.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsGetAvsStatus(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .getAvsStatusWithResponse("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .getByResourceGroupWithResponse("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_GetHealthStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_GetHealthStatus.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsGetHealthStatus(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .getHealthStatusWithResponse("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_ListBySubscription_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_ListByResourceGroup_MaximumSet_Gen.json
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

### StoragePools_ListPlatformConsoleActivationCode

```java
/**
 * Samples for StoragePools ListPlatformConsoleActivationCode.
 */
public final class StoragePoolsListPlatformConsoleActivationCodeSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/StoragePools_ListPlatformConsoleActivationCode_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_ListPlatformConsoleActivationCode.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsListPlatformConsoleActivationCode(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools()
            .listPlatformConsoleActivationCodeWithResponse("rgpurestorage", "storagepool-01",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_RepairAvsConnection_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_RepairAvsConnection.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        storagePoolsRepairAvsConnection(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.storagePools().repairAvsConnection("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/StoragePools_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: StoragePools_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void storagePoolsUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        StoragePool resource = manager.storagePools()
            .getByResourceGroupWithResponse("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key9065", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("identity-01", new UserAssignedIdentity())))
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

### VolumeGroupSnapshots_Create

```java
import com.azure.resourcemanager.purestorageblock.models.VolumeGroupSnapshotProperties;

/**
 * Samples for VolumeGroupSnapshots Create.
 */
public final class VolumeGroupSnapshotsCreateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroupSnapshots_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroupSnapshots_Create.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        volumeGroupSnapshotsCreate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroupSnapshots()
            .define("snapshot-01")
            .withExistingVolumeGroup("rgpurestorage", "storagepool-01", "volumegroup-01")
            .withProperties(new VolumeGroupSnapshotProperties().withSourceSnapshotResourceId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/PureStorage.Block/storagePools/storagepool-01/volumeGroups/volumegroup-01/snapshots/snapshot-01"))
            .create();
    }
}
```

### VolumeGroupSnapshots_Delete

```java
/**
 * Samples for VolumeGroupSnapshots Delete.
 */
public final class VolumeGroupSnapshotsDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroupSnapshots_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroupSnapshots_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        volumeGroupSnapshotsDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroupSnapshots()
            .delete("rgpurestorage", "storagepool-01", "volumegroup-01", "snapshotdd",
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroupSnapshots_Get

```java
/**
 * Samples for VolumeGroupSnapshots Get.
 */
public final class VolumeGroupSnapshotsGetSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroupSnapshots_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroupSnapshots_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        volumeGroupSnapshotsGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroupSnapshots()
            .getWithResponse("rgpurestorage", "storagepool-01", "volumegroup-01", "01",
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroupSnapshots_ListByVolumeGroup

```java
/**
 * Samples for VolumeGroupSnapshots ListByVolumeGroup.
 */
public final class VolumeGroupSnapshotsListByVolumeGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroupSnapshots_ListByVolumeGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroupSnapshots_ListByVolumeGroup.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumeGroupSnapshotsListByVolumeGroup(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroupSnapshots()
            .listByVolumeGroup("rgpurestorage", "storagepool-01", "volumegroup-01", null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroupSnapshots_ListSnapshots

```java
import com.azure.resourcemanager.purestorageblock.models.VolumeGroupSnapshotListRequest;

/**
 * Samples for VolumeGroupSnapshots ListSnapshots.
 */
public final class VolumeGroupSnapshotsListSnapshotsSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroupSnapshots_ListSnapshots_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroupSnapshots_ListSnapshots.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        volumeGroupSnapshotsListSnapshots(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroupSnapshots()
            .listSnapshotsWithResponse("rgpurestorage", "storagepool-01", "volumegroup-01",
                new VolumeGroupSnapshotListRequest().withFilter("substringof('snapshot', name)")
                    .withOrderby("name asc")
                    .withTop(10)
                    .withSkip(0),
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_Create

```java
import com.azure.resourcemanager.purestorageblock.models.PerformanceParameters;
import com.azure.resourcemanager.purestorageblock.models.ProtectionParameters;
import com.azure.resourcemanager.purestorageblock.models.VolumeGroupProperties;
import com.azure.resourcemanager.purestorageblock.models.VolumeGroupSourceType;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VolumeGroups Create.
 */
public final class VolumeGroupsCreateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroups_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Create.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumeGroupsCreate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroups()
            .define("volumegroup-01")
            .withRegion("eastus")
            .withExistingStoragePool("rgpurestorage", "storagepool-01")
            .withTags(mapOf("environment", "production"))
            .withProperties(new VolumeGroupProperties().withSourceType(VolumeGroupSourceType.SNAPSHOT)
                .withSourceSnapshotResourceId(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/PureStorage.Block/storagePools/storagepool-01/volumeGroups/volumegroup-src/snapshots/snapshot-01")
                .withPerformanceParameters(
                    new PerformanceParameters().withBandwidthLimitMbPerSec(500L).withIopsLimit(10000L))
                .withProtectionParameters(new ProtectionParameters().withRetention(Duration.parse("P7D"))
                    .withFrequency(Duration.parse("PT1H"))))
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

### VolumeGroups_Delete

```java
/**
 * Samples for VolumeGroups Delete.
 */
public final class VolumeGroupsDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroups_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumeGroupsDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroups()
            .delete("rgpurestorage", "storagepool-01", "volumegroup-01", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_Get

```java
/**
 * Samples for VolumeGroups Get.
 */
public final class VolumeGroupsGetSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroups_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumeGroupsGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroups()
            .getWithResponse("rgpurestorage", "storagepool-01", "volumegroup-01", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_GetStatus

```java
/**
 * Samples for VolumeGroups GetStatus.
 */
public final class VolumeGroupsGetStatusSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroups_GetStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_GetStatus.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        volumeGroupsGetStatus(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroups()
            .getStatusWithResponse("rgpurestorage", "storagepool-01", "volumegroup-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_ListByStoragePool

```java
/**
 * Samples for VolumeGroups ListByStoragePool.
 */
public final class VolumeGroupsListByStoragePoolSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroups_ListByStoragePool_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_ListByStoragePool.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        volumeGroupsListByStoragePool(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroups().listByStoragePool("rgpurestorage", "storagepool-01", com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_ListConnectionParameters

```java
/**
 * Samples for VolumeGroups ListConnectionParameters.
 */
public final class VolumeGroupsListConnectionParametersSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroups_ListConnectionParameters_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_ListConnectionParameters.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumeGroupsListConnectionParameters(
        com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroups()
            .listConnectionParametersWithResponse("rgpurestorage", "storagepool-01", "volumegroup-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_Overwrite

```java
import com.azure.resourcemanager.purestorageblock.models.VolumeGroupOverwriteRequest;

/**
 * Samples for VolumeGroups Overwrite.
 */
public final class VolumeGroupsOverwriteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroups_Overwrite_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Overwrite.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        volumeGroupsOverwrite(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumeGroups()
            .overwrite("rgpurestorage", "storagepool-01", "volumegroup-01", new VolumeGroupOverwriteRequest()
                .withSourceSnapshotResourceId(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/PureStorage.Block/storagePools/storagepool-01/volumeGroups/volumegroup-01/snapshots/snapshot-01")
                .withSourceVolumeGroupResourceId(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/PureStorage.Block/storagePools/storagepool-01/volumeGroups/volumegroup-01"),
                com.azure.core.util.Context.NONE);
    }
}
```

### VolumeGroups_Update

```java
import com.azure.resourcemanager.purestorageblock.models.PerformanceParameters;
import com.azure.resourcemanager.purestorageblock.models.ProtectionParameters;
import com.azure.resourcemanager.purestorageblock.models.VolumeGroup;
import com.azure.resourcemanager.purestorageblock.models.VolumeGroupUpdateProperties;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VolumeGroups Update.
 */
public final class VolumeGroupsUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/VolumeGroups_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: VolumeGroups_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumeGroupsUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        VolumeGroup resource = manager.volumeGroups()
            .getWithResponse("rgpurestorage", "storagepool-01", "volumegroup-01", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production"))
            .withProperties(new VolumeGroupUpdateProperties()
                .withPerformanceParameters(
                    new PerformanceParameters().withBandwidthLimitMbPerSec(750L).withIopsLimit(15000L))
                .withProtectionParameters(new ProtectionParameters().withRetention(Duration.parse("P14D"))
                    .withFrequency(Duration.parse("PT2H"))))
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

### Volumes_Create

```java
import com.azure.resourcemanager.purestorageblock.models.AzureVolumeProperties;
import com.azure.resourcemanager.purestorageblock.models.VolumeSourceType;

/**
 * Samples for Volumes Create.
 */
public final class VolumesCreateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Volumes_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Create.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumesCreate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumes()
            .define("volume-01")
            .withExistingVolumeGroup("rgpurestorage", "storagepool-01", "volumegroup-01")
            .withProperties(new AzureVolumeProperties().withProvisionedSize(10737418240L)
                .withSourceVolumeResourceId(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/PureStorage.Block/storagePools/storagepool-01/volumeGroups/volumegroup-01/volumes/source-volume")
                .withSourceType(VolumeSourceType.VOLUME))
            .create();
    }
}
```

### Volumes_Delete

```java
/**
 * Samples for Volumes Delete.
 */
public final class VolumesDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Volumes_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Delete.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumesDelete(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumes()
            .delete("rgpurestorage", "storagepool-01", "volumegroup-01", "volume-01", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Get

```java
/**
 * Samples for Volumes Get.
 */
public final class VolumesGetSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Volumes_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Get.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumesGet(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumes()
            .getWithResponse("rgpurestorage", "storagepool-01", "volumegroup-01", "volume-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ListByVolumeGroup

```java
/**
 * Samples for Volumes ListByVolumeGroup.
 */
public final class VolumesListByVolumeGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Volumes_ListByVolumeGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_ListByVolumeGroup.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void
        volumesListByVolumeGroup(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumes()
            .listByVolumeGroup("rgpurestorage", "storagepool-01", "volumegroup-01", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Overwrite

```java
import com.azure.resourcemanager.purestorageblock.models.VolumeOverwriteRequest;
import com.azure.resourcemanager.purestorageblock.models.VolumeSnapshotSource;
import com.azure.resourcemanager.purestorageblock.models.VolumeSourceType;

/**
 * Samples for Volumes Overwrite.
 */
public final class VolumesOverwriteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Volumes_Overwrite_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Overwrite.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumesOverwrite(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        manager.volumes()
            .overwrite("rgpurestorage", "storagepool-01", "volumegroup-01", "volume-01", new VolumeOverwriteRequest()
                .withSourceType(VolumeSourceType.SNAPSHOT)
                .withSourceVolumeGroupResourceId(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/PureStorage.Block/storagePools/storagepool-01/volumeGroups/volumegroup-01")
                .withSourceVolumeSnapshot(new VolumeSnapshotSource().withVolumeGroupSnapshotResourceId(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgpurestorage/providers/PureStorage.Block/storagePools/storagepool-01/volumeGroups/volumegroup-01/snapshots/snapshot-01")
                    .withVolumeSnapshotName("volume-01")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Update

```java
import com.azure.resourcemanager.purestorageblock.models.Volume;
import com.azure.resourcemanager.purestorageblock.models.VolumeUpdateProperties;

/**
 * Samples for Volumes Update.
 */
public final class VolumesUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Volumes_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Volumes_Update.
     * 
     * @param manager Entry point to PureStorageBlockManager.
     */
    public static void volumesUpdate(com.azure.resourcemanager.purestorageblock.PureStorageBlockManager manager) {
        Volume resource = manager.volumes()
            .getWithResponse("rgpurestorage", "storagepool-01", "volumegroup-01", "volume-01",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new VolumeUpdateProperties().withProvisionedSize(21474836480L)).apply();
    }
}
```

