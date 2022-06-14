# Code snippets and samples


## Devices

- [CreateOrUpdate](#devices_createorupdate)
- [Delete](#devices_delete)
- [GetByResourceGroup](#devices_getbyresourcegroup)
- [List](#devices_list)
- [ListByResourceGroup](#devices_listbyresourcegroup)
- [ListRegistrationKey](#devices_listregistrationkey)
- [UpdateTags](#devices_updatetags)

## NetworkFunctionVendorSkus

- [ListBySku](#networkfunctionvendorskus_listbysku)
- [ListByVendor](#networkfunctionvendorskus_listbyvendor)

## NetworkFunctionVendors

- [List](#networkfunctionvendors_list)

## NetworkFunctions

- [CreateOrUpdate](#networkfunctions_createorupdate)
- [Delete](#networkfunctions_delete)
- [ExecuteRequest](#networkfunctions_executerequest)
- [GetByResourceGroup](#networkfunctions_getbyresourcegroup)
- [List](#networkfunctions_list)
- [ListByResourceGroup](#networkfunctions_listbyresourcegroup)
- [UpdateTags](#networkfunctions_updatetags)

## Operations

- [List](#operations_list)

## RoleInstances

- [Get](#roleinstances_get)
- [List](#roleinstances_list)
- [Restart](#roleinstances_restart)
- [Start](#roleinstances_start)
- [Stop](#roleinstances_stop)

## VendorNetworkFunctions

- [CreateOrUpdate](#vendornetworkfunctions_createorupdate)
- [Get](#vendornetworkfunctions_get)
- [List](#vendornetworkfunctions_list)

## VendorSkuPreview

- [CreateOrUpdate](#vendorskupreview_createorupdate)
- [Delete](#vendorskupreview_delete)
- [Get](#vendorskupreview_get)
- [List](#vendorskupreview_list)

## VendorSkus

- [CreateOrUpdate](#vendorskus_createorupdate)
- [Delete](#vendorskus_delete)
- [Get](#vendorskus_get)
- [List](#vendorskus_list)
- [ListCredential](#vendorskus_listcredential)

## Vendors

- [CreateOrUpdate](#vendors_createorupdate)
- [Delete](#vendors_delete)
- [Get](#vendors_get)
- [List](#vendors_list)
### Devices_CreateOrUpdate

```java
/** Samples for Devices CreateOrUpdate. */
public final class DevicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/DeviceCreate.json
     */
    /**
     * Sample code: Create or update device.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateDevice(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.devices().define("TestDevice").withRegion("eastus").withExistingResourceGroup("rg1").create();
    }
}
```

### Devices_Delete

```java
import com.azure.core.util.Context;

/** Samples for Devices Delete. */
public final class DevicesDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/DeviceDelete.json
     */
    /**
     * Sample code: Delete device resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteDeviceResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.devices().delete("rg1", "TestDevice", Context.NONE);
    }
}
```

### Devices_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Devices GetByResourceGroup. */
public final class DevicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/DeviceGet.json
     */
    /**
     * Sample code: Get device resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getDeviceResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.devices().getByResourceGroupWithResponse("rg1", "TestDevice", Context.NONE);
    }
}
```

### Devices_List

```java
import com.azure.core.util.Context;

/** Samples for Devices List. */
public final class DevicesListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/DeviceListBySubscription.json
     */
    /**
     * Sample code: List all devices in a subscription.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllDevicesInASubscription(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.devices().list(Context.NONE);
    }
}
```

### Devices_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Devices ListByResourceGroup. */
public final class DevicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/DeviceListByResourceGroup.json
     */
    /**
     * Sample code: List all devices in resource group.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllDevicesInResourceGroup(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.devices().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### Devices_ListRegistrationKey

```java
import com.azure.core.util.Context;

/** Samples for Devices ListRegistrationKey. */
public final class DevicesListRegistrationKeySamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/DeviceListRegistrationKey.json
     */
    /**
     * Sample code: Get device registration key.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getDeviceRegistrationKey(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.devices().listRegistrationKeyWithResponse("rg1", "TestDevice", Context.NONE);
    }
}
```

### Devices_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.hybridnetwork.models.Device;
import java.util.HashMap;
import java.util.Map;

/** Samples for Devices UpdateTags. */
public final class DevicesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/DeviceUpdateTags.json
     */
    /**
     * Sample code: Update hybrid network device tags.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateHybridNetworkDeviceTags(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        Device resource =
            manager.devices().getByResourceGroupWithResponse("rg1", "TestDevice", Context.NONE).getValue();
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

### NetworkFunctionVendorSkus_ListBySku

```java
import com.azure.core.util.Context;

/** Samples for NetworkFunctionVendorSkus ListBySku. */
public final class NetworkFunctionVendorSkusListBySkuSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionSkuDetailsGet.json
     */
    /**
     * Sample code: Get network function sku details.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkFunctionSkuDetails(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionVendorSkus().listBySku("testVendor", "testSku", Context.NONE);
    }
}
```

### NetworkFunctionVendorSkus_ListByVendor

```java
import com.azure.core.util.Context;

/** Samples for NetworkFunctionVendorSkus ListByVendor. */
public final class NetworkFunctionVendorSkusListByVendorSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionSkuListByVendor.json
     */
    /**
     * Sample code: List vendors and skus.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listVendorsAndSkus(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionVendorSkus().listByVendor("testVendor", Context.NONE);
    }
}
```

### NetworkFunctionVendors_List

```java
import com.azure.core.util.Context;

/** Samples for NetworkFunctionVendors List. */
public final class NetworkFunctionVendorsListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionVendorAndSkuListBySubscription.json
     */
    /**
     * Sample code: List vendors and skus by subscription.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listVendorsAndSkusBySubscription(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionVendors().list(Context.NONE);
    }
}
```

### NetworkFunctions_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.hybridnetwork.models.IpAllocationMethod;
import com.azure.resourcemanager.hybridnetwork.models.IpVersion;
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionUserConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.NetworkInterface;
import com.azure.resourcemanager.hybridnetwork.models.NetworkInterfaceIpConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.VMSwitchType;
import java.io.IOException;
import java.util.Arrays;

/** Samples for NetworkFunctions CreateOrUpdate. */
public final class NetworkFunctionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionCreate.json
     */
    /**
     * Sample code: Create network function resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createNetworkFunctionResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) throws IOException {
        manager
            .networkFunctions()
            .define("testNf")
            .withRegion("eastus")
            .withExistingResourceGroup("rg")
            .withDevice(
                new SubResource()
                    .withId(
                        "/subscriptions/subid/resourcegroups/rg/providers/Microsoft.HybridNetwork/devices/testDevice"))
            .withSkuName("testSku")
            .withVendorName("testVendor")
            .withManagedApplicationParameters(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON))
            .withNetworkFunctionUserConfigurations(
                Arrays
                    .asList(
                        new NetworkFunctionUserConfiguration()
                            .withRoleName("testRole")
                            .withUserDataParameters(
                                SerializerFactory
                                    .createDefaultManagementSerializerAdapter()
                                    .deserialize("{}", Object.class, SerializerEncoding.JSON))
                            .withNetworkInterfaces(
                                Arrays
                                    .asList(
                                        new NetworkInterface()
                                            .withNetworkInterfaceName("nic1")
                                            .withMacAddress("")
                                            .withIpConfigurations(
                                                Arrays
                                                    .asList(
                                                        new NetworkInterfaceIpConfiguration()
                                                            .withIpAllocationMethod(IpAllocationMethod.DYNAMIC)
                                                            .withIpAddress("")
                                                            .withSubnet("")
                                                            .withGateway("")
                                                            .withIpVersion(IpVersion.IPV4)))
                                            .withVmSwitchType(VMSwitchType.MANAGEMENT),
                                        new NetworkInterface()
                                            .withNetworkInterfaceName("nic2")
                                            .withMacAddress("DC-97-F8-79-16-7D")
                                            .withIpConfigurations(
                                                Arrays
                                                    .asList(
                                                        new NetworkInterfaceIpConfiguration()
                                                            .withIpAllocationMethod(IpAllocationMethod.DYNAMIC)
                                                            .withIpAddress("")
                                                            .withSubnet("")
                                                            .withGateway("")
                                                            .withIpVersion(IpVersion.IPV4)))
                                            .withVmSwitchType(VMSwitchType.WAN)))))
            .create();
    }
}
```

### NetworkFunctions_Delete

```java
import com.azure.core.util.Context;

/** Samples for NetworkFunctions Delete. */
public final class NetworkFunctionsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionDelete.json
     */
    /**
     * Sample code: Delete network function resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteNetworkFunctionResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().delete("rg", "testNf", Context.NONE);
    }
}
```

### NetworkFunctions_ExecuteRequest

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.hybridnetwork.models.ExecuteRequestParameters;
import com.azure.resourcemanager.hybridnetwork.models.HttpMethod;
import com.azure.resourcemanager.hybridnetwork.models.RequestMetadata;

/** Samples for NetworkFunctions ExecuteRequest. */
public final class NetworkFunctionsExecuteRequestSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionsExecuteRequest.json
     */
    /**
     * Sample code: Send request to network function services.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void sendRequestToNetworkFunctionServices(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager
            .networkFunctions()
            .executeRequest(
                "rg",
                "testNetworkfunction",
                new ExecuteRequestParameters()
                    .withServiceEndpoint("serviceEndpoint")
                    .withRequestMetadata(
                        new RequestMetadata()
                            .withRelativePath("/simProfiles/testSimProfile")
                            .withHttpMethod(HttpMethod.POST)
                            .withSerializedBody(
                                "{\"subscriptionProfile\":\"ChantestSubscription15\",\"permanentKey\":\"00112233445566778899AABBCCDDEEFF\",\"opcOperatorCode\":\"63bfa50ee6523365ff14c1f45f88737d\",\"staticIpAddresses\":{\"internet\":{\"ipv4Addr\":\"198.51.100.1\",\"ipv6Prefix\":\"2001:db8:abcd:12::0/64\"},\"another_network\":{\"ipv6Prefix\":\"2001:111:cdef:22::0/64\"}}}")
                            .withApiVersion("apiVersionQueryString")),
                Context.NONE);
    }
}
```

### NetworkFunctions_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for NetworkFunctions GetByResourceGroup. */
public final class NetworkFunctionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionGet.json
     */
    /**
     * Sample code: Get network function resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkFunctionResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().getByResourceGroupWithResponse("rg", "testNf", Context.NONE);
    }
}
```

### NetworkFunctions_List

```java
import com.azure.core.util.Context;

/** Samples for NetworkFunctions List. */
public final class NetworkFunctionsListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionListBySubscription.json
     */
    /**
     * Sample code: List all network function resources in subscription.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllNetworkFunctionResourcesInSubscription(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().list(Context.NONE);
    }
}
```

### NetworkFunctions_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for NetworkFunctions ListByResourceGroup. */
public final class NetworkFunctionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionListByResourceGroup.json
     */
    /**
     * Sample code: List network function in resource group.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listNetworkFunctionInResourceGroup(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().listByResourceGroup("rg", Context.NONE);
    }
}
```

### NetworkFunctions_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunction;
import java.util.HashMap;
import java.util.Map;

/** Samples for NetworkFunctions UpdateTags. */
public final class NetworkFunctionsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/NetworkFunctionUpdateTags.json
     */
    /**
     * Sample code: Update tags for network function resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateTagsForNetworkFunctionResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        NetworkFunction resource =
            manager.networkFunctions().getByResourceGroupWithResponse("rg", "testNf", Context.NONE).getValue();
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
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/GetOperations.json
     */
    /**
     * Sample code: Get Registration Operations.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getRegistrationOperations(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### RoleInstances_Get

```java
import com.azure.core.util.Context;

/** Samples for RoleInstances Get. */
public final class RoleInstancesGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/RoleInstanceGet.json
     */
    /**
     * Sample code: Get the operational state of role instance of vendor network function.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getTheOperationalStateOfRoleInstanceOfVendorNetworkFunction(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.roleInstances().getWithResponse("eastus", "testVendor", "testServiceKey", "mrm", Context.NONE);
    }
}
```

### RoleInstances_List

```java
import com.azure.core.util.Context;

/** Samples for RoleInstances List. */
public final class RoleInstancesListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/RoleInstanceListByVendorNetworkFunction.json
     */
    /**
     * Sample code: List all role instances of vendor network function.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllRoleInstancesOfVendorNetworkFunction(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.roleInstances().list("eastus", "testVendor", "testServiceKey", Context.NONE);
    }
}
```

### RoleInstances_Restart

```java
import com.azure.core.util.Context;

/** Samples for RoleInstances Restart. */
public final class RoleInstancesRestartSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/RoleInstanceRestart.json
     */
    /**
     * Sample code: Restart a role instance of a vendor network function.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void restartARoleInstanceOfAVendorNetworkFunction(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.roleInstances().restart("eastus", "testVendor", "testServiceKey", "mrm", Context.NONE);
    }
}
```

### RoleInstances_Start

```java
import com.azure.core.util.Context;

/** Samples for RoleInstances Start. */
public final class RoleInstancesStartSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/RoleInstanceStart.json
     */
    /**
     * Sample code: Start a role instance of a vendor network function.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void startARoleInstanceOfAVendorNetworkFunction(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.roleInstances().start("eastus", "testVendor", "testServiceKey", "mrm", Context.NONE);
    }
}
```

### RoleInstances_Stop

```java
import com.azure.core.util.Context;

/** Samples for RoleInstances Stop. */
public final class RoleInstancesStopSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/RoleInstanceStop.json
     */
    /**
     * Sample code: Stop a role instance of a vendor network function.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void stopARoleInstanceOfAVendorNetworkFunction(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.roleInstances().stop("eastus", "testVendor", "testServiceKey", "mrm", Context.NONE);
    }
}
```

### VendorNetworkFunctions_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.IpAllocationMethod;
import com.azure.resourcemanager.hybridnetwork.models.IpVersion;
import com.azure.resourcemanager.hybridnetwork.models.LinuxConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionVendorConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.NetworkInterface;
import com.azure.resourcemanager.hybridnetwork.models.NetworkInterfaceIpConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.OsProfile;
import com.azure.resourcemanager.hybridnetwork.models.SshConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.SshPublicKey;
import com.azure.resourcemanager.hybridnetwork.models.VMSwitchType;
import com.azure.resourcemanager.hybridnetwork.models.VendorProvisioningState;
import java.util.Arrays;

/** Samples for VendorNetworkFunctions CreateOrUpdate. */
public final class VendorNetworkFunctionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorNfCreate.json
     */
    /**
     * Sample code: Create or update vendor network function sub resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateVendorNetworkFunctionSubResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager
            .vendorNetworkFunctions()
            .define("testServiceKey")
            .withExistingVendor("eastus", "testVendor")
            .withVendorProvisioningState(VendorProvisioningState.PROVISIONING)
            .withNetworkFunctionVendorConfigurations(
                Arrays
                    .asList(
                        new NetworkFunctionVendorConfiguration()
                            .withRoleName("testRole")
                            .withOsProfile(
                                new OsProfile()
                                    .withAdminUsername("dummyuser")
                                    .withLinuxConfiguration(
                                        new LinuxConfiguration()
                                            .withSsh(
                                                new SshConfiguration()
                                                    .withPublicKeys(
                                                        Arrays
                                                            .asList(
                                                                new SshPublicKey()
                                                                    .withPath("home/user/.ssh/authorized_keys")
                                                                    .withKeyData(
                                                                        "ssh-rsa"
                                                                            + " AAAAB3NzaC1yc2EAAAABIwAAAgEAwrr66r8n6B8Y0zMF3dOpXEapIQD9DiYQ6D6/zwor9o39jSkHNiMMER/GETBbzP83LOcekm02aRjo55ArO7gPPVvCXbrirJu9pkm4AC4BBre5xSLS="
                                                                            + " user@constoso-DSH")))))
                                    .withCustomData("base-64 encoded string of custom data"))
                            .withNetworkInterfaces(
                                Arrays
                                    .asList(
                                        new NetworkInterface()
                                            .withNetworkInterfaceName("nic1")
                                            .withMacAddress("")
                                            .withIpConfigurations(
                                                Arrays
                                                    .asList(
                                                        new NetworkInterfaceIpConfiguration()
                                                            .withIpAllocationMethod(IpAllocationMethod.DYNAMIC)
                                                            .withIpAddress("")
                                                            .withSubnet("")
                                                            .withGateway("")
                                                            .withIpVersion(IpVersion.IPV4)))
                                            .withVmSwitchType(VMSwitchType.MANAGEMENT),
                                        new NetworkInterface()
                                            .withNetworkInterfaceName("nic2")
                                            .withMacAddress("DC-97-F8-79-16-7D")
                                            .withIpConfigurations(
                                                Arrays
                                                    .asList(
                                                        new NetworkInterfaceIpConfiguration()
                                                            .withIpAllocationMethod(IpAllocationMethod.DYNAMIC)
                                                            .withIpAddress("")
                                                            .withSubnet("")
                                                            .withGateway("")
                                                            .withIpVersion(IpVersion.IPV4)))
                                            .withVmSwitchType(VMSwitchType.WAN)))))
            .create();
    }
}
```

### VendorNetworkFunctions_Get

```java
import com.azure.core.util.Context;

/** Samples for VendorNetworkFunctions Get. */
public final class VendorNetworkFunctionsGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorNfGet.json
     */
    /**
     * Sample code: Get vendor network function sub resource by service key of network function.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getVendorNetworkFunctionSubResourceByServiceKeyOfNetworkFunction(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorNetworkFunctions().getWithResponse("eastus", "testVendor", "testServiceKey", Context.NONE);
    }
}
```

### VendorNetworkFunctions_List

```java
import com.azure.core.util.Context;

/** Samples for VendorNetworkFunctions List. */
public final class VendorNetworkFunctionsListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorNfListByVendor.json
     */
    /**
     * Sample code: List all nfs of vendor resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllNfsOfVendorResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorNetworkFunctions().list("eastus", "testVendor", null, Context.NONE);
    }
}
```

### VendorSkuPreview_CreateOrUpdate

```java
/** Samples for VendorSkuPreview CreateOrUpdate. */
public final class VendorSkuPreviewCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorSkuPreviewCreate.json
     */
    /**
     * Sample code: Create or update preview subscription of vendor sku sub resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdatePreviewSubscriptionOfVendorSkuSubResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorSkuPreviews().define("previewSub").withExistingVendorSku("TestVendor", "TestSku").create();
    }
}
```

### VendorSkuPreview_Delete

```java
import com.azure.core.util.Context;

/** Samples for VendorSkuPreview Delete. */
public final class VendorSkuPreviewDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorSkuPreviewDelete.json
     */
    /**
     * Sample code: Delete preview subscription of vendor sku sub resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deletePreviewSubscriptionOfVendorSkuSubResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorSkuPreviews().delete("TestVendor", "TestSku", "previewSub", Context.NONE);
    }
}
```

### VendorSkuPreview_Get

```java
import com.azure.core.util.Context;

/** Samples for VendorSkuPreview Get. */
public final class VendorSkuPreviewGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorSkuPreviewGet.json
     */
    /**
     * Sample code: Get preview subscription of vendor sku sub resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getPreviewSubscriptionOfVendorSkuSubResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorSkuPreviews().getWithResponse("TestVendor", "TestSku", "previewSub", Context.NONE);
    }
}
```

### VendorSkuPreview_List

```java
import com.azure.core.util.Context;

/** Samples for VendorSkuPreview List. */
public final class VendorSkuPreviewListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorSkuPreviewListBySku.json
     */
    /**
     * Sample code: List all preview subscriptions of vendor sku sub resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllPreviewSubscriptionsOfVendorSkuSubResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorSkuPreviews().list("TestVendor", "TestSku", Context.NONE);
    }
}
```

### VendorSkus_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.hybridnetwork.models.CustomProfile;
import com.azure.resourcemanager.hybridnetwork.models.DataDisk;
import com.azure.resourcemanager.hybridnetwork.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.hybridnetwork.models.ImageReference;
import com.azure.resourcemanager.hybridnetwork.models.IpAllocationMethod;
import com.azure.resourcemanager.hybridnetwork.models.IpVersion;
import com.azure.resourcemanager.hybridnetwork.models.LinuxConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionRoleConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionRoleConfigurationType;
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionTemplate;
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionType;
import com.azure.resourcemanager.hybridnetwork.models.NetworkInterface;
import com.azure.resourcemanager.hybridnetwork.models.NetworkInterfaceIpConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.OperatingSystemTypes;
import com.azure.resourcemanager.hybridnetwork.models.OsDisk;
import com.azure.resourcemanager.hybridnetwork.models.OsProfile;
import com.azure.resourcemanager.hybridnetwork.models.SkuDeploymentMode;
import com.azure.resourcemanager.hybridnetwork.models.SshConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.SshPublicKey;
import com.azure.resourcemanager.hybridnetwork.models.StorageProfile;
import com.azure.resourcemanager.hybridnetwork.models.VMSwitchType;
import com.azure.resourcemanager.hybridnetwork.models.VirtualHardDisk;
import com.azure.resourcemanager.hybridnetwork.models.VirtualMachineSizeTypes;
import java.io.IOException;
import java.util.Arrays;

/** Samples for VendorSkus CreateOrUpdate. */
public final class VendorSkusCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorSkuCreate.json
     */
    /**
     * Sample code: Create or update the sku of vendor resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateTheSkuOfVendorResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) throws IOException {
        manager
            .vendorSkus()
            .define("TestSku")
            .withExistingVendor("TestVendor")
            .withDeploymentMode(SkuDeploymentMode.PRIVATE_EDGE_ZONE)
            .withNetworkFunctionType(NetworkFunctionType.VIRTUAL_NETWORK_FUNCTION)
            .withPreview(true)
            .withManagedApplicationTemplate(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON))
            .withNetworkFunctionTemplate(
                new NetworkFunctionTemplate()
                    .withNetworkFunctionRoleConfigurations(
                        Arrays
                            .asList(
                                new NetworkFunctionRoleConfiguration()
                                    .withRoleName("test")
                                    .withRoleType(NetworkFunctionRoleConfigurationType.VIRTUAL_MACHINE)
                                    .withVirtualMachineSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                                    .withOsProfile(
                                        new OsProfile()
                                            .withAdminUsername("dummyuser")
                                            .withLinuxConfiguration(
                                                new LinuxConfiguration()
                                                    .withSsh(
                                                        new SshConfiguration()
                                                            .withPublicKeys(
                                                                Arrays
                                                                    .asList(
                                                                        new SshPublicKey()
                                                                            .withPath("home/user/.ssh/authorized_keys")
                                                                            .withKeyData(
                                                                                "ssh-rsa"
                                                                                    + " AAAAB3NzaC1yc2EAAAABIwAAAgEAwrr66r8n6B8Y0zMF3dOpXEapIQD9DiYQ6D6/zwor9o39jSkHNiMMER/GETBbzP83LOcekm02aRjo55ArO7gPPVvCXbrirJu9pkm4AC4BBre5xSLS="
                                                                                    + " user@constoso-DSH")))))
                                            .withCustomData("base-64 encoded string of custom data"))
                                    .withNetworkInterfaces(
                                        Arrays
                                            .asList(
                                                new NetworkInterface()
                                                    .withNetworkInterfaceName("nic1")
                                                    .withMacAddress("")
                                                    .withIpConfigurations(
                                                        Arrays
                                                            .asList(
                                                                new NetworkInterfaceIpConfiguration()
                                                                    .withIpAllocationMethod(IpAllocationMethod.DYNAMIC)
                                                                    .withIpAddress("")
                                                                    .withSubnet("")
                                                                    .withGateway("")
                                                                    .withIpVersion(IpVersion.IPV4)))
                                                    .withVmSwitchType(VMSwitchType.WAN),
                                                new NetworkInterface()
                                                    .withNetworkInterfaceName("nic2")
                                                    .withMacAddress("")
                                                    .withIpConfigurations(
                                                        Arrays
                                                            .asList(
                                                                new NetworkInterfaceIpConfiguration()
                                                                    .withIpAllocationMethod(IpAllocationMethod.DYNAMIC)
                                                                    .withIpAddress("")
                                                                    .withSubnet("")
                                                                    .withGateway("")
                                                                    .withIpVersion(IpVersion.IPV4)))
                                                    .withVmSwitchType(VMSwitchType.MANAGEMENT)))
                                    .withStorageProfile(
                                        new StorageProfile()
                                            .withImageReference(
                                                new ImageReference()
                                                    .withPublisher("Canonical")
                                                    .withOffer("UbuntuServer")
                                                    .withSku("18.04-LTS")
                                                    .withVersion("18.04.201804262"))
                                            .withOsDisk(
                                                new OsDisk()
                                                    .withOsType(OperatingSystemTypes.LINUX)
                                                    .withName("vhdName")
                                                    .withVhd(
                                                        new VirtualHardDisk()
                                                            .withUri(
                                                                "https://contoso.net/link/vnd.vhd?sp=rl&st=2020-10-08T20:38:19Z&se=2020-12-09T19:38:00Z&sv=2019-12-12&sr=b&sig=7BM2f4yOw%3D"))
                                                    .withDiskSizeGB(30))
                                            .withDataDisks(
                                                Arrays
                                                    .asList(
                                                        new DataDisk()
                                                            .withCreateOption(DiskCreateOptionTypes.EMPTY)
                                                            .withName("DataDisk1")
                                                            .withDiskSizeGB(10))))
                                    .withCustomProfile(
                                        new CustomProfile().withMetadataConfigurationPath("/var/logs/network.cfg")))))
            .create();
    }
}
```

### VendorSkus_Delete

```java
import com.azure.core.util.Context;

/** Samples for VendorSkus Delete. */
public final class VendorSkusDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorSkuDelete.json
     */
    /**
     * Sample code: Delete the sku of vendor resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteTheSkuOfVendorResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorSkus().delete("TestVendor", "TestSku", Context.NONE);
    }
}
```

### VendorSkus_Get

```java
import com.azure.core.util.Context;

/** Samples for VendorSkus Get. */
public final class VendorSkusGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorSkuGet.json
     */
    /**
     * Sample code: Get the sku of vendor resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getTheSkuOfVendorResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorSkus().getWithResponse("TestVendor", "TestSku", Context.NONE);
    }
}
```

### VendorSkus_List

```java
import com.azure.core.util.Context;

/** Samples for VendorSkus List. */
public final class VendorSkusListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorSkuListByVendor.json
     */
    /**
     * Sample code: List all the vendor skus of vendor resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllTheVendorSkusOfVendorResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorSkus().list("TestVendor", Context.NONE);
    }
}
```

### VendorSkus_ListCredential

```java
import com.azure.core.util.Context;

/** Samples for VendorSkus ListCredential. */
public final class VendorSkusListCredentialSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorSkuListCredential.json
     */
    /**
     * Sample code: Generate a credential for vendor sku.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void generateACredentialForVendorSku(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendorSkus().listCredentialWithResponse("TestVendor", "TestSku", Context.NONE);
    }
}
```

### Vendors_CreateOrUpdate

```java
/** Samples for Vendors CreateOrUpdate. */
public final class VendorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorCreate.json
     */
    /**
     * Sample code: Create or update Vendor resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateVendorResource(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendors().define("TestVendor").create();
    }
}
```

### Vendors_Delete

```java
import com.azure.core.util.Context;

/** Samples for Vendors Delete. */
public final class VendorsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorDelete.json
     */
    /**
     * Sample code: Delete vendor resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteVendorResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendors().delete("TestVendor", Context.NONE);
    }
}
```

### Vendors_Get

```java
import com.azure.core.util.Context;

/** Samples for Vendors Get. */
public final class VendorsGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorGet.json
     */
    /**
     * Sample code: Get Vendor resource.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getVendorResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendors().getWithResponse("TestVendor", Context.NONE);
    }
}
```

### Vendors_List

```java
import com.azure.core.util.Context;

/** Samples for Vendors List. */
public final class VendorsListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/preview/2022-01-01-preview/examples/VendorListBySubscription.json
     */
    /**
     * Sample code: List all vendor resources in subscription.
     *
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllVendorResourcesInSubscription(
        com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.vendors().list(Context.NONE);
    }
}
```

