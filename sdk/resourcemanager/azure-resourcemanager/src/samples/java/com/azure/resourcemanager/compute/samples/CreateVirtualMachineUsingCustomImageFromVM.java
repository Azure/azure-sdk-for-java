// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.samples.SampleUtils;

/**
 * Azure Compute sample for creating a virtual machine from a custom image.
 *  - Create a managed virtual machine from a marketplace image
 *  - Deallocate and generalize the virtual machine
 *  - Capture a custom image from the virtual machine
 *  - Create a second virtual machine using the custom image
 *  - Delete the custom image
 */
public final class CreateVirtualMachineUsingCustomImageFromVM {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST2;
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String linuxVMName1 = SampleUtils.randomResourceName(azureResourceManager, "VM1", 15);
        final String linuxVMName2 = SampleUtils.randomResourceName(azureResourceManager, "VM2", 15);
        final String customImageName = SampleUtils.randomResourceName(azureResourceManager, "img", 15);
        final String userName = "tirekicker";
        final String sshPublicKey = SampleUtils.sshPublicKey();

        try {
            // Create a Linux virtual machine backed by managed disks.
            VirtualMachine linuxVM = azureResourceManager.virtualMachines()
                .define(linuxVMName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS)
                .withRootUsername(userName)
                .withSsh(sshPublicKey)
                .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
                .create();

            // A virtual machine must be deallocated and generalized before it can be captured as an image.
            linuxVM.deallocate();
            linuxVM.generalize();

            // Capture the virtual machine into a reusable custom image.
            VirtualMachineCustomImage customImage = azureResourceManager.virtualMachineCustomImages()
                .define(customImageName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .fromVirtualMachine(linuxVM)
                .create();

            System.out.println("Created custom image: " + customImage.id());

            // Create a new virtual machine from the custom image.
            VirtualMachine linuxVM2 = azureResourceManager.virtualMachines()
                .define(linuxVMName2)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withGeneralizedLinuxCustomImage(customImage.id())
                .withRootUsername(userName)
                .withSsh(sshPublicKey)
                .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
                .create();

            System.out.println("Created virtual machine from custom image: " + linuxVM2.id());

            azureResourceManager.virtualMachineCustomImages().deleteById(customImage.id());
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CreateVirtualMachineUsingCustomImageFromVM() {
    }
}
