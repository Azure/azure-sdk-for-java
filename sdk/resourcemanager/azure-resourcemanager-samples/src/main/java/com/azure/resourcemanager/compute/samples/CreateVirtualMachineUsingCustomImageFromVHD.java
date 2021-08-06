// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.RunCommandInput;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.OperatingSystemStateTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.samples.Utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create an un-managed virtual machine from PIR image with data disks
 *  - Deallocate the virtual machine
 *  - Generalize the virtual machine
 *  - Create a virtual machine custom image from OS and Data disk VHDs of virtual machine
 *  - Create a second virtual machine using the custom image
 *  - Create a second virtual machine using the custom image and configure the data disks
 *  - Deletes the custom image
 *  - Get SAS Uri to the virtual machine's managed disks.
 */
public final class CreateVirtualMachineUsingCustomImageFromVHD {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String linuxVMName1 = Utils.randomResourceName(azureResourceManager, "VM1", 10);
        final String linuxVMName2 = Utils.randomResourceName(azureResourceManager, "VM2", 10);
        final String linuxVMName3 = Utils.randomResourceName(azureResourceManager, "VM3", 10);
        final String customImageName = Utils.randomResourceName(azureResourceManager, "img", 10);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String publicIPDnsLabel = Utils.randomResourceName(azureResourceManager, "pip", 10);
        final String userName = "tirekicker";
        final String password = Utils.password();
        final Region region = Region.US_WEST;

        final String apacheInstallScript = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/resourcemanager/azure-resourcemanager-samples/src/main/resources/install_apache.sh";
        final String apacheInstallCommand = "bash install_apache.sh";
        List<String> apacheInstallScriptUris = new ArrayList<>();
        apacheInstallScriptUris.add(apacheInstallScript);

        try {
            //=============================================================
            // Create a Linux VM using a PIR image with un-managed OS and data disks and customize virtual
            // machine using custom script extension

            System.out.println("Creating a un-managed Linux VM");

            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(linuxVMName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIPDnsLabel)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withUnmanagedDisks()
                    .defineUnmanagedDataDisk("disk-1")
                        .withNewVhd(100)
                        .withLun(1)
                        .attach()
                    .defineUnmanagedDataDisk("disk-2")
                        .withNewVhd(50)
                        .withLun(2)
                        .attach()
                    .defineUnmanagedDataDisk("disk-3")
                        .withNewVhd(60)
                        .withLun(3)
                        .attach()
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", apacheInstallScriptUris)
                        .withPublicSetting("commandToExecute", apacheInstallCommand)
                        .attach()
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created a Linux VM with un-managed OS and data disks: " + linuxVM.id());
            Utils.print(linuxVM);

            // De-provision the virtual machine
            deprovisionAgentInLinuxVM(linuxVM);

            //=============================================================
            // Deallocate the virtual machine
            System.out.println("Deallocate VM: " + linuxVM.id());

            linuxVM.deallocate();

            System.out.println("De-allocated VM: " + linuxVM.id() + "; state = " + linuxVM.powerState());

            //=============================================================
            // Generalize the virtual machine
            System.out.println("Generalize VM: " + linuxVM.id());

            linuxVM.generalize();

            System.out.println("Generalized VM: " + linuxVM.id());

            //=============================================================
            // Create a virtual machine custom image from OS and data disks of the virtual machine

            System.out.println("Creating virtual machine custom image from un-managed disk VHDs: " + linuxVM.id());

            VirtualMachineCustomImage virtualMachineCustomImage = azureResourceManager.virtualMachineCustomImages()
                    .define(customImageName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromVhd(linuxVM.osUnmanagedDiskVhdUri(), OperatingSystemStateTypes.GENERALIZED)
                    .defineDataDiskImage()
                        .withLun(linuxVM.unmanagedDataDisks().get(1).lun())
                        .fromVhd(linuxVM.unmanagedDataDisks().get(1).vhdUri())
                        .attach()
                    .defineDataDiskImage()
                        .withLun(linuxVM.unmanagedDataDisks().get(2).lun())
                        .fromVhd(linuxVM.unmanagedDataDisks().get(2).vhdUri())
                        .attach()
                    .defineDataDiskImage()
                        .withLun(linuxVM.unmanagedDataDisks().get(3).lun())
                        .fromVhd(linuxVM.unmanagedDataDisks().get(3).vhdUri())
                        .withDiskCaching(CachingTypes.READ_ONLY)
                        .attach()
                    .create();

            System.out.println("Created custom image");

            Utils.print(virtualMachineCustomImage);

            //=============================================================
            // Create a Linux VM using custom image

            System.out.println("Creating a Linux VM using custom image: " + virtualMachineCustomImage.id());

            VirtualMachine linuxVM2 = azureResourceManager.virtualMachines().define(linuxVMName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withGeneralizedLinuxCustomImage(virtualMachineCustomImage.id())
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created Linux VM");
            Utils.print(linuxVM2);

            //=============================================================
            // Create another Linux VM using custom image and configure the data disks from image and
            // add another data disk

            VirtualMachine linuxVM3 = azureResourceManager.virtualMachines().define(linuxVMName3)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withGeneralizedLinuxCustomImage(virtualMachineCustomImage.id())
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withNewDataDiskFromImage(1, 200, CachingTypes.READ_WRITE)
                    .withNewDataDiskFromImage(2, 100, CachingTypes.READ_ONLY)
                    .withNewDataDiskFromImage(3, 100, CachingTypes.READ_WRITE)
                    .withNewDataDisk(50)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            Utils.print(linuxVM3);

            // Getting the SAS URIs requires virtual machines to be de-allocated
            // [Access not permitted because'disk' is currently attached to running VM]
            //
            System.out.println("De-allocating the virtual machine - " + linuxVM3.id());

            linuxVM3.deallocate();

            //=============================================================
            // Get the readonly SAS URI to the OS and data disks

            System.out.println("Getting OS and data disks SAS Uris");

            // OS Disk SAS Uri
            Disk osDisk = azureResourceManager.disks().getById(linuxVM3.osDiskId());

            String osDiskSasUri = osDisk.grantAccess(24 * 60);

            System.out.println("OS disk SAS Uri: " + osDiskSasUri);

            // Data Disks SAS Uri
            for (VirtualMachineDataDisk disk : linuxVM3.dataDisks().values()) {
                Disk dataDisk = azureResourceManager.disks().getById(disk.id());
                String dataDiskSasUri = dataDisk.grantAccess(24 * 60);
                System.out.println(String.format("Data disk (lun: %d) SAS Uri: %s", disk.lun(), dataDiskSasUri));
            }

            //=============================================================
            // Deleting the custom image
            System.out.println("Deleting custom Image: " + virtualMachineCustomImage.id());

            azureResourceManager.virtualMachineCustomImages().deleteById(virtualMachineCustomImage.id());

            System.out.println("Deleted custom image");
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
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

    /**
     * De-provision an Azure linux virtual machine.
     *
     * @param virtualMachine the virtual machine
     */
    protected static void deprovisionAgentInLinuxVM(VirtualMachine virtualMachine) {
        System.out.println("Trying to de-provision");

        virtualMachine.manager().serviceClient().getVirtualMachines().beginRunCommand(
            virtualMachine.resourceGroupName(), virtualMachine.name(),
            new RunCommandInput()
                .withCommandId("RunShellScript")
                .withScript(Collections.singletonList("sudo waagent -deprovision+user --force")));

        // wait as above command will not return as sync
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));
    }

    private CreateVirtualMachineUsingCustomImageFromVHD() {
    }
}
