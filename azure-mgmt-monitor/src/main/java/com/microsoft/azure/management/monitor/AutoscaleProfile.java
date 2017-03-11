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
public interface AutoscaleProfile extends
        ChildResource<AutoscaleSetting>,
        Refreshable<AutoscaleProfile>,
        Updatable<AutoscaleProfile.Update> {

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
            DefinitionStages.Blank<ParentT> {
    }

    interface DefinitionStages {

        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {
        }

        interface Blank<ParentT> {
            WithScaleRule<ParentT> withScaleCapacity(String capacityMinimum, String capacityMaximum, String capacityDefault);
        }

        interface WithScaleRule<ParentT> {
            ScaleRule.DefinitionStages.Blank<ParentT> defineScaleRule();
        }

        interface WithScaleRuleOptional<ParentT> extends
                WithAttach<ParentT> {
            ScaleRule.DefinitionStages.Blank<ParentT> defineScaleRule();
            WithScaleRuleOptional<ParentT> withTimeWindow(DateTime start, DateTime end);
            WithScaleRuleOptional<ParentT> withTimeWindow(DateTime start, DateTime end, String timeZone);
            WithScaleRuleOptional<ParentT> withRecurrence(Recurrence recurrence);
            RecurrenceDefinitionStages.WithRecurrenceFrequency<WithScaleRuleOptional<ParentT>> defineRecurrence();
        }

        interface RecurrenceDefinitionStages {
            interface WithRecurrenceFrequency<ParentT> {
                WithRecurrentScheduleTimeZone<ParentT> withFrequency(RecurrenceFrequency frequency);
            }

            interface WithRecurrentScheduleTimeZone<ParentT> {
                WithRecurrentScheduleHours<ParentT> withScheduleTimeZone(String scheduleTimeZoney);
            }

            interface WithRecurrentScheduleHours<ParentT> {
                WithRecurrentScheduleMinutes<ParentT> withScheduleHours(int hours);
            }

            interface WithRecurrentScheduleMinutes<ParentT> {
                WithRecurrentScheduleDays<ParentT> withScheduleMinutes(int minutes);
            }

            interface WithRecurrentScheduleDays<ParentT> {
                WithRecurrentScheduleDaysApplicable<ParentT> withScheduleDay(String day);
                WithRecurrentScheduleDaysApplicable<ParentT> withScheduleDays(List<String> day);
            }

            interface WithRecurrentScheduleDaysApplicable<ParentT> extends
                    WithRecurrentScheduleDays<ParentT> {
                ParentT apply();
            }
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
        }

        interface WithTimeWindow<ParentT> {
            Update<ParentT> withTimeWindow(DateTime start, DateTime end);
            Update<ParentT> withTimeWindow(DateTime start, DateTime end, String timeZone);
            Update<ParentT> withoutTimeWindow();
        }

        interface WithRecurrence<ParentT> {
            UpdateRecurrance<Update<ParentT>> updateRecurrence();
            DefinitionStages.RecurrenceDefinitionStages.WithRecurrenceFrequency<Update<ParentT>> defineRecurrence();
            /**/
        }

        interface UpdateRecurrance<ParentT> {
            UpdateRecurrance<ParentT> withFrequency(RecurrenceFrequency frequency);
            UpdateRecurrance<ParentT> withScheduleTimeZone(String scheduleTimeZoney);
            UpdateRecurrance<ParentT> withScheduleHours(int hours);
            UpdateRecurrance<ParentT> withScheduleMinutes(int minutes);
            UpdateRecurrance<ParentT> withScheduleDay(String day);
            UpdateRecurrance<ParentT> withScheduleDays(List<String> day);
            UpdateRecurrance<ParentT> withoutScheduleDay(String day);
            ParentT apply();
        }
    }

}
