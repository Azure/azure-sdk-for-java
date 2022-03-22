// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.experimental.models.MessageWithMetadata;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import org.apache.avro.Schema;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Schema Registry-based serializer implementation for Avro data format using Apache Avro.
 */
public final class SchemaRegistryApacheAvroSerializer {
    static final String AVRO_MIME_TYPE = "avro/binary";
    static final byte[] RECORD_FORMAT_INDICATOR = new byte[]{0x00, 0x00, 0x00, 0x00};
    static final int RECORD_FORMAT_INDICATOR_SIZE = RECORD_FORMAT_INDICATOR.length;
    static final int SCHEMA_ID_SIZE = 32;

    private final ClientLogger logger = new ClientLogger(SchemaRegistryApacheAvroSerializer.class);
    private final SchemaRegistryAsyncClient schemaRegistryClient;
    private final AvroSerializer avroSerializer;
    private final SerializerOptions serializerOptions;

    /**
     * Creates a new instance.
     *
     * @param schemaRegistryClient Client that interacts with Schema Registry.
     * @param avroSerializer Serializer implemented using Apache Avro.
     * @param serializerOptions Options to configure the serializer with.
     */
    SchemaRegistryApacheAvroSerializer(SchemaRegistryAsyncClient schemaRegistryClient,
        AvroSerializer avroSerializer, SerializerOptions serializerOptions) {
        this.schemaRegistryClient = Objects.requireNonNull(schemaRegistryClient,
            "'schemaRegistryClient' cannot be null.");
        this.avroSerializer = Objects.requireNonNull(avroSerializer,
            "'avroSerializer' cannot be null.");
        this.serializerOptions = Objects.requireNonNull(serializerOptions, "'serializerOptions' cannot be null.");
    }

    /**
     * Serializes an object into a message.
     *
     * @param object Object to serialize.
     * @param typeReference Type of message to create.
     * @param <T> Concrete type of {@link MessageWithMetadata}.
     *
     * @return The message encoded or {@code null} if the message could not be serialized.
     *
     * @throws IllegalArgumentException if {@code messageFactory} is null and type {@code T} does not have a no
     *     argument constructor. Or if the schema could not be fetched from {@code T}.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated. Or there was a problem
     *     encoding the object.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     */
    public <T extends MessageWithMetadata> T serializeMessageData(Object object, TypeReference<T> typeReference) {
        return serializeMessageDataAsync(object, typeReference).block();
    }

    /**
     * Serializes an object into a message.
     *
     * @param object Object to serialize.
     * @param typeReference Type of message to create.
     * @param messageFactory Factory to create an instance given the serialized Avro.
     * @param <T> Concrete type of {@link MessageWithMetadata}.
     *
     * @return The message encoded or {@code null} if the message could not be serialized.
     *
     * @throws IllegalArgumentException if {@code messageFactory} is null and type {@code T} does not have a no
     *     argument constructor. Or if the schema could not be fetched from {@code T}.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated. Or there was a problem
     *     encoding the object.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     */
    public <T extends MessageWithMetadata> T serializeMessageData(Object object, TypeReference<T> typeReference,
        Function<BinaryData, T> messageFactory) {
        return serializeMessageDataAsync(object, typeReference, messageFactory).block();
    }

    /**
     * Serializes an object into a message.
     *
     * @param object Object to serialize.
     * @param typeReference Type of message to create.
     * @param <T> Concrete type of {@link MessageWithMetadata}.
     *
     * @return A Mono that completes with the serialized message.
     *
     * @throws IllegalArgumentException if {@code messageFactory} is null and type {@code T} does not have a no
     *     argument constructor. Or if the schema could not be fetched from {@code T}.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated. Or there was a problem
     *     encoding the object.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     */
    public <T extends MessageWithMetadata> Mono<T> serializeMessageDataAsync(Object object,
        TypeReference<T> typeReference) {

        return serializeMessageDataAsync(object, typeReference, null);
    }

    /**
     * Serializes an object into a message.
     *
     * @param object Object to serialize.
     * @param typeReference Type of message to create.
     * @param messageFactory Factory to create an instance given the serialized Avro. If null is passed in, then the
     *     no argument constructor will be used.
     * @param <T> Concrete type of {@link MessageWithMetadata}.
     *
     * @return A Mono that completes with the serialized message.
     *
     * @throws IllegalArgumentException if {@code messageFactory} is null and type {@code T} does not have a no
     *     argument constructor. Or if the schema could not be fetched from {@code T}.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated. Or there was a problem
     *     encoding the object.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     */
    public <T extends MessageWithMetadata> Mono<T> serializeMessageDataAsync(Object object,
        TypeReference<T> typeReference, Function<BinaryData, T> messageFactory) {

        if (object == null) {
            return monoError(logger, new NullPointerException(
                "Null object, behavior should be defined in concrete serializer implementation."));
        } else if (typeReference == null) {
            return monoError(logger, new NullPointerException("'typeReference' cannot be null."));
        }

        final Optional<Constructor<?>> constructor =
            Arrays.stream(typeReference.getJavaClass().getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == 0)
                .findFirst();

        if (!constructor.isPresent() && messageFactory == null) {
            return Mono.error(new IllegalArgumentException(typeReference.getJavaClass() + "does not have have a no-arg "
                + "constructor to create a new instance of T with. Use the overload that accepts 'messageFactory'."));
        }

        final Function<BinaryData, T> messageFactoryToUse = messageFactory != null ? messageFactory
            : binaryData -> {
                final T instance = createNoArgumentInstance(typeReference);
                instance.setBodyAsBinaryData(binaryData);

                return instance;
            };

        Schema schema;
        try {
            schema = AvroSerializer.getSchema(object);
        } catch (IllegalArgumentException exception) {
            return monoError(logger, exception);
        }

        final String schemaFullName = schema.getFullName();
        final String schemaString = schema.toString();

        return maybeRegisterSchema(serializerOptions.getSchemaGroup(), schemaFullName, schemaString)
            .handle((schemaId, sink) -> {
                try {
                    final byte[] encoded = avroSerializer.encode(object);
                    final T serializedMessage = messageFactoryToUse.apply(BinaryData.fromBytes(encoded));

                    serializedMessage.setContentType(AVRO_MIME_TYPE + "+" + schemaId);

                    sink.next(serializedMessage);
                    sink.complete();
                } catch (Exception e) {
                    sink.error(new RuntimeException(String.format(
                        "Error encountered serializing object: %s with schemaId '%s'.", object, schemaId), e));
                }
            });
    }

    /**
     * Deserializes a message into its object.
     *
     * @param message Object to deserialize.
     * @param typeReference Message type to deserialize to.
     * @param <T> Concrete type of {@link MessageWithMetadata}.
     *
     * @return The message deserialized.
     *
     * @throws NullPointerException if {@code message} or {@code typeReference} is null.
     */
    public <T> T deserializeMessageData(MessageWithMetadata message, TypeReference<T> typeReference) {
        return deserializeMessageDataAsync(message, typeReference).block();
    }

    /**
     * Deserializes a message into its object.
     *
     * @param message Object to deserialize.
     * @param typeReference Message to deserialize to.
     * @param <T> Concrete type of {@link MessageWithMetadata}.
     *
     * @return A Mono that completes when the message encoded. If {@code message.getBodyAsBinaryData()} is null or
     *     empty, then an empty Mono is returned.
     *
     * @throws NullPointerException if {@code message} or {@code typeReference} is null.
     */
    public <T> Mono<T> deserializeMessageDataAsync(MessageWithMetadata message, TypeReference<T> typeReference) {
        if (message == null) {
            return monoError(logger, new NullPointerException("'message' cannot be null."));
        } else if (typeReference == null) {
            return monoError(logger, new NullPointerException("'typeReference' cannot be null."));
        }

        final BinaryData body = message.getBodyAsBinaryData();

        if (Objects.isNull(body)) {
            logger.warning("Message provided does not have a BinaryBody, returning empty response.");
            return Mono.empty();
        }

        final ByteBuffer contents = body.toByteBuffer();

        if (contents.remaining() == 0) {
            logger.warning("Message provided has an empty BinaryBody, returning empty response.");
            return Mono.empty();
        }

        final String schemaId;

        // Temporary back-compat for the first beta while we phase this out. In the future, it will return an error.
        // Check if the first 4 bytes of the payload have the format.
        final byte[] recordFormatIndicator = new byte[RECORD_FORMAT_INDICATOR_SIZE];
        contents.mark();

        // Don't try to get 4 bytes if there isn't enough, so we don't get a BufferUnderflowException.
        final boolean hasPreamble;
        if (contents.remaining() < RECORD_FORMAT_INDICATOR_SIZE) {
            hasPreamble = false;
        } else {
            contents.get(recordFormatIndicator);
            hasPreamble = Arrays.equals(RECORD_FORMAT_INDICATOR, recordFormatIndicator);
        }

        if (hasPreamble) {
            final byte[] schemaGuidByteArray = new byte[SCHEMA_ID_SIZE];
            contents.get(schemaGuidByteArray);

            schemaId = new String(schemaGuidByteArray, StandardCharsets.UTF_8);
        } else {
            if (CoreUtils.isNullOrEmpty(message.getContentType())) {
                return monoError(logger, new IllegalArgumentException("Cannot deserialize message with no content-type."));
            }

            // It is the new format, so we parse the mime-type.
            final String[] parts = message.getContentType().split("\\+");
            if (parts.length != 2) {
                return monoError(logger, new IllegalArgumentException(
                    "Content type was not in the expected format of MIME type + schema ID. Actual: "
                        + message.getContentType()));
            }

            if (!AVRO_MIME_TYPE.equalsIgnoreCase(parts[0])) {
                return monoError(logger, new IllegalArgumentException(
                    "An avro encoder may only be used on content that is of 'avro/binary' type. Actual: "
                        + message.getContentType()));
            }

            schemaId = parts[1];

            // There is no header so reset back to where we marked the buffer before starting to look for the preamble.
            contents.reset();
        }

        return deserializeMessageDataAsync(schemaId, contents, typeReference);
    }

    private <T> Mono<T> deserializeMessageDataAsync(String schemaId, ByteBuffer buffer, TypeReference<T> typeReference) {
        return this.schemaRegistryClient.getSchema(schemaId)
            .handle((registryObject, sink) -> {
                final byte[] payloadSchema = registryObject.getDefinition().getBytes(StandardCharsets.UTF_8);
                final T decode = avroSerializer.decode(buffer, payloadSchema, typeReference);
                sink.next(decode);
            });
    }

    /**
     * Instantiates an instance of T with no arg constructor.
     *
     * @param typeReference Type reference of the class to instantiate.
     * @param <T> The type T to create.
     *
     * @return T with the given {@code binaryData} set.
     *
     * @throws RuntimeException if an instance of {@code T} could not be instantiated.
     */
    @SuppressWarnings("unchecked")
    private static <T extends MessageWithMetadata> T createNoArgumentInstance(TypeReference<T> typeReference) {

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

    /**
     * If auto-registering is enabled, register schema against Schema Registry. If auto-registering is disabled, fetch
     * schema ID for provided schema. Requires pre-registering of schema against registry.
     *
     * @param schemaGroup Schema group where schema should be registered.
     * @param schemaName name of schema
     * @param schemaString string representation of schema being stored - must match group schema type
     *
     * @return string representation of schema ID
     */
    private Mono<String> maybeRegisterSchema(String schemaGroup, String schemaName, String schemaString) {
        if (serializerOptions.autoRegisterSchemas()) {
            return this.schemaRegistryClient
                .registerSchema(schemaGroup, schemaName, schemaString, SchemaFormat.AVRO)
                .map(SchemaProperties::getId);
        } else {
            return this.schemaRegistryClient.getSchemaProperties(
                schemaGroup, schemaName, schemaString, SchemaFormat.AVRO).map(properties -> properties.getId());
        }
    }
}

