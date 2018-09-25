/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.eventgrid.customization;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.protocol.SerializerAdapter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * The type that can be used to de-serialize events.
 */
public class EventGridSubscriber {
    /**
     * The default adapter for to be used for de-serializing the events
     */
    private final AzureJacksonAdapter defaultSerializerAdapter;
    /**
     * The map containing user defined mapping of eventType to Java model type
     */
    private Map<String, Type> eventTypeToEventDataMapping;

    public EventGridSubscriber() {
        this.defaultSerializerAdapter = new AzureJacksonAdapter();
        this.eventTypeToEventDataMapping =  new HashMap<>();
    }

    /**
     * Add a custom event mapping. If a mapping with same eventType exists then the old eventDataType is replaced by
     * the specified eventDataType
     *
     * @param eventType the event type name
     * @param eventDataType type of the Java model that the event type name mapped to
     */
    public void putCustomEventMapping(String eventType, Type eventDataType) {
        if (eventType == null || eventType.isEmpty()) {
            throw new IllegalArgumentException("eventType parameter is required and cannot be null or empty");
        }
        if (eventDataType == null) {
            throw new IllegalArgumentException("eventDataType parameter is required and cannot be null");
        }
        this.eventTypeToEventDataMapping.put(eventType.toLowerCase(), eventDataType);
    }

    /**
     * Get type of the Java model that is mapped to the given eventType.
     *
     * @param eventType the event type name
     * @return type of the Java model id mapping exists, null otherwise
     */
    public Type getCustomEventMapping(String eventType) {
        if (eventType == null || eventType.isEmpty()) {
            throw new IllegalArgumentException("eventType parameter is required and cannot be null or empty");
        }
        if (!this.eventTypeToEventDataMapping.containsKey(eventType.toLowerCase())) {
            return null;
        }
        return this.eventTypeToEventDataMapping.get(eventType.toLowerCase());
    }

    /**
     * Removes the mapping with the given eventType.
     *
     * @param eventType the event type name
     * @return true if the mapping exists and removed, false if mapping does not exists
     */
    public boolean removeCustomEventMapping(String eventType) {
        if (eventType == null || eventType.isEmpty()) {
            throw new IllegalArgumentException("eventType parameter is required and cannot be null or empty");
        }
        if (!this.eventTypeToEventDataMapping.containsKey(eventType.toLowerCase())) {
            return false;
        }
        this.eventTypeToEventDataMapping.remove(eventType.toLowerCase());
        return true;
    }

    /**
     * Checks an event mapping with the given eventType exists.
     *
     * @param eventType the event type name
     * @return true if the mapping exists, false otherwise
     */
    public boolean containsEventMappingFor(String eventType) {
        if (eventType == null || eventType.isEmpty()) {
            throw new IllegalArgumentException("eventType parameter is required and cannot be null or empty");
        }
        return this.eventTypeToEventDataMapping.containsKey(eventType.toLowerCase());
    }

    /**
     * De-serialize the events in the given requested content using default de-serializer.
     *
     * @param requestContent the request content in string format
     * @return De-serialized events.
     *
     * @throws IOException
     */
    public EventGridEvent[] DeserializeEventGridEvents(final String requestContent) throws IOException {
        return this.DeserializeEventGridEvents(requestContent, this.defaultSerializerAdapter);
    }

    /**
     * De-serialize the events in the given requested content using the provided de-serializer.
     *
     * @param requestContent the request content in string format
     * @param serializerAdapter the de-serializer
     * @return e-serialized events.
     * @throws IOException
     */
    public EventGridEvent[] DeserializeEventGridEvents(final String requestContent, final SerializerAdapter<ObjectMapper> serializerAdapter) throws IOException {
        EventGridEvent[] eventGridEvents = serializerAdapter.<EventGridEvent[]>deserialize(requestContent, EventGridEvent[].class);
        for (EventGridEvent receivedEvent : eventGridEvents) {
            if (receivedEvent.data() == null) {
                continue;
            } else {
                final String dataStr = serializerAdapter.serializeRaw(receivedEvent.data());
                final String eventType = receivedEvent.eventType();
                if (SystemEventTypeMappings.mappingExists(eventType)) {
                    final Object eventData = serializerAdapter.<Object>deserialize(dataStr, SystemEventTypeMappings.getMapping(eventType));
                    setEventData(receivedEvent, eventData);
                } else if (containsEventMappingFor(eventType)) {
                    final Object eventData = serializerAdapter.<Object>deserialize(dataStr, getCustomEventMapping(eventType));
                    setEventData(receivedEvent, eventData);
                }
            }
        }
        return eventGridEvents;
    }

    private void setEventData(EventGridEvent event, Object data) {
        try {
            Field dataField = event.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            dataField.set(event, data);
        } catch (NoSuchFieldException nsfe) {
            throw new RuntimeException(nsfe);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        }
    }
}