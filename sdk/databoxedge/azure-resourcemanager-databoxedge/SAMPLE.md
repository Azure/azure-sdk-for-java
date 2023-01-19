# Code snippets and samples


## Alerts

- [Get](#alerts_get)
- [ListByDataBoxEdgeDevice](#alerts_listbydataboxedgedevice)

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

## Devices

- [CreateOrUpdate](#devices_createorupdate)
- [CreateOrUpdateSecuritySettings](#devices_createorupdatesecuritysettings)
- [Delete](#devices_delete)
- [DownloadUpdates](#devices_downloadupdates)
- [GetByResourceGroup](#devices_getbyresourcegroup)
- [GetExtendedInformation](#devices_getextendedinformation)
- [GetNetworkSettings](#devices_getnetworksettings)
- [GetUpdateSummary](#devices_getupdatesummary)
- [InstallUpdates](#devices_installupdates)
- [List](#devices_list)
- [ListByResourceGroup](#devices_listbyresourcegroup)
- [ScanForUpdates](#devices_scanforupdates)
- [Update](#devices_update)
- [UploadCertificate](#devices_uploadcertificate)

## Jobs

- [Get](#jobs_get)

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

## Skus

- [List](#skus_list)

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
### Alerts_Get

```java
/** Samples for Alerts Get. */
public final class AlertsGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/AlertGet.json
     */
    /**
     * Sample code: AlertGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void alertGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .alerts()
            .getWithResponse(
                "testedgedevice",
                "159a00c7-8543-4343-9435-263ac87df3bb",
                "GroupForEdgeAutomation",
                com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_ListByDataBoxEdgeDevice

```java
/** Samples for Alerts ListByDataBoxEdgeDevice. */
public final class AlertsListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/AlertGetAllInDevice.json
     */
    /**
     * Sample code: AlertGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void alertGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .alerts()
            .listByDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### BandwidthSchedules_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.DayOfWeek;
import java.util.Arrays;

/** Samples for BandwidthSchedules CreateOrUpdate. */
public final class BandwidthSchedulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/BandwidthSchedulePut.json
     */
    /**
     * Sample code: BandwidthSchedulePut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void bandwidthSchedulePut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .bandwidthSchedules()
            .define("bandwidth-1")
            .withExistingDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation")
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
/** Samples for BandwidthSchedules Delete. */
public final class BandwidthSchedulesDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/BandwidthScheduleDelete.json
     */
    /**
     * Sample code: BandwidthScheduleDelete.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void bandwidthScheduleDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .bandwidthSchedules()
            .delete("testedgedevice", "bandwidth-1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### BandwidthSchedules_Get

```java
/** Samples for BandwidthSchedules Get. */
public final class BandwidthSchedulesGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/BandwidthScheduleGet.json
     */
    /**
     * Sample code: BandwidthScheduleGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void bandwidthScheduleGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .bandwidthSchedules()
            .getWithResponse(
                "testedgedevice", "bandwidth-1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### BandwidthSchedules_ListByDataBoxEdgeDevice

```java
/** Samples for BandwidthSchedules ListByDataBoxEdgeDevice. */
public final class BandwidthSchedulesListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/BandwidthScheduleGetAllInDevice.json
     */
    /**
     * Sample code: BandwidthScheduleGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void bandwidthScheduleGetAllInDevice(
        com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .bandwidthSchedules()
            .listByDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Containers_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.AzureContainerDataFormat;

/** Samples for Containers CreateOrUpdate. */
public final class ContainersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ContainerPut.json
     */
    /**
     * Sample code: ContainerPut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .containers()
            .define("blobcontainer1")
            .withExistingStorageAccount("testedgedevice", "storageaccount1", "GroupForEdgeAutomation")
            .withDataFormat(AzureContainerDataFormat.BLOCK_BLOB)
            .create();
    }
}
```

### Containers_Delete

```java
/** Samples for Containers Delete. */
public final class ContainersDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ContainerDelete.json
     */
    /**
     * Sample code: ContainerDelete.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .containers()
            .delete(
                "testedgedevice",
                "storageaccount1",
                "blobcontainer1",
                "GroupForEdgeAutomation",
                com.azure.core.util.Context.NONE);
    }
}
```

### Containers_Get

```java
/** Samples for Containers Get. */
public final class ContainersGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ContainerGet.json
     */
    /**
     * Sample code: ContainerGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .containers()
            .getWithResponse(
                "testedgedevice",
                "storageaccount1",
                "blobcontainer1",
                "GroupForEdgeAutomation",
                com.azure.core.util.Context.NONE);
    }
}
```

### Containers_ListByStorageAccount

```java
/** Samples for Containers ListByStorageAccount. */
public final class ContainersListByStorageAccountSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ContainerListAllInDevice.json
     */
    /**
     * Sample code: ContainerListAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerListAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .containers()
            .listByStorageAccount(
                "testedgedevice", "storageaccount1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Containers_Refresh

```java
/** Samples for Containers Refresh. */
public final class ContainersRefreshSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ContainerRefresh.json
     */
    /**
     * Sample code: ContainerRefresh.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void containerRefresh(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .containers()
            .refresh(
                "testedgedevice",
                "storageaccount1",
                "blobcontainer1",
                "GroupForEdgeAutomation",
                com.azure.core.util.Context.NONE);
    }
}
```

### Devices_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.Sku;
import com.azure.resourcemanager.databoxedge.models.SkuName;
import com.azure.resourcemanager.databoxedge.models.SkuTier;
import java.util.HashMap;
import java.util.Map;

/** Samples for Devices CreateOrUpdate. */
public final class DevicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/DataBoxEdgeDevicePut.json
     */
    /**
     * Sample code: DataBoxEdgeDevicePut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void dataBoxEdgeDevicePut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .devices()
            .define("testedgedevice")
            .withRegion("eastus")
            .withExistingResourceGroup("GroupForEdgeAutomation")
            .withTags(mapOf())
            .withSku(new Sku().withName(SkuName.EDGE).withTier(SkuTier.STANDARD))
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

### Devices_CreateOrUpdateSecuritySettings

```java
import com.azure.resourcemanager.databoxedge.models.AsymmetricEncryptedSecret;
import com.azure.resourcemanager.databoxedge.models.EncryptionAlgorithm;
import com.azure.resourcemanager.databoxedge.models.SecuritySettings;

/** Samples for Devices CreateOrUpdateSecuritySettings. */
public final class DevicesCreateOrUpdateSecuritySettingsSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/SecuritySettingsUpdatePost.json
     */
    /**
     * Sample code: CreateOrUpdateSecuritySettings.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void createOrUpdateSecuritySettings(
        com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .devices()
            .createOrUpdateSecuritySettings(
                "testedgedevice",
                "AzureVM",
                new SecuritySettings()
                    .withDeviceAdminPassword(
                        new AsymmetricEncryptedSecret()
                            .withValue("<value>")
                            .withEncryptionCertThumbprint("7DCBDFC44ED968D232C9A998FC105B5C70E84BE0")
                            .withEncryptionAlgorithm(EncryptionAlgorithm.AES256)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Devices_Delete

```java
/** Samples for Devices Delete. */
public final class DevicesDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/DataBoxEdgeDeviceDelete.json
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
/** Samples for Devices DownloadUpdates. */
public final class DevicesDownloadUpdatesSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/DownloadUpdatesPost.json
     */
    /**
     * Sample code: DownloadUpdatesPost.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void downloadUpdatesPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().downloadUpdates("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_GetByResourceGroup

```java
/** Samples for Devices GetByResourceGroup. */
public final class DevicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/DataBoxEdgeDeviceGetByName.json
     */
    /**
     * Sample code: DataBoxEdgeDeviceGetByName.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void dataBoxEdgeDeviceGetByName(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .devices()
            .getByResourceGroupWithResponse(
                "GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_GetExtendedInformation

```java
/** Samples for Devices GetExtendedInformation. */
public final class DevicesGetExtendedInformationSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ExtendedInfoPost.json
     */
    /**
     * Sample code: ExtendedInfoPost.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void extendedInfoPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .devices()
            .getExtendedInformationWithResponse(
                "testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_GetNetworkSettings

```java
/** Samples for Devices GetNetworkSettings. */
public final class DevicesGetNetworkSettingsSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/NetworkSettingsGet.json
     */
    /**
     * Sample code: NetworkSettingsGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void networkSettingsGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .devices()
            .getNetworkSettingsWithResponse(
                "testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_GetUpdateSummary

```java
/** Samples for Devices GetUpdateSummary. */
public final class DevicesGetUpdateSummarySamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/UpdateSummaryGet.json
     */
    /**
     * Sample code: UpdateSummaryGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void updateSummaryGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .devices()
            .getUpdateSummaryWithResponse("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_InstallUpdates

```java
/** Samples for Devices InstallUpdates. */
public final class DevicesInstallUpdatesSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/InstallUpdatesPost.json
     */
    /**
     * Sample code: InstallUpdatesPost.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void installUpdatesPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().installUpdates("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_List

```java
/** Samples for Devices List. */
public final class DevicesListSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/DataBoxEdgeDeviceGetBySubscription.json
     */
    /**
     * Sample code: DataBoxEdgeDeviceGetBySubscription.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void dataBoxEdgeDeviceGetBySubscription(
        com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### Devices_ListByResourceGroup

```java
/** Samples for Devices ListByResourceGroup. */
public final class DevicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/DataBoxEdgeDeviceGetByResourceGroup.json
     */
    /**
     * Sample code: DataBoxEdgeDeviceGetByResourceGroup.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void dataBoxEdgeDeviceGetByResourceGroup(
        com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().listByResourceGroup("GroupForEdgeAutomation", null, com.azure.core.util.Context.NONE);
    }
}
```

### Devices_ScanForUpdates

```java
/** Samples for Devices ScanForUpdates. */
public final class DevicesScanForUpdatesSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ScanForUpdatesPost.json
     */
    /**
     * Sample code: ScanForUpdatesPost.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void scanForUpdatesPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.devices().scanForUpdates("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Devices_Update

```java
import com.azure.resourcemanager.databoxedge.models.DataBoxEdgeDevice;
import java.util.HashMap;
import java.util.Map;

/** Samples for Devices Update. */
public final class DevicesUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/DataBoxEdgeDevicePatch.json
     */
    /**
     * Sample code: DataBoxEdgeDevicePatch.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void dataBoxEdgeDevicePatch(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        DataBoxEdgeDevice resource =
            manager
                .devices()
                .getByResourceGroupWithResponse(
                    "GroupForEdgeAutomation", "testedgedevice", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("Key1", "value1", "Key2", "value2")).apply();
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

### Devices_UploadCertificate

```java
import com.azure.resourcemanager.databoxedge.models.UploadCertificateRequest;

/** Samples for Devices UploadCertificate. */
public final class DevicesUploadCertificateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/UploadCertificatePost.json
     */
    /**
     * Sample code: UploadCertificatePost.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void uploadCertificatePost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .devices()
            .uploadCertificateWithResponse(
                "testedgedevice",
                "GroupForEdgeAutomation",
                new UploadCertificateRequest()
                    .withCertificate(
                        "MIIC9DCCAdygAwIBAgIQWJae7GNjiI9Mcv/gJyrOPTANBgkqhkiG9w0BAQUFADASMRAwDgYDVQQDDAdXaW5kb3dzMB4XDTE4MTEyNzAwMTA0NVoXDTIxMTEyODAwMTA0NVowEjEQMA4GA1UEAwwHV2luZG93czCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKxkRExqxf0qH1avnyORptIbRC2yQwqe3EIbJ2FPKr5jtAppGeX/dGKrFSnX+7/0HFr77aJHafdpEAtOiLyJ4zCAVs0obZCCIq4qJdmjYUTU0UXH/w/YzXfQA0d9Zh9AN+NJBX9xj05NzgsT24fkgsK2v6mWJQXT7YcWAsl5sEYPnx1e+MrupNyVSL/RUJmrS+etJSysHtFeWRhsUhVAs1DD5ExJvBLU3WH0IsojEvpXcjrutB5/MDQNrd/StGI6WovoSSPH7FyT9tgERx+q+Yg3YUGzfaIPCctlrRGehcdtzdNoKd0rsX62yCq0U6POoSfwe22NJu41oAUMd7e6R8cCAwEAAaNGMEQwEwYDVR0lBAwwCgYIKwYBBQUHAwIwHQYDVR0OBBYEFDd0VxnS3LnMIfwc7xW4b4IZWG5GMA4GA1UdDwEB/wQEAwIFIDANBgkqhkiG9w0BAQUFAAOCAQEAPQRby2u9celvtvL/DLEb5Vt3/tPStRQC5MyTD62L5RT/q8E6EMCXVZNkXF5WlWucLJi/18tY+9PNgP9xWLJh7kpSWlWdi9KPtwMqKDlEH8L2TnQdjimt9XuiCrTnoFy/1X2BGLY/rCaUJNSd15QCkz2xeW+Z+YSk2GwAc/A/4YfNpqSIMfNuPrT76o02VdD9WmJUA3fS/HY0sU9qgQRS/3F5/0EPS+HYQ0SvXCK9tggcCd4O050ytNBMJC9qMOJ7yE0iOrFfOJSCfDAuPhn/rHFh79Kn1moF+/CE+nc0/2RPiLC8r54/rt5dYyyxJDfXg0a3VrrX39W69WZGW5OXiw=="),
                com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_Get

```java
/** Samples for Jobs Get. */
public final class JobsGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/JobsGet.json
     */
    /**
     * Sample code: JobsGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void jobsGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .jobs()
            .getWithResponse(
                "testedgedevice",
                "159a00c7-8543-4343-9435-263ac87df3bb",
                "GroupForEdgeAutomation",
                com.azure.core.util.Context.NONE);
    }
}
```

### Nodes_ListByDataBoxEdgeDevice

```java
/** Samples for Nodes ListByDataBoxEdgeDevice. */
public final class NodesListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/NodeGetAllInDevice.json
     */
    /**
     * Sample code: NodesGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void nodesGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .nodes()
            .listByDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/OperationsGet.json
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
/** Samples for OperationsStatus Get. */
public final class OperationsStatusGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/OperationsStatusGet.json
     */
    /**
     * Sample code: OperationsStatusGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void operationsStatusGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .operationsStatus()
            .getWithResponse(
                "testedgedevice",
                "159a00c7-8543-4343-9435-263ac87df3bb",
                "GroupForEdgeAutomation",
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

/** Samples for Orders CreateOrUpdate. */
public final class OrdersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/OrderPut.json
     */
    /**
     * Sample code: OrderPut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void orderPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .orders()
            .createOrUpdate(
                "testedgedevice",
                "GroupForEdgeAutomation",
                new OrderInner()
                    .withContactInformation(
                        new ContactDetails()
                            .withContactPerson("John Mcclane")
                            .withCompanyName("Microsoft")
                            .withPhone("(800) 426-9400")
                            .withEmailList(Arrays.asList("john@microsoft.com")))
                    .withShippingAddress(
                        new Address()
                            .withAddressLine1("Microsoft Corporation")
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
/** Samples for Orders Delete. */
public final class OrdersDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/OrderDelete.json
     */
    /**
     * Sample code: OrderDelete.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void orderDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.orders().delete("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Orders_Get

```java
/** Samples for Orders Get. */
public final class OrdersGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/OrderGet.json
     */
    /**
     * Sample code: OrderGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void orderGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.orders().getWithResponse("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Orders_ListByDataBoxEdgeDevice

```java
/** Samples for Orders ListByDataBoxEdgeDevice. */
public final class OrdersListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/OrderGetAllInDevice.json
     */
    /**
     * Sample code: OrderGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void orderGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .orders()
            .listByDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.AsymmetricEncryptedSecret;
import com.azure.resourcemanager.databoxedge.models.Authentication;
import com.azure.resourcemanager.databoxedge.models.EncryptionAlgorithm;
import com.azure.resourcemanager.databoxedge.models.IoTDeviceInfo;
import com.azure.resourcemanager.databoxedge.models.IoTRole;
import com.azure.resourcemanager.databoxedge.models.PlatformType;
import com.azure.resourcemanager.databoxedge.models.RoleStatus;
import com.azure.resourcemanager.databoxedge.models.SymmetricKey;
import java.util.Arrays;

/** Samples for Roles CreateOrUpdate. */
public final class RolesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/RolePut.json
     */
    /**
     * Sample code: RolePut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void rolePut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .roles()
            .createOrUpdate(
                "testedgedevice",
                "IoTRole1",
                "GroupForEdgeAutomation",
                new IoTRole()
                    .withHostPlatform(PlatformType.LINUX)
                    .withIoTDeviceDetails(
                        new IoTDeviceInfo()
                            .withDeviceId("iotdevice")
                            .withIoTHostHub("iothub.azure-devices.net")
                            .withAuthentication(
                                new Authentication()
                                    .withSymmetricKey(
                                        new SymmetricKey()
                                            .withConnectionString(
                                                new AsymmetricEncryptedSecret()
                                                    .withValue(
                                                        "Encrypted<<HostName=iothub.azure-devices.net;DeviceId=iotDevice;SharedAccessKey=2C750FscEas3JmQ8Bnui5yQWZPyml0/UiRt1bQwd8=>>")
                                                    .withEncryptionCertThumbprint("348586569999244")
                                                    .withEncryptionAlgorithm(EncryptionAlgorithm.AES256)))))
                    .withIoTEdgeDeviceDetails(
                        new IoTDeviceInfo()
                            .withDeviceId("iotEdge")
                            .withIoTHostHub("iothub.azure-devices.net")
                            .withAuthentication(
                                new Authentication()
                                    .withSymmetricKey(
                                        new SymmetricKey()
                                            .withConnectionString(
                                                new AsymmetricEncryptedSecret()
                                                    .withValue(
                                                        "Encrypted<<HostName=iothub.azure-devices.net;DeviceId=iotEdge;SharedAccessKey=2C750FscEas3JmQ8Bnui5yQWZPyml0/UiRt1bQwd8=>>")
                                                    .withEncryptionCertThumbprint("1245475856069999244")
                                                    .withEncryptionAlgorithm(EncryptionAlgorithm.AES256)))))
                    .withShareMappings(Arrays.asList())
                    .withRoleStatus(RoleStatus.ENABLED),
                com.azure.core.util.Context.NONE);
    }
}
```

### Roles_Delete

```java
/** Samples for Roles Delete. */
public final class RolesDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/RoleDelete.json
     */
    /**
     * Sample code: RoleDelete.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void roleDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .roles()
            .delete("testedgedevice", "IoTRole1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_Get

```java
/** Samples for Roles Get. */
public final class RolesGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/RoleGet.json
     */
    /**
     * Sample code: RoleGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void roleGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .roles()
            .getWithResponse("testedgedevice", "IoTRole1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_ListByDataBoxEdgeDevice

```java
/** Samples for Roles ListByDataBoxEdgeDevice. */
public final class RolesListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/RoleGetAllInDevice.json
     */
    /**
     * Sample code: RoleGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void roleGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .roles()
            .listByDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
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

/** Samples for Shares CreateOrUpdate. */
public final class SharesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/SharePut.json
     */
    /**
     * Sample code: SharePut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sharePut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .shares()
            .define("smbshare")
            .withExistingDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation")
            .withShareStatus(ShareStatus.fromString("Online"))
            .withMonitoringStatus(MonitoringStatus.ENABLED)
            .withAccessProtocol(ShareAccessProtocol.SMB)
            .withDescription("")
            .withAzureContainerInfo(
                new AzureContainerInfo()
                    .withStorageAccountCredentialId("fakeTokenPlaceholder")
                    .withContainerName("testContainerSMB")
                    .withDataFormat(AzureContainerDataFormat.BLOCK_BLOB))
            .withUserAccessRights(
                Arrays
                    .asList(
                        new UserAccessRight()
                            .withUserId(
                                "/subscriptions/4385cf00-2d3a-425a-832f-f4285b1c9dce/resourceGroups/GroupForEdgeAutomation/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/testedgedevice/users/user2")
                            .withAccessType(ShareAccessType.CHANGE)))
            .withDataPolicy(DataPolicy.CLOUD)
            .create();
    }
}
```

### Shares_Delete

```java
/** Samples for Shares Delete. */
public final class SharesDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ShareDelete.json
     */
    /**
     * Sample code: ShareDelete.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void shareDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .shares()
            .delete("testedgedevice", "smbshare", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Shares_Get

```java
/** Samples for Shares Get. */
public final class SharesGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ShareGet.json
     */
    /**
     * Sample code: ShareGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void shareGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .shares()
            .getWithResponse("testedgedevice", "smbshare", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Shares_ListByDataBoxEdgeDevice

```java
/** Samples for Shares ListByDataBoxEdgeDevice. */
public final class SharesListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ShareGetAllInDevice.json
     */
    /**
     * Sample code: ShareGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void shareGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .shares()
            .listByDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Shares_Refresh

```java
/** Samples for Shares Refresh. */
public final class SharesRefreshSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ShareRefreshPost.json
     */
    /**
     * Sample code: ShareRefreshPost.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void shareRefreshPost(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .shares()
            .refresh("testedgedevice", "smbshare", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_List

```java
/** Samples for Skus List. */
public final class SkusListSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/ListSkus.json
     */
    /**
     * Sample code: ListSkus.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void listSkus(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.skus().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccountCredentials_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.AccountType;
import com.azure.resourcemanager.databoxedge.models.AsymmetricEncryptedSecret;
import com.azure.resourcemanager.databoxedge.models.EncryptionAlgorithm;
import com.azure.resourcemanager.databoxedge.models.SslStatus;

/** Samples for StorageAccountCredentials CreateOrUpdate. */
public final class StorageAccountCredentialsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/SACPut.json
     */
    /**
     * Sample code: SACPut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sACPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .storageAccountCredentials()
            .define("sac1")
            .withExistingDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation")
            .withAlias("sac1")
            .withSslStatus(SslStatus.DISABLED)
            .withAccountType(AccountType.BLOB_STORAGE)
            .withUsername("cisbvt")
            .withAccountKey(
                new AsymmetricEncryptedSecret()
                    .withValue(
                        "lAeZEYi6rNP1/EyNaVUYmTSZEYyaIaWmwUsGwek0+xiZj54GM9Ue9/UA2ed/ClC03wuSit2XzM/cLRU5eYiFBwks23rGwiQOr3sruEL2a74EjPD050xYjA6M1I2hu/w2yjVHhn5j+DbXS4Xzi+rHHNZK3DgfDO3PkbECjPck+PbpSBjy9+6Mrjcld5DIZhUAeMlMHrFlg+WKRKB14o/og56u5/xX6WKlrMLEQ+y6E18dUwvWs2elTNoVO8PBE8SM/CfooX4AMNvaNdSObNBPdP+F6Lzc556nFNWXrBLRt0vC7s9qTiVRO4x/qCNaK/B4y7IqXMllwQFf4Np9UQ2ECA==")
                    .withEncryptionCertThumbprint("2A9D8D6BE51574B5461230AEF02F162C5F01AD31")
                    .withEncryptionAlgorithm(EncryptionAlgorithm.AES256))
            .create();
    }
}
```

### StorageAccountCredentials_Delete

```java
/** Samples for StorageAccountCredentials Delete. */
public final class StorageAccountCredentialsDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/SACDelete.json
     */
    /**
     * Sample code: SACDelete.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sACDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .storageAccountCredentials()
            .delete("testedgedevice", "sac1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccountCredentials_Get

```java
/** Samples for StorageAccountCredentials Get. */
public final class StorageAccountCredentialsGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/SACGet.json
     */
    /**
     * Sample code: SACGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sACGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .storageAccountCredentials()
            .getWithResponse("testedgedevice", "sac1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccountCredentials_ListByDataBoxEdgeDevice

```java
/** Samples for StorageAccountCredentials ListByDataBoxEdgeDevice. */
public final class StorageAccountCredentialsListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/SACGetAllInDevice.json
     */
    /**
     * Sample code: SACGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void sACGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .storageAccountCredentials()
            .listByDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccounts_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.DataPolicy;
import com.azure.resourcemanager.databoxedge.models.StorageAccountStatus;

/** Samples for StorageAccounts CreateOrUpdate. */
public final class StorageAccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/StorageAccountPut.json
     */
    /**
     * Sample code: StorageAccountPut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void storageAccountPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .storageAccounts()
            .define("blobstorageaccount1")
            .withExistingDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation")
            .withDescription("It's an awesome storage account")
            .withStorageAccountStatus(StorageAccountStatus.OK)
            .withDataPolicy(DataPolicy.CLOUD)
            .withStorageAccountCredentialId(
                "/subscriptions/4385cf00-2d3a-425a-832f-f4285b1c9dce/resourceGroups/GroupForDataBoxEdgeAutomation/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/testedgedevice/storageAccountCredentials/cisbvt")
            .create();
    }
}
```

### StorageAccounts_Delete

```java
/** Samples for StorageAccounts Delete. */
public final class StorageAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/StorageAccountDelete.json
     */
    /**
     * Sample code: StorageAccountDelete.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void storageAccountDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .storageAccounts()
            .delete("testedgedevice", "storageaccount1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccounts_Get

```java
/** Samples for StorageAccounts Get. */
public final class StorageAccountsGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/StorageAccountGet.json
     */
    /**
     * Sample code: StorageAccountGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void storageAccountGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .storageAccounts()
            .getWithResponse(
                "testedgedevice", "blobstorageaccount1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAccounts_ListByDataBoxEdgeDevice

```java
/** Samples for StorageAccounts ListByDataBoxEdgeDevice. */
public final class StorageAccountsListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/StorageAccountGetAllInDevice.json
     */
    /**
     * Sample code: StorageAccountGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void storageAccountGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .storageAccounts()
            .listByDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.FileEventTrigger;
import com.azure.resourcemanager.databoxedge.models.FileSourceInfo;
import com.azure.resourcemanager.databoxedge.models.RoleSinkInfo;

/** Samples for Triggers CreateOrUpdate. */
public final class TriggersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/TriggerPut.json
     */
    /**
     * Sample code: TriggerPut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void triggerPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .triggers()
            .createOrUpdate(
                "testedgedevice",
                "trigger1",
                "GroupForEdgeAutomation",
                new FileEventTrigger()
                    .withSourceInfo(
                        new FileSourceInfo()
                            .withShareId(
                                "/subscriptions/4385cf00-2d3a-425a-832f-f4285b1c9dce/resourceGroups/GroupForEdgeAutomation/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/testedgedevice/shares/share1"))
                    .withSinkInfo(
                        new RoleSinkInfo()
                            .withRoleId(
                                "/subscriptions/4385cf00-2d3a-425a-832f-f4285b1c9dce/resourceGroups/GroupForEdgeAutomation/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/testedgedevice/roles/role1"))
                    .withCustomContextTag("CustomContextTags-1235346475"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_Delete

```java
/** Samples for Triggers Delete. */
public final class TriggersDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/TriggerDelete.json
     */
    /**
     * Sample code: TriggerDelete.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void triggerDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .triggers()
            .delete("testedgedevice", "trigger1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_Get

```java
/** Samples for Triggers Get. */
public final class TriggersGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/TriggerGet.json
     */
    /**
     * Sample code: TriggerGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void triggerGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .triggers()
            .getWithResponse("testedgedevice", "trigger1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_ListByDataBoxEdgeDevice

```java
/** Samples for Triggers ListByDataBoxEdgeDevice. */
public final class TriggersListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/TriggerGetAllInDevice.json
     */
    /**
     * Sample code: TriggerGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void triggerGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .triggers()
            .listByDataBoxEdgeDevice(
                "testedgedevice", "GroupForEdgeAutomation", null, com.azure.core.util.Context.NONE);
    }
}
```

### Users_CreateOrUpdate

```java
import com.azure.resourcemanager.databoxedge.models.AsymmetricEncryptedSecret;
import com.azure.resourcemanager.databoxedge.models.EncryptionAlgorithm;
import com.azure.resourcemanager.databoxedge.models.UserType;
import java.util.Arrays;

/** Samples for Users CreateOrUpdate. */
public final class UsersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/UserPut.json
     */
    /**
     * Sample code: UserPut.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void userPut(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .users()
            .define("user1")
            .withExistingDataBoxEdgeDevice("testedgedevice", "GroupForEdgeAutomation")
            .withUserType(UserType.SHARE)
            .withEncryptedPassword(
                new AsymmetricEncryptedSecret()
                    .withValue("<value>")
                    .withEncryptionCertThumbprint("blah")
                    .withEncryptionAlgorithm(EncryptionAlgorithm.NONE))
            .withShareAccessRights(Arrays.asList())
            .create();
    }
}
```

### Users_Delete

```java
/** Samples for Users Delete. */
public final class UsersDeleteSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/UserDelete.json
     */
    /**
     * Sample code: UserDelete.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void userDelete(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager.users().delete("testedgedevice", "user1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Users_Get

```java
/** Samples for Users Get. */
public final class UsersGetSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/UserGet.json
     */
    /**
     * Sample code: UserGet.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void userGet(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .users()
            .getWithResponse("testedgedevice", "user1", "GroupForEdgeAutomation", com.azure.core.util.Context.NONE);
    }
}
```

### Users_ListByDataBoxEdgeDevice

```java
/** Samples for Users ListByDataBoxEdgeDevice. */
public final class UsersListByDataBoxEdgeDeviceSamples {
    /*
     * x-ms-original-file: specification/databoxedge/resource-manager/Microsoft.DataBoxEdge/stable/2019-08-01/examples/UserGetAllInDevice.json
     */
    /**
     * Sample code: UserGetAllInDevice.
     *
     * @param manager Entry point to DataBoxEdgeManager.
     */
    public static void userGetAllInDevice(com.azure.resourcemanager.databoxedge.DataBoxEdgeManager manager) {
        manager
            .users()
            .listByDataBoxEdgeDevice(
                "testedgedevice", "GroupForEdgeAutomation", null, com.azure.core.util.Context.NONE);
    }
}
```

