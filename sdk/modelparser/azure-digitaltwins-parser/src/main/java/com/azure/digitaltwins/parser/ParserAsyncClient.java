// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;

/**
 * This class provides a client for interacting with Azure DigitalTwins Model Parser
 * This client is instantiated through {@link ParserClientBuilder}.
 */
@ServiceClient(builder = ParserClientBuilder.class, isAsync = true)
public final class ParserAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ParserAsyncClient.class);
    private static final String DIGITALTWINS_MODEL_PARSER_TRACING_NAMESPACE_VALUE = "Azure.DigitalTwins.Parser";
    private final ParserServiceVersion serviceVersion;

    ParserAsyncClient(
        ParserServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    /**
     * Gets the Models Repository service API version that this client is configured to use for all service requests.
     * Unless configured while building this client through {@link ParserClientBuilder#serviceVersion(ParserServiceVersion)},
     * this value will be equal to the latest service API version supported by this client.
     *
     * @return The ModelsRepository service API version.
     */
    public ParserServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }
}
