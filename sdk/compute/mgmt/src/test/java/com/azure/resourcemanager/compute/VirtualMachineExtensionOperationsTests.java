// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineExtension;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineExtensionOperationsTests extends ComputeManagementTest {
    private String rgName = "";
    private Region region = Region.US_SOUTH_CENTRAL;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("vmexttest", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canEnableDiagnosticsExtension() throws Exception {
        final String storageAccountName = generateRandomResourceName("stg", 15);
        final String vmName = "javavm1";

        // Creates a storage account
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(storageAccountName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .create();

        // Create a Linux VM
        //
        VirtualMachine vm =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("BaR@12abc!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                .withExistingStorageAccount(storageAccount)
                .create();

        final InputStream embeddedJsonConfig =
            VirtualMachineExtensionOperationsTests.class.getResourceAsStream("/linux_diagnostics_public_config.json");
        String jsonConfig = ((new ObjectMapper()).readTree(embeddedJsonConfig)).toString();
        jsonConfig = jsonConfig.replace("%VirtualMachineResourceId%", vm.id());

        // Update Linux VM to enable Diagnostics
        vm
            .update()
            .defineNewExtension("LinuxDiagnostic")
            .withPublisher("Microsoft.OSTCExtensions")
            .withType("LinuxDiagnostic")
            .withVersion("2.3")
            .withPublicSetting("ladCfg", new String(Base64.getEncoder().encode(jsonConfig.getBytes())))
            .withPublicSetting("storageAccount", storageAccount.name())
            .withProtectedSetting("storageAccountName", storageAccount.name())
            .withProtectedSetting("storageAccountKey", storageAccount.getKeys().get(0).value())
            .withProtectedSetting("storageAccountEndPoint", "https://core.windows.net:443/")
            .attach()
            .apply();

        Map<String, VirtualMachineExtension> extensions = vm.listExtensions();
        Assertions.assertNotNull(extensions);
        Assertions.assertFalse(extensions.isEmpty());
        VirtualMachineExtension diagExtension = extensions.get("LinuxDiagnostic");
        Assertions.assertNotNull(diagExtension);
        Assertions.assertNotNull(diagExtension.publicSettings());
        Assertions.assertFalse(diagExtension.publicSettings().isEmpty());

        vm.refresh();
        extensions = vm.listExtensions();
        Assertions.assertNotNull(extensions);
        Assertions.assertFalse(extensions.isEmpty());
        diagExtension = extensions.get("LinuxDiagnostic");
        Assertions.assertNotNull(diagExtension);
        Assertions.assertNotNull(diagExtension.publicSettings());
        Assertions.assertFalse(diagExtension.publicSettings().isEmpty());
    }

    @Test
    public void canResetPasswordUsingVMAccessExtension() throws Exception {
        final String vmName = "javavm2";

        // Create a Linux VM
        //
        VirtualMachine vm =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("BaR@12abc!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                .create();

        // Using VMAccess Linux extension to reset the password for the existing user 'Foo12'
        // https://github.com/Azure/azure-linux-extensions/blob/master/VMAccess/README.md
        //
        vm
            .update()
            .defineNewExtension("VMAccessForLinux")
            .withPublisher("Microsoft.OSTCExtensions")
            .withType("VMAccessForLinux")
            .withVersion("1.4")
            .withProtectedSetting("username", "Foo12")
            .withProtectedSetting("password", "B12a6@12xyz!")
            .withProtectedSetting("reset_ssh", "true")
            .attach()
            .apply();

        Assertions.assertTrue(vm.listExtensions().size() > 0);
        Assertions.assertTrue(vm.listExtensions().containsKey("VMAccessForLinux"));

        // Update the VMAccess Linux extension to reset password again for the user 'Foo12'
        //
        vm
            .update()
            .updateExtension("VMAccessForLinux")
            .withProtectedSetting("username", "Foo12")
            .withProtectedSetting("password", "muy!234OR")
            .withProtectedSetting("reset_ssh", "true")
            .parent()
            .apply();
    }

    @Test
    public void canInstallUninstallCustomExtension() throws Exception {
        final String vmName = "javavm3";

        final String mySqlInstallScript =
            "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String installCommand = "bash install_mysql_server_5.6.sh Abc.123x(";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(mySqlInstallScript);

        // Create Linux VM with a custom extension to install MySQL
        //
        VirtualMachine vm =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                // mysql-server-5.6 not available for Ubuntu 16 and 18
                .withLatestLinuxImage("Canonical", "UbuntuServer", "14.04.4-LTS")
                .withRootUsername("Foo12")
                .withRootPassword("BaR@12abc!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                .defineNewExtension("CustomScriptForLinux")
                .withPublisher("Microsoft.OSTCExtensions")
                .withType("CustomScriptForLinux")
                .withVersion("1.4")
                .withMinorVersionAutoUpgrade()
                .withPublicSetting("fileUris", fileUris)
                .withPublicSetting("commandToExecute", installCommand)
                .attach()
                .create();

        Assertions.assertTrue(vm.listExtensions().size() > 0);
        Assertions.assertTrue(vm.listExtensions().containsKey("CustomScriptForLinux"));
        VirtualMachineExtension customScriptExtension = vm.listExtensions().get("CustomScriptForLinux");
        Assertions.assertEquals(customScriptExtension.publisherName(), "Microsoft.OSTCExtensions");
        Assertions.assertEquals(customScriptExtension.typeName(), "CustomScriptForLinux");
        Assertions.assertEquals(customScriptExtension.autoUpgradeMinorVersionEnabled(), true);

        // Remove the custom extension
        //
        vm.update().withoutExtension("CustomScriptForLinux").apply();

        Assertions.assertTrue(vm.listExtensions().size() == 0);

        vm.refresh();
        Assertions.assertTrue(vm.listExtensions().size() == 0);
    }

    @Test
    public void canHandleExtensionReference() throws Exception {
        final String vmName = "javavm4";

        // Create a Linux VM
        //
        VirtualMachine vm =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("BaR@12abc!")
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

        Assertions.assertTrue(vm.listExtensions().size() > 0);

        // Get the created virtual machine via VM List not by VM GET
        PagedIterable<VirtualMachine> virtualMachines = computeManager.virtualMachines().listByResourceGroup(rgName);
        VirtualMachine vmWithExtensionReference = null;
        for (VirtualMachine virtualMachine : virtualMachines) {
            if (virtualMachine.name().equalsIgnoreCase(vmName)) {
                vmWithExtensionReference = virtualMachine;
                break;
            }
        }
        // The VM retrieved from the list will contain extensions as reference (i.e. with only id)
        Assertions.assertNotNull(vmWithExtensionReference);

        // Update the extension
        VirtualMachine vmWithExtensionUpdated =
            vmWithExtensionReference
                .update()
                .updateExtension("VMAccessForLinux")
                .withProtectedSetting("username", "Foo12")
                .withProtectedSetting("password", "muy!234OR")
                .withProtectedSetting("reset_ssh", "true")
                .parent()
                .apply();

        // Again getting VM with extension reference
        virtualMachines = computeManager.virtualMachines().listByResourceGroup(rgName);
        vmWithExtensionReference = null;
        for (VirtualMachine virtualMachine : virtualMachines) {
            vmWithExtensionReference = virtualMachine;
        }
        Assertions.assertNotNull(vmWithExtensionReference);

        VirtualMachineExtension accessExtension = null;
        for (VirtualMachineExtension extension : vmWithExtensionReference.listExtensions().values()) {
            if (extension.name().equalsIgnoreCase("VMAccessForLinux")) {
                accessExtension = extension;
                break;
            }
        }
        // Even though VM's inner contain just extension reference VirtualMachine::getExtensions()
        // should resolve the reference and get full extension.
        Assertions.assertNotNull(accessExtension);
        Assertions.assertNotNull(accessExtension.publisherName());
        Assertions.assertNotNull(accessExtension.typeName());
        Assertions.assertNotNull(accessExtension.versionName());
    }
}
