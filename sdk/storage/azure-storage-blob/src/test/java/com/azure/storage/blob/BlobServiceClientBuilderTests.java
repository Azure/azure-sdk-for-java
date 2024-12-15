// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.test.http.NoOpHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlobServiceClientBuilderTests {

    @Test
    public void createBlobServiceClientWithEndpoint() {
        BlobServiceClient client = new BlobServiceClientBuilder().endpoint("https://127.0.0.1:10000")
            .httpClient(new NoOpHttpClient())
            .buildClient();

        assertEquals(client.getAccountUrl(), "https://127.0.0.1:10000/");
    }

    @Test
    public void createBlobServiceClientWithConnectionString() {
        BlobServiceClient client = new BlobServiceClientBuilder().connectionString(
            "DefaultEndpointsProtocol=https;AccountName=test;AccountKey=test;EndpointSuffix=ignored;BlobEndpoint=https://127.0.0.1:10000")
            .httpClient(new NoOpHttpClient())
            .buildClient();

        assertEquals(client.getAccountUrl(), "https://127.0.0.1:10000/");
    }
}
