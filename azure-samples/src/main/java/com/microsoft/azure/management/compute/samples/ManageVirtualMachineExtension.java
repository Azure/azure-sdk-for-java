package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machine extensions. -
 *  - Create a Linux and Windows virtual machine
 *  - Add three users (user names and passwords for windows, SSH keys for Linux)
 *  - Resets user credentials
 *  - Remove a user
 *  - Install MySQL on Linux | something significant on Windows
 *  - Remove extensions
 */
public final class ManageVirtualMachineExtension {
    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final String linuxVmName = ResourceNamer.randomResourceName("lVM", 10);
        final String windowsVmName = ResourceNamer.randomResourceName("wVM", 10);
        final String rgName = ResourceNamer.randomResourceName("rgCOMV", 15);
        final String pipDnsLabelLinuxVM = ResourceNamer.randomResourceName("rgPip1", 25);
        final String pipDnsLabelWindowsVM = ResourceNamer.randomResourceName("rgPip2", 25);

        // Linux configurations
        //
        final String firstLinuxUserName = "tirekicker";
        final String firstLinuxUserPassword = "12NewPA$$w0rd!";
        final String firstLinuxUserNewPassword = "muy!234OR";

        final String secondLinuxUserName = "seconduser";
        final String secondLinuxUserPassword = "B12a6@12xyz!";
        final String secondLinuxUserExpiration = "2020-12-31";

        final String thirdLinuxUserName = "thirduser";
        final String thirdLinuxUserPassword = "12xyz!B12a6@";
        final String thirdLinuxUserExpiration = "2020-12-31";

        final String linuxCustomScriptExtensionName = "CustomScriptForLinux";
        final String linuxCustomScriptExtensionPublisherName = "Microsoft.OSTCExtensions";
        final String linuxCustomScriptExtensionTypeName = "CustomScriptForLinux";
        final String linuxCustomScriptExtensionVersionName = "1.4";

        final String mySqlLinuxInstallScript = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String mySqlScriptInstallCommand = "bash install_mysql_server_5.6.sh Abc.123x(";
        final List<String> fileUris = new ArrayList<>();
        fileUris.add(mySqlLinuxInstallScript);

        final String linuxVmAccessExtensionName = "VMAccessForLinux";
        final String linuxVmAccessExtensionPublisherName = "Microsoft.OSTCExtensions";
        final String linuxVmAccessExtensionTypeName = "VMAccessForLinux";
        final String linuxVmAccessExtensionVersionName = "1.4";

        // Windows configurations
        //
        final String firstWindowsUserName = "tirekicker";
        final String firstWindowsUserPassword = "12NewPA$$w0rd!";
        final String firstWindowsUserNewPassword = "muy!234OR";

        final String secondWindowsUserName = "seconduser";
        final String secondWindowsUserPassword = "B12a6@12xyz!";

        final String thirdWindowsUserName = "thirduser";
        final String thirdWindowsUserPassword = "12xyz!B12a6@";

        final String windowsVmAccessExtensionName = "VMAccessAgent";
        final String windowsVmAccessExtensionPublisherName = "Microsoft.Compute";
        final String windowsVmAccessExtensionTypeName = "VMAccessAgent";
        final String windowsVmAccessExtensionVersionName = "2.3";

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());


            try {


                //=============================================================
                // Create a Linux VM with root (sudo) user

                System.out.println("Creating a Linux VM");

                VirtualMachine linuxVM = azure.virtualMachines().define(linuxVmName)
                        .withRegion(Region.US_EAST)
                        .withNewResourceGroup(rgName)
                        .withNewPrimaryNetwork("10.0.0.0/28")
                        .withPrimaryPrivateIpAddressDynamic()
                        .withNewPrimaryPublicIpAddress(pipDnsLabelLinuxVM)
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                        .withRootUserName(firstLinuxUserName)
                        .withPassword(firstLinuxUserPassword)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .create();

                System.out.println("Created a Linux VM with" + linuxVM.id());
                Utils.print(linuxVM);

                //=============================================================
                // Add a second sudo user to Linux VM using VMAccess extension

                linuxVM.update()
                        .defineNewExtension(linuxVmAccessExtensionName)
                            .withPublisher(linuxVmAccessExtensionPublisherName)
                            .withType(linuxVmAccessExtensionTypeName)
                            .withVersion(linuxVmAccessExtensionVersionName)
                            .withProtectedSetting("username", secondLinuxUserName)
                            .withProtectedSetting("password", secondLinuxUserPassword)
                            .withProtectedSetting("expiration", secondLinuxUserExpiration)
                            .attach()
                        .apply();

                System.out.println("Added a second sudo user to the Linux VM");

                //=============================================================
                // Add a third sudo user to Linux VM by updating VMAccess extension

                linuxVM.update()
                        .updateExtension(linuxVmAccessExtensionName)
                            .withProtectedSetting("username", thirdLinuxUserName)
                            .withProtectedSetting("password", thirdLinuxUserPassword)
                            .withProtectedSetting("expiration", thirdLinuxUserExpiration)
                        .parent()
                        .apply();

                System.out.println("Added a third sudo user to the Linux VM");

                //=============================================================
                // Reset ssh password of first user of Linux VM by updating VMAccess extension

                linuxVM.update()
                        .updateExtension(linuxVmAccessExtensionName)
                            .withProtectedSetting("username", firstLinuxUserName)
                            .withProtectedSetting("password", firstLinuxUserNewPassword)
                            .withProtectedSetting("reset_ssh", "true")
                        .parent()
                        .apply();

                System.out.println("Password of first user of Linux VM has been updated");

                //=============================================================
                // Removes the second sudo user from Linux VM using VMAccess extension

                linuxVM.update()
                        .updateExtension(linuxVmAccessExtensionName)
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
                            .withAutoUpgradeMinorVersionEnabled()
                            .withPublicSetting("fileUris", fileUris)
                            .withPublicSetting("commandToExecute", mySqlScriptInstallCommand)
                        .attach()
                        .apply();

                System.out.println("Installed MySql using custom script extension");
                Utils.print(linuxVM);

                //=============================================================
                // Removes the extensions from Linux VM

                linuxVM.update()
                        .withoutExtension(linuxCustomScriptExtensionName)
                        .withoutExtension(linuxVmAccessExtensionName)
                        .apply();
                System.out.println("Removed the custom script and VM Access extensions from Linux VM");
                Utils.print(linuxVM);

                //=============================================================
                // Create a Windows VM with admin user

                System.out.println("Creating a Windows VM");

                VirtualMachine windowsVM = azure.virtualMachines().define(windowsVmName)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(rgName)
                        .withNewPrimaryNetwork("10.0.0.0/28")
                        .withPrimaryPrivateIpAddressDynamic()
                        .withNewPrimaryPublicIpAddress(pipDnsLabelWindowsVM)
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUserName(firstWindowsUserName)
                        .withPassword(firstWindowsUserPassword)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .create();

                System.out.println("Created a Windows VM" + windowsVM.id());
                Utils.print(windowsVM);

                //=============================================================
                // Add a second admin user to Windows VM using VMAccess extension

                windowsVM.update()
                        .defineNewExtension(windowsVmAccessExtensionName)
                            .withPublisher(windowsVmAccessExtensionPublisherName)
                            .withType(windowsVmAccessExtensionTypeName)
                            .withVersion(windowsVmAccessExtensionVersionName)
                            .withProtectedSetting("username", secondWindowsUserName)
                            .withProtectedSetting("password", secondWindowsUserPassword)
                        .attach()
                        .apply();

                System.out.println("Added a second admin user to the Windows VM");

                //=============================================================
                // Add a third admin user to Windows VM by updating VMAccess extension

                windowsVM.update()
                        .updateExtension(windowsVmAccessExtensionName)
                            .withProtectedSetting("username", thirdWindowsUserName)
                            .withProtectedSetting("password", thirdWindowsUserPassword)
                        .parent()
                        .apply();

                System.out.println("Added a third admin user to the Windows VM");

                //=============================================================
                // Reset admin password of first user of Windows VM by updating VMAccess extension

                windowsVM.update()
                        .updateExtension(windowsVmAccessExtensionName)
                            .withProtectedSetting("username", firstWindowsUserName)
                            .withProtectedSetting("password", firstWindowsUserNewPassword)
                        .parent()
                        .apply();

                System.out.println("Password of first user of Windows VM has been updated");

                //=============================================================
                // Removes the extensions from Linux VM

                windowsVM.update()
                        .withoutExtension(windowsVmAccessExtensionName)
                        .apply();
                System.out.println("Removed the VM Access extensions from Windows VM");
                Utils.print(windowsVM);

            } catch (Exception f) {

                System.out.println(f.getMessage());
                f.printStackTrace();

            } finally {
                try {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().delete(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                } catch (NullPointerException npe) {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
                } catch (Exception g) {
                    g.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageVirtualMachineExtension() {

    }
}
