// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertSnoozeCondition;
import com.azure.ai.metricsadvisor.models.MetricBoundaryCondition;
import com.azure.core.util.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AnomalyAlertTestBase extends MetricsAdvisorAdministrationClientTestBase {

    static final String DETECTION_CONFIGURATION_ID = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
    static final String ALERT_HOOK_ID = "e880ea6b-517e-43b7-a9ea-633583096be7";

    @Override
    protected void beforeTest() {
    }

    void listAnomalyAlertRunner(Consumer<List<AnomalyAlertConfiguration>> testRunner) {
        // create anomaly alert
        testRunner.accept(Collections.singletonList(getAnomalyAlertConfiguration()));
    }

    void creatAnomalyAlertRunner(Consumer<AnomalyAlertConfiguration> testRunner) {
        // create anomaly alert
        testRunner.accept(getAnomalyAlertConfiguration());
    }

    private AnomalyAlertConfiguration getAnomalyAlertConfiguration() {
        final MetricAnomalyAlertConfiguration metricAnomalyAlertConfiguration
            = new MetricAnomalyAlertConfiguration(DETECTION_CONFIGURATION_ID, MetricAnomalyAlertScope.forWholeSeries());

        return new AnomalyAlertConfiguration("test_alert_configuration")
            .setDescription("testing_alert_configuration_description")
            .addMetricAlertConfiguration(metricAnomalyAlertConfiguration)
            .addIdOfHookToAlert(ALERT_HOOK_ID);
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
        List<MetricAnomalyAlertConfiguration> expectedConfiguration,
        List<MetricAnomalyAlertConfiguration> actualAlertConfiguration) {
        assertEquals(expectedConfiguration.size(), actualAlertConfiguration.size());
        for (int i = 0; i < expectedConfiguration.size(); i++) {
            final MetricAnomalyAlertConfiguration expectedConfig = expectedConfiguration.get(i);
            final MetricAnomalyAlertConfiguration actualConfig = actualAlertConfiguration.get(i);
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
