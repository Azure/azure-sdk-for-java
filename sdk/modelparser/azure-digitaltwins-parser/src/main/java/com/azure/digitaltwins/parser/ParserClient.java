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
    ParserClient(ParserAsyncClient parserAsyncClient) {
    }
}
