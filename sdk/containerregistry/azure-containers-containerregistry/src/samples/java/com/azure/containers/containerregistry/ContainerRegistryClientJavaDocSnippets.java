// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.containers.containerregistry.models.ContainerRepositoryProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.Context;

public class ContainerRegistryClientJavaDocSnippets {
    /**
     * Generates code sample for creating a {@link ContainerRegistryClient}
     *
     * @return An instance of {@link ContainerRegistryClient}
     * @throws IllegalStateException If client cannot be created
     */
    public ContainerRegistryClient createAsyncContainerRegistryClient() {
        final String endpoint = getEndpoint();
        final TokenCredential credential = getTokenCredentials();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryClient.instantiation
        ContainerRegistryClient registryAsyncClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURERESOURCEMANAGERPUBLICCLOUD)
            .credential(credential)
            .buildClient();
        // END: com.azure.containers.containerregistry.ContainerRegistryClient.instantiation
        return registryAsyncClient;
    }

    public ContainerRegistryClient createAsyncContainerRegistryClientWithPipeline() {
        final String endpoint = getEndpoint();
        final TokenCredential credential = getTokenCredentials();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        ContainerRegistryClient registryAsyncClient = new ContainerRegistryClientBuilder()
            .pipeline(pipeline)
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURERESOURCEMANAGERPUBLICCLOUD)
            .credential(credential)
            .buildClient();
        // END: com.azure.containers.containerregistry.ContainerRegistryClient.pipeline.instantiation
        return registryAsyncClient;
    }

    public void deleteRepositoryCodeSnippet() {
        ContainerRegistryClient client = getAsyncClient();
        final String repositoryName = getRepositoryName();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepository#String
        client.deleteRepository(repositoryName);
        // END: com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepository#String
    }

    public void deleteRepositoryWithResponseCodeSnippet() {
        ContainerRegistryClient client = getAsyncClient();
        final String repositoryName = getRepositoryName();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepositoryWithResponse#String-Context
        client.deleteRepositoryWithResponse(repositoryName, Context.NONE);
        // END: com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepositoryWithResponse#String-Context
    }

    public void listRepositoryNamesCodeSnippet() {
        ContainerRegistryClient client = getAsyncClient();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames
        client.listRepositoryNames().stream().forEach(name -> {
            System.out.printf("Repository Name:%s,", name);
        });
        // END: com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames
    }

    public void listRepositoryNamesWithContextCodeSnippet() {
        ContainerRegistryClient client = getAsyncClient();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames#Context
        client.listRepositoryNames(Context.NONE).stream().forEach(name -> {
            System.out.printf("Repository Name:%s,", name);
        });
        // END: com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames#Context
    }

    public void getRepositoryCodeSnippet() {
        ContainerRegistryClient client = getAsyncClient();
        final String repositoryName = getRepositoryName();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryClient.getRepository
        ContainerRepository repository = client.getRepository(repositoryName);
        ContainerRepositoryProperties properties = repository.getProperties();
        System.out.println(properties.getName());
        // END: com.azure.containers.containerregistry.ContainerRegistryClient.getRepository
    }

    public void getArtifactCodeSnippet() {
        ContainerRegistryClient client = getAsyncClient();
        final String repositoryName = getRepositoryName();
        final String tagOrDigest = getTagOrDigest();

        // BEGIN: com.azure.containers.containerregistry.ContainerRegistryClient.getArtifact
        RegistryArtifact registryArtifact = client.getArtifact(repositoryName, tagOrDigest);
        ArtifactManifestProperties properties = registryArtifact.getManifestProperties();
        System.out.println(properties.getDigest());
        // END: com.azure.containers.containerregistry.ContainerRegistryClient.getArtifact
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
    private ContainerRegistryClient getAsyncClient() {
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

