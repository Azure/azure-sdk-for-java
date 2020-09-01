// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.DetectRequest;
import com.azure.ai.anomalydetector.models.LastDetectResponse;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sample for detecting whether the last point of time series is anomaly or not.
 */
public class DetectAnomaliesLastPoint {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the lines from the csv file.
     */
    public static void main(final String[] args) throws IOException {
        String endpoint = "<anomaly-detector-resource-endpoint>";
        String key = "<anomaly-detector-resource-key>";
        HttpHeaders headers = new HttpHeaders()
            .put("Accept", ContentType.APPLICATION_JSON);

        HttpPipelinePolicy authPolicy = new AzureKeyCredentialPolicy("Ocp-Apim-Subscription-Key",
            new AzureKeyCredential(key));
        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(HttpClient.createDefault())
            .policies(authPolicy, addHeadersPolicy).build();
        AnomalyDetectorClient anomalyDetectorClient = new AnomalyDetectorClientBuilder()
            .pipeline(httpPipeline)
            .endpoint(endpoint)
            .buildClient();

        Path path = Paths.get("./src/samples/java/sample_data/request-data.csv");
        List<String> requestData = Files.readAllLines(path);
        List<TimeSeriesPoint> series = requestData.stream()
            .map(line -> line.trim())
            .filter(line -> line.length() > 0)
            .map(line -> line.split(",", 2))
            .filter(splits -> splits.length == 2)
            .map(splits -> {
                TimeSeriesPoint timeSeriesPoint = new TimeSeriesPoint();
                timeSeriesPoint.setTimestamp(OffsetDateTime.parse(splits[0]));
                timeSeriesPoint.setValue(Float.parseFloat(splits[1]));
                return timeSeriesPoint;
            })
            .collect(Collectors.toList());

        System.out.println("Determining if latest data point is an anomaly...");
        DetectRequest request = new DetectRequest();
        request.setSeries(series);
        request.setGranularity(TimeGranularity.DAILY);
        LastDetectResponse response = anomalyDetectorClient.detectLastPoint(request);
        if (response.isAnomaly()) {
            System.out.println("The latest point was detected as an anomaly.");
        } else {
            System.out.println("The latest point was not detected as an anomaly.");
        }
    }
}
