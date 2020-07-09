// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.messaging.eventgrid.models.EventGridEvent;

import java.util.Map;

/**
 * General class that contains common data from all event Schema, and that can be transformed into its proper strongly
 * typed event object.
 */
public class EventSchema {

    /**
     * Gives the id of the event. All events must have an id.
     * @return the id of the event.
     */
    public String getId() {
        // TODO: implement methods
        return null;
    }

    /**
     * Sets the current id.
     * @param id the id to set.
     *
     * @return The EventSchema object itself.
     */
    public EventSchema setId(String id) {
        // TODO: implement methods
        return null;
    }

    /**
     * Gives the event's data. Can be used to try to access data before specific event checks.
     * @return the event data, or null if there is no data.
     */
    Object getData() {
        // TODO: implement methods
        return null;
    }

    /**
     * Sets the event's data. Events where the <code>eventType</code> corresponds to data must have non-null data,
     * however <code>eventType</code> fields that correspond to no data can have null data.
     * @param data the data to set.
     *
     * @return the Event Object itself.
     */
    public EventSchema setData(Object data) {
        // TODO: implement methods
        return null;
    }

    /**
     * Gives the event's data type, e.g. "Contoso.Items.ItemReceived". In the CloudEvent Schema this corresponds
     * to the <code>type</code> field. All events must have an event type field.
     * @return the event type string.
     */
    public String getEventType() {
        // TODO: implement methods
        return null;
    }

    /**
     * Sets the event's data type, e.g. "Contoso.Items.ItemReceived". In the CloudEvent Schema this corresponds
     * to the "type" field.
     * @param eventType the event type signature to set.
     *
     * @return the Event Object itself.
     */
    public EventSchema setEventType(String eventType) {
        // TODO: implement methods
        return null;
    }

    /**
     * Gives all the additional fields in this event that cannot be accessed through the other methods.
     * @return a map from the field's name to the value it holds.
     */
    public Map<String, Object> additionalFields() {
        // TODO: implement methods
        return null;
    }

}
