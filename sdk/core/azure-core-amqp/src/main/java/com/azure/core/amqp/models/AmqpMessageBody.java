// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
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
 */
public final class AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpMessageBody.class);
    private AmqpMessageBodyType bodyType;

    // We expect user to call `getFirstData()` more because we support one byte[] as present.
    // This the priority here to store payload as `byte[] data` and
    private byte[] data;
    private List<byte[]> dataList;
    private Object value;
    //private List<AmqpSequenceData<Object>> sequence;
    private List<Object> sequence;
    private List<List<Object>> sistOfListSequence;
    //private AmqpSequenceDataList<AmqpSequenceData> amqpSequenceDataList;

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
     * Creates instance of {@link AmqpMessageBody} with given {@link Object value}.
     *
     * @param value used to create another instance of {@link AmqpMessageBody}.
     *
     * @return AmqpMessageBody Newly created instance.
     *
     * @throws NullPointerException if {@code data} is null.
     */
    public static AmqpMessageBody fromValue(Object value) {
        Objects.requireNonNull(value, "'value' cannot be null.");
        AmqpMessageBody body = new AmqpMessageBody();
        body.bodyType = AmqpMessageBodyType.VALUE;
        body.value = value;
        return body;
    }

    /**
     * Creates instance of {@link AmqpMessageBody} with given {@link Object value}.
     *
     * @param sequence used to create another instance of {@link AmqpMessageBody}.
     *
     * @return AmqpMessageBody Newly created instance.
     *
     * @throws NullPointerException if {@code data} is null.
     */
    public static AmqpMessageBody fromSequence(List<Object> sequence) {
        Objects.requireNonNull(sequence, "'sequence' cannot be null.");
        AmqpMessageBody body = new AmqpMessageBody();
        body.bodyType = AmqpMessageBodyType.SEQUENCE;
        body.sequence = sequence;
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
     * Gets the AMQP Value set on this {@link AmqpMessageBody}. It can be any premitive AMQP Data types.
     *
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
    public Object getValue() {
        if (bodyType != AmqpMessageBodyType.VALUE) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format(Locale.US, "This method can be called if AMQP Message body type is 'VALUE'. "
                        + "The actual type is [%s].", bodyType)));
        }
        return value;
    }

    /**
     * Gets the unmodifiable AMQP Sequence set on this {@link AmqpMessageBody}.
     *
     * <p><b>Client should test for {@link AmqpMessageBodyType} before calling corresponding get method. Get methods not
     * corresponding to the type of the body throws exception.</b></p>
     *
     * <p><strong>How to check for {@link AmqpMessageBodyType}</strong></p>
     * {@codesnippet com.azure.core.amqp.models.AmqpBodyType.checkBodyType}
     * @return data set on {@link AmqpMessageBody}.
     *
     * @throws IllegalArgumentException If {@link AmqpMessageBodyType} is not {@link AmqpMessageBodyType#DATA DATA}.
     * @throws UnsupportedOperationException If you try to modify returned sequence.
     * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format" target="_blank">
     *     Amqp Message Format.</a>
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
     * Gets the AMQP Sequence set on this {@link AmqpMessageBody}.
     *
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
    /*public List<AmqpSequenceData<Object>> getAmqpSequenceData() {
        if (bodyType != AmqpMessageBodyType.SEQUENCE) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format(Locale.US, "This method can be called if AMQP Message body type is 'SEQUENCE'. "
                    + "The actual type is [%s].", bodyType)));
        }
        return sequence;
    }*/

   /* public  IterableStream<AmqpSequenceData> getSequence() {
        return new IterableStream<>(sequence);
    }*/

   /* public static void main(String[] args) {
        List<Object> sequence =  new ArrayList<>();
        sequence.add(1L);
        sequence.add(2L);

        AmqpSequenceData longList =  new AmqpSequenceData(sequence);


        //AmqpSequenceDataList<AmqpSequenceData> amqpSequenceDataList =  new AmqpSequenceDataList(longList);

        AmqpAnnotatedMessage amqpAnnotatedMessage = new AmqpAnnotatedMessage(AmqpMessageBody.fromSequence(longList));

        //AmqpSequenceDataList<AmqpSequenceData> x =   amqpAnnotatedMessage.getBody().getSequence();
        //List<AmqpSequenceData> receivedAmqpSequenceDataList =   amqpAnnotatedMessage.getBody().getSequence();
        List<List<Object>> receivedAmqpSequenceDataList =   amqpAnnotatedMessage.getBody().getSequence();

        System.out.println("amqpAnnotatedMessage.getBody().getSequence() : "+ receivedAmqpSequenceDataList.get(0).getClass().getName());

        amqpAnnotatedMessage.getBody().getSequence().forEach(amqpSequenceData -> {
            amqpSequenceData.forEach(payload -> {
                System.out.println("payload : "+ payload);
            });
        });

        ///

        System.out.println("----------------------- List<AmqpSequenceData> as  iterator -------------");

        amqpAnnotatedMessage.getBody().getAmqpSequenceData().forEach(amqpSequenceData -> {
            amqpSequenceData.forEach(payload -> {
                System.out.println("payload : "+ payload);
            });
        });

        System.out.println("----------------------- List<AmqpSequenceData>  LIST -------------");
        List<AmqpSequenceData<Object>>  amqpSequenceDataList =   amqpAnnotatedMessage.getBody().getAmqpSequenceData();
        for (int i = 0; i < amqpSequenceDataList.size(); i++){
            List<Object> dataList = amqpSequenceDataList.get(i).toList();
            for (int j = 0; j < dataList.size(); j++){
                System.out.println(" payload : "+ dataList.get(j));
            }
        }
    }
    */

}
