package com.azure.digitaltwins.core;

import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.util.UpdateOperationUtility;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;

public class TestAssetsHelper {
    protected static int MaxTries = 10;
    protected static int MaxIdLength = 27;

    // Remove all new line characters as they are different in different Operaing Systems.
    // This will ensure that the recorded files always match the request in playback mode of tests.
    private static String RemoveNewLines(String payload)
    {
        return payload.replace(System.lineSeparator(), "");
    }

    public static String GetFloorModelPayload(String floorModelId, String roomModelId, String hvacModelId)
    {
        return RemoveNewLines(readResourceFile("FloorModelPayload")
            .replace("FLOOR_MODEL_ID", floorModelId)
            .replace("ROOM_MODEL_ID", roomModelId)
            .replace("HVAC_MODEL_ID", hvacModelId));
    }

    public static String GetRoomModelPayload(String roomModelId, String floorModelId)
    {
        return RemoveNewLines(readResourceFile("RoomModelPayload")
            .replace("ROOM_MODEL_ID", roomModelId)
            .replace("FLOOR_MODEL_ID", floorModelId));
    }

    public static String GetHvacModelPayload(String hvacModelId, String floorModelId)
    {
        return RemoveNewLines(readResourceFile("HvacModelPayload")
            .replace("HVAC_MODEL_ID", hvacModelId)
            .replace("FLOOR_MODEL_ID", floorModelId));
    }

    public static String GetBuildingModelPayload(String buildingModelId, String hvacModelId, String floorModelId)
    {
        return RemoveNewLines(readResourceFile("BuildingModelPayload")
            .replace("BUILDING_MODEL_ID", buildingModelId)
            .replace("HVAC_MODEL_ID", hvacModelId)
            .replace("FLOOR_MODEL_ID", floorModelId));
    }

    public static String GetWardModelPayload(String wardModelId)
    {
        return RemoveNewLines(readResourceFile("WardModelPayload")
            .replace("WARD_MODEL_ID", wardModelId));
    }

    public static List<Object> GetRoomTwinUpdatePayload()
    {
        UpdateOperationUtility uou = new UpdateOperationUtility();
        uou.appendAddOperation("/Humidity", 30);
        uou.appendReplaceOperation("/Temperature", 70);
        uou.appendRemoveOperation("/EmployeeId");
        return uou.getUpdateOperations();
    }

    public static List<Object> GetWifiComponentUpdatePayload()
    {
        UpdateOperationUtility uou = new UpdateOperationUtility();
        uou.appendReplaceOperation("/Network", "New Network");
        return uou.getUpdateOperations();
    }

    public static String GetFloorTwinPayload(String floorModelId)
    {
        return RemoveNewLines(readResourceFile("FloorTwinPayload")
            .replace("FLOOR_MODEL_ID", floorModelId));
    }

    public static String GetRoomTwinPayload(String roomModelId)
    {
        return RemoveNewLines(readResourceFile("RoomTwinPayload")
            .replace("ROOM_MODEL_ID", roomModelId));
    }

    public static String GetRelationshipPayload(String targetTwinId, String relationshipName)
    {
        return RemoveNewLines(readResourceFile("RelationshipPayload")
            .replace("TARGET_TWIN_ID", targetTwinId)
            .replace("RELATIONSHIP_NAME", relationshipName));
    }

    public static String GetRelationshipWithPropertyPayload(String targetTwinId, String relationshipName, String propertyName, boolean propertyValue)
    {
        return RemoveNewLines(readResourceFile("RelationshipWithPropertyPayload")
            .replace("TARGET_TWIN_ID", targetTwinId)
            .replace("RELATIONSHIP_NAME", relationshipName)
            .replace("PROPERTY_NAME", propertyName)
            .replace("\"PROPERTY_VALUE\"", String.valueOf(propertyValue).toLowerCase()));
    }

    public static List<Object> GetRelationshipUpdatePayload(String propertyName, boolean propertyValue)
    {
        UpdateOperationUtility uou = new UpdateOperationUtility();
        uou.appendReplaceOperation(propertyName, propertyValue);
        return uou.getUpdateOperations();
    }

    public static String GetWifiModelPayload(String wifiModelId)
    {
        return RemoveNewLines(readResourceFile("WifiModelPayload")
            .replace("WIFI_MODEL_ID", wifiModelId));
    }

    public static String GetRoomWithWifiModelPayload(String roomWithWifiModelId, String wifiModelId, String wifiComponentName)
    {
        return RemoveNewLines(readResourceFile("RoomWithWifiModelPayload")
            .replace("ROOM_WITH_WIFI_MODEL_ID", roomWithWifiModelId)
            .replace("WIFI_MODEL_ID", wifiModelId)
            .replace("WIFI_COMPONENT_NAME", wifiComponentName));
    }

    public static String GetRoomWithWifiTwinPayload(String roomWithWifiModelId, String wifiComponentName)
    {
        return RemoveNewLines(readResourceFile("RoomWithWifiTwinPayload")
            .replace("ROOM_WITH_WIFI_MODEL_ID", roomWithWifiModelId)
            .replace("WIFI_COMPONENT_NAME", wifiComponentName));
    }

    public static String GetHvacTwinPayload(String hvacModelId)
    {
        return RemoveNewLines(readResourceFile("HvacTwinPayload")
            .replace("HVAC_MODEL_ID", hvacModelId));
    }

    // This method assumes that the file name is a json file under the test-assets folder within the resources directory
    public static String readResourceFile(String fileName)
    {
        String resourceFileContents = "";
        try (InputStream inputStream = TestAssetsHelper.class.getResourceAsStream("/test-assets/" + fileName + ".json")) {
            Scanner s = new Scanner(inputStream);
            while (s.hasNext()) {
                resourceFileContents += s.next();
            }

            return resourceFileContents;
        } catch (IOException e) {
            throw new RuntimeException("Cannot find file " + fileName, e);
        }
    }

    public static String GetUniqueModelId(DigitalTwinsClient dtClient, String baseName)
    {
        return GetUniqueModelId(baseName, (id) -> dtClient.getModel(id));
    }

    public static String GetUniqueDigitalTwinId(DigitalTwinsClient dtClient, String baseName)
    {
        return GetUniqueModelId(baseName, (id) -> dtClient.getDigitalTwin(id));
    }

    public static String GetUniqueModelId(DigitalTwinsAsyncClient dtClient, String baseName)
    {
        return GetUniqueModelId(baseName, (id) -> dtClient.getModel(id).block());
    }

    public static String GetUniqueDigitalTwinId(DigitalTwinsAsyncClient dtClient, String baseName)
    {
        return GetUniqueModelId(baseName, (id) -> dtClient.getDigitalTwin(id).block());
    }

    private static String GetUniqueModelId(String baseName, Consumer<String> getMethod)
    {
        String id;
        Random random = new Random();
        for (int i = 0; i < MaxTries; ++i)
        {
            id = baseName + random.nextInt();
            id = id.length() > MaxIdLength ? id.substring(0, MaxIdLength) : id;
            try
            {
                getMethod.accept(id);
            }
            catch (ErrorResponseException e)
            {
                if (e.getResponse().getStatusCode() == 404)
                {
                    return id;
                }
            }
        }

        throw new AssertionFailedError("Unique Id could not be found with base " + baseName);
    }
}
