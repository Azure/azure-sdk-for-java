// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.Gallery;
import com.azure.resourcemanager.compute.models.GalleryImage;
import com.azure.resourcemanager.compute.models.GalleryImageVersion;
import com.azure.resourcemanager.compute.models.HyperVGeneration;
import com.azure.resourcemanager.compute.models.HyperVGenerationTypes;
import com.azure.resourcemanager.compute.models.ImageDataDisk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemStateTypes;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.SecurityTypes;
import com.azure.resourcemanager.compute.models.TargetRegion;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class SharedGalleryImageTests extends ComputeManagementTest {
    private String rgName = "";
    private final Region region = Region.US_SOUTH_CENTRAL;
    private final String vmName = "javavm";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateUpdateListGetDeleteGallery() {
        // Create a gallery
        //
        Gallery javaGallery =
            this
                .computeManager
                .galleries()
                .define("JavaImageGallery")
                .withRegion(region)
                .withNewResourceGroup(rgName)
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
        javaGallery.update().withDescription("updated java's image gallery").withTag("jdk", "openjdk").apply();

        Assertions.assertEquals("updated java's image gallery", javaGallery.description());
        Assertions.assertNotNull(javaGallery.tags());
        Assertions.assertEquals(1, javaGallery.tags().size());
        //
        // List galleries
        //
        PagedIterable<Gallery> galleries = this.computeManager.galleries().listByResourceGroup(rgName);
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
        Gallery javaGallery =
            this
                .computeManager
                .galleries()
                .define(galleryName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withDescription("java's image gallery")
                .create();
        //
        // Create an image in the gallery
        //
        GalleryImage galleryImage =
            this
                .computeManager
                .galleryImages()
                .define(galleryImageName)
                .withExistingGallery(javaGallery)
                .withLocation(region)
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
        Assertions.assertNotNull(galleryImage.innerModel());
        Assertions.assertTrue(galleryImage.location().equalsIgnoreCase(region.toString()));
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
        galleryImage
            .update()
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

        String description = "This is my gallery image";
        String releaseURI = "http://www.example.com/compute/galleryimageuri";
        OffsetDateTime offsetDateTime = OffsetDateTime.now().plusDays(10);
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "myTag1");
        galleryImage
            .update()
            .withDescription(description)
            .withReleaseNoteUri(releaseURI)
            .withEndOfLifeDate(offsetDateTime)
            .withRecommendedCPUsCountForVirtualMachine(10, 20)
            .withRecommendedMemoryForVirtualMachine(10, 20)
            .withUnsupportedDiskType(DiskSkuTypes.PREMIUM_LRS)
//            .withOsState(OperatingSystemStateTypes.SPECIALIZED) // changing of osState is not allowed
            .withTags(tags)
            .apply();

        galleryImage.refresh();

        Assertions.assertEquals(description, galleryImage.description());
        Assertions.assertEquals(releaseURI, galleryImage.releaseNoteUri());
        Assertions.assertEquals(tags, galleryImage.tags());
        Assertions.assertEquals(10, galleryImage.recommendedVirtualMachineConfiguration().vCPUs().min());
        Assertions.assertEquals(20, galleryImage.recommendedVirtualMachineConfiguration().vCPUs().max());
        Assertions.assertEquals(10, galleryImage.recommendedVirtualMachineConfiguration().memory().min());
        Assertions.assertEquals(20, galleryImage.recommendedVirtualMachineConfiguration().memory().max());
        Assertions.assertTrue(galleryImage.disallowed().diskTypes().contains(DiskSkuTypes.PREMIUM_LRS.toString()));
//        Assertions.assertEquals(galleryImage.osState(), OperatingSystemStateTypes.SPECIALIZED);

        //
        // List images in the gallery
        //
        PagedIterable<GalleryImage> images = this.computeManager.galleryImages().listByGallery(rgName, galleryName);

        Assertions.assertEquals(1, TestUtilities.getSize(images));
        //
        // Get image from gallery
        //
        galleryImage = this.computeManager.galleryImages().getByGallery(rgName, galleryName, galleryImageName);

        Assertions.assertNotNull(galleryImage);
        Assertions.assertNotNull(galleryImage.innerModel());
        //
        // Delete an image from gallery
        //
        this.computeManager.galleryImages().deleteByGallery(rgName, galleryName, galleryImageName);
    }

    @Test
    public void canCreateUpdateGetDeleteGalleryImageVersion() {
        final String galleryName = generateRandomResourceName("jsim", 15); // "jsim94f154754";

        Gallery gallery =
            this
                .computeManager
                .galleries()
                .define(galleryName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withDescription("java's image gallery")
                .create();
        //
        // Create an image in the gallery (a container to hold custom linux image)
        //
        final String galleryImageName = "SampleImages";

        GalleryImage galleryImage =
            this
                .computeManager
                .galleryImages()
                .define(galleryImageName)
                .withExistingGallery(gallery)
                .withLocation(region)
                .withIdentifier("JavaSDKTeam", "JDK", "Jdk-9")
                .withGeneralizedLinux()
                .create();
        //
        // Create a custom image to base the version on
        //
        VirtualMachineCustomImage customImage = prepareCustomImage(rgName, region, computeManager);
        // String customImageId =
        // "/subscriptions/0b1f6471-1bf0-4dda-aec3-cb9272f09590/resourceGroups/javacsmrg91482/providers/Microsoft.Compute/images/img96429090dee3";
        //
        // Create a image version based on the custom image
        //

        final String versionName = "0.0.4";

        GalleryImageVersion imageVersion =
            this
                .computeManager
                .galleryImageVersions()
                .define(versionName)
                .withExistingImage(rgName, gallery.name(), galleryImage.name())
                .withLocation(region.toString())
                .withSourceCustomImage(customImage)
                // Options - Start
                .withRegionAvailability(Region.US_WEST2, 1)
                // Options - End
                .create();

        Assertions.assertNotNull(imageVersion);
        Assertions.assertNotNull(imageVersion.innerModel());
        Assertions.assertNotNull(imageVersion.availableRegions());
        Assertions.assertEquals(2, imageVersion.availableRegions().size());
        boolean found = false;
        String expectedRegion = "westus2";
        for (TargetRegion targetRegion : imageVersion.availableRegions()) {
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
        imageVersion.update().withoutRegionAvailability(Region.US_WEST2).apply();

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
        this
            .computeManager
            .galleryImageVersions()
            .deleteByGalleryImage(rgName, galleryName, galleryImageName, versionName);
    }

    @Test
    public void canCreateTrustedLaunchVMsFromGalleryImage() {
        final String galleryName = generateRandomResourceName("jsim", 15);

        Gallery gallery =
            this
                .computeManager
                .galleries()
                .define(galleryName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withDescription("java's image gallery")
                .create();

        final String galleryImageName = "SampleImages";
        GalleryImage galleryImage =
            this
                .computeManager
                .galleryImages()
                .define(galleryImageName)
                .withExistingGallery(gallery)
                .withLocation(region)
                .withIdentifier("JavaSDKTeam", "JDK", "Jdk-9")
                .withGeneralizedLinux()
                .withHyperVGeneration(HyperVGeneration.V2)
                .withTrustedLaunch()
                .create();

        Assertions.assertEquals(HyperVGeneration.V2, galleryImage.hyperVGeneration());
        Assertions.assertEquals(SecurityTypes.TRUSTED_LAUNCH, galleryImage.securityType());

        VirtualMachineCustomImage customImage = prepareCustomImageWithTrustedLaunch(rgName, region, computeManager);

        final String versionName = "0.0.1";

        GalleryImageVersion imageVersion =
            this
                .computeManager
                .galleryImageVersions()
                .define(versionName)
                .withExistingImage(rgName, gallery.name(), galleryImage.name())
                .withLocation(region.toString())
                .withSourceCustomImage(customImage)
                .withRegionAvailability(Region.US_WEST2, 1)
                .create();

        final String trustedLaunchVmName = generateRandomResourceName("tlvm", 15);

        VirtualMachine.DefinitionStages.WithManagedCreate withManagedCreate = computeManager
            .virtualMachines()
            .define(trustedLaunchVmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.1.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withGeneralizedLinuxCustomImage(imageVersion.id())
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey());

        for (ImageDataDisk ddi : customImage.dataDiskImages().values()) {
            withManagedCreate.withNewDataDiskFromImage(ddi.lun(), ddi.diskSizeGB() + 1, ddi.caching());
        }

        VirtualMachine trustedLaunchVm =
            withManagedCreate
                .withSize(VirtualMachineSizeTypes.STANDARD_D2S_V3)
                // gallery images with 'TrustedLaunch` feature can only create VMs with 'TrustedLaunch' feature
                .withTrustedLaunch()
                .withSecureBoot()
                .withVTpm()
                .create();

        Assertions.assertEquals(SecurityTypes.TRUSTED_LAUNCH, trustedLaunchVm.securityType());
        Assertions.assertTrue(trustedLaunchVm.isSecureBootEnabled());
        Assertions.assertTrue(trustedLaunchVm.isVTpmEnabled());

        Assertions.assertEquals(2, trustedLaunchVm.dataDisks().size());
    }

    private VirtualMachineCustomImage prepareCustomImageWithTrustedLaunch(String rgName, Region region, ComputeManager computeManager) {
        final String uname = "javauser";
        final KnownLinuxVirtualMachineImage linuxImage = KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS_GEN2;
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(linuxImage)
                .withRootUsername(uname)
                .withSsh(sshPublicKey())
                .withNewDataDisk(1)
                .withNewDataDisk(1, 2, CachingTypes.READ_WRITE)
                .withSize(VirtualMachineSizeTypes.STANDARD_D2S_V3)
                .withNewStorageAccount(generateRandomResourceName("stg", 17))
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withTrustedLaunch()
                .withSecureBoot()
                .withVTpm()
                .create();

        virtualMachine.deallocate();
        virtualMachine.generalize();

        final String imageName = generateRandomResourceName("img", 20);
        //
        VirtualMachineCustomImage.DefinitionStages.WithCreateAndDataDiskImageOSDiskSettings creatableDisk =
            computeManager
                .virtualMachineCustomImages()
                .define(imageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withHyperVGeneration(HyperVGenerationTypes.V2)
                .withLinuxFromDisk(virtualMachine.osDiskId(), OperatingSystemStateTypes.GENERALIZED)
                .withOSDiskCaching(virtualMachine.osDiskCachingType());

        for (VirtualMachineDataDisk dataDisk : virtualMachine.dataDisks().values()) {
            creatableDisk.defineDataDiskImage()
                .withLun(dataDisk.lun())
                .fromManagedDisk(dataDisk.id())
                .withDiskCaching(dataDisk.cachingType())
                .withDiskSizeInGB(dataDisk.size() + 1)
                .attach();
        }

        return creatableDisk.create();
    }

    private VirtualMachineCustomImage prepareCustomImage(String rgName, Region region, ComputeManager computeManager) {
        VirtualMachine linuxVM =
            prepareGeneralizedVmWith2EmptyDataDisks(
                rgName, generateRandomResourceName("muldvm", 15), region, computeManager);

        final String vhdBasedImageName = generateRandomResourceName("img", 20);
        //
        VirtualMachineCustomImage.DefinitionStages.WithCreateAndDataDiskImageOSDiskSettings creatableDisk =
            computeManager
                .virtualMachineCustomImages()
                .define(vhdBasedImageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinuxFromVhd(linuxVM.osUnmanagedDiskVhdUri(), OperatingSystemStateTypes.GENERALIZED)
                .withOSDiskCaching(linuxVM.osDiskCachingType());
        for (VirtualMachineUnmanagedDataDisk disk : linuxVM.unmanagedDataDisks().values()) {
            creatableDisk
                .defineDataDiskImage()
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

    private VirtualMachine prepareGeneralizedVmWith2EmptyDataDisks(
        String rgName, String vmName, Region region, ComputeManager computeManager) {
        final String uname = "javauser";
        final String password = password();
        final KnownLinuxVirtualMachineImage linuxImage = KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS;
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(linuxImage)
                .withRootUsername(uname)
                .withSsh(sshPublicKey())
                .withUnmanagedDisks()
                .defineUnmanagedDataDisk("disk-1")
                .withNewVhd(30)
                .withCaching(CachingTypes.READ_WRITE)
                .attach()
                .defineUnmanagedDataDisk("disk-2")
                .withNewVhd(60)
                .withCaching(CachingTypes.READ_ONLY)
                .attach()
                .withSize(VirtualMachineSizeTypes.STANDARD_D2S_V3)
                .withNewStorageAccount(generateRandomResourceName("stg", 17))
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();
        //
        deprovisionAgentInLinuxVM(virtualMachine);
        virtualMachine.deallocate();
        virtualMachine.generalize();
        return virtualMachine;
    }
}
