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
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Apache based implementation of the {@link AvroSerializer} interface.
 */
public class ApacheAvroSerializer implements AvroSerializer {
    private static final Schema.Parser PARSER = new Schema.Parser();
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();

    private final ClientLogger logger = new ClientLogger(ApacheAvroSerializer.class);

    @Override
    public <T> Mono<T> read(byte[] input, String schema) {
        return Mono.fromCallable(() -> {
            if (input == null) {
                return null;
            }

            try {
                DatumReader<T> reader = new GenericDatumReader<>(PARSER.parse(schema));
                return reader.read(null, DECODER_FACTORY.binaryDecoder(input, null));
            } catch (IOException ex) {
                throw logger.logExceptionAsError(Exceptions.propagate(ex));
            }
        });
    }

    @Override
    public Mono<byte[]> write(Object value, String schema) {
        return Mono.fromCallable(() -> {
            DatumWriter<Object> writer = new GenericDatumWriter<>(PARSER.parse(schema));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            try {
                writer.write(value, ENCODER_FACTORY.binaryEncoder(stream, null));
                return stream.toByteArray();
            } catch (IOException ex) {
                throw logger.logExceptionAsError(Exceptions.propagate(ex));
            }
        });
    }

    @Override
    public Mono<Void> write(Object value, String schema, OutputStream stream) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        return Mono.fromRunnable(() -> {
            try {
                DatumWriter<Object> writer = new GenericDatumWriter<>(PARSER.parse(schema));
                writer.write(value, ENCODER_FACTORY.binaryEncoder(stream, null));
            } catch (IOException ex) {
                throw logger.logExceptionAsError(Exceptions.propagate(ex));
            }
        });
    }
}