// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.digitaltwins.core.models.ListModelsOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sync client implementation of the model tests defined in {@link ModelsTestBase}
 */
public class ModelsTest extends ModelsTestBase {

    private final ClientLogger logger = new ClientLogger(ModelsTest.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void modelLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        // Create some models to test the lifecycle of
        final List<DigitalTwinsModelData> createdModels = new ArrayList<>();
        createModelsRunner(client, (modelsList) -> {
            Iterable<DigitalTwinsModelData> createdModelsResponseList = client.createModels(modelsList);
            createdModelsResponseList.forEach((modelData) -> {
                createdModels.add(modelData);
                logger.info("Created models successfully");
            });
        });

        for (final DigitalTwinsModelData expected : createdModels) {
            // Get the model
            getModelRunner(expected.getModelId(), (modelId) -> {
                DigitalTwinsModelData actual = client.getModel(modelId);
                assertModelDataAreEqual(expected, actual, false);
                logger.info("Model {} matched expectations", modelId);
            });

            // Decommission the model
            decommissionModelRunner(expected.getModelId(), (modelId) -> {
                logger.info("Decommissioning model {}", modelId);
                client.decommissionModel(modelId);
            });

            // Get the model again to see if it was decommissioned as expected
            getModelRunner(expected.getModelId(), (modelId) -> {
                DigitalTwinsModelData actual = client.getModel(modelId);
                assertTrue(actual.isDecommissioned());
                logger.info("Model {} was decommissioned successfully", modelId);
            });

            // Delete the model
            deleteModelRunner(expected.getModelId(), (modelId) -> {
                logger.info("Deleting model {}", modelId);
                client.deleteModel(modelId);
            });
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void getModelThrowsIfModelDoesNotExist(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        final String nonExistentModelId = "dtmi:doesnotexist:fakemodel;1000";
        getModelRunner(nonExistentModelId, (modelId) -> assertRestException(() -> client.getModel(modelId), HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createModelThrowsIfModelAlreadyExists(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        final List<String> modelsToCreate = new ArrayList<>();
        final String wardModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WARD_MODEL_ID, client, getRandomIntegerStringGenerator());
        final String wardModelPayload = TestAssetsHelper.getWardModelPayload(wardModelId);
        modelsToCreate.add(wardModelPayload);

        Iterable<DigitalTwinsModelData> createdModels = client.createModels(modelsToCreate);
        createdModels.forEach(Assertions::assertNotNull);

        assertRestException(
            () -> client.createModels(modelsToCreate),
            HttpURLConnection.HTTP_CONFLICT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void listModelsMultiplePages(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        final List<DigitalTwinsModelData> createdModels = new ArrayList<>();
        createModelsRunner(client, (modelsList) -> {
            Iterable<DigitalTwinsModelData> createdModelsResponseList = client.createModels(modelsList);
            createdModelsResponseList.forEach((modelData) -> {
                createdModels.add(modelData);
                logger.info("Created models successfully");
            });
        });

        createdModels.forEach(Assertions::assertNotNull);

        AtomicInteger pageCount = new AtomicInteger();

        // List models in multiple pages
        client.listModels(new ListModelsOptions().setMaxItemsPerPage(2), Context.NONE)
            .iterableByPage()
            .forEach(digitalTwinsModelDataPagedResponse -> {
                pageCount.getAndIncrement();
                logger.info("content for this page " + pageCount);
                for (DigitalTwinsModelData data : digitalTwinsModelDataPagedResponse.getValue()) {
                    logger.info(data.getModelId());
                }
            });

        assertTrue(pageCount.get() > 1);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void getModelThrowsIfModelIdInvalid(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        final String malformedModelId = "thisIsNotAValidModelId";
        getModelRunner(malformedModelId, (modelId) -> assertRestException(() -> client.getModel(modelId), HttpURLConnection.HTTP_BAD_REQUEST));
    }

    private void createModelsRunner(DigitalTwinsClient client, Consumer<List<String>> createModelsTestRunner) {
        String buildingModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.BUILDING_MODEL_ID, client, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, getRandomIntegerStringGenerator());
        String hvacModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID, client, getRandomIntegerStringGenerator());
        String wardModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WARD_MODEL_ID, client, getRandomIntegerStringGenerator());

        createModelsRunner(buildingModelId, floorModelId, hvacModelId, wardModelId, createModelsTestRunner);
    }
}
