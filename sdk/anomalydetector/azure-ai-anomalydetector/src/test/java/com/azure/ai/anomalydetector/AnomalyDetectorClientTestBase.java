// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.util.function.Consumer;

/**
 * Base class for Anomaly Detector clients test.
 */
public class AnomalyDetectorClientTestBase extends TestProxyTestBase {
    void testDetectEntireSeriesWithResponse(Consumer<BinaryData> testRunner) {
        testRunner.accept(getDetectRequest());
    }

    AnomalyDetectorClientBuilder getClientBuilder() {
        AnomalyDetectorClientBuilder builder
            = new AnomalyDetectorClientBuilder().endpoint(getEndpoint()).credential(new AzureKeyCredential(getKey()));

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    private String getKey() {
        return interceptorManager.isPlaybackMode()
            ? "fakeKeyPlaceholder"
            : Configuration.getGlobalConfiguration().get("ANOMALY_DETECTOR_API_KEY");
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("ANOMALY_DETECTOR_ENDPOINT");
    }

    private BinaryData getDetectRequest() {
        return BinaryData.fromString("{\"series\":[{\"timestamp\":\"2018-01-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-02-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-03-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-04-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-05-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-06-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-07-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-08-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-09-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-10-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-11-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2018-12-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-01-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-02-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-03-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-04-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-05-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-06-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-07-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-08-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-09-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-10-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-11-19T00:00:00Z\",\"value\":855},"
            + "{\"timestamp\":\"2019-12-19T00:00:00Z\",\"value\":855}],"
            + "\"granularity\":\"monthly\",\"maxAnomalyRatio\":0.25,\"sensitivity\":95}");
    }
}
