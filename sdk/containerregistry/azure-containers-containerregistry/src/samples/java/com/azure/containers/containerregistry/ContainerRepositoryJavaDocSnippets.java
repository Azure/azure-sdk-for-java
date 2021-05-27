// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestOrderBy;
import com.azure.containers.containerregistry.models.ContainerRepositoryProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

public class ContainerRepositoryJavaDocSnippets {
    /**
     * Generates code sample for creating a {@link ContainerRepository}
     *
     * @return An instance of {@link ContainerRepository}
     */
    public ContainerRepository createContainerRepository() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.repository.instantiation
        ContainerRepository repositoryClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient().getRepository(repository);
        // END: com.azure.containers.containerregistry.repository.instantiation
        return repositoryClient;
    }

    public ContainerRepository createContainerRepositoryWithPipeline() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.repository.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        ContainerRepository repositoryClient = new ContainerRegistryClientBuilder()
            .pipeline(pipeline)
            .endpoint(endpoint)
            .credential(credential)
            .buildClient().getRepository(repository);
        // END: com.azure.containers.containerregistry.repository.pipeline.instantiation
        return repositoryClient;
    }

    public void deleteRepositoryCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.deleteRepository
        client.delete();
        System.out.printf("Successfully initiated delete.");
        // END: com.azure.containers.containerregistry.repository.deleteRepository
    }

    public void deleteRepositoryWithResponseCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.deleteRepositoryWithResponse
        Response<Void> response = client.deleteWithResponse(Context.NONE);
        System.out.printf("Successfully initiated delete.");
        // END: com.azure.containers.containerregistry.repository.deleteRepositoryWithResponse
    }

    public void getPropertiesCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.getProperties
        ContainerRepositoryProperties properties = client.getProperties();
        System.out.printf("Name:%s,", properties.getName());
        // END: com.azure.containers.containerregistry.repository.getProperties
    }

    public void getPropertiesWithResponseCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.getPropertiesWithResponse
        Response<ContainerRepositoryProperties> response = client.getPropertiesWithResponse(Context.NONE);
        final ContainerRepositoryProperties properties = response.getValue();
        System.out.printf("Name:%s,", properties.getName());
        // END: com.azure.containers.containerregistry.repository.getPropertiesWithResponse
    }

    public void updatePropertiesCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.updateProperties
        ContainerRepositoryProperties properties = getRepositoryProperties();
        client.updateProperties(properties);
        // END: com.azure.containers.containerregistry.repository.updateProperties
    }

    public void updatePropertiesWithResponseCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.updatePropertiesWithResponse
        ContainerRepositoryProperties properties = getRepositoryProperties();
        client.updatePropertiesWithResponse(properties, Context.NONE);
        // END: com.azure.containers.containerregistry.repository.updatePropertiesWithResponse
    }

    public void listManifestPropertiesCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.listManifestProperties
        client.listManifestProperties().iterableByPage(10)
            .forEach(pagedResponse -> {
                pagedResponse.getValue().stream().forEach(
                    ManifestProperties -> System.out.println(ManifestProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.repository.listManifestProperties
    }

    public void listManifestPropertiesWithOptionsNoContextCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.listManifestPropertiesWithOptionsNoContext
        client.listManifestProperties(ArtifactManifestOrderBy.LAST_UPDATED_ON_DESCENDING).iterableByPage(10)
            .forEach(pagedResponse -> {
                pagedResponse.getValue().stream().forEach(
                    ManifestProperties -> System.out.println(ManifestProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.repository.listManifestPropertiesWithOptionsNoContext
    }

    public void listManifestPropertiesWithOptionsCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.listManifestPropertiesWithOptions
        client.listManifestProperties(ArtifactManifestOrderBy.LAST_UPDATED_ON_DESCENDING, Context.NONE).iterableByPage(10)
            .forEach(pagedResponse -> {
                pagedResponse.getValue().stream().forEach(
                    ManifestProperties -> System.out.println(ManifestProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.repository.listManifestPropertiesWithOptions
    }

    /**
     * Implementation not provided for this method.
     *
     * @return {@code null}
     */
    private ContainerRepositoryProperties getRepositoryProperties() {
        return null;
    }

    /**
     * Implementation not provided for this method.
     *
     * @return {@code null}
     */
    private String getDigest() {
        return null;
    }

    /**
     * Implementation not provided for this method.
     *
     * @return {@code null}
     */
    private String getTag() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    private ContainerRepository getClient() {
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
    private String getRepository() {
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
