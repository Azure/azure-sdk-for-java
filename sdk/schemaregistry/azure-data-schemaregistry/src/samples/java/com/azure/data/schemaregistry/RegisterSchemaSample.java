// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample to demonstrate registering a schema with Schema Registry.
 *
 * @see RegisterSchemaSampleAsync for the async sample.
 */
public class RegisterSchemaSample {
    /**
     * The main method to run this program.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint}")
            .credential(tokenCredential)
            .buildClient();

        // Register a schema
        SchemaProperties schemaProperties = client
            .registerSchema("{group-name}", "{schema-name}", "{schema-string}", SchemaFormat.AVRO);

        System.out.println("Successfully registered a schema with id " + schemaProperties.getId());
        System.out.println("Schema Group: " + schemaProperties.getGroupName());
        System.out.println("Schema Name: " + schemaProperties.getName());
    }
}
