// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.CoreUtils;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains helper methods for working with AMQP messages
 */
public final class TestUtils {
    private static final MessageSerializer MESSAGE_SERIALIZER = new EventHubMessageSerializer();

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
    // An application property key to identify where in the stream this event was created.
    public static final String MESSAGE_POSITION_ID = "message-position";

    static {
        APPLICATION_PROPERTIES.put("test-name", EventDataTest.class.getName());
        APPLICATION_PROPERTIES.put("a-number", 10L);
    }

    static Symbol getSymbol(AmqpMessageConstant messageConstant) {
        return Symbol.getSymbol(messageConstant.getValue());
    }

    /**
     * Creates a mock message with the contents provided.
     */
    static Message getMessage(byte[] contents) {
        return getMessage(contents, null);
    }

    /**
     * Creates a mock message with the contents provided.
     */
    static Message getMessage(byte[] contents, String messageTrackingValue) {
        return getMessage(contents, messageTrackingValue, Collections.emptyMap());
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
     * Creates a message with the required system properties set.
     */
    static Message getMessage(byte[] contents, Long sequenceNumber, Long offsetNumber, Date enqueuedTime) {
        final Map<Symbol, Object> systemProperties = new HashMap<>();
        systemProperties.put(getSymbol(OFFSET_ANNOTATION_NAME), String.valueOf(offsetNumber));
        systemProperties.put(getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME), enqueuedTime);
        systemProperties.put(getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME), sequenceNumber);
        systemProperties.put(getSymbol(PARTITION_KEY_ANNOTATION_NAME), PARTITION_KEY);

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(systemProperties));
        message.setBody(new Data(new Binary(contents)));

        return message;
    }

    /**
     * Creates an EventData with the received properties set.
     */
    public static EventData getEventData(byte[] contents, Long sequenceNumber, Long offsetNumber, Date enqueuedTime) {
        final Message message = getMessage(contents, sequenceNumber, offsetNumber, enqueuedTime);
        return MESSAGE_SERIALIZER.deserialize(message, EventData.class);
    }

    public static Flux<EventData> getEvents(int numberOfEvents, String messageTrackingValue) {
        return Flux.range(0, numberOfEvents)
            .map(number -> getEvent("Event " + number, messageTrackingValue, number));
    }

    static EventData getEvent(String body, String messageTrackingValue, int position) {
        final EventData eventData = new EventData(body.getBytes(UTF_8));
        eventData.getProperties().put(MESSAGE_TRACKING_ID, messageTrackingValue);
        eventData.getProperties().put(MESSAGE_POSITION_ID, position);
        return eventData;
    }

    /**
     * Checks the {@link #MESSAGE_TRACKING_ID} to see if it matches the {@code expectedValue}.
     */
    public static boolean isMatchingEvent(PartitionEvent partitionEvent, String expectedValue) {
        return isMatchingEvent(partitionEvent.getData(), expectedValue);
    }

    /**
     * Checks the {@link #MESSAGE_TRACKING_ID} to see if it matches the {@code expectedValue}.
     */
    public static boolean isMatchingEvent(EventData event, String expectedValue) {
        return event.getProperties() != null && event.getProperties().containsKey(MESSAGE_TRACKING_ID)
            && expectedValue.equals(event.getProperties().get(MESSAGE_TRACKING_ID));
    }

    private TestUtils() {
    }
}
