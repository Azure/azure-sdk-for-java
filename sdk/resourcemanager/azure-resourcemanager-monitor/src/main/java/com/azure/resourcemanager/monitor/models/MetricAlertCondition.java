// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.Collection;

/** An immutable client-side representation of an Azure metric dynamic alert criteria. */
@Fluent
public interface MetricAlertCondition extends HasInner<MetricCriteria>, HasParent<MetricAlert> {
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
     * Get the criteria operator. Possible values include: 'Equals', 'NotEquals', 'GreaterThan', 'GreaterThanOrEqual',
     * 'LessThan', 'LessThanOrEqual'.
     *
     * @return the operator value
     */
    MetricAlertRuleCondition condition();

    /**
     * Get the criteria time aggregation types. Possible values include: 'Average', 'Minimum', 'Maximum', 'Total'.
     *
     * @return the timeAggregation value
     */
    MetricAlertRuleTimeAggregation timeAggregation();

    /**
     * Get the criteria threshold value that activates the alert.
     *
     * @return the threshold value
     */
    double threshold();

    /**
     * Get list of dimension conditions.
     *
     * @return the dimensions value
     */
    Collection<MetricDimension> dimensions();

    /** Grouping of metric alerts condition definition stages. */
    interface DefinitionStages {
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
             * @param timeAggregation the criteria time aggregation types. Possible values include: 'Average',
             *     'Minimum', 'Maximum', 'Total'.
             * @param condition the criteria operator. Possible values include: 'Equals', 'NotEquals', 'GreaterThan',
             *     'GreaterThanOrEqual', 'LessThan', 'LessThanOrEqual'.
             * @param threshold the criteria threshold value that activates the alert.
             * @return the next stage of metric alert condition definition.
             */
            WithConditionAttach<ParentT> withCondition(
                MetricAlertRuleTimeAggregation timeAggregation, MetricAlertRuleCondition condition, double threshold);
        }

        /**
         * The stage of the definition which specifies metric alert additional filtering options.
         *
         * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this definition
         */
        interface WithConditionAttach<ParentT> {
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
             * @param condition the criteria operator. Possible values include: 'Equals', 'NotEquals', 'GreaterThan',
             *     'GreaterThanOrEqual', 'LessThan', 'LessThanOrEqual'.
             * @param timeAggregation the criteria time aggregation types. Possible values include: 'Average',
             *     'Minimum', 'Maximum', 'Total'.
             * @param threshold the criteria threshold value that activates the alert.
             * @return the next stage of metric alert condition definition.
             */
            WithConditionAttach<ParentT> withCondition(
                MetricAlertRuleTimeAggregation timeAggregation, MetricAlertRuleCondition condition, double threshold);
        }

        /**
         * The stage of the definition which specifies metric alert additional filtering options.
         *
         * @param <ParentT> the stage of the parent Metric Alert definition to return to after attaching this definition
         */
        interface WithConditionAttach<ParentT> {
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
         * @param condition the criteria operator. Possible values include: 'Equals', 'NotEquals', 'GreaterThan',
         *     'GreaterThanOrEqual', 'LessThan', 'LessThanOrEqual'.
         * @param timeAggregation the criteria time aggregation types. Possible values include: 'Average', 'Minimum',
         *     'Maximum', 'Total'.
         * @param threshold the criteria threshold value that activates the alert.
         * @return the next stage of the metric alert condition update.
         */
        UpdateStages withCondition(
            MetricAlertRuleTimeAggregation timeAggregation, MetricAlertRuleCondition condition, double threshold);

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
         * Returns back to the metric alert update flow.
         *
         * @return the next stage of the metric alert update.
         */
        MetricAlert.Update parent();
    }
}
