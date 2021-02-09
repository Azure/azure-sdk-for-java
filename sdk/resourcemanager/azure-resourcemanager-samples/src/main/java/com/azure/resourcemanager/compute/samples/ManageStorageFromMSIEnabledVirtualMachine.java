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
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;

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
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String linuxVMName = Utils.randomResourceName(azureResourceManager, "VM1", 15);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String pipName = Utils.randomResourceName(azureResourceManager, "pip1", 15);
        final String userName = "tirekicker";
        final String password = Utils.password();
        final Region region = Region.US_WEST_CENTRAL;

        final String installScript = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/resources/create_resources_with_msi.sh";
        String installCommand = "bash create_resources_with_msi.sh {stgName} {rgName} {location}";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(installScript);

        try {
            //=============================================================
            // Create a Linux VM with MSI enabled for contributor access to the current resource group

            System.out.println("Creating a Linux VM with MSI enabled");

            VirtualMachine virtualMachine = azureResourceManager.virtualMachines()
                    .define(linuxVMName)
                        .withRegion(region)
                        .withNewResourceGroup(rgName)
                        .withNewPrimaryNetwork("10.0.0.0/28")
                        .withPrimaryPrivateIPAddressDynamic()
                        .withNewPrimaryPublicIPAddress(pipName)
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername(userName)
                        .withRootPassword(password)
                        .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                        .withOSDiskCaching(CachingTypes.READ_WRITE)
                        .withSystemAssignedManagedServiceIdentity()
                        .withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                        .create();

            System.out.println("Created virtual machine with MSI enabled");
            Utils.print(virtualMachine);

            // Prepare custom script t install az cli that uses MSI to create a storage account
            //
            final String stgName = Utils.randomResourceName(azureResourceManager, "st44", 15);
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
            StorageAccount storageAccount = azureResourceManager.storageAccounts()
                    .getByResourceGroup(rgName, stgName);

            System.out.println("Storage account created by az cli using MSI credential");
            Utils.print(storageAccount);
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
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

    private ManageStorageFromMSIEnabledVirtualMachine() {
    }
}
