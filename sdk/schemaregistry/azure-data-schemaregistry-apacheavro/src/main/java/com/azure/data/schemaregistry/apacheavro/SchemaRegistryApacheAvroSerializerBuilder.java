// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;

/**
 * The builder implementation for building {@link SchemaRegistryApacheAvroSerializer}.
 *
 * @see SchemaRegistryApacheAvroSerializer
 */
public final class SchemaRegistryApacheAvroSerializerBuilder {
    private static final boolean AVRO_SPECIFIC_READER_DEFAULT = false;
    private static final int MAX_CACHE_SIZE = 128;

    private Boolean autoRegisterSchemas;
    private Boolean avroSpecificReader;
    private SchemaRegistryAsyncClient schemaRegistryAsyncClient;
    private String schemaGroup;

    /**
     * Instantiates instance of Builder class. Supplies client defaults.
     */
    public SchemaRegistryApacheAvroSerializerBuilder() {
        this.autoRegisterSchemas = false;
        this.avroSpecificReader = false;
    }

    /**
     * Specifies schema group for interacting with Azure Schema Registry service. This is optional unless
     * {@link #autoRegisterSchema(boolean) autoRegisterSchema} is set to {@code true}.
     *
     * If auto-registering schemas, schema will be stored under this group. If not auto-registering, serializer will
     * request schema ID for matching data schema under specified group.
     *
     * @param schemaGroup Azure Schema Registry schema group
     *
     * @return updated {@link SchemaRegistryApacheAvroSerializerBuilder} instance
     */
    public SchemaRegistryApacheAvroSerializerBuilder schemaGroup(String schemaGroup) {
        this.schemaGroup = schemaGroup;
        return this;
    }

    /**
     * If specified true, serializer will register schemas against Azure Schema Registry service under the specified
     * group.  See Azure Schema Registry documentation for a description of schema registration behavior.
     *
     * If specified false, serializer will simply query the service for an existing ID given schema content.
     * Serialization will fail if the schema has not been pre-created.
     *
     * Auto-registration is <strong>NOT RECOMMENDED</strong> for production scenarios.
     *
     * @param autoRegisterSchemas flag for schema auto-registration
     *
     * @return updated {@link SchemaRegistryApacheAvroSerializerBuilder} instance
     */
    public SchemaRegistryApacheAvroSerializerBuilder autoRegisterSchema(boolean autoRegisterSchemas) {
        this.autoRegisterSchemas = autoRegisterSchemas;
        return this;
    }

    /**
     * Specifies if objects should be deserialized into Avro {@link SpecificRecord} via Avro's {@link
     * SpecificDatumReader}.
     *
     * @param avroSpecificReader {@code true} to deserialize into {@link SpecificRecord} via {@link
     *     SpecificDatumReader}; {@code false} otherwise.
     *
     * @return updated {@link SchemaRegistryApacheAvroSerializerBuilder} instance.
     */
    public SchemaRegistryApacheAvroSerializerBuilder avroSpecificReader(boolean avroSpecificReader) {
        this.avroSpecificReader = avroSpecificReader;
        return this;
    }

    /**
     * The {@link SchemaRegistryAsyncClient} to use to interact with the Schema Registry service.
     *
     * @param schemaRegistryAsyncClient The {@link SchemaRegistryAsyncClient}.
     *
     * @return updated {@link SchemaRegistryApacheAvroSerializerBuilder} instance.
     */
    public SchemaRegistryApacheAvroSerializerBuilder schemaRegistryAsyncClient(
        SchemaRegistryAsyncClient schemaRegistryAsyncClient) {
        this.schemaRegistryAsyncClient = schemaRegistryAsyncClient;
        return this;
    }

    /**
     * Creates a new instance of Schema Registry serializer.
     *
     * @return A new instance of {@link SchemaRegistryApacheAvroSerializer}.
     *
     * @throws NullPointerException if {@link #schemaRegistryAsyncClient(SchemaRegistryAsyncClient)} is {@code null}
     *     or {@link #schemaGroup(String) schemaGroup} is {@code null}.
     * @throws IllegalArgumentException if credential is not set.
     */
    public SchemaRegistryApacheAvroSerializer buildSerializer() {
        final boolean isAutoRegister = autoRegisterSchemas != null && autoRegisterSchemas;
        final boolean useAvroSpecificReader = avroSpecificReader == null
            ? AVRO_SPECIFIC_READER_DEFAULT : avroSpecificReader;
        final Schema.Parser parser = new Schema.Parser();
        final AvroSerializer codec = new AvroSerializer(useAvroSpecificReader, parser,
            EncoderFactory.get(), DecoderFactory.get());
        final SerializerOptions options = new SerializerOptions(schemaGroup, isAutoRegister, MAX_CACHE_SIZE);

        return new SchemaRegistryApacheAvroSerializer(schemaRegistryAsyncClient, codec, options);
    }
}
