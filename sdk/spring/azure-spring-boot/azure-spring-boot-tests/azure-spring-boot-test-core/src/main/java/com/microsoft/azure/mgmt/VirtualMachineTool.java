/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.mgmt;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.RunCommandInput;
import com.microsoft.azure.management.compute.RunCommandResult;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.SecurityRuleProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VirtualMachineTool {


    private static final String JAVA_MVN_GIT_INSTALL_SCRIPT = "https://raw.githubusercontent.com/Azure/" +
            "azure-libraries-for-java/master/azure-samples/src/main/resources/install_jva_mvn_git.sh";
    private static final String INVOKE_SCRIPT_COMMAND = "bash install_jva_mvn_git.sh";
    private Azure azure;

    public VirtualMachineTool(Access access) {
        azure = Azure
                .authenticate(access.credentials())
                .withSubscription(access.subscription());
    }

    public VirtualMachine createVM(String resourceGroup, String prefix, String rootUserName, String rootPassword) {
        final String vmName = SdkContext.randomResourceName(prefix, 20);

        final List<String> fileUris = new ArrayList<>();
        fileUris.add(JAVA_MVN_GIT_INSTALL_SCRIPT);

        log.info("Creating a Linux VM with MSI associated and install Java8, Maven and Git");

        final NetworkSecurityGroup nsg = azure.networkSecurityGroups()
                .define("debnsg")
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(resourceGroup)
                    .defineRule("http")
                        .allowInbound()
                        .fromAnyAddress()
                        .fromAnyPort()
                        .toAnyAddress()
                        .toPort(8080)
                        .withAnyProtocol()
                        .withPriority(234)
                        .attach()
                    .defineRule("ssh")
                        .allowInbound()
                        .fromAnyAddress()
                        .fromAnyPort()
                        .toAnyAddress()
                        .toPort(22)
                        .withProtocol(SecurityRuleProtocol.TCP)
                        .withPriority(236)
                        .attach()
                .create();

        final NetworkInterface ni = azure.networkInterfaces()
                .define("debnet")
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(resourceGroup)
                    .withNewPrimaryNetwork("10.0.0.0/24")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withAcceleratedNetworking()
                    .withExistingNetworkSecurityGroup(nsg)
                    .withNewPrimaryPublicIPAddress()
                .create();


        final VirtualMachine virtualMachine = azure.virtualMachines()
                .define(vmName)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(resourceGroup)
                    .withExistingPrimaryNetworkInterface(ni)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(rootUserName)
                    .withRootPassword(rootPassword)
                    .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
                    .withSystemAssignedManagedServiceIdentity()
                    //.withExistingUserAssignedManagedServiceIdentity(identity)
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", fileUris)
                        .withPublicSetting("commandToExecute", INVOKE_SCRIPT_COMMAND)
                        .attach()
                .create();

        log.info("Created virtual machine enabling system assigned managed identity");

        return virtualMachine;
    }

    public RunCommandResult runCommandOnVM(VirtualMachine virtualMachine, List<String> commands) {
        final RunCommandInput runParams = new RunCommandInput()
                .withCommandId("RunShellScript")
                .withScript(commands);

        return azure.virtualMachines().runCommand(virtualMachine.resourceGroupName(), virtualMachine.name(), runParams);
    }
}
