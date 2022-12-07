// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import java.util.function.Consumer;


/**
 * Base class for Anomaly Detector clients test.
 */
public class AnomalyDetectorClientTestBase extends TestBase {
    private static final String FAKE_API_KEY = "fakeKeyPlaceholder";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";

    void testDetectEntireSeriesWithResponse(Consumer<BinaryData> testRunner) {
        testRunner.accept(getDetectRequest());
    }

    AnomalyDetectorClientBuilder getClientBuilder() {
        String endpoint = getEndpoint();

        HttpPipelinePolicy authPolicy = new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY,
            new AzureKeyCredential(getKey()));
        HttpClient httpClient;
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            httpClient = HttpClient.createDefault();
        } else {
            httpClient = interceptorManager.getPlaybackClient();
        }
        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(authPolicy, interceptorManager.getRecordPolicy()).build();

        return new AnomalyDetectorClientBuilder()
            .pipeline(httpPipeline)
            .endpoint(endpoint);
    }

    private String getKey() {
        if (getTestMode() == TestMode.PLAYBACK) {
            return FAKE_API_KEY;
        } else {
            return Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_API_KEY");
        }
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
               ? "https://localhost:8080"
               : Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_ENDPOINT");
    }

    private BinaryData getDetectRequest() {
        BinaryData requestBody = BinaryData.fromString("{\"series\":[{\"timestamp\":\"2018-01-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-02-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-03-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-04-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-05-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-06-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-07-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-08-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-09-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-10-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-11-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2018-12-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-01-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-02-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-03-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-04-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-05-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-06-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-07-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-08-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-09-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-10-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-11-19T00:00:00Z\",\"value\":855},{\"timestamp\":\"2019-12-19T00:00:00Z\",\"value\":855}],\"granularity\":\"monthly\",\"maxAnomalyRatio\":0.25,\"sensitivity\":95}");
        return requestBody;
    }
}
