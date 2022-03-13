// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.CreateOrReplaceDigitalTwinOptions;
import com.azure.digitaltwins.core.models.DeleteDigitalTwinOptions;
import com.azure.digitaltwins.core.models.UpdateDigitalTwinOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TwinAsyncTests extends TwinTestBase {
    private final ClientLogger logger = new ClientLogger(TwinAsyncTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void digitalTwinLifecycle(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            // Create a Twin
            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();

            // Get a Twin
            StepVerifier.create(asyncClient.getDigitalTwinWithResponse(roomTwinId, String.class))
                .assertNext(getResponse -> {
                    assertEquals(getResponse.getStatusCode(), HttpURLConnection.HTTP_OK);
                    logger.info("Got Twin successfully");

                })
                .verifyComplete();

            // Update Twin
            StepVerifier.create(asyncClient.updateDigitalTwinWithResponse(roomTwinId, TestAssetsHelper.getRoomTwinUpdatePayload(), null))
                .assertNext(updateResponse -> {
                    assertEquals(updateResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
                    logger.info("Updated the twin successfully");
                })
                .verifyComplete();

            // Get Twin and verify update was successful
            StepVerifier.create(asyncClient.getDigitalTwin(roomTwinId, BasicDigitalTwin.class))
                .assertNext(response -> {
                    assertThat(response.getContents().get("Humidity"))
                        .as("Humidity is added")
                        .isEqualTo(30);
                    assertThat(response.getContents().get("Temperature"))
                        .as("Temperature is updated")
                        .isEqualTo(70);
                })
                .verifyComplete();
        } finally {
            try {
                if (roomTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomTwinId).block();
                }
                if (roomModelId != null) {
                    asyncClient.deleteModel(roomModelId).block();
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void twinNotExistThrowsNotFoundException(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String twinId = testResourceNamer.randomUuid();

        StepVerifier.create(asyncClient.getDigitalTwin(twinId, String.class))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createOrReplaceTwinFailsWhenIfNoneMatchStar(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            BasicDigitalTwin twin = deserializeJsonString(roomTwin, BasicDigitalTwin.class);

            // Create a Twin
            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomTwinId, twin, BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();

            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomTwinId, twin, BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();

            StepVerifier.create(
                asyncClient.createOrReplaceDigitalTwinWithResponse(
                    roomTwinId,
                    twin,
                    BasicDigitalTwin.class,
                    new CreateOrReplaceDigitalTwinOptions().setIfNoneMatch("*")))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_PRECON_FAILED));
        } finally {
            try {
                if (roomTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomTwinId).block();
                }
                if (roomModelId != null) {
                    asyncClient.deleteModel(roomModelId).block();
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createOrReplaceTwinSucceedsWhenNoIfNoneHeader(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            BasicDigitalTwin twin = deserializeJsonString(roomTwin, BasicDigitalTwin.class);

            // Create a Twin
            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomTwinId, twin, BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();

            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomTwinId, twin, BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();

            StepVerifier.create(
                asyncClient.createOrReplaceDigitalTwinWithResponse(
                    roomTwinId,
                    twin,
                    BasicDigitalTwin.class,
                    null)) //don't set ifNoneMatch header
                .assertNext(response -> { /* don't care as long as it is a success status code */ })
                .verifyComplete();
        } finally {
            try {
                if (roomTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomTwinId).block();
                }
                if (roomModelId != null) {
                    asyncClient.deleteModel(roomModelId).block();
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchTwinFailsWhenETagDoesNotMatch(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            // Create a Twin
            AtomicReference<String> etagBeforeUpdate = new AtomicReference<>();
            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                    etagBeforeUpdate.set(createdTwin.getETag());
                })
                .verifyComplete();

            // Update Twin
            StepVerifier.create(asyncClient.updateDigitalTwinWithResponse(roomTwinId, TestAssetsHelper.getRoomTwinUpdatePayload(), null))
                .assertNext(updateResponse -> {
                    assertEquals(updateResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
                    logger.info("Updated the twin successfully");
                })
                .verifyComplete();

            StepVerifier.create(
                asyncClient.updateDigitalTwinWithResponse(
                    roomTwinId,
                    TestAssetsHelper.getRoomTwinSecondUpdatePayload(),
                    new UpdateDigitalTwinOptions().setIfMatch(etagBeforeUpdate.get())))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_PRECON_FAILED));
        } finally {
            try {
                if (roomTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomTwinId).block();
                }
                if (roomModelId != null) {
                    asyncClient.deleteModel(roomModelId).block();
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchTwinSucceedsWhenETagMatches(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            // Create a Twin
            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();

            // Update Twin
            AtomicReference<String> updateToDateETag = new AtomicReference<>();
            StepVerifier.create(asyncClient.updateDigitalTwinWithResponse(roomTwinId, TestAssetsHelper.getRoomTwinUpdatePayload(), null))
                .assertNext(updateResponse -> {
                    assertEquals(updateResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
                    logger.info("Updated the twin successfully");
                    updateToDateETag.set(updateResponse.getDeserializedHeaders().getETag());
                })
                .verifyComplete();

            StepVerifier.create(
                asyncClient.updateDigitalTwinWithResponse(
                    roomTwinId,
                    TestAssetsHelper.getRoomTwinSecondUpdatePayload(),
                    new UpdateDigitalTwinOptions().setIfMatch(updateToDateETag.get())))
                .assertNext(response -> { /* don't care as long as it is a success status code */ })
                .verifyComplete();
        } finally {
            try {
                if (roomTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomTwinId).block();
                }
                if (roomModelId != null) {
                    asyncClient.deleteModel(roomModelId).block();
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void deleteTwinFailsWhenETagDoesNotMatch(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            // Create a Twin
            AtomicReference<String> etagBeforeUpdate = new AtomicReference<>();
            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                    etagBeforeUpdate.set(createdTwin.getETag());
                })
                .verifyComplete();

            // Update Twin
            StepVerifier.create(asyncClient.updateDigitalTwinWithResponse(roomTwinId, TestAssetsHelper.getRoomTwinUpdatePayload(), null))
                .assertNext(updateResponse -> {
                    assertEquals(updateResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
                    logger.info("Updated the twin successfully");
                })
                .verifyComplete();

            StepVerifier.create(
                asyncClient.deleteDigitalTwinWithResponse(
                    roomTwinId,
                    new DeleteDigitalTwinOptions().setIfMatch(etagBeforeUpdate.get())))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_PRECON_FAILED));
        } finally {
            try {
                if (roomTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomTwinId).block();
                }
                if (roomModelId != null) {
                    asyncClient.deleteModel(roomModelId).block();
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void deleteTwinSucceedsWhenETagMatches(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            // Create a Twin
            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();

            // Update Twin
            AtomicReference<String> updateToDateETag = new AtomicReference<>();
            StepVerifier.create(asyncClient.updateDigitalTwinWithResponse(roomTwinId, TestAssetsHelper.getRoomTwinUpdatePayload(), null))
                .assertNext(updateResponse -> {
                    assertEquals(updateResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
                    logger.info("Updated the twin successfully");
                    updateToDateETag.set(updateResponse.getDeserializedHeaders().getETag());
                })
                .verifyComplete();

            StepVerifier.create(
                asyncClient.deleteDigitalTwinWithResponse(
                    roomTwinId,
                    new DeleteDigitalTwinOptions().setIfMatch(updateToDateETag.get())))
                .assertNext(response -> { /* don't care as long as it is a success status code */ })
                .verifyComplete();
        } finally {
            try {
                if (roomModelId != null) {
                    asyncClient.deleteModel(roomModelId).block();
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void digitalTwinWithNumericStringProperty(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String floorTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());

        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());
        String hvacModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, asyncClient, getRandomIntegerStringGenerator());

        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        String floorModel = TestAssetsHelper.getFloorModelPayload(floorModelId, roomModelId, hvacModelId);
        String floorTwin = TestAssetsHelper.getFloorTwinPayload(floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel, floorModel));

        try {
            // Create models to test the Twin lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            // Create a Twin
            BasicDigitalTwin floorTwinToCreate = deserializeJsonString(floorTwin, BasicDigitalTwin.class);
            floorTwinToCreate.addToContents("name", "1234");
            floorTwinToCreate.addToContents("roomType", "1234 spacious");

            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(floorTwinId, floorTwinToCreate, BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), floorTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();
        } finally {
            try {
                if (roomModelId != null) {
                    asyncClient.deleteModel(roomModelId).block();
                }
                if (floorTwinId != null) {
                    asyncClient.deleteDigitalTwin(floorTwinId).block();
                }
                if (floorModelId != null) {
                    asyncClient.deleteModel(floorModelId).block();
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }
}
