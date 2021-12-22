// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Code samples for the README.md
 */
@SuppressWarnings("unused")
public class ReadmeSamples {
    private final SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
        .fullyQualifiedNamespace("{schema-registry-endpoint")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

    /**
     * Sample for creating async client.
     */
    public void createAsyncClient() {
        // BEGIN: readme-sample-createAsyncClient
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildAsyncClient();
        // END: readme-sample-createAsyncClient
    }

    /**
     * Sample for creating sync client.
     */
    public void createSyncClient() {
        // BEGIN: readme-sample-createSyncClient
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildClient();
        // END: readme-sample-createSyncClient
    }

    /**
     * Sample for registering a schema.
     */
    public void registerSchema() {
        // BEGIN: readme-sample-registerSchema
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
        // END: readme-sample-registerSchema
    }

    /**
     * Sample for getting the schema's properties from a schema.
     */
    public void getSchema() {
        // BEGIN: readme-sample-getSchema
        SchemaRegistrySchema schema = schemaRegistryClient.getSchema("{schema-id}");

        System.out.printf("Retrieved schema: '%s'. Contents: %s%n", schema.getProperties().getId(),
            schema.getDefinition());
        // END: readme-sample-getSchema
    }

    /**
     * Sample for getting the schema id of a registered schema.
     */
    public void getSchemaId() {
        // BEGIN: readme-sample-getSchemaId
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
        // END: readme-sample-getSchemaId
    }
}
