// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;

import java.util.Objects;

/**
 * The builder for instantiating a {@link SchemaRegistryApacheAvroSerializer}. Additional code samples are in
 * {@link SchemaRegistryApacheAvroSerializer}.
 *
 * <p><strong>Sample: Creating a SchemaRegistryApacheAvroSerializer</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the serializer and
 * {@link com.azure.data.schemaregistry.SchemaRegistryAsyncClient}.  The credential used to create the async client is
 * {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and development and
 * chooses the credential to used based on its running environment.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.construct -->
 * <pre>
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder&#40;&#41;
 *     .credential&#40;tokenCredential&#41;
 *     .fullyQualifiedNamespace&#40;&quot;&#123;schema-registry-endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder&#40;&#41;
 *     .schemaRegistryClient&#40;schemaRegistryAsyncClient&#41;
 *     .schemaGroup&#40;&quot;&#123;schema-group&#125;&quot;&#41;
 *     .buildSerializer&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.construct -->
 *
 * @see SchemaRegistryApacheAvroSerializer
 */
public final class SchemaRegistryApacheAvroSerializerBuilder {
    private static final boolean AVRO_SPECIFIC_READER_DEFAULT = false;
    static final int MAX_CACHE_SIZE = 128;

    private final ClientLogger logger = new ClientLogger(SchemaRegistryApacheAvroSerializerBuilder.class);
    private Boolean autoRegisterSchemas;
    private Boolean avroSpecificReader;
    private SchemaRegistryAsyncClient schemaRegistryAsyncClient;
    private String schemaGroup;

    /**
     * Instantiates instance of this class.  By default, {@link #autoRegisterSchemas(boolean)} is false and
     * {@link #avroSpecificReader(boolean)} is false.
     */
    public SchemaRegistryApacheAvroSerializerBuilder() {
        this.autoRegisterSchemas = false;
        this.avroSpecificReader = false;
    }

    /**
     * <p>Specifies schema group for interacting with Azure Schema Registry service. This is optional unless
     * {@link #autoRegisterSchemas(boolean) autoRegisterSchema} is set to {@code true}.</p>
     *
     * <p>If auto-registering schemas, schema will be stored under this group. If not auto-registering, serializer will
     * request schema ID for matching data schema under specified group.</p>
     *
     * @param schemaGroup Schema Registry group name.
     *
     * @return The updated {@link SchemaRegistryApacheAvroSerializerBuilder} instance.
     */
    public SchemaRegistryApacheAvroSerializerBuilder schemaGroup(String schemaGroup) {
        this.schemaGroup = schemaGroup;
        return this;
    }

    /**
     * <p>If true, the serializer will register schemas against Azure Schema Registry service under the specified
     * group if it fails to find an existing schema to serialize.  See
     * <a href="https://learn.microsoft.com/azure/event-hubs/schema-registry-overview">Azure Schema Registry
     * documentation</a> for a description of schema registration behavior.</p>
     *
     * <p>If specified false, serializer will query the service for an existing ID given schema content.
     * Serialization will fail if the schema has not been pre-created.</p>
     *
     * Auto-registration is <strong>NOT RECOMMENDED</strong> for production scenarios.
     *
     * @param autoRegisterSchemas flag for schema auto-registration
     *
     * @return The updated {@link SchemaRegistryApacheAvroSerializerBuilder} instance.
     */
    public SchemaRegistryApacheAvroSerializerBuilder autoRegisterSchemas(boolean autoRegisterSchemas) {
        this.autoRegisterSchemas = autoRegisterSchemas;
        return this;
    }

    /**
     * Specifies if objects should be deserialized into Avro {@link SpecificRecord} via Avro's {@link
     * SpecificDatumReader}. If {@code false} then {@link GenericDatumReader} is used.
     *
     * @param avroSpecificReader {@code true} to deserialize into {@link SpecificRecord} via {@link
     *         SpecificDatumReader}; {@code false} otherwise.
     *
     * @return The updated {@link SchemaRegistryApacheAvroSerializerBuilder} instance.
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
     * @return The updated {@link SchemaRegistryApacheAvroSerializerBuilder} instance.
     */
    public SchemaRegistryApacheAvroSerializerBuilder
        schemaRegistryClient(SchemaRegistryAsyncClient schemaRegistryAsyncClient) {
        this.schemaRegistryAsyncClient = schemaRegistryAsyncClient;
        return this;
    }

    /**
     * Creates a new instance of Schema Registry serializer.
     *
     * @return A new instance of {@link SchemaRegistryApacheAvroSerializer}.
     *
     * @throws NullPointerException if {@link #schemaRegistryClient(SchemaRegistryAsyncClient)} is {@code null}.
     * @throws IllegalStateException if {@link #autoRegisterSchemas(boolean)} is {@code true} but {@link
     *         #schemaGroup(String) schemaGroup} is {@code null}.
     */
    public SchemaRegistryApacheAvroSerializer buildSerializer() {
        final boolean isAutoRegister = autoRegisterSchemas != null && autoRegisterSchemas;

        if (Objects.isNull(schemaRegistryAsyncClient)) {
            throw logger.logExceptionAsError(new NullPointerException("'schemaRegistryAsyncClient' cannot be null."));
        }

        if (isAutoRegister && CoreUtils.isNullOrEmpty(schemaGroup)) {
            throw logger.logExceptionAsError(
                new IllegalStateException("'schemaGroup' cannot be null or empty when 'autoRegisterSchema' is true."));
        }

        final boolean useAvroSpecificReader
            = avroSpecificReader == null ? AVRO_SPECIFIC_READER_DEFAULT : avroSpecificReader;
        final SerializerOptions options = new SerializerOptions(schemaGroup, isAutoRegister, MAX_CACHE_SIZE);
        final AvroSerializer codec
            = new AvroSerializer(useAvroSpecificReader, EncoderFactory.get(), DecoderFactory.get());

        return new SchemaRegistryApacheAvroSerializer(schemaRegistryAsyncClient, codec, options);
    }
}
