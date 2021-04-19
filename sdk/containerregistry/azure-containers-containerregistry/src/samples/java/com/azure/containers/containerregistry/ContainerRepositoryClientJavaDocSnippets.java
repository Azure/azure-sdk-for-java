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
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

public class ContainerRepositoryClientJavaDocSnippets {
    /**
     * Generates code sample for creating a {@link ContainerRepositoryClient}
     *
     * @return An instance of {@link ContainerRepositoryClient}
     */
    public ContainerRepositoryClient createContainerRepositoryClient() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.instantiation
        ContainerRepositoryClient repositoryClient = new ContainerRepositoryClientBuilder()
            .endpoint(endpoint)
            .repository(repository)
            .credential(credential)
            .buildClient();
        // END: com.azure.containers.containerregistry.repositoryclient.instantiation
        return repositoryClient;
    }

    public ContainerRepositoryClient createContainerRepositoryClientWithPipeline() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        ContainerRepositoryClient repositoryClient = new ContainerRepositoryClientBuilder()
            .pipeline(pipeline)
            .endpoint(endpoint)
            .repository(repository)
            .credential(credential)
            .buildClient();
        // END: com.azure.containers.containerregistry.repositoryclient.pipeline.instantiation
        return repositoryClient;
    }

    public void deleteRepositoryCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.deleteRepository
        DeleteRepositoryResult result = client.delete();
        System.out.printf(
            "Tag Count: %1d, Artifact Count: %2d",
            result.getDeletedTags(),
            result.getDeletedRegistryArtifactDigests());
        // END: com.azure.containers.containerregistry.repositoryclient.deleteRepository
    }

    public void deleteRepositoryWithResponseCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.deleteRepositoryWithResponse
        Response<DeleteRepositoryResult> response = client.deleteWithResponse(Context.NONE);
        DeleteRepositoryResult result = response.getValue();
        System.out.printf(
            "Tag Count: %1d, Artifact Count: %2d",
            result.getDeletedTags(),
            result.getDeletedRegistryArtifactDigests());
        // END: com.azure.containers.containerregistry.repositoryclient.deleteRepositoryWithResponse
    }

    public void deleteRegistryArtifactCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.deleteRegistryArtifact
        String digest = getDigest();
        client.deleteRegistryArtifact(digest);
        // END: com.azure.containers.containerregistry.repositoryclient.deleteRegistryArtifact
    }

    public void deleteRegistryArtifactWithResponseCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.deleteRegistryArtifactWithResponse
        String digest = getDigest();
        client.deleteRegistryArtifactWithResponse(digest, Context.NONE);
        // END: com.azure.containers.containerregistry.repositoryclient.deleteRegistryArtifactWithResponse
    }

    public void deleteTagCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.deleteTag
        String tag = getTag();
        client.deleteTag(tag);
        // END: com.azure.containers.containerregistry.repositoryclient.deleteTag
    }

    public void deleteTagWithResponseCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.deleteTagWithResponse
        String tag = getTag();
        client.deleteRegistryArtifactWithResponse(tag, Context.NONE);
        // END: com.azure.containers.containerregistry.repositoryclient.deleteTagWithResponse
    }

    public void getPropertiesCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.getProperties
        RepositoryProperties properties = client.getProperties();
        System.out.printf("Name:%s,", properties.getName());
        // END: com.azure.containers.containerregistry.repositoryclient.getProperties
    }

    public void getPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.getPropertiesWithResponse
        Response<RepositoryProperties> response = client.getPropertiesWithResponse(Context.NONE);
        final RepositoryProperties properties = response.getValue();
        System.out.printf("Name:%s,", properties.getName());
        // END: com.azure.containers.containerregistry.repositoryclient.getPropertiesWithResponse
    }

    public void getRegistryArtifactPropertiesCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.getRegistryArtifactProperties
        String tagOrDigest = getTagOrDigest();
        RegistryArtifactProperties properties = client.getRegistryArtifactProperties(tagOrDigest);
        System.out.printf("Digest:%s,", properties.getDigest());
        // END: com.azure.containers.containerregistry.repositoryclient.getRegistryArtifactProperties
    }

    public void getRegistryArtifactPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.getRegistryArtifactPropertiesWithResponse
        String tagOrDigest = getTagOrDigest();
        Response<RegistryArtifactProperties> response = client.getRegistryArtifactPropertiesWithResponse(
            tagOrDigest,
            Context.NONE);
        final RegistryArtifactProperties properties = response.getValue();
        System.out.printf("Digest:%s,", properties.getDigest());
        // END: com.azure.containers.containerregistry.repositoryclient.getRegistryArtifactPropertiesWithResponse
    }

    public void listRegistryArtifactPropertiesCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.listRegistryArtifacts
        client.listRegistryArtifacts().iterableByPage(10)
            .forEach(pagedResponse -> {
                pagedResponse.getValue().stream().forEach(
                    artifactProperties -> System.out.println(artifactProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.repositoryclient.listRegistryArtifacts
    }

    public void listRegistryArtifactPropertiesWithOptionsCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.listRegistryArtifactsWithOptions
        ListRegistryArtifactOptions options = new ListRegistryArtifactOptions()
            .setRegistryArtifactOrderBy(RegistryArtifactOrderBy.LAST_UPDATED_ON_DESCENDING);
        client.listRegistryArtifacts(options).iterableByPage(10)
            .forEach(pagedResponse -> {
                pagedResponse.getValue().stream().forEach(
                    artifactProperties -> System.out.println(artifactProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.repositoryclient.listRegistryArtifactsWithOptions
    }

    public void getTagPropertiesCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.getTagProperties
        String tag = getTag();
        TagProperties properties = client.getTagProperties(tag);
        System.out.printf("Digest:%s,", properties.getDigest());
        // END: com.azure.containers.containerregistry.repositoryclient.getTagProperties
    }

    public void getTagPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.getTagPropertiesWithResponse
        String tag = getTag();
        Response<TagProperties> response = client.getTagPropertiesWithResponse(tag, Context.NONE);
        final TagProperties properties = response.getValue();
        System.out.printf("Digest:%s,", properties.getDigest());
        // END: com.azure.containers.containerregistry.repositoryclient.getTagPropertiesWithResponse
    }

    public void listTagsCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.listTags
        client.listTags().iterableByPage(10).forEach(pagedResponse -> {
            pagedResponse.getValue().stream().forEach(
                tagProperties -> System.out.println(tagProperties.getDigest()));
        });
        // END: com.azure.containers.containerregistry.repositoryclient.listTags
    }

    public void listTagsWithOptionsCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.listTagsWithOptions
        ListTagsOptions options = new ListTagsOptions()
            .setTagOrderBy(TagOrderBy.LAST_UPDATED_ON_DESCENDING);
        client.listTags(options).iterableByPage(10).forEach(pagedResponse -> {
            pagedResponse.getValue().stream().forEach(
                tagProperties -> System.out.println(tagProperties.getDigest()));
        });
        // END: com.azure.containers.containerregistry.repositoryclient.listTagsWithOptions
    }

    public void updatePropertiesCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.updateProperties
        ContentProperties properties = getContentProperties();
        client.updateProperties(properties);
        // END: com.azure.containers.containerregistry.repositoryclient.updateProperties
    }

    public void updatePropertiesWithResponseCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.updatePropertiesWithResponse
        ContentProperties properties = getContentProperties();
        client.updatePropertiesWithResponse(properties, Context.NONE);
        // END: com.azure.containers.containerregistry.repositoryclient.updatePropertiesWithResponse
    }

    public void updateTagPropertiesCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.updateTagProperties
        ContentProperties properties = getContentProperties();
        String tag = getTag();
        client.updateTagProperties(tag, properties);
        // END: com.azure.containers.containerregistry.repositoryclient.updateTagProperties
    }

    public void updateTagPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.updateTagPropertiesWithResponse
        ContentProperties properties = getContentProperties();
        String tag = getTag();
        client.updateTagPropertiesWithResponse(tag, properties, Context.NONE);
        client.updateProperties(properties);
        // END: com.azure.containers.containerregistry.repositoryclient.updateTagPropertiesWithResponse
    }

    public void updateManifestPropertiesCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.updateManifestProperties
        ContentProperties properties = getContentProperties();
        String digest = getDigest();
        client.updateManifestProperties(digest, properties);
        // END: com.azure.containers.containerregistry.repositoryclient.updateManifestProperties
    }

    public void updateManifestPropertiesWithResponseCodeSnippet() {
        ContainerRepositoryClient client = getClient();
        // BEGIN: com.azure.containers.containerregistry.repositoryclient.updateManifestPropertiesWithResponse
        ContentProperties properties = getContentProperties();
        String digest = getDigest();
        client.updateManifestPropertiesWithResponse(digest, properties, Context.NONE);
        // END: com.azure.containers.containerregistry.repositoryclient.updateManifestPropertiesWithResponse
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
    private ContainerRepositoryClient getClient() {
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
