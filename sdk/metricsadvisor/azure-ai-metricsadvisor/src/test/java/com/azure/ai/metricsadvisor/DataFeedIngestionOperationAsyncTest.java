// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class DataFeedIngestionOperationAsyncTest extends DataFeedIngestionOperationTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listIngestionStatus(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();

        PagedFlux<DataFeedIngestionStatus> ingestionStatusFlux
            = client.listDataFeedIngestionStatus(ListIngestionStatusInput.INSTANCE.dataFeedId,
            ListIngestionStatusInput.INSTANCE.options);

        Assertions.assertNotNull(ingestionStatusFlux);

        StepVerifier.create(ingestionStatusFlux)
            .thenConsumeWhile(ingestionStatus -> {
                assertListIngestionStatusOutput(ingestionStatus);
                return true;
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void getIngestionProgress(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();

        Mono<DataFeedIngestionProgress> ingestionProgressMono
            = client.getDataFeedIngestionProgress(GetIngestionProgressInput.INSTANCE.dataFeedId);

        Assertions.assertNotNull(ingestionProgressMono);

        StepVerifier.create(ingestionProgressMono)
            .assertNext(this::assertListIngestionProgressOutput)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void refreshIngestion(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();

        Mono<Response<Void>> refreshIngestionMono = client.refreshDataFeedIngestionWithResponse(
            RefreshIngestionInput.INSTANCE.dataFeedId,
            RefreshIngestionInput.INSTANCE.startTime,
            RefreshIngestionInput.INSTANCE.endTime);

        Assertions.assertNotNull(refreshIngestionMono);

        StepVerifier.create(refreshIngestionMono)
            .assertNext(this::assertRefreshIngestionInputOutput)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }
}
