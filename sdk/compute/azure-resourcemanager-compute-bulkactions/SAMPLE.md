# Code snippets and samples


## LaunchBulkInstancesOperation

- [Cancel](#launchbulkinstancesoperation_cancel)
- [CreateOrUpdate](#launchbulkinstancesoperation_createorupdate)
- [Delete](#launchbulkinstancesoperation_delete)
- [Get](#launchbulkinstancesoperation_get)
- [GetOperationStatus](#launchbulkinstancesoperation_getoperationstatus)
- [ListByResourceGroup](#launchbulkinstancesoperation_listbyresourcegroup)
- [ListBySubscription](#launchbulkinstancesoperation_listbysubscription)
- [ListVirtualMachines](#launchbulkinstancesoperation_listvirtualmachines)

## Operations

- [List](#operations_list)

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
import com.azure.resourcemanager.compute.bulkactions.models.ResourcesWithContext;
import com.azure.resourcemanager.compute.bulkactions.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for VirtualMachineBulkOperations BulkStartOperation.
 */
public final class VirtualMachineBulkOperationsBulkStartOperationSamples {
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

