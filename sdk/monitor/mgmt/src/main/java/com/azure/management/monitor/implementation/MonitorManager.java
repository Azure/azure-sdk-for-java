/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.monitor.ActionGroups;
import com.azure.management.monitor.AutoscaleSettings;
import com.azure.management.monitor.DiagnosticSettings;
import com.azure.management.monitor.MetricDefinitions;
import com.azure.management.monitor.models.MonitorClientBuilder;
import com.azure.management.monitor.models.MonitorClientImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.AzureTokenCredential;
import com.azure.management.monitor.ActivityLogs;
import com.azure.management.monitor.AlertRules;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.utils.SdkContext;

/**
 * Entry point to Azure Monitor.
 */
public final class MonitorManager extends Manager<MonitorManager, MonitorClientImpl> {
    // Collections
    private ActivityLogs activityLogs;
    private MetricDefinitions metricDefinitions;
    private DiagnosticSettings diagnosticSettings;
    private ActionGroups actionGroups;
    private AlertRules alerts;
    private AutoscaleSettings autoscaleSettings;

    /**
    * Get a Configurable instance that can be used to create MonitorManager with optional configuration.
    *
    * @return the instance allowing configurations
    */
    public static Configurable configure() {
        return new MonitorManager.ConfigurableImpl();
    }
    /**
    * Creates an instance of MonitorManager that exposes Monitor API entry points.
    *
    * @param credential the credential to use
    * @param subscriptionId the subscription UUID
    * @param sdkContext the sdk context
    * @return the MonitorManager
    */
    public static MonitorManager authenticate(AzureTokenCredential credential, String subscriptionId, SdkContext sdkContext) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withPolicy(new ProviderRegistrationPolicy(credential))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), subscriptionId, sdkContext);
    }
    /**
     * Creates an instance of MonitorManager that exposes Monitor API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the MonitorManager
     */
    public static MonitorManager authenticate(RestClient restClient, String subscriptionId) {
        return new MonitorManager(restClient, subscriptionId, new SdkContext());
    }
    /**
    * Creates an instance of MonitorManager that exposes Monitor API entry points.
    *
    * @param restClient the RestClient to be used for API calls.
    * @param subscriptionId the subscription UUID
    * @param sdkContext the sdk context
    * @return the MonitorManager
    */
    public static MonitorManager authenticate(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        return new MonitorManager(restClient, subscriptionId, sdkContext);
    }
    /**
    * The interface allowing configurations to be set.
    */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
        * Creates an instance of MonitorManager that exposes Monitor API entry points.
        *
        * @param credential the credential to use
        * @param subscriptionId the subscription UUID
        * @return the interface exposing monitor API entry points that work across subscriptions
        */
        MonitorManager authenticate(AzureTokenCredential credential, String subscriptionId);
    }

    /**
     * @return the Azure Activity Logs API entry point
     */
    public ActivityLogs activityLogs() {
        if (this.activityLogs == null) {
            this.activityLogs = new ActivityLogsImpl(this);
        }
        return this.activityLogs;
    }

    /**
     * @return the Azure Metric Definitions API entry point
     */
    public MetricDefinitions metricDefinitions() {
        if (this.metricDefinitions == null) {
            this.metricDefinitions = new MetricDefinitionsImpl(this);
        }
        return this.metricDefinitions;
    }

    /**
     * @return the Azure Diagnostic Settings API entry point
     */
    public DiagnosticSettings diagnosticSettings() {
        if (this.diagnosticSettings == null) {
            this.diagnosticSettings = new DiagnosticSettingsImpl(this);
        }
        return this.diagnosticSettings;
    }

    /**
     * @return the Azure Action Groups API entry point
     */
    public ActionGroups actionGroups() {
        if (this.actionGroups == null) {
            this.actionGroups = new ActionGroupsImpl(this);
        }
        return this.actionGroups;
    }

    /**
     * @return the Azure AlertRules API entry point
     */
    public AlertRules alertRules() {
        if (this.alerts == null) {
            this.alerts = new AlertRulesImpl(this);
        }
        return this.alerts;
    }

    /**
     * @return the Azure AutoscaleSettings API entry point
     */
    public AutoscaleSettings autoscaleSettings() {
        if (this.autoscaleSettings == null) {
            this.autoscaleSettings = new AutoscaleSettingsImpl(this);
        }
        return this.autoscaleSettings;
    }

    /**
    * The implementation for Configurable interface.
    */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public MonitorManager authenticate(AzureTokenCredential credential, String subscriptionId) {
           return MonitorManager.authenticate(buildRestClient(credential), subscriptionId);
        }
    }

    private MonitorManager(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        super(
                restClient,
                subscriptionId,
                new MonitorClientBuilder()
                        .pipeline(restClient.getHttpPipeline())
                        .host(restClient.getBaseUrl().toString())
                        .subscriptionId(subscriptionId).build(),
                sdkContext);
    }
}
