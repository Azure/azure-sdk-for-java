/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.eventgrid.customization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.protocol.SerializerAdapter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The type that can be used to de-serialize eventgrid events.
 */
@Beta
public class EventGridSubscriber {
    /**
     * The default adapter to be used for de-serializing the events.
     */
    private final AzureJacksonAdapter defaultSerializerAdapter;
    /**
     * The map containing user defined mapping of eventType to Java model type.
     */
    private Map<String, Type> eventTypeToEventDataMapping;

    /**
     * Creates EventGridSubscriber with default de-serializer.
     */
    @Beta
    public EventGridSubscriber() {
        this.defaultSerializerAdapter = new AzureJacksonAdapter();
        this.eventTypeToEventDataMapping =  new HashMap<>();
    }

    /**
     * Add a custom event mapping. If a mapping with same eventType exists then the old eventDataType is replaced by
     * the specified eventDataType.
     *
     * @param eventType the event type name.
     * @param eventDataType type of the Java model that the event type name mapped to.
     */
    @Beta
    public void putCustomEventMapping(final String eventType, final Type eventDataType) {
        if (eventType == null || eventType.isEmpty()) {
            throw new IllegalArgumentException("eventType parameter is required and cannot be null or empty");
        }
        if (eventDataType == null) {
            throw new IllegalArgumentException("eventDataType parameter is required and cannot be null");
        }
        this.eventTypeToEventDataMapping.put(canonicalizeEventType(eventType), eventDataType);
    }

    /**
     * Get type of the Java model that is mapped to the given eventType.
     *
     * @param eventType the event type name.
     * @return type of the Java model if mapping exists, null otherwise.
     */
    @Beta
    public Type getCustomEventMapping(final String eventType) {
        if (!containsCustomEventMappingFor(eventType)) {
            return null;
        } else {
            return this.eventTypeToEventDataMapping.get(canonicalizeEventType(eventType));
        }
    }

    /**
     * @return get all registered custom event mappings.
     */
    @Beta
    public Set<Map.Entry<String, Type>> getAllCustomEventMappings() {
        return Collections.unmodifiableSet(this.eventTypeToEventDataMapping.entrySet());
    }

    /**
     * Removes the mapping with the given eventType.
     *
     * @param eventType the event type name.
     * @return true if the mapping exists and removed, false if mapping does not exists.
     */
    @Beta
    public boolean removeCustomEventMapping(final String eventType) {
        if (!containsCustomEventMappingFor(eventType)) {
            return false;
        } else {
            this.eventTypeToEventDataMapping.remove(canonicalizeEventType(eventType));
            return true;
        }
    }

    /**
     * Checks if an event mapping with the given eventType exists.
     *
     * @param eventType the event type name.
     * @return true if the mapping exists, false otherwise.
     */
    @Beta
    public boolean containsCustomEventMappingFor(final String eventType) {
        if (eventType == null || eventType.isEmpty()) {
            return false;
        } else {
            return this.eventTypeToEventDataMapping.containsKey(canonicalizeEventType(eventType));
        }
    }

    /**
     * De-serialize the events in the given requested content using default de-serializer.
     *
     * @param requestContent the request content in string format.
     * @return De-serialized events.
     *
     * @throws IOException
     */
    @Beta
    public EventGridEvent[] deserializeEventGridEvents(final String requestContent) throws IOException {
        return this.deserializeEventGridEvents(requestContent, this.defaultSerializerAdapter);
    }

    /**
     * De-serialize the events in the given requested content using the provided de-serializer.
     *
     * @param requestContent the request content as string.
     * @param serializerAdapter the de-serializer.
     * @return de-serialized events.
     * @throws IOException
     */
    @Beta
    public EventGridEvent[] deserializeEventGridEvents(final String requestContent, final SerializerAdapter<ObjectMapper> serializerAdapter) throws IOException {
        EventGridEvent[] eventGridEvents = serializerAdapter.<EventGridEvent[]>deserialize(requestContent, EventGridEvent[].class);
        for (EventGridEvent receivedEvent : eventGridEvents) {
            if (receivedEvent.data() == null) {
                continue;
            } else {
                final String eventType = receivedEvent.eventType();
                final Type eventDataType;
                if (SystemEventTypeMappings.containsMappingFor(eventType)) {
                    eventDataType = SystemEventTypeMappings.getMapping(eventType);
                } else if (containsCustomEventMappingFor(eventType)) {
                    eventDataType = getCustomEventMapping(eventType);
                } else {
                    eventDataType = null;
                }
                if (eventDataType != null) {
                    final String eventDataAsString = serializerAdapter.serializeRaw(receivedEvent.data());
                    final Object eventData = serializerAdapter.<Object>deserialize(eventDataAsString, eventDataType);
                    setEventData(receivedEvent, eventData);
                }
            }
        }
        return eventGridEvents;
    }

    private static void setEventData(EventGridEvent event, final Object data) {
        // This reflection based way to set the data field needs to be removed once
        // we expose a wither in EventGridEvent to set the data. (Check swagger + codegen)
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

    private static String canonicalizeEventType(final String eventType) {
        if (eventType == null) {
            return null;
        } else {
            return eventType.toLowerCase();
        }
    }
}
