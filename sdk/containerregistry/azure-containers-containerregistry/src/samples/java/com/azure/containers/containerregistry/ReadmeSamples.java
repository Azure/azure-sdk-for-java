// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.core.exception.HttpResponseException;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private String endpoint = "endpoint";

    public void createClient() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();
    }

    public void createAsyncClient() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();
    }

    public void listRepositoryNamesSample() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();

        client.listRepositoryNames().forEach(repository -> System.out.println(repository));
    }

    public void listRepositoryNamesAsyncSample() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        client.listRepositoryNames().subscribe(repository -> System.out.println(repository));
    }

    private String repository = "repository";

    public void getPropertiesThrows() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRepository containerRepository = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient()
            .getRepository(repository);
        try {
            containerRepository.getProperties();
        } catch (HttpResponseException exception) {
            // Do something with the exception.
        }
    }
}

