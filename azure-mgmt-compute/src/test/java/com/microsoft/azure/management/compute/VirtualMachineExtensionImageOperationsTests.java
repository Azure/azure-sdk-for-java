package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class VirtualMachineExtensionImageOperationsTests extends ComputeManagementTest {
    @Test
    public void canListExtensionImages() throws Exception {
        final int maxListing = 20;
        int count = 0;
        List<VirtualMachineExtensionImage> extensionImages =
                computeManager.virtualMachineExtensionImages()
                        .listByRegion(Region.US_EAST);
        // Lazy listing
        for (VirtualMachineExtensionImage extensionImage : extensionImages) {
            Assert.assertNotNull(extensionImage);
            count++;
            if (count >= maxListing) {
                break;
            }
        }
        Assert.assertTrue(count == maxListing);
    }

    @Test
    public void canGetExtensionTypeVersionAndImage() throws Exception {
        List<VirtualMachineExtensionImage> extensionImages =
                computeManager.virtualMachineExtensionImages()
                        .listByRegion(Region.US_EAST);

        final String dockerExtensionPublisherName = "Microsoft.Azure.Extensions";
        final String dockerExtensionImageTypeName = "DockerExtension";

        // Lookup Azure docker extension publisher
        //
        List<VirtualMachinePublisher> publishers =
                computeManager.virtualMachineExtensionImages()
                        .publishers()
                        .listByRegion(Region.US_EAST);

        VirtualMachinePublisher azureDockerExtensionPublisher = null;
        for (VirtualMachinePublisher publisher : publishers) {
            if (publisher.name().equalsIgnoreCase(dockerExtensionPublisherName)) {
                azureDockerExtensionPublisher = publisher;
                break;
            }
        }
        Assert.assertNotNull(azureDockerExtensionPublisher);

        // Lookup Azure docker extension type
        //
        VirtualMachineExtensionImageTypes extensionImageTypes = azureDockerExtensionPublisher.extensionTypes();
        Assert.assertTrue(extensionImageTypes.list().size() > 0);

        VirtualMachineExtensionImageType dockerExtensionImageType = null;
        for (VirtualMachineExtensionImageType extensionImageType : extensionImageTypes.list()) {
            if (extensionImageType.name().equalsIgnoreCase(dockerExtensionImageTypeName)) {
                dockerExtensionImageType = extensionImageType;
                break;
            }
        }
        Assert.assertNotNull(dockerExtensionImageType);

        Assert.assertNotNull(dockerExtensionImageType.id());
        Assert.assertTrue(dockerExtensionImageType.name().equalsIgnoreCase(dockerExtensionImageTypeName));
        Assert.assertTrue(dockerExtensionImageType.regionName().equalsIgnoreCase(Region.US_EAST.toString()));
        Assert.assertTrue(dockerExtensionImageType.id()
                .toLowerCase()
                .endsWith("/Providers/Microsoft.Compute/Locations/eastus/Publishers/Microsoft.Azure.Extensions/ArtifactTypes/VMExtension/Types/DockerExtension".toLowerCase()));
        Assert.assertNotNull(dockerExtensionImageType.publisher());
        Assert.assertTrue(dockerExtensionImageType.publisher().name().equalsIgnoreCase(dockerExtensionPublisherName));

        // Fetch Azure docker extension versions
        //
        VirtualMachineExtensionImageVersions extensionImageVersions = dockerExtensionImageType.versions();
        Assert.assertTrue(extensionImageVersions.list().size() > 0);

        VirtualMachineExtensionImageVersion extensionImageFirstVersion = null;
        for (VirtualMachineExtensionImageVersion extensionImageVersion : extensionImageVersions.list()) {
            extensionImageFirstVersion = extensionImageVersion;
            break;
        }

        Assert.assertNotNull(extensionImageFirstVersion);
        String versionName = extensionImageFirstVersion.name();
        Assert.assertTrue(extensionImageFirstVersion.id()
                .toLowerCase()
                .endsWith(("/Providers/Microsoft.Compute/Locations/eastus/Publishers/Microsoft.Azure.Extensions/ArtifactTypes/VMExtension/Types/DockerExtension/Versions/" + versionName).toLowerCase()));
        Assert.assertNotNull(extensionImageFirstVersion.type());

        // Fetch the Azure docker extension image
        //
        VirtualMachineExtensionImage dockerExtensionImage = extensionImageFirstVersion.getImage();

        Assert.assertTrue(dockerExtensionImage.regionName().equalsIgnoreCase(Region.US_EAST.toString()));
        Assert.assertTrue(dockerExtensionImage.publisherName().equalsIgnoreCase(dockerExtensionPublisherName));
        Assert.assertTrue(dockerExtensionImage.typeName().equalsIgnoreCase(dockerExtensionImageTypeName));
        Assert.assertTrue(dockerExtensionImage.versionName().equalsIgnoreCase(versionName));
        Assert.assertTrue(dockerExtensionImage.osType() == OperatingSystemTypes.LINUX || dockerExtensionImage.osType() == OperatingSystemTypes.WINDOWS);
    }
}