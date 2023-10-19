// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DataFeedIngestionOperationTest extends DataFeedIngestionOperationTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listIngestionStatus(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();

        PagedIterable<DataFeedIngestionStatus> ingestionStatusIterable
            = client.listDataFeedIngestionStatus(ListIngestionStatusInput.INSTANCE.dataFeedId,
            ListIngestionStatusInput.INSTANCE.options);

        for (DataFeedIngestionStatus ingestionStatus : ingestionStatusIterable) {
            assertListIngestionStatusOutput(ingestionStatus);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void getIngestionProgress(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();

        DataFeedIngestionProgress ingestionProgress
            = client.getDataFeedIngestionProgress(GetIngestionProgressInput.INSTANCE.dataFeedId);

        assertListIngestionProgressOutput(ingestionProgress);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void refreshIngestion(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();

        Response<Void> refreshResponse = client.refreshDataFeedIngestionWithResponse(
            RefreshIngestionInput.INSTANCE.dataFeedId,
            RefreshIngestionInput.INSTANCE.startTime,
            RefreshIngestionInput.INSTANCE.endTime,
            Context.NONE);

        Assertions.assertNotNull(refreshResponse);
        assertRefreshIngestionInputOutput(refreshResponse);
    }
}
