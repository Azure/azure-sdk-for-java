// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.metricsadvisor.AnomalyAlertTestBase.DETECTION_CONFIGURATION_ID;

public abstract class MetricEnrichedSeriesDataTestBase extends MetricsAdvisorClientTestBase {
    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33586")
    public abstract void getEnrichedSeriesData(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class GetEnrichedSeriesDataInput {
        static final GetEnrichedSeriesDataInput INSTANCE = new GetEnrichedSeriesDataInput();
        final DimensionKey seriesKey = new DimensionKey()
            .put("region", "Miami")
            .put("category", "Health & Personal Care");
        final String detectionConfigurationId = DETECTION_CONFIGURATION_ID;
        final OffsetDateTime startTime = OffsetDateTime.parse("2021-09-22T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2021-10-22T00:00:00Z");

        List<DimensionKey> getSeriesKeys() {
            final List<DimensionKey> seriesKeys = new ArrayList<DimensionKey>();
            seriesKeys.add(this.seriesKey);
            return seriesKeys;
        }
    }

    protected void assertGetEnrichedSeriesDataOutput(MetricEnrichedSeriesData enrichedSeriesData) {
        Assertions.assertNotNull(enrichedSeriesData);
        DimensionKey enrichedSeriesKey = enrichedSeriesData.getSeriesKey();
        Assertions.assertNotNull(enrichedSeriesKey);
        Assertions.assertTrue(GetEnrichedSeriesDataInput.INSTANCE.seriesKey.equals(enrichedSeriesKey));
        // Fixed test data has exactly 27 data points in the time range.
        List<OffsetDateTime> timestamps = enrichedSeriesData.getTimestamps();
        Assertions.assertEquals(30, timestamps.size());
        // Ensure the received data points are in requested range.
        for (OffsetDateTime timestamp : timestamps) {
            Assertions.assertNotNull(timestamp);
            boolean isInRange = (timestamp.isEqual(GetEnrichedSeriesDataInput.INSTANCE.startTime)
                || timestamp.isAfter(GetEnrichedSeriesDataInput.INSTANCE.startTime))
                && (timestamp.isEqual(GetEnrichedSeriesDataInput.INSTANCE.endTime)
                || timestamp.isBefore(GetEnrichedSeriesDataInput.INSTANCE.endTime));
            Assertions.assertTrue(isInRange);
        }
    }
}
