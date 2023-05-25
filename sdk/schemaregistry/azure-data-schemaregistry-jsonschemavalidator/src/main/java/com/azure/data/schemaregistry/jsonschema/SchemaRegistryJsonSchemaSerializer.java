// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.models.MessageContent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import reactor.core.publisher.Mono;

public final class SchemaRegistryJsonSchemaSerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryJsonSchemaSerializer.class);

    SchemaRegistryJsonSchemaSerializer(SchemaRegistryAsyncClient schemaRegistryClient,
        SerializerOptions serializerOptions) {
    }

    /**
     * Serializes the object into a message.  Tries to infer the schema definition based on the object.  If no schema
     * cannot be inferred, no validation is performed.
     *
     * @param object Object to serialize.
     * @param typeReference Type reference of message to create.
     * @param <T> Type of message to serialize.
     *
     * @return The object serialized into a message content.  If the inferred schema definition does not exist or the
     *     object does not match the existing schema definition, an exception is thrown.
     */
    public <T extends MessageContent> T serialize(Object object, TypeReference<T> typeReference) {
        return serializeAsync(object, typeReference).block();
    }

    /**
     * Serializes the object into a message.  Tries to infer the schema definition based on the object.  If no schema
     * cannot be inferred, no validation is performed.
     *
     * @param object Object to serialize.
     * @param typeReference Type reference of message to create.
     * @param <T> Type of message to serialize.
     *
     * @return The object serialized into a message content.  If the inferred schema definition does not exist or the
     *     object does not match the existing schema definition, an exception is thrown.
     */
    public <T extends MessageContent> Mono<T> serializeAsync(Object object,
        TypeReference<T> typeReference) {

        return Mono.empty();
    }

    /**
     * Deserializes a message into its object.  If there is a schema defined in {@link MessageContent#getContentType()},
     * it will fetch  the schema and validate.
     *
     * @param message Message to deserialize.
     * @param typeReference Type reference of object.
     * @param <T> Type of object to deserialize.
     *
     * @return The message deserialized into its object.  If the schema definition is defined but does not exist or the
     *     object does not match the existing schema definition, an exception is thrown.
     */
    public <T> T deserialize(MessageContent message, TypeReference<T> typeReference) {
        return deserializeAsync(message, typeReference).block();
    }

    /**
     * Deserializes a message into its object.  If there is a schema defined in {@link MessageContent#getContentType()},
     * it will fetch  the schema and validate.
     *
     * @param message Message to deserialize.
     * @param typeReference Type reference of object.
     * @param <T> Type of object to deserialize.
     *
     * @return Mono that completes when the message deserialized into its object.  If the schema definition is defined
     *     but does not exist or the object does not match the existing schema definition, an exception is thrown.
     */
    public <T> Mono<T> deserializeAsync(MessageContent message, TypeReference<T> typeReference) {
        return Mono.empty();
    }
}
