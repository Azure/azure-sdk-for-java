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
     * x-ms-original-file: 2026-09-06-preview/BulkCreate_Cancel_MaximumSet_Gen.json
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
import com.azure.resourcemanager.compute.bulkactions.models.AllocationStrategy;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateProperties;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateVmSizeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.BulkactionVMProperties;
import com.azure.resourcemanager.compute.bulkactions.models.CachingTypes;
import com.azure.resourcemanager.compute.bulkactions.models.CapacityType;
import com.azure.resourcemanager.compute.bulkactions.models.ComputeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.DeleteOptions;
import com.azure.resourcemanager.compute.bulkactions.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DistributionStrategy;
import com.azure.resourcemanager.compute.bulkactions.models.EvictionPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
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
import com.azure.resourcemanager.compute.bulkactions.models.ZoneAllocationPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ZonePreference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for BulkCreate CreateOrUpdate.
 */
public final class BulkCreateCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-06-preview/BulkCreate_CreateOrUpdate_MaximumSet_Gen.json
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
                    .withEvictionPolicy(EvictionPolicy.DELETE)
                    .withAllocationStrategy(AllocationStrategy.LOWEST_PRICE))
                .withVmSizesProfile(Arrays.asList(new BulkCreateVmSizeProfile().withName("Standard_D2s_v5").withRank(1),
                    new BulkCreateVmSizeProfile().withName("Standard_D4s_v5").withRank(2)))
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
                .withZoneAllocationPolicy(
                    new ZoneAllocationPolicy().withDistributionStrategy(DistributionStrategy.BEST_EFFORT_BALANCED)
                        .withZonePreferences(Arrays.asList(new ZonePreference().withZone("1").withRank(1),
                            new ZonePreference().withZone("2").withRank(2))))
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreate_Delete_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreate_Get_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreate_GetAsyncOperationStatus_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreate_ListByResourceGroup_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreate_ListBySubscription_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreate_VirtualMachinesGetOperationStatus_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreateCustom_Cancel_MaximumSet_Gen.json
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
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomAllocationStrategy;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomDistributionStrategy;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomOverride;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomOverrideBase;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomOverridesProfile;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomPriorityProfile;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomProperties;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomVmSizeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.BulkCreateCustomZoneAllocationPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.BulkactionVMProperties;
import com.azure.resourcemanager.compute.bulkactions.models.CachingTypes;
import com.azure.resourcemanager.compute.bulkactions.models.CapacityType;
import com.azure.resourcemanager.compute.bulkactions.models.ComputeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.DeleteOptions;
import com.azure.resourcemanager.compute.bulkactions.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.EvictionPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
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
import com.azure.resourcemanager.compute.bulkactions.models.ZonePreference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for BulkCreateCustom CreateOrUpdate.
 */
public final class BulkCreateCustomCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-06-preview/BulkCreateCustom_CreateOrUpdate_MaximumSet_Gen.json
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
                    .withEvictionPolicy(EvictionPolicy.DELETE)
                    .withAllocationStrategy(BulkCreateCustomAllocationStrategy.LOWEST_PRICE))
                .withVmSizesProfile(Arrays.asList(
                    new BulkCreateCustomVmSizeProfile().withName("Standard_D2s_v5").withRank(1),
                    new BulkCreateCustomVmSizeProfile().withName("Standard_D4s_v5")
                        .withRank(2)
                        .withOverride(new BulkCreateCustomOverrideBase()
                            .withVirtualMachineProfile(new BulkactionVMProperties().withStorageProfile(
                                new StorageProfile().withImageReference(new ImageReference().withPublisher("Canonical")
                                    .withOffer("0001-com-ubuntu-server-jammy")
                                    .withSku("22_04-lts-arm64")
                                    .withVersion("latest")))))))
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
                .withZoneAllocationPolicy(new BulkCreateCustomZoneAllocationPolicy()
                    .withDistributionStrategy(BulkCreateCustomDistributionStrategy.BEST_EFFORT_BALANCED)
                    .withZonePreferences(Arrays.asList(new ZonePreference().withZone("1").withRank(1),
                        new ZonePreference().withZone("2").withRank(2))))
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreateCustom_Delete_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreateCustom_Get_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreateCustom_GetAsyncOperationStatus_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreateCustom_ListByResourceGroup_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreateCustom_ListBySubscription_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/BulkCreateCustom_VirtualMachinesGetOperationStatus_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/OccurrenceExtension_ListOccurrenceByVms_MaximumSet_Gen.json
     */
    /**
     * Sample code: List scheduled action occurrences for a compute resource.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listScheduledActionOccurrencesForAComputeResource(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrenceExtensions()
            .listOccurrenceByVms(
                "subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVm",
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
     * x-ms-original-file: 2026-09-06-preview/Occurrences_Cancel_MaximumSet_Gen.json
     */
    /**
     * Sample code: Cancel resources in a scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void cancelResourcesInAScheduledActionOccurrence(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .cancel("rgcompute", "myScheduledAction", "67b5bada-4772-43fc-8dbb-402476d98a45",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm",
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm2")),
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
     * x-ms-original-file: 2026-09-06-preview/Occurrences_Delay_MaximumSet_Gen.json
     */
    /**
     * Sample code: Delay resources in a scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void delayResourcesInAScheduledActionOccurrence(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .delay("rgcompute", "myScheduledAction", "67b5bada-4772-43fc-8dbb-402476d98a45", new DelayRequest()
                .withDelay(OffsetDateTime.parse("2026-08-05T17:00:00.000-07:00"))
                .withResourceIds(Arrays.asList(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm",
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm2")),
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
     * x-ms-original-file: 2026-09-06-preview/Occurrences_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Get a scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        getAScheduledActionOccurrence(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .getWithResponse("rgcompute", "myScheduledAction", "67b5bada-4772-43fc-8dbb-402476d98a45",
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
     * x-ms-original-file: 2026-09-06-preview/Occurrences_ListByScheduledAction_MaximumSet_Gen.json
     */
    /**
     * Sample code: List occurrences for a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listOccurrencesForAScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences().listByScheduledAction("rgcompute", "myScheduledAction", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-06-preview/Occurrences_ListResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: List resources in a scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listResourcesInAScheduledActionOccurrence(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .listResources("rgcompute", "myScheduledAction", "67b5bada-4772-43fc-8dbb-402476d98a45",
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
     * x-ms-original-file: 2026-09-06-preview/Operations_List_MinimumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/Operations_List_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActionExtension_ListByVms_MaximumSet_Gen.json
     */
    /**
     * Sample code: List scheduled actions for a compute resource.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listScheduledActionsForAComputeResource(
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActionOperationStatus_Get_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_AttachResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: Add resources to a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void addResourcesToAScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .attachResources("rgcompute", "myScheduledAction", new ResourceAttachRequest().withResources(Arrays.asList(
                new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("admin@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(true))),
                new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm2")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("admin@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(true))))),
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_CancelNextOccurrence_MaximumSet_Gen.json
     */
    /**
     * Sample code: Cancel the next scheduled action occurrence.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void cancelTheNextScheduledActionOccurrence(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .cancelNextOccurrence("rgcompute", "myScheduledAction",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm",
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm2")),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Create or update a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void createOrUpdateAScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .define("myScheduledAction")
            .withRegion("eastus")
            .withExistingResourceGroup("rgcompute")
            .withTags(mapOf("key2102", "fakeTokenPlaceholder"))
            .withProperties(new ScheduledActionProperties().withResourceType(ResourceType.VIRTUAL_MACHINE)
                .withActionType(ScheduledActionType.START)
                .withStartTime(OffsetDateTime.parse("2025-04-17T00:23:55.281Z"))
                .withEndTime(OffsetDateTime.parse("2026-04-17T00:23:55.281Z"))
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Delete a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        deleteAScheduledAction(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().delete("rgcompute", "myScheduledAction", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_DetachResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: Remove resources from a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void removeResourcesFromAScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .detachResources("rgcompute", "myScheduledAction", new ResourceDetachRequest().withResources(Arrays.asList(
                "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm",
                "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm2")),
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_Disable_MaximumSet_Gen.json
     */
    /**
     * Sample code: Disable a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        disableAScheduledAction(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().disable("rgcompute", "myScheduledAction", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_Enable_MaximumSet_Gen.json
     */
    /**
     * Sample code: Enable a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        enableAScheduledAction(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().enable("rgcompute", "myScheduledAction", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Get a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        getAScheduledAction(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .getByResourceGroupWithResponse("rgcompute", "myScheduledAction", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: List scheduled actions in a subscription.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listScheduledActionsInASubscription(
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: List scheduled actions in a resource group.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listScheduledActionsInAResourceGroup(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().listByResourceGroup("rgcompute", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_ListResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: List resources associated with a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void listResourcesAssociatedWithAScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions().listResources("rgcompute", "myScheduledAction", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_PatchResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: Update resource settings for a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void updateResourceSettingsForAScheduledAction(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .patchResourcesWithResponse("rgcompute", "myScheduledAction", new ResourcePatchRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("admin@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(true))),
                    new ScheduledActionResourceInput().withResourceId(
                        "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/rgcompute/providers/Microsoft.Compute/virtualMachines/myVm2")
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
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_TriggerManualOccurrence_MaximumSet_Gen.json
     */
    /**
     * Sample code: Run a scheduled action immediately.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void runAScheduledActionImmediately(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .triggerManualOccurrence("rgcompute", "myScheduledAction", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_Update

```java
import com.azure.resourcemanager.compute.bulkactions.models.Language;
import com.azure.resourcemanager.compute.bulkactions.models.Month;
import com.azure.resourcemanager.compute.bulkactions.models.NotificationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.NotificationType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceType;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionType;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionUpdate;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionUpdateProperties;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionsDeadlineType;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionsExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionsRetryPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionsScheduleUpdate;
import com.azure.resourcemanager.compute.bulkactions.models.WeekDay;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ScheduledActions Update.
 */
public final class ScheduledActionsUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-06-preview/ScheduledActions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Update a scheduled action.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        updateAScheduledAction(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .update("rgcompute", "myScheduledAction",
                new ScheduledActionUpdate().withTags(mapOf("key9989", "fakeTokenPlaceholder"))
                    .withProperties(new ScheduledActionUpdateProperties().withResourceType(ResourceType.VIRTUAL_MACHINE)
                        .withActionType(ScheduledActionType.START)
                        .withStartTime(OffsetDateTime.parse("2025-04-17T00:23:58.149Z"))
                        .withEndTime(OffsetDateTime.parse("2025-04-17T00:23:58.149Z"))
                        .withSchedule(new ScheduledActionsScheduleUpdate().withScheduledTime("19:00:00")
                            .withTimeZone("America/Los_Angeles")
                            .withRequestedWeekDays(Arrays.asList(WeekDay.MONDAY))
                            .withRequestedMonths(Arrays.asList(Month.JANUARY))
                            .withRequestedDaysOfTheMonth(Arrays.asList(15))
                            .withExecutionParameters(new ScheduledActionsExecutionParameters().withRetryPolicy(
                                new ScheduledActionsRetryPolicy().withRetryCount(17).withRetryWindowInMinutes(29)))
                            .withDeadlineType(ScheduledActionsDeadlineType.INITIATE_AT))
                        .withNotificationSettings(
                            Arrays.asList(new NotificationProperties().withDestination("admin@contoso.com")
                                .withType(NotificationType.EMAIL)
                                .withLanguage(Language.EN_US)
                                .withDisabled(true)))
                        .withDisabled(true)),
                com.azure.core.util.Context.NONE);
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

### VirtualMachineBulkOperations_BulkCancelOperations

```java
import com.azure.resourcemanager.compute.bulkactions.models.CancelOperationsContent;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkCancelOperations.
 */
public final class VirtualMachineBulkOperationsBulkCancelOperationsSamples {
    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkCancel_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkCancel_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkCancelExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkCancelOperationsWithResponse("rgBulkactions", "useast2euap",
                new CancelOperationsContent().withOperationIds(Arrays.asList("af449548-8e1a-4079-874e-2caa4ff783cc")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkCancel_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkCancel_Example - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkCancelExampleGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkCancelOperationsWithResponse("rgBulkactions", "useast2euap",
                new CancelOperationsContent().withOperationIds(Arrays.asList("af449548-8e1a-4079-874e-2caa4ff783cc")),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkDeallocateOperation

```java
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteDeallocateContent;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkDeallocateOperation.
 */
public final class VirtualMachineBulkOperationsBulkDeallocateOperationSamples {
    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkDeallocate_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkDeallocate - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkDeallocateGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkDeallocateOperationWithResponse("myResourceGroup", "eastus2euap", new ExecuteDeallocateContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkDeallocate_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkDeallocate_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkDeallocateExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkDeallocateOperationWithResponse("rgBulkactions", "useast2euap", new ExecuteDeallocateContent()
                .withExecutionParameters(new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryCount(2)
                    .withRetryWindowInMinutes(19)
                    .withOnFailureAction(ResourceOperationType.DEALLOCATE)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkDeleteOperation

```java
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteDeleteContent;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkDeleteOperation.
 */
public final class VirtualMachineBulkOperationsBulkDeleteOperationSamples {
    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkDelete_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkDelete - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkDeleteGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkDeleteOperationWithResponse("myResourceGroup", "eastus2euap", new ExecuteDeleteContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkDelete_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkDelete_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkDeleteExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkDeleteOperationWithResponse("rgBulkactions", "useast2euap", new ExecuteDeleteContent()
                .withExecutionParameters(new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryCount(2)
                    .withRetryWindowInMinutes(19)
                    .withOnFailureAction(ResourceOperationType.DELETE)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM")))
                .withForceDeletion(true), com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkGetOperationsStatus_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkGetOperationsStatus_Example - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkGetOperationsStatusExampleGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkGetOperationsStatusWithResponse("rgBulkactions", "useast2euap",
                new GetOperationStatusContent().withOperationIds(Arrays.asList("406e7856-f94b-48ae-93ee-b062afee54e5")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkGetOperationsStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkGetOperationsStatus_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkGetOperationsStatusExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkGetOperationsStatusWithResponse("rgBulkactions", "useast2euap",
                new GetOperationStatusContent().withOperationIds(Arrays.asList("406e7856-f94b-48ae-93ee-b062afee54e5")),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkHibernateOperation

```java
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteHibernateContent;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceWithContext;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import com.azure.resourcemanager.compute.bulkactions.models.ResourcesWithContext;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkHibernateOperation.
 */
public final class VirtualMachineBulkOperationsBulkHibernateOperationSamples {
    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkHibernate_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkHibernate_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkHibernateExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkHibernateOperationWithResponse("rgBulkactions", "useast2euap", new ExecuteHibernateContent()
                .withExecutionParameters(new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryCount(2)
                    .withRetryWindowInMinutes(19)
                    .withOnFailureAction(ResourceOperationType.HIBERNATE)))
                .withResourcesWithContext(
                    new ResourcesWithContext().withResources(Arrays.asList(new ResourceWithContext().withResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM")
                        .withResourceContext("hibernateContext")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkHibernate_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkHibernate - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkHibernateGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkHibernateOperationWithResponse("myResourceGroup", "eastus2euap", new ExecuteHibernateContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM"))),
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
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkReimage_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkReimage_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkReimageExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkReimageOperationWithResponse("rgBulkactions", "useast2euap", new ExecuteReimageRequest()
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(2).withRetryWindowInMinutes(19)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM")))
                .withReimageParameters(new ReimagePayload()
                    .withBaseProfile(new VirtualMachineReimageParameters().withTempDisk(true)
                        .withExactVersion("zjmkrnqjmzs")
                        .withOsProfile(new OSProfileProvisioningData().withAdminPassword("fakeTokenPlaceholder")
                            .withCustomData("teyngslcznlxihiitqbul")))
                    .withResourceOverrides(Arrays.asList(new ReimageResourceOverride().withResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM")
                        .withProfile(new VirtualMachineReimageParameters().withTempDisk(true)
                            .withExactVersion("zjmkrnqjmzs")
                            .withOsProfile(new OSProfileProvisioningData().withAdminPassword("fakeTokenPlaceholder")
                                .withCustomData("teyngslcznlxihiitqbul")))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkStartOperation

```java
import com.azure.resourcemanager.compute.bulkactions.models.CapacityRecommendationParameters;
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteStartContent;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceWithContext;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import com.azure.resourcemanager.compute.bulkactions.models.ResourcesWithContext;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkStartOperation.
 */
public final class VirtualMachineBulkOperationsBulkStartOperationSamples {
    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkStart_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkStart - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkStartGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkStartOperationWithResponse("myResourceGroup", "eastus2euap", new ExecuteStartContent()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-06-preview/VirtualMachineBulkOperations_BulkStart_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkStart_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkStartExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkStartOperationWithResponse("rgBulkactions", "useast2euap", new ExecuteStartContent()
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(2)
                        .withRetryWindowInMinutes(19)
                        .withOnFailureAction(ResourceOperationType.START))
                    .withCapacityRecommendationParameters(
                        new CapacityRecommendationParameters().withDesiredLocations(Arrays.asList("eastus", "westus2"))
                            .withDesiredSizes(Arrays.asList("Standard_D2s_v5", "Standard_D4s_v5"))
                            .withAvailabilityZones(true)))
                .withResourcesWithContext(
                    new ResourcesWithContext().withResources(Arrays.asList(new ResourceWithContext().withResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM")
                        .withResourceContext("startContext")))),
                com.azure.core.util.Context.NONE);
    }
}
```

