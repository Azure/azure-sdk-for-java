// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RxDocumentClientUnderTest;
import com.azure.data.cosmos.internal.directconnectivity.ReflectionUtils;

import java.net.URI;
import java.net.URISyntaxException;

public class ClientUnderTestBuilder extends CosmosClientBuilder {

    public ClientUnderTestBuilder(CosmosClientBuilder builder) {
        this.configs(builder.configs());
        this.connectionPolicy(builder.connectionPolicy());
        this.consistencyLevel(builder.consistencyLevel());
        this.key(builder.key());
        this.endpoint(builder.endpoint());
        this.cosmosKeyCredential(builder.cosmosKeyCredential());
    }

    @Override
    public CosmosClient build() {
        RxDocumentClientUnderTest rxClient;
        try {
            rxClient = new RxDocumentClientUnderTest(
                new URI(this.endpoint()),
                this.key(),
                this.connectionPolicy(),
                this.consistencyLevel(),
                this.configs(),
                this.cosmosKeyCredential());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        CosmosClient cosmosClient = super.build();
        ReflectionUtils.setAsyncDocumentClient(cosmosClient, rxClient);
        return cosmosClient;
    }
}
