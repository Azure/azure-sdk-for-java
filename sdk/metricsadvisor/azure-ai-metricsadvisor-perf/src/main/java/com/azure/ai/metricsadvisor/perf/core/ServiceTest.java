// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.perf.core;

import com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient;
import com.azure.ai.metricsadvisor.MetricsAdvisorClient;
import com.azure.ai.metricsadvisor.MetricsAdvisorClientBuilder;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

/**
 * Base class for Azure MetricsAdvisor performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
        + "or system properties.%n";
    private static final int DEFAULT_MAX_LIST_ELEMENTS = 100;

    private final ClientLogger logger = new ClientLogger(ServiceTest.class);

    protected final MetricsAdvisorClient metricsAdvisorClient;
    protected final MetricsAdvisorAsyncClient metricsAdvisorAsyncClient;
    protected final String alertConfigId;
    protected final String alertId;
    protected final String detectionConfigId;
    protected final String incidentId;
    protected final int maxListElements;


    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public ServiceTest(TOptions options) {
        super(options);

        final String endpoint = Configuration.getGlobalConfiguration().get("METRICS_ADVISOR_ENDPOINT");
        if (CoreUtils.isNullOrEmpty(endpoint)) {
            throw logger.logExceptionAsError(
                new RuntimeException(String.format(CONFIGURATION_ERROR, "METRICS_ADVISOR_ENDPOINT")));
        }

        final String subscriptionKey = Configuration.getGlobalConfiguration().get("METRICS_ADVISOR_SUBSCRIPTION_KEY");
        if (CoreUtils.isNullOrEmpty(subscriptionKey)) {
            throw logger.logExceptionAsError(
                new RuntimeException(String.format(CONFIGURATION_ERROR, "METRICS_ADVISOR_SUBSCRIPTION_KEY")));
        }

        final String apiKey = Configuration.getGlobalConfiguration().get("METRICS_ADVISOR_API_KEY");
        if (CoreUtils.isNullOrEmpty(apiKey)) {
            throw logger.logExceptionAsError(
                new RuntimeException(String.format(CONFIGURATION_ERROR, "METRICS_ADVISOR_API_KEY")));
        }

        MetricsAdvisorClientBuilder builder = new MetricsAdvisorClientBuilder()
            .endpoint(endpoint)
            .credential(new MetricsAdvisorKeyCredential(subscriptionKey, apiKey));

        this.metricsAdvisorClient = builder.buildClient();
        this.metricsAdvisorAsyncClient = builder.buildAsyncClient();

        this.alertConfigId = Configuration.getGlobalConfiguration().get("METRICS_ADVISOR_ALERT_CONFIG_ID");
        if (CoreUtils.isNullOrEmpty(alertConfigId)) {
            throw logger.logExceptionAsError(
                new RuntimeException(String.format(CONFIGURATION_ERROR, "METRICS_ADVISOR_ALERT_CONFIG_ID")));
        }

        this.alertId = Configuration.getGlobalConfiguration().get("METRICS_ADVISOR_ALERT_ID");
        if (CoreUtils.isNullOrEmpty(alertId)) {
            throw logger.logExceptionAsError(
                new RuntimeException(String.format(CONFIGURATION_ERROR, "METRICS_ADVISOR_ALERT_ID")));
        }

        this.detectionConfigId
            = Configuration.getGlobalConfiguration().get("METRICS_ADVISOR_DETECTION_CONFIG_ID");
        if (CoreUtils.isNullOrEmpty(detectionConfigId)) {
            throw logger.logExceptionAsError(
                new RuntimeException(String.format(CONFIGURATION_ERROR, "METRICS_ADVISOR_DETECTION_CONFIG_ID")));
        }

        this.incidentId = Configuration.getGlobalConfiguration().get("METRICS_ADVISOR_INCIDENT_ID");
        if (CoreUtils.isNullOrEmpty(incidentId)) {
            throw logger.logExceptionAsError(
                new RuntimeException(String.format(CONFIGURATION_ERROR, "METRICS_ADVISOR_INCIDENT_ID")));
        }

        this.maxListElements = Configuration.getGlobalConfiguration()
            .get("METRICS_ADVISOR_MAX_LIST_ELEMENTS", DEFAULT_MAX_LIST_ELEMENTS);
    }
}
