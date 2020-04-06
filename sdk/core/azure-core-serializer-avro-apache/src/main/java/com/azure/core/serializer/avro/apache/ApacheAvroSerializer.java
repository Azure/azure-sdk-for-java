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
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ApacheAvroSerializer implements AvroSerializer<Schema> {
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();

    @Override
    public <T> T read(byte[] input, Schema schema) {
        try {
            DatumReader<T> reader = new GenericDatumReader<>(schema);
            return reader.read(null, DECODER_FACTORY.binaryDecoder(input, null));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public <T> Mono<T> readAsync(byte[] input, Schema schema) {
        return Mono.fromCallable(() -> read(input, schema));
    }

    @Override
    public byte[] write(Object value, Schema schema) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        write(value, schema, stream);

        return stream.toByteArray();
    }

    @Override
    public Mono<byte[]> writeAsync(Object value, Schema schema) {
        return Mono.fromCallable(() -> write(value, schema));
    }

    @Override
    public void write(Object value, Schema schema, OutputStream stream) {
        try {
            DatumWriter<Object> writer = new GenericDatumWriter<>(schema);
            writer.write(value, ENCODER_FACTORY.binaryEncoder(stream, null));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Mono<Void> writeAsync(Object value, Schema schema, OutputStream stream) {
        return Mono.fromRunnable(() -> write(value, schema, stream));
    }
}
