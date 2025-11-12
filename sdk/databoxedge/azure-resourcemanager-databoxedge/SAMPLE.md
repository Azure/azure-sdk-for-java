# Code snippets and samples


## Addons

- [CreateOrUpdate](#addons_createorupdate)
- [Delete](#addons_delete)
- [Get](#addons_get)
- [ListByRole](#addons_listbyrole)

## Alerts

- [Get](#alerts_get)
- [ListByDataBoxEdgeDevice](#alerts_listbydataboxedgedevice)

## AvailableSkus

- [List](#availableskus_list)

## BandwidthSchedules

- [CreateOrUpdate](#bandwidthschedules_createorupdate)
- [Delete](#bandwidthschedules_delete)
- [Get](#bandwidthschedules_get)
- [ListByDataBoxEdgeDevice](#bandwidthschedules_listbydataboxedgedevice)

## Containers

- [CreateOrUpdate](#containers_createorupdate)
- [Delete](#containers_delete)
- [Get](#containers_get)
- [ListByStorageAccount](#containers_listbystorageaccount)
- [Refresh](#containers_refresh)

## DeviceCapacityCheck

- [CheckResourceCreationFeasibility](#devicecapacitycheck_checkresourcecreationfeasibility)

## DeviceCapacityInfo

- [GetDeviceCapacityInfo](#devicecapacityinfo_getdevicecapacityinfo)

## Devices

- [CreateOrUpdate](#devices_createorupdate)
- [CreateOrUpdateSecuritySettings](#devices_createorupdatesecuritysettings)
- [Delete](#devices_delete)
- [DownloadUpdates](#devices_downloadupdates)
- [GenerateCertificate](#devices_generatecertificate)
- [GetByResourceGroup](#devices_getbyresourcegroup)
- [GetExtendedInformation](#devices_getextendedinformation)
- [GetNetworkSettings](#devices_getnetworksettings)
- [GetUpdateSummary](#devices_getupdatesummary)
- [InstallUpdates](#devices_installupdates)
- [List](#devices_list)
- [ListByResourceGroup](#devices_listbyresourcegroup)
- [ScanForUpdates](#devices_scanforupdates)
- [Update](#devices_update)
- [UpdateExtendedInformation](#devices_updateextendedinformation)
- [UploadCertificate](#devices_uploadcertificate)

## DiagnosticSettings

- [GetDiagnosticProactiveLogCollectionSettings](#diagnosticsettings_getdiagnosticproactivelogcollectionsettings)
- [GetDiagnosticRemoteSupportSettings](#diagnosticsettings_getdiagnosticremotesupportsettings)
- [UpdateDiagnosticProactiveLogCollectionSettings](#diagnosticsettings_updatediagnosticproactivelogcollectionsettings)
- [UpdateDiagnosticRemoteSupportSettings](#diagnosticsettings_updatediagnosticremotesupportsettings)

## Jobs

- [Get](#jobs_get)

## MonitoringConfig

- [CreateOrUpdate](#monitoringconfig_createorupdate)
- [Delete](#monitoringconfig_delete)
- [Get](#monitoringconfig_get)
- [List](#monitoringconfig_list)

## Nodes

- [ListByDataBoxEdgeDevice](#nodes_listbydataboxedgedevice)

## Operations

- [List](#operations_list)

## OperationsStatus

- [Get](#operationsstatus_get)

## Orders

- [CreateOrUpdate](#orders_createorupdate)
- [Delete](#orders_delete)
- [Get](#orders_get)
- [ListByDataBoxEdgeDevice](#orders_listbydataboxedgedevice)
- [ListDCAccessCode](#orders_listdcaccesscode)

## Roles

- [CreateOrUpdate](#roles_createorupdate)
- [Delete](#roles_delete)
- [Get](#roles_get)
- [ListByDataBoxEdgeDevice](#roles_listbydataboxedgedevice)

## Shares

- [CreateOrUpdate](#shares_createorupdate)
- [Delete](#shares_delete)
- [Get](#shares_get)
- [ListByDataBoxEdgeDevice](#shares_listbydataboxedgedevice)
- [Refresh](#shares_refresh)

## StorageAccountCredentials

- [CreateOrUpdate](#storageaccountcredentials_createorupdate)
- [Delete](#storageaccountcredentials_delete)
- [Get](#storageaccountcredentials_get)
- [ListByDataBoxEdgeDevice](#storageaccountcredentials_listbydataboxedgedevice)

## StorageAccounts

- [CreateOrUpdate](#storageaccounts_createorupdate)
- [Delete](#storageaccounts_delete)
- [Get](#storageaccounts_get)
- [ListByDataBoxEdgeDevice](#storageaccounts_listbydataboxedgedevice)

## SupportPackages

- [TriggerSupportPackage](#supportpackages_triggersupportpackage)

## Triggers

- [CreateOrUpdate](#triggers_createorupdate)
- [Delete](#triggers_delete)
- [Get](#triggers_get)
- [ListByDataBoxEdgeDevice](#triggers_listbydataboxedgedevice)

## Users

- [CreateOrUpdate](#users_createorupdate)
- [Delete](#users_delete)
- [Get](#users_get)
- [ListByDataBoxEdgeDevice](#users_listbydataboxedgedevice)
### Addons_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.AddonProperties;
import com.azure.resourcemanager.databoxedge.models.AddonType;

/**
 * Samples for Addons CreateOrUpdate.
 */
public final class AddonsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/PutAddons.json
     */
    /**
     * Sample code: PutAddOns.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void putAddOns(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.addons()
            .define("arcName")
            .withExistingDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice")
            .withKind(AddonType.ARC_FOR_KUBERNETES)
            .withProperties(new AddonProperties())
            .create();
    }
}
```

### Addons_Delete

```java
/**
 * Samples for Addons Delete.
 */
public final class AddonsDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/DeleteAddons.json
     */
    /**
     * Sample code: DeleteAddOns.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void deleteAddOns(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.addons()
            .delete("GroupForEdgeAutomation", "testedgedevice", "arcName", com.azure.core.util.Context.NONE);
    }
}
```

### Addons_Get

```java
/**
 * Samples for Addons Get.
 */
public final class AddonsGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/GetAddons.json
     */
    /**
     * Sample code: GetAddOns.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void getAddOns(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.addons()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "arcName", com.azure.core.util.Context.NONE);
    }
}
```

### Addons_ListByRole

```java
/**
 * Samples for Addons ListByRole.
 */
public final class AddonsListByRoleSamples {
    /*
     * x-ms-original-file: 2023-12-01/RoleListAddOns.json
     */
    /**
     * Sample code: RoleListAddOns.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void roleListAddOns(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.addons().listByRole("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_Get

```java
/**
 * Samples for Alerts Get.
 */
public final class AlertsGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/AlertGet.json
     */
    /**
     * Sample code: AlertGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void alertGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.alerts()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "159a00c7-8543-4343-9435-263ac87df3bb",
                com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_ListByDataBoxEdgeDevice

```java
/**
 * Samples for Alerts ListByDataBoxEdgeDevice.
 */
public final class AlertsListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/AlertGetAllInDevice.json
     */
    /**
     * Sample code: AlertGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void alertGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.alerts()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### AvailableSkus_List

```java
/**
 * Samples for AvailableSkus List.
 */
public final class AvailableSkusListSamples {
    /*
     * x-ms-original-file: 2023-12-01/AvailableSkusList.json
     */
    /**
     * Sample code: AvailableSkus.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void availableSkus(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.availableSkus().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### BandwidthSchedules_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.DayOfWeek;
import java.util.Arrays;

/**
 * Samples for BandwidthSchedules CreateOrUpdate.
 */
public final class BandwidthSchedulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/BandwidthSchedulePut.json
     */
    /**
     * Sample code: BandwidthSchedulePut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void bandwidthSchedulePut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.bandwidthSchedules()
            .define("bandwidth-1")
            .withExistingDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice")
            .withStart("0:0:0")
            .withStop("13:59:0")
            .withRateInMbps(100)
            .withDays(Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.MONDAY))
            .create();
    }
}
```

### BandwidthSchedules_Delete

```java
/**
 * Samples for BandwidthSchedules Delete.
 */
public final class BandwidthSchedulesDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/BandwidthScheduleDelete.json
     */
    /**
     * Sample code: BandwidthScheduleDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void bandwidthScheduleDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.bandwidthSchedules()
            .delete("GroupForEdgeAutomation", "testedgedevice", "bandwidth-1", com.azure.core.util.Context.NONE);
    }
}
```

### BandwidthSchedules_Get

```java
/**
 * Samples for BandwidthSchedules Get.
 */
public final class BandwidthSchedulesGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/BandwidthScheduleGet.json
     */
    /**
     * Sample code: BandwidthScheduleGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void bandwidthScheduleGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.bandwidthSchedules()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "bandwidth-1",
                com.azure.core.util.Context.NONE);
    }
}
```

### BandwidthSchedules_ListByDataBoxEdgeDevice

```java
/**
 * Samples for BandwidthSchedules ListByDataBoxEdgeDevice.
 */
public final class BandwidthSchedulesListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/BandwidthScheduleGetAllInDevice.json
     */
    /**
     * Sample code: BandwidthScheduleGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void
        bandwidthScheduleGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.bandwidthSchedules()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Containers_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.AzureContainerDataFormat;

/**
 * Samples for Containers CreateOrUpdate.
 */
public final class ContainersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/ContainerPut.json
     */
    /**
     * Sample code: ContainerPut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.containers()
            .define("blobcontainer1")
            .withExistingStorageAccount("GroupForEdgeAutomation", "testedgedevice", "storageaccount1")
            .withDataFormat(AzureContainerDataFormat.BLOCK_BLOB)
            .create();
    }
}
```

### Containers_Delete

```java
/**
 * Samples for Containers Delete.
 */
public final class ContainersDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/ContainerDelete.json
     */
    /**
     * Sample code: ContainerDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.containers()
            .delete("GroupForEdgeAutomation", "testedgedevice", "storageaccount1", "blobcontainer1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Containers_Get

```java
/**
 * Samples for Containers Get.
 */
public final class ContainersGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/ContainerGet.json
     */
    /**
     * Sample code: ContainerGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.containers()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "storageaccount1", "blobcontainer1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Containers_ListByStorageAccount

```java
/**
 * Samples for Containers ListByStorageAccount.
 */
public final class ContainersListByStorageAccountSamples {
    /*
     * x-ms-original-file: 2023-12-01/ContainerListAllInDevice.json
     */
    /**
     * Sample code: ContainerListAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerListAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.containers()
            .listByStorageAccount("GroupForEdgeAutomation", "testedgedevice", "storageaccount1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Containers_Refresh

```java
/**
 * Samples for Containers Refresh.
 */
public final class ContainersRefreshSamples {
    /*
     * x-ms-original-file: 2023-12-01/ContainerRefresh.json
     */
    /**
     * Sample code: ContainerRefresh.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerRefresh(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.containers()
            .refresh("GroupForEdgeAutomation", "testedgedevice", "storageaccount1", "blobcontainer1",
                com.azure.core.util.Context.NONE);
    }
}
```

### DeviceCapacityCheck_CheckResourceCreationFeasibility

```java
import com.azure.resourcemanager.databoxedge.models.DeviceCapacityRequestInfo;
import java.util.Arrays;

/**
 * Samples for DeviceCapacityCheck CheckResourceCreationFeasibility.
 */
public final class DeviceCapacityCheckCheckResourceCreationFeasibilitySamples {
    /*
     * x-ms-original-file: 2023-12-01/DeviceCapacityRequestPost.json
     */
    /**
     * Sample code: DeviceCapacityRequestPost.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void deviceCapacityRequestPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.deviceCapacityChecks()
            .checkResourceCreationFeasibility("GroupForEdgeAutomation", "testedgedevice",
                new DeviceCapacityRequestInfo().withVmPlacementQuery(Arrays.asList(Arrays.asList("Standard_D2_v2"))),
                null, com.azure.core.util.Context.NONE);
    }
}
```

### DeviceCapacityInfo_GetDeviceCapacityInfo

```java
/**
 * Samples for DeviceCapacityInfo GetDeviceCapacityInfo.
 */
public final class DeviceCapacityInfoGetDeviceCapacityInfoSamples {
    /*
     * x-ms-original-file: 2023-12-01/DeviceCapacityGet.json
     */
    /**
     * Sample code: DeviceCapacityGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void deviceCapacityGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.deviceCapacityInfoes()
            .getDeviceCapacityInfoWithResponse("GroupForEdgeAutomation", "testedgedevice",
                com.azure.core.util.Context.NONE);
    }
}
```

### Devices_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.DataResidency;
import com.azure.resourcemanager.databoxedge.models.DataResidencyType;
import com.azure.resourcemanager.databoxedge.models.Sku;
import com.azure.resourcemanager.databoxedge.models.SkuName;
import com.azure.resourcemanager.databoxedge.models.SkuTier;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Devices CreateOrUpdate.
 */
public final class DevicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/DataBoxEdgeDevicePutWithDataResidency.json
     */
    /**
     * Sample code: DataBoxEdgeDevicePutWithDataResidency.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void
        dataBoxEdgeDevicePutWithDataResidency(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .define("testedgedevice")
            .withRegion("WUS")
            .withExistingResourceGroup("GroupForEdgeAutomation")
            .withTags(mapOf())
            .withSku(new Sku().withName(SkuName.EDGE).withTier(SkuTier.STANDARD))
            .withDataResidency(new DataResidency().withType(DataResidencyType.ZONE_REPLICATION))
            .create();
    }

    /*
     * x-ms-original-file: 2023-12-01/DataBoxEdgeDevicePut.json
     */
    /**
     * Sample code: DataBoxEdgeDevicePut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void dataBoxEdgeDevicePut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .define("testedgedevice")
            .withRegion("WUS")
            .withExistingResourceGroup("GroupForEdgeAutomation")
            .withTags(mapOf())
            .withSku(new Sku().withName(SkuName.EDGE).withTier(SkuTier.STANDARD))
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

### Devices_CreateOrUpdateSecuritySettings

```java
import com.azure.resourcemanager.databoxedge.models.AsymmetricEncryptedSecret;
import com.azure.resourcemanager.databoxedge.models.EncryptionAlgorithm;
import com.azure.resourcemanager.databoxedge.models.SecuritySettings;

/**
 * Samples for Devices CreateOrUpdateSecuritySettings.
 */
public final class DevicesCreateOrUpdateSecuritySettingsSamples {
    /*
     * x-ms-original-file: 2023-12-01/SecuritySettingsUpdatePost.json
     */
    /**
     * Sample code: CreateOrUpdateSecuritySettings.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void
        createOrUpdateSecuritySettings(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .createOrUpdateSecuritySettings("AzureVM", "testedgedevice",
                new SecuritySettings()
                    .withDeviceAdminPassword(new AsymmetricEncryptedSecret().withValue("<deviceAdminPassword>")
                        .withEncryptionCertThumbprint("<encryptionThumprint>")
                        .withEncryptionAlgorithm(EncryptionAlgorithm.AES256)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Devices_Delete

```java
/**
 * Samples for Devices Delete.
 */
public final class DevicesDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/DataBoxEdgeDeviceDelete.json
     */
    /**
     * Sample code: DataBoxEdgeDeviceDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void dataBoxEdgeDeviceDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().delete("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_DownloadUpdates

```java
/**
 * Samples for Devices DownloadUpdates.
 */
public final class DevicesDownloadUpdatesSamples {
    /*
     * x-ms-original-file: 2023-12-01/DownloadUpdatesPost.json
     */
    /**
     * Sample code: DownloadUpdatesPost.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void downloadUpdatesPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().downloadUpdates("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_GenerateCertificate

```java
/**
 * Samples for Devices GenerateCertificate.
 */
public final class DevicesGenerateCertificateSamples {
    /*
     * x-ms-original-file: 2023-12-01/GenerateCertificate.json
     */
    /**
     * Sample code: GenerateCertificate.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void generateCertificate(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .generateCertificateWithResponse("GroupForEdgeAutomation", "testedgedevice",
                com.azure.core.util.Context.NONE);
    }
}
```

### Devices_GetByResourceGroup

```java
/**
 * Samples for Devices GetByResourceGroup.
 */
public final class DevicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-12-01/DataBoxEdgeDeviceGetByName.json
     */
    /**
     * Sample code: DataBoxEdgeDeviceGetByName.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void dataBoxEdgeDeviceGetByName(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .getByResourceGroupWithResponse("GroupForEdgeAutomation", "testedgedevice",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2023-12-01/DataBoxEdgeDeviceGetByNameWithDataResidency.json
     */
    /**
     * Sample code: DataBoxEdgeDeviceGetByNameWithDataResidency.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void
        dataBoxEdgeDeviceGetByNameWithDataResidency(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .getByResourceGroupWithResponse("GroupForEdgeAutomation", "testedgedevice",
                com.azure.core.util.Context.NONE);
    }
}
```

### Devices_GetExtendedInformation

```java
/**
 * Samples for Devices GetExtendedInformation.
 */
public final class DevicesGetExtendedInformationSamples {
    /*
     * x-ms-original-file: 2023-12-01/ExtendedInfoPost.json
     */
    /**
     * Sample code: ExtendedInfoPost.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void extendedInfoPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .getExtendedInformationWithResponse("GroupForEdgeAutomation", "testedgedevice",
                com.azure.core.util.Context.NONE);
    }
}
```

### Devices_GetNetworkSettings

```java
/**
 * Samples for Devices GetNetworkSettings.
 */
public final class DevicesGetNetworkSettingsSamples {
    /*
     * x-ms-original-file: 2023-12-01/NetworkSettingsGet.json
     */
    /**
     * Sample code: NetworkSettingsGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void networkSettingsGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .getNetworkSettingsWithResponse("GroupForEdgeAutomation", "testedgedevice",
                com.azure.core.util.Context.NONE);
    }
}
```

### Devices_GetUpdateSummary

```java
/**
 * Samples for Devices GetUpdateSummary.
 */
public final class DevicesGetUpdateSummarySamples {
    /*
     * x-ms-original-file: 2023-12-01/UpdateSummaryGet.json
     */
    /**
     * Sample code: UpdateSummaryGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void updateSummaryGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .getUpdateSummaryWithResponse("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_InstallUpdates

```java
/**
 * Samples for Devices InstallUpdates.
 */
public final class DevicesInstallUpdatesSamples {
    /*
     * x-ms-original-file: 2023-12-01/InstallUpdatesPost.json
     */
    /**
     * Sample code: InstallUpdatesPost.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void installUpdatesPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().installUpdates("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_List

```java
/**
 * Samples for Devices List.
 */
public final class DevicesListSamples {
    /*
     * x-ms-original-file: 2023-12-01/DataBoxEdgeDeviceGetBySubscription.json
     */
    /**
     * Sample code: DataBoxEdgeDeviceGetBySubscription.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void
        dataBoxEdgeDeviceGetBySubscription(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### Devices_ListByResourceGroup

```java
/**
 * Samples for Devices ListByResourceGroup.
 */
public final class DevicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-12-01/DataBoxEdgeDeviceGetByResourceGroup.json
     */
    /**
     * Sample code: DataBoxEdgeDeviceGetByResourceGroup.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void
        dataBoxEdgeDeviceGetByResourceGroup(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().listByResourceGroup("GroupForEdgeAutomation", null, com.azure.core.util.Context.NONE);
    }
}
```

### Devices_ScanForUpdates

```java
/**
 * Samples for Devices ScanForUpdates.
 */
public final class DevicesScanForUpdatesSamples {
    /*
     * x-ms-original-file: 2023-12-01/ScanForUpdatesPost.json
     */
    /**
     * Sample code: ScanForUpdatesPost.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void scanForUpdatesPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().scanForUpdates("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_Update

```java
import com.azure.resourcemanager.databoxedge.models.DataBoxEdgeDevice;
import com.azure.resourcemanager.databoxedge.models.EdgeProfilePatch;
import com.azure.resourcemanager.databoxedge.models.EdgeProfileSubscriptionPatch;

/**
 * Samples for Devices Update.
 */
public final class DevicesUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/DataBoxEdgeDevicePatch.json
     */
    /**
     * Sample code: DataBoxEdgeDevicePatch.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void dataBoxEdgeDevicePatch(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        DataBoxEdgeDevice resource = manager.devices()
            .getByResourceGroupWithResponse("GroupForEdgeAutomation", "testedgedevice",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withEdgeProfile(new EdgeProfilePatch().withSubscription(new EdgeProfileSubscriptionPatch().withId(
                "/subscriptions/0d44739e-0563-474f-97e7-24a0cdb23b29/resourceGroups/rapvs-rg/providers/Microsoft.AzureStack/linkedSubscriptions/ca014ddc-5cf2-45f8-b390-e901e4a0ae87")))
            .apply();
    }
}
```

### Devices_UpdateExtendedInformation

```java
import com.azure.resourcemanager.databoxedge.models.DataBoxEdgeDeviceExtendedInfoPatch;

/**
 * Samples for Devices UpdateExtendedInformation.
 */
public final class DevicesUpdateExtendedInformationSamples {
    /*
     * x-ms-original-file: 2023-12-01/GetUpdateExtendedInfo.json
     */
    /**
     * Sample code: GetUpdateExtendedInfo.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void getUpdateExtendedInfo(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .updateExtendedInformationWithResponse("GroupForEdgeAutomation", "testedgedevice",
                new DataBoxEdgeDeviceExtendedInfoPatch(), com.azure.core.util.Context.NONE);
    }
}
```

### Devices_UploadCertificate

```java
import com.azure.resourcemanager.databoxedge.models.UploadCertificateRequest;

/**
 * Samples for Devices UploadCertificate.
 */
public final class DevicesUploadCertificateSamples {
    /*
     * x-ms-original-file: 2023-12-01/UploadCertificatePost.json
     */
    /**
     * Sample code: UploadCertificatePost.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void uploadCertificatePost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices()
            .uploadCertificateWithResponse("GroupForEdgeAutomation", "testedgedevice",
                new UploadCertificateRequest().withCertificate(
                    "MIIC9DCCAdygAwIBAgIQWJae7GNjiI9Mcv/gJyrOPTANBgkqhkiG9w0BAQUFADASMRAwDgYDVQQDDAdXaW5kb3dzMB4XDTE4MTEyNzAwMTA0NVoXDTIxMTEyODAwMTA0NVowEjEQMA4GA1UEAwwHV2luZG93czCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKxkRExqxf0qH1avnyORptIbRC2yQwqe3EIbJ2FPKr5jtAppGeX/dGKrFSnX+7/0HFr77aJHafdpEAtOiLyJ4zCAVs0obZCCIq4qJdmjYUTU0UXH/w/YzXfQA0d9Zh9AN+NJBX9xj05NzgsT24fkgsK2v6mWJQXT7YcWAsl5sEYPnx1e+MrupNyVSL/RUJmrS+etJSysHtFeWRhsUhVAs1DD5ExJvBLU3WH0IsojEvpXcjrutB5/MDQNrd/StGI6WovoSSPH7FyT9tgERx+q+Yg3YUGzfaIPCctlrRGehcdtzdNoKd0rsX62yCq0U6POoSfwe22NJu41oAUMd7e6R8cCAwEAAaNGMEQwEwYDVR0lBAwwCgYIKwYBBQUHAwIwHQYDVR0OBBYEFDd0VxnS3LnMIfwc7xW4b4IZWG5GMA4GA1UdDwEB/wQEAwIFIDANBgkqhkiG9w0BAQUFAAOCAQEAPQRby2u9celvtvL/DLEb5Vt3/tPStRQC5MyTD62L5RT/q8E6EMCXVZNkXF5WlWucLJi/18tY+9PNgP9xWLJh7kpSWlWdi9KPtwMqKDlEH8L2TnQdjimt9XuiCrTnoFy/1X2BGLY/rCaUJNSd15QCkz2xeW+Z+YSk2GwAc/A/4YfNpqSIMfNuPrT76o02VdD9WmJUA3fS/HY0sU9qgQRS/3F5/0EPS+HYQ0SvXCK9tggcCd4O050ytNBMJC9qMOJ7yE0iOrFfOJSCfDAuPhn/rHFh79Kn1moF+/CE+nc0/2RPiLC8r54/rt5dYyyxJDfXg0a3VrrX39W69WZGW5OXiw=="),
                com.azure.core.util.Context.NONE);
    }
}
```

### DiagnosticSettings_GetDiagnosticProactiveLogCollectionSettings

```java
/**
 * Samples for DiagnosticSettings GetDiagnosticProactiveLogCollectionSettings.
 */
public final class DiagnosticSettingsGetDiagnosticProactiveLogCollectionSettingsSamples {
    /*
     * x-ms-original-file: 2023-12-01/GetDiagnosticProactiveLogCollectionSettings.json
     */
    /**
     * Sample code: GetDiagnosticProactiveLogCollectionSettings.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void
        getDiagnosticProactiveLogCollectionSettings(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.diagnosticSettings()
            .getDiagnosticProactiveLogCollectionSettingsWithResponse("GroupForEdgeAutomation", "testedgedevice",
                com.azure.core.util.Context.NONE);
    }
}
```

### DiagnosticSettings_GetDiagnosticRemoteSupportSettings

```java
/**
 * Samples for DiagnosticSettings GetDiagnosticRemoteSupportSettings.
 */
public final class DiagnosticSettingsGetDiagnosticRemoteSupportSettingsSamples {
    /*
     * x-ms-original-file: 2023-12-01/GetDiagnosticRemoteSupportSettings.json
     */
    /**
     * Sample code: GetDiagnosticRemoteSupportSettings.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void
        getDiagnosticRemoteSupportSettings(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.diagnosticSettings()
            .getDiagnosticRemoteSupportSettingsWithResponse("GroupForEdgeAutomation", "testedgedevice",
                com.azure.core.util.Context.NONE);
    }
}
```

### DiagnosticSettings_UpdateDiagnosticProactiveLogCollectionSettings

```java
import com.azure.resourcemanager.databoxedge.fluent.models.DiagnosticProactiveLogCollectionSettingsInner;
import com.azure.resourcemanager.databoxedge.models.ProactiveDiagnosticsConsent;

/**
 * Samples for DiagnosticSettings UpdateDiagnosticProactiveLogCollectionSettings.
 */
public final class DiagnosticSettingsUpdateDiagnosticProactiveLogCollectionSettingsSamples {
    /*
     * x-ms-original-file: 2023-12-01/UpdateDiagnosticProactiveLogCollectionSettings.json
     */
    /**
     * Sample code: UpdateDiagnosticProactiveLogCollectionSettings.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void updateDiagnosticProactiveLogCollectionSettings(
        com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.diagnosticSettings()
            .updateDiagnosticProactiveLogCollectionSettings("GroupForEdgeAutomation", "testedgedevice",
                new DiagnosticProactiveLogCollectionSettingsInner()
                    .withUserConsent(ProactiveDiagnosticsConsent.ENABLED),
                com.azure.core.util.Context.NONE);
    }
}
```

### DiagnosticSettings_UpdateDiagnosticRemoteSupportSettings

```java
import com.azure.resourcemanager.databoxedge.fluent.models.DiagnosticRemoteSupportSettingsInner;
import com.azure.resourcemanager.databoxedge.models.AccessLevel;
import com.azure.resourcemanager.databoxedge.models.RemoteApplicationType;
import com.azure.resourcemanager.databoxedge.models.RemoteSupportSettings;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for DiagnosticSettings UpdateDiagnosticRemoteSupportSettings.
 */
public final class DiagnosticSettingsUpdateDiagnosticRemoteSupportSettingsSamples {
    /*
     * x-ms-original-file: 2023-12-01/UpdateDiagnosticRemoteSupportSettings.json
     */
    /**
     * Sample code: UpdateDiagnosticRemoteSupportSettings.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void
        updateDiagnosticRemoteSupportSettings(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.diagnosticSettings()
            .updateDiagnosticRemoteSupportSettings("GroupForEdgeAutomation", "testedgedevice",
                new DiagnosticRemoteSupportSettingsInner().withRemoteSupportSettingsList(Arrays
                    .asList(new RemoteSupportSettings().withRemoteApplicationType(RemoteApplicationType.POWERSHELL)
                        .withAccessLevel(AccessLevel.READ_WRITE)
                        .withExpirationTimeStampInUTC(OffsetDateTime.parse("2021-07-07T00:00:00+00:00")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_Get

```java
/**
 * Samples for Jobs Get.
 */
public final class JobsGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/JobsGet.json
     */
    /**
     * Sample code: JobsGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void jobsGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.jobs()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "159a00c7-8543-4343-9435-263ac87df3bb",
                com.azure.core.util.Context.NONE);
    }
}
```

### MonitoringConfig_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.fluent.models.MonitoringMetricConfigurationInner;
import com.azure.resourcemanager.databoxedge.models.MetricConfiguration;
import com.azure.resourcemanager.databoxedge.models.MetricCounter;
import com.azure.resourcemanager.databoxedge.models.MetricCounterSet;
import java.util.Arrays;

/**
 * Samples for MonitoringConfig CreateOrUpdate.
 */
public final class MonitoringConfigCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/PutMonitoringConfig.json
     */
    /**
     * Sample code: PutMonitoringConfig.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void putMonitoringConfig(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.monitoringConfigs()
            .createOrUpdate("GroupForEdgeAutomation", "testedgedevice", new MonitoringMetricConfigurationInner()
                .withMetricConfigurations(Arrays.asList(new MetricConfiguration().withResourceId("test")
                    .withMdmAccount("test")
                    .withMetricNameSpace("test")
                    .withCounterSets(Arrays.asList(
                        new MetricCounterSet().withCounters(Arrays.asList(new MetricCounter().withName("test"))))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### MonitoringConfig_Delete

```java
/**
 * Samples for MonitoringConfig Delete.
 */
public final class MonitoringConfigDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/DeleteMonitoringConfig.json
     */
    /**
     * Sample code: DeleteMonitoringConfig.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void deleteMonitoringConfig(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.monitoringConfigs()
            .delete("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### MonitoringConfig_Get

```java
/**
 * Samples for MonitoringConfig Get.
 */
public final class MonitoringConfigGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/GetMonitoringConfig.json
     */
    /**
     * Sample code: GetMonitoringConfig.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void getMonitoringConfig(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.monitoringConfigs()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### MonitoringConfig_List

```java
/**
 * Samples for MonitoringConfig List.
 */
public final class MonitoringConfigListSamples {
    /*
     * x-ms-original-file: 2023-12-01/ListMonitoringConfig.json
     */
    /**
     * Sample code: ListMonitoringConfig.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void listMonitoringConfig(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.monitoringConfigs().list("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Nodes_ListByDataBoxEdgeDevice

```java
/**
 * Samples for Nodes ListByDataBoxEdgeDevice.
 */
public final class NodesListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/NodeGetAllInDevice.json
     */
    /**
     * Sample code: NodesGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void nodesGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.nodes()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2023-12-01/OperationsGet.json
     */
    /**
     * Sample code: OperationsGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void operationsGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### OperationsStatus_Get

```java
/**
 * Samples for OperationsStatus Get.
 */
public final class OperationsStatusGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/OperationsStatusGet.json
     */
    /**
     * Sample code: OperationsStatusGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void operationsStatusGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.operationsStatus()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "159a00c7-8543-4343-9435-263ac87df3bb",
                com.azure.core.util.Context.NONE);
    }
}
```

### Orders_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.fluent.models.OrderInner;
import com.azure.resourcemanager.databoxedge.models.Address;
import com.azure.resourcemanager.databoxedge.models.ContactDetails;
import java.util.Arrays;

/**
 * Samples for Orders CreateOrUpdate.
 */
public final class OrdersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/OrderPut.json
     */
    /**
     * Sample code: OrderPut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void orderPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.orders()
            .createOrUpdate("GroupForEdgeAutomation", "testedgedevice",
                new OrderInner()
                    .withContactInformation(new ContactDetails().withContactPerson("John Mcclane")
                        .withCompanyName("Microsoft")
                        .withPhone("(800) 426-9400")
                        .withEmailList(Arrays.asList("john@microsoft.com")))
                    .withShippingAddress(new Address().withAddressLine1("Microsoft Corporation")
                        .withAddressLine2("One Microsoft Way")
                        .withAddressLine3("Redmond")
                        .withPostalCode("fakeTokenPlaceholder")
                        .withCity("WA")
                        .withState("WA")
                        .withCountry("USA")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Orders_Delete

```java
/**
 * Samples for Orders Delete.
 */
public final class OrdersDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/OrderDelete.json
     */
    /**
     * Sample code: OrderDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void orderDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.orders().delete("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Orders_Get

```java
/**
 * Samples for Orders Get.
 */
public final class OrdersGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/OrderGet.json
     */
    /**
     * Sample code: OrderGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void orderGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.orders().getWithResponse("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Orders_ListByDataBoxEdgeDevice

```java
/**
 * Samples for Orders ListByDataBoxEdgeDevice.
 */
public final class OrdersListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/OrderGetAllInDevice.json
     */
    /**
     * Sample code: OrderGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void orderGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.orders()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Orders_ListDCAccessCode

```java
/**
 * Samples for Orders ListDCAccessCode.
 */
public final class OrdersListDCAccessCodeSamples {
    /*
     * x-ms-original-file: 2023-12-01/GetDCAccessCode.json
     */
    /**
     * Sample code: GetDCAccessCode.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void getDCAccessCode(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.orders()
            .listDCAccessCodeWithResponse("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.RoleProperties;
import com.azure.resourcemanager.databoxedge.models.RoleTypes;

/**
 * Samples for Roles CreateOrUpdate.
 */
public final class RolesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/RolePut.json
     */
    /**
     * Sample code: RolePut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void rolePut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.roles()
            .define("IoTRole1")
            .withExistingDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice")
            .withKind(RoleTypes.IOT)
            .withProperties(new RoleProperties())
            .create();
    }
}
```

### Roles_Delete

```java
/**
 * Samples for Roles Delete.
 */
public final class RolesDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/RoleDelete.json
     */
    /**
     * Sample code: RoleDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void roleDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.roles()
            .delete("GroupForEdgeAutomation", "testedgedevice", "IoTRole1", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_Get

```java
/**
 * Samples for Roles Get.
 */
public final class RolesGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/RoleGet.json
     */
    /**
     * Sample code: RoleGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void roleGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.roles()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "IoTRole1", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_ListByDataBoxEdgeDevice

```java
/**
 * Samples for Roles ListByDataBoxEdgeDevice.
 */
public final class RolesListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/RoleGetAllInDevice.json
     */
    /**
     * Sample code: RoleGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void roleGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.roles()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Shares_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.AzureContainerDataFormat;
import com.azure.resourcemanager.databoxedge.models.AzureContainerInfo;
import com.azure.resourcemanager.databoxedge.models.DataPolicy;
import com.azure.resourcemanager.databoxedge.models.MonitoringStatus;
import com.azure.resourcemanager.databoxedge.models.ShareAccessProtocol;
import com.azure.resourcemanager.databoxedge.models.ShareAccessType;
import com.azure.resourcemanager.databoxedge.models.ShareStatus;
import com.azure.resourcemanager.databoxedge.models.UserAccessRight;
import java.util.Arrays;

/**
 * Samples for Shares CreateOrUpdate.
 */
public final class SharesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/SharePut.json
     */
    /**
     * Sample code: SharePut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sharePut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.shares()
            .define("smbshare")
            .withExistingDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice")
            .withShareStatus(ShareStatus.fromString("Online"))
            .withMonitoringStatus(MonitoringStatus.ENABLED)
            .withAccessProtocol(ShareAccessProtocol.SMB)
            .withDescription("")
            .withAzureContainerInfo(new AzureContainerInfo().withStorageAccountCredentialId("fakeTokenPlaceholder")
                .withContainerName("testContainerSMB")
                .withDataFormat(AzureContainerDataFormat.BLOCK_BLOB))
            .withUserAccessRights(Arrays.asList(new UserAccessRight().withUserId(
                "/subscriptions/4385cf00-2d3a-425a-832f-f4285b1c9dce/resourceGroups/GroupForEdgeAutomation/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/testedgedevice/users/user2")
                .withAccessType(ShareAccessType.CHANGE)))
            .withDataPolicy(DataPolicy.CLOUD)
            .create();
    }
}
```

### Shares_Delete

```java
/**
 * Samples for Shares Delete.
 */
public final class SharesDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/ShareDelete.json
     */
    /**
     * Sample code: ShareDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void shareDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.shares()
            .delete("GroupForEdgeAutomation", "testedgedevice", "smbshare", com.azure.core.util.Context.NONE);
    }
}
```

### Shares_Get

```java
/**
 * Samples for Shares Get.
 */
public final class SharesGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/ShareGet.json
     */
    /**
     * Sample code: ShareGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void shareGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.shares()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "smbshare", com.azure.core.util.Context.NONE);
    }
}
```

### Shares_ListByDataBoxEdgeDevice

```java
/**
 * Samples for Shares ListByDataBoxEdgeDevice.
 */
public final class SharesListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/ShareGetAllInDevice.json
     */
    /**
     * Sample code: ShareGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void shareGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.shares()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Shares_Refresh

```java
/**
 * Samples for Shares Refresh.
 */
public final class SharesRefreshSamples {
    /*
     * x-ms-original-file: 2023-12-01/ShareRefreshPost.json
     */
    /**
     * Sample code: ShareRefreshPost.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void shareRefreshPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.shares()
            .refresh("GroupForEdgeAutomation", "testedgedevice", "smbshare", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccountCredentials_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.AccountType;
import com.azure.resourcemanager.databoxedge.models.AsymmetricEncryptedSecret;
import com.azure.resourcemanager.databoxedge.models.EncryptionAlgorithm;
import com.azure.resourcemanager.databoxedge.models.SSLStatus;

/**
 * Samples for StorageAccountCredentials CreateOrUpdate.
 */
public final class StorageAccountCredentialsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/SACPut.json
     */
    /**
     * Sample code: SACPut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sACPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.storageAccountCredentials()
            .define("sac1")
            .withExistingDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice")
            .withAlias("sac1")
            .withSslStatus(SSLStatus.DISABLED)
            .withAccountType(AccountType.BLOB_STORAGE)
            .withUserName("cisbvt")
            .withAccountKey(new AsymmetricEncryptedSecret().withValue(
                "lAeZEYi6rNP1/EyNaVUYmTSZEYyaIaWmwUsGwek0+xiZj54GM9Ue9/UA2ed/ClC03wuSit2XzM/cLRU5eYiFBwks23rGwiQOr3sruEL2a74EjPD050xYjA6M1I2hu/w2yjVHhn5j+DbXS4Xzi+rHHNZK3DgfDO3PkbECjPck+PbpSBjy9+6Mrjcld5DIZhUAeMlMHrFlg+WKRKB14o/og56u5/xX6WKlrMLEQ+y6E18dUwvWs2elTNoVO8PBE8SM/CfooX4AMNvaNdSObNBPdP+F6Lzc556nFNWXrBLRt0vC7s9qTiVRO4x/qCNaK/B4y7IqXMllwQFf4Np9UQ2ECA==")
                .withEncryptionCertThumbprint("2A9D8D6BE51574B5461230AEF02F162C5F01AD31")
                .withEncryptionAlgorithm(EncryptionAlgorithm.AES256))
            .create();
    }
}
```

### StorageAccountCredentials_Delete

```java
/**
 * Samples for StorageAccountCredentials Delete.
 */
public final class StorageAccountCredentialsDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/SACDelete.json
     */
    /**
     * Sample code: SACDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sACDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.storageAccountCredentials()
            .delete("GroupForEdgeAutomation", "testedgedevice", "sac1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccountCredentials_Get

```java
/**
 * Samples for StorageAccountCredentials Get.
 */
public final class StorageAccountCredentialsGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/SACGet.json
     */
    /**
     * Sample code: SACGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sACGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.storageAccountCredentials()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "sac1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccountCredentials_ListByDataBoxEdgeDevice

```java
/**
 * Samples for StorageAccountCredentials ListByDataBoxEdgeDevice.
 */
public final class StorageAccountCredentialsListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/SACGetAllInDevice.json
     */
    /**
     * Sample code: SACGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sACGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.storageAccountCredentials()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccounts_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.DataPolicy;
import com.azure.resourcemanager.databoxedge.models.StorageAccountStatus;

/**
 * Samples for StorageAccounts CreateOrUpdate.
 */
public final class StorageAccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/StorageAccountPut.json
     */
    /**
     * Sample code: StorageAccountPut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void storageAccountPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.storageAccounts()
            .define("blobstorageaccount1")
            .withExistingDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice")
            .withDataPolicy(DataPolicy.CLOUD)
            .withDescription("It's an awesome storage account")
            .withStorageAccountStatus(StorageAccountStatus.OK)
            .withStorageAccountCredentialId(
                "/subscriptions/4385cf00-2d3a-425a-832f-f4285b1c9dce/resourceGroups/GroupForDataBoxEdgeAutomation/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/testedgedevice/storageAccountCredentials/cisbvt")
            .create();
    }
}
```

### StorageAccounts_Delete

```java
/**
 * Samples for StorageAccounts Delete.
 */
public final class StorageAccountsDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/StorageAccountDelete.json
     */
    /**
     * Sample code: StorageAccountDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void storageAccountDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.storageAccounts()
            .delete("GroupForEdgeAutomation", "testedgedevice", "storageaccount1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccounts_Get

```java
/**
 * Samples for StorageAccounts Get.
 */
public final class StorageAccountsGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/StorageAccountGet.json
     */
    /**
     * Sample code: StorageAccountGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void storageAccountGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.storageAccounts()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "blobstorageaccount1",
                com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccounts_ListByDataBoxEdgeDevice

```java
/**
 * Samples for StorageAccounts ListByDataBoxEdgeDevice.
 */
public final class StorageAccountsListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/StorageAccountGetAllInDevice.json
     */
    /**
     * Sample code: StorageAccountGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void storageAccountGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.storageAccounts()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### SupportPackages_TriggerSupportPackage

```java
import com.azure.resourcemanager.databoxedge.models.TriggerSupportPackageRequest;
import java.time.OffsetDateTime;

/**
 * Samples for SupportPackages TriggerSupportPackage.
 */
public final class SupportPackagesTriggerSupportPackageSamples {
    /*
     * x-ms-original-file: 2023-12-01/TriggerSupportPackage.json
     */
    /**
     * Sample code: TriggerSupportPackage.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void triggerSupportPackage(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.supportPackages()
            .triggerSupportPackage("GroupForEdgeAutomation", "testedgedevice",
                new TriggerSupportPackageRequest()
                    .withMinimumTimeStamp(OffsetDateTime.parse("2018-12-18T02:18:51.4270267Z"))
                    .withMaximumTimeStamp(OffsetDateTime.parse("2018-12-18T02:19:51.4270267Z"))
                    .withInclude("DefaultWithDumps"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.TriggerEventType;
import com.azure.resourcemanager.databoxedge.models.TriggerProperties;

/**
 * Samples for Triggers CreateOrUpdate.
 */
public final class TriggersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/TriggerPut.json
     */
    /**
     * Sample code: TriggerPut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void triggerPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.triggers()
            .define("trigger1")
            .withExistingDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice")
            .withKind(TriggerEventType.FILE_EVENT)
            .withProperties(new TriggerProperties())
            .create();
    }
}
```

### Triggers_Delete

```java
/**
 * Samples for Triggers Delete.
 */
public final class TriggersDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/TriggerDelete.json
     */
    /**
     * Sample code: TriggerDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void triggerDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.triggers()
            .delete("GroupForEdgeAutomation", "testedgedevice", "trigger1", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_Get

```java
/**
 * Samples for Triggers Get.
 */
public final class TriggersGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/TriggerGet.json
     */
    /**
     * Sample code: TriggerGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void triggerGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.triggers()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "trigger1", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_ListByDataBoxEdgeDevice

```java
/**
 * Samples for Triggers ListByDataBoxEdgeDevice.
 */
public final class TriggersListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/TriggerGetAllInDevice.json
     */
    /**
     * Sample code: TriggerGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void triggerGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.triggers()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Users_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.AsymmetricEncryptedSecret;
import com.azure.resourcemanager.databoxedge.models.EncryptionAlgorithm;
import com.azure.resourcemanager.databoxedge.models.UserType;

/**
 * Samples for Users CreateOrUpdate.
 */
public final class UsersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-12-01/UserPut.json
     */
    /**
     * Sample code: UserPut.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void userPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.users()
            .define("user1")
            .withExistingDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice")
            .withUserType(UserType.SHARE)
            .withEncryptedPassword(new AsymmetricEncryptedSecret().withValue("<value>")
                .withEncryptionCertThumbprint("blah")
                .withEncryptionAlgorithm(EncryptionAlgorithm.NONE))
            .create();
    }
}
```

### Users_Delete

```java
/**
 * Samples for Users Delete.
 */
public final class UsersDeleteSamples {
    /*
     * x-ms-original-file: 2023-12-01/UserDelete.json
     */
    /**
     * Sample code: UserDelete.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void userDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.users().delete("GroupForEdgeAutomation", "testedgedevice", "user1", com.azure.core.util.Context.NONE);
    }
}
```

### Users_Get

```java
/**
 * Samples for Users Get.
 */
public final class UsersGetSamples {
    /*
     * x-ms-original-file: 2023-12-01/UserGet.json
     */
    /**
     * Sample code: UserGet.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void userGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.users()
            .getWithResponse("GroupForEdgeAutomation", "testedgedevice", "user1", com.azure.core.util.Context.NONE);
    }
}
```

### Users_ListByDataBoxEdgeDevice

```java
/**
 * Samples for Users ListByDataBoxEdgeDevice.
 */
public final class UsersListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2023-12-01/UserGetAllInDevice.json
     */
    /**
     * Sample code: UserGetAllInDevice.
     * 
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void userGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.users()
            .listByDataBoxEdgeDevice("GroupForEdgeAutomation", "testedgedevice", null,
                com.azure.core.util.Context.NONE);
    }
}
```

