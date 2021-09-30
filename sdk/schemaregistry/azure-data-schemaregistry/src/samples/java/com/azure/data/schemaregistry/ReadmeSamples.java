// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationFormat;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    /**
     * Sample for creating async client.
     */
    public void createAsyncClient() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .endpoint("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildAsyncClient();
    }

    /**
     * Sample for creating sync client.
     */
    public void createSyncClient() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
            .endpoint("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildClient();
    }

    /**
     * Sample for registering a schema.
     */
    public void registerSchema() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
            .endpoint("{schema-registry-endpoint")
            .credential(tokenCredential)
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
            schemaContent, SerializationFormat.AVRO);
        System.out.println("Registered schema: " + schemaProperties.getSchemaId());
    }

    /**
     * Sample for getting the schema from a schema id.
     */
    public void getSchema() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
            .endpoint("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildClient();

        SchemaProperties schemaProperties = schemaRegistryClient.getSchema("{schema-id}");
        System.out.println("Retrieved schema: " + schemaProperties.getSchemaName());
    }

    /**
     * Sample for getting the schema id of a registered schema.
     */
    public void getSchemaId() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
            .endpoint("{schema-registry-endpoint")
            .credential(tokenCredential)
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
        String schemaId = schemaRegistryClient.getSchemaId("{schema-group}", "{schema-name}",
            schemaContent, SerializationFormat.AVRO);
        System.out.println("Retreived schema id: " + schemaId);
    }
}
