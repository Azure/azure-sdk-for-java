// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.azure.resourcemanager.resources.models.DeploymentOperation;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;

/**
 * Azure Resource sample for deploying resources using an ARM template.
 */

public final class DeployUsingARMTemplate {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws IOException, IllegalAccessException {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgRSAT", 24);
        final String deploymentName = Utils.randomResourceName(azureResourceManager, "dpRSAT", 24);
        try {
            String templateJson = DeployUsingARMTemplate.getTemplate(azureResourceManager);


            //=============================================================
            // Create resource group.

            System.out.println("Creating a resource group with name: " + rgName);

            azureResourceManager.resourceGroups().define(rgName)
                    .withRegion(Region.US_EAST2)
                    .create();

            System.out.println("Created a resource group with name: " + rgName);


            //=============================================================
            // Create a deployment for an Azure App Service via an ARM
            // template.

            System.out.println(templateJson);
            //
            System.out.println("Starting a deployment for an Azure App Service: " + deploymentName);

            azureResourceManager.deployments().define(deploymentName)
                    .withExistingResourceGroup(rgName)
                    .withTemplate(templateJson)
                    .withParameters("{}")
                    .withMode(DeploymentMode.INCREMENTAL)
                    .create();

            System.out.println("Started a deployment for an Azure App Service: " + deploymentName);
            return true;
        } finally {
            try {
                Deployment deployment = azureResourceManager.deployments()
                        .getByResourceGroup(rgName, deploymentName);
                PagedIterable<DeploymentOperation> operations = deployment.deploymentOperations()
                        .list();

                for (DeploymentOperation operation : operations) {
                    if (operation.targetResource() != null) {
                        String operationTxt = String.format("id:%s name:%s type: %s provisioning-state:%s code: %s msg: %s",
                                operation.targetResource().id(),
                                operation.targetResource().resourceName(),
                                operation.targetResource().resourceType(),
                                operation.provisioningState(),
                                operation.statusCode(),
                                operation.statusMessage());
                        System.out.println(operationTxt);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }


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

            //=================================================================
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

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getTemplate(AzureResourceManager azureResourceManager) throws IllegalAccessException, JsonProcessingException, IOException {
        final String hostingPlanName = Utils.randomResourceName(azureResourceManager, "hpRSAT", 24);
        final String webappName = Utils.randomResourceName(azureResourceManager, "wnRSAT", 24);

        try (InputStream embeddedTemplate = DeployUsingARMTemplate.class.getResourceAsStream("/templateValue.json")) {

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode tmp = mapper.readTree(embeddedTemplate);

            DeployUsingARMTemplate.validateAndAddFieldValue("string", hostingPlanName, "hostingPlanName", null, tmp);
            DeployUsingARMTemplate.validateAndAddFieldValue("string", webappName, "webSiteName", null, tmp);
            DeployUsingARMTemplate.validateAndAddFieldValue("string", "F1", "skuName", null, tmp);
            DeployUsingARMTemplate.validateAndAddFieldValue("int", "1", "skuCapacity", null, tmp);

            return tmp.toString();
        }
    }

    private static void validateAndAddFieldValue(String type, String fieldValue, String fieldName, String errorMessage,
                                                 JsonNode tmp) throws IllegalAccessException {
        // Add count variable for loop....
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode parameter = mapper.createObjectNode();
        parameter.put("type", type);
        if ("int".equals(type)) {
            parameter.put("defaultValue", Integer.parseInt(fieldValue));
        } else {
            parameter.put("defaultValue", fieldValue);
        }
        ObjectNode.class.cast(tmp.get("parameters")).replace(fieldName, parameter);
    }
}
