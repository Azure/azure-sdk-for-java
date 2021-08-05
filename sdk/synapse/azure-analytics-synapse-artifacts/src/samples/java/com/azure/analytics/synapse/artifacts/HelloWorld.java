// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.artifacts;

import com.azure.analytics.synapse.artifacts.models.PipelineResource;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.Random;

/**
 * Sample demonstrates how to set, get and delete a pipeline.
 */
public class HelloWorld {
    /**
     * Authenticates with the Synapse workspace and shows how to set, get and delete a pipeline in the workspace.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid workspace endpoint is passed.
     */
    public static void main(String[] args) throws IllegalArgumentException, InterruptedException {
        // Instantiate a pipeline client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        PipelineClient client = new ArtifactsClientBuilder()
            .endpoint("https://{YOUR_WORKSPACE_NAME}.dev.azuresynapse.net")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildPipelineClient();

        // Create a pipeline
        String pipelineName = "MyPipeline" + new Random().nextInt(1000);
        PipelineResource createdPipeline = client.createOrUpdatePipeline(pipelineName, new PipelineResource()
            .setActivities(new ArrayList<>()));
        System.out.printf("Created pipeline with id: %s\n", createdPipeline.getId());

        // Wait a few seconds for the operation completion
        Thread.sleep(30000);

        // Retrieve a pipeline
        PipelineResource retrievedPipeline = client.getPipeline(pipelineName);
        System.out.printf("Retrieved pipeline with id: %s\n", retrievedPipeline.getId());

        // List pipelines in a workspace
        PagedIterable<PipelineResource> pipelines = client.getPipelinesByWorkspace();
        for (PipelineResource p : pipelines) {
            System.out.printf("Retrieved pipeline with id: %s\n", p.getId());
        }

        // Remove a pipeline
        client.deletePipeline(pipelineName);

        // Wait a few seconds for the operation completion
        Thread.sleep(30000);
    }
}
