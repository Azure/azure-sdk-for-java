// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.core.annotation.ServiceClient;

/**
 * This class provides a client for interacting with Azure DigitalTwins Model Parser
 * This client is instantiated through {@link ParserClientBuilder}.
 */
@ServiceClient(builder = ParserClientBuilder.class)
public final class ParserClient {
    private final ParserAsyncClient parserAsyncClient;

    ParserClient(ParserAsyncClient parserAsyncClient) {
        this.parserAsyncClient = parserAsyncClient;
    }

    /**
     * Gets the Azure Models Repository service API version that this client is configured to use for all service requests.
     * Unless configured while building this client through {@link ParserClientBuilder#serviceVersion(ParserServiceVersion)},
     * this value will be equal to the latest service API version supported by this client.
     *
     * @return The Azure Models Repository service API version.
     */
    public ParserServiceVersion getServiceVersion() {
        return this.parserAsyncClient.getServiceVersion();
    }
}
