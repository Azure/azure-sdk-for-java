# Code snippets and samples


## BulkCreate

- [Create](#bulkcreate_create)
- [Get](#bulkcreate_get)
- [GetAsyncOperationStatus](#bulkcreate_getasyncoperationstatus)
- [ListByResourceGroup](#bulkcreate_listbyresourcegroup)
- [ListBySubscription](#bulkcreate_listbysubscription)
- [VirtualMachinesGetOperationStatus](#bulkcreate_virtualmachinesgetoperationstatus)

## BulkCreateCustom

- [Create](#bulkcreatecustom_create)
- [Get](#bulkcreatecustom_get)
- [GetAsyncOperationStatus](#bulkcreatecustom_getasyncoperationstatus)
- [ListByResourceGroup](#bulkcreatecustom_listbyresourcegroup)
- [ListBySubscription](#bulkcreatecustom_listbysubscription)
- [VirtualMachinesGetOperationStatus](#bulkcreatecustom_virtualmachinesgetoperationstatus)

## Operations

- [List](#operations_list)

## VirtualMachineBulkOperations

- [BulkCancelOperations](#virtualmachinebulkoperations_bulkcanceloperations)
- [BulkDeallocateOperation](#virtualmachinebulkoperations_bulkdeallocateoperation)
- [BulkDeleteOperation](#virtualmachinebulkoperations_bulkdeleteoperation)
- [BulkGetOperationsStatus](#virtualmachinebulkoperations_bulkgetoperationsstatus)
- [BulkHibernateOperation](#virtualmachinebulkoperations_bulkhibernateoperation)
- [BulkStartOperation](#virtualmachinebulkoperations_bulkstartoperation)
### BulkCreate_Create

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.bulkactions.models.BillingProfile;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateProperties;
import com.azure.resourcemanager.compute.bulkactions.models.BulkactionVMProperties;
import com.azure.resourcemanager.compute.bulkactions.models.CapacityType;
import com.azure.resourcemanager.compute.bulkactions.models.ComputeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.DeleteOptions;
import com.azure.resourcemanager.compute.bulkactions.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.EvictionPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.HardwareProfile;
import com.azure.resourcemanager.compute.bulkactions.models.ImageReference;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxPatchAssessmentMode;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxPatchSettings;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxVMGuestPatchMode;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedDiskParametersContent;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkApiVersion;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkProfile;
import com.azure.resourcemanager.compute.bulkactions.models.OSDisk;
import com.azure.resourcemanager.compute.bulkactions.models.OSProfile;
import com.azure.resourcemanager.compute.bulkactions.models.PriorityType;
import com.azure.resourcemanager.compute.bulkactions.models.SshConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.SshPublicKey;
import com.azure.resourcemanager.compute.bulkactions.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.bulkactions.models.StorageProfile;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceConfigurationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceIPConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceIPConfigurationProperties;
import java.util.Arrays;

/**
 * Samples for BulkCreate Create.
 */
public final class BulkCreateCreateSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreate_Create.json
     */
    /**
     * Sample code: Create two Linux virtual machines with a shared profile.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void createTwoLinuxVirtualMachinesWithASharedProfile(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .define("fb5e3fe5-f509-4d7c-8f6a-fe65eeef921e")
            .withExistingLocation("rg-bulk-create", "eastus")
            .withProperties(
                new BulkCreateProperties().withCapacity(2)
                    .withCapacityType(CapacityType.VM)
                    .withComputeProfile(
                        new ComputeProfile()
                            .withVirtualMachineProfile(new BulkactionVMProperties()
                                .withStorageProfile(new StorageProfile()
                                    .withImageReference(new ImageReference().withPublisher("Canonical")
                                        .withOffer("0001-com-ubuntu-server-jammy")
                                        .withSku("22_04-lts-gen2")
                                        .withVersion("latest"))
                                    .withOsDisk(new OSDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                        .withManagedDisk(new ManagedDiskParametersContent()
                                            .withStorageAccountType(StorageAccountTypes.PREMIUM_LRS))
                                        .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                                .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D2s_v5"))
                                .withOsProfile(new OSProfile().withComputerName("batchvm")
                                    .withAdminUsername("azureuser")
                                    .withLinuxConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(Arrays
                                            .asList(new SshPublicKey().withPath("/home/azureuser/.ssh/authorized_keys")
                                                .withKeyData("fakeTokenPlaceholder"))))
                                        .withProvisionVMAgent(true)
                                        .withPatchSettings(new LinuxPatchSettings()
                                            .withPatchMode(LinuxVMGuestPatchMode.AUTOMATIC_BY_PLATFORM)
                                            .withAssessmentMode(LinuxPatchAssessmentMode.AUTOMATIC_BY_PLATFORM))))
                                .withNetworkProfile(new NetworkProfile()
                                    .withNetworkApiVersion(NetworkApiVersion.TWO_ZERO_TWO_TWO_ONE_ONE_ZERO_ONE)
                                    .withNetworkInterfaceConfigurations(Arrays
                                        .asList(new VirtualMachineNetworkInterfaceConfiguration().withName("vm-nic")
                                            .withProperties(new VirtualMachineNetworkInterfaceConfigurationProperties()
                                                .withPrimary(true)
                                                .withDeleteOption(DeleteOptions.DELETE)
                                                .withIpConfigurations(
                                                    Arrays.asList(new VirtualMachineNetworkInterfaceIPConfiguration()
                                                        .withName("ipconfig")
                                                        .withProperties(
                                                            new VirtualMachineNetworkInterfaceIPConfigurationProperties()
                                                                .withSubnet(new SubResource().withId(
                                                                    "/subscriptions/57018409-63ae-4ec4-91ef-57bafe128d74/resourceGroups/rg-bulk-create/providers/Microsoft.Network/virtualNetworks/batch-vnet/subnets/default"))
                                                                .withPrimary(true)))))))))
                            .withComputeApiVersion("2024-11-01")))
            .withZones(Arrays.asList("1"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-16/BulkCreate_Create_Spot.json
     */
    /**
     * Sample code: Create two Spot virtual machines with a shared profile.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void createTwoSpotVirtualMachinesWithASharedProfile(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .define("c06999ea-a1b6-494c-9f90-47d6e081719d")
            .withExistingLocation("rg-bulk-create", "eastus")
            .withProperties(new BulkCreateProperties().withCapacity(2)
                .withCapacityType(CapacityType.VM)
                .withComputeProfile(new ComputeProfile().withVirtualMachineProfile(new BulkactionVMProperties()
                    .withStorageProfile(new StorageProfile()
                        .withImageReference(new ImageReference().withPublisher("Canonical")
                            .withOffer("0001-com-ubuntu-server-jammy")
                            .withSku("22_04-lts-gen2")
                            .withVersion("latest"))
                        .withOsDisk(new OSDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                            .withManagedDisk(new ManagedDiskParametersContent()
                                .withStorageAccountType(StorageAccountTypes.PREMIUM_LRS))
                            .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                    .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D2s_v5"))
                    .withOsProfile(new OSProfile().withComputerName("batchvm")
                        .withAdminUsername("azureuser")
                        .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                            .withSsh(new SshConfiguration().withPublicKeys(
                                Arrays.asList(new SshPublicKey().withPath("/home/azureuser/.ssh/authorized_keys")
                                    .withKeyData("fakeTokenPlaceholder"))))
                            .withProvisionVMAgent(true)
                            .withPatchSettings(
                                new LinuxPatchSettings().withPatchMode(LinuxVMGuestPatchMode.AUTOMATIC_BY_PLATFORM)
                                    .withAssessmentMode(LinuxPatchAssessmentMode.AUTOMATIC_BY_PLATFORM))))
                    .withNetworkProfile(new NetworkProfile()
                        .withNetworkApiVersion(NetworkApiVersion.TWO_ZERO_TWO_TWO_ONE_ONE_ZERO_ONE)
                        .withNetworkInterfaceConfigurations(
                            Arrays.asList(new VirtualMachineNetworkInterfaceConfiguration().withName("vm-nic")
                                .withProperties(new VirtualMachineNetworkInterfaceConfigurationProperties()
                                    .withPrimary(true)
                                    .withDeleteOption(DeleteOptions.DELETE)
                                    .withIpConfigurations(Arrays
                                        .asList(new VirtualMachineNetworkInterfaceIPConfiguration().withName("ipconfig")
                                            .withProperties(
                                                new VirtualMachineNetworkInterfaceIPConfigurationProperties()
                                                    .withSubnet(new SubResource().withId(
                                                        "/subscriptions/57018409-63ae-4ec4-91ef-57bafe128d74/resourceGroups/rg-bulk-create/providers/Microsoft.Network/virtualNetworks/batch-vnet/subnets/default"))
                                                    .withPrimary(true))))))))
                    .withPriority(PriorityType.SPOT)
                    .withBillingProfile(new BillingProfile().withMaxPrice(-1.0D))
                    .withEvictionPolicy(EvictionPolicy.DELETE)).withComputeApiVersion("2024-11-01")))
            .withZones(Arrays.asList("1"))
            .create();
    }
}
```

### BulkCreate_Get

```java
/**
 * Samples for BulkCreate Get.
 */
public final class BulkCreateGetSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreate_Get.json
     */
    /**
     * Sample code: Get a completed bulk create operation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void getACompletedBulkCreateOperation(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .getWithResponse("rg-bulk-create", "eastus", "fb5e3fe5-f509-4d7c-8f6a-fe65eeef921e",
                com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreate_GetAsyncOperationStatus

```java
/**
 * Samples for BulkCreate GetAsyncOperationStatus.
 */
public final class BulkCreateGetAsyncOperationStatusSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreate_GetAsyncOperationStatus.json
     */
    /**
     * Sample code: Get the status of a completed bulk create operation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void getTheStatusOfACompletedBulkCreateOperation(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .getAsyncOperationStatusWithResponse("eastus", "58dcb168-d2d0-42a2-8c38-b2650682c369",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/BulkCreate_GetAsyncOperationStatus_Failed.json
     */
    /**
     * Sample code: Get the status of a failed bulk create operation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void getTheStatusOfAFailedBulkCreateOperation(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .getAsyncOperationStatusWithResponse("eastus", "cbdbac52-dad1-4c54-81e1-972427ecd140",
                com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreate_ListByResourceGroup

```java
/**
 * Samples for BulkCreate ListByResourceGroup.
 */
public final class BulkCreateListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreate_ListByResourceGroup.json
     */
    /**
     * Sample code: List bulk create operations in a resource group and region.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listBulkCreateOperationsInAResourceGroupAndRegion(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates().listByResourceGroup("rg-bulk-create", "eastus", com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreate_ListBySubscription

```java
/**
 * Samples for BulkCreate ListBySubscription.
 */
public final class BulkCreateListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreate_ListBySubscription.json
     */
    /**
     * Sample code: List bulk create operations in a subscription and region.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listBulkCreateOperationsInASubscriptionAndRegion(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates().listBySubscription("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreate_VirtualMachinesGetOperationStatus

```java
/**
 * Samples for BulkCreate VirtualMachinesGetOperationStatus.
 */
public final class BulkCreateVirtualMachinesGetOperationStatusSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreate_VirtualMachinesGetOperationStatus.json
     */
    /**
     * Sample code: Get the individual virtual machine results for a bulk create operation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void getTheIndividualVirtualMachineResultsForABulkCreateOperation(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .virtualMachinesGetOperationStatus("rg-bulk-create", "eastus", "fb5e3fe5-f509-4d7c-8f6a-fe65eeef921e",
                com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreateCustom_Create

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomOverride;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomOverridesProfile;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomProperties;
import com.azure.resourcemanager.compute.bulkactions.models.BulkactionVMProperties;
import com.azure.resourcemanager.compute.bulkactions.models.CapacityType;
import com.azure.resourcemanager.compute.bulkactions.models.ComputeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.DeleteOptions;
import com.azure.resourcemanager.compute.bulkactions.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.HardwareProfile;
import com.azure.resourcemanager.compute.bulkactions.models.ImageReference;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxPatchAssessmentMode;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxPatchSettings;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxVMGuestPatchMode;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedDiskParametersContent;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkApiVersion;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkProfile;
import com.azure.resourcemanager.compute.bulkactions.models.OSDisk;
import com.azure.resourcemanager.compute.bulkactions.models.OSProfile;
import com.azure.resourcemanager.compute.bulkactions.models.SshConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.SshPublicKey;
import com.azure.resourcemanager.compute.bulkactions.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.bulkactions.models.StorageProfile;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceConfigurationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceIPConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceIPConfigurationProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for BulkCreateCustom Create.
 */
public final class BulkCreateCustomCreateSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreateCustom_Create.json
     */
    /**
     * Sample code: Create two Linux virtual machines with individual names and tags.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void createTwoLinuxVirtualMachinesWithIndividualNamesAndTags(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .define("a1223fe4-126f-4905-a870-6d53e360cab5")
            .withExistingLocation("rg-bulk-create", "eastus")
            .withProperties(new BulkCreateCustomProperties().withCapacity(2)
                .withCapacityType(CapacityType.VM)
                .withComputeProfile(new ComputeProfile().withVirtualMachineProfile(new BulkactionVMProperties()
                    .withStorageProfile(new StorageProfile()
                        .withImageReference(new ImageReference().withPublisher("Canonical")
                            .withOffer("0001-com-ubuntu-server-jammy")
                            .withSku("22_04-lts-gen2")
                            .withVersion("latest"))
                        .withOsDisk(new OSDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                            .withManagedDisk(new ManagedDiskParametersContent()
                                .withStorageAccountType(StorageAccountTypes.PREMIUM_LRS))
                            .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                    .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D2s_v5"))
                    .withOsProfile(new OSProfile().withComputerName("batchvm")
                        .withAdminUsername("azureuser")
                        .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                            .withSsh(new SshConfiguration().withPublicKeys(
                                Arrays.asList(new SshPublicKey().withPath("/home/azureuser/.ssh/authorized_keys")
                                    .withKeyData("fakeTokenPlaceholder"))))
                            .withProvisionVMAgent(true)
                            .withPatchSettings(
                                new LinuxPatchSettings().withPatchMode(LinuxVMGuestPatchMode.AUTOMATIC_BY_PLATFORM)
                                    .withAssessmentMode(LinuxPatchAssessmentMode.AUTOMATIC_BY_PLATFORM))))
                    .withNetworkProfile(new NetworkProfile()
                        .withNetworkApiVersion(NetworkApiVersion.TWO_ZERO_TWO_TWO_ONE_ONE_ZERO_ONE)
                        .withNetworkInterfaceConfigurations(
                            Arrays.asList(new VirtualMachineNetworkInterfaceConfiguration().withName("vm-nic")
                                .withProperties(new VirtualMachineNetworkInterfaceConfigurationProperties()
                                    .withPrimary(true)
                                    .withDeleteOption(DeleteOptions.DELETE)
                                    .withIpConfigurations(Arrays
                                        .asList(new VirtualMachineNetworkInterfaceIPConfiguration().withName("ipconfig")
                                            .withProperties(
                                                new VirtualMachineNetworkInterfaceIPConfigurationProperties()
                                                    .withSubnet(new SubResource().withId(
                                                        "/subscriptions/57018409-63ae-4ec4-91ef-57bafe128d74/resourceGroups/rg-bulk-create/providers/Microsoft.Network/virtualNetworks/batch-vnet/subnets/default"))
                                                    .withPrimary(true)))))))))
                    .withComputeApiVersion("2024-11-01"))
                .withOverridesProfile(
                    new BulkCreateCustomOverridesProfile()
                        .withOverrides(Arrays.asList(
                            new BulkCreateCustomOverride().withVirtualMachineName("web-01")
                                .withVirtualMachineProfile(new BulkactionVMProperties()
                                    .withOsProfile(new OSProfile().withComputerName("web-01")))
                                .withTags(mapOf("role", "web")),
                            new BulkCreateCustomOverride().withVirtualMachineName("worker-01")
                                .withVirtualMachineProfile(new BulkactionVMProperties()
                                    .withOsProfile(new OSProfile().withComputerName("worker-01")))
                                .withTags(mapOf("role", "worker"))))))
            .withZones(Arrays.asList("1"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-16/BulkCreateCustom_Create_NamePrefix.json
     */
    /**
     * Sample code: Create virtual machines with generated names and per-VM zones.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void createVirtualMachinesWithGeneratedNamesAndPerVMZones(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .define("7a647154-67a1-4c1f-9fad-5e8db7f7155c")
            .withExistingLocation("rg-bulk-create", "eastus")
            .withProperties(new BulkCreateCustomProperties().withCapacity(2)
                .withCapacityType(CapacityType.VM)
                .withComputeProfile(new ComputeProfile().withVirtualMachineProfile(new BulkactionVMProperties()
                    .withStorageProfile(new StorageProfile()
                        .withImageReference(new ImageReference().withPublisher("Canonical")
                            .withOffer("0001-com-ubuntu-server-jammy")
                            .withSku("22_04-lts-gen2")
                            .withVersion("latest"))
                        .withOsDisk(new OSDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                            .withManagedDisk(new ManagedDiskParametersContent()
                                .withStorageAccountType(StorageAccountTypes.PREMIUM_LRS))
                            .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                    .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D2s_v5"))
                    .withOsProfile(new OSProfile().withComputerName("batchvm")
                        .withAdminUsername("azureuser")
                        .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                            .withSsh(new SshConfiguration().withPublicKeys(
                                Arrays.asList(new SshPublicKey().withPath("/home/azureuser/.ssh/authorized_keys")
                                    .withKeyData("fakeTokenPlaceholder"))))
                            .withProvisionVMAgent(true)
                            .withPatchSettings(
                                new LinuxPatchSettings().withPatchMode(LinuxVMGuestPatchMode.AUTOMATIC_BY_PLATFORM)
                                    .withAssessmentMode(LinuxPatchAssessmentMode.AUTOMATIC_BY_PLATFORM))))
                    .withNetworkProfile(new NetworkProfile()
                        .withNetworkApiVersion(NetworkApiVersion.TWO_ZERO_TWO_TWO_ONE_ONE_ZERO_ONE)
                        .withNetworkInterfaceConfigurations(
                            Arrays.asList(new VirtualMachineNetworkInterfaceConfiguration().withName("vm-nic")
                                .withProperties(new VirtualMachineNetworkInterfaceConfigurationProperties()
                                    .withPrimary(true)
                                    .withDeleteOption(DeleteOptions.DELETE)
                                    .withIpConfigurations(Arrays
                                        .asList(new VirtualMachineNetworkInterfaceIPConfiguration().withName("ipconfig")
                                            .withProperties(
                                                new VirtualMachineNetworkInterfaceIPConfigurationProperties()
                                                    .withSubnet(new SubResource().withId(
                                                        "/subscriptions/57018409-63ae-4ec4-91ef-57bafe128d74/resourceGroups/rg-bulk-create/providers/Microsoft.Network/virtualNetworks/batch-vnet/subnets/default"))
                                                    .withPrimary(true)))))))))
                    .withComputeApiVersion("2024-11-01"))
                .withOverridesProfile(
                    new BulkCreateCustomOverridesProfile().withVirtualMachineNamePrefix("batch-worker")
                        .withOverrides(Arrays.asList(
                            new BulkCreateCustomOverride().withZone("1").withTags(mapOf("partition", "first")),
                            new BulkCreateCustomOverride().withZone("2").withTags(mapOf("partition", "second"))))))
            .withZones(Arrays.asList("1"))
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

### BulkCreateCustom_Get

```java
/**
 * Samples for BulkCreateCustom Get.
 */
public final class BulkCreateCustomGetSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreateCustom_Get.json
     */
    /**
     * Sample code: Get a completed bulk create custom operation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void getACompletedBulkCreateCustomOperation(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .getWithResponse("rg-bulk-create", "eastus", "a1223fe4-126f-4905-a870-6d53e360cab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreateCustom_GetAsyncOperationStatus

```java
/**
 * Samples for BulkCreateCustom GetAsyncOperationStatus.
 */
public final class BulkCreateCustomGetAsyncOperationStatusSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreateCustom_GetAsyncOperationStatus_Failed.json
     */
    /**
     * Sample code: Get the status of a failed bulk create custom operation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void getTheStatusOfAFailedBulkCreateCustomOperation(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .getAsyncOperationStatusWithResponse("eastus", "cbdbac52-dad1-4c54-81e1-972427ecd140",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/BulkCreateCustom_GetAsyncOperationStatus.json
     */
    /**
     * Sample code: Get the status of a completed bulk create custom operation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void getTheStatusOfACompletedBulkCreateCustomOperation(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .getAsyncOperationStatusWithResponse("eastus", "58553c25-b4f4-4210-bfee-e460a04d609f",
                com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreateCustom_ListByResourceGroup

```java
/**
 * Samples for BulkCreateCustom ListByResourceGroup.
 */
public final class BulkCreateCustomListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreateCustom_ListByResourceGroup.json
     */
    /**
     * Sample code: List bulk create custom operations in a resource group and region.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listBulkCreateCustomOperationsInAResourceGroupAndRegion(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms().listByResourceGroup("rg-bulk-create", "eastus", com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreateCustom_ListBySubscription

```java
/**
 * Samples for BulkCreateCustom ListBySubscription.
 */
public final class BulkCreateCustomListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreateCustom_ListBySubscription.json
     */
    /**
     * Sample code: List bulk create custom operations in a subscription and region.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listBulkCreateCustomOperationsInASubscriptionAndRegion(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms().listBySubscription("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreateCustom_VirtualMachinesGetOperationStatus

```java
/**
 * Samples for BulkCreateCustom VirtualMachinesGetOperationStatus.
 */
public final class BulkCreateCustomVirtualMachinesGetOperationStatusSamples {
    /*
     * x-ms-original-file: 2026-10-16/BulkCreateCustom_VirtualMachinesGetOperationStatus.json
     */
    /**
     * Sample code: Get the individual virtual machine results for a bulk create custom operation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void getTheIndividualVirtualMachineResultsForABulkCreateCustomOperation(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .virtualMachinesGetOperationStatus("rg-bulk-create", "eastus", "a1223fe4-126f-4905-a870-6d53e360cab5",
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
     * x-ms-original-file: 2026-10-16/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_Minimum_Gen_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void operationsListMinimumGenExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/Operations_List_MaximumSet_Gen copy.json
     */
    /**
     * Sample code: 02 - Operations_List_MaximumSet_Gen_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroTwoSpaceHyphenMinusSpaceOperationsListMaximumSetGenExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void operationsListMaximumSetGenExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkCancelOperations

```java
import com.azure.resourcemanager.compute.bulkactions.models.CancelOperationsContent;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkCancelOperations.
 */
public final class VirtualMachineBulkOperationsBulkCancelOperationsSamples {
    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkCancel_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Cancel multiple operations.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceCancelSpacemultipleSpaceoperations(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkCancelOperationsWithResponse("example-rg", "eastus",
                new CancelOperationsContent().withOperationIds(
                    Arrays.asList("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkCancel_OperationNotFoundError.json
     */
    /**
     * Sample code: 03 - Response with an unknown operation error during cancellation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceResponseSpacewithSpaceanSpaceunknownSpaceoperationSpaceerrorSpaceduringSpacecancellation(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkCancelOperationsWithResponse("example-rg", "eastus",
                new CancelOperationsContent().withOperationIds(Arrays.asList("dddddddd-dddd-dddd-dddd-dddddddddddd")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkCancel_PartialSuccess.json
     */
    /**
     * Sample code: 02 - Response with partially successful result.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroTwoSpaceHyphenMinusSpaceResponseSpacewithSpacepartiallySpacesuccessfulSpaceresult(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkCancelOperationsWithResponse("example-rg", "eastus",
                new CancelOperationsContent().withOperationIds(
                    Arrays.asList("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "dddddddd-dddd-dddd-dddd-dddddddddddd")),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkDeallocateOperation

```java
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteDeallocateContent;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkDeallocateOperation.
 */
public final class VirtualMachineBulkOperationsBulkDeallocateOperationSamples {
    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkDeallocate_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Deallocate multiple virtual machines.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceDeallocateSpacemultipleSpacevirtualSpacemachines(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkDeallocateOperationWithResponse("example-rg", "eastus", new ExecuteDeallocateContent()
                .withExecutionParameters(
                    new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryWindowInMinutes(30)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkDeallocate_VmNotFoundError.json
     */
    /**
     * Sample code: 02 - Response when a virtual machine does not exist.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceResponseSpacewhenSpaceaSpacevirtualSpacemachineSpacedoesSpacenotSpaceexist(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkDeallocateOperationWithResponse("example-rg", "eastus", new ExecuteDeallocateContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/missing-vm"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkDeleteOperation

```java
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteDeleteContent;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkDeleteOperation.
 */
public final class VirtualMachineBulkOperationsBulkDeleteOperationSamples {
    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkDelete_VmNotFoundError.json
     */
    /**
     * Sample code: 03 - Response when a virtual machine does not exist.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceResponseSpacewhenSpaceaSpacevirtualSpacemachineSpacedoesSpacenotSpaceexist(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkDeleteOperationWithResponse("example-rg", "eastus", new ExecuteDeleteContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/missing-vm"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkDelete_ForceDeleteSuccess.json
     */
    /**
     * Sample code: 02 - Force delete multiple virtual machines.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroTwoSpaceHyphenMinusSpaceForceSpacedeleteSpacemultipleSpacevirtualSpacemachines(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkDeleteOperationWithResponse("example-rg", "eastus", new ExecuteDeleteContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02")))
                .withForceDeletion(true), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkDelete_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Delete multiple virtual machines.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceDeleteSpacemultipleSpacevirtualSpacemachines(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkDeleteOperationWithResponse("example-rg", "eastus", new ExecuteDeleteContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkGetOperationsStatus

```java
import com.azure.resourcemanager.compute.bulkactions.models.GetOperationStatusContent;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkGetOperationsStatus.
 */
public final class VirtualMachineBulkOperationsBulkGetOperationsStatusSamples {
    /*
     * x-ms-original-file:
     * 2026-10-16/VirtualMachineBulkOperations_BulkGetOperationsStatus_DeallocateFallbackFailedAfterHibernateFail.json
     */
    /**
     * Sample code: 05 - Response with failed deallocation fallback after hibernation fails.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroFiveSpaceHyphenMinusSpaceResponseSpacewithSpacefailedSpacedeallocationSpacefallbackSpaceafterSpacehibernationSpacefails(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkGetOperationsStatusWithResponse("example-rg", "eastus",
                new GetOperationStatusContent().withOperationIds(Arrays.asList("7f3c98a4-64b8-4d6a-b215-890c16d27643")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * 2026-10-16/VirtualMachineBulkOperations_BulkGetOperationsStatus_DeallocateFallbackAfterHibernateFail.json
     */
    /**
     * Sample code: 04 - Response with successful deallocation fallback after hibernation fails.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroFourSpaceHyphenMinusSpaceResponseSpacewithSpacesuccessfulSpacedeallocationSpacefallbackSpaceafterSpacehibernationSpacefails(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkGetOperationsStatusWithResponse("example-rg", "eastus",
                new GetOperationStatusContent().withOperationIds(Arrays.asList("ffffffff-ffff-ffff-ffff-ffffffffffff")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkGetOperationsStatus_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Get the status of successfully completed operations.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroOneSpaceHyphenMinusSpaceGetSpacetheSpacestatusSpaceofSpacesuccessfullySpacecompletedSpaceoperations(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkGetOperationsStatusWithResponse("example-rg", "eastus",
                new GetOperationStatusContent().withOperationIds(
                    Arrays.asList("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkGetOperationsStatus_OperationNotFoundError.json
     */
    /**
     * Sample code: 03 - Response with an operation not found error.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroThreeSpaceHyphenMinusSpaceResponseSpacewithSpaceanSpaceoperationSpacenotSpacefoundSpaceerror(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkGetOperationsStatusWithResponse("example-rg", "eastus",
                new GetOperationStatusContent().withOperationIds(
                    Arrays.asList("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "dddddddd-dddd-dddd-dddd-dddddddddddd")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkGetOperationsStatus_FailedOperation.json
     */
    /**
     * Sample code: 02 - Get the status of a failed operation.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroTwoSpaceHyphenMinusSpaceGetSpacetheSpacestatusSpaceofSpaceaSpacefailedSpaceoperation(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkGetOperationsStatusWithResponse("example-rg", "eastus",
                new GetOperationStatusContent().withOperationIds(Arrays.asList("e69c80d2-4f31-46ac-9e35-c6a7cb63fe12")),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkHibernateOperation

```java
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteHibernateContent;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkHibernateOperation.
 */
public final class VirtualMachineBulkOperationsBulkHibernateOperationSamples {
    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkHibernate_VmNotFoundError.json
     */
    /**
     * Sample code: 03 - Response when a virtual machine does not exist.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceResponseSpacewhenSpaceaSpacevirtualSpacemachineSpacedoesSpacenotSpaceexist(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkHibernateOperationWithResponse("example-rg", "eastus", new ExecuteHibernateContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/missing-vm"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkHibernate_WithFallback.json
     */
    /**
     * Sample code: 02 - Hibernate virtual machines and fallback to deallocation if hibernate is not successful.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceHibernateSpacevirtualSpacemachinesSpaceandSpacefallbackSpacetoSpacedeallocationSpaceifSpacehibernateSpaceisSpacenotSpacesuccessful(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkHibernateOperationWithResponse("example-rg", "eastus", new ExecuteHibernateContent()
                .withExecutionParameters(
                    new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryWindowInMinutes(30)
                        .withOnFailureAction(ResourceOperationType.DEALLOCATE)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkHibernate_Basic.json
     */
    /**
     * Sample code: 01 - Hibernate multiple virtual machines.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceHibernateSpacemultipleSpacevirtualSpacemachines(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkHibernateOperationWithResponse("example-rg", "eastus", new ExecuteHibernateContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkStartOperation

```java
import com.azure.resourcemanager.compute.bulkactions.models.CapacityRecommendationParameters;
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteStartContent;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkStartOperation.
 */
public final class VirtualMachineBulkOperationsBulkStartOperationSamples {
    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkStart_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Start multiple virtual machines.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceStartSpacemultipleSpacevirtualSpacemachines(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkStartOperationWithResponse("example-rg", "eastus", new ExecuteStartContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkStart_WithVmAgentHealthVerification.json
     */
    /**
     * Sample code: 02 - Start virtual machines with VM agent health verification.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceStartSpacevirtualSpacemachinesSpacewithSpaceVMSpaceagentSpacehealthSpaceverification(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkStartOperationWithResponse("example-rg", "eastus", new ExecuteStartContent()
                .withExecutionParameters(
                    new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryWindowInMinutes(30))
                        .withVerifyVmAgentHealth(true))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkStart_WithCapacityRecommendations.json
     */
    /**
     * Sample code: 03 - Start virtual machines with capacity recommendations.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceStartSpacevirtualSpacemachinesSpacewithSpacecapacitySpacerecommendations(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkStartOperationWithResponse("example-rg", "eastus", new ExecuteStartContent()
                .withExecutionParameters(
                    new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryWindowInMinutes(30))
                        .withCapacityRecommendationParameters(new CapacityRecommendationParameters()
                            .withDesiredLocations(Arrays.asList("eastus", "westus2"))
                            .withDesiredSizes(Arrays.asList("Standard_D2s_v5", "Standard_D4s_v5"))
                            .withAvailabilityZones(true)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-16/VirtualMachineBulkOperations_BulkStart_VmNotFoundError.json
     */
    /**
     * Sample code: 04 - Response when a virtual machine does not exist.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroFourSpaceHyphenMinusSpaceResponseSpacewhenSpaceaSpacevirtualSpacemachineSpacedoesSpacenotSpaceexist(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkStartOperationWithResponse("example-rg", "eastus", new ExecuteStartContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/missing-vm"))),
                com.azure.core.util.Context.NONE);
    }
}
```

