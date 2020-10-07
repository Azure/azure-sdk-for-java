// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Assertions;

import java.time.OffsetDateTime;

public abstract class DataFeedIngestionOperationTestBase extends MetricsAdvisorAdministrationClientTestBase {
    public abstract void listIngestionStatus(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListIngestionStatusInput {
        static final ListIngestionStatusInput INSTANCE = new ListIngestionStatusInput();
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions(startTime, endTime);
    }

    protected void assertListIngestionStatusOutput(DataFeedIngestionStatus ingestionStatus) {
        Assertions.assertNotNull(ingestionStatus);
        Assertions.assertNotNull(ingestionStatus.getStatus());
        Assertions.assertNotNull(ingestionStatus.getTimestamp());
    }

    public abstract void getIngestionProgress(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class GetIngestionProgressInput {
        static final GetIngestionProgressInput INSTANCE = new GetIngestionProgressInput();
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
    }

    protected void assertListIngestionProgressOutput(DataFeedIngestionProgress ingestionProgress) {
        Assertions.assertNotNull(ingestionProgress);
        Assertions.assertNotNull(ingestionProgress.getLatestActiveTimestamp());
        Assertions.assertNotNull(ingestionProgress.getLatestSuccessTimestamp());
    }

    public abstract void refreshIngestion(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class RefreshIngestionInput {
        static final RefreshIngestionInput INSTANCE = new RefreshIngestionInput();
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-03-03T00:00:00Z");
    }

    protected void assertRefreshIngestionInputOutput(Response<Void> response) {
        Assertions.assertNotNull(response);
        Assertions.assertEquals(204, response.getStatusCode());
    }
}
