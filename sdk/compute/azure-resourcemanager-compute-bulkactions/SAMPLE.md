# Code snippets and samples


## BulkCreate

- [Cancel](#bulkcreate_cancel)
- [CreateOrUpdate](#bulkcreate_createorupdate)
- [Delete](#bulkcreate_delete)
- [Get](#bulkcreate_get)
- [GetAsyncOperationStatus](#bulkcreate_getasyncoperationstatus)
- [ListByResourceGroup](#bulkcreate_listbyresourcegroup)
- [ListBySubscription](#bulkcreate_listbysubscription)
- [VirtualMachinesGetOperationStatus](#bulkcreate_virtualmachinesgetoperationstatus)

## BulkCreateCustom

- [Cancel](#bulkcreatecustom_cancel)
- [CreateOrUpdate](#bulkcreatecustom_createorupdate)
- [Delete](#bulkcreatecustom_delete)
- [Get](#bulkcreatecustom_get)
- [GetAsyncOperationStatus](#bulkcreatecustom_getasyncoperationstatus)
- [ListByResourceGroup](#bulkcreatecustom_listbyresourcegroup)
- [ListBySubscription](#bulkcreatecustom_listbysubscription)
- [VirtualMachinesGetOperationStatus](#bulkcreatecustom_virtualmachinesgetoperationstatus)

## OccurrenceExtension

- [ListOccurrenceByVms](#occurrenceextension_listoccurrencebyvms)

## Occurrences

- [Cancel](#occurrences_cancel)
- [Delay](#occurrences_delay)
- [Get](#occurrences_get)
- [ListByScheduledAction](#occurrences_listbyscheduledaction)
- [ListResources](#occurrences_listresources)

## Operations

- [List](#operations_list)

## ScheduledActionExtension

- [ListByVms](#scheduledactionextension_listbyvms)

## ScheduledActionOperationStatus

- [Get](#scheduledactionoperationstatus_get)

## ScheduledActions

- [AttachResources](#scheduledactions_attachresources)
- [CancelNextOccurrence](#scheduledactions_cancelnextoccurrence)
- [CreateOrUpdate](#scheduledactions_createorupdate)
- [Delete](#scheduledactions_delete)
- [DetachResources](#scheduledactions_detachresources)
- [Disable](#scheduledactions_disable)
- [Enable](#scheduledactions_enable)
- [GetByResourceGroup](#scheduledactions_getbyresourcegroup)
- [List](#scheduledactions_list)
- [ListByResourceGroup](#scheduledactions_listbyresourcegroup)
- [ListResources](#scheduledactions_listresources)
- [PatchResources](#scheduledactions_patchresources)
- [TriggerManualOccurrence](#scheduledactions_triggermanualoccurrence)
- [Update](#scheduledactions_update)

## VirtualMachineBulkOperations

- [BulkCancelOperations](#virtualmachinebulkoperations_bulkcanceloperations)
- [BulkDeallocateOperation](#virtualmachinebulkoperations_bulkdeallocateoperation)
- [BulkDeleteOperation](#virtualmachinebulkoperations_bulkdeleteoperation)
- [BulkGetOperationsStatus](#virtualmachinebulkoperations_bulkgetoperationsstatus)
- [BulkHibernateOperation](#virtualmachinebulkoperations_bulkhibernateoperation)
- [BulkReimageOperation](#virtualmachinebulkoperations_bulkreimageoperation)
- [BulkStartOperation](#virtualmachinebulkoperations_bulkstartoperation)
### BulkCreate_Cancel

```java
/**
 * Samples for BulkCreate Cancel.
 */
public final class BulkCreateCancelSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/BulkCreate_Cancel_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreate_Cancel_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        bulkCreateCancelMaximumSet(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .cancel("rgBulkactions", "eastus", "20496756-e4bc-402c-8e7e-8ffed8e00c41",
                com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreate_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateProperties;
import com.azure.resourcemanager.compute.bulkactions.models.BulkactionVMProperties;
import com.azure.resourcemanager.compute.bulkactions.models.CachingTypes;
import com.azure.resourcemanager.compute.bulkactions.models.CapacityType;
import com.azure.resourcemanager.compute.bulkactions.models.ComputeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.DeleteOptions;
import com.azure.resourcemanager.compute.bulkactions.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.EvictionPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.HardwareProfile;
import com.azure.resourcemanager.compute.bulkactions.models.ImageReference;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedDiskParametersContent;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedServiceIdentity;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkApiVersion;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkProfile;
import com.azure.resourcemanager.compute.bulkactions.models.OSDisk;
import com.azure.resourcemanager.compute.bulkactions.models.OSProfile;
import com.azure.resourcemanager.compute.bulkactions.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.bulkactions.models.PartialFulfillmentMode;
import com.azure.resourcemanager.compute.bulkactions.models.PartialFulfillmentPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.PriorityProfile;
import com.azure.resourcemanager.compute.bulkactions.models.PriorityType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
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
 * Samples for BulkCreate CreateOrUpdate.
 */
public final class BulkCreateCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/BulkCreate_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreate_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .define("89f31926-145c-410c-a56a-5bc97359274c")
            .withExistingLocation("rgBulkactions", "eastus")
            .withTags(mapOf("workload", "batch-render", "env", "prod"))
            .withProperties(new BulkCreateProperties().withCapacity(2)
                .withCapacityType(CapacityType.VM)
                .withMinCapacity(1)
                .withPartialFulfillmentPolicy(new PartialFulfillmentPolicy().withMode(PartialFulfillmentMode.ENABLED))
                .withPriorityProfile(new PriorityProfile().withType(PriorityType.SPOT)
                    .withMaxPricePerVM(0.2D)
                    .withEvictionPolicy(EvictionPolicy.DELETE))
                .withComputeProfile(new ComputeProfile().withVirtualMachineProfile(new BulkactionVMProperties()
                    .withStorageProfile(new StorageProfile()
                        .withImageReference(new ImageReference().withPublisher("Canonical")
                            .withOffer("0001-com-ubuntu-server-jammy")
                            .withSku("22_04-lts-gen2")
                            .withVersion("latest"))
                        .withOsDisk(new OSDisk().withOsType(OperatingSystemTypes.LINUX)
                            .withCaching(CachingTypes.READ_WRITE)
                            .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                            .withManagedDisk(new ManagedDiskParametersContent()
                                .withStorageAccountType(StorageAccountTypes.PREMIUM_LRS))
                            .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                    .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D2s_v5"))
                    .withOsProfile(new OSProfile().withComputerName("bulkvm")
                        .withAdminUsername("azureuser")
                        .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                            .withSsh(new SshConfiguration().withPublicKeys(
                                Arrays.asList(new SshPublicKey().withPath("/home/azureuser/.ssh/authorized_keys")
                                    .withKeyData("fakeTokenPlaceholder"))))))
                    .withNetworkProfile(new NetworkProfile()
                        .withNetworkApiVersion(NetworkApiVersion.TWO_ZERO_TWO_ZERO_ONE_ONE_ZERO_ONE)
                        .withNetworkInterfaceConfigurations(
                            Arrays.asList(new VirtualMachineNetworkInterfaceConfiguration().withName("bulkvm-nic")
                                .withProperties(new VirtualMachineNetworkInterfaceConfigurationProperties()
                                    .withPrimary(true)
                                    .withDeleteOption(DeleteOptions.DELETE)
                                    .withIpConfigurations(Arrays.asList(
                                        new VirtualMachineNetworkInterfaceIPConfiguration().withName("bulkvm-ipconfig")
                                            .withProperties(
                                                new VirtualMachineNetworkInterfaceIPConfigurationProperties()
                                                    .withSubnet(new SubResource().withId(
                                                        "/subscriptions/1FBA3C66-5C9C-4391-B72F-9F52735FC9F2/resourceGroups/rgBulkactions/providers/Microsoft.Network/virtualNetworks/bulkvnet/subnets/default"))
                                                    .withPrimary(true)))))))))
                    .withComputeApiVersion("2024-11-01"))
                .withExecutionParameters(new ExecutionParameters().withRetryPolicy(
                    new RetryPolicy().withRetryWindowInMinutes(30).withOnFailureAction(ResourceOperationType.DELETE))))
            .withZones(Arrays.asList("1", "2"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### BulkCreate_Delete

```java
/**
 * Samples for BulkCreate Delete.
 */
public final class BulkCreateDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/BulkCreate_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreate_Delete_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        bulkCreateDeleteMaximumSet(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .delete("rgBulkactions", "eastus", "709c2556-6a82-45ee-ba68-b935bb4e8ba0", true,
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreate_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreate_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        bulkCreateGetMaximumSet(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .getWithResponse("rgBulkactions", "eastus", "85c374f7-9857-4fd7-9267-81019219c362",
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreate_GetAsyncOperationStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreate_GetAsyncOperationStatus_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateGetAsyncOperationStatusMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .getAsyncOperationStatusWithResponse("eastus", "f1ac145b-9d8b-417d-8101-9962d03c0904",
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreate_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreate_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateListByResourceGroupMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates().listByResourceGroup("rgBulkactions", "eastus", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreate_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreate_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateListBySubscriptionMaximumSet(
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreate_VirtualMachinesGetOperationStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreate_VirtualMachinesGetOperationStatus_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateVirtualMachinesGetOperationStatusMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreates()
            .virtualMachinesGetOperationStatus("local-test-rg", "eastus", "00000000-0000-0000-0000-000000000102",
                com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreateCustom_Cancel

```java
/**
 * Samples for BulkCreateCustom Cancel.
 */
public final class BulkCreateCustomCancelSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/BulkCreateCustom_Cancel_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreateCustom_Cancel_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateCustomCancelMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .cancel("rgBulkactions", "eastus", "20496756-e4bc-402c-8e7e-8ffed8e00c41",
                com.azure.core.util.Context.NONE);
    }
}
```

### BulkCreateCustom_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomOverride;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomOverridesProfile;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomPriorityProfile;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomProperties;
import com.azure.resourcemanager.compute.bulkactions.models.BulkactionVMProperties;
import com.azure.resourcemanager.compute.bulkactions.models.CachingTypes;
import com.azure.resourcemanager.compute.bulkactions.models.CapacityType;
import com.azure.resourcemanager.compute.bulkactions.models.ComputeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.DeleteOptions;
import com.azure.resourcemanager.compute.bulkactions.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.EvictionPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.HardwareProfile;
import com.azure.resourcemanager.compute.bulkactions.models.ImageReference;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedDiskParametersContent;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedServiceIdentity;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkApiVersion;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkProfile;
import com.azure.resourcemanager.compute.bulkactions.models.OSDisk;
import com.azure.resourcemanager.compute.bulkactions.models.OSProfile;
import com.azure.resourcemanager.compute.bulkactions.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.bulkactions.models.PartialFulfillmentMode;
import com.azure.resourcemanager.compute.bulkactions.models.PartialFulfillmentPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.PriorityType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
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
 * Samples for BulkCreateCustom CreateOrUpdate.
 */
public final class BulkCreateCustomCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/BulkCreateCustom_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreateCustom_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateCustomCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .define("89f31926-145c-410c-a56a-5bc97359274c")
            .withExistingLocation("rgBulkactions", "eastus")
            .withTags(mapOf("workload", "batch-render", "env", "prod"))
            .withProperties(new BulkCreateCustomProperties().withCapacity(2)
                .withCapacityType(CapacityType.VM)
                .withMinCapacity(1)
                .withPartialFulfillmentPolicy(new PartialFulfillmentPolicy().withMode(PartialFulfillmentMode.ENABLED))
                .withPriorityProfile(new BulkCreateCustomPriorityProfile().withType(PriorityType.SPOT)
                    .withMaxPricePerVM(0.2D)
                    .withEvictionPolicy(EvictionPolicy.DELETE))
                .withComputeProfile(new ComputeProfile().withVirtualMachineProfile(new BulkactionVMProperties()
                    .withStorageProfile(new StorageProfile()
                        .withImageReference(new ImageReference().withPublisher("Canonical")
                            .withOffer("0001-com-ubuntu-server-jammy")
                            .withSku("22_04-lts-gen2")
                            .withVersion("latest"))
                        .withOsDisk(new OSDisk().withOsType(OperatingSystemTypes.LINUX)
                            .withCaching(CachingTypes.READ_WRITE)
                            .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                            .withManagedDisk(new ManagedDiskParametersContent()
                                .withStorageAccountType(StorageAccountTypes.PREMIUM_LRS))
                            .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                    .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D2s_v5"))
                    .withOsProfile(new OSProfile().withComputerName("bulkvm")
                        .withAdminUsername("azureuser")
                        .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                            .withSsh(new SshConfiguration().withPublicKeys(
                                Arrays.asList(new SshPublicKey().withPath("/home/azureuser/.ssh/authorized_keys")
                                    .withKeyData("fakeTokenPlaceholder"))))))
                    .withNetworkProfile(new NetworkProfile()
                        .withNetworkApiVersion(NetworkApiVersion.TWO_ZERO_TWO_ZERO_ONE_ONE_ZERO_ONE)
                        .withNetworkInterfaceConfigurations(
                            Arrays.asList(new VirtualMachineNetworkInterfaceConfiguration().withName("bulkvm-nic")
                                .withProperties(new VirtualMachineNetworkInterfaceConfigurationProperties()
                                    .withPrimary(true)
                                    .withDeleteOption(DeleteOptions.DELETE)
                                    .withIpConfigurations(Arrays.asList(
                                        new VirtualMachineNetworkInterfaceIPConfiguration().withName("bulkvm-ipconfig")
                                            .withProperties(
                                                new VirtualMachineNetworkInterfaceIPConfigurationProperties()
                                                    .withSubnet(new SubResource().withId(
                                                        "/subscriptions/1FBA3C66-5C9C-4391-B72F-9F52735FC9F2/resourceGroups/rgBulkactions/providers/Microsoft.Network/virtualNetworks/bulkvnet/subnets/default"))
                                                    .withPrimary(true)))))))))
                    .withComputeApiVersion("2024-11-01"))
                .withOverridesProfile(new BulkCreateCustomOverridesProfile().withVirtualMachineNamePrefix("bulkvm")
                    .withOverrides(Arrays.asList(
                        new BulkCreateCustomOverride().withVirtualMachineName("bulkvm-payments-0")
                            .withTags(mapOf("workload", "payments", "env", "prod")),
                        new BulkCreateCustomOverride().withTags(mapOf("workload", "batch")))))
                .withExecutionParameters(new ExecutionParameters().withRetryPolicy(
                    new RetryPolicy().withRetryWindowInMinutes(30).withOnFailureAction(ResourceOperationType.DELETE))))
            .withZones(Arrays.asList("1", "2"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### BulkCreateCustom_Delete

```java
/**
 * Samples for BulkCreateCustom Delete.
 */
public final class BulkCreateCustomDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/BulkCreateCustom_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreateCustom_Delete_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateCustomDeleteMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .delete("rgBulkactions", "eastus", "709c2556-6a82-45ee-ba68-b935bb4e8ba0", true,
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreateCustom_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreateCustom_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        bulkCreateCustomGetMaximumSet(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .getWithResponse("rgBulkactions", "eastus", "85c374f7-9857-4fd7-9267-81019219c362",
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreateCustom_GetAsyncOperationStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreateCustom_GetAsyncOperationStatus_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateCustomGetAsyncOperationStatusMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .getAsyncOperationStatusWithResponse("eastus", "f1ac145b-9d8b-417d-8101-9962d03c0904",
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreateCustom_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreateCustom_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateCustomListByResourceGroupMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms().listByResourceGroup("rgBulkactions", "eastus", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreateCustom_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreateCustom_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateCustomListBySubscriptionMaximumSet(
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
     * x-ms-original-file: 2026-10-06-preview/BulkCreateCustom_VirtualMachinesGetOperationStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: BulkCreateCustom_VirtualMachinesGetOperationStatus_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void bulkCreateCustomVirtualMachinesGetOperationStatusMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.bulkCreateCustoms()
            .virtualMachinesGetOperationStatus("local-test-rg", "eastus", "00000000-0000-0000-0000-000000000102",
                com.azure.core.util.Context.NONE);
    }
}
```

### OccurrenceExtension_ListOccurrenceByVms

```java
/**
 * Samples for OccurrenceExtension ListOccurrenceByVms.
 */
public final class OccurrenceExtensionListOccurrenceByVmsSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/OccurrenceExtension_ListOccurrenceByVms_BasicSuccess.json
     */
    /**
     * Sample code: List recurring scheduled action occurrences for a virtual machine.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listRecurringScheduledActionOccurrencesForAVirtualMachine(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrenceExtensions()
            .listOccurrenceByVms(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_Cancel

```java
import com.azure.resourcemanager.compute.bulkactions.models.CancelOccurrenceRequest;
import java.util.Arrays;

/**
 * Samples for Occurrences Cancel.
 */
public final class OccurrencesCancelSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_Cancel_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Cancel operations in a recurring scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroOneSpaceHyphenMinusSpaceCancelSpaceoperationsSpaceinSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .cancel("example-rg", "weekday-start", "77777777-7777-7777-7777-777777777777",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_Cancel_EntireOccurrenceSuccess.json
     */
    /**
     * Sample code: 02 - Cancel all operations in a recurring scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceCancelSpaceallSpaceoperationsSpaceinSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .cancel("example-rg", "weekday-start", "77777777-7777-7777-7777-777777777777",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList()), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_Cancel_PartialSuccess.json
     */
    /**
     * Sample code: 03 - Response with partial results when canceling resources in a recurring scheduled action
     * occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceResponseSpacewithSpacepartialSpaceresultsSpacewhenSpacecancelingSpaceresourcesSpaceinSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .cancel("example-rg", "weekday-start", "77777777-7777-7777-7777-777777777777",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_Delay

```java
import com.azure.resourcemanager.compute.bulkactions.models.DelayRequest;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for Occurrences Delay.
 */
public final class OccurrencesDelaySamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_Delay_PartialSuccess.json
     */
    /**
     * Sample code: 03 - Response with partial success results when delaying operations in a recurring scheduled action
     * occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceResponseSpacewithSpacepartialSpacesuccessSpaceresultsSpacewhenSpacedelayingSpaceoperationsSpaceinSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .delay("example-rg", "weekday-start", "77777777-7777-7777-7777-777777777777", new DelayRequest()
                .withDelay(OffsetDateTime.parse("2026-09-15T09:00:00-07:00"))
                .withResourceIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_Delay_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Delay operations in a recurring scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroOneSpaceHyphenMinusSpaceDelaySpaceoperationsSpaceinSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .delay("example-rg", "weekday-start", "77777777-7777-7777-7777-777777777777", new DelayRequest()
                .withDelay(OffsetDateTime.parse("2026-09-15T09:00:00-07:00"))
                .withResourceIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_Delay_EntireOccurrenceSuccess.json
     */
    /**
     * Sample code: 02 - Delay all operations in a recurring scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceDelaySpaceallSpaceoperationsSpaceinSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .delay("example-rg", "weekday-start", "77777777-7777-7777-7777-777777777777",
                new DelayRequest().withDelay(OffsetDateTime.parse("2026-09-15T09:00:00-07:00"))
                    .withResourceIds(Arrays.asList()),
                com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_Get

```java
/**
 * Samples for Occurrences Get.
 */
public final class OccurrencesGetSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_Get_ComprehensiveSuccess.json
     */
    /**
     * Sample code: 02 - Read a recurring scheduled action occurrence with mixed results.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceReadSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrenceSpacewithSpacemixedSpaceresults(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .getWithResponse("example-rg", "weekday-start", "88888888-8888-8888-8888-888888888888",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_Get_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Read a recurring scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceReadSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .getWithResponse("example-rg", "weekday-start", "77777777-7777-7777-7777-777777777777",
                com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_ListByScheduledAction

```java
/**
 * Samples for Occurrences ListByScheduledAction.
 */
public final class OccurrencesListByScheduledActionSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_ListByScheduledAction_BasicSuccess.json
     */
    /**
     * Sample code: 01 - List recurring scheduled action occurrences.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceListSpacerecurringSpacescheduledSpaceactionSpaceoccurrences(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences().listByScheduledAction("example-rg", "weekday-start", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_ListByScheduledAction_PagedSuccess.json
     */
    /**
     * Sample code: 02 - List a page of recurring scheduled action occurrences.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceListSpaceaSpacepageSpaceofSpacerecurringSpacescheduledSpaceactionSpaceoccurrences(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences().listByScheduledAction("example-rg", "weekday-start", com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_ListResources

```java
/**
 * Samples for Occurrences ListResources.
 */
public final class OccurrencesListResourcesSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_ListResources_PagedSuccess.json
     */
    /**
     * Sample code: 02 - List a page of resources in a recurring scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceListSpaceaSpacepageSpaceofSpaceresourcesSpaceinSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .listResources("example-rg", "weekday-start", "88888888-8888-8888-8888-888888888888",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/Occurrences_ListResources_BasicSuccess.json
     */
    /**
     * Sample code: 01 - List resources in a recurring scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroOneSpaceHyphenMinusSpaceListSpaceresourcesSpaceinSpaceaSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .listResources("example-rg", "weekday-start", "77777777-7777-7777-7777-777777777777",
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
     * x-ms-original-file: 2026-10-06-preview/Operations_List_MinimumSet_Gen.json
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
     * x-ms-original-file: 2026-10-06-preview/Operations_List_MaximumSet_Gen copy.json
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
     * x-ms-original-file: 2026-10-06-preview/Operations_List_MaximumSet_Gen.json
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

### ScheduledActionExtension_ListByVms

```java
/**
 * Samples for ScheduledActionExtension ListByVms.
 */
public final class ScheduledActionExtensionListByVmsSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActionExtension_ListByVms_MaximumSet_Gen.json
     */
    /**
     * Sample code: List recurring scheduled actions for a VM.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listRecurringScheduledActionsForAVM(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActionExtensions()
            .listByVms(
                "subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVm",
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActionOperationStatus_Get

```java
/**
 * Samples for ScheduledActionOperationStatus Get.
 */
public final class ScheduledActionOperationStatusGetSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActionOperationStatus_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Get scheduled action operation status.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void getScheduledActionOperationStatus(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActionOperationStatus()
            .getWithResponse("eastus", "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_AttachResources

```java
import com.azure.resourcemanager.compute.bulkactions.models.Language;
import com.azure.resourcemanager.compute.bulkactions.models.NotificationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.NotificationType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceAttachRequest;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionResourceInput;
import java.util.Arrays;

/**
 * Samples for ScheduledActions AttachResources.
 */
public final class ScheduledActionsAttachResourcesSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_AttachResources_ComprehensiveSuccess.json
     */
    /**
     * Sample code: 02 - Attach resources to a recurring scheduled action with individual notification settings.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceAttachSpaceresourcesSpacetoSpaceaSpacerecurringSpacescheduledSpaceactionSpacewithSpaceindividualSpacenotificationSpacesettings(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .attachResources("example-rg", "weekday-start", new ResourceAttachRequest().withResources(Arrays.asList(
                new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("web-operations@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(false))),
                new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")
                    .withNotificationSettings(Arrays.asList(
                        new NotificationProperties().withDestination("service-owners@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(false),
                        new NotificationProperties().withDestination("audit@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(true))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_AttachResources_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Attach resources to a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroOneSpaceHyphenMinusSpaceAttachSpaceresourcesSpacetoSpaceaSpacerecurringSpacescheduledSpaceaction(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .attachResources("example-rg", "weekday-start", new ResourceAttachRequest().withResources(Arrays.asList(
                new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01"),
                new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_AttachResources_PartialSuccess.json
     */
    /**
     * Sample code: 03 - Response with partial results when attaching resources to a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceResponseSpacewithSpacepartialSpaceresultsSpacewhenSpaceattachingSpaceresourcesSpacetoSpaceaSpacerecurringSpacescheduledSpaceaction(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .attachResources("example-rg", "weekday-start", new ResourceAttachRequest().withResources(Arrays.asList(
                new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01"),
                new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_CancelNextOccurrence

```java
import com.azure.resourcemanager.compute.bulkactions.models.CancelOccurrenceRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions CancelNextOccurrence.
 */
public final class ScheduledActionsCancelNextOccurrenceSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_CancelNextOccurrence_PartialSuccess.json
     */
    /**
     * Sample code: 03 - Response with partial results when canceling the next recurring scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceResponseSpacewithSpacepartialSpaceresultsSpacewhenSpacecancelingSpacetheSpacenextSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .cancelNextOccurrence("example-rg", "weekday-start",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_CancelNextOccurrence_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Cancel the next recurring scheduled action occurrence for multiple resources.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroOneSpaceHyphenMinusSpaceCancelSpacetheSpacenextSpacerecurringSpacescheduledSpaceactionSpaceoccurrenceSpaceforSpacemultipleSpaceresources(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .cancelNextOccurrence("example-rg", "weekday-start",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_CancelNextOccurrence_EntireOccurrenceSuccess.json
     */
    /**
     * Sample code: 02 - Cancel all operations in the next recurring scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceCancelSpaceallSpaceoperationsSpaceinSpacetheSpacenextSpacerecurringSpacescheduledSpaceactionSpaceoccurrence(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .cancelNextOccurrence("example-rg", "weekday-start",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList()), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_CreateOrUpdate

```java
import com.azure.resourcemanager.compute.bulkactions.models.Language;
import com.azure.resourcemanager.compute.bulkactions.models.Month;
import com.azure.resourcemanager.compute.bulkactions.models.NotificationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.NotificationType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceType;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionProperties;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionType;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionsSchedule;
import com.azure.resourcemanager.compute.bulkactions.models.WeekDay;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ScheduledActions CreateOrUpdate.
 */
public final class ScheduledActionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_CreateOrUpdate_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Create a new recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceCreateSpaceaSpacenewSpacerecurringSpacescheduledSpaceaction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .define("weekday-start")
            .withRegion("eastus")
            .withExistingResourceGroup("example-rg")
            .withProperties(new ScheduledActionProperties().withResourceType(ResourceType.VIRTUAL_MACHINE)
                .withActionType(ScheduledActionType.START)
                .withStartTime(OffsetDateTime.parse("2026-09-15T07:00:00-07:00"))
                .withSchedule(new ScheduledActionsSchedule().withScheduledTime("07:00:00")
                    .withTimeZone("America/Los_Angeles")
                    .withRequestedWeekDays(Arrays.asList(WeekDay.MONDAY, WeekDay.TUESDAY, WeekDay.WEDNESDAY,
                        WeekDay.THURSDAY, WeekDay.FRIDAY)))
                .withNotificationSettings(Arrays.asList()))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_CreateOrUpdate_ComprehensiveSuccess.json
     */
    /**
     * Sample code: 02 - Create a recurring scheduled action with comprehensive settings.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceCreateSpaceaSpacerecurringSpacescheduledSpaceactionSpacewithSpacecomprehensiveSpacesettings(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .define("first-fifteenth-start")
            .withRegion("eastus")
            .withExistingResourceGroup("example-rg")
            .withTags(mapOf("environment", "production"))
            .withProperties(new ScheduledActionProperties().withResourceType(ResourceType.VIRTUAL_MACHINE)
                .withActionType(ScheduledActionType.START)
                .withStartTime(OffsetDateTime.parse("2026-09-01T19:00:00-07:00"))
                .withEndTime(OffsetDateTime.parse("2027-09-01T19:00:00-07:00"))
                .withSchedule(new ScheduledActionsSchedule().withScheduledTime("19:00:00")
                    .withTimeZone("America/Los_Angeles")
                    .withRequestedWeekDays(Arrays.asList(WeekDay.ALL))
                    .withRequestedMonths(Arrays.asList(Month.ALL))
                    .withRequestedDaysOfTheMonth(Arrays.asList(1, 15)))
                .withNotificationSettings(
                    Arrays.asList(new NotificationProperties().withDestination("admin@contoso.com")
                        .withType(NotificationType.EMAIL)
                        .withLanguage(Language.EN_US)
                        .withDisabled(true)))
                .withDisabled(false))
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

### ScheduledActions_Delete

```java
/**
 * Samples for ScheduledActions Delete.
 */
public final class ScheduledActionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_Delete_BasicSuccess.json
     */
    /**
     * Sample code: Delete a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void deleteARecurringScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().delete("example-rg", "weekday-start", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_DetachResources

```java
import com.azure.resourcemanager.compute.bulkactions.models.ResourceDetachRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions DetachResources.
 */
public final class ScheduledActionsDetachResourcesSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_DetachResources_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Detach resources from a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroOneSpaceHyphenMinusSpaceDetachSpaceresourcesSpacefromSpaceaSpacerecurringSpacescheduledSpaceaction(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .detachResources("example-rg", "weekday-start", new ResourceDetachRequest().withResources(Arrays.asList(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01",
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_DetachResources_PartialSuccess.json
     */
    /**
     * Sample code: 02 - Detach resources from a recurring scheduled action with partial success.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceDetachSpaceresourcesSpacefromSpaceaSpacerecurringSpacescheduledSpaceactionSpacewithSpacepartialSpacesuccess(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .detachResources("example-rg", "weekday-start", new ResourceDetachRequest().withResources(Arrays.asList(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01",
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_Disable

```java
/**
 * Samples for ScheduledActions Disable.
 */
public final class ScheduledActionsDisableSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_Disable_BasicSuccess.json
     */
    /**
     * Sample code: Disable a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void disableARecurringScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().disable("example-rg", "weekday-start", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_Enable

```java
/**
 * Samples for ScheduledActions Enable.
 */
public final class ScheduledActionsEnableSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_Enable_BasicSuccess.json
     */
    /**
     * Sample code: Enable a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void enableARecurringScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().enable("example-rg", "weekday-start", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_GetByResourceGroup

```java
/**
 * Samples for ScheduledActions GetByResourceGroup.
 */
public final class ScheduledActionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_Get_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Get a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceGetSpaceaSpacerecurringSpacescheduledSpaceaction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .getByResourceGroupWithResponse("example-rg", "weekday-start", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_Get_ComprehensiveSuccess.json
     */
    /**
     * Sample code: 02 - Get a recurring scheduled action with complete configuration.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceGetSpaceaSpacerecurringSpacescheduledSpaceactionSpacewithSpacecompleteSpaceconfiguration(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .getByResourceGroupWithResponse("example-rg", "weekday-start", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_List

```java
/**
 * Samples for ScheduledActions List.
 */
public final class ScheduledActionsListSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_ListBySubscription_PagedSuccess.json
     */
    /**
     * Sample code: List a page of recurring scheduled actions in a subscription.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listAPageOfRecurringScheduledActionsInASubscription(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().list(com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_ListByResourceGroup

```java
/**
 * Samples for ScheduledActions ListByResourceGroup.
 */
public final class ScheduledActionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_ListByResourceGroup_PagedSuccess.json
     */
    /**
     * Sample code: List a page of recurring scheduled actions in a resource group.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listAPageOfRecurringScheduledActionsInAResourceGroup(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().listByResourceGroup("example-rg", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_ListResources

```java
/**
 * Samples for ScheduledActions ListResources.
 */
public final class ScheduledActionsListResourcesSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_ListResources_PagedSuccess.json
     */
    /**
     * Sample code: List a page of resources associated with a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listAPageOfResourcesAssociatedWithARecurringScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().listResources("example-rg", "weekday-start", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_PatchResources

```java
import com.azure.resourcemanager.compute.bulkactions.models.Language;
import com.azure.resourcemanager.compute.bulkactions.models.NotificationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.NotificationType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourcePatchRequest;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionResourceInput;
import java.util.Arrays;

/**
 * Samples for ScheduledActions PatchResources.
 */
public final class ScheduledActionsPatchResourcesSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_PatchResources_ComprehensiveSuccess.json
     */
    /**
     * Sample code: 02 - Update resource-specific notification settings for a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceUpdateSpaceresourceHyphenMinusspecificSpacenotificationSpacesettingsSpaceforSpaceaSpacerecurringSpacescheduledSpaceaction(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .patchResourcesWithResponse("example-rg", "weekday-start", new ResourcePatchRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("web-operations@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(false))),
                    new ScheduledActionResourceInput().withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")
                        .withNotificationSettings(Arrays.asList(
                            new NotificationProperties().withDestination("service-owners@contoso.com")
                                .withType(NotificationType.EMAIL)
                                .withLanguage(Language.EN_US)
                                .withDisabled(false),
                            new NotificationProperties().withDestination("audit@contoso.com")
                                .withType(NotificationType.EMAIL)
                                .withLanguage(Language.EN_US)
                                .withDisabled(true))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_PatchResources_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Update settings for recurring scheduled action resources.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroOneSpaceHyphenMinusSpaceUpdateSpacesettingsSpaceforSpacerecurringSpacescheduledSpaceactionSpaceresources(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .patchResourcesWithResponse("example-rg", "weekday-start", new ResourcePatchRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("operations@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(false))),
                    new ScheduledActionResourceInput().withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")
                        .withNotificationSettings(
                            Arrays.asList(new NotificationProperties().withDestination("operations@contoso.com")
                                .withType(NotificationType.EMAIL)
                                .withLanguage(Language.EN_US)
                                .withDisabled(false))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_PatchResources_PartialSuccess.json
     */
    /**
     * Sample code: 03 - Response with partial results when updating recurring scheduled action resources.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceResponseSpacewithSpacepartialSpaceresultsSpacewhenSpaceupdatingSpacerecurringSpacescheduledSpaceactionSpaceresources(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .patchResourcesWithResponse("example-rg", "weekday-start", new ResourcePatchRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-01")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("admin@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(true))),
                    new ScheduledActionResourceInput().withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/web-vm-02")
                        .withNotificationSettings(
                            Arrays.asList(new NotificationProperties().withDestination("admin@contoso.com")
                                .withType(NotificationType.EMAIL)
                                .withLanguage(Language.EN_US)
                                .withDisabled(true))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_TriggerManualOccurrence

```java
/**
 * Samples for ScheduledActions TriggerManualOccurrence.
 */
public final class ScheduledActionsTriggerManualOccurrenceSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_TriggerManualOccurrence_BasicSuccess.json
     */
    /**
     * Sample code: Run a recurring scheduled action immediately.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void runARecurringScheduledActionImmediately(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .triggerManualOccurrence("example-rg", "weekday-start", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_Update

```java
import com.azure.resourcemanager.compute.bulkactions.models.Month;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionType;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionUpdate;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionUpdateProperties;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionsScheduleUpdate;
import com.azure.resourcemanager.compute.bulkactions.models.WeekDay;
import java.util.Arrays;

/**
 * Samples for ScheduledActions Update.
 */
public final class ScheduledActionsUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_Update_ComprehensiveSuccess.json
     */
    /**
     * Sample code: 02 - Update a recurring scheduled action schedule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroTwoSpaceHyphenMinusSpaceUpdateSpaceaSpacerecurringSpacescheduledSpaceactionSpaceschedule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .update("example-rg", "weekday-start",
                new ScheduledActionUpdate().withProperties(new ScheduledActionUpdateProperties()
                    .withSchedule(new ScheduledActionsScheduleUpdate().withScheduledTime("19:00:00")
                        .withTimeZone("America/Los_Angeles")
                        .withRequestedWeekDays(Arrays.asList(WeekDay.MONDAY))
                        .withRequestedMonths(Arrays.asList(Month.JANUARY))
                        .withRequestedDaysOfTheMonth(Arrays.asList(15)))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/ScheduledActions_Update_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Update the action type of a recurring scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroOneSpaceHyphenMinusSpaceUpdateSpacetheSpaceactionSpacetypeSpaceofSpaceaSpacerecurringSpacescheduledSpaceaction(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .update("example-rg", "weekday-start",
                new ScheduledActionUpdate().withProperties(
                    new ScheduledActionUpdateProperties().withActionType(ScheduledActionType.DEALLOCATE)),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkCancel_BasicSuccess.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkCancel_OperationNotFoundError.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkCancel_PartialSuccess.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkDeallocate_BasicSuccess.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkDeallocate_VmNotFoundError.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkDelete_VmNotFoundError.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkDelete_ForceDeleteSuccess.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkDelete_BasicSuccess.json
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
     * x-ms-original-file: 2026-10-06-preview/
     * VirtualMachineBulkOperations_BulkGetOperationsStatus_DeallocateFallbackFailedAfterHibernateFail.json
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
     * 2026-10-06-preview/VirtualMachineBulkOperations_BulkGetOperationsStatus_DeallocateFallbackAfterHibernateFail.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkGetOperationsStatus_BasicSuccess.json
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
     * x-ms-original-file:
     * 2026-10-06-preview/VirtualMachineBulkOperations_BulkGetOperationsStatus_OperationNotFoundError.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkGetOperationsStatus_FailedOperation.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkHibernate_VmNotFoundError.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkHibernate_WithFallback.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkHibernate_Basic.json
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

### VirtualMachineBulkOperations_BulkReimageOperation

```java
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteReimageRequest;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.OSProfileProvisioningData;
import com.azure.resourcemanager.compute.bulkactions.models.ReimagePayload;
import com.azure.resourcemanager.compute.bulkactions.models.ReimageResourceOverride;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineReimageParameters;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkReimageOperation.
 */
public final class VirtualMachineBulkOperationsBulkReimageOperationSamples {
    /*
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkReimage_BasicSuccess.json
     */
    /**
     * Sample code: 01 - Reimage multiple virtual machines.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void zeroOneSpaceHyphenMinusSpaceReimageSpacemultipleSpacevirtualSpacemachines(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkReimageOperationWithResponse("example-rg", "eastus", new ExecuteReimageRequest()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkReimage_WithReimagePayload.json
     */
    /**
     * Sample code: 03 - Reimage virtual machines with per-VM temporary disk settings.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroThreeSpaceHyphenMinusSpaceReimageSpacevirtualSpacemachinesSpacewithSpaceperHyphenMinusVMSpacetemporarySpacediskSpacesettings(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkReimageOperationWithResponse("example-rg", "eastus", new ExecuteReimageRequest()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/ephemeral-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/ephemeral-vm-02")))
                .withReimageParameters(new ReimagePayload()
                    .withBaseProfile(new VirtualMachineReimageParameters().withTempDisk(true))
                    .withResourceOverrides(Arrays.asList(new ReimageResourceOverride().withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/ephemeral-vm-02")
                        .withProfile(new VirtualMachineReimageParameters().withTempDisk(false))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkReimage_ComprehensiveSuccess.json
     */
    /**
     * Sample code: 02 - Reimage virtual machines with shared settings and a per-VM override.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        zeroTwoSpaceHyphenMinusSpaceReimageSpacevirtualSpacemachinesSpacewithSpacesharedSpacesettingsSpaceandSpaceaSpaceperHyphenMinusVMSpaceoverride(
            com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkReimageOperationWithResponse("example-rg", "eastus", new ExecuteReimageRequest()
                .withExecutionParameters(
                    new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryWindowInMinutes(30)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02")))
                .withReimageParameters(new ReimagePayload()
                    .withBaseProfile(new VirtualMachineReimageParameters().withTempDisk(false)
                        .withExactVersion("1.0.0")
                        .withOsProfile(new OSProfileProvisioningData()
                            .withCustomData("I2Nsb3VkLWNvbmZpZwpwYWNrYWdlX3VwZ3JhZGU6IHRydWUK")))
                    .withResourceOverrides(Arrays.asList(new ReimageResourceOverride().withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-02")
                        .withProfile(
                            new VirtualMachineReimageParameters().withTempDisk(false).withExactVersion("1.1.0"))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkReimage_VmNotFoundError.json
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
            .bulkReimageOperationWithResponse("example-rg", "eastus", new ExecuteReimageRequest()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/bulk-vm-01",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Compute/virtualMachines/missing-vm"))),
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkStart_BasicSuccess.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkStart_WithVmAgentHealthVerification.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkStart_WithCapacityRecommendations.json
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
     * x-ms-original-file: 2026-10-06-preview/VirtualMachineBulkOperations_BulkStart_VmNotFoundError.json
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

