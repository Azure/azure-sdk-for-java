# Code snippets and samples


## Fleets

- [CreateOrUpdate](#fleets_createorupdate)
- [Delete](#fleets_delete)
- [GetByResourceGroup](#fleets_getbyresourcegroup)
- [List](#fleets_list)
- [ListByResourceGroup](#fleets_listbyresourcegroup)
- [ListVirtualMachineScaleSets](#fleets_listvirtualmachinescalesets)
- [Update](#fleets_update)

## Operations

- [List](#operations_list)
### Fleets_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContent;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContentComponentName;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContentPassName;
import com.azure.resourcemanager.computefleet.models.ApiEntityReference;
import com.azure.resourcemanager.computefleet.models.ApplicationProfile;
import com.azure.resourcemanager.computefleet.models.BaseVirtualMachineProfile;
import com.azure.resourcemanager.computefleet.models.BootDiagnostics;
import com.azure.resourcemanager.computefleet.models.CachingTypes;
import com.azure.resourcemanager.computefleet.models.CapacityReservationProfile;
import com.azure.resourcemanager.computefleet.models.ComputeProfile;
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
import com.azure.resourcemanager.computefleet.models.FleetProperties;
import com.azure.resourcemanager.computefleet.models.ImageReference;
import com.azure.resourcemanager.computefleet.models.IPVersion;
import com.azure.resourcemanager.computefleet.models.KeyVaultSecretReference;
import com.azure.resourcemanager.computefleet.models.LinuxConfiguration;
import com.azure.resourcemanager.computefleet.models.LinuxPatchAssessmentMode;
import com.azure.resourcemanager.computefleet.models.LinuxPatchSettings;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchMode;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentity;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.computefleet.models.Mode;
import com.azure.resourcemanager.computefleet.models.NetworkApiVersion;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliaryMode;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliarySku;
import com.azure.resourcemanager.computefleet.models.OperatingSystemTypes;
import com.azure.resourcemanager.computefleet.models.OSImageNotificationProfile;
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
import com.azure.resourcemanager.computefleet.models.UserAssignedIdentity;
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
import com.azure.resourcemanager.computefleet.models.VMDiskSecurityProfile;
import com.azure.resourcemanager.computefleet.models.VMGalleryApplication;
import com.azure.resourcemanager.computefleet.models.VmSizeProfile;
import com.azure.resourcemanager.computefleet.models.VMSizeProperties;
import com.azure.resourcemanager.computefleet.models.WindowsConfiguration;
import com.azure.resourcemanager.computefleet.models.WindowsPatchAssessmentMode;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchMode;
import com.azure.resourcemanager.computefleet.models.WinRMConfiguration;
import com.azure.resourcemanager.computefleet.models.WinRMListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Fleets CreateOrUpdate.
 */
public final class FleetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurefleet/AzureFleet.Management/examples/2024-05-01-preview/Fleets_CreateOrUpdate.json
     */
    /**
     * Sample code: Fleets_CreateOrUpdate.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsCreateOrUpdate(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets()
            .define("testFleet")
            .withRegion("westus")
            .withExistingResourceGroup("rgazurefleet")
            .withTags(mapOf("key3518", "fakeTokenPlaceholder"))
            .withProperties(new FleetProperties().withSpotPriorityProfile(new SpotPriorityProfile().withCapacity(20)
                .withMinCapacity(10)
                .withMaxPricePerVM(0.00865D)
                .withEvictionPolicy(EvictionPolicy.DELETE)
                .withAllocationStrategy(SpotAllocationStrategy.PRICE_CAPACITY_OPTIMIZED)
                .withMaintain(true))
                .withRegularPriorityProfile(new RegularPriorityProfile().withCapacity(20)
                    .withMinCapacity(10)
                    .withAllocationStrategy(RegularPriorityAllocationStrategy.LOWEST_PRICE))
                .withVmSizesProfile(Arrays.asList(new VmSizeProfile().withName("Standard_d1_v2").withRank(19225)))
                .withComputeProfile(
                    new ComputeProfile()
                        .withBaseVirtualMachineProfile(
                            new BaseVirtualMachineProfile()
                                .withOsProfile(new VirtualMachineScaleSetOSProfile().withComputerNamePrefix("o")
                                    .withAdminUsername("nrgzqciiaaxjrqldbmjbqkyhntp")
                                    .withAdminPassword("fakeTokenPlaceholder")
                                    .withCustomData("xjjib")
                                    .withWindowsConfiguration(new WindowsConfiguration().withProvisionVMAgent(true)
                                        .withEnableAutomaticUpdates(true)
                                        .withTimeZone("hlyjiqcfksgrpjrct")
                                        .withAdditionalUnattendContent(Arrays.asList(new AdditionalUnattendContent()
                                            .withPassName(AdditionalUnattendContentPassName.OOBE_SYSTEM)
                                            .withComponentName(
                                                AdditionalUnattendContentComponentName.MICROSOFT_WINDOWS_SHELL_SETUP)
                                            .withSettingName(SettingNames.AUTO_LOGON)
                                            .withContent("bubmqbxjkj")))
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
                                                .withCertificateUrl("phwesineizrl"))))
                                        .withEnableVMAgentPlatformUpdates(true))
                                    .withLinuxConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withPath("kmqz")
                                                .withKeyData("fakeTokenPlaceholder"))))
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
                                        .withVaultCertificates(
                                            Arrays.asList(new VaultCertificate().withCertificateUrl("tyldwkzafmnkvpo")
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
                                        .withImage(new VirtualHardDisk().withUri("thqceubivdrjs"))
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
                                .withNetworkProfile(new VirtualMachineScaleSetNetworkProfile().withHealthProbe(
                                    new ApiEntityReference().withId(
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
                                                .withDnsSettings(
                                                    new VirtualMachineScaleSetNetworkConfigurationDnsSettings()
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
                                                                                        .withIpTagType(
                                                                                            "sddgsoemnzgqizale")
                                                                                        .withTag(
                                                                                            "wufmhrjsakbiaetyara")))
                                                                                .withPublicIPPrefix(
                                                                                    new SubResource().withId(
                                                                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/publicIPPrefixes/{publicIPPrefixName}"))
                                                                                .withPublicIPAddressVersion(
                                                                                    IPVersion.IPV4)
                                                                                .withDeleteOption(DeleteOptions.DELETE))
                                                                        .withSku(new PublicIPAddressSku()
                                                                            .withName(PublicIPAddressSkuName.BASIC)
                                                                            .withTier(PublicIPAddressSkuTier.REGIONAL)))
                                                                .withPrivateIPAddressVersion(IPVersion.IPV4)
                                                                .withApplicationGatewayBackendAddressPools(
                                                                    Arrays.asList(
                                                                        new SubResource().withId(
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
                                    .withNetworkApiVersion(NetworkApiVersion.TWO_ZERO_TWO_ZERO_ONE_ONE_ZERO_ONE))
                                .withSecurityProfile(new SecurityProfile()
                                    .withUefiSettings(
                                        new UefiSettings().withSecureBootEnabled(true).withVTpmEnabled(true))
                                    .withEncryptionAtHost(true)
                                    .withSecurityType(SecurityTypes.TRUSTED_LAUNCH)
                                    .withEncryptionIdentity(new EncryptionIdentity().withUserAssignedIdentityResourceId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{userAssignedIdentityName}"))
                                    .withProxyAgentSettings(new ProxyAgentSettings()
                                        .withEnabled(true)
                                        .withMode(Mode.AUDIT)
                                        .withKeyIncarnationId(20)))
                                .withDiagnosticsProfile(
                                    new DiagnosticsProfile().withBootDiagnostics(new BootDiagnostics()
                                        .withEnabled(true)
                                        .withStorageUri("rzamfwghybpx")))
                                .withExtensionProfile(new VirtualMachineScaleSetExtensionProfile()
                                    .withExtensions(Arrays.asList(new VirtualMachineScaleSetExtension()
                                        .withName("bndxuxx")
                                        .withProperties(
                                            new VirtualMachineScaleSetExtensionProperties().withForceUpdateTag("yhgxw")
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
                                                    .withSourceVault(
                                                        new SubResource().withId(
                                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))))))
                                    .withExtensionsTimeBudget("mbhjahtdygwgyszdwjtvlvtgchdwil"))
                                .withLicenseType("v")
                                .withScheduledEventsProfile(new ScheduledEventsProfile()
                                    .withTerminateNotificationProfile(
                                        new TerminateNotificationProfile().withNotBeforeTimeout("iljppmmw")
                                            .withEnable(true))
                                    .withOsImageNotificationProfile(new OSImageNotificationProfile()
                                        .withNotBeforeTimeout("olbpadmevekyczfokodtfprxti")
                                        .withEnable(true)))
                                .withUserData("s")
                                .withCapacityReservation(new CapacityReservationProfile()
                                    .withCapacityReservationGroup(new SubResource().withId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/capacityReservationGroups/{capacityReservationGroupName}")))
                                .withApplicationProfile(new ApplicationProfile().withGalleryApplications(Arrays
                                    .asList(new VMGalleryApplication().withTags("eyrqjbib")
                                        .withOrder(5)
                                        .withPackageReferenceId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/applications/{applicationName}/versions/{versionName}")
                                        .withConfigurationReference("ulztmiavpojpbpbddgnuuiimxcpau")
                                        .withTreatFailureAsDeploymentFailure(true)
                                        .withEnableAutomaticUpgrade(true))))
                                .withHardwareProfile(new VirtualMachineScaleSetHardwareProfile()
                                    .withVmSizeProperties(new VMSizeProperties()
                                        .withVCPUsAvailable(16)
                                        .withVCPUsPerCore(23)))
                                .withServiceArtifactReference(new ServiceArtifactReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/serviceArtifacts/{serviceArtifactsName}/vmArtifactsProfiles/{vmArtifactsProfileName}"))
                                .withSecurityPostureReference(new SecurityPostureReference().withId(
                                    "/CommunityGalleries/{communityGalleryName}/securityPostures/{securityPostureName}/versions/{major.minor.patch}|{major.*}|latest")
                                    .withExcludeExtensions(Arrays.asList("{securityPostureVMExtensionName}"))
                                    .withIsOverridable(true)))
                        .withComputeApiVersion("2023-07-01")
                        .withPlatformFaultDomainCount(1)))
            .withZones(Arrays.asList("zone1", "zone2"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf("key9851", new UserAssignedIdentity())))
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
     * x-ms-original-file: specification/azurefleet/AzureFleet.Management/examples/2024-05-01-preview/Fleets_Delete.json
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
     * x-ms-original-file: specification/azurefleet/AzureFleet.Management/examples/2024-05-01-preview/Fleets_Get.json
     */
    /**
     * Sample code: Fleets_Get.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsGet(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets().getByResourceGroupWithResponse("rgazurefleet", "testFleet", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/azurefleet/AzureFleet.Management/examples/2024-05-01-preview/Fleets_ListBySubscription.json
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
     * x-ms-original-file:
     * specification/azurefleet/AzureFleet.Management/examples/2024-05-01-preview/Fleets_ListByResourceGroup.json
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
     * x-ms-original-file:
     * specification/azurefleet/AzureFleet.Management/examples/2024-05-01-preview/Fleets_ListVirtualMachineScaleSets.
     * json
     */
    /**
     * Sample code: Fleets_ListVirtualMachineScaleSets.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void
        fleetsListVirtualMachineScaleSets(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        manager.fleets()
            .listVirtualMachineScaleSetsWithResponse("rgazurefleet", "myFleet", com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_Update

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContent;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContentComponentName;
import com.azure.resourcemanager.computefleet.models.AdditionalUnattendContentPassName;
import com.azure.resourcemanager.computefleet.models.ApiEntityReference;
import com.azure.resourcemanager.computefleet.models.ApplicationProfile;
import com.azure.resourcemanager.computefleet.models.BaseVirtualMachineProfile;
import com.azure.resourcemanager.computefleet.models.BootDiagnostics;
import com.azure.resourcemanager.computefleet.models.CachingTypes;
import com.azure.resourcemanager.computefleet.models.CapacityReservationProfile;
import com.azure.resourcemanager.computefleet.models.ComputeProfile;
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
import com.azure.resourcemanager.computefleet.models.FleetProperties;
import com.azure.resourcemanager.computefleet.models.ImageReference;
import com.azure.resourcemanager.computefleet.models.IPVersion;
import com.azure.resourcemanager.computefleet.models.KeyVaultSecretReference;
import com.azure.resourcemanager.computefleet.models.LinuxConfiguration;
import com.azure.resourcemanager.computefleet.models.LinuxPatchAssessmentMode;
import com.azure.resourcemanager.computefleet.models.LinuxPatchSettings;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.computefleet.models.LinuxVMGuestPatchMode;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentityUpdate;
import com.azure.resourcemanager.computefleet.models.Mode;
import com.azure.resourcemanager.computefleet.models.NetworkApiVersion;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliaryMode;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliarySku;
import com.azure.resourcemanager.computefleet.models.OperatingSystemTypes;
import com.azure.resourcemanager.computefleet.models.OSImageNotificationProfile;
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
import com.azure.resourcemanager.computefleet.models.VMDiskSecurityProfile;
import com.azure.resourcemanager.computefleet.models.VMGalleryApplication;
import com.azure.resourcemanager.computefleet.models.VmSizeProfile;
import com.azure.resourcemanager.computefleet.models.VMSizeProperties;
import com.azure.resourcemanager.computefleet.models.WindowsConfiguration;
import com.azure.resourcemanager.computefleet.models.WindowsPatchAssessmentMode;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchAutomaticByPlatformRebootSetting;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchAutomaticByPlatformSettings;
import com.azure.resourcemanager.computefleet.models.WindowsVMGuestPatchMode;
import com.azure.resourcemanager.computefleet.models.WinRMConfiguration;
import com.azure.resourcemanager.computefleet.models.WinRMListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Fleets Update.
 */
public final class FleetsUpdateSamples {
    /*
     * x-ms-original-file: specification/azurefleet/AzureFleet.Management/examples/2024-05-01-preview/Fleets_Update.json
     */
    /**
     * Sample code: Fleets_Update.
     * 
     * @param manager Entry point to ComputeFleetManager.
     */
    public static void fleetsUpdate(com.azure.resourcemanager.computefleet.ComputeFleetManager manager) {
        Fleet resource = manager.fleets()
            .getByResourceGroupWithResponse("rgazurefleet", "testFleet", com.azure.core.util.Context.NONE)
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
            .withProperties(new FleetProperties()
                .withSpotPriorityProfile(new SpotPriorityProfile().withCapacity(20)
                    .withMinCapacity(10)
                    .withMaxPricePerVM(0.00865D)
                    .withEvictionPolicy(EvictionPolicy.DELETE)
                    .withAllocationStrategy(SpotAllocationStrategy.PRICE_CAPACITY_OPTIMIZED)
                    .withMaintain(true))
                .withRegularPriorityProfile(new RegularPriorityProfile().withCapacity(20)
                    .withMinCapacity(10)
                    .withAllocationStrategy(RegularPriorityAllocationStrategy.LOWEST_PRICE))
                .withVmSizesProfile(Arrays.asList(new VmSizeProfile().withName("Standard_d1_v2").withRank(19225)))
                .withComputeProfile(
                    new ComputeProfile()
                        .withBaseVirtualMachineProfile(
                            new BaseVirtualMachineProfile()
                                .withOsProfile(new VirtualMachineScaleSetOSProfile().withComputerNamePrefix("o")
                                    .withAdminUsername("nrgzqciiaaxjrqldbmjbqkyhntp")
                                    .withAdminPassword("fakeTokenPlaceholder")
                                    .withCustomData("xjjib")
                                    .withWindowsConfiguration(new WindowsConfiguration().withProvisionVMAgent(true)
                                        .withEnableAutomaticUpdates(true)
                                        .withTimeZone("hlyjiqcfksgrpjrct")
                                        .withAdditionalUnattendContent(Arrays.asList(new AdditionalUnattendContent()
                                            .withPassName(AdditionalUnattendContentPassName.OOBE_SYSTEM)
                                            .withComponentName(
                                                AdditionalUnattendContentComponentName.MICROSOFT_WINDOWS_SHELL_SETUP)
                                            .withSettingName(SettingNames.AUTO_LOGON)
                                            .withContent("bubmqbxjkj")))
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
                                                .withCertificateUrl("phwesineizrl"))))
                                        .withEnableVMAgentPlatformUpdates(true))
                                    .withLinuxConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withPath("kmqz")
                                                .withKeyData("fakeTokenPlaceholder"))))
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
                                        .withVaultCertificates(
                                            Arrays.asList(new VaultCertificate().withCertificateUrl("tyldwkzafmnkvpo")
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
                                        .withImage(new VirtualHardDisk().withUri("thqceubivdrjs"))
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
                                .withNetworkProfile(new VirtualMachineScaleSetNetworkProfile().withHealthProbe(
                                    new ApiEntityReference().withId(
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
                                                .withDnsSettings(
                                                    new VirtualMachineScaleSetNetworkConfigurationDnsSettings()
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
                                                                                        .withIpTagType(
                                                                                            "sddgsoemnzgqizale")
                                                                                        .withTag(
                                                                                            "wufmhrjsakbiaetyara")))
                                                                                .withPublicIPPrefix(
                                                                                    new SubResource().withId(
                                                                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/publicIPPrefixes/{publicIPPrefixName}"))
                                                                                .withPublicIPAddressVersion(
                                                                                    IPVersion.IPV4)
                                                                                .withDeleteOption(DeleteOptions.DELETE))
                                                                        .withSku(new PublicIPAddressSku()
                                                                            .withName(PublicIPAddressSkuName.BASIC)
                                                                            .withTier(PublicIPAddressSkuTier.REGIONAL)))
                                                                .withPrivateIPAddressVersion(IPVersion.IPV4)
                                                                .withApplicationGatewayBackendAddressPools(
                                                                    Arrays.asList(
                                                                        new SubResource().withId(
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
                                    .withNetworkApiVersion(NetworkApiVersion.TWO_ZERO_TWO_ZERO_ONE_ONE_ZERO_ONE))
                                .withSecurityProfile(new SecurityProfile()
                                    .withUefiSettings(
                                        new UefiSettings().withSecureBootEnabled(true).withVTpmEnabled(true))
                                    .withEncryptionAtHost(true)
                                    .withSecurityType(SecurityTypes.TRUSTED_LAUNCH)
                                    .withEncryptionIdentity(new EncryptionIdentity().withUserAssignedIdentityResourceId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{userAssignedIdentityName}"))
                                    .withProxyAgentSettings(new ProxyAgentSettings()
                                        .withEnabled(true)
                                        .withMode(Mode.AUDIT)
                                        .withKeyIncarnationId(20)))
                                .withDiagnosticsProfile(
                                    new DiagnosticsProfile().withBootDiagnostics(new BootDiagnostics()
                                        .withEnabled(true)
                                        .withStorageUri("rzamfwghybpx")))
                                .withExtensionProfile(new VirtualMachineScaleSetExtensionProfile()
                                    .withExtensions(Arrays.asList(new VirtualMachineScaleSetExtension()
                                        .withName("bndxuxx")
                                        .withProperties(
                                            new VirtualMachineScaleSetExtensionProperties().withForceUpdateTag("yhgxw")
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
                                                    .withSourceVault(
                                                        new SubResource().withId(
                                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.KeyVault/vaults/{vaultName}"))))))
                                    .withExtensionsTimeBudget("mbhjahtdygwgyszdwjtvlvtgchdwil"))
                                .withLicenseType("v")
                                .withScheduledEventsProfile(new ScheduledEventsProfile()
                                    .withTerminateNotificationProfile(
                                        new TerminateNotificationProfile().withNotBeforeTimeout("iljppmmw")
                                            .withEnable(true))
                                    .withOsImageNotificationProfile(new OSImageNotificationProfile()
                                        .withNotBeforeTimeout("olbpadmevekyczfokodtfprxti")
                                        .withEnable(true)))
                                .withUserData("s")
                                .withCapacityReservation(new CapacityReservationProfile()
                                    .withCapacityReservationGroup(new SubResource().withId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/capacityReservationGroups/{capacityReservationGroupName}")))
                                .withApplicationProfile(new ApplicationProfile().withGalleryApplications(Arrays
                                    .asList(new VMGalleryApplication().withTags("eyrqjbib")
                                        .withOrder(5)
                                        .withPackageReferenceId(
                                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/applications/{applicationName}/versions/{versionName}")
                                        .withConfigurationReference("ulztmiavpojpbpbddgnuuiimxcpau")
                                        .withTreatFailureAsDeploymentFailure(true)
                                        .withEnableAutomaticUpgrade(true))))
                                .withHardwareProfile(new VirtualMachineScaleSetHardwareProfile()
                                    .withVmSizeProperties(new VMSizeProperties()
                                        .withVCPUsAvailable(16)
                                        .withVCPUsPerCore(23)))
                                .withServiceArtifactReference(new ServiceArtifactReference().withId(
                                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/galleries/{galleryName}/serviceArtifacts/{serviceArtifactsName}/vmArtifactsProfiles/{vmArtifactsProfileName}"))
                                .withSecurityPostureReference(new SecurityPostureReference().withId(
                                    "/CommunityGalleries/{communityGalleryName}/securityPostures/{securityPostureName}/versions/{major.minor.patch}|{major.*}|latest")
                                    .withExcludeExtensions(Arrays.asList("{securityPostureVMExtensionName}"))
                                    .withIsOverridable(true)))
                        .withComputeApiVersion("2023-07-01")
                        .withPlatformFaultDomainCount(1)))
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
     * x-ms-original-file:
     * specification/azurefleet/AzureFleet.Management/examples/2024-05-01-preview/Operations_List.json
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

