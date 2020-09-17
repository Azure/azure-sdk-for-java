// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.time.OffsetDateTime;
import java.util.Collection;

/** An immutable client-side representation of an Azure metric alert criteria. */
@Fluent
public interface MetricDynamicAlertCondition extends HasInner<DynamicMetricCriteria>, HasParent<MetricAlert> {

    /**
     * Get name of the criteria.
     *
     * @return the name value
     */
    String name();

    /**
     * Get name of the metric signal.
     *
     * @return the metricName value
     */
    String metricName();

    /**
     * Get namespace of the metric.
     *
     * @return the metricNamespace value
     */
    String metricNamespace();

    /**
     * Get the operator used to compare the metric value against the threshold.
     *
     * @return the operator value
     */
    DynamicThresholdOperator condition();

    /**
     * Get the criteria time aggregation types. Possible values include: 'Average', 'Minimum', 'Maximum', 'Total'.
     *
     * @return the timeAggregation value
     */
    MetricAlertRuleTimeAggregation timeAggregation();

    /**
     * Get the extent of deviation required to trigger an alert. This will affect how tight the threshold is to the
     * metric series pattern.
     *
     * @return the threshold value
     */
    DynamicThresholdSensitivity alertSensitivity();

    /**
     * Get list of dimension conditions.
     *
     * @return the dimensions value
     */
    Collection<MetricDimension> dimensions();

    /**
     * Get the minimum number of violations required within the selected lookback time window required to raise an
     * alert.
     *
     * @return the failingPeriods value
     */
    DynamicThresholdFailingPeriods failingPeriods();

    /**
     * Get the date from which to start learning the metric historical data and calculate the dynamic thresholds (in
     * ISO8601 format).
     *
     * @return the ignoreDataBefore value
     */
    OffsetDateTime ignoreDataBefore();

    /** Grouping of metric alerts condition definition stages. */
    interface DefinitionStages {
        /** The first stage of a Metric Alert condition definition. */
        interface Blank {
            /**
             * The stage of the definition which specifies metric signal name.
             *
             * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this
             *     definition
             */
            interface MetricName<ParentT> {
                /**
                 * Sets the name of the signal name to monitor.
                 *
                 * @param metricName metric name of the signal.
                 * @return the next stage of metric alert condition definition.
                 */
                WithCriteriaOperator<ParentT> withMetricName(String metricName);

                /**
                 * Sets the name of the signal name to monitor.
                 *
                 * @param metricName metric name of the signal.
                 * @param metricNamespace the Namespace of the metric.
                 * @return the next stage of metric alert condition definition.
                 */
                WithCriteriaOperator<ParentT> withMetricName(String metricName, String metricNamespace);
            }
        }

        /**
         * The stage of the definition which specifies metric alert condition.
         *
         * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this definition
         */
        interface WithCriteriaOperator<ParentT> {
            /**
             * Sets the condition to monitor for the current metric alert.
             *
             * @param timeAggregation the criteria time aggregation types.
             * @param condition the criteria operator used to compare the metric value against the threshold.
             * @param alertSensitivity the extent of deviation required to trigger an alert.
             * @return the next stage of metric alert condition definition.
             */
            WithFailingPeriods<ParentT> withCondition(
                MetricAlertRuleTimeAggregation timeAggregation,
                DynamicThresholdOperator condition,
                DynamicThresholdSensitivity alertSensitivity);
        }

        /**
         * The stage of the definition which specifies metric alert condition.
         *
         * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this definition
         */
        interface WithFailingPeriods<ParentT> {
            /**
             * Sets the failing periods for triggering the alert.
             *
             * @param failingPeriods the failing periods for triggering the alert.
             * @return the next stage of metric alert condition definition.
             */
            WithConditionAttach<ParentT> withFailingPeriods(DynamicThresholdFailingPeriods failingPeriods);
        }

        /**
         * The stage of the definition which specifies metric alert additional filtering options.
         *
         * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this definition
         */
        interface WithConditionAttach<ParentT> {
            /**
             * Sets the date from which to start learning the metric historical data and calculate the dynamic
             * thresholds.
             *
             * @param dateTime the date from which to start learning the metric historical data and calculate the
             *     dynamic thresholds.
             * @return the next stage of metric alert condition definition.
             */
            WithConditionAttach<ParentT> withIgnoreDataBefore(OffsetDateTime dateTime);

            /**
             * Adds a metric dimension filter.
             *
             * @param dimensionName the name of the dimension.
             * @param values list of dimension values to alert on.
             * @return the next stage of metric alert condition definition.
             */
            WithConditionAttach<ParentT> withDimension(String dimensionName, String... values);

            /**
             * Attaches the defined condition to the parent metric alert.
             *
             * @return the next stage of metric alert definition.
             */
            ParentT attach();
        }
    }

    /** The entirety of a metric alert condition definition as a part of a parent metric alert update. */
    interface UpdateDefinitionStages {
        /** The first stage of a Metric Alert condition definition. */
        interface Blank {
            /**
             * The stage of the definition which specifies metric signal name. *
             *
             * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this
             *     definition
             */
            interface MetricName<ParentT> {
                /**
                 * Sets the name of the signal name to monitor.
                 *
                 * @param metricName metric name of the signal.
                 * @return the next stage of metric alert condition definition.
                 */
                WithCriteriaOperator<ParentT> withMetricName(String metricName);

                /**
                 * Sets the name of the signal name to monitor.
                 *
                 * @param metricName metric name of the signal.
                 * @param metricNamespace the Namespace of the metric.
                 * @return the next stage of metric alert condition definition.
                 */
                WithCriteriaOperator<ParentT> withMetricName(String metricName, String metricNamespace);
            }
        }

        /**
         * The stage of the definition which specifies metric alert condition.
         *
         * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this definition
         */
        interface WithCriteriaOperator<ParentT> {
            /**
             * Sets the condition to monitor for the current metric alert.
             *
             * @param timeAggregation the criteria time aggregation types.
             * @param condition the criteria operator used to compare the metric value against the threshold.
             * @param alertSensitivity the extent of deviation required to trigger an alert.
             * @return the next stage of metric alert condition definition.
             */
            WithFailingPeriods<ParentT> withCondition(
                MetricAlertRuleTimeAggregation timeAggregation,
                DynamicThresholdOperator condition,
                DynamicThresholdSensitivity alertSensitivity);
        }

        /**
         * The stage of the definition which specifies metric alert condition.
         *
         * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this definition
         */
        interface WithFailingPeriods<ParentT> {
            /**
             * Sets the failing periods for triggering the alert.
             *
             * @param failingPeriods the failing periods for triggering the alert.
             * @return the next stage of metric alert condition definition.
             */
            WithConditionAttach<ParentT> withFailingPeriods(DynamicThresholdFailingPeriods failingPeriods);
        }

        /**
         * The stage of the definition which specifies metric alert additional filtering options.
         *
         * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this definition
         */
        interface WithConditionAttach<ParentT> {
            /**
             * Sets the date from which to start learning the metric historical data and calculate the dynamic
             * thresholds.
             *
             * @param date the date from which to start learning the metric historical data and calculate the dynamic
             *     thresholds.
             * @return the next stage of metric alert condition definition.
             */
            WithConditionAttach<ParentT> withIgnoreDataBefore(OffsetDateTime date);

            /**
             * Adds a metric dimension filter.
             *
             * @param dimensionName the name of the dimension.
             * @param values list of dimension values to alert on.
             * @return the next stage of metric alert condition definition.
             */
            WithConditionAttach<ParentT> withDimension(String dimensionName, String... values);

            /**
             * Attaches the defined condition to the parent metric alert.
             *
             * @return the next stage of metric alert definition.
             */
            ParentT attach();
        }
    }

    /** Grouping of metric alert condition update stages. */
    interface UpdateStages {
        /**
         * Sets the condition to monitor for the current metric alert.
         *
         * @param condition the criteria operator.
         * @param timeAggregation the criteria time aggregation types.
         * @param sensitivity the threshold sensitivity that activates the alert.
         * @return the next stage of metric alert condition definition.
         */
        UpdateStages withCondition(
            MetricAlertRuleTimeAggregation timeAggregation,
            DynamicThresholdOperator condition,
            DynamicThresholdSensitivity sensitivity);

        /**
         * Adds a metric dimension filter.
         *
         * @param dimensionName the name of the dimension.
         * @param values list of dimension values to alert on.
         * @return the next stage of the metric alert condition update.
         */
        UpdateStages withDimension(String dimensionName, String... values);

        /**
         * Removes the specified dimension filter.
         *
         * @param dimensionName dimensionName the name of the dimension.
         * @return the next stage of the metric alert condition update.
         */
        UpdateStages withoutDimension(String dimensionName);

        /**
         * Sets the failing periods for triggering the alert.
         *
         * @param failingPeriods the failing periods for triggering the alert.
         * @return the next stage of metric alert condition definition.
         */
        UpdateStages withFailingPeriods(DynamicThresholdFailingPeriods failingPeriods);

        /**
         * Sets the date from which to start learning the metric historical data and calculate the dynamic thresholds.
         *
         * @param date the date from which to start learning the metric historical data and calculate the dynamic
         *     thresholds.
         * @return the next stage of metric alert condition definition.
         */
        UpdateStages withIgnoreDataBefore(OffsetDateTime date);

        /**
         * Removes the date from which to start learning the metric historical data and calculate the dynamic
         * thresholds.
         *
         * @return the next stage of the metric alert condition update.
         */
        UpdateStages withoutIgnoreDataBefore();

        /**
         * Returns back to the metric alert update flow.
         *
         * @return the next stage of the metric alert update.
         */
        MetricAlert.Update parent();
    }
}
