// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.util.CoreUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * A set of AMQP client options.
 */
public class AmqpClientOptions {
    private String identifier;

    /**
     * Gets the identifier for the amqp client.
     * @return Amqp client identifier.
     */
    public String getIdentifier() {
        return CoreUtils.isNullOrEmpty(identifier) ? UUID.randomUUID().toString() : identifier;
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
