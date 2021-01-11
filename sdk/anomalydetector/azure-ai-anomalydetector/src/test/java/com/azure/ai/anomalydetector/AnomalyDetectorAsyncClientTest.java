// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link AnomalyDetectorAsyncClient}.
 */
public class AnomalyDetectorAsyncClientTest extends AnomalyDetectorClientTestBase {

    private AnomalyDetectorAsyncClient getClient() {
        return getClientBuilder().buildAsyncClient();
    }

    @Test
    public void testDetect() {
        testDetectEntireSeriesWithResponse(request -> {
            StepVerifier.create(getClient().detectEntireSeriesWithResponse(request)).assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(24, response.getValue().getExpectedValues().size());
                assertEquals(24, response.getValue().getIsAnomaly().size());
                assertEquals(24, response.getValue().getIsPositiveAnomaly().size());
                assertEquals(24, response.getValue().getIsPositiveAnomaly().size());

                response.getValue().getIsAnomaly().forEach(Assertions::assertFalse);
                response.getValue().getIsPositiveAnomaly().forEach(Assertions::assertFalse);
                response.getValue().getIsPositiveAnomaly().forEach(Assertions::assertFalse);
            }).verifyComplete();
        });
    }

}
