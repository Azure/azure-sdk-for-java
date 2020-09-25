package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.BasicDigitalTwin;
import com.azure.digitaltwins.core.models.UpdateComponentRequestOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.*;
import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;

public class ComponentsAsyncTests extends ComponentsTestBase {

    private final ClientLogger logger = new ClientLogger(ComponentsAsyncTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void componentLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String wifiComponentName = "wifiAccessPoint";

        String roomWithWifiTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_WITH_WIFI_TWIN_ID_PREFIX, asyncClient, randomIntegerStringGenerator);
        String roomWithWifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_WITH_WIFI_MODEL_ID_PREFIX, asyncClient, randomIntegerStringGenerator);
        String wifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WIFI_MODEL_ID_PREFIX, asyncClient, randomIntegerStringGenerator);

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

            StepVerifier.create(asyncClient.createDigitalTwin(roomWithWifiTwinId, deserializeJsonString(roomWithWifiTwin, BasicDigitalTwin.class), BasicDigitalTwin.class))
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

            StepVerifier.create(asyncClient.updateComponentWithResponse(roomWithWifiTwinId, wifiComponentName, TestAssetsHelper.getWifiComponentUpdatePayload(), new UpdateComponentRequestOptions()))
                .assertNext(updateResponse -> {
                    assertEquals(updateResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
                    logger.info("Updated the component successfully");
                })
                .verifyComplete();
        }
        finally {
            try
            {
                if (roomWithWifiTwinId != null)
                {
                    asyncClient.deleteDigitalTwin(roomWithWifiTwinId).block();
                }
                if (roomWithWifiModelId != null)
                {
                    asyncClient.deleteModel(roomWithWifiModelId).block();
                }
                if (wifiModelId != null)
                {
                    asyncClient.deleteModel(wifiModelId).block();
                }
            }
            catch (Exception ex)
            {
                fail("Test cleanup failed", ex);
            }
        }
    }
}
