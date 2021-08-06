// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestOrderBy;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This is a sample for deleting images asynchronously.
 */
public class DeleteImagesAsync {
    static final String ENDPOINT = "https://registryName.azure.io";

    public static void main(String[] args) {
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(defaultCredential)
            .audience(ContainerRegistryAudience.AZURERESOURCEMANAGERPUBLICCLOUD)
            .buildAsyncClient();

        final int imagesCountToKeep = 3;
        client.listRepositoryNames()
            .map(repositoryName -> client.getRepository(repositoryName))
            .flatMap(repository -> repository.listManifestProperties(ArtifactManifestOrderBy.LAST_UPDATED_ON_DESCENDING))
            .skip(imagesCountToKeep)
            .subscribe(imageManifest -> {
                System.out.printf(String.format("Deleting image with digest %s.%n", imageManifest.getDigest()));
                System.out.printf("    This image has the following tags: ");

                for (String tagName : imageManifest.getTags()) {
                    System.out.printf("        %s:%s", imageManifest.getRepositoryName(), tagName);
                }

                client.getArtifact(imageManifest.getRepositoryName(), imageManifest.getDigest()).delete().subscribe();
            }, error -> {
                System.out.println("Failed to delete older images.");
            });
    }
}
