// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machine extensions. -
 *  - Create a Linux and Windows virtual machine
 *  - Add three users (user names and passwords for windows, SSH keys for Linux)
 *  - Resets user credentials
 *  - Remove a user
 *  - Install MySQL on Linux | Choco and MySQL on Windows
 *  - Remove extensions
 */
public final class ManageVirtualMachineExtension {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST;
        final String linuxVMName = Utils.randomResourceName(azureResourceManager, "lVM", 10);
        final String windowsVMName = Utils.randomResourceName(azureResourceManager, "wVM", 10);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOVE", 15);
        final String pipDnsLabelLinuxVM = Utils.randomResourceName(azureResourceManager, "rgPip1", 25);
        final String pipDnsLabelWindowsVM = Utils.randomResourceName(azureResourceManager, "rgPip2", 25);

        // Linux configurations
        //
        final String firstLinuxUserName = "tirekicker";
        final String firstLinuxUserPassword = Utils.password();
        final String firstLinuxUserNewPassword = Utils.password();

        final String secondLinuxUserName = "seconduser";
        final String secondLinuxUserPassword = Utils.password();
        final String secondLinuxUserExpiration = "2020-12-31";

        final String thirdLinuxUserName = "thirduser";
        final String thirdLinuxUserPassword = Utils.password();
        final String thirdLinuxUserExpiration = "2020-12-31";

        final String linuxCustomScriptExtensionName = "CustomScriptForLinux";
        final String linuxCustomScriptExtensionPublisherName = "Microsoft.OSTCExtensions";
        final String linuxCustomScriptExtensionTypeName = "CustomScriptForLinux";
        final String linuxCustomScriptExtensionVersionName = "1.4";

        final String mySqlLinuxInstallScript = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String installMySQLLinuxCommand = "bash install_mysql_server_5.6.sh Abc.123x(";
        final List<String> linuxScriptFileUris = new ArrayList<>();
        linuxScriptFileUris.add(mySqlLinuxInstallScript);

        final String windowsCustomScriptExtensionName = "CustomScriptExtension";
        final String windowsCustomScriptExtensionPublisherName = "Microsoft.Compute";
        final String windowsCustomScriptExtensionTypeName = "CustomScriptExtension";
        final String windowsCustomScriptExtensionVersionName = "1.7";

        final String mySqlWindowsInstallScript = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/resourcemanager/azure-resourcemanager-samples/src/main/resources/installMySQL.ps1";
        final String installMySQLWindowsCommand = "powershell.exe -ExecutionPolicy Unrestricted -File installMySQL.ps1";
        final List<String> windowsScriptFileUris = new ArrayList<>();
        windowsScriptFileUris.add(mySqlWindowsInstallScript);

        final String linuxVMAccessExtensionName = "VMAccessForLinux";
        final String linuxVMAccessExtensionPublisherName = "Microsoft.OSTCExtensions";
        final String linuxVMAccessExtensionTypeName = "VMAccessForLinux";
        final String linuxVMAccessExtensionVersionName = "1.4";

        // Windows configurations
        //
        final String firstWindowsUserName = "tirekicker";
        final String firstWindowsUserPassword = Utils.password();
        final String firstWindowsUserNewPassword = Utils.password();

        final String secondWindowsUserName = "seconduser";
        final String secondWindowsUserPassword = Utils.password();

        final String thirdWindowsUserName = "thirduser";
        final String thirdWindowsUserPassword = Utils.password();

        final String windowsVMAccessExtensionName = "VMAccessAgent";
        final String windowsVMAccessExtensionPublisherName = "Microsoft.Compute";
        final String windowsVMAccessExtensionTypeName = "VMAccessAgent";
        final String windowsVMAccessExtensionVersionName = "2.3";
        try {


            //=============================================================
            // Create a Linux VM with root (sudo) user

            System.out.println("Creating a Linux VM");

            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(linuxVMName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(pipDnsLabelLinuxVM)
                    // mysql-server-5.6 not available for Ubuntu 16 and 18
                    .withLatestLinuxImage("Canonical", "UbuntuServer", "14.04.4-LTS")
                    .withRootUsername(firstLinuxUserName)
                    .withRootPassword(firstLinuxUserPassword)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created a Linux VM with" + linuxVM.id());
            Utils.print(linuxVM);

            //=============================================================
            // Add a second sudo user to Linux VM using VMAccess extension

            linuxVM.update()
                    .defineNewExtension(linuxVMAccessExtensionName)
                        .withPublisher(linuxVMAccessExtensionPublisherName)
                        .withType(linuxVMAccessExtensionTypeName)
                        .withVersion(linuxVMAccessExtensionVersionName)
                        .withProtectedSetting("username", secondLinuxUserName)
                        .withProtectedSetting("password", secondLinuxUserPassword)
                        .withProtectedSetting("expiration", secondLinuxUserExpiration)
                        .attach()
                    .apply();

            System.out.println("Added a second sudo user to the Linux VM");

            //=============================================================
            // Add a third sudo user to Linux VM by updating VMAccess extension

            linuxVM.update()
                    .updateExtension(linuxVMAccessExtensionName)
                        .withProtectedSetting("username", thirdLinuxUserName)
                        .withProtectedSetting("password", thirdLinuxUserPassword)
                        .withProtectedSetting("expiration", thirdLinuxUserExpiration)
                        .parent()
                    .apply();

            System.out.println("Added a third sudo user to the Linux VM");

            //=============================================================
            // Reset ssh password of first user of Linux VM by updating VMAccess extension

            linuxVM.update()
                    .updateExtension(linuxVMAccessExtensionName)
                        .withProtectedSetting("username", firstLinuxUserName)
                        .withProtectedSetting("password", firstLinuxUserNewPassword)
                        .withProtectedSetting("reset_ssh", "true")
                        .parent()
                    .apply();

            System.out.println("Password of first user of Linux VM has been updated");

            //=============================================================
            // Removes the second sudo user from Linux VM using VMAccess extension

            linuxVM.update()
                    .updateExtension(linuxVMAccessExtensionName)
                        .withProtectedSetting("remove_user", secondLinuxUserName)
                        .parent()
                    .apply();

            //=============================================================
            // Install MySQL in Linux VM using CustomScript extension

            linuxVM.update()
                    .defineNewExtension(linuxCustomScriptExtensionName)
                        .withPublisher(linuxCustomScriptExtensionPublisherName)
                        .withType(linuxCustomScriptExtensionTypeName)
                        .withVersion(linuxCustomScriptExtensionVersionName)
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", linuxScriptFileUris)
                        .withPublicSetting("commandToExecute", installMySQLLinuxCommand)
                        .attach()
                    .apply();

            System.out.println("Installed MySql using custom script extension");
            Utils.print(linuxVM);

            //=============================================================
            // Removes the extensions from Linux VM

            linuxVM.update()
                    .withoutExtension(linuxCustomScriptExtensionName)
                    .withoutExtension(linuxVMAccessExtensionName)
                    .apply();
            System.out.println("Removed the custom script and VM Access extensions from Linux VM");
            Utils.print(linuxVM);

            //=============================================================
            // Create a Windows VM with admin user and install choco package manager and MySQL using custom script

            System.out.println("Creating a Windows VM");

            VirtualMachine windowsVM = azureResourceManager.virtualMachines().define(windowsVMName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(pipDnsLabelWindowsVM)
                    .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                    .withAdminUsername(firstWindowsUserName)
                    .withAdminPassword(firstWindowsUserPassword)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .defineNewExtension(windowsCustomScriptExtensionName)
                        .withPublisher(windowsCustomScriptExtensionPublisherName)
                        .withType(windowsCustomScriptExtensionTypeName)
                        .withVersion(windowsCustomScriptExtensionVersionName)
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", windowsScriptFileUris)
                        .withPublicSetting("commandToExecute", installMySQLWindowsCommand)
                        .attach()
                    .create();

            System.out.println("Created a Windows VM" + windowsVM.id());
            Utils.print(windowsVM);

            //=============================================================
            // Add a second admin user to Windows VM using VMAccess extension

            windowsVM.update()
                    .defineNewExtension(windowsVMAccessExtensionName)
                        .withPublisher(windowsVMAccessExtensionPublisherName)
                        .withType(windowsVMAccessExtensionTypeName)
                        .withVersion(windowsVMAccessExtensionVersionName)
                        .withProtectedSetting("username", secondWindowsUserName)
                        .withProtectedSetting("password", secondWindowsUserPassword)
                        .attach()
                    .apply();

            System.out.println("Added a second admin user to the Windows VM");

            //=============================================================
            // Add a third admin user to Windows VM by updating VMAccess extension

            windowsVM.update()
                    .updateExtension(windowsVMAccessExtensionName)
                        .withProtectedSetting("username", thirdWindowsUserName)
                        .withProtectedSetting("password", thirdWindowsUserPassword)
                        .parent()
                    .apply();

            System.out.println("Added a third admin user to the Windows VM");

            //=============================================================
            // Reset admin password of first user of Windows VM by updating VMAccess extension

            windowsVM.update()
                    .updateExtension(windowsVMAccessExtensionName)
                        .withProtectedSetting("username", firstWindowsUserName)
                        .withProtectedSetting("password", firstWindowsUserNewPassword)
                        .parent()
                    .apply();

            System.out.println("Password of first user of Windows VM has been updated");

            //=============================================================
            // Removes the extensions from Windows VM

            windowsVM.update()
                    .withoutExtension(windowsVMAccessExtensionName)
                    .apply();
            System.out.println("Removed the VM Access extensions from Windows VM");
            Utils.print(windowsVM);
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

    private ManageVirtualMachineExtension() {

    }
}
