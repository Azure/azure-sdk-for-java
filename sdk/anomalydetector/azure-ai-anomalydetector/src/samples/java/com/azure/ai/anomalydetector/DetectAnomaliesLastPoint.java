// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.ImputeMode;
import com.azure.ai.anomalydetector.models.TimeGranularity;
import com.azure.ai.anomalydetector.models.TimeSeriesPoint;
import com.azure.ai.anomalydetector.models.UnivariateDetectionOptions;
import com.azure.ai.anomalydetector.models.UnivariateLastDetectionResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.io.IOException;
import java.util.List;

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
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_API_KEY");

        UnivariateClient anomalyDetectorClient = new AnomalyDetectorClientBuilder().credential(
            new AzureKeyCredential(key)).endpoint(endpoint).buildUnivariateClient();

        List<TimeSeriesPoint> series = SampleUtils.loadTimeSeriesData();

        System.out.println("Determining if latest data point is an anomaly...");
        UnivariateDetectionOptions request = new UnivariateDetectionOptions(series);
        // Set the granularity to be DAILY since the minimal interval in time of the sample data is one day.
        request.setGranularity(TimeGranularity.DAILY);
        request.setImputeMode(ImputeMode.AUTO);

        UnivariateLastDetectionResult response = anomalyDetectorClient.detectUnivariateLastPoint(request);
        System.out.println("ExpectedValue: " + response.getExpectedValue() + ", Severity: " + response.getSeverity());
        if (response.isAnomaly()) {
            System.out.println("The latest point was detected as an anomaly.");
        } else {
            System.out.println("The latest point was not detected as an anomaly.");
        }
    }
}
