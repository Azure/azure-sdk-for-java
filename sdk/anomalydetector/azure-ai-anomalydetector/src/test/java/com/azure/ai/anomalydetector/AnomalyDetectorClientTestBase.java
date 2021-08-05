// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.DetectRequest;
import com.azure.ai.anomalydetector.models.TimeGranularity;
import com.azure.ai.anomalydetector.models.TimeSeriesPoint;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for Anomaly Detector clients test.
 */
public class AnomalyDetectorClientTestBase extends TestBase {
    private static final String FAKE_API_KEY = "1234567890";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";

    void testDetectEntireSeriesWithResponse(Consumer<DetectRequest> testRunner) {
        testRunner.accept(getDetectRequest());
    }

    AnomalyDetectorClientBuilder getClientBuilder() {
        String endpoint = getEndpoint();
        HttpHeaders headers = new HttpHeaders()
            .put("Accept", ContentType.APPLICATION_JSON);

        HttpPipelinePolicy authPolicy = new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY,
            new AzureKeyCredential(getKey()));

        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

        HttpClient httpClient;
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            httpClient = HttpClient.createDefault();
        } else {
            httpClient = interceptorManager.getPlaybackClient();
        }

        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(authPolicy, addHeadersPolicy, interceptorManager.getRecordPolicy()).build();

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

    private DetectRequest getDetectRequest() {
        List<TimeSeriesPoint> series = new ArrayList<>();
        int year = 2018;
        int month = 0;

        for (int i = 0; i < 24; i++) {
            if (month == 12) {
                month = 0;
                year++;
            }
            month++;
            TimeSeriesPoint timeSeriesPoint = new TimeSeriesPoint().setValue(855)
                .setTimestamp(OffsetDateTime.now().withMonth(month).withYear(year));
            series.add(timeSeriesPoint);
        }

        return new DetectRequest()
            .setGranularity(TimeGranularity.MONTHLY)
            .setMaxAnomalyRatio(0.25f)
            .setSensitivity(95)
            .setSeries(series);
    }
}
