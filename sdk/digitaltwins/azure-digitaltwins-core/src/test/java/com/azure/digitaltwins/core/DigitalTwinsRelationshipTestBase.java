// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

/**
 * This abstract test class defines all the tests that both the sync and async relationship test classes need to implement. It also
 * houses some relationship test specific helper functions.
 */
public abstract class DigitalTwinsRelationshipTestBase extends DigitalTwinsTestBase {
    static final String CONTAINS_RELATIONSHIP = "contains";
    static final String CONTAINED_IN_RELATIONSHIP = "containedIn";
    static final String COOLS_RELATIONSHIP = "cools";
    static final String COOLED_BY_RELATIONSHIP = "cooledBy";

    static final String FLOOR_CONTAINS_ROOM_RELATIONSHIP_ID = "FloorToRoomRelationship";
    static final String FLOOR_COOLED_BY_HVAC_RELATIONSHIP_ID = "FloorToHvacRelationship";
    static final String HVAC_COOLS_FLOOR_RELATIONSHIP_ID = "HvacToFloorRelationship";
    static final String ROOM_CONTAINED_IN_FLOOR_RELATIONSHIP_ID = "RoomToFloorRelationship";

    // Relationships list operation default max item count is 10. We create 31 to make sure we will get over 3 pages of
    // response. Ideally, service team would let us set max items per page when listing, but that isn't a feature yet
    static final int BULK_RELATIONSHIP_COUNT = 21;
    static final int RELATIONSHIP_PAGE_SIZE_DEFAULT = 10;

    @Test
    public abstract void relationshipLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion)
        throws IOException;

    @Test
    public abstract void relationshipListOperationWithMultiplePages(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException, InterruptedException;

    @Test
    public abstract void createOrReplaceRelationshipFailsWhenIfNoneMatchStar(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void createOrReplaceRelationshipSucceedsWhenNoIfNoneHeader(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void patchRelationshipFailsWhenETagDoesNotMatch(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void patchRelationshipSucceedsWhenETagMatches(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void deleteRelationshipFailsWhenETagDoesNotMatch(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void deleteRelationshipSucceedsWhenETagMatches(HttpClient httpClient,
        DigitalTwinsServiceVersion serviceVersion) throws IOException;

    void createModelsRunner(String floorModelId, String roomModelId, String hvacModelId,
        Consumer<List<String>> createModelsTestRunner) {
        String floorModel = TestAssetsHelper.getFloorModelPayload(floorModelId, roomModelId, hvacModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        String hvacModel = TestAssetsHelper.getHvacModelPayload(hvacModelId, floorModelId);

        createModelsTestRunner.accept(asList(floorModel, roomModel, hvacModel));
    }

    void createFloorTwinRunner(String floorTwinId, String floorModelId,
        BiConsumer<String, BasicDigitalTwin> createFloorTwinTestRunner) throws IOException {
        String floorTwin = TestAssetsHelper.getFloorTwinPayload(floorModelId);
        createTwinRunner(floorTwinId, deserializeJsonString(floorTwin, BasicDigitalTwin::fromJson),
            createFloorTwinTestRunner);
    }

    void createRoomTwinRunner(String roomTwinId, String roomModelId,
        BiConsumer<String, BasicDigitalTwin> createRoomTwinTestRunner) throws IOException {
        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        createTwinRunner(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin::fromJson),
            createRoomTwinTestRunner);
    }

    void createHvacTwinRunner(String hvacTwinId, String hvacModelId,
        BiConsumer<String, BasicDigitalTwin> createHvacTwinTestRunner) throws IOException {
        String hvacTwin = TestAssetsHelper.getHvacTwinPayload(hvacModelId);
        createTwinRunner(hvacTwinId, deserializeJsonString(hvacTwin, BasicDigitalTwin::fromJson),
            createHvacTwinTestRunner);
    }

    void createTwinRunner(String twinId, BasicDigitalTwin twin,
        BiConsumer<String, BasicDigitalTwin> createTwinTestRunner) {
        createTwinTestRunner.accept(twinId, twin);
    }
}
