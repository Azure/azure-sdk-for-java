package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class VirtualMachineExtensionOperationsTests extends ComputeManagementTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void canResetPasswordUsingVMAccessExtension() throws Exception {
        final String RG_NAME = ResourceNamer.randomResourceName("vmexttest", 15);
        final String LOCATION = "eastus";
        final String VMNAME = "javavm";

        // Create a Linux VM
        //
        VirtualMachine vm = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(LOCATION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                .withRootUserName("Foo12")
                .withPassword("BaR@12abc!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                .create();

        // Using VMAccess Linux extension to reset the password for the existing user 'Foo12'
        // https://github.com/Azure/azure-linux-extensions/blob/master/VMAccess/README.md
        //
        vm.update()
                .defineNewExtension("VMAccessForLinux")
                    .withPublisher("Microsoft.OSTCExtensions")
                    .withType("VMAccessForLinux")
                    .withVersion("1.4")
                    .withProtectedSetting("username", "Foo12")
                    .withProtectedSetting("password", "B12a6@12xyz!")
                    .withProtectedSetting("reset_ssh", "true")
                .attach()
                .apply();

        Assert.assertTrue(vm.extensions().size() > 0);
        Assert.assertTrue(vm.extensions().containsKey("VMAccessForLinux"));

        // Update the VMAccess Linux extension to reset password again for the user 'Foo12'
        //
        vm.update()
                .updateExtension("VMAccessForLinux")
                    .withProtectedSetting("username", "Foo12")
                    .withProtectedSetting("password", "muy!234OR")
                    .withProtectedSetting("reset_ssh", "true")
                .parent()
                .apply();
    }

    @Test
    public void canInstallUninstallCustomExtension() throws Exception {
        final String RG_NAME = ResourceNamer.randomResourceName("vmexttest", 15);
        final String LOCATION = "eastus";
        final String VMNAME = "javavm";

        final String mySqlInstallScript = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String installCommand = "bash install_mysql_server_5.6.sh Abc.123x(";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(mySqlInstallScript);

        // Create Linux VM with a custom extension to install MySQL
        //
        VirtualMachine vm = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(LOCATION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                .withRootUserName("Foo12")
                .withPassword("BaR@12abc!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                .defineNewExtension("CustomScriptForLinux")
                    .withPublisher("Microsoft.OSTCExtensions")
                    .withType("CustomScriptForLinux")
                    .withVersion("1.4")
                    .withAutoUpgradeMinorVersionEnabled()
                    .withPublicSetting("fileUris",fileUris)
                    .withPublicSetting("commandToExecute", installCommand)
                .attach()
                .create();

        Assert.assertTrue(vm.extensions().size() > 0);
        Assert.assertTrue(vm.extensions().containsKey("CustomScriptForLinux"));
        VirtualMachineExtension customScriptExtension = vm.extensions().get("CustomScriptForLinux");
        Assert.assertEquals(customScriptExtension.publisherName(), "Microsoft.OSTCExtensions");
        Assert.assertEquals(customScriptExtension.typeName(), "CustomScriptForLinux");
        Assert.assertEquals(customScriptExtension.autoUpgradeMinorVersionEnabled(), true);

        // Remove the custom extension
        //
        vm.update()
                .withoutExtension("CustomScriptForLinux")
                .apply();

        Assert.assertTrue(vm.extensions().size() == 0);
    }
}