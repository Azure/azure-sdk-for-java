// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * This class encapsulates the body of a message. The {@link AmqpMessageBodyType} map to an AMQP specification message
 * body types. Current implementation support {@link AmqpMessageBodyType#DATA DATA} AMQP data type.
 *
 * <p><b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method. Get methods not
 * corresponding to the type of the body throws exception.</b></p>
 *
 * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
 * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
 *
 * @see AmqpMessageBodyType
 * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-types-v1.0-os.html#section-primitive-type-definitions" target="_blank">
 *     Amqp primitive data type.</a>
 * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format" target="_blank">
 *     Amqp message format.</a>
 *
 */
public final class AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpMessageBody.class);
    private AmqpMessageBodyType bodyType;

    // We expect user to call `getFirstData()` more because we support one byte[] as present.
    // This the priority here to store payload as `byte[] data` and
    private byte[] data;
    private List<byte[]> dataList;
    private Object value;
    private List<Object> sequence;

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
     * Creates an instance of {@link AmqpMessageBody} with the given {@link List sequence}. It supports only one
     * {@code sequence} at present.
     *
     * @param sequence used to create an instance of {@link AmqpMessageBody}. A sequence can be {@link List} of
     * {@link Object objects}. The {@link Object object} can be any of the AMQP supported primitive data type.
     *
     * @return newly created instance of {@link AmqpMessageBody}.
     *
     * @throws NullPointerException if {@code sequence} is null.
     * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-types-v1.0-os.html#section-primitive-type-definitions" target="_blank">
     *     Amqp primitive data type.</a>
     */
    public static AmqpMessageBody fromSequence(List<Object> sequence) {
        Objects.requireNonNull(sequence, "'sequence' cannot be null.");
        AmqpMessageBody body = new AmqpMessageBody();
        body.bodyType = AmqpMessageBodyType.SEQUENCE;
        body.sequence = sequence;
        return body;
    }

    /**
     * Creates an instance of {@link AmqpMessageBody} with the given {@link Object value}. A value can be any of the
     * AMQP supported primitive data type.
     *
     * @param value used to create an instance of {@link AmqpMessageBody}.
     *
     * @return newly created instance of {@link AmqpMessageBody}.
     *
     * @throws NullPointerException if {@code value} is null.
     * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-types-v1.0-os.html#section-primitive-type-definitions" target="_blank">
     *     Amqp primitive data type.</a>
     */
    public static AmqpMessageBody fromValue(Object value) {
        Objects.requireNonNull(value, "'value' cannot be null.");
        AmqpMessageBody body = new AmqpMessageBody();
        body.bodyType = AmqpMessageBodyType.VALUE;
        body.value = value;
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
                "This method can only be called if AMQP Message body type is 'DATA'."));
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
                String.format(Locale.US, "This method can be called if AMQP Message body type is 'DATA'. "
                    + "The actual type is [%s].", bodyType)));
        }
        return data;
    }

    /**
     * Gets the unmodifiable AMQP Sequence set on this {@link AmqpMessageBody}. It support only one {@code sequence} at
     * present.
     *
     * <p><b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method. Get methods not
     * corresponding to the type of the body throws exception.</b></p>
     *
     * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
     * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
     * @return sequence of this {@link AmqpMessageBody} instance.
     *
     * @throws IllegalArgumentException If {@link AmqpMessageBodyType} is not
     *         {@link AmqpMessageBodyType#SEQUENCE SEQUENCE}.
     * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-types-v1.0-os.html#section-primitive-type-definitions" target="_blank">
     *     Amqp primitive data type.</a>
     * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format" target="_blank">
     *     Amqp message format.</a>
     */
    public List<Object> getSequence() {
        if (bodyType != AmqpMessageBodyType.SEQUENCE) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format(Locale.US, "This method can be called if AMQP Message body type is 'SEQUENCE'. "
                    + "The actual type is [%s].", bodyType)));
        }

        return Collections.unmodifiableList(sequence);
    }

    /**
     * Gets the AMQP value set on this {@link AmqpMessageBody} instance. It can be any of the primitive AMQP data type.
     *
     * <p><b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method. The 'Get'
     * methods not corresponding to the type of the body throws exception.</b></p>
     *
     * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
     * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
     * @return value of this {@link AmqpMessageBody} instance.
     *
     * @throws IllegalArgumentException If {@link AmqpMessageBodyType} is not {@link AmqpMessageBodyType#VALUE VALUE}.
     * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-types-v1.0-os.html#section-primitive-type-definitions" target="_blank">
     *     Amqp primitive data type.</a>
     * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format" target="_blank">
     *     Amqp message format.</a>
     */
    public Object getValue() {
        if (bodyType != AmqpMessageBodyType.VALUE) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format(Locale.US, "This method can be called if AMQP Message body type is 'VALUE'. "
                    + "The actual type is [%s].", bodyType)));
        }
        return value;
    }
}
