// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.Metric;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DetectionConfigurationTest extends DetectionConfigurationTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void createDetectionConfigurationForWholeSeries(HttpClient httpClient,
                                                           MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        DataFeed dataFeed = super.createDataFeed(httpClient, serviceVersion);

        Optional<Metric> optMetric = dataFeed.getSchema().getMetrics()
            .stream()
            .filter(m -> m.getName().equalsIgnoreCase("cost"))
            .findFirst();

        final Metric costMetric = optMetric.get();
        final String costMetricId = costMetric.getId();

        AnomalyDetectionConfiguration configuration
            = client.createMetricAnomalyDetectionConfiguration(costMetricId,
            CreateDetectionConfigurationForWholeSeriesInput.INSTANCE.detectionConfiguration);

        super.assertCreateDetectionConfigurationForWholeSeriesOutput(configuration, costMetricId);

        client.deleteMetricAnomalyDetectionConfiguration(configuration.getId());

        super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void createDetectionConfigurationForSeriesAndGroup(HttpClient httpClient,
                                                              MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        DataFeed dataFeed = super.createDataFeed(httpClient, serviceVersion);

        Optional<Metric> optMetric = dataFeed.getSchema().getMetrics()
            .stream()
            .filter(m -> m.getName().equalsIgnoreCase("cost"))
            .findFirst();

        final Metric costMetric = optMetric.get();
        final String costMetricId = costMetric.getId();

        AnomalyDetectionConfiguration configuration
            = client.createMetricAnomalyDetectionConfiguration(costMetricId,
            CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.detectionConfiguration);

        super.assertCreateDetectionConfigurationForSeriesAndGroupOutput(configuration, costMetricId);

        client.deleteMetricAnomalyDetectionConfiguration(configuration.getId());

        super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void createDetectionConfigurationForMultipleSeriesAndGroup(HttpClient httpClient,
                                                                      MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        DataFeed dataFeed = super.createDataFeed(httpClient, serviceVersion);

        Optional<Metric> optMetric = dataFeed.getSchema().getMetrics()
            .stream()
            .filter(m -> m.getName().equalsIgnoreCase("cost"))
            .findFirst();

        final Metric costMetric = optMetric.get();
        final String costMetricId = costMetric.getId();

        AnomalyDetectionConfiguration configuration
            = client.createMetricAnomalyDetectionConfiguration(costMetricId,
            CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.detectionConfiguration);

        super.assertCreateDetectionConfigurationForMultipleSeriesAndGroupOutput(configuration, costMetricId);

        client.listMetricAnomalyDetectionConfigurations(costMetricId)
            .forEach(config -> {
                Assertions.assertNotNull(config);
            });

        client.deleteMetricAnomalyDetectionConfiguration(configuration.getId());

        super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void updateDetectionConfiguration(HttpClient httpClient,
                                             MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        DataFeed dataFeed = super.createDataFeed(httpClient, serviceVersion);

        Optional<Metric> optMetric = dataFeed.getSchema().getMetrics()
            .stream()
            .filter(m -> m.getName().equalsIgnoreCase("cost"))
            .findFirst();

        final Metric costMetric = optMetric.get();
        final String costMetricId = costMetric.getId();

        final AnomalyDetectionConfiguration[] configs = new AnomalyDetectionConfiguration[1];

        AnomalyDetectionConfiguration configuration
            = client.createMetricAnomalyDetectionConfiguration(costMetricId,
            UpdateDetectionConfigurationInput.INSTANCE.detectionConfiguration);

        Assertions.assertNotNull(configuration);

        configuration.removeSingleSeriesDetectionCondition(UpdateDetectionConfigurationInput
            .INSTANCE
            .seriesKeyToRemoveOnUpdate);

        configuration.addSeriesGroupDetectionCondition(UpdateDetectionConfigurationInput
            .INSTANCE
            .seriesGroupConditionToAddOnUpdate);

        configuration = client.updateMetricAnomalyDetectionConfiguration(configuration);
        super.assertUpdateDetectionConfigurationOutput(configuration, costMetricId);

        client.deleteMetricAnomalyDetectionConfiguration(configuration.getId());

        super.deleteDateFeed(dataFeed, httpClient, serviceVersion);
    }
}
