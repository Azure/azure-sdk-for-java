// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Gallery;
import com.azure.resourcemanager.compute.models.GalleryDataDiskImage;
import com.azure.resourcemanager.compute.models.GalleryImage;
import com.azure.resourcemanager.compute.models.GalleryImageVersion;
import com.azure.resourcemanager.compute.models.HyperVGeneration;
import com.azure.resourcemanager.compute.models.HyperVGenerationTypes;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemStateTypes;
import com.azure.resourcemanager.compute.models.RunCommandInput;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.samples.Utils;

import java.util.Arrays;

/**
 * Azure Compute sample for creating virtual machine with TrustedLaunch from gallery image.
 * - Create a managed virtual machine with TrustedLaunch from PIR windows image with two empty data disks
 * - Prepare virtual machine for generalization
 * - Deallocate virtual machine
 * - Generalize virtual machine
 * - Create a virtual machine custom image from the created virtual machine
 * - Create a gallery to hold gallery images
 * - Create a gallery image in the gallery with hypervisor generation 2 and with TrustedLaunch feature
 * - Create a gallery image version from the virtual machine custom image
 * - Create a virtual machine with TrustedLaunch from the gallery image version
 */
public class CreateVirtualMachineWithTrustedLaunchFromGalleryImage {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azure) {
        final String rgName = Utils.randomResourceName(azure, "rg", 15);
        final Region region = Region.US_WEST;
        final String newRgName = Utils.randomResourceName(azure, "rg", 15);
        final Region newRegion = Region.US_WEST2;

        ResourceGroup resourceGroup = null;
        ResourceGroup newResourceGroup = null;
        try {
            resourceGroup = azure.resourceGroups()
                .define(rgName)
                .withRegion(region)
                .create();
            newResourceGroup = azure.resourceGroups()
                .define(newRgName)
                .withRegion(newRegion)
                .create();

            //=============================================================
            // Create a managed virtual machine with TrustedLaunch from PIR image with two empty data disks
            final String trustedVmName = Utils.randomResourceName(azure, "vm", 15);
            VirtualMachine trustedVMFromPirImage = azure
                .virtualMachines()
                .define(trustedVmName)
                .withRegion(resourceGroup.region())
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2016_DATACENTER_GEN2)
                .withAdminUsername("azureuser")
                .withAdminPassword(Utils.password())
                .withNewDataDisk(10)
                .withNewDataDisk(10, 2, CachingTypes.READ_WRITE)
                .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
                // Trusted Launch options start
                .withTrustedLaunch()
                .withSecureBoot()
                .withVTpm()
                // Trusted Launch options end
                .create();

            Utils.print(trustedVMFromPirImage);

            //=============================================================
            // Prepare virtual machine for generalization
            prepareWindowsVMForGeneralization(trustedVMFromPirImage);

            //=============================================================
            // Deallocate virtual machine
            trustedVMFromPirImage.deallocate();

            //=============================================================
            // Generalize virtual machine
            trustedVMFromPirImage.generalize();

            //=============================================================
            // Create a virtual machine custom image
            final String imageName = Utils.randomResourceName(azure, "img", 20);
            VirtualMachineCustomImage.DefinitionStages.WithCreateAndDataDiskImageOSDiskSettings creatableDisk =
                azure.virtualMachineCustomImages()
                    .define(imageName)
                    .withRegion(resourceGroup.region())
                    .withExistingResourceGroup(resourceGroup)
                    .withHyperVGeneration(HyperVGenerationTypes.V2)
                    .withWindowsFromDisk(trustedVMFromPirImage.osDiskId(), OperatingSystemStateTypes.GENERALIZED)
                    .withOSDiskCaching(trustedVMFromPirImage.osDiskCachingType());

            for (VirtualMachineDataDisk dataDisk : trustedVMFromPirImage.dataDisks().values()) {
                creatableDisk.defineDataDiskImage()
                    .withLun(dataDisk.lun())
                    .fromManagedDisk(dataDisk.id())
                    .withDiskCaching(dataDisk.cachingType())
                    .withDiskSizeInGB(dataDisk.size())
                    .attach();
            }

            VirtualMachineCustomImage customImage = creatableDisk.create();
            Utils.print(customImage);

            //=============================================================
            // Create a gallery to hold gallery images
            final String galleryName = Utils.randomResourceName(azure, "jsim", 15);
            Gallery gallery = azure
                .galleries()
                .define(galleryName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withExistingResourceGroup(resourceGroup)
                .withDescription("java's image gallery")
                .create();

            //=============================================================
            // Create a gallery image in the gallery with hypervisor generation 2 and with TrustedLaunch feature
            final String galleryImageName = "SampleImages";
            GalleryImage galleryImage = azure
                .galleryImages()
                .define(galleryImageName)
                .withExistingGallery(gallery)
                .withLocation(resourceGroup.region())
                .withIdentifier("JavaSDKTeam", "JDK", "Jdk-9")
                .withGeneralizedWindows()
                .withHyperVGeneration(HyperVGeneration.V2)
                .withTrustedLaunch()
                .create();

            //=============================================================
            // Create a gallery image version from the virtual machine custom image
            final String versionName = "0.0.1";
            GalleryImageVersion imageVersion = azure
                .galleryImageVersions()
                .define(versionName)
                .withExistingImage(resourceGroup.name(), gallery.name(), galleryImage.name())
                .withLocation(resourceGroup.region())
                .withSourceCustomImage(customImage)
                .withRegionAvailability(newRegion, 1)
                .create();

            //=============================================================
            // Create a virtual machine with TrustedLaunch from the gallery image version
            final String vmFromImageName = Utils.randomResourceName(azure, "tlvm", 15);
            VirtualMachine.DefinitionStages.WithManagedCreate withManagedCreate =
                azure
                    .virtualMachines()
                    .define(vmFromImageName)
                    .withRegion(newRegion)
                    .withExistingResourceGroup(newResourceGroup)
                    .withNewPrimaryNetwork("10.0.1.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withGeneralizedWindowsGalleryImageVersion(imageVersion.id())
                    .withAdminUsername("jvuser")
                    .withAdminPassword(Utils.password());

            for (GalleryDataDiskImage ddi : imageVersion.storageProfile().dataDiskImages()) {
                withManagedCreate.withNewDataDiskFromImage(
                    ddi.lun(),
                    ddi.sizeInGB() + 1,
                    ddi.hostCaching() == null ? null : CachingTypes.fromString(ddi.hostCaching().toString()));
            }

            VirtualMachine trustedVMFromGalleryImage = withManagedCreate
                .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
                // gallery images with 'TrustedLaunch` feature can only create VMs with 'TrustedLaunch' feature
                .withTrustedLaunch()
                .withSecureBoot()
                .withVTpm()
                .create();
            Utils.print(trustedVMFromGalleryImage);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (resourceGroup != null) {
                azure.resourceGroups().beginDeleteByName(rgName);
            }
            if (newResourceGroup != null) {
                azure.resourceGroups().beginDeleteByName(newRgName);
            }
        }
    }

    //https://learn.microsoft.com/en-us/azure/virtual-machines/generalize#windows
    private static void prepareWindowsVMForGeneralization(VirtualMachine virtualMachine) {
        System.out.println("Trying to de-provision");
        virtualMachine.manager()
            .serviceClient()
            .getVirtualMachines()
            .beginRunCommand(
                virtualMachine.resourceGroupName(), virtualMachine.name(),
                new RunCommandInput()
                    .withCommandId("RunPowerShellScript")
                    .withScript(Arrays.asList(
                        "Remove-Item 'C:\\Windows\\Panther' -Recurse",
                        "& $env:SystemRoot\\System32\\Sysprep\\Sysprep.exe /oobe /generalize /mode:vm /shutdown"
                    )))
            .waitForCompletion();
        System.out.println("De-provision finished");
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
