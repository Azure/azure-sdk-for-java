// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * All AmqpBodyType available for AMQP Message.
 */
public enum AmqpBodyType {
    /**
     * Message content is byte array, equivalent to AMQP Data.
     */
    DATA,
    /**
     * Message content is a list of objects, equivalent to AMQP Sequence. Each object must be of a type supported
     * by AMQP.
     */
    SEQUENCE,
    /**
     * Message content is a single object, equivalent to AMQP Value. The object must be of a type supported by AMQP.
     */
    VALUE

}
