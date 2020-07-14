// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;

import java.util.List;
import java.util.Map;

/**
 * A class used to decode events and their data from EventGrid at the endpoints. Able to decode all system event data
 * types, and provides support for custom event data deserialization mapping.
 * @see com.azure.messaging.eventgrid.models.EventGridEvent
 * @see com.azure.messaging.eventgrid.models.CloudEvent
 */
public class EventConsumer {

    /**
     * Creates a new instance of the consumer, with default deserialization knowledge, i.e. it is capable of
     * deserializing all system event data.
     */
    public EventConsumer() {
        // TODO: implement method
    }

    /**
     * Add a mapping from the event data type string to the object class for the deserializer to deserialize.
     * @param eventType the event data type identifier string, e.g. "Contoso.Items.ItemRecieved". In EventGridEvents,
     *                  this is the <code>eventType</code> field. In CloudEvents, this is the <code>type</code> field.
     * @param dataType  the data type class of the object to be decoded.
     * @param <T>       the type of the data to be decoded. This corresponds to the data field. The object must
     *                  have proper JsonProperty annotations to allow for deserialization.
     */
    public <T> void putDataMapping(String eventType, Class<T> dataType) {
        // TODO: implement method
    }

    /**
     * Gives the type currently mapped to this event data type string.
     * @param eventType the event data type identifier string, e.g. "Contoso.Items.ItemRecieved".
     *
     * @return the class of the data type mapped to this string, null if not found.
     */
    public Class<?> getDataMapping(String eventType) {
        // TODO: implement method
        return null;
    }

    /**
     * Returns whether this event data type has a mapping.
     * @param eventType the event data type identifier string, e.g. "Contoso.Items.ItemRecieved".
     *
     * @return true iff there is a mapping for this string.
     */
    public boolean containsDataMapping(String eventType) {
        // TODO: implement method
        return false;
    }

    /**
     * Gives all mappings of event data types to data type classes.
     * @return a mapping of all event data types to their respective data ype classes.
     */
    public Map<String, Class<?>> getAllDataMappings() {
        // TODO: implement method
        return null;
    }

    /**
     * Removes a data type mapping and returns whether it was successful.
     * @param eventType the event data type identifier string, e.g. "Contoso.Items.ItemRecieved".
     *
     * @return true iff the mapping was present and then removed, false otherwise.
     */
    public boolean removeDataMapping(String eventType) {
        // TODO: implement method
        return false;
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
