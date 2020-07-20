package com.azure.messaging.eventgrid;

public class EventGridConsumerBuilder {

    public EventGridConsumerBuilder() {
        // TODO: implement method
    }

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


}
