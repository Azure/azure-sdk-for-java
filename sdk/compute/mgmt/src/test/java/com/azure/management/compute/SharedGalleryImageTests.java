/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.RestClient;
import com.azure.management.compute.implementation.ComputeManager;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SharedGalleryImageTests extends ComputeManagementTest {
    private String RG_NAME = "";
    private final Region REGION = Region.US_WEST_CENTRAL;
    private final String VMNAME = "javavm";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
       resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCreateUpdateListGetDeleteGallery() {
        // Create a gallery
        //
        Gallery javaGallery = this.computeManager.galleries().define("JavaImageGallery")
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                // Optionals - Start
                .withDescription("java's image gallery")
                // Optionals - End
                .create();

        Assertions.assertNotNull(javaGallery.uniqueName());
        Assertions.assertEquals("JavaImageGallery", javaGallery.name());
        Assertions.assertEquals("java's image gallery", javaGallery.description());
        Assertions.assertNotNull(javaGallery.provisioningState());
        //
        // Update the gallery
        //
        javaGallery.update()
                .withDescription("updated java's image gallery")
                .withTag("jdk", "openjdk")
                .apply();

        Assertions.assertEquals("updated java's image gallery", javaGallery.description());
        Assertions.assertNotNull(javaGallery.tags());
        Assertions.assertEquals(1, javaGallery.tags().size());
        //
        // List galleries
        //
        PagedIterable<Gallery> galleries = this.computeManager.galleries().listByResourceGroup(RG_NAME);
        Assertions.assertEquals(1, TestUtilities.getSize(galleries));
        galleries = this.computeManager.galleries().list();
        Assertions.assertTrue(TestUtilities.getSize(galleries) > 0);
        //
        this.computeManager.galleries().deleteByResourceGroup(javaGallery.resourceGroupName(), javaGallery.name());
    }

    @Test
    public void canCreateUpdateGetDeleteGalleryImage() {
        final String galleryName = generateRandomResourceName("jsim", 15);
        final String galleryImageName = "JavaImages";

        // Create a gallery
        //
        Gallery javaGallery = this.computeManager.galleries().define(galleryName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .withDescription("java's image gallery")
                .create();
        //
        // Create an image in the gallery
        //
        GalleryImage galleryImage = this.computeManager.galleryImages().define(galleryImageName)
                .withExistingGallery(javaGallery)
                .withLocation(REGION)
                .withIdentifier("JavaSDKTeam", "JDK", "Jdk-9")
                .withGeneralizedWindows()
                // Optionals - Start
                .withUnsupportedDiskType(DiskSkuTypes.STANDARD_LRS)
                .withUnsupportedDiskType(DiskSkuTypes.PREMIUM_LRS)
                .withRecommendedMaximumCPUsCountForVirtualMachine(25)
                .withRecommendedMaximumMemoryForVirtualMachine(3200)
                // Options - End
                .create();

        Assertions.assertNotNull(galleryImage);
        Assertions.assertNotNull(galleryImage.inner());
        Assertions.assertTrue(galleryImage.location().equalsIgnoreCase(REGION.toString()));
        Assertions.assertTrue(galleryImage.osType().equals(OperatingSystemTypes.WINDOWS));
        Assertions.assertTrue(galleryImage.osState().equals(OperatingSystemStateTypes.GENERALIZED));
        Assertions.assertEquals(2, galleryImage.unsupportedDiskTypes().size());
        Assertions.assertNotNull(galleryImage.identifier());
        Assertions.assertEquals("JavaSDKTeam", galleryImage.identifier().publisher());
        Assertions.assertEquals("JDK", galleryImage.identifier().offer());
        Assertions.assertEquals("Jdk-9", galleryImage.identifier().sku());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().vCPUs());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().vCPUs().max());
        Assertions.assertEquals(25, galleryImage.recommendedVirtualMachineConfiguration().vCPUs().max().intValue());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().memory());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().memory().max());
        Assertions.assertEquals(3200, galleryImage.recommendedVirtualMachineConfiguration().memory().max().intValue());
        //
        // Update an image in the gallery
        //
        galleryImage.update()
                .withoutUnsupportedDiskType(DiskSkuTypes.PREMIUM_LRS)
                .withRecommendedMinimumCPUsCountForVirtualMachine(15)
                .withRecommendedMemoryForVirtualMachine(2200, 3200)
                .apply();

        Assertions.assertEquals(1, galleryImage.unsupportedDiskTypes().size());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().vCPUs());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().vCPUs().max());
        Assertions.assertEquals(25, galleryImage.recommendedVirtualMachineConfiguration().vCPUs().max().intValue());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().vCPUs().min());
        Assertions.assertEquals(15, galleryImage.recommendedVirtualMachineConfiguration().vCPUs().min().intValue());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().memory());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().memory().max());
        Assertions.assertEquals(3200, galleryImage.recommendedVirtualMachineConfiguration().memory().max().intValue());
        Assertions.assertNotNull(galleryImage.recommendedVirtualMachineConfiguration().memory().min());
        Assertions.assertEquals(2200, galleryImage.recommendedVirtualMachineConfiguration().memory().min().intValue());
        //
        // List images in the gallery
        //
        PagedIterable<GalleryImage> images = this.computeManager.galleryImages().listByGallery(RG_NAME, galleryName);

        Assertions.assertEquals(1, TestUtilities.getSize(images));
        //
        // Get image from gallery
        //
        galleryImage = this.computeManager.galleryImages().getByGallery(RG_NAME, galleryName, galleryImageName);

        Assertions.assertNotNull(galleryImage);
        Assertions.assertNotNull(galleryImage.inner());
        //
        // Delete an image from gallery
        //
        this.computeManager.galleryImages().deleteByGallery(RG_NAME, galleryName, galleryImageName);
    }

    @Test
    @Disabled("Service consistently fail with error 'Replication job not completed at region:XXXXX', reported to service team, ")
    public void canCreateUpdateGetDeleteGalleryImageVersion() {
        //
        // Create {
        //  "startTime": "2018-09-18T19:19:33.6467692+00:00",
        //  "endTime": "2018-09-18T19:27:34.3244427+00:00",
        //  "status": "Failed",
        //  "error": {
        //    "code": "CrpPirReplicationJobsNotCompleted",
        //    "message": "Replication job not completed at region: westcentralus"
        //  },
        //  "name": "971500cb-f79e-4303-9f6a-df90010a7cc1"
        //}a gallery
        //
        final String galleryName = generateRandomResourceName("jsim", 15); // "jsim94f154754";

        Gallery gallery = this.computeManager.galleries().define(galleryName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .withDescription("java's image gallery")
                .create();
        //
        // Create an image in the gallery (a container to hold custom linux image)
        //
        final String galleryImageName = "SampleImages";

        GalleryImage galleryImage = this.computeManager.galleryImages().define(galleryImageName)
                .withExistingGallery(gallery)
                .withLocation(REGION)
                .withIdentifier("JavaSDKTeam", "JDK", "Jdk-9")
                .withGeneralizedLinux()
                .create();
        //
        // Create a custom image to base the version on
        //
        VirtualMachineCustomImage customImage = prepareCustomImage(RG_NAME, REGION, computeManager);
        // String customImageId = "/subscriptions/0b1f6471-1bf0-4dda-aec3-cb9272f09590/resourceGroups/javacsmrg91482/providers/Microsoft.Compute/images/img96429090dee3";
        //
        // Create a image version based on the custom image
        //

        final String versionName = "0.0.4";

        GalleryImageVersion imageVersion = this.computeManager.galleryImageVersions().define(versionName)
                .withExistingImage(RG_NAME, gallery.name(), galleryImage.name())
                .withLocation(REGION.toString())
                .withSourceCustomImage(customImage)
                // Options - Start
                .withRegionAvailability(Region.US_WEST2, 1)
                // Options - End
                .create();

        Assertions.assertNotNull(imageVersion);
        Assertions.assertNotNull(imageVersion.inner());
        Assertions.assertNotNull(imageVersion.availableRegions());
        Assertions.assertEquals(2, imageVersion.availableRegions().size());
        boolean found = false;
        String expectedRegion = "westus2";
        for(TargetRegion targetRegion: imageVersion.availableRegions()) {
            if (targetRegion.name().replaceAll("\\s", "").equalsIgnoreCase(expectedRegion)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "expected region" + expectedRegion + " not found");
        Assertions.assertFalse(imageVersion.isExcludedFromLatest());

        //
        // Update image version
        //
        imageVersion.update()
                .withoutRegionAvailability(Region.US_WEST2)
                .apply();

        Assertions.assertNotNull(imageVersion.availableRegions());
        Assertions.assertEquals(1, imageVersion.availableRegions().size());
        Assertions.assertFalse(imageVersion.isExcludedFromLatest());

        //
        // List image versions
        //
        PagedIterable<GalleryImageVersion> versions = galleryImage.listVersions();

        Assertions.assertNotNull(versions);
        Assertions.assertTrue(TestUtilities.getSize(versions) > 0);

        //
        // Delete the image version
        //
        this.computeManager.galleryImageVersions().deleteByGalleryImage(RG_NAME, galleryName, galleryImageName, versionName);
    }

    private VirtualMachineCustomImage prepareCustomImage(String rgName, Region region, ComputeManager computeManager) {
        VirtualMachine linuxVM = prepareGeneralizedVmWith2EmptyDataDisks(rgName,
                generateRandomResourceName("muldvm", 15),
                region,
                computeManager);

        final String vhdBasedImageName = generateRandomResourceName("img", 20);
        //
        VirtualMachineCustomImage.DefinitionStages.WithCreateAndDataDiskImageOSDiskSettings
                creatableDisk = computeManager
                .virtualMachineCustomImages()
                .define(vhdBasedImageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinuxFromVhd(linuxVM.osUnmanagedDiskVhdUri(), OperatingSystemStateTypes.GENERALIZED)
                .withOSDiskCaching(linuxVM.osDiskCachingType());
        for (VirtualMachineUnmanagedDataDisk disk : linuxVM.unmanagedDataDisks().values()) {
            creatableDisk.defineDataDiskImage()
                    .withLun(disk.lun())
                    .fromVhd(disk.vhdUri())
                    .withDiskCaching(disk.cachingType())
                    .withDiskSizeInGB(disk.size() + 10) // Resize each data disk image by +10GB
                    .attach();
        }
        //
        VirtualMachineCustomImage customImage = creatableDisk.create();
        return customImage;
    }

    private VirtualMachine prepareGeneralizedVmWith2EmptyDataDisks(String rgName,
                                                                   String vmName,
                                                                   Region region,
                                                                   ComputeManager computeManager) {
        final String uname = "javauser";
        final String password = "12NewPA$$w0rd!";
        final KnownLinuxVirtualMachineImage linuxImage = KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS;
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(linuxImage)
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
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();
        //
        deprovisionAgentInLinuxVM(virtualMachine.getPrimaryPublicIPAddress().fqdn(), 22, uname, password);
        virtualMachine.deallocate();
        virtualMachine.generalize();
        return virtualMachine;
    }

}
