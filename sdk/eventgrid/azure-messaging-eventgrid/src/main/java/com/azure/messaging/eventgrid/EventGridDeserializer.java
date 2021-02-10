package com.azure.messaging.eventgrid;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;

import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a convenience class that deserializes a json string into events.
 * {@link #deserializeCloudEvents(String)} deserializes a JSON string into a list of {@link CloudEvent} instances.
 * {@link #deserializeEventGridEvents(String)} deserializes a JSON string into a list of {@link EventGridEvent}
 * instances.
 *
 */
final class EventGridDeserializer {
    private static final ClientLogger LOGGER = new ClientLogger(EventGridDeserializer.class);

    private EventGridDeserializer() {
        // Hide the constructor
    }

    static final JsonSerializer DESERIALIZER = JsonSerializerProviders.createInstance();

    /**
     * Deserialize the {@link EventGridEvent} from a JSON string.
     * @param eventGridEventsJson the JSON payload containing one or more events.
     *
     * @return all of the events in the payload deserialized as {@link EventGridEvent}s.
     * @throws IllegalArgumentException if the input parameter isn't a JSON string for a eventgrid event
     * or an array of it.
     */
    static List<EventGridEvent> deserializeEventGridEvents(String eventGridEventsJson) {
        try {
            return Arrays.stream(DESERIALIZER
                .deserialize(new ByteArrayInputStream(eventGridEventsJson.getBytes(StandardCharsets.UTF_8)),
                    TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.EventGridEvent[].class)))
                .map(internalEvent -> {
                    if (internalEvent.getSubject() == null || internalEvent.getEventType() == null
                        || internalEvent.getData() == null) {
                        throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                            "'subject', 'type', and 'data' are mandatory attributes for an EventGridEvent. " +
                                "Check if the input param is a JSON string for an EventGridEvent or an array of it."));
                    }
                    return new EventGridEvent(internalEvent);
                })
                .collect(Collectors.toList());
        } catch (UncheckedIOException uncheckedIOException) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The input parameter isn't a JSON string.",
                uncheckedIOException.getCause()));
        }
    }

    /**
     * Deserialize the {@link CloudEvent} from a JSON string.
     * @param cloudEventsJson the JSON payload containing one or more events.
     *
     * @return all of the events in the payload deserialized as {@link CloudEvent}s.
     * @throws IllegalArgumentException if the input parameter isn't a JSON string for a cloud event or an array of it.
     */
    static List<CloudEvent> deserializeCloudEvents(String cloudEventsJson) {
        try {
            return Arrays.stream(DESERIALIZER
                .deserialize(new ByteArrayInputStream(cloudEventsJson.getBytes(StandardCharsets.UTF_8)),
                    TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.CloudEvent[].class))
            )
                .map(internalEvent -> {
                    if (internalEvent.getSource() == null || internalEvent.getType() == null) {
                        throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                            "'source' and 'type' are mandatory attributes for a CloudEvent. " +
                                "Check if the input param is a JSON string for a CloudEvent or an array of it."));
                    }
                    return new CloudEvent(internalEvent);
                })
                .collect(Collectors.toList());
        } catch (UncheckedIOException uncheckedIOException) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The input parameter isn't a JSON string.",
                uncheckedIOException.getCause()));
        }
    }

    static BinaryData getData(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof byte[]) {
            return BinaryData.fromBytes((byte[]) data);
        }
        if (data instanceof String) {
            return BinaryData.fromString((String) data);
        }
        return BinaryData.fromObject(data);
    }
}
