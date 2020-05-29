// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.ByteDecoder;
import com.azure.data.schemaregistry.SerializationException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;

import java.io.IOException;
import java.util.Objects;

/**
 * Apache Avro ByteDecoder implementation with all Avro-specific functionality required to deserialize byte arrays
 * given an Avro schema.
 */
public class AvroByteDecoder extends AvroCodec
        implements ByteDecoder {
    private final ClientLogger logger = new ClientLogger(AvroByteDecoder.class);
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private final boolean avroSpecificReader;

    /**
     * Instantiates AvroByteDecoder instance
     * @param avroSpecificReader flag indicating if attempting to decode as Avro SpecificRecord
     */
    public AvroByteDecoder(boolean avroSpecificReader) {
        this.avroSpecificReader = avroSpecificReader;
    }

    /**
     * @param b byte array containing encoded bytes
     * @param object schema for Avro reader read - fetched from Azure Schema Registry
     * @return deserialized object
     * @throws SerializationException upon deserialization failure
     */
    public Object decodeBytes(byte[] b, Object object) {
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
