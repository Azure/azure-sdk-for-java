// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

/**
 * Azure Compute sample for managing virtual machines -
 *   - Create a virtual machine with un-managed OS and data disks
 *   - Deallocate the virtual machine
 *   - Migrate the virtual machine to use managed disk.
 */
public final class ConvertVirtualMachineToManagedDisks {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String linuxVMName = azure.sdkContext().randomResourceName("VM1", 15);
        final String rgName = azure.sdkContext().randomResourceName("rgCOMV", 15);
        final String userName = "tirekicker";
        final String password = Utils.password();
        final Region region = Region.US_WEST;

        try {
            //=============================================================
            // Create a Linux VM using a PIR image with un-managed OS and data disks

            System.out.println("Creating an un-managed Linux VM");

            VirtualMachine linuxVM = azure.virtualMachines().define(linuxVMName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
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
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            System.out.println("Created a Linux VM with un-managed OS and data disks: " + linuxVM.id());
            Utils.print(linuxVM);

            //=============================================================
            // Deallocate the virtual machine
            System.out.println("Deallocate VM: " + linuxVM.id());

            linuxVM.deallocate();

            System.out.println("De-allocated VM: " + linuxVM.id() + "; state = " + linuxVM.powerState());

            //=============================================================
            // Migrate the virtual machine
            System.out.println("Migrate VM: " + linuxVM.id());

            linuxVM.convertToManaged();

            System.out.println("Migrated VM: " + linuxVM.id());

            Utils.print(linuxVM);

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

    private ConvertVirtualMachineToManagedDisks() {
    }
}
