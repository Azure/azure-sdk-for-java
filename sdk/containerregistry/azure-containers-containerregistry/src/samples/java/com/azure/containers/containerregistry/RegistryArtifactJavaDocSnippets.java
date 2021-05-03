// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.ContentProperties;
import com.azure.containers.containerregistry.models.TagOrderBy;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

public class RegistryArtifactJavaDocSnippets {
    public RegistryArtifact createRegistryArtifact() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        String digest = getDigest();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.instantiation
        RegistryArtifact registryArtifact = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient().getArtifact(repository, digest);
        // END: com.azure.containers.containerregistry.registryartifact.instantiation
        return registryArtifact;
    }

    public RegistryArtifact createContainerRepositoryWithPipeline() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        String digest = getDigest();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        RegistryArtifact registryArtifact = new ContainerRegistryClientBuilder()
            .pipeline(pipeline)
            .endpoint(endpoint)
            .credential(credential)
            .buildClient().getArtifact(repository, digest);
        // END: com.azure.containers.containerregistry.registryartifact.pipeline.instantiation
        return registryArtifact;
    }

    public void deleteRegistryArtifactCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.delete
        client.delete();
        // END: com.azure.containers.containerregistry.registryartifact.delete
    }

    public void deleteRegistryArtifactWithResponseCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.deleteWithResponse
        client.deleteWithResponse(Context.NONE);
        // END: com.azure.containers.containerregistry.registryartifact.deleteWithResponse
    }

    public void deleteTagCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.deleteTag
        String tag = getTag();
        client.deleteTag(tag);
        // END: com.azure.containers.containerregistry.registryartifact.deleteTag
    }

    public void deleteTagWithResponseCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.deleteTagWithResponse
        String tag = getTag();
        client.deleteTagWithResponse(tag, Context.NONE);
        // END: com.azure.containers.containerregistry.registryartifact.deleteTagWithResponse
    }

    public void getManifestPropertiesCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.getManifestProperties
        ArtifactManifestProperties properties = client.getManifestProperties();
        System.out.printf("Digest:%s,", properties.getDigest());
        // END: com.azure.containers.containerregistry.registryartifact.getManifestProperties
    }

    public void getManifestPropertiesWithResponseCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.getManifestPropertiesWithResponse
        Response<ArtifactManifestProperties> response = client.getManifestPropertiesWithResponse(
            Context.NONE);
        final ArtifactManifestProperties properties = response.getValue();
        System.out.printf("Digest:%s,", properties.getDigest());
        // END: com.azure.containers.containerregistry.registryartifact.getManifestPropertiesWithResponse
    }

    public void getTagPropertiesCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.getTagProperties
        String tag = getTag();
        ArtifactTagProperties properties = client.getTagProperties(tag);
        System.out.printf("Digest:%s,", properties.getDigest());
        // END: com.azure.containers.containerregistry.registryartifact.getTagProperties
    }

    public void getTagPropertiesWithResponseCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.getTagPropertiesWithResponse
        String tag = getTag();
        Response<ArtifactTagProperties> response = client.getTagPropertiesWithResponse(tag, Context.NONE);
        final ArtifactTagProperties properties = response.getValue();
        System.out.printf("Digest:%s,", properties.getDigest());
        // END: com.azure.containers.containerregistry.registryartifact.getTagPropertiesWithResponse
    }

    public void listTagsCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.listTags
        client.listTags().iterableByPage(10).forEach(pagedResponse -> {
            pagedResponse.getValue().stream().forEach(
                tagProperties -> System.out.println(tagProperties.getDigest()));
        });
        // END: com.azure.containers.containerregistry.registryartifact.listTags
    }

    public void listTagsWithOptionsCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.listTagsWithOptions
        client.listTags(TagOrderBy.LAST_UPDATED_ON_DESCENDING, Context.NONE).iterableByPage(10).forEach(pagedResponse -> {
            pagedResponse.getValue().stream().forEach(
                tagProperties -> System.out.println(tagProperties.getDigest()));
        });
        // END: com.azure.containers.containerregistry.registryartifact.listTagsWithOptions
    }
    public void updateTagPropertiesCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.updateTagProperties
        ContentProperties properties = getContentProperties();
        String tag = getTag();
        client.updateTagProperties(tag, properties);
        // END: com.azure.containers.containerregistry.registryartifact.updateTagProperties
    }

    public void updateTagPropertiesWithResponseCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.updateTagPropertiesWithResponse
        ContentProperties properties = getContentProperties();
        String tag = getTag();
        client.updateTagPropertiesWithResponse(tag, properties, Context.NONE);
        // END: com.azure.containers.containerregistry.registryartifact.updateTagPropertiesWithResponse
    }

    public void updateManifestPropertiesCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.updateManifestProperties
        ContentProperties properties = getContentProperties();
        client.updateManifestProperties(properties);
        // END: com.azure.containers.containerregistry.registryartifact.updateManifestProperties
    }

    public void updateManifestPropertiesWithResponseCodeSnippet() {
        RegistryArtifact client = getClient();
        // BEGIN: com.azure.containers.containerregistry.registryartifact.updateManifestPropertiesWithResponse
        ContentProperties properties = getContentProperties();
        client.updateManifestPropertiesWithResponse(properties, Context.NONE);
        // END: com.azure.containers.containerregistry.registryartifact.updateManifestPropertiesWithResponse
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
    private RegistryArtifact getClient() {
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
