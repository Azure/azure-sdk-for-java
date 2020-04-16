// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import reactor.core.Exceptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Apache based implementation of the {@link AvroSerializer} interface.
 */
public class ApacheAvroSerializer implements AvroSerializer {
    private final ClientLogger logger = new ClientLogger(ApacheAvroSerializer.class);

    private final Schema.Parser parser;
    private final DecoderFactory decoderFactory;
    private final  EncoderFactory encoderFactory;

    ApacheAvroSerializer(Schema.Parser parser, DecoderFactory decoderFactory, EncoderFactory encoderFactory) {
        this.parser = parser;
        this.decoderFactory = decoderFactory;
        this.encoderFactory = encoderFactory;
    }

    @Override
    public <T> T read(byte[] input, String schema) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");
        if (input == null) {
            return null;
        }

        try {
            DatumReader<T> reader = new GenericDatumReader<>(parser.parse(schema));
            return reader.read(null, decoderFactory.binaryDecoder(input, null));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(ex));
        }
    }

    @Override
    public byte[] write(Object value, String schema) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");

        DatumWriter<Object> writer = new GenericDatumWriter<>(parser.parse(schema));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            writer.write(value, encoderFactory.binaryEncoder(stream, null));
            return stream.toByteArray();
        } catch (IOException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(ex));
        }
    }

    @Override
    public void write(Object value, String schema, OutputStream stream) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        try {
            DatumWriter<Object> writer = new GenericDatumWriter<>(parser.parse(schema));
            writer.write(value, encoderFactory.binaryEncoder(stream, null));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(ex));
        }
    }
}
