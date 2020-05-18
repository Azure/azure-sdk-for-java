// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import reactor.core.Exceptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Apache based implementation of the {@link AvroSerializer} interface.
 */
public class ApacheAvroSerializer implements AvroSerializer {
    private final ClientLogger logger = new ClientLogger(ApacheAvroSerializer.class);

    private final Schema.Parser parser;
    private final DecoderFactory decoderFactory;
    private final EncoderFactory encoderFactory;
    private final GenericData genericData;

    ApacheAvroSerializer(Schema.Parser parser, DecoderFactory decoderFactory, EncoderFactory encoderFactory,
        GenericData genericData) {
        this.parser = parser;
        this.decoderFactory = decoderFactory;
        this.encoderFactory = encoderFactory;
        this.genericData = genericData;
    }

    @Override
    public <T> T read(byte[] input, String schema) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");
        if (input == null) {
            return null;
        }

        try {
            Schema avroSchema = parser.parse(schema);
            DatumReader<T> reader = new GenericDatumReader<>(avroSchema, avroSchema, genericData);
            return reader.read(null, decoderFactory.binaryDecoder(input, null));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(ex));
        }
    }

    @Override
    public byte[] write(Object value, String schema) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");

        Schema avroSchema = parser.parse(schema);
        DatumWriter<Object> writer = new GenericDatumWriter<>(avroSchema, genericData);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            Encoder encoder = encoderFactory.binaryEncoder(stream, null);
            writer.write(value, encoder);
            encoder.flush();
            return stream.toByteArray();
        } catch (IOException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(ex));
        }
    }
}
