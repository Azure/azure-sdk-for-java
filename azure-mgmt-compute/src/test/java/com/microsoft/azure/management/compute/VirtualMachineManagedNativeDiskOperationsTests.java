package com.microsoft.azure.management.compute;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.network.implementation.NetworkManager;
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
import java.util.Map;

public class VirtualMachineManagedNativeDiskOperationsTests extends ComputeManagementTestBase {
    private static ApplicationTokenCredentials credentials;
    private static RestClient restClient;
    private static Region region = Region.fromName("westcentralus"); // Region.fromName("eastus2euap");   // Special regions for canary deployment 'eastus2euap' and 'centraluseuap'

    @BeforeClass
    public static void setup() throws Exception {
        File credFile = new File("C:\\my.azureauth");
        credentials = ApplicationTokenCredentials.fromFile(credFile);

        AzureEnvironment canary = new AzureEnvironment("https://login.microsoftonline.com/",
                "https://management.core.windows.net/",
                "https://management.azure.com/", // "https://brazilus.management.azure.com/",
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
        networkManager = NetworkManager
                .authenticate(restClient, credentials.defaultSubscriptionId());

    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void canCreateVirtualMachineFromPIRImageWithManagedOsDisk() {
        final VirtualMachineImage image = getImage();
        final String vmName1 = "myvm1";
        final String rgName = ResourceNamer.randomResourceName("rg-", 15);
        final String publicIpDnsLabel = ResourceNamer.randomResourceName("pip", 20);
        final String uname = "juser";
        final String password = "123tEst!@|ac";

        System.out.println(rgName);

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(vmName1)
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
                // Start: Add 5 empty managed disks
                .withNewDataDisk(100)                                             // CreateOption: EMPTY
                .withNewDataDisk(100, 1, CachingTypes.READ_ONLY)                  // CreateOption: EMPTY
                .withNewDataDisk(creatableEmptyDisk1)                             // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk2, 2, CachingTypes.NONE)       // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk3, 3, CachingTypes.NONE)       // CreateOption: ATTACH
                // End : Add 5 empty managed disks
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assert.assertTrue(virtualMachine.isManagedDiskEnabled());
        // There should not be any native data disks
        //
        Assert.assertNotNull(virtualMachine.nativeDataDisks());
        Assert.assertEquals(virtualMachine.nativeDataDisks().size(), 0);
        // Validate the managed data disks
        //
        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine.dataDisks();
        Assert.assertNotNull(dataDisks);
        Assert.assertTrue(dataDisks.size() == 5);
        Assert.assertTrue(dataDisks.containsKey(1));
        VirtualMachineDataDisk dataDiskLun1 = dataDisks.get(1);
        Assert.assertNotNull(dataDiskLun1.id());
        Assert.assertEquals(dataDiskLun1.cachingType(), CachingTypes.READ_ONLY);
        Assert.assertEquals(dataDiskLun1.size(), 100);

        Assert.assertTrue(dataDisks.containsKey(2));
        VirtualMachineDataDisk dataDiskLun2 = dataDisks.get(2);
        Assert.assertNotNull(dataDiskLun2.id());
        Assert.assertEquals(dataDiskLun2.cachingType(), CachingTypes.NONE);
        Assert.assertEquals(dataDiskLun2.size(), 150);

        Assert.assertTrue(dataDisks.containsKey(3));
        VirtualMachineDataDisk dataDiskLun3 = dataDisks.get(3);
        Assert.assertNotNull(dataDiskLun3.id());
        Assert.assertEquals(dataDiskLun3.cachingType(), CachingTypes.NONE);
        Assert.assertEquals(dataDiskLun3.size(), 150);
        // Validate the defaults assigned
        //
        for (VirtualMachineDataDisk dataDisk : dataDisks.values()) {
            if (dataDisk.lun() != 1 && dataDisk.lun() != 2 && dataDisk.lun() != 3) {
                Assert.assertEquals(dataDisk.cachingType(), CachingTypes.READ_WRITE);
                Assert.assertEquals(dataDisk.storageAccountType(), StorageAccountTypes.STANDARD_LRS);
            }
        }
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateVirtualMachineFromCustomImageWithManagedDisks() {
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
        writeToFile(rgName);

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

        VirtualMachine virtualMachine1 = computeManager.virtualMachines()
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
                .withNewDataDisk(creatableEmptyDisk3, 3, CachingTypes.NONE)       // CreateOption: ATTACH
                // End : Add bunch of empty managed disks
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();
        System.out.println("Waiting for some time before de-provision");
        sleep(60 * 1000); // Wait for some time to ensure vm is publicly accessible
        deprovisionLinuxVM(virtualMachine1.getPrimaryPublicIpAddress().fqdn(),
                22,
                uname,
                password);

        virtualMachine1.deallocate();
        virtualMachine1.generalize();

        final String customImageName = ResourceNamer.randomResourceName("img-", 10);
        VirtualMachineCustomImage customImage = computeManager.virtualMachineCustomImages().define(customImageName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .fromVirtualMachine(virtualMachine1)
                .create();
        Assert.assertNotNull(customImage);
        Assert.assertNotNull(customImage.sourceVirtualMachineId());
        Assert.assertTrue(customImage.sourceVirtualMachineId().equalsIgnoreCase(virtualMachine1.id().toLowerCase()));
        Assert.assertNotNull(customImage.osDiskImage());
        Assert.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assert.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assert.assertNotNull(customImage.dataDiskImages());
        Assert.assertEquals(customImage.dataDiskImages().size(), 5);
        for (ImageDataDisk imageDataDisk : customImage.dataDiskImages().values()) {
            Assert.assertNull(imageDataDisk.blobUri());
            Assert.assertNotNull(imageDataDisk.managedDisk().id());
        }

        // Create virtual machine from the custom image
        // This one relies on CRP's capability to create implicit data disks from the virtual machine
        // image data disk images.
        //
        final String vmName2 = "myvm2";
        VirtualMachine virtualMachine2 = computeManager.virtualMachines()
                .define(vmName2)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withLinuxCustomImage(customImage.id())
                .withRootUsername(uname)
                .withRootPassword(password)
                // No explicit data disks, let CRP create it from the image's data disk images
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine2.dataDisks();
        Assert.assertNotNull(dataDisks);
        Assert.assertEquals(dataDisks.size(), customImage.dataDiskImages().size());
        for (ImageDataDisk imageDataDisk : customImage.dataDiskImages().values()) {
            Assert.assertTrue(dataDisks.containsKey(imageDataDisk.lun()));
            VirtualMachineDataDisk dataDisk = dataDisks.get(imageDataDisk.lun());
            Assert.assertEquals(dataDisk.cachingType(), imageDataDisk.caching());
            Assert.assertEquals(dataDisk.size(), (long) imageDataDisk.diskSizeGB());
        }

        // Create virtual machine from the custom image
        // This one override the size and caching type of data disks from data disk images and
        // adds one additional disk
        //

        final String vmName3 = "myvm3";
        VirtualMachine.DefinitionStages.WithManagedCreate creatableVirtualMachine3 = computeManager.virtualMachines()
                .define(vmName3)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withLinuxCustomImage(customImage.id())
                .withRootUsername(uname)
                .withRootPassword(password);
        for (ImageDataDisk dataDiskImage : customImage.dataDiskImages().values()) {
            // Explicitly override the properties of the data disks created from disk image
            //
            // CreateOption: FROM_IMAGE
            creatableVirtualMachine3.withNewDataDiskFromImage(dataDiskImage.lun(),
                    dataDiskImage.diskSizeGB() + 10,    // increase size by 10 GB
                    CachingTypes.READ_ONLY);
        }
        VirtualMachine virtualMachine3 = creatableVirtualMachine3
                .withNewDataDisk(200)                               // CreateOption: EMPTY
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        dataDisks = virtualMachine3.dataDisks();
        Assert.assertNotNull(dataDisks);
        Assert.assertEquals(dataDisks.size(), customImage.dataDiskImages().size() + 1 /* count one extra empty disk */);
        for (ImageDataDisk imageDataDisk : customImage.dataDiskImages().values()) {
            Assert.assertTrue(dataDisks.containsKey(imageDataDisk.lun()));
            VirtualMachineDataDisk dataDisk = dataDisks.get(imageDataDisk.lun());
            Assert.assertEquals(dataDisk.cachingType(), CachingTypes.READ_ONLY);
            Assert.assertEquals(dataDisk.size(), (long) imageDataDisk.diskSizeGB() + 10);
        }
        resourceManager.resourceGroups().deleteByName(resourceGroup.name());
    }

    @Test
    public void canUpdateVirtualMachineByAddingAndRemovingManagedDisks() {
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
        writeToFile(rgName);

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

        VirtualMachine virtualMachine1 = computeManager.virtualMachines()
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
                .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)                 // CreateOption: EMPTY
                .withNewDataDisk(creatableEmptyDisk1)                             // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk2, 2, CachingTypes.NONE)       // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk3, 3, CachingTypes.NONE)       // CreateOption: ATTACH
                // End : Add bunch of empty managed disks
                .withDataDiskDefaultCachingType(CachingTypes.READ_ONLY)
                .withDataDiskDefaultStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        virtualMachine1.update()
                .withoutDataDisk(1)
                .withNewDataDisk(100, 6, CachingTypes.READ_WRITE)                 // CreateOption: EMPTY
                .apply();

        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine1.dataDisks();
        Assert.assertNotNull(dataDisks);
        Assert.assertEquals(dataDisks.size(), 5); // Removed one added another
        Assert.assertTrue(dataDisks.containsKey(6));
        Assert.assertFalse(dataDisks.containsKey(1));
        resourceManager.resourceGroups().deleteByName(resourceGroup.name());
    }

    @Test
    public void canCreateVirtualMachineByAttachingManagedOsDisk() {
        final String rgName = ResourceNamer.randomResourceName("rg-", 15);
        final String uname = "juser";
        final String password = "123tEst!@|ac";
        final String vmName = "myvm6";
        writeToFile(rgName);

        // Creates a native virtual machine
        //
        VirtualMachine nativeVm = computeManager.virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withLatestLinuxImage("Canonical", "UbuntuServer", "14.04.2-LTS")
                .withRootUsername(uname)
                .withRootPassword(password)
                .withNativeDisks()                  /* UN-MANAGED OS and DATA DISKS */
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 17))
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assert.assertFalse(nativeVm.isManagedDiskEnabled());
        String osVhdUri = nativeVm.osNativeDiskVhdUri();
        Assert.assertNotNull(osVhdUri);

        computeManager.virtualMachines().deleteById(nativeVm.id());

        final String diskName = ResourceNamer.randomResourceName("dsk-", 15);
        Disk osDisk = computeManager.disks().define(diskName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withLinuxFromVhd(osVhdUri)
                .create();

        // Creates a managed virtual machine
        //
        VirtualMachine managedVm = computeManager.virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withSpecializedOsDisk(osDisk, OperatingSystemTypes.LINUX)
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assert.assertTrue(managedVm.isManagedDiskEnabled());
        Assert.assertTrue(managedVm.osDiskId().equalsIgnoreCase(osDisk.id().toLowerCase()));
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateVirtualMachineWithManagedDiskInManagedAvailabilitySet() {
        final String rgName = ResourceNamer.randomResourceName("rg-", 15);
        final String availSetName = ResourceNamer.randomResourceName("av-", 15);
        final String uname = "juser";
        final String password = "123tEst!@|ac";
        final String vmName = "myvm6";
        writeToFile(rgName);

        VirtualMachineImage image = getImage();
        VirtualMachine managedVm = computeManager.virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withSpecificLinuxImageVersion(image.imageReference())
                .withRootUsername(uname)
                .withRootPassword(password)
                .withNewDataDisk(100)
                .withNewDataDisk(100, 1, CachingTypes.READ_ONLY)
                .withNewDataDisk(100, 2, CachingTypes.READ_WRITE, StorageAccountTypes.STANDARD_LRS)
                .withNewAvailabilitySet(availSetName)           // Default to managed availability set
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assert.assertNotNull(managedVm.availabilitySetId());
        AvailabilitySet availabilitySet = computeManager.availabilitySets().getById(managedVm.availabilitySetId());
        Assert.assertTrue(availabilitySet.virtualMachineIds().size() > 0);
        Assert.assertTrue(availabilitySet.isManaged()); // TODO: Server is not honoring this flag in request, need to check with CRP

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
