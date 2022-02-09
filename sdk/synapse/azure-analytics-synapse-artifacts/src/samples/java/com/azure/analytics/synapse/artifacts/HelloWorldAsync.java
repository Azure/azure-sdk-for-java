// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.artifacts;

import com.azure.analytics.synapse.artifacts.models.PipelineResource;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.Random;

/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class HelloWorldAsync {

    /**
     * Authenticates with the Synapse workspace and shows how to set, get, update and delete a role assignment in the workspace.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid workspace endpoint is passed.
     */
    public static void main(String[] args) throws IllegalArgumentException, InterruptedException {
        // Instantiate a access control client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        PipelineAsyncClient client = new ArtifactsClientBuilder()
            .endpoint("https://{YOUR_WORKSPACE_NAME}.dev.azuresynapse.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildPipelineAsyncClient();

        // Create a pipeline
        String pipelineName = "MyPipeline" + new Random().nextInt(1000);
        client.createOrUpdatePipeline(pipelineName, new PipelineResource().setActivities(new ArrayList<>()))
            .subscribe(p -> System.out.printf("Created pipeline with id: %s\n", p.getId()));

        // Retrieve a pipeline
        client.getPipeline(pipelineName).subscribe(pipelineResponse ->
            System.out.printf("Retrieved pipeline with id: %s\n", pipelineResponse.getId()));

        // List pipelines in a workspace
        client.getPipelinesByWorkspace().subscribe(pipeline ->
                System.out.printf("Retrieved pipeline with id: %s\n", pipeline.getId()));

        // Remove a pipeline
        client.deletePipeline(pipelineName).block();
    }
}
