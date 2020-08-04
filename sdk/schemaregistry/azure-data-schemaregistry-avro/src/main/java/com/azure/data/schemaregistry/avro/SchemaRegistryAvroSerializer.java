// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistrySerializer;
import com.azure.data.schemaregistry.models.SerializationType;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Asynchronous registry-based serializer implementation.
 */
public final class SchemaRegistryAvroSerializer extends SchemaRegistrySerializer implements ObjectSerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAvroSerializer.class);

    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final Boolean AVRO_SPECIFIC_READER_DEFAULT = false;

    private final Boolean avroSpecificReader;

    /**
     *
     * @param registryClient
     * @param avroSpecificReader
     * @param schemaGroup
     * @param autoRegisterSchemas
     */
    SchemaRegistryAvroSerializer(SchemaRegistryAsyncClient registryClient, Boolean avroSpecificReader,
                                      String schemaGroup, Boolean autoRegisterSchemas) {
        super(registryClient, autoRegisterSchemas, schemaGroup);

        if (avroSpecificReader == null) {
            this.avroSpecificReader = SchemaRegistryAvroSerializer.AVRO_SPECIFIC_READER_DEFAULT;
        }
        else {
            this.avroSpecificReader = avroSpecificReader;
        }
    }

    /**
     *
     * @return serialization type
     */
    protected SerializationType getSerializationType() {
        return SerializationType.AVRO;
    };


    /**
     * Returns schema name for storing schemas in schema registry store.
     *
     * @param object Schema object used to generate schema path
     * @return schema name as string
     */
    protected String getSchemaName(Object object) {
        return AvroSchemaUtils.getSchema(object).getFullName();
    }

    /**
     * @param object Schema object used to generate schema string
     * @see AvroSchemaUtils for distinction between primitive and Avro schema generation
     * @return string representation of schema
     */
    protected String getSchemaString(Object object) {
        Schema schema = AvroSchemaUtils.getSchema(object);
        return schema.toString();
    }

    /**
     * @param schemaString string representation of schema
     * @return avro schema
     */
    Schema parseSchemaString(String schemaString) {
        return (new Schema.Parser()).parse(schemaString);
    }


    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object object) {
        if (object == null) {
            return Mono.empty();
        }

        return super.serializeAsync(stream, object);
    }

//    @Override
//    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
//        return this.deserialize(stream)
//            .map(o -> {
//                if (clazz.isInstance(o)) {
//                    return clazz.cast(o);
//                }
//                throw logger.logExceptionAsError(new IllegalStateException("Deserialized object not of class %s"));
//            });
//    }

    /**
     * Returns ByteArrayOutputStream containing Avro encoding of object parameter
     * @param object Object to be encoded into byte stream
     * @return closed ByteArrayOutputStream
     */
    protected byte[] encode(Object object) {
        Schema schema = AvroSchemaUtils.getSchema(object);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (object instanceof byte[]) {
                out.write((byte[]) object); // todo: real avro byte arrays require writing array size to buffer
            } else {
                BinaryEncoder encoder = ENCODER_FACTORY.directBinaryEncoder(out, null);
                DatumWriter<Object> writer;
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
            throw logger.logExceptionAsError(
                new IllegalStateException("Error serializing Avro message", e));
        }
    }


    /**
     * @param b byte array containing encoded bytes
     * @param object schema for Avro reader read - fetched from Azure Schema Registry
     * @return deserialized object
     */
    protected Object decode(byte[] b, Object object) {
        Objects.requireNonNull(object, "Schema must not be null.");

        if (!(object instanceof Schema)) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Object must be an Avro schema."));
        }
        Schema schema = (Schema) object;

        if (schema.getType().equals(Schema.Type.BYTES)) {
            return b;
        }

        DatumReader<?> reader = getDatumReader(schema);

        try {
            Object result = reader.read(null, DECODER_FACTORY.binaryDecoder(b, null));

            if (schema.getType().equals(Schema.Type.STRING)) {
                return result.toString();
            }

            return result;
        } catch (IOException | RuntimeException e) {
            // avro deserialization may throw AvroRuntimeException, NullPointerException, etc
            throw logger.logExceptionAsError(new IllegalStateException("Error deserializing Avro message.", e));
        }
    }

    /**
     * Returns correct reader for decoding payload.
     *
     * @param writerSchema Avro schema fetched from schema registry store
     * @return correct Avro DatumReader object given encoder configuration
     */
    private DatumReader<?> getDatumReader(Schema writerSchema) {
        boolean writerSchemaIsPrimitive = AvroSchemaUtils.getPrimitiveSchemas().values().contains(writerSchema);
        // do not use SpecificDatumReader if writerSchema is a primitive
        if (avroSpecificReader && !writerSchemaIsPrimitive) {
            return new SpecificDatumReader<>(writerSchema);
        } else {
            return new GenericDatumReader<>(writerSchema);
        }
    }
}

