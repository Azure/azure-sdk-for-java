// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.azure.ai.anomalydetector.models.EntireDetectResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
            Response<EntireDetectResponse> response = getClient()
                .detectEntireSeriesWithResponse(request, Context.NONE);
            assertEquals(200, response.getStatusCode());
            assertEquals(24, response.getValue().getExpectedValues().size());
            assertEquals(24, response.getValue().getIsAnomaly().size());
            assertEquals(24, response.getValue().getIsPositiveAnomaly().size());
            assertEquals(24, response.getValue().getIsPositiveAnomaly().size());

            response.getValue().getIsAnomaly().forEach(Assertions::assertFalse);
            response.getValue().getIsPositiveAnomaly().forEach(Assertions::assertFalse);
            response.getValue().getIsPositiveAnomaly().forEach(Assertions::assertFalse);
        });
    }
}
