/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *   - Create a virtual machine with Managed Service Identity enabled with access to resource group
 *   - Set custom script in the virtual machine that
 *          - install az cli in the virtual machine
 *          - uses az cli MSI credentials to create a storage account
 *   - Get storage account created through MSI credentials.
 */
public final class ManageStorageFromMSIEnabledVirtualMachine {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String linuxVMName = Utils.createRandomName("VM1");
        final String rgName = Utils.createRandomName("rgCOMV");
        final String pipName = Utils.createRandomName("pip1");
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";
        final Region region = Region.US_WEST_CENTRAL;

        final String installScript = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/azure-samples/src/main/resources/create_resources_with_msi.sh";
        String installCommand = "bash create_resources_with_msi.sh {stgName} {rgName} {location}";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(installScript);

        try {
            //=============================================================
            // Create a Linux VM with MSI enabled for contributor access to the current resource group

            System.out.println("Creating a Linux VM with MSI enabled");

            VirtualMachine virtualMachine = azure.virtualMachines()
                    .define(linuxVMName)
                        .withRegion(region)
                        .withNewResourceGroup(rgName)
                        .withNewPrimaryNetwork("10.0.0.0/28")
                        .withPrimaryPrivateIPAddressDynamic()
                        .withNewPrimaryPublicIPAddress(pipName)
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername(userName)
                        .withRootPassword(password)
                        .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
                        .withOSDiskCaching(CachingTypes.READ_WRITE)
                        .withManagedServiceIdentity()
                        .withRoleBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                        .create();

            System.out.println("Created virtual machine with MSI enabled");
            Utils.print(virtualMachine);

            // Prepare custom script t install az cli that uses MSI to create a storage account
            //
            final String stgName = Utils.createRandomName("st44");
            installCommand = installCommand.replace("{stgName}", stgName)
                            .replace("{rgName}", rgName)
                            .replace("{location}", region.name());

            // Update the VM by installing custom script extension.
            //
            System.out.println("Installing custom script extension to configure az cli in the virtual machine");
            System.out.println("az cli will use MSI credentials to create storage account");

            virtualMachine
                    .update()
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", fileUris)
                        .withPublicSetting("commandToExecute", installCommand)
                        .attach()
                    .apply();

            // Retrieve the storage account created by az cli using MSI credentials
            //
            StorageAccount storageAccount = azure.storageAccounts()
                    .getByResourceGroup(rgName, stgName);

            System.out.println("Storage account created by az cli using MSI credential");
            Utils.print(storageAccount);
            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
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

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageStorageFromMSIEnabledVirtualMachine() {
    }
}
