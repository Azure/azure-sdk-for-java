// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.monitor.implementation.ActionGroupsImpl;
import com.azure.resourcemanager.monitor.implementation.ActivityLogsImpl;
import com.azure.resourcemanager.monitor.implementation.AlertRulesImpl;
import com.azure.resourcemanager.monitor.implementation.AutoscaleSettingsImpl;
import com.azure.resourcemanager.monitor.implementation.DiagnosticSettingsImpl;
import com.azure.resourcemanager.monitor.implementation.MetricDefinitionsImpl;
import com.azure.resourcemanager.monitor.models.ActionGroups;
import com.azure.resourcemanager.monitor.models.ActivityLogs;
import com.azure.resourcemanager.monitor.models.AlertRules;
import com.azure.resourcemanager.monitor.models.AutoscaleSettings;
import com.azure.resourcemanager.monitor.models.DiagnosticSettings;
import com.azure.resourcemanager.monitor.models.MetricDefinitions;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.Manager;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;

/** Entry point to Azure Monitor. */
public final class MonitorManager extends Manager<MonitorManager, MonitorClient> {
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
     * @param profile the profile to use
     * @param sdkContext the sdk context
     * @return the MonitorManager
     */
    public static MonitorManager authenticate(
        TokenCredential credential, AzureProfile profile, SdkContext sdkContext) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile, sdkContext);
    }
    /**
     * Creates an instance of MonitorManager that exposes Monitor API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the MonitorManager
     */
    public static MonitorManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new MonitorManager(httpPipeline, profile, new SdkContext());
    }
    /**
     * Creates an instance of MonitorManager that exposes Monitor API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @param sdkContext the sdk context
     * @return the MonitorManager
     */
    public static MonitorManager authenticate(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new MonitorManager(httpPipeline, profile, sdkContext);
    }
    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of MonitorManager that exposes Monitor API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing monitor API entry points that work across subscriptions
         */
        MonitorManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** @return the Azure Activity Logs API entry point */
    public ActivityLogs activityLogs() {
        if (this.activityLogs == null) {
            this.activityLogs = new ActivityLogsImpl(this);
        }
        return this.activityLogs;
    }

    /** @return the Azure Metric Definitions API entry point */
    public MetricDefinitions metricDefinitions() {
        if (this.metricDefinitions == null) {
            this.metricDefinitions = new MetricDefinitionsImpl(this);
        }
        return this.metricDefinitions;
    }

    /** @return the Azure Diagnostic Settings API entry point */
    public DiagnosticSettings diagnosticSettings() {
        if (this.diagnosticSettings == null) {
            this.diagnosticSettings = new DiagnosticSettingsImpl(this);
        }
        return this.diagnosticSettings;
    }

    /** @return the Azure Action Groups API entry point */
    public ActionGroups actionGroups() {
        if (this.actionGroups == null) {
            this.actionGroups = new ActionGroupsImpl(this);
        }
        return this.actionGroups;
    }

    /** @return the Azure AlertRules API entry point */
    public AlertRules alertRules() {
        if (this.alerts == null) {
            this.alerts = new AlertRulesImpl(this);
        }
        return this.alerts;
    }

    /** @return the Azure AutoscaleSettings API entry point */
    public AutoscaleSettings autoscaleSettings() {
        if (this.autoscaleSettings == null) {
            this.autoscaleSettings = new AutoscaleSettingsImpl(this);
        }
        return this.autoscaleSettings;
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public MonitorManager authenticate(TokenCredential credential, AzureProfile profile) {
            return MonitorManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private MonitorManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(
            httpPipeline,
            profile,
            new MonitorClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient(),
            sdkContext);
    }
}
