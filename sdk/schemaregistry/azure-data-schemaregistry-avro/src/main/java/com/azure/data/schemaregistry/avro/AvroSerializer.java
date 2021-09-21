// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Base Codec class for Avro encoder and decoder implementations
 */
class AvroSerializer {
    private static final Map<Class<?>, Schema> PRIMITIVE_SCHEMAS;
    private static final Schema NULL_SCHEMA = Schema.create(Schema.Type.NULL);
    private static final int V1_HEADER_LENGTH = 10;
    private static final byte[] V1_HEADER = new byte[]{-61, 1};

    private final ClientLogger logger = new ClientLogger(AvroSerializer.class);
    private final boolean avroSpecificReader;
    private final Schema.Parser parser;
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
        schemas.put(ByteBuffer.class, byteSchema);
        schemas.put(byte[].class, byteSchema);
        schemas.put(Byte[].class, byteSchema);

        final Schema stringSchema = Schema.create(Schema.Type.STRING);
        schemas.put(String.class, stringSchema);

        PRIMITIVE_SCHEMAS = Collections.unmodifiableMap(schemas);
    }

    /**
     * Instantiates AvroCodec instance
     *
     * @param avroSpecificReader flag indicating if decoder should decode records as {@link SpecificRecord
     *     SpecificRecords}.
     * @param parser Schema parser to use.
     * @param encoderFactory Encoder factory
     * @param decoderFactory Decoder factory
     */
    AvroSerializer(boolean avroSpecificReader, Schema.Parser parser, EncoderFactory encoderFactory,
        DecoderFactory decoderFactory) {

        this.avroSpecificReader = avroSpecificReader;
        this.parser = Objects.requireNonNull(parser, "'parser' cannot be null.");
        this.encoderFactory = Objects.requireNonNull(encoderFactory, "'encoderFactory' cannot be null.");
        this.decoderFactory = Objects.requireNonNull(decoderFactory, "'decoderFactory' cannot be null.");
    }

    /**
     * @param schemaString string representation of schema
     *
     * @return avro schema
     */
    Schema parseSchemaString(String schemaString) {
        return this.parser.parse(schemaString);
    }

    /**
     * Returns A byte[] containing Avro encoding of object parameter.
     *
     * @param object Object to be encoded into byte stream
     *
     * @return A set of bytes that represent the object.
     *
     * @throws IllegalArgumentException If the object is not a serializable type.
     * @throws IllegalStateException if the object could not be serialized to an object stream or there was a
     *     runtime exception during serialization.
     */
    <T> byte[] encode(T object) throws IllegalStateException, IllegalArgumentException {
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
            throw logger.logExceptionAsError(new IllegalStateException("Error serializing Avro message", e));
        }
    }

    /**
     * @param bytes byte array containing encoded bytes
     * @param schemaBytes schema content for Avro reader to read - fetched from Azure Schema Registry
     *
     * @return deserialized object
     */
    <T> T decode(byte[] bytes, byte[] schemaBytes, TypeReference<T> typeReference) {
        Objects.requireNonNull(bytes, "'bytes' must not be null.");
        Objects.requireNonNull(schemaBytes, "'schemaBytes' must not be null.");

        String schemaString = new String(schemaBytes, StandardCharsets.UTF_8);
        Schema schemaObject = parseSchemaString(schemaString);

        if (isSingleObjectEncoded(bytes)) {
            final BinaryMessageDecoder<T> messageDecoder = new BinaryMessageDecoder<>(SpecificData.get(), schemaObject);

            try {
                return messageDecoder.decode(bytes);
            } catch (IOException e) {
                throw logger.logExceptionAsError(new UncheckedIOException(
                    "Unable to deserialize Avro schema object using binary message decoder.", e));
            }
        } else {
            final DatumReader<T> reader = getDatumReader(schemaObject, typeReference);

            try {
                return reader.read(null, decoderFactory.binaryDecoder(bytes, null));
            } catch (IOException | RuntimeException e) {
                throw logger.logExceptionAsError(new IllegalStateException("Error deserializing raw Avro message.", e));
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
    static Schema getSchema(Object object) throws IllegalArgumentException {
        if (object instanceof GenericContainer) {
            return ((GenericContainer) object).getSchema();
        }

        if (object == null) {
            return NULL_SCHEMA;
        }

        final Class<?> objectClass = object.getClass();
        final Schema schema = PRIMITIVE_SCHEMAS.get(objectClass);
        if (schema != null) {
            return schema;
        } else {
            throw new IllegalArgumentException("Unsupported Avro type. Supported types are null, GenericContainer,"
                + " Boolean, Integer, Long, Float, Double, String, Byte[], Byte, ByteBuffer, and their primitive"
                + " equivalents.");
        }
    }

    /**
     * True if the object has the single object payload header. The header is comprised of:
     * <ul>
     *     <li>2 byte marker, C3 01</li>
     *     <li>8 byte little-endian CRC-64-AVRO fingerprint of the object's schema</li>
     * </ul>
     *
     * @param schemaBytes Bytes to read from.
     *
     * @return true if the object has the single object payload header; false otherwise.
     *
     * @see <a href="https://avro.apache.org/docs/current/spec.html#single_object_encoding">Single Object Encoding</a>
     */
    static boolean isSingleObjectEncoded(byte[] schemaBytes) {
        if (schemaBytes.length < V1_HEADER_LENGTH) {
            return false;
        }

        return V1_HEADER[0] == schemaBytes[0] && V1_HEADER[1] == schemaBytes[1];
    }

    /**
     * Returns correct reader for decoding payload.
     *
     * @param writerSchema Avro schema fetched from schema registry store
     *
     * @return correct Avro DatumReader object given encoder configuration
     */
    @SuppressWarnings("unchecked")
    private <T> DatumReader<T> getDatumReader(Schema writerSchema, TypeReference<T> typeReference) {
        boolean writerSchemaIsPrimitive = writerSchema.getType() != null;

        if (writerSchemaIsPrimitive) {
            if (avroSpecificReader) {
                return new SpecificDatumReader<>(writerSchema);
            } else {
                return new GenericDatumReader<>(writerSchema);
            }
        }

        // Suppressing this warning because we know that the Type is a representation of the Class<T>
        final Class<T> clazz = (Class<T>) typeReference.getJavaType();
        if (SpecificRecord.class.isAssignableFrom(clazz)) {
            return new SpecificDatumReader<>(writerSchema);
        } else {
            return new GenericDatumReader<>(writerSchema);
        }
    }
}
