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

    private BinaryData getRequestBody() {
        BinaryData requestBody = BinaryData.fromString("{\"series\":[{\"timestamp\":\"2018-01-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-02-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-03-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-04-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-05-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-06-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-07-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-08-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-09-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-10-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-11-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-12-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-01-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-02-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-03-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-04-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-05-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-06-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-07-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-08-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-09-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-10-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-11-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-12-19T00:00:00Z\",\"value\":855}],\"granularity\":\"monthly\",\"maxAnomalyRatio\":0.25,\"sensitivity\":95}");
        return requestBody;
    }

    @Test
    public void testDetectEntireSeriesWithResponse() {
        BinaryData request = getRequestBody();
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
    }


    @Test
    public void testLastPoint() {
        BinaryData request = getRequestBody();

        Response<BinaryData> response = getClient().detectUnivariateLastPointWithResponse(request, new RequestOptions());
        System.out.println(response.toString());

        String responseBodyStr = response.getValue().toString();
        JsonObject responseJsonObject = Json.createReader(new StringReader(responseBodyStr)).readObject();
        assertEquals(200, response.getStatusCode());
        assertFalse(responseJsonObject.getBoolean("isAnomaly"));

    }

    @Test
    public void testChangePoint() {
        BinaryData request = getRequestBody();

        Response<BinaryData> response = getClient().detectUnivariateChangePointWithResponse(request, new RequestOptions());
        System.out.println(response.toString());

        String responseBodyStr = response.getValue().toString();
        JsonObject responseJsonObject = Json.createReader(new StringReader(responseBodyStr)).readObject();
        assertEquals(200, response.getStatusCode());
        assertEquals(24, responseJsonObject.getJsonArray("confidenceScores").size());
        assertEquals(24, responseJsonObject.getJsonArray("isChangePoint").size());
    }


}
