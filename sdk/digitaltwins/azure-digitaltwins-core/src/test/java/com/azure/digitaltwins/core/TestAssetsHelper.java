// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.models.JsonPatchDocument;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class TestAssetsHelper {
    // Remove all new line characters as they are different in different Operaing Systems.
    // This will ensure that the recorded files always match the request in playback mode of tests.
    private static String removeNewLines(String payload) {
        return payload.replace(System.lineSeparator(), "");
    }

    public static String getFloorModelPayload(String floorModelId, String roomModelId, String hvacModelId) {
        return removeNewLines(readResourceFile("FloorModelPayload")
            .replace("FLOOR_MODEL_ID", floorModelId)
            .replace("ROOM_MODEL_ID", roomModelId)
            .replace("HVAC_MODEL_ID", hvacModelId));
    }

    public static String getRoomModelPayload(String roomModelId, String floorModelId) {
        return removeNewLines(readResourceFile("RoomModelPayload")
            .replace("ROOM_MODEL_ID", roomModelId)
            .replace("FLOOR_MODEL_ID", floorModelId));
    }

    public static String getHvacModelPayload(String hvacModelId, String floorModelId) {
        return removeNewLines(readResourceFile("HvacModelPayload")
            .replace("HVAC_MODEL_ID", hvacModelId)
            .replace("FLOOR_MODEL_ID", floorModelId));
    }

    public static String getBuildingModelPayload(String buildingModelId, String hvacModelId, String floorModelId) {
        return removeNewLines(readResourceFile("BuildingModelPayload")
            .replace("BUILDING_MODEL_ID", buildingModelId)
            .replace("HVAC_MODEL_ID", hvacModelId)
            .replace("FLOOR_MODEL_ID", floorModelId));
    }

    public static String getWardModelPayload(String wardModelId) {
        return removeNewLines(readResourceFile("WardModelPayload")
            .replace("WARD_MODEL_ID", wardModelId));
    }

    public static JsonPatchDocument getRoomTwinUpdatePayload() {
        JsonPatchDocument jsonPatch = new JsonPatchDocument();
        jsonPatch.appendAdd("/Humidity", 30);
        jsonPatch.appendReplace("/Temperature", 70);
        jsonPatch.appendRemove("/EmployeeId");
        return jsonPatch;
    }

    public static JsonPatchDocument getRoomTwinSecondUpdatePayload() {
        JsonPatchDocument jsonPatch = new JsonPatchDocument();
        jsonPatch.appendReplace("/Temperature", 80);
        return jsonPatch;
    }

    public static JsonPatchDocument getWifiComponentUpdatePayload() {
        JsonPatchDocument jsonPatch = new JsonPatchDocument();
        jsonPatch.appendReplace("/Network", "New Network");
        return jsonPatch;
    }

    public static JsonPatchDocument getWifiComponentSecondUpdatePayload() {
        JsonPatchDocument jsonPatch = new JsonPatchDocument();
        jsonPatch.appendReplace("/Network", "Even newer Network");
        return jsonPatch;
    }

    public static String getFloorTwinPayload(String floorModelId) {
        return removeNewLines(readResourceFile("FloorTwinPayload")
            .replace("FLOOR_MODEL_ID", floorModelId));
    }

    public static String getRoomTwinPayload(String roomModelId) {
        return removeNewLines(readResourceFile("RoomTwinPayload")
            .replace("ROOM_MODEL_ID", roomModelId));
    }

    public static String getRelationshipPayload(String targetTwinId, String relationshipName) {
        return removeNewLines(readResourceFile("RelationshipPayload")
            .replace("TARGET_TWIN_ID", targetTwinId)
            .replace("RELATIONSHIP_NAME", relationshipName));
    }

    public static String getRelationshipWithPropertyPayload(String targetTwinId, String relationshipName, String propertyName, boolean propertyValue) {
        return removeNewLines(readResourceFile("RelationshipWithPropertyPayload")
            .replace("TARGET_TWIN_ID", targetTwinId)
            .replace("RELATIONSHIP_NAME", relationshipName)
            .replace("PROPERTY_NAME", propertyName)
            .replace("\"PROPERTY_VALUE\"", String.valueOf(propertyValue).toLowerCase()));
    }

    public static JsonPatchDocument getRelationshipUpdatePayload(String propertyName, boolean propertyValue) {
        JsonPatchDocument jsonPatch = new JsonPatchDocument();
        jsonPatch.appendReplace(propertyName, propertyValue);
        return jsonPatch;
    }

    public static String getWifiModelPayload(String wifiModelId) {
        return removeNewLines(readResourceFile("WifiModelPayload")
            .replace("WIFI_MODEL_ID", wifiModelId));
    }

    public static String getRoomWithWifiModelPayload(String roomWithWifiModelId, String wifiModelId, String wifiComponentName) {
        return removeNewLines(readResourceFile("RoomWithWifiModelPayload")
            .replace("ROOM_WITH_WIFI_MODEL_ID", roomWithWifiModelId)
            .replace("WIFI_MODEL_ID", wifiModelId)
            .replace("WIFI_COMPONENT_NAME", wifiComponentName));
    }

    public static String getRoomWithWifiTwinPayload(String roomWithWifiModelId, String wifiComponentName) {
        return removeNewLines(readResourceFile("RoomWithWifiTwinPayload")
            .replace("ROOM_WITH_WIFI_MODEL_ID", roomWithWifiModelId)
            .replace("WIFI_COMPONENT_NAME", wifiComponentName));
    }

    public static String getHvacTwinPayload(String hvacModelId) {
        return removeNewLines(readResourceFile("HvacTwinPayload")
            .replace("HVAC_MODEL_ID", hvacModelId));
    }

    // This method assumes that the file name is a json file under the test-assets folder within the resources directory
    public static String readResourceFile(String fileName) {
        StringBuilder resourceFileContents = new StringBuilder();
        try (InputStream inputStream = TestAssetsHelper.class.getResourceAsStream("/test-assets/" + fileName + ".json")) {
            Scanner s = new Scanner(inputStream);
            while (s.hasNext()) {
                resourceFileContents.append(s.next());
            }

            return resourceFileContents.toString();
        } catch (IOException e) {
            throw new RuntimeException("Cannot find file /test-assets/" + fileName + ".json in the resources folder", e);
        }
    }
}
