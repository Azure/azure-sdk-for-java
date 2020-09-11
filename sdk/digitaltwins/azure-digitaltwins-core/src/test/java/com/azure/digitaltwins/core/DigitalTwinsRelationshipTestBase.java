// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.digitaltwins.core.TestAssetsHelper.*;
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

    @Test
    public abstract void relationshipLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    void createModelsRunner(String floorModelId, String roomModelId, String hvacModelId, Consumer<List<String>> createModelsTestRunner) {
        String floorModel = getFloorModelPayload(floorModelId, roomModelId, hvacModelId);
        String roomModel = getRoomModelPayload(roomModelId, floorModelId);
        String hvacModel = getHvacModelPayload(hvacModelId, floorModelId);

        createModelsTestRunner.accept(asList(floorModel, roomModel, hvacModel));
    }

    void createFloorTwinRunner(String floorTwinId, String floorModelId, BiConsumer<String, String> createFloorTwinTestRunner) {
        String floorTwin = getFloorTwinPayload(floorModelId);
        createTwinRunner(floorTwinId, floorTwin, createFloorTwinTestRunner);
    }

    void createRoomTwinRunner(String roomTwinId, String roomModelId, BiConsumer<String, String> createRoomTwinTestRunner) {
        String roomTwin = getRoomTwinPayload(roomModelId);
        createTwinRunner(roomTwinId, roomTwin, createRoomTwinTestRunner);
    }

    void createHvacTwinRunner(String hvacTwinId, String hvacModelId, BiConsumer<String, String> createHvacTwinTestRunner) {
        String hvacTwin = getHvacTwinPayload(hvacModelId);
        createTwinRunner(hvacTwinId, hvacTwin, createHvacTwinTestRunner);
    }

    void createTwinRunner(String twinId, String twin, BiConsumer<String, String> createTwinTestRunner) {
        createTwinTestRunner.accept(twinId, twin);
    }

}
