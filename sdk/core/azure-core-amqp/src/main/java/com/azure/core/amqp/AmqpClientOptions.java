// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;

/**
 * A set of AMQP client options.
 */
public final class AmqpClientOptions extends ClientOptions {
    private String identifier;

    /** {@inheritDoc} **/
    @Override
    public AmqpClientOptions setApplicationId(String applicationId) {
        super.setApplicationId(applicationId);
        return this;
    }

    /** {@inheritDoc} **/
    @Override
    public AmqpClientOptions setHeaders(Iterable<Header> headers) {
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
     * @return The updated {@link AmqpClientOptions} object.
     */
    public AmqpClientOptions setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }
}
