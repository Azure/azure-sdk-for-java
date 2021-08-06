// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

public class ContainerRegistryAsyncClientJavaDocSnippets {
    /**
     * Generates code sample for creating a {@link ContainerRegistryAsyncClient}
     *
     * @return An instance of {@link ContainerRegistryAsyncClient}
     * @throws IllegalStateException If client cannot be created
     */
    public ContainerRegistryAsyncClient createAsyncContainerRegistryClient() {
        final String endpoint = getEndpoint();
        final TokenCredential credential = getTokenCredentials();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.instantiation
        ContainerRegistryAsyncClient registryAsyncClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .audience(ContainerRegistryAudience.AZURERESOURCEMANAGERPUBLICCLOUD)
            .buildAsyncClient();
        // END: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.instantiation
        return registryAsyncClient;
    }

    public ContainerRegistryAsyncClient createAsyncContainerRegistryClientWithPipeline() {
        final String endpoint = getEndpoint();
        final TokenCredential credential = getTokenCredentials();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        ContainerRegistryAsyncClient registryAsyncClient = new ContainerRegistryClientBuilder()
            .pipeline(pipeline)
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURERESOURCEMANAGERPUBLICCLOUD)
            .credential(credential)
            .buildAsyncClient();
        // END: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.pipeline.instantiation
        return registryAsyncClient;
    }

    public void deleteRepositoryCodeSnippet() {
        ContainerRegistryAsyncClient client = getAsyncClient();
        final String repositoryName = getRepositoryName();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.deleteRepository#String
        client.deleteRepository(repositoryName).subscribe(response -> {
            System.out.printf("Successfully initiated delete of the repository.");
        }, error -> {
            System.out.println("Failed to initiate a delete of the repository.");
        });
        // END: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.deleteRepository#String
    }

    public void deleteRepositoryWithResponseCodeSnippet() {
        ContainerRegistryAsyncClient client = getAsyncClient();
        final String repositoryName = getRepositoryName();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.deleteRepositoryWithResponse#String
        client.deleteRepositoryWithResponse(repositoryName).subscribe(response -> {
            System.out.printf("Successfully initiated delete of the repository.");
        }, error -> {
            System.out.println("Failed to initiate a delete of the repository.");
        });
        // END: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.deleteRepositoryWithResponse#String
    }

    public void listRepositoryNamesCodeSnippet() {
        ContainerRegistryAsyncClient client = getAsyncClient();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.listRepositoryNames
        client.listRepositoryNames().subscribe(name -> {
            System.out.printf("Repository Name:%s,", name);
        });
        // END: com.azure.containers.containerregistry.ContainerRegistryAsyncClient.listRepositoryNames
    }

    public void getRepositoryCodeSnippet() {
        ContainerRegistryAsyncClient client = getAsyncClient();
        final String repositoryName = getRepositoryName();

        // BEGIN: com.azure.containers.containerregistry.containeregistryasyncclient.getRepository
        ContainerRepositoryAsync repositoryAsync = client.getRepository(repositoryName);
        repositoryAsync.getProperties().subscribe(properties -> {
            System.out.println(properties.getName());
        });
        // END: com.azure.containers.containerregistry.containeregistryasyncclient.getRepository
    }

    public void getArtifactCodeSnippet() {
        ContainerRegistryAsyncClient client = getAsyncClient();
        final String repositoryName = getRepositoryName();
        final String tagOrDigest = getTagOrDigest();

        // BEGIN: com.azure.containers.containerregistry.containeregistryasyncclient.getArtifact
        RegistryArtifactAsync registryArtifactAsync = client.getArtifact(repositoryName, tagOrDigest);
        registryArtifactAsync.getManifestProperties().subscribe(properties -> {
            System.out.println(properties.getDigest());
        });
        // END: com.azure.containers.containerregistry.containeregistryasyncclient.getArtifact
    }

    /**
     * Implementation not provided for this method.
     *
     * @return {@code null}
     */
    private String getTagOrDigest() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    private ContainerRegistryAsyncClient getAsyncClient() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    private String getEndpoint() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    private String getRepositoryName() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    private TokenCredential getTokenCredentials() {
        return null;
    }
}
