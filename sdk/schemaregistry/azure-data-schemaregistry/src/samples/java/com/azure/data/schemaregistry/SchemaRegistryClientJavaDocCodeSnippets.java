// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
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
        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.construct
        DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder()
            .build();
        SchemaRegistryClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("https://<your-schema-registry-endpoint>.servicebus.windows.net")
            .credential(azureCredential)
            .buildClient();
        // END: com.azure.data.schemaregistry.schemaregistryclient.construct
    }

    /**
     * Instantiates {@link SchemaRegistryAsyncClient}.
     */
    public void createAsyncClient() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.construct
        DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder()
            .build();
        SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("https://<your-schema-registry-endpoint>.servicebus.windows.net")
            .credential(azureCredential)
            .buildAsyncClient();
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.construct
    }

    /**
     * Updates retry policy and logging.
     */
    public void retryPolicyAndLogging() {
        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.retrypolicy.construct
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
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.retrypolicy.construct
    }

    /**
     * Shows how to register a schema.
     */
    public void registerSchema() {
        SchemaRegistryClient client = buildClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.registerschema-avro
        String schema = "{\"type\":\"enum\",\"name\":\"TEST\",\"symbols\":[\"UNIT\",\"INTEGRATION\"]}";
        SchemaProperties properties = client.registerSchema("{schema-group}", "{schema-name}", schema,
            SchemaFormat.AVRO);

        System.out.printf("Schema id: %s, schema format: %s%n", properties.getId(), properties.getFormat());
        // END: com.azure.data.schemaregistry.schemaregistryclient.registerschema-avro
    }

    /**
     * Shows how to register a schema using async client.
     */
    public void registerSchemaAsync() {
        SchemaRegistryAsyncClient client = buildAsyncClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.registerschema-avro
        String schema = "{\"type\":\"enum\",\"name\":\"TEST\",\"symbols\":[\"UNIT\",\"INTEGRATION\"]}";
        client.registerSchema("{schema-group}", "{schema-name}", schema, SchemaFormat.AVRO)
            .subscribe(properties -> {
                System.out.printf("Schema id: %s, schema format: %s%n", properties.getId(),
                    properties.getFormat());
            }, error -> {
                System.err.println("Error occurred registering schema: " + error);
            }, () -> {
                System.out.println("Register schema completed.");
            });
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.registerschema-avro
    }

    /**
     * Gets schema by id.
     */
    public void getSchema() {
        SchemaRegistryClient client = buildClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.getschema
        SchemaRegistrySchema schema = client.getSchema("{schema-id}");

        System.out.printf("Schema id: %s, schema format: %s%n", schema.getProperties().getId(),
            schema.getProperties().getFormat());
        System.out.println("Schema contents: " + schema.getDefinition());
        // END: com.azure.data.schemaregistry.schemaregistryclient.getschema
    }

    /**
     * Gets schema by name, group name, version.
     */
    public void getSchemaWithResponse() {
        SchemaRegistryClient client = buildClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.getschemawithresponse
        Response<SchemaRegistrySchema> response = client.getSchemaWithResponse("{group-name}",
            "{schema-name}", 1, Context.NONE);

        System.out.println("Headers in HTTP response: ");
        response.getHeaders().forEach(header -> System.out.printf("%s: %s%n", header.getName(), header.getValue()));

        SchemaRegistrySchema schema = response.getValue();

        System.out.printf("Schema id: %s, schema format: %s%n", schema.getProperties().getId(),
            schema.getProperties().getFormat());
        System.out.println("Schema contents: " + schema.getDefinition());
        // END: com.azure.data.schemaregistry.schemaregistryclient.getschemawithresponse
    }

    /**
     * Gets schema by id using async client.
     */
    public void getSchemaAsync() {
        SchemaRegistryAsyncClient client = buildAsyncClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.getschema
        client.getSchema("{schema-id}")
            .subscribe(schema -> {
                System.out.printf("Schema id: %s, schema format: %s%n", schema.getProperties().getId(),
                    schema.getProperties().getFormat());
                System.out.println("Schema contents: " + schema.getDefinition());
            }, error -> {
                System.err.println("Error occurred getting schema: " + error);
            }, () -> {
                System.out.println("Get schema completed.");
            });
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.getschema
    }

    /**
     * Gets schema by name, group name, version using async client.
     */
    public void getSchemaWithResponseAsync() {
        SchemaRegistryAsyncClient client = buildAsyncClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.getschemawithresponse
        client.getSchemaWithResponse("{group-name}",
                "{schema-name}", 1, Context.NONE)
            .subscribe(response -> {
                System.out.println("Headers in HTTP response: ");

                for (HttpHeader header : response.getHeaders()) {
                    System.out.printf("%s: %s%n", header.getName(), header.getValue());
                }

                SchemaRegistrySchema schema = response.getValue();

                System.out.printf("Schema id: %s, schema format: %s%n", schema.getProperties().getId(),
                    schema.getProperties().getFormat());
                System.out.println("Schema contents: " + schema.getDefinition());
            }, error -> {
                System.err.println("Error occurred getting schema: " + error);
            }, () -> {
                System.out.println("Get schema with response completed.");
            });
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.getschemawithresponse
    }

    /**
     * Gets the schema properties.
     */
    public void getSchemaProperties() {
        SchemaRegistryClient client = buildClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryclient.getschemaproperties
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
        SchemaProperties properties = client.getSchemaProperties("{schema-group}", "{schema-name}",
            schemaContent, SchemaFormat.AVRO);

        System.out.println("Schema id: " + properties.getId());
        System.out.println("Format: " + properties.getFormat());
        System.out.println("Version: " + properties.getVersion());
        // END: com.azure.data.schemaregistry.schemaregistryclient.getschemaproperties
    }

    /**
     * Gets the schema properties.
     */
    public void getSchemaPropertiesAsync() {
        SchemaRegistryAsyncClient client = buildAsyncClient();

        // BEGIN: com.azure.data.schemaregistry.schemaregistryasyncclient.getschemaproperties
        String schema = "{\"type\":\"enum\",\"name\":\"TEST\",\"symbols\":[\"UNIT\",\"INTEGRATION\"]}";
        client.getSchemaProperties("{schema-group}", "{schema-name}", schema, SchemaFormat.AVRO)
            .subscribe(properties -> {
                System.out.println("Schema id: " + properties.getId());
                System.out.println("Format: " + properties.getFormat());
                System.out.println("Version: " + properties.getVersion());
            }, error -> {
                System.err.println("Error occurred getting schema: " + error);
            }, () -> {
                System.out.println("Get schema completed.");
            });
        // END: com.azure.data.schemaregistry.schemaregistryasyncclient.getschemaproperties
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
