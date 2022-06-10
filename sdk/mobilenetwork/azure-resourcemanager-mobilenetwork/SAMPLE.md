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
- [ListSimIds](#mobilenetworks_listsimids)
- [UpdateTags](#mobilenetworks_updatetags)

## Operations

- [List](#operations_list)

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

## SimPolicies

- [CreateOrUpdate](#simpolicies_createorupdate)
- [Delete](#simpolicies_delete)
- [Get](#simpolicies_get)
- [ListByMobileNetwork](#simpolicies_listbymobilenetwork)
- [UpdateTags](#simpolicies_updatetags)

## Sims

- [CreateOrUpdate](#sims_createorupdate)
- [Delete](#sims_delete)
- [GetByResourceGroup](#sims_getbyresourcegroup)
- [List](#sims_list)
- [ListByResourceGroup](#sims_listbyresourcegroup)
- [UpdateTags](#sims_updatetags)

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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/AttachedDataNetworkCreate.json
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
            .withNaptConfiguration(
                new NaptConfiguration()
                    .withEnabled(NaptEnabled.ENABLED)
                    .withPortRange(new PortRange().withMinPort(1024).withMaxPort(65535))
                    .withPortReuseHoldTime(new PortReuseHoldTimes().withTcp(120).withUdp(60))
                    .withPinholeLimits(65536)
                    .withPinholeTimeouts(new PinholeTimeouts().withTcp(7440).withUdp(300).withIcmp(60)))
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/AttachedDataNetworkDelete.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/AttachedDataNetworkGet.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/AttachedDataNetworkListByPacketCoreDataPlane.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/AttachedDataNetworkUpdateTags.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/DataNetworkCreate.json
     */
    /**
     * Sample code: Create mobile network dataNetwork.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createMobileNetworkDataNetwork(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/DataNetworkDelete.json
     */
    /**
     * Sample code: Delete mobile network dataNetwork.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteMobileNetworkDataNetwork(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/DataNetworkGet.json
     */
    /**
     * Sample code: Get mobile network dataNetwork.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getMobileNetworkDataNetwork(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/DataNetworkListByMobileNetwork.json
     */
    /**
     * Sample code: List mobile network dataNetworks in a mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listMobileNetworkDataNetworksInAMobileNetwork(
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/DataNetworkUpdateTags.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/MobileNetworkCreate.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/MobileNetworkDelete.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/MobileNetworkGet.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/MobileNetworkListBySubscription.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/MobileNetworkListByResourceGroup.json
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

### MobileNetworks_ListSimIds

```java
import com.azure.core.util.Context;

/** Samples for MobileNetworks ListSimIds. */
public final class MobileNetworksListSimIdsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimIdListByMobileNetwork.json
     */
    /**
     * Sample code: List sim profile ids by network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listSimProfileIdsByNetwork(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.mobileNetworks().listSimIds("rg", "testMobileNetworkName", Context.NONE);
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/MobileNetworkUpdateTags.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/OperationList.json
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

### PacketCoreControlPlanes_CreateOrUpdate

```java
import com.azure.resourcemanager.mobilenetwork.models.CoreNetworkType;
import com.azure.resourcemanager.mobilenetwork.models.CustomLocationResourceId;
import com.azure.resourcemanager.mobilenetwork.models.InterfaceProperties;
import com.azure.resourcemanager.mobilenetwork.models.MobileNetworkResourceId;

/** Samples for PacketCoreControlPlanes CreateOrUpdate. */
public final class PacketCoreControlPlanesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreControlPlaneCreate.json
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
            .withMobileNetwork(
                new MobileNetworkResourceId()
                    .withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork"))
            .withControlPlaneAccessInterface(new InterfaceProperties().withName("N2"))
            .withCustomLocation(
                new CustomLocationResourceId()
                    .withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ExtendedLocation/customLocations/TestCustomLocation"))
            .withCoreNetworkTechnology(CoreNetworkType.FIVE_GC)
            .withVersion("0.2.0")
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreControlPlaneDelete.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreControlPlaneGet.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreControlPlaneListBySubscription.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreControlPlaneListByResourceGroup.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreControlPlaneUpdateTags.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreDataPlaneCreate.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreDataPlaneDelete.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreDataPlaneGet.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreDataPlaneListByPacketCoreControlPlane.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/PacketCoreDataPlaneUpdateTags.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/ServiceCreate.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/ServiceDelete.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/ServiceGet.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/ServiceListByMobileNetwork.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/ServiceUpdateTags.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimPolicyCreate.json
     */
    /**
     * Sample code: Create sim policy.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createSimPolicy(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
                                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork/services/testService")))))))
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimPolicyDelete.json
     */
    /**
     * Sample code: Delete sim policy.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteSimPolicy(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimPolicyGet.json
     */
    /**
     * Sample code: Get sim policy.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getSimPolicy(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimPolicyListByMobileNetwork.json
     */
    /**
     * Sample code: List sim policies in a mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listSimPoliciesInAMobileNetwork(
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimPolicyUpdateTags.json
     */
    /**
     * Sample code: Update sim policy tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateSimPolicyTags(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
import com.azure.resourcemanager.mobilenetwork.models.MobileNetworkResourceId;
import com.azure.resourcemanager.mobilenetwork.models.SimPolicyResourceId;
import com.azure.resourcemanager.mobilenetwork.models.SimStaticIpProperties;
import com.azure.resourcemanager.mobilenetwork.models.SimStaticIpPropertiesStaticIp;
import com.azure.resourcemanager.mobilenetwork.models.SliceResourceId;
import java.util.Arrays;

/** Samples for Sims CreateOrUpdate. */
public final class SimsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimCreate.json
     */
    /**
     * Sample code: Create sim.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createSim(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager
            .sims()
            .define("testSim")
            .withRegion("testLocation")
            .withExistingResourceGroup("rg1")
            .withInternationalMobileSubscriberIdentity("00000")
            .withIntegratedCircuitCardIdentifier("8900000000000000000")
            .withAuthenticationKey("00000000000000000000000000000000")
            .withOperatorKeyCode("00000000000000000000000000000000")
            .withMobileNetwork(
                new MobileNetworkResourceId()
                    .withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.MobileNetwork/mobileNetworks/testMobileNetwork"))
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimDelete.json
     */
    /**
     * Sample code: Delete sim.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteSim(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sims().delete("testResourceGroupName", "testSim", Context.NONE);
    }
}
```

### Sims_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Sims GetByResourceGroup. */
public final class SimsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimGet.json
     */
    /**
     * Sample code: Get sim.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getSim(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sims().getByResourceGroupWithResponse("testResourceGroupName", "testSimName", Context.NONE);
    }
}
```

### Sims_List

```java
import com.azure.core.util.Context;

/** Samples for Sims List. */
public final class SimsListSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimListBySubscription.json
     */
    /**
     * Sample code: List sims in a subscription.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listSimsInASubscription(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sims().list(Context.NONE);
    }
}
```

### Sims_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Sims ListByResourceGroup. */
public final class SimsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimListByResourceGroup.json
     */
    /**
     * Sample code: List sims in a resource group.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listSimsInAResourceGroup(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        manager.sims().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### Sims_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mobilenetwork.models.Sim;
import java.util.HashMap;
import java.util.Map;

/** Samples for Sims UpdateTags. */
public final class SimsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SimUpdateTags.json
     */
    /**
     * Sample code: Update sim tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateSimTags(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
        Sim resource = manager.sims().getByResourceGroupWithResponse("rg1", "testSim", Context.NONE).getValue();
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

### Sites_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import java.util.Arrays;

/** Samples for Sites CreateOrUpdate. */
public final class SitesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SiteCreate.json
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
            .withNetworkFunctions(
                Arrays
                    .asList(
                        new SubResource()
                            .withId(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.HybridNetwork/networkFunctions/testNf")))
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SiteDelete.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SiteGet.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SiteListByMobileNetwork.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SiteUpdateTags.json
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SliceCreate.json
     */
    /**
     * Sample code: Create mobile network slice.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void createMobileNetworkSlice(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SliceDelete.json
     */
    /**
     * Sample code: Delete mobile network slice.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void deleteMobileNetworkSlice(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SliceGet.json
     */
    /**
     * Sample code: Get mobile network slice.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void getMobileNetworkSlice(com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SliceListByMobileNetwork.json
     */
    /**
     * Sample code: List mobile network slices in a mobile network.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void listMobileNetworkSlicesInAMobileNetwork(
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
     * x-ms-original-file: specification/mobilenetwork/resource-manager/Microsoft.MobileNetwork/preview/2022-03-01-preview/examples/SliceUpdateTags.json
     */
    /**
     * Sample code: Update mobile network slice tags.
     *
     * @param manager Entry point to MobileNetworkManager.
     */
    public static void updateMobileNetworkSliceTags(
        com.azure.resourcemanager.mobilenetwork.MobileNetworkManager manager) {
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

