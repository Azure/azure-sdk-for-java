// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.ImputeMode;
import com.azure.ai.anomalydetector.models.TimeGranularity;
import com.azure.ai.anomalydetector.models.TimeSeriesPoint;
import com.azure.ai.anomalydetector.models.UnivariateDetectionOptions;
import com.azure.ai.anomalydetector.models.UnivariateEntireDetectionResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.io.IOException;
import java.util.List;


/**
 * Sample for detecting anomalies in a piece of time series.
 */
public class DetectAnomaliesEntireSeries {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the lines from the csv file.
     */
    public static void main(final String[] args) throws IOException {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_API_KEY");

        UnivariateClient anomalyDetectorClient =
            new AnomalyDetectorClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildUnivariateClient();

        List<TimeSeriesPoint> series = SampleUtils.loadTimeSeriesData();

        System.out.println("Detecting anomalies as a batch...");
        UnivariateDetectionOptions request = new UnivariateDetectionOptions(series);
        // Set the granularity to be DAILY since the minimal interval in time of the sample data is one day.
        request.setGranularity(TimeGranularity.DAILY);
        request.setImputeMode(ImputeMode.AUTO);

        UnivariateEntireDetectionResult response = anomalyDetectorClient.detectUnivariateEntireSeries(request);
        if (response.getIsAnomaly().contains(true)) {
            System.out.println("Anomalies found in the following data positions:");
            for (int i = 0; i < request.getSeries().size(); ++i) {
                if (response.getIsAnomaly().get(i)) {
                    System.out.print(i + " ");
                }
            }
            System.out.println();
        } else {
            System.out.println("No anomalies were found in the series.");
        }

        System.out.println();
        System.out.println("All response data: ");
        System.out.println(response.getPeriod());
        System.out.println("expectedValues: " + response.getExpectedValues());
        System.out.print("upperMargins: " + response.getUpperMargins());
        System.out.print("lowerMargins: " + response.getLowerMargins());
        System.out.print("isAnomaly: " + response.getIsAnomaly());
    }
}
