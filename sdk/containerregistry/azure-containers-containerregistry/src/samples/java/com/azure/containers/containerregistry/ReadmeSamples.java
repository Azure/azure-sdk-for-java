// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.ArtifactManifestOrder;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.containers.containerregistry.models.GetManifestResult;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
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
    private String repository = "samples/nginx";

    public void createClient() {
        // BEGIN: readme-sample-createClient
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();
        // END: readme-sample-createClient
    }

    public void createContentClient() {
        // BEGIN: readme-sample-createContentClient
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .repositoryName(repository)
            .buildClient();
        // END: readme-sample-createContentClient
    }

    public void createAsyncClient() {
        // BEGIN: readme-sample-createAsyncClient
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryAsyncClient registryClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();
        // END: readme-sample-createAsyncClient
    }

    public void createBlobAsyncClient() {
        // BEGIN: readme-sample-createBlobAsyncClient
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryContentAsyncClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .repositoryName(repository)
            .buildAsyncClient();
        // END: readme-sample-createBlobAsyncClient
    }


    public void listRepositoryNamesSample() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();

        // BEGIN: readme-sample-listRepositoryNames
        registryClient.listRepositoryNames().forEach(repository -> System.out.println(repository));
        // END: readme-sample-listRepositoryNames
    }

    private final String repositoryName = "repository";

    public void getPropertiesThrows() {
        // BEGIN: readme-sample-getProperties
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRepository containerRepository = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
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
        ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .buildClient();
        // END: readme-sample-createAnonymousAccessClient
    }

    public void createAnonymousAccessAsyncClient() {
        // BEGIN: readme-sample-createAnonymousAsyncAccessClient
        ContainerRegistryAsyncClient registryClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .buildAsyncClient();
        // END: readme-sample-createAnonymousAsyncAccessClient
    }

    public void deleteImages() {
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(defaultCredential)
            .buildClient();

        // BEGIN: readme-sample-deleteImages
        final int imagesCountToKeep = 3;
        for (String repositoryName : registryClient.listRepositoryNames()) {
            final ContainerRepository repository = registryClient.getRepository(repositoryName);

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
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(defaultCredential)
            .buildClient();

        // BEGIN: readme-sample-setArtifactProperties
        RegistryArtifact image = registryClient.getArtifact(repositoryName, digest);

        image.updateTagProperties(
            tag,
            new ArtifactTagProperties()
                .setWriteEnabled(false)
                .setDeleteEnabled(false));
        // END: readme-sample-setArtifactProperties
    }

    private final String digest = "digest";

    public void listTagProperties() {
        ContainerRegistryClient anonymousClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .buildClient();

        // BEGIN: readme-sample-listTagProperties
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

        final TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
        // BEGIN: readme-sample-enablehttplogging
        ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
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

    private final TokenCredential credential = null;
    public void nationalCloudSample() {
        // BEGIN: readme-sample-armTokenChina
        ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
            .endpoint(getEndpoint())
            .credential(credential)
            // only if ACR access tokens are disabled or not supported
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_CHINA)
            .buildClient();

        registryClient
            .listRepositoryNames()
            .forEach(name -> System.out.println(name));
        // END: readme-sample-armTokenChina
    }

    public void armTokenSample() {
        // BEGIN: readme-sample-armTokenPublic
        ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
            .endpoint(getEndpoint())
            .credential(credential)
            .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
            .buildClient();

        registryClient
            .listRepositoryNames()
            .forEach(name -> System.out.println(name));
        // END: readme-sample-armTokenPublic
    }

    public void deleteBlob()  {
        ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(getEndpoint())
            .repositoryName(repository)
            .credential(credential)
            .buildClient();

        // BEGIN: readme-sample-deleteBlob
        GetManifestResult manifestResult = contentClient.getManifest("latest");

        OciImageManifest manifest = manifestResult.getManifest().toObject(OciImageManifest.class);
        for (OciDescriptor layer : manifest.getLayers()) {
            contentClient.deleteBlob(layer.getDigest());
        }
        // END: readme-sample-deleteBlob
    }

    public void deleteManifest()  {
        ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(getEndpoint())
            .repositoryName(repository)
            .credential(credential)
            .buildClient();

        // BEGIN: readme-sample-deleteManifest
        GetManifestResult manifestResult = contentClient.getManifest("latest");
        contentClient.deleteManifest(manifestResult.getDigest());
        // END: readme-sample-deleteManifest
    }

    public void deleteBlobAsync()  {
        ContainerRegistryContentAsyncClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(getEndpoint())
            .repositoryName(repository)
            .credential(credential)
            .buildAsyncClient();

        // BEGIN: readme-sample-deleteBlobAsync
        contentClient.getManifest("latest")
            .flatMap(manifest -> contentClient.deleteBlob(manifest.getDigest()))
            .block();
        // END: readme-sample-deleteBlobAsync
    }

    public void deleteManifestAsync()  {
        ContainerRegistryContentAsyncClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(getEndpoint())
            .repositoryName(repository)
            .credential(credential)
            .buildAsyncClient();

        // BEGIN: readme-sample-deleteManifestAsync
        contentClient.getManifest("latest")
            .flatMap(manifest -> contentClient.deleteManifest(manifest.getDigest()))
            .block();
        // END: readme-sample-deleteManifestAsync
    }
}

