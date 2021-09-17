// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlobServiceClientBuilderTest {
    @Test
    public void createBlockClientWithEndpoint() {
        final BlobServiceClient client = new BlobServiceClientBuilder()
            .endpoint("https://127.0.0.1:10000")
            .buildClient();
        assertEquals("https://127.0.0.1:10000/", client.getAccountUrl());
    }
    
    @Test
    public void createBlockClientWithConnectionString() {
        final BlobServiceClient client = new BlobServiceClientBuilder()
            .connectionString("DefaultEndpointsProtocol=https;AccountName=test;AccountKey=test;EndpointSuffix=ignored;BlobEndpoint=https://127.0.0.1:10000")
            .buildClient();
        assertEquals("https://127.0.0.1:10000/", client.getAccountUrl());
    }
}
