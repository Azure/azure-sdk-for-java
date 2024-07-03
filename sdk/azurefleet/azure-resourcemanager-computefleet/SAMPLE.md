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
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.computefleet.models.ComputeProfile;
import com.azure.resourcemanager.computefleet.models.EvictionPolicy;
import com.azure.resourcemanager.computefleet.models.FleetProperties;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentity;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.computefleet.models.RegularPriorityAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.RegularPriorityProfile;
import com.azure.resourcemanager.computefleet.models.SpotAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.SpotPriorityProfile;
import com.azure.resourcemanager.computefleet.models.UserAssignedIdentity;
import com.azure.resourcemanager.computefleet.models.VmSizeProfile;
import java.io.IOException;
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
    public static void fleetsCreateOrUpdate(com.azure.resourcemanager.computefleet.ComputeFleetManager manager)
        throws IOException {
        manager.fleets()
            .define("testFleet")
            .withRegion("westus")
            .withExistingResourceGroup("rgazurefleet")
            .withTags(mapOf("key3518", "fakeTokenPlaceholder"))
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
                .withComputeProfile(new ComputeProfile().withBaseVirtualMachineProfile(SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"osProfile\":{\"computerNamePrefix\":\"o\",\"adminUsername\":\"nrgzqciiaaxjrqldbmjbqkyhntp\",\"adminPassword\":\"adfbrdxpv\",\"customData\":\"xjjib\",\"windowsConfiguration\":{\"provisionVMAgent\":true,\"enableAutomaticUpdates\":true,\"timeZone\":\"hlyjiqcfksgrpjrct\",\"additionalUnattendContent\":[{\"passName\":\"OobeSystem\",\"componentName\":\"Microsoft-Windows-Shell-Setup\",\"settingName\":\"AutoLogon\",\"content\":\"bubmqbxjkj\"}],\"patchSettings\":{\"patchMode\":\"Manual\",\"enableHotpatching\":true,\"assessmentMode\":\"ImageDefault\",\"automaticByPlatformSettings\":{\"rebootSetting\":\"Unknown\",\"bypassPlatformSafetyChecksOnUserSchedule\":true}},\"winRM\":{\"listeners\":[{\"protocol\":\"Http\",\"certificateUrl\":\"phwesineizrl\"}]},\"enableVMAgentPlatformUpdates\":true},\"linuxConfiguration\":{\"disablePasswordAuthentication\":true,\"ssh\":{\"publicKeys\":[{\"path\":\"kmqz\",\"keyData\":\"kivgsubusvpprwqaqpjcmhsv\"}]},\"provisionVMAgent\":true,\"patchSettings\":{\"patchMode\":\"ImageDefault\",\"assessmentMode\":\"ImageDefault\",\"automaticByPlatformSettings\":{\"rebootSetting\":\"Unknown\",\"bypassPlatformSafetyChecksOnUserSchedule\":true}},\"enableVMAgentPlatformUpdates\":true},\"secrets\":[{\"sourceVault\":{\"id\":\"groxwd\"},\"vaultCertificates\":[{\"certificateUrl\":\"tyldwkzafmnkvpo\",\"certificateStore\":\"nlxrwavpzhueffxsshlun\"}]}],\"allowExtensionOperations\":true,\"requireGuestProvisionSignal\":true},\"storageProfile\":{\"imageReference\":{\"publisher\":\"mqxgwbiyjzmxavhbkd\",\"offer\":\"isxgumkarlkomp\",\"sku\":\"eojmppqcrnpmxirtp\",\"version\":\"wvpcqefgtmqdgltiuz\",\"sharedGalleryImageId\":\"kmkgihoxwlawuuhcinfirktdwkmx\",\"communityGalleryImageId\":\"vlqe\",\"id\":\"aiunknwgksu\"},\"osDisk\":{\"name\":\"wfttw\",\"caching\":\"None\",\"writeAcceleratorEnabled\":true,\"createOption\":\"FromImage\",\"diffDiskSettings\":{\"option\":\"Local\",\"placement\":\"CacheDisk\"},\"diskSizeGB\":14,\"osType\":\"Windows\",\"image\":{\"uri\":\"thqceubivdrjs\"},\"vhdContainers\":[\"tkzcwddtinkfpnfklatw\"],\"managedDisk\":{\"storageAccountType\":\"Standard_LRS\",\"diskEncryptionSet\":{\"id\":\"vmatqblzjalbatdepyzqmnd\"},\"securityProfile\":{\"securityEncryptionType\":\"VMGuestStateOnly\",\"diskEncryptionSet\":{\"id\":\"vmatqblzjalbatdepyzqmnd\"}}},\"deleteOption\":\"Delete\"},\"dataDisks\":[{\"name\":\"eogiykmdmeikswxmigjws\",\"lun\":14,\"caching\":\"None\",\"writeAcceleratorEnabled\":true,\"createOption\":\"FromImage\",\"diskSizeGB\":6,\"managedDisk\":{\"storageAccountType\":\"Standard_LRS\",\"diskEncryptionSet\":{\"id\":\"vmatqblzjalbatdepyzqmnd\"},\"securityProfile\":{\"securityEncryptionType\":\"VMGuestStateOnly\",\"diskEncryptionSet\":{\"id\":\"vmatqblzjalbatdepyzqmnd\"}}},\"diskIOPSReadWrite\":27,\"diskMBpsReadWrite\":2,\"deleteOption\":\"Delete\"}],\"diskControllerType\":\"uzb\"},\"networkProfile\":{\"healthProbe\":{\"id\":\"cmkfcjhmrwxwqtac\"},\"networkInterfaceConfigurations\":[{\"name\":\"i\",\"properties\":{\"primary\":true,\"enableAcceleratedNetworking\":true,\"disableTcpStateTracking\":true,\"enableFpga\":true,\"networkSecurityGroup\":{\"id\":\"groxwd\"},\"dnsSettings\":{\"dnsServers\":[\"nxmmfolhclsesu\"]},\"ipConfigurations\":[{\"name\":\"oezqhkidfhyywlfzwuotilrpbqnjg\",\"properties\":{\"subnet\":{\"id\":\"cmkfcjhmrwxwqtac\"},\"primary\":true,\"publicIPAddressConfiguration\":{\"name\":\"fvpqf\",\"properties\":{\"idleTimeoutInMinutes\":9,\"dnsSettings\":{\"domainNameLabel\":\"ukrddzvmorpmfsczjwtbvp\",\"domainNameLabelScope\":\"TenantReuse\"},\"ipTags\":[{\"ipTagType\":\"sddgsoemnzgqizale\",\"tag\":\"wufmhrjsakbiaetyara\"}],\"publicIPPrefix\":{\"id\":\"groxwd\"},\"publicIPAddressVersion\":\"IPv4\",\"deleteOption\":\"Delete\"},\"sku\":{\"name\":\"Basic\",\"tier\":\"Regional\"}},\"privateIPAddressVersion\":\"IPv4\",\"applicationGatewayBackendAddressPools\":[{\"id\":\"groxwd\"}],\"applicationSecurityGroups\":[{\"id\":\"groxwd\"}],\"loadBalancerBackendAddressPools\":[{\"id\":\"groxwd\"}],\"loadBalancerInboundNatPools\":[{\"id\":\"groxwd\"}]}}],\"enableIPForwarding\":true,\"deleteOption\":\"Delete\",\"auxiliaryMode\":\"None\",\"auxiliarySku\":\"None\"}}],\"networkApiVersion\":\"2020-11-01\"},\"securityProfile\":{\"uefiSettings\":{\"secureBootEnabled\":true,\"vTpmEnabled\":true},\"encryptionAtHost\":true,\"securityType\":\"TrustedLaunch\",\"encryptionIdentity\":{\"userAssignedIdentityResourceId\":\"qoersmt\"},\"proxyAgentSettings\":{\"enabled\":true,\"mode\":\"Audit\",\"keyIncarnationId\":20}},\"diagnosticsProfile\":{\"bootDiagnostics\":{\"enabled\":true,\"storageUri\":\"rzamfwghybpx\"}},\"extensionProfile\":{\"extensions\":[{\"name\":\"bndxuxx\",\"properties\":{\"forceUpdateTag\":\"yhgxw\",\"publisher\":\"kpxtirxjfprhs\",\"type\":\"pgjilctjjwaa\",\"typeHandlerVersion\":\"zevivcoilxmbwlrihhhibq\",\"autoUpgradeMinorVersion\":true,\"enableAutomaticUpgrade\":true,\"settings\":{},\"protectedSettings\":{},\"provisionAfterExtensions\":[\"nftzosroolbcwmpupujzqwqe\"],\"suppressFailures\":true,\"protectedSettingsFromKeyVault\":{\"secretUrl\":\"vyhzfkqsqanacgzjthpjoe\",\"sourceVault\":{\"id\":\"groxwd\"}}}}],\"extensionsTimeBudget\":\"mbhjahtdygwgyszdwjtvlvtgchdwil\"},\"licenseType\":\"v\",\"scheduledEventsProfile\":{\"terminateNotificationProfile\":{\"notBeforeTimeout\":\"iljppmmw\",\"enable\":true},\"osImageNotificationProfile\":{\"notBeforeTimeout\":\"olbpadmevekyczfokodtfprxti\",\"enable\":true}},\"userData\":\"s\",\"capacityReservation\":{\"capacityReservationGroup\":{\"id\":\"groxwd\"}},\"applicationProfile\":{\"galleryApplications\":[{\"tags\":\"eyrqjbib\",\"order\":5,\"packageReferenceId\":\"lfxqiadouhmbovcd\",\"configurationReference\":\"ulztmiavpojpbpbddgnuuiimxcpau\",\"treatFailureAsDeploymentFailure\":true,\"enableAutomaticUpgrade\":true}]},\"hardwareProfile\":{\"vmSizeProperties\":{\"vCPUsAvailable\":16,\"vCPUsPerCore\":23}},\"serviceArtifactReference\":{\"id\":\"qlkeeyskyr\"},\"securityPostureReference\":{\"id\":\"mubredelfbshboaxrsxiajihahaa\",\"excludeExtensions\":[{\"properties\":{\"forceUpdateTag\":\"oriasdwawveilgusfrn\",\"publisher\":\"rxoajzb\",\"type\":\"vhrtgbaqdkbrrqxsdiiaqxksmqukl\",\"typeHandlerVersion\":\"bvzbiibps\",\"autoUpgradeMinorVersion\":true,\"enableAutomaticUpgrade\":true,\"settings\":{},\"protectedSettings\":{},\"instanceView\":{\"name\":\"ip\",\"type\":\"woadxvobquacooaujyg\",\"typeHandlerVersion\":\"ftkkqxdqiofgsusvxekdcpua\",\"substatuses\":[{\"code\":\"kuehuahsdozupmjrtp\",\"level\":\"Info\",\"displayStatus\":\"pomenhgqjoelbxvsqwxxexqxv\",\"message\":\"jxjjmlwbjylzvrislyyflhnmizhiu\",\"time\":\"2024-04-29T21:51:44.042Z\"}],\"statuses\":[{\"code\":\"kuehuahsdozupmjrtp\",\"level\":\"Info\",\"displayStatus\":\"pomenhgqjoelbxvsqwxxexqxv\",\"message\":\"jxjjmlwbjylzvrislyyflhnmizhiu\",\"time\":\"2024-04-29T21:51:44.042Z\"}]},\"suppressFailures\":true,\"protectedSettingsFromKeyVault\":{\"secretUrl\":\"vyhzfkqsqanacgzjthpjoe\",\"sourceVault\":{\"id\":\"groxwd\"}},\"provisionAfterExtensions\":[\"lwsfavklrgzuwmyxscskt\"]},\"location\":\"wrqxhbqaebwkzmcdmngyqmhogc\",\"tags\":{\"key7356\":\"apuftleiygpnemfzryiop\"}}]}}",
                        Object.class, SerializerEncoding.JSON))
                    .withComputeApiVersion("2023-07-01")
                    .withPlatformFaultDomainCount(1)))
            .withZones(Arrays.asList("zone1", "zone2"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf("key9851", new UserAssignedIdentity())))
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
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.computefleet.models.ComputeProfile;
import com.azure.resourcemanager.computefleet.models.EvictionPolicy;
import com.azure.resourcemanager.computefleet.models.Fleet;
import com.azure.resourcemanager.computefleet.models.FleetProperties;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.computefleet.models.ManagedServiceIdentityUpdate;
import com.azure.resourcemanager.computefleet.models.RegularPriorityAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.RegularPriorityProfile;
import com.azure.resourcemanager.computefleet.models.ResourcePlanUpdate;
import com.azure.resourcemanager.computefleet.models.SpotAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.SpotPriorityProfile;
import com.azure.resourcemanager.computefleet.models.VmSizeProfile;
import java.io.IOException;
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
    public static void fleetsUpdate(com.azure.resourcemanager.computefleet.ComputeFleetManager manager)
        throws IOException {
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
                .withComputeProfile(new ComputeProfile().withBaseVirtualMachineProfile(SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"osProfile\":{\"computerNamePrefix\":\"o\",\"adminUsername\":\"nrgzqciiaaxjrqldbmjbqkyhntp\",\"adminPassword\":\"adfbrdxpv\",\"customData\":\"xjjib\",\"windowsConfiguration\":{\"provisionVMAgent\":true,\"enableAutomaticUpdates\":true,\"timeZone\":\"hlyjiqcfksgrpjrct\",\"additionalUnattendContent\":[{\"passName\":\"OobeSystem\",\"componentName\":\"Microsoft-Windows-Shell-Setup\",\"settingName\":\"AutoLogon\",\"content\":\"bubmqbxjkj\"}],\"patchSettings\":{\"patchMode\":\"Manual\",\"enableHotpatching\":true,\"assessmentMode\":\"ImageDefault\",\"automaticByPlatformSettings\":{\"rebootSetting\":\"Unknown\",\"bypassPlatformSafetyChecksOnUserSchedule\":true}},\"winRM\":{\"listeners\":[{\"protocol\":\"Http\",\"certificateUrl\":\"phwesineizrl\"}]},\"enableVMAgentPlatformUpdates\":true},\"linuxConfiguration\":{\"disablePasswordAuthentication\":true,\"ssh\":{\"publicKeys\":[{\"path\":\"kmqz\",\"keyData\":\"kivgsubusvpprwqaqpjcmhsv\"}]},\"provisionVMAgent\":true,\"patchSettings\":{\"patchMode\":\"ImageDefault\",\"assessmentMode\":\"ImageDefault\",\"automaticByPlatformSettings\":{\"rebootSetting\":\"Unknown\",\"bypassPlatformSafetyChecksOnUserSchedule\":true}},\"enableVMAgentPlatformUpdates\":true},\"secrets\":[{\"sourceVault\":{\"id\":\"groxwd\"},\"vaultCertificates\":[{\"certificateUrl\":\"tyldwkzafmnkvpo\",\"certificateStore\":\"nlxrwavpzhueffxsshlun\"}]}],\"allowExtensionOperations\":true,\"requireGuestProvisionSignal\":true},\"storageProfile\":{\"imageReference\":{\"publisher\":\"mqxgwbiyjzmxavhbkd\",\"offer\":\"isxgumkarlkomp\",\"sku\":\"eojmppqcrnpmxirtp\",\"version\":\"wvpcqefgtmqdgltiuz\",\"sharedGalleryImageId\":\"kmkgihoxwlawuuhcinfirktdwkmx\",\"communityGalleryImageId\":\"vlqe\",\"id\":\"aiunknwgksu\"},\"osDisk\":{\"name\":\"wfttw\",\"caching\":\"None\",\"writeAcceleratorEnabled\":true,\"createOption\":\"FromImage\",\"diffDiskSettings\":{\"option\":\"Local\",\"placement\":\"CacheDisk\"},\"diskSizeGB\":14,\"osType\":\"Windows\",\"image\":{\"uri\":\"thqceubivdrjs\"},\"vhdContainers\":[\"tkzcwddtinkfpnfklatw\"],\"managedDisk\":{\"storageAccountType\":\"Standard_LRS\",\"diskEncryptionSet\":{\"id\":\"vmatqblzjalbatdepyzqmnd\"},\"securityProfile\":{\"securityEncryptionType\":\"VMGuestStateOnly\",\"diskEncryptionSet\":{\"id\":\"vmatqblzjalbatdepyzqmnd\"}}},\"deleteOption\":\"Delete\"},\"dataDisks\":[{\"name\":\"eogiykmdmeikswxmigjws\",\"lun\":14,\"caching\":\"None\",\"writeAcceleratorEnabled\":true,\"createOption\":\"FromImage\",\"diskSizeGB\":6,\"managedDisk\":{\"storageAccountType\":\"Standard_LRS\",\"diskEncryptionSet\":{\"id\":\"vmatqblzjalbatdepyzqmnd\"},\"securityProfile\":{\"securityEncryptionType\":\"VMGuestStateOnly\",\"diskEncryptionSet\":{\"id\":\"vmatqblzjalbatdepyzqmnd\"}}},\"diskIOPSReadWrite\":27,\"diskMBpsReadWrite\":2,\"deleteOption\":\"Delete\"}],\"diskControllerType\":\"uzb\"},\"networkProfile\":{\"healthProbe\":{\"id\":\"cmkfcjhmrwxwqtac\"},\"networkInterfaceConfigurations\":[{\"name\":\"i\",\"properties\":{\"primary\":true,\"enableAcceleratedNetworking\":true,\"disableTcpStateTracking\":true,\"enableFpga\":true,\"networkSecurityGroup\":{\"id\":\"groxwd\"},\"dnsSettings\":{\"dnsServers\":[\"nxmmfolhclsesu\"]},\"ipConfigurations\":[{\"name\":\"oezqhkidfhyywlfzwuotilrpbqnjg\",\"properties\":{\"subnet\":{\"id\":\"cmkfcjhmrwxwqtac\"},\"primary\":true,\"publicIPAddressConfiguration\":{\"name\":\"fvpqf\",\"properties\":{\"idleTimeoutInMinutes\":9,\"dnsSettings\":{\"domainNameLabel\":\"ukrddzvmorpmfsczjwtbvp\",\"domainNameLabelScope\":\"TenantReuse\"},\"ipTags\":[{\"ipTagType\":\"sddgsoemnzgqizale\",\"tag\":\"wufmhrjsakbiaetyara\"}],\"publicIPPrefix\":{\"id\":\"groxwd\"},\"publicIPAddressVersion\":\"IPv4\",\"deleteOption\":\"Delete\"},\"sku\":{\"name\":\"Basic\",\"tier\":\"Regional\"}},\"privateIPAddressVersion\":\"IPv4\",\"applicationGatewayBackendAddressPools\":[{\"id\":\"groxwd\"}],\"applicationSecurityGroups\":[{\"id\":\"groxwd\"}],\"loadBalancerBackendAddressPools\":[{\"id\":\"groxwd\"}],\"loadBalancerInboundNatPools\":[{\"id\":\"groxwd\"}]}}],\"enableIPForwarding\":true,\"deleteOption\":\"Delete\",\"auxiliaryMode\":\"None\",\"auxiliarySku\":\"None\"}}],\"networkApiVersion\":\"2020-11-01\"},\"securityProfile\":{\"uefiSettings\":{\"secureBootEnabled\":true,\"vTpmEnabled\":true},\"encryptionAtHost\":true,\"securityType\":\"TrustedLaunch\",\"encryptionIdentity\":{\"userAssignedIdentityResourceId\":\"qoersmt\"},\"proxyAgentSettings\":{\"enabled\":true,\"mode\":\"Audit\",\"keyIncarnationId\":20}},\"diagnosticsProfile\":{\"bootDiagnostics\":{\"enabled\":true,\"storageUri\":\"rzamfwghybpx\"}},\"extensionProfile\":{\"extensions\":[{\"name\":\"bndxuxx\",\"properties\":{\"forceUpdateTag\":\"yhgxw\",\"publisher\":\"kpxtirxjfprhs\",\"type\":\"pgjilctjjwaa\",\"typeHandlerVersion\":\"zevivcoilxmbwlrihhhibq\",\"autoUpgradeMinorVersion\":true,\"enableAutomaticUpgrade\":true,\"settings\":{},\"protectedSettings\":{},\"provisionAfterExtensions\":[\"nftzosroolbcwmpupujzqwqe\"],\"suppressFailures\":true,\"protectedSettingsFromKeyVault\":{\"secretUrl\":\"vyhzfkqsqanacgzjthpjoe\",\"sourceVault\":{\"id\":\"groxwd\"}}}}],\"extensionsTimeBudget\":\"mbhjahtdygwgyszdwjtvlvtgchdwil\"},\"licenseType\":\"v\",\"scheduledEventsProfile\":{\"terminateNotificationProfile\":{\"notBeforeTimeout\":\"iljppmmw\",\"enable\":true},\"osImageNotificationProfile\":{\"notBeforeTimeout\":\"olbpadmevekyczfokodtfprxti\",\"enable\":true}},\"userData\":\"s\",\"capacityReservation\":{\"capacityReservationGroup\":{\"id\":\"groxwd\"}},\"applicationProfile\":{\"galleryApplications\":[{\"tags\":\"eyrqjbib\",\"order\":5,\"packageReferenceId\":\"lfxqiadouhmbovcd\",\"configurationReference\":\"ulztmiavpojpbpbddgnuuiimxcpau\",\"treatFailureAsDeploymentFailure\":true,\"enableAutomaticUpgrade\":true}]},\"hardwareProfile\":{\"vmSizeProperties\":{\"vCPUsAvailable\":16,\"vCPUsPerCore\":23}},\"serviceArtifactReference\":{\"id\":\"qlkeeyskyr\"},\"securityPostureReference\":{\"id\":\"mubredelfbshboaxrsxiajihahaa\",\"excludeExtensions\":[{\"properties\":{\"forceUpdateTag\":\"oriasdwawveilgusfrn\",\"publisher\":\"rxoajzb\",\"type\":\"vhrtgbaqdkbrrqxsdiiaqxksmqukl\",\"typeHandlerVersion\":\"bvzbiibps\",\"autoUpgradeMinorVersion\":true,\"enableAutomaticUpgrade\":true,\"settings\":{},\"protectedSettings\":{},\"instanceView\":{\"name\":\"ip\",\"type\":\"woadxvobquacooaujyg\",\"typeHandlerVersion\":\"ftkkqxdqiofgsusvxekdcpua\",\"substatuses\":[{\"code\":\"kuehuahsdozupmjrtp\",\"level\":\"Info\",\"displayStatus\":\"pomenhgqjoelbxvsqwxxexqxv\",\"message\":\"jxjjmlwbjylzvrislyyflhnmizhiu\",\"time\":\"2024-04-29T21:51:44.042Z\"}],\"statuses\":[{\"code\":\"kuehuahsdozupmjrtp\",\"level\":\"Info\",\"displayStatus\":\"pomenhgqjoelbxvsqwxxexqxv\",\"message\":\"jxjjmlwbjylzvrislyyflhnmizhiu\",\"time\":\"2024-04-29T21:51:44.042Z\"}]},\"suppressFailures\":true,\"protectedSettingsFromKeyVault\":{\"secretUrl\":\"vyhzfkqsqanacgzjthpjoe\",\"sourceVault\":{\"id\":\"groxwd\"}},\"provisionAfterExtensions\":[\"lwsfavklrgzuwmyxscskt\"]},\"location\":\"wrqxhbqaebwkzmcdmngyqmhogc\",\"tags\":{}}]}}",
                        Object.class, SerializerEncoding.JSON))
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

