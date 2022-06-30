// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.ArtifactManifestOrder;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Code samples for the README.md
 *
 */
public class ReadmeSamples {

    private String endpoint = "endpoint";

    public void createClient() {
        // BEGIN: readme-sample-createClient
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .credential(credential)
            .buildClient();
        // END: readme-sample-createClient
    }

    public void createAsyncClient() {
        // BEGIN: readme-sample-createAsyncClient
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .credential(credential)
            .buildAsyncClient();
        // END: readme-sample-createAsyncClient
    }

    public void listRepositoryNamesSample() {
        // BEGIN: readme-sample-listRepositoryNames
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .credential(credential)
            .buildClient();

        client.listRepositoryNames().forEach(repository -> System.out.println(repository));
        // END: readme-sample-listRepositoryNames
    }

    private final String repositoryName = "repository";

    public void getPropertiesThrows() {
        // BEGIN: readme-sample-getProperties
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
        // END: readme-sample-getProperties
    }

    public void createAnonymousAccessClient() {
        // BEGIN: readme-sample-createAnonymousAccessClient
        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .buildClient();
        // END: readme-sample-createAnonymousAccessClient
    }

    public void createAnonymousAccessAsyncClient() {
        // BEGIN: readme-sample-createAnonymousAsyncAccessClient
        ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .buildAsyncClient();
        // END: readme-sample-createAnonymousAsyncAccessClient
    }

    public void deleteImages() {
        // BEGIN: readme-sample-deleteImages
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
                    ArtifactManifestOrder.LAST_UPDATED_ON_DESCENDING,
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
        // END: readme-sample-deleteImages
    }

    private String tag = "tag";

    public void setArtifactProperties() {
        // BEGIN: readme-sample-setArtifactProperties
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
        // END: readme-sample-setArtifactProperties
    }

    private final String architecture = "architecture";
    private final String os = "os";
    private final String digest = "digest";

    public void listTagProperties() {
        // BEGIN: readme-sample-listTagProperties
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
        // END: readme-sample-listTagProperties
    }

    public void anonymousClientThrows() {
        // BEGIN: readme-sample-anonymousClientThrows
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
        // END: readme-sample-anonymousClientThrows
    }

    public void anonymousAsyncClientThrows() {
        final String endpoint = getEndpoint();
        final String repositoryName = getRepositoryName();

        ContainerRegistryAsyncClient anonymousClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .buildAsyncClient();

        // BEGIN: readme-sample-anonymousAsyncClientThrows
        anonymousClient.deleteRepository(repositoryName)
            .doOnSuccess(
                ignored -> System.out.println("Unexpected Success: Delete is not allowed on anonymous access!"))
            .doOnError(
                error -> error instanceof ClientAuthenticationException,
                error -> System.out.println("Expected exception: Delete is not allowed on anonymous access"));
        // END: readme-sample-anonymousAsyncClientThrows
    }

    public void enableHttpLogging() {
        final String endpoint = getEndpoint();
        final String repositoryName = getRepositoryName();

        final TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
        // BEGIN: readme-sample-enablehttplogging
        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(defaultCredential)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: readme-sample-enablehttplogging
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
        // BEGIN: readme-sample-nationalCloudSample
        ContainerRegistryClient containerRegistryClient = new ContainerRegistryClientBuilder()
            .endpoint(getEndpoint())
            .credential(credentials)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_CHINA)
            .buildClient();

        containerRegistryClient
            .listRepositoryNames()
            .forEach(name -> System.out.println(name));
        // END: readme-sample-nationalCloudSample
    }
}

