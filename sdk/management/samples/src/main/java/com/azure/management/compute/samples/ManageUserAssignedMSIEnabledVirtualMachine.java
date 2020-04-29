/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.RunCommandInput;
import com.microsoft.azure.management.compute.RunCommandResult;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.msi.Identity;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create a Resource Group and User Assigned MSI with CONTRIBUTOR access to the resource group
 *  - Create a Linux VM and associate it with User Assigned MSI
 *      - Install Java8, Maven3 and GIT on the VM using Azure Custom Script Extension
 *  - Run Java application in the MSI enabled Linux VM which uses MSI credentials to manage Azure resource
 *  - Retrieve the Virtual machine created from the MSI enabled Linux VM.
 */

public final class ManageUserAssignedMSIEnabledVirtualMachine {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName1 = Utils.createRandomName("uamsi-rg-1");
        final String rgName2 = Utils.createRandomName("uamsi-rg-2");
        final String identityName = Utils.createRandomName("id");
        final String linuxVMName = Utils.createRandomName("VM1");
        final String pipName = Utils.createRandomName("pip1");
        final String userName = "tirekicker";
        // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
        final String password = "12NewPAwX0rd!";
        final Region region = Region.US_WEST_CENTRAL;


        try {

            //============================================================================================
            // Create a Resource Group and User Assigned MSI with CONTRIBUTOR access to the resource group

            System.out.println("Creating a Resource Group and User Assigned MSI with CONTRIBUTOR access to the resource group");

            ResourceGroup resourceGroup1 = azure.resourceGroups()
                    .define(rgName1)
                    .withRegion(region)
                    .create();

            Identity identity = azure.identities()
                    .define(identityName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName2)
                    .withAccessTo(resourceGroup1.id(), BuiltInRole.CONTRIBUTOR)
                    .create();

            System.out.println("Created Resource Group and User Assigned MSI");

            Utils.print(resourceGroup1);
            Utils.print(identity);

            //============================================================================================
            // Create a Linux VM and associate it with User Assigned MSI
            // Install Java8, Maven3 and GIT on the VM using Azure Custom Script Extension

            // The script to install Java8, Maven3 and Git on a virtual machine using Azure Custom Script Extension
            //
            final String javaMvnGitInstallScript = "https://raw.githubusercontent.com/Azure/azure-libraries-for-java/master/azure-samples/src/main/resources/install_jva_mvn_git.sh";
            final String invokeScriptCommand = "bash install_jva_mvn_git.sh";
            List<String> fileUris = new ArrayList<>();
            fileUris.add(javaMvnGitInstallScript);

            System.out.println("Creating a Linux VM with MSI associated and install Java8, Maven and Git");

            VirtualMachine virtualMachine = azure.virtualMachines()
                    .define(linuxVMName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName2)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(pipName)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
                    .withExistingUserAssignedManagedServiceIdentity(identity)
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", fileUris)
                        .withPublicSetting("commandToExecute", invokeScriptCommand)
                        .attach()
                    .create();

            System.out.println("Created Linux VM");

            Utils.print(virtualMachine);

            //=============================================================
            // Run Java application in the MSI enabled Linux VM which uses MSI credentials to manage Azure resource

            System.out.println("Running a Java application in the MSI enabled VM which creates another virtual machine");

            List<String> commands = new ArrayList<>();
            commands.add("git clone https://github.com/Azure-Samples/compute-java-manage-vm-from-vm-with-msi-credentials.git");
            commands.add("cd compute-java-manage-vm-from-vm-with-msi-credentials");
            commands.add(String.format("mvn clean compile exec:java -Dexec.args='%s %s %s'", azure.subscriptionId(), resourceGroup1.name(), identity.clientId()));

            runCommandOnVM(azure, virtualMachine, commands);

            System.out.println("Java application executed");

            //=============================================================
            // Retrieve the Virtual machine created from the MSI enabled Linux VM

            System.out.println("Retrieving the virtual machine created from the MSI enabled Linux VM");

            PagedList<VirtualMachine> virtualMachines = azure.virtualMachines().listByResourceGroup(resourceGroup1.name());
            for (VirtualMachine vm : virtualMachines) {
                Utils.print(vm);
            }

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName1);
                azure.resourceGroups().deleteByName(rgName1);
                System.out.println("Deleting Resource Group: " + rgName2);
                azure.resourceGroups().deleteByName(rgName2);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    private static RunCommandResult runCommandOnVM(Azure azure, VirtualMachine virtualMachine, List<String> commands) {
        RunCommandInput runParams = new RunCommandInput()
                .withCommandId("RunShellScript")
                .withScript(commands);

        return azure.virtualMachines().runCommand(virtualMachine.resourceGroupName(), virtualMachine.name(), runParams);
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageUserAssignedMSIEnabledVirtualMachine() {
    }
}
