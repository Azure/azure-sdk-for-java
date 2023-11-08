// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This is a sample for listing repository names asynchronously.
 */
public class ListRepositoryNamesAsync {
    static final String ENDPOINT = "https://registryName.azure.io";

    public static void main(String[] args) {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(credential)
            .buildAsyncClient();

        client.listRepositoryNames().subscribe(repository -> System.out.println(repository));
    }
}
