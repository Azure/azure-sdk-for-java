// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestOrderBy;
import com.azure.containers.containerregistry.models.ContainerRepositoryProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

public class ContainerRepositoryAsyncJavaDocSnippets {
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
            System.out.printf("Successfully initiated delete of the repository.");
        }, error -> {
            System.out.println("Failed to initiate a delete of the repository.");
        });
        // END: com.azure.containers.containerregistry.async.repository.deleteRepository
    }

    public void deleteRepositoryWithResponseCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.deleteRepositoryWithResponse
        client.deleteWithResponse().subscribe(response -> {
            System.out.printf("Successfully initiated delete of the repository.");
        }, error -> {
            System.out.println("Failed to initiate a delete of the repository.");
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
            final ContainerRepositoryProperties properties = response.getValue();
            System.out.printf("Name:%s,", properties.getName());
        });
        // END: com.azure.containers.containerregistry.async.repository.getPropertiesWithResponse
    }


    public void updatePropertiesCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.updateProperties
        ContainerRepositoryProperties properties = getRepositoryProperties();
        client.updateProperties(properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repository.updateProperties
    }

    public void updatePropertiesWithResponseCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.updatePropertiesWithResponse
        ContainerRepositoryProperties properties = getRepositoryProperties();
        client.updatePropertiesWithResponse(properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repository.updatePropertiesWithResponse
    }

    public void listManifestPropertiesCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.listManifestProperties
        client.listManifestProperties().byPage(10)
            .subscribe(ManifestPropertiesPagedResponse -> {
                ManifestPropertiesPagedResponse.getValue().stream().forEach(
                    ManifestProperties -> System.out.println(ManifestProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.async.repository.listManifestProperties
    }

    public void listManifestPropertiesWithOptionsCodeSnippet() {
        ContainerRepositoryAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repository.listManifestPropertiesWithOptions
        client.listManifestProperties(ArtifactManifestOrderBy.LAST_UPDATED_ON_DESCENDING).byPage(10)
            .subscribe(ManifestPropertiesPagedResponse -> {
                ManifestPropertiesPagedResponse.getValue().stream().forEach(
                    ManifestProperties -> System.out.println(ManifestProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.async.repository.listManifestPropertiesWithOptions
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
