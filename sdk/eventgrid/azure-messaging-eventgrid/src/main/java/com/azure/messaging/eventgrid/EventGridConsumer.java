// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import reactor.core.publisher.Flux;

/**
 * A class used to decode events and their data from EventGrid at the endpoints. Able to decode all system event data
 * types, and provides support for custom event data deserialization mapping.
 * @see EventSchema
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
     * Add a mapping from the event type string to the object class for the deserializer to deserialize
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
     * Asynchronously deserializes the given JSON and returns a Flux of the full event objects wrapped in an
     * {@link EventSchema} object which can transformed into the event itself
     * @param json the event in JSON format
     *
     * @return a flux of all the events in EventSchema format.
     */
    public Flux<EventSchema> deserialize(String json) {
        // TODO: implement method
        return null;
    }
}
