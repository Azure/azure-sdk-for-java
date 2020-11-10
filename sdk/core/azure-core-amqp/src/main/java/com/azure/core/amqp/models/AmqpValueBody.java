// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents amqp value body type.
 */
public final class AmqpValueBody implements AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpValueBody.class);
    private final Object value;

    /**
     * Creates {@link AmqpValueBody} with given value.
     * @param value to use.
     * @throws NullPointerException is {@code value} is {@code null}.
     */
    public AmqpValueBody(Object value) {
        this.value = Objects.requireNonNull(value, "'value' cannot be null.");
    }

    @Override
    public AmqpBodyType getBodyType() {
        return AmqpBodyType.VALUE;
    }

    /**
     * Gets the value set on this {@link AmqpValueBody}.
     * @return data set on {@link AmqpValueBody}.
     */
    public Object getValue() {
        return value;
    }
}
