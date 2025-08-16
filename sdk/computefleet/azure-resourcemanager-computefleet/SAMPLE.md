# Code snippets and samples


## Fleets

- [Cancel](#fleets_cancel)
- [CreateOrUpdate](#fleets_createorupdate)
- [Delete](#fleets_delete)
- [GetByResourceGroup](#fleets_getbyresourcegroup)
- [List](#fleets_list)
- [ListByResourceGroup](#fleets_listbyresourcegroup)
- [ListVirtualMachineScaleSets](#fleets_listvirtualmachinescalesets)
- [ListVirtualMachines](#fleets_listvirtualmachines)
- [Update](#fleets_update)

## Operations

- [List](#operations_list)
### Fleets_Cancel

```java
/**
 * Samples for Fleets Cancel.
 */
public final class FleetsCancelSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Fleets_Cancel.json
     */
    /**
     * Sample code: Fleets_Cancel.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsCancel(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets().cancel("rgazurefleet", "myFleet", com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.computefleet.models.AcceleratorManufacturer;
import com.azure.resourcemanager.computefleet.models.AcceleratorType;
import com.azure.resourcemanager.computefleet.models.AdditionalCapabilities;
import com.azure.resourcemanager.computefleet.models.AdditionalLocationsProfile;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContent;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContentComponentName;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContentPassName;
import com.azure.resourcemanager.computefleet.models.ApiEntityReference;
import com.azure.resourcemanager.computefleet.models.ApplicationProfile;
import com.azure.resourcemanager.computefleet.models.ArchitectureType;
import com.azure.resourcemanager.computefleet.models.BaseVirtualMachineProfile;
import com.azure.resourcemanager.computefleet.models.BootDiagnostics;
import com.azure.resourcemanager.computefleet.models.CachingTypes;
import com.azure.resourcemanager.computefleet.models.CapacityReservationProfile;
import com.azure.resourcemanager.computefleet.models.CapacityType;
import com.azure.resourcemanager.computefleet.models.ComputeProfile;
import com.azure.resourcemanager.computefleet.models.CpuManufacturer;
import com.azure.resourcemanager.computefleet.models.DeleteOptions;
import com.azure.resourcemanager.computefleet.models.DiagnosticsProfile;
import com.azure.resourcemanager.computefleet.models.DiffDiskOptions;
import com.azure.resourcemanager.computefleet.models.DiffDiskPlacement;
import com.azure.resourcemanager.computefleet.models.DiffDiskSettings;
import com.azure.resourcemanager.computefleet.models.DiskControllerTypes;
import com.azure.resourcemanager.computefleet.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.computefleet.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.computefleet.models.DiskEncryptionSetParameters;
import com.azure.resourcemanager.computefleet.models.DomainNameLabelScopeTypes;
import com.azure.resourcemanager.computefleet.models.EncryptionIdentity;
import com.azure.resourcemanager.computefleet.models.EvictionPolicy;
import com.azure.resourcemanager.computefleet.models.FleetMode;
import com.azure.resourcemanager.computefleet.models.FleetProperties;
import com.azure.resourcemanager.computefleet.models.IPVersion;
import com.azure.resourcemanager.computefleet.models.ImageReference;
import com.azure.resourcemanager.computefleet.models.KeyVaultSecretReference;
import com.azure.resourcemanager.computefleet.models.LinuxConfiguration;
import com.azure.resourcemanager.computefleet.models.LinuxPatchAssessmentMode;
import com.azure.resourcemanager.computefleet.models.LinuxPatchSettings;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchMode;
import com.azure.resourcemanager.computefleet.models.LocalStorageDiskType;
import com.azure.resourcemanager.computefleet.models.LocationProfile;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentity;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.computefleet.models.Mode;
import com.azure.resourcemanager.computefleet.models.NetworkApiVersion;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliaryMode;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliarySku;
import com.azure.resourcemanager.computefleet.models.OSImageNotificationProfile;
import com.azure.resourcemanager.computefleet.models.OperatingSystemTypes;
import com.azure.resourcemanager.computefleet.models.PatchSettings;
import com.azure.resourcemanager.computefleet.models.Plan;
import com.azure.resourcemanager.computefleet.models.ProtocolTypes;
import com.azure.resourcemanager.computefleet.models.ProxyAgentSettings;
import com.azure.resourcemanager.computefleet.models.PublicIPAddressSku;
import com.azure.resourcemanager.computefleet.models.PublicIPAddressSkuName;
import com.azure.resourcemanager.computefleet.models.PublicIPAddressSkuTier;
import com.azure.resourcemanager.computefleet.models.RegularPriorityAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.RegularPriorityProfile;
import com.azure.resourcemanager.computefleet.models.ScheduledEventsProfile;
import com.azure.resourcemanager.computefleet.models.SecurityEncryptionTypes;
import com.azure.resourcemanager.computefleet.models.SecurityPostureReference;
import com.azure.resourcemanager.computefleet.models.SecurityProfile;
import com.azure.resourcemanager.computefleet.models.SecurityTypes;
import com.azure.resourcemanager.computefleet.models.ServiceArtifactReference;
import com.azure.resourcemanager.computefleet.models.SettingNames;
import com.azure.resourcemanager.computefleet.models.SpotAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.SpotPriorityProfile;
import com.azure.resourcemanager.computefleet.models.SshConfiguration;
import com.azure.resourcemanager.computefleet.models.SshPublicKey;
import com.azure.resourcemanager.computefleet.models.StorageAccountTypes;
import com.azure.resourcemanager.computefleet.models.TerminateNotificationProfile;
import com.azure.resourcemanager.computefleet.models.UefiSettings;
import com.azure.resourcemanager.computefleet.models.VMAttributeMinMaxDouble;
import com.azure.resourcemanager.computefleet.models.VMAttributeMinMaxInteger;
import com.azure.resourcemanager.computefleet.models.VMAttributeSupport;
import com.azure.resourcemanager.computefleet.models.VMAttributes;
import com.azure.resourcemanager.computefleet.models.VMCategory;
import com.azure.resourcemanager.computefleet.models.VMDiskSecurityProfile;
import com.azure.resourcemanager.computefleet.models.VMGalleryApplication;
import com.azure.resourcemanager.computefleet.models.VMSizeProperties;
import com.azure.resourcemanager.computefleet.models.VaultCertificate;
import com.azure.resourcemanager.computefleet.models.VaultSecretGroup;
import com.azure.resourcemanager.computefleet.models.VirtualHardDisk;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetDataDisk;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetExtension;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetExtensionProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetExtensionProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetHardwareProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIPConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIPConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIpTag;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetManagedDiskParameters;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfigurationDnsSettings;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetOSDisk;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetOSProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetPublicIPAddressConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetPublicIPAddressConfigurationDnsSettings;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetPublicIPAddressConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetStorageProfile;
import com.azure.resourcemanager.computefleet.models.VmSizeProfile;
import com.azure.resourcemanager.computefleet.models.WinRMConfiguration;
import com.azure.resourcemanager.computefleet.models.WinRMListener;
import com.azure.resourcemanager.computefleet.models.WindowsConfiguration;
import com.azure.resourcemanager.computefleet.models.WindowsPatchAssessmentMode;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchMode;
import com.azure.resourcemanager.computefleet.models.ZoneAllocationPolicy;
import com.azure.resourcemanager.computefleet.models.ZoneDistributionStrategy;
import com.azure.resourcemanager.computefleet.models.ZonePreference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Fleets CreateOrUpdate.
 */
public final class FleetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Fleets_CreateOrUpdate.json
     */
    /**
     * Sample code: Fleets_CreateOrUpdate.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsCreateOrUpdate(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets()
            .define("myFleet")
            .withRegion("westus")
            .withExistingResourceGroup("rgazurefleet")
            .withTags(mapOf())
            .withProperties(new FleetProperties().withSpotPriorityProfile(new SpotPriorityProfile().withCapacity(20)
                .withMinCapacity(10)
                .withMaxPricePerVM(0.00865D)
                .withEvictionPolicy(EvictionPolicy.DELETE)
                .withAllocationStrategy(SpotAllocationStrategy.PRICE_CAPACITY_OPTIMIZED)
                .withMaintain(true))
                .withRegularPriorityProfile(new RegularPriorityProfile().withCapacity(20)
                    .withMinCapacity(10)
                    .withAllocationStrategy(RegularPriorityAllocationStrategy.PRIORITIZED))
                .withVmSizesProfile(Arrays.asList(new VmSizeProfile().withName("Standard_D1_v2").withRank(0),
                    new VmSizeProfile().withName("Standard_D2_v2").withRank(1)))
                .withVmAttributes(new VMAttributes().withVCpuCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withMemoryInGiB(new VMAttributeMinMaxDouble().withMin(2.0D).withMax(4.0D))
                    .withMemoryInGiBPerVCpu(new VMAttributeMinMaxDouble().withMin(2.0D).withMax(4.0D))
                    .withLocalStorageSupport(VMAttributeSupport.EXCLUDED)
                    .withLocalStorageInGiB(new VMAttributeMinMaxDouble().withMin(2.0D).withMax(4.0D))
                    .withLocalStorageDiskTypes(Arrays.asList(LocalStorageDiskType.HDD))
                    .withDataDiskCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withNetworkInterfaceCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withNetworkBandwidthInMbps(new VMAttributeMinMaxDouble().withMin(2.0D).withMax(4.0D))
                    .withRdmaSupport(VMAttributeSupport.EXCLUDED)
                    .withRdmaNetworkInterfaceCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withAcceleratorSupport(VMAttributeSupport.EXCLUDED)
                    .withAcceleratorManufacturers(Arrays.asList(AcceleratorManufacturer.AMD))
                    .withAcceleratorTypes(Arrays.asList(AcceleratorType.GPU))
                    .withAcceleratorCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withVmCategories(Arrays.asList(VMCategory.GPU_ACCELERATED))
                    .withArchitectureTypes(Arrays.asList(ArchitectureType.ARM64))
                    .withCpuManufacturers(Arrays.asList(CpuManufacturer.INTEL))
                    .withBurstableSupport(VMAttributeSupport.EXCLUDED)
                    .withExcludedVMSizes(Arrays.asList("Standard_A1")))
                .withAdditionalLocationsProfile(new AdditionalLocationsProfile().withLocationProfiles(Arrays.asList(
                    new LocationProfile().withLocation("ekbzgzhs")
                        .withVirtualMachineProfileOverride(new BaseVirtualMachineProfile()
                            .withOsProfile(new VirtualMachineScaleSetOSProfile()
                                .withComputerNamePrefix("xoxwfnjjuqibzxldgxu")
                                .withAdminUsername("wwjyuhblwecni")
                                .withAdminPassword("fakeTokenPlaceholder")
                                .withCustomData("gvyvbgcgutteiivwjn")
                                .withWindowsConfiguration(new WindowsConfiguration().withProvisionVMAgent(true)
                                    .withEnableAutomaticUpdates(true)
                                    .withTimeZone("oqpoladmchkkugpxocrynztkok")
                                    .withAdditionalUnattendContent(Arrays.asList(new AdditionalUnattendContent()
                                        .withPassName(AdditionalUnattendContentPassName.OOBE_SYSTEM)
                                        .withComponentName(
                                            AdditionalUnattendContentComponentName.MICROSOFT_WINDOWS_SHELL_SETUP)
                                        .withSettingName(SettingNames.AUTO_LOGON)
                                        .withContent("ynkrgbreqtuxgftjgeuvozzypzx")))
                                    .withPatchSettings(new PatchSettings().withPatchMode(WindowsVMGuestPatchMode.MANUAL)
                                        .withEnableHotpatching(true)
                                        .withAssessmentMode(WindowsPatchAssessmentMode.IMAGE_DEFAULT)
                                        .withAutomaticByPlatformSettings(
                                            new WindowsVMGuestPatchAutomaticByPlatformSettings()
                                                .withRebootSetting(
                                                    WindowsVMGuestPatchAutomaticByPlatformRebootSetting.UNKNOWN)
                                                .withBypassPlatformSafetyChecksOnUserSchedule(true)))
                                    .withWinRM(new WinRMConfiguration().withListeners(
                                        Arrays.asList(new WinRMListener().withProtocol(ProtocolTypes.HTTP)
                                            .withCertificateUrl("https://microsoft.com/a"))))
                                    .withEnableVMAgentPlatformUpdates(true))
                                .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSsh(new SshConfiguration().withPublicKeys(Arrays.asList(
                                        new SshPublicKey().withPath("bci").withKeyData("fakeTokenPlaceholder"))))
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
                                    .withSourceVault(new SubResource().withId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))
                                    .withVaultCertificates(Arrays
                                        .asList(new VaultCertificate().withCertificateUrl("https://microsoft.com/a")
                                            .withCertificateStore("hdts")))))
                                .withAllowExtensionOperations(true)
                                .withRequireGuestProvisionSignal(true))
                            .withStorageProfile(new VirtualMachineScaleSetStorageProfile()
                                .withImageReference(new ImageReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/images/{imageName}/versions/{versionName}")
                                    .withPublisher("mqxgwbiyjzmxavhbkd")
                                    .withOffer("isxgumkarlkomp")
                                    .withSku("eojmppqcrnpmxirtp")
                                    .withVersion("wvpcqefgtmqdgltiuz")
                                    .withSharedGalleryImageId("kmkgihoxwlawuuhcinfirktdwkmx")
                                    .withCommunityGalleryImageId("vlqe"))
                                .withOsDisk(new VirtualMachineScaleSetOSDisk().withName("xhwnqpqigoymwwetvhjuuhiu")
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiffDiskSettings(new DiffDiskSettings().withOption(DiffDiskOptions.LOCAL)
                                        .withPlacement(DiffDiskPlacement.CACHE_DISK))
                                    .withDiskSizeGB(21)
                                    .withOsType(OperatingSystemTypes.WINDOWS)
                                    .withImage(new VirtualHardDisk().withUri("https://microsoft.com/a"))
                                    .withVhdContainers(Arrays.asList("mgyqnavpb"))
                                    .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))))
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE))
                                .withDataDisks(Arrays.asList(new VirtualMachineScaleSetDataDisk().withName("nqblcowgig")
                                    .withLun(14)
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiskSizeGB(11)
                                    .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))))
                                    .withDiskIOPSReadWrite(5L)
                                    .withDiskMBpsReadWrite(4L)
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                                .withDiskControllerType(DiskControllerTypes.SCSI))
                            .withNetworkProfile(new VirtualMachineScaleSetNetworkProfile()
                                .withHealthProbe(new ApiEntityReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/probes/{probeName}"))
                                .withNetworkInterfaceConfigurations(Arrays.asList(
                                    new VirtualMachineScaleSetNetworkConfiguration().withName("uyemquurltujhbjkhm")
                                        .withProperties(new VirtualMachineScaleSetNetworkConfigurationProperties()
                                            .withPrimary(true)
                                            .withEnableAcceleratedNetworking(true)
                                            .withDisableTcpStateTracking(true)
                                            .withEnableFpga(true)
                                            .withNetworkSecurityGroup(new SubResource().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/networkSecurityGroups/{networkSecurityGroupName}"))
                                            .withDnsSettings(new VirtualMachineScaleSetNetworkConfigurationDnsSettings()
                                                .withDnsServers(Arrays.asList("ajcsckebabrus")))
                                            .withIpConfigurations(Arrays.asList(
                                                new VirtualMachineScaleSetIPConfiguration().withName("xpwuwsvkuml")
                                                    .withProperties(
                                                        new VirtualMachineScaleSetIPConfigurationProperties()
                                                            .withSubnet(new ApiEntityReference().withId(
                                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/virtualNetworks/{virtualNetworkName}/subnets/{subnetName}"))
                                                            .withPrimary(true)
                                                            .withPublicIPAddressConfiguration(
                                                                new VirtualMachineScaleSetPublicIPAddressConfiguration()
                                                                    .withName("wbpdlbxflssopphq")
                                                                    .withProperties(
                                                                        new VirtualMachineScaleSetPublicIPAddressConfigurationProperties()
                                                                            .withIdleTimeoutInMinutes(9)
                                                                            .withDnsSettings(
                                                                                new VirtualMachineScaleSetPublicIPAddressConfigurationDnsSettings()
                                                                                    .withDomainNameLabel(
                                                                                        "uwjtwqgwalsctypszcbnxo")
                                                                                    .withDomainNameLabelScope(
                                                                                        DomainNameLabelScopeTypes.TENANT_REUSE))
                                                                            .withIpTags(Arrays.asList(
                                                                                new VirtualMachineScaleSetIpTag()
                                                                                    .withIpTagType(
                                                                                        "hxkbmbisknggtfdqoaqagjhipdkd")
                                                                                    .withTag("vzxhyyrzieaocbxyxieivj")))
                                                                            .withPublicIPPrefix(
                                                                                new SubResource().withId(
                                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/publicIPPrefixes/{publicIPPrefixName}"))
                                                                            .withPublicIPAddressVersion(IPVersion.IPV4)
                                                                            .withDeleteOption(DeleteOptions.DELETE))
                                                                    .withSku(new PublicIPAddressSku()
                                                                        .withName(PublicIPAddressSkuName.BASIC)
                                                                        .withTier(PublicIPAddressSkuTier.REGIONAL)))
                                                            .withPrivateIPAddressVersion(IPVersion.IPV4)
                                                            .withApplicationGatewayBackendAddressPools(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/applicationGateways/{applicationGatewayName}/backendAddressPools/{backendAddressPoolName}")))
                                                            .withApplicationSecurityGroups(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/applicationSecurityGroups/{applicationSecurityGroupName}")))
                                                            .withLoadBalancerBackendAddressPools(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/backendAddressPools/{backendAddressPoolName}")))
                                                            .withLoadBalancerInboundNatPools(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/inboundNatPools/{inboundNatPoolName}"))))))
                                            .withEnableIPForwarding(true)
                                            .withDeleteOption(DeleteOptions.DELETE)
                                            .withAuxiliaryMode(NetworkInterfaceAuxiliaryMode.NONE)
                                            .withAuxiliarySku(NetworkInterfaceAuxiliarySku.NONE))))
                                .withNetworkApiVersion(NetworkApiVersion.V2020_11_01))
                            .withSecurityProfile(new SecurityProfile()
                                .withUefiSettings(new UefiSettings().withSecureBootEnabled(true).withVTpmEnabled(true))
                                .withEncryptionAtHost(true)
                                .withSecurityType(SecurityTypes.TRUSTED_LAUNCH)
                                .withEncryptionIdentity(new EncryptionIdentity().withUserAssignedIdentityResourceId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{userAssignedIdentityName}"))
                                .withProxyAgentSettings(new ProxyAgentSettings().withEnabled(true)
                                    .withMode(Mode.AUDIT)
                                    .withKeyIncarnationId(22)))
                            .withDiagnosticsProfile(new DiagnosticsProfile().withBootDiagnostics(
                                new BootDiagnostics().withEnabled(true).withStorageUri("https://microsoft.com/a")))
                            .withExtensionProfile(new VirtualMachineScaleSetExtensionProfile()
                                .withExtensions(Arrays.asList(new VirtualMachineScaleSetExtension()
                                    .withName("dockglmmvl")
                                    .withProperties(new VirtualMachineScaleSetExtensionProperties()
                                        .withForceUpdateTag("wzyqlpszoiewqbhlnzckfshdtpwkbd")
                                        .withPublisher("iikgjziralgrfsrxrlrdigqyfhuqg")
                                        .withType("xzhgosms")
                                        .withTypeHandlerVersion("mfzdzdwucagkogmxoosyjpej")
                                        .withAutoUpgradeMinorVersion(true)
                                        .withEnableAutomaticUpgrade(true)
                                        .withSettings(mapOf())
                                        .withProtectedSettings(mapOf())
                                        .withProvisionAfterExtensions(Arrays.asList("rqrycujrpdodllirebkfg"))
                                        .withSuppressFailures(true)
                                        .withProtectedSettingsFromKeyVault(new KeyVaultSecretReference()
                                            .withSecretUrl("fakeTokenPlaceholder")
                                            .withSourceVault(new SubResource().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))))))
                                .withExtensionsTimeBudget("srxtwxrc"))
                            .withLicenseType("hilutelnuqxtpdznq")
                            .withScheduledEventsProfile(new ScheduledEventsProfile()
                                .withTerminateNotificationProfile(new TerminateNotificationProfile()
                                    .withNotBeforeTimeout("jgycfvgxpzvgsdylbcspkrxwhgxkyd")
                                    .withEnable(true))
                                .withOsImageNotificationProfile(
                                    new OSImageNotificationProfile().withNotBeforeTimeout("nbgfbvisxveyywfyjgcfb")
                                        .withEnable(true)))
                            .withUserData("ezhyl")
                            .withCapacityReservation(
                                new CapacityReservationProfile().withCapacityReservationGroup(new SubResource().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/capacityReservationGroups/{capacityReservationGroupName}")))
                            .withApplicationProfile(new ApplicationProfile().withGalleryApplications(
                                Arrays.asList(new VMGalleryApplication().withTags("fronuehbtzhxaoijmdmjzwaswgevh")
                                    .withOrder(19)
                                    .withPackageReferenceId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/applications/{applicationName}/versions/{versionName}")
                                    .withConfigurationReference("gqxsvizquzglpsgqaundtyh")
                                    .withTreatFailureAsDeploymentFailure(true)
                                    .withEnableAutomaticUpgrade(true))))
                            .withHardwareProfile(new VirtualMachineScaleSetHardwareProfile()
                                .withVmSizeProperties(new VMSizeProperties().withVCPUsAvailable(1).withVCPUsPerCore(4)))
                            .withServiceArtifactReference(new ServiceArtifactReference().withId(
                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/serviceArtifacts/{serviceArtifactsName}/vmArtifactsProfiles/{vmArtifactsProfileName}"))
                            .withSecurityPostureReference(new SecurityPostureReference().withId(
                                "/CommunityGalleries/{communityGalleryName}/securityPostures/{securityPostureName}/versions/{major.minor.patch}|{major.*}|latest")
                                .withExcludeExtensions(Arrays.asList("zagiokiwvg"))
                                .withIsOverridable(true))))))
                .withComputeProfile(
                    new ComputeProfile()
                        .withBaseVirtualMachineProfile(new BaseVirtualMachineProfile()
                            .withOsProfile(new VirtualMachineScaleSetOSProfile().withComputerNamePrefix("o")
                                .withAdminUsername("nrgzqciiaaxjrqldbmjbqkyhntp")
                                .withAdminPassword("fakeTokenPlaceholder")
                                .withCustomData("xjjib")
                                .withWindowsConfiguration(new WindowsConfiguration().withProvisionVMAgent(true)
                                    .withEnableAutomaticUpdates(true)
                                    .withTimeZone("hlyjiqcfksgrpjrct")
                                    .withAdditionalUnattendContent(
                                        Arrays.asList(new AdditionalUnattendContent()
                                            .withPassName(AdditionalUnattendContentPassName.OOBE_SYSTEM)
                                            .withComponentName(
                                                AdditionalUnattendContentComponentName.MICROSOFT_WINDOWS_SHELL_SETUP)
                                            .withSettingName(SettingNames.AUTO_LOGON)
                                            .withContent("bubmqbxjkj")))
                                    .withPatchSettings(new PatchSettings()
                                        .withPatchMode(WindowsVMGuestPatchMode.MANUAL)
                                        .withEnableHotpatching(true)
                                        .withAssessmentMode(WindowsPatchAssessmentMode.IMAGE_DEFAULT)
                                        .withAutomaticByPlatformSettings(
                                            new WindowsVMGuestPatchAutomaticByPlatformSettings()
                                                .withRebootSetting(
                                                    WindowsVMGuestPatchAutomaticByPlatformRebootSetting.UNKNOWN)
                                                .withBypassPlatformSafetyChecksOnUserSchedule(true)))
                                    .withWinRM(new WinRMConfiguration().withListeners(Arrays.asList(new WinRMListener()
                                        .withProtocol(ProtocolTypes.HTTPS)
                                        .withCertificateUrl("https://myVaultName.vault.azure.net/secrets/myCertName"))))
                                    .withEnableVMAgentPlatformUpdates(true))
                                .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSsh(new SshConfiguration().withPublicKeys(Arrays.asList(
                                        new SshPublicKey().withPath("kmqz").withKeyData("fakeTokenPlaceholder"))))
                                    .withProvisionVMAgent(true)
                                    .withPatchSettings(new LinuxPatchSettings()
                                        .withPatchMode(LinuxVMGuestPatchMode.IMAGE_DEFAULT)
                                        .withAssessmentMode(LinuxPatchAssessmentMode.IMAGE_DEFAULT)
                                        .withAutomaticByPlatformSettings(
                                            new LinuxVMGuestPatchAutomaticByPlatformSettings()
                                                .withRebootSetting(
                                                    LinuxVMGuestPatchAutomaticByPlatformRebootSetting.UNKNOWN)
                                                .withBypassPlatformSafetyChecksOnUserSchedule(true)))
                                    .withEnableVMAgentPlatformUpdates(true))
                                .withSecrets(Arrays.asList(new VaultSecretGroup()
                                    .withSourceVault(new SubResource().withId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))
                                    .withVaultCertificates(Arrays.asList(new VaultCertificate()
                                        .withCertificateUrl("https://myVaultName.vault.azure.net/secrets/myCertName")
                                        .withCertificateStore("nlxrwavpzhueffxsshlun")))))
                                .withAllowExtensionOperations(true)
                                .withRequireGuestProvisionSignal(true))
                            .withStorageProfile(new VirtualMachineScaleSetStorageProfile()
                                .withImageReference(new ImageReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/images/{imageName}/versions/{versionName}")
                                    .withPublisher("mqxgwbiyjzmxavhbkd")
                                    .withOffer("isxgumkarlkomp")
                                    .withSku("eojmppqcrnpmxirtp")
                                    .withVersion("wvpcqefgtmqdgltiuz")
                                    .withSharedGalleryImageId("kmkgihoxwlawuuhcinfirktdwkmx")
                                    .withCommunityGalleryImageId("vlqe"))
                                .withOsDisk(new VirtualMachineScaleSetOSDisk().withName("wfttw")
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiffDiskSettings(new DiffDiskSettings()
                                        .withOption(DiffDiskOptions.LOCAL)
                                        .withPlacement(DiffDiskPlacement.CACHE_DISK))
                                    .withDiskSizeGB(14)
                                    .withOsType(OperatingSystemTypes.WINDOWS)
                                    .withImage(new VirtualHardDisk().withUri(
                                        "https://myStorageAccountName.blob.core.windows.net/myContainerName/myVhdName.vhd"))
                                    .withVhdContainers(Arrays.asList("tkzcwddtinkfpnfklatw"))
                                    .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(
                                                new DiskEncryptionSetParameters().withId(
                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))))
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE))
                                .withDataDisks(Arrays.asList(new VirtualMachineScaleSetDataDisk()
                                    .withName("eogiykmdmeikswxmigjws")
                                    .withLun(14)
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiskSizeGB(6)
                                    .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(
                                                new DiskEncryptionSetParameters().withId(
                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))))
                                    .withDiskIOPSReadWrite(27L)
                                    .withDiskMBpsReadWrite(2L)
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                                .withDiskControllerType(DiskControllerTypes.fromString("uzb")))
                            .withNetworkProfile(new VirtualMachineScaleSetNetworkProfile()
                                .withHealthProbe(new ApiEntityReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/probes/{probeName}"))
                                .withNetworkInterfaceConfigurations(
                                    Arrays.asList(new VirtualMachineScaleSetNetworkConfiguration().withName("i")
                                        .withProperties(new VirtualMachineScaleSetNetworkConfigurationProperties()
                                            .withPrimary(true)
                                            .withEnableAcceleratedNetworking(true)
                                            .withDisableTcpStateTracking(true)
                                            .withEnableFpga(true)
                                            .withNetworkSecurityGroup(new SubResource().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/networkSecurityGroups/{networkSecurityGroupName}"))
                                            .withDnsSettings(new VirtualMachineScaleSetNetworkConfigurationDnsSettings()
                                                .withDnsServers(Arrays.asList("nxmmfolhclsesu")))
                                            .withIpConfigurations(
                                                Arrays.asList(new VirtualMachineScaleSetIPConfiguration()
                                                    .withName("oezqhkidfhyywlfzwuotilrpbqnjg")
                                                    .withProperties(
                                                        new VirtualMachineScaleSetIPConfigurationProperties()
                                                            .withSubnet(new ApiEntityReference().withId(
                                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/virtualNetworks/{virtualNetworkName}/subnets/{subnetName}"))
                                                            .withPrimary(true)
                                                            .withPublicIPAddressConfiguration(
                                                                new VirtualMachineScaleSetPublicIPAddressConfiguration()
                                                                    .withName("fvpqf")
                                                                    .withProperties(
                                                                        new VirtualMachineScaleSetPublicIPAddressConfigurationProperties()
                                                                            .withIdleTimeoutInMinutes(9)
                                                                            .withDnsSettings(
                                                                                new VirtualMachineScaleSetPublicIPAddressConfigurationDnsSettings()
                                                                                    .withDomainNameLabel(
                                                                                        "ukrddzvmorpmfsczjwtbvp")
                                                                                    .withDomainNameLabelScope(
                                                                                        DomainNameLabelScopeTypes.TENANT_REUSE))
                                                                            .withIpTags(Arrays.asList(
                                                                                new VirtualMachineScaleSetIpTag()
                                                                                    .withIpTagType("sddgsoemnzgqizale")
                                                                                    .withTag("wufmhrjsakbiaetyara")))
                                                                            .withPublicIPPrefix(
                                                                                new SubResource().withId(
                                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/publicIPPrefixes/{publicIPPrefixName}"))
                                                                            .withPublicIPAddressVersion(IPVersion.IPV4)
                                                                            .withDeleteOption(DeleteOptions.DELETE))
                                                                    .withSku(new PublicIPAddressSku()
                                                                        .withName(PublicIPAddressSkuName.BASIC)
                                                                        .withTier(PublicIPAddressSkuTier.REGIONAL)))
                                                            .withPrivateIPAddressVersion(IPVersion.IPV4)
                                                            .withApplicationGatewayBackendAddressPools(Arrays
                                                                .asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/applicationGateways/{applicationGatewayName}/backendAddressPools/{backendAddressPoolName}")))
                                                            .withApplicationSecurityGroups(Arrays
                                                                .asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/applicationSecurityGroups/{applicationSecurityGroupName}")))
                                                            .withLoadBalancerBackendAddressPools(Arrays
                                                                .asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/backendAddressPools/{backendAddressPoolName}")))
                                                            .withLoadBalancerInboundNatPools(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/inboundNatPools/{inboundNatPoolName}"))))))
                                            .withEnableIPForwarding(true)
                                            .withDeleteOption(DeleteOptions.DELETE)
                                            .withAuxiliaryMode(NetworkInterfaceAuxiliaryMode.NONE)
                                            .withAuxiliarySku(NetworkInterfaceAuxiliarySku.NONE))))
                                .withNetworkApiVersion(NetworkApiVersion.V2020_11_01))
                            .withSecurityProfile(new SecurityProfile()
                                .withUefiSettings(new UefiSettings().withSecureBootEnabled(true).withVTpmEnabled(true))
                                .withEncryptionAtHost(true)
                                .withSecurityType(SecurityTypes.TRUSTED_LAUNCH)
                                .withEncryptionIdentity(new EncryptionIdentity().withUserAssignedIdentityResourceId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{userAssignedIdentityName}"))
                                .withProxyAgentSettings(new ProxyAgentSettings().withEnabled(true)
                                    .withMode(Mode.AUDIT)
                                    .withKeyIncarnationId(20)))
                            .withDiagnosticsProfile(
                                new DiagnosticsProfile().withBootDiagnostics(new BootDiagnostics()
                                    .withEnabled(true)
                                    .withStorageUri("http://myStorageAccountName.blob.core.windows.net")))
                            .withExtensionProfile(new VirtualMachineScaleSetExtensionProfile()
                                .withExtensions(Arrays.asList(new VirtualMachineScaleSetExtension().withName("bndxuxx")
                                    .withProperties(new VirtualMachineScaleSetExtensionProperties()
                                        .withForceUpdateTag("yhgxw")
                                        .withPublisher("kpxtirxjfprhs")
                                        .withType("pgjilctjjwaa")
                                        .withTypeHandlerVersion("zevivcoilxmbwlrihhhibq")
                                        .withAutoUpgradeMinorVersion(true)
                                        .withEnableAutomaticUpgrade(true)
                                        .withSettings(mapOf())
                                        .withProtectedSettings(mapOf())
                                        .withProvisionAfterExtensions(Arrays.asList("nftzosroolbcwmpupujzqwqe"))
                                        .withSuppressFailures(true)
                                        .withProtectedSettingsFromKeyVault(new KeyVaultSecretReference()
                                            .withSecretUrl("fakeTokenPlaceholder")
                                            .withSourceVault(new SubResource().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))))))
                                .withExtensionsTimeBudget("mbhjahtdygwgyszdwjtvlvtgchdwil"))
                            .withLicenseType("v")
                            .withScheduledEventsProfile(new ScheduledEventsProfile()
                                .withTerminateNotificationProfile(new TerminateNotificationProfile()
                                    .withNotBeforeTimeout("iljppmmw")
                                    .withEnable(true))
                                .withOsImageNotificationProfile(
                                    new OSImageNotificationProfile().withNotBeforeTimeout("olbpadmevekyczfokodtfprxti")
                                        .withEnable(true)))
                            .withUserData("s")
                            .withCapacityReservation(new CapacityReservationProfile()
                                .withCapacityReservationGroup(new SubResource().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/capacityReservationGroups/{capacityReservationGroupName}")))
                            .withApplicationProfile(new ApplicationProfile().withGalleryApplications(
                                Arrays.asList(new VMGalleryApplication().withTags("eyrqjbib")
                                    .withOrder(5)
                                    .withPackageReferenceId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/applications/{applicationName}/versions/{versionName}")
                                    .withConfigurationReference("ulztmiavpojpbpbddgnuuiimxcpau")
                                    .withTreatFailureAsDeploymentFailure(true)
                                    .withEnableAutomaticUpgrade(true))))
                            .withHardwareProfile(
                                new VirtualMachineScaleSetHardwareProfile().withVmSizeProperties(new VMSizeProperties()
                                    .withVCPUsAvailable(16)
                                    .withVCPUsPerCore(23)))
                            .withServiceArtifactReference(new ServiceArtifactReference().withId(
                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/serviceArtifacts/{serviceArtifactsName}/vmArtifactsProfiles/{vmArtifactsProfileName}"))
                            .withSecurityPostureReference(new SecurityPostureReference().withId(
                                "/CommunityGalleries/{communityGalleryName}/securityPostures/{securityPostureName}/versions/{major.minor.patch}|{major.*}|latest")
                                .withExcludeExtensions(Arrays.asList("{securityPostureVMExtensionName}"))
                                .withIsOverridable(true)))
                        .withComputeApiVersion("2023-07-01")
                        .withPlatformFaultDomainCount(1)
                        .withAdditionalVirtualMachineCapabilities(
                            new AdditionalCapabilities().withUltraSSDEnabled(true).withHibernationEnabled(true)))
                .withMode(FleetMode.INSTANCE)
                .withCapacityType(CapacityType.VCPU)
                .withZoneAllocationPolicy(
                    new ZoneAllocationPolicy().withDistributionStrategy(ZoneDistributionStrategy.PRIORITIZED)
                        .withZonePreferences(Arrays.asList(new ZonePreference().withZone("1").withRank(0),
                            new ZonePreference().withZone("2").withRank(1)))))
            .withZones(Arrays.asList("1", "2"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf()))
            .withPlan(new Plan().withName("jwgrcrnrtfoxn")
                .withPublisher("iozjbiqqckqm")
                .withProduct("cgopbyvdyqikahwyxfpzwaqk")
                .withPromotionCode("fakeTokenPlaceholder")
                .withVersion("wa"))
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

### Fleets_Delete

```java
/**
 * Samples for Fleets Delete.
 */
public final class FleetsDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Fleets_Delete.json
     */
    /**
     * Sample code: Fleets_Delete.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsDelete(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets().delete("rgazurefleet", "testFleet", com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_GetByResourceGroup

```java
/**
 * Samples for Fleets GetByResourceGroup.
 */
public final class FleetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Fleets_Get.json
     */
    /**
     * Sample code: Fleets_Get.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsGet(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets().getByResourceGroupWithResponse("rgazurefleet", "myFleet", com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_List

```java
/**
 * Samples for Fleets List.
 */
public final class FleetsListSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Fleets_ListBySubscription.json
     */
    /**
     * Sample code: Fleets_ListBySubscription.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsListBySubscription(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets().list(com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_ListByResourceGroup

```java
/**
 * Samples for Fleets ListByResourceGroup.
 */
public final class FleetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Fleets_ListByResourceGroup.json
     */
    /**
     * Sample code: Fleets_ListByResourceGroup.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsListByResourceGroup(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets().listByResourceGroup("rgazurefleet", com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_ListVirtualMachineScaleSets

```java
/**
 * Samples for Fleets ListVirtualMachineScaleSets.
 */
public final class FleetsListVirtualMachineScaleSetsSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Fleets_ListVirtualMachineScaleSets.json
     */
    /**
     * Sample code: Fleets_ListVirtualMachineScaleSets.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void
        fleetsListVirtualMachineScaleSets(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets().listVirtualMachineScaleSets("rgazurefleet", "myFleet", com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_ListVirtualMachines

```java
/**
 * Samples for Fleets ListVirtualMachines.
 */
public final class FleetsListVirtualMachinesSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Fleets_ListVirtualMachines.json
     */
    /**
     * Sample code: Fleets_ListVirtualMachines_MaximumSet.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void
        fleetsListVirtualMachinesMaximumSet(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets()
            .listVirtualMachines("rgazurefleet", "myFleet", "xzcepyottghqa", "hydepbmwuypaprlphcdecsz",
                com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_Update

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.computefleet.models.AcceleratorManufacturer;
import com.azure.resourcemanager.computefleet.models.AcceleratorType;
import com.azure.resourcemanager.computefleet.models.AdditionalCapabilities;
import com.azure.resourcemanager.computefleet.models.AdditionalLocationsProfile;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContent;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContentComponentName;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContentPassName;
import com.azure.resourcemanager.computefleet.models.ApiEntityReference;
import com.azure.resourcemanager.computefleet.models.ApplicationProfile;
import com.azure.resourcemanager.computefleet.models.ArchitectureType;
import com.azure.resourcemanager.computefleet.models.BaseVirtualMachineProfile;
import com.azure.resourcemanager.computefleet.models.BootDiagnostics;
import com.azure.resourcemanager.computefleet.models.CachingTypes;
import com.azure.resourcemanager.computefleet.models.CapacityReservationProfile;
import com.azure.resourcemanager.computefleet.models.CapacityType;
import com.azure.resourcemanager.computefleet.models.ComputeProfile;
import com.azure.resourcemanager.computefleet.models.CpuManufacturer;
import com.azure.resourcemanager.computefleet.models.DeleteOptions;
import com.azure.resourcemanager.computefleet.models.DiagnosticsProfile;
import com.azure.resourcemanager.computefleet.models.DiffDiskOptions;
import com.azure.resourcemanager.computefleet.models.DiffDiskPlacement;
import com.azure.resourcemanager.computefleet.models.DiffDiskSettings;
import com.azure.resourcemanager.computefleet.models.DiskControllerTypes;
import com.azure.resourcemanager.computefleet.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.computefleet.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.computefleet.models.DiskEncryptionSetParameters;
import com.azure.resourcemanager.computefleet.models.DomainNameLabelScopeTypes;
import com.azure.resourcemanager.computefleet.models.EncryptionIdentity;
import com.azure.resourcemanager.computefleet.models.EvictionPolicy;
import com.azure.resourcemanager.computefleet.models.Fleet;
import com.azure.resourcemanager.computefleet.models.FleetMode;
import com.azure.resourcemanager.computefleet.models.FleetProperties;
import com.azure.resourcemanager.computefleet.models.IPVersion;
import com.azure.resourcemanager.computefleet.models.ImageReference;
import com.azure.resourcemanager.computefleet.models.KeyVaultSecretReference;
import com.azure.resourcemanager.computefleet.models.LinuxConfiguration;
import com.azure.resourcemanager.computefleet.models.LinuxPatchAssessmentMode;
import com.azure.resourcemanager.computefleet.models.LinuxPatchSettings;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchMode;
import com.azure.resourcemanager.computefleet.models.LocalStorageDiskType;
import com.azure.resourcemanager.computefleet.models.LocationProfile;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentityUpdate;
import com.azure.resourcemanager.computefleet.models.Mode;
import com.azure.resourcemanager.computefleet.models.NetworkApiVersion;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliaryMode;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliarySku;
import com.azure.resourcemanager.computefleet.models.OSImageNotificationProfile;
import com.azure.resourcemanager.computefleet.models.OperatingSystemTypes;
import com.azure.resourcemanager.computefleet.models.PatchSettings;
import com.azure.resourcemanager.computefleet.models.ProtocolTypes;
import com.azure.resourcemanager.computefleet.models.ProxyAgentSettings;
import com.azure.resourcemanager.computefleet.models.PublicIPAddressSku;
import com.azure.resourcemanager.computefleet.models.PublicIPAddressSkuName;
import com.azure.resourcemanager.computefleet.models.PublicIPAddressSkuTier;
import com.azure.resourcemanager.computefleet.models.RegularPriorityAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.RegularPriorityProfile;
import com.azure.resourcemanager.computefleet.models.ResourcePlanUpdate;
import com.azure.resourcemanager.computefleet.models.ScheduledEventsProfile;
import com.azure.resourcemanager.computefleet.models.SecurityEncryptionTypes;
import com.azure.resourcemanager.computefleet.models.SecurityPostureReference;
import com.azure.resourcemanager.computefleet.models.SecurityProfile;
import com.azure.resourcemanager.computefleet.models.SecurityTypes;
import com.azure.resourcemanager.computefleet.models.ServiceArtifactReference;
import com.azure.resourcemanager.computefleet.models.SettingNames;
import com.azure.resourcemanager.computefleet.models.SpotAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.SpotPriorityProfile;
import com.azure.resourcemanager.computefleet.models.SshConfiguration;
import com.azure.resourcemanager.computefleet.models.SshPublicKey;
import com.azure.resourcemanager.computefleet.models.StorageAccountTypes;
import com.azure.resourcemanager.computefleet.models.TerminateNotificationProfile;
import com.azure.resourcemanager.computefleet.models.UefiSettings;
import com.azure.resourcemanager.computefleet.models.VMAttributeMinMaxDouble;
import com.azure.resourcemanager.computefleet.models.VMAttributeMinMaxInteger;
import com.azure.resourcemanager.computefleet.models.VMAttributeSupport;
import com.azure.resourcemanager.computefleet.models.VMAttributes;
import com.azure.resourcemanager.computefleet.models.VMCategory;
import com.azure.resourcemanager.computefleet.models.VMDiskSecurityProfile;
import com.azure.resourcemanager.computefleet.models.VMGalleryApplication;
import com.azure.resourcemanager.computefleet.models.VMSizeProperties;
import com.azure.resourcemanager.computefleet.models.VaultCertificate;
import com.azure.resourcemanager.computefleet.models.VaultSecretGroup;
import com.azure.resourcemanager.computefleet.models.VirtualHardDisk;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetDataDisk;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetExtension;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetExtensionProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetExtensionProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetHardwareProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIPConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIPConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIpTag;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetManagedDiskParameters;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfigurationDnsSettings;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetOSDisk;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetOSProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetPublicIPAddressConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetPublicIPAddressConfigurationDnsSettings;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetPublicIPAddressConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetStorageProfile;
import com.azure.resourcemanager.computefleet.models.VmSizeProfile;
import com.azure.resourcemanager.computefleet.models.WinRMConfiguration;
import com.azure.resourcemanager.computefleet.models.WinRMListener;
import com.azure.resourcemanager.computefleet.models.WindowsConfiguration;
import com.azure.resourcemanager.computefleet.models.WindowsPatchAssessmentMode;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchMode;
import com.azure.resourcemanager.computefleet.models.ZoneAllocationPolicy;
import com.azure.resourcemanager.computefleet.models.ZoneDistributionStrategy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Fleets Update.
 */
public final class FleetsUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Fleets_Update.json
     */
    /**
     * Sample code: Fleets_Update.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsUpdate(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        Fleet resource = manager.fleets()
            .getByResourceGroupWithResponse("rgazurefleet", "myFleet", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf())
            .withIdentity(new ManagedServiceIdentityUpdate().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf()))
            .withPlan(new ResourcePlanUpdate().withName("jwgrcrnrtfoxn")
                .withPublisher("iozjbiqqckqm")
                .withProduct("cgopbyvdyqikahwyxfpzwaqk")
                .withPromotionCode("fakeTokenPlaceholder")
                .withVersion("wa"))
            .withProperties(new FleetProperties().withSpotPriorityProfile(new SpotPriorityProfile().withCapacity(20)
                .withMinCapacity(10)
                .withMaxPricePerVM(0.00865D)
                .withEvictionPolicy(EvictionPolicy.DELETE)
                .withAllocationStrategy(SpotAllocationStrategy.PRICE_CAPACITY_OPTIMIZED)
                .withMaintain(true))
                .withRegularPriorityProfile(new RegularPriorityProfile().withCapacity(20)
                    .withMinCapacity(10)
                    .withAllocationStrategy(RegularPriorityAllocationStrategy.PRIORITIZED))
                .withVmSizesProfile(Arrays.asList(new VmSizeProfile().withName("Standard_D1_v2").withRank(0),
                    new VmSizeProfile().withName("Standard_D2_v2").withRank(1)))
                .withVmAttributes(new VMAttributes().withVCpuCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withMemoryInGiB(new VMAttributeMinMaxDouble().withMin(2.0D).withMax(4.0D))
                    .withMemoryInGiBPerVCpu(new VMAttributeMinMaxDouble().withMin(2.0D).withMax(4.0D))
                    .withLocalStorageSupport(VMAttributeSupport.EXCLUDED)
                    .withLocalStorageInGiB(new VMAttributeMinMaxDouble().withMin(2.0D).withMax(4.0D))
                    .withLocalStorageDiskTypes(Arrays.asList(LocalStorageDiskType.HDD))
                    .withDataDiskCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withNetworkInterfaceCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withNetworkBandwidthInMbps(new VMAttributeMinMaxDouble().withMin(2.0D).withMax(4.0D))
                    .withRdmaSupport(VMAttributeSupport.EXCLUDED)
                    .withRdmaNetworkInterfaceCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withAcceleratorSupport(VMAttributeSupport.EXCLUDED)
                    .withAcceleratorManufacturers(Arrays.asList(AcceleratorManufacturer.AMD))
                    .withAcceleratorTypes(Arrays.asList(AcceleratorType.GPU))
                    .withAcceleratorCount(new VMAttributeMinMaxInteger().withMin(2).withMax(4))
                    .withVmCategories(Arrays.asList(VMCategory.GENERAL_PURPOSE))
                    .withArchitectureTypes(Arrays.asList(ArchitectureType.ARM64))
                    .withCpuManufacturers(Arrays.asList(CpuManufacturer.INTEL))
                    .withBurstableSupport(VMAttributeSupport.EXCLUDED)
                    .withExcludedVMSizes(Arrays.asList("Standard_A1")))
                .withAdditionalLocationsProfile(new AdditionalLocationsProfile().withLocationProfiles(Arrays.asList(
                    new LocationProfile().withLocation("v")
                        .withVirtualMachineProfileOverride(new BaseVirtualMachineProfile()
                            .withOsProfile(new VirtualMachineScaleSetOSProfile().withComputerNamePrefix("tec")
                                .withAdminUsername("xdgnnqymtamdyqxy")
                                .withAdminPassword("fakeTokenPlaceholder")
                                .withCustomData("whcielwnerogvbxnbia")
                                .withWindowsConfiguration(new WindowsConfiguration().withProvisionVMAgent(true)
                                    .withEnableAutomaticUpdates(true)
                                    .withTimeZone("ktf")
                                    .withAdditionalUnattendContent(Arrays.asList(new AdditionalUnattendContent()
                                        .withPassName(AdditionalUnattendContentPassName.OOBE_SYSTEM)
                                        .withComponentName(
                                            AdditionalUnattendContentComponentName.MICROSOFT_WINDOWS_SHELL_SETUP)
                                        .withSettingName(SettingNames.AUTO_LOGON)
                                        .withContent("xcigofrcurxdwx")))
                                    .withPatchSettings(new PatchSettings().withPatchMode(WindowsVMGuestPatchMode.MANUAL)
                                        .withEnableHotpatching(true)
                                        .withAssessmentMode(WindowsPatchAssessmentMode.IMAGE_DEFAULT)
                                        .withAutomaticByPlatformSettings(
                                            new WindowsVMGuestPatchAutomaticByPlatformSettings()
                                                .withRebootSetting(
                                                    WindowsVMGuestPatchAutomaticByPlatformRebootSetting.UNKNOWN)
                                                .withBypassPlatformSafetyChecksOnUserSchedule(true)))
                                    .withWinRM(new WinRMConfiguration().withListeners(
                                        Arrays.asList(new WinRMListener().withProtocol(ProtocolTypes.HTTP)
                                            .withCertificateUrl("https://microsoft.com/apzd"))))
                                    .withEnableVMAgentPlatformUpdates(true))
                                .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSsh(new SshConfiguration().withPublicKeys(
                                        Arrays.asList(new SshPublicKey().withPath("ebeglujkldnntlpmazrg")
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
                                    .withSourceVault(new SubResource().withId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))
                                    .withVaultCertificates(Arrays
                                        .asList(new VaultCertificate().withCertificateUrl("https://microsoft.com/a")
                                            .withCertificateStore("yycyfwpymjtwzza")))))
                                .withAllowExtensionOperations(true)
                                .withRequireGuestProvisionSignal(true))
                            .withStorageProfile(new VirtualMachineScaleSetStorageProfile()
                                .withImageReference(new ImageReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/images/{imageName}/versions/{versionName}")
                                    .withPublisher("mqxgwbiyjzmxavhbkd")
                                    .withOffer("isxgumkarlkomp")
                                    .withSku("eojmppqcrnpmxirtp")
                                    .withVersion("wvpcqefgtmqdgltiuz")
                                    .withSharedGalleryImageId("kmkgihoxwlawuuhcinfirktdwkmx")
                                    .withCommunityGalleryImageId("vlqe"))
                                .withOsDisk(new VirtualMachineScaleSetOSDisk().withName("dt")
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiffDiskSettings(new DiffDiskSettings().withOption(DiffDiskOptions.LOCAL)
                                        .withPlacement(DiffDiskPlacement.CACHE_DISK))
                                    .withDiskSizeGB(9)
                                    .withOsType(OperatingSystemTypes.WINDOWS)
                                    .withImage(new VirtualHardDisk().withUri("https://microsoft.com/a"))
                                    .withVhdContainers(Arrays.asList("kdagj"))
                                    .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))))
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE))
                                .withDataDisks(Arrays.asList(new VirtualMachineScaleSetDataDisk()
                                    .withName("mhljivkyryuomrapmmxx")
                                    .withLun(6)
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiskSizeGB(9)
                                    .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))))
                                    .withDiskIOPSReadWrite(24L)
                                    .withDiskMBpsReadWrite(4L)
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                                .withDiskControllerType(DiskControllerTypes.SCSI))
                            .withNetworkProfile(new VirtualMachineScaleSetNetworkProfile()
                                .withHealthProbe(new ApiEntityReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/probes/{probeName}"))
                                .withNetworkInterfaceConfigurations(Arrays.asList(
                                    new VirtualMachineScaleSetNetworkConfiguration().withName("gpunpcdsdphgspvgwwbnk")
                                        .withProperties(new VirtualMachineScaleSetNetworkConfigurationProperties()
                                            .withPrimary(true)
                                            .withEnableAcceleratedNetworking(true)
                                            .withDisableTcpStateTracking(true)
                                            .withEnableFpga(true)
                                            .withNetworkSecurityGroup(new SubResource().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/networkSecurityGroups/{networkSecurityGroupName}"))
                                            .withDnsSettings(new VirtualMachineScaleSetNetworkConfigurationDnsSettings()
                                                .withDnsServers(Arrays.asList("sjpmlu")))
                                            .withIpConfigurations(
                                                Arrays.asList(new VirtualMachineScaleSetIPConfiguration()
                                                    .withName("fweiphgkyhbcsbfjmxzczkpg")
                                                    .withProperties(
                                                        new VirtualMachineScaleSetIPConfigurationProperties()
                                                            .withSubnet(new ApiEntityReference().withId(
                                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/virtualNetworks/{virtualNetworkName}/subnets/{subnetName}"))
                                                            .withPrimary(true)
                                                            .withPublicIPAddressConfiguration(
                                                                new VirtualMachineScaleSetPublicIPAddressConfiguration()
                                                                    .withName("dvnoamqjyshquvtmf")
                                                                    .withProperties(
                                                                        new VirtualMachineScaleSetPublicIPAddressConfigurationProperties()
                                                                            .withIdleTimeoutInMinutes(1)
                                                                            .withDnsSettings(
                                                                                new VirtualMachineScaleSetPublicIPAddressConfigurationDnsSettings()
                                                                                    .withDomainNameLabel("ayofnb")
                                                                                    .withDomainNameLabelScope(
                                                                                        DomainNameLabelScopeTypes.TENANT_REUSE))
                                                                            .withIpTags(Arrays.asList(
                                                                                new VirtualMachineScaleSetIpTag()
                                                                                    .withIpTagType("zqpznczmc")
                                                                                    .withTag(
                                                                                        "ugnfzikniqjisffrbvryavenhmtd")))
                                                                            .withPublicIPPrefix(
                                                                                new SubResource().withId(
                                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/publicIPPrefixes/{publicIPPrefixName}"))
                                                                            .withPublicIPAddressVersion(IPVersion.IPV4)
                                                                            .withDeleteOption(DeleteOptions.DELETE))
                                                                    .withSku(new PublicIPAddressSku()
                                                                        .withName(PublicIPAddressSkuName.BASIC)
                                                                        .withTier(PublicIPAddressSkuTier.REGIONAL)))
                                                            .withPrivateIPAddressVersion(IPVersion.IPV4)
                                                            .withApplicationGatewayBackendAddressPools(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/applicationGateways/{applicationGatewayName}/backendAddressPools/{backendAddressPoolName}")))
                                                            .withApplicationSecurityGroups(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/applicationSecurityGroups/{applicationSecurityGroupName}")))
                                                            .withLoadBalancerBackendAddressPools(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/backendAddressPools/{backendAddressPoolName}")))
                                                            .withLoadBalancerInboundNatPools(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/inboundNatPools/{inboundNatPoolName}"))))))
                                            .withEnableIPForwarding(true)
                                            .withDeleteOption(DeleteOptions.DELETE)
                                            .withAuxiliaryMode(NetworkInterfaceAuxiliaryMode.NONE)
                                            .withAuxiliarySku(NetworkInterfaceAuxiliarySku.NONE))))
                                .withNetworkApiVersion(NetworkApiVersion.V2020_11_01))
                            .withSecurityProfile(new SecurityProfile()
                                .withUefiSettings(new UefiSettings().withSecureBootEnabled(true).withVTpmEnabled(true))
                                .withEncryptionAtHost(true)
                                .withSecurityType(SecurityTypes.TRUSTED_LAUNCH)
                                .withEncryptionIdentity(new EncryptionIdentity().withUserAssignedIdentityResourceId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{userAssignedIdentityName}"))
                                .withProxyAgentSettings(new ProxyAgentSettings().withEnabled(true)
                                    .withMode(Mode.AUDIT)
                                    .withKeyIncarnationId(6)))
                            .withDiagnosticsProfile(new DiagnosticsProfile().withBootDiagnostics(
                                new BootDiagnostics().withEnabled(true).withStorageUri("https://microsoft.com/a")))
                            .withExtensionProfile(new VirtualMachineScaleSetExtensionProfile()
                                .withExtensions(Arrays.asList(new VirtualMachineScaleSetExtension()
                                    .withName("oredyuufsd")
                                    .withProperties(new VirtualMachineScaleSetExtensionProperties()
                                        .withForceUpdateTag("muglieujh")
                                        .withPublisher("ccbiyfuveemaaopgxbjpm")
                                        .withType("yorumzkbfpxnrdwgczwwaeaxmda")
                                        .withTypeHandlerVersion("nlnqbmgzwubbc")
                                        .withAutoUpgradeMinorVersion(true)
                                        .withEnableAutomaticUpgrade(true)
                                        .withSettings(mapOf())
                                        .withProtectedSettings(mapOf())
                                        .withProvisionAfterExtensions(Arrays.asList("xuefrutmgzsxrpjjayvy"))
                                        .withSuppressFailures(true)
                                        .withProtectedSettingsFromKeyVault(new KeyVaultSecretReference()
                                            .withSecretUrl("fakeTokenPlaceholder")
                                            .withSourceVault(new SubResource().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))))))
                                .withExtensionsTimeBudget("trluxrynunvdnukztblhbnsubr"))
                            .withLicenseType("ginsqshzwimjteiyfxhnjxfrcaat")
                            .withScheduledEventsProfile(new ScheduledEventsProfile()
                                .withTerminateNotificationProfile(
                                    new TerminateNotificationProfile().withNotBeforeTimeout("plbazenobaeueixatewbey")
                                        .withEnable(true))
                                .withOsImageNotificationProfile(
                                    new OSImageNotificationProfile().withNotBeforeTimeout("ednjvcedpjmczw")
                                        .withEnable(true)))
                            .withUserData("zekdr")
                            .withCapacityReservation(
                                new CapacityReservationProfile().withCapacityReservationGroup(new SubResource().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/capacityReservationGroups/{capacityReservationGroupName}")))
                            .withApplicationProfile(new ApplicationProfile()
                                .withGalleryApplications(Arrays.asList(new VMGalleryApplication().withTags("eomzidad")
                                    .withOrder(22)
                                    .withPackageReferenceId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/applications/{applicationName}/versions/{versionName}")
                                    .withConfigurationReference("zdqfcpvt")
                                    .withTreatFailureAsDeploymentFailure(true)
                                    .withEnableAutomaticUpgrade(true))))
                            .withHardwareProfile(new VirtualMachineScaleSetHardwareProfile().withVmSizeProperties(
                                new VMSizeProperties().withVCPUsAvailable(8).withVCPUsPerCore(17)))
                            .withServiceArtifactReference(new ServiceArtifactReference().withId(
                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/serviceArtifacts/{serviceArtifactsName}/vmArtifactsProfiles/{vmArtifactsProfileName}"))
                            .withSecurityPostureReference(new SecurityPostureReference().withId(
                                "/CommunityGalleries/{communityGalleryName}/securityPostures/{securityPostureName}/versions/{major.minor.patch}|{major.*}|latest")
                                .withExcludeExtensions(Arrays.asList("ragwgzswxzzz"))
                                .withIsOverridable(true))))))
                .withComputeProfile(
                    new ComputeProfile()
                        .withBaseVirtualMachineProfile(new BaseVirtualMachineProfile()
                            .withOsProfile(new VirtualMachineScaleSetOSProfile().withComputerNamePrefix("o")
                                .withAdminUsername("nrgzqciiaaxjrqldbmjbqkyhntp")
                                .withAdminPassword("fakeTokenPlaceholder")
                                .withCustomData("xjjib")
                                .withWindowsConfiguration(new WindowsConfiguration().withProvisionVMAgent(true)
                                    .withEnableAutomaticUpdates(true)
                                    .withTimeZone("hlyjiqcfksgrpjrct")
                                    .withAdditionalUnattendContent(
                                        Arrays.asList(new AdditionalUnattendContent()
                                            .withPassName(AdditionalUnattendContentPassName.OOBE_SYSTEM)
                                            .withComponentName(
                                                AdditionalUnattendContentComponentName.MICROSOFT_WINDOWS_SHELL_SETUP)
                                            .withSettingName(SettingNames.AUTO_LOGON)
                                            .withContent("bubmqbxjkj")))
                                    .withPatchSettings(new PatchSettings()
                                        .withPatchMode(WindowsVMGuestPatchMode.MANUAL)
                                        .withEnableHotpatching(true)
                                        .withAssessmentMode(WindowsPatchAssessmentMode.IMAGE_DEFAULT)
                                        .withAutomaticByPlatformSettings(
                                            new WindowsVMGuestPatchAutomaticByPlatformSettings()
                                                .withRebootSetting(
                                                    WindowsVMGuestPatchAutomaticByPlatformRebootSetting.UNKNOWN)
                                                .withBypassPlatformSafetyChecksOnUserSchedule(true)))
                                    .withWinRM(new WinRMConfiguration().withListeners(Arrays.asList(new WinRMListener()
                                        .withProtocol(ProtocolTypes.HTTP)
                                        .withCertificateUrl("https://myVaultName.vault.azure.net/secrets/myCertName"))))
                                    .withEnableVMAgentPlatformUpdates(true))
                                .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSsh(new SshConfiguration().withPublicKeys(Arrays.asList(
                                        new SshPublicKey().withPath("kmqz").withKeyData("fakeTokenPlaceholder"))))
                                    .withProvisionVMAgent(true)
                                    .withPatchSettings(new LinuxPatchSettings()
                                        .withPatchMode(LinuxVMGuestPatchMode.IMAGE_DEFAULT)
                                        .withAssessmentMode(LinuxPatchAssessmentMode.IMAGE_DEFAULT)
                                        .withAutomaticByPlatformSettings(
                                            new LinuxVMGuestPatchAutomaticByPlatformSettings()
                                                .withRebootSetting(
                                                    LinuxVMGuestPatchAutomaticByPlatformRebootSetting.UNKNOWN)
                                                .withBypassPlatformSafetyChecksOnUserSchedule(true)))
                                    .withEnableVMAgentPlatformUpdates(true))
                                .withSecrets(Arrays.asList(new VaultSecretGroup()
                                    .withSourceVault(new SubResource().withId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))
                                    .withVaultCertificates(Arrays.asList(new VaultCertificate()
                                        .withCertificateUrl("https://myVaultName.vault.azure.net/secrets/myCertName")
                                        .withCertificateStore("nlxrwavpzhueffxsshlun")))))
                                .withAllowExtensionOperations(true)
                                .withRequireGuestProvisionSignal(true))
                            .withStorageProfile(new VirtualMachineScaleSetStorageProfile()
                                .withImageReference(new ImageReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/images/{imageName}/versions/{versionName}")
                                    .withPublisher("mqxgwbiyjzmxavhbkd")
                                    .withOffer("isxgumkarlkomp")
                                    .withSku("eojmppqcrnpmxirtp")
                                    .withVersion("wvpcqefgtmqdgltiuz")
                                    .withSharedGalleryImageId("kmkgihoxwlawuuhcinfirktdwkmx")
                                    .withCommunityGalleryImageId("vlqe"))
                                .withOsDisk(new VirtualMachineScaleSetOSDisk().withName("wfttw")
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiffDiskSettings(new DiffDiskSettings()
                                        .withOption(DiffDiskOptions.LOCAL)
                                        .withPlacement(DiffDiskPlacement.CACHE_DISK))
                                    .withDiskSizeGB(14)
                                    .withOsType(OperatingSystemTypes.WINDOWS)
                                    .withImage(new VirtualHardDisk().withUri(
                                        "https://myStorageAccountName.blob.core.windows.net/myContainerName/myVhdName.vhd"))
                                    .withVhdContainers(Arrays.asList("tkzcwddtinkfpnfklatw"))
                                    .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(
                                                new DiskEncryptionSetParameters().withId(
                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))))
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE))
                                .withDataDisks(Arrays.asList(new VirtualMachineScaleSetDataDisk()
                                    .withName("eogiykmdmeikswxmigjws")
                                    .withLun(14)
                                    .withCaching(CachingTypes.NONE)
                                    .withWriteAcceleratorEnabled(true)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDiskSizeGB(6)
                                    .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                                        .withStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                                        .withDiskEncryptionSet(new DiskEncryptionSetParameters().withId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))
                                        .withSecurityProfile(new VMDiskSecurityProfile()
                                            .withSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY)
                                            .withDiskEncryptionSet(
                                                new DiskEncryptionSetParameters().withId(
                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/diskEncryptionSets/{diskEncryptionSetName}"))))
                                    .withDiskIOPSReadWrite(27L)
                                    .withDiskMBpsReadWrite(2L)
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE)))
                                .withDiskControllerType(DiskControllerTypes.fromString("uzb")))
                            .withNetworkProfile(new VirtualMachineScaleSetNetworkProfile()
                                .withHealthProbe(new ApiEntityReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/probes/{probeName}"))
                                .withNetworkInterfaceConfigurations(
                                    Arrays.asList(new VirtualMachineScaleSetNetworkConfiguration().withName("i")
                                        .withProperties(new VirtualMachineScaleSetNetworkConfigurationProperties()
                                            .withPrimary(true)
                                            .withEnableAcceleratedNetworking(true)
                                            .withDisableTcpStateTracking(true)
                                            .withEnableFpga(true)
                                            .withNetworkSecurityGroup(new SubResource().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/networkSecurityGroups/{networkSecurityGroupName}"))
                                            .withDnsSettings(new VirtualMachineScaleSetNetworkConfigurationDnsSettings()
                                                .withDnsServers(Arrays.asList("nxmmfolhclsesu")))
                                            .withIpConfigurations(
                                                Arrays.asList(new VirtualMachineScaleSetIPConfiguration()
                                                    .withName("oezqhkidfhyywlfzwuotilrpbqnjg")
                                                    .withProperties(
                                                        new VirtualMachineScaleSetIPConfigurationProperties()
                                                            .withSubnet(new ApiEntityReference().withId(
                                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/virtualNetworks/{virtualNetworkName}/subnets/{subnetName}"))
                                                            .withPrimary(true)
                                                            .withPublicIPAddressConfiguration(
                                                                new VirtualMachineScaleSetPublicIPAddressConfiguration()
                                                                    .withName("fvpqf")
                                                                    .withProperties(
                                                                        new VirtualMachineScaleSetPublicIPAddressConfigurationProperties()
                                                                            .withIdleTimeoutInMinutes(9)
                                                                            .withDnsSettings(
                                                                                new VirtualMachineScaleSetPublicIPAddressConfigurationDnsSettings()
                                                                                    .withDomainNameLabel(
                                                                                        "ukrddzvmorpmfsczjwtbvp")
                                                                                    .withDomainNameLabelScope(
                                                                                        DomainNameLabelScopeTypes.TENANT_REUSE))
                                                                            .withIpTags(Arrays.asList(
                                                                                new VirtualMachineScaleSetIpTag()
                                                                                    .withIpTagType("sddgsoemnzgqizale")
                                                                                    .withTag("wufmhrjsakbiaetyara")))
                                                                            .withPublicIPPrefix(
                                                                                new SubResource().withId(
                                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/publicIPPrefixes/{publicIPPrefixName}"))
                                                                            .withPublicIPAddressVersion(IPVersion.IPV4)
                                                                            .withDeleteOption(DeleteOptions.DELETE))
                                                                    .withSku(new PublicIPAddressSku()
                                                                        .withName(PublicIPAddressSkuName.BASIC)
                                                                        .withTier(PublicIPAddressSkuTier.REGIONAL)))
                                                            .withPrivateIPAddressVersion(IPVersion.IPV4)
                                                            .withApplicationGatewayBackendAddressPools(Arrays
                                                                .asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/applicationGateways/{applicationGatewayName}/backendAddressPools/{backendAddressPoolName}")))
                                                            .withApplicationSecurityGroups(Arrays
                                                                .asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/applicationSecurityGroups/{applicationSecurityGroupName}")))
                                                            .withLoadBalancerBackendAddressPools(Arrays
                                                                .asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/backendAddressPools/{backendAddressPoolName}")))
                                                            .withLoadBalancerInboundNatPools(
                                                                Arrays.asList(new SubResource().withId(
                                                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/loadBalancers/{loadBalancerName}/inboundNatPools/{inboundNatPoolName}"))))))
                                            .withEnableIPForwarding(true)
                                            .withDeleteOption(DeleteOptions.DELETE)
                                            .withAuxiliaryMode(NetworkInterfaceAuxiliaryMode.NONE)
                                            .withAuxiliarySku(NetworkInterfaceAuxiliarySku.NONE))))
                                .withNetworkApiVersion(NetworkApiVersion.V2020_11_01))
                            .withSecurityProfile(new SecurityProfile()
                                .withUefiSettings(new UefiSettings().withSecureBootEnabled(true).withVTpmEnabled(true))
                                .withEncryptionAtHost(true)
                                .withSecurityType(SecurityTypes.TRUSTED_LAUNCH)
                                .withEncryptionIdentity(new EncryptionIdentity().withUserAssignedIdentityResourceId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{userAssignedIdentityName}"))
                                .withProxyAgentSettings(new ProxyAgentSettings().withEnabled(true)
                                    .withMode(Mode.AUDIT)
                                    .withKeyIncarnationId(20)))
                            .withDiagnosticsProfile(
                                new DiagnosticsProfile().withBootDiagnostics(new BootDiagnostics()
                                    .withEnabled(true)
                                    .withStorageUri("http://myStorageAccountName.blob.core.windows.net")))
                            .withExtensionProfile(new VirtualMachineScaleSetExtensionProfile()
                                .withExtensions(Arrays.asList(new VirtualMachineScaleSetExtension().withName("bndxuxx")
                                    .withProperties(new VirtualMachineScaleSetExtensionProperties()
                                        .withForceUpdateTag("yhgxw")
                                        .withPublisher("kpxtirxjfprhs")
                                        .withType("pgjilctjjwaa")
                                        .withTypeHandlerVersion("zevivcoilxmbwlrihhhibq")
                                        .withAutoUpgradeMinorVersion(true)
                                        .withEnableAutomaticUpgrade(true)
                                        .withSettings(mapOf())
                                        .withProtectedSettings(mapOf())
                                        .withProvisionAfterExtensions(Arrays.asList("nftzosroolbcwmpupujzqwqe"))
                                        .withSuppressFailures(true)
                                        .withProtectedSettingsFromKeyVault(new KeyVaultSecretReference()
                                            .withSecretUrl("fakeTokenPlaceholder")
                                            .withSourceVault(new SubResource().withId(
                                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))))))
                                .withExtensionsTimeBudget("mbhjahtdygwgyszdwjtvlvtgchdwil"))
                            .withLicenseType("v")
                            .withScheduledEventsProfile(new ScheduledEventsProfile()
                                .withTerminateNotificationProfile(new TerminateNotificationProfile()
                                    .withNotBeforeTimeout("iljppmmw")
                                    .withEnable(true))
                                .withOsImageNotificationProfile(
                                    new OSImageNotificationProfile().withNotBeforeTimeout("olbpadmevekyczfokodtfprxti")
                                        .withEnable(true)))
                            .withUserData("s")
                            .withCapacityReservation(new CapacityReservationProfile()
                                .withCapacityReservationGroup(new SubResource().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/capacityReservationGroups/{capacityReservationGroupName}")))
                            .withApplicationProfile(new ApplicationProfile().withGalleryApplications(
                                Arrays.asList(new VMGalleryApplication().withTags("eyrqjbib")
                                    .withOrder(5)
                                    .withPackageReferenceId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/applications/{applicationName}/versions/{versionName}")
                                    .withConfigurationReference("ulztmiavpojpbpbddgnuuiimxcpau")
                                    .withTreatFailureAsDeploymentFailure(true)
                                    .withEnableAutomaticUpgrade(true))))
                            .withHardwareProfile(
                                new VirtualMachineScaleSetHardwareProfile().withVmSizeProperties(new VMSizeProperties()
                                    .withVCPUsAvailable(16)
                                    .withVCPUsPerCore(23)))
                            .withServiceArtifactReference(new ServiceArtifactReference().withId(
                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/serviceArtifacts/{serviceArtifactsName}/vmArtifactsProfiles/{vmArtifactsProfileName}"))
                            .withSecurityPostureReference(new SecurityPostureReference().withId(
                                "/CommunityGalleries/{communityGalleryName}/securityPostures/{securityPostureName}/versions/{major.minor.patch}|{major.*}|latest")
                                .withExcludeExtensions(Arrays.asList("{securityPostureVMExtensionName}"))
                                .withIsOverridable(true)))
                        .withComputeApiVersion("2023-07-01")
                        .withPlatformFaultDomainCount(1)
                        .withAdditionalVirtualMachineCapabilities(
                            new AdditionalCapabilities().withUltraSSDEnabled(true).withHibernationEnabled(true)))
                .withMode(FleetMode.MANAGED)
                .withCapacityType(CapacityType.VM)
                .withZoneAllocationPolicy(new ZoneAllocationPolicy()
                    .withDistributionStrategy(ZoneDistributionStrategy.BEST_EFFORT_SINGLE_ZONE)))
            .apply();
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void operationsList(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

