/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import java.util.Map;

/**
 */
@Fluent
public interface ScaleRule {

    MetricTrigger metricTrigger();

    ScaleAction scaleAction();

    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithScaleAction<ParentT>,
            DefinitionStages.WithApply<ParentT> {
    }

    interface DefinitionStages {
        interface Blank<ParentT> {
            MetricTrigger.DefinitionStages.Blank<WithScaleAction<ParentT>> defineMetricTrigger(String name);
        }

        interface WithScaleAction<ParentT> {
            ScaleAction.DefinitionStages.Blank<WithApply<ParentT>> defineScaleAction();
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
            MetricTrigger.UpdateStages.Blank<Update<ParentT>> updateMetricTrigger();
            ScaleAction.UpdateStages.Blank<Update<ParentT>> updateScaleAction();
            ParentT apply();
        }
    }
}
