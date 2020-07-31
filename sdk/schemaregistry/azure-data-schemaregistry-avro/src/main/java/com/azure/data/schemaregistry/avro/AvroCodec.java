// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.Codec;
import com.azure.data.schemaregistry.SerializationException;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Base Codec class for Avro encoder and decoder implementations
 */
public class AvroCodec implements Codec {
    private final ClientLogger logger = new ClientLogger(AvroCodec.class);
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final Boolean AVRO_SPECIFIC_READER_DEFAULT = false;

    private final Boolean avroSpecificReader;

    /**
     * Instantiates AvroCodec instance
     * @param avroSpecificReader flag indicating if decoder should decode records as SpecificRecords
     */
    public AvroCodec(Boolean avroSpecificReader) {
        if (avroSpecificReader == null) {
            this.avroSpecificReader = AvroCodec.AVRO_SPECIFIC_READER_DEFAULT;
        }
        else {
            this.avroSpecificReader = avroSpecificReader;
        }
    }

    @Override
    public String getSchemaType() {
        return "avro";
    }

    /**
     * @param schemaString string representation of schema
     * @return avro schema
     */
    @Override
    public Schema parseSchemaString(String schemaString) {
        return (new Schema.Parser()).parse(schemaString);
    }


    /**
     * @param object Schema object used to generate schema string
     * @see AvroSchemaUtils for distinction between primitive and Avro schema generation
     * @return string representation of schema
     */
    @Override
    public String getSchemaString(Object object) {
        Schema schema = AvroSchemaUtils.getSchema(object);
        return schema.toString();
    }

    /**
     * Returns schema name for storing schemas in schema registry store.
     *
     * @param object Schema object used to generate schema path
     * @return schema name as string
     */
    @Override
    public String getSchemaName(Object object) {
        return AvroSchemaUtils.getSchema(object).getFullName();
    }

    /**
     * Returns ByteArrayOutputStream containing Avro encoding of object parameter
     * @param object Object to be encoded into byte stream
     * @return closed ByteArrayOutputStream
     * @throws SerializationException wraps runtime exceptions
     */
    @Override
    public ByteArrayOutputStream encode(Object object) {
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
            return out;
        } catch (IOException | RuntimeException e) {
            // Avro serialization can throw AvroRuntimeException, NullPointerException, ClassCastException, etc
            throw logger.logExceptionAsError(
                new SerializationException("Error serializing Avro message", e));
        }
    }


    /**
     * @param b byte array containing encoded bytes
     * @param object schema for Avro reader read - fetched from Azure Schema Registry
     * @return deserialized object
     * @throws SerializationException upon deserialization failure
     */
    public Object decode(byte[] b, Object object) {
        Objects.requireNonNull(object, "Schema must not be null.");

        if (!(object instanceof Schema)) {
            throw logger.logExceptionAsError(
                new SerializationException("Object must be an Avro schema."));
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
            throw logger.logExceptionAsError(new SerializationException("Error deserializing Avro message.", e));
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
