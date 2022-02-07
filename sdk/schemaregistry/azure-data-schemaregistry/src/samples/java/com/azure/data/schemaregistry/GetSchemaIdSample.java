// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample to demonstrate retrieving properties of a schema from Schema Registry.
 *
 * @see GetSchemaIdSampleAsync for the async sample.
 */
public class GetSchemaIdSample {

    /**
     * The main method to run this program.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildClient();

        // Gets the properties of an existing schema.
        SchemaProperties schemaProperties = schemaRegistryClient
            .getSchemaProperties("{group-name}", "{schema-name}", "{schema-string}", SchemaFormat.AVRO);

        System.out.println("Successfully retrieved the schema id: " + schemaProperties.getId());
    }
}
