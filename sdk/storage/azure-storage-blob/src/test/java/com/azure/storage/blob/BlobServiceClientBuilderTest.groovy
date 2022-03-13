// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import spock.lang.Specification

class BlobServiceClientBuilderTest extends Specification {
    def "Create Blob Service client with endpoint"() {
        when:
        def client = new BlobServiceClientBuilder()
            .endpoint("https://127.0.0.1:10000")
            .buildClient()

        then:
        client.getAccountUrl() == "https://127.0.0.1:10000/"
    }
    
    def "Create Blob Service client with connection string"() {
        when:
        def client = new BlobServiceClientBuilder()
            .connectionString("DefaultEndpointsProtocol=https;AccountName=test;AccountKey=test;EndpointSuffix=ignored;BlobEndpoint=https://127.0.0.1:10000")
            .buildClient()

        then:
        client.getAccountUrl() == "https://127.0.0.1:10000/"
    }
}
