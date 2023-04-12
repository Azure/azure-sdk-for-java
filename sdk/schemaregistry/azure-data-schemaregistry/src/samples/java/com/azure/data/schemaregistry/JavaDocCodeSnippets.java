// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Code samples for Javadocs
 */
@SuppressWarnings("unused")
public class JavaDocCodeSnippets {
    private final SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
        .fullyQualifiedNamespace("{schema-registry-endpoint}")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

    /**
     * Sample for creating async client.
     */
    public void createAsyncClient() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.construct
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildAsyncClient();
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.construct
    }

    /**
     * Sample for creating sync client.
     */
    public void createSyncClient() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.construct
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildClient();
        // END: com.azure.data.schemaregistry.schemaregistryclient.construct
    }

    /**
     * Sample for registering an avro schema.
     */
    public void registerSchema() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.registerschema-avro
        SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        String schemaContent = "{\n"
            + "    \"type\" : \"record\",  \n"
            + "    \"namespace\" : \"SampleSchemaNameSpace\", \n"
            + "    \"name\" : \"Person\", \n"
            + "    \"fields\" : [\n"
            + "        { \n"
            + "            \"name\" : \"FirstName\" , \"type\" : \"string\" \n"
            + "        }, \n"
            + "        { \n"
            + "            \"name\" : \"LastName\", \"type\" : \"string\" \n"
            + "        }\n"
            + "    ]\n"
            + "}";
        SchemaProperties schemaProperties = schemaRegistryClient.registerSchema("{schema-group}", "{schema-name}",
            schemaContent, SchemaFormat.AVRO);

        System.out.println("Registered schema: " + schemaProperties.getId());
        // END: com.azure.data.schemaregistry.schemaregistryclient.registerschema-avro
    }

    /**
     * Sample for getting the schema's properties from a schema.
     */
    public void getSchema() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.getschema
        SchemaRegistrySchema schema = schemaRegistryClient.getSchema("{schema-id}");

        System.out.printf("Retrieved schema: '%s'. Contents: %s%n", schema.getProperties().getId(),
            schema.getDefinition());
        // END: com.azure.data.schemaregistry.schemaregistryclient.getschema
    }

    /**
     * Sample for getting the schema id of a registered schema.
     */
    public void getSchemaId() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.getschemaid
        String schemaContent = "{\n"
            + "    \"type\" : \"record\",  \n"
            + "    \"namespace\" : \"SampleSchemaNameSpace\", \n"
            + "    \"name\" : \"Person\", \n"
            + "    \"fields\" : [\n"
            + "        { \n"
            + "            \"name\" : \"FirstName\" , \"type\" : \"string\" \n"
            + "        }, \n"
            + "        { \n"
            + "            \"name\" : \"LastName\", \"type\" : \"string\" \n"
            + "        }\n"
            + "    ]\n"
            + "}";
        SchemaProperties properties = schemaRegistryClient.getSchemaProperties("{schema-group}", "{schema-name}",
            schemaContent, SchemaFormat.AVRO);

        System.out.println("Retrieved schema id: " + properties.getId());
        // END: com.azure.data.schemaregistry.schemaregistryclient.getschemaid
    }
}
