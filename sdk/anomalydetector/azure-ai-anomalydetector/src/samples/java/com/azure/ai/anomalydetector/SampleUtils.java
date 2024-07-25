// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.TimeSeriesPoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for common sample methods.
 */
final class SampleUtils {
    /**
     * Loads the {@link TimeSeriesPoint} data from the csv file.
     *
     * @return A list of {@link TimeSeriesPoint} data.
     * @throws IOException Exception thrown when there is an error in reading all the lines from the csv file.
     */
    static List<TimeSeriesPoint> loadTimeSeriesData() throws IOException {
        // Read the time series from csv file and organize the time series into list of TimeSeriesPoint.
        // The sample csv file has no header, and it contains 2 columns, namely timestamp and value.
        // The following is a snippet of the sample csv file:
        //      2018-03-01T00:00:00Z,32858923
        //      2018-03-02T00:00:00Z,29615278
        //      2018-03-03T00:00:00Z,22839355
        //      2018-03-04T00:00:00Z,25948736
        Path path = Paths.get("azure-ai-anomalydetector/src/samples/java/sample_data/request-data.csv");
        List<String> requestData = Files.readAllLines(path);
        return requestData.stream()
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
    }
}
