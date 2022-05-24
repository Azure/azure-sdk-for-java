// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;

import java.util.Objects;
import java.util.UUID;

/**
 * A set of AMQP client options.
 */
public final class AmqpClientOptions extends ClientOptions {
    private String identifier = UUID.randomUUID().toString();

    /** {@inheritDoc} **/
    @Override
    public ClientOptions setApplicationId(String applicationId) {
        super.setApplicationId(applicationId);
        return this;
    }

    /** {@inheritDoc} **/
    @Override
    public ClientOptions setHeaders(Iterable<Header> headers) {
        super.setHeaders(headers);
        return this;
    }

    /**
     * Gets the identifier for the amqp client.
     * @return Amqp client identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier for the amqp client.
     * @param identifier a specific string to identify amqp client.
     * @return The updated {@link AmqpRetryOptions} object.
     */
    public AmqpClientOptions setIdentifier(String identifier) {
        this.identifier = Objects.requireNonNull(identifier, "'identifier' cannot be null.");
        return this;
    }
}
