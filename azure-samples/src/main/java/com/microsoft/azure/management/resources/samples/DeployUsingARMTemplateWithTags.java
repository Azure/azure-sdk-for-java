/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.DeploymentOperation;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Resource sample for deploying resources using an ARM template.
 */

public final class DeployUsingARMTemplateWithTags {

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final String rgName = ResourceNamer.randomResourceName("rgRSAT", 24);
            final String deploymentName = ResourceNamer.randomResourceName("dpRSAT", 24);

            try {


                //=================================================================
                // Authenticate

                final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

                Azure azure = Azure.configure()
                        .withLogLevel(HttpLoggingInterceptor.Level.HEADERS)
                        .authenticate(credFile)
                        .withDefaultSubscription();

                try {
                    String templateLink = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vm-multiple-data-disk/azuredeploy.json";
                    String parameterJson = "{\"adminUsername\":{\"value\":\"azureUser\"},\"adminPassword\":{\"value\":\"StrongPass!123\"},\"dnsLabelPrefix\":{\"value\":\"uniqueazure58889\"},\"vmSize\":{\"value\":\"Standard_D2\"},\"sizeOfEachDataDiskInGB\":{\"value\":\"100\"}}";


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

                    System.out.println("Starting a deployment for an Azure VM with multiple data disks: " + deploymentName);

                    Deployment deployment = azure.deployments().define(deploymentName)
                        .withExistingResourceGroup(rgName)
                        .withTemplateLink(templateLink, "1.0.0.0")
                        .withParameters(parameterJson)
                        .withMode(DeploymentMode.INCREMENTAL)
                        .create();

                    System.out.println("Started a deployment for an Azure VM with multiple data disks: " + deploymentName);

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
}
