package com.azure.messaging.eventgrid;

/**
 * Interface defining properties that all Event Schema must implement in order to be sent to EventGrid.
 * In addition, all EventSchema must be Json serializable.
 */
public interface EventSchema {

    /**
     * Gives the event's data. All event schema must contain data that can be accessed this way.
     * @return the event data.
     */
    Object getData();

    /**
     * Sets the event's data. All event schema must have data that can be set.
     * @param data the data to set.
     *
     * @return the Event Object itself.
     */
    EventSchema setData(Object data);
}
