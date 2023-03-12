// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This is a sample for updating registry artifact properties asynchronously.
 */
public class UpdateRegistryArtifactPropertiesAsync {
    static final String ENDPOINT = "https://registryName.azure.io";
    static final String REPOSITORY_NAME = "library/hello-world";
    static final String TAG = "latest";
    static final String DIGEST = "digest";

    public static void main(String[] args) {
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(defaultCredential)
            .buildAsyncClient();

        RegistryArtifactAsync image = client.getArtifact(REPOSITORY_NAME, DIGEST);

        image.updateTagProperties(TAG, new ArtifactTagProperties()
            .setWriteEnabled(false)
            .setDeleteEnabled(false)).subscribe(artifactTagProperties -> {
                System.out.println("Tag properties are now read-only");
            }, error -> {
                System.out.println("Failed to make the tag properties read-only.");
            });
    }
}
