// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.experimental.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Fluent builder class that configures and instantiates instances of {@link JacksonAvroSerializer}.
 *
 * @see JacksonAvroSerializer
 */
public class JacksonAvroSerializerBuilder {
    private final ClientLogger logger = new ClientLogger(JacksonAvroSerializerBuilder.class);

    private String schema;
    private AvroMapper avroMapper;

    /**
     * Instantiates a new instance of {@link JacksonAvroSerializer} based on the configurations set on the builder.
     *
     * @return A new instance of {@link JacksonAvroSerializer}.
     * @throws NullPointerException If {@code schema} is {@code null}.
     * @throws UncheckedIOException If {@code schema} cannot be parsed.
     */
    public AvroSerializer build() {
        Objects.requireNonNull(schema, "'schema' cannot be null.");

        AvroMapper buildAvroMapper = (avroMapper == null) ? new AvroMapper() : avroMapper;
        try {
            return new JacksonAvroSerializer(buildAvroMapper.schemaFrom(schema), buildAvroMapper);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    /**
     * Configures the schema that will be associated to the {@link JacksonAvroSerializer} when {@link #build()} is
     * called.
     *
     * @param schema Avro schema to associate to the serializer that is built.
     * @return The updated JacksonAvroSerializerBuilder object.
     */
    public JacksonAvroSerializerBuilder schema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Configures the {@link AvroMapper} that will be used to serialize objects to Avro streams and deserialize Avro
     * streams to objects.
     * <p>
     * If {@code avroMapper} is {@code null} {@link AvroMapper#AvroMapper()} will be used.
     *
     * @param avroMapper The {@link AvroMapper} that will serialize to and deserialize Avro.
     * @return The updated JacksonAvroSerializerBuilder object.
     */
    public JacksonAvroSerializerBuilder avroMapper(AvroMapper avroMapper) {
        this.avroMapper = avroMapper;
        return this;
    }
}
