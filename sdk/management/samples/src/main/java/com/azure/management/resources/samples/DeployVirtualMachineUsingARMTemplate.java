/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.resources.samples;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.management.Azure;
import com.azure.management.resources.Deployment;
import com.azure.management.resources.DeploymentMode;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Azure Resource sample for deploying virtual machine with managed disk using an ARM template.
 */
public class DeployVirtualMachineUsingARMTemplate {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = azure.sdkContext().randomResourceName("rgRSAT", 24);
        final String deploymentName = azure.sdkContext().randomResourceName("dpRSAT", 24);
        try {
            String templateJson = DeployVirtualMachineUsingARMTemplate.getTemplate(azure);

            System.out.println(templateJson);

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

            System.out.println("Starting a deployment for an Azure Virtual Machine with managed disks: " + deploymentName);

            azure.deployments().define(deploymentName)
                    .withExistingResourceGroup(rgName)
                    .withTemplate(templateJson)
                    .withParameters("{}")
                    .withMode(DeploymentMode.INCREMENTAL)
                    .create();

            System.out.println("Started a deployment for an Azure Virtual Machine with managed disks: " + deploymentName);
            Deployment deployment = azure.deployments().getByResourceGroup(rgName, deploymentName);
            System.out.println("Current deployment status : " + deployment.provisioningState());

            while (!(deployment.provisioningState().equalsIgnoreCase("Succeeded")
                    || deployment.provisioningState().equalsIgnoreCase("Failed")
                    || deployment.provisioningState().equalsIgnoreCase("Cancelled"))) {
                SdkContext.sleep(10000);
                deployment = azure.deployments().getByResourceGroup(rgName, deploymentName);
                System.out.println("Current deployment status : " + deployment.provisioningState());
            }
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

            //=================================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                    .authenticate(credFile)
                    .withDefaultSubscription();

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getTemplate(Azure azure) throws IllegalAccessException, JsonProcessingException, IOException {
        final String adminUsername = "tirekicker";
        // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
        final String adminPassword = "12NewPA$$w0rd!";
        final String osDiskName = azure.sdkContext().randomResourceName("osdisk-", 24);

        final InputStream embeddedTemplate;
        embeddedTemplate = DeployVirtualMachineUsingARMTemplate.class.getResourceAsStream("/virtualMachineWithManagedDisksTemplate.json");

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode tmp = mapper.readTree(embeddedTemplate);

        DeployVirtualMachineUsingARMTemplate.validateAndAddFieldValue("string", adminUsername, "adminUsername", null, tmp);
        DeployVirtualMachineUsingARMTemplate.validateAndAddFieldValue("string", adminPassword, "adminPassword", null, tmp);
        DeployVirtualMachineUsingARMTemplate.validateAndAddFieldValue("string", osDiskName, "osDiskName", null, tmp);
        return tmp.toString();
    }

    private static void validateAndAddFieldValue(String type, String fieldValue, String fieldName, String errorMessage,
                                                 JsonNode tmp) throws IllegalAccessException {
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
