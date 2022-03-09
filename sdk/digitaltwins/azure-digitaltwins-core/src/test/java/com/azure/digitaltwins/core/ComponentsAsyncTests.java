// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.UpdateComponentOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComponentsAsyncTests extends ComponentsTestBase {

    private final ClientLogger logger = new ClientLogger(ComponentsAsyncTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void componentLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String wifiComponentName = "wifiAccessPoint";

        String roomWithWifiTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_WITH_WIFI_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String roomWithWifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_WITH_WIFI_MODEL_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String wifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WIFI_MODEL_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());

        String modelWifi = TestAssetsHelper.getWifiModelPayload(wifiModelId);
        String modelRoomWithWifi = TestAssetsHelper.getRoomWithWifiModelPayload(roomWithWifiModelId, wifiModelId, wifiComponentName);
        String roomWithWifiTwin = TestAssetsHelper.getRoomWithWifiTwinPayload(roomWithWifiModelId, wifiComponentName);
        List<String> modelsList = new ArrayList<>(Arrays.asList(modelWifi, modelRoomWithWifi));

        try {
            // Create models and components to test the lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            StepVerifier.create(asyncClient.createOrReplaceDigitalTwin(roomWithWifiTwinId, deserializeJsonString(roomWithWifiTwin, BasicDigitalTwin.class), BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomWithWifiTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();

            StepVerifier.create(asyncClient.getComponentWithResponse(roomWithWifiTwinId, wifiComponentName, String.class))
                .assertNext(createResponse -> {
                    assertEquals(createResponse.getStatusCode(), HttpURLConnection.HTTP_OK);
                    logger.info("Got component successfully");

                })
                .verifyComplete();

            StepVerifier.create(asyncClient.updateComponentWithResponse(roomWithWifiTwinId, wifiComponentName, TestAssetsHelper.getWifiComponentUpdatePayload(), null))
                .assertNext(updateResponse -> {
                    assertEquals(updateResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
                    logger.info("Updated the component successfully");
                })
                .verifyComplete();
        } finally {
            try {
                if (roomWithWifiTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomWithWifiTwinId).block();
                }
                if (roomWithWifiModelId != null) {
                    asyncClient.deleteModel(roomWithWifiModelId).block();
                }
                if (wifiModelId != null) {
                    asyncClient.deleteModel(wifiModelId).block();
                }
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchComponentFailsIfETagDoesNotMatch(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String wifiComponentName = "wifiAccessPoint";

        String roomWithWifiTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_WITH_WIFI_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String roomWithWifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_WITH_WIFI_MODEL_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String wifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WIFI_MODEL_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());

        String modelWifi = TestAssetsHelper.getWifiModelPayload(wifiModelId);
        String modelRoomWithWifi = TestAssetsHelper.getRoomWithWifiModelPayload(roomWithWifiModelId, wifiModelId, wifiComponentName);
        String roomWithWifiTwin = TestAssetsHelper.getRoomWithWifiTwinPayload(roomWithWifiModelId, wifiComponentName);
        List<String> modelsList = new ArrayList<>(Arrays.asList(modelWifi, modelRoomWithWifi));

        try {
            // Create models and components to test the lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            AtomicReference<String> etagBeforeUpdate = new AtomicReference<>();
            StepVerifier.create(asyncClient.createOrReplaceDigitalTwinWithResponse(
                roomWithWifiTwinId,
                deserializeJsonString(roomWithWifiTwin, BasicDigitalTwin.class),
                BasicDigitalTwin.class,
                null))
                .assertNext(createdTwinResponse -> {
                    etagBeforeUpdate.set(createdTwinResponse.getDeserializedHeaders().getETag());
                })
                .verifyComplete();

            StepVerifier.create(asyncClient.updateComponentWithResponse(roomWithWifiTwinId, wifiComponentName, TestAssetsHelper.getWifiComponentUpdatePayload(), null))
                .assertNext(updateResponse -> {
                    logger.info("Updated the component successfully");
                })
                .verifyComplete();

            // Update the component again, but with the out of date etag
            StepVerifier.create(
                asyncClient.updateComponentWithResponse(
                    roomWithWifiTwinId,
                    wifiComponentName,
                    TestAssetsHelper.getWifiComponentSecondUpdatePayload(),
                    new UpdateComponentOptions().setIfMatch(etagBeforeUpdate.get())))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_PRECON_FAILED));
        } finally {
            try {
                if (roomWithWifiTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomWithWifiTwinId).block();
                }
                if (roomWithWifiModelId != null) {
                    asyncClient.deleteModel(roomWithWifiModelId).block();
                }
                if (wifiModelId != null) {
                    asyncClient.deleteModel(wifiModelId).block();
                }
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchComponentSucceedsIfETagMatches(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String wifiComponentName = "wifiAccessPoint";

        String roomWithWifiTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_WITH_WIFI_TWIN_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String roomWithWifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_WITH_WIFI_MODEL_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());
        String wifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WIFI_MODEL_ID_PREFIX, asyncClient, getRandomIntegerStringGenerator());

        String modelWifi = TestAssetsHelper.getWifiModelPayload(wifiModelId);
        String modelRoomWithWifi = TestAssetsHelper.getRoomWithWifiModelPayload(roomWithWifiModelId, wifiModelId, wifiComponentName);
        String roomWithWifiTwin = TestAssetsHelper.getRoomWithWifiTwinPayload(roomWithWifiModelId, wifiComponentName);
        List<String> modelsList = new ArrayList<>(Arrays.asList(modelWifi, modelRoomWithWifi));

        try {
            // Create models and components to test the lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created models successfully"))
                .verifyComplete();

            StepVerifier
                .create(asyncClient.createOrReplaceDigitalTwinWithResponse(
                    roomWithWifiTwinId,
                    deserializeJsonString(roomWithWifiTwin, BasicDigitalTwin.class),
                    BasicDigitalTwin.class,
                    null))
                .assertNext(createdTwinResponse -> {
                    logger.info("Updated the component successfully");
                })
                .verifyComplete();

            AtomicReference<String> upToDateETag = new AtomicReference<>();
            StepVerifier
                .create(
                    asyncClient.updateComponentWithResponse(
                        roomWithWifiTwinId,
                        wifiComponentName,
                        TestAssetsHelper.getWifiComponentUpdatePayload(),
                        null))
                .assertNext(updateResponse -> {
                    upToDateETag.set(updateResponse.getDeserializedHeaders().getETag());
                    logger.info("Updated the component successfully");
                })
                .verifyComplete();

            // Update the component again, but with the out of date etag
            StepVerifier.create(
                asyncClient.updateComponentWithResponse(
                    roomWithWifiTwinId,
                    wifiComponentName,
                    TestAssetsHelper.getWifiComponentSecondUpdatePayload(),
                    new UpdateComponentOptions().setIfMatch(upToDateETag.get())))
                .assertNext(response -> { /* don't care as long as it is a success status code */ })
                .verifyComplete();
        } finally {
            try {
                if (roomWithWifiTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomWithWifiTwinId).block();
                }
                if (roomWithWifiModelId != null) {
                    asyncClient.deleteModel(roomWithWifiModelId).block();
                }
                if (wifiModelId != null) {
                    asyncClient.deleteModel(wifiModelId).block();
                }
            } catch (Exception ex) {
                fail("Test cleanup failed", ex);
            }
        }
    }
}
