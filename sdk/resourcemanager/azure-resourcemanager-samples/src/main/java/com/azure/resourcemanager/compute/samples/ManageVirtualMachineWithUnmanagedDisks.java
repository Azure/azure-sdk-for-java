// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.util.Date;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create a virtual machine with unmanaged OS Disk
 *  - Start a virtual machine
 *  - Stop a virtual machine
 *  - Restart a virtual machine
 *  - Update a virtual machine
 *    - Expand the OS drive
 *    - Tag a virtual machine (there are many possible variations here)
 *    - Attach un-managed data disks
 *    - Detach un-managed data disks
 *  - List virtual machines
 *  - Delete a virtual machine.
 */
public final class ManageVirtualMachineWithUnmanagedDisks {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST;
        final String windowsVMName = Utils.randomResourceName(azureResourceManager, "wVM", 15);
        final String linuxVMName = Utils.randomResourceName(azureResourceManager, "lVM", 15);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String userName = "tirekicker";
        final String password = Utils.password();
        final String sshPublicKey = Utils.sshPublicKey();
        final String dataDiskName = "disk2";

        try {

            //=============================================================
            // Create a Windows virtual machine

            System.out.println("Creating a Windows VM");

            Date t1 = new Date();

            VirtualMachine windowsVM = azureResourceManager.virtualMachines().define(windowsVMName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                    .withAdminUsername(userName)
                    .withAdminPassword(password)
                    .withUnmanagedDisks()
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            Date t2 = new Date();
            System.out.println("Created VM: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + windowsVM.id());
            // Print virtual machine details
            Utils.print(windowsVM);


            //=============================================================
            // Update - Tag the virtual machine

            windowsVM.update()
                    .withTag("who-rocks", "java")
                    .withTag("where", "on azure")
                    .apply();

            System.out.println("Tagged VM: " + windowsVM.id());


            //=============================================================
            // Update - Attach data disks

            windowsVM.update()
                    .withNewUnmanagedDataDisk(10)
                    .defineUnmanagedDataDisk(dataDiskName)
                        .withNewVhd(20)
                        .withCaching(CachingTypes.READ_WRITE)
                        .attach()
                    .apply();


            System.out.println("Attached a new data disk" + dataDiskName + " to VM" + windowsVM.id());
            Utils.print(windowsVM);


            //=============================================================
            // Update - detach data disk

            windowsVM.update()
                    .withoutUnmanagedDataDisk(dataDiskName)
                    .apply();

            System.out.println("Detached data disk " + dataDiskName + " from VM " + windowsVM.id());


            //=============================================================
            // Update - Resize (expand) the data disk
            // First, deallocate the virtual machine and then proceed with resize

            System.out.println("De-allocating VM: " + windowsVM.id());

            windowsVM.deallocate();

            System.out.println("De-allocated VM: " + windowsVM.id());

            VirtualMachineUnmanagedDataDisk dataDisk = windowsVM.unmanagedDataDisks().get(0);

            windowsVM.update()
                    .updateUnmanagedDataDisk(dataDisk.name())
                        .withSizeInGB(30)
                        .parent()
                    .apply();

            System.out.println("Expanded VM " + windowsVM.id() + "'s data disk to 30GB");


            //=============================================================
            // Update - Expand the OS drive size by 10 GB

            int osDiskSizeInGb = windowsVM.osDiskSize();
            if (osDiskSizeInGb == 0) {
                // Server is not returning the OS Disk size, possible bug in server
                System.out.println("Server is not returning the OS disk size, possible bug in the server?");
                System.out.println("Assuming that the OS disk size is 256 GB");
                osDiskSizeInGb = 256;
            }

            windowsVM.update()
                    .withOSDiskSizeInGB(osDiskSizeInGb + 10)
                    .apply();

            System.out.println("Expanded VM " + windowsVM.id() + "'s OS disk to " + (osDiskSizeInGb + 10));


            //=============================================================
            // Start the virtual machine

            System.out.println("Starting VM " + windowsVM.id());

            windowsVM.start();

            System.out.println("Started VM: " + windowsVM.id() + "; state = " + windowsVM.powerState());


            //=============================================================
            // Restart the virtual machine

            System.out.println("Restarting VM: " + windowsVM.id());

            windowsVM.restart();

            System.out.println("Restarted VM: " + windowsVM.id() + "; state = " + windowsVM.powerState());


            //=============================================================
            // Stop (powerOff) the virtual machine

            System.out.println("Powering OFF VM: " + windowsVM.id());

            windowsVM.powerOff();

            System.out.println("Powered OFF VM: " + windowsVM.id() + "; state = " + windowsVM.powerState());

            // Get the network where Windows VM is hosted
            Network network = windowsVM.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();


            //=============================================================
            // Create a Linux VM in the same virtual network

            System.out.println("Creating a Linux VM in the network");

            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(linuxVMName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("subnet1") // Referencing the default subnet name when no name specified at creation
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshPublicKey)
                    .withUnmanagedDisks()
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created a Linux VM (in the same virtual network): " + linuxVM.id());
            Utils.print(linuxVM);

            //=============================================================
            // List virtual machines in the resource group

            String resourceGroupName = windowsVM.resourceGroupName();

            System.out.println("Printing list of VMs =======");

            for (VirtualMachine virtualMachine : azureResourceManager.virtualMachines().listByResourceGroup(resourceGroupName)) {
                Utils.print(virtualMachine);
            }

            //=============================================================
            // Delete the virtual machine
            System.out.println("Deleting VM: " + windowsVM.id());

            azureResourceManager.virtualMachines().deleteById(windowsVM.id());

            System.out.println("Deleted VM: " + windowsVM.id());
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

    private ManageVirtualMachineWithUnmanagedDisks() {
    }
}
