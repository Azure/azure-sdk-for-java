// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.logging.ClientLogger;

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
 * <b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method.Get methods not
 * corresponding to the type of the body throws exception.</b>
 * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
 * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
 *
 * @see AmqpMessageBodyType
 */
public final class AmqpMessageBody {
    private static final ClientLogger LOGGER = new ClientLogger(AmqpMessageBody.class);
    private AmqpMessageBodyType bodyType;

    private byte[] data;

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
        body.data = data;
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
     * Gets an immutable list containing only first byte array set on this {@link AmqpMessageBody}. The proton-j
     * library used only support one byte array, so the returned list will have only one element. Look for future
     * releases where we will support multiple byte array.
     * <b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method.Get methods not
     * corresponding to the type of the body throws exception.</b>
     * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
     * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
     * @return data set on {@link AmqpMessageBody}.
     *
     * @throws IllegalArgumentException If {@link AmqpMessageBodyType} is not {@link AmqpMessageBodyType#DATA DATA}.
     */
    public List<byte[]> getData() {
        if (bodyType != AmqpMessageBodyType.DATA) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format("Can not return data for a "
                + "message which is of type %s.", getBodyType().toString())));
        }
        return Collections.singletonList(data);
    }
}
