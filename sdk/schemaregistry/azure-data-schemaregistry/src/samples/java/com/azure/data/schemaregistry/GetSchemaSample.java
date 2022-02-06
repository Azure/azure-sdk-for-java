// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample to demonstrate retrieving a schema from Schema Registry using the sync client.
 */
public class GetSchemaSample {
    /**
     * The main method to run this program.
     *
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildClient();

        // Get a schema using its id. The schema id is generated when it is registered via the client or Azure Portal.
        SchemaRegistrySchema schema = client.getSchema("{schema-id}");

        System.out.println("Successfully retrieved schema.");
        System.out.printf("Id: %s%nContents: %s%n", schema.getProperties().getId(), schema.getDefinition());

    }
}
