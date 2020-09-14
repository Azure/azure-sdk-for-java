// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * Interface representing Amqp Message Body.
 */
public interface AmqpMessageBody {
    /**
     * Type representing various supported amqp body types.
     *
     * @return The {@link AmqpBodyType}.
     */
    AmqpBodyType getBodyType();
}
