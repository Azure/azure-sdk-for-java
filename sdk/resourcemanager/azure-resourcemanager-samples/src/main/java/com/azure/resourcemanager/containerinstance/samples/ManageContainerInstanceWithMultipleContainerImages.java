// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupRestartPolicy;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;

import java.time.Duration;

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
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgACI", 15);
        final String aciName = Utils.randomResourceName(azureResourceManager, "acisample", 20);
        final String containerImageName1 = "microsoft/aci-helloworld";
        final String containerImageName2 = "microsoft/aci-tutorial-sidecar";

        try {
            //=============================================================
            // Create a container group with two container instances

            ContainerGroup containerGroup = azureResourceManager.containerGroups().define(aciName)
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
            Utils.sendGetRequest("http://" + containerGroup.ipAddress());
            ResourceManagerUtils.sleep(Duration.ofSeconds(15));
            System.out.println("CURLing " + containerGroup.ipAddress());
            System.out.println(Utils.sendGetRequest("http://" + containerGroup.ipAddress()));

            //=============================================================
            // Check the container instance logs

            String logContent = containerGroup.getLogContent(aciName + "-1");
            System.out.format("Logs for container instance: %s%n%s", aciName + "-1", logContent);
            logContent = containerGroup.getLogContent(aciName + "-2");
            System.out.format("Logs for container instance: %s%n%s", aciName + "-2", logContent);

            //=============================================================
            // Remove the container group

            azureResourceManager.containerGroups().deleteById(containerGroup.id());

            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
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

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
