// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.identity.ClientSecretCredentialBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AsyncSample
{
    public static void main(String[] args) throws InterruptedException
    {
        String tenantId = System.getenv("TENANT_ID");
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");
        String endpoint = System.getenv("DIGITAL_TWINS_ENDPOINT");
        String digitalTwinId = System.getenv("DIGITAL_TWIN_ID");
        String relationshipId = System.getenv("RELATIONSHIP_ID");
        String relationship = System.getenv("RELATIONSHIP");

        TokenCredential tokenCredential = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();

        DigitalTwinsAsyncClient client = new DigitalTwinsClientBuilder()
            .tokenCredential(tokenCredential)
            .endpoint(endpoint)
            .httpLogOptions(
                new HttpLogOptions()
                    .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();

        // Create the source and target twins
        final Semaphore createTwinsSemaphore = new Semaphore(0);

        // Create source digital twin
        Mono<String> sourceTwin = client.createDigitalTwin(sourceDigitalTwinId, sourceDigitalTwin);
        sourceTwin.subscribe(
            result -> System.out.println("Created source twin: " + result),
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + sourceDigitalTwinId + " due to error message " + throwable.getMessage()),
            // Once the createRelationship operation has completed asynchronously, release the corresponding semaphore so that
            // the thread that is subscribed to this method call can complete successfully.
            createTwinsSemaphore::release); // This is an method reference call, which is the same as the following lambda expression: () -> createSemaphore.release()

        // Create target digital twin
        Mono<String> targetTwin = client.createDigitalTwin(targetDigitalTwinId, targetDigitalTwin);
        targetTwin.subscribe(
            result -> System.out.println("Created target twin: " + result),
            throwable -> System.err.println("Failed to create target twin on digital twin with Id " + targetDigitalTwinId + " due to error message " + throwable.getMessage()),
            // Once the createRelationship operation has completed asynchronously, release the corresponding semaphore so that
            // the thread that is subscribed to this method call can complete successfully.
            createTwinsSemaphore::release); // This is an method reference call, which is the same as the following lambda expression: () -> createSemaphore.release()

        boolean created = createTwinsSemaphore.tryAcquire(2, 5, TimeUnit.SECONDS);
        System.out.println("Source and target twins created: " + created);

        // Create relationship on a digital twin

        // Initialize a semaphore with no permits available. A permit must be released before this semaphore can be grabbed by a thread.
        final Semaphore createSemaphore = new Semaphore(0);

        Mono<String> createdRelationship = client.createRelationship(sourceDigitalTwinId, relationshipId, relationship);

        // Once the async thread completes, the relationship created on the digital twin will be printed, or an error will be printed.
        createdRelationship.subscribe(
            result -> System.out.println("Created relationship: " + result),
            throwable -> System.err.println("Failed to create relationship " + relationshipId + "on digital twin with Id " + sourceDigitalTwin + " due to error message " + throwable.getMessage()),
            // Once the createRelationship operation has completed asynchronously, release the corresponding semaphore so that
            // the thread that is subscribed to this method call can complete successfully.
            createSemaphore::release); // This is an method reference call, which is the same as the following lambda expression: () -> createSemaphore.release()

        // Wait for a maximum of 5secs to acquire the semaphore.
        boolean createSuccessful = createSemaphore.tryAcquire(5, TimeUnit.SECONDS);

        if (createSuccessful) {
            System.out.println("Create operations completed successfully.");
        } else {
            System.out.println("Could not finish the create operations within the specified time.");
        }

        if (createSuccessful) {
            // List all relationships on a digital twin

            // Initialize a semaphore with no permits available. A permit must be released before this semaphore can be grabbed by a thread.
            final Semaphore listSemaphore = new Semaphore(0);

            PagedFlux<Object> relationships = client.listRelationships(sourceDigitalTwinId, relationshipId);

            // Subscribe to process one relationship at a time
            relationships
                .subscribe(
                    item -> System.out.println("Relationship retrieved: " + item),
                    throwable -> System.err.println("Error occurred while retrieving relationship: " + throwable),
                    () -> {
                        System.out.println("Completed processing, all relationships have been retrieved.");
                        // Once the createRelationship operation has completed asynchronously, release the corresponding semaphore so that
                        // the thread that is subscribed to this API call can complete successfully.
                        listSemaphore.release();
                    });

            // Subscribe to process one page at a time from the beginning
            relationships
                // You can also subscribe to pages by specifying the preferred page size or the associated continuation token to start the processing from.
                .byPage()
                .subscribe(
                    page -> {
                        System.out.println("Response headers status code is " + page.getStatusCode());
                        page.getValue().forEach(item -> System.out.println("Relationship retrieved: " + item));
                    },
                    throwable -> System.err.println("Error occurred while retrieving relationship: " + throwable),
                    () -> {
                        System.out.println("Completed processing, all relationships have been retrieved.");
                        // Once the createRelationship operation has completed asynchronously, release the corresponding semaphore so that
                        // the thread that is subscribed to this API call can complete successfully.
                        listSemaphore.release();
                    }
                );

            // Wait for a maximum of 5secs to acquire both the semaphores.
            boolean listSuccessful = listSemaphore.tryAcquire(2,5, TimeUnit.SECONDS);

            if (listSuccessful) {
                System.out.println("List operations completed successfully.");
            } else {
                System.out.println("Could not finish the list operations within the specified time.");
            }

            System.out.println("Done, exiting.");
        }
    }
}
