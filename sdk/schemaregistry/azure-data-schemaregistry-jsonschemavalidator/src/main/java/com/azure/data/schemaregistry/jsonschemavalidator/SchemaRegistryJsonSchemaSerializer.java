// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschemavalidator;

import com.azure.core.models.MessageContent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.jsonschemavalidator.models.SerializationResult;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;

public final class SchemaRegistryJsonSchemaSerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryJsonSchemaSerializer.class);

    SchemaRegistryJsonSchemaSerializer(SchemaRegistryAsyncClient schemaRegistryClient,
        SerializerOptions serializerOptions) {
    }

    public <T extends MessageContent> T serialize(Object object, TypeReference<T> typeReference) {
        return serializeAsync(object, typeReference).block();
    }

    public <T extends MessageContent> SerializationResult<T> serializeWithValidation(
        Object object, TypeReference<T> typeReference, String schemaId) {
        return serializeWithValidationAsync(object, typeReference, schemaId).block();
    }

    public <T extends MessageContent> Mono<T> serializeAsync(Object object,
        TypeReference<T> typeReference) {

        return Mono.empty();
    }

    public <T extends MessageContent> Mono<SerializationResult<T>> serializeWithValidationAsync(
        Object object, TypeReference<T> typeReference, String schemaId) {

        if (object == null) {
            return monoError(logger, new NullPointerException(
                "Null object, behavior should be defined in concrete serializer implementation."));
        } else if (typeReference == null) {
            return monoError(logger, new NullPointerException("'typeReference' cannot be null."));
        }

        return Mono.empty();
    }

    public <T> T deserialize(MessageContent message, TypeReference<T> typeReference) {
        return deserializeAsync(message, typeReference).block();
    }

    public <T> SerializationResult<T> deserializeWithValidation(MessageContent message, TypeReference<T> typeReference) {
        return deserializeWithValidationAsync(message, typeReference).block();
    }

    public <T> Mono<T> deserializeAsync(MessageContent message, TypeReference<T> typeReference) {
        return Mono.empty();
    }

    public <T> Mono<SerializationResult<T>> deserializeWithValidationAsync(MessageContent message,
        TypeReference<T> typeReference) {
        return Mono.empty();
    }
}
