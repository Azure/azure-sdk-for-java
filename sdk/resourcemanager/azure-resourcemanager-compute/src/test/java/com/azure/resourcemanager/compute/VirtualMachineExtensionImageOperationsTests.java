// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImage;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageType;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageVersion;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageVersions;
import com.azure.resourcemanager.compute.models.VirtualMachinePublisher;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineExtensionImageOperationsTests extends ComputeManagementTest {
    @Test
    public void canListExtensionImages() throws Exception {
        final int maxListing = 20;
        int count = 0;
        PagedIterable<VirtualMachineExtensionImage> extensionImages =
            computeManager.virtualMachineExtensionImages().listByRegion(Region.US_EAST);
        // Lazy listing
        for (VirtualMachineExtensionImage extensionImage : extensionImages) {
            Assertions.assertNotNull(extensionImage);
            count++;
            if (count >= maxListing) {
                break;
            }
        }
        Assertions.assertTrue(count == maxListing);
    }

    @Test
    public void canGetExtensionTypeVersionAndImage() throws Exception {
        PagedIterable<VirtualMachineExtensionImage> extensionImages =
            computeManager.virtualMachineExtensionImages().listByRegion(Region.US_EAST);

        final String dockerExtensionPublisherName = "Microsoft.Azure.Extensions";
        final String dockerExtensionImageTypeName = "DockerExtension";

        // Lookup Azure docker extension publisher
        //
        PagedIterable<VirtualMachinePublisher> publishers =
            computeManager.virtualMachineExtensionImages().publishers().listByRegion(Region.US_EAST);

        VirtualMachinePublisher azureDockerExtensionPublisher = null;
        for (VirtualMachinePublisher publisher : publishers) {
            if (publisher.name().equalsIgnoreCase(dockerExtensionPublisherName)) {
                azureDockerExtensionPublisher = publisher;
                break;
            }
        }
        Assertions.assertNotNull(azureDockerExtensionPublisher);

        // Lookup Azure docker extension type
        //
        VirtualMachineExtensionImageTypes extensionImageTypes = azureDockerExtensionPublisher.extensionTypes();
        Assertions.assertTrue(TestUtilities.getSize(extensionImageTypes.list()) > 0);

        VirtualMachineExtensionImageType dockerExtensionImageType = null;
        for (VirtualMachineExtensionImageType extensionImageType : extensionImageTypes.list()) {
            if (extensionImageType.name().equalsIgnoreCase(dockerExtensionImageTypeName)) {
                dockerExtensionImageType = extensionImageType;
                break;
            }
        }
        Assertions.assertNotNull(dockerExtensionImageType);

        Assertions.assertNotNull(dockerExtensionImageType.id());
        Assertions.assertTrue(dockerExtensionImageType.name().equalsIgnoreCase(dockerExtensionImageTypeName));
        Assertions.assertTrue(dockerExtensionImageType.regionName().equalsIgnoreCase(Region.US_EAST.toString()));
        Assertions
            .assertTrue(
                dockerExtensionImageType
                    .id()
                    .toLowerCase()
                    .endsWith(
                        "/Providers/Microsoft.Compute/Locations/eastus/Publishers/Microsoft.Azure.Extensions/ArtifactTypes/VMExtension/Types/DockerExtension"
                            .toLowerCase()));
        Assertions.assertNotNull(dockerExtensionImageType.publisher());
        Assertions
            .assertTrue(dockerExtensionImageType.publisher().name().equalsIgnoreCase(dockerExtensionPublisherName));

        // Fetch Azure docker extension versions
        //
        VirtualMachineExtensionImageVersions extensionImageVersions = dockerExtensionImageType.versions();
        Assertions.assertTrue(TestUtilities.getSize(extensionImageVersions.list()) > 0);

        VirtualMachineExtensionImageVersion extensionImageFirstVersion = null;
        for (VirtualMachineExtensionImageVersion extensionImageVersion : extensionImageVersions.list()) {
            extensionImageFirstVersion = extensionImageVersion;
            break;
        }

        Assertions.assertNotNull(extensionImageFirstVersion);
        String versionName = extensionImageFirstVersion.name();
        Assertions
            .assertTrue(
                extensionImageFirstVersion
                    .id()
                    .toLowerCase()
                    .endsWith(
                        ("/Providers/Microsoft.Compute/Locations/eastus/Publishers/Microsoft.Azure.Extensions/ArtifactTypes/VMExtension/Types/DockerExtension/Versions/"
                                + versionName)
                            .toLowerCase()));
        Assertions.assertNotNull(extensionImageFirstVersion.type());

        // Fetch the Azure docker extension image
        //
        VirtualMachineExtensionImage dockerExtensionImage = extensionImageFirstVersion.getImage();

        Assertions.assertTrue(dockerExtensionImage.regionName().equalsIgnoreCase(Region.US_EAST.toString()));
        Assertions.assertTrue(dockerExtensionImage.publisherName().equalsIgnoreCase(dockerExtensionPublisherName));
        Assertions.assertTrue(dockerExtensionImage.typeName().equalsIgnoreCase(dockerExtensionImageTypeName));
        Assertions.assertTrue(dockerExtensionImage.versionName().equalsIgnoreCase(versionName));
        Assertions
            .assertTrue(
                dockerExtensionImage.osType() == OperatingSystemTypes.LINUX
                    || dockerExtensionImage.osType() == OperatingSystemTypes.WINDOWS);
    }
}
