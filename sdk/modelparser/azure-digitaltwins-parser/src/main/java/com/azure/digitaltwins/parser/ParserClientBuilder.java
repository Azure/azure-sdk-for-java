// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.core.annotation.ServiceClientBuilder;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link ParserClient}
 * and {@link ParserAsyncClient}, call {@link #buildClient() buildClient} and {@link
 * #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 */
@ServiceClientBuilder(serviceClients = {ParserClient.class, ParserAsyncClient.class})
public final class ParserClientBuilder {
    /**
     * The public constructor for {@link ParserClientBuilder}
     */
    public ParserClientBuilder() {
    }

    /**
     * Create a {@link ParserClient} based on the builder settings.
     *
     * @return the created synchronous {@link ParserClient}
     */
    public ParserClient buildClient() {
        return new ParserClient(buildAsyncClient());
    }

    /**
     * Create a {@link ParserAsyncClient} based on the builder settings.
     *
     * @return the created asynchronous {@link ParserAsyncClient}
     */
    public ParserAsyncClient buildAsyncClient() {
        return new ParserAsyncClient();
    }
}
