// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to construct instances of the immutable classes {@link EventGridConsumer} and
 * {@link EventGridAsyncConsumer}, which are used to receive and deserialize events sent from the EventGrid service.
 */
@Fluent
public class EventGridConsumerBuilder {

    private final Map<String, Class<?>> typeMappings;

    private JsonSerializer dataDeserializer;

    /**
     * Create the Consumer Builder with system event mappings already loaded.
     */
    public EventGridConsumerBuilder() {
        typeMappings = new HashMap<>(SystemEventMappings.getSystemEventMappings());
    }


    /**
     * Build an instance of the async consumer. If no deserializer is provided, then a default Jackson one is provided.
     * @return the async consumer with the settings that were already set.
     */
    public EventGridAsyncConsumer buildAsyncConsumer() {
        return new EventGridAsyncConsumer(typeMappings, dataDeserializer);
    }

    /**
     * Build an instance of the sync consumer. If no deserializer is provided, then a default Jackson one is provided.
     * @return the sync consumer with the settings that were already set.
     */
    public EventGridConsumer buildConsumer() {
        return new EventGridConsumer(buildAsyncConsumer());
    }

    /**
     * Add a mapping from the event data type string to the object class for the deserializer to deserialize.
     * @param eventType the event data type identifier string, e.g. "Contoso.Items.ItemRecieved". In EventGridEvents,
     *                  this is the <code>eventType</code> field. In CloudEvents, this is the <code>type</code> field.
     *                  The event data type will be converted to lowercase.
     * @param dataClass the data type class of the object to be decoded. The deserializer must be able to deserialize
     *                  this type, meaning it should have {@link com.fasterxml.jackson.annotation.JsonProperty}
     *                  annotations if the default serializer is used.
     *
     * @return the builder itself.
     */
    public EventGridConsumerBuilder addDataMapping(String eventType, Class<?> dataClass) {
        if (CoreUtils.isNullOrEmpty(eventType)) {
            throw new IllegalArgumentException("event type cannot be null or empty");
        }
        typeMappings.put(SystemEventMappings.canonicalizeEventType(eventType), dataClass);
        return this;
    }

    /**
     * Set the custom serializer to use when deserializing the event data or custom schema events. This deserializer
     * should be able to decode all types expected to be in the data field or the custom schema event.
     * @param deserializer the deserializer to use.
     *
     * @return the builder itself.
     */
    public EventGridConsumerBuilder dataDeserializer(JsonSerializer deserializer) {
        this.dataDeserializer = deserializer;
        return this;
    }
}
