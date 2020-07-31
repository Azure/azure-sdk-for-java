// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.RunCommandInput;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

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
        final String rgName1 = azure.sdkContext().randomResourceName("uamsi-rg-1", 15);
        final String rgName2 = azure.sdkContext().randomResourceName("uamsi-rg-2", 15);
        final String identityName = azure.sdkContext().randomResourceName("id", 15);
        final String linuxVMName = azure.sdkContext().randomResourceName("VM1", 15);
        final String pipName = azure.sdkContext().randomResourceName("pip1", 15);
        final String userName = "tirekicker";
        final String password = Utils.password();
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

            PagedIterable<VirtualMachine> virtualMachines = azure.virtualMachines().listByResourceGroup(resourceGroup1.name());
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

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
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
