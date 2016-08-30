package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.ArrayList;

public class VirtualMachineExtensionOperationsTests extends ComputeManagementTestBase {
    private static final String RG_NAME = ResourceNamer.randomResourceName("jsdkunittest", 15);
    private static final String LOCATION = "eastus";
    private static final String VMNAME = "javavm";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void canInstallUpdateUninstallExtension() throws Exception {
        final String mySqlInstallScript = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String installCommand = "bash install_mysql_server_5.6.sh Abc.123x(";
        // Create VM and add extension
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
                    .withPublicSetting("fileUris", new ArrayList<String>() {{
                        add(mySqlInstallScript);
                    }})
                    .withPublicSetting("commandToExecute", installCommand)
                    .attach()
                .create();
    }
}
