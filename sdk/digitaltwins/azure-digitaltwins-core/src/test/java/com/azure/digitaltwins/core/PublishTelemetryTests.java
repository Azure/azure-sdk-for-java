// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PublishTelemetryTests extends PublishTelemetryTestBase {
    private final ClientLogger logger = new ClientLogger(PublishTelemetryTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void publishTelemetryLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String wifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.WIFI_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomWithWifiModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_WITH_WIFI_MODEL_ID_PREFIX, client, getRandomIntegerStringGenerator());
        String roomWithWifiTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_WITH_WIFI_TWIN_ID_PREFIX, client, getRandomIntegerStringGenerator());

        try {
            // Create models needed for the digital twin
            createModelsAndTwins(client, wifiModelId, roomWithWifiModelId, roomWithWifiTwinId);

            // Act

            Response<Void> publishTelemetryResponse = client.publishTelemetryWithResponse(
                roomWithWifiTwinId,
                testResourceNamer.randomUuid(),
                "{\"Telemetry1\": 5}",
                null,
                Context.NONE);

            assertThat(publishTelemetryResponse.getStatusCode())
                .as("Publish telemetry succeeds.")
                .isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);


            Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
            telemetryPayload.put("ComponentTelemetry1", 9);

            Response<Void> publishComponentTelemetryResponse = client.publishComponentTelemetryWithResponse(
                roomWithWifiTwinId,
                TestAssetDefaults.WIFI_COMPONENT_NAME,
                testResourceNamer.randomUuid(),
                telemetryPayload,
                null,
                Context.NONE);

            assertThat(publishComponentTelemetryResponse.getStatusCode())
                .as("Publish component telemetry succeeds.")
                .isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);
        } catch (Exception ex) {
            fail("Failure in executing a step in the test case", ex);
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
                fail("Test cleanup failed", ex);
            }

        }
    }

    private void createModelsAndTwins(DigitalTwinsClient client, String wifiModelId, String roomWithWifiModelId, String roomWithWifiTwinId) {
        String wifiModelPayload = TestAssetsHelper.getWifiModelPayload(wifiModelId);
        String roomWithWifiModelPayload = TestAssetsHelper.getRoomWithWifiModelPayload(roomWithWifiModelId, wifiModelId, TestAssetDefaults.WIFI_COMPONENT_NAME);

        client.createModels(new ArrayList<>(Arrays.asList(wifiModelPayload, roomWithWifiModelPayload)));

        String roomWithWifiTwinPayload = TestAssetsHelper.getRoomWithWifiTwinPayload(roomWithWifiModelId, TestAssetDefaults.WIFI_COMPONENT_NAME);

        client.createOrReplaceDigitalTwin(roomWithWifiTwinId, roomWithWifiTwinPayload, String.class);
    }
}
