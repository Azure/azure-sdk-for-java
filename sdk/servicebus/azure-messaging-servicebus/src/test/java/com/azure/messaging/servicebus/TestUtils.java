// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

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
    static final Long SEQUENCE_NUMBER = 1025L;
    static final String OTHER_SYSTEM_PROPERTY = "Some-other-system-property";
    static final Boolean OTHER_SYSTEM_PROPERTY_VALUE = Boolean.TRUE;
    static final Map<String, Object> APPLICATION_PROPERTIES = new HashMap<>();
    static final int USE_CASE_DEFAULT = 0;
    static final int USE_CASE_RECEIVE_MORE_AND_COMPLETE = 1;
    static final int USE_CASE_RECEIVE_BY_TIME = 2;
    static final int USE_CASE_RECEIVE_NO_MESSAGES = 3;
    static final int USE_CASE_SEND_RECEIVE_WITH_PROPERTIES = 4;
    static final int USE_CASE_MULTIPLE_RECEIVE_ONE_TIMEOUT = 5;
    static final int USE_CASE_PEEK_BATCH_MESSAGES = 6;
    static final int USE_CASE_SEND_READ_BACK_MESSAGES = 7;
    static final int USE_CASE_MULTIPLE_SESSION = 8;
    static final int USE_CASE_PEEK_MESSAGE_FROM_SEQUENCE = 9;
    static final int USE_CASE_PEEK_RECEIVE_AND_DEFER = 10;
    static final int USE_CASE_PEEK_TRANSACTION_SENDRECEIVE_AND_COMPLETE = 11;
    static final int USE_CASE_SINGLE_SESSION = 12;
    static final int USE_CASE_SEND_VIA_1 = 13;
    static final int USE_CASE_SEND_VIA_2 = 14;

    // An application property key to identify where in the stream this message was created.
    static final String MESSAGE_POSITION_ID = "message-position";

    static {
        APPLICATION_PROPERTIES.put("test-name", ServiceBusMessage.class.getName());
        APPLICATION_PROPERTIES.put("a-number", 10L);
        APPLICATION_PROPERTIES.put("status-code", AmqpResponseCode.OK.getValue());
    }

    /**
     * Gets the namespace connection string.
     *
     * @return The namespace connection string.
     */
    public static String getConnectionString() {
        return System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    }

    /**
     * Gets the fully qualified domain name for the service bus resource.
     *
     * @return The fully qualified domain name for the service bus resource.
     */
    public static String getFullyQualifiedDomainName() {
        return System.getenv("AZURE_SERVICEBUS_FULLY_QUALIFIED_DOMAIN_NAME");
    }

    /**
     * The Service Bus queue name (NOT session enabled).
     *
     * @return The Service Bus queue name.
     */
    public static String getQueueBaseName() {
        return System.getenv("AZURE_SERVICEBUS_QUEUE_NAME");
    }

    /**
     * The Service Bus queue name (session enabled).
     *
     * @return The Service Bus queue name.
     */
    public static String getSessionQueueBaseName() {
        return System.getenv("AZURE_SERVICEBUS_SESSION_QUEUE_NAME");
    }

    /**
     * Gets the Service Bus subscription name (NOT session enabled)
     *
     * @return The Service Bus subscription name.
     */
    public static String getSubscriptionBaseName() {
        return System.getenv("AZURE_SERVICEBUS_SUBSCRIPTION_NAME");
    }

    /**
     * Gets the Service Bus subscription name (NOT session enabled)
     *
     * @return The Service Bus subscription name.
     */
    public static String getTopicBaseName() {
        return System.getenv("AZURE_SERVICEBUS_TOPIC_NAME");
    }

    /**
     * Gets the Service Bus subscription name (session enabled)
     *
     * @return The Service Bus subscription name.
     */
    public static String getSessionSubscriptionBaseName() {
        return System.getenv("AZURE_SERVICEBUS_SESSION_SUBSCRIPTION_NAME");
    }

    /**
     * Gets the name of an entity based on its base name.
     *
     * @param baseName Base of the entity.
     * @param index Index number.
     *
     * @return The entity name.
     */
    public static String getEntityName(String baseName, int index) {
        return String.join("-", baseName, String.valueOf(index));
    }

    /**
     * Creates a message with the given contents, default system properties, and adds a {@code messageId} in the
     * application properties. Useful for helping filter messages.
     */
    public static Message getMessage(byte[] contents, String messageId, Map<String, String> additionalProperties) {
        final Map<Symbol, Object> systemProperties = new HashMap<>();
        systemProperties.put(Symbol.getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()), Date.from(ENQUEUED_TIME));
        systemProperties.put(Symbol.getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()), SEQUENCE_NUMBER);

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(systemProperties));
        message.setBody(new Data(new Binary(contents)));
        message.getMessageAnnotations().getValue()
            .put(Symbol.getSymbol(OTHER_SYSTEM_PROPERTY), OTHER_SYSTEM_PROPERTY_VALUE);

        Map<String, Object> applicationProperties = new HashMap<>();
        APPLICATION_PROPERTIES.forEach(applicationProperties::put);

        if (!CoreUtils.isNullOrEmpty(messageId)) {
            message.setMessageId(messageId);
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
    public static Message getMessage(byte[] contents, String messageTrackingValue) {
        return getMessage(contents, messageTrackingValue, Collections.emptyMap());
    }

    /**
     * Gets a set of messages with {@link ServiceBusMessage#getMessageId()} as a unique identifier for that service bus
     * message.
     *
     * @param numberOfEvents Number of events to create.
     * @param messageId An identifier for the set of messages.
     *
     * @return A list of messages.
     */
    public static List<ServiceBusMessage> getServiceBusMessages(int numberOfEvents, String messageId, byte[] content) {
        return IntStream.range(0, numberOfEvents)
            .mapToObj(number -> {
                final ServiceBusMessage message = getServiceBusMessage(content, messageId);
                message.getProperties().put(MESSAGE_POSITION_ID, number);

                return message;
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets a set of messages with {@link ServiceBusMessage#getMessageId()} as a unique identifier for that service bus
     * message.
     *
     * @param numberOfEvents Number of events to create.
     * @param messageId An identifier for the set of messages.
     *
     * @return A list of messages.
     */
    public static List<ServiceBusMessage> getServiceBusMessages(int numberOfEvents, String messageId) {
        return IntStream.range(0, numberOfEvents)
            .mapToObj(number -> {
                final ServiceBusMessage message = getServiceBusMessage("Event " + number, messageId);
                message.getProperties().put(MESSAGE_POSITION_ID, number);

                return message;
            })
            .collect(Collectors.toList());
    }

    public static ServiceBusMessage getServiceBusMessage(String body, String messageId) {
        return getServiceBusMessage(body.getBytes(UTF_8), messageId);
    }

    public static ServiceBusMessage getServiceBusMessage(byte[] body, String messageId) {
        final ServiceBusMessage message = new ServiceBusMessage(body);
        message.setMessageId(messageId);
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
