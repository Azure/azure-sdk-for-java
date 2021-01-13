package com.azure.messaging.eventgrid;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class EventParser {
    private EventParser() {
        // Hide the constructor
    }

    static final JsonSerializer DESERIALIZER = new JacksonJsonSerializerBuilder()
        .serializer(new JacksonAdapter().serializer() // this is a workaround to get the FlatteningDeserializer
            .registerModule(new JavaTimeModule())) // probably also change this to DateTimeDeserializer when/if it
        .build();                                  // becomes public in core

    /**
     * Parse the EventGrid Event from a JSON string. This can be used to interpret the event at the event destination
     * from raw JSON into rich event(s).
     * @param json the JSON payload containing one or more events.
     *
     * @return all of the events in the payload parsed as {@link EventGridEvent}s.
     */
    public static List<EventGridEvent> parseEventGridEvents(String json) {
        return Flux.fromArray(DESERIALIZER
            .deserialize(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.EventGridEvent[].class))
        )
            .map(EventGridEvent::new)
            .collectList()
            .block();
    }

    /**
     * Parse the Cloud Event from a JSON string. This can be used to interpret the event at the event destination
     * from raw JSON into rich event(s).
     * @param json the JSON payload containing one or more events.
     *
     * @return all of the events in the payload parsed as {@link CloudEvent}s.
     */
    public static List<CloudEvent> parseCloudEvents(String json) {
        return Flux.fromArray(DESERIALIZER
            .deserialize(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.CloudEvent[].class))
        )
            .map(CloudEvent::new)
            .collectList()
            .block();
    }

    static Object getSystemEventData(BinaryData data, String eventType) {
        if (SystemEventMappings.getSystemEventMappings().containsKey(eventType)) {
            return data
                .toObject(TypeReference.createInstance(SystemEventMappings.getSystemEventMappings().get(eventType)));
        }
        return null;
    }

    static Mono<Object> getSystemEventDataAsync(Mono<BinaryData> data, String eventType) {
        if (SystemEventMappings.getSystemEventMappings().containsKey(eventType)) {
            return data.map(binaryData -> binaryData
                .toObject(TypeReference.createInstance(SystemEventMappings.getSystemEventMappings().get(eventType))));
        }
        return Mono.empty();
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
