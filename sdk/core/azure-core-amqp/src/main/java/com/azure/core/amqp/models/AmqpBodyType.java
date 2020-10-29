// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * All {@link AmqpBodyType} available for AMQP Message.
 */
public enum AmqpBodyType {
    /**
     * When message content is byte array, equivalent to AMQP Data.
     */
    DATA,

    /**
     * When message content is a list of objects, equivalent to AMQP Sequence. Each object must be of a type supported
     * by AMQP.
     */
    VALUE,

    /**
     * When message content is a single object, equivalent to AMQP Value. The object must be of a type supported by
     * AMQP.
     */
    SEQUENCE;

}
