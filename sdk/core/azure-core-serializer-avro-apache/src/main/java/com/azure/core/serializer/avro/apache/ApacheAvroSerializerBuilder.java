// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.experimental.serializer.AvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;

import java.util.Objects;

/**
 * Fluent builder class that configures and instantiates instances of {@link ApacheAvroSerializer}.
 *
 * @see ApacheAvroSerializer
 */
public class ApacheAvroSerializerBuilder {
    private String schema;
    private DecoderFactory decoderFactory;
    private EncoderFactory encoderFactory;
    private SpecificData specificData;

    /**
     * Instantiates a new instance of {@link ApacheAvroSerializer} based on the configurations set on the builder.
     *
     * @return A new instance of {@link ApacheAvroSerializer}.
     * @throws NullPointerException If {@code schema} is {@code null}.
     */
    public AvroSerializer build() {
        Objects.requireNonNull(schema, "'schema' cannot be null.");

        DecoderFactory buildDecoderFactory = (decoderFactory == null) ? DecoderFactory.get() : decoderFactory;
        EncoderFactory buildEncoderFactory = (encoderFactory == null) ? EncoderFactory.get() : encoderFactory;
        SpecificData buildSpecificData = (specificData == null) ? SpecificData.get() : specificData;

        return new ApacheAvroSerializer(new Schema.Parser().parse(schema), buildDecoderFactory, buildEncoderFactory,
            buildSpecificData);
    }

    /**
     * Configures the schema that will be associated to the {@link ApacheAvroSerializer} when {@link #build()} is
     * called.
     *
     * @param schema Avro schema to associate to the serializer that is built.
     * @return The updated ApacheAvroSerializerBuilder object.
     */
    public ApacheAvroSerializerBuilder schema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Configures the {@link DecoderFactory} that will be used to deserialize the Avro stream into an object.
     * <p>
     * If {@code decoderFactory} is {@code null} when {@link #build()} is called {@link DecoderFactory#get()
     * DecoderFactory.get()} will be used as the decoder factory.
     *
     * @param decoderFactory The {@link DecoderFactory} used to deserialize the Avro stream into an object.
     * @return The updated ApacheAvroSerializerBuilder object.
     */
    public ApacheAvroSerializerBuilder decoderFactory(DecoderFactory decoderFactory) {
        this.decoderFactory = decoderFactory;
        return this;
    }

    /**
     * Configures the {@link EncoderFactory} that will be used to serialize an object into an Avro stream.
     * <p>
     * If {@code encoderFactory} is {@code null} when {@link #build()} is called {@link EncoderFactory#get()
     * EncoderFactory.get()} will be used as the encoder factory.
     *
     * @param encoderFactory The {@link EncoderFactory} used to serialize an object into an Avro stream.
     * @return The updated ApacheAvroSerializerBuilder object.
     */
    public ApacheAvroSerializerBuilder encoderFactory(EncoderFactory encoderFactory) {
        this.encoderFactory = encoderFactory;
        return this;
    }

    /**
     * Configures the {@link SpecificData} that will be used during serialization and deserialization of Avro.
     * <p>
     * If {@code specificData} is {@code null} when {@link #build()} is called {@link SpecificData#get()
     * SpecificData.get()} will be used as the generic data.
     *
     * @param specificData The {@link SpecificData} used during serialization and deserialization of Avro.
     * @return The updated ApacheAvroSerializerBuilder object.
     */
    public ApacheAvroSerializerBuilder specificData(SpecificData specificData) {
        this.specificData = specificData;
        return this;
    }
}
