package com.azure.messaging.eventgrid;

import com.azure.core.experimental.serializer.JsonSerializer;

public class EventGridConsumerBuilder {

    public EventGridConsumerBuilder() {
        // TODO: implement method
    }

    /**
     * Constructs an instance of the consumer for use in deserialization.
     * @return an instance of the EventConsumer with the current settings as well as the standard system event data
     * mappings.
     */
    public EventGridConsumer build() {
        // TODO: implement method
        return null;
    }

    /**
     * Add a mapping from the event data type string to the object class for the deserializer to deserialize.
     * @param eventType the event data type identifier string, e.g. "Contoso.Items.ItemRecieved". In EventGridEvents,
     *                  this is the <code>eventType</code> field. In CloudEvents, this is the <code>type</code> field.
     * @param dataType  the data type class of the object to be decoded.
     * @param <T>       the type of the data to be decoded. This corresponds to the data field. The object must
     *                  have proper JsonProperty annotations to allow for deserialization.
     *
     * @return the builder itself.
     */
    public <T> EventGridConsumerBuilder putDataMapping(String eventType, Class<T> dataType) {
        // TODO: implement method
        return this;
    }

    /**
     * Add a custom deserializer to interpret the json as a rich event object. If this value is not set, the default
     * is loaded from the classpath.
     * @param jsonSerializer the serializer to use.
     *
     * @return the builder itself.
     */
    public EventGridConsumerBuilder serializer(JsonSerializer jsonSerializer) {
        // TODO: implement method
        return this;
    }


}
