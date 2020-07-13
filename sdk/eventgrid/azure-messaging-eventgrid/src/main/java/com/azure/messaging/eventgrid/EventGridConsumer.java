// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;
import reactor.core.publisher.Flux;

/**
 * A class used to decode events and their data from EventGrid at the endpoints. Able to decode all system event data
 * types, and provides support for custom event data deserialization mapping.
 * @see com.azure.messaging.eventgrid.models.EventGridEvent
 * @see com.azure.messaging.eventgrid.models.CloudEvent
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
     * Add a mapping from the event type string to the object class for the deserializer to deserialize.
     * @param eventType the event type identifier string, e.g. "Contoso.Items.ItemRecieved". In EventGridEvents,
     *                  this is the <code>eventType</code> field. In CloudEvents, this is the <code>type</code> field.
     * @param classType the class of the object to be decoded.
     * @param <T>       the type of the object to be decoded. This corresponds to the data field. The object must
     *                  have proper JsonProperty annotations to allow for deserialization.
     */
    public <T> void putMapping(String eventType, Class<T> classType) {
        // TODO: implement method
    }

    /**
     * Deserialize the given JSON using the available mappings and return the rich EventGridEvent Object, with the
     * data field as a rich object, if possible.
     * @param json The information to be deserialize in JSON format.
     *
     * @return The deserialized events in a Flux.
     */
    public Flux<EventGridEvent> deserializeEventGridEvents(String json) {
        // TODO: implement method
        return null;
    }

    /**
     * Deserialize the given JSON using the available mappings and return the rich CloudEvent Object, with the
     * data field as a rich object, if possible.
     * @param json The information to be deserialize as a JSON string.
     *
     * @return The deserialized events in a Flux.
     */
    public Flux<CloudEvent> deserializeCloudEvents(String json) {
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
     * @return The deserialized events in a Flux
     */
    public <TEvent> Flux<TEvent> deserializeCustomEvents(String json, Class<TEvent> eventTypeClass) {
        // TODO: implement method
        return null;
    }
}
