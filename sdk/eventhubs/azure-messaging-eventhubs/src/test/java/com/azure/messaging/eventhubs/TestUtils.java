// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.implementation.util.ImplUtils;
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
import java.util.List;
import java.util.Map;

import static com.azure.core.amqp.MessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains helper methods for working with AMQP messages
 */
final class TestUtils {
    static final String TEST_CONNECTION_STRING = "Endpoint=sb://test-event-hub.servicebus.windows.net/;SharedAccessKeyName=dummyaccount;SharedAccessKey=ctzMq410TV3wS7upTBcunJTDLEJwMAZuFPfr0mrrA08=;EntityPath=non-existent-hub;";

    // System and application properties from the generated test message.
    static final Instant ENQUEUED_TIME = Instant.ofEpochSecond(1561344661);
    static final Long OFFSET = 1534L;
    static final String PARTITION_KEY = "a-partition-key";
    static final Long SEQUENCE_NUMBER = 1025L;
    static final String OTHER_SYSTEM_PROPERTY = "Some-other-system-property";
    static final Boolean OTHER_SYSTEM_PROPERTY_VALUE = Boolean.TRUE;
    static final Map<String, Object> APPLICATION_PROPERTIES = new HashMap<>();

    // An application property key used to identify that the request belongs to a test set.
    static final String MESSAGE_TRACKING_ID = "message-tracking-id";
    // An application property key to identify where in the stream this event was created.
    static final String MESSAGE_POSITION_ID = "message-position";

    static {
        APPLICATION_PROPERTIES.put("test-name", EventDataTest.class.getName());
        APPLICATION_PROPERTIES.put("a-number", 10L);
    }

    static Symbol getSymbol(MessageConstant messageConstant) {
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

    static Message getMessage(byte[] contents, String messageTrackingValue, Map<String, String> additionalProperties) {
        final Map<Symbol, Object> systemProperties = new HashMap<>();
        systemProperties.put(getSymbol(OFFSET_ANNOTATION_NAME), String.valueOf(OFFSET));
        systemProperties.put(getSymbol(PARTITION_KEY_ANNOTATION_NAME), PARTITION_KEY);
        systemProperties.put(getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME), Date.from(ENQUEUED_TIME));
        systemProperties.put(getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME), SEQUENCE_NUMBER);
        systemProperties.put(Symbol.getSymbol(OTHER_SYSTEM_PROPERTY), OTHER_SYSTEM_PROPERTY_VALUE);

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(systemProperties));

        Map<String, Object> applicationProperties = new HashMap<>();
        APPLICATION_PROPERTIES.forEach(applicationProperties::put);

        if (!ImplUtils.isNullOrEmpty(messageTrackingValue)) {
            applicationProperties.put(MESSAGE_TRACKING_ID, messageTrackingValue);
        }

        if (additionalProperties != null) {
            additionalProperties.forEach(applicationProperties::put);
        }

        message.setApplicationProperties(new ApplicationProperties(applicationProperties));
        message.setBody(new Data(new Binary(contents)));

        return message;
    }

    static Flux<EventData> getEvents(int numberOfEvents, String messageTrackingValue) {
        return Flux.range(0, numberOfEvents)
            .map(number -> getEvent("Event " + number, messageTrackingValue, number));
    }

    static List<EventData> getEventsAsList(int numberOfEvents, String messageTrackingValue) {
        return getEvents(numberOfEvents, messageTrackingValue).collectList().block();
    }

    static EventData getEvent(String body, String messageTrackingValue, int position) {
        final EventData eventData = new EventData(body.getBytes(UTF_8));
        eventData.addProperty(MESSAGE_TRACKING_ID, messageTrackingValue);
        eventData.addProperty(MESSAGE_POSITION_ID, position);
        return eventData;
    }

    /**
     * Checks the {@link #MESSAGE_TRACKING_ID} to see if it matches the {@code expectedValue}.
     */
    static boolean isMatchingEvent(EventData event, String expectedValue) {
        return event.properties() != null && event.properties().containsKey(MESSAGE_TRACKING_ID)
            && expectedValue.equals(event.properties().get(MESSAGE_TRACKING_ID));
    }

    private TestUtils() {
    }
}
