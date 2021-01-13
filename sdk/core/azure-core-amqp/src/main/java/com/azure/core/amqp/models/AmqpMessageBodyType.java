// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * Represents all valid {@link AmqpMessageBodyType} for an AMQP Message. Current SDK only support
 * {@link AmqpMessageBodyType#DATA DATA} AMQP data type. Track this <a href="https://github.com/Azure/azure-sdk-for-java/issues/17614" target="_blank">issue</a>
 * to find out support for other AMQP types.
 *
 * <p><b>Types of Amqp message body</b></p>
 * <ul>
 * <li><a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-data" target="_blank">DATA</a></li>
 * <li><a href="http://docs.oasis-open.org/amqp/core/v1.0/amqp-core-messaging-v1.0.html#type-amqp-sequence" target="_blank">SEQUENCE</a></li>
 * <li><a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-amqp-value" target="_blank">VALUE</a></li>
 * </ul>

 */
public enum AmqpMessageBodyType {
    /**
     * Message content is byte array, equivalent to AMQP Data.
     */
    DATA,
    /**
     * Message content is a single object, equivalent to AMQP Value. The object must be of a type supported by AMQP.
     */
    VALUE,
    /**
     * Message content is a list of objects, equivalent to AMQP Sequence. Each object must be of a type supported
     * by AMQP.
     */
    SEQUENCE
}
