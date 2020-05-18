// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;

/**
 * Fluent builder class that configures and instantiates instances of {@link ApacheAvroSerializer}.
 *
 * @see ApacheAvroSerializer
 */
public class ApacheAvroSerializerBuilder {
    private Schema.Parser parser;
    private DecoderFactory decoderFactory;
    private EncoderFactory encoderFactory;
    private GenericData genericData;

    /**
     * Instantiates a new instance of {@link ApacheAvroSerializer} based on the configurations set on the builder.
     *
     * @return A new instance of {@link ApacheAvroSerializer}.
     */
    public ApacheAvroSerializer build() {
        Schema.Parser buildParser = (parser == null) ? new Schema.Parser() : parser;
        DecoderFactory buildDecoderFactory = (decoderFactory == null) ? DecoderFactory.get() : decoderFactory;
        EncoderFactory buildEncoderFactory = (encoderFactory == null) ? EncoderFactory.get() : encoderFactory;
        GenericData buildGenericData = (genericData == null) ? GenericData.get() : genericData;

        return new ApacheAvroSerializer(buildParser, buildDecoderFactory, buildEncoderFactory, buildGenericData);
    }

    /**
     * Configures the {@link Schema.Parser} that will be used to parse schema strings passed into decode and encode
     * methods on {@link ApacheAvroSerializer}.
     * <p>
     * If {@code parser} is {@code null} when {@link #build()} is called {@link Schema.Parser#Parser() new
     * Schema.Parser()} will be used as the parser.
     *
     * @param parser The {@link Schema.Parser} used to parse schema strings.
     * @return The updated ApacheAvroSerializerBuilder object.
     */
    public ApacheAvroSerializerBuilder schemaParser(Schema.Parser parser) {
        this.parser = parser;
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

    public ApacheAvroSerializerBuilder genericData(GenericData genericData) {
        this.genericData = genericData;
        return this;
    }
}
