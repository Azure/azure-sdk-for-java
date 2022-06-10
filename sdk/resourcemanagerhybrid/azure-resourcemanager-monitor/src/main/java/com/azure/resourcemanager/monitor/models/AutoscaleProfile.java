// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.monitor.fluent.models.AutoscaleProfileInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.time.OffsetDateTime;
import java.util.List;

/** An immutable client-side representation of an Azure autoscale profile. */
@Fluent
public interface AutoscaleProfile extends HasInnerModel<AutoscaleProfileInner>, HasParent<AutoscaleSetting>, HasName {
    /**
     * Get the minimum number of instances for the resource.
     *
     * @return the minimum value.
     */
    int minInstanceCount();

    /**
     * Get the maximum number of instances for the resource. The actual maximum number of instances is limited by the
     * cores that are available in the subscription.
     *
     * @return the maximum value.
     */
    int maxInstanceCount();

    /**
     * Get the number of instances that will be set if metrics are not available for evaluation. The default is only
     * used if the current instance count is lower than the default.
     *
     * @return the defaultProperty value.
     */
    int defaultInstanceCount();

    /**
     * Get the specific date-time for the profile. This element is not used if the Recurrence element is used.
     *
     * @return the fixedDate value.
     */
    TimeWindow fixedDateSchedule();

    /**
     * Get the repeating times at which this profile begins. This element is not used if the FixedDate element is used.
     *
     * @return the recurrence value.
     */
    Recurrence recurrentSchedule();

    /**
     * Get the collection of rules that provide the triggers and parameters for the scaling action. A maximum of 10
     * rules can be specified.
     *
     * @return the rules value.
     */
    List<ScaleRule> rules();

    /** The entirety of an autoscale profile definition. */
    interface Definition
        extends DefinitionStages.WithAttach,
            DefinitionStages.Blank,
            DefinitionStages.WithScaleRule,
            DefinitionStages.WithScaleRuleOptional,
            DefinitionStages.WithScaleSchedule {
    }

    /** Grouping of autoscale profile definition stages. */
    interface DefinitionStages {
        /** The final stage of the definition which attaches defined profile to the current Autoscale settings. */
        interface WithAttach extends Attachable.InDefinition<AutoscaleSetting.DefinitionStages.WithCreate> {
        }

        /** The first stage of autoscale profile definition. */
        interface Blank {
            /**
             * Selects metric based autoscale profile.
             *
             * @param minimumInstanceCount the minimum number of instances for the resource.
             * @param maximumInstanceCount the maximum number of instances for the resource. The actual maximum number
             *     of instances is limited by the cores that are available in the subscription.
             * @param defaultInstanceCount the number of instances that will be set if metrics are not available for
             *     evaluation. The default is only used if the current instance count is lower than the default.
             * @return the next stage of the definition.
             */
            WithScaleRule withMetricBasedScale(
                int minimumInstanceCount, int maximumInstanceCount, int defaultInstanceCount);

            /**
             * Selects schedule based autoscale profile.
             *
             * @param instanceCount the number of instances that will be set during specified schedule. The actual
             *     number of instances is limited by the cores that are available in the subscription.
             * @return the next stage of the definition.
             */
            WithScaleSchedule withScheduleBasedScale(int instanceCount);

            /**
             * Selects a specific instance count for the current Default profile.
             *
             * @param instanceCount the number of instances that will be set during specified schedule. The actual
             *     number of instances is limited by the cores that are available in the subscription.
             * @return the next stage of the definition.
             */
            WithAttach withFixedInstanceCount(int instanceCount);
        }

        /** The stage of the definition which adds scale rules. */
        interface WithScaleRule {
            /**
             * Starts the definition of scale rule for the current autoscale profile.
             *
             * @return the next stage of the definition.
             */
            ScaleRule.DefinitionStages.Blank defineScaleRule();
        }

        /** The stage of the definition which adds optional scale rules and schedules. */
        interface WithScaleRuleOptional extends WithAttach {
            /**
             * Starts the definition of scale rule for the current autoscale profile.
             *
             * @return the next stage of the definition.
             */
            ScaleRule.DefinitionStages.Blank defineScaleRule();

            /**
             * Specifies fixed date schedule for autoscale profile.
             *
             * @param timeZone time zone for the schedule.
             * @param start start time.
             * @param end end time.
             * @return the next stage of the definition.
             */
            WithScaleRuleOptional withFixedDateSchedule(String timeZone, OffsetDateTime start, OffsetDateTime end);

            /**
             * Specifies recurrent schedule for autoscale profile.
             *
             * @param scheduleTimeZone time zone for the schedule. Some examples of valid timezones are: Dateline
             *     Standard Time, UTC-11, Hawaiian Standard Time, Alaskan Standard Time, Pacific Standard Time (Mexico),
             *     Pacific Standard Time, US Mountain Standard Time, Mountain Standard Time (Mexico), Mountain Standard
             *     Time, Central America Standard Time, Central Standard Time, Central Standard Time (Mexico), Canada
             *     Central Standard Time, SA Pacific Standard Time, Eastern Standard Time, US Eastern Standard Time,
             *     Venezuela Standard Time, Paraguay Standard Time, Atlantic Standard Time, Central Brazilian Standard
             *     Time, SA Western Standard Time, Pacific SA Standard Time, Newfoundland Standard Time, E. South
             *     America Standard Time, Argentina Standard Time, SA Eastern Standard Time, Greenland Standard Time,
             *     Montevideo Standard Time, Bahia Standard Time, UTC-02, Mid-Atlantic Standard Time, Azores Standard
             *     Time, Cape Verde Standard Time, Morocco Standard Time, UTC, GMT Standard Time, Greenwich Standard
             *     Time, W. Europe Standard Time, Central Europe Standard Time, Romance Standard Time, Central European
             *     Standard Time, W. Central Africa Standard Time, Namibia Standard Time, Jordan Standard Time, GTB
             *     Standard Time, Middle East Standard Time, Egypt Standard Time, Syria Standard Time, E. Europe
             *     Standard Time, South Africa Standard Time, FLE Standard Time, Turkey Standard Time, Israel Standard
             *     Time, Kaliningrad Standard Time, Libya Standard Time, Arabic Standard Time, Arab Standard Time,
             *     Belarus Standard Time, Russian Standard Time, E. Africa Standard Time, Iran Standard Time, Arabian
             *     Standard Time, Azerbaijan Standard Time, Russia Time Zone 3, Mauritius Standard Time, Georgian
             *     Standard Time, Caucasus Standard Time, Afghanistan Standard Time, West Asia Standard Time,
             *     Ekaterinburg Standard Time, Pakistan Standard Time, India Standard Time, Sri Lanka Standard Time,
             *     Nepal Standard Time, Central Asia Standard Time, Bangladesh Standard Time, N. Central Asia Standard
             *     Time, Myanmar Standard Time, SE Asia Standard Time, North Asia Standard Time, China Standard Time,
             *     North Asia East Standard Time, Singapore Standard Time, W. Australia Standard Time, Taipei Standard
             *     Time, Ulaanbaatar Standard Time, Tokyo Standard Time, Korea Standard Time, Yakutsk Standard Time,
             *     Cen. Australia Standard Time, AUS Central Standard Time, E. Australia Standard Time, AUS Eastern
             *     Standard Time, West Pacific Standard Time, Tasmania Standard Time, Magadan Standard Time, Vladivostok
             *     Standard Time, Russia Time Zone 10, Central Pacific Standard Time, Russia Time Zone 11, New Zealand
             *     Standard Time, UTC+12, Fiji Standard Time, Kamchatka Standard Time, Tonga Standard Time, Samoa
             *     Standard Time, Line Islands Standard Time.
             * @param startTime start time in hh:mm format.
             * @param weekday list of week days when the schedule should be active.
             * @return the next stage of the definition.
             */
            WithScaleRuleOptional withRecurrentSchedule(
                String scheduleTimeZone, String startTime, DayOfWeek... weekday);
        }

        /** The stage of the definition which specifies autoscale profile schedule. */
        interface WithScaleSchedule {
            /**
             * Specifies fixed date schedule for autoscale profile.
             *
             * @param timeZone time zone for the schedule.
             * @param start start time.
             * @param end end time.
             * @return the next stage of the definition.
             */
            WithAttach withFixedDateSchedule(String timeZone, OffsetDateTime start, OffsetDateTime end);

            /**
             * Specifies recurrent schedule for autoscale profile.
             *
             * @param scheduleTimeZone time zone for the schedule. Some examples of valid timezones are: Dateline
             *     Standard Time, UTC-11, Hawaiian Standard Time, Alaskan Standard Time, Pacific Standard Time (Mexico),
             *     Pacific Standard Time, US Mountain Standard Time, Mountain Standard Time (Mexico), Mountain Standard
             *     Time, Central America Standard Time, Central Standard Time, Central Standard Time (Mexico), Canada
             *     Central Standard Time, SA Pacific Standard Time, Eastern Standard Time, US Eastern Standard Time,
             *     Venezuela Standard Time, Paraguay Standard Time, Atlantic Standard Time, Central Brazilian Standard
             *     Time, SA Western Standard Time, Pacific SA Standard Time, Newfoundland Standard Time, E. South
             *     America Standard Time, Argentina Standard Time, SA Eastern Standard Time, Greenland Standard Time,
             *     Montevideo Standard Time, Bahia Standard Time, UTC-02, Mid-Atlantic Standard Time, Azores Standard
             *     Time, Cape Verde Standard Time, Morocco Standard Time, UTC, GMT Standard Time, Greenwich Standard
             *     Time, W. Europe Standard Time, Central Europe Standard Time, Romance Standard Time, Central European
             *     Standard Time, W. Central Africa Standard Time, Namibia Standard Time, Jordan Standard Time, GTB
             *     Standard Time, Middle East Standard Time, Egypt Standard Time, Syria Standard Time, E. Europe
             *     Standard Time, South Africa Standard Time, FLE Standard Time, Turkey Standard Time, Israel Standard
             *     Time, Kaliningrad Standard Time, Libya Standard Time, Arabic Standard Time, Arab Standard Time,
             *     Belarus Standard Time, Russian Standard Time, E. Africa Standard Time, Iran Standard Time, Arabian
             *     Standard Time, Azerbaijan Standard Time, Russia Time Zone 3, Mauritius Standard Time, Georgian
             *     Standard Time, Caucasus Standard Time, Afghanistan Standard Time, West Asia Standard Time,
             *     Ekaterinburg Standard Time, Pakistan Standard Time, India Standard Time, Sri Lanka Standard Time,
             *     Nepal Standard Time, Central Asia Standard Time, Bangladesh Standard Time, N. Central Asia Standard
             *     Time, Myanmar Standard Time, SE Asia Standard Time, North Asia Standard Time, China Standard Time,
             *     North Asia East Standard Time, Singapore Standard Time, W. Australia Standard Time, Taipei Standard
             *     Time, Ulaanbaatar Standard Time, Tokyo Standard Time, Korea Standard Time, Yakutsk Standard Time,
             *     Cen. Australia Standard Time, AUS Central Standard Time, E. Australia Standard Time, AUS Eastern
             *     Standard Time, West Pacific Standard Time, Tasmania Standard Time, Magadan Standard Time, Vladivostok
             *     Standard Time, Russia Time Zone 10, Central Pacific Standard Time, Russia Time Zone 11, New Zealand
             *     Standard Time, UTC+12, Fiji Standard Time, Kamchatka Standard Time, Tonga Standard Time, Samoa
             *     Standard Time, Line Islands Standard Time.
             * @param startTime start time in hh:mm format.
             * @param weekday list of week days when the schedule should be active.
             * @return the next stage of the definition.
             */
            WithAttach withRecurrentSchedule(String scheduleTimeZone, String startTime, DayOfWeek... weekday);
        }
    }

    /** The entirety of an autoscale profile definition during current autoscale settings update. */
    interface UpdateDefinition
        extends UpdateDefinitionStages.WithAttach,
            UpdateDefinitionStages.Blank,
            UpdateDefinitionStages.WithScaleRule,
            UpdateDefinitionStages.WithScaleRuleOptional,
            UpdateDefinitionStages.WithScaleSchedule {
    }

    /** Grouping of autoscale profile definition stages during current autoscale settings update stage. */
    interface UpdateDefinitionStages {
        /** The final stage of the definition which attaches defined profile to the current Autoscale settings. */
        interface WithAttach extends Attachable.InUpdate<AutoscaleSetting.Update> {
        }

        /** The first stage of autoscale profile definition. */
        interface Blank {
            /**
             * Selects metric based autoscale profile.
             *
             * @param minimumInstanceCount the minimum number of instances for the resource.
             * @param maximumInstanceCount the maximum number of instances for the resource. The actual maximum number
             *     of instances is limited by the cores that are available in the subscription.
             * @param defaultInstanceCount the number of instances that will be set if metrics are not available for
             *     evaluation. The default is only used if the current instance count is lower than the default.
             * @return the next stage of the definition.
             */
            WithScaleRule withMetricBasedScale(
                int minimumInstanceCount, int maximumInstanceCount, int defaultInstanceCount);

            /**
             * Selects schedule based autoscale profile.
             *
             * @param instanceCount the number of instances that will be set during specified schedule. The actual
             *     number of instances is limited by the cores that are available in the subscription.
             * @return the next stage of the definition.
             */
            WithScaleSchedule withScheduleBasedScale(int instanceCount);
        }

        /** The stage of the definition which adds scale rules. */
        interface WithScaleRule {
            /**
             * Starts the definition of scale rule for the current autoscale profile.
             *
             * @return the next stage of the definition.
             */
            ScaleRule.ParentUpdateDefinitionStages.Blank defineScaleRule();
        }

        /** The stage of the definition which adds optional scale rules and schedules. */
        interface WithScaleRuleOptional extends WithAttach {
            /**
             * Starts the definition of scale rule for the current autoscale profile.
             *
             * @return the next stage of the definition.
             */
            ScaleRule.ParentUpdateDefinitionStages.Blank defineScaleRule();

            /**
             * Specifies fixed date schedule for autoscale profile.
             *
             * @param timeZone time zone for the schedule.
             * @param start start time.
             * @param end end time.
             * @return the next stage of the definition.
             */
            WithScaleRuleOptional withFixedDateSchedule(String timeZone, OffsetDateTime start, OffsetDateTime end);

            /**
             * Specifies recurrent schedule for autoscale profile.
             *
             * @param scheduleTimeZone time zone for the schedule. Some examples of valid timezones are: Dateline
             *     Standard Time, UTC-11, Hawaiian Standard Time, Alaskan Standard Time, Pacific Standard Time (Mexico),
             *     Pacific Standard Time, US Mountain Standard Time, Mountain Standard Time (Mexico), Mountain Standard
             *     Time, Central America Standard Time, Central Standard Time, Central Standard Time (Mexico), Canada
             *     Central Standard Time, SA Pacific Standard Time, Eastern Standard Time, US Eastern Standard Time,
             *     Venezuela Standard Time, Paraguay Standard Time, Atlantic Standard Time, Central Brazilian Standard
             *     Time, SA Western Standard Time, Pacific SA Standard Time, Newfoundland Standard Time, E. South
             *     America Standard Time, Argentina Standard Time, SA Eastern Standard Time, Greenland Standard Time,
             *     Montevideo Standard Time, Bahia Standard Time, UTC-02, Mid-Atlantic Standard Time, Azores Standard
             *     Time, Cape Verde Standard Time, Morocco Standard Time, UTC, GMT Standard Time, Greenwich Standard
             *     Time, W. Europe Standard Time, Central Europe Standard Time, Romance Standard Time, Central European
             *     Standard Time, W. Central Africa Standard Time, Namibia Standard Time, Jordan Standard Time, GTB
             *     Standard Time, Middle East Standard Time, Egypt Standard Time, Syria Standard Time, E. Europe
             *     Standard Time, South Africa Standard Time, FLE Standard Time, Turkey Standard Time, Israel Standard
             *     Time, Kaliningrad Standard Time, Libya Standard Time, Arabic Standard Time, Arab Standard Time,
             *     Belarus Standard Time, Russian Standard Time, E. Africa Standard Time, Iran Standard Time, Arabian
             *     Standard Time, Azerbaijan Standard Time, Russia Time Zone 3, Mauritius Standard Time, Georgian
             *     Standard Time, Caucasus Standard Time, Afghanistan Standard Time, West Asia Standard Time,
             *     Ekaterinburg Standard Time, Pakistan Standard Time, India Standard Time, Sri Lanka Standard Time,
             *     Nepal Standard Time, Central Asia Standard Time, Bangladesh Standard Time, N. Central Asia Standard
             *     Time, Myanmar Standard Time, SE Asia Standard Time, North Asia Standard Time, China Standard Time,
             *     North Asia East Standard Time, Singapore Standard Time, W. Australia Standard Time, Taipei Standard
             *     Time, Ulaanbaatar Standard Time, Tokyo Standard Time, Korea Standard Time, Yakutsk Standard Time,
             *     Cen. Australia Standard Time, AUS Central Standard Time, E. Australia Standard Time, AUS Eastern
             *     Standard Time, West Pacific Standard Time, Tasmania Standard Time, Magadan Standard Time, Vladivostok
             *     Standard Time, Russia Time Zone 10, Central Pacific Standard Time, Russia Time Zone 11, New Zealand
             *     Standard Time, UTC+12, Fiji Standard Time, Kamchatka Standard Time, Tonga Standard Time, Samoa
             *     Standard Time, Line Islands Standard Time.
             * @param startTime start time in hh:mm format.
             * @param weekday list of week days when the schedule should be active.
             * @return the next stage of the definition.
             */
            WithScaleRuleOptional withRecurrentSchedule(
                String scheduleTimeZone, String startTime, DayOfWeek... weekday);
        }

        /** The stage of the definition which specifies autoscale profile schedule. */
        interface WithScaleSchedule {
            /**
             * Specifies fixed date schedule for autoscale profile.
             *
             * @param timeZone time zone for the schedule.
             * @param start start time.
             * @param end end time.
             * @return the next stage of the definition.
             */
            WithAttach withFixedDateSchedule(String timeZone, OffsetDateTime start, OffsetDateTime end);

            /**
             * Specifies recurrent schedule for autoscale profile.
             *
             * @param scheduleTimeZone time zone for the schedule. Some examples of valid timezones are: Dateline
             *     Standard Time, UTC-11, Hawaiian Standard Time, Alaskan Standard Time, Pacific Standard Time (Mexico),
             *     Pacific Standard Time, US Mountain Standard Time, Mountain Standard Time (Mexico), Mountain Standard
             *     Time, Central America Standard Time, Central Standard Time, Central Standard Time (Mexico), Canada
             *     Central Standard Time, SA Pacific Standard Time, Eastern Standard Time, US Eastern Standard Time,
             *     Venezuela Standard Time, Paraguay Standard Time, Atlantic Standard Time, Central Brazilian Standard
             *     Time, SA Western Standard Time, Pacific SA Standard Time, Newfoundland Standard Time, E. South
             *     America Standard Time, Argentina Standard Time, SA Eastern Standard Time, Greenland Standard Time,
             *     Montevideo Standard Time, Bahia Standard Time, UTC-02, Mid-Atlantic Standard Time, Azores Standard
             *     Time, Cape Verde Standard Time, Morocco Standard Time, UTC, GMT Standard Time, Greenwich Standard
             *     Time, W. Europe Standard Time, Central Europe Standard Time, Romance Standard Time, Central European
             *     Standard Time, W. Central Africa Standard Time, Namibia Standard Time, Jordan Standard Time, GTB
             *     Standard Time, Middle East Standard Time, Egypt Standard Time, Syria Standard Time, E. Europe
             *     Standard Time, South Africa Standard Time, FLE Standard Time, Turkey Standard Time, Israel Standard
             *     Time, Kaliningrad Standard Time, Libya Standard Time, Arabic Standard Time, Arab Standard Time,
             *     Belarus Standard Time, Russian Standard Time, E. Africa Standard Time, Iran Standard Time, Arabian
             *     Standard Time, Azerbaijan Standard Time, Russia Time Zone 3, Mauritius Standard Time, Georgian
             *     Standard Time, Caucasus Standard Time, Afghanistan Standard Time, West Asia Standard Time,
             *     Ekaterinburg Standard Time, Pakistan Standard Time, India Standard Time, Sri Lanka Standard Time,
             *     Nepal Standard Time, Central Asia Standard Time, Bangladesh Standard Time, N. Central Asia Standard
             *     Time, Myanmar Standard Time, SE Asia Standard Time, North Asia Standard Time, China Standard Time,
             *     North Asia East Standard Time, Singapore Standard Time, W. Australia Standard Time, Taipei Standard
             *     Time, Ulaanbaatar Standard Time, Tokyo Standard Time, Korea Standard Time, Yakutsk Standard Time,
             *     Cen. Australia Standard Time, AUS Central Standard Time, E. Australia Standard Time, AUS Eastern
             *     Standard Time, West Pacific Standard Time, Tasmania Standard Time, Magadan Standard Time, Vladivostok
             *     Standard Time, Russia Time Zone 10, Central Pacific Standard Time, Russia Time Zone 11, New Zealand
             *     Standard Time, UTC+12, Fiji Standard Time, Kamchatka Standard Time, Tonga Standard Time, Samoa
             *     Standard Time, Line Islands Standard Time.
             * @param startTime start time in hh:mm format.
             * @param weekday list of week days when the schedule should be active.
             * @return the next stage of the definition.
             */
            WithAttach withRecurrentSchedule(String scheduleTimeZone, String startTime, DayOfWeek... weekday);
        }
    }

    /** Grouping of autoscale profile update stages. */
    interface Update extends Settable<AutoscaleSetting.Update> {
        /**
         * Updates metric based autoscale profile.
         *
         * @param minimumInstanceCount the minimum number of instances for the resource.
         * @param maximumInstanceCount the maximum number of instances for the resource. The actual maximum number of
         *     instances is limited by the cores that are available in the subscription.
         * @param defaultInstanceCount the number of instances that will be set if metrics are not available for
         *     evaluation. The default is only used if the current instance count is lower than the default.
         * @return the next stage of the autoscale profile update.
         */
        Update withMetricBasedScale(int minimumInstanceCount, int maximumInstanceCount, int defaultInstanceCount);

        /**
         * Updates schedule based autoscale profile.
         *
         * @param instanceCount instanceCount the number of instances that will be set during specified schedule. The
         *     actual number of instances is limited by the cores that are available in the subscription.
         * @return the next stage of the autoscale profile update.
         */
        Update withScheduleBasedScale(int instanceCount);

        /**
         * Updates fixed date schedule for autoscale profile.
         *
         * @param timeZone time zone for the schedule.
         * @param start start time.
         * @param end end time.
         * @return the next stage of the autoscale profile update.
         */
        Update withFixedDateSchedule(String timeZone, OffsetDateTime start, OffsetDateTime end);

        /**
         * Updates recurrent schedule for autoscale profile.
         *
         * @param scheduleTimeZone time zone for the schedule. Some examples of valid timezones are: Dateline Standard
         *     Time, UTC-11, Hawaiian Standard Time, Alaskan Standard Time, Pacific Standard Time (Mexico), Pacific
         *     Standard Time, US Mountain Standard Time, Mountain Standard Time (Mexico), Mountain Standard Time,
         *     Central America Standard Time, Central Standard Time, Central Standard Time (Mexico), Canada Central
         *     Standard Time, SA Pacific Standard Time, Eastern Standard Time, US Eastern Standard Time, Venezuela
         *     Standard Time, Paraguay Standard Time, Atlantic Standard Time, Central Brazilian Standard Time, SA
         *     Western Standard Time, Pacific SA Standard Time, Newfoundland Standard Time, E. South America Standard
         *     Time, Argentina Standard Time, SA Eastern Standard Time, Greenland Standard Time, Montevideo Standard
         *     Time, Bahia Standard Time, UTC-02, Mid-Atlantic Standard Time, Azores Standard Time, Cape Verde Standard
         *     Time, Morocco Standard Time, UTC, GMT Standard Time, Greenwich Standard Time, W. Europe Standard Time,
         *     Central Europe Standard Time, Romance Standard Time, Central European Standard Time, W. Central Africa
         *     Standard Time, Namibia Standard Time, Jordan Standard Time, GTB Standard Time, Middle East Standard Time,
         *     Egypt Standard Time, Syria Standard Time, E. Europe Standard Time, South Africa Standard Time, FLE
         *     Standard Time, Turkey Standard Time, Israel Standard Time, Kaliningrad Standard Time, Libya Standard
         *     Time, Arabic Standard Time, Arab Standard Time, Belarus Standard Time, Russian Standard Time, E. Africa
         *     Standard Time, Iran Standard Time, Arabian Standard Time, Azerbaijan Standard Time, Russia Time Zone 3,
         *     Mauritius Standard Time, Georgian Standard Time, Caucasus Standard Time, Afghanistan Standard Time, West
         *     Asia Standard Time, Ekaterinburg Standard Time, Pakistan Standard Time, India Standard Time, Sri Lanka
         *     Standard Time, Nepal Standard Time, Central Asia Standard Time, Bangladesh Standard Time, N. Central Asia
         *     Standard Time, Myanmar Standard Time, SE Asia Standard Time, North Asia Standard Time, China Standard
         *     Time, North Asia East Standard Time, Singapore Standard Time, W. Australia Standard Time, Taipei Standard
         *     Time, Ulaanbaatar Standard Time, Tokyo Standard Time, Korea Standard Time, Yakutsk Standard Time, Cen.
         *     Australia Standard Time, AUS Central Standard Time, E. Australia Standard Time, AUS Eastern Standard
         *     Time, West Pacific Standard Time, Tasmania Standard Time, Magadan Standard Time, Vladivostok Standard
         *     Time, Russia Time Zone 10, Central Pacific Standard Time, Russia Time Zone 11, New Zealand Standard Time,
         *     UTC+12, Fiji Standard Time, Kamchatka Standard Time, Tonga Standard Time, Samoa Standard Time, Line
         *     Islands Standard Time.
         * @param startTime start time in hh:mm format.
         * @param weekday list of week days when the schedule should be active.
         * @return the next stage of the autoscale profile update.
         */
        Update withRecurrentSchedule(String scheduleTimeZone, String startTime, DayOfWeek... weekday);

        /**
         * Starts the definition of scale rule for the current autoscale profile.
         *
         * @return the next stage of the autoscale profile update.
         */
        ScaleRule.UpdateDefinitionStages.Blank defineScaleRule();

        /**
         * Starts the update of the scale rule for the current autoscale profile.
         *
         * @param ruleIndex the index of the scale rule in the current autoscale profile. The index represents the order
         *     at which rules were added to the current profile.
         * @return the next stage of the autoscale profile update.
         */
        ScaleRule.Update updateScaleRule(int ruleIndex);

        /**
         * Removes scale rule from the current autoscale profile.
         *
         * @param ruleIndex the index of the scale rule in the current autoscale profile.
         * @return the next stage of the autoscale profile update.
         */
        Update withoutScaleRule(int ruleIndex);
    }
}
