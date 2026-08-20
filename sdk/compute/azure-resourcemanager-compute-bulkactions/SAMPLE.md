# Code snippets and samples


## BulkCreateCustom

- [Cancel](#bulkcreatecustom_cancel)
- [CreateOrUpdate](#bulkcreatecustom_createorupdate)
- [Delete](#bulkcreatecustom_delete)
- [Get](#bulkcreatecustom_get)
- [GetAsyncOperationStatus](#bulkcreatecustom_getasyncoperationstatus)
- [ListByResourceGroup](#bulkcreatecustom_listbyresourcegroup)
- [ListBySubscription](#bulkcreatecustom_listbysubscription)

## LaunchBulkInstancesOperation

- [Cancel](#launchbulkinstancesoperation_cancel)
- [CreateOrUpdate](#launchbulkinstancesoperation_createorupdate)
- [Delete](#launchbulkinstancesoperation_delete)
- [Get](#launchbulkinstancesoperation_get)
- [GetOperationStatus](#launchbulkinstancesoperation_getoperationstatus)
- [ListByResourceGroup](#launchbulkinstancesoperation_listbyresourcegroup)
- [ListBySubscription](#launchbulkinstancesoperation_listbysubscription)
- [ListVirtualMachines](#launchbulkinstancesoperation_listvirtualmachines)

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

- [BulkAcknowledgeOperationErrors](#virtualmachinebulkoperations_bulkacknowledgeoperationerrors)
- [BulkCancelOperations](#virtualmachinebulkoperations_bulkcanceloperations)
- [BulkCreateOperation](#virtualmachinebulkoperations_bulkcreateoperation)
- [BulkDeallocateOperation](#virtualmachinebulkoperations_bulkdeallocateoperation)
- [BulkDeleteOperation](#virtualmachinebulkoperations_bulkdeleteoperation)
- [BulkGetOperationsStatus](#virtualmachinebulkoperations_bulkgetoperationsstatus)
- [BulkHibernateOperation](#virtualmachinebulkoperations_bulkhibernateoperation)
- [BulkListOperationErrors](#virtualmachinebulkoperations_bulklistoperationerrors)
- [BulkReimageOperation](#virtualmachinebulkoperations_bulkreimageoperation)
- [BulkStartOperation](#virtualmachinebulkoperations_bulkstartoperation)
- [BulkVdiFlexCreateOperation](#virtualmachinebulkoperations_bulkvdiflexcreateoperation)
### BulkCreateCustom_Cancel

```java
/**
 * Samples for BulkCreateCustom Cancel.
 */
public final class BulkCreateCustomCancelSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/BulkCreateCustom_Cancel_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/BulkCreateCustom_CreateOrUpdate_MaximumSet_Gen.json
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
            .withProperties(new BulkCreateCustomProperties().withCapacity(10)
                .withCapacityType(CapacityType.VM)
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
     * x-ms-original-file: 2026-07-06-preview/BulkCreateCustom_Delete_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/BulkCreateCustom_Get_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/BulkCreateCustom_GetAsyncOperationStatus_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/BulkCreateCustom_ListByResourceGroup_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/BulkCreateCustom_ListBySubscription_MaximumSet_Gen.json
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

### LaunchBulkInstancesOperation_Cancel

```java
/**
 * Samples for LaunchBulkInstancesOperation Cancel.
 */
public final class LaunchBulkInstancesOperationCancelSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_Cancel_MaximumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_Cancel_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationCancelExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations()
            .cancel("rgBulkactions", "useast2euap", "434d5a2a-167a-4e26-a89c-fbe622dfd0bc",
                com.azure.core.util.Context.NONE);
    }
}
```

### LaunchBulkInstancesOperation_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.bulkactions.models.AcceleratorManufacturer;
import com.azure.resourcemanager.compute.bulkactions.models.AcceleratorType;
import com.azure.resourcemanager.compute.bulkactions.models.AdditionalCapabilities;
import com.azure.resourcemanager.compute.bulkactions.models.AdditionalUnattendContent;
import com.azure.resourcemanager.compute.bulkactions.models.AdditionalUnattendContentComponentName;
import com.azure.resourcemanager.compute.bulkactions.models.AdditionalUnattendContentPassName;
import com.azure.resourcemanager.compute.bulkactions.models.AllInstancesDown;
import com.azure.resourcemanager.compute.bulkactions.models.AllocationStrategy;
import com.azure.resourcemanager.compute.bulkactions.models.ApiEntityReference;
import com.azure.resourcemanager.compute.bulkactions.models.ApplicationProfile;
import com.azure.resourcemanager.compute.bulkactions.models.ArchitectureType;
import com.azure.resourcemanager.compute.bulkactions.models.BootDiagnostics;
import com.azure.resourcemanager.compute.bulkactions.models.BulkActionVmExtensionProperties;
import com.azure.resourcemanager.compute.bulkactions.models.BulkactionVMExtension;
import com.azure.resourcemanager.compute.bulkactions.models.BulkactionVMProperties;
import com.azure.resourcemanager.compute.bulkactions.models.CachingTypes;
import com.azure.resourcemanager.compute.bulkactions.models.CapacityReservationProfile;
import com.azure.resourcemanager.compute.bulkactions.models.CapacityType;
import com.azure.resourcemanager.compute.bulkactions.models.ComputeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.CpuManufacturer;
import com.azure.resourcemanager.compute.bulkactions.models.DataDisk;
import com.azure.resourcemanager.compute.bulkactions.models.DeleteOptions;
import com.azure.resourcemanager.compute.bulkactions.models.DiagnosticsProfile;
import com.azure.resourcemanager.compute.bulkactions.models.DiffDiskOptions;
import com.azure.resourcemanager.compute.bulkactions.models.DiffDiskPlacement;
import com.azure.resourcemanager.compute.bulkactions.models.DiffDiskSettings;
import com.azure.resourcemanager.compute.bulkactions.models.DiskControllerTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskDetachOptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.DiskEncryptionSetParametersContent;
import com.azure.resourcemanager.compute.bulkactions.models.DiskEncryptionSettings;
import com.azure.resourcemanager.compute.bulkactions.models.DistributionStrategy;
import com.azure.resourcemanager.compute.bulkactions.models.DomainNameLabelScopeTypes;
import com.azure.resourcemanager.compute.bulkactions.models.EncryptionIdentity;
import com.azure.resourcemanager.compute.bulkactions.models.EventGridAndResourceGraph;
import com.azure.resourcemanager.compute.bulkactions.models.EvictionPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.HardwareProfile;
import com.azure.resourcemanager.compute.bulkactions.models.HostEndpointSettings;
import com.azure.resourcemanager.compute.bulkactions.models.HyperVGeneration;
import com.azure.resourcemanager.compute.bulkactions.models.IPVersions;
import com.azure.resourcemanager.compute.bulkactions.models.ImageReference;
import com.azure.resourcemanager.compute.bulkactions.models.KeyVaultKeyReference;
import com.azure.resourcemanager.compute.bulkactions.models.KeyVaultSecretReference;
import com.azure.resourcemanager.compute.bulkactions.models.LaunchBulkInstancesOperationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxPatchAssessmentMode;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxPatchSettings;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.compute.bulkactions.models.LinuxVMGuestPatchMode;
import com.azure.resourcemanager.compute.bulkactions.models.LocalStorageDiskType;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedDiskParametersContent;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedServiceIdentity;
import com.azure.resourcemanager.compute.bulkactions.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.compute.bulkactions.models.Mode;
import com.azure.resourcemanager.compute.bulkactions.models.Modes;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkApiVersion;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkInterfaceAuxiliaryMode;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkInterfaceAuxiliarySku;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkInterfaceReference;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkInterfaceReferenceProperties;
import com.azure.resourcemanager.compute.bulkactions.models.NetworkProfile;
import com.azure.resourcemanager.compute.bulkactions.models.OSDisk;
import com.azure.resourcemanager.compute.bulkactions.models.OSImageNotificationProfile;
import com.azure.resourcemanager.compute.bulkactions.models.OSProfile;
import com.azure.resourcemanager.compute.bulkactions.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.bulkactions.models.PatchSettings;
import com.azure.resourcemanager.compute.bulkactions.models.Plan;
import com.azure.resourcemanager.compute.bulkactions.models.PriorityProfile;
import com.azure.resourcemanager.compute.bulkactions.models.PriorityType;
import com.azure.resourcemanager.compute.bulkactions.models.ProtocolTypes;
import com.azure.resourcemanager.compute.bulkactions.models.ProxyAgentSettings;
import com.azure.resourcemanager.compute.bulkactions.models.PublicIPAddressSku;
import com.azure.resourcemanager.compute.bulkactions.models.PublicIPAddressSkuName;
import com.azure.resourcemanager.compute.bulkactions.models.PublicIPAddressSkuTier;
import com.azure.resourcemanager.compute.bulkactions.models.PublicIPAllocationMethod;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledEventsAdditionalPublishingTargets;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledEventsPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledEventsProfile;
import com.azure.resourcemanager.compute.bulkactions.models.SecurityEncryptionTypes;
import com.azure.resourcemanager.compute.bulkactions.models.SecurityProfile;
import com.azure.resourcemanager.compute.bulkactions.models.SecurityTypes;
import com.azure.resourcemanager.compute.bulkactions.models.SettingNames;
import com.azure.resourcemanager.compute.bulkactions.models.SshConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.SshPublicKey;
import com.azure.resourcemanager.compute.bulkactions.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.bulkactions.models.StorageProfile;
import com.azure.resourcemanager.compute.bulkactions.models.TerminateNotificationProfile;
import com.azure.resourcemanager.compute.bulkactions.models.UefiSettings;
import com.azure.resourcemanager.compute.bulkactions.models.UserAssignedIdentity;
import com.azure.resourcemanager.compute.bulkactions.models.UserInitiatedReboot;
import com.azure.resourcemanager.compute.bulkactions.models.UserInitiatedRedeploy;
import com.azure.resourcemanager.compute.bulkactions.models.VMAttributeMinMaxDouble;
import com.azure.resourcemanager.compute.bulkactions.models.VMAttributeMinMaxInteger;
import com.azure.resourcemanager.compute.bulkactions.models.VMAttributeSupport;
import com.azure.resourcemanager.compute.bulkactions.models.VMAttributes;
import com.azure.resourcemanager.compute.bulkactions.models.VMCategory;
import com.azure.resourcemanager.compute.bulkactions.models.VMDiskSecurityProfile;
import com.azure.resourcemanager.compute.bulkactions.models.VMGalleryApplication;
import com.azure.resourcemanager.compute.bulkactions.models.VaultCertificate;
import com.azure.resourcemanager.compute.bulkactions.models.VaultSecretGroup;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualHardDisk;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineIpTag;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceConfigurationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceDnsSettingsConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceIPConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineNetworkInterfaceIPConfigurationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachinePublicIPAddressConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachinePublicIPAddressConfigurationProperties;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachinePublicIPAddressDnsSettingsConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.VmSizeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.VmSizeProperties;
import com.azure.resourcemanager.compute.bulkactions.models.WinRMConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.WinRMListener;
import com.azure.resourcemanager.compute.bulkactions.models.WindowsConfiguration;
import com.azure.resourcemanager.compute.bulkactions.models.WindowsPatchAssessmentMode;
import com.azure.resourcemanager.compute.bulkactions.models.WindowsVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.compute.bulkactions.models.WindowsVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.compute.bulkactions.models.WindowsVMGuestPatchMode;
import com.azure.resourcemanager.compute.bulkactions.models.ZoneAllocationPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ZonePreference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LaunchBulkInstancesOperation CreateOrUpdate.
 */
public final class LaunchBulkInstancesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_CreateOrUpdate_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationCreateOrUpdateExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations()
            .define("4ad45156-c829-4cd4-aafa-ecabe28aa029")
            .withExistingLocation("rgBulkactions", "useast2euap")
            .withTags(mapOf("key1909", "fakeTokenPlaceholder"))
            .withProperties(new LaunchBulkInstancesOperationProperties().withCapacity(21)
                .withCapacityType(CapacityType.VM)
                .withPriorityProfile(new PriorityProfile().withType(PriorityType.REGULAR)
                    .withMaxPricePerVM(23.0D)
                    .withEvictionPolicy(EvictionPolicy.DELETE)
                    .withAllocationStrategy(AllocationStrategy.LOWEST_PRICE))
                .withVmSizesProfile(Arrays.asList(new VmSizeProfile().withName("frbnnpdkq").withRank(7)))
                .withVmAttributes(new VMAttributes().withVCpuCount(new VMAttributeMinMaxInteger().withMin(0).withMax(0))
                    .withMemoryInGiB(new VMAttributeMinMaxDouble().withMin(0.0D).withMax(0.0D))
                    .withArchitectureTypes(Arrays.asList(ArchitectureType.ARM64))
                    .withMemoryInGiBPerVCpu(new VMAttributeMinMaxDouble().withMin(0.0D).withMax(0.0D))
                    .withLocalStorageSupport(VMAttributeSupport.EXCLUDED)
                    .withLocalStorageInGiB(new VMAttributeMinMaxDouble().withMin(0.0D).withMax(0.0D))
                    .withLocalStorageDiskTypes(Arrays.asList(LocalStorageDiskType.HDD))
                    .withDataDiskCount(new VMAttributeMinMaxInteger().withMin(0).withMax(0))
                    .withNetworkInterfaceCount(new VMAttributeMinMaxInteger().withMin(0).withMax(0))
                    .withNetworkBandwidthInMbps(new VMAttributeMinMaxDouble().withMin(0.0D).withMax(0.0D))
                    .withRdmaSupport(VMAttributeSupport.EXCLUDED)
                    .withRdmaNetworkInterfaceCount(new VMAttributeMinMaxInteger().withMin(0).withMax(0))
                    .withAcceleratorSupport(VMAttributeSupport.EXCLUDED)
                    .withAcceleratorManufacturers(Arrays.asList(AcceleratorManufacturer.AMD))
                    .withAcceleratorTypes(Arrays.asList(AcceleratorType.GPU))
                    .withAcceleratorCount(new VMAttributeMinMaxInteger().withMin(0).withMax(0))
                    .withVmCategories(Arrays.asList(VMCategory.GENERAL_PURPOSE))
                    .withCpuManufacturers(Arrays.asList(CpuManufacturer.INTEL))
                    .withHyperVGenerations(Arrays.asList(HyperVGeneration.GEN1))
                    .withBurstableSupport(VMAttributeSupport.EXCLUDED)
                    .withAllowedVMSizes(Arrays.asList("dwcsrlrzrzzqleqivkzwpczpf", "dwcsrlrzrzzqleqivkzwpczpf",
                        "dwcsrlrzrzzqleqivkzwpczpf", "dwcsrlrzrzzqleqivkzwpczpf", "dwcsrlrzrzzqleqivkzwpczpf",
                        "dwcsrlrzrzzqleqivkzwpczpf", "dwcsrlrzrzzqleqivkzwpczpf"))
                    .withExcludedVMSizes(Arrays.asList("igehpnuaybwy", "igehpnuaybwy", "igehpnuaybwy", "igehpnuaybwy",
                        "igehpnuaybwy", "igehpnuaybwy", "igehpnuaybwy")))
                .withComputeProfile(
                    new ComputeProfile()
                        .withVirtualMachineProfile(new BulkactionVMProperties()
                            .withScheduledEventsPolicy(new ScheduledEventsPolicy()
                                .withUserInitiatedRedeploy(
                                    new UserInitiatedRedeploy().withUserInitiatedRedeployAutomaticallyApprove(true))
                                .withUserInitiatedReboot(
                                    new UserInitiatedReboot().withUserInitiatedRebootAutomaticallyApprove(true))
                                .withScheduledEventsAdditionalPublishingTargets(
                                    new ScheduledEventsAdditionalPublishingTargets()
                                        .withEventGridAndResourceGraph(new EventGridAndResourceGraph().withEnable(true)
                                            .withScheduledEventsApiVersion("lifncbftlkounuyfn")))
                                .withAllInstancesDown(
                                    new AllInstancesDown().withAllInstancesDownAutomaticallyApprove(true)))
                            .withStorageProfile(new StorageProfile()
                                .withImageReference(new ImageReference()
                                    .withId("cdbrkpdicibtlliq")
                                    .withPublisher("ojlplghybdamadvsrq")
                                    .withOffer("uvnqoxhkxefqwbsvjgbswqy")
                                    .withSku("hajdxhjmlkx")
                                    .withVersion("u")
                                    .withSharedGalleryImageId("fz")
                                    .withCommunityGalleryImageId("tsfpcq"))
                                .withOsDisk(new OSDisk().withOsType(OperatingSystemTypes.WINDOWS)
                                    .withEncryptionSettings(new DiskEncryptionSettings()
                                        .withDiskEncryptionKey(
                                            new KeyVaultSecretReference().withSecretUrl("fakeTokenPlaceholder")
                                                .withSourceVault(new SubResource().withId("lvzxxyypkeqlflftmfn")))
                                        .withKeyEncryptionKey(
                                            new KeyVaultKeyReference().withKeyUrl("fakeTokenPlaceholder")
                                                .withSourceVault(new SubResource().withId("lvzxxyypkeqlflftmfn")))
                                        .withEnabled(true))
                                    .withName("opogpznvctmraoajgizcyrfvpt")
                                    .withVhd(new VirtualHardDisk().withUri("elpzggtxubepzgjqvdbjmbu"))
                                    .withImage(new VirtualHardDisk().withUri("elpzggtxubepzgjqvdbjmbu"))
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withDiffDiskSettings(new DiffDiskSettings().withOption(DiffDiskOptions.LOCAL)
                                        .withPlacement(DiffDiskPlacement.CACHE_DISK))
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiskSizeGB(2)
                                    .withManagedDisk(new ManagedDiskParametersContent().withId("numddbqmkxuu")
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(
                                            new DiskEncryptionSetParametersContent().withId("magvkzhdmzhktjlqkkk"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(new DiskEncryptionSetParametersContent()
                                                .withId("magvkzhdmzhktjlqkkk"))))
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE))
                                .withDataDisks(Arrays.asList(new DataDisk().withLun(7)
                                    .withName("nbthfzqsxyqvqnbgcljxbwyyoj")
                                    .withVhd(new VirtualHardDisk().withUri("elpzggtxubepzgjqvdbjmbu"))
                                    .withImage(new VirtualHardDisk().withUri("elpzggtxubepzgjqvdbjmbu"))
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiskSizeGB(19)
                                    .withManagedDisk(new ManagedDiskParametersContent().withId("numddbqmkxuu")
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(
                                            new DiskEncryptionSetParametersContent().withId("magvkzhdmzhktjlqkkk"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(new DiskEncryptionSetParametersContent()
                                                .withId("magvkzhdmzhktjlqkkk"))))
                                    .withSourceResource(new ApiEntityReference().withId("qnukyordmomtjjqabovlsxl"))
                                    .withToBeDetached(true)
                                    .withDetachOption(DiskDetachOptionTypes.FORCE_DETACH)
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                                .withDiskControllerType(DiskControllerTypes.SCSI))
                            .withHardwareProfile(new HardwareProfile().withVmSize("szrnjqwbruz")
                                .withVmSizeProperties(
                                    new VmSizeProperties().withVCpusAvailable(24).withVCpusPerCore(6)))
                            .withAdditionalCapabilities(
                                new AdditionalCapabilities().withUltraSSDEnabled(true).withHibernationEnabled(true))
                            .withOsProfile(
                                new OSProfile().withComputerName("bplxnfp")
                                    .withAdminUsername("fxzbi")
                                    .withAdminPassword("fakeTokenPlaceholder")
                                    .withCustomData("hbdlirohsgnbrahscboc")
                                    .withWindowsConfiguration(new WindowsConfiguration().withProvisionVMAgent(true)
                                        .withEnableAutomaticUpdates(true)
                                        .withTimeZone("t")
                                        .withAdditionalUnattendContent(Arrays.asList(new AdditionalUnattendContent()
                                            .withPassName(AdditionalUnattendContentPassName.OOBE_SYSTEM)
                                            .withComponentName(
                                                AdditionalUnattendContentComponentName.MICROSOFT_WINDOWS_SHELL_SETUP)
                                            .withSettingName(SettingNames.AUTO_LOGON)
                                            .withContent("rguazthnx")))
                                        .withPatchSettings(
                                            new PatchSettings().withPatchMode(WindowsVMGuestPatchMode.MANUAL)
                                                .withEnableHotpatching(true)
                                                .withAssessmentMode(WindowsPatchAssessmentMode.IMAGE_DEFAULT)
                                                .withAutomaticByPlatformSettings(
                                                    new WindowsVMGuestPatchAutomaticByPlatformSettings()
                                                        .withRebootSetting(
                                                            WindowsVMGuestPatchAutomaticByPlatformRebootSetting.UNKNOWN)
                                                        .withBypassPlatformSafetyChecksOnUserSchedule(true)))
                                        .withWinRM(new WinRMConfiguration().withListeners(
                                            Arrays.asList(new WinRMListener().withProtocol(ProtocolTypes.HTTP)
                                                .withCertificateUrl("quhfapfpyeeocwvwtvuggoqqwt")))))
                                    .withLinuxConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withPath("mrdfxnfjazxog")
                                                .withKeyData("fakeTokenPlaceholder"))))
                                        .withProvisionVMAgent(true)
                                        .withPatchSettings(
                                            new LinuxPatchSettings().withPatchMode(LinuxVMGuestPatchMode.IMAGE_DEFAULT)
                                                .withAssessmentMode(LinuxPatchAssessmentMode.IMAGE_DEFAULT)
                                                .withAutomaticByPlatformSettings(
                                                    new LinuxVMGuestPatchAutomaticByPlatformSettings()
                                                        .withRebootSetting(
                                                            LinuxVMGuestPatchAutomaticByPlatformRebootSetting.UNKNOWN)
                                                        .withBypassPlatformSafetyChecksOnUserSchedule(true)))
                                        .withEnableVMAgentPlatformUpdates(true))
                                    .withSecrets(Arrays.asList(new VaultSecretGroup()
                                        .withSourceVault(new SubResource().withId("lvzxxyypkeqlflftmfn"))
                                        .withVaultCertificates(Arrays
                                            .asList(new VaultCertificate().withCertificateUrl("crgbpfdvlohwkupdjp")
                                                .withCertificateStore("hyx")))))
                                    .withAllowExtensionOperations(true)
                                    .withRequireGuestProvisionSignal(true))
                            .withNetworkProfile(new NetworkProfile()
                                .withNetworkInterfaces(Arrays
                                    .asList(new NetworkInterfaceReference().withId("ymfxctb")
                                        .withProperties(new NetworkInterfaceReferenceProperties()
                                            .withPrimary(true)
                                            .withDeleteOption(DeleteOptions.DELETE))))
                                .withNetworkApiVersion(NetworkApiVersion.TWO_ZERO_TWO_ZERO_ONE_ONE_ZERO_ONE)
                                .withNetworkInterfaceConfigurations(Arrays.asList(
                                    new VirtualMachineNetworkInterfaceConfiguration().withName("qrkzoctmzjketostzabnra")
                                        .withProperties(new VirtualMachineNetworkInterfaceConfigurationProperties()
                                            .withPrimary(true)
                                            .withDeleteOption(DeleteOptions.DELETE)
                                            .withEnableAcceleratedNetworking(true)
                                            .withDisableTcpStateTracking(true)
                                            .withEnableFpga(true)
                                            .withEnableIPForwarding(true)
                                            .withNetworkSecurityGroup(new SubResource().withId("lvzxxyypkeqlflftmfn"))
                                            .withDnsSettings(
                                                new VirtualMachineNetworkInterfaceDnsSettingsConfiguration()
                                                    .withDnsServers(Arrays.asList("tqcqopnanyyiavfwhqbkarxtrfqbww")))
                                            .withIpConfigurations(
                                                Arrays.asList(new VirtualMachineNetworkInterfaceIPConfiguration()
                                                    .withName("gqymuvgzzfmxqvdadx")
                                                    .withProperties(
                                                        new VirtualMachineNetworkInterfaceIPConfigurationProperties()
                                                            .withSubnet(new SubResource().withId("lvzxxyypkeqlflftmfn"))
                                                            .withPrimary(true)
                                                            .withPublicIPAddressConfiguration(
                                                                new VirtualMachinePublicIPAddressConfiguration()
                                                                    .withName("cwxsqjijtwbsyqdwht")
                                                                    .withProperties(
                                                                        new VirtualMachinePublicIPAddressConfigurationProperties()
                                                                            .withIdleTimeoutInMinutes(17)
                                                                            .withDeleteOption(DeleteOptions.DELETE)
                                                                            .withDnsSettings(
                                                                                new VirtualMachinePublicIPAddressDnsSettingsConfiguration()
                                                                                    .withDomainNameLabel("fampou")
                                                                                    .withDomainNameLabelScope(
                                                                                        DomainNameLabelScopeTypes.TENANT_REUSE))
                                                                            .withIpTags(
                                                                                Arrays.asList(new VirtualMachineIpTag()
                                                                                    .withIpTagType(
                                                                                        "hkjoxhqadudjartwooezaxl")
                                                                                    .withTag("xywunkjglkmmwfpf")))
                                                                            .withPublicIPPrefix(new SubResource()
                                                                                .withId("lvzxxyypkeqlflftmfn"))
                                                                            .withPublicIPAddressVersion(IPVersions.IPV4)
                                                                            .withPublicIPAllocationMethod(
                                                                                PublicIPAllocationMethod.DYNAMIC))
                                                                    .withSku(new PublicIPAddressSku()
                                                                        .withName(PublicIPAddressSkuName.BASIC)
                                                                        .withTier(PublicIPAddressSkuTier.REGIONAL))
                                                                    .withTags(mapOf("key5442", "fakeTokenPlaceholder")))
                                                            .withPrivateIPAddressVersion(IPVersions.IPV4)
                                                            .withApplicationSecurityGroups(Arrays.asList(
                                                                new SubResource().withId("lvzxxyypkeqlflftmfn")))
                                                            .withApplicationGatewayBackendAddressPools(Arrays.asList(
                                                                new SubResource().withId("lvzxxyypkeqlflftmfn")))
                                                            .withLoadBalancerBackendAddressPools(Arrays.asList(
                                                                new SubResource().withId("lvzxxyypkeqlflftmfn"))))))
                                            .withDscpConfiguration(new SubResource().withId("lvzxxyypkeqlflftmfn"))
                                            .withAuxiliaryMode(NetworkInterfaceAuxiliaryMode.NONE)
                                            .withAuxiliarySku(NetworkInterfaceAuxiliarySku.NONE))
                                        .withTags(mapOf("key9436", "fakeTokenPlaceholder")))))
                            .withSecurityProfile(new SecurityProfile()
                                .withUefiSettings(new UefiSettings().withSecureBootEnabled(true).withVTpmEnabled(true))
                                .withEncryptionAtHost(true)
                                .withSecurityType(SecurityTypes.TRUSTED_LAUNCH)
                                .withEncryptionIdentity(
                                    new EncryptionIdentity().withUserAssignedIdentityResourceId("tnajlgbwcepmhytzb"))
                                .withProxyAgentSettings(new ProxyAgentSettings().withEnabled(true)
                                    .withMode(Mode.AUDIT)
                                    .withKeyIncarnationId(4)
                                    .withWireServer(new HostEndpointSettings().withMode(Modes.AUDIT)
                                        .withInVMAccessControlProfileReferenceId("xvlzroy"))
                                    .withImds(new HostEndpointSettings().withMode(Modes.AUDIT)
                                        .withInVMAccessControlProfileReferenceId("xvlzroy"))
                                    .withAddProxyAgentExtension(true)))
                            .withDiagnosticsProfile(new DiagnosticsProfile().withBootDiagnostics(
                                new BootDiagnostics().withEnabled(true).withStorageUri("pxuhtzehlfsqolbdleirgj")))
                            .withLicenseType("ymwuemwuntbignqyvzqflvjpcdus")
                            .withExtensionsTimeBudget("dnyqmcijikzkltjav")
                            .withScheduledEventsProfile(new ScheduledEventsProfile()
                                .withTerminateNotificationProfile(
                                    new TerminateNotificationProfile().withNotBeforeTimeout("owbwifqrlsdmm")
                                        .withEnable(true))
                                .withOsImageNotificationProfile(
                                    new OSImageNotificationProfile().withNotBeforeTimeout("ataqykjdakdvyyzdspaqnhd")
                                        .withEnable(true)))
                            .withUserData("nwjvxe")
                            .withCapacityReservation(new CapacityReservationProfile()
                                .withCapacityReservationGroup(new SubResource().withId("lvzxxyypkeqlflftmfn")))
                            .withApplicationProfile(
                                new ApplicationProfile()
                                    .withGalleryApplications(
                                        Arrays.asList(new VMGalleryApplication().withTags("cmygipvpkegyclvpznfu")
                                            .withOrder(8)
                                            .withPackageReferenceId("afrfkjdrtzftmwramfyu")
                                            .withConfigurationReference("nmfaspclhidtznslsps")
                                            .withTreatFailureAsDeploymentFailure(true)
                                            .withEnableAutomaticUpgrade(true))))
                            .withVmExtensions(
                                Arrays
                                    .asList(
                                        new BulkactionVMExtension().withName("jkpmcxwuahpzwkvexgzpypk")
                                            .withProperties(new BulkActionVmExtensionProperties()
                                                .withForceUpdateTag("dockqxgatsfzhctxrncuw")
                                                .withPublisher("qesyfldbfoaexyoywhcxafdtdwcg")
                                                .withType("ptlmlzpbpbkfbu")
                                                .withTypeHandlerVersion("crllsludntz")
                                                .withAutoUpgradeMinorVersion(true)
                                                .withEnableAutomaticUpgrade(true)
                                                .withSettings(mapOf())
                                                .withProtectedSettings(mapOf())
                                                .withSuppressFailures(true)
                                                .withProtectedSettingsFromKeyVault(new KeyVaultSecretReference()
                                                    .withSecretUrl("fakeTokenPlaceholder")
                                                    .withSourceVault(new SubResource().withId("lvzxxyypkeqlflftmfn")))
                                                .withProvisionAfterExtensions(Arrays.asList("onbtyoeifafiktrkmal"))))))
                        .withExtensions(
                            Arrays
                                .asList(new BulkactionVMExtension().withName("jkpmcxwuahpzwkvexgzpypk")
                                    .withProperties(new BulkActionVmExtensionProperties()
                                        .withForceUpdateTag("dockqxgatsfzhctxrncuw")
                                        .withPublisher("qesyfldbfoaexyoywhcxafdtdwcg")
                                        .withType("ptlmlzpbpbkfbu")
                                        .withTypeHandlerVersion("crllsludntz")
                                        .withAutoUpgradeMinorVersion(true)
                                        .withEnableAutomaticUpgrade(true)
                                        .withSettings(mapOf())
                                        .withProtectedSettings(mapOf())
                                        .withSuppressFailures(true)
                                        .withProtectedSettingsFromKeyVault(
                                            new KeyVaultSecretReference().withSecretUrl("fakeTokenPlaceholder")
                                                .withSourceVault(new SubResource().withId("lvzxxyypkeqlflftmfn")))
                                        .withProvisionAfterExtensions(Arrays.asList("onbtyoeifafiktrkmal")))))
                        .withComputeApiVersion("qqxldedyfmfmidkvtkixh"))
                .withZoneAllocationPolicy(
                    new ZoneAllocationPolicy().withDistributionStrategy(DistributionStrategy.BEST_EFFORT_SINGLE_ZONE)
                        .withZonePreferences(
                            Arrays.asList(new ZonePreference().withZone("ixksjnaxwelhfpsoyjfaezievquqv").withRank(19))))
                .withRetryPolicy(new RetryPolicy().withRetryCount(2)
                    .withRetryWindowInMinutes(19)
                    .withOnFailureAction(ResourceOperationType.UNKNOWN)))
            .withZones(Arrays.asList("hzqzrbvpgsudtesi"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key9643", new UserAssignedIdentity())))
            .withPlan(new Plan().withName("iemasqqkbixbewezyrhnpntjd")
                .withPublisher("bvggylbvfstnscuupuithafvvgc")
                .withProduct("bguuzrknnuohugjhernflurpx")
                .withPromotionCode("fakeTokenPlaceholder")
                .withVersion("uyxetqmmzvqianqv"))
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

### LaunchBulkInstancesOperation_Delete

```java
/**
 * Samples for LaunchBulkInstancesOperation Delete.
 */
public final class LaunchBulkInstancesOperationDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_Delete_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationDeleteExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations()
            .delete("rgBulkactions", "useast2euap", "8a71b9df-efee-48a9-a381-4e6d60b4304f", true,
                com.azure.core.util.Context.NONE);
    }
}
```

### LaunchBulkInstancesOperation_Get

```java
/**
 * Samples for LaunchBulkInstancesOperation Get.
 */
public final class LaunchBulkInstancesOperationGetSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_Get_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationGetExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations()
            .getWithResponse("rgBulkactions", "useast2euap", "495544ae-8710-4e8b-bca3-49a1dbb1623a",
                com.azure.core.util.Context.NONE);
    }
}
```

### LaunchBulkInstancesOperation_GetOperationStatus

```java
/**
 * Samples for LaunchBulkInstancesOperation GetOperationStatus.
 */
public final class LaunchBulkInstancesOperationGetOperationStatusSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_GetOperationStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_GetOperationStatus_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationGetOperationStatusExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations()
            .getOperationStatusWithResponse("useast2euap", "8596407e-8834-4a62-8d3c-9231af92d785",
                com.azure.core.util.Context.NONE);
    }
}
```

### LaunchBulkInstancesOperation_ListByResourceGroup

```java
/**
 * Samples for LaunchBulkInstancesOperation ListByResourceGroup.
 */
public final class LaunchBulkInstancesOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_ListByResourceGroup_Example - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationListByResourceGroupExampleGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations()
            .listByResourceGroup("rgBulkactions", "useast2euap", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_ListByResourceGroup_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationListByResourceGroupExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations()
            .listByResourceGroup("rgBulkactions", "useast2euap", com.azure.core.util.Context.NONE);
    }
}
```

### LaunchBulkInstancesOperation_ListBySubscription

```java
/**
 * Samples for LaunchBulkInstancesOperation ListBySubscription.
 */
public final class LaunchBulkInstancesOperationListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_ListBySubscription_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationListBySubscriptionExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations().listBySubscription("useast2euap", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_ListBySubscription_Example - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationListBySubscriptionExampleGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations().listBySubscription("useast2euap", com.azure.core.util.Context.NONE);
    }
}
```

### LaunchBulkInstancesOperation_ListVirtualMachines

```java
/**
 * Samples for LaunchBulkInstancesOperation ListVirtualMachines.
 */
public final class LaunchBulkInstancesOperationListVirtualMachinesSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/LaunchBulkInstancesOperation_ListVirtualMachines_MaximumSet_Gen.json
     */
    /**
     * Sample code: LaunchBulkInstancesOperation_ListVirtualMachines_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void launchBulkInstancesOperationListVirtualMachinesExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.launchBulkInstancesOperations()
            .listVirtualMachines("rgBulkactions", "useast2euap", "b038ec94-0860-42a5-b149-f1ce5f144e15",
                "onywxjwswbhlbkbbusgmkfgabdku", "tcbhwfqtoiwnlbjdbsnukxpgpa", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-07-06-preview/OccurrenceExtension_ListOccurrenceByVms_MaximumSet_Gen.json
     */
    /**
     * Sample code: OccurrenceExtension_ListOccurrenceByVms_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void occurrenceExtensionListOccurrenceByVmsMaximumSetGeneratedByMaximumSetRule(
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
     * x-ms-original-file: 2026-07-06-preview/Occurrences_Cancel_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_Cancel_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        occurrencesCancelMaximumSet(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .cancel("rgcompute", "myScheduledAction", "CB26D7CB-3E27-465F-99C8-EAF7A4118245",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")),
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
     * x-ms-original-file: 2026-07-06-preview/Occurrences_Delay_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_Delay_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        occurrencesDelayMaximumSet(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .delay("rgcompute", "myScheduledAction", "CB26D7CB-3E27-465F-99C8-EAF7A4118245", new DelayRequest()
                .withDelay(OffsetDateTime.parse("2025-05-22T17:00:00.000-07:00"))
                .withResourceIds(Arrays.asList(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")),
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
     * x-ms-original-file: 2026-07-06-preview/Occurrences_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        occurrencesGetMaximumSet(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
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
     * x-ms-original-file: 2026-07-06-preview/Occurrences_ListByScheduledAction_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_ListByScheduledAction_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void occurrencesListByScheduledActionMaximumSet(
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
     * x-ms-original-file: 2026-07-06-preview/Occurrences_ListResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_ListResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void occurrencesListResourcesMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.occurrences()
            .listResources("rgcompute", "myScheduledAction", "CB26D7CB-3E27-465F-99C8-EAF7A4118245",
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
     * x-ms-original-file: 2026-07-06-preview/Operations_List_MinimumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/Operations_List_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActionExtension_ListByVms_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActionExtension_ListByVms_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionExtensionListByVmsMaximumSet(
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActionOperationStatus_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActionOperationStatus_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionOperationStatusGetMaximumSet(
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_AttachResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_AttachResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsAttachResourcesMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .attachResources("rgcompute", "myScheduledAction", new ResourceAttachRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_CancelNextOccurrence_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_CancelNextOccurrence_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsCancelNextOccurrenceMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .cancelNextOccurrence("rgcompute", "myScheduledAction",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")),
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
import com.azure.resourcemanager.compute.bulkactions.models.OptimizationPreference;
import com.azure.resourcemanager.compute.bulkactions.models.RecurringScheduledActionsDeadlineType;
import com.azure.resourcemanager.compute.bulkactions.models.RecurringScheduledActionsExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.RecurringScheduledActionsRetryPolicy;
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .define("myScheduledAction")
            .withRegion("eastus")
            .withExistingResourceGroup("rgcompute")
            .withTags(mapOf("key2102", "fakeTokenPlaceholder"))
            .withProperties(
                new ScheduledActionProperties().withResourceType(ResourceType.VIRTUAL_MACHINE)
                    .withActionType(ScheduledActionType.START)
                    .withStartTime(OffsetDateTime.parse("2025-04-17T00:23:55.281Z"))
                    .withEndTime(OffsetDateTime.parse("2025-04-17T00:23:55.286Z"))
                    .withSchedule(new ScheduledActionsSchedule().withScheduledTime("19:00:00")
                        .withTimeZone("America/Los_Angeles")
                        .withRequestedWeekDays(Arrays.asList(WeekDay.MONDAY))
                        .withRequestedMonths(Arrays.asList(Month.JANUARY))
                        .withRequestedDaysOfTheMonth(Arrays.asList(15))
                        .withExecutionParameters(new RecurringScheduledActionsExecutionParameters()
                            .withOptimizationPreference(OptimizationPreference.COST)
                            .withRetryPolicy(new RecurringScheduledActionsRetryPolicy().withRetryCount(17)
                                .withRetryWindowInMinutes(29)))
                        .withDeadlineType(RecurringScheduledActionsDeadlineType.UNKNOWN))
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("admin@contoso.com")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(true)))
                    .withDisabled(true))
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Delete_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsDeleteMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_DetachResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_DetachResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsDetachResourcesMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .detachResources("rgcompute", "myScheduledAction", new ResourceDetachRequest().withResources(Arrays.asList(
                "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")),
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_Disable_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Disable_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsDisableMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_Enable_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Enable_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsEnableMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void
        scheduledActionsGetMaximumSet(com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsListBySubscriptionMaximumSet(
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsListByResourceGroupMaximumSet(
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_ListResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_ListResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsListResourcesMaximumSet(
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_PatchResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_PatchResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsPatchResourcesMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.scheduledActions()
            .patchResourcesWithResponse("rgcompute", "myScheduledAction", new ResourcePatchRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInput().withResourceId(
                    "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_TriggerManualOccurrence_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_TriggerManualOccurrence_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsTriggerManualOccurrenceMaximumSet(
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
import com.azure.resourcemanager.compute.bulkactions.models.OptimizationPreference;
import com.azure.resourcemanager.compute.bulkactions.models.RecurringScheduledActionsDeadlineType;
import com.azure.resourcemanager.compute.bulkactions.models.RecurringScheduledActionsExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.RecurringScheduledActionsRetryPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceType;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionType;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionUpdate;
import com.azure.resourcemanager.compute.bulkactions.models.ScheduledActionUpdateProperties;
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
     * x-ms-original-file: 2026-07-06-preview/ScheduledActions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Update_MaximumSet.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void scheduledActionsUpdateMaximumSet(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
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
                            .withExecutionParameters(new RecurringScheduledActionsExecutionParameters()
                                .withOptimizationPreference(OptimizationPreference.COST)
                                .withRetryPolicy(new RecurringScheduledActionsRetryPolicy().withRetryCount(17)
                                    .withRetryWindowInMinutes(29)))
                            .withDeadlineType(RecurringScheduledActionsDeadlineType.UNKNOWN))
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

### VirtualMachineBulkOperations_BulkAcknowledgeOperationErrors

```java
import com.azure.resourcemanager.compute.bulkactions.models.AcknowledgeBulkOperationErrorsRequest;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkAcknowledgeOperationErrors.
 */
public final class VirtualMachineBulkOperationsBulkAcknowledgeOperationErrorsSamples {
    /*
     * x-ms-original-file:
     * 2026-07-06-preview/VirtualMachineBulkOperations_BulkAcknowledgeOperationErrors_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkAcknowledgeOperationErrors_MinimumSet_Gen.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkAcknowledgeOperationErrorsMinimumSetGen(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkAcknowledgeOperationErrorsWithResponse("rgBulkactions", "useast2euap",
                new AcknowledgeBulkOperationErrorsRequest().withOperationIds(
                    Arrays.asList("af449548-8e1a-4079-874e-2caa4ff783cc")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * 2026-07-06-preview/VirtualMachineBulkOperations_BulkAcknowledgeOperationErrors_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkAcknowledgeOperationErrors_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkAcknowledgeOperationErrorsExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkAcknowledgeOperationErrorsWithResponse("rgBulkactions", "useast2euap",
                new AcknowledgeBulkOperationErrorsRequest().withOperationIds(
                    Arrays.asList("af449548-8e1a-4079-874e-2caa4ff783cc")),
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkCancel_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkCancel_MinimumSet_Gen.json
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

### VirtualMachineBulkOperations_BulkCreateOperation

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteCreateContent;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceProvisionPayload;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualMachineBulkOperations BulkCreateOperation.
 */
public final class VirtualMachineBulkOperationsBulkCreateOperationSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkCreate_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkCreate_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkCreateExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkCreateOperationWithResponse("rgBulkactions", "useast2euap", new ExecuteCreateContent()
                .withResourceConfigParameters(new ResourceProvisionPayload().withBaseProfile(mapOf("plan",
                    BinaryData.fromBytes(
                        "{name=iemasqqkbixbewezyrhnpntjd, publisher=bvggylbvfstnscuupuithafvvgc, product=bguuzrknnuohugjhernflurpx, promotionCode=bxgonranwqoryfkhkfaumdgz, version=uyxetqmmzvqianqv}"
                            .getBytes(StandardCharsets.UTF_8)),
                    "zones", BinaryData.fromBytes("[wczj]".getBytes(StandardCharsets.UTF_8)), "identity",
                    BinaryData.fromBytes(
                        "{type=SystemAssigned, userAssignedIdentities={key7={}}}".getBytes(StandardCharsets.UTF_8)),
                    "extendedLocation",
                    BinaryData.fromBytes("{name=gbnxzymbdkxhwjpqkur, type=EdgeZone}".getBytes(StandardCharsets.UTF_8)),
                    "placement",
                    BinaryData.fromBytes(
                        "{zonePlacementPolicy=Any, includeZones=[inagtbtedobdea], excludeZones=[pvvwrhuhdpvbacwmesblpgwzk]}"
                            .getBytes(StandardCharsets.UTF_8)),
                    "tags", BinaryData.fromBytes("{key6824=cefndldgkx}".getBytes(StandardCharsets.UTF_8)), "properties",
                    BinaryData.fromBytes(
                        "{scheduledEventsPolicy={userInitiatedRedeploy={automaticallyApprove=true}, userInitiatedReboot={automaticallyApprove=true}, scheduledEventsAdditionalPublishingTargets={eventGridAndResourceGraph={enable=true, scheduledEventsApiVersion=lifncbftlkounuyfn}}, allInstancesDown={automaticallyApprove=true}}, storageProfile={imageReference={publisher=ojlplghybdamadvsrq, offer=uvnqoxhkxefqwbsvjgbswqy, sku=hajdxhjmlkx, version=u, sharedGalleryImageId=fz, communityGalleryImageId=tsfpcq, id=cdbrkpdicibtlliq}, osDisk={osType=Windows, encryptionSettings={diskEncryptionKey={secretUrl=vzkogocyw, sourceVault={id=lvzxxyypkeqlflftmfn}}, keyEncryptionKey={keyUrl=mjjkvgpoohatw, sourceVault={id=lvzxxyypkeqlflftmfn}}, enabled=true}, name=opogpznvctmraoajgizcyrfvpt, vhd={uri=elpzggtxubepzgjqvdbjmbu}, image={uri=elpzggtxubepzgjqvdbjmbu}, caching=None, writeAcceleratorEnabled=true, diffDiskSettings={option=Local, placement=CacheDisk}, createOption=FromImage, diskSizeGB=2, managedDisk={storageAccountType=Standard_LRS, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}, securityProfile={securityEncryptionType=VMGuestStateOnly, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}}, id=numddbqmkxuu}, deleteOption=Delete}, dataDisks=[{lun=7, name=nbthfzqsxyqvqnbgcljxbwyyoj, vhd={uri=elpzggtxubepzgjqvdbjmbu}, image={uri=elpzggtxubepzgjqvdbjmbu}, caching=None, writeAcceleratorEnabled=true, createOption=FromImage, diskSizeGB=19, managedDisk={storageAccountType=Standard_LRS, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}, securityProfile={securityEncryptionType=VMGuestStateOnly, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}}, id=numddbqmkxuu}, sourceResource={id=qnukyordmomtjjqabovlsxl}, toBeDetached=true, detachOption=ForceDetach, deleteOption=Delete}], diskControllerType=SCSI}, hardwareProfile={vmSize=szrnjqwbruz, vmSizeProperties={vCpusAvailable=24, vCpusPerCore=6}}, additionalCapabilities={ultraSSDEnabled=true, hibernationEnabled=true}, osProfile={computerName=bplxnfp, adminUsername=fxzbi, adminPassword=<a-password-goes-here>, customData=hbdlirohsgnbrahscboc, windowsConfiguration={provisionVMAgent=true, enableAutomaticUpdates=true, timeZone=t, additionalUnattendContent=[{passName=OobeSystem, componentName=Microsoft-Windows-Shell-Setup, settingName=AutoLogon, content=rguazthnx}], patchSettings={patchMode=Manual, enableHotpatching=true, assessmentMode=ImageDefault, automaticByPlatformSettings={rebootSetting=Unknown, bypassPlatformSafetyChecksOnUserSchedule=true}}, winRM={listeners=[{protocol=Http, certificateUrl=quhfapfpyeeocwvwtvuggoqqwt}]}}, linuxConfiguration={disablePasswordAuthentication=true, ssh={publicKeys=[{path=mrdfxnfjazxog, keyData=wfhrknkehgesontscqyrewfmhgwt}]}, provisionVMAgent=true, patchSettings={patchMode=ImageDefault, assessmentMode=ImageDefault, automaticByPlatformSettings={rebootSetting=Unknown, bypassPlatformSafetyChecksOnUserSchedule=true}}, enableVMAgentPlatformUpdates=true}, secrets=[{sourceVault={id=lvzxxyypkeqlflftmfn}, vaultCertificates=[{certificateUrl=crgbpfdvlohwkupdjp, certificateStore=hyx}]}], allowExtensionOperations=true, requireGuestProvisionSignal=true}, networkProfile={networkInterfaces=[{properties={primary=true, deleteOption=Delete}, id=ymfxctb}], networkApiVersion=2020-11-01, networkInterfaceConfigurations=[{name=qrkzoctmzjketostzabnra, properties={primary=true, deleteOption=Delete, enableAcceleratedNetworking=true, disableTcpStateTracking=true, enableFpga=true, enableIPForwarding=true, networkSecurityGroup={id=lvzxxyypkeqlflftmfn}, dnsSettings={dnsServers=[tqcqopnanyyiavfwhqbkarxtrfqbww]}, ipConfigurations=[{name=gqymuvgzzfmxqvdadx, properties={subnet={id=lvzxxyypkeqlflftmfn}, primary=true, publicIPAddressConfiguration={name=cwxsqjijtwbsyqdwht, properties={idleTimeoutInMinutes=17, deleteOption=Delete, dnsSettings={domainNameLabel=fampou, domainNameLabelScope=TenantReuse}, ipTags=[{ipTagType=hkjoxhqadudjartwooezaxl, tag=xywunkjglkmmwfpf}], publicIPPrefix={id=lvzxxyypkeqlflftmfn}, publicIPAddressVersion=IPv4, publicIPAllocationMethod=Dynamic}, sku={name=Basic, tier=Regional}, tags={key5442=qhpwpnylvmdthxazhxamnbhdfpf}}, privateIPAddressVersion=IPv4, applicationSecurityGroups=[{id=lvzxxyypkeqlflftmfn}], applicationGatewayBackendAddressPools=[{id=lvzxxyypkeqlflftmfn}], loadBalancerBackendAddressPools=[{id=lvzxxyypkeqlflftmfn}]}}], dscpConfiguration={id=lvzxxyypkeqlflftmfn}, auxiliaryMode=None, auxiliarySku=None}, tags={key9436=bjbadzbfvpszbsickv}}]}, securityProfile={uefiSettings={secureBootEnabled=true, vTpmEnabled=true}, encryptionAtHost=true, securityType=TrustedLaunch, encryptionIdentity={userAssignedIdentityResourceId=tnajlgbwcepmhytzb}, proxyAgentSettings={enabled=true, mode=Audit, keyIncarnationId=4, wireServer={mode=Audit, inVMAccessControlProfileReferenceId=xvlzroy}, imds={mode=Audit, inVMAccessControlProfileReferenceId=xvlzroy}, addProxyAgentExtension=true}}, diagnosticsProfile={bootDiagnostics={enabled=true, storageUri=pxuhtzehlfsqolbdleirgj}}, licenseType=ymwuemwuntbignqyvzqflvjpcdus, extensionsTimeBudget=dnyqmcijikzkltjav, scheduledEventsProfile={terminateNotificationProfile={notBeforeTimeout=owbwifqrlsdmm, enable=true}, osImageNotificationProfile={notBeforeTimeout=ataqykjdakdvyyzdspaqnhd, enable=true}}, userData=nwjvxe, capacityReservation={capacityReservationGroup={id=lvzxxyypkeqlflftmfn}}, applicationProfile={galleryApplications=[{tags=cmygipvpkegyclvpznfu, order=8, packageReferenceId=afrfkjdrtzftmwramfyu, configurationReference=nmfaspclhidtznslsps, treatFailureAsDeploymentFailure=true, enableAutomaticUpgrade=true}]}, vmExtensions=[{name=jkpmcxwuahpzwkvexgzpypk, properties={forceUpdateTag=dockqxgatsfzhctxrncuw, publisher=qesyfldbfoaexyoywhcxafdtdwcg, type=ptlmlzpbpbkfbu, typeHandlerVersion=crllsludntz, autoUpgradeMinorVersion=true, enableAutomaticUpgrade=true, settings={}, protectedSettings={}, suppressFailures=true, protectedSettingsFromKeyVault={secretUrl=vzkogocyw, sourceVault={id=lvzxxyypkeqlflftmfn}}, provisionAfterExtensions=[onbtyoeifafiktrkmal]}}]}"
                            .getBytes(StandardCharsets.UTF_8)),
                    "computeApiVersion",
                    BinaryData.fromBytes("axcvphjtsdjzcwqczcglmq".getBytes(StandardCharsets.UTF_8)), "name",
                    BinaryData.fromBytes("dbozdvegpdvqxltqipvmqsfgunpe".getBytes(StandardCharsets.UTF_8))))
                    .withResourceOverrides(Arrays.asList(mapOf("plan", BinaryData.fromBytes(
                        "{name=iemasqqkbixbewezyrhnpntjd, publisher=bvggylbvfstnscuupuithafvvgc, product=bguuzrknnuohugjhernflurpx, promotionCode=bxgonranwqoryfkhkfaumdgz, version=uyxetqmmzvqianqv}"
                            .getBytes(StandardCharsets.UTF_8)),
                        "zones", BinaryData.fromBytes("[wczj]".getBytes(StandardCharsets.UTF_8)), "identity",
                        BinaryData.fromBytes(
                            "{type=SystemAssigned, userAssignedIdentities={key7={}}}".getBytes(StandardCharsets.UTF_8)),
                        "extendedLocation",
                        BinaryData
                            .fromBytes("{name=gbnxzymbdkxhwjpqkur, type=EdgeZone}".getBytes(StandardCharsets.UTF_8)),
                        "placement",
                        BinaryData.fromBytes(
                            "{zonePlacementPolicy=Any, includeZones=[inagtbtedobdea], excludeZones=[pvvwrhuhdpvbacwmesblpgwzk]}"
                                .getBytes(StandardCharsets.UTF_8)),
                        "tags", BinaryData.fromBytes("{key6824=cefndldgkx}".getBytes(StandardCharsets.UTF_8)),
                        "properties",
                        BinaryData.fromBytes(
                            "{scheduledEventsPolicy={userInitiatedRedeploy={automaticallyApprove=true}, userInitiatedReboot={automaticallyApprove=true}, scheduledEventsAdditionalPublishingTargets={eventGridAndResourceGraph={enable=true, scheduledEventsApiVersion=lifncbftlkounuyfn}}, allInstancesDown={automaticallyApprove=true}}, storageProfile={imageReference={publisher=ojlplghybdamadvsrq, offer=uvnqoxhkxefqwbsvjgbswqy, sku=hajdxhjmlkx, version=u, sharedGalleryImageId=fz, communityGalleryImageId=tsfpcq, id=cdbrkpdicibtlliq}, osDisk={osType=Windows, encryptionSettings={diskEncryptionKey={secretUrl=vzkogocyw, sourceVault={id=lvzxxyypkeqlflftmfn}}, keyEncryptionKey={keyUrl=mjjkvgpoohatw, sourceVault={id=lvzxxyypkeqlflftmfn}}, enabled=true}, name=opogpznvctmraoajgizcyrfvpt, vhd={uri=elpzggtxubepzgjqvdbjmbu}, image={uri=elpzggtxubepzgjqvdbjmbu}, caching=None, writeAcceleratorEnabled=true, diffDiskSettings={option=Local, placement=CacheDisk}, createOption=FromImage, diskSizeGB=2, managedDisk={storageAccountType=Standard_LRS, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}, securityProfile={securityEncryptionType=VMGuestStateOnly, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}}, id=numddbqmkxuu}, deleteOption=Delete}, dataDisks=[{lun=7, name=nbthfzqsxyqvqnbgcljxbwyyoj, vhd={uri=elpzggtxubepzgjqvdbjmbu}, image={uri=elpzggtxubepzgjqvdbjmbu}, caching=None, writeAcceleratorEnabled=true, createOption=FromImage, diskSizeGB=19, managedDisk={storageAccountType=Standard_LRS, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}, securityProfile={securityEncryptionType=VMGuestStateOnly, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}}, id=numddbqmkxuu}, sourceResource={id=qnukyordmomtjjqabovlsxl}, toBeDetached=true, detachOption=ForceDetach, deleteOption=Delete}], diskControllerType=SCSI}, hardwareProfile={vmSize=szrnjqwbruz, vmSizeProperties={vCpusAvailable=24, vCpusPerCore=6}}, additionalCapabilities={ultraSSDEnabled=true, hibernationEnabled=true}, osProfile={computerName=bplxnfp, adminUsername=fxzbi, adminPassword=<a-password-goes-here>, customData=hbdlirohsgnbrahscboc, windowsConfiguration={provisionVMAgent=true, enableAutomaticUpdates=true, timeZone=t, additionalUnattendContent=[{passName=OobeSystem, componentName=Microsoft-Windows-Shell-Setup, settingName=AutoLogon, content=rguazthnx}], patchSettings={patchMode=Manual, enableHotpatching=true, assessmentMode=ImageDefault, automaticByPlatformSettings={rebootSetting=Unknown, bypassPlatformSafetyChecksOnUserSchedule=true}}, winRM={listeners=[{protocol=Http, certificateUrl=quhfapfpyeeocwvwtvuggoqqwt}]}}, linuxConfiguration={disablePasswordAuthentication=true, ssh={publicKeys=[{path=mrdfxnfjazxog, keyData=wfhrknkehgesontscqyrewfmhgwt}]}, provisionVMAgent=true, patchSettings={patchMode=ImageDefault, assessmentMode=ImageDefault, automaticByPlatformSettings={rebootSetting=Unknown, bypassPlatformSafetyChecksOnUserSchedule=true}}, enableVMAgentPlatformUpdates=true}, secrets=[{sourceVault={id=lvzxxyypkeqlflftmfn}, vaultCertificates=[{certificateUrl=crgbpfdvlohwkupdjp, certificateStore=hyx}]}], allowExtensionOperations=true, requireGuestProvisionSignal=true}, networkProfile={networkInterfaces=[{properties={primary=true, deleteOption=Delete}, id=ymfxctb}], networkApiVersion=2020-11-01, networkInterfaceConfigurations=[{name=qrkzoctmzjketostzabnra, properties={primary=true, deleteOption=Delete, enableAcceleratedNetworking=true, disableTcpStateTracking=true, enableFpga=true, enableIPForwarding=true, networkSecurityGroup={id=lvzxxyypkeqlflftmfn}, dnsSettings={dnsServers=[tqcqopnanyyiavfwhqbkarxtrfqbww]}, ipConfigurations=[{name=gqymuvgzzfmxqvdadx, properties={subnet={id=lvzxxyypkeqlflftmfn}, primary=true, publicIPAddressConfiguration={name=cwxsqjijtwbsyqdwht, properties={idleTimeoutInMinutes=17, deleteOption=Delete, dnsSettings={domainNameLabel=fampou, domainNameLabelScope=TenantReuse}, ipTags=[{ipTagType=hkjoxhqadudjartwooezaxl, tag=xywunkjglkmmwfpf}], publicIPPrefix={id=lvzxxyypkeqlflftmfn}, publicIPAddressVersion=IPv4, publicIPAllocationMethod=Dynamic}, sku={name=Basic, tier=Regional}, tags={key5442=qhpwpnylvmdthxazhxamnbhdfpf}}, privateIPAddressVersion=IPv4, applicationSecurityGroups=[{id=lvzxxyypkeqlflftmfn}], applicationGatewayBackendAddressPools=[{id=lvzxxyypkeqlflftmfn}], loadBalancerBackendAddressPools=[{id=lvzxxyypkeqlflftmfn}]}}], dscpConfiguration={id=lvzxxyypkeqlflftmfn}, auxiliaryMode=None, auxiliarySku=None}, tags={key9436=bjbadzbfvpszbsickv}}]}, securityProfile={uefiSettings={secureBootEnabled=true, vTpmEnabled=true}, encryptionAtHost=true, securityType=TrustedLaunch, encryptionIdentity={userAssignedIdentityResourceId=tnajlgbwcepmhytzb}, proxyAgentSettings={enabled=true, mode=Audit, keyIncarnationId=4, wireServer={mode=Audit, inVMAccessControlProfileReferenceId=xvlzroy}, imds={mode=Audit, inVMAccessControlProfileReferenceId=xvlzroy}, addProxyAgentExtension=true}}, diagnosticsProfile={bootDiagnostics={enabled=true, storageUri=pxuhtzehlfsqolbdleirgj}}, licenseType=ymwuemwuntbignqyvzqflvjpcdus, extensionsTimeBudget=dnyqmcijikzkltjav, scheduledEventsProfile={terminateNotificationProfile={notBeforeTimeout=owbwifqrlsdmm, enable=true}, osImageNotificationProfile={notBeforeTimeout=ataqykjdakdvyyzdspaqnhd, enable=true}}, userData=nwjvxe, capacityReservation={capacityReservationGroup={id=lvzxxyypkeqlflftmfn}}, applicationProfile={galleryApplications=[{tags=cmygipvpkegyclvpznfu, order=8, packageReferenceId=afrfkjdrtzftmwramfyu, configurationReference=nmfaspclhidtznslsps, treatFailureAsDeploymentFailure=true, enableAutomaticUpgrade=true}]}, vmExtensions=[{name=jkpmcxwuahpzwkvexgzpypk, properties={forceUpdateTag=dockqxgatsfzhctxrncuw, publisher=qesyfldbfoaexyoywhcxafdtdwcg, type=ptlmlzpbpbkfbu, typeHandlerVersion=crllsludntz, autoUpgradeMinorVersion=true, enableAutomaticUpgrade=true, settings={}, protectedSettings={}, suppressFailures=true, protectedSettingsFromKeyVault={secretUrl=vzkogocyw, sourceVault={id=lvzxxyypkeqlflftmfn}}, provisionAfterExtensions=[onbtyoeifafiktrkmal]}}]}"
                                .getBytes(StandardCharsets.UTF_8)),
                        "computeApiVersion",
                        BinaryData.fromBytes("axcvphjtsdjzcwqczcglmq".getBytes(StandardCharsets.UTF_8)), "name",
                        BinaryData.fromBytes("dbozdvegpdvqxltqipvmqsfgunpe".getBytes(StandardCharsets.UTF_8)))))
                    .withResourceCount(23)
                    .withResourcePrefix("flivkboavfhjuiucwdjof"))
                .withExecutionParameters(new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryCount(2)
                    .withRetryWindowInMinutes(19)
                    .withOnFailureAction(ResourceOperationType.UNKNOWN))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkCreate_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkCreate_Example - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkCreateExampleGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkCreateOperationWithResponse("rgBulkactions", "useast2euap",
                new ExecuteCreateContent()
                    .withResourceConfigParameters(new ResourceProvisionPayload().withResourceCount(23))
                    .withExecutionParameters(new ExecutionParameters()),
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkDeallocate_MinimumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkDeallocate_MaximumSet_Gen.json
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
                    .withOnFailureAction(ResourceOperationType.UNKNOWN)))
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkDelete_MinimumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkDelete_MaximumSet_Gen.json
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
                    .withOnFailureAction(ResourceOperationType.UNKNOWN)))
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkGetOperationsStatus_MinimumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkGetOperationsStatus_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkHibernate_MaximumSet_Gen.json
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
                    .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                .withResourcesWithContext(
                    new ResourcesWithContext().withResources(Arrays.asList(new ResourceWithContext().withResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM")
                        .withResourceContext("hibernateContext")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkHibernate_MinimumSet_Gen.json
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

### VirtualMachineBulkOperations_BulkListOperationErrors

```java
/**
 * Samples for VirtualMachineBulkOperations BulkListOperationErrors.
 */
public final class VirtualMachineBulkOperationsBulkListOperationErrorsSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkListOperationErrors_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkListOperationErrors_MinimumSet_Gen.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkListOperationErrorsMinimumSetGen(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkListOperationErrors("rgBulkactions", "useast2euap", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkListOperationErrors_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkListOperationErrors_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkListOperationErrorsExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkListOperationErrors("rgBulkactions", "useast2euap", null, com.azure.core.util.Context.NONE);
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
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.Resources;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.VirtualMachineReimageParameters;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkReimageOperation.
 */
public final class VirtualMachineBulkOperationsBulkReimageOperationSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkReimage_MaximumSet_Gen.json
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
                .withExecutionParameters(new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryCount(2)
                    .withRetryWindowInMinutes(19)
                    .withOnFailureAction(ResourceOperationType.UNKNOWN)))
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkStart_MinimumSet_Gen.json
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
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkStart_MaximumSet_Gen.json
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
                .withExecutionParameters(new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryCount(2)
                    .withRetryWindowInMinutes(19)
                    .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                .withResourcesWithContext(
                    new ResourcesWithContext().withResources(Arrays.asList(new ResourceWithContext().withResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM")
                        .withResourceContext("startContext")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineBulkOperations_BulkVdiFlexCreateOperation

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.compute.bulkactions.models.AllocationStrategy;
import com.azure.resourcemanager.compute.bulkactions.models.DistributionStrategy;
import com.azure.resourcemanager.compute.bulkactions.models.EvictionPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ExecuteVdiCreateRequest;
import com.azure.resourcemanager.compute.bulkactions.models.ExecutionParameters;
import com.azure.resourcemanager.compute.bulkactions.models.FlexProperties;
import com.azure.resourcemanager.compute.bulkactions.models.OsType;
import com.azure.resourcemanager.compute.bulkactions.models.PriorityProfile;
import com.azure.resourcemanager.compute.bulkactions.models.PriorityType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceOperationType;
import com.azure.resourcemanager.compute.bulkactions.models.ResourceProvisionVdiPayload;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.VmSizeProfile;
import com.azure.resourcemanager.compute.bulkactions.models.ZoneAllocationPolicy;
import com.azure.resourcemanager.compute.bulkactions.models.ZonePreference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualMachineBulkOperations BulkVdiFlexCreateOperation.
 */
public final class VirtualMachineBulkOperationsBulkVdiFlexCreateOperationSamples {
    /*
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkVdiFlexCreate_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkVdiFlexCreate_Example.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkVdiFlexCreateExample(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkVdiFlexCreateOperationWithResponse("rgBulkactions", "useast2euap", new ExecuteVdiCreateRequest()
                .withResourceConfigParameters(new ResourceProvisionVdiPayload().withBaseProfile(mapOf("plan",
                    BinaryData.fromBytes(
                        "{name=iemasqqkbixbewezyrhnpntjd, publisher=bvggylbvfstnscuupuithafvvgc, product=bguuzrknnuohugjhernflurpx, promotionCode=bxgonranwqoryfkhkfaumdgz, version=uyxetqmmzvqianqv}"
                            .getBytes(StandardCharsets.UTF_8)),
                    "zones", BinaryData.fromBytes("[wczj]".getBytes(StandardCharsets.UTF_8)), "identity",
                    BinaryData.fromBytes(
                        "{type=SystemAssigned, userAssignedIdentities={key7={}}}".getBytes(StandardCharsets.UTF_8)),
                    "extendedLocation",
                    BinaryData.fromBytes("{name=gbnxzymbdkxhwjpqkur, type=EdgeZone}".getBytes(StandardCharsets.UTF_8)),
                    "placement",
                    BinaryData.fromBytes(
                        "{zonePlacementPolicy=Any, includeZones=[inagtbtedobdea], excludeZones=[pvvwrhuhdpvbacwmesblpgwzk]}"
                            .getBytes(StandardCharsets.UTF_8)),
                    "tags", BinaryData.fromBytes("{key6824=cefndldgkx}".getBytes(StandardCharsets.UTF_8)), "properties",
                    BinaryData.fromBytes(
                        "{scheduledEventsPolicy={userInitiatedRedeploy={automaticallyApprove=true}, userInitiatedReboot={automaticallyApprove=true}, scheduledEventsAdditionalPublishingTargets={eventGridAndResourceGraph={enable=true, scheduledEventsApiVersion=lifncbftlkounuyfn}}, allInstancesDown={automaticallyApprove=true}}, storageProfile={imageReference={publisher=ojlplghybdamadvsrq, offer=uvnqoxhkxefqwbsvjgbswqy, sku=hajdxhjmlkx, version=u, sharedGalleryImageId=fz, communityGalleryImageId=tsfpcq, id=cdbrkpdicibtlliq}, osDisk={osType=Windows, encryptionSettings={diskEncryptionKey={secretUrl=vzkogocyw, sourceVault={id=lvzxxyypkeqlflftmfn}}, keyEncryptionKey={keyUrl=mjjkvgpoohatw, sourceVault={id=lvzxxyypkeqlflftmfn}}, enabled=true}, name=opogpznvctmraoajgizcyrfvpt, vhd={uri=elpzggtxubepzgjqvdbjmbu}, image={uri=elpzggtxubepzgjqvdbjmbu}, caching=None, writeAcceleratorEnabled=true, diffDiskSettings={option=Local, placement=CacheDisk}, createOption=FromImage, diskSizeGB=2, managedDisk={storageAccountType=Standard_LRS, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}, securityProfile={securityEncryptionType=VMGuestStateOnly, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}}, id=numddbqmkxuu}, deleteOption=Delete}, dataDisks=[{lun=7, name=nbthfzqsxyqvqnbgcljxbwyyoj, vhd={uri=elpzggtxubepzgjqvdbjmbu}, image={uri=elpzggtxubepzgjqvdbjmbu}, caching=None, writeAcceleratorEnabled=true, createOption=FromImage, diskSizeGB=19, managedDisk={storageAccountType=Standard_LRS, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}, securityProfile={securityEncryptionType=VMGuestStateOnly, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}}, id=numddbqmkxuu}, sourceResource={id=qnukyordmomtjjqabovlsxl}, toBeDetached=true, detachOption=ForceDetach, deleteOption=Delete}], diskControllerType=SCSI}, hardwareProfile={vmSize=szrnjqwbruz, vmSizeProperties={vCpusAvailable=24, vCpusPerCore=6}}, additionalCapabilities={ultraSSDEnabled=true, hibernationEnabled=true}, osProfile={computerName=bplxnfp, adminUsername=fxzbi, adminPassword=<a-password-goes-here>, customData=hbdlirohsgnbrahscboc, windowsConfiguration={provisionVMAgent=true, enableAutomaticUpdates=true, timeZone=t, additionalUnattendContent=[{passName=OobeSystem, componentName=Microsoft-Windows-Shell-Setup, settingName=AutoLogon, content=rguazthnx}], patchSettings={patchMode=Manual, enableHotpatching=true, assessmentMode=ImageDefault, automaticByPlatformSettings={rebootSetting=Unknown, bypassPlatformSafetyChecksOnUserSchedule=true}}, winRM={listeners=[{protocol=Http, certificateUrl=quhfapfpyeeocwvwtvuggoqqwt}]}}, linuxConfiguration={disablePasswordAuthentication=true, ssh={publicKeys=[{path=mrdfxnfjazxog, keyData=wfhrknkehgesontscqyrewfmhgwt}]}, provisionVMAgent=true, patchSettings={patchMode=ImageDefault, assessmentMode=ImageDefault, automaticByPlatformSettings={rebootSetting=Unknown, bypassPlatformSafetyChecksOnUserSchedule=true}}, enableVMAgentPlatformUpdates=true}, secrets=[{sourceVault={id=lvzxxyypkeqlflftmfn}, vaultCertificates=[{certificateUrl=crgbpfdvlohwkupdjp, certificateStore=hyx}]}], allowExtensionOperations=true, requireGuestProvisionSignal=true}, networkProfile={networkInterfaces=[{properties={primary=true, deleteOption=Delete}, id=ymfxctb}], networkApiVersion=2020-11-01, networkInterfaceConfigurations=[{name=qrkzoctmzjketostzabnra, properties={primary=true, deleteOption=Delete, enableAcceleratedNetworking=true, disableTcpStateTracking=true, enableFpga=true, enableIPForwarding=true, networkSecurityGroup={id=lvzxxyypkeqlflftmfn}, dnsSettings={dnsServers=[tqcqopnanyyiavfwhqbkarxtrfqbww]}, ipConfigurations=[{name=gqymuvgzzfmxqvdadx, properties={subnet={id=lvzxxyypkeqlflftmfn}, primary=true, publicIPAddressConfiguration={name=cwxsqjijtwbsyqdwht, properties={idleTimeoutInMinutes=17, deleteOption=Delete, dnsSettings={domainNameLabel=fampou, domainNameLabelScope=TenantReuse}, ipTags=[{ipTagType=hkjoxhqadudjartwooezaxl, tag=xywunkjglkmmwfpf}], publicIPPrefix={id=lvzxxyypkeqlflftmfn}, publicIPAddressVersion=IPv4, publicIPAllocationMethod=Dynamic}, sku={name=Basic, tier=Regional}, tags={key5442=qhpwpnylvmdthxazhxamnbhdfpf}}, privateIPAddressVersion=IPv4, applicationSecurityGroups=[{id=lvzxxyypkeqlflftmfn}], applicationGatewayBackendAddressPools=[{id=lvzxxyypkeqlflftmfn}], loadBalancerBackendAddressPools=[{id=lvzxxyypkeqlflftmfn}]}}], dscpConfiguration={id=lvzxxyypkeqlflftmfn}, auxiliaryMode=None, auxiliarySku=None}, tags={key9436=bjbadzbfvpszbsickv}}]}, securityProfile={uefiSettings={secureBootEnabled=true, vTpmEnabled=true}, encryptionAtHost=true, securityType=TrustedLaunch, encryptionIdentity={userAssignedIdentityResourceId=tnajlgbwcepmhytzb}, proxyAgentSettings={enabled=true, mode=Audit, keyIncarnationId=4, wireServer={mode=Audit, inVMAccessControlProfileReferenceId=xvlzroy}, imds={mode=Audit, inVMAccessControlProfileReferenceId=xvlzroy}, addProxyAgentExtension=true}}, diagnosticsProfile={bootDiagnostics={enabled=true, storageUri=pxuhtzehlfsqolbdleirgj}}, licenseType=ymwuemwuntbignqyvzqflvjpcdus, extensionsTimeBudget=dnyqmcijikzkltjav, scheduledEventsProfile={terminateNotificationProfile={notBeforeTimeout=owbwifqrlsdmm, enable=true}, osImageNotificationProfile={notBeforeTimeout=ataqykjdakdvyyzdspaqnhd, enable=true}}, userData=nwjvxe, capacityReservation={capacityReservationGroup={id=lvzxxyypkeqlflftmfn}}, applicationProfile={galleryApplications=[{tags=cmygipvpkegyclvpznfu, order=8, packageReferenceId=afrfkjdrtzftmwramfyu, configurationReference=nmfaspclhidtznslsps, treatFailureAsDeploymentFailure=true, enableAutomaticUpgrade=true}]}, vmExtensions=[{name=jkpmcxwuahpzwkvexgzpypk, properties={forceUpdateTag=dockqxgatsfzhctxrncuw, publisher=qesyfldbfoaexyoywhcxafdtdwcg, type=ptlmlzpbpbkfbu, typeHandlerVersion=crllsludntz, autoUpgradeMinorVersion=true, enableAutomaticUpgrade=true, settings={}, protectedSettings={}, suppressFailures=true, protectedSettingsFromKeyVault={secretUrl=vzkogocyw, sourceVault={id=lvzxxyypkeqlflftmfn}}, provisionAfterExtensions=[onbtyoeifafiktrkmal]}}]}"
                            .getBytes(StandardCharsets.UTF_8)),
                    "computeApiVersion",
                    BinaryData.fromBytes("axcvphjtsdjzcwqczcglmq".getBytes(StandardCharsets.UTF_8)), "name",
                    BinaryData.fromBytes("dbozdvegpdvqxltqipvmqsfgunpe".getBytes(StandardCharsets.UTF_8))))
                    .withResourceOverrides(Arrays.asList(mapOf("plan", BinaryData.fromBytes(
                        "{name=iemasqqkbixbewezyrhnpntjd, publisher=bvggylbvfstnscuupuithafvvgc, product=bguuzrknnuohugjhernflurpx, promotionCode=bxgonranwqoryfkhkfaumdgz, version=uyxetqmmzvqianqv}"
                            .getBytes(StandardCharsets.UTF_8)),
                        "zones", BinaryData.fromBytes("[wczj]".getBytes(StandardCharsets.UTF_8)), "identity",
                        BinaryData.fromBytes(
                            "{type=SystemAssigned, userAssignedIdentities={key7={}}}".getBytes(StandardCharsets.UTF_8)),
                        "extendedLocation",
                        BinaryData
                            .fromBytes("{name=gbnxzymbdkxhwjpqkur, type=EdgeZone}".getBytes(StandardCharsets.UTF_8)),
                        "placement",
                        BinaryData.fromBytes(
                            "{zonePlacementPolicy=Any, includeZones=[inagtbtedobdea], excludeZones=[pvvwrhuhdpvbacwmesblpgwzk]}"
                                .getBytes(StandardCharsets.UTF_8)),
                        "tags", BinaryData.fromBytes("{key6824=cefndldgkx}".getBytes(StandardCharsets.UTF_8)),
                        "properties",
                        BinaryData.fromBytes(
                            "{scheduledEventsPolicy={userInitiatedRedeploy={automaticallyApprove=true}, userInitiatedReboot={automaticallyApprove=true}, scheduledEventsAdditionalPublishingTargets={eventGridAndResourceGraph={enable=true, scheduledEventsApiVersion=lifncbftlkounuyfn}}, allInstancesDown={automaticallyApprove=true}}, storageProfile={imageReference={publisher=ojlplghybdamadvsrq, offer=uvnqoxhkxefqwbsvjgbswqy, sku=hajdxhjmlkx, version=u, sharedGalleryImageId=fz, communityGalleryImageId=tsfpcq, id=cdbrkpdicibtlliq}, osDisk={osType=Windows, encryptionSettings={diskEncryptionKey={secretUrl=vzkogocyw, sourceVault={id=lvzxxyypkeqlflftmfn}}, keyEncryptionKey={keyUrl=mjjkvgpoohatw, sourceVault={id=lvzxxyypkeqlflftmfn}}, enabled=true}, name=opogpznvctmraoajgizcyrfvpt, vhd={uri=elpzggtxubepzgjqvdbjmbu}, image={uri=elpzggtxubepzgjqvdbjmbu}, caching=None, writeAcceleratorEnabled=true, diffDiskSettings={option=Local, placement=CacheDisk}, createOption=FromImage, diskSizeGB=2, managedDisk={storageAccountType=Standard_LRS, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}, securityProfile={securityEncryptionType=VMGuestStateOnly, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}}, id=numddbqmkxuu}, deleteOption=Delete}, dataDisks=[{lun=7, name=nbthfzqsxyqvqnbgcljxbwyyoj, vhd={uri=elpzggtxubepzgjqvdbjmbu}, image={uri=elpzggtxubepzgjqvdbjmbu}, caching=None, writeAcceleratorEnabled=true, createOption=FromImage, diskSizeGB=19, managedDisk={storageAccountType=Standard_LRS, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}, securityProfile={securityEncryptionType=VMGuestStateOnly, diskEncryptionSet={id=magvkzhdmzhktjlqkkk}}, id=numddbqmkxuu}, sourceResource={id=qnukyordmomtjjqabovlsxl}, toBeDetached=true, detachOption=ForceDetach, deleteOption=Delete}], diskControllerType=SCSI}, hardwareProfile={vmSize=szrnjqwbruz, vmSizeProperties={vCpusAvailable=24, vCpusPerCore=6}}, additionalCapabilities={ultraSSDEnabled=true, hibernationEnabled=true}, osProfile={computerName=bplxnfp, adminUsername=fxzbi, adminPassword=<a-password-goes-here>, customData=hbdlirohsgnbrahscboc, windowsConfiguration={provisionVMAgent=true, enableAutomaticUpdates=true, timeZone=t, additionalUnattendContent=[{passName=OobeSystem, componentName=Microsoft-Windows-Shell-Setup, settingName=AutoLogon, content=rguazthnx}], patchSettings={patchMode=Manual, enableHotpatching=true, assessmentMode=ImageDefault, automaticByPlatformSettings={rebootSetting=Unknown, bypassPlatformSafetyChecksOnUserSchedule=true}}, winRM={listeners=[{protocol=Http, certificateUrl=quhfapfpyeeocwvwtvuggoqqwt}]}}, linuxConfiguration={disablePasswordAuthentication=true, ssh={publicKeys=[{path=mrdfxnfjazxog, keyData=wfhrknkehgesontscqyrewfmhgwt}]}, provisionVMAgent=true, patchSettings={patchMode=ImageDefault, assessmentMode=ImageDefault, automaticByPlatformSettings={rebootSetting=Unknown, bypassPlatformSafetyChecksOnUserSchedule=true}}, enableVMAgentPlatformUpdates=true}, secrets=[{sourceVault={id=lvzxxyypkeqlflftmfn}, vaultCertificates=[{certificateUrl=crgbpfdvlohwkupdjp, certificateStore=hyx}]}], allowExtensionOperations=true, requireGuestProvisionSignal=true}, networkProfile={networkInterfaces=[{properties={primary=true, deleteOption=Delete}, id=ymfxctb}], networkApiVersion=2020-11-01, networkInterfaceConfigurations=[{name=qrkzoctmzjketostzabnra, properties={primary=true, deleteOption=Delete, enableAcceleratedNetworking=true, disableTcpStateTracking=true, enableFpga=true, enableIPForwarding=true, networkSecurityGroup={id=lvzxxyypkeqlflftmfn}, dnsSettings={dnsServers=[tqcqopnanyyiavfwhqbkarxtrfqbww]}, ipConfigurations=[{name=gqymuvgzzfmxqvdadx, properties={subnet={id=lvzxxyypkeqlflftmfn}, primary=true, publicIPAddressConfiguration={name=cwxsqjijtwbsyqdwht, properties={idleTimeoutInMinutes=17, deleteOption=Delete, dnsSettings={domainNameLabel=fampou, domainNameLabelScope=TenantReuse}, ipTags=[{ipTagType=hkjoxhqadudjartwooezaxl, tag=xywunkjglkmmwfpf}], publicIPPrefix={id=lvzxxyypkeqlflftmfn}, publicIPAddressVersion=IPv4, publicIPAllocationMethod=Dynamic}, sku={name=Basic, tier=Regional}, tags={key5442=qhpwpnylvmdthxazhxamnbhdfpf}}, privateIPAddressVersion=IPv4, applicationSecurityGroups=[{id=lvzxxyypkeqlflftmfn}], applicationGatewayBackendAddressPools=[{id=lvzxxyypkeqlflftmfn}], loadBalancerBackendAddressPools=[{id=lvzxxyypkeqlflftmfn}]}}], dscpConfiguration={id=lvzxxyypkeqlflftmfn}, auxiliaryMode=None, auxiliarySku=None}, tags={key9436=bjbadzbfvpszbsickv}}]}, securityProfile={uefiSettings={secureBootEnabled=true, vTpmEnabled=true}, encryptionAtHost=true, securityType=TrustedLaunch, encryptionIdentity={userAssignedIdentityResourceId=tnajlgbwcepmhytzb}, proxyAgentSettings={enabled=true, mode=Audit, keyIncarnationId=4, wireServer={mode=Audit, inVMAccessControlProfileReferenceId=xvlzroy}, imds={mode=Audit, inVMAccessControlProfileReferenceId=xvlzroy}, addProxyAgentExtension=true}}, diagnosticsProfile={bootDiagnostics={enabled=true, storageUri=pxuhtzehlfsqolbdleirgj}}, licenseType=ymwuemwuntbignqyvzqflvjpcdus, extensionsTimeBudget=dnyqmcijikzkltjav, scheduledEventsProfile={terminateNotificationProfile={notBeforeTimeout=owbwifqrlsdmm, enable=true}, osImageNotificationProfile={notBeforeTimeout=ataqykjdakdvyyzdspaqnhd, enable=true}}, userData=nwjvxe, capacityReservation={capacityReservationGroup={id=lvzxxyypkeqlflftmfn}}, applicationProfile={galleryApplications=[{tags=cmygipvpkegyclvpznfu, order=8, packageReferenceId=afrfkjdrtzftmwramfyu, configurationReference=nmfaspclhidtznslsps, treatFailureAsDeploymentFailure=true, enableAutomaticUpgrade=true}]}, vmExtensions=[{name=jkpmcxwuahpzwkvexgzpypk, properties={forceUpdateTag=dockqxgatsfzhctxrncuw, publisher=qesyfldbfoaexyoywhcxafdtdwcg, type=ptlmlzpbpbkfbu, typeHandlerVersion=crllsludntz, autoUpgradeMinorVersion=true, enableAutomaticUpgrade=true, settings={}, protectedSettings={}, suppressFailures=true, protectedSettingsFromKeyVault={secretUrl=vzkogocyw, sourceVault={id=lvzxxyypkeqlflftmfn}}, provisionAfterExtensions=[onbtyoeifafiktrkmal]}}]}"
                                .getBytes(StandardCharsets.UTF_8)),
                        "computeApiVersion",
                        BinaryData.fromBytes("axcvphjtsdjzcwqczcglmq".getBytes(StandardCharsets.UTF_8)), "name",
                        BinaryData.fromBytes("dbozdvegpdvqxltqipvmqsfgunpe".getBytes(StandardCharsets.UTF_8)))))
                    .withResourceCount(10)
                    .withResourcePrefix("mwbopevxbjcunljvruov")
                    .withFlexProperties(new FlexProperties()
                        .withVmSizeProfiles(Arrays.asList(new VmSizeProfile().withName("frbnnpdkq").withRank(7)))
                        .withOsType(OsType.WINDOWS)
                        .withPriorityProfile(new PriorityProfile().withType(PriorityType.REGULAR)
                            .withMaxPricePerVM(23.0D)
                            .withEvictionPolicy(EvictionPolicy.DELETE)
                            .withAllocationStrategy(AllocationStrategy.LOWEST_PRICE))
                        .withZoneAllocationPolicy(new ZoneAllocationPolicy()
                            .withDistributionStrategy(DistributionStrategy.BEST_EFFORT_SINGLE_ZONE)
                            .withZonePreferences(Arrays
                                .asList(new ZonePreference().withZone("ixksjnaxwelhfpsoyjfaezievquqv").withRank(19))))
                        .withMinCapacity(5)))
                .withExecutionParameters(new ExecutionParameters().withRetryPolicy(new RetryPolicy().withRetryCount(2)
                    .withRetryWindowInMinutes(19)
                    .withOnFailureAction(ResourceOperationType.UNKNOWN))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-06-preview/VirtualMachineBulkOperations_BulkVdiFlexCreate_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineBulkOperations_BulkVdiFlexCreate_Example - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeBulkActionsManager.
     */
    public static void virtualMachineBulkOperationsBulkVdiFlexCreateExampleGeneratedByMinimumSetRule(
        com.azure.resourcemanager.compute.bulkactions.ComputeBulkActionsManager manager) {
        manager.virtualMachineBulkOperations()
            .bulkVdiFlexCreateOperationWithResponse("rgBulkactions", "useast2euap",
                new ExecuteVdiCreateRequest()
                    .withResourceConfigParameters(new ResourceProvisionVdiPayload().withResourceCount(10)
                        .withFlexProperties(new FlexProperties()
                            .withVmSizeProfiles(
                                Arrays.asList(new VmSizeProfile().withName("Standard_D2ads_v5").withRank(7)))
                            .withOsType(OsType.WINDOWS)
                            .withPriorityProfile(new PriorityProfile())))
                    .withExecutionParameters(new ExecutionParameters()),
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

