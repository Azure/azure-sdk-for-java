/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.samples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Azure Resource sample for deploying resources using an ARM template and
 * showing progress.
 */

public final class DeployUsingARMTemplateWithProgress {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final String rgName = ResourceNamer.randomResourceName("rgRSAP", 24);
            final String deploymentName = ResourceNamer.randomResourceName("dpRSAP", 24);

            try {


                //=================================================================
                // Authenticate

                final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

                Azure azure = Azure.configure()
                        .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                        .authenticate(credFile)
                        .withDefaultSubscription();

                try {
                    String templateJson = DeployUsingARMTemplateWithProgress.getTemplate();

                    //=============================================================
                    // Create resource group.

                    System.out.println("Creating a resource group with name: " + rgName);

                    azure.resourceGroups().define(rgName)
                            .withRegion(Region.US_WEST)
                            .create();

                    System.out.println("Created a resource group with name: " + rgName);


                    //=============================================================
                    // Create a deployment for an Azure App Service via an ARM
                    // template.

                    System.out.println("Starting a deployment for an Azure App Service: " + deploymentName);

                    azure.deployments().define(deploymentName)
                            .withExistingResourceGroup(rgName)
                            .withTemplate(templateJson)
                            .withParameters("{}")
                            .withMode(DeploymentMode.INCREMENTAL)
                            .beginCreate();

                    System.out.println("Started a deployment for an Azure App Service: " + deploymentName);

                    Deployment deployment = azure.deployments().getByGroup(rgName, deploymentName);
                    System.out.println("Current deployment status : " + deployment.provisioningState());

                    while (!(deployment.provisioningState().equalsIgnoreCase("Succeeded")
                            || deployment.provisioningState().equalsIgnoreCase("Failed")
                            || deployment.provisioningState().equalsIgnoreCase("Cancelled"))) {
                        Thread.sleep(10000);
                        deployment = azure.deployments().getByGroup(rgName, deploymentName);
                        System.out.println("Current deployment status : " + deployment.provisioningState());
                    }
                } catch (Exception f) {

                    System.out.println(f.getMessage());
                    f.printStackTrace();

                } finally {

                    try {
                        System.out.println("Deleting Resource Group: " + rgName);
                        azure.resourceGroups().delete(rgName);
                        System.out.println("Deleted Resource Group: " + rgName);
                    } catch (NullPointerException npe) {
                        System.out.println("Did not create any resources in Azure. No clean up is necessary");
                    } catch (Exception g) {
                        g.printStackTrace();
                    }

                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static String getTemplate() throws IllegalAccessException, JsonProcessingException, IOException {
        final String hostingPlanName = ResourceNamer.randomResourceName("hpRSAT", 24);
        final String webappName = ResourceNamer.randomResourceName("wnRSAT", 24);
        final InputStream embeddedTemplate;
        embeddedTemplate = DeployUsingARMTemplate.class.getResourceAsStream("/templateValue.json");

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode tmp = mapper.readTree(embeddedTemplate);

        DeployUsingARMTemplateWithProgress.validateAndAddFieldValue("string", hostingPlanName, "hostingPlanName", null, tmp);
        DeployUsingARMTemplateWithProgress.validateAndAddFieldValue("string", webappName, "webSiteName", null, tmp);
        DeployUsingARMTemplateWithProgress.validateAndAddFieldValue("string", "F1", "skuName", null, tmp);
        DeployUsingARMTemplateWithProgress.validateAndAddFieldValue("int", "1", "skuCapacity", null, tmp);

        return tmp.toString();
    }

    private static void validateAndAddFieldValue(String type, String fieldValue, String fieldName, String errorMessage,
                                                 JsonNode tmp) throws IllegalAccessException {
        // Add count variable for loop....
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode parameter = mapper.createObjectNode();
        parameter.put("type", type);
        if (type == "int") {
            parameter.put("defaultValue", Integer.parseInt(fieldValue));
        } else {
            parameter.put("defaultValue", fieldValue);
        }
        ObjectNode.class.cast(tmp.get("parameters")).replace(fieldName, parameter);
    }

    private DeployUsingARMTemplateWithProgress() {

    }
}
