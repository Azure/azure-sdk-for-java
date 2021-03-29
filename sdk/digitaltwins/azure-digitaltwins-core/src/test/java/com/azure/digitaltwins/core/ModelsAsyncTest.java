package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.ListModelsOptions;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Async client implementation of the model tests defined in {@link ModelsTestBase}
 */
public class ModelsAsyncTest extends ModelsTestBase {

    private final ClientLogger logger = new ClientLogger(ModelsAsyncTest.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void modelLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        // Create some models to test the lifecycle of
        List<DigitalTwinsModelData> createdModels = new ArrayList<>();
        createModelsRunner(asyncClient, (modelsList) -> StepVerifier.create(asyncClient.createModels(modelsList))
            .assertNext(createdModelsResponseList -> {
                createdModelsResponseList.forEach(item -> createdModels.add(item));
                logger.info("Created {} models successfully");
            })
            .verifyComplete());

        for (final DigitalTwinsModelData expected : createdModels) {
            // Get the model
            getModelRunner(expected.getModelId(), (modelId) -> {
                StepVerifier.create(asyncClient.getModelWithResponse(modelId))
                    .assertNext(retrievedModel -> assertModelDataAreEqual(expected, retrievedModel.getValue(), false))
                    .verifyComplete();
                logger.info("Model {} matched expectations", modelId);
            });

            // Decommission the model
            decommissionModelRunner(expected.getModelId(), (modelId) -> {
                logger.info("Decommissioning model {}", modelId);
                StepVerifier.create(asyncClient.decommissionModel(modelId))
                    .verifyComplete();
            });

            // Get the model again to see if it was decommissioned as expected
            getModelRunner(expected.getModelId(), (modelId) -> {
                StepVerifier.create(asyncClient.getModel(modelId))
                    .assertNext(retrievedModel -> assertTrue(retrievedModel.isDecommissioned()))
                    .verifyComplete();
                logger.info("Model {} was decommissioned successfully", modelId);
            });

            // Delete the model
            deleteModelRunner(expected.getModelId(), (modelId) -> {
                logger.info("Deleting model {}", modelId);
                StepVerifier.create(asyncClient.deleteModel(modelId))
                    .verifyComplete();
            });
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void getModelThrowsIfModelDoesNotExist(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        final String nonExistentModelId = "dtmi:doesnotexist:fakemodel;1000";
        StepVerifier.create(asyncClient.getModel(nonExistentModelId))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createModelThrowsIfModelAlreadyExists(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        final List<String> modelsToCreate = new ArrayList<>();
        final String wardModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WARD_MODEL_ID, asyncClient, this.randomIntegerStringGenerator);
        final String wardModelPayload = TestAssetsHelper.getWardModelPayload(wardModelId);
        modelsToCreate.add(wardModelPayload);

        StepVerifier.create(asyncClient.createModels(modelsToCreate))
            .assertNext((Assertions::assertNotNull))
            .verifyComplete();

        StepVerifier.create(asyncClient.createModels(modelsToCreate))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_CONFLICT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void listModelsMultiplePages(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        // Create some models
        List<DigitalTwinsModelData> createdModels = new ArrayList<>();
        createModelsRunner(asyncClient, (modelsList) -> StepVerifier.create(asyncClient.createModels(modelsList))
            .assertNext(createdModelsResponseList -> {
                createdModelsResponseList.forEach(item -> createdModels.add(item));
                logger.info("Created models successfully");
            })
            .verifyComplete());

        createdModels.forEach(Assertions::assertNotNull);

        AtomicInteger pageCount = new AtomicInteger();

        // List models in multiple pages and verify more than one page was viewed.
        StepVerifier.create(asyncClient.listModels(new ListModelsOptions().setMaxItemsPerPage(2)).byPage())
            .thenConsumeWhile(
                page -> {
                    pageCount.getAndIncrement();
                    logger.info("content for this page " + pageCount);
                    for (DigitalTwinsModelData model : page.getValue()) {
                        logger.info(model.getModelId());
                    }
                    return true;
                })
            .verifyComplete();

        int finalPageCount = pageCount.get();

        assertTrue(finalPageCount > 1);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void getModelThrowsIfModelIdInvalid(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        final String malformedModelId = "thisIsNotAValidModelId";
        StepVerifier.create(asyncClient.getModel(malformedModelId))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    private void createModelsRunner(DigitalTwinsAsyncClient asyncClient, Consumer<List<String>> createModelsTestRunner) {
        String buildingModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.BUILDING_MODEL_ID, asyncClient, this.randomIntegerStringGenerator);
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, this.randomIntegerStringGenerator);
        String hvacModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID, asyncClient, this.randomIntegerStringGenerator);
        String wardModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WARD_MODEL_ID, asyncClient, this.randomIntegerStringGenerator);

        createModelsRunner(buildingModelId, floorModelId, hvacModelId, wardModelId, createModelsTestRunner);
    }
}
