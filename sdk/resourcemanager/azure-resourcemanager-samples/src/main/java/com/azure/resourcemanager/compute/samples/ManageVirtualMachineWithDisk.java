// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create a virtual machine with
 *      - Implicit data disks
 *      - Creatable data disks
 *      - Existing data disks
 *  - Update a virtual machine
 *      - Attach data disks
 *      - Detach data disks
 *  - Stop a virtual machine
 *  - Update a virtual machine
 *      - Expand the OS disk
 *      - Expand data disks.
 */
public final class ManageVirtualMachineWithDisk {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String linuxVMName1 = Utils.randomResourceName(azureResourceManager, "VM1", 15);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String publicIPDnsLabel = Utils.randomResourceName(azureResourceManager, "pip", 15);
        final String userName = "tirekicker";
        final String sshPublicKey = Utils.sshPublicKey();
        final Region region = Region.US_WEST;

        try {
            // Creates an empty data disk to attach to the virtual machine
            //
            System.out.println("Creating an empty managed disk");

            Disk dataDisk1 = azureResourceManager.disks().define(Utils.randomResourceName(azureResourceManager, "dsk-", 15))
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(50)
                    .create();

            System.out.println("Created managed disk");

            // Prepare first creatable data disk
            //
            Creatable<Disk> dataDiskCreatable1 = azureResourceManager.disks().define(Utils.randomResourceName(azureResourceManager, "dsk-", 15))
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(100);

            // Prepare second creatable data disk
            //
            Creatable<Disk> dataDiskCreatable2 = azureResourceManager.disks().define(Utils.randomResourceName(azureResourceManager, "dsk-", 15))
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(50)
                    .withSku(DiskSkuTypes.STANDARD_LRS);

            //======================================================================
            // Create a Linux VM using a PIR image with managed OS and Data disks

            System.out.println("Creating a managed Linux VM");

            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(linuxVMName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIPDnsLabel)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshPublicKey)

                    // Begin: Managed data disks
                    .withNewDataDisk(100)
                    .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)
                    .withNewDataDisk(dataDiskCreatable1)
                    .withNewDataDisk(dataDiskCreatable2, 2, CachingTypes.READ_ONLY)
                    .withExistingDataDisk(dataDisk1)

                    // End: Managed data disks
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D4a_v4"))
                    .create();

            System.out.println("Created a Linux VM with managed OS and data disks: " + linuxVM.id());
            Utils.print(linuxVM);

            //======================================================================
            // Update the virtual machine by detaching two data disks with lun 3 and 4 and adding one

            System.out.println("Updating Linux VM");

            String lun3DiskId = linuxVM.dataDisks().get(3).id();

            linuxVM.update()
                    .withoutDataDisk(3)
                    .withoutDataDisk(4)
                    .withNewDataDisk(200)
                    .apply();

            System.out.println("Updated Linux VM: " + linuxVM.id());
            Utils.print(linuxVM);

            // ======================================================================
            // Delete a managed disk

            Disk disk = azureResourceManager.disks().getById(lun3DiskId);
            System.out.println("Delete managed disk: " + disk.id());

            azureResourceManager.disks().deleteByResourceGroup(disk.resourceGroupName(), disk.name());

            System.out.println("Deleted managed disk");

            //======================================================================
            // Deallocate the virtual machine

            System.out.println("De-allocate Linux VM");

            linuxVM.deallocate();

            System.out.println("De-allocated Linux VM");

            //======================================================================
            // Resize the OS and Data Disks

            Disk osDisk = azureResourceManager.disks().getById(linuxVM.osDiskId());
            List<Disk> dataDisks = new ArrayList<>();
            for (VirtualMachineDataDisk vmDataDisk : linuxVM.dataDisks().values()) {
                Disk dataDisk = azureResourceManager.disks().getById(vmDataDisk.id());
                dataDisks.add(dataDisk);
            }

            System.out.println("Update OS disk: " + osDisk.id());

            osDisk.update()
                    .withSizeInGB(2 * osDisk.sizeInGB())
                    .apply();

            System.out.println("OS disk updated");

            for (Disk dataDisk : dataDisks) {
                System.out.println("Update data disk: " + dataDisk.id());

                dataDisk.update()
                        .withSizeInGB(dataDisk.sizeInGB() + 10)
                        .apply();

                System.out.println("Data disk updated");
            }

            //======================================================================
            // Starting the virtual machine

            System.out.println("Starting Linux VM");

            linuxVM.start();

            System.out.println("Started Linux VM");
            Utils.print(linuxVM);
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

    private ManageVirtualMachineWithDisk() {
    }
}
