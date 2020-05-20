// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import org.apache.avro.AvroRuntimeException;
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
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Apache based implementation of the {@link AvroSerializer} interface.
 */
public class ApacheAvroSerializer implements AvroSerializer {
    private final ClientLogger logger = new ClientLogger(ApacheAvroSerializer.class);

    private final boolean validateSchema;
    private final boolean validateSchemaDefaults;
    private final DecoderFactory decoderFactory;
    private final EncoderFactory encoderFactory;
    private final GenericData genericData;

    ApacheAvroSerializer(boolean validateSchema, boolean validateSchemaDefaults, DecoderFactory decoderFactory,
        EncoderFactory encoderFactory, GenericData genericData) {
        this.validateSchema = validateSchema;
        this.validateSchemaDefaults = validateSchemaDefaults;
        this.decoderFactory = decoderFactory;
        this.encoderFactory = encoderFactory;
        this.genericData = genericData;
    }

    @Override
    public <T> T read(byte[] input, String schema) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");

        return readInternal(input, schema, schema);
    }

    @Override
    public <T> T read(byte[] input, String readerSchema, String writerSchema) {
        Objects.requireNonNull(readerSchema, "'readerSchema' cannot be null.");
        Objects.requireNonNull(writerSchema, "'writerSchema' cannot be null.");

        return readInternal(input, readerSchema, writerSchema);
    }

    private <T> T readInternal(byte[] input, String readerSchema, String writerSchema) {
        if (input == null) {
            return null;
        }

        Schema.Parser parser = getParser(validateSchema, validateSchemaDefaults);
        DatumReader<T> reader;
        try {
            if (readerSchema.equalsIgnoreCase(writerSchema)) {
                Schema avroSchema = parser.parse(readerSchema);
                reader = new GenericDatumReader<>(avroSchema, avroSchema, genericData);
            } else {
                reader = new GenericDatumReader<>(parser.parse(writerSchema), parser.parse(readerSchema), genericData);
            }
        } catch (AvroRuntimeException ex) {
            throw logger.logExceptionAsError(ex);
        }

        try {
            return reader.read(null, decoderFactory.binaryDecoder(input, null));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public byte[] write(Object value, String schema) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");

        Schema avroSchema = getParser(validateSchema, validateSchemaDefaults).parse(schema);
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

    private static Schema.Parser getParser(boolean validateSchema, boolean validateSchemaDefaults) {
        return new Schema.Parser().setValidate(validateSchema)
            .setValidateDefaults(validateSchemaDefaults);
    }
}
