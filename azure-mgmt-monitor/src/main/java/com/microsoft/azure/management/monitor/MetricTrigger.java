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

    interface Definition<ParentT> extends
            DefinitionStages.WithApply<ParentT>,
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithTimeGrain<ParentT>,
            DefinitionStages.WithStatistic<ParentT>,
            DefinitionStages.WithTimeWindow<ParentT>,
            DefinitionStages.WithTimeAggregation<ParentT>,
            DefinitionStages.WithOperator<ParentT>,
            DefinitionStages.WithThreshold<ParentT> {
    }

    interface DefinitionStages {
        interface Blank<ParentT> {
            WithTimeGrain<ParentT> withMetricResourceUri(String metricResourceUri);
        }

        interface WithTimeGrain<ParentT> {
            WithStatistic<ParentT> withTimeGrain(Period timeGrain);
        }

        interface WithStatistic<ParentT> {
            WithTimeWindow<ParentT> withStatistic(MetricStatisticType statistic);
        }

        interface WithTimeWindow<ParentT> {
            WithTimeAggregation<ParentT> withTimeWindow(Period timeWindow);
        }

        interface WithTimeAggregation<ParentT> {
            WithOperator<ParentT> withTimeAggregation(TimeAggregationType timeAggregation);
        }

        interface WithOperator<ParentT> {
            WithThreshold<ParentT> withOperator(ComparisonOperationType operator);
        }

        interface WithThreshold<ParentT> {
            WithApply<ParentT> withThreshold(double threshold);
        }

        interface WithApply<ParentT> {
            ParentT apply();
        }
    }

    interface Update<ParentT> extends
            UpdateStages.Blank<ParentT> {
    }

    interface UpdateStages {
        interface Blank<ParentT> {
            Update<ParentT> withMetricName(String metricResourceUri);
            Update<ParentT> withMetricResourceUri(String metricResourceUri);
            Update<ParentT> withTimeGrain(Period timeGrain);
            Update<ParentT> withStatistic(MetricStatisticType statistic);
            Update<ParentT> withTimeWindow(Period timeWindow);
            Update<ParentT> withTimeAggregation(TimeAggregationType timeAggregation);
            Update<ParentT> withOperator(ComparisonOperationType operator);
            Update<ParentT> withThreshold(double threshold);
            ParentT apply();
        }
    }
}
