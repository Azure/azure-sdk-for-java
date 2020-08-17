// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.jcraft.jsch.JSchException;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.samples.SSHShell;
import com.azure.resourcemanager.samples.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create an un-managed virtual machine from PIR image with data disks
 *  - Deallocate the virtual machine
 *  - Generalize the virtual machine
 *  - Create a virtual machine custom image from the virtual machine
 *  - Create a second managed virtual machine using the custom image
 *  - Create a third virtual machine using the custom image and configure the data disks
 *  - Deletes the custom image
 *  - Get SAS Uri to the virtual machine's managed disks.
 */
public final class CreateVirtualMachineUsingCustomImageFromVM {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String linuxVMName1 = azure.sdkContext().randomResourceName("VM1", 15);
        final String linuxVMName2 = azure.sdkContext().randomResourceName("VM2", 15);
        final String linuxVMName3 = azure.sdkContext().randomResourceName("VM3", 15);
        final String customImageName = azure.sdkContext().randomResourceName("img", 15);
        final String rgName = azure.sdkContext().randomResourceName("rgCOMV", 15);
        final String publicIpDnsLabel = azure.sdkContext().randomResourceName("pip", 15);
        final String userName = "tirekicker";
        final String password = Utils.password();
        final Region region = Region.US_WEST_CENTRAL;

        final String apacheInstallScript = "https://raw.githubusercontent.com/Azure/azure-libraries-for-java/master/azure-samples/src/main/resources/install_apache.sh";
        final String apacheInstallCommand = "bash install_apache.sh";
        List<String> apacheInstallScriptUris = new ArrayList<>();
        apacheInstallScriptUris.add(apacheInstallScript);

        try {
            //=============================================================
            // Create a Linux VM using a PIR image with un-managed OS and data disks and customize virtual
            // machine using custom script extension

            System.out.println("Creating a un-managed Linux VM");

            VirtualMachine linuxVM = azure.virtualMachines().define(linuxVMName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
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
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", apacheInstallScriptUris)
                        .withPublicSetting("commandToExecute", apacheInstallCommand)
                        .attach()
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            System.out.println("Created a Linux VM with un-managed OS and data disks: " + linuxVM.id());
            Utils.print(linuxVM);

            // De-provision the virtual machine
            deprovisionAgentInLinuxVM(linuxVM.getPrimaryPublicIPAddress().fqdn(), 22, userName, password);

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
            // Capture the virtual machine to get a 'Generalized image' with Apache

            System.out.println("Capturing VM as custom image: " + linuxVM.id());

            VirtualMachineCustomImage virtualMachineCustomImage = azure.virtualMachineCustomImages()
                    .define(customImageName)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .fromVirtualMachine(linuxVM)
                        .create();

            System.out.println("Captured VM: " + linuxVM.id());

            Utils.print(virtualMachineCustomImage);

            //=============================================================
            // Create a Linux VM using custom image

            System.out.println("Creating a Linux VM using custom image - " + virtualMachineCustomImage.id());

            VirtualMachine linuxVM2 = azure.virtualMachines().define(linuxVMName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withGeneralizedLinuxCustomImage(virtualMachineCustomImage.id())
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            Utils.print(linuxVM2);

            //=============================================================
            // Create another Linux VM using custom image and configure the data disks from image and
            // add another data disk

            System.out.println("Creating another Linux VM with additional data disks using custom image - " + virtualMachineCustomImage.id());

            VirtualMachine linuxVM3 = azure.virtualMachines().define(linuxVMName3)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withGeneralizedLinuxCustomImage(virtualMachineCustomImage.id())
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withNewDataDiskFromImage(1, 200, CachingTypes.READ_WRITE)  // TODO: Naming needs to be finalized
                    .withNewDataDiskFromImage(2, 100, CachingTypes.READ_ONLY)
                    .withNewDataDisk(50)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
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
            Disk osDisk = azure.disks().getById(linuxVM3.osDiskId());

            String osDiskSasUri = osDisk.grantAccess(24 * 60);
            System.out.println("OS disk SAS Uri: " + osDiskSasUri);

            // Data disks SAS Uri
            for (VirtualMachineDataDisk disk : linuxVM3.dataDisks().values()) {
                Disk dataDisk = azure.disks().getById(disk.id());
                String dataDiskSasUri = dataDisk.grantAccess(24 * 60);
                System.out.println(String.format("Data disk (lun: %d) SAS Uri: %s", disk.lun(), dataDiskSasUri));
            }

            //=============================================================
            // Deleting the custom image
            System.out.println("Deleting custom Image: " + virtualMachineCustomImage.id());

            azure.virtualMachineCustomImages().deleteById(virtualMachineCustomImage.id());

            System.out.println("Deleted custom image");
            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }

        }
        return false;
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
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * De-provision an Azure linux virtual machine.
     *
     * @param host the public host name
     * @param port the ssh port
     * @param userName the ssh user name
     * @param password the ssh user password
     */
    protected static void deprovisionAgentInLinuxVM(String host, int port, String userName, String password) {
        SSHShell shell = null;
        try {
            System.out.println("Trying to de-provision: " + host);
            shell = SSHShell.open(host, port, userName, password);
            List<String> deprovisionCommand = new ArrayList<>();
            deprovisionCommand.add("sudo waagent -deprovision+user --force");
            String output = shell.runCommands(deprovisionCommand);
            System.out.println(output);
        } catch (JSchException jSchException) {
            System.out.println(jSchException.getMessage());
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        } finally {
            if (shell != null) {
                shell.close();
            }
        }
    }

    private CreateVirtualMachineUsingCustomImageFromVM() {
    }
}
