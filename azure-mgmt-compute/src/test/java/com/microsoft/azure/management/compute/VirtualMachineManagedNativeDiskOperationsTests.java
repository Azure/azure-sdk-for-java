package com.microsoft.azure.management.compute;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class VirtualMachineManagedNativeDiskOperationsTests extends ComputeManagementTestBase {
    private static ApplicationTokenCredentials credentials;
    private static RestClient restClient;
    private static Region region = Region.fromName("eastus2euap");   // Special regions for canary deployment 'eastus2euap' and 'centraluseuap'

    @BeforeClass
    public static void setup() throws Exception {
        File credFile = new File("C:\\my.azureauth");
        credentials = ApplicationTokenCredentials.fromFile(credFile);

        AzureEnvironment canary = new AzureEnvironment("https://login.microsoftonline.com/",
                "https://management.core.windows.net/",
                "https://brazilus.management.azure.com/",
                "https://graph.windows.net/");

        restClient = new RestClient.Builder()
                .withBaseUrl(canary, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withLogLevel(LogLevel.BODY_AND_HEADERS)
                .build();

        computeManager = ComputeManager
                .authenticate(restClient, credentials.defaultSubscriptionId());
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(credentials.defaultSubscriptionId());
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void canCreateVirtualMachineFromPIRImageWithManagedOsDisk() {
        final VirtualMachineImage image = getImage();
        final String vmName = "myvm";
        final String rgName = ResourceNamer.randomResourceName("rg-", 15);
        final String publicIpDnsLabel = ResourceNamer.randomResourceName("pip", 20);
        final String uname = "juser";
        final String password = "123tEst!@|ac";

        System.out.println(rgName);

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withNewPrimaryPublicIpAddress(publicIpDnsLabel)
                .withSpecificLinuxImageVersion(image.imageReference())
                .withRootUsername(uname)
                .withRootPassword(password)
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();
        // Ensure default to managed disk
        //
        Assert.assertTrue(virtualMachine.isManagedDiskEnabled());
        // Validate caching, size and the default storage account type set for the managed disk
        // backing os disk
        //
        Assert.assertNotNull(virtualMachine.osDiskStorageAccountType());
        Assert.assertEquals(virtualMachine.osDiskCachingType(), CachingTypes.READ_WRITE);
        Assert.assertEquals(virtualMachine.size(), VirtualMachineSizeTypes.STANDARD_D5_V2);
        // Validate the implicit managed disk created by CRP to back the os disk
        //
        Assert.assertNotNull(virtualMachine.osDiskId());
        Disk osDisk = computeManager.disks().getById(virtualMachine.osDiskId());
        Assert.assertTrue(osDisk.isAttachedToVirtualMachine());
        Assert.assertEquals(osDisk.osType(), OperatingSystemTypes.LINUX);
        Assert.assertEquals(osDisk.osState(), OperatingSystemStateTypes.GENERALIZED);
        // Check the auto created public ip
        //
        String publicIpId = virtualMachine.getPrimaryPublicIpAddressId();
        Assert.assertNotNull(publicIpId);
        // Validates the options which are valid only for native disks
        //
        Assert.assertNull(virtualMachine.osNativeDiskVhdUri());
        Assert.assertNotNull(virtualMachine.nativeDataDisks());
        Assert.assertTrue(virtualMachine.nativeDataDisks().size() == 0);
        // clean
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateVirtualMachineFromPIRImageWithNativeOsDisk() {
        VirtualMachineImage image = getImage();
    }

    @Test
    public void canCreateVirtualMachineWithEmptyManagedDataDisks() {
        VirtualMachineImage image = getImage();
        final String rgName = ResourceNamer.randomResourceName("rg-", 15);
        final String publicIpDnsLabel = ResourceNamer.randomResourceName("pip", 20);
        final String uname = "juser";
        final String password = "123tEst!@|ac";
        // Create with implicit + explicit empty disks, check default and override
        //
        final String vmName1 = "myvm1";
        final String explicitlyCreatedEmptyDiskName1 = ResourceNamer.randomResourceName(vmName1 + "_mdisk_", 25);
        final String explicitlyCreatedEmptyDiskName2 = ResourceNamer.randomResourceName(vmName1 + "_mdisk_", 25);
        final String explicitlyCreatedEmptyDiskName3 = ResourceNamer.randomResourceName(vmName1 + "_mdisk_", 25);
        System.out.println(rgName);

        ResourceGroup resourceGroup = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(region)
                .create();

        Creatable<Disk> creatableEmptyDisk1 = computeManager.disks()
                .define(explicitlyCreatedEmptyDiskName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        Creatable<Disk> creatableEmptyDisk2 = computeManager.disks()
                .define(explicitlyCreatedEmptyDiskName2)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        Creatable<Disk> creatableEmptyDisk3 = computeManager.disks()
                .define(explicitlyCreatedEmptyDiskName3)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(vmName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withNewPrimaryPublicIpAddress(publicIpDnsLabel)
                .withSpecificLinuxImageVersion(image.imageReference())
                .withRootUsername(uname)
                .withRootPassword(password)
                // Start: Add bunch of empty managed disks
                .withNewDataDisk(100)                                             // CreateOption: EMPTY
                .withNewDataDisk(100, 1, CachingTypes.READ_ONLY)                  // CreateOption: EMPTY
                .withNewDataDisk(creatableEmptyDisk1)                             // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk2, 2, CachingTypes.NONE)       // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk3, 200, 3, CachingTypes.NONE)  // CreateOption: ATTACH (Changing size)
                // End : Add bunch of empty managed disks
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        // TODO: Validate the data disks - requires VirtualMachine.dataDisks() to be implemented
        //
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    private VirtualMachineImage getImage() {
        VirtualMachineImage linuxVmImage = computeManager.virtualMachineImages().getImage(region,
                "Canonical",
                "UbuntuServer",
                "14.04.2-LTS",
                "14.04.201507060");
        Assert.assertNotNull(linuxVmImage);
        Assert.assertNotNull(linuxVmImage.inner());
        return linuxVmImage;
    }
}
