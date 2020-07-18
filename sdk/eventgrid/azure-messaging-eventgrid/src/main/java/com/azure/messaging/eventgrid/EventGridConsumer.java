// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.messaging.eventgrid.events.CloudEvent;
import com.azure.messaging.eventgrid.events.EventGridEvent;

import java.util.List;

/**
 * A class used to decode events and their data from EventGrid at the endpoints. Able to decode all system event data
 * types, and provides support for custom event data deserialization mapping.
 * @see EventGridEvent
 * @see CloudEvent
 */
public class EventGridConsumer {

    /**
     * Creates a new instance of the consumer, with default deserialization knowledge, i.e. it is capable of
     * deserializing all system event data.
     */
    public EventGridConsumer() {
        // TODO: implement method
    }

    /**
     * Deserialize the given JSON using the available mappings and return the rich EventGridEvent Object, with the
     * data field as a rich object, if possible.
     * @param json The information to be deserialize in JSON format.
     *
     * @return The deserialized events in a List.
     */
    public List<EventGridEvent> deserializeEventGridEvents(String json) {
        // TODO: implement method
        return null;
    }

    /**
     * Deserialize the given JSON using the available mappings and return the rich CloudEvent Object, with the
     * data field as a rich object, if possible.
     * @param json The information to be deserialize as a JSON string.
     *
     * @return The deserialized events in a List.
     */
    public List<CloudEvent> deserializeCloudEvents(String json) {
        // TODO: implement method
        return null;
    }

    /**
     * Deserialize the given JSON into a given custom event schema and return the rich event type, using
     * available mappings to fill in rich object data
     * @param json           The information to deserialize as a JSON string
     * @param eventTypeClass The class type of the custom event
     * @param <TEvent>       The custom event object. Must have the proper Json annotations to allow for deserialization
     *
     * @return The deserialized events in a List
     */
    public <TEvent> List<TEvent> deserializeCustomEvents(String json, Class<TEvent> eventTypeClass) {
        // TODO: implement method
        return null;
    }
}
