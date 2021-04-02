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

    // optional/have default values
    private ParserServiceVersion serviceVersion;

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
        // Set defaults for these fields if they were not set while building the client
        ParserServiceVersion serviceVersion = this.serviceVersion;
        if (serviceVersion == null) {
            serviceVersion = ParserServiceVersion.getLatest();
        }

        return new ParserAsyncClient(serviceVersion);
    }

    /**
     * Sets the {@link ParserServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param serviceVersion The service API version to use.
     * @return the updated {@link ParserClientBuilder} instance for fluent building.
     */
    public ParserClientBuilder serviceVersion(ParserServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }
}
