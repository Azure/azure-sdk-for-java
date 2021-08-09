// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class DataFeedIngestionOperationAsyncTest extends DataFeedIngestionOperationTestBase {

    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listIngestionStatus(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        PagedFlux<DataFeedIngestionStatus> ingestionStatusFlux
            = client.listDataFeedIngestionStatus(ListIngestionStatusInput.INSTANCE.dataFeedId,
            ListIngestionStatusInput.INSTANCE.options);

        Assertions.assertNotNull(ingestionStatusFlux);

        StepVerifier.create(ingestionStatusFlux)
            .thenConsumeWhile(ingestionStatus -> {
                assertListIngestionStatusOutput(ingestionStatus);
                return true;
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void getIngestionProgress(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        Mono<DataFeedIngestionProgress> ingestionProgressMono
            = client.getDataFeedIngestionProgress(GetIngestionProgressInput.INSTANCE.dataFeedId);

        Assertions.assertNotNull(ingestionProgressMono);

        StepVerifier.create(ingestionProgressMono)
            .assertNext(ingestionProgress -> assertListIngestionProgressOutput(ingestionProgress))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void refreshIngestion(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        Mono<Response<Void>> refreshIngestionMono = client.refreshDataFeedIngestionWithResponse(
            RefreshIngestionInput.INSTANCE.dataFeedId,
            RefreshIngestionInput.INSTANCE.startTime,
            RefreshIngestionInput.INSTANCE.endTime);

        Assertions.assertNotNull(refreshIngestionMono);

        StepVerifier.create(refreshIngestionMono)
            .assertNext(response -> assertRefreshIngestionInputOutput(response))
            .verifyComplete();
    }
}
