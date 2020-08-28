// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SyncSample
{
    public static void main(String[] args) throws JsonProcessingException {
        String tenantId = System.getenv("TENANT_ID");
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");
        String endpoint = System.getenv("DIGITAL_TWINS_ENDPOINT");
        String sourceDigitalTwinId = System.getenv("SOURCE_DIGITAL_TWIN_ID");
        String sourceDigitalTwin = System.getenv("SOURCE_DIGITAL_TWIN");
        String targetDigitalTwinId = System.getenv("TARGET_DIGITAL_TWIN_ID");
        String targetDigitalTwin = System.getenv("TARGET_DIGITAL_TWIN");
        String relationshipId = System.getenv("RELATIONSHIP_ID");
        String relationship = System.getenv("RELATIONSHIP");

        TokenCredential tokenCredential = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();

        DigitalTwinsClient client = new DigitalTwinsClientBuilder()
            .tokenCredential(tokenCredential)
            .endpoint(endpoint)
            .buildClient();

        // Create relationship on a digital twin
        String createdRelationship = client.createRelationshipWithResponse(sourceDigitalTwinId, relationshipId, relationship, Context.NONE).getValue();
        System.out.println("Created relationship: " + createdRelationship);

        // List all relationships on a digital twin
        PagedIterable<String> relationships = client.listRelationships(sourceDigitalTwinId, relationshipId, Context.NONE);

        // Process using the Stream interface by iterating over each page
        relationships
            // You can also subscribe to pages by specifying the preferred page size or the associated continuation token to start the processing from.
            .streamByPage()
            .forEach(page -> {
                System.out.println("Response headers status code is " + page.getStatusCode());
                page.getValue().forEach(item -> System.out.println("Relationship retrieved: " + item));
            });

        // Process using the Iterable interface by iterating over each page
        relationships
            // You can also subscribe to pages by specifying the preferred page size or the associated continuation token to start the processing from.
            .iterableByPage()
            .forEach(page -> {
                System.out.println("Response headers status code is " + page.getStatusCode());
                page.getValue().forEach(item -> System.out.println("Relationship retrieved: " + item));
            });
    }
}
