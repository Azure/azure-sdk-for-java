package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
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
 *  - Create a virtual machine with custom script extension to install MySQL
 *  - Reset ssh password of an existing user using VMAccess extension
 *  - Creates a new sudo user account with ssh password using VMAccess extension
 *  - Removes a sudo user account by using VMAccess extension
 *  - Removes the custom script extension
 */
public final class ManageVirtualMachineExtension {
    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final String linuxVmName = ResourceNamer.randomResourceName("lVM", 10);
        final String rgName = ResourceNamer.randomResourceName("rgCOMV", 15);
        final String pipDnsLabel = ResourceNamer.randomResourceName("rgPip", 25);

        final String firstUserName = "tirekicker";
        final String firstUserPassword = "12NewPA$$w0rd!";
        final String firstUserNewPassword = "muy!234OR";

        final String secondUserName = "onemoreuser";
        final String secondUserPassword = "B12a6@12xyz!";
        final String secondUserExpiration = "2020-12-31";

        final String customScriptExtensionName = "CustomScriptForLinux";
        final String customScriptExtensionPublisherName = "Microsoft.OSTCExtensions";
        final String customScriptExtensionTypeName = "CustomScriptForLinux";
        final String customScriptExtensionVersionName = "1.4";

        final String mySqlInstallScript = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String scriptInstallCommand = "bash install_mysql_server_5.6.sh Abc.123x(";
        final List<String> fileUris = new ArrayList<>();
        fileUris.add(mySqlInstallScript);

        final String vmAccessExtensionName = "VMAccessForLinux";
        final String vmAccessExtensionPublisherName = "Microsoft.OSTCExtensions";
        final String vmAccessExtensionTypeName = "VMAccessForLinux";
        final String vmAccessExtensionVersionName = "1.4";

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
                // Create a Linux VM with root (sudo) user name and password

                System.out.println("Creating a Linux VM");

                VirtualMachine vm = azure.virtualMachines().define(linuxVmName)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(rgName)
                        .withNewPrimaryNetwork("10.0.0.0/28")
                        .withPrimaryPrivateIpAddressDynamic()
                        .withNewPrimaryPublicIpAddress(pipDnsLabel)
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                        .withRootUserName(firstUserName)
                        .withPassword(firstUserPassword)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .defineNewExtension(customScriptExtensionName)
                            .withPublisher(customScriptExtensionPublisherName)
                            .withType(customScriptExtensionTypeName)
                            .withVersion(customScriptExtensionVersionName)
                            .withAutoUpgradeMinorVersionEnabled()
                            .withPublicSetting("fileUris", fileUris)
                            .withPublicSetting("commandToExecute", scriptInstallCommand)
                        .attach()
                        .create();

                System.out.println("Created a Linux VM with MySQL" + vm.id());
                Utils.print(vm);

                //=============================================================
                // Reset ssh password of an existing user using VMAccess extension

                vm.update()
                        .defineNewExtension(vmAccessExtensionName)
                            .withPublisher(vmAccessExtensionPublisherName)
                            .withType(vmAccessExtensionTypeName)
                            .withVersion(vmAccessExtensionVersionName)
                            .withProtectedSetting("username", firstUserName)
                            .withProtectedSetting("password", firstUserNewPassword)
                            .withProtectedSetting("reset_ssh", "true")
                        .attach()
                        .apply();

                System.out.println("Added VMAccess extension and reset password of first user");
                Utils.print(vm);


                //=============================================================
                // Creates a new sudo user account with ssh password using VMAccess extension

                vm.update()
                        .defineNewExtension(vmAccessExtensionName)
                        .withPublisher(vmAccessExtensionPublisherName)
                        .withType(vmAccessExtensionTypeName)
                        .withVersion(vmAccessExtensionVersionName)
                        .withProtectedSetting("username", secondUserName)
                        .withProtectedSetting("password", secondUserPassword)
                        .withProtectedSetting("expiration", secondUserExpiration)
                        .attach()
                        .apply();

                System.out.println("Added a new user to the virtual machine");
                Utils.print(vm);

                //=============================================================
                // Removes the second sudo user account using VMAccess extension

                vm.update()
                        .defineNewExtension(vmAccessExtensionName)
                        .withPublisher(vmAccessExtensionPublisherName)
                        .withType(vmAccessExtensionTypeName)
                        .withVersion(vmAccessExtensionVersionName)
                        .withProtectedSetting("remove_user", secondUserName)
                        .attach()
                        .apply();


                //=============================================================
                // Removes the custom script extension

                vm.update()
                        .withoutExtension(customScriptExtensionName)
                        .apply();
                System.out.println("Removed the custom script extension");
                Utils.print(vm);
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
