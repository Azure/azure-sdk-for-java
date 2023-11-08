// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.MetricAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertSnoozeCondition;
import com.azure.ai.metricsadvisor.administration.models.MetricBoundaryCondition;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AnomalyAlertTestBase extends MetricsAdvisorAdministrationClientTestBase {

    static final String DETECTION_CONFIGURATION_ID = "78f3a4e7-fe53-4a05-9f4d-d724ab6c23a7";
    static final String ALERT_HOOK_ID = "ad2639d2-84dd-4b12-9635-82b8ff14e832";

    @Override
    protected void beforeTest() {
    }

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33586")
    public abstract void getAnomalyAlertValidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33586")
    public abstract void createAnomalyAlertConfiguration(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33586")
    public abstract void testListAnomalyAlert(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33586")
    public abstract void updateAnomalyAlertRemoveHooks(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33586")
    public abstract void updateAnomalyAlertHappyPath(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33586")
    public abstract void deleteAnomalyAlertWithResponse(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    void listAnomalyAlertRunner(Consumer<List<AnomalyAlertConfiguration>> testRunner) {
        // create anomaly alert
        testRunner.accept(Collections.singletonList(getAnomalyAlertConfiguration()));
    }

    void creatAnomalyAlertRunner(Consumer<AnomalyAlertConfiguration> testRunner) {
        // create anomaly alert
        testRunner.accept(getAnomalyAlertConfiguration());
    }

    private AnomalyAlertConfiguration getAnomalyAlertConfiguration() {
        final MetricAlertConfiguration metricAnomalyAlertConfiguration
            = new MetricAlertConfiguration(DETECTION_CONFIGURATION_ID, MetricAnomalyAlertScope.forWholeSeries());

        return new AnomalyAlertConfiguration("test_alert_configuration")
            .setDescription("testing_alert_configuration_description")
            .addMetricAlertConfiguration(metricAnomalyAlertConfiguration)
            .setHookIdsToAlert(Collections.emptyList());
    }

    void validateAnomalyAlertResult(AnomalyAlertConfiguration expectedAnomalyAlertConfiguration,
        AnomalyAlertConfiguration actualAnomalyAlertConfiguration) {
        assertNotNull(actualAnomalyAlertConfiguration.getId());
        assertEquals(expectedAnomalyAlertConfiguration.getDescription(),
            actualAnomalyAlertConfiguration.getDescription());
        validateMetricAnomalyDetectionConfiguration(expectedAnomalyAlertConfiguration.getMetricAlertConfigurations(),
            actualAnomalyAlertConfiguration.getMetricAlertConfigurations());
    }

    private void validateMetricAnomalyDetectionConfiguration(
        List<MetricAlertConfiguration> expectedConfiguration,
        List<MetricAlertConfiguration> actualAlertConfiguration) {
        assertEquals(expectedConfiguration.size(), actualAlertConfiguration.size());
        for (int i = 0; i < expectedConfiguration.size(); i++) {
            final MetricAlertConfiguration expectedConfig = expectedConfiguration.get(i);
            final MetricAlertConfiguration actualConfig = actualAlertConfiguration.get(i);
            validateAlertConditions(expectedConfig.getAlertConditions(), actualConfig.getAlertConditions());
            validateAlertScope(expectedConfig.getAlertScope(), actualConfig.getAlertScope());
            validateSnoozeCondition(expectedConfig.getAlertSnoozeCondition(), actualConfig.getAlertSnoozeCondition());
            assertEquals(expectedConfig.getDetectionConfigurationId(), actualConfig.getDetectionConfigurationId());
            assertNotNull(actualConfig.isNegationOperationEnabled());
        }
    }

    private void validateSnoozeCondition(MetricAnomalyAlertSnoozeCondition expectedSnoozeCondition,
        MetricAnomalyAlertSnoozeCondition actualSnoozeCondition) {
        if (expectedSnoozeCondition != null && actualSnoozeCondition != null) {
            assertEquals(expectedSnoozeCondition.getAutoSnooze(), actualSnoozeCondition.getAutoSnooze());
            assertEquals(expectedSnoozeCondition.getSnoozeScope().toString(),
                actualSnoozeCondition.getSnoozeScope().toString());
        }
    }

    private void validateAlertScope(MetricAnomalyAlertScope expectedAlertScope,
        MetricAnomalyAlertScope actualAlertScope) {
        assertEquals(expectedAlertScope.getScopeType().toString(), actualAlertScope.getScopeType().toString());
        assertEquals(expectedAlertScope.getSeriesGroupInScope(), actualAlertScope.getSeriesGroupInScope());
        if (expectedAlertScope.getTopNGroupInScope() != null && actualAlertScope.getTopNGroupInScope() != null) {
            assertEquals(expectedAlertScope.getTopNGroupInScope().getMinTopCount(),
                actualAlertScope.getTopNGroupInScope().getMinTopCount());
            assertEquals(expectedAlertScope.getTopNGroupInScope().getPeriod(),
                actualAlertScope.getTopNGroupInScope().getPeriod());
            assertEquals(expectedAlertScope.getTopNGroupInScope().getTop(),
                actualAlertScope.getTopNGroupInScope().getTop());
        }
    }

    private void validateAlertConditions(MetricAnomalyAlertConditions expectedAlertCondition,
        MetricAnomalyAlertConditions actualAlertCondition) {
        if (expectedAlertCondition != null && actualAlertCondition != null) {
            validateBoundaryCondition(expectedAlertCondition.getMetricBoundaryCondition(),
                actualAlertCondition.getMetricBoundaryCondition());
            assertEquals(expectedAlertCondition.getSeverityCondition().getMaxAlertSeverity(),
                actualAlertCondition.getSeverityCondition().getMaxAlertSeverity());
            assertEquals(expectedAlertCondition.getSeverityCondition().getMinAlertSeverity(),
                actualAlertCondition.getSeverityCondition().getMinAlertSeverity());
        }
    }

    private void validateBoundaryCondition(MetricBoundaryCondition expectedBoundaryCondition,
        MetricBoundaryCondition actualBoundaryCondition) {
        assertEquals(expectedBoundaryCondition.getCompanionMetricId(), actualBoundaryCondition.getCompanionMetricId());
        assertEquals(expectedBoundaryCondition.getDirection().toString(),
            actualBoundaryCondition.getDirection().toString());
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }
}
