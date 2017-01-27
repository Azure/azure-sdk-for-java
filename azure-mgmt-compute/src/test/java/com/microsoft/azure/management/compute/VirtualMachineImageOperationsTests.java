package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class VirtualMachineImageOperationsTests extends ComputeManagementTest {
    @Test
    public void canListVirtualMachineImages() throws Exception {
        final int maxListing = 20;
        int count = 0;
        PagedList<VirtualMachineImage> images = computeManager.virtualMachineImages()
                .listByRegion(Region.US_EAST);
        // Lazy listing
        for (VirtualMachineImage image : images) {
            count++;
            if (count >= maxListing) {
                break;
            }
        }
        Assert.assertTrue(count == maxListing);

        List<VirtualMachinePublisher> publishers =
                computeManager.virtualMachineImages().publishers().listByRegion(Region.US_EAST);

        VirtualMachinePublisher canonicalPublisher = null;
        for (VirtualMachinePublisher publisher : publishers) {
            if (publisher.name().equalsIgnoreCase("Canonical")) {
                canonicalPublisher = publisher;
                break;
            }
        }

        Assert.assertNotNull(canonicalPublisher);
        VirtualMachineImage firstVMImage = null;
        for (VirtualMachineOffer offer : canonicalPublisher.offers().list()) {
            for (VirtualMachineSku sku: offer.skus().list()) {
                for (VirtualMachineImage image : sku.images().list()) {
                    firstVMImage = image;
                    break;
                }
                if (firstVMImage != null) {
                    break;
                }
            }

            if (firstVMImage != null) {
                break;
            }
        }

        Assert.assertNotNull(firstVMImage);
        for (DataDiskImage diskImage : firstVMImage.dataDiskImages().values()) {
            Assert.assertNotNull(diskImage.lun());
        }
    }


    private void foo() {
        computeManager.virtualMachineCustomImages()
                .define("myimage")
                .withRegion(Region.US_EAST)
                .withNewResourceGroup("rg")
                .fromVirtualMachine("")
                .createAsync();

        // withWindowsFromDisk(Disk, OperatingSystemStateTypes)
        // withWindowsFromSnapshot(Snapshot, OperatingSystemStateTypes)
        // withWindowsFromVhd(string vhdUrl, OperatingSystemStateTypes)

        // .defineDataDiskImage(void)
        //        .withLun(int)
        //        .withSizeInGB(int)


//        computeManager.disks()
//                .define("")
//                .withRegion(Region.US_EAST)
//                .withNewResourceGroup("")
//                .withWindowsFromDisk | withLinuxFromDisk |

        // Operating System short form is "OS" not 'Os'

//        computeManager.disks()
//                .define("")
//                .withRegion(Region.US_EAST)
//                .withNewResourceGroup("")
//                .withWindowsFromSnapshot()
//                .create();
//
//
//        computeManager.disks()
//                .define("")
//                .withRegion(Region.US_EAST)
//                .withNewResourceGroup("")
//                .withData()
//                .withSize(100)
//                // Optionals
//                .create();
//                // Optionals
//
//
//
//        computeManager.disks()
//                .define("")
//                .withRegion(Region.US_EAST)
//                .withNewResourceGroup("")
//                .withDataFromSnapshot(id)
//                // Optionals
//                .withSize()
//                .create();
//                // Optionals
//
//
//
//
//        // withSpecializedLinuxOSDisk() -> for native
//
//        computeManager.virtualMachines()
//                .define("")
//                .withRegion(Region.US_EAST)
//                .withNewResourceGroup("")
//                .withNewPrimaryNetwork("111")
//                .withPrimaryPrivateIpAddressDynamic()
//                .withoutPrimaryPublicIpAddress()
//                .withStoredLinuxImage()
//                .withRootUsername()
//                .withRootPassword()
//                .




    }

}