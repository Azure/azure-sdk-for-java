// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.UnivariateEntireDetectionResult;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link UnivariateClient}.
 */
public class AnomalyDetectorClientTest extends AnomalyDetectorClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(AnomalyDetectorClientTest.class);

    private UnivariateClient getClient() {
        return getClientBuilder().buildUnivariateClient();
    }

    @Test
    public void testDetect() {
        testDetectEntireSeriesWithResponse(request -> {
            Response<BinaryData> response = getClient().detectUnivariateEntireSeriesWithResponse(request,
                new RequestOptions());
            LOGGER.log(LogLevel.INFORMATIONAL, response::toString);

            String responseBodyStr = response.getValue().toString();

            UnivariateEntireDetectionResult result;
            try (JsonReader jsonReader = JsonProviders.createReader(responseBodyStr)) {
                result = UnivariateEntireDetectionResult.fromJson(jsonReader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            assertEquals(200, response.getStatusCode());
            assertEquals(24, result.getExpectedValues().size());
            assertEquals(24, result.getIsAnomaly().size());
            assertEquals(24, result.getIsPositiveAnomaly().size());
            assertEquals(24, result.getLowerMargins().size());
            assertEquals(24, result.getSeverity().size());
            assertEquals(24, result.getUpperMargins().size());

            result.getIsAnomaly().forEach(Assertions::assertFalse);
            result.getIsPositiveAnomaly().forEach(Assertions::assertFalse);
        });
    }
}
