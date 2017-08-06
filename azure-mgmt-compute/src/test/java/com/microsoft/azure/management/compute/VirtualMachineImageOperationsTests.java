/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

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
                    System.out.println(image.version());
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

        VirtualMachineImage vmImage = computeManager.virtualMachineImages()
            .getImage(Region.US_EAST, firstVMImage.publisherName(), firstVMImage.offer(), firstVMImage.sku(), firstVMImage.version());
        Assert.assertNotNull(vmImage);

        vmImage = computeManager.virtualMachineImages()
            .getImage("eastus", firstVMImage.publisherName(), firstVMImage.offer(), firstVMImage.sku(), firstVMImage.version());
        Assert.assertNotNull(vmImage);

        vmImage = computeManager.virtualMachineImages()
                .getImage("eastus", firstVMImage.publisherName(), firstVMImage.offer(), firstVMImage.sku(), "latest");
        Assert.assertNotNull(vmImage);
    }
}