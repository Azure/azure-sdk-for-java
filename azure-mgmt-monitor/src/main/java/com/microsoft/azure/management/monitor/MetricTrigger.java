/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import org.joda.time.Period;

/**
 */
@Fluent
public interface MetricTrigger {

    StandaloneUpdateStages.Blank update();

    interface StandaloneDefinition extends
            StandaloneDefinitionStages.WithApply,
            StandaloneDefinitionStages.Blank,
            StandaloneDefinitionStages.WithTimeGrain,
            StandaloneDefinitionStages.WithStatistic,
            StandaloneDefinitionStages.WithTimeWindow,
            StandaloneDefinitionStages.WithTimeAggregation,
            StandaloneDefinitionStages.WithOperator,
            StandaloneDefinitionStages.WithThreshold {
    }

    interface StandaloneDefinitionStages {
        interface Blank {
            WithTimeGrain withMetricResourceUri(String metricResourceUri);
        }

        interface WithTimeGrain {
            WithStatistic withTimeGrain(Period timeGrain);
        }

        interface WithStatistic {
            WithTimeWindow withStatistic(MetricStatisticType statistic);
        }

        interface WithTimeWindow {
            WithTimeAggregation withTimeWindow(Period timeWindow);
        }

        interface WithTimeAggregation {
            WithOperator withTimeAggregation(TimeAggregationType timeAggregation);
        }

        interface WithOperator {
            WithThreshold withOperator(ComparisonOperationType operator);
        }

        interface WithThreshold {
            WithApply withThreshold(double threshold);
        }

        interface WithApply {
            MetricTrigger create();
        }
    }

    interface StandaloneUpdate extends
            StandaloneUpdateStages.Blank {
    }

    interface StandaloneUpdateStages {
        interface Blank {
            StandaloneUpdate withMetricName(String metricName);
            StandaloneUpdate withMetricResourceUri(String metricResourceUri);
            StandaloneUpdate withTimeGrain(Period timeGrain);
            StandaloneUpdate withStatistic(MetricStatisticType statistic);
            StandaloneUpdate withTimeWindow(Period timeWindow);
            StandaloneUpdate withTimeAggregation(TimeAggregationType timeAggregation);
            StandaloneUpdate withOperator(ComparisonOperationType operator);
            StandaloneUpdate withThreshold(double threshold);
            MetricTrigger apply();
        }
    }

    interface Definition extends
            DefinitionStages.WithApply,
            DefinitionStages.Blank,
            DefinitionStages.WithTimeGrain,
            DefinitionStages.WithStatistic,
            DefinitionStages.WithTimeWindow,
            DefinitionStages.WithTimeAggregation,
            DefinitionStages.WithOperator,
            DefinitionStages.WithThreshold {
    }

    interface DefinitionStages {
        interface Blank {
            WithTimeGrain withMetricResourceUri(String metricResourceUri);
        }

        interface WithTimeGrain {
            WithStatistic withTimeGrain(Period timeGrain);
        }

        interface WithStatistic {
            WithTimeWindow withStatistic(MetricStatisticType statistic);
        }

        interface WithTimeWindow {
            WithTimeAggregation withTimeWindow(Period timeWindow);
        }

        interface WithTimeAggregation {
            WithOperator withTimeAggregation(TimeAggregationType timeAggregation);
        }

        interface WithOperator {
            WithThreshold withOperator(ComparisonOperationType operator);
        }

        interface WithThreshold {
            WithApply withThreshold(double threshold);
        }

        interface WithApply {
            ScaleRule.DefinitionStages.WithScaleAction attach();
        }
    }

    interface ParentUpdateDefinition extends
            ParentUpdateDefinitionStages.WithApply,
            ParentUpdateDefinitionStages.Blank,
            ParentUpdateDefinitionStages.WithTimeGrain,
            ParentUpdateDefinitionStages.WithStatistic,
            ParentUpdateDefinitionStages.WithTimeWindow,
            ParentUpdateDefinitionStages.WithTimeAggregation,
            ParentUpdateDefinitionStages.WithOperator,
            ParentUpdateDefinitionStages.WithThreshold {
    }

    interface ParentUpdateDefinitionStages {
        interface Blank {
            WithTimeGrain withMetricResourceUri(String metricResourceUri);
        }

        interface WithTimeGrain {
            WithStatistic withTimeGrain(Period timeGrain);
        }

        interface WithStatistic {
            WithTimeWindow withStatistic(MetricStatisticType statistic);
        }

        interface WithTimeWindow {
            WithTimeAggregation withTimeWindow(Period timeWindow);
        }

        interface WithTimeAggregation {
            WithOperator withTimeAggregation(TimeAggregationType timeAggregation);
        }

        interface WithOperator {
            WithThreshold withOperator(ComparisonOperationType operator);
        }

        interface WithThreshold {
            WithApply withThreshold(double threshold);
        }

        interface WithApply {
            ScaleRule.ParentUpdateDefinitionStages.WithScaleAction attach();
        }
    }

    interface UpdateDefinition extends
            UpdateDefinitionStages.WithApply,
            UpdateDefinitionStages.Blank,
            UpdateDefinitionStages.WithTimeGrain,
            UpdateDefinitionStages.WithStatistic,
            UpdateDefinitionStages.WithTimeWindow,
            UpdateDefinitionStages.WithTimeAggregation,
            UpdateDefinitionStages.WithOperator,
            UpdateDefinitionStages.WithThreshold {
    }

    interface UpdateDefinitionStages {
        interface Blank {
            WithTimeGrain withMetricResourceUri(String metricResourceUri);
        }

        interface WithTimeGrain {
            WithStatistic withTimeGrain(Period timeGrain);
        }

        interface WithStatistic {
            WithTimeWindow withStatistic(MetricStatisticType statistic);
        }

        interface WithTimeWindow {
            WithTimeAggregation withTimeWindow(Period timeWindow);
        }

        interface WithTimeAggregation {
            WithOperator withTimeAggregation(TimeAggregationType timeAggregation);
        }

        interface WithOperator {
            WithThreshold withOperator(ComparisonOperationType operator);
        }

        interface WithThreshold {
            WithApply withThreshold(double threshold);
        }

        interface WithApply {
            ScaleRule.UpdateDefinitionStages.WithScaleAction attach();
        }
    }

    interface Update extends
            UpdateStages.Blank {
    }

    interface UpdateStages {
        interface Blank {
            Update withMetricName(String metricName);
            Update withMetricResourceUri(String metricResourceUri);
            Update withTimeGrain(Period timeGrain);
            Update withStatistic(MetricStatisticType statistic);
            Update withTimeWindow(Period timeWindow);
            Update withTimeAggregation(TimeAggregationType timeAggregation);
            Update withOperator(ComparisonOperationType operator);
            Update withThreshold(double threshold);
            ScaleRule.Update parent();
        }
    }
}
