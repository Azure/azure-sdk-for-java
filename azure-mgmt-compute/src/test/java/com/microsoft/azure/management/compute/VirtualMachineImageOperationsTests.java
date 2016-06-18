package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.api.DataDiskImage;
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
        List<VirtualMachineImage.Publisher> publishers =
                computeManager.virtualMachineImages().publishers().listByRegion(Region.US_EAST);

        VirtualMachineImage.Publisher canonicalPublisher = null;
        for (VirtualMachineImage.Publisher publisher : publishers) {
            if (publisher.name().equalsIgnoreCase("Canonical")) {
                canonicalPublisher = publisher;
                break;
            }
        }

        Assert.assertNotNull(canonicalPublisher);
        VirtualMachineImage firstVMImage = null;
        for (Offer offer : canonicalPublisher.offers().list()) {
            for (VirtualMachineImage.Sku sku: offer.listSkus()) {
                for (VirtualMachineImage image : sku.listImages()) {
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
