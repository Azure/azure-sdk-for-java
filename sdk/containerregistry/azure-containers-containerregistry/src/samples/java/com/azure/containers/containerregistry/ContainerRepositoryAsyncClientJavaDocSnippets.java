// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ContentProperties;
import com.azure.containers.containerregistry.models.DeleteRepositoryResult;
import com.azure.containers.containerregistry.models.ListRegistryArtifactOptions;
import com.azure.containers.containerregistry.models.ListTagsOptions;
import com.azure.containers.containerregistry.models.RegistryArtifactOrderBy;
import com.azure.containers.containerregistry.models.RegistryArtifactProperties;
import com.azure.containers.containerregistry.models.RepositoryProperties;
import com.azure.containers.containerregistry.models.TagOrderBy;
import com.azure.containers.containerregistry.models.TagProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

public class ContainerRepositoryAsyncClientJavaDocSnippets {
    /**
     * Generates code sample for creating a {@link ContainerRepositoryAsyncClient}
     *
     * @return An instance of {@link ContainerRepositoryAsyncClient}
     * @throws IllegalStateException If configuration credentials cannot be created
     */
    public ContainerRepositoryAsyncClient createAsyncContainerRepositoryClient() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.instantiation
        ContainerRepositoryAsyncClient repositoryAsyncClient = new ContainerRepositoryClientBuilder()
            .endpoint(endpoint)
            .repository(repository)
            .credential(credential)
            .buildAsyncClient();
        // END: com.azure.containers.containerregistry.async.repositoryclient.instantiation
        return repositoryAsyncClient;
    }

    public ContainerRepositoryAsyncClient createAsyncContainerRespositoryClientWithPipeline() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        ContainerRepositoryAsyncClient repositoryAsyncClient = new ContainerRepositoryClientBuilder()
            .pipeline(pipeline)
            .endpoint(endpoint)
            .repository(repository)
            .credential(credential)
            .buildAsyncClient();
        // END: com.azure.containers.containerregistry.async.repositoryclient.pipeline.instantiation
        return repositoryAsyncClient;
    }

    public void deleteRepositoryCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.deleteRepository
        client.delete().subscribe(response -> {
            System.out.printf(
                "Tag Count: %1d, Artifact Count: %2d",
                response.getDeletedTags(),
                response.getDeletedRegistryArtifactDigests());
        });
        // END: com.azure.containers.containerregistry.async.repositoryclient.deleteRepository
    }

    public void deleteRepositoryWithResponseCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.deleteRepositoryWithResponse
        client.deleteWithResponse().subscribe(response -> {
            final DeleteRepositoryResult result = response.getValue();
            System.out.printf(
                "Tag Count: %1d, Artifact Count: %2d",
                result.getDeletedTags(),
                result.getDeletedRegistryArtifactDigests());
        });
        // END: com.azure.containers.containerregistry.async.repositoryclient.deleteRepositoryWithResponse
    }

    public void deleteRegistryArtifactCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.deleteRegistryArtifact
        String digest = getDigest();
        client.deleteRegistryArtifact(digest).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.deleteRegistryArtifact
    }

    public void deleteRegistryArtifactWithResponseCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.deleteRegistryArtifactWithResponse
        String digest = getDigest();
        client.deleteRegistryArtifactWithResponse(digest).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.deleteRegistryArtifactWithResponse
    }

    public void deleteTagCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.deleteTag
        String tag = getTag();
        client.deleteTag(tag).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.deleteTag
    }

    public void deleteTagWithResponseCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.deleteTagWithResponse
        String tag = getTag();
        client.deleteRegistryArtifactWithResponse(tag).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.deleteTagWithResponse
    }

    public void getPropertiesCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.getProperties
        client.getProperties().subscribe(response -> {
            System.out.printf("Name:%s,", response.getName());
        });
        // END: com.azure.containers.containerregistry.async.repositoryclient.getProperties
    }

    public void getPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.getPropertiesWithResponse
        client.getPropertiesWithResponse().subscribe(response -> {
            final RepositoryProperties properties = response.getValue();
            System.out.printf("Name:%s,", properties.getName());
        });
        // END: com.azure.containers.containerregistry.async.repositoryclient.getPropertiesWithResponse
    }

    public void getRegistryArtifactPropertiesCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.getRegistryArtifactProperties
        String tagOrDigest = getTagOrDigest();
        client.getRegistryArtifactProperties(tagOrDigest)
            .subscribe(properties -> {
                System.out.printf("Digest:%s,", properties.getDigest());
            });
        // END: com.azure.containers.containerregistry.async.repositoryclient.getRegistryArtifactProperties
    }

    public void getRegistryArtifactPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.getRegistryArtifactPropertiesWithResponse
        String tagOrDigest = getTagOrDigest();
        client.getRegistryArtifactPropertiesWithResponse(tagOrDigest)
            .subscribe(response -> {
                final RegistryArtifactProperties properties = response.getValue();
                System.out.printf("Digest:%s,", properties.getDigest());
            });
        // END: com.azure.containers.containerregistry.async.repositoryclient.getRegistryArtifactPropertiesWithResponse
    }

    public void listRegistryArtifactsCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.listRegistryArtifacts
        client.listRegistryArtifacts().byPage(10)
            .subscribe(artifactPropertiesPagedResponse -> {
                artifactPropertiesPagedResponse.getValue().stream().forEach(
                    artifactProperties -> System.out.println(artifactProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.async.repositoryclient.listRegistryArtifacts
    }

    public void listRegistryArtifactsWithOptionsCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.listRegistryArtifactsWithOptions
        ListRegistryArtifactOptions options = new ListRegistryArtifactOptions()
            .setRegistryArtifactOrderBy(RegistryArtifactOrderBy.LAST_UPDATED_ON_DESCENDING);
        client.listRegistryArtifacts(options).byPage(10)
            .subscribe(artifactPropertiesPagedResponse -> {
                artifactPropertiesPagedResponse.getValue().stream().forEach(
                    artifactProperties -> System.out.println(artifactProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.async.repositoryclient.listRegistryArtifactsWithOptions
    }

    public void getTagPropertiesCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.getTagProperties
        String tag = getTag();
        client.getTagProperties(tag).subscribe(properties -> {
            System.out.printf("Digest:%s,", properties.getDigest());
        });
        // END: com.azure.containers.containerregistry.async.repositoryclient.getTagProperties
    }

    public void getTagPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.getTagPropertiesWithResponse
        String tag = getTag();
        client.getTagPropertiesWithResponse(tag).subscribe(response -> {
            final TagProperties properties = response.getValue();
            System.out.printf("Digest:%s,", properties.getDigest());
        });
        // END: com.azure.containers.containerregistry.async.repositoryclient.getTagPropertiesWithResponse
    }

    public void listTagsCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.listTags
        client.listTags().byPage(10)
            .subscribe(tagPropertiesPagedResponse -> {
                tagPropertiesPagedResponse.getValue().stream().forEach(
                    tagProperties -> System.out.println(tagProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.async.repositoryclient.listTags
    }

    public void listTagsWithOptionsCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.listTagsWithOptions
        ListTagsOptions options = new ListTagsOptions()
            .setTagOrderBy(TagOrderBy.LAST_UPDATED_ON_DESCENDING);
        client.listTags(options).byPage(10)
            .subscribe(tagPropertiesPagedResponse -> {
                tagPropertiesPagedResponse.getValue().stream().forEach(
                    tagProperties -> System.out.println(tagProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.async.repositoryclient.listTagsWithOptions
    }

    public void updatePropertiesCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.updateProperties
        ContentProperties properties = getContentProperties();
        client.updateProperties(properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.updateProperties
    }

    public void updatePropertiesWithResponseCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.updatePropertiesWithResponse
        ContentProperties properties = getContentProperties();
        client.updatePropertiesWithResponse(properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.updatePropertiesWithResponse
    }

    public void updateTagPropertiesCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.updateTagProperties
        ContentProperties properties = getContentProperties();
        String tag = getTag();
        client.updateTagProperties(tag, properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.updateTagProperties
    }

    public void updateTagPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.updateTagPropertiesWithResponse
        ContentProperties properties = getContentProperties();
        String tag = getTag();
        client.updateTagPropertiesWithResponse(tag, properties).subscribe();
        client.updateProperties(properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.updateTagPropertiesWithResponse
    }

    public void updateManifestPropertiesCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.updateManifestProperties
        ContentProperties properties = getContentProperties();
        String digest = getDigest();
        client.updateManifestProperties(digest, properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.updateManifestProperties
    }

    public void updateManifestPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.async.repositoryclient.updateManifestPropertiesWithResponse
        ContentProperties properties = getContentProperties();
        String digest = getDigest();
        client.updateManifestPropertiesWithResponse(digest, properties).subscribe();
        // END: com.azure.containers.containerregistry.async.repositoryclient.updateManifestPropertiesWithResponse
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
    private ContainerRepositoryAsyncClient getAsyncClient() {
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
