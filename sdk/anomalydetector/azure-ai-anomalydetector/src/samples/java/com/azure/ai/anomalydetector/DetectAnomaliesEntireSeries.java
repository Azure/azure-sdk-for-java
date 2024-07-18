// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.UnivariateEntireDetectionResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


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

        // Read the time series from csv file and organize the time series into list of TimeSeriesPoint.
        // The sample csv file has no header, and it contains 2 columns, namely timestamp and value.
        // The following is a snippet of the sample csv file:
        //      2018-03-01T00:00:00Z,32858923
        //      2018-03-02T00:00:00Z,29615278
        //      2018-03-03T00:00:00Z,22839355
        //      2018-03-04T00:00:00Z,25948736
        Path path = Paths.get("azure-ai-anomalydetector/src/samples/java/sample_data/request-data.csv");
        List<String> requestData = Files.readAllLines(path);
        List<TimeSeriesPoint> series = requestData.stream()
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .map(line -> line.split(",", 2))
            .filter(splits -> splits.length == 2)
            .map(splits -> {
                TimeSeriesPoint timeSeriesPoint = new TimeSeriesPoint(Float.parseFloat(splits[1]));
                timeSeriesPoint.setTimestamp(OffsetDateTime.parse(splits[0]));
                return timeSeriesPoint;
            })
            .collect(Collectors.toList());

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
        System.out.println("expectedValues: " + Arrays.toString(response.getExpectedValues().toArray(new Double[0])));
        System.out.print("upperMargins: " + Arrays.toString(response.getUpperMargins().toArray(new Double[0])));
        System.out.print("lowerMargins: " + Arrays.toString(response.getLowerMargins().toArray(new Double[0])));
        System.out.print("isAnomaly: " + Arrays.toString(response.getIsAnomaly().toArray(new Boolean[0])));
    }
}
