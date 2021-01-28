// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DetectionConfigurationTest extends DetectionConfigurationTestBase {

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
    public void createDetectionConfigurationForWholeSeries(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<DataFeedMetric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final DataFeedMetric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            AnomalyDetectionConfiguration configuration
                = client.createMetricAnomalyDetectionConfig(costMetricId,
                CreateDetectionConfigurationForWholeSeriesInput.INSTANCE.detectionConfiguration);
            assertNotNull(configuration);

            id.set(configuration.getId());
            super.assertCreateDetectionConfigurationForWholeSeriesOutput(configuration, costMetricId);

        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                client.deleteMetricAnomalyDetectionConfig(id.get());
            }
            if (dataFeed != null) {
                super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void createDetectionConfigurationForSeriesAndGroup(HttpClient httpClient,
                                                              MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<DataFeedMetric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final DataFeedMetric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            AnomalyDetectionConfiguration configuration
                = client.createMetricAnomalyDetectionConfig(costMetricId,
                CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.detectionConfiguration);
            assertNotNull(configuration);
            id.set(configuration.getId());

            super.assertCreateDetectionConfigurationForSeriesAndGroupOutput(configuration, costMetricId);
        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                client.deleteMetricAnomalyDetectionConfig(id.get());
            }
            if (dataFeed != null) {
                super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void createDetectionConfigurationForMultipleSeriesAndGroup(HttpClient httpClient,
                                                                      MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<DataFeedMetric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final DataFeedMetric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            AnomalyDetectionConfiguration configuration
                = client.createMetricAnomalyDetectionConfig(costMetricId,
                CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.detectionConfiguration);

            super.assertCreateDetectionConfigurationForMultipleSeriesAndGroupOutput(configuration, costMetricId);
            assertNotNull(configuration);
            id.set(configuration.getId());

            client.listMetricAnomalyDetectionConfigs(costMetricId)
                .forEach(config -> Assertions.assertNotNull(config));
        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                client.deleteMetricAnomalyDetectionConfig(id.get());
            }
            if (dataFeed != null) {
                super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void updateDetectionConfiguration(HttpClient httpClient,
                                             MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        final AtomicReference<String> id = new AtomicReference<>();
        try {
            dataFeed = super.createDataFeed(httpClient, serviceVersion);

            Optional<DataFeedMetric> optMetric = dataFeed.getSchema().getMetrics()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("cost"))
                .findFirst();

            final DataFeedMetric costMetric = optMetric.get();
            final String costMetricId = costMetric.getId();

            AnomalyDetectionConfiguration configuration
                = client.createMetricAnomalyDetectionConfig(costMetricId,
                UpdateDetectionConfigurationInput.INSTANCE.detectionConfiguration);

            Assertions.assertNotNull(configuration);

            configuration.removeSingleSeriesDetectionCondition(UpdateDetectionConfigurationInput
                .INSTANCE
                .seriesKeyToRemoveOnUpdate);

            configuration.addSeriesGroupDetectionCondition(UpdateDetectionConfigurationInput
                .INSTANCE
                .seriesGroupConditionToAddOnUpdate);

            configuration = client.updateMetricAnomalyDetectionConfig(configuration);
            super.assertUpdateDetectionConfigurationOutput(configuration, costMetricId);
            id.set(configuration.getId());
        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                client.deleteMetricAnomalyDetectionConfig(id.get());
            }
            if (dataFeed != null) {
                super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
            }
        }
    }
}
