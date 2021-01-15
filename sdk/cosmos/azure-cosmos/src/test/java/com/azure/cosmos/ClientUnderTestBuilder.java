// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.RxDocumentClientUnderTest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;

import java.net.URI;
import java.net.URISyntaxException;

public class ClientUnderTestBuilder extends CosmosClientBuilder {

    public ClientUnderTestBuilder(CosmosClientBuilder builder) {
        if (!Strings.isNullOrEmpty(builder.getKey())) {
            this.key(builder.getKey());
        }

        if (!Strings.isNullOrEmpty(builder.getEndpoint())) {
            this.endpoint(builder.getEndpoint());
        }

        if (builder.getCredential() != null) {
            this.credential(builder.getCredential());
        }

        this.configs(builder.configs());
        this.gatewayMode(builder.getGatewayConnectionConfig());
        this.directMode(builder.getDirectConnectionConfig());
        this.consistencyLevel(builder.getConsistencyLevel());
        this.contentResponseOnWriteEnabled(builder.isContentResponseOnWriteEnabled());
        this.userAgentSuffix(builder.getUserAgentSuffix());
        this.throttlingRetryOptions(builder.getThrottlingRetryOptions());
        this.preferredRegions(builder.getPreferredRegions());
        this.endpointDiscoveryEnabled(builder.isEndpointDiscoveryEnabled());
        this.multipleWriteRegionsEnabled(builder.isMultipleWriteRegionsEnabled());
        this.readRequestsFallbackEnabled(builder.isReadRequestsFallbackEnabled());
        this.clientTelemetryEnabled(builder.isClientTelemetryEnabled());
    }

    @Override
    public CosmosAsyncClient buildAsyncClient() {
        CosmosAsyncClient cosmosAsyncClient = super.buildAsyncClient();
        RxDocumentClientUnderTest rxClient;
        try {
            rxClient = new RxDocumentClientUnderTest(
                new URI(this.getEndpoint()),
                this.getKey(),
                this.getConnectionPolicy(),
                this.getConsistencyLevel(),
                this.configs(),
                this.getCredential(),
                this.isContentResponseOnWriteEnabled());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        ReflectionUtils.setAsyncDocumentClient(cosmosAsyncClient, rxClient);
        return cosmosAsyncClient;
    }
}
