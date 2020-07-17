/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.ContainerGroupRestartPolicy;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure Container Instance sample for managing container instances.
 *    - Create an Azure container group with two container instances using Docker images "microsoft/aci-helloworld" and "microsoft/aci-tutorial-sidecar"
 *    - Set the container group restart policy to "never"
 *    - Test that the container app can be reached via "curl" like HTTP GET calls
 *    - Retrieve container log content
 *    - Delete the container group resource
 */
public class ManageContainerInstanceWithMultipleContainerImages {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgACI", 15);
        final String aciName = SdkContext.randomResourceName("acisample", 20);
        final String containerImageName1 = "microsoft/aci-helloworld";
        final String containerImageName2 = "microsoft/aci-tutorial-sidecar";

        try {
            //=============================================================
            // Create a container group with two container instances

            ContainerGroup containerGroup = azure.containerGroups().define(aciName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .withoutVolume()
                .defineContainerInstance(aciName + "-1")
                    .withImage(containerImageName1)
                    .withExternalTcpPort(80)
                    .withCpuCoreCount(.5)
                    .withMemorySizeInGB(0.8)
                    .attach()
                .defineContainerInstance(aciName + "-2")
                    .withImage(containerImageName2)
                    .withoutPorts()
                    .withCpuCoreCount(.5)
                    .withMemorySizeInGB(0.8)
                    .attach()
                .withRestartPolicy(ContainerGroupRestartPolicy.NEVER)
                .withDnsPrefix(aciName)
                .create();

            Utils.print(containerGroup);

            //=============================================================
            // Check that the container instance is up and running

            // warm up
            System.out.println("Warming up " + containerGroup.ipAddress());
            Utils.curl("http://" + containerGroup.ipAddress());
            SdkContext.sleep(15000);
            System.out.println("CURLing " + containerGroup.ipAddress());
            System.out.println(Utils.curl("http://" + containerGroup.ipAddress()));

            //=============================================================
            // Check the container instance logs

            String logContent = containerGroup.getLogContent(aciName + "-1");
            System.out.format("Logs for container instance: %s\n%s", aciName + "-1", logContent);
            logContent = containerGroup.getLogContent(aciName + "-2");
            System.out.format("Logs for container instance: %s\n%s", aciName + "-2", logContent);

            //=============================================================
            // Remove the container group

            azure.containerGroups().deleteById(containerGroup.id());

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
                .withLogLevel(LogLevel.BODY)
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
