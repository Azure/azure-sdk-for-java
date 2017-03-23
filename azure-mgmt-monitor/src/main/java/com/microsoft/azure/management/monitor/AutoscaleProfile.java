/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.monitor.implementation.AutoscaleProfileInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import org.joda.time.DateTime;

import java.util.List;

/**
 */
@Fluent
public interface AutoscaleProfile extends
        ExternalChildResource<AutoscaleProfile, AutoscaleSetting>,
        HasInner<AutoscaleProfileInner> {

    /**
     * the name of the profile.
     */
    String name();

    /**
     * the number of instances that can be used during this profile.
     */
    ScaleCapacity capacity();

    /**
     * the collection of rules that provide the triggers and parameters for the scaling action. A maximum of 10 rules can be specified.
     */
    List<ScaleRule> rules();

    /**
     * the specific date-time for the profile. This element is not used if the Recurrence element is used.
     */
    TimeWindow fixedDate();

    /**
     * the repeating times at which this profile begins. This element is not used if the FixedDate element is used.
     */
    Recurrence recurrence();

    interface Definition extends
            DefinitionStages.WithAttach,
            DefinitionStages.Blank,
            DefinitionStages.WithScaleRule,
            DefinitionStages.WithScaleRuleOptional {
    }

    interface DefinitionStages {
        interface WithAttach extends
                Attachable.InDefinition<AutoscaleSetting.DefinitionStages.WithCreate> {
        }

        interface Blank {
            WithScaleRule withScaleCapacity(String capacityMinimum, String capacityMaximum, String capacityDefault);
        }

        interface WithScaleRule {
            ScaleRule.DefinitionStages.Blank defineScaleRule();
        }

        interface WithScaleRuleOptional extends
                WithAttach {
            ScaleRule.DefinitionStages.Blank defineScaleRule();
            WithScaleRuleOptional withFixedDate(DateTime start, DateTime end);
            WithScaleRuleOptional withFixedDate(DateTime start, DateTime end, String timeZone);
            WithScaleRuleOptional withRecurrence(Recurrence recurrence);
            Recurrence.DefinitionStages.Blank defineRecurrence();
        }
    }

    interface UpdateDefinition extends
            UpdateDefinitionStages.WithAttach,
            UpdateDefinitionStages.Blank,
            UpdateDefinitionStages.WithScaleRule,
            UpdateDefinitionStages.WithScaleRuleOptional {
    }

    interface UpdateDefinitionStages {
        interface WithAttach extends
                Attachable.InUpdate<AutoscaleSetting.Update> {
        }

        interface Blank {
            WithScaleRule withScaleCapacity(String capacityMinimum, String capacityMaximum, String capacityDefault);
        }

        interface WithScaleRule {
            ScaleRule.ParentUpdateDefinitionStages.Blank defineScaleRule();
        }

        interface WithScaleRuleOptional extends
                WithAttach {
            ScaleRule.ParentUpdateDefinitionStages.Blank defineScaleRule();
            WithScaleRuleOptional withFixedDate(DateTime start, DateTime end);
            WithScaleRuleOptional withFixedDate(DateTime start, DateTime end, String timeZone);
            WithScaleRuleOptional withRecurrence(Recurrence recurrence);
            Recurrence.DefinitionStages.Blank defineRecurrence();
        }
    }

    interface Update extends
            Settable<AutoscaleSetting.Update>,
            UpdateStages.WithName,
            UpdateStages.WithScaleCapacity,
            UpdateStages.WithScaleRule,
            UpdateStages.WithTimeWindow,
            UpdateStages.WithRecurrence {
    }

    interface UpdateStages {
        interface WithName {
            Update withName(String Name);
        }

        interface WithScaleCapacity {
            Update withScaleCapacity(String capacityMinimum, String capacityMaximum, String capacityDefault);
        }

        interface WithScaleRule {
            Update withoutScaleRule(ScaleRule scaleRule);
            ScaleRule.Update updateScaleRule(ScaleRule scaleRule);
            ScaleRule.UpdateDefinitionStages.Blank defineScaleRule();
        }

        interface WithTimeWindow {
            Update withFixedDate(DateTime start, DateTime end);
            Update withFixedDate(DateTime start, DateTime end, String timeZone);
            Update withoutFixedDate();
        }

        interface WithRecurrence {
            Update withoutRecurrence();
            Recurrence.UpdateStages.Blank updateRecurrence();
            Recurrence.UpdateDefinitionStages.Blank defineRecurrence();
        }
    }

}
