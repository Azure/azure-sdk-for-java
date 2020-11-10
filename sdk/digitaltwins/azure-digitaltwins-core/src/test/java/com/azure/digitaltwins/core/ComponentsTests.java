package com.azure.digitaltwins.core;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.http.HttpClient;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.digitaltwins.core.models.DigitalTwinsResponse;
import com.azure.digitaltwins.core.models.UpdateComponentOptions;
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
import static org.junit.jupiter.api.Assertions.*;

public class ComponentsTests extends ComponentsTestBase {

    private final ClientLogger logger = new ClientLogger(ComponentsTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void componentLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String wifiComponentName = "wifiAccessPoint";

        String roomWithWifiTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_WITH_WIFI_TWIN_ID_PREFIX, client, randomIntegerStringGenerator);
        String roomWithWifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_WITH_WIFI_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);
        String wifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WIFI_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);

        String modelWifi = TestAssetsHelper.getWifiModelPayload(wifiModelId);
        String modelRoomWithWifi = TestAssetsHelper.getRoomWithWifiModelPayload(roomWithWifiModelId, wifiModelId, wifiComponentName);
        String roomWithWifiTwin = TestAssetsHelper.getRoomWithWifiTwinPayload(roomWithWifiModelId, wifiComponentName);

        List<String> modelsList = new ArrayList<>(Arrays.asList(modelWifi, modelRoomWithWifi));

        try {
            // Create models and components to test the lifecycle.
            Iterable<DigitalTwinsModelData> createdList = client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(roomWithWifiTwinId, deserializeJsonString(roomWithWifiTwin, BasicDigitalTwin.class), BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdTwin.getId());
            assertEquals(createdTwin.getId(), roomWithWifiTwinId);

            // Get the component
            Response<String> getComponentResponse = client.getComponentWithResponse(roomWithWifiTwinId, wifiComponentName, String.class, Context.NONE);
            assertEquals(getComponentResponse.getStatusCode(), HttpURLConnection.HTTP_OK);

            // Update component
            DigitalTwinsResponse<Void> updateComponentResponse = client.updateComponentWithResponse(
                roomWithWifiTwinId,
                wifiComponentName,
                TestAssetsHelper.getWifiComponentUpdatePayload(),
                null,
                Context.NONE);

            assertEquals(updateComponentResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }
        // clean up
        finally {
            try {
                if (roomWithWifiTwinId != null) {
                    client.deleteDigitalTwin(roomWithWifiTwinId);
                }
                if (roomWithWifiModelId != null) {
                    client.deleteModel(roomWithWifiModelId);
                }
                if (wifiModelId != null) {
                    client.deleteModel(wifiModelId);
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test celanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchComponentFailsIfETagDoesNotMatch(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String wifiComponentName = "wifiAccessPoint";

        String roomWithWifiTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_WITH_WIFI_TWIN_ID_PREFIX, client, randomIntegerStringGenerator);
        String roomWithWifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_WITH_WIFI_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);
        String wifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WIFI_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);

        String modelWifi = TestAssetsHelper.getWifiModelPayload(wifiModelId);
        String modelRoomWithWifi = TestAssetsHelper.getRoomWithWifiModelPayload(roomWithWifiModelId, wifiModelId, wifiComponentName);
        String roomWithWifiTwin = TestAssetsHelper.getRoomWithWifiTwinPayload(roomWithWifiModelId, wifiComponentName);

        List<String> modelsList = new ArrayList<>(Arrays.asList(modelWifi, modelRoomWithWifi));

        try {
            // Create models and components to test the lifecycle.
            client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(roomWithWifiTwinId, deserializeJsonString(roomWithWifiTwin, BasicDigitalTwin.class), BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdTwin.getId());

            // A component does not have its own etag. It uses the digital twin's etag instead for concurrency protection
            String etagBeforeUpdate = createdTwin.getETag();

            // Update component
            client.updateComponentWithResponse(
                roomWithWifiTwinId,
                wifiComponentName,
                TestAssetsHelper.getWifiComponentUpdatePayload(),
                null,
                Context.NONE);

            // Update component, but with the out of date ETag
            assertRestException(
                () -> client.updateComponentWithResponse(
                    roomWithWifiTwinId,
                    wifiComponentName,
                    TestAssetsHelper.getWifiComponentSecondUpdatePayload(),
                    new UpdateComponentOptions().setIfMatch(etagBeforeUpdate),
                    Context.NONE),
                HTTP_PRECON_FAILED
            );
        } finally {
            try {
                if (roomWithWifiTwinId != null) {
                    client.deleteDigitalTwin(roomWithWifiTwinId);
                }
                if (roomWithWifiModelId != null) {
                    client.deleteModel(roomWithWifiModelId);
                }
                if (wifiModelId != null) {
                    client.deleteModel(wifiModelId);
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test celanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void patchComponentSucceedsIfETagMatches(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String wifiComponentName = "wifiAccessPoint";

        String roomWithWifiTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_WITH_WIFI_TWIN_ID_PREFIX, client, randomIntegerStringGenerator);
        String roomWithWifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_WITH_WIFI_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);
        String wifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WIFI_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);

        String modelWifi = TestAssetsHelper.getWifiModelPayload(wifiModelId);
        String modelRoomWithWifi = TestAssetsHelper.getRoomWithWifiModelPayload(roomWithWifiModelId, wifiModelId, wifiComponentName);
        String roomWithWifiTwin = TestAssetsHelper.getRoomWithWifiTwinPayload(roomWithWifiModelId, wifiComponentName);

        List<String> modelsList = new ArrayList<>(Arrays.asList(modelWifi, modelRoomWithWifi));

        try {
            // Create models and components to test the lifecycle.
            client.createModels(modelsList);
            logger.info("Created models successfully");

            BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(roomWithWifiTwinId, deserializeJsonString(roomWithWifiTwin, BasicDigitalTwin.class), BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdTwin.getId());

            // Update component
            DigitalTwinsResponse<Void> updateComponentResponse = client.updateComponentWithResponse(
                roomWithWifiTwinId,
                wifiComponentName,
                TestAssetsHelper.getWifiComponentUpdatePayload(),
                null,
                Context.NONE);

            // A component does not have its own etag. It uses the digital twin's etag instead for concurrency protection
            String upToDateETag = updateComponentResponse.getDeserializedHeaders().getETag();

            // Update component, but with the out of date ETag
            try {
                client.updateComponentWithResponse(
                    roomWithWifiTwinId,
                    wifiComponentName,
                    TestAssetsHelper.getWifiComponentSecondUpdatePayload(),
                    new UpdateComponentOptions().setIfMatch(upToDateETag),
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
                if (roomWithWifiTwinId != null) {
                    client.deleteDigitalTwin(roomWithWifiTwinId);
                }
                if (roomWithWifiModelId != null) {
                    client.deleteModel(roomWithWifiModelId);
                }
                if (wifiModelId != null) {
                    client.deleteModel(wifiModelId);
                }
            } catch (Exception ex) {
                throw new AssertionFailedError("Test celanup failed", ex);
            }
        }
    }
}
