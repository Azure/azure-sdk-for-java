# Code snippets and samples


## DedicatedHsm

- [CreateOrUpdate](#dedicatedhsm_createorupdate)
- [Delete](#dedicatedhsm_delete)
- [GetByResourceGroup](#dedicatedhsm_getbyresourcegroup)
- [List](#dedicatedhsm_list)
- [ListByResourceGroup](#dedicatedhsm_listbyresourcegroup)
- [ListOutboundNetworkDependenciesEndpoints](#dedicatedhsm_listoutboundnetworkdependenciesendpoints)
- [Update](#dedicatedhsm_update)

## Operations

- [List](#operations_list)
### DedicatedHsm_CreateOrUpdate

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.ApiEntityReference;
import com.azure.resourcemanager.hardwaresecuritymodules.models.NetworkInterface;
import com.azure.resourcemanager.hardwaresecuritymodules.models.NetworkProfile;
import com.azure.resourcemanager.hardwaresecuritymodules.models.Sku;
import com.azure.resourcemanager.hardwaresecuritymodules.models.SkuName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for DedicatedHsm CreateOrUpdate. */
public final class DedicatedHsmCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/PaymentHsm_CreateOrUpdate_WithManagementProfile.json
     */
    /**
     * Sample code: Create a new or update an existing payment HSM with management profile.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void createANewOrUpdateAnExistingPaymentHSMWithManagementProfile(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager
            .dedicatedHsms()
            .define("hsm1")
            .withRegion("westus")
            .withExistingResourceGroup("hsm-group")
            .withTags(mapOf("Dept", "hsm", "Environment", "dogfood"))
            .withSku(new Sku().withName(SkuName.PAY_SHIELD10K_LMK1_CPS60))
            .withNetworkProfile(
                new NetworkProfile()
                    .withSubnet(
                        new ApiEntityReference()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/hsm-group/providers/Microsoft.Network/virtualNetworks/stamp01/subnets/stamp01"))
                    .withNetworkInterfaces(Arrays.asList(new NetworkInterface().withPrivateIpAddress("1.0.0.1"))))
            .withManagementNetworkProfile(
                new NetworkProfile()
                    .withSubnet(
                        new ApiEntityReference()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/hsm-group/providers/Microsoft.Network/virtualNetworks/stamp01/subnets/stamp01"))
                    .withNetworkInterfaces(Arrays.asList(new NetworkInterface().withPrivateIpAddress("1.0.0.2"))))
            .withStampId("stamp01")
            .create();
    }

    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/DedicatedHsm_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a new or update an existing dedicated HSM.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void createANewOrUpdateAnExistingDedicatedHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager
            .dedicatedHsms()
            .define("hsm1")
            .withRegion("westus")
            .withExistingResourceGroup("hsm-group")
            .withTags(mapOf("Dept", "hsm", "Environment", "dogfood"))
            .withSku(new Sku().withName(SkuName.SAFE_NET_LUNA_NETWORK_HSM_A790))
            .withNetworkProfile(
                new NetworkProfile()
                    .withSubnet(
                        new ApiEntityReference()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/hsm-group/providers/Microsoft.Network/virtualNetworks/stamp01/subnets/stamp01"))
                    .withNetworkInterfaces(Arrays.asList(new NetworkInterface().withPrivateIpAddress("1.0.0.1"))))
            .withStampId("stamp01")
            .create();
    }

    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/PaymentHsm_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a new or update an existing payment HSM.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void createANewOrUpdateAnExistingPaymentHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager
            .dedicatedHsms()
            .define("hsm1")
            .withRegion("westus")
            .withExistingResourceGroup("hsm-group")
            .withTags(mapOf("Dept", "hsm", "Environment", "dogfood"))
            .withSku(new Sku().withName(SkuName.PAY_SHIELD10K_LMK1_CPS60))
            .withNetworkProfile(
                new NetworkProfile()
                    .withSubnet(
                        new ApiEntityReference()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/hsm-group/providers/Microsoft.Network/virtualNetworks/stamp01/subnets/stamp01"))
                    .withNetworkInterfaces(Arrays.asList(new NetworkInterface().withPrivateIpAddress("1.0.0.1"))))
            .withStampId("stamp01")
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

### DedicatedHsm_Delete

```java
import com.azure.core.util.Context;

/** Samples for DedicatedHsm Delete. */
public final class DedicatedHsmDeleteSamples {
    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/DedicatedHsm_Delete.json
     */
    /**
     * Sample code: Delete a dedicated HSM.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void deleteADedicatedHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().delete("hsm-group", "hsm1", Context.NONE);
    }
}
```

### DedicatedHsm_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DedicatedHsm GetByResourceGroup. */
public final class DedicatedHsmGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/PaymentHsm_Get.json
     */
    /**
     * Sample code: Get a payment HSM.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void getAPaymentHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/PaymentHsm_Get_With_2018-10-31Preview_Version.json
     */
    /**
     * Sample code: Get a payment HSM with 2018-10-31Preview api version.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void getAPaymentHSMWith20181031PreviewApiVersion(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/DedicatedHsm_Get.json
     */
    /**
     * Sample code: Get a dedicated HSM.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void getADedicatedHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", Context.NONE);
    }
}
```

### DedicatedHsm_List

```java
import com.azure.core.util.Context;

/** Samples for DedicatedHsm List. */
public final class DedicatedHsmListSamples {
    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/PaymentHsm_ListBySubscription.json
     */
    /**
     * Sample code: List dedicated HSM devices in a subscription including payment HSM.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listDedicatedHSMDevicesInASubscriptionIncludingPaymentHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().list(null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/DedicatedHsm_ListBySubscription.json
     */
    /**
     * Sample code: List dedicated HSM devices in a subscription.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listDedicatedHSMDevicesInASubscription(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().list(null, Context.NONE);
    }
}
```

### DedicatedHsm_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DedicatedHsm ListByResourceGroup. */
public final class DedicatedHsmListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/DedicatedHsm_ListByResourceGroup.json
     */
    /**
     * Sample code: List dedicated HSM devices in a resource group.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listDedicatedHSMDevicesInAResourceGroup(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().listByResourceGroup("hsm-group", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/PaymentHsm_ListByResourceGroup.json
     */
    /**
     * Sample code: List dedicated HSM devices in a resource group including payment HSM.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listDedicatedHSMDevicesInAResourceGroupIncludingPaymentHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().listByResourceGroup("hsm-group", null, Context.NONE);
    }
}
```

### DedicatedHsm_ListOutboundNetworkDependenciesEndpoints

```java
import com.azure.core.util.Context;

/** Samples for DedicatedHsm ListOutboundNetworkDependenciesEndpoints. */
public final class DedicatedHsmListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/GetOutboundNetworkDependenciesEndpointsList.json
     */
    /**
     * Sample code: List OutboundNetworkDependenciesEndpoints by Managed Cluster.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listOutboundNetworkDependenciesEndpointsByManagedCluster(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().listOutboundNetworkDependenciesEndpoints("hsm-group", "hsm1", Context.NONE);
    }
}
```

### DedicatedHsm_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.hardwaresecuritymodules.models.DedicatedHsm;
import java.util.HashMap;
import java.util.Map;

/** Samples for DedicatedHsm Update. */
public final class DedicatedHsmUpdateSamples {
    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/DedicatedHsm_Update.json
     */
    /**
     * Sample code: Update an existing dedicated HSM.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void updateAnExistingDedicatedHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        DedicatedHsm resource =
            manager.dedicatedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", Context.NONE).getValue();
        resource.update().withTags(mapOf("Dept", "hsm", "Environment", "dogfood", "Slice", "A")).apply();
    }

    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/PaymentHsm_Update.json
     */
    /**
     * Sample code: Update an existing payment HSM.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void updateAnExistingPaymentHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        DedicatedHsm resource =
            manager.dedicatedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", Context.NONE).getValue();
        resource.update().withTags(mapOf("Dept", "hsm", "Environment", "dogfood", "Slice", "A")).apply();
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
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/PaymentHsm_OperationsList.json
     */
    /**
     * Sample code: Get a list of Payment HSM operations.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void getAListOfPaymentHSMOperations(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.operations().list(Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hardwaresecuritymodules/resource-manager/Microsoft.HardwareSecurityModules/stable/2021-11-30/examples/DedicatedHsm_OperationsList.json
     */
    /**
     * Sample code: Get a list of Dedicated HSM operations.
     *
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void getAListOfDedicatedHSMOperations(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

