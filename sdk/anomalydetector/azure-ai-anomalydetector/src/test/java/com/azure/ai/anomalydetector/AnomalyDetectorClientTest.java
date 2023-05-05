// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.UnivariateDetectionOptions;
import com.azure.ai.anomalydetector.models.UnivariateEntireDetectionResult;
import com.azure.ai.anomalydetector.models.UnivariateLastDetectionResult;
import com.azure.ai.anomalydetector.models.UnivariateChangePointDetectionOptions;
import com.azure.ai.anomalydetector.models.UnivariateChangePointDetectionResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;


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
        UnivariateDetectionOptions options = request.toObject(UnivariateDetectionOptions.class);
        UnivariateEntireDetectionResult result = getClient().detectUnivariateEntireSeries(options);

        assertEquals(24, result.getExpectedValues().size());
        assertEquals(24, result.getIsAnomaly().size());
        assertEquals(24, result.getIsPositiveAnomaly().size());
        assertEquals(24, result.getLowerMargins().size());
        assertEquals(24, result.getSeverity().size());
        assertEquals(24, result.getSeverity().size());
        assertEquals(24, result.getUpperMargins().size());
        for (Boolean isA : result.getIsAnomaly()) {
            assertFalse(isA);
        }
        for (Boolean isPA : result.getIsPositiveAnomaly()) {
            assertFalse(isPA);
        }
    }


    @Test
    public void testLastPoint() {
        BinaryData request = getRequestBody();

        UnivariateDetectionOptions options = request.toObject(UnivariateDetectionOptions.class);
        UnivariateLastDetectionResult result = getClient().detectUnivariateLastPoint(options);
        assertFalse(result.isAnomaly());
    }

    @Test
    public void testChangePoint() {
        BinaryData request = getRequestBody();

        UnivariateChangePointDetectionOptions options = request.toObject(UnivariateChangePointDetectionOptions.class);
        UnivariateChangePointDetectionResult result = getClient().detectUnivariateChangePoint(options);
        assertEquals(24, result.getConfidenceScores().size());
        assertEquals(24, result.getIsChangePoint().size());
    }


}
