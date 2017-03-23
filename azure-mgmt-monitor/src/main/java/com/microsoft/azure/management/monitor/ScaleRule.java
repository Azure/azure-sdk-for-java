/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;

/**
 */
@Fluent
public interface ScaleRule {

    MetricTrigger metricTrigger();

    ScaleAction scaleAction();

    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithScaleAction,
            DefinitionStages.WithAttach {
    }

    interface DefinitionStages {
        interface Blank {
            MetricTrigger.DefinitionStages.Blank defineMetricTrigger(String name);
        }

        interface WithScaleAction {
            ScaleAction.DefinitionStages.Blank defineScaleAction();
        }

        interface WithAttach extends
                Attachable.InDefinition<AutoscaleProfile.DefinitionStages.WithScaleRuleOptional>{
        }
    }

    interface ParentUpdateDefinition extends
            ParentUpdateDefinitionStages.Blank,
            ParentUpdateDefinitionStages.WithScaleAction,
            ParentUpdateDefinitionStages.WithAttach {
    }

    interface ParentUpdateDefinitionStages {
        interface Blank {
            MetricTrigger.ParentUpdateDefinitionStages.Blank defineMetricTrigger(String name);
        }

        interface WithScaleAction {
            ScaleAction.ParentUpdateDefinitionStages.Blank defineScaleAction();
        }

        interface WithAttach {
            AutoscaleProfile.UpdateDefinitionStages.WithScaleRuleOptional attach();
        }
    }

    interface UpdateDefinition extends
            UpdateDefinitionStages.Blank,
            UpdateDefinitionStages.WithScaleAction,
            UpdateDefinitionStages.WithAttach {
    }

    interface UpdateDefinitionStages {
        interface Blank {
            MetricTrigger.UpdateDefinitionStages.Blank defineMetricTrigger(String name);
        }

        interface WithScaleAction {
            ScaleAction.UpdateDefinitionStages.Blank defineScaleAction();
        }

        interface WithAttach extends
                Attachable.InUpdate<AutoscaleProfile.Update>{
        }
    }

    interface Update extends
            Settable<AutoscaleProfile.Update>,
            UpdateStages.Blank {
    }

    interface UpdateStages {
        interface Blank {
            MetricTrigger.UpdateStages.Blank updateMetricTrigger();
            ScaleAction.UpdateStages.Blank updateScaleAction();
        }
    }
}
