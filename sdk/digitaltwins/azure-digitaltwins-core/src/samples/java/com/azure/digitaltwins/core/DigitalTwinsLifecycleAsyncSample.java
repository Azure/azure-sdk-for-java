// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.implementation.serialization.BasicRelationship;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.azure.digitaltwins.core.SamplesConstants.*;
import static com.azure.digitaltwins.core.SamplesUtil.IgnoreConflictError;
import static com.azure.digitaltwins.core.SamplesUtil.IgnoreNotFoundError;
import static java.util.Arrays.asList;

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
    private static final ObjectMapper mapper = new ObjectMapper();

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

        // Delete existing models
        deleteAllModels();

        // Create all the models
        createAllModels();

        // Get all models
        listAllModels();

        // Create twin counterparts for all the models
        createAllTwins();

        // TODO: Get all twins
        // queryTwins();

        // Create all the relationships
        connectTwinsTogether();

        // TODO: Creating event route
        // createEventRoute();

        // TODO: Get all event routes
        // listEventRoutes();

        // TODO: Deleting event route
        // deleteEventRoute();
    }

    /**
     * Delete a twin, and any relationships it might have.
     * @throws IOException If an I/O error is thrown when accessing the starting file.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void deleteTwins() throws IOException, InterruptedException {
        System.out.println("DELETE DIGITAL TWINS");
        Map<String, String> twins = FileHelper.loadAllFilesInPath(TwinsPath);

        // Call APIs to clean up any pre-existing resources that might be referenced by this sample. If digital twin does not exist, ignore.
        // Once the async API terminates (either successfully, or with an error), the latch count is decremented, or the semaphore is released.
        for (Map.Entry<String, String> twin : twins.entrySet()) {
            String twinId = twin.getKey();

            // This list contains all the relationships that existing between the twins referenced by this sample.
            List<BasicRelationship> relationshipList = Collections.synchronizedList(new ArrayList<>());

            // These semaphores indicate when the relationship list and relationship delete operations have completed.
            // We cannot use a latch here since we do not know the no. of relationships that will be deleted (so we do cannot set the latch initial count).
            Semaphore listRelationshipSemaphore = new Semaphore(0);
            Semaphore deleteRelationshipsSemaphore = new Semaphore(0);

            // This latch is to ensure that we wait for the delete twin operation to complete, before proceeding.
            CountDownLatch deleteTwinsLatch = new CountDownLatch(1);

            // Call APIs to retrieve all relationships.
            client.listRelationships(twinId, BasicRelationship.class)
                .doOnNext(relationshipList::add)
                .doOnError(IgnoreNotFoundError)
                .doOnTerminate(listRelationshipSemaphore::release)
                .subscribe();

            // Call APIs to retrieve all incoming relationships.
            client.listIncomingRelationships(twinId)
                .doOnNext(e -> relationshipList.add(mapper.convertValue(e, BasicRelationship.class)))
                .doOnError(IgnoreNotFoundError)
                .doOnTerminate(listRelationshipSemaphore::release)
                .subscribe();

            // Call APIs to delete all relationships.
            if (listRelationshipSemaphore.tryAcquire(2, MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS)) {
                relationshipList
                    .forEach(relationship -> client.deleteRelationship(relationship.getSourceId(), relationship.getId())
                        .doOnSuccess(aVoid -> {
                            if (twinId.equals(relationship.getSourceId())) {
                                System.out.println("Found and deleted relationship: " + relationship.getId());
                            } else {
                                System.out.println("Found and deleted incoming relationship: " + relationship.getId());
                            }
                        })
                        .doOnError(IgnoreNotFoundError)
                        .doOnTerminate(deleteRelationshipsSemaphore::release)
                        .subscribe());
            }

            // Verify that the relationships have been deleted.
            if (deleteRelationshipsSemaphore.tryAcquire(relationshipList.size(), MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS)) {
                // Now the digital twin should be safe to delete

                // Call APIs to delete the twins.
                client.deleteDigitalTwin(twinId)
                    .doOnSuccess(aVoid -> System.out.println("Deleted digital twin: " + twinId))
                    .doOnError(IgnoreNotFoundError)
                    .doOnTerminate(deleteTwinsLatch::countDown)
                    .subscribe();

                // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
                deleteTwinsLatch.await(MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Delete models created by FullLifecycleSample for the ADT service instance.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void deleteAllModels() throws InterruptedException {
        System.out.println("DELETING MODELS");

        // This is to ensure models are deleted in an order such that no other models are referencing it.
        List<String> models = asList(RoomModelId, WifiModelId, BuildingModelId, FloorModelId, HvacModelId);

        // Call APIs to delete the models.
        // Note that we are blocking the async API call. This is to ensure models are deleted in an order such that no other models are referencing it.
        models
            .forEach(modelId -> {
                try {
                    client.deleteModel(modelId).block();
                    System.out.println("Deleted model: " + modelId);
                } catch (ErrorResponseException ex) {
                    if (ex.getResponse().getStatusCode() != HttpStatus.SC_NOT_FOUND) {
                        System.err.println("Could not delete model " + modelId + " due to " + ex);
                    }
                }
            });
    }

    /**
     * Loads all the models found in the Models directory into memory and uses CreateModelsAsync API to create all the models in the ADT service instance.
     * @throws IOException If an I/O error is thrown when accessing the starting file.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void createAllModels() throws IOException, InterruptedException {
        System.out.println("CREATING MODELS");
        List<String> modelsToCreate = new ArrayList<>(FileHelper.loadAllFilesInPath(ModelsPath).values());
        final CountDownLatch createModelsLatch = new CountDownLatch(1);

        // Call API to create the models. For each async operation, once the operation is completed successfully, a latch is counted down.
        client.createModels(modelsToCreate)
            .doOnNext(modelData -> System.out.println("Created model: " + modelData.getId()))
            .doOnError(IgnoreConflictError)
            .doOnTerminate(createModelsLatch::countDown)
            .subscribe();

        // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
        createModelsLatch.await(MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Gets all the models within the ADT service instance.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void listAllModels() throws InterruptedException {
        System.out.println("LISTING MODELS");
        final CountDownLatch listModelsLatch = new CountDownLatch(1);

        // Call API to list the models. For each async operation, once the operation is completed successfully, a latch is counted down.
        client.listModels()
            .doOnNext(modelData -> System.out.println("Retrieved model: " + modelData.getId() + ", display name '" + modelData.getDisplayName().get("en") + "'," +
                    " upload time '" + modelData.getUploadTime() + "' and decommissioned '" + modelData.isDecommissioned() + "'"))
            .doOnError(throwable -> System.err.println("List models error: " + throwable))
            .doOnTerminate(listModelsLatch::countDown)
            .subscribe();

        // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
        listModelsLatch.await(MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Creates all twins specified in the DTDL->DigitalTwins directory.
     * @throws IOException If an I/O error is thrown when accessing the starting file.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void createAllTwins() throws IOException, InterruptedException {
        System.out.println("CREATE DIGITAL TWINS");
        Map<String, String> twins = FileHelper.loadAllFilesInPath(TwinsPath);
        final CountDownLatch createTwinsLatch = new CountDownLatch(twins.size());

        // Call APIs to create the twins. For each async operation, once the operation is completed successfully, a latch is counted down.
        twins
            .forEach((twinId, twinContent) -> client.createDigitalTwin(twinId, twinContent)
                .subscribe(
                    twin -> System.out.println("Created digital twin: " + twinId + "\n\t Body: " + twin),
                    throwable -> System.err.println("Could not create digital twin " + twinId + " due to " + throwable),
                    createTwinsLatch::countDown));

        // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
        createTwinsLatch.await(MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Creates all the relationships defined in the DTDL->Relationships directory
     * @throws IOException If an I/O error is thrown when accessing the starting file.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void connectTwinsTogether() throws IOException, InterruptedException {
        System.out.println("CONNECT DIGITAL TWINS");
        Map<String, String> allRelationships = FileHelper.loadAllFilesInPath(RelationshipsPath);
        final CountDownLatch connectTwinsLatch = new CountDownLatch(4);

        // For each relationship array we deserialize it first.
        // We deserialize as BasicRelationship to get the entire custom relationship (custom relationship properties).
        allRelationships.values().forEach(
            relationshipContent -> {
                try {
                    List<BasicRelationship> relationships = mapper.readValue(relationshipContent, new TypeReference<List<BasicRelationship>>() { });

                    // From loaded relationships, get the source Id and Id from each one, and create it with full relationship payload.
                    relationships
                        .forEach(relationship -> {
                            try {
                                client.createRelationship(relationship.getSourceId(), relationship.getId(), mapper.writeValueAsString(relationship))
                                    .doOnSuccess(s -> System.out.println("Linked twin " + relationship.getSourceId() + " to twin " + relationship.getTargetId() + " as " + relationship.getName()))
                                    .doOnError(IgnoreConflictError)
                                    .doOnTerminate(connectTwinsLatch::countDown)
                                    .subscribe();
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException("JsonProcessingException while serializing relationship string from BasicRelationship: ", e);
                            }
                        });
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("JsonProcessingException while deserializing relationship string to BasicRelationship: ", e);
                }
            }
        );

        // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
        connectTwinsLatch.await(MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS);
    }
}
