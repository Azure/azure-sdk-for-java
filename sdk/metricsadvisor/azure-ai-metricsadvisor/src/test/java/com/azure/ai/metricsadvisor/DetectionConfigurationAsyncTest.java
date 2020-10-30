// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient;
import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.Metric;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DetectionConfigurationAsyncTest extends DetectionConfigurationTestBase {
    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void createDetectionConfigurationForWholeSeries(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<Metric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final Metric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            StepVerifier.create(client.createMetricAnomalyDetectionConfiguration(costMetricId,
                CreateDetectionConfigurationForWholeSeriesInput.INSTANCE.detectionConfiguration))
                .assertNext(configuration -> {
                    id.set(configuration.getId());
                    super.assertCreateDetectionConfigurationForWholeSeriesOutput(configuration, costMetricId);
                })
                .verifyComplete();

        } finally {
            StepVerifier.create(client.deleteMetricAnomalyDetectionConfiguration(id.get()))
                .verifyComplete();
            super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void createDetectionConfigurationForSeriesAndGroup(HttpClient httpClient,
                                                              MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);
            Optional<Metric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final Metric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            StepVerifier.create(client.createMetricAnomalyDetectionConfiguration(costMetricId,
                CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.detectionConfiguration))
                .assertNext(configuration -> {
                    id.set(configuration.getId());
                    super.assertCreateDetectionConfigurationForSeriesAndGroupOutput(configuration, costMetricId);
                })
                .verifyComplete();
        } finally {
            StepVerifier.create(client.deleteMetricAnomalyDetectionConfiguration(id.get()))
                .verifyComplete();

            super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void createDetectionConfigurationForMultipleSeriesAndGroup(HttpClient httpClient,
                                                                      MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<Metric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final Metric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            StepVerifier.create(client.createMetricAnomalyDetectionConfiguration(costMetricId,
                CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.detectionConfiguration))
                .assertNext(configuration -> {
                    id.set(configuration.getId());
                    super.assertCreateDetectionConfigurationForMultipleSeriesAndGroupOutput(configuration, costMetricId);
                })
                .verifyComplete();

            StepVerifier.create(client.listMetricAnomalyDetectionConfigurations(costMetricId))
                // Expect 2 config: Default + the one just created.
                .assertNext(configuration -> Assertions.assertNotNull(configuration))
                .assertNext(configuration -> Assertions.assertNotNull(configuration))
                .verifyComplete();
        } finally {
            StepVerifier.create(client.deleteMetricAnomalyDetectionConfiguration(id.get()))
                .verifyComplete();
            super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void updateDetectionConfiguration(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<Metric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final Metric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            final AnomalyDetectionConfiguration[] configs = new AnomalyDetectionConfiguration[1];
            StepVerifier.create(client.createMetricAnomalyDetectionConfiguration(costMetricId,
                UpdateDetectionConfigurationInput.INSTANCE.detectionConfiguration))
                .assertNext(configuration -> {
                    Assertions.assertNotNull(configuration);
                    configs[0] = configuration;
                })
                .verifyComplete();

            Assertions.assertNotNull(configs[0]);
            AnomalyDetectionConfiguration config = configs[0];
            config.removeSingleSeriesDetectionCondition(UpdateDetectionConfigurationInput
                .INSTANCE
                .seriesKeyToRemoveOnUpdate);

            config.addSeriesGroupDetectionCondition(UpdateDetectionConfigurationInput
                .INSTANCE
                .seriesGroupConditionToAddOnUpdate);

            StepVerifier.create(client.updateMetricAnomalyDetectionConfiguration(config))
                .assertNext(configuration -> {
                    id.set(configuration.getId());
                    super.assertUpdateDetectionConfigurationOutput(configuration, costMetricId);
                })
                .verifyComplete();
        } finally {
            StepVerifier.create(client.deleteMetricAnomalyDetectionConfiguration(id.get()))
                .verifyComplete();

            super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
        }
    }
}
