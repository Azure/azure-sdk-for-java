package com.azure.messaging.eventhubs.implementation;

import org.apache.qpid.proton.message.Message;

/**
 * Serializer to translate objects to and from proton-j {@link Message messages}.
 */
public interface MessageSerializer {
    /**
     * Gets the serialized size of an AMQP message
     *
     * @param amqpMessage AMQP message to serialize.
     * @return The size, in bytes, of the serialized AMQP message.
     */
    int getSize(Message amqpMessage);

    /**
     * Serializes the given {@code object} into an AMQP message.
     *
     * @param object Object to send to AMQP service.
     * @param clazz Class of the {@code object} to serialize.
     * @param <T> Type of the object to serialize into an AMQP message.
     *
     * @return An AMQP message that can be sent to the service.
     */
    <T> Message serialize(T object, Class<T> clazz);

    /**
     * Deserializes the AMQP message to a concrete object.
     *
     * @param message AMQP message to deserialize.
     * @param clazz Class to deserialize object into.
     * @param <T> Type of the deserialized message.
     *
     * @return The deserialized object from an AMQP message or {@code null} if it cannot be deserialized.
     */
    <T> T deserialize(Message message, Class<T> clazz);
}
