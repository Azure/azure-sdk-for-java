package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This abstract test class defines all the tests that both the sync and async model test classes need to implement. It also
 * houses some model test specific helper functions.
 */
public abstract class ModelsTestBase extends DigitalTwinsTestBase {
    @Test
    public abstract void modelLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    @Test
    public abstract void getModelThrowsIfModelDoesNotExist(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);
    @Test
    public abstract void createModelThrowsIfModelAlreadyExists(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    @Test
    public abstract void listModelsMultiplePages(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    @Test
    public abstract void getModelThrowsIfModelIdInvalid(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    static void createModelsRunner(String buildingModelId, String floorModelId, String hvacModelId, String wardModelId, Consumer<List<String>> createModelsTestRunner) {
        String modelBuilding = TestAssetsHelper.getBuildingModelPayload(buildingModelId, hvacModelId, floorModelId);
        String modelHvac = TestAssetsHelper.getHvacModelPayload(hvacModelId, floorModelId);
        String modelWard = TestAssetsHelper.getWardModelPayload(wardModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(modelBuilding, modelHvac, modelWard));

        createModelsTestRunner.accept(modelsList);
    }

    void getModelRunner(String modelId, Consumer<String> getModelTestRunner) {
        getModelTestRunner.accept(modelId);
    }

    void decommissionModelRunner(String modelId, Consumer<String> decommissionModelRunner) {
        decommissionModelRunner.accept(modelId);
    }

    void deleteModelRunner(String modelId, Consumer<String> deleteModelRunner) {
        deleteModelRunner.accept(modelId);
    }

    static void assertModelDataAreEqual(DigitalTwinsModelData expected, DigitalTwinsModelData actual, boolean compareModel) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUploadTime(), actual.getUploadTime());

        // ModelData objects that are obtained through the createModels API do not populate the model field, so it isn't
        // worth comparing those ModelData objects with ModelData objects retrieved through getModel calls
        if (compareModel) {
            assertEquals(expected.getModel(), actual.getModel());
        }

        assertEquals(expected.getDescription().size(), actual.getDescription().size());
        for (String key : expected.getDescription().keySet()) {
            assertTrue(actual.getDescription().containsKey(key));
            assertEquals(expected.getDescription().get(key), actual.getDescription().get(key));
        }

        assertEquals(expected.getDisplayName().size(), actual.getDisplayName().size());
        for (String key : expected.getDisplayName().keySet()) {
            assertTrue(actual.getDisplayName().containsKey(key));
            assertEquals(expected.getDisplayName().get(key), actual.getDisplayName().get(key));
        }

        assertEquals(expected.isDecommissioned(), actual.isDecommissioned());
    }
}
