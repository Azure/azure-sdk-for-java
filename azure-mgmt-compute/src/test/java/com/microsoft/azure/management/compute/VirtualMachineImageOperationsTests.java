package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class VirtualMachineImageOperationsTests extends ComputeManagementTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

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
        for (DataDiskImage diskImage : firstVMImage.dataDiskImages()) {
            Assert.assertNotNull(diskImage.lun());
        }
    }
}