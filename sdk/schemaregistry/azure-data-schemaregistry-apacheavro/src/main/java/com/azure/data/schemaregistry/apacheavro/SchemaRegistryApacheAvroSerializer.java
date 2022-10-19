// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import org.apache.avro.Schema;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Schema Registry-based serializer implementation for Avro data format using Apache Avro.
 *
 * <p><strong>Creating a {@link SchemaRegistryApacheAvroSerializer}</strong></p>
 * <!-- src_embed com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.instantiation -->
 * <pre>
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder&#40;&#41;
 *     .credential&#40;tokenCredential&#41;
 *     .fullyQualifiedNamespace&#40;&quot;&#123;schema-registry-endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * &#47;&#47; By setting autoRegisterSchema to true, if the schema does not exist in the Schema Registry instance, it is
 * &#47;&#47; added to the instance. By default, this is false, so it will error if the schema is not found.
 * SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder&#40;&#41;
 *     .schemaRegistryClient&#40;schemaRegistryAsyncClient&#41;
 *     .autoRegisterSchemas&#40;true&#41;
 *     .schemaGroup&#40;&quot;&#123;schema-group&#125;&quot;&#41;
 *     .buildSerializer&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.instantiation -->
 *
 * <p><strong>Serialize an object</strong></p>
 * Serializes an Avro generated object into {@link MessageContent}. {@link #serialize(Object, TypeReference)} assumes
 * that there is a no argument constructor used to instantiate the {@link MessageContent} type. If there is a different
 * way to instantiate the concrete type, use the overload which takes a message factory function, {@link
 * #serialize(Object, TypeReference, Function)}.
 *
 * <!-- src_embed com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize -->
 * <pre>
 * &#47;&#47; The object to encode. The avro schema is:
 * &#47;&#47; &#123;
 * &#47;&#47;     &quot;namespace&quot;: &quot;com.azure.data.schemaregistry.apacheavro.generatedtestsources&quot;,
 * &#47;&#47;     &quot;type&quot;: &quot;record&quot;,
 * &#47;&#47;     &quot;name&quot;: &quot;Person&quot;,
 * &#47;&#47;     &quot;fields&quot;: [
 * &#47;&#47;         &#123;&quot;name&quot;:&quot;name&quot;, &quot;type&quot;: &quot;string&quot;&#125;,
 * &#47;&#47;         &#123;&quot;name&quot;:&quot;favourite_number&quot;, &quot;type&quot;: [&quot;int&quot;, &quot;null&quot;]&#125;,
 * &#47;&#47;         &#123;&quot;name&quot;:&quot;favourite_colour&quot;, &quot;type&quot;: [&quot;string&quot;, &quot;null&quot;]&#125;
 * &#47;&#47;   ]
 * &#47;&#47; &#125;
 * Person person = Person.newBuilder&#40;&#41;
 *     .setName&#40;&quot;Alina&quot;&#41;
 *     .setFavouriteColour&#40;&quot;Turquoise&quot;&#41;
 *     .build&#40;&#41;;
 *
 * MessageContent message = serializer.serialize&#40;person,
 *     TypeReference.createInstance&#40;MessageContent.class&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize -->
 *
 * <p><strong>Deserialize an object</strong></p>
 * <!-- src_embed com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize -->
 * <pre>
 * &#47;&#47; Message to deserialize. Assume that the body contains data which has been serialized using an Avro encoder.
 * MessageContent message = new MessageContent&#40;&#41;
 *     .setBodyAsBinaryData&#40;BinaryData.fromBytes&#40;new byte[0]&#41;&#41;
 *     .setContentType&#40;&quot;avro&#47;binary+&#123;schema-id&#125;&quot;&#41;;
 *
 * &#47;&#47; This is an object generated from the Avro schema used in the serialization sample.
 * Person person = serializer.deserialize&#40;message, TypeReference.createInstance&#40;Person.class&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize -->
 *
 * <p><strong>Serialize an object using a message factory</strong></p>
 * Serializes an Avro generated object into {@link MessageContent}. It uses the {@link Function messageFactory} to
 * instantiate and populate the type.
 *
 * <!-- src_embed com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serializeMessageFactory
 * -->
 * <pre>
 * &#47;&#47; The object to encode. The avro schema is:
 * &#47;&#47; &#123;
 * &#47;&#47;     &quot;namespace&quot;: &quot;com.azure.data.schemaregistry.apacheavro.generatedtestsources&quot;,
 * &#47;&#47;     &quot;type&quot;: &quot;record&quot;,
 * &#47;&#47;     &quot;name&quot;: &quot;Person&quot;,
 * &#47;&#47;     &quot;fields&quot;: [
 * &#47;&#47;         &#123;&quot;name&quot;:&quot;name&quot;, &quot;type&quot;: &quot;string&quot;&#125;,
 * &#47;&#47;         &#123;&quot;name&quot;:&quot;favourite_number&quot;, &quot;type&quot;: [&quot;int&quot;, &quot;null&quot;]&#125;,
 * &#47;&#47;         &#123;&quot;name&quot;:&quot;favourite_colour&quot;, &quot;type&quot;: [&quot;string&quot;, &quot;null&quot;]&#125;
 * &#47;&#47;   ]
 * &#47;&#47; &#125;
 * Person person = Person.newBuilder&#40;&#41;
 *     .setName&#40;&quot;Alina&quot;&#41;
 *     .setFavouriteColour&#40;&quot;Turquoise&quot;&#41;
 *     .build&#40;&#41;;
 *
 * &#47;&#47; Serializes and creates an instance of ComplexMessage using the messageFactory function.
 * ComplexMessage message = serializer.serialize&#40;person,
 *     TypeReference.createInstance&#40;ComplexMessage.class&#41;,
 *     &#40;encodedData&#41; -&gt; &#123;
 *         return new ComplexMessage&#40;&quot;unique-id&quot;, OffsetDateTime.now&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serializeMessageFactory -->
 */
public final class SchemaRegistryApacheAvroSerializer {
    static final String AVRO_MIME_TYPE = "avro/binary";

    private final ClientLogger logger = new ClientLogger(SchemaRegistryApacheAvroSerializer.class);
    private final AvroSerializer avroSerializer;
    private final SchemaRegistrySchemaCache schemaCache;

    /**
     * Creates a new instance.
     *
     * @param schemaRegistryClient Client that interacts with Schema Registry.
     * @param avroSerializer Serializer implemented using Apache Avro.
     * @param serializerOptions Options to configure the serializer with.
     */
    SchemaRegistryApacheAvroSerializer(SchemaRegistryAsyncClient schemaRegistryClient, AvroSerializer avroSerializer,
        SerializerOptions serializerOptions) {

        Objects.requireNonNull(schemaRegistryClient, "'schemaRegistryClient' cannot be null.");
        Objects.requireNonNull(serializerOptions, "'serializerOptions' cannot be null.");

        this.avroSerializer = Objects.requireNonNull(avroSerializer, "'avroSerializer' cannot be null.");
        this.schemaCache = new SchemaRegistrySchemaCache(schemaRegistryClient, serializerOptions.getSchemaGroup(),
            serializerOptions.autoRegisterSchemas(), serializerOptions.getMaxCacheSize());
    }

    /**
     * Serializes an object into a message.
     *
     * @param object Object to serialize.
     * @param typeReference Type of message to create.
     * @param <T> Concrete type of {@link MessageContent}.
     *
     * @return The message encoded or {@code null} if the message could not be serialized.
     *
     * @throws IllegalArgumentException if {@code T} does not have a no argument constructor. Or if the schema could
     *     not be fetched from {@code T}.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated.
     * @throws SchemaRegistryApacheAvroException if an instance of {@code T} could not be instantiated or there was
     *     a problem serializing the object.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     * @throws ResourceNotFoundException if the schema could not be found and {@link
     *     SchemaRegistryApacheAvroSerializerBuilder#autoRegisterSchemas(boolean)} is false.
     * @throws HttpResponseException if an error occurred while trying to fetch the schema from the service.
     */
    public <T extends MessageContent> T serialize(Object object, TypeReference<T> typeReference) {
        return serializeAsync(object, typeReference).block();
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
     *
     * @throws IllegalArgumentException if {@code messageFactory} is null and type {@code T} does not have a no
     *     argument constructor. Or if the schema could not be fetched from {@code T}.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     * @throws SchemaRegistryApacheAvroException if the object could not be serialized.
     * @throws ResourceNotFoundException if the schema could not be found and {@link
     *     SchemaRegistryApacheAvroSerializerBuilder#autoRegisterSchemas(boolean)} is false.
     * @throws HttpResponseException if an error occurred while trying to fetch the schema from the service.
     */
    public <T extends MessageContent> T serialize(Object object, TypeReference<T> typeReference,
        Function<BinaryData, T> messageFactory) {
        return serializeAsync(object, typeReference, messageFactory).block();
    }

    /**
     * Serializes an object into a message.
     *
     * @param object Object to serialize.
     * @param typeReference Type of message to create.
     * @param <T> Concrete type of {@link MessageContent}.
     *
     * @return A Mono that completes with the serialized message.
     *
     * @throws IllegalArgumentException if {@code T} does not have a no argument constructor. Or if the schema could
     *     not be fetched from {@code T}.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     * @throws SchemaRegistryApacheAvroException if the object could not be serialized.
     * @throws ResourceNotFoundException if the schema could not be found and {@link
     *     SchemaRegistryApacheAvroSerializerBuilder#autoRegisterSchemas(boolean)} is false.
     * @throws HttpResponseException if an error occurred while trying to fetch the schema from the service.
     */
    public <T extends MessageContent> Mono<T> serializeAsync(Object object,
        TypeReference<T> typeReference) {

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
     *
     * @throws IllegalArgumentException if {@code messageFactory} is null and type {@code T} does not have a no
     *     argument constructor. Or if the schema could not be fetched from {@code T}.
     * @throws IllegalStateException if {@link SchemaRegistryApacheAvroSerializerBuilder#schemaGroup(String)
     *     schemaGroup} is not set.
     * @throws RuntimeException if an instance of {@code T} could not be instantiated.
     * @throws NullPointerException if the {@code object} is null or {@code typeReference} is null.
     * @throws SchemaRegistryApacheAvroException if the object could not be serialized.
     * @throws ResourceNotFoundException if the schema could not be found and {@link
     *     SchemaRegistryApacheAvroSerializerBuilder#autoRegisterSchemas(boolean)} is false.
     * @throws HttpResponseException if an error occurred while trying to fetch the schema from the service.
     */
    public <T extends MessageContent> Mono<T> serializeAsync(Object object,
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

        return this.schemaCache.getSchemaId(schema).handle((schemaId, sink) -> {

            try {
                final byte[] encoded = avroSerializer.serialize(object, schemaId);
                final T serializedMessage = messageFactoryToUse.apply(BinaryData.fromBytes(encoded));

                serializedMessage.setContentType(AVRO_MIME_TYPE + "+" + schemaId);

                sink.next(serializedMessage);
                sink.complete();
            } catch (SchemaRegistryApacheAvroException e) {
                // If an exception happens in the avro library while calling serializer.serialize(object, schemaId)
                // we already wrap in an exception, so we don't want to wrap it again.
                sink.error(e);
            } catch (Exception e) {
                sink.error(new SchemaRegistryApacheAvroException(String.format(
                    "Error encountered serializing object: %s with schemaId '%s'.", object, schemaId), e, schemaId));
            }
        });
    }

    /**
     * Deserializes a message into its object.
     *
     * @param message Object to deserialize.
     * @param typeReference Message type to deserialize to.
     * @param <T> Concrete type of {@link MessageContent}.
     *
     * @return The message deserialized.
     *
     * @throws NullPointerException if {@code message} or {@code typeReference} is null.
     * @throws IllegalArgumentException if the message does not have a content type to use for deserialization. If
     *     the mime-type in the content type cannot be parsed or the type is not avro/binary.
     * @throws ResourceNotFoundException if a schema with a matching schema id could not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     * @throws SchemaRegistryApacheAvroException if the message could not be deserialized.
     * @throws ResourceNotFoundException if the schema could not be found and {@link
     *     SchemaRegistryApacheAvroSerializerBuilder#autoRegisterSchemas(boolean)} is false.
     * @throws HttpResponseException if an error occurred while trying to fetch the schema from the service.
     */
    public <T> T deserialize(MessageContent message, TypeReference<T> typeReference) {
        return deserializeAsync(message, typeReference).block();
    }

    /**
     * Deserializes a message into its object.
     *
     * @param message Object to deserialize.
     * @param typeReference Message to deserialize to.
     * @param <T> Concrete type of {@link MessageContent}.
     *
     * @return A Mono that completes when the message encoded. If {@code message.getBodyAsBinaryData()} is null or
     *     empty, then an empty Mono is returned.
     *
     * @throws NullPointerException if {@code message} or {@code typeReference} is null.
     * @throws IllegalArgumentException if the message does not have a content type to use for deserialization. If
     *     the mime-type in the content type cannot be parsed or the type is not avro/binary.
     * @throws ResourceNotFoundException if a schema with a matching schema id could not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     * @throws SchemaRegistryApacheAvroException if the message could not be deserialized.
     * @throws ResourceNotFoundException if the schema could not be found and {@link
     *     SchemaRegistryApacheAvroSerializerBuilder#autoRegisterSchemas(boolean)} is false.
     * @throws HttpResponseException if an error occurred while trying to fetch the schema from the service.
     */
    public <T> Mono<T> deserializeAsync(MessageContent message, TypeReference<T> typeReference) {
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

        final String schemaId = parts[1];

        return this.schemaCache.getSchema(schemaId)
            .handle((schema, sink) -> {
                try {
                    final T decode = avroSerializer.deserialize(contents, schema, typeReference);
                    sink.next(decode);
                } catch (Exception e) {
                    sink.error(e);
                }
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

