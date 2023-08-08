// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.fluent.models.MetricAlertResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;

/** An immutable client-side representation of a Metric Alert. */
@Fluent
public interface MetricAlert
    extends GroupableResource<MonitorManager, MetricAlertResourceInner>,
        Refreshable<MetricAlert>,
        Updatable<MetricAlert.Update> {

    /**
     * Get the description of the metric alert that will be included in the alert email.
     *
     * @return the description value
     */
    String description();

    /**
     * Get alert severity {0, 1, 2, 3, 4}.
     *
     * @return the severity value
     */
    int severity();

    /**
     * Get the flag that indicates whether the metric alert is enabled.
     *
     * @return the enabled value
     */
    boolean enabled();

    /**
     * Get the list of resource id's that this metric alert is scoped to.
     *
     * @return the scopes value
     */
    Collection<String> scopes();

    /**
     * Get how often the metric alert is evaluated represented in ISO 8601 duration format.
     *
     * @return the evaluationFrequency value
     */
    Duration evaluationFrequency();

    /**
     * Get the Duration of time (in ISO 8601 duration format) that is used to monitor alert activity based on the
     * threshold.
     *
     * @return the windowSize value
     */
    Duration windowSize();

    /** @return metric alert criterias, indexed by name */
    Map<String, MetricAlertCondition> alertCriterias();

    /** @return metric dynamic alert criterias, indexed by name */
    Map<String, MetricDynamicAlertCondition> dynamicAlertCriterias();

    /**
     * Get the flag that indicates whether the alert should be auto resolved or not.
     *
     * @return the autoMitigate value
     */
    boolean autoMitigate();

    /**
     * Get the array of actions that are performed when the alert rule becomes active, and when an alert condition is
     * resolved.
     *
     * @return the actions value
     */
    Collection<String> actionGroupIds();

    /**
     * Get last time the rule was updated in ISO8601 format.
     *
     * @return the lastUpdatedTime value
     */
    OffsetDateTime lastUpdatedTime();

    /** The entirety of a Metric Alert definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithCreate,
            DefinitionStages.WithScopes,
            DefinitionStages.WithWindowSize,
            DefinitionStages.WithEvaluationFrequency,
            DefinitionStages.WithSeverity,
            DefinitionStages.WithActionGroup,
            DefinitionStages.WithCriteriaDefinition {
    }

    /** Metric Alert definition for multiple resource. */
    interface DefinitionMultipleResource
        extends DefinitionStages.Blank,
            DefinitionStages.WithCreateDynamicCondition,
            DefinitionStages.WithScopes,
            DefinitionStages.WithWindowSizeMultipleResource,
            DefinitionStages.WithEvaluationFrequencyMultipleResource,
            DefinitionStages.WithSeverityMultipleResource,
            DefinitionStages.WithActionGroupMultipleResource,
            DefinitionStages.WithCriteriaDefinitionMultipleResource {
    }

    /** Grouping of metric alerts definition stages. */
    interface DefinitionStages {
        /** The first stage of a Metric Alert definition. */
        interface Blank extends GroupableResource.DefinitionStages.WithGroupAndRegion<WithScopes> {
        }

        /** The stage of the definition which specifies target resource for metric alert. */
        interface WithScopes {
            /**
             * Sets specified resource as a target to alert on metric.
             *
             * @param resourceId resource Id string.
             * @return the next stage of metric alert definition.
             */
            WithWindowSize withTargetResource(String resourceId);

            /**
             * Sets specified resource as a target to alert on metric.
             *
             * @param resource resource type that is inherited from {@link HasId} interface
             * @return the next stage of metric alert definition.
             */
            WithWindowSize withTargetResource(HasId resource);

            /**
             * Sets specified resources as target to alert on metric. All resources must be of the same type and in the
             * same region.
             *
             * @param resourceIds collection of resource id to alert on metric.
             * @param type resource type.
             * @param regionName resource region.
             * @return the next stage of metric alert definition.
             */
            WithWindowSizeMultipleResource withMultipleTargetResources(
                Collection<String> resourceIds, String type, String regionName);

            /**
             * Sets specified resources as target to alert on metric. All resources must be of the same type and in the
             * same region.
             *
             * @param resources collection of resources to alert on metric, which must be of the same type and in the
             *     same region.
             * @return the next stage of metric alert definition.
             */
            WithWindowSizeMultipleResource withMultipleTargetResources(Collection<? extends Resource> resources);
        }

        /** The stage of the definition which specifies monitoring window for metric alert. */
        interface WithWindowSize {
            /**
             * Sets the period of time (in ISO 8601 duration format) that is used to monitor alert activity based on the
             * threshold.
             *
             * @param size the windowSize value to set
             * @return the next stage of metric alert definition.
             */
            WithEvaluationFrequency withPeriod(Duration size);
        }

        /** The stage of the definition which specifies evaluation frequency for metric alert. */
        interface WithEvaluationFrequency {
            /**
             * Sets how often the metric alert is evaluated represented in ISO 8601 duration format.
             *
             * @param frequency the evaluationFrequency value to set.
             * @return the next stage of metric alert definition.
             */
            WithSeverity withFrequency(Duration frequency);
        }

        /** The stage of the definition which specifies severity for metric alert. */
        interface WithSeverity {
            /**
             * Sets alert severity {0, 1, 2, 3, 4} and description.
             *
             * @param severity the severity value to set
             * @param description Human readable text description of the metric alert.
             * @return the next stage of metric alert definition.
             */
            WithActionGroup withAlertDetails(int severity, String description);
        }

        /**
         * The stage of the definition which specifies actions that will be activated when the conditions are met in the
         * metric alert rules.
         */
        interface WithActionGroup {
            /**
             * Sets the actions that will activate when the condition is met.
             *
             * @param actionGroupId resource Ids of the {@link ActionGroup}.
             * @return the next stage of metric alert definition.
             */
            WithCriteriaDefinition withActionGroups(String... actionGroupId);
        }

        /** The stage of the definition which specifies condition that will cause this alert to activate. */
        interface WithCriteriaDefinition {
            /**
             * Starts definition of the metric alert condition.
             *
             * @param name sets the name of the condition.
             * @return the next stage of metric alert condition definition.
             */
            MetricAlertCondition.DefinitionStages.Blank.MetricName<WithCreate> defineAlertCriteria(String name);
        }

        /** The stage of the definition which specifies monitoring window for metric alert. */
        interface WithWindowSizeMultipleResource {
            /**
             * Sets the period of time (in ISO 8601 duration format) that is used to monitor alert activity based on the
             * threshold.
             *
             * @param size the windowSize value to set
             * @return the next stage of metric alert definition.
             */
            WithEvaluationFrequencyMultipleResource withPeriod(Duration size);
        }

        /** The stage of the definition which specifies evaluation frequency for metric alert. */
        interface WithEvaluationFrequencyMultipleResource {
            /**
             * Sets how often the metric alert is evaluated represented in ISO 8601 duration format.
             *
             * @param frequency the evaluationFrequency value to set.
             * @return the next stage of metric alert definition.
             */
            WithSeverityMultipleResource withFrequency(Duration frequency);
        }

        /** The stage of the definition which specifies severity for metric alert. */
        interface WithSeverityMultipleResource {
            /**
             * Sets alert severity {0, 1, 2, 3, 4} and description.
             *
             * @param severity the severity value to set
             * @param description Human readable text description of the metric alert.
             * @return the next stage of metric alert definition.
             */
            WithActionGroupMultipleResource withAlertDetails(int severity, String description);
        }

        /**
         * The stage of the definition which specifies actions that will be activated when the conditions are met in the
         * metric alert rules.
         */
        interface WithActionGroupMultipleResource {
            /**
             * Sets the actions that will activate when the condition is met.
             *
             * @param actionGroupId resource Ids of the {@link ActionGroup}.
             * @return the next stage of metric alert definition.
             */
            WithCriteriaDefinitionMultipleResource withActionGroups(String... actionGroupId);
        }

        /** The stage of the definition which specifies condition that will cause this alert to activate. */
        interface WithCriteriaDefinitionMultipleResource {
            /**
             * Starts definition of the metric alert condition.
             *
             * @param name sets the name of the condition.
             * @return the next stage of metric alert condition definition.
             */
            MetricAlertCondition.DefinitionStages.Blank.MetricName<WithCreate> defineAlertCriteria(String name);

            /**
             * Starts definition of the metric dynamic alert condition.
             *
             * @param name sets the name of the dynamic condition.
             * @return the next stage of metric alert condition definition.
             */
            MetricDynamicAlertCondition.DefinitionStages.Blank.MetricName<WithCreate> defineDynamicAlertCriteria(
                String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created but
         * also allows for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<MetricAlert>, DefinitionWithTags<WithCreate>, WithCriteriaDefinition {
            /**
             * Sets the flag that indicates the alert should not be auto resolved.
             *
             * @return the next stage of metric alert condition definition.
             */
            WithCreate withoutAutoMitigation();

            /**
             * Sets metric alert as disabled during the creation.
             *
             * @return the next stage of metric alert definition.
             */
            WithActionGroup withRuleDisabled();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created but
         * also allows for any other optional settings to be specified.
         */
        interface WithCreateDynamicCondition extends Creatable<MetricAlert>, DefinitionWithTags<WithCreate> {
            /**
             * Sets the flag that indicates the alert should not be auto resolved.
             *
             * @return the next stage of metric alert condition definition.
             */
            WithCreate withoutAutoMitigation();

            /**
             * Sets metric alert as disabled during the creation.
             *
             * @return the next stage of metric alert definition.
             */
            WithActionGroup withRuleDisabled();
        }
    }

    /** Grouping of metric alerts update stages. */
    interface UpdateStages {
        /** The stage of a metric alerts update allowing to modify settings. */
        interface WithMetricUpdate {
            /**
             * Sets the period of time (in ISO 8601 duration format) that is used to monitor alert activity based on the
             * threshold.
             *
             * @param size the windowSize value to set
             * @return the next stage of the metric alert update.
             */
            Update withPeriod(Duration size);

            /**
             * Sets how often the metric alert is evaluated represented in ISO 8601 duration format.
             *
             * @param frequency the evaluationFrequency value to set.
             * @return the next stage of the metric alert update.
             */
            Update withFrequency(Duration frequency);

            /**
             * Sets alert severity {0, 1, 2, 3, 4}.
             *
             * @param severity the severity value to set
             * @return the next stage of the metric alert update.
             */
            Update withSeverity(int severity);

            /**
             * Sets description for metric alert.
             *
             * @param description Human readable text description of the metric alert.
             * @return the next stage of the metric alert update.
             */
            Update withDescription(String description);

            /**
             * Sets metric alert as enabled.
             *
             * @return the next stage of the metric alert update.
             */
            Update withRuleEnabled();

            /**
             * Sets metric alert as disabled.
             *
             * @return the next stage of the metric alert update.
             */
            Update withRuleDisabled();

            /**
             * Sets the actions that will activate when the condition is met.
             *
             * @param actionGroupId resource Ids of the {@link ActionGroup}.
             * @return the next stage of the metric alert update.
             */
            Update withActionGroups(String... actionGroupId);

            /**
             * Removes the specified action group from the actions list.
             *
             * @param actionGroupId resource Id of the {@link ActionGroup} to remove.
             * @return the next stage of the metric alert update.
             */
            Update withoutActionGroup(String actionGroupId);

            /**
             * Starts definition of the metric alert condition.
             *
             * @param name sets the name of the condition.
             * @return the next stage of the metric alert update.
             */
            MetricAlertCondition.UpdateDefinitionStages.Blank.MetricName<Update> defineAlertCriteria(String name);

            /**
             * Starts definition of the metric dynamic alert condition.
             *
             * @param name sets the name of the condition.
             * @return the next stage of the metric alert update.
             */
            MetricDynamicAlertCondition.UpdateDefinitionStages.Blank.MetricName<Update> defineDynamicAlertCriteria(
                String name);

            /**
             * Starts update of the previously defined metric alert condition.
             *
             * @param name name of the condition that should be updated.
             * @return the next stage of the metric alert update.
             */
            MetricAlertCondition.UpdateStages updateAlertCriteria(String name);

            /**
             * Starts update of the previously defined metric dynamic alert condition.
             *
             * @param name name of the condition that should be updated.
             * @return the next stage of the metric alert update.
             */
            MetricDynamicAlertCondition.UpdateStages updateDynamicAlertCriteria(String name);

            /**
             * Removes a condition from the previously defined metric alert conditions.
             *
             * @param name name of the condition that should be removed.
             * @return the next stage of the metric alert update.
             */
            Update withoutAlertCriteria(String name);

            /**
             * Sets the flag that indicates the alert should be auto resolved.
             *
             * @return the next stage of the metric alert update.
             */
            Update withAutoMitigation();

            /**
             * Sets the flag that indicates the alert should not be auto resolved.
             *
             * @return the next stage of the metric alert update.
             */
            Update withoutAutoMitigation();
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update extends Appliable<MetricAlert>, UpdateStages.WithMetricUpdate, Resource.UpdateWithTags<Update> {
    }
}
