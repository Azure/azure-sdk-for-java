package com.azure.messaging.eventgrid;

import reactor.core.publisher.Flux;

/**
 * Deserializer class for consuming events from EventGrid at an endpoint, such as Azure Functions or WebHooks.
 * Contains built-in support for all Azure system events, and support for adding custom events and event schemas.
 */
public class EventGridSubscriber {

    /**
     * Asynchronously deserializes events, returning the events with the correct schema and data type. The data type
     * must be mapped to a class using the putMapping method.
     * @param json the event to be decoded from json format.
     *
     * @return a stream containing the decoded events.
     */
    public Flux<EventSchema> deserialize(String json) {
        return null;
    }

    /**
     * Adds the given eventType field and maps it to the given class. The class type must be Json serializable via
     * the Jackson deserializer. All Azure system events are already mapped.
     * @param eventType the string corresponding to the event type signature, e.g. "Contoso.Items.ItemRecieved".
     * @param typeClass the Json serializable class corresponding to the data type.
     * @param <T>       the type corresponding to the class.
     *
     * @return the previous mapping to this eventType, if any.
     */
    public <T> Class<T> putMapping(String eventType, Class<T> typeClass) {
        return null;
    }

    /**
     * Adds the specified schema to be decoded. The class must be Json serializable as well as contain unique fields
     * to any other mapped classes. CloudEvent and EventGridEvent schema are already mapped.
     * @param eventSchema the class type to add to be mapped.
     *
     * @return true iff this class was not already mapped.
     */
    public boolean putCustomSchema(Class<? extends EventSchema> eventSchema) {
        return false;
    }
}
