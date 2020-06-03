// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.serializer.SchemaSerializer;
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

import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * Apache Avro based implementation of the {@link SchemaSerializer} interface.
 */
public class ApacheAvroSerializer implements SchemaSerializer {
    private final boolean validateSchema;
    private final boolean validateSchemaDefaults;
    private final DecoderFactory decoderFactory;
    private final EncoderFactory encoderFactory;
    private final SpecificData specificData;

    ApacheAvroSerializer(boolean validateSchema, boolean validateSchemaDefaults, DecoderFactory decoderFactory,
        EncoderFactory encoderFactory, SpecificData specificData) {
        this.validateSchema = validateSchema;
        this.validateSchemaDefaults = validateSchemaDefaults;
        this.decoderFactory = decoderFactory;
        this.encoderFactory = encoderFactory;
        this.specificData = specificData;
    }

    @Override
    public <T> Mono<T> deserialize(byte[] input, String schema, Class<T> clazz) {
        return Mono.fromCallable(() -> {
            Objects.requireNonNull(schema, "'schema' cannot be null.");

            if (input == null) {
                return null;
            }

            Schema avroSchema = getParser(validateSchema, validateSchemaDefaults).parse(schema);
            DatumReader<T> reader = new SpecificDatumReader<>(avroSchema, avroSchema, specificData);

            return clazz.cast(reader.read(null, decoderFactory.binaryDecoder(input, null)));
        });
    }

    @Override
    public Mono<byte[]> serialize(Object value, String schema) {
        return Mono.fromCallable(() -> {
            Objects.requireNonNull(schema, "'schema' cannot be null.");

            Schema avroSchema = getParser(validateSchema, validateSchemaDefaults).parse(schema);
            DatumWriter<Object> writer = new SpecificDatumWriter<>(avroSchema, specificData);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            Encoder encoder = encoderFactory.binaryEncoder(stream, null);
            writer.write(value, encoder);
            encoder.flush();
            return stream.toByteArray();
        });
    }

    private static Schema.Parser getParser(boolean validateSchema, boolean validateSchemaDefaults) {
        return new Schema.Parser().setValidate(validateSchema)
            .setValidateDefaults(validateSchemaDefaults);
    }
}
