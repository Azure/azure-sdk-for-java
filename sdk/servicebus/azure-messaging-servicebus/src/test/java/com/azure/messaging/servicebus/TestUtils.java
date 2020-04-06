// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.util.CoreUtils;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TestUtils {

    // System and application properties from the generated test message.
    static final Instant ENQUEUED_TIME = Instant.ofEpochSecond(1561344661);
    static final Long OFFSET = 1534L;
    static final String PARTITION_KEY = "a-partition-key";
    static final Long SEQUENCE_NUMBER = 1025L;
    static final String OTHER_SYSTEM_PROPERTY = "Some-other-system-property";
    static final Boolean OTHER_SYSTEM_PROPERTY_VALUE = Boolean.TRUE;
    static final Map<String, Object> APPLICATION_PROPERTIES = new HashMap<>();

    // An application property key used to identify that the request belongs to a test set.
    public static final String MESSAGE_TRACKING_ID = "message-tracking-id";
    // An application property key to identify where in the stream this message was created.
    public static final String MESSAGE_POSITION_ID = "message-position";

    static {
        APPLICATION_PROPERTIES.put("test-name", ServiceBusMessage.class.getName());
        APPLICATION_PROPERTIES.put("a-number", 10L);
        APPLICATION_PROPERTIES.put("status-code", AmqpResponseCode.OK.getValue());
    }

    static Symbol getSymbol(AmqpMessageConstant messageConstant) {
        return Symbol.getSymbol(messageConstant.getValue());
    }

    /**
     * Creates a message with the required system properties set.
     */
    static org.apache.qpid.proton.message.Message getMessage(byte[] contents, Long sequenceNumber, Long offsetNumber, Date enqueuedTime) {
        final Map<Symbol, Object> systemProperties = new HashMap<>();
        systemProperties.put(getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME), enqueuedTime);
        systemProperties.put(getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME), sequenceNumber);

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(systemProperties));
        message.setBody(new Data(new Binary(contents)));

        return message;
    }

    /**
     * Creates a message with the given contents, default system properties, and adds a {@code messageTrackingValue} in
     * the application properties. Useful for helping filter messages.
     */
    static Message getMessage(byte[] contents, String messageTrackingValue, Map<String, String> additionalProperties) {
        final Message message = getMessage(contents, SEQUENCE_NUMBER, OFFSET, Date.from(ENQUEUED_TIME));

        message.getMessageAnnotations().getValue()
            .put(Symbol.getSymbol(OTHER_SYSTEM_PROPERTY), OTHER_SYSTEM_PROPERTY_VALUE);

        Map<String, Object> applicationProperties = new HashMap<>();
        APPLICATION_PROPERTIES.forEach(applicationProperties::put);

        if (!CoreUtils.isNullOrEmpty(messageTrackingValue)) {
            applicationProperties.put(MESSAGE_TRACKING_ID, messageTrackingValue);
        }

        if (additionalProperties != null) {
            additionalProperties.forEach(applicationProperties::put);
        }

        message.setApplicationProperties(new ApplicationProperties(applicationProperties));

        return message;
    }

    /**
     * Creates a mock message with the contents provided.
     */
    public static Message getMessage(byte[] contents) {
        return getMessage(contents, null);
    }

    /**
     * Creates a mock message with the contents provided.
     */
    static Message getMessage(byte[] contents, String messageTrackingValue) {
        return getMessage(contents, messageTrackingValue, Collections.emptyMap());
    }

    /**
     * Gets a set of messages with {@link #MESSAGE_TRACKING_ID} as a unique identifier for that service bus message.
     *
     * @param numberOfEvents Number of events to create.
     * @param messageTrackingValue An identifier for the set of messages.
     *
     * @return A list of messages.
     */
    public static List<ServiceBusMessage> getServiceBusMessages(int numberOfEvents, String messageTrackingValue) {
        return IntStream.range(0, numberOfEvents)
            .mapToObj(number -> getServiceBusMessage("Event " + number, messageTrackingValue, number))
            .collect(Collectors.toList());
    }

    public static ServiceBusMessage getServiceBusMessage(String body, String messageTrackingValue, int position) {
        final ServiceBusMessage message = new ServiceBusMessage(body.getBytes(UTF_8));
        message.getProperties().put(MESSAGE_TRACKING_ID, messageTrackingValue);
        message.getProperties().put(MESSAGE_POSITION_ID, position);
        return message;
    }

    /**
     * Given a set of messages, will create a FluxSink that emits them. When there are no more messages to emit, a
     * completion signal is emitted.
     *
     * @param messages Messages to emit.
     *
     * @return A flux of messages.
     */
    public static Flux<ServiceBusReceivedMessage> createMessageSink(ServiceBusReceivedMessage... messages) {
        final ConcurrentLinkedDeque<ServiceBusReceivedMessage> queue = new ConcurrentLinkedDeque<>(
            Arrays.asList(messages));

        return Flux.create(emitter -> {
            emitter.onRequest(request -> {
                if (queue.isEmpty()) {
                    return;
                }

                for (int i = 0; i < request; i++) {
                    final ServiceBusReceivedMessage message = queue.poll();
                    if (message == null) {
                        emitter.complete();
                        return;
                    }

                    emitter.next(message);
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }
}
