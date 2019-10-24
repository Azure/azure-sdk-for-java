// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.internal.RxDocumentClientUnderTest;
import com.azure.cosmos.internal.directconnectivity.ReflectionUtils;
import com.azure.cosmos.internal.RxDocumentClientUnderTest;
import com.azure.cosmos.internal.directconnectivity.ReflectionUtils;

import java.net.URI;
import java.net.URISyntaxException;

public class ClientUnderTestBuilder extends CosmosClientBuilder {

    public ClientUnderTestBuilder(CosmosClientBuilder builder) {
        this.configs(builder.configs());
        this.setConnectionPolicy(builder.getConnectionPolicy());
        this.setConsistencyLevel(builder.getConsistencyLevel());
        this.setKey(builder.getKey());
        this.setEndpoint(builder.getEndpoint());
        this.setCosmosKeyCredential(builder.getCosmosKeyCredential());
    }

    @Override
    public CosmosAsyncClient buildAsyncClient() {
        RxDocumentClientUnderTest rxClient;
        try {
            rxClient = new RxDocumentClientUnderTest(
                new URI(this.getEndpoint()),
                this.getKey(),
                this.getConnectionPolicy(),
                this.getConsistencyLevel(),
                this.configs(),
                this.getCosmosKeyCredential());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        CosmosAsyncClient cosmosAsyncClient = super.buildAsyncClient();
        ReflectionUtils.setAsyncDocumentClient(cosmosAsyncClient, rxClient);
        return cosmosAsyncClient;
    }
}
