// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.serializer.AvroSerializer;
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

/**
 * Apache based implementation of the {@link AvroSerializer} interface.
 */
public class ApacheAvroSerializer implements AvroSerializer<Schema> {
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();

    @Override
    public <T> Mono<T> read(byte[] input, Schema schema) {
        return Mono.defer(() -> {
            try {
                DatumReader<T> reader = new GenericDatumReader<>(schema);
                return Mono.just(reader.read(null, DECODER_FACTORY.binaryDecoder(input, null)));
            } catch (IOException ex) {
                return Mono.error(ex);
            }
        });
    }

    @Override
    public Mono<byte[]> write(Object value, Schema schema) {
        return Mono.defer(() -> {
            DatumWriter<Object> writer = new GenericDatumWriter<>(schema);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            try {
                writer.write(value, ENCODER_FACTORY.binaryEncoder(stream, null));
                return Mono.just(stream.toByteArray());
            } catch (IOException ex) {
                return Mono.error(ex);
            }
        });
    }

    @Override
    public Mono<Void> write(Object value, Schema schema, OutputStream stream) {
        return Mono.defer(() -> Mono.fromRunnable(() -> {
            try {
                DatumWriter<Object> writer = new GenericDatumWriter<>(schema);
                writer.write(value, ENCODER_FACTORY.binaryEncoder(stream, null));
            } catch (IOException ex) {
                throw Exceptions.propagate(ex);
            }
        }));
    }
}
