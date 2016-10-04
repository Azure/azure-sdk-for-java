package com.microsoft.azure.management.compute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.storage.StorageAccount;
import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.InputStream;
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
    public void canEnableDiagnosticsExtension() throws Exception {
        final String RG_NAME = ResourceNamer.randomResourceName("vmexttest", 15);
        final String STORAGEACCOUNTNAME = ResourceNamer.randomResourceName("stg", 15);
        final String LOCATION = "eastus";
        final String VMNAME = "javavm";

        // Creates a storage account
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(STORAGEACCOUNTNAME)
                .withRegion(LOCATION)
                .withNewResourceGroup(RG_NAME)
                .create();

        // Create a Linux VM
        //
        VirtualMachine vm = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(LOCATION)
                .withExistingResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                .withRootUserName("Foo12")
                .withPassword("BaR@12abc!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                .withExistingStorageAccount(storageAccount)
                .create();

        final InputStream embeddedJsonConfig = VirtualMachineExtensionOperationsTests.class.getResourceAsStream("/linux_diagnostics_public_config.json");
        String jsonConfig = ((new ObjectMapper()).readTree(embeddedJsonConfig)).toString();
        jsonConfig = jsonConfig.replace("%VirtualMachineResourceId%", vm.id());

        // Update Linux VM to enable Diagnostics
        vm.update()
                .defineNewExtension("LinuxDiagnostic")
                .withPublisher("Microsoft.OSTCExtensions")
                .withType("LinuxDiagnostic")
                .withVersion("2.3")
                .withPublicSetting("ladCfg", new String(Base64.encodeBase64(jsonConfig.getBytes())))
                .withPublicSetting("storageAccount", storageAccount.name())
                .withProtectedSetting("storageAccountName", storageAccount.name())
                .withProtectedSetting("storageAccountKey", storageAccount.getKeys().get(0).value())
                .withProtectedSetting("storageAccountEndPoint", "https://core.windows.net:443/")
                .attach()
                .apply();
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

    @Test
    public void canHandleExtensionReference() throws Exception {
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
                .defineNewExtension("VMAccessForLinux")
                    .withPublisher("Microsoft.OSTCExtensions")
                    .withType("VMAccessForLinux")
                    .withVersion("1.4")
                    .withProtectedSetting("username", "Foo12")
                    .withProtectedSetting("password", "B12a6@12xyz!")
                    .withProtectedSetting("reset_ssh", "true")
                .attach()
                .create();

        Assert.assertTrue(vm.extensions().size() > 0);

        // Get the created virtual machine via VM List not by VM GET
        List<VirtualMachine> virtualMachines = computeManager.virtualMachines()
                .listByGroup(RG_NAME);
        VirtualMachine vmWithExtensionReference = null;
        for (VirtualMachine virtualMachine : virtualMachines) {
            if (virtualMachine.name().equalsIgnoreCase(VMNAME)) {
                vmWithExtensionReference = virtualMachine;
                break;
            }
        }
        // The VM retrieved from the list will contain extensions as reference (i.e. with only id)
        Assert.assertNotNull(vmWithExtensionReference);

        // Update the extension
        VirtualMachine vmWithExtensionUpdated = vmWithExtensionReference.update()
                .updateExtension("VMAccessForLinux")
                .withProtectedSetting("username", "Foo12")
                .withProtectedSetting("password", "muy!234OR")
                .withProtectedSetting("reset_ssh", "true")
                .parent()
                .apply();

        // Again getting VM with extension reference
        virtualMachines = computeManager.virtualMachines()
                .listByGroup(RG_NAME);
        vmWithExtensionReference = null;
        for (VirtualMachine virtualMachine : virtualMachines) {
            vmWithExtensionReference = virtualMachine;
        }
        Assert.assertNotNull(vmWithExtensionReference);

        VirtualMachineExtension accessExtension = null;
        for (VirtualMachineExtension extension : vmWithExtensionReference.extensions().values()) {
            if (extension.name().equalsIgnoreCase("VMAccessForLinux")) {
                accessExtension = extension;
                break;
            }
        }
        // Even though VM's inner contain just extension reference VirtualMachine::extensions()
        // should resolve the reference and get full extension.
        Assert.assertNotNull(accessExtension);
        Assert.assertNull(accessExtension.publisherName());
        Assert.assertNull(accessExtension.typeName());
        Assert.assertNull(accessExtension.versionName());
    }
}