// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.digitaltwins.core.helpers.ConsoleLogger;
import com.azure.digitaltwins.core.helpers.FileHelper;
import com.azure.digitaltwins.core.helpers.SamplesArguments;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.azure.digitaltwins.core.models.QueryChargeHelper.*;
import static com.azure.digitaltwins.core.helpers.SamplesConstants.*;
import static com.azure.digitaltwins.core.helpers.SamplesUtil.IgnoreConflictError;
import static com.azure.digitaltwins.core.helpers.SamplesUtil.IgnoreNotFoundError;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
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

    private static final int MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS = 10;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final URL DTDL_DIRECTORY_URL = DigitalTwinsLifecycleAsyncSample.class.getClassLoader().getResource("DTDL");
    private static final Path DTDL_DIRECTORY_PATH;
    private static final Path TWINS_PATH;
    private static final Path MODELS_PATH;
    private static final Path RELATIONSHIPS_PATH;

    private static DigitalTwinsAsyncClient client;

    static {
        try {
            assert DTDL_DIRECTORY_URL != null;
            DTDL_DIRECTORY_PATH = Paths.get(DTDL_DIRECTORY_URL.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to convert the DTDL directory URL to URI", e);
        }
        TWINS_PATH = Paths.get(DTDL_DIRECTORY_PATH.toString(), "DigitalTwins");
        MODELS_PATH = Paths.get(DTDL_DIRECTORY_PATH.toString(), "Models");
        RELATIONSHIPS_PATH = Paths.get(DTDL_DIRECTORY_PATH.toString(), "Relationships");
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        SamplesArguments parsedArguments = new SamplesArguments(args);

        client = new DigitalTwinsClientBuilder()
            .credential(
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

        // Get all twins
        queryTwins();

        // Create all the relationships
        connectTwinsTogether();
    }

    /**
     * Delete a twin, and any relationships it might have.
     * @throws IOException If an I/O error is thrown when accessing the starting file.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void deleteTwins() throws IOException, InterruptedException {
        ConsoleLogger.printHeader("Delete digital twins");
        Map<String, String> twins = FileHelper.loadAllFilesInPath(TWINS_PATH);

        // Call APIs to clean up any pre-existing resources that might be referenced by this sample. If digital twin does not exist, ignore.
        // Once the async API terminates (either successfully, or with an error), the latch count is decremented, or the semaphore is released.
        for (Map.Entry<String, String> twin : twins.entrySet()) {
            String twinId = twin.getKey();

            // This list contains all the relationships that existing between the twins referenced by this sample.
            List<BasicRelationship> relationshipList = Collections.synchronizedList(new ArrayList<>());

            // This latch is to maintain a thread-safe count to ensure that we wait for both the list relationships and list incoming relationships operations to complete,
            // before proceeding to the delete operation.
            CountDownLatch listRelationshipSemaphore = new CountDownLatch(2);
            // This latch is to maintain a thread-safe count to ensure that we wait for the delete twin operation to complete, before proceeding.
            CountDownLatch deleteTwinsLatch = new CountDownLatch(1);

            // This semaphores indicates when the relationship delete operation has has completed.
            // We cannot use a latch here since we do not know the no. of relationships that will be deleted (so we do cannot set the latch initial count).
            Semaphore deleteRelationshipsSemaphore = new Semaphore(0);

            // Call APIs to retrieve all relationships.
            client.listRelationships(twinId, BasicRelationship.class)
                .doOnNext(relationshipList::add)
                .doOnError(IgnoreNotFoundError)
                .doOnTerminate(listRelationshipSemaphore::countDown)
                .subscribe();

            // Call APIs to retrieve all incoming relationships.
            client.listIncomingRelationships(twinId, null)
                .doOnNext(e -> relationshipList.add(MAPPER.convertValue(e, BasicRelationship.class)))
                .doOnError(IgnoreNotFoundError)
                .doOnTerminate(listRelationshipSemaphore::countDown)
                .subscribe();

            // Call APIs to delete all relationships.
            if (listRelationshipSemaphore.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS)) {
                relationshipList
                    .forEach(relationship -> client.deleteRelationship(relationship.getSourceId(), relationship.getId())
                        .doOnSuccess(aVoid -> {
                            if (twinId.equals(relationship.getSourceId())) {
                                ConsoleLogger.printSuccess("Found and deleted relationship: " + relationship.getId());
                            } else {
                                ConsoleLogger.printSuccess("Found and deleted incoming relationship: " + relationship.getId());
                            }
                        })
                        .doOnError(IgnoreNotFoundError)
                        .doOnTerminate(deleteRelationshipsSemaphore::release)
                        .subscribe());
            }

            // Verify that the relationships have been deleted.
            if (deleteRelationshipsSemaphore.tryAcquire(relationshipList.size(), MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS)) {
                // Now the digital twin should be safe to delete

                // Call APIs to delete the twins.
                client.deleteDigitalTwin(twinId)
                    .doOnSuccess(aVoid -> ConsoleLogger.printSuccess("Deleted digital twin: " + twinId))
                    .doOnError(IgnoreNotFoundError)
                    .doOnTerminate(deleteTwinsLatch::countDown)
                    .subscribe();

                // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
                deleteTwinsLatch.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Delete models created by FullLifecycleSample for the ADT service instance.
     */
    public static void deleteAllModels() {

        ConsoleLogger.printHeader("Deleting models");

        // This is to ensure models are deleted in an order such that no other models are referencing it.
        List<String> models = asList(ROOM_MODEL_ID, WIFI_MODEL_ID, BUILDING_MODEL_ID, FLOOR_MODEL_ID, HVAC_MODEL_ID);

        // Call APIs to delete the models.
        // Note that we are blocking the async API call. This is to ensure models are deleted in an order such that no other models are referencing it.
        models
            .forEach(modelId -> {
                try {
                    client.deleteModel(modelId).block();
                    ConsoleLogger.printSuccess("Deleted model: " + modelId);
                } catch (ErrorResponseException ex) {
                    if (ex.getResponse().getStatusCode() != HTTP_NOT_FOUND) {
                        ConsoleLogger.printFatal("Could not delete model " + modelId + " due to " + ex);
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
        ConsoleLogger.printHeader("Creating models");
        List<String> modelsToCreate = new ArrayList<>(FileHelper.loadAllFilesInPath(MODELS_PATH).values());
        final CountDownLatch createModelsLatch = new CountDownLatch(1);

        // Call API to create the models. For each async operation, once the operation is completed successfully, a latch is counted down.
        client.createModels(modelsToCreate)
            .doOnNext(listOfModelData -> listOfModelData.forEach(
                modelData -> ConsoleLogger.printSuccess("Created model: " + modelData.getModelId())
            ))
            .doOnError(IgnoreConflictError)
            .doOnTerminate(createModelsLatch::countDown)
            .subscribe();

        // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
        createModelsLatch.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Gets all the models within the ADT service instance.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void listAllModels() throws InterruptedException {
        ConsoleLogger.printHeader("Listing models");
        final CountDownLatch listModelsLatch = new CountDownLatch(1);

        // Call API to list the models. For each async operation, once the operation is completed successfully, a latch is counted down.
        client.listModels()
            .doOnNext(modelData -> ConsoleLogger.printSuccess("Retrieved model: " + modelData.getModelId() + ", display name '" + modelData.getDisplayNameLanguageMap().get("en") + "'," +
                    " upload time '" + modelData.getUploadedOn() + "' and decommissioned '" + modelData.isDecommissioned() + "'"))
            .doOnError(throwable -> ConsoleLogger.printFatal("List models error: " + throwable))
            .doOnTerminate(listModelsLatch::countDown)
            .subscribe();

        // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
        listModelsLatch.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Creates all twins specified in the DTDL->DigitalTwins directory.
     * @throws IOException If an I/O error is thrown when accessing the starting file.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void createAllTwins() throws IOException, InterruptedException {
        ConsoleLogger.printHeader("Create digital twins");
        Map<String, String> twins = FileHelper.loadAllFilesInPath(TWINS_PATH);
        final CountDownLatch createTwinsLatch = new CountDownLatch(twins.size());

        // Call APIs to create the twins. For each async operation, once the operation is completed successfully, a latch is counted down.
        twins
            .forEach((twinId, twinContent) -> client.createOrReplaceDigitalTwin(twinId, twinContent, String.class)
                .subscribe(
                    twin -> ConsoleLogger.printSuccess("Created digital twin: " + twinId + "\n\t Body: " + twin),
                    throwable -> ConsoleLogger.printFatal("Could not create digital twin " + twinId + " due to " + throwable),
                    createTwinsLatch::countDown));

        // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
        createTwinsLatch.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Creates all the relationships defined in the DTDL->Relationships directory
     * @throws IOException If an I/O error is thrown when accessing the starting file.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void connectTwinsTogether() throws IOException, InterruptedException {
        ConsoleLogger.printHeader("Connect digital twins");
        Map<String, String> allRelationships = FileHelper.loadAllFilesInPath(RELATIONSHIPS_PATH);
        final CountDownLatch connectTwinsLatch = new CountDownLatch(4);

        // For each relationship array we deserialize it first.
        // We deserialize as BasicRelationship to get the entire custom relationship (custom relationship properties).
        allRelationships.values().forEach(
            relationshipContent -> {
                try {
                    List<BasicRelationship> relationships = MAPPER.readValue(relationshipContent, new TypeReference<List<BasicRelationship>>() { });

                    // From loaded relationships, get the source Id and Id from each one, and create it with full relationship payload.
                    relationships
                        .forEach(relationship -> {
                            try {
                                client.createOrReplaceRelationship(relationship.getSourceId(), relationship.getId(), MAPPER.writeValueAsString(relationship), String.class)
                                    .doOnSuccess(s -> ConsoleLogger.printSuccess("Linked twin " + relationship.getSourceId() + " to twin " + relationship.getTargetId() + " as " + relationship.getName()))
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
        connectTwinsLatch.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     *
     * Queries for all the digital twins.
     * @throws InterruptedException If the current thread is interrupted while waiting to acquire latch.
     */
    public static void queryTwins() throws InterruptedException {
        ConsoleLogger.printHeader("QUERY DIGITAL TWINS");

        final CountDownLatch queryLatch = new CountDownLatch(1);

        ConsoleLogger.printHeader("Making a twin query and iterating over the results.");

        // Call API to query digital twins. For each async operation, once the operation is completed successfully, a latch is counted down.
        client.query("SELECT * FROM digitaltwins", String.class, null)
            .doOnNext(queryResult -> ConsoleLogger.printHeader("Query result: " + queryResult))
            .doOnError(throwable -> ConsoleLogger.printFatal("Query error: " + throwable))
            .doOnTerminate(queryLatch::countDown)
            .subscribe();

        // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
        queryLatch.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);

        ConsoleLogger.printHeader("Making a twin query, with query-charge header extraction.");

        final CountDownLatch queryWithChargeLatch = new CountDownLatch(1);

        // Call API to query digital twins. For each async operation, once the operation is completed successfully, a latch is counted down.
        client.query("SELECT * FROM digitaltwins", BasicDigitalTwin.class, null)
            .byPage()
            .doOnNext(page ->
            {
                ConsoleLogger.printHeader("Query charge: " + getQueryCharge(page));
                page.getValue()
                    .forEach(item -> ConsoleLogger.printHeader("Found digital twin: " + item.getId()));
            })
            .doOnError(throwable -> ConsoleLogger.printFatal("Query error: " + throwable))
            .doOnTerminate(queryLatch::countDown)
            .subscribe();

        // Wait until the latch count reaches zero, signifying that the async calls have completed successfully.
        queryWithChargeLatch.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
    }
}
