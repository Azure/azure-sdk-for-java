// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class encapsulates the body of a message. The {@link AmqpMessageBodyType} map to AMQP specification message body
 * types. Current implementation only support {@link AmqpMessageBodyType#DATA DATA}. Other types will be supported in
 * future releases.
 * <b>Amqp message body types:</b>
 * <ul>
 * <li><a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-data">DATA</a></li>
 * <li><a href="http://docs.oasis-open.org/amqp/core/v1.0/amqp-core-messaging-v1.0.html#type-amqp-sequence">SEQUENCE</a></li>
 * <li><a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-amqp-value">VALUE</a></li>
 * </ul>
 *
 * <b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method.Get methods not
 * corresponding to the type of the body throws exception.</b>
 *
 * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
 * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
 *
 * @see AmqpMessageBodyType
 */
public final class AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpMessageBody.class);
    private AmqpMessageBodyType bodyType;

    private List<byte[]> data;

    AmqpMessageBody() {
        // package constructor so no one can create instance of this except classes im this package.
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
        body.bodyType = AmqpMessageBodyType.DATA;
        body.data = Collections.singletonList(Arrays.copyOf(data, data.length));
        return body;
    }

    /**
     * Gets the {@link AmqpMessageBodyType} of the message.
     * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
     * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
     * @return AmqpBodyType type of the message.
     */
    public AmqpMessageBodyType getBodyType() {
        return bodyType;
    }

    /**
     * Gets an {@link IterableStream} of byte array containing only first byte array set on this
     * {@link AmqpMessageBody}. This library only support one byte array, so the returned list will have only one
     * element. Look for future releases where we will support multiple byte array.
     *
     * <b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method.Get methods not
     * corresponding to the type of the body throws exception.</b>
     *
     * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
     * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
     * @return data set on {@link AmqpMessageBody}.
     *
     * @throws IllegalArgumentException If {@link AmqpMessageBodyType} is not {@link AmqpMessageBodyType#DATA DATA}.
     */
    public IterableStream<byte[]> getData() {
        if (bodyType != AmqpMessageBodyType.DATA) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format("Cannot return data for a "
                + "message which is of type %s.", getBodyType().toString())));
        }

        return new IterableStream<>(data);
    }

    /**
     * Gets first byte array set on this {@link AmqpMessageBody}. This library only support one byte array on Amqp
     * Message. Look for future releases where we will support multiple byte array and you can use
     * {@link AmqpMessageBody#getData()} API.
     * <b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method.Get methods not
     * corresponding to the type of the body throws exception.</b>
     * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
     * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
     * @return data set on {@link AmqpMessageBody}.
     *
     * @throws IllegalArgumentException If {@link AmqpMessageBodyType} is not {@link AmqpMessageBodyType#DATA DATA}.
     * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format">
     *     Amqp Message Format.</a>
     */
    public byte[] getFirstData() {
        if (bodyType != AmqpMessageBodyType.DATA) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format("Cannot return data for a "
                + "message which is of type %s.", getBodyType().toString())));
        }

        return data.get(0);
    }
}
