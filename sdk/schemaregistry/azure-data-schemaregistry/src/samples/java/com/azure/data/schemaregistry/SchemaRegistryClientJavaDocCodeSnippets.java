// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;

/**
 * Snippets for building and using {@link SchemaRegistryClient} and {@link SchemaRegistryAsyncClient}.
 */
public class SchemaRegistryClientJavaDocCodeSnippets {
    /**
     * Instantiates {@link SchemaRegistryClient}.
     */
    public void createClient() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.instantiation
        // AAD credential to authorize with Schema Registry service.
        DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder()
            .build();
        SchemaRegistryClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("https://<your-schema-registry-endpoint>.servicebus.windows.net")
            .credential(azureCredential)
            .buildClient();
        // END: com.azure.data.schemaregistry.schemaregistryclient.instantiation
    }

    /**
     * Instantiates {@link SchemaRegistryAsyncClient}.
     */
    public void createAsyncClient() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.instantiation
        // AAD credential to authorize with Schema Registry service.
        DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder()
            .build();
        SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("https://<your-schema-registry-endpoint>.servicebus.windows.net")
            .credential(azureCredential)
            .buildAsyncClient();
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.instantiation
    }

    /**
     * Updates retry policy and logging.
     */
    public void retryPolicyAndLogging() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.retrypolicy.instantiation
        DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder()
            .build();

        HttpLogOptions httpLogOptions = new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.BODY)
            .setPrettyPrintBody(true);

        RetryPolicy retryPolicy = new RetryPolicy(new FixedDelay(5, Duration.ofSeconds(30)));
        SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("https://<your-schema-registry-endpoint>.servicebus.windows.net")
            .httpLogOptions(httpLogOptions)
            .retryPolicy(retryPolicy)
            .credential(azureCredential)
            .buildAsyncClient();
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.retrypolicy.instantiation
    }

    /**
     * Shows how to register a schema.
     */
    public void registerSchema() {
        SchemaRegistryClient client = buildClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.registerschema
        String schema = "{\"type\":\"enum\",\"name\":\"TEST\",\"symbols\":[\"UNIT\",\"INTEGRATION\"]}";
        SchemaProperties properties = client.registerSchema("{schema-group}", "{schema-name}", schema,
            SchemaFormat.AVRO);

        System.out.printf("Schema id: %s, schema format: %s%n", properties.getSchemaId(), properties.getFormat());
        // END: com.azure.data.schemaregistry.schemaregistryclient.registerschema
    }

    /**
     * Shows how to register a schema using async client.
     */
    public void registerSchemaAsync() {
        SchemaRegistryAsyncClient client = buildAsyncClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.registerschema
        String schema = "{\"type\":\"enum\",\"name\":\"TEST\",\"symbols\":[\"UNIT\",\"INTEGRATION\"]}";
        client.registerSchema("{schema-group}", "{schema-name}", schema, SchemaFormat.AVRO)
            .subscribe(properties -> {
                System.out.printf("Schema id: %s, schema format: %s%n", properties.getSchemaId(),
                    properties.getFormat());
            });
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.registerschema
    }

    /**
     * Gets schema by id.
     */
    public void getSchema() {
        SchemaRegistryClient client = buildClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.getSchema
        SchemaRegistrySchema schema = client.getSchema("{schema-id}");

        System.out.printf("Schema id: %s, schema format: %s%n", schema.getProperties().getSchemaId(),
            schema.getProperties().getFormat());
        System.out.println("Schema contents: " + schema.getContent());
        // END: com.azure.data.schemaregistry.schemaregistryclient.getSchema
    }

    /**
     * Gets schema by id using async client.
     */
    public void getSchemaAsync() {
        SchemaRegistryAsyncClient client = buildAsyncClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.getSchema
        client.getSchema("{schema-id}").subscribe(schema -> {
            System.out.printf("Schema id: %s, schema format: %s%n", schema.getProperties().getSchemaId(),
                schema.getProperties().getFormat());
            System.out.println("Schema contents: " + schema.getContent());
        });
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.getSchema
    }

    /**
     * Gets the schema properties.
     */
    public void getSchemaProperties() {
        SchemaRegistryClient client = buildClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.getSchemaId
        String schema = "{\"type\":\"enum\",\"name\":\"TEST\",\"symbols\":[\"UNIT\",\"INTEGRATION\"]}";
        SchemaProperties properties = client.getSchemaProperties("{schema-group}", "{schema-name}", schema,
            SchemaFormat.AVRO);

        System.out.println("The schema id: " + properties.getSchemaId());
        // END: com.azure.data.schemaregistry.schemaregistryclient.getSchemaId
    }

    /**
     * Gets the schema properties.
     */
    public void getSchemaPropertiesAsync() {
        SchemaRegistryAsyncClient client = buildAsyncClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.getSchemaId
        String schema = "{\"type\":\"enum\",\"name\":\"TEST\",\"symbols\":[\"UNIT\",\"INTEGRATION\"]}";
        client.getSchemaProperties("{schema-group}", "{schema-name}", schema,
            SchemaFormat.AVRO).subscribe(properties -> {
                System.out.println("The schema id: " + properties.getSchemaId());
            });
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.getSchemaId
    }

    private static SchemaRegistryAsyncClient buildAsyncClient() {
        return new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
    }

    private static SchemaRegistryClient buildClient() {
        return new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }
}
