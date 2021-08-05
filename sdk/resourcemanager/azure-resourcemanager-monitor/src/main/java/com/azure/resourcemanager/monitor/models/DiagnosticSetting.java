// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.time.Duration;
import java.util.List;

/** An immutable client-side representation of an Azure diagnostic settings. */
@Fluent
public interface DiagnosticSetting
    extends Indexable,
        HasId,
        HasName,
        HasManager<MonitorManager>,
        HasInnerModel<DiagnosticSettingsResourceInner>,
        Refreshable<DiagnosticSetting>,
        Updatable<DiagnosticSetting.Update> {
    /**
     * Get the associated resource Id value.
     *
     * @return the associated resource Id value
     */
    String resourceId();

    /**
     * Get the storageAccountId value.
     *
     * @return the storageAccountId value
     */
    String storageAccountId();

    /**
     * Get the eventHubAuthorizationRuleId value.
     *
     * @return the eventHubAuthorizationRuleId value
     */
    String eventHubAuthorizationRuleId();

    /**
     * Get the eventHubName value.
     *
     * @return the eventHubName value
     */
    String eventHubName();

    /**
     * Get the metrics value.
     *
     * @return the metrics value
     */
    List<MetricSettings> metrics();

    /**
     * Get the logs value.
     *
     * @return the logs value
     */
    List<LogSettings> logs();

    /**
     * Get the workspaceId value.
     *
     * @return the workspaceId value
     */
    String workspaceId();

    /** The entirety of a diagnostic settings definition. */
    interface Definition
        extends DefinitionStages.Blank, DefinitionStages.WithDiagnosticLogRecipient, DefinitionStages.WithCreate {
    }

    /** Grouping of diagnostic settings definition stages. */
    interface DefinitionStages {
        /** The first stage of a diagnostic setting definition. */
        interface Blank {
            /**
             * Sets the resource for which Diagnostic Settings will be created.
             *
             * @param resourceId of the resource.
             * @return the stage of selecting data recipient.
             */
            WithDiagnosticLogRecipient withResource(String resourceId);
        }

        /**
         * The stage of the definition which contains minimum required properties to be specified for Diagnostic
         * Settings creation.
         */
        interface WithDiagnosticLogRecipient {
            /**
             * Sets Log Analytics workspace for data transfer.
             *
             * @param workspaceId of Log Analytics that should exist in the same region as resource.
             * @return the stage of creating Diagnostic Settings.
             */
            WithCreate withLogAnalytics(String workspaceId);

            /**
             * Sets Storage Account for data transfer.
             *
             * @param storageAccountId of storage account that should exist in the same region as resource.
             * @return the stage of creating Diagnostic Settings.
             */
            WithCreate withStorageAccount(String storageAccountId);

            /**
             * Sets EventHub Namespace Authorization Rule for data transfer.
             *
             * @param eventHubAuthorizationRuleId of EventHub namespace authorization rule that should exist in the same
             *     region as resource.
             * @return the stage of creating Diagnostic Settings.
             */
            WithCreate withEventHub(String eventHubAuthorizationRuleId);

            /**
             * Sets EventHub Namespace Authorization Rule for data transfer.
             *
             * @param eventHubAuthorizationRuleId of EventHub namespace authorization rule that should exist in the same
             *     region as resource.
             * @param eventHubName name of the EventHub. If none is specified, the default EventHub will be selected.
             * @return the stage of creating Diagnostic Settings.
             */
            WithCreate withEventHub(String eventHubAuthorizationRuleId, String eventHubName);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created but
         * also allows for any other optional settings to be specified.
         */
        interface WithCreate extends WithDiagnosticLogRecipient, Creatable<DiagnosticSetting> {
            /**
             * Adds a Metric Setting to the list of Metric Settings for the current Diagnostic Settings.
             *
             * @param category name of a Metric category for a resource type this setting is applied to.
             * @param timeGrain the timegrain of the metric in ISO8601 format.
             * @param retentionDays the number of days for the retention in days. A value of 0 will retain the events
             *     indefinitely.
             * @return the stage of creating Diagnostic Settings.
             */
            WithCreate withMetric(String category, Duration timeGrain, int retentionDays);

            /**
             * Adds a Log Setting to the list of Log Settings for the current Diagnostic Settings.
             *
             * @param category name of a Log category for a resource type this setting is applied to.
             * @param retentionDays the number of days for the retention in days. A value of 0 will retain the events
             *     indefinitely.
             * @return the stage of creating Diagnostic Settings.
             */
            WithCreate withLog(String category, int retentionDays);

            /**
             * Adds a Log and Metric Settings to the list Log and Metric Settings for the current Diagnostic Settings.
             *
             * @param categories a list of diagnostic settings category.
             * @param timeGrain the timegrain of the metric in ISO8601 format for all Metrics in the {@code categories}
             *     list.
             * @param retentionDays the number of days for the retention in days. A value of 0 will retain the events
             *     indefinitely.
             * @return the stage of creating Diagnostic Settings.
             */
            WithCreate withLogsAndMetrics(
                List<DiagnosticSettingsCategory> categories, Duration timeGrain, int retentionDays);
        }
    }

    /** Grouping of diagnostic setting update stages. */
    interface UpdateStages {
        /** The stage of a Diagnostic Settings update allowing to modify Storage Account settings. */
        interface WithStorageAccount {
            /**
             * Sets Storage Account for data transfer.
             *
             * @param storageAccountId of storage account that should exist in the same region as resource.
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withStorageAccount(String storageAccountId);

            /**
             * Removes the Storage Account from the Diagnostic Settings.
             *
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withoutStorageAccount();
        }

        /** The stage of a Diagnostic Settings update allowing to modify EventHub settings. */
        interface WithEventHub {
            /**
             * Sets EventHub Namespace Authorization Rule for data transfer.
             *
             * @param eventHubAuthorizationRuleId of EventHub namespace authorization rule that should exist in the same
             *     region as resource.
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withEventHub(String eventHubAuthorizationRuleId);

            /**
             * Sets EventHub Namespace Authorization Rule for data transfer.
             *
             * @param eventHubAuthorizationRuleId of EventHub namespace authorization rule that should exist in the same
             *     region as resource.
             * @param eventHubName name of the EventHub. If none is specified, the default EventHub will be selected.
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withEventHub(String eventHubAuthorizationRuleId, String eventHubName);

            /**
             * Removes the EventHub from the Diagnostic Settings.
             *
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withoutEventHub();
        }

        /** The stage of a Diagnostic Settings update allowing to modify Log Analytics settings. */
        interface WithLogAnalytics {
            /**
             * Sets Log Analytics workspace for data transfer.
             *
             * @param workspaceId of Log Analytics that should exist in the same region as resource.
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withLogAnalytics(String workspaceId);

            /**
             * Removes the Log Analytics from the Diagnostic Settings.
             *
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withoutLogAnalytics();
        }

        interface WithMetricAndLogs {
            /**
             * Adds a Metric Setting to the list of Metric Settings for the current Diagnostic Settings.
             *
             * @param category name of a Metric category for a resource type this setting is applied to.
             * @param timeGrain the timegrain of the metric in ISO8601 format.
             * @param retentionDays the number of days for the retention in days. A value of 0 will retain the events
             *     indefinitely.
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withMetric(String category, Duration timeGrain, int retentionDays);

            /**
             * Adds a Log Setting to the list of Log Settings for the current Diagnostic Settings.
             *
             * @param category name of a Log category for a resource type this setting is applied to.
             * @param retentionDays the number of days for the retention in days. A value of 0 will retain the events
             *     indefinitely.
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withLog(String category, int retentionDays);

            /**
             * Adds a Log and Metric Settings to the list Log and Metric Settings for the current Diagnostic Settings.
             *
             * @param categories a list of diagnostic settings category.
             * @param timeGrain the timegrain of the metric in ISO8601 format for all Metrics in the {@code categories}
             *     list.
             * @param retentionDays the number of days for the retention in days. A value of 0 will retain the events
             *     indefinitely.
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withLogsAndMetrics(
                List<DiagnosticSettingsCategory> categories, Duration timeGrain, int retentionDays);

            /**
             * Removes the Metric Setting from the Diagnostic Setting.
             *
             * @param category name of a Metric category to remove.
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withoutMetric(String category);

            /**
             * Removes the Log Setting from the Diagnostic Setting.
             *
             * @param category name of a Log category to remove.
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withoutLog(String category);

            /**
             * Removes all the Log Settings from the Diagnostic Setting.
             *
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withoutLogs();

            /**
             * Removes all the Metric Settings from the Diagnostic Setting.
             *
             * @return the next stage of the Diagnostic Settings update.
             */
            Update withoutMetrics();
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<DiagnosticSetting>,
            UpdateStages.WithStorageAccount,
            UpdateStages.WithEventHub,
            UpdateStages.WithLogAnalytics,
            UpdateStages.WithMetricAndLogs {
    }
}
