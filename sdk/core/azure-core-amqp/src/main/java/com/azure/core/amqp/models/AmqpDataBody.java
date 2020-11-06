// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This represent amqp payload of {@link AmqpBodyType#DATA} type.
 */
public final class AmqpDataBody implements AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpDataBody.class);
    private final byte[] data;

    /**
     * Creates instance of {@link AmqpDataBody} with given {@link Iterable} of byte array. Please note that this version
     * of the SDK supports only one element in given {@link Iterable}.
     *
     * @param data to be set on amqp body.
     *
     * @throws NullPointerException if {@code data} is null.
     * @throws IllegalArgumentException if size of 'data' is zero or greater than one.
     */
    public AmqpDataBody(Iterable<byte[]> data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        byte[] payload = null;
        for (byte[] binaryData : data) {
            if (payload != null) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Only one instance of byte array is allowed in 'data'."));
            }
            payload = binaryData;
        }

        if (payload == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'data' can not be empty."));
        } else {
            this.data = payload;
        }
    }

    /**
     * Creates instance of {@link AmqpDataBody} with given byte array.
     *
     * @param data to be set on amqp body.
     *
     * @throws NullPointerException if {@code data} is null.
     */
    public AmqpDataBody(byte[] data) {
        this.data =Objects.requireNonNull(data, "'data' cannot be null.");
    }

    /**
     * Creates instance of {@link AmqpDataBody} with given {@link AmqpDataBody} instance.
     *
     * @param data used to create another instance of {@link AmqpDataBody}.
     *
     * @throws NullPointerException if {@code data} or {@link AmqpDataBody#getData() body} is null.
     */
    AmqpDataBody(AmqpDataBody data) {
        this(Objects.requireNonNull(data, "'data' cannot be null.").getData());
    }

    @Override
    public AmqpBodyType getBodyType() {
        return AmqpBodyType.DATA;
    }

    /**
     * Gets an immutable list containing only of first byte array set on this {@link AmqpDataBody}.
     * @return data set on {@link AmqpDataBody}.
     */
    public List<byte[]> getData() {
        return Collections.singletonList(data);
    }
}
