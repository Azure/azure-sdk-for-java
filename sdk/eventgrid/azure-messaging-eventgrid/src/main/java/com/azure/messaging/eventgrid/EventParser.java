package com.azure.messaging.eventgrid;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EventParser {
    static final JsonSerializer DESERIALIZER = new JacksonJsonSerializerBuilder()
        .serializer(new JacksonAdapter().serializer() // this is a workaround to get the FlatteningDeserializer
            .registerModule(new JavaTimeModule())) // probably also change this to DateTimeDeserializer when/if it
        .build();                                  // becomes public in core

    /**
     * Parse the EventGrid Event from a JSON string. This can be used to interpret the event at the event destination
     * from raw JSON into rich event(s).
     * @param json the JSON payload containing one or more events.
     *
     * @return all of the events in the payload parsed as CloudEvents.
     */
    public static final List<EventGridEvent> parseEventGridEvent(String json) {
        return Flux.fromArray(DESERIALIZER
            .deserialize(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.EventGridEvent[].class))
        )
            .map(event -> {
                if (event.getData() == null) {
                    return new EventGridEvent(event);
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DESERIALIZER.serialize(stream, event.getData());
                return new EventGridEvent(event.setData(stream.toByteArray())); // use BinaryData instead?
            })
            .collectList()
            .block();
    }

    /**
     * Parse the Cloud Event from a JSON string. This can be used to interpret the event at the event destination
     * from raw JSON into rich event(s).
     * @param json the JSON payload containing one or more events.
     *
     * @return all of the events in the payload parsed as CloudEvents.
     */
    public static List<CloudEvent> parseCloudEvent(String json) {
        return Flux.fromArray(DESERIALIZER
            .deserialize(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.CloudEvent[].class))
        )
            .map(event1 -> {
                if (event1.getData() != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    DESERIALIZER.serialize(stream, event1.getData());
                    return new CloudEvent(event1).setData(stream.toByteArray()); // use BinaryData instead?
                } else { // both null, don't set data and keep null
                    return new CloudEvent(event1);
                }
            })
            .collectList()
            .block();
    }
}
