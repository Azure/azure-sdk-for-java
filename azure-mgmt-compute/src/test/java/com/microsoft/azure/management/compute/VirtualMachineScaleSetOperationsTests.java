package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.ResourceGroup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VirtualMachineScaleSetOperationsTests extends ComputeManagementTestBase {
    private static final String RG_NAME = "javacsmrg";
    private static final String LOCATION = "southcentralus";
    private static final String VMSCALESETNAME = "javavm";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCreateVirtualMachineScaleSet() throws Exception {
        final String mySqlInstallScript = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String installCommand = "bash install_mysql_server_5.6.sh Abc.123x(";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(mySqlInstallScript);

        ResourceGroup.DefinitionStages.WithCreate resourceGroupCreatable = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(LOCATION);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(VMSCALESETNAME)
                .withRegion(LOCATION)
                .withNewResourceGroup(resourceGroupCreatable)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUserName("jvuser")
                .withPassword("123OData!@#123")
                .defineNewExtension("CustomScriptForLinux")
                    .withPublisher("Microsoft.OSTCExtensions")
                    .withType("CustomScriptForLinux")
                    .withVersion("1.4")
                    .withAutoUpgradeMinorVersionEnabled()
                    .withPublicSetting("fileUris",fileUris)
                    .withPublicSetting("commandToExecute", installCommand)
                    .attach()
                .create();
    }
}
