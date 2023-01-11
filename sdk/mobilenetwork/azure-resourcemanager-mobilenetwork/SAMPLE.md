# Code snippets and samples


## AttachedDataNetworks

- [CreateOrUpdate](#attacheddatanetworks_createorupdate)
- [Delete](#attacheddatanetworks_delete)
- [Get](#attacheddatanetworks_get)
- [ListByPacketCoreDataPlane](#attacheddatanetworks_listbypacketcoredataplane)
- [UpdateTags](#attacheddatanetworks_updatetags)

## DataNetworks

- [CreateOrUpdate](#datanetworks_createorupdate)
- [Delete](#datanetworks_delete)
- [Get](#datanetworks_get)
- [ListByMobileNetwork](#datanetworks_listbymobilenetwork)
- [UpdateTags](#datanetworks_updatetags)

## MobileNetworks

- [CreateOrUpdate](#mobilenetworks_createorupdate)
- [Delete](#mobilenetworks_delete)
- [GetByResourceGroup](#mobilenetworks_getbyresourcegroup)
- [List](#mobilenetworks_list)
- [ListByResourceGroup](#mobilenetworks_listbyresourcegroup)
- [UpdateTags](#mobilenetworks_updatetags)

## Operations

- [List](#operations_list)

## PacketCoreControlPlaneOperation

- [CollectDiagnosticsPackage](#packetcorecontrolplaneoperation_collectdiagnosticspackage)
- [Reinstall](#packetcorecontrolplaneoperation_reinstall)
- [Rollback](#packetcorecontrolplaneoperation_rollback)

## PacketCoreControlPlaneVersions

- [Get](#packetcorecontrolplaneversions_get)
- [List](#packetcorecontrolplaneversions_list)

## PacketCoreControlPlanes

- [CreateOrUpdate](#packetcorecontrolplanes_createorupdate)
- [Delete](#packetcorecontrolplanes_delete)
- [GetByResourceGroup](#packetcorecontrolplanes_getbyresourcegroup)
- [List](#packetcorecontrolplanes_list)
- [ListByResourceGroup](#packetcorecontrolplanes_listbyresourcegroup)
- [UpdateTags](#packetcorecontrolplanes_updatetags)

## PacketCoreDataPlanes

- [CreateOrUpdate](#packetcoredataplanes_createorupdate)
- [Delete](#packetcoredataplanes_delete)
- [Get](#packetcoredataplanes_get)
- [ListByPacketCoreControlPlane](#packetcoredataplanes_listbypacketcorecontrolplane)
- [UpdateTags](#packetcoredataplanes_updatetags)

## Services

- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [Get](#services_get)
- [ListByMobileNetwork](#services_listbymobilenetwork)
- [UpdateTags](#services_updatetags)

## SimGroups

- [CreateOrUpdate](#simgroups_createorupdate)
- [Delete](#simgroups_delete)
- [GetByResourceGroup](#simgroups_getbyresourcegroup)
- [List](#simgroups_list)
- [ListByResourceGroup](#simgroups_listbyresourcegroup)
- [UpdateTags](#simgroups_updatetags)

## SimOperation

- [BulkDelete](#simoperation_bulkdelete)
- [BulkUpload](#simoperation_bulkupload)
- [BulkUploadEncrypted](#simoperation_bulkuploadencrypted)

## SimPolicies

- [CreateOrUpdate](#simpolicies_createorupdate)
- [Delete](#simpolicies_delete)
- [Get](#simpolicies_get)
- [ListByMobileNetwork](#simpolicies_listbymobilenetwork)
- [UpdateTags](#simpolicies_updatetags)

## Sims

- [CreateOrUpdate](#sims_createorupdate)
- [Delete](#sims_delete)
- [Get](#sims_get)
- [ListByGroup](#sims_listbygroup)

## Sites

- [CreateOrUpdate](#sites_createorupdate)
- [Delete](#sites_delete)
- [Get](#sites_get)
- [ListByMobileNetwork](#sites_listbymobilenetwork)
- [UpdateTags](#sites_updatetags)

## Slices

- [CreateOrUpdate](#slices_createorupdate)
- [Delete](#slices_delete)
- [Get](#slices_get)
- [ListByMobileNetwork](#slices_listbymobilenetwork)
- [UpdateTags](#slices_updatetags)
### AttachedDataNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.InterfaceProperties;
import com.azure.resourcemanager.mobilenetwork.models.NaptConfiguration;
import com.azure.resourcemanager.mobilenetwork.models.NaptEnabled;
import com.azure.resourcemanager.mobilenetwork.models.PinholeTimeouts;
import com.azure.resourcemanager.mobilenetwork.models.PortRange;
import com.azure.resourcemanager.mobilenetwork.models.PortReuseHoldTimes;
import java.util.Arrays;

/** Samples for AttachedDataNetworks CreateOrUpdate. */
public final class AttachedDataNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/AttachedDataNetworkCreate.json
     */
    /**
     * Sample code: Create attached data network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createAttachedDataNetwork(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .attachedDataNetworks()
            .define("TestAttachedDataNetwork")
            .withRegion("eastus")
            .withExistingPacketCoreDataPlane("rg1", "TestPacketCoreCP", "TestPacketCoreDP")
            .withUserPlaneDataInterface(new InterfaceProperties().withName("N6"))
            .withDnsAddresses(Arrays.asList("1.1.1.1"))
            .withNaptConfiguration(
                new NaptConfiguration()
                    .withEnabled(NaptEnabled.ENABLED)
                    .withPortRange(new PortRange().withMinPort(1024).withMaxPort(49999))
                    .withPortReuseHoldTime(new PortReuseHoldTimes().withTcp(120).withUdp(60))
                    .withPinholeLimits(65536)
                    .withPinholeTimeouts(new PinholeTimeouts().withTcp(180).withUdp(30).withIcmp(30)))
            .withUserEquipmentAddressPoolPrefix(Arrays.asList("2.2.0.0/16"))
            .withUserEquipmentStaticAddressPoolPrefix(Arrays.asList("2.4.0.0/16"))
            .create();
    }
}
```

### AttachedDataNetworks_Delete

```java
import com.azure.core.util.Context;

/** Samples for AttachedDataNetworks Delete. */
public final class AttachedDataNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/AttachedDataNetworkDelete.json
     */
    /**
     * Sample code: Delete attached data network resource.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteAttachedDataNetworkResource(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .attachedDataNetworks()
            .delete("rg1", "TestPacketCoreCP", "TestPacketCoreDP", "TestAttachedDataNetwork", Context.NONE);
    }
}
```

### AttachedDataNetworks_Get

```java
import com.azure.core.util.Context;

/** Samples for AttachedDataNetworks Get. */
public final class AttachedDataNetworksGetSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/AttachedDataNetworkGet.json
     */
    /**
     * Sample code: Get attached data network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getAttachedDataNetwork(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .attachedDataNetworks()
            .getWithResponse("rg1", "TestPacketCoreCP", "TestPacketCoreDP", "TestAttachedDataNetwork", Context.NONE);
    }
}
```

### AttachedDataNetworks_ListByPacketCoreDataPlane

```java
import com.azure.core.util.Context;

/** Samples for AttachedDataNetworks ListByPacketCoreDataPlane. */
public final class AttachedDataNetworksListByPacketCoreDataPlaneSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/AttachedDataNetworkListByPacketCoreDataPlane.json
     */
    /**
     * Sample code: List attached data networks in a data plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listAttachedDataNetworksInADataPlane(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .attachedDataNetworks()
            .listByPacketCoreDataPlane("rg1", "TestPacketCoreCP", "TestPacketCoreDP", Context.NONE);
    }
}
```

### AttachedDataNetworks_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.AttachedDataNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for AttachedDataNetworks UpdateTags. */
public final class AttachedDataNetworksUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/AttachedDataNetworkUpdateTags.json
     */
    /**
     * Sample code: Update attached data network tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateAttachedDataNetworkTags(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        AttachedDataNetwork resource =
            manager
                .attachedDataNetworks()
                .getWithResponse("rg1", "TestPacketCoreCP", "TestPacketCoreDP", "TestAttachedDataNetwork", Context.NONE)
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

### DataNetworks_CreateOrUpdate

```java
/** Samples for DataNetworks CreateOrUpdate. */
public final class DataNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/DataNetworkCreate.json
     */
    /**
     * Sample code: Create data network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createDataNetwork(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .dataNetworks()
            .define("testDataNetwork")
            .withRegion("eastus")
            .withExistingMobileNetwork("rg1", "testMobileNetwork")
            .withDescription("myFavouriteDataNetwork")
            .create();
    }
}
```

### DataNetworks_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataNetworks Delete. */
public final class DataNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/DataNetworkDelete.json
     */
    /**
     * Sample code: Delete data network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteDataNetwork(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.dataNetworks().delete("rg1", "testMobileNetwork", "testDataNetwork", Context.NONE);
    }
}
```

### DataNetworks_Get

```java
import com.azure.core.util.Context;

/** Samples for DataNetworks Get. */
public final class DataNetworksGetSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/DataNetworkGet.json
     */
    /**
     * Sample code: Get data network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getDataNetwork(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.dataNetworks().getWithResponse("rg1", "testMobileNetwork", "testDataNetwork", Context.NONE);
    }
}
```

### DataNetworks_ListByMobileNetwork

```java
import com.azure.core.util.Context;

/** Samples for DataNetworks ListByMobileNetwork. */
public final class DataNetworksListByMobileNetworkSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/DataNetworkListByMobileNetwork.json
     */
    /**
     * Sample code: List data networks in a mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listDataNetworksInAMobileNetwork(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.dataNetworks().listByMobileNetwork("rg1", "testMobileNetwork", Context.NONE);
    }
}
```

### DataNetworks_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.DataNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for DataNetworks UpdateTags. */
public final class DataNetworksUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/DataNetworkUpdateTags.json
     */
    /**
     * Sample code: Update data network tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateDataNetworkTags(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        DataNetwork resource =
            manager
                .dataNetworks()
                .getWithResponse("rg1", "testMobileNetwork", "testDataNetwork", Context.NONE)
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

### MobileNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.PlmnId;

/** Samples for MobileNetworks CreateOrUpdate. */
public final class MobileNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/MobileNetworkCreate.json
     */
    /**
     * Sample code: Create mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createMobileNetwork(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .mobileNetworks()
            .define("testMobileNetwork")
            .withRegion("eastus")
            .withExistingResourceGroup("rg1")
            .withPublicLandMobileNetworkIdentifier(new PlmnId().withMcc("001").withMnc("01"))
            .create();
    }
}
```

### MobileNetworks_Delete

```java
import com.azure.core.util.Context;

/** Samples for MobileNetworks Delete. */
public final class MobileNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/MobileNetworkDelete.json
     */
    /**
     * Sample code: Delete mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteMobileNetwork(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.mobileNetworks().delete("rg1", "testMobileNetwork", Context.NONE);
    }
}
```

### MobileNetworks_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for MobileNetworks GetByResourceGroup. */
public final class MobileNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/MobileNetworkGet.json
     */
    /**
     * Sample code: Get mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getMobileNetwork(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.mobileNetworks().getByResourceGroupWithResponse("rg1", "testMobileNetwork", Context.NONE);
    }
}
```

### MobileNetworks_List

```java
import com.azure.core.util.Context;

/** Samples for MobileNetworks List. */
public final class MobileNetworksListSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/MobileNetworkListBySubscription.json
     */
    /**
     * Sample code: List mobile networks in a subscription.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listMobileNetworksInASubscription(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.mobileNetworks().list(Context.NONE);
    }
}
```

### MobileNetworks_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for MobileNetworks ListByResourceGroup. */
public final class MobileNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/MobileNetworkListByResourceGroup.json
     */
    /**
     * Sample code: List mobile networks in resource group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listMobileNetworksInResourceGroup(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.mobileNetworks().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### MobileNetworks_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.MobileNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for MobileNetworks UpdateTags. */
public final class MobileNetworksUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/MobileNetworkUpdateTags.json
     */
    /**
     * Sample code: Update mobile network tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateMobileNetworkTags(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        MobileNetwork resource =
            manager
                .mobileNetworks()
                .getByResourceGroupWithResponse("rg1", "testMobileNetwork", Context.NONE)
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/OperationList.json
     */
    /**
     * Sample code: Get Registration Operations.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getRegistrationOperations(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PacketCoreControlPlaneOperation_CollectDiagnosticsPackage

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.PacketCoreControlPlaneCollectDiagnosticsPackage;

/** Samples for PacketCoreControlPlaneOperation CollectDiagnosticsPackage. */
public final class PacketCoreControlPlaneOperationCollectDiagnosticsPackageSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneCollectDiagnosticsPackage.json
     */
    /**
     * Sample code: Collect diagnostics package from packet core control plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void collectDiagnosticsPackageFromPacketCoreControlPlane(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .packetCoreControlPlaneOperations()
            .collectDiagnosticsPackage(
                "rg1",
                "TestPacketCoreCP",
                new PacketCoreControlPlaneCollectDiagnosticsPackage()
                    .withStorageAccountBlobUrl(
                        "https://contosoaccount.blob.core.windows.net/container/diagnosticsPackage.zip"),
                Context.NONE);
    }
}
```

### PacketCoreControlPlaneOperation_Reinstall

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreControlPlaneOperation Reinstall. */
public final class PacketCoreControlPlaneOperationReinstallSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneReinstall.json
     */
    /**
     * Sample code: Reinstall packet core control plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void reinstallPacketCoreControlPlane(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreControlPlaneOperations().reinstall("rg1", "TestPacketCoreCP", Context.NONE);
    }
}
```

### PacketCoreControlPlaneOperation_Rollback

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreControlPlaneOperation Rollback. */
public final class PacketCoreControlPlaneOperationRollbackSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneRollback.json
     */
    /**
     * Sample code: Rollback packet core control plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void rollbackPacketCoreControlPlane(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreControlPlaneOperations().rollback("rg1", "TestPacketCoreCP", Context.NONE);
    }
}
```

### PacketCoreControlPlaneVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreControlPlaneVersions Get. */
public final class PacketCoreControlPlaneVersionsGetSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneVersionGet.json
     */
    /**
     * Sample code: Get packet core control plane version.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getPacketCoreControlPlaneVersion(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreControlPlaneVersions().getWithResponse("PMN-4-11-1", Context.NONE);
    }
}
```

### PacketCoreControlPlaneVersions_List

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreControlPlaneVersions List. */
public final class PacketCoreControlPlaneVersionsListSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneVersionList.json
     */
    /**
     * Sample code: Get supported packet core control plane versions.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getSupportedPacketCoreControlPlaneVersions(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreControlPlaneVersions().list(Context.NONE);
    }
}
```

### PacketCoreControlPlanes_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.AuthenticationType;
import com.azure.resourcemanager.mobilenetwork.models.AzureStackEdgeDeviceResourceId;
import com.azure.resourcemanager.mobilenetwork.models.BillingSku;
import com.azure.resourcemanager.mobilenetwork.models.ConnectedClusterResourceId;
import com.azure.resourcemanager.mobilenetwork.models.CoreNetworkType;
import com.azure.resourcemanager.mobilenetwork.models.CustomLocationResourceId;
import com.azure.resourcemanager.mobilenetwork.models.HttpsServerCertificate;
import com.azure.resourcemanager.mobilenetwork.models.InterfaceProperties;
import com.azure.resourcemanager.mobilenetwork.models.LocalDiagnosticsAccessConfiguration;
import com.azure.resourcemanager.mobilenetwork.models.PlatformConfiguration;
import com.azure.resourcemanager.mobilenetwork.models.PlatformType;
import com.azure.resourcemanager.mobilenetwork.models.SiteResourceId;
import java.util.Arrays;

/** Samples for PacketCoreControlPlanes CreateOrUpdate. */
public final class PacketCoreControlPlanesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneCreate.json
     */
    /**
     * Sample code: Create packet core control plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createPacketCoreControlPlane(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .packetCoreControlPlanes()
            .define("TestPacketCoreCP")
            .withRegion("eastus")
            .withExistingResourceGroup("rg1")
            .withSites(
                Arrays
                    .asList(
                        new SiteResourceId()
                            .withId(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/sites/testSite")))
            .withPlatform(
                new PlatformConfiguration()
                    .withType(PlatformType.AKS_HCI)
                    .withAzureStackEdgeDevice(
                        new AzureStackEdgeDeviceResourceId()
                            .withId(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/TestAzureStackEdgeDevice"))
                    .withConnectedCluster(
                        new ConnectedClusterResourceId()
                            .withId(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Kubernetes/connectedClusters/TestConnectedCluster"))
                    .withCustomLocation(
                        new CustomLocationResourceId()
                            .withId(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ExtendedLocation/customLocations/TestCustomLocation")))
            .withControlPlaneAccessInterface(new InterfaceProperties().withName("N2"))
            .withSku(BillingSku.G0)
            .withLocalDiagnosticsAccess(
                new LocalDiagnosticsAccessConfiguration()
                    .withAuthenticationType(AuthenticationType.AAD)
                    .withHttpsServerCertificate(
                        new HttpsServerCertificate()
                            .withCertificateUrl("https://contosovault.vault.azure.net/certificates/ingress")))
            .withCoreNetworkTechnology(CoreNetworkType.FIVE_GC)
            .withVersion("0.2.0")
            .withUeMtu(1600)
            .create();
    }
}
```

### PacketCoreControlPlanes_Delete

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreControlPlanes Delete. */
public final class PacketCoreControlPlanesDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneDelete.json
     */
    /**
     * Sample code: Delete packet core control plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deletePacketCoreControlPlane(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreControlPlanes().delete("rg1", "TestPacketCoreCP", Context.NONE);
    }
}
```

### PacketCoreControlPlanes_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreControlPlanes GetByResourceGroup. */
public final class PacketCoreControlPlanesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneGet.json
     */
    /**
     * Sample code: Get packet core control plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getPacketCoreControlPlane(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreControlPlanes().getByResourceGroupWithResponse("rg1", "TestPacketCoreCP", Context.NONE);
    }
}
```

### PacketCoreControlPlanes_List

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreControlPlanes List. */
public final class PacketCoreControlPlanesListSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneListBySubscription.json
     */
    /**
     * Sample code: List packet core control planes in a subscription.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listPacketCoreControlPlanesInASubscription(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreControlPlanes().list(Context.NONE);
    }
}
```

### PacketCoreControlPlanes_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreControlPlanes ListByResourceGroup. */
public final class PacketCoreControlPlanesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneListByResourceGroup.json
     */
    /**
     * Sample code: List packet core control planes in resource group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listPacketCoreControlPlanesInResourceGroup(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreControlPlanes().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### PacketCoreControlPlanes_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.PacketCoreControlPlane;
import java.util.HashMap;
import java.util.Map;

/** Samples for PacketCoreControlPlanes UpdateTags. */
public final class PacketCoreControlPlanesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreControlPlaneUpdateTags.json
     */
    /**
     * Sample code: Update packet core control plane tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updatePacketCoreControlPlaneTags(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        PacketCoreControlPlane resource =
            manager
                .packetCoreControlPlanes()
                .getByResourceGroupWithResponse("rg1", "TestPacketCoreCP", Context.NONE)
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

### PacketCoreDataPlanes_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.InterfaceProperties;

/** Samples for PacketCoreDataPlanes CreateOrUpdate. */
public final class PacketCoreDataPlanesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreDataPlaneCreate.json
     */
    /**
     * Sample code: Create packet core data plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createPacketCoreDataPlane(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .packetCoreDataPlanes()
            .define("testPacketCoreDP")
            .withRegion("eastus")
            .withExistingPacketCoreControlPlane("rg1", "testPacketCoreCP")
            .withUserPlaneAccessInterface(new InterfaceProperties().withName("N3"))
            .create();
    }
}
```

### PacketCoreDataPlanes_Delete

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreDataPlanes Delete. */
public final class PacketCoreDataPlanesDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreDataPlaneDelete.json
     */
    /**
     * Sample code: Delete packet core data plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deletePacketCoreDataPlane(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreDataPlanes().delete("rg1", "testPacketCoreCP", "testPacketCoreDP", Context.NONE);
    }
}
```

### PacketCoreDataPlanes_Get

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreDataPlanes Get. */
public final class PacketCoreDataPlanesGetSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreDataPlaneGet.json
     */
    /**
     * Sample code: Get packet core data plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getPacketCoreDataPlane(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreDataPlanes().getWithResponse("rg1", "testPacketCoreCP", "testPacketCoreDP", Context.NONE);
    }
}
```

### PacketCoreDataPlanes_ListByPacketCoreControlPlane

```java
import com.azure.core.util.Context;

/** Samples for PacketCoreDataPlanes ListByPacketCoreControlPlane. */
public final class PacketCoreDataPlanesListByPacketCoreControlPlaneSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreDataPlaneListByPacketCoreControlPlane.json
     */
    /**
     * Sample code: List packet core data planes in a control plane.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listPacketCoreDataPlanesInAControlPlane(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.packetCoreDataPlanes().listByPacketCoreControlPlane("rg1", "testPacketCoreCP", Context.NONE);
    }
}
```

### PacketCoreDataPlanes_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.PacketCoreDataPlane;
import java.util.HashMap;
import java.util.Map;

/** Samples for PacketCoreDataPlanes UpdateTags. */
public final class PacketCoreDataPlanesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/PacketCoreDataPlaneUpdateTags.json
     */
    /**
     * Sample code: Update packet core data plane tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updatePacketCoreDataPlaneTags(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        PacketCoreDataPlane resource =
            manager
                .packetCoreDataPlanes()
                .getWithResponse("rg1", "testPacketCoreCP", "testPacketCoreDP", Context.NONE)
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

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.Ambr;
import com.azure.resourcemanager.mobilenetwork.models.PccRuleConfiguration;
import com.azure.resourcemanager.mobilenetwork.models.PccRuleQosPolicy;
import com.azure.resourcemanager.mobilenetwork.models.PreemptionCapability;
import com.azure.resourcemanager.mobilenetwork.models.PreemptionVulnerability;
import com.azure.resourcemanager.mobilenetwork.models.QosPolicy;
import com.azure.resourcemanager.mobilenetwork.models.SdfDirection;
import com.azure.resourcemanager.mobilenetwork.models.ServiceDataFlowTemplate;
import com.azure.resourcemanager.mobilenetwork.models.TrafficControlPermission;
import java.util.Arrays;

/** Samples for Services CreateOrUpdate. */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/ServiceCreate.json
     */
    /**
     * Sample code: Create service.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createService(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .services()
            .define("TestService")
            .withRegion("eastus")
            .withExistingMobileNetwork("rg1", "testMobileNetwork")
            .withServicePrecedence(255)
            .withPccRules(
                Arrays
                    .asList(
                        new PccRuleConfiguration()
                            .withRuleName("default-rule")
                            .withRulePrecedence(255)
                            .withRuleQosPolicy(
                                new PccRuleQosPolicy()
                                    .withFiveQi(9)
                                    .withAllocationAndRetentionPriorityLevel(9)
                                    .withPreemptionCapability(PreemptionCapability.NOT_PREEMPT)
                                    .withPreemptionVulnerability(PreemptionVulnerability.PREEMPTABLE)
                                    .withMaximumBitRate(new Ambr().withUplink("500 Mbps").withDownlink("1 Gbps")))
                            .withTrafficControl(TrafficControlPermission.ENABLED)
                            .withServiceDataFlowTemplates(
                                Arrays
                                    .asList(
                                        new ServiceDataFlowTemplate()
                                            .withTemplateName("IP-to-server")
                                            .withDirection(SdfDirection.UPLINK)
                                            .withProtocol(Arrays.asList("ip"))
                                            .withRemoteIpList(Arrays.asList("10.3.4.0/24"))
                                            .withPorts(Arrays.asList())))))
            .withServiceQosPolicy(
                new QosPolicy()
                    .withFiveQi(9)
                    .withAllocationAndRetentionPriorityLevel(9)
                    .withPreemptionCapability(PreemptionCapability.NOT_PREEMPT)
                    .withPreemptionVulnerability(PreemptionVulnerability.PREEMPTABLE)
                    .withMaximumBitRate(new Ambr().withUplink("500 Mbps").withDownlink("1 Gbps")))
            .create();
    }
}
```

### Services_Delete

```java
import com.azure.core.util.Context;

/** Samples for Services Delete. */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/ServiceDelete.json
     */
    /**
     * Sample code: Delete service.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteService(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.services().delete("rg1", "testMobileNetwork", "TestService", Context.NONE);
    }
}
```

### Services_Get

```java
import com.azure.core.util.Context;

/** Samples for Services Get. */
public final class ServicesGetSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/ServiceGet.json
     */
    /**
     * Sample code: Get service.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getService(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.services().getWithResponse("rg1", "testMobileNetwork", "TestService", Context.NONE);
    }
}
```

### Services_ListByMobileNetwork

```java
import com.azure.core.util.Context;

/** Samples for Services ListByMobileNetwork. */
public final class ServicesListByMobileNetworkSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/ServiceListByMobileNetwork.json
     */
    /**
     * Sample code: List services in a mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listServicesInAMobileNetwork(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.services().listByMobileNetwork("testResourceGroupName", "testMobileNetwork", Context.NONE);
    }
}
```

### Services_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.Service;
import java.util.HashMap;
import java.util.Map;

/** Samples for Services UpdateTags. */
public final class ServicesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/ServiceUpdateTags.json
     */
    /**
     * Sample code: Update service tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateServiceTags(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        Service resource =
            manager.services().getWithResponse("rg1", "testMobileNetwork", "TestService", Context.NONE).getValue();
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

### SimGroups_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.KeyVaultKey;
import com.azure.resourcemanager.mobilenetwork.models.ManagedServiceIdentity;
import com.azure.resourcemanager.mobilenetwork.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.mobilenetwork.models.MobileNetworkResourceId;
import com.azure.resourcemanager.mobilenetwork.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for SimGroups CreateOrUpdate. */
public final class SimGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimGroupCreate.json
     */
    /**
     * Sample code: Create SIM group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createSIMGroup(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .simGroups()
            .define("testSimGroup")
            .withRegion("eastus")
            .withExistingResourceGroup("rg1")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/subid/resourcegroups/rg1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testUserAssignedManagedIdentity",
                            new UserAssignedIdentity())))
            .withEncryptionKey(new KeyVaultKey().withKeyUrl("fakeTokenPlaceholder"))
            .withMobileNetwork(
                new MobileNetworkResourceId()
                    .withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork"))
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

### SimGroups_Delete

```java
import com.azure.core.util.Context;

/** Samples for SimGroups Delete. */
public final class SimGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimGroupDelete.json
     */
    /**
     * Sample code: Delete SIM group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteSIMGroup(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.simGroups().delete("testResourceGroupName", "testSimGroup", Context.NONE);
    }
}
```

### SimGroups_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SimGroups GetByResourceGroup. */
public final class SimGroupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimGroupGet.json
     */
    /**
     * Sample code: Get SIM group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getSIMGroup(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.simGroups().getByResourceGroupWithResponse("testResourceGroupName", "testSimGroupName", Context.NONE);
    }
}
```

### SimGroups_List

```java
import com.azure.core.util.Context;

/** Samples for SimGroups List. */
public final class SimGroupsListSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimGroupListBySubscription.json
     */
    /**
     * Sample code: List SIM groups in a subscription.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listSIMGroupsInASubscription(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.simGroups().list(Context.NONE);
    }
}
```

### SimGroups_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SimGroups ListByResourceGroup. */
public final class SimGroupsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimGroupListByResourceGroup.json
     */
    /**
     * Sample code: List SIM groups in a resource group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listSIMGroupsInAResourceGroup(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.simGroups().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### SimGroups_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.SimGroup;
import java.util.HashMap;
import java.util.Map;

/** Samples for SimGroups UpdateTags. */
public final class SimGroupsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimGroupUpdateTags.json
     */
    /**
     * Sample code: Update SIM group tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateSIMGroupTags(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        SimGroup resource =
            manager.simGroups().getByResourceGroupWithResponse("rg1", "testSimGroup", Context.NONE).getValue();
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

### SimOperation_BulkDelete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.SimDeleteList;
import java.util.Arrays;

/** Samples for SimOperation BulkDelete. */
public final class SimOperationBulkDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimBulkDelete.json
     */
    /**
     * Sample code: Bulk delete SIMs from a SIM group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void bulkDeleteSIMsFromASIMGroup(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .simOperations()
            .bulkDelete(
                "testResourceGroupName",
                "testSimGroup",
                new SimDeleteList().withSims(Arrays.asList("testSim", "testSim2")),
                Context.NONE);
    }
}
```

### SimOperation_BulkUpload

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.AttachedDataNetworkResourceId;
import com.azure.resourcemanager.mobilenetwork.models.SimNameAndProperties;
import com.azure.resourcemanager.mobilenetwork.models.SimPolicyResourceId;
import com.azure.resourcemanager.mobilenetwork.models.SimStaticIpProperties;
import com.azure.resourcemanager.mobilenetwork.models.SimStaticIpPropertiesStaticIp;
import com.azure.resourcemanager.mobilenetwork.models.SimUploadList;
import com.azure.resourcemanager.mobilenetwork.models.SliceResourceId;
import java.util.Arrays;

/** Samples for SimOperation BulkUpload. */
public final class SimOperationBulkUploadSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimBulkUpload.json
     */
    /**
     * Sample code: Bulk upload SIMs in a SIM group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void bulkUploadSIMsInASIMGroup(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .simOperations()
            .bulkUpload(
                "rg1",
                "testSimGroup",
                new SimUploadList()
                    .withSims(
                        Arrays
                            .asList(
                                new SimNameAndProperties()
                                    .withName("testSim")
                                    .withAuthenticationKey("fakeTokenPlaceholder")
                                    .withOperatorKeyCode("fakeTokenPlaceholder")
                                    .withInternationalMobileSubscriberIdentity("00000")
                                    .withIntegratedCircuitCardIdentifier("8900000000000000000")
                                    .withDeviceType("Video camera")
                                    .withSimPolicy(
                                        new SimPolicyResourceId()
                                            .withId(
                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/simPolicies/MySimPolicy"))
                                    .withStaticIpConfiguration(
                                        Arrays
                                            .asList(
                                                new SimStaticIpProperties()
                                                    .withAttachedDataNetwork(
                                                        new AttachedDataNetworkResourceId()
                                                            .withId(
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/packetCoreControlPlanes/TestPacketCoreCP/packetCoreDataPlanes/TestPacketCoreDP/attachedDataNetworks/TestAttachedDataNetwork"))
                                                    .withSlice(
                                                        new SliceResourceId()
                                                            .withId(
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/slices/testSlice"))
                                                    .withStaticIp(
                                                        new SimStaticIpPropertiesStaticIp()
                                                            .withIpv4Address("2.4.0.1")))),
                                new SimNameAndProperties()
                                    .withName("testSim2")
                                    .withAuthenticationKey("fakeTokenPlaceholder")
                                    .withOperatorKeyCode("fakeTokenPlaceholder")
                                    .withInternationalMobileSubscriberIdentity("00000")
                                    .withIntegratedCircuitCardIdentifier("8900000000000000001")
                                    .withDeviceType("Video camera")
                                    .withSimPolicy(
                                        new SimPolicyResourceId()
                                            .withId(
                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/simPolicies/MySimPolicy"))
                                    .withStaticIpConfiguration(
                                        Arrays
                                            .asList(
                                                new SimStaticIpProperties()
                                                    .withAttachedDataNetwork(
                                                        new AttachedDataNetworkResourceId()
                                                            .withId(
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/packetCoreControlPlanes/TestPacketCoreCP/packetCoreDataPlanes/TestPacketCoreDP/attachedDataNetworks/TestAttachedDataNetwork"))
                                                    .withSlice(
                                                        new SliceResourceId()
                                                            .withId(
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/slices/testSlice"))
                                                    .withStaticIp(
                                                        new SimStaticIpPropertiesStaticIp()
                                                            .withIpv4Address("2.4.0.2")))))),
                Context.NONE);
    }
}
```

### SimOperation_BulkUploadEncrypted

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.AttachedDataNetworkResourceId;
import com.azure.resourcemanager.mobilenetwork.models.EncryptedSimUploadList;
import com.azure.resourcemanager.mobilenetwork.models.SimNameAndEncryptedProperties;
import com.azure.resourcemanager.mobilenetwork.models.SimPolicyResourceId;
import com.azure.resourcemanager.mobilenetwork.models.SimStaticIpProperties;
import com.azure.resourcemanager.mobilenetwork.models.SimStaticIpPropertiesStaticIp;
import com.azure.resourcemanager.mobilenetwork.models.SliceResourceId;
import java.util.Arrays;

/** Samples for SimOperation BulkUploadEncrypted. */
public final class SimOperationBulkUploadEncryptedSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimBulkUploadEncrypted.json
     */
    /**
     * Sample code: Bulk upload encrypted SIMs to a SIM group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void bulkUploadEncryptedSIMsToASIMGroup(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .simOperations()
            .bulkUploadEncrypted(
                "rg1",
                "testSimGroup",
                new EncryptedSimUploadList()
                    .withVersion(1)
                    .withAzureKeyIdentifier(1)
                    .withVendorKeyFingerprint("fakeTokenPlaceholder")
                    .withEncryptedTransportKey("fakeTokenPlaceholder")
                    .withSignedTransportKey("fakeTokenPlaceholder")
                    .withSims(
                        Arrays
                            .asList(
                                new SimNameAndEncryptedProperties()
                                    .withName("testSim")
                                    .withEncryptedCredentials("fakeTokenPlaceholder")
                                    .withInternationalMobileSubscriberIdentity("00000")
                                    .withIntegratedCircuitCardIdentifier("8900000000000000000")
                                    .withDeviceType("Video camera")
                                    .withSimPolicy(
                                        new SimPolicyResourceId()
                                            .withId(
                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/simPolicies/MySimPolicy"))
                                    .withStaticIpConfiguration(
                                        Arrays
                                            .asList(
                                                new SimStaticIpProperties()
                                                    .withAttachedDataNetwork(
                                                        new AttachedDataNetworkResourceId()
                                                            .withId(
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/packetCoreControlPlanes/TestPacketCoreCP/packetCoreDataPlanes/TestPacketCoreDP/attachedDataNetworks/TestAttachedDataNetwork"))
                                                    .withSlice(
                                                        new SliceResourceId()
                                                            .withId(
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/slices/testSlice"))
                                                    .withStaticIp(
                                                        new SimStaticIpPropertiesStaticIp()
                                                            .withIpv4Address("2.4.0.1")))),
                                new SimNameAndEncryptedProperties()
                                    .withName("testSim2")
                                    .withEncryptedCredentials("fakeTokenPlaceholder")
                                    .withInternationalMobileSubscriberIdentity("00000")
                                    .withIntegratedCircuitCardIdentifier("8900000000000000001")
                                    .withDeviceType("Video camera")
                                    .withSimPolicy(
                                        new SimPolicyResourceId()
                                            .withId(
                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/simPolicies/MySimPolicy"))
                                    .withStaticIpConfiguration(
                                        Arrays
                                            .asList(
                                                new SimStaticIpProperties()
                                                    .withAttachedDataNetwork(
                                                        new AttachedDataNetworkResourceId()
                                                            .withId(
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/packetCoreControlPlanes/TestPacketCoreCP/packetCoreDataPlanes/TestPacketCoreDP/attachedDataNetworks/TestAttachedDataNetwork"))
                                                    .withSlice(
                                                        new SliceResourceId()
                                                            .withId(
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/slices/testSlice"))
                                                    .withStaticIp(
                                                        new SimStaticIpPropertiesStaticIp()
                                                            .withIpv4Address("2.4.0.2")))))),
                Context.NONE);
    }
}
```

### SimPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.Ambr;
import com.azure.resourcemanager.mobilenetwork.models.DataNetworkConfiguration;
import com.azure.resourcemanager.mobilenetwork.models.DataNetworkResourceId;
import com.azure.resourcemanager.mobilenetwork.models.PduSessionType;
import com.azure.resourcemanager.mobilenetwork.models.PreemptionCapability;
import com.azure.resourcemanager.mobilenetwork.models.PreemptionVulnerability;
import com.azure.resourcemanager.mobilenetwork.models.ServiceResourceId;
import com.azure.resourcemanager.mobilenetwork.models.SliceConfiguration;
import com.azure.resourcemanager.mobilenetwork.models.SliceResourceId;
import java.util.Arrays;

/** Samples for SimPolicies CreateOrUpdate. */
public final class SimPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimPolicyCreate.json
     */
    /**
     * Sample code: Create SIM policy.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createSIMPolicy(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .simPolicies()
            .define("testPolicy")
            .withRegion("eastus")
            .withExistingMobileNetwork("rg1", "testMobileNetwork")
            .withUeAmbr(new Ambr().withUplink("500 Mbps").withDownlink("1 Gbps"))
            .withDefaultSlice(
                new SliceResourceId()
                    .withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/slices/testSlice"))
            .withSliceConfigurations(
                Arrays
                    .asList(
                        new SliceConfiguration()
                            .withSlice(
                                new SliceResourceId()
                                    .withId(
                                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/slices/testSlice"))
                            .withDefaultDataNetwork(
                                new DataNetworkResourceId()
                                    .withId(
                                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/dataNetworks/testdataNetwork"))
                            .withDataNetworkConfigurations(
                                Arrays
                                    .asList(
                                        new DataNetworkConfiguration()
                                            .withDataNetwork(
                                                new DataNetworkResourceId()
                                                    .withId(
                                                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/dataNetworks/testdataNetwork"))
                                            .withSessionAmbr(new Ambr().withUplink("500 Mbps").withDownlink("1 Gbps"))
                                            .withFiveQi(9)
                                            .withAllocationAndRetentionPriorityLevel(9)
                                            .withPreemptionCapability(PreemptionCapability.NOT_PREEMPT)
                                            .withPreemptionVulnerability(PreemptionVulnerability.PREEMPTABLE)
                                            .withDefaultSessionType(PduSessionType.IPV4)
                                            .withAdditionalAllowedSessionTypes(Arrays.asList())
                                            .withAllowedServices(
                                                Arrays
                                                    .asList(
                                                        new ServiceResourceId()
                                                            .withId(
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/services/testService")))
                                            .withMaximumNumberOfBufferedPackets(200)))))
            .withRegistrationTimer(3240)
            .create();
    }
}
```

### SimPolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for SimPolicies Delete. */
public final class SimPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimPolicyDelete.json
     */
    /**
     * Sample code: Delete SIM policy.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteSIMPolicy(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.simPolicies().delete("rg1", "testMobileNetwork", "testPolicy", Context.NONE);
    }
}
```

### SimPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for SimPolicies Get. */
public final class SimPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimPolicyGet.json
     */
    /**
     * Sample code: Get SIM policy.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getSIMPolicy(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.simPolicies().getWithResponse("rg1", "testMobileNetwork", "testPolicy", Context.NONE);
    }
}
```

### SimPolicies_ListByMobileNetwork

```java
import com.azure.core.util.Context;

/** Samples for SimPolicies ListByMobileNetwork. */
public final class SimPoliciesListByMobileNetworkSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimPolicyListByMobileNetwork.json
     */
    /**
     * Sample code: List SIM policies in a mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listSIMPoliciesInAMobileNetwork(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.simPolicies().listByMobileNetwork("testResourceGroupName", "testMobileNetwork", Context.NONE);
    }
}
```

### SimPolicies_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.SimPolicy;
import java.util.HashMap;
import java.util.Map;

/** Samples for SimPolicies UpdateTags. */
public final class SimPoliciesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimPolicyUpdateTags.json
     */
    /**
     * Sample code: Update SIM policy tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateSIMPolicyTags(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        SimPolicy resource =
            manager.simPolicies().getWithResponse("rg1", "testMobileNetwork", "testPolicy", Context.NONE).getValue();
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

### Sims_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.AttachedDataNetworkResourceId;
import com.azure.resourcemanager.mobilenetwork.models.SimPolicyResourceId;
import com.azure.resourcemanager.mobilenetwork.models.SimStaticIpProperties;
import com.azure.resourcemanager.mobilenetwork.models.SimStaticIpPropertiesStaticIp;
import com.azure.resourcemanager.mobilenetwork.models.SliceResourceId;
import java.util.Arrays;

/** Samples for Sims CreateOrUpdate. */
public final class SimsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimCreate.json
     */
    /**
     * Sample code: Create SIM.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createSIM(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .sims()
            .define("testSim")
            .withExistingSimGroup("rg1", "testSimGroup")
            .withInternationalMobileSubscriberIdentity("00000")
            .withAuthenticationKey("00000000000000000000000000000000")
            .withOperatorKeyCode("00000000000000000000000000000000")
            .withIntegratedCircuitCardIdentifier("8900000000000000000")
            .withDeviceType("Video camera")
            .withSimPolicy(
                new SimPolicyResourceId()
                    .withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/simPolicies/MySimPolicy"))
            .withStaticIpConfiguration(
                Arrays
                    .asList(
                        new SimStaticIpProperties()
                            .withAttachedDataNetwork(
                                new AttachedDataNetworkResourceId()
                                    .withId(
                                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/packetCoreControlPlanes/TestPacketCoreCP/packetCoreDataPlanes/TestPacketCoreDP/attachedDataNetworks/TestAttachedDataNetwork"))
                            .withSlice(
                                new SliceResourceId()
                                    .withId(
                                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/slices/testSlice"))
                            .withStaticIp(new SimStaticIpPropertiesStaticIp().withIpv4Address("2.4.0.1"))))
            .create();
    }
}
```

### Sims_Delete

```java
import com.azure.core.util.Context;

/** Samples for Sims Delete. */
public final class SimsDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimDelete.json
     */
    /**
     * Sample code: Delete SIM.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteSIM(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sims().delete("testResourceGroupName", "testSimGroup", "testSim", Context.NONE);
    }
}
```

### Sims_Get

```java
import com.azure.core.util.Context;

/** Samples for Sims Get. */
public final class SimsGetSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimGet.json
     */
    /**
     * Sample code: Get SIM.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getSIM(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sims().getWithResponse("testResourceGroupName", "testSimGroup", "testSimName", Context.NONE);
    }
}
```

### Sims_ListByGroup

```java
import com.azure.core.util.Context;

/** Samples for Sims ListByGroup. */
public final class SimsListByGroupSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SimListBySimGroup.json
     */
    /**
     * Sample code: List SIMs in a SIM group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listSIMsInASIMGroup(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sims().listByGroup("rg1", "testSimGroup", Context.NONE);
    }
}
```

### Sites_CreateOrUpdate

```java
/** Samples for Sites CreateOrUpdate. */
public final class SitesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SiteCreate.json
     */
    /**
     * Sample code: Create mobile network site.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createMobileNetworkSite(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .sites()
            .define("testSite")
            .withRegion("testLocation")
            .withExistingMobileNetwork("rg1", "testMobileNetwork")
            .create();
    }
}
```

### Sites_Delete

```java
import com.azure.core.util.Context;

/** Samples for Sites Delete. */
public final class SitesDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SiteDelete.json
     */
    /**
     * Sample code: Delete mobile network site.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteMobileNetworkSite(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sites().delete("rg1", "testMobileNetwork", "testSite", Context.NONE);
    }
}
```

### Sites_Get

```java
import com.azure.core.util.Context;

/** Samples for Sites Get. */
public final class SitesGetSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SiteGet.json
     */
    /**
     * Sample code: Get mobile network site.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getMobileNetworkSite(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sites().getWithResponse("rg1", "testMobileNetwork", "testSite", Context.NONE);
    }
}
```

### Sites_ListByMobileNetwork

```java
import com.azure.core.util.Context;

/** Samples for Sites ListByMobileNetwork. */
public final class SitesListByMobileNetworkSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SiteListByMobileNetwork.json
     */
    /**
     * Sample code: List mobile network sites in a mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listMobileNetworkSitesInAMobileNetwork(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sites().listByMobileNetwork("rg1", "testMobileNetwork", Context.NONE);
    }
}
```

### Sites_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.Site;
import java.util.HashMap;
import java.util.Map;

/** Samples for Sites UpdateTags. */
public final class SitesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SiteUpdateTags.json
     */
    /**
     * Sample code: Update mobile network site tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateMobileNetworkSiteTags(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        Site resource =
            manager.sites().getWithResponse("rg1", "testMobileNetwork", "testSite", Context.NONE).getValue();
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

### Slices_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.Snssai;

/** Samples for Slices CreateOrUpdate. */
public final class SlicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SliceCreate.json
     */
    /**
     * Sample code: Create network slice.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createNetworkSlice(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .slices()
            .define("testSlice")
            .withRegion("eastus")
            .withExistingMobileNetwork("rg1", "testMobileNetwork")
            .withSnssai(new Snssai().withSst(1).withSd("1abcde"))
            .withDescription("myFavouriteSlice")
            .create();
    }
}
```

### Slices_Delete

```java
import com.azure.core.util.Context;

/** Samples for Slices Delete. */
public final class SlicesDeleteSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SliceDelete.json
     */
    /**
     * Sample code: Delete network slice.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteNetworkSlice(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.slices().delete("rg1", "testMobileNetwork", "testSlice", Context.NONE);
    }
}
```

### Slices_Get

```java
import com.azure.core.util.Context;

/** Samples for Slices Get. */
public final class SlicesGetSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SliceGet.json
     */
    /**
     * Sample code: Get network slice.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getNetworkSlice(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.slices().getWithResponse("rg1", "testMobileNetwork", "testSlice", Context.NONE);
    }
}
```

### Slices_ListByMobileNetwork

```java
import com.azure.core.util.Context;

/** Samples for Slices ListByMobileNetwork. */
public final class SlicesListByMobileNetworkSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SliceListByMobileNetwork.json
     */
    /**
     * Sample code: List network slices in a mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listNetworkSlicesInAMobileNetwork(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.slices().listByMobileNetwork("rg1", "testMobileNetwork", Context.NONE);
    }
}
```

### Slices_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.Slice;
import java.util.HashMap;
import java.util.Map;

/** Samples for Slices UpdateTags. */
public final class SlicesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/stable/2022-11-01/examples/SliceUpdateTags.json
     */
    /**
     * Sample code: Update network slice tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateNetworkSliceTags(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        Slice resource =
            manager.slices().getWithResponse("rg1", "testMobileNetwork", "testSlice", Context.NONE).getValue();
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

