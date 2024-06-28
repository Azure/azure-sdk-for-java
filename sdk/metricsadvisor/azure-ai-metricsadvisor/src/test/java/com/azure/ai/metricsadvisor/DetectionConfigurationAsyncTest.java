// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.ListDetectionConfigsOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DetectionConfigurationAsyncTest extends DetectionConfigurationTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    @LiveOnly
    public void createDetectionConfigurationForWholeSeries(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<DataFeedMetric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final DataFeedMetric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            StepVerifier.create(client.createDetectionConfig(costMetricId,
                CreateDetectionConfigurationForWholeSeriesInput.INSTANCE.detectionConfiguration))
                .assertNext(configuration -> {
                    id.set(configuration.getId());
                    super.assertCreateDetectionConfigurationForWholeSeriesOutput(configuration, costMetricId);
                })
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                StepVerifier.create(client.deleteDetectionConfig(id.get()))
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            }
            if (dataFeed != null) {
                super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    @LiveOnly
    public void createDetectionConfigurationForSeriesAndGroup(HttpClient httpClient,
                                                              MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);
            Optional<DataFeedMetric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final DataFeedMetric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            StepVerifier.create(client.createDetectionConfig(costMetricId,
                CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.detectionConfiguration))
                .assertNext(configuration -> {
                    assertNotNull(configuration);
                    id.set(configuration.getId());
                    super.assertCreateDetectionConfigurationForSeriesAndGroupOutput(configuration, costMetricId);
                })
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                StepVerifier.create(client.deleteDetectionConfig(id.get()))
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            }
            if (dataFeed != null) {
                super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    @LiveOnly
    public void createDetectionConfigurationForMultipleSeriesAndGroup(HttpClient httpClient,
                                                                      MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<DataFeedMetric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final DataFeedMetric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            StepVerifier.create(client.createDetectionConfig(costMetricId,
                CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.detectionConfiguration))
                .assertNext(configuration -> {
                    assertNotNull(configuration);
                    id.set(configuration.getId());
                    super.assertCreateDetectionConfigurationForMultipleSeriesAndGroupOutput(configuration, costMetricId);
                })
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

            StepVerifier.create(client.listDetectionConfigs(costMetricId,
                new ListDetectionConfigsOptions()))
                // Expect 2 config: Default + the one just created.
                .assertNext(Assertions::assertNotNull)
                .assertNext(Assertions::assertNotNull)
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                StepVerifier.create(client.deleteDetectionConfig(id.get()))
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            }
            if (dataFeed != null) {
                super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    @LiveOnly
    public void updateDetectionConfiguration(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<DataFeedMetric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final DataFeedMetric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            final AnomalyDetectionConfiguration[] configs = new AnomalyDetectionConfiguration[1];
            StepVerifier.create(client.createDetectionConfig(costMetricId,
                UpdateDetectionConfigurationInput.INSTANCE.detectionConfiguration))
                .assertNext(configuration -> {
                    assertNotNull(configuration);
                    configs[0] = configuration;
                })
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

            assertNotNull(configs[0]);
            AnomalyDetectionConfiguration config = configs[0];
            config.removeSingleSeriesDetectionCondition(UpdateDetectionConfigurationInput
                .INSTANCE
                .seriesKeyToRemoveOnUpdate);

            config.addSeriesGroupDetectionCondition(UpdateDetectionConfigurationInput
                .INSTANCE
                .seriesGroupConditionToAddOnUpdate);

            StepVerifier.create(client.updateDetectionConfig(config))
                .assertNext(configuration -> {
                    id.set(configuration.getId());
                    super.assertUpdateDetectionConfigurationOutput(configuration, costMetricId);
                })
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                StepVerifier.create(client.deleteDetectionConfig(id.get()))
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            }
            if (dataFeed != null) {
                super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
            }
        }
    }
}
