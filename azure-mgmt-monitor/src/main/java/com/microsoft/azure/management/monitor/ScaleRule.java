/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import javafx.scene.Parent;
import org.joda.time.Period;

import java.util.Map;

/**
 */
@Fluent
public interface ScaleRule extends
        ChildResource<AutoscaleProfile>,
        Refreshable<ScaleRule>,
        Updatable<ScaleRule.Update> {

    MetricTrigger metricTrigger();

    ScaleAction scaleAction();

    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT> {
    }

    interface DefinitionStages {

        interface WithScaleRuleApply<ParentT> {
            AutoscaleProfile.DefinitionStages.WithScaleRuleOptional<ParentT> apply();
        }

        interface Blank<ParentT> {
            MetricTriggerDefinitionStages.WithMetricResourceUri<WithScaleAction<ParentT>> defineMetricTrigger(String name);
            WithScaleAction<ParentT> withExistingMetricTrigger(MetricTrigger metricTrigger);
        }

        interface WithScaleAction<ParentT> {
            ScaleActionDefinitionStages.WithDirection<WithScaleRuleApply<ParentT>> defineScaleAction();
            WithScaleRuleApply<ParentT>  withExistingScaleAction(ScaleAction scaleAction);
        }

        interface MetricTriggerDefinitionStages {
            interface WithMetricResourceUri<ParentT> {
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

        interface ScaleActionDefinitionStages {
            interface WithDirection<ParentT> {
                WithType<ParentT> withDirection(ScaleDirection direction);
            }

            interface WithType<ParentT> {
                WithCooldown<ParentT> withType(ScaleType type);
            }

            interface WithCooldown<ParentT> {
                WithValue<ParentT> withCooldown(Period cooldown);
            }

            interface WithValue<ParentT> {
                WithValue<ParentT> withValue(String value);
                ParentT apply();
            }
        }
    }

    interface Update<ParentT> extends
            UpdateStages.WithAttach<ParentT>,
            UpdateStages.Blank<ParentT> {
    }

    interface UpdateStages {

        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {
        }

        interface Blank<ParentT> {
            WithMetricTriggerUpdate<Blank<ParentT>> updateMetricTrigger();
            WithScaleActionUpdate<Blank<ParentT>> updateScaleAction();
            ParentT apply();
        }

        interface WithMetricTriggerUpdate<ParentT> {
            WithMetricTriggerUpdate<ParentT> withMetricName(String metricResourceUri);
            WithMetricTriggerUpdate<ParentT> withMetricResourceUri(String metricResourceUri);
            WithMetricTriggerUpdate<ParentT> withTimeGrain(Period timeGrain);
            WithMetricTriggerUpdate<ParentT> withStatistic(MetricStatisticType statistic);
            WithMetricTriggerUpdate<ParentT> withTimeWindow(Period timeWindow);
            WithMetricTriggerUpdate<ParentT> withTimeAggregation(TimeAggregationType timeAggregation);
            WithMetricTriggerUpdate<ParentT> withOperator(ComparisonOperationType operator);
            WithMetricTriggerUpdate<ParentT> withThreshold(double threshold);
            ParentT apply();
        }

        interface WithScaleActionUpdate<ParentT> {
            WithScaleActionUpdate<ParentT> withDirection(ScaleDirection direction);
            WithScaleActionUpdate<ParentT> withType(ScaleType type);
            WithScaleActionUpdate<ParentT> withCooldown(Period cooldown);
            WithScaleActionUpdate<ParentT> withValue(String value);
            ParentT apply();
        }
    }
}
