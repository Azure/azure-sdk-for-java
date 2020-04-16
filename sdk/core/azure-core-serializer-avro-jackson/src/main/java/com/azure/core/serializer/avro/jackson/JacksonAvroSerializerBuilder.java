// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;

/**
 * Fluent builder class that configures and instantiates instances of {@link JacksonAvroSerializer}.
 *
 * @see JacksonAvroSerializer
 */
public class JacksonAvroSerializerBuilder {
    private AvroMapper avroMapper;

    /**
     * Instantiates a new instance of {@link JacksonAvroSerializer} based on the configurations set on the builder.
     *
     * @return A new instance of {@link JacksonAvroSerializer}.
     */
    public JacksonAvroSerializer build() {
        return new JacksonAvroSerializer((avroMapper == null) ? new AvroMapper() : avroMapper);
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
