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

public class ContainerAsyncRepositoryJavaDocSnippets {
    /**
     * Generates code sample for creating a {@link ContainerRepositoryAsync}
     *
     * @return An instance of {@link ContainerRepositoryAsync}
     * @throws IllegalStateException If configuration credentials cannot be created
     */
    public ContainerRepositoryAsync createAsyncContainerRepository() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.async.repository.instantiation
        ContainerRepositoryAsync repositoryAsyncClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient()
            .getRepository(repository);
        // END: com.azure.containers.containerregistry.async.repository.instantiation
        return repositoryAsyncClient;
    }

    public ContainerRepositoryAsync createAsyncContainerRespositoryClientWithPipeline() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.async.repository.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        ContainerRepositoryAsync repositoryAsyncClient = new ContainerRegistryClientBuilder()
            .pipeline(pipeline)
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient()
            .getRepository(repository);
        // END: com.azure.containers.containerregistry.async.repository.pipeline.instantiation
        return repositoryAsyncClient;
    }

    public void deleteRepositoryCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.deleteRepository
        client.delete().subscribe(response -> {
            System.out.printf(
                "Tag Count: %1d, Artifact Count: %2d",
                response.getDeletedTags(),
                response.getDeletedManifests());
        });
        // END: com.azure.containers.containerregistry.async.repository.deleteRepository
    }

    public void deleteRepositoryWithResponseCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.deleteRepositoryWithResponse
        client.deleteWithResponse().subscribe(response -> {
            final DeleteRepositoryResult result = response.getValue();
            System.out.printf(
                "Tag Count: %1d, Artifact Count: %2d",
                result.getDeletedTags(),
                result.getDeletedManifests());
        });
        // END: com.azure.containers.containerregistry.async.repository.deleteRepositoryWithResponse
    }


    public void getPropertiesCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.getProperties
        client.getProperties().subscribe(response -> {
            System.out.printf("Name:%s,", response.getName());
        });
        // END: com.azure.containers.containerregistry.async.repository.getProperties
    }

    public void getPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.getPropertiesWithResponse
        client.getPropertiesWithResponse().subscribe(response -> {
            final RepositoryProperties properties = response.getValue();
            System.out.printf("Name:%s,", properties.getName());
        });
        // END: com.azure.containers.containerregistry.async.repository.getPropertiesWithResponse
    }


    public void updatePropertiesCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.updateProperties
        ContentProperties properties = getContentProperties();
        client.updateProperties(properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repository.updateProperties
    }

    public void updatePropertiesWithResponseCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.updatePropertiesWithResponse
        ContentProperties properties = getContentProperties();
        client.updatePropertiesWithResponse(properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repository.updatePropertiesWithResponse
    }

    public void listManifestsCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.listManifests
        client.listManifests().byPage(10)
            .subscribe(ManifestPropertiesPagedResponse -> {
                ManifestPropertiesPagedResponse.getValue().stream().forEach(
                    ManifestProperties -> System.out.println(ManifestProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.async.repository.listManifests
    }

    public void listManifestsWithOptionsCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.listManifestsWithOptions
        client.listManifests(ManifestOrderBy.LAST_UPDATED_ON_DESCENDING).byPage(10)
            .subscribe(ManifestPropertiesPagedResponse -> {
                ManifestPropertiesPagedResponse.getValue().stream().forEach(
                    ManifestProperties -> System.out.println(ManifestProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.async.repository.listManifestsWithOptions
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
    private ContainerRepositoryAsync getAsyncClient() {
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
