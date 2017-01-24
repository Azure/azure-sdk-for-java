package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class VirtualMachineCustomImageOperationsTest extends ComputeManagementTest {
    private static Region region = Region.fromName("eastus2euap");   // Special regions for canary deployment 'eastus2euap' and 'centraluseuap'

    @Test
    public void canCreateImageFromNativeVhd() throws IOException {
        final String rgName = generateRandomResourceName("custo512ert", 20);
        final String vhdBasedImageName = generateRandomResourceName("img", 20);
        writeToFile(rgName);

        VirtualMachine linuxVM = prepareGeneralizedVmWith2EmptyDataDisks(rgName,
                generateRandomResourceName("multidvm", 15),
                region,
                computeManager);
        //
        VirtualMachineCustomImage.DefinitionStages.WithCreateAndDataDiskImageOsDiskSettings
                creatableDisk = computeManager
                .virtualMachineCustomImages()
                .define(vhdBasedImageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinuxFromVhd(linuxVM.osUnmanagedDiskVhdUri(), OperatingSystemStateTypes.GENERALIZED)
                .withOsDiskCaching(linuxVM.osDiskCachingType());
        for (VirtualMachineUnmanagedDataDisk disk : linuxVM.unmanagedDataDisks().values()) {
            creatableDisk.defineDataDiskImage()
                    .withLun(disk.lun())
                    .fromVhd(disk.vhdUri())
                    .withDiskCaching(disk.cachingType())
                    .withDiskSizeInGB(disk.size() + 10) // Resize each data disk image by +10GB
                    .attach();
        }
        VirtualMachineCustomImage customImage = creatableDisk.create();
        Assert.assertNotNull(customImage.id());
        Assert.assertEquals(customImage.name(), vhdBasedImageName);
        Assert.assertFalse(customImage.isCreatedFromVirtualMachine());
        Assert.assertNull(customImage.sourceVirtualMachineId());
        Assert.assertNotNull(customImage.osDiskImage());
        Assert.assertNotNull(customImage.osDiskImage().blobUri());
        Assert.assertEquals(customImage.osDiskImage().caching(), CachingTypes.READ_WRITE);
        Assert.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assert.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assert.assertNotNull(customImage.dataDiskImages());
        Assert.assertEquals(customImage.dataDiskImages().size(), linuxVM.unmanagedDataDisks().size());
        for (ImageDataDisk diskImage : customImage.dataDiskImages().values()) {
            VirtualMachineUnmanagedDataDisk matchedDisk = null;
            for (VirtualMachineUnmanagedDataDisk vmDisk : linuxVM.unmanagedDataDisks().values()) {
                if (vmDisk.lun() == diskImage.lun()) {
                    matchedDisk = vmDisk;
                    break;
                }
            }
            Assert.assertNotNull(matchedDisk);
            Assert.assertEquals(matchedDisk.cachingType(), diskImage.caching());
            Assert.assertEquals(matchedDisk.vhdUri(), diskImage.blobUri());
            Assert.assertEquals((long)matchedDisk.size() + 10, (long)diskImage.diskSizeGB());
        }
        VirtualMachineCustomImage image = computeManager
                .virtualMachineCustomImages()
                .getByGroup(rgName, vhdBasedImageName);
        Assert.assertNotNull(image);
        PagedList<VirtualMachineCustomImage> images = computeManager
                .virtualMachineCustomImages()
                .listByGroup(rgName);
        Assert.assertTrue(images.size() > 0);

        // Create virtual machine from custom image
        //
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(generateRandomResourceName("cusvm", 15))
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withLinuxCustomImage(image.id())
                .withRootUsername("javauser")
                .withRootPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine.dataDisks();
        Assert.assertNotNull(dataDisks);
        Assert.assertEquals(dataDisks.size(), image.dataDiskImages().size());
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateImageByCapturingVM() {
        final String rgName = generateRandomResourceName("rg", 15);
        String vmName = generateRandomResourceName("vm67-", 20);
        writeToFile(rgName);

        VirtualMachine vm = prepareGeneralizedVmWith2EmptyDataDisks(rgName, vmName, region, computeManager);
        //
        final String imageName = generateRandomResourceName("img", 15);
        VirtualMachineCustomImage customImage = computeManager.virtualMachineCustomImages()
                .define(imageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .fromVirtualMachine(vm.id())
                .create();

        Assert.assertTrue(customImage.name().equalsIgnoreCase(imageName));
        Assert.assertNotNull(customImage.osDiskImage());
        Assert.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assert.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assert.assertNotNull(customImage.dataDiskImages());
        Assert.assertEquals(customImage.dataDiskImages().size(), 2);
        Assert.assertNotNull(customImage.sourceVirtualMachineId());
        Assert.assertTrue(customImage.sourceVirtualMachineId().equalsIgnoreCase(vm.id()));

        for (VirtualMachineUnmanagedDataDisk vmDisk : vm.unmanagedDataDisks().values()) {
            Assert.assertTrue(customImage.dataDiskImages().containsKey(vmDisk.lun()));
            ImageDataDisk diskImage = customImage.dataDiskImages().get(vmDisk.lun());
            Assert.assertEquals(diskImage.caching(), vmDisk.cachingType());
            Assert.assertEquals((long) diskImage.diskSizeGB(), vmDisk.size());
            Assert.assertNotNull(diskImage.blobUri());
            diskImage.blobUri().equalsIgnoreCase(vmDisk.vhdUri());
        }

        customImage = computeManager.virtualMachineCustomImages().getByGroup(rgName, imageName);
        Assert.assertNotNull(customImage);
        Assert.assertNotNull(customImage.inner());
        computeManager.virtualMachineCustomImages().deleteById(customImage.id());
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateImageFromManagedDisk() {
        String rgName = generateRandomResourceName("rg-", 20);
        String vmName = generateRandomResourceName("vm7-", 20);
        writeToFile(rgName);

        final String uname = "juser";
        final String password = "123tEst!@|ac";

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
                .withUnmanagedDisks()                  /* UN-MANAGED OS and DATA DISKS */
                .defineUnmanagedDataDisk("disk1")
                    .withNewVhd(100)
                    .withCaching(CachingTypes.READ_ONLY)
                    .attach()
                .withNewUnmanagedDataDisk(100)
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withNewStorageAccount(generateRandomResourceName("stg", 17))
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assert.assertFalse(nativeVm.isManagedDiskEnabled());
        String osVhdUri = nativeVm.osUnmanagedDiskVhdUri();
        Assert.assertNotNull(osVhdUri);
        Map<Integer, VirtualMachineUnmanagedDataDisk> dataDisks = nativeVm.unmanagedDataDisks();
        Assert.assertEquals(dataDisks.size(), 2);

        computeManager.virtualMachines().deleteById(nativeVm.id());

        final String osDiskName = generateRandomResourceName("dsk", 15);
        // Create managed disk with Os from vm's Os disk
        //
        Disk managedOsDisk = computeManager.disks().define(osDiskName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinuxFromVhd(osVhdUri)
                .create();

        // Create managed disk with Data from vm's lun0 data disk
        //
        final String dataDiskName1 = generateRandomResourceName("dsk", 15);
        VirtualMachineUnmanagedDataDisk vmNativeDataDisk1 = dataDisks.get(0);
        Disk managedDataDisk1 = computeManager.disks().define(dataDiskName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withData()
                .fromVhd(vmNativeDataDisk1.vhdUri())
                .create();

        // Create managed disk with Data from vm's lun1 data disk
        //
        final String dataDiskName2 = generateRandomResourceName("dsk", 15);
        VirtualMachineUnmanagedDataDisk vmNativeDataDisk2 = dataDisks.get(1);
        Disk managedDataDisk2 = computeManager.disks().define(dataDiskName2)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withData()
                .fromVhd(vmNativeDataDisk2.vhdUri())
                .create();

        // Create an image from the above managed disks
        // Note that this is not a direct user scenario, but including this as per CRP team request
        //
        final String imageName = generateRandomResourceName("img", 15);
        VirtualMachineCustomImage customImage = computeManager.virtualMachineCustomImages().define(imageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinuxFromDisk(managedOsDisk, OperatingSystemStateTypes.GENERALIZED)
                .defineDataDiskImage()
                    .withLun(vmNativeDataDisk1.lun())
                    .fromManagedDisk(managedDataDisk1)
                    .withDiskCaching(vmNativeDataDisk1.cachingType())
                    .withDiskSizeInGB(vmNativeDataDisk1.size() + 10)
                    .attach()
                .defineDataDiskImage()
                    .withLun(vmNativeDataDisk2.lun())
                    .fromManagedDisk(managedDataDisk2)
                    .withDiskSizeInGB(vmNativeDataDisk2.size() + 10)
                    .attach()
                .create();

        Assert.assertNotNull(customImage);
        Assert.assertTrue(customImage.name().equalsIgnoreCase(imageName));
        Assert.assertNotNull(customImage.osDiskImage());
        Assert.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assert.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assert.assertNotNull(customImage.dataDiskImages());
        Assert.assertEquals(customImage.dataDiskImages().size(), 2);
        Assert.assertNull(customImage.sourceVirtualMachineId());

        Assert.assertTrue(customImage.dataDiskImages().containsKey(vmNativeDataDisk1.lun()));
        Assert.assertEquals(customImage.dataDiskImages().get(vmNativeDataDisk1.lun()).caching(), vmNativeDataDisk1.cachingType());
        Assert.assertTrue(customImage.dataDiskImages().containsKey(vmNativeDataDisk2.lun()));
        Assert.assertEquals(customImage.dataDiskImages().get(vmNativeDataDisk2.lun()).caching(), CachingTypes.NONE);

        for (VirtualMachineUnmanagedDataDisk vmDisk : dataDisks.values()) {
            Assert.assertTrue(customImage.dataDiskImages().containsKey(vmDisk.lun()));
            ImageDataDisk diskImage = customImage.dataDiskImages().get(vmDisk.lun());
            Assert.assertEquals((long) diskImage.diskSizeGB(), vmDisk.size() + 10);
            Assert.assertNull(diskImage.blobUri());
            Assert.assertNotNull(diskImage.managedDisk());
            Assert.assertTrue(diskImage.managedDisk().id().equalsIgnoreCase(managedDataDisk1.id())
                    || diskImage.managedDisk().id().equalsIgnoreCase(managedDataDisk2.id()));
        }
        computeManager.disks().deleteById(managedOsDisk.id());
        computeManager.disks().deleteById(managedDataDisk1.id());
        computeManager.disks().deleteById(managedDataDisk2.id());
        computeManager.virtualMachineCustomImages().deleteById(customImage.id());
        computeManager.resourceManager().resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateImageFromSnapshot() {
    }

    private VirtualMachine prepareGeneralizedVmWith2EmptyDataDisks(String rgName,
                                                         String vmName,
                                                         Region region,
                                                         ComputeManager computeManager) {
        final String uname = "javauser";
        final String password = "12NewPA$$w0rd!";

        VirtualMachineImage linuxVmImage = null;
        PagedList<VirtualMachineImage> vmImages = computeManager
                .virtualMachineImages()
                .listByRegion(region);
        for (VirtualMachineImage vmImage : vmImages) {
            if (vmImage.osDiskImage().operatingSystem() == OperatingSystemTypes.LINUX) {
                linuxVmImage = vmImage;
                break;
            }
        }

        Assert.assertNotNull("A linux image could not found",
                linuxVmImage);
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withNewPrimaryPublicIpAddress(publicIpDnsLabel)
                .withSpecificLinuxImageVersion(linuxVmImage.imageReference())
                .withRootUsername(uname)
                .withRootPassword(password)
                .withUnmanagedDisks()
                .defineUnmanagedDataDisk("disk-1")
                    .withNewVhd(30)
                    .withCaching(CachingTypes.READ_WRITE)
                    .attach()
                .defineUnmanagedDataDisk("disk-2")
                    .withNewVhd(60)
                    .withCaching(CachingTypes.READ_ONLY)
                    .attach()
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withNewStorageAccount(generateRandomResourceName("stg", 17))
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();
        //
         deprovisionLinuxVM(virtualMachine.getPrimaryPublicIpAddress().fqdn(), 22, uname, password);
         virtualMachine.deallocate();
         virtualMachine.generalize();
        return virtualMachine;
    }
}