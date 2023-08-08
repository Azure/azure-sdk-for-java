// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.models.JsonPatchDocument;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.models.CreateOrReplaceRelationshipOptions;
import com.azure.digitaltwins.core.models.DeleteRelationshipOptions;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.digitaltwins.core.models.DigitalTwinsResponse;
import com.azure.digitaltwins.core.models.IncomingRelationship;
import com.azure.digitaltwins.core.models.UpdateRelationshipOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static com.azure.digitaltwins.core.helpers.UniqueIdHelper.getUniqueDigitalTwinId;
import static com.azure.digitaltwins.core.helpers.UniqueIdHelper.getUniqueModelId;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_PRECON_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigitalTwinsRelationshipTest extends DigitalTwinsRelationshipTestBase {
    private final ClientLogger logger = new ClientLogger(DigitalTwinsRelationshipTest.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void relationshipLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(client, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId, hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId, CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            String floorTwinCoolsRelationshipPayload = TestAssetsHelper.getRelationshipPayload(floorTwinId, COOLS_RELATIONSHIP);
            String floorTwinContainedInRelationshipPayload = TestAssetsHelper.getRelationshipPayload(floorTwinId, CONTAINED_IN_RELATIONSHIP);
            String floorCooledByHvacPayload = TestAssetsHelper.getRelationshipPayload(hvacTwinId, COOLED_BY_RELATIONSHIP);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload("/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            BasicRelationship floorRoomRelationship = client.createOrReplaceRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, deserializeJsonString(floorContainsRoomPayload, BasicRelationship.class), BasicRelationship.class);
            assertThat(floorRoomRelationship.getId())
                .isEqualTo(FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID)
                .as("Created relationship from floor -> room");
            logger.info("Created {} relationship between source = {} and target = {}", floorRoomRelationship.getId(), floorRoomRelationship.getSourceId(), floorRoomRelationship.getTargetId());

            // Create relationship from Floor -> Hvac
            BasicRelationship floorHvacRelationship = client.createOrReplaceRelationship(floorTwinId, FLOOR_COOLED_BY_HVAC_RELATIONSHIP_ID, deserializeJsonString(floorCooledByHvacPayload, BasicRelationship.class), BasicRelationship.class);
            assertThat(floorHvacRelationship.getId())
                .isEqualTo(FLOOR_COOLED_BY_HVAC_RELATIONSHIP_ID)
                .as("Created relationship from floor -> hvac");
            logger.info("Created {} relationship between source = {} and target = {}", floorHvacRelationship.getId(), floorHvacRelationship.getSourceId(), floorHvacRelationship.getTargetId());

            // Create relationship from Hvac -> Floor
            BasicRelationship hvacFloorRelationship = client.createOrReplaceRelationship(hvacTwinId, HVAC_COOLS_FLOOR_RELATIONSHIP_ID, deserializeJsonString(floorTwinCoolsRelationshipPayload, BasicRelationship.class), BasicRelationship.class);
            assertThat(hvacFloorRelationship.getId())
                .isEqualTo(HVAC_COOLS_FLOOR_RELATIONSHIP_ID)
                .as("Created relationship from hvac -> floor");
            logger.info("Created {} relationship between source = {} and target = {}", hvacFloorRelationship.getId(), hvacFloorRelationship.getSourceId(), hvacFloorRelationship.getTargetId());

            // Create relationship from Room -> Floor
            BasicRelationship roomFloorRelationship = client.createOrReplaceRelationship(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID, deserializeJsonString(floorTwinContainedInRelationshipPayload, BasicRelationship.class), BasicRelationship.class);
            assertThat(roomFloorRelationship.getId())
                .isEqualTo(ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID)
                .as("Created relationship from room -> floor");
            logger.info("Created {} relationship between source = {} and target = {}", roomFloorRelationship.getId(), roomFloorRelationship.getSourceId(), roomFloorRelationship.getTargetId());

            // Create a relation which already exists - should return status code 409 (Conflict).
            assertRestException(
                () -> client.createOrReplaceRelationshipWithResponse(
                    roomTwinId,
                    ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID,
                    floorTwinContainedInRelationshipPayload,
                    String.class,
                    new CreateOrReplaceRelationshipOptions().setIfNoneMatch("*"),
                    Context.NONE),
                HTTP_PRECON_FAILED
            );

            // Update relationships

            // Create relationship from Floor -> Room
            DigitalTwinsResponse<Void> updateRelationshipResponse = client.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorContainsRoomUpdatePayload, null, Context.NONE);
            assertThat(updateRelationshipResponse.getStatusCode())
                .as("Updated relationship floor -> room")
                .isEqualTo(HTTP_NO_CONTENT);
            logger.info("Updated {} relationship successfully in source {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);

            // GET relationship
            BasicRelationship floorContainsRoomRelationship = client.getRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, BasicRelationship.class);
            assertThat(floorContainsRoomRelationship.getId())
                .isEqualTo(FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID)
                .as("Retrieved floor -> room relationship");
            logger.info("Retrieved {} relationship under source {}", floorContainsRoomRelationship.getId(), floorContainsRoomRelationship.getSourceId());

            // LIST incoming relationships
            List<String> incomingRelationshipsSourceIds = new ArrayList<>();
            PagedIterable<IncomingRelationship> listIncomingRelationships = client.listIncomingRelationships(floorTwinId, Context.NONE);
            listIncomingRelationships.forEach(incomingRelationship -> incomingRelationshipsSourceIds.add(incomingRelationship.getSourceId()));
            assertThat(incomingRelationshipsSourceIds)
                .as("Floor has incoming relationships from room and hvac")
                .containsExactlyInAnyOrder(roomTwinId, hvacTwinId);
            logger.info("Retrieved incoming relationships for {}, found sources {}", floorTwinId, Arrays.toString(incomingRelationshipsSourceIds.toArray()));

            // LIST relationships
            List<String> relationshipsTargetIds = new ArrayList<>();
            PagedIterable<BasicRelationship> listRelationships = client.listRelationships(floorTwinId, BasicRelationship.class);
            listRelationships.forEach(basicRelationship -> relationshipsTargetIds.add(basicRelationship.getTargetId()));
            assertThat(relationshipsTargetIds)
                .as("Floor has a relationship to room and hvac")
                .containsExactlyInAnyOrder(roomTwinId, hvacTwinId);
            logger.info("Retrieved all relationships for {}, found targets {}", floorTwinId, Arrays.toString(relationshipsTargetIds.toArray()));

            // LIST relationship by name
            List<String> containedInRelationshipsTargetIds = new ArrayList<>();
            PagedIterable<BasicRelationship> listContainedInRelationship = client.listRelationships(roomTwinId, CONTAINED_IN_RELATIONSHIP, BasicRelationship.class, Context.NONE);
            listContainedInRelationship.forEach(basicRelationship -> {
                containedInRelationshipsTargetIds.add(basicRelationship.getTargetId());
                logger.info("Retrieved relationship {} for twin {}", basicRelationship.getId(), roomTwinId);
            });
            assertThat(containedInRelationshipsTargetIds.size())
                .as("Room has only one containedIn relationship to floor")
                .isEqualTo(1);

            // DELETE the created relationships
            client.deleteRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID);
            logger.info("Deleted relationship {} for twin {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);

            client.deleteRelationship(roomTwinId, ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID);
            logger.info("Deleted relationship {} for twin {}", ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID, roomTwinId);

            client.deleteRelationship(floorTwinId, FLOOR_COOLED_BY_HVAC_RELATIONSHIP_ID);
            logger.info("Deleted relationship {} for twin {}", FLOOR_COOLED_BY_HVAC_RELATIONSHIP_ID, floorTwinId);

            client.deleteRelationship(hvacTwinId, HVAC_COOLS_FLOOR_RELATIONSHIP_ID);
            logger.info("Deleted relationship {} for twin {}", HVAC_COOLS_FLOOR_RELATIONSHIP_ID, hvacTwinId);

            // GET a relationship which doesn't exist - should return status code 404 (Not Found).
            assertRestException(
                () -> client.getRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, String.class),
                HTTP_NOT_FOUND
            );

        } finally {
            // Clean up
            try {
                logger.info("Cleaning up test resources.");

                logger.info("Deleting created relationships.");
                // Delete the created relationships.
                List<BasicRelationship> relationships = new ArrayList<>();
                client.listRelationships(floorTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(roomTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(hvacTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                relationships.forEach(basicRelationship -> client.deleteRelationship(basicRelationship.getSourceId(), basicRelationship.getId()));

                // Now the twins and models can be deleted.
                logger.info("Deleting created digital twins.");
                client.deleteDigitalTwin(floorTwinId);
                client.deleteDigitalTwin(roomTwinId);
                client.deleteDigitalTwin(hvacTwinId);

                logger.info("Deleting created models.");
                client.deleteModel(floorModelId);
                client.deleteModel(roomModelId);
                client.deleteModel(hvacModelId);
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void relationshipListOperationWithMultiplePages(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException, InterruptedException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        List<String> createdOutgoingRelationshipIds = new ArrayList<>();
        List<String> createdIncomingRelationshipIds = new ArrayList<>();

        try {
            createModelsAndTwins(client, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId, hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId, CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            String roomContainedInFloorPayload = TestAssetsHelper.getRelationshipPayload(floorTwinId, CONTAINED_IN_RELATIONSHIP);

            // Create large number of relationships to test paging functionality
            // Relationship list api does not have max item count request option so we have to create a large number of them to trigger paging functionality from the service.
            // Create relationships from Floor -> Room
            for (int i = 0; i < BULK_RELATIONSHIP_COUNT; i++) {
                String relationshipId = FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID + this.testResourceNamer.randomUuid();
                client.createOrReplaceRelationship(floorTwinId, relationshipId, deserializeJsonString(floorContainsRoomPayload, BasicRelationship.class), BasicRelationship.class);
                createdOutgoingRelationshipIds.add(relationshipId);
            }

            waitIfLive();

            // Create multiple incoming relationships to the floor. Typically a room would have relationships to multiple
            // different floors, but for the sake of test simplicity, we'll just add multiple relationships from the same room
            // to the same floor.
            for (int i = 0; i < BULK_RELATIONSHIP_COUNT; i++) {
                String relationshipId = ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID + this.testResourceNamer.randomUuid();
                client.createOrReplaceRelationship(roomTwinId, relationshipId, deserializeJsonString(roomContainedInFloorPayload, BasicRelationship.class), BasicRelationship.class);
                createdIncomingRelationshipIds.add(relationshipId);
            }

            waitIfLive();

            // LIST relationships
            PagedIterable<BasicRelationship> listOutgoingRelationships = client.listRelationships(floorTwinId, BasicRelationship.class);

            AtomicInteger outgoingRelationshipsPageCount = new AtomicInteger();
            listOutgoingRelationships.iterableByPage().forEach(relationshipsPagedResponse -> {
                outgoingRelationshipsPageCount.getAndIncrement();
                logger.info("content for this page " + outgoingRelationshipsPageCount);
                for (BasicRelationship data : relationshipsPagedResponse.getValue()) {
                    logger.info(data.getId());
                }

                if (relationshipsPagedResponse.getContinuationToken() != null) {
                    assertEquals(RELATIONSHIP_PAGE_SIZE_DEFAULT, relationshipsPagedResponse.getValue().size(), "Unexpected page size for a non-terminal page");
                }
            });

            assertThat(outgoingRelationshipsPageCount.get()).isGreaterThan(1);

            // LIST incoming relationships
            PagedIterable<IncomingRelationship> listIncomingRelationships = client.listIncomingRelationships(floorTwinId);

            AtomicInteger incomingRelationshipsPageCount = new AtomicInteger();
            listIncomingRelationships.iterableByPage().forEach(relationshipsPagedResponse -> {
                incomingRelationshipsPageCount.getAndIncrement();
                logger.info("content for this page " + incomingRelationshipsPageCount);
                for (IncomingRelationship data : relationshipsPagedResponse.getValue()) {
                    logger.info(data.getRelationshipId());
                }

                if (relationshipsPagedResponse.getContinuationToken() != null) {
                    assertEquals(RELATIONSHIP_PAGE_SIZE_DEFAULT, relationshipsPagedResponse.getValue().size(), "Unexpected page size for a non-terminal page");
                }
            });

            assertThat(incomingRelationshipsPageCount.get()).isGreaterThan(1);
        } finally {
            // Clean up
            try {
                logger.info("Cleaning up test resources.");

                logger.info("Deleting created relationships.");
                // Delete the created relationships.
                createdOutgoingRelationshipIds.forEach(relationshipId -> client.deleteRelationship(floorTwinId, relationshipId));
                createdIncomingRelationshipIds.forEach(relationshipId -> client.deleteRelationship(roomTwinId, relationshipId));

                // Now the twins and models can be deleted.
                logger.info("Deleting created digital twins.");
                client.deleteDigitalTwin(floorTwinId);
                client.deleteDigitalTwin(roomTwinId);
                client.deleteDigitalTwin(hvacTwinId);

                logger.info("Deleting created models.");
                client.deleteModel(floorModelId);
                client.deleteModel(roomModelId);
                client.deleteModel(hvacModelId);
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createOrReplaceRelationshipFailsWhenIfNoneMatchStar(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(client, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId, hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId, CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            BasicRelationship floorContainsRoomRelationship = deserializeJsonString(floorContainsRoomPayload, BasicRelationship.class);

            // Create relationship from Floor -> Room
            BasicRelationship floorRoomRelationship = client.createOrReplaceRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorContainsRoomRelationship, BasicRelationship.class);
            assertThat(floorRoomRelationship.getId())
                .isEqualTo(FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID)
                .as("Created relationship from floor -> room");
            logger.info("Created {} relationship between source = {} and target = {}", floorRoomRelationship.getId(), floorRoomRelationship.getSourceId(), floorRoomRelationship.getTargetId());

            assertRestException(
                () -> client.createOrReplaceRelationshipWithResponse(
                    floorTwinId,
                    FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomRelationship,
                    BasicRelationship.class,
                    new CreateOrReplaceRelationshipOptions().setIfNoneMatch("*"),
                    Context.NONE),
                HTTP_PRECON_FAILED
            );
        } finally {
            // Clean up
            try {
                logger.info("Cleaning up test resources.");

                logger.info("Deleting created relationships.");
                // Delete the created relationships.
                List<BasicRelationship> relationships = new ArrayList<>();
                client.listRelationships(floorTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(roomTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(hvacTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                relationships.forEach(basicRelationship -> client.deleteRelationship(basicRelationship.getSourceId(), basicRelationship.getId()));

                // Now the twins and models can be deleted.
                logger.info("Deleting created digital twins.");
                client.deleteDigitalTwin(floorTwinId);
                client.deleteDigitalTwin(roomTwinId);
                client.deleteDigitalTwin(hvacTwinId);

                logger.info("Deleting created models.");
                client.deleteModel(floorModelId);
                client.deleteModel(roomModelId);
                client.deleteModel(hvacModelId);
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createOrReplaceRelationshipSucceedsWhenNoIfNoneHeader(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(client, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId, hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId, CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            String floorTwinContainedInRelationshipPayload = TestAssetsHelper.getRelationshipPayload(floorTwinId, CONTAINED_IN_RELATIONSHIP);

            // Create relationship from Floor -> Room
            BasicRelationship floorRoomRelationship = client.createOrReplaceRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, deserializeJsonString(floorContainsRoomPayload, BasicRelationship.class), BasicRelationship.class);
            assertThat(floorRoomRelationship.getId())
                .isEqualTo(FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID)
                .as("Created relationship from floor -> room");
            logger.info("Created {} relationship between source = {} and target = {}", floorRoomRelationship.getId(), floorRoomRelationship.getSourceId(), floorRoomRelationship.getTargetId());

            try {
                client.createOrReplaceRelationshipWithResponse(
                    roomTwinId,
                    ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID,
                    floorTwinContainedInRelationshipPayload,
                    String.class,
                    null, //don't set ifNoneMatch header
                    Context.NONE);
            } catch (ErrorResponseException ex) {
                if (ex.getResponse().getStatusCode() == HTTP_PRECON_FAILED) {
                    fail("Should not have gotten a 412 error since ifNoneMatch header was not sent", ex);
                } else {
                    throw ex;
                }
            }
        } finally {
            // Clean up
            try {
                logger.info("Cleaning up test resources.");

                logger.info("Deleting created relationships.");
                // Delete the created relationships.
                List<BasicRelationship> relationships = new ArrayList<>();
                client.listRelationships(floorTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(roomTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(hvacTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                relationships.forEach(basicRelationship -> client.deleteRelationship(basicRelationship.getSourceId(), basicRelationship.getId()));

                // Now the twins and models can be deleted.
                logger.info("Deleting created digital twins.");
                client.deleteDigitalTwin(floorTwinId);
                client.deleteDigitalTwin(roomTwinId);
                client.deleteDigitalTwin(hvacTwinId);

                logger.info("Deleting created models.");
                client.deleteModel(floorModelId);
                client.deleteModel(roomModelId);
                client.deleteModel(hvacModelId);
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchRelationshipFailsWhenETagDoesNotMatch(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(client, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId, hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId, CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload("/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            DigitalTwinsResponse<BasicRelationship> floorRoomRelationship = client.createOrReplaceRelationshipWithResponse(
                floorTwinId,
                FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                deserializeJsonString(floorContainsRoomPayload, BasicRelationship.class),
                BasicRelationship.class,
                null,
                Context.NONE);
            logger.info(
                "Created {} relationship between source = {} and target = {}",
                floorRoomRelationship.getValue().getId(),
                floorRoomRelationship.getValue().getSourceId(),
                floorRoomRelationship.getValue().getTargetId());

            String etagBeforeUpdate = floorRoomRelationship.getDeserializedHeaders().getETag();

            // Update relationship from Floor -> Room
            client.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorContainsRoomUpdatePayload, null, Context.NONE);

            // Update the relationship again, but with the out of date etag
            JsonPatchDocument floorContainsRoomSecondUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload("/isAccessRestricted", true);

            assertRestException(
                () -> client.updateRelationshipWithResponse(
                    floorTwinId,
                    FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomSecondUpdatePayload,
                    new UpdateRelationshipOptions().setIfMatch(etagBeforeUpdate),
                    Context.NONE),
                HTTP_PRECON_FAILED
            );

            // DELETE the created relationship
            client.deleteRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID);
            logger.info("Deleted relationship {} for twin {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
        } finally {
            // Clean up
            try {
                logger.info("Cleaning up test resources.");

                logger.info("Deleting created relationships.");
                // Delete the created relationships.
                List<BasicRelationship> relationships = new ArrayList<>();
                client.listRelationships(floorTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(roomTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(hvacTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                relationships.forEach(basicRelationship -> client.deleteRelationship(basicRelationship.getSourceId(), basicRelationship.getId()));

                // Now the twins and models can be deleted.
                logger.info("Deleting created digital twins.");
                client.deleteDigitalTwin(floorTwinId);
                client.deleteDigitalTwin(roomTwinId);
                client.deleteDigitalTwin(hvacTwinId);

                logger.info("Deleting created models.");
                client.deleteModel(floorModelId);
                client.deleteModel(roomModelId);
                client.deleteModel(hvacModelId);
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchRelationshipSucceedsWhenETagMatches(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(client, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId, hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId, CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload("/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            DigitalTwinsResponse<BasicRelationship> floorRoomRelationship = client.createOrReplaceRelationshipWithResponse(
                floorTwinId,
                FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                deserializeJsonString(floorContainsRoomPayload, BasicRelationship.class),
                BasicRelationship.class,
                null,
                Context.NONE);
            logger.info(
                "Created {} relationship between source = {} and target = {}",
                floorRoomRelationship.getValue().getId(),
                floorRoomRelationship.getValue().getSourceId(),
                floorRoomRelationship.getValue().getTargetId());

            // Update relationship from Floor -> Room
            DigitalTwinsResponse<Void> updateResponse = client.updateRelationshipWithResponse(
                floorTwinId,
                FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                floorContainsRoomUpdatePayload,
                null,
                Context.NONE);

            String upToDateETag = updateResponse.getDeserializedHeaders().getETag();

            // Update the relationship again, but with the up to date etag
            JsonPatchDocument floorContainsRoomSecondUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload("/isAccessRestricted", true);

            try {
                client.updateRelationshipWithResponse(
                    floorTwinId,
                    FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    floorContainsRoomSecondUpdatePayload,
                    new UpdateRelationshipOptions().setIfMatch(upToDateETag),
                    Context.NONE);
            } catch (ErrorResponseException ex) {
                if (ex.getResponse().getStatusCode() == HTTP_PRECON_FAILED) {
                    fail("Should not have gotten a 412 error since ifMatch header had up to date etag", ex);
                } else {
                    throw ex;
                }
            }

            // DELETE the created relationship
            client.deleteRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID);
            logger.info("Deleted relationship {} for twin {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
        } finally {
            // Clean up
            try {
                logger.info("Cleaning up test resources.");

                logger.info("Deleting created relationships.");
                // Delete the created relationships.
                List<BasicRelationship> relationships = new ArrayList<>();
                client.listRelationships(floorTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(roomTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(hvacTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                relationships.forEach(basicRelationship -> client.deleteRelationship(basicRelationship.getSourceId(), basicRelationship.getId()));

                // Now the twins and models can be deleted.
                logger.info("Deleting created digital twins.");
                client.deleteDigitalTwin(floorTwinId);
                client.deleteDigitalTwin(roomTwinId);
                client.deleteDigitalTwin(hvacTwinId);

                logger.info("Deleting created models.");
                client.deleteModel(floorModelId);
                client.deleteModel(roomModelId);
                client.deleteModel(hvacModelId);
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void deleteRelationshipFailsWhenETagDoesNotMatch(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(client, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId, hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId, CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload("/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            DigitalTwinsResponse<BasicRelationship> floorRoomRelationship = client.createOrReplaceRelationshipWithResponse(
                floorTwinId,
                FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                deserializeJsonString(floorContainsRoomPayload, BasicRelationship.class),
                BasicRelationship.class,
                null,
                Context.NONE);
            logger.info(
                "Created {} relationship between source = {} and target = {}",
                floorRoomRelationship.getValue().getId(),
                floorRoomRelationship.getValue().getSourceId(),
                floorRoomRelationship.getValue().getTargetId());

            String etagBeforeUpdate = floorRoomRelationship.getDeserializedHeaders().getETag();

            // Update relationship from Floor -> Room
            client.updateRelationshipWithResponse(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorContainsRoomUpdatePayload, null, Context.NONE);

            // Delete the relationship, but with the out of date etag
            assertRestException(
                () -> client.deleteRelationshipWithResponse(
                    floorTwinId,
                    FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    new DeleteRelationshipOptions().setIfMatch(etagBeforeUpdate),
                    Context.NONE),
                HTTP_PRECON_FAILED
            );

            // DELETE the created relationship with no etag specified to clean up
            client.deleteRelationship(floorTwinId, FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID);
            logger.info("Deleted relationship {} for twin {}", FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID, floorTwinId);
        } finally {
            // Clean up
            try {
                logger.info("Cleaning up test resources.");

                logger.info("Deleting created relationships.");
                // Delete the created relationships.
                List<BasicRelationship> relationships = new ArrayList<>();
                client.listRelationships(floorTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(roomTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(hvacTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                relationships.forEach(basicRelationship -> client.deleteRelationship(basicRelationship.getSourceId(), basicRelationship.getId()));

                // Now the twins and models can be deleted.
                logger.info("Deleting created digital twins.");
                client.deleteDigitalTwin(floorTwinId);
                client.deleteDigitalTwin(roomTwinId);
                client.deleteDigitalTwin(hvacTwinId);

                logger.info("Deleting created models.");
                client.deleteModel(floorModelId);
                client.deleteModel(roomModelId);
                client.deleteModel(hvacModelId);
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void deleteRelationshipSucceedsWhenETagMatches(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorModelId = getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomModelId = getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacModelId = getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorTwinId = getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomTwinId = getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String hvacTwinId = getUniqueDigitalTwinId(TestAssetDefaults.HVAC_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        try {
            // Create floor, room and hvac model
            createModelsAndTwins(client, floorModelId, roomModelId, hvacModelId, floorTwinId, roomTwinId, hvacTwinId);

            // Connect the created twins via relationships
            String floorContainsRoomPayload = TestAssetsHelper.getRelationshipWithPropertyPayload(roomTwinId, CONTAINS_RELATIONSHIP, "isAccessRestricted", true);
            JsonPatchDocument floorContainsRoomUpdatePayload = TestAssetsHelper.getRelationshipUpdatePayload("/isAccessRestricted", false);

            // Create relationship from Floor -> Room
            DigitalTwinsResponse<BasicRelationship> floorRoomRelationship = client.createOrReplaceRelationshipWithResponse(
                floorTwinId,
                FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                deserializeJsonString(floorContainsRoomPayload, BasicRelationship.class),
                BasicRelationship.class,
                null,
                Context.NONE);
            logger.info(
                "Created {} relationship between source = {} and target = {}",
                floorRoomRelationship.getValue().getId(),
                floorRoomRelationship.getValue().getSourceId(),
                floorRoomRelationship.getValue().getTargetId());

            // Update relationship from Floor -> Room
            DigitalTwinsResponse<Void> updateResponse = client.updateRelationshipWithResponse(
                floorTwinId,
                FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                floorContainsRoomUpdatePayload,
                null,
                Context.NONE);

            String upToDateETag = updateResponse.getDeserializedHeaders().getETag();

            // Delete the relationship, but with the up to date etag
            try {
                client.deleteRelationshipWithResponse(
                    floorTwinId,
                    FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID,
                    new DeleteRelationshipOptions().setIfMatch(upToDateETag),
                    Context.NONE);
            } catch (ErrorResponseException ex) {
                if (ex.getResponse().getStatusCode() == HTTP_PRECON_FAILED) {
                    fail("Should not have gotten a 412 error since ifMatch header had up to date etag", ex);
                } else {
                    throw ex;
                }
            }
        } finally {
            // Clean up
            try {
                logger.info("Cleaning up test resources.");

                logger.info("Deleting created relationships.");
                // Delete the created relationships.
                List<BasicRelationship> relationships = new ArrayList<>();
                client.listRelationships(floorTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(roomTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                client.listRelationships(hvacTwinId, BasicRelationship.class)
                    .iterableByPage()
                    .forEach(basicRelationshipPagedResponse -> relationships.addAll(basicRelationshipPagedResponse.getValue()));
                relationships.forEach(basicRelationship -> client.deleteRelationship(basicRelationship.getSourceId(), basicRelationship.getId()));

                // Now the twins and models can be deleted.
                logger.info("Deleting created digital twins.");
                client.deleteDigitalTwin(floorTwinId);
                client.deleteDigitalTwin(roomTwinId);
                client.deleteDigitalTwin(hvacTwinId);

                logger.info("Deleting created models.");
                client.deleteModel(floorModelId);
                client.deleteModel(roomModelId);
                client.deleteModel(hvacModelId);
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }

    private void createModelsAndTwins(DigitalTwinsClient client, String floorModelId, String roomModelId, String hvacModelId, String floorTwinId, String roomTwinId, String hvacTwinId) throws JsonProcessingException {
        // Create floor, room and hvac model
        createModelsRunner(
            floorModelId,
            roomModelId,
            hvacModelId,
            modelsList -> {
                Iterable<DigitalTwinsModelData> createdModels = client.createModels(modelsList);
                logger.info("Created models successfully");
            }
        );

        // Create floor twin
        createFloorTwinRunner(
            floorTwinId,
            floorModelId,
            (twinId, twin) -> {
                BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(twinId, twin, BasicDigitalTwin.class);
                logger.info("Created {} twin successfully", createdTwin.getId());
            }
        );

        // Create room twin
        createRoomTwinRunner(
            roomTwinId,
            roomModelId,
            (twinId, twin) -> {
                BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(twinId, twin, BasicDigitalTwin.class);
                logger.info("Created {} twin successfully", createdTwin.getId());
            }
        );

        // Create hvac twin
        createHvacTwinRunner(
            hvacTwinId,
            hvacModelId,
            (twinId, twin) -> {
                BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(twinId, twin, BasicDigitalTwin.class);
                logger.info("Created {} twin successfully", createdTwin.getId());
            }
        );
    }
}
