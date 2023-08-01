// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.core.exception.ClientAuthenticationException;

/**
 * This class returns a sample for the anonymous access client.
 */
public class AnonymousAsyncClientThrows {

    static final String ENDPOINT = "https://registryName.azure.io";
    static final String REPOSITORY_NAME = "library/hello-world";

    public static void main(String[] args) {
        ContainerRegistryAsyncClient anonymousClient = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .buildAsyncClient();

        anonymousClient.deleteRepository(REPOSITORY_NAME).subscribe(deleteRepositoryResult -> {
            System.out.println("Unexpected Success: Delete is not allowed on anonymous access");
        }, error -> {
            if (error instanceof ClientAuthenticationException) {
                System.out.println("Expected exception: Delete is not allowed on anonymous access");
            } else {
                System.out.println("Unexpected exception.");
            }
        });
    }
}
