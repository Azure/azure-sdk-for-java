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
import com.microsoft.azure.management.resources.DeploymentOperation;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Resource sample for deploying resources using an ARM template.
 */

public final class DeployUsingARMTemplateWithTags {


    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgRSAT", 24);
        final String deploymentName = SdkContext.randomResourceName("dpRSAT", 24);
        try {
            String templateJson = getTemplate();

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

            Deployment deployment = azure.deployments().define(deploymentName)
                    .withExistingResourceGroup(rgName)
                    .withTemplate(templateJson)
                    .withParameters("{}")
                    .withMode(DeploymentMode.INCREMENTAL)
                    .create();

            System.out.println("Finished a deployment for an Azure App Service: " + deploymentName);

            List<DeploymentOperation> operations  = deployment.deploymentOperations().list();
            List<GenericResource> genericResources = new ArrayList<>();

            // Getting created resources
            for (DeploymentOperation operation : operations) {
                if (operation.targetResource() != null) {
                    genericResources.add(azure.genericResources().getById(operation.targetResource().id()));
                }
            }

            System.out.println("Resource created during deployment: " + deploymentName);
            for (GenericResource genericResource : genericResources) {
                System.out.println(genericResource.resourceProviderNamespace() + "/" + genericResource.resourceType() + ": " + genericResource.name());
                // Tag resource
                genericResource.update()
                        .withTag("label", "deploy1")
                        .apply();
            }

            genericResources = azure.genericResources().listByTag(rgName, "label", "deploy1");
            System.out.println("Tagged resources for deployment: " + deploymentName);
            for (GenericResource genericResource : genericResources) {
                System.out.println(genericResource.resourceProviderNamespace() + "/" + genericResource.resourceType() + ": " + genericResource.name());
            }
            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
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
            //=================================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getTemplate() throws IllegalAccessException, JsonProcessingException, IOException {
        final String hostingPlanName = SdkContext.randomResourceName("hpRSAT", 24);
        final String webappName = SdkContext.randomResourceName("wnRSAT", 24);
        final InputStream embeddedTemplate;
        embeddedTemplate = DeployUsingARMTemplate.class.getResourceAsStream("/templateValue.json");

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode tmp = mapper.readTree(embeddedTemplate);

        validateAndAddFieldValue("string", hostingPlanName, "hostingPlanName", null, tmp);
        validateAndAddFieldValue("string", webappName, "webSiteName", null, tmp);
        validateAndAddFieldValue("string", "B1", "skuName", null, tmp);
        validateAndAddFieldValue("int", "1", "skuCapacity", null, tmp);

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
}
