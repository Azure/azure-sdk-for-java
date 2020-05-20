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
    private boolean validateSchema = true;
    private boolean validateSchemaDefaults = true;
    private DecoderFactory decoderFactory;
    private EncoderFactory encoderFactory;
    private GenericData genericData;

    /**
     * Instantiates a new instance of {@link ApacheAvroSerializer} based on the configurations set on the builder.
     *
     * @return A new instance of {@link ApacheAvroSerializer}.
     */
    public ApacheAvroSerializer build() {
        DecoderFactory buildDecoderFactory = (decoderFactory == null) ? DecoderFactory.get() : decoderFactory;
        EncoderFactory buildEncoderFactory = (encoderFactory == null) ? EncoderFactory.get() : encoderFactory;
        GenericData buildGenericData = (genericData == null) ? GenericData.get() : genericData;

        return new ApacheAvroSerializer(validateSchema, validateSchemaDefaults, buildDecoderFactory,
            buildEncoderFactory, buildGenericData);
    }

    /**
     * Configures the {@link ApacheAvroSerializer} to validate that a passed schema meets Avro specifications.
     * <p>
     * By default {@code validateSchema} is {@code true}.
     *
     * @param validateSchema Flag indicating if schema should be validated.
     * @return The updated ApacheAvroSerializerBuilder object.
     */
    public ApacheAvroSerializerBuilder validateSchema(boolean validateSchema) {
        this.validateSchema = validateSchema;
        return this;
    }

    /**
     * Configures the {@link ApacheAvroSerializer} to validate default values in the passed schema.
     * <p>
     * By default {@code validateSchemaDefaults} is {@code true}.
     *
     * @param validateSchemaDefaults Flag indicating if schema defaults should be validated.
     * @return The updated ApacheAvroSerializerBuilder object.
     */
    public ApacheAvroSerializerBuilder validateSchemaDefaults(boolean validateSchemaDefaults) {
        this.validateSchemaDefaults = validateSchemaDefaults;
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
     * Configures the {@link GenericData} that will be used during serialization and deserialization of Avro.
     * <p>
     * If {@code genericData} is {@code null} when {@link #build()} is called {@link GenericData#get()
     * GenericData.get()} will be used as the generic data.
     *
     * @param genericData The {@link GenericData} used during serialization and deserialization of Avro.
     * @return The updated ApacheAvroSerializerBuilder object.
     */
    public ApacheAvroSerializerBuilder genericData(GenericData genericData) {
        this.genericData = genericData;
        return this;
    }
}
