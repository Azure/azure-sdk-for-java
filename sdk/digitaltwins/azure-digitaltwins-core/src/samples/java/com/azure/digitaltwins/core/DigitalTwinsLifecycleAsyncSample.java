// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.implementation.serialization.BasicRelationship;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * This sample creates all the models in \DTDL\Models folder in the ADT service instance and creates the corresponding twins in \DTDL\DigitalTwins folder.
 * The Diagram for the Hospital model looks like this:
 *
 *          +------------+
 *          |  Building  +-----isEquippedWith-----+
 *          +------------+                        |
 *                |                               v
 *               has                           +-----+
 *                |                            | HVAC|
 *                v                            +-----+
 *          +------------+                        |
 *          |   Floor    +<--controlsTemperature--+
 *          +------------+
 *                |
 *             contains
 *                |
 *                v
 *          +------------+                 +-----------------+
 *          |   Room     |-with component->| WifiAccessPoint |
 *          +------------+                 +-----------------+
 *
 */
public class DigitalTwinsLifecycleAsyncSample {

    private static final int MaxWaitTimeAsyncOperationsInSeconds = 10;

    private static final URL DtdlDirectoryUrl = DigitalTwinsLifecycleAsyncSample.class.getClassLoader().getResource("DTDL");
    private static final Path DtDlDirectoryPath;
    private static final Path TwinsPath;
    private static final Path ModelsPath;
    private static final Path RelationshipsPath;

    private static DigitalTwinsAsyncClient client;

    static {
        try {
            assert DtdlDirectoryUrl != null;
            DtDlDirectoryPath = Paths.get(DtdlDirectoryUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to convert the DTDL directory URL to URI", e);
        }
        TwinsPath = Paths.get(DtDlDirectoryPath.toString(), "DigitalTwins");
        ModelsPath = Paths.get(DtDlDirectoryPath.toString(), "Models");
        RelationshipsPath = Paths.get(DtDlDirectoryPath.toString(), "Relationships");
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        SamplesArguments parsedArguments = new SamplesArguments(args);

        client = new DigitalTwinsClientBuilder()
            .tokenCredential(
                new ClientSecretCredentialBuilder()
                    .tenantId(parsedArguments.getTenantId())
                    .clientId(parsedArguments.getClientId())
                    .clientSecret(parsedArguments.getClientSecret())
                    .build()
            )
            .endpoint(parsedArguments.getDigitalTwinEndpoint())
            .httpLogOptions(
                new HttpLogOptions()
                    .setLogLevel(parsedArguments.getHttpLogDetailLevel()))
            .buildAsyncClient();

        // Ensure existing twins with the same name are deleted first
        deleteTwins();

        // Create twin counterparts for all the models
        createTwins();
    }

    /**
     * Delete a twin, and any relationships it might have.
     * @throws IOException If an I/O error is thrown when accessing the starting file.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire permits on a semaphore.
     */
    public static void deleteTwins() throws IOException, InterruptedException {
        System.out.println("DELETE DIGITAL TWINS");
        Map<String, String> twins = FileHelper.loadAllFilesInPath(TwinsPath);
        final Semaphore deleteTwinsSemaphore = new Semaphore(0);
        final Semaphore deleteRelationshipsSemaphore = new Semaphore(0);

        // Call APIs to clean up any pre-existing resources that might be referenced by this sample. If digital twin does not exist, ignore.
        twins
            .forEach((twinId, twinContent) -> {
                // Call APIs to delete all relationships.
                client.listRelationships(twinId, BasicRelationship.class)
                    .doOnComplete(deleteRelationshipsSemaphore::release)
                    .doOnError(throwable -> {
                        if (throwable instanceof ErrorResponseException && ((ErrorResponseException) throwable).getResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                            deleteRelationshipsSemaphore.release();
                        } else {
                            System.err.println("List relationships error: " + throwable);
                        }
                    })
                    .subscribe(
                        relationship -> client.deleteRelationship(twinId, relationship.getId())
                            .subscribe(
                                aVoid -> System.out.println("Found and deleted relationship: " + relationship.getId()),
                                throwable -> System.err.println("Delete relationship error: " + throwable)
                            ));

                // Call APIs to delete any incoming relationships.
                client.listIncomingRelationships(twinId)
                    .doOnComplete(deleteRelationshipsSemaphore::release)
                    .doOnError(throwable -> {
                        if (throwable instanceof ErrorResponseException && ((ErrorResponseException) throwable).getResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                            deleteRelationshipsSemaphore.release();
                        } else {
                            System.err.println("List incoming relationships error: " + throwable);
                        }
                    })
                    .subscribe(
                        incomingRelationship -> client.deleteRelationship(incomingRelationship.getSourceId(), incomingRelationship.getRelationshipId())
                            .subscribe(
                                aVoid -> System.out.println("Found and deleted incoming relationship: " + incomingRelationship.getRelationshipId()),
                                throwable -> System.err.println("Delete incoming relationship error: " + throwable)
                            ));

                try {
                    // Verify that the list relationships and list incoming relationships async operations have completed.
                    if (deleteRelationshipsSemaphore.tryAcquire(2, MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS)) {
                        // Now the digital twin should be safe to delete

                        // Call APIs to delete the twins.
                        client.deleteDigitalTwin(twinId)
                            .doOnSuccess(aVoid -> {
                                System.out.println("Deleted digital twin: " + twinId);
                                deleteTwinsSemaphore.release();
                            })
                            .doOnError(throwable -> {
                                if (throwable instanceof ErrorResponseException && ((ErrorResponseException) throwable).getResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                                    deleteTwinsSemaphore.release();
                                } else {
                                    System.err.println("Could not delete digital twin " + twinId + " due to " + throwable);
                                }
                            })
                            .subscribe();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Could not cleanup the pre-existing resources: ", e);
                }
            });

        // Verify that a semaphore has been released for each delete async operation, signifying that the async call has completed successfully..
        boolean created = deleteTwinsSemaphore.tryAcquire(twins.size(), MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS);
        System.out.println("Twins deleted: " + created);
    }

    /**
     * Creates all twins specified in the DTDL->DigitalTwins directory.
     * @throws IOException If an I/O error is thrown when accessing the starting file.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire permits on a semaphore.
     */
    public static void createTwins() throws IOException, InterruptedException {
        System.out.println("CREATE DIGITAL TWINS");
        Map<String, String> twins = FileHelper.loadAllFilesInPath(TwinsPath);
        final Semaphore createTwinsSemaphore = new Semaphore(0);

        // Call APIs to create the twins. For each async operation, once the operation is completed successfully, a semaphore is released.
        twins
            .forEach((twinId, twinContent) -> client.createDigitalTwinWithResponse(twinId, twinContent)
                .subscribe(
                    response -> System.out.println("Created digital twin: " + twinId + "\n\t Body: " + response.getValue()),
                    throwable -> System.err.println("Could not create digital twin " + twinId + " due to " + throwable),
                    createTwinsSemaphore::release));

        // Verify that a semaphore has been released for each async operation, signifying that the async call has completed successfully.
        boolean created = createTwinsSemaphore.tryAcquire(twins.size(), MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS);
        System.out.println("Twins created: " + created);
    }
}
