// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.util.serializer.ObjectSerializer;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Apache Avro based implementation of the {@link ObjectSerializer} interface.
 */
public class ApacheAvroSerializer implements ObjectSerializer {
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
    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
        return Mono.fromCallable(() -> {
            if (stream == null) {
                return null;
            }

            DatumReader<T> reader = new SpecificDatumReader<>(schema, schema, specificData);

            return clazz.cast(reader.read(null, decoderFactory.binaryDecoder(stream, null)));
        });
    }

    @Override
    public Mono<OutputStream> serialize(OutputStream stream, Object value) {
        return Mono.fromCallable(() -> {
            DatumWriter<Object> writer = new SpecificDatumWriter<>(schema, specificData);

            Encoder encoder = encoderFactory.binaryEncoder(stream, null);
            writer.write(value, encoder);
            encoder.flush();
            return stream;
        });
    }
}
