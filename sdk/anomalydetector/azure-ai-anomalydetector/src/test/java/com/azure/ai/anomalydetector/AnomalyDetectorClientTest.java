// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.StringReader;

/**
 * Unit tests for {@link AnomalyDetectorClient}.
 */
public class AnomalyDetectorClientTest extends AnomalyDetectorClientTestBase {

    private AnomalyDetectorClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void testDetect() {
        testDetectEntireSeriesWithResponse(request -> {

            Response<BinaryData> response = getClient().detectUnivariateEntireSeriesWithResponse(request, new RequestOptions());
            System.out.println(response.toString());

            String responseBodyStr = response.getValue().toString();
            JsonObject responseJsonObject = Json.createReader(new StringReader(responseBodyStr)).readObject();

            assertEquals(200, response.getStatusCode());
            assertEquals(24, responseJsonObject.getJsonArray("expectedValues").size());
            assertEquals(24, responseJsonObject.getJsonArray("isAnomaly").size());
            assertEquals(24, responseJsonObject.getJsonArray("isPositiveAnomaly").size());
            assertEquals(24, responseJsonObject.getJsonArray("lowerMargins").size());
            assertEquals(24, responseJsonObject.getJsonArray("severity").size());
            assertEquals(24, responseJsonObject.getJsonArray("upperMargins").size());

            JsonArray isAnomalyJA = responseJsonObject.getJsonArray("isAnomaly");
            for (int i = 0; i < isAnomalyJA.size(); i++) {
                assertFalse(isAnomalyJA.getBoolean(i));
            }
            JsonArray isPositiveAnomalyJA = responseJsonObject.getJsonArray("isPositiveAnomaly");
            for (int i = 0; i < isPositiveAnomalyJA.size(); i++) {
                assertFalse(isPositiveAnomalyJA.getBoolean(i));
            }

        });

    }
}
