// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.ContentType;
import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Class that serializes and deserializes objects using <a href="https://json-schema.org/">JSON schema</a>.
 *
 * @see SchemaRegistryJsonSchemaSerializerBuilder
 */
public final class SchemaRegistryJsonSchemaSerializer {
    private static final String CONTENT_TYPE = ContentType.APPLICATION_JSON;
    private static final SerializerEncoding ENCODING = SerializerEncoding.JSON;

    private static final ClientLogger LOGGER = new ClientLogger(SchemaRegistryJsonSchemaSerializer.class);
    private final SchemaRegistrySchemaCache schemaCache;
    private final JsonSchemaGenerator schemaGenerator;
    private final JsonSerializer jsonSerializer;

    SchemaRegistryJsonSchemaSerializer(SchemaRegistryAsyncClient schemaRegistryAsyncClient,
        SchemaRegistryClient schemaRegistryClient, JsonSchemaGenerator schemaGenerator,
        SerializerOptions serializerOptions) {

        Objects.requireNonNull(serializerOptions, "'serializerOptions' cannot be null.");

        this.schemaGenerator = Objects.requireNonNull(schemaGenerator, "'schemaGenerator' cannot be null.");
        this.jsonSerializer = serializerOptions.getJsonSerializer();
        this.schemaCache = new SchemaRegistrySchemaCache(schemaRegistryAsyncClient, schemaRegistryClient,
            serializerOptions.getSchemaGroup(), serializerOptions.autoRegisterSchemas(),
            serializerOptions.getMaxCacheSize());
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
        return serialize(object, typeReference, null);
    }

    /**
     * Serializes an object into a message.
     *
     * @param object Object to serialize.
     * @param typeReference Type of message to create.
     * @param messageFactory Factory to create an instance given the serialized Avro.
     * @param <T> Concrete type of {@link MessageContent}.
     *
     * @return The message encoded or {@code null} if the message could not be serialized.
     * @throws IllegalArgumentException if {@code messageFactory} is null and type {@code T} does not have a no
     *     argument constructor. Or if the schema could not be fetched from {@code T}.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     * @throws ResourceNotFoundException if the schema could not be found and
     *     {@link SchemaRegistryJsonSchemaSerializerBuilder#autoRegisterSchemas(boolean)} is false.
     * @throws HttpResponseException if an error occurred while trying to fetch the schema from the service.
     */
    public <T extends MessageContent> T serialize(Object object, TypeReference<T> typeReference,
        Function<BinaryData, T> messageFactory) {

        final String schemaFullName = object.getClass().getName();
        final Function<BinaryData, T> resolvedMessageFactory = getMessageContentFactory(typeReference, messageFactory);
        final String schemaDefinition = getSchemaDefinition(object);

        final BinaryData serializedBytes = BinaryData.fromBytes(jsonSerializer.serializeToBytes(object));
        final T serializedMessage = resolvedMessageFactory.apply(serializedBytes);

        final String schemaId = schemaCache.getSchemaId(schemaFullName, schemaDefinition);
        serializedMessage.setContentType(CONTENT_TYPE + "+" + schemaId);

        return serializedMessage;
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
    public <T extends MessageContent> Mono<T> serializeAsync(Object object, TypeReference<T> typeReference) {
        return serializeAsync(object, typeReference, null);
    }

    /**
     * Serializes an object into a message.
     *
     * @param object Object to serialize.
     * @param typeReference Type of message to create.
     * @param messageFactory Factory to create an instance given the serialized Avro. If null is passed in, then the
     *     no argument constructor will be used.
     * @param <T> Concrete type of {@link MessageContent}.
     *
     * @return A Mono that completes with the serialized message.
     * @throws IllegalArgumentException if {@code messageFactory} is null and type {@code T} does not have a no
     *     argument constructor. Or if the schema could not be fetched from {@code T}.
     * @throws IllegalStateException if
     *     {@link SchemaRegistryJsonSchemaSerializerBuilder#schemaGroup(String) schemaGroup} is not set.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     * @throws ResourceNotFoundException if the schema could not be found and
     *     {@link SchemaRegistryJsonSchemaSerializerBuilder#autoRegisterSchemas(boolean)} is false.
     * @throws HttpResponseException if an error occurred while trying to fetch the schema from the service.
     */
    public <T extends MessageContent> Mono<T> serializeAsync(Object object,
        TypeReference<T> typeReference, Function<BinaryData, T> messageFactory) {

        final String schemaFullName = object.getClass().getName();
        final Function<BinaryData, T> resolvedMessageFactory = getMessageContentFactory(typeReference, messageFactory);

        final String schemaDefinition;
        try {
            schemaDefinition = getSchemaDefinition(object);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }

        return jsonSerializer.serializeToBytesAsync(object)
            .flatMap(messageBytes -> {
                final T serializedMessage = resolvedMessageFactory.apply(BinaryData.fromBytes(messageBytes));

                return schemaCache.getSchemaIdAsync(schemaFullName, schemaDefinition)
                    .handle((schemaId, sink) -> {
                        try {
                            serializedMessage.setContentType(CONTENT_TYPE + "+" + schemaId);

                            sink.next(serializedMessage);
                        } catch (Exception e) {
                            LOGGER.atError()
                                .addKeyValue("schemaId", schemaId)
                                .addKeyValue("type", schemaFullName)
                                .log(() -> "Error encountered serializing object", e);

                            sink.error(e);
                        }
                    });
            });
    }

    /**
     * Deserializes a message into its object.  If there is a schema defined in {@link MessageContent#getContentType()},
     * it will fetch the schema and validate.
     *
     * @param message Message to deserialize.
     * @param typeReference Type reference of object.
     * @param <T> Type of object to deserialize.
     *
     * @return The message deserialized into its object.  {@code null} if there is no contents in the message. If the
     *     schema definition is defined but does not exist or the object does not match the existing schema definition,
     *     an exception is thrown.
     */
    public <T> T deserialize(MessageContent message, TypeReference<T> typeReference) {
        if (message == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException("'message' cannot be null."));
        } else if (typeReference == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'typeReference' cannot be null."));
        }

        final String schemaId = extractAndValidateSchemaId(message);

        final BinaryData body = message.getBodyAsBinaryData();
        if (Objects.isNull(body)) {
            LOGGER.warning("Message provided does not have a BinaryBody, returning empty response.");
            return null;
        }

        if (body.getLength() == null || body.getLength() == 0) {
            LOGGER.warning("Message provided has an empty byte[], returning empty response.");
            return null;
        }

        final T deserialized = jsonSerializer.deserialize(body.toStream(), typeReference);
        if (deserialized == null) {
            return null;
        }

        final String schemaDefinition = schemaCache.getSchemaDefinition(schemaId);

        if (isValid(deserialized, typeReference, schemaDefinition)) {
            return deserialized;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format("Deserialized JSON object"
                    + "does not match schema. Type: %s%nActual: %s%nDefinition: %s.",
                typeReference.getJavaClass(), deserialized, schemaDefinition)));
        }
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
     * @throws NullPointerException if {@code message} or {@code typeReference} is null.
     * @throws IllegalArgumentException if the message's content type is empty/null or does not contain
     *     {@link ContentType#APPLICATION_JSON}.
     */
    public <T> Mono<T> deserializeAsync(MessageContent message, TypeReference<T> typeReference) {
        if (message == null) {
            return monoError(LOGGER, new NullPointerException("'message' cannot be null."));
        } else if (typeReference == null) {
            return monoError(LOGGER, new NullPointerException("'typeReference' cannot be null."));
        }

        final String schemaId;
        try {
            schemaId = extractAndValidateSchemaId(message);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }

        final BinaryData body = message.getBodyAsBinaryData();
        if (Objects.isNull(body)) {
            LOGGER.warning("Message provided does not have a BinaryBody, returning empty response.");
            return Mono.empty();
        }

        if (body.getLength() == null || body.getLength() == 0) {
            LOGGER.warning("Message provided has an empty byte[], returning empty response.");
            return Mono.empty();
        }

        return jsonSerializer.deserializeAsync(body.toStream(), typeReference)
            .flatMap(decoded -> {
                return this.schemaCache.getSchemaDefinitionAsync(schemaId)
                    .handle((schemaDefinition, sink) -> {
                        final String schemaFullName = typeReference.getJavaClass().getName();

                        final boolean isValid;
                        try {
                            isValid = schemaGenerator.isValid(decoded, typeReference, schemaDefinition);
                        } catch (Exception e) {
                            LOGGER.atError()
                                .addKeyValue("type", schemaFullName)
                                .addKeyValue("schemaDefinition", schemaDefinition)
                                .log("Validating schema threw an error.", e);

                            sink.error(e);
                            return;
                        }

                        if (isValid) {
                            sink.next(decoded);
                        } else {
                            sink.error(new IllegalArgumentException(String.format("Deserialized JSON object"
                                    + "does not match schema. Type: %s%nActual: %s%nDefinition: %s.",
                                schemaFullName, decoded, schemaDefinition)));
                        }
                    });
            });
    }

    private <T> boolean isValid(T deserialized, TypeReference<T> typeReference, String schemaDefinition) {
        if (deserialized == null) {
            return true;
        }

        try {
            return schemaGenerator.isValid(deserialized, typeReference, schemaDefinition);
        } catch (RuntimeException e) {
            LOGGER.atError()
                .addKeyValue("type", deserialized.getClass().getName())
                .addKeyValue("schemaDefinition", schemaDefinition)
                .log("Validating schema threw an error.", e);

            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Gets the schema definition for an object.
     *
     * @param object Object to get schema definition for.
     * @return The schema definition.
     * @throws IllegalArgumentException Error occurs when trying to generate the schema definition.
     * @throws NullPointerException If the schema is null.
     */
    private String getSchemaDefinition(Object object) {
        final String schemaFullName = object.getClass().getName();

        String schemaDefinition;
        try {
            schemaDefinition = schemaGenerator.generateSchema(TypeReference.createInstance(object.getClass()));
        } catch (Exception exception) {
            LOGGER.atError()
                .addKeyValue("type", schemaFullName)
                .log(() -> "An error occurred while attempting to generate the schema.", exception);
            throw LOGGER.logThrowableAsError(new IllegalArgumentException(exception));
        }

        if (schemaDefinition == null) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("JSON schema cannot be null. Type: "
                + schemaFullName));
        }

        return schemaDefinition;
    }

    private static String extractAndValidateSchemaId(MessageContent message) {
        if (CoreUtils.isNullOrEmpty(message.getContentType())) {
            throw new IllegalArgumentException("Cannot deserialize message with no content-type.");
        }

        // It is the new format, so we parse the mime-type.
        final String[] parts = message.getContentType().split("\\+");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Content type was not in the expected format of MIME type + schema ID. Actual: "
                    + message.getContentType());
        }

        if (!CONTENT_TYPE.equalsIgnoreCase(parts[0])) {
            throw new IllegalArgumentException(
                "Json deserialization may only be used on content that is of '" + CONTENT_TYPE + "' type. Actual: "
                    + message.getContentType());
        }

        return parts[1];
    }

    /**
     * Resolves the function to use to instantiate the {@link MessageContent}.  It tries to find the no argument
     * constructor to instantiate object.  If a constructor cannot be found, uses the {@code messageFactory}.
     *
     * @param typeReference Type of {@link MessageContent} to get constructor for.
     * @param messageFactory Optional factory to use.
     * @return A function to create the {@link MessageContent} given {@link BinaryData}.
     * @param <T> Type of message content to return.
     * @throws IllegalArgumentException If type does not have a no-arg constructor and messageFactory is null.
     */
    private static <T extends MessageContent> Function<BinaryData, T> getMessageContentFactory(
        TypeReference<T> typeReference, Function<BinaryData, T> messageFactory) {

        final Optional<Constructor<?>> constructor =
            Arrays.stream(typeReference.getJavaClass().getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == 0)
                .findFirst();

        if (!constructor.isPresent() && messageFactory == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(typeReference.getJavaClass()
                + "does not have have a no-arg constructor to create a new instance of T with. Use the overload that"
                + "accepts 'messageFactory'."));
        }

        return messageFactory != null ? messageFactory
            : binaryData -> {
            final T instance = createNoArgumentInstance(typeReference);
            instance.setBodyAsBinaryData(binaryData);

            return instance;
        };
    }

    /**
     * Instantiates an instance of T with no arg constructor.
     *
     * @param typeReference Type reference of the class to instantiate.
     * @param <T> The type T to create.
     *
     * @return T with the given {@code binaryData} set.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated.
     */
    @SuppressWarnings("unchecked")
    private static <T extends MessageContent> T createNoArgumentInstance(TypeReference<T> typeReference) {

        final Optional<Constructor<?>> constructor =
            Arrays.stream(typeReference.getJavaClass().getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == 0)
                .findFirst();

        if (!constructor.isPresent()) {
            throw new IllegalArgumentException(typeReference.getJavaClass() + "does not have have a no-arg "
                + "constructor to create a new instance of T with. Use the overload that accepts 'messageFactory'.");
        }

        Object newObject;
        try {
            newObject = constructor.get().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format(
                "Could not instantiate '%s' with no-arg constructor.", typeReference.getJavaClass()), e);
        }

        if (!typeReference.getJavaClass().isInstance(newObject)) {
            throw new RuntimeException(String.format(
                "Constructed '%s' object was not an instanceof T '%s'.", newObject, typeReference.getJavaClass()));
        } else {
            return (T) newObject;
        }
    }
}
