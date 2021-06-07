// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

/**
 * This is a sample for listing tags asynchronously.
 */
public class ListTagsAsync {
    static final String ENDPOINT = "https://registryName.azure.io";
    static final String REPOSITORY_NAME = "library/hello-world";
    static final String DIGEST = "digest";

    public static void main(String[] args) {
        ContainerRegistryAsyncClient anonymousClient = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .buildAsyncClient();

        RegistryArtifactAsync image = anonymousClient.getArtifact(REPOSITORY_NAME, DIGEST);

        System.out.printf(String.format("%s has the following aliases:", image.getFullyQualifiedReference()));

        image.listTagProperties().subscribe(tag -> {
            System.out.printf(String.format("%s/%s:%s", anonymousClient.getEndpoint(), ENDPOINT, tag.getName()));
        }, error -> {
            System.out.println("There was an error while trying to list tags" + error);
        });
    }
}
