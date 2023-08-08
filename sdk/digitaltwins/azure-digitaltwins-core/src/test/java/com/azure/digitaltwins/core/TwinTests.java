// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.models.CreateOrReplaceDigitalTwinOptions;
import com.azure.digitaltwins.core.models.DeleteDigitalTwinOptions;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.digitaltwins.core.models.DigitalTwinsResponse;
import com.azure.digitaltwins.core.models.UpdateDigitalTwinOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static java.net.HttpURLConnection.HTTP_PRECON_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class TwinTests extends TwinTestBase {

    private final ClientLogger logger = new ClientLogger(TwinTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void digitalTwinLifecycle(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, client, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            Iterable<DigitalTwinsModelData> createdList = client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin createdRoomTwin = client.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdRoomTwin.getId());
            assertEquals(createdRoomTwin.getId(), roomTwinId);

            // Get Twin.
            DigitalTwinsResponse<String> getTwinResponse = client.getDigitalTwinWithResponse(roomTwinId, String.class, Context.NONE);
            assertEquals(getTwinResponse.getStatusCode(), HttpURLConnection.HTTP_OK);

            // Update Twin.
            DigitalTwinsResponse<Void> updateTwinResponse = client.updateDigitalTwinWithResponse(
                roomTwinId,
                TestAssetsHelper.getRoomTwinUpdatePayload(),
                null,
                Context.NONE);

            assertEquals(updateTwinResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

            BasicDigitalTwin getTwinObject = client.getDigitalTwin(roomTwinId, BasicDigitalTwin.class);

            assertThat(getTwinObject.getContents().get("Humidity"))
                .as("Humidity is added")
                .isEqualTo(30);

            assertThat(getTwinObject.getContents().get("Temperature"))
                .as("Temperature is updated")
                .isEqualTo(70);

        } finally {
            try {
                if (roomTwinId != null) {
                    client.deleteDigitalTwin(roomTwinId);
                }
                if (roomModelId != null) {
                    client.deleteModel(roomModelId);
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
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        String twinId = testResourceNamer.randomUuid();

        assertRestException(() -> client.getDigitalTwin(twinId, String.class), HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createOrReplaceTwinFailsWhenIfNoneMatchStar(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, client, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin twin = deserializeJsonString(roomTwin, BasicDigitalTwin.class);

            client.createOrReplaceDigitalTwin(
                roomTwinId,
                twin,
                BasicDigitalTwin.class);

            assertRestException(
                () -> client.createOrReplaceDigitalTwinWithResponse(
                    roomTwinId,
                    twin,
                    BasicDigitalTwin.class,
                    new CreateOrReplaceDigitalTwinOptions().setIfNoneMatch("*"),
                    Context.NONE),
                HTTP_PRECON_FAILED
            );
        } finally {
            try {
                if (roomTwinId != null) {
                    client.deleteDigitalTwin(roomTwinId);
                }
                if (roomModelId != null) {
                    client.deleteModel(roomModelId);
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
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, client, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin twin = deserializeJsonString(roomTwin, BasicDigitalTwin.class);

            client.createOrReplaceDigitalTwin(
                roomTwinId,
                twin,
                BasicDigitalTwin.class);

            try {
                client.createOrReplaceDigitalTwinWithResponse(
                    roomTwinId,
                    twin,
                    BasicDigitalTwin.class,
                    null, //don't send ifNoneMatch header
                    Context.NONE);
            } catch (ErrorResponseException ex) {
                if (ex.getResponse().getStatusCode() == HTTP_PRECON_FAILED) {
                    fail("Should not have gotten a 412 error since ifNoneMatch header was not sent", ex);
                } else {
                    throw ex;
                }
            }
        } finally {
            try {
                if (roomTwinId != null) {
                    client.deleteDigitalTwin(roomTwinId);
                }
                if (roomModelId != null) {
                    client.deleteModel(roomModelId);
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
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, client, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdTwin.getId());

            String etagBeforeUpdate = createdTwin.getETag();
            assertNotNull(etagBeforeUpdate);

            // Update Twin.
            client.updateDigitalTwinWithResponse(
                roomTwinId,
                TestAssetsHelper.getRoomTwinUpdatePayload(),
                null,
                Context.NONE);

            assertRestException(
                () -> client.updateDigitalTwinWithResponse(
                    roomTwinId,
                    TestAssetsHelper.getRoomTwinSecondUpdatePayload(),
                    new UpdateDigitalTwinOptions().setIfMatch(etagBeforeUpdate),
                    Context.NONE),
                HTTP_PRECON_FAILED);
        } finally {
            try {
                if (roomTwinId != null) {
                    client.deleteDigitalTwin(roomTwinId);
                }
                if (roomModelId != null) {
                    client.deleteModel(roomModelId);
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
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, client, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdTwin.getId());

            // Update Twin.
            DigitalTwinsResponse<Void> response = client.updateDigitalTwinWithResponse(
                roomTwinId,
                TestAssetsHelper.getRoomTwinUpdatePayload(),
                null,
                Context.NONE);

            String updateToDateETag = response.getDeserializedHeaders().getETag();
            assertNotNull(updateToDateETag);

            try {
                client.updateDigitalTwinWithResponse(
                    roomTwinId,
                    TestAssetsHelper.getRoomTwinSecondUpdatePayload(),
                    new UpdateDigitalTwinOptions().setIfMatch(updateToDateETag),
                    Context.NONE);
            } catch (ErrorResponseException ex) {
                if (ex.getResponse().getStatusCode() == HTTP_PRECON_FAILED) {
                    fail("Should not have gotten a 412 error since ifMatch header had up to date etag", ex);
                } else {
                    throw ex;
                }
            }
        } finally {
            try {
                if (roomTwinId != null) {
                    client.deleteDigitalTwin(roomTwinId);
                }
                if (roomModelId != null) {
                    client.deleteModel(roomModelId);
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
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, client, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdTwin.getId());

            String etagBeforeUpdate = createdTwin.getETag();
            assertNotNull(etagBeforeUpdate);

            // Update Twin.
            client.updateDigitalTwinWithResponse(
                roomTwinId,
                TestAssetsHelper.getRoomTwinUpdatePayload(),
                null,
                Context.NONE);

            assertRestException(
                () -> client.deleteDigitalTwinWithResponse(
                    roomTwinId,
                    new DeleteDigitalTwinOptions().setIfMatch(etagBeforeUpdate),
                    Context.NONE),
                HTTP_PRECON_FAILED);
        } finally {
            try {
                if (roomTwinId != null) {
                    client.deleteDigitalTwin(roomTwinId);
                }

                if (roomModelId != null) {
                    client.deleteModel(roomModelId);
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
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, client, getRandomIntegerStringGenerator());

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(roomTwinId, deserializeJsonString(roomTwin, BasicDigitalTwin.class), BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdTwin.getId());

            // Update Twin.
            DigitalTwinsResponse<Void> response = client.updateDigitalTwinWithResponse(
                roomTwinId,
                TestAssetsHelper.getRoomTwinUpdatePayload(),
                null,
                Context.NONE);

            String updateToDateETag = response.getDeserializedHeaders().getETag();
            assertNotNull(updateToDateETag);

            try {
                client.deleteDigitalTwinWithResponse(
                    roomTwinId,
                    new DeleteDigitalTwinOptions().setIfMatch(updateToDateETag),
                    Context.NONE);
            } catch (ErrorResponseException ex) {
                //If the delete above failed, delete without specifying an ifMatch header
                client.deleteDigitalTwin(roomTwinId);

                if (ex.getResponse().getStatusCode() == HTTP_PRECON_FAILED) {
                    fail("Should not have gotten a 412 error since ifMatch header had up to date etag", ex);
                } else {
                    throw ex;
                }
            }
        } finally {
            try {
                if (roomModelId != null) {
                    client.deleteModel(roomModelId);
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
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.FLOOR_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, getRandomIntegerStringGenerator());
        String hvacModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.HVAC_MODEL_ID, client, getRandomIntegerStringGenerator());
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, client, getRandomIntegerStringGenerator());

        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        String floorModel = TestAssetsHelper.getFloorModelPayload(floorModelId, roomModelId, hvacModelId);
        String floorTwin = TestAssetsHelper.getFloorTwinPayload(floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel, floorModel));

        try {
            // Create models to test the Twin lifecycle.
            client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin floorTwinToCreate = deserializeJsonString(floorTwin, BasicDigitalTwin.class);
            floorTwinToCreate.addToContents("name", "1234");
            floorTwinToCreate.addToContents("roomType", "1234 spacious");

            BasicDigitalTwin createdFloorTwin = client.createOrReplaceDigitalTwin(floorTwinId, floorTwinToCreate, BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdFloorTwin.getId());
            assertEquals(createdFloorTwin.getId(), floorTwinId);
        } finally {
            try {
                if (roomModelId != null) {
                    client.deleteModel(roomModelId);
                }
                if (floorTwinId != null) {
                    client.deleteDigitalTwin(floorTwinId);
                }
                if (floorModelId != null) {
                    client.deleteModel(floorModelId);
                }

            } catch (Exception ex) {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }
}
