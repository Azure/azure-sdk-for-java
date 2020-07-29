// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import java.util.List;

/**
 * This class provides synchronous methods for deserializing events from the EventGrid Service.
 * To construct, use {@link EventGridConsumerBuilder}
 * @see EventGridConsumerBuilder
 * @see EventGridAsyncConsumer
 */
public final class EventGridConsumer {

    private final EventGridAsyncConsumer asyncConsumer;

    EventGridConsumer(EventGridAsyncConsumer consumer) {
        this.asyncConsumer = consumer;
    }

    /**
     * Deserialize the given JSON using the available mappings and return the rich EventGridEvent Object, with the
     * data field as a rich object, if possible.
     * @param json The information to be deserialize in JSON format.
     *
     * @return The deserialized events in a List.
     */
    public List<EventGridEvent> deserializeEventGridEvents(String json) {
        return asyncConsumer.deserializeEventGridEvents(json)
            .collectList()
            .block();
    }

    /**
     * Deserialize the given JSON and return the rich CloudEvent, with the data field as a
     * rich object(s), if possible.
     * @param json the JSON string containing the event that was received from the EventGrid service.
     *
     * @return the deserialized cloud events in a List, with rich object data.
     */
    public List<CloudEvent> deserializeCloudEvents(String json) {
        return asyncConsumer.deserializeCloudEvents(json)
            .collectList()
            .block();
    }

    /**
     * Deserialize the given JSON into a given custom event schema and return the rich event type, using
     * available mappings to fill in rich object data
     * @param json     The information to deserialize as a JSON string
     * @param clazz    The class type of the custom event
     * @param <TEvent> The custom event object. Must be deserializable by the set deserializer.
     *
     * @return The deserialized events in a List
     */
    public <TEvent> List<TEvent> deserializeCustomEvents(String json, Class<TEvent> clazz) {
        return asyncConsumer.deserializeCustomEvents(json, clazz)
            .collectList()
            .block();
    }
}
