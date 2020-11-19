// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class encapsulates the body of a message. The {@link AmqpMessageBodyType} map to an AMQP specification message
 * body types. Current implementation only support {@link AmqpMessageBodyType#DATA DATA} AMQP data type. Track this
 * <a href="https://github.com/Azure/azure-sdk-for-java/issues/17614" target="_blank">issue</a> to find out support for
 * other AMQP types.
 *
 * <p><b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method. Get methods not
 * corresponding to the type of the body throws exception.</b></p>
 *
 * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
 * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
 *
 * @see AmqpMessageBodyType
 */
public final class AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpMessageBody.class);
    private AmqpMessageBodyType bodyType;

    // We expect user to call `getFirstData()` more because we support one byte[] as present.
    // This the priority here to store payload as `byte[] data` and
    private byte[] data;
    private List<byte[]> dataList;

    private AmqpMessageBody() {
        // private constructor so no one outside can create instance of this except classes im this package.
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
     * Gets an {@link IterableStream} of byte array containing only first byte array set on this
     * {@link AmqpMessageBody}. This library only support one byte array at present, so the returned list will have only
     *  one element.
     * <p><b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method. Get methods not
     * corresponding to the type of the body throws exception.</b></p>
     *
     * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
     * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
     * @return data set on {@link AmqpMessageBody}.
     *
     * @throws IllegalArgumentException If {@link AmqpMessageBodyType} is not {@link AmqpMessageBodyType#DATA DATA}.
     */
    public IterableStream<byte[]> getData() {
        if (bodyType != AmqpMessageBodyType.DATA) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "This method can only be called for AMQP Data body type at present. Track this issue, "
                    + "https://github.com/Azure/azure-sdk-for-java/issues/17614 for other body type support in "
                    + "future."));
        }
        if (dataList ==  null) {
            dataList = Collections.singletonList(data);
        }
        return new IterableStream<>(dataList);
    }

    /**
     * Gets first byte array set on this {@link AmqpMessageBody}. This library only support one byte array on Amqp
     * Message at present.
     * <p><b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method. Get methods not
     * corresponding to the type of the body throws exception.</b></p>
     *
     * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
     * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
     * @return data set on {@link AmqpMessageBody}.
     *
     * @throws IllegalArgumentException If {@link AmqpMessageBodyType} is not {@link AmqpMessageBodyType#DATA DATA}.
     * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format" target="_blank">
     *     Amqp Message Format.</a>
     */
    public byte[] getFirstData() {
        if (bodyType != AmqpMessageBodyType.DATA) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "This method can only be called for AMQP Data body type at present. Track this issue, "
                    + "https://github.com/Azure/azure-sdk-for-java/issues/17614 for other body type support in "
                    + "future."));
        }
        return data;
    }
}
