// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.DataDiskImage;
import com.azure.resourcemanager.compute.models.VirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineOffer;
import com.azure.resourcemanager.compute.models.VirtualMachinePublisher;
import com.azure.resourcemanager.compute.models.VirtualMachineSku;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineImageOperationsTests extends ComputeManagementTest {
    @Test
    public void canListVirtualMachineImages() throws Exception {
        PagedIterable<VirtualMachineImage> images = computeManager.virtualMachineImages().listByRegion(Region.US_EAST);
        Assertions.assertTrue(TestUtilities.getSize(images) > 0);

        PagedIterable<VirtualMachinePublisher> publishers =
            computeManager.virtualMachineImages().publishers().listByRegion(Region.US_EAST);

        VirtualMachinePublisher canonicalPublisher = null;
        for (VirtualMachinePublisher publisher : publishers) {
            if (publisher.name().equalsIgnoreCase("Canonical")) {
                canonicalPublisher = publisher;
                break;
            }
        }

        Assertions.assertNotNull(canonicalPublisher);
        VirtualMachineImage firstVMImage = null;
        for (VirtualMachineOffer offer : canonicalPublisher.offers().list()) {
            for (VirtualMachineSku sku : offer.skus().list()) {
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

        Assertions.assertNotNull(firstVMImage);
        for (DataDiskImage diskImage : firstVMImage.dataDiskImages().values()) {
            Assertions.assertNotNull(diskImage.lun());
        }

        VirtualMachineImage vmImage =
            computeManager
                .virtualMachineImages()
                .getImage(
                    Region.US_EAST,
                    firstVMImage.publisherName(),
                    firstVMImage.offer(),
                    firstVMImage.sku(),
                    firstVMImage.version());
        Assertions.assertNotNull(vmImage);

        vmImage =
            computeManager
                .virtualMachineImages()
                .getImage(
                    "eastus",
                    firstVMImage.publisherName(),
                    firstVMImage.offer(),
                    firstVMImage.sku(),
                    firstVMImage.version());
        Assertions.assertNotNull(vmImage);

        vmImage =
            computeManager
                .virtualMachineImages()
                .getImage("eastus", firstVMImage.publisherName(), firstVMImage.offer(), firstVMImage.sku(), "latest");
        Assertions.assertNotNull(vmImage);
    }
}
