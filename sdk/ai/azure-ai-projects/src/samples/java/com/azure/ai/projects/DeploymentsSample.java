// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Deployment;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class DeploymentsSample {

    private static DeploymentsClient deploymentsClient
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildDeploymentsClient();

    public static void main(String[] args) {

        //listDeployments();
        //getDeployment();
    }

    public static void listDeployments() {
        // BEGIN:com.azure.ai.projects.DeploymentsSample.listDeployments

        PagedIterable<Deployment> deployments = deploymentsClient.list();
        for (Deployment deployment : deployments) {
            System.out.printf("Deployment name: %s%n", deployment.getName());
        }

        // END:com.azure.ai.projects.DeploymentsSample.listDeployments
    }

    public static void getDeployment() {
        // BEGIN:com.azure.ai.projects.DeploymentsSample.getDeployment

        String deploymentName = Configuration.getGlobalConfiguration().get("DEPLOYMENT_NAME", "");
        Deployment deployment = deploymentsClient.get(deploymentName);

        System.out.printf("Deployment name: %s%n", deployment.getName());
        System.out.printf("Deployment type: %s%n", deployment.getType().getValue());

        // END:com.azure.ai.projects.DeploymentsSample.getDeployment
    }
}
