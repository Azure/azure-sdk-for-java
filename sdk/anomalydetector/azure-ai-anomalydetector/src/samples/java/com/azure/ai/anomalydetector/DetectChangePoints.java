// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.TimeGranularity;
import com.azure.ai.anomalydetector.models.TimeSeriesPoint;
import com.azure.ai.anomalydetector.models.UnivariateChangePointDetectionOptions;
import com.azure.ai.anomalydetector.models.UnivariateChangePointDetectionResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.io.IOException;
import java.util.List;

/**
 * Sample for detecting change points in a piece of time series.
 */
public class DetectChangePoints {

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

        System.out.println("Detecting change points...");
        // Set the granularity to be DAILY since the minimal interval in time of the sample data is one day.
        UnivariateChangePointDetectionOptions request = new UnivariateChangePointDetectionOptions(series,
            TimeGranularity.DAILY);
        UnivariateChangePointDetectionResult response = anomalyDetectorClient.detectUnivariateChangePoint(request);
        if (response.getIsChangePoint().contains(true)) {
            System.out.println("Change points found in the following data positions:");
            for (int i = 0; i < request.getSeries().size(); ++i) {
                if (response.getIsChangePoint().get(i)) {
                    System.out.print(i + " ");
                }
            }
            System.out.println();
        } else {
            System.out.println("No change points were found in the series.");
        }

        System.out.println("All response data: ");
        System.out.println(response.getPeriod());
        System.out.println(response.getIsChangePoint());
        System.out.println(response.getConfidenceScores());
        System.out.println(response.getIsChangePoint());
    }
}
