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
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 */
@Fluent
public interface AutoscaleProfile {

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


    interface Definition<ParentT> extends
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithScaleRule<ParentT>,
            DefinitionStages.WithScaleRuleOptional<ParentT> {
    }

    interface DefinitionStages {

        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {
        }

        interface Blank<ParentT> {
            WithScaleRule<ParentT> withScaleCapacity(String capacityMinimum, String capacityMaximum, String capacityDefault);
        }

        interface WithScaleRule<ParentT> {
            ScaleRule.DefinitionStages.Blank<WithScaleRuleOptional<ParentT>> defineScaleRule();
        }

        interface WithScaleRuleOptional<ParentT> extends
                WithAttach<ParentT> {
            ScaleRule.DefinitionStages.Blank<WithScaleRuleOptional<ParentT>> defineScaleRule();
            WithScaleRuleOptional<ParentT> withTimeWindow(DateTime start, DateTime end);
            WithScaleRuleOptional<ParentT> withTimeWindow(DateTime start, DateTime end, String timeZone);
            WithScaleRuleOptional<ParentT> withRecurrence(Recurrence recurrence);
            Recurrence.DefinitionStages.Blank<WithScaleRuleOptional<ParentT>> defineRecurrence();
        }
    }

    interface Update<ParentT> extends
            UpdateStages.WithAttach<ParentT>,
            UpdateStages.WithName<ParentT>,
            UpdateStages.WithScaleCapacity<ParentT>,
            UpdateStages.WithScaleRule<ParentT>,
            UpdateStages.WithTimeWindow<ParentT>,
            UpdateStages.WithRecurrence<ParentT> {
    }

    interface UpdateStages {

        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {
        }

        interface WithName<ParentT> {
            Update<ParentT> withName(String name);
        }

        interface WithScaleCapacity<ParentT> {
            Update<ParentT> withScaleCapacity(String capacityMinimum, String capacityMaximum, String capacityDefault);
        }

        interface WithScaleRule<ParentT> {
            Update<ParentT> withoutScaleRule(ScaleRule scaleRule);
            ScaleRule.Update<Update<ParentT>> updateScaleRule(ScaleRule scaleRule);
            ScaleRule.DefinitionStages.Blank<Update<ParentT>> defineScaleRule();
        }

        interface WithTimeWindow<ParentT> {
            Update<ParentT> withTimeWindow(DateTime start, DateTime end);
            Update<ParentT> withTimeWindow(DateTime start, DateTime end, String timeZone);
            Update<ParentT> withoutTimeWindow();
        }

        interface WithRecurrence<ParentT> {
            Update<ParentT> withoutRecurrence();
            Recurrence.UpdateStages.Blank<Update<ParentT>> updateRecurrence();
            Recurrence.DefinitionStages.Blank<Update<ParentT>> defineRecurrence();
        }
    }

}
