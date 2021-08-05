// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.experimental.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

/**
 * Apache Avro based implementation of the {@link AvroSerializer} interface.
 */
public class ApacheAvroSerializer implements AvroSerializer {
    private final ClientLogger logger = new ClientLogger(ApacheAvroSerializer.class);

    private final Schema schema;
    private final DecoderFactory decoderFactory;
    private final EncoderFactory encoderFactory;
    private final SpecificData specificData;

    ApacheAvroSerializer(Schema schema, DecoderFactory decoderFactory, EncoderFactory encoderFactory,
        SpecificData specificData) {
        this.schema = schema;
        this.decoderFactory = decoderFactory;
        this.encoderFactory = encoderFactory;
        this.specificData = specificData;
    }

    @Override
    public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        return deserialize(data, () -> decoderFactory.binaryDecoder(data, null));
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        return deserialize(stream, () -> decoderFactory.binaryDecoder(stream, null));
    }

    private <T> T deserialize(Object data, Supplier<Decoder> decoderSupplier) {
        if (data == null) {
            return null;
        }

        DatumReader<T> reader = new SpecificDatumReader<>(schema, schema, specificData);

        try {
            return reader.read(null, decoderSupplier.get());
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public <T> Mono<T> deserializeFromBytesAsync(byte[] data, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> this.deserializeFromBytes(data, typeReference));
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserialize(stream, typeReference));
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        DatumWriter<Object> writer = new SpecificDatumWriter<>(schema, specificData);

        Encoder encoder = encoderFactory.binaryEncoder(stream, null);
        try {
            writer.write(value, encoder);
            encoder.flush();
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object value) {
        return Mono.fromRunnable(() -> serialize(stream, value));
    }
}
