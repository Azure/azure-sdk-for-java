// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.models.JsonPatchDocument;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.models.CreateOrReplaceRelationshipOptions;
import com.azure.digitaltwins.core.models.DeleteRelationshipOptions;
import com.azure.digitaltwins.core.models.IncomingRelationship;
import com.azure.digitaltwins.core.models.UpdateRelationshipOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static com.azure.digitaltwins.core.helpers.UniqueIdHelper.getUniqueDigitalTwinId;
import static com.azure.digitaltwins.core.helpers.UniqueIdHelper.getUniqueModelId;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_PRECON_FAILED;
import static javax.net.ssl.HttpsURLConnection.HTTP_NO_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DigitalTwinsRelationshipAsyncTest extends DigitalTwinsRelationshipTestBase {
    private final ClientLogger logger = new ClientLogger(DigitalTwinsRelationshipAsyncTest.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void relationshipLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion)
        throws IOException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(asyncClient, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId,
                hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId,
                CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            String floorTwinCoolsRelationshipPayload = TestAssetsHelper.getRelationshipPayload(floorTwinId,
                COOLS_RELATIONSHIP);
            String floorTwinContainedInRelationshipPayload = TestAssetsHelper.getRelationshipPayload(floorTwinId,
                CONTAINED_IN_RELATIONSHIP);
            String floorCooledByHvacPayload = TestAssetsHelper.getRelationshipPayload(hvacTwinId,
                COOLED_BY_RELATIONSHIP);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload(
                "/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            StepVerifier.create(
                    asyncClient.createOrReplaceRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                        deserializeJsonString(floorContainsRoomPayload, BasicRelationship::fromJson),
                        BasicRelationship.class))
                .assertNext(basicRelationship -> {
                    assertEquals(FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, basicRelationship.getId());
                    logger.info("Created {} relationship between source = {} and target = {}",
                        basicRelationship.getId(), basicRelationship.getSourceId(), basicRelationship.getTargetId());
                })
                .verifyComplete();

            // Create relationship from Floor -> Hvac
            StepVerifier.create(
                    asyncClient.createOrReplaceRelationship(floorTwinId, FLOOR_COOLED_BY_HVAC_RELATIONSHIP_ID,
                        deserializeJsonString(floorCooledByHvacPayload, BasicRelationship::fromJson),
                        BasicRelationship.class))
                .assertNext(basicRelationship -> {
                    assertEquals(FLOOR_COOLED_BY_HVAC_RELATIONSHIP_ID, basicRelationship.getId());
                    logger.info("Created {} relationship between source = {} and target = {}",
                        basicRelationship.getId(), basicRelationship.getSourceId(), basicRelationship.getTargetId());
                })
                .verifyComplete();

            // Create relationship from Hvac -> Floor
            StepVerifier.create(asyncClient.createOrReplaceRelationship(hvacTwinId, HVAC_COOLS_FLOOR_RELATIONSHIP_ID,
                deserializeJsonString(floorTwinCoolsRelationshipPayload, BasicRelationship::fromJson),
                BasicRelationship.class))
                .assertNext(basicRelationship -> {
                    assertEquals(HVAC_COOLS_FLOOR_RELATIONSHIP_ID, basicRelationship.getId());
                    logger.info("Created {} relationship between source = {} and target = {}",
                        basicRelationship.getId(), basicRelationship.getSourceId(), basicRelationship.getTargetId());
                })
                .verifyComplete();

            // Create relationship from Room -> Floor
            StepVerifier.create(
                asyncClient.createOrReplaceRelationship(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID,
                    deserializeJsonString(floorTwinContainedInRelationshipPayload, BasicRelationship::fromJson),
                    BasicRelationship.class))
                .assertNext(basicRelationship -> {
                    assertEquals(ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID, basicRelationship.getId());
                    logger.info("Created {} relationship between source = {} and target = {}",
                        basicRelationship.getId(), basicRelationship.getSourceId(), basicRelationship.getTargetId());
                })
                .verifyComplete();

            // Create a relation which already exists - should return status code 409 (Conflict).
            StepVerifier.create(
                asyncClient.createOrReplaceRelationshipWithResponse(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID,
                    floorTwinContainedInRelationshipPayload, String.class,
                    new CreateOrReplaceRelationshipOptions().setIfNoneMatch("*")))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HTTP_PRECON_FAILED));

            // Update relationships

            // Create relationship from Floor -> Room
            StepVerifier.create(
                asyncClient.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomUpdatePayload, null))
                .assertNext(voidDigitalTwinsResponse -> {
                    assertEquals(HTTP_NO_CONTENT, voidDigitalTwinsResponse.getStatusCode());
                    logger.info("Updated {} relationship successfully in source {}",
                        FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
                })
                .verifyComplete();

            // GET relationship
            StepVerifier.create(
                asyncClient.getRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, BasicRelationship.class))
                .assertNext(basicRelationship -> {
                    assertEquals(FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, basicRelationship.getId());
                    logger.info("Retrieved {} relationship under source {}", basicRelationship.getId(),
                        basicRelationship.getSourceId());
                })
                .verifyComplete();

            // LIST incoming relationships
            List<String> incomingRelationshipsSourceIds = new ArrayList<>();
            StepVerifier.create(asyncClient.listIncomingRelationships(floorTwinId))
                .assertNext(
                    incomingRelationship -> incomingRelationshipsSourceIds.add(incomingRelationship.getSourceId()))
                .assertNext(
                    incomingRelationship -> incomingRelationshipsSourceIds.add(incomingRelationship.getSourceId()))
                .expectComplete()
                .verify();
            assertEquals(2, incomingRelationshipsSourceIds.size());
            assertTrue(incomingRelationshipsSourceIds.contains(roomTwinId));
            assertTrue(incomingRelationshipsSourceIds.contains(hvacTwinId));
            logger.info("Retrieved incoming relationships for {}, found sources {}", floorTwinId,
                Arrays.toString(incomingRelationshipsSourceIds.toArray()));

            // LIST relationships
            List<String> relationshipsTargetIds = new ArrayList<>();
            StepVerifier.create(asyncClient.listRelationships(floorTwinId, BasicRelationship.class))
                .assertNext(basicRelationship -> relationshipsTargetIds.add(basicRelationship.getTargetId()))
                .assertNext(basicRelationship -> relationshipsTargetIds.add(basicRelationship.getTargetId()))
                .expectComplete()
                .verify();
            assertEquals(2, relationshipsTargetIds.size());
            assertTrue(relationshipsTargetIds.contains(roomTwinId));
            assertTrue(relationshipsTargetIds.contains(hvacTwinId));
            logger.info("Retrieved all relationships for {}, found targets {}", floorTwinId,
                Arrays.toString(relationshipsTargetIds.toArray()));

            // LIST relationship by name
            StepVerifier.create(
                    asyncClient.listRelationships(roomTwinId, CONTAINED_IN_RELATIONSHIP, BasicRelationship.class))
                .assertNext(basicRelationship -> {
                    assertEquals(CONTAINED_IN_RELATIONSHIP, basicRelationship.getName());
                    assertEquals(floorTwinId, basicRelationship.getTargetId());
                    logger.info("Retrieved relationship {} for twin {}", basicRelationship.getId(), roomTwinId);
                })
                .expectComplete()
                .verify();

            // DELETE the created relationships
            StepVerifier.create(asyncClient.deleteRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID))
                .verifyComplete();
            logger.info("Deleted relationship {} for twin {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);

            StepVerifier.create(asyncClient.deleteRelationship(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID))
                .verifyComplete();
            logger.info("Deleted relationship {} for twin {}", ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID, roomTwinId);

            StepVerifier.create(asyncClient.deleteRelationship(floorTwinId, FLOOR_COOLED_BY_HVAC_RELATIONSHIP_ID))
                .verifyComplete();
            logger.info("Deleted relationship {} for twin {}", FLOOR_COOLED_BY_HVAC_RELATIONSHIP_ID, floorTwinId);

            StepVerifier.create(asyncClient.deleteRelationship(hvacTwinId, HVAC_COOLS_FLOOR_RELATIONSHIP_ID))
                .verifyComplete();
            logger.info("Deleted relationship {} for twin {}", HVAC_COOLS_FLOOR_RELATIONSHIP_ID, hvacTwinId);

            // GET a relationship which doesn't exist - should return status code 404 (Not Found).
            StepVerifier.create(
                    asyncClient.getRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, String.class))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HTTP_NOT_FOUND));

        } finally {
            // Clean up
            logger.info("Cleaning up test resources.");

            logger.info("Deleting created relationships.");
            // Delete the created relationships.
            List<BasicRelationship> relationships = new ArrayList<>();
            asyncClient.listRelationships(floorTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(roomTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(hvacTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            relationships.forEach(
                basicRelationship -> asyncClient.deleteRelationship(basicRelationship.getSourceId(),
                    basicRelationship.getId()).block());

            // Now the twins and models can be deleted.
            logger.info("Deleting created digital twins.");
            asyncClient.deleteDigitalTwin(floorTwinId).block();
            asyncClient.deleteDigitalTwin(roomTwinId).block();
            asyncClient.deleteDigitalTwin(hvacTwinId).block();

            logger.info("Deleting created models.");
            asyncClient.deleteModel(floorModelId).block();
            asyncClient.deleteModel(roomModelId).block();
            asyncClient.deleteModel(hvacModelId).block();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void relationshipListOperationWithMultiplePages(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        List<String> createdOutgoingRelationshipIds = new ArrayList<>();
        List<String> createdIncomingRelationshipIds = new ArrayList<>();

        try {
            createModelsAndTwins(asyncClient, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId,
                hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId,
                CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            String roomContainedInFloorPayload = TestAssetsHelper.getRelationshipPayload(floorTwinId,
                CONTAINED_IN_RELATIONSHIP);

            // Create large number of relationships to test paging functionality
            // Relationship list api does not have max item count request option, so we have to create a large number of
            // them to trigger paging functionality from the service.
            // Create relationships from Floor -> Room
            for (int i = 0; i < BULK_RELATIONSHIP_COUNT; i++) {
                String relationshipId = FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID + this.testResourceNamer.randomUuid();
                StepVerifier.create(asyncClient.createOrReplaceRelationship(floorTwinId, relationshipId,
                        deserializeJsonString(floorContainsRoomPayload, BasicRelationship::fromJson),
                        BasicRelationship.class))
                    .assertNext(response -> logger.info("Created relationship with Id {}", relationshipId))
                    .verifyComplete();
                createdOutgoingRelationshipIds.add(relationshipId);
            }

            sleepIfRunningAgainstService(5000);

            // Create multiple incoming relationships to the floor. Typically a room would have relationships to multiple
            // different floors, but for the sake of test simplicity, we'll just add multiple relationships from the same room
            // to the same floor.
            for (int i = 0; i < BULK_RELATIONSHIP_COUNT; i++) {
                String relationshipId = ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID + this.testResourceNamer.randomUuid();
                StepVerifier.create(asyncClient.createOrReplaceRelationship(roomTwinId, relationshipId,
                        deserializeJsonString(roomContainedInFloorPayload, BasicRelationship::fromJson),
                        BasicRelationship.class))
                    .assertNext(response -> logger.info("Created relationship with Id {}", relationshipId))
                    .verifyComplete();
                createdIncomingRelationshipIds.add(relationshipId);
            }

            sleepIfRunningAgainstService(5000);

            AtomicInteger outgoingRelationshipsPageCount = new AtomicInteger();
            // List relationships in multiple pages and verify more than one page was retrieved.
            StepVerifier.create(asyncClient.listRelationships(floorTwinId, BasicRelationship.class).byPage())
                .thenConsumeWhile(page -> {
                    outgoingRelationshipsPageCount.getAndIncrement();
                    logger.info("content for this page " + outgoingRelationshipsPageCount);
                    for (BasicRelationship relationship : page.getValue()) {
                        logger.info(relationship.getId());
                    }

                    if (page.getContinuationToken() != null) {
                        assertEquals(RELATIONSHIP_PAGE_SIZE_DEFAULT, page.getValue().size(),
                            "Unexpected page size for a non-terminal page");
                    }

                    return true;
                })
                .verifyComplete();

            assertTrue(outgoingRelationshipsPageCount.get() > 1,
                "Expected more than one page of outgoing relationships");

            AtomicInteger incomingRelationshipsPageCount = new AtomicInteger();
            // List relationships in multiple pages and verify more than one page was retrieved.
            StepVerifier.create(asyncClient.listIncomingRelationships(floorTwinId).byPage()).thenConsumeWhile(page -> {
                incomingRelationshipsPageCount.getAndIncrement();
                logger.info("content for this page " + incomingRelationshipsPageCount);
                for (IncomingRelationship relationship : page.getValue()) {
                    logger.info(relationship.getSourceId());
                }

                if (page.getContinuationToken() != null) {
                    assertEquals(RELATIONSHIP_PAGE_SIZE_DEFAULT, page.getValue().size(),
                        "Unexpected page size for a non-terminal page");
                }

                return true;
            }).verifyComplete();

            assertTrue(incomingRelationshipsPageCount.get() > 1,
                "Expected more than one page of incoming relationships");
        } finally {
            // Clean up
            logger.info("Cleaning up test resources.");

            logger.info("Deleting created relationships.");
            // Delete the created relationships.
            createdOutgoingRelationshipIds.forEach(
                relationshipId -> asyncClient.deleteRelationship(floorTwinId, relationshipId).block());
            createdIncomingRelationshipIds.forEach(
                relationshipId -> asyncClient.deleteRelationship(roomTwinId, relationshipId).block());

            // Now the twins and models can be deleted.
            logger.info("Deleting created digital twins.");
            asyncClient.deleteDigitalTwin(floorTwinId).block();
            asyncClient.deleteDigitalTwin(roomTwinId).block();
            asyncClient.deleteDigitalTwin(hvacTwinId).block();

            logger.info("Deleting created models.");
            asyncClient.deleteModel(floorModelId).block();
            asyncClient.deleteModel(roomModelId).block();
            asyncClient.deleteModel(hvacModelId).block();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createOrReplaceRelationshipFailsWhenIfNoneMatchStar(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(asyncClient, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId,
                hvacTwinId);

            // Connect the created twins via relationships
            String floorTwinContainedInRelationshipPayload = TestAssetsHelper.getRelationshipPayload(floorTwinId,
                CONTAINED_IN_RELATIONSHIP);

            // Create relationship from Room -> Floor
            StepVerifier.create(
                asyncClient.createOrReplaceRelationship(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID,
                    deserializeJsonString(floorTwinContainedInRelationshipPayload, BasicRelationship::fromJson),
                    BasicRelationship.class))
                .assertNext(basicRelationship -> logger.info(
                    "Created {} relationship between source = {} and target = {}", basicRelationship.getId(),
                    basicRelationship.getSourceId(), basicRelationship.getTargetId()))
                .verifyComplete();

            StepVerifier.create(
                asyncClient.createOrReplaceRelationshipWithResponse(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID,
                    floorTwinContainedInRelationshipPayload, String.class,
                    new CreateOrReplaceRelationshipOptions().setIfNoneMatch("*")))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HTTP_PRECON_FAILED));

            StepVerifier.create(asyncClient.deleteRelationship(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID))
                .verifyComplete();
            logger.info("Deleted relationship {} for twin {}", ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID, roomTwinId);

        } finally {
            // Clean up
            logger.info("Cleaning up test resources.");

            logger.info("Deleting created relationships.");
            // Delete the created relationships.
            List<BasicRelationship> relationships = new ArrayList<>();
            asyncClient.listRelationships(floorTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(roomTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(hvacTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            relationships.forEach(
                basicRelationship -> asyncClient.deleteRelationship(basicRelationship.getSourceId(),
                    basicRelationship.getId()).block());

            // Now the twins and models can be deleted.
            logger.info("Deleting created digital twins.");
            asyncClient.deleteDigitalTwin(floorTwinId).block();
            asyncClient.deleteDigitalTwin(roomTwinId).block();
            asyncClient.deleteDigitalTwin(hvacTwinId).block();

            logger.info("Deleting created models.");
            asyncClient.deleteModel(floorModelId).block();
            asyncClient.deleteModel(roomModelId).block();
            asyncClient.deleteModel(hvacModelId).block();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createOrReplaceRelationshipSucceedsWhenNoIfNoneHeader(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(asyncClient, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId,
                hvacTwinId);

            // Connect the created twins via relationships
            String floorTwinContainedInRelationshipPayload = TestAssetsHelper.getRelationshipPayload(floorTwinId,
                CONTAINED_IN_RELATIONSHIP);

            // Create relationship from Room -> Floor
            StepVerifier.create(
                asyncClient.createOrReplaceRelationship(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID,
                    deserializeJsonString(floorTwinContainedInRelationshipPayload, BasicRelationship::fromJson),
                    BasicRelationship.class))
                .assertNext(basicRelationship -> logger.info(
                    "Created {} relationship between source = {} and target = {}", basicRelationship.getId(),
                    basicRelationship.getSourceId(), basicRelationship.getTargetId()))
                .verifyComplete();

            StepVerifier.create(
                asyncClient.createOrReplaceRelationshipWithResponse(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID,
                    floorTwinContainedInRelationshipPayload, String.class, null)) // don't set ifMatchNone header
                .expectNextCount(1) /* don't care as long as it is a success status code */
                .verifyComplete();

            StepVerifier.create(asyncClient.deleteRelationship(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID))
                .verifyComplete();
            logger.info("Deleted relationship {} for twin {}", ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID, roomTwinId);

        } finally {
            // Clean up
            logger.info("Cleaning up test resources.");

            logger.info("Deleting created relationships.");
            // Delete the created relationships.
            List<BasicRelationship> relationships = new ArrayList<>();
            asyncClient.listRelationships(floorTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(roomTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(hvacTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            relationships.forEach(
                basicRelationship -> asyncClient.deleteRelationship(basicRelationship.getSourceId(),
                    basicRelationship.getId()).block());

            // Now the twins and models can be deleted.
            logger.info("Deleting created digital twins.");
            asyncClient.deleteDigitalTwin(floorTwinId).block();
            asyncClient.deleteDigitalTwin(roomTwinId).block();
            asyncClient.deleteDigitalTwin(hvacTwinId).block();

            logger.info("Deleting created models.");
            asyncClient.deleteModel(floorModelId).block();
            asyncClient.deleteModel(roomModelId).block();
            asyncClient.deleteModel(hvacModelId).block();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchRelationshipFailsWhenETagDoesNotMatch(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(asyncClient, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId,
                hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId,
                CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload(
                "/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            AtomicReference<String> etagBeforeUpdate = new AtomicReference<>();
            StepVerifier.create(
                asyncClient.createOrReplaceRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    deserializeJsonString(floorContainsRoomPayload, BasicRelationship::fromJson),
                    BasicRelationship.class, null))
                .assertNext(basicRelationshipResponse -> {
                    etagBeforeUpdate.set(basicRelationshipResponse.getDeserializedHeaders().getETag());
                    logger.info("Created {} relationship between source = {} and target = {}",
                        basicRelationshipResponse.getValue().getId(),
                        basicRelationshipResponse.getValue().getSourceId(),
                        basicRelationshipResponse.getValue().getTargetId());
                })
                .verifyComplete();

            // Update relationship to make etag fall out of date
            StepVerifier.create(
                asyncClient.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomUpdatePayload, null))
                .assertNext(voidDigitalTwinsResponse -> logger.info("Updated {} relationship successfully in source {}",
                    FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId))
                .verifyComplete();

            JsonPatchDocument floorContainsRoomSecondUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload(
                "/isAccessRestricted", true);

            // Try to update the relationship with the now out of date etag, expect it to throw a 412
            StepVerifier.create(
                asyncClient.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomSecondUpdatePayload,
                    new UpdateRelationshipOptions().setIfMatch(etagBeforeUpdate.get())))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_PRECON_FAILED));

            // DELETE the created relationships
            StepVerifier.create(asyncClient.deleteRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID))
                .verifyComplete();
            logger.info("Deleted relationship {} for twin {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
        } finally {
            // Clean up
            logger.info("Cleaning up test resources.");

            logger.info("Deleting created relationships.");
            // Delete the created relationships.
            List<BasicRelationship> relationships = new ArrayList<>();
            asyncClient.listRelationships(floorTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(roomTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(hvacTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            relationships.forEach(
                basicRelationship -> asyncClient.deleteRelationship(basicRelationship.getSourceId(),
                    basicRelationship.getId()).block());

            // Now the twins and models can be deleted.
            logger.info("Deleting created digital twins.");
            asyncClient.deleteDigitalTwin(floorTwinId).block();
            asyncClient.deleteDigitalTwin(roomTwinId).block();
            asyncClient.deleteDigitalTwin(hvacTwinId).block();

            logger.info("Deleting created models.");
            asyncClient.deleteModel(floorModelId).block();
            asyncClient.deleteModel(roomModelId).block();
            asyncClient.deleteModel(hvacModelId).block();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchRelationshipSucceedsWhenETagMatches(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(asyncClient, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId,
                hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId,
                CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload(
                "/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            StepVerifier.create(
                asyncClient.createOrReplaceRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    deserializeJsonString(floorContainsRoomPayload, BasicRelationship::fromJson),
                    BasicRelationship.class, null))
                .assertNext(basicRelationshipResponse -> logger.info(
                    "Created {} relationship between source = {} and target = {}",
                    basicRelationshipResponse.getValue().getId(), basicRelationshipResponse.getValue().getSourceId(),
                    basicRelationshipResponse.getValue().getTargetId()))
                .verifyComplete();

            AtomicReference<String> upToDateETag = new AtomicReference<>();
            StepVerifier.create(
                asyncClient.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomUpdatePayload, null))
                .assertNext(voidDigitalTwinsResponse -> {
                    upToDateETag.set(voidDigitalTwinsResponse.getDeserializedHeaders().getETag());
                    logger.info("Updated {} relationship successfully in source {}",
                        FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
                })
                .verifyComplete();

            JsonPatchDocument floorContainsRoomSecondUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload(
                "/isAccessRestricted", true);

            // Try to update the relationship with an up to date ETag
            StepVerifier.create(
                asyncClient.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomSecondUpdatePayload,
                    new UpdateRelationshipOptions().setIfMatch(upToDateETag.get())))
                .expectNextCount(1) /* don't care as long as it is a success status code */
                .verifyComplete();

            // DELETE the created relationships
            StepVerifier.create(asyncClient.deleteRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID))
                .verifyComplete();
            logger.info("Deleted relationship {} for twin {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
        } finally {
            // Clean up
            logger.info("Cleaning up test resources.");

            logger.info("Deleting created relationships.");
            // Delete the created relationships.
            List<BasicRelationship> relationships = new ArrayList<>();
            asyncClient.listRelationships(floorTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(roomTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(hvacTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            relationships.forEach(
                basicRelationship -> asyncClient.deleteRelationship(basicRelationship.getSourceId(),
                    basicRelationship.getId()).block());

            // Now the twins and models can be deleted.
            logger.info("Deleting created digital twins.");
            asyncClient.deleteDigitalTwin(floorTwinId).block();
            asyncClient.deleteDigitalTwin(roomTwinId).block();
            asyncClient.deleteDigitalTwin(hvacTwinId).block();

            logger.info("Deleting created models.");
            asyncClient.deleteModel(floorModelId).block();
            asyncClient.deleteModel(roomModelId).block();
            asyncClient.deleteModel(hvacModelId).block();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void deleteRelationshipFailsWhenETagDoesNotMatch(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(asyncClient, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId,
                hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId,
                CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload(
                "/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            AtomicReference<String> etagBeforeUpdate = new AtomicReference<>();
            StepVerifier.create(
                asyncClient.createOrReplaceRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    deserializeJsonString(floorContainsRoomPayload, BasicRelationship::fromJson),
                    BasicRelationship.class, null))
                .assertNext(basicRelationshipResponse -> {
                    etagBeforeUpdate.set(basicRelationshipResponse.getDeserializedHeaders().getETag());
                    logger.info("Created {} relationship between source = {} and target = {}",
                        basicRelationshipResponse.getValue().getId(),
                        basicRelationshipResponse.getValue().getSourceId(),
                        basicRelationshipResponse.getValue().getTargetId());
                })
                .verifyComplete();

            // Update relationship to make etag fall out of date
            StepVerifier.create(
                asyncClient.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomUpdatePayload, null))
                .assertNext(voidDigitalTwinsResponse -> logger.info("Updated {} relationship successfully in source {}",
                    FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId))
                .verifyComplete();

            // Try to delete the relationship with the now out of date etag, expect it to throw a 412
            StepVerifier.create(
                asyncClient.deleteRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    new DeleteRelationshipOptions().setIfMatch(etagBeforeUpdate.get())))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_PRECON_FAILED));

            logger.info("Deleted relationship {} for twin {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
        } finally {
            // Clean up
            logger.info("Cleaning up test resources.");

            logger.info("Deleting created relationships.");
            // Delete the created relationships.
            List<BasicRelationship> relationships = new ArrayList<>();
            asyncClient.listRelationships(floorTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(roomTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(hvacTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            relationships.forEach(
                basicRelationship -> asyncClient.deleteRelationship(basicRelationship.getSourceId(),
                    basicRelationship.getId()).block());

            // Now the twins and models can be deleted.
            logger.info("Deleting created digital twins.");
            asyncClient.deleteDigitalTwin(floorTwinId).block();
            asyncClient.deleteDigitalTwin(roomTwinId).block();
            asyncClient.deleteDigitalTwin(hvacTwinId).block();

            logger.info("Deleting created models.");
            asyncClient.deleteModel(floorModelId).block();
            asyncClient.deleteModel(roomModelId).block();
            asyncClient.deleteModel(hvacModelId).block();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void deleteRelationshipSucceedsWhenETagMatches(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, asyncClient,
            getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(asyncClient, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId,
                hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId,
                CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload(
                "/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            StepVerifier.create(
                asyncClient.createOrReplaceRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    deserializeJsonString(floorContainsRoomPayload, BasicRelationship::fromJson),
                    BasicRelationship.class, null))
                .assertNext(basicRelationshipResponse -> logger.info(
                    "Created {} relationship between source = {} and target = {}",
                    basicRelationshipResponse.getValue().getId(), basicRelationshipResponse.getValue().getSourceId(),
                    basicRelationshipResponse.getValue().getTargetId()))
                .verifyComplete();

            AtomicReference<String> upToDateETag = new AtomicReference<>();
            StepVerifier.create(
                asyncClient.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomUpdatePayload, null))
                .assertNext(voidDigitalTwinsResponse -> {
                    upToDateETag.set(voidDigitalTwinsResponse.getDeserializedHeaders().getETag());
                    logger.info("Updated {} relationship successfully in source {}",
                        FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
                })
                .verifyComplete();

            // Try to delete the relationship with an up to date ETag
            StepVerifier.create(
                asyncClient.deleteRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    new DeleteRelationshipOptions().setIfMatch(upToDateETag.get())))
                .expectNextCount(1) /* don't care as long as it is a success status code */
                .verifyComplete();

            logger.info("Deleted relationship {} for twin {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
        } finally {
            // Clean up
            logger.info("Cleaning up test resources.");

            logger.info("Deleting created relationships.");
            // Delete the created relationships.
            List<BasicRelationship> relationships = new ArrayList<>();
            asyncClient.listRelationships(floorTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(roomTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            asyncClient.listRelationships(hvacTwinId, BasicRelationship.class)
                .doOnNext(relationships::add)
                .blockLast();
            relationships.forEach(
                basicRelationship -> asyncClient.deleteRelationship(basicRelationship.getSourceId(),
                    basicRelationship.getId()).block());

            // Now the twins and models can be deleted.
            logger.info("Deleting created digital twins.");
            asyncClient.deleteDigitalTwin(floorTwinId).block();
            asyncClient.deleteDigitalTwin(roomTwinId).block();
            asyncClient.deleteDigitalTwin(hvacTwinId).block();

            logger.info("Deleting created models.");
            asyncClient.deleteModel(floorModelId).block();
            asyncClient.deleteModel(roomModelId).block();
            asyncClient.deleteModel(hvacModelId).block();
        }
    }

    private void createModelsAndTwins(DigitalTwinsAsyncClient asyncClient, String floorModelId, String roomModelId,
        String hvacModelId, String floorTwinId, String roomTwinId, String hvacTwinId) throws IOException {
        // Create floor, room and hvac model
        createModelsRunner(floorModelId, roomModelId, hvacModelId,
            modelsList -> StepVerifier.create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete());

        // Create floor twin
        createFloorTwinRunner(floorTwinId, floorModelId, (twinId, twin) -> StepVerifier.create(
                asyncClient.createOrReplaceDigitalTwin(twinId, twin, BasicDigitalTwin.class))
            .assertNext(basicDigitalTwin -> logger.info("Created {} twin successfully", basicDigitalTwin.getId()))
            .verifyComplete());

        // Create room twin
        createRoomTwinRunner(roomTwinId, roomModelId, (twinId, twin) -> StepVerifier.create(
                asyncClient.createOrReplaceDigitalTwin(twinId, twin, BasicDigitalTwin.class))
            .assertNext(basicDigitalTwin -> logger.info("Created {} twin successfully", basicDigitalTwin.getId()))
            .verifyComplete());

        // Create hvac twin
        createHvacTwinRunner(hvacTwinId, hvacModelId, (twinId, twin) -> StepVerifier.create(
                asyncClient.createOrReplaceDigitalTwin(twinId, twin, BasicDigitalTwin.class))
            .assertNext(basicDigitalTwin -> logger.info("Created {} twin successfully", basicDigitalTwin.getId()))
            .verifyComplete());
    }
}
