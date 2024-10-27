// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.util.ByteBufferInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Class containing implementation of Apache Avro serializer
 */
class AvroSerializer {
    private static final Map<Class<?>, Schema> PRIMITIVE_SCHEMAS;
    private static final Schema NULL_SCHEMA = Schema.create(Schema.Type.NULL);
    private static final int V1_HEADER_LENGTH = 10;
    private static final byte[] V1_HEADER = new byte[]{-61, 1};

    private final ClientLogger logger = new ClientLogger(AvroSerializer.class);
    private final boolean avroSpecificReader;
    private final EncoderFactory encoderFactory;
    private final DecoderFactory decoderFactory;

    static {
        final HashMap<Class<?>, Schema> schemas = new HashMap<>();

        final Schema booleanSchema = Schema.create(Schema.Type.BOOLEAN);
        schemas.put(Boolean.class, booleanSchema);
        schemas.put(boolean.class, booleanSchema);

        final Schema intSchema = Schema.create(Schema.Type.INT);
        schemas.put(Integer.class, intSchema);
        schemas.put(int.class, intSchema);

        final Schema longSchema = Schema.create(Schema.Type.LONG);
        schemas.put(Long.class, longSchema);
        schemas.put(long.class, longSchema);

        final Schema floatSchema = Schema.create(Schema.Type.FLOAT);
        schemas.put(Float.class, floatSchema);
        schemas.put(float.class, floatSchema);

        final Schema doubleSchema = Schema.create(Schema.Type.DOUBLE);
        schemas.put(Double.class, doubleSchema);
        schemas.put(double.class, doubleSchema);

        final Schema byteSchema = Schema.create(Schema.Type.BYTES);
        schemas.put(byte.class, byteSchema);
        schemas.put(Byte.class, byteSchema);
        schemas.put(byte[].class, byteSchema);
        schemas.put(Byte[].class, byteSchema);

        // This class is abstract but not final.
        schemas.put(ByteBuffer.class, byteSchema);

        final Schema stringSchema = Schema.create(Schema.Type.STRING);
        schemas.put(String.class, stringSchema);

        PRIMITIVE_SCHEMAS = Collections.unmodifiableMap(schemas);
    }

    /**
     * Instantiates AvroCodec instance
     *
     * @param avroSpecificReader flag indicating if decoder should decode records as {@link SpecificRecord
     *     SpecificRecords}.
     * @param encoderFactory Encoder factory
     * @param decoderFactory Decoder factory
     */
    AvroSerializer(boolean avroSpecificReader, EncoderFactory encoderFactory,
        DecoderFactory decoderFactory) {

        this.avroSpecificReader = avroSpecificReader;
        this.encoderFactory = Objects.requireNonNull(encoderFactory, "'encoderFactory' cannot be null.");
        this.decoderFactory = Objects.requireNonNull(decoderFactory, "'decoderFactory' cannot be null.");
    }

    /**
     * Returns A byte[] containing Avro encoding of object parameter.
     *
     * @param object Object to be encoded into byte stream
     * @param schemaId Identifier of the schema trying to be encoded.
     *
     * @return A set of bytes that represent the object.
     *
     * @throws IllegalArgumentException If the object is not a serializable type.
     * @throws IllegalStateException if the object could not be serialized to an object stream or there was a
     *     runtime exception during serialization.
     */
    <T> byte[] serialize(T object, String schemaId) {
        final Schema schema = getSchema(object);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (object instanceof byte[]) {
                // todo: real avro byte arrays require writing array size to buffer
                outputStream.write((byte[]) object);
            } else {
                BinaryEncoder encoder = encoderFactory.directBinaryEncoder(outputStream, null);
                DatumWriter<T> writer;
                if (object instanceof SpecificRecord) {
                    writer = new SpecificDatumWriter<>(schema);
                } else {
                    writer = new GenericDatumWriter<>(schema);
                }
                writer.write(object, encoder);
                encoder.flush();
            }
            return outputStream.toByteArray();
        } catch (IOException | RuntimeException e) {
            // Avro serialization can throw AvroRuntimeException, NullPointerException, ClassCastException, etc
            throw logger.logExceptionAsError(new SchemaRegistryApacheAvroException(
                "An error occurred while attempting to serialize to Avro.", e, schemaId));
        }
    }

    /**
     * @param contents byte array containing encoded bytes
     * @param schemaObject Schema to deserialize object.
     *
     * @return deserialized object
     */
    <T> T deserialize(ByteBuffer contents, Schema schemaObject, TypeReference<T> typeReference) {
        Objects.requireNonNull(contents, "'bytes' must not be null.");

        if (isSingleObjectEncoded(contents)) {
            final BinaryMessageDecoder<T> messageDecoder = new BinaryMessageDecoder<>(SpecificData.get(), schemaObject);

            try {
                return messageDecoder.decode(contents);
            } catch (IOException e) {
                throw logger.logExceptionAsError(new SchemaRegistryApacheAvroException(
                    "Unable to deserialize Avro schema object using binary message decoder.", e));
            }
        } else {
            final DatumReader<T> reader = getDatumReader(schemaObject, typeReference);

            try {
                try (ByteBufferInputStream input = new ByteBufferInputStream(Collections.singletonList(contents))) {
                    return reader.read(null, decoderFactory.binaryDecoder(input, null));
                }
            } catch (IOException | RuntimeException e) {
                throw logger.logExceptionAsError(new SchemaRegistryApacheAvroException(
                    "Error deserializing raw Avro message.", e));
            }
        }
    }

    /**
     * Returns Avro schema for specified object, including null values
     *
     * @param object object for which Avro schema is being returned
     *
     * @return Avro schema for object's data structure
     *
     * @throws IllegalArgumentException if object type is unsupported.
     */
    static Schema getSchema(Object object) {
        if (object instanceof GenericContainer) {
            return ((GenericContainer) object).getSchema();
        }

        if (object == null) {
            return NULL_SCHEMA;
        }

        final Class<?> objectClass = object.getClass();
        final Schema primitiveSchema = getPrimitiveSchema(objectClass);
        if (primitiveSchema != null) {
            return primitiveSchema;
        } else {
            throw new IllegalArgumentException("Unsupported Avro type. Supported types are null, GenericContainer,"
                + " Boolean, Integer, Long, Float, Double, String, Byte[], Byte, ByteBuffer, and their primitive"
                + " equivalents. Actual: " + objectClass);
        }
    }

    /**
     * True if the object has the single object payload header. The header is comprised of:
     * <ul>
     *     <li>2 byte marker, C3 01</li>
     *     <li>8 byte little-endian CRC-64-AVRO fingerprint of the object's schema</li>
     * </ul>
     *
     * @param byteBuffer Bytes to read from.
     *
     * @return true if the object has the single object payload header; false otherwise.
     *
     * @see <a href="https://avro.apache.org/docs/current/spec.html#single_object_encoding">Single Object Encoding</a>
     */
    static boolean isSingleObjectEncoded(ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() < V1_HEADER_LENGTH) {
            return false;
        }

        // Before we started moving the position in the buffer.
        byteBuffer.mark();

        final byte[] contents = new byte[V1_HEADER_LENGTH];
        byteBuffer.get(contents);
        byteBuffer.reset();

        return V1_HEADER[0] == contents[0] && V1_HEADER[1] == contents[1];
    }

    /**
     * Gets the type's schema if there is one.
     *
     * @param clazz Class to get schema.
     * @param <T> The type of object.
     *
     * @return The {@link Schema} or {@code null} if it was not a GenericContainer, could not instantiate the type, or
     *     there was no default constructor.
     */
    <T> Schema getSchemaFromTypeReference(Class<T> clazz) {
        if (!GenericContainer.class.isAssignableFrom(clazz)) {
            return null;
        }

        final Optional<Constructor<?>> defaultConstructor;
        try {
            defaultConstructor = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .findFirst();
        } catch (SecurityException e) {
            logger.info("Could not get declaring constructors for deserializing T ({}). Using writer schema.",
                clazz, e);
            return null;
        }

        if (!defaultConstructor.isPresent()) {
            return null;
        }

        Object instance = null;
        try {
            instance = defaultConstructor.get().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.info("Could not create new instance for deserializing T ({}). Using writer schema.", clazz, e);
        }

        return instance instanceof GenericContainer
            ? ((GenericContainer) instance).getSchema()
            : null;
    }

    /**
     * Gets a schema for the given class if it is an Avro primitive type.
     *
     * @param clazz Object class
     *
     * @return Matching primitive schema, otherwise {@code null} if it is not.
     */
    private static Schema getPrimitiveSchema(Class<?> clazz) {
        final Schema schema = PRIMITIVE_SCHEMAS.get(clazz);
        if (schema != null) {
            return schema;
        } else if (CharSequence.class.isAssignableFrom(clazz)) {
            return PRIMITIVE_SCHEMAS.get(String.class);
        } else if (ByteBuffer.class.isAssignableFrom(clazz)) {
            return PRIMITIVE_SCHEMAS.get(Byte[].class);
        } else {
            return null;
        }
    }

    /**
     * Returns correct reader for decoding payload.
     *
     * @param writerSchema Avro schema fetched from schema registry store
     *
     * @return correct Avro DatumReader object given encoder configuration
     */
    private <T> DatumReader<T> getDatumReader(Schema writerSchema, TypeReference<T> typeReference) {
        final Class<T> clazz = typeReference.getJavaClass();
        final Schema primitiveSchema = getPrimitiveSchema(clazz);

        if (primitiveSchema != null) {
            if (avroSpecificReader) {
                return new SpecificDatumReader<>(writerSchema);
            } else {
                return new GenericDatumReader<>(writerSchema);
            }
        }

        final Schema readerSchema = getSchemaFromTypeReference(clazz);
        if (readerSchema != null && !readerSchema.equals(writerSchema)) {
            logger.verbose("The writer schema is different than reader schema. Using reader schema. "
                + "Writer: '{}'. Reader: '{}'", writerSchema, readerSchema);

            return new SpecificDatumReader<>(writerSchema, readerSchema);
        }

        if (SpecificRecord.class.isAssignableFrom(clazz)) {
            return new SpecificDatumReader<>(writerSchema);
        } else {
            return new GenericDatumReader<>(writerSchema);
        }
    }
}
