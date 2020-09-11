package com.azure.digitaltwins.core;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.http.HttpClient;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.ModelData;
import com.azure.digitaltwins.core.models.BasicDigitalTwin;
import com.azure.digitaltwins.core.models.DigitalTwinsResponse;
import com.azure.digitaltwins.core.models.UpdateComponentRequestOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

public class ComponentsTests extends ComponentsTestBase {

    private final ClientLogger logger = new ClientLogger(ComponentsTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void componentLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
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
            List<ModelData> createdList = client.createModels(modelsList);
            logger.info("Created {} models successfully", createdList.size());

            BasicDigitalTwin createdTwin = client.createDigitalTwin(roomWithWifiTwinId, roomWithWifiTwin,BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdTwin.getId());
            assertEquals(createdTwin.getId(), roomWithWifiTwinId);

            // Get the component
            Response<String> getComponentResponse = client.getComponentWithResponse(roomWithWifiTwinId, wifiComponentName, Context.NONE);
            assertEquals(getComponentResponse.getStatusCode(), HttpURLConnection.HTTP_OK);

            // Update component
            DigitalTwinsResponse<Void> updateComponentResponse = client.updateComponentWithResponse(
                roomWithWifiTwinId,
                wifiComponentName,
                TestAssetsHelper.getWifiComponentUpdatePayload(),
                new UpdateComponentRequestOptions(),
                Context.NONE);

            assertEquals(updateComponentResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }
        // clean up
        finally {
            try
            {
                if (roomWithWifiTwinId != null)
                {
                    client.deleteDigitalTwin(roomWithWifiTwinId);
                }
                if (roomWithWifiModelId != null)
                {
                    client.deleteModel(roomWithWifiModelId);
                }
                if (wifiModelId != null)
                {
                    client.deleteModel(wifiModelId);
                }
            }
            catch (Exception ex)
            {
                throw new AssertionFailedError("Test celanup failed", ex);
            }
        }
    }
}
