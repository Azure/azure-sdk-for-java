// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.ArtifactTagOrderBy;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

public class RegistryArtifactAsyncJavaDocSnippets {
    public RegistryArtifactAsync createRegistryArtifact() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        String digest = getDigest();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.instantiation
        RegistryArtifactAsync registryArtifactAsync = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .buildAsyncClient().getArtifact(repository, digest);
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.instantiation
        return registryArtifactAsync;
    }

    public RegistryArtifactAsync createContainerRepositoryWithPipeline() {
        String endpoint = getEndpoint();
        String repository = getRepository();
        String digest = getDigest();
        TokenCredential credential = getTokenCredentials();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        RegistryArtifactAsync registryArtifactAsync = new ContainerRegistryClientBuilder()
            .pipeline(pipeline)
            .endpoint(endpoint)
            .credential(credential)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .buildAsyncClient().getArtifact(repository, digest);
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.pipeline.instantiation
        return registryArtifactAsync;
    }

    public void deleteCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.delete
        client.delete().subscribe();
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.delete
    }

    public void deleteWithResponseCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.deleteWithResponse
        client.deleteWithResponse().subscribe();
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.deleteWithResponse
    }

    public void deleteTagCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.deleteTag
        String tag = getTag();
        client.deleteTag(tag).subscribe();
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.deleteTag
    }

    public void deleteTagWithResponseCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.deleteTagWithResponse
        String tag = getTag();
        client.deleteTagWithResponse(tag).subscribe();
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.deleteTagWithResponse
    }

    public void getManifestPropertiesCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.getManifestProperties
        client.getManifestProperties()
            .subscribe(properties -> {
                System.out.printf("Digest:%s,", properties.getDigest());
            });
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.getManifestProperties
    }

    public void getManifestPropertiesWithResponseCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.getManifestPropertiesWithResponse
        client.getManifestPropertiesWithResponse()
            .subscribe(response -> {
                final ArtifactManifestProperties properties = response.getValue();
                System.out.printf("Digest:%s,", properties.getDigest());
            });
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.getManifestPropertiesWithResponse
    }

    public void getTagPropertiesCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.getTagProperties
        String tag = getTag();
        client.getTagProperties(tag).subscribe(properties -> {
            System.out.printf("Digest:%s,", properties.getDigest());
        });
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.getTagProperties
    }

    public void getTagPropertiesWithResponseCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.getTagPropertiesWithResponse
        String tag = getTag();
        client.getTagPropertiesWithResponse(tag).subscribe(response -> {
            final ArtifactTagProperties properties = response.getValue();
            System.out.printf("Digest:%s,", properties.getDigest());
        });
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.getTagPropertiesWithResponse
    }

    public void listTagPropertiesCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.listTagProperties
        client.listTagProperties().byPage(10)
            .subscribe(tagPropertiesPagedResponse -> {
                tagPropertiesPagedResponse.getValue().stream().forEach(
                    tagProperties -> System.out.println(tagProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.listTagProperties
    }

    public void listTagPropertiesWithOptionsCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.listTagPropertiesWithOptions
        client.listTagProperties(ArtifactTagOrderBy.LAST_UPDATED_ON_DESCENDING)
            .byPage(10)
            .subscribe(tagPropertiesPagedResponse -> {
                tagPropertiesPagedResponse.getValue()
                    .stream()
                    .forEach(tagProperties -> System.out.println(tagProperties.getDigest()));
            });
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.listTagPropertiesWithOptions
    }

    public void updateTagPropertiesCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.updateTagProperties
        ArtifactTagProperties properties = getTagProperties();
        String tag = getTag();
        client.updateTagProperties(tag, properties).subscribe();
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.updateTagProperties
    }

    public void updateTagPropertiesWithResponseCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.updateTagPropertiesWithResponse
        ArtifactTagProperties properties = getTagProperties();
        String tag = getTag();
        client.updateTagPropertiesWithResponse(tag, properties).subscribe();
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.updateTagPropertiesWithResponse
    }

    public void updateManifestPropertiesCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.updateManifestProperties
        ArtifactManifestProperties properties = getArtifactManifestProperties();
        client.updateManifestProperties(properties).subscribe();
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.updateManifestProperties
    }

    public void updateManifestPropertiesWithResponseCodeSnippet() {
        RegistryArtifactAsync client = getAsyncClient();
        // BEGIN: com.azure.containers.containerregistry.RegistryArtifactAsync.updateManifestPropertiesWithResponse
        ArtifactManifestProperties properties = getArtifactManifestProperties();
        client.updateManifestPropertiesWithResponse(properties).subscribe();
        // END: com.azure.containers.containerregistry.RegistryArtifactAsync.updateManifestPropertiesWithResponse
    }

    /**
     * Implementation not provided for this method.
     *
     * @return {@code null}
     */
    private ArtifactTagProperties getTagProperties() {
        return null;
    }

    /**
     * Implementation not provided for this method.
     *
     * @return {@code null}
     */
    private ArtifactManifestProperties getArtifactManifestProperties() {
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
    private RegistryArtifactAsync getAsyncClient() {
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
