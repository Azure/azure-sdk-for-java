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
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Base Codec class for Avro encoder and decoder implementations
 */
class AvroSchemaRegistryUtils {
    private final ClientLogger logger = new ClientLogger(AvroSchemaRegistryUtils.class);

    private static final int V1_HEADER_LENGTH = 10;
    private static final byte[] V1_HEADER = new byte[]{-61, 1};

    private final boolean avroSpecificReader;
    private final Schema.Parser parser;
    private final EncoderFactory encoderFactory;
    private final DecoderFactory decoderFactory;

    /**
     * Instantiates AvroCodec instance
     *
     * @param avroSpecificReader flag indicating if decoder should decode records as {@link SpecificRecord
     *     SpecificRecords}.
     * @param parser Schema parser to use.
     * @param encoderFactory Encoder factory
     * @param decoderFactory Decoder factory
     */
    AvroSchemaRegistryUtils(boolean avroSpecificReader, Schema.Parser parser, EncoderFactory encoderFactory,
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
     * @param object Schema object used to generate schema string
     *
     * @return string representation of schema
     *
     * @see AvroSchemaUtils for distinction between primitive and Avro schema generation
     */
    String getSchemaString(Object object) {
        Schema schema = AvroSchemaUtils.getSchema(object);
        return schema.toString();
    }

    /**
     * Returns schema name for storing schemas in schema registry store.
     *
     * @param object Schema object used to generate schema path
     *
     * @return schema name as string
     *
     * @throws IllegalArgumentException if {@code object} is not a primitive type and not of type {@link
     *     GenericContainer}.
     */
    String getSchemaName(Object object) {
        return AvroSchemaUtils.getSchema(object).getFullName();
    }

    /**
     * Returns A byte[] containing Avro encoding of object parameter.
     *
     * @param object Object to be encoded into byte stream
     *
     * @return A set of bytes that represent the object.
     */
    <T> byte[] encode(T object) throws IOException {
        final Schema schema = AvroSchemaUtils.getSchema(object);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            if (object instanceof byte[]) {
                out.write((byte[]) object); // todo: real avro byte arrays require writing array size to buffer
            } else {
                BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
                DatumWriter<T> writer;
                if (object instanceof SpecificRecord) {
                    writer = new SpecificDatumWriter<>(schema);
                } else {
                    writer = new GenericDatumWriter<>(schema);
                }
                writer.write(object, encoder);
                encoder.flush();
            }
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            // Avro serialization can throw AvroRuntimeException, NullPointerException, ClassCastException, etc
            throw logger.logExceptionAsError(new IllegalStateException("Error serializing Avro message", e));
        } finally {
            out.close();
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
     * Returns correct reader for decoding payload.
     *
     * @param writerSchema Avro schema fetched from schema registry store
     *
     * @return correct Avro DatumReader object given encoder configuration
     */
    @SuppressWarnings("unchecked")
    private <T> DatumReader<T> getDatumReader(Schema writerSchema, TypeReference<T> typeReference) {
        boolean writerSchemaIsPrimitive = writerSchema.getType() != null
            && AvroSchemaUtils.getPrimitiveSchemas().containsKey(writerSchema.getType());

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

    /**
     * True if the object has the single object payload header. The header is comprised of:
     * <ul>
     *     <li>2 byte marker, C3 01</li>
     *     <li>8 byte little-endian CRC-64-AVRO fingerprint of the object's schema</li>
     * </ul>
     *
     * @param schemaBytes Bytes to read from.
     * @return true if the object has the single object payload header; false otherwise.
     *
     * @see <a href="https://avro.apache.org/docs/current/spec.html#single_object_encoding">Single Object Encoding</a>
     */
    private static boolean isSingleObjectEncoded(byte[] schemaBytes) {
        if (schemaBytes.length < V1_HEADER_LENGTH) {
            return false;
        }

        return V1_HEADER[0] == schemaBytes[0] && V1_HEADER[1] == schemaBytes[1];
    }
}
