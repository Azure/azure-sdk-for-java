// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.ArtifactManifestOrderBy;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Code samples for the README.md
 *
 */
public class ReadmeSamples {

    private String endpoint = "endpoint";

    public void createClient() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .credential(credential)
            .buildClient();
    }

    public void createAsyncClient() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .credential(credential)
            .buildAsyncClient();
    }

    public void listRepositoryNamesSample() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .credential(credential)
            .buildClient();

        client.listRepositoryNames().forEach(repository -> System.out.println(repository));
    }

    private final String repositoryName = "repository";

    public void getPropertiesThrows() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRepository containerRepository = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .credential(credential)
            .buildClient()
            .getRepository(repositoryName);
        try {
            containerRepository.getProperties();
        } catch (HttpResponseException exception) {
            // Do something with the exception.
        }
    }

    public void createAnonymousAccessClient() {
        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .buildClient();
    }

    public void createAnonymousAccessAsyncClient() {
        ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .buildAsyncClient();
    }

    public void deleteImages() {
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .credential(defaultCredential)
            .buildClient();

        final int imagesCountToKeep = 3;
        for (String repositoryName : client.listRepositoryNames()) {
            final ContainerRepository repository = client.getRepository(repositoryName);

            // Obtain the images ordered from newest to oldest
            PagedIterable<ArtifactManifestProperties> imageManifests =
                repository.listManifestProperties(
                    ArtifactManifestOrderBy.LAST_UPDATED_ON_DESCENDING,
                    Context.NONE);

            imageManifests.stream().skip(imagesCountToKeep)
                .forEach(imageManifest -> {
                    System.out.printf(String.format("Deleting image with digest %s.%n", imageManifest.getDigest()));
                    System.out.printf("    This image has the following tags: ");

                    for (String tagName : imageManifest.getTags()) {
                        System.out.printf("        %s:%s", imageManifest.getRepositoryName(), tagName);
                    }

                    repository.getArtifact(imageManifest.getDigest()).delete();
                });
        }
    }

    private String tag = "tag";

    public void setArtifactProperties() {
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .credential(defaultCredential)
            .buildClient();

        RegistryArtifact image = client.getArtifact(repositoryName, digest);

        image.updateTagProperties(
            tag,
            new ArtifactTagProperties()
                .setWriteEnabled(false)
                .setDeleteEnabled(false));
    }

    private final String architecture = "architecture";
    private final String os = "os";
    private final String digest = "digest";

    public void listTagProperties() {
        ContainerRegistryClient anonymousClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .buildClient();

        RegistryArtifact image = anonymousClient.getArtifact(repositoryName, digest);
        PagedIterable<ArtifactTagProperties> tags = image.listTagProperties();

        System.out.printf(String.format("%s has the following aliases:", image.getFullyQualifiedReference()));

        for (ArtifactTagProperties tag : tags) {
            System.out.printf(String.format("%s/%s:%s", anonymousClient.getEndpoint(), repositoryName, tag.getName()));
        }
    }

    public void anonymousClientThrows() {
        final String endpoint = getEndpoint();
        final String repositoryName = getRepositoryName();

        ContainerRegistryClient anonymousClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .buildClient();

        try {
            anonymousClient.deleteRepository(repositoryName);
            System.out.println("Unexpected Success: Delete is not allowed on anonymous access");
        } catch (ClientAuthenticationException ex) {
            System.out.println("Expected exception: Delete is not allowed on anonymous access");
        }
    }

    private static String getEndpoint() {
        return null;
    }

    private static String getRepositoryName() {
        return null;
    }

    private static String getTagName() {
        return null;
    }

    private final TokenCredential credentials = null;
    public void nationalCloudSample() {
        ContainerRegistryClient containerRegistryClient = new ContainerRegistryClientBuilder()
            .endpoint(getEndpoint())
            .credential(credentials)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_CHINA)
            .buildClient();

        containerRegistryClient
            .listRepositoryNames()
            .forEach(name -> System.out.println(name));
    }
}

