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
        assertEquals(expected.getModelId(), actual.getModelId());
        assertEquals(expected.getUploadedOn(), actual.getUploadedOn());

        // ModelData objects that are obtained through the createModels API do not populate the model field, so it isn't
        // worth comparing those ModelData objects with ModelData objects retrieved through getModel calls
        if (compareModel) {
            assertEquals(expected.getDtdlModel(), actual.getDtdlModel());
        }

        assertEquals(expected.getDescriptionLanguageMap().size(), actual.getDescriptionLanguageMap().size());
        for (String key : expected.getDescriptionLanguageMap().keySet()) {
            assertTrue(actual.getDescriptionLanguageMap().containsKey(key));
            assertEquals(expected.getDescriptionLanguageMap().get(key), actual.getDescriptionLanguageMap().get(key));
        }

        assertEquals(expected.getDisplayNameLanguageMap().size(), actual.getDisplayNameLanguageMap().size());
        for (String key : expected.getDisplayNameLanguageMap().keySet()) {
            assertTrue(actual.getDisplayNameLanguageMap().containsKey(key));
            assertEquals(expected.getDisplayNameLanguageMap().get(key), actual.getDisplayNameLanguageMap().get(key));
        }

        assertEquals(expected.isDecommissioned(), actual.isDecommissioned());
    }
}
