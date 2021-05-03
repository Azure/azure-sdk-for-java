// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ContentProperties;
import com.azure.containers.containerregistry.models.DeleteRepositoryResult;
import com.azure.containers.containerregistry.models.ManifestOrderBy;
import com.azure.containers.containerregistry.models.RepositoryProperties;
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
        DeleteRepositoryResult result = client.delete();
        System.out.printf(
            "Tag Count: %1d, Artifact Count: %2d",
            result.getDeletedTags(),
            result.getDeletedManifests());
        // END: com.azure.containers.containerregistry.repository.deleteRepository
    }

    public void deleteRepositoryWithResponseCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.deleteRepositoryWithResponse
        Response<DeleteRepositoryResult> response = client.deleteWithResponse(Context.NONE);
        DeleteRepositoryResult result = response.getValue();
        System.out.printf(
            "Tag Count: %1d, Artifact Count: %2d",
            result.getDeletedTags(),
            result.getDeletedManifests());
        // END: com.azure.containers.containerregistry.repository.deleteRepositoryWithResponse
    }



    public void getPropertiesCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.getProperties
        RepositoryProperties properties = client.getProperties();
        System.out.printf("Name:%s,", properties.getName());
        // END: com.azure.containers.containerregistry.repository.getProperties
    }

    public void getPropertiesWithResponseCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.getPropertiesWithResponse
        Response<RepositoryProperties> response = client.getPropertiesWithResponse(Context.NONE);
        final RepositoryProperties properties = response.getValue();
        System.out.printf("Name:%s,", properties.getName());
        // END: com.azure.containers.containerregistry.repository.getPropertiesWithResponse
    }

    public void updatePropertiesCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.updateProperties
        ContentProperties properties = getContentProperties();
        client.updateProperties(properties);
        // END: com.azure.containers.containerregistry.repository.updateProperties
    }

    public void updatePropertiesWithResponseCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.updatePropertiesWithResponse
        ContentProperties properties = getContentProperties();
        client.updatePropertiesWithResponse(properties, Context.NONE);
        // END: com.azure.containers.containerregistry.repository.updatePropertiesWithResponse
    }

    public void listManifestPropertiesCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.listManifests
        client.listManifests().iterableByPage(10)
            .forEach(pagedResponse -> {
                pagedResponse.getValue().stream().forEach(
                    ManifestProperties -> System.out.println(ManifestProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.repository.listManifests
    }

    public void listManifestPropertiesWithOptionsCodeSnippet() {
        ContainerRepository client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repository.listManifestsWithOptions
        client.listManifests(ManifestOrderBy.LAST_UPDATED_ON_DESCENDING, Context.NONE).iterableByPage(10)
            .forEach(pagedResponse -> {
                pagedResponse.getValue().stream().forEach(
                    ManifestProperties -> System.out.println(ManifestProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.repository.listManifestsWithOptions
    }


    /**
     * Implementation not provided for this method.
     *
     * @return {@code null}
     */
    private ContentProperties getContentProperties() {
        return null;
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
     * Implementation not provided for this method.
     *
     * @return {@code null}
     */
    private String getTag() {
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
