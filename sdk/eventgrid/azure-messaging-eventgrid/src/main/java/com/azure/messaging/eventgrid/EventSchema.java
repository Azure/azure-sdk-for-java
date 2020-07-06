package com.azure.messaging.eventgrid;

/**
 * Interface defining properties that all Event Schema must implement in order to be sent to EventGrid.
 * In addition, all EventSchema must be Json serializable.
 */
public interface EventSchema {

    /**
     * Returns whether this event type is an CloudEvent or a subclass of it.
     * @return true iff this object is of type CloudEvent or a subclass of it.
     */
    boolean isCloudEvent();

    /**
     * Returns whether this event type is an EventGridEvent or a subclass of it.
     * @return true iff this object is of type EventGridEvent or a subclass of it.
     */
    boolean isEventGridEvent();

    /**
     * Returns whether this event type has custom event properties, indicating that it is a custom event schema.
     * Any implementer of this interface that is not specifically EventGridEvent or CloudEvent must return true for
     * this method. Note that this includes any subclass of EventGridEvent or CloudEvent, meaning that a subclass of
     * EventGridEvent or CloudEvent must return true for both its respective superclass checker as well as this method.
     * @return true iff this object is not exactly of type EventGridEvent or CloudEvent.
     */
    boolean isCustomEvent();

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
