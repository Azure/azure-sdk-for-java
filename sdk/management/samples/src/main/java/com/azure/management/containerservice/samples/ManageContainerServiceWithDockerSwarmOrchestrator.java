/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerservice.samples;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.management.Azure;
import com.azure.management.containerservice.ContainerService;
import com.azure.management.containerservice.ContainerServiceMasterProfileCount;
import com.azure.management.containerservice.ContainerServiceVMSizeTypes;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.samples.SSHShell;
import com.azure.management.samples.Utils;

import java.io.File;
import java.util.Date;

/**
 * An Azure Container Services sample for managing a container service with Docker Swarm orchestration.
 *  - Create an Azure Container Service with Docker Swarm orchestration
 *  - Create a SSH private/public key
 *  - Update the number of agent virtual machines in an Azure Container Service
 */
public class ManageContainerServiceWithDockerSwarmOrchestrator {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = azure.sdkContext().randomResourceName("rgACS", 15);
        final String acsName = azure.sdkContext().randomResourceName("acssample", 30);
        final Region region = Region.US_EAST;
        final String rootUserName = "acsuser";

        try {

            //=============================================================
            // Create an SSH private/public key pair to be used when creating the container service

            System.out.println("Creating an SSH private and public key pair");

            SSHShell.SshPublicPrivateKey sshKeys = SSHShell.generateSSHKeys("", "ACS");
            System.out.println("SSH private key value: " + sshKeys.getSshPrivateKey());
            System.out.println("SSH public key value: " + sshKeys.getSshPublicKey());


            //=============================================================
            // Create an Azure Container Service with Docker Swarm orchestration

            System.out.println("Creating an Azure Container Service with Docker Swarm ochestration and one agent (virtual machine)");

            Date t1 = new Date();

            ContainerService azureContainerService = azure.containerServices().define(acsName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withSwarmOrchestration()
                    .withLinux()
                    .withRootUsername(rootUserName)
                    .withSshKey(sshKeys.getSshPublicKey())
                    .withMasterNodeCount(ContainerServiceMasterProfileCount.MIN)
                    .defineAgentPool("agentpool")
                        .withVirtualMachineCount(1)
                        .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D1_V2)
                        .withDnsPrefix("dns-ap-" + acsName)
                        .attach()
                    .withMasterDnsPrefix("dns-" + acsName)
                    .create();

            Date t2 = new Date();
            System.out.println("Created Azure Container Service: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + azureContainerService.id());
            Utils.print(azureContainerService);

            //=============================================================
            // Update a Kubernetes Azure Container Service with two agents (virtual machines)

            System.out.println("Updating a Docker Swarm Azure Container Service with two agents (virtual machines)");

            t1 = new Date();

            azureContainerService.update()
                    .withAgentVirtualMachineCount(2)
                    .apply();

            t2 = new Date();
            System.out.println("Updated Azure Container Service: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + azureContainerService.id());
            Utils.print(azureContainerService);

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY))
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
}
