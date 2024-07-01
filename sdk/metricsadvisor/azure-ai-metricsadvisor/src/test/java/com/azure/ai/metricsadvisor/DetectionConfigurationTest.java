// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DetectionConfigurationTest extends DetectionConfigurationTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    @LiveOnly
    public void createDetectionConfigurationForWholeSeries(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
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
                = client.createDetectionConfig(costMetricId,
                CreateDetectionConfigurationForWholeSeriesInput.INSTANCE.detectionConfiguration);
            assertNotNull(configuration);

            id.set(configuration.getId());
            super.assertCreateDetectionConfigurationForWholeSeriesOutput(configuration, costMetricId);

        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                client.deleteDetectionConfig(id.get());
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
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
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
                = client.createDetectionConfig(costMetricId,
                CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.detectionConfiguration);
            assertNotNull(configuration);
            id.set(configuration.getId());

            super.assertCreateDetectionConfigurationForSeriesAndGroupOutput(configuration, costMetricId);
        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                client.deleteDetectionConfig(id.get());
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
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
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
                = client.createDetectionConfig(costMetricId,
                CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.detectionConfiguration);

            super.assertCreateDetectionConfigurationForMultipleSeriesAndGroupOutput(configuration, costMetricId);
            assertNotNull(configuration);
            id.set(configuration.getId());

            client.listDetectionConfigs(costMetricId)
                .forEach(Assertions::assertNotNull);
        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                client.deleteDetectionConfig(id.get());
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
    public void updateDetectionConfiguration(HttpClient httpClient,
                                             MetricsAdvisorServiceVersion serviceVersion) {
        DataFeed dataFeed = null;
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
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
                = client.createDetectionConfig(costMetricId,
                UpdateDetectionConfigurationInput.INSTANCE.detectionConfiguration);

            Assertions.assertNotNull(configuration);

            configuration.removeSingleSeriesDetectionCondition(UpdateDetectionConfigurationInput
                .INSTANCE
                .seriesKeyToRemoveOnUpdate);

            configuration.addSeriesGroupDetectionCondition(UpdateDetectionConfigurationInput
                .INSTANCE
                .seriesGroupConditionToAddOnUpdate);

            configuration = client.updateDetectionConfig(configuration);
            super.assertUpdateDetectionConfigurationOutput(configuration, costMetricId);
            id.set(configuration.getId());
        } finally {
            if (!CoreUtils.isNullOrEmpty(id.get())) {
                client.deleteDetectionConfig(id.get());
            }
            if (dataFeed != null) {
                super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
            }
        }
    }
}
