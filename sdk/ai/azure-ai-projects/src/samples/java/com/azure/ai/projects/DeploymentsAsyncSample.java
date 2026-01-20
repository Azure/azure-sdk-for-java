// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Deployment;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DeploymentsAsyncSample {

    private static DeploymentsAsyncClient deploymentsAsyncClient
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildDeploymentsAsyncClient();

    public static void main(String[] args) {
        // Using block() to wait for the async operations to complete in the sample
        listDeployments().blockLast();
        getDeployment().block();
    }

    public static Flux<Deployment> listDeployments() {
        // BEGIN:com.azure.ai.projects.DeploymentsAsyncSample.listDeployments

        return deploymentsAsyncClient.list()
            .doOnNext(deployment -> System.out.printf("Deployment name: %s%n", deployment.getName()));

        // END:com.azure.ai.projects.DeploymentsAsyncSample.listDeployments
    }

    public static Mono<Deployment> getDeployment() {
        // BEGIN:com.azure.ai.projects.DeploymentsAsyncSample.getDeployment

        String deploymentName = Configuration.getGlobalConfiguration().get("DEPLOYMENT_NAME", "");
        return deploymentsAsyncClient.get(deploymentName)
            .doOnNext(deployment -> {
                System.out.printf("Deployment name: %s%n", deployment.getName());
                System.out.printf("Deployment type: %s%n", deployment.getType().getValue());
            });

        // END:com.azure.ai.projects.DeploymentsAsyncSample.getDeployment
    }
}
