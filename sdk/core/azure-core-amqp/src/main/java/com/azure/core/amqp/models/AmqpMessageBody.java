// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class encapsulates the body of a message. The {@link AmqpBodyType} map to AMQP specification message body types.
 * Current implementation only support {@link AmqpBodyType#DATA DATA}. Other types will be supported in future releases.
 * Client should test for {@link AmqpBodyType} before calling corresponding get method.
 * Get methods not corresponding to the type of the body throw exception.
 *
 * @see AmqpBodyType
 */
public final class AmqpMessageBody {
    private static final ClientLogger LOGGER = new ClientLogger(AmqpMessageBody.class);
    private AmqpBodyType bodyType;

    private byte[] data;

    /**
     * Creates instance of {@link AmqpMessageBody} with given {@link Iterable} of byte array. Please note that this
     * version of the SDK supports only one element in given {@link Iterable}.
     *
     * @param data used to create another instance of {@link AmqpMessageBody}.
     *
     * @return AmqpMessageBody Newly created instance.
     *
     * @throws NullPointerException if {@code data} is null.
     *
     * @throws IllegalArgumentException if size of 'data' is zero or greater than one.
     */
    public static AmqpMessageBody fromData(Iterable<byte[]> data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        byte[] payload = null;
        for (byte[] binaryData : data) {
            if (payload != null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Only one instance of byte array is allowed in 'data'."));
            }
            payload = binaryData;
        }

        if (payload == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'data' can not be empty."));
        }

        AmqpMessageBody body = new AmqpMessageBody();
        body.bodyType = AmqpBodyType.DATA;
        body.data = payload;
        return body;
    }

    /**
     * Creates instance of {@link AmqpMessageBody} with given byte array.
     *
     * @param data used to create another instance of {@link AmqpMessageBody}.
     *
     * @return AmqpMessageBody Newly created instance.
     *
     * @throws NullPointerException if {@code data} is null.
     */
    public static AmqpMessageBody fromData(byte[] data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        AmqpMessageBody body = new AmqpMessageBody();
        body.bodyType = AmqpBodyType.DATA;
        body.data = data;
        return body;
    }

    /**
     * Gets the {@link AmqpBodyType} of the message.
     *
     * @return AmqpBodyType type of the message.
     */
    public AmqpBodyType getBodyType() {
        return bodyType;
    }

    /**
     * Gets an immutable list containing only first byte array set on this {@link AmqpMessageBody}.
     *
     * @return data set on {@link AmqpMessageBody}.
     *
     * @throws IllegalArgumentException If {@link AmqpBodyType} is not {@link AmqpBodyType#DATA DATA}.
     */
    public List<byte[]> getData() {
        if (bodyType != AmqpBodyType.DATA) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format("Can not return data for a "
                + "message which is of type %s.", getBodyType().toString())));
        }
        return Collections.singletonList(data);
    }
}
