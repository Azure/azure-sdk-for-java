// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.monitor.models.AutoscaleProfile;
import com.azure.resourcemanager.monitor.models.DayOfWeek;
import com.azure.resourcemanager.monitor.models.Recurrence;
import com.azure.resourcemanager.monitor.models.RecurrenceFrequency;
import com.azure.resourcemanager.monitor.models.RecurrentSchedule;
import com.azure.resourcemanager.monitor.models.ScaleCapacity;
import com.azure.resourcemanager.monitor.models.ScaleRule;
import com.azure.resourcemanager.monitor.models.TimeWindow;
import com.azure.resourcemanager.monitor.fluent.models.AutoscaleProfileInner;
import com.azure.resourcemanager.monitor.fluent.models.ScaleRuleInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** Implementation for AutoscaleProfile. */
class AutoscaleProfileImpl extends WrapperImpl<AutoscaleProfileInner>
    implements AutoscaleProfile,
        AutoscaleProfile.Definition,
        AutoscaleProfile.UpdateDefinition,
        AutoscaleProfile.Update {

    private final ClientLogger logger = new ClientLogger(getClass());

    private final AutoscaleSettingImpl parent;

    AutoscaleProfileImpl(String name, AutoscaleProfileInner innerObject, AutoscaleSettingImpl parent) {
        super(innerObject);
        this.innerModel().withName(name);
        this.parent = parent;
        if (this.innerModel().capacity() == null) {
            this.innerModel().withCapacity(new ScaleCapacity());
        }
        if (this.innerModel().rules() == null) {
            this.innerModel().withRules(new ArrayList<>());
        }
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public int minInstanceCount() {
        if (this.innerModel().capacity() != null) {
            return Integer.parseInt(this.innerModel().capacity().minimum());
        }
        return 0;
    }

    @Override
    public int maxInstanceCount() {
        if (this.innerModel().capacity() != null) {
            return Integer.parseInt(this.innerModel().capacity().maximum());
        }
        return 0;
    }

    @Override
    public int defaultInstanceCount() {
        if (this.innerModel().capacity() != null) {
            return Integer.parseInt(this.innerModel().capacity().defaultProperty());
        }
        return 0;
    }

    @Override
    public TimeWindow fixedDateSchedule() {
        return this.innerModel().fixedDate();
    }

    @Override
    public Recurrence recurrentSchedule() {
        return this.innerModel().recurrence();
    }

    @Override
    public List<ScaleRule> rules() {
        List<ScaleRule> rules = new ArrayList<>();
        if (this.innerModel().rules() != null) {
            for (ScaleRuleInner ruleInner : this.innerModel().rules()) {
                rules.add(new ScaleRuleImpl(ruleInner, this));
            }
        }
        return rules;
    }

    @Override
    public AutoscaleSettingImpl parent() {
        return parent;
    }

    @Override
    public AutoscaleSettingImpl attach() {
        return parent.addNewAutoscaleProfile(this);
    }

    @Override
    public AutoscaleProfileImpl withMetricBasedScale(
        int minimumInstanceCount, int maximumInstanceCount, int defaultInstanceCount) {
        this.innerModel().capacity().withMinimum(Integer.toString(minimumInstanceCount));
        this.innerModel().capacity().withMaximum(Integer.toString(maximumInstanceCount));
        this.innerModel().capacity().withDefaultProperty(Integer.toString(defaultInstanceCount));
        return this;
    }

    @Override
    public AutoscaleProfileImpl withScheduleBasedScale(int instanceCount) {
        return this.withMetricBasedScale(instanceCount, instanceCount, instanceCount);
    }

    @Override
    public AutoscaleProfileImpl withFixedInstanceCount(int instanceCount) {
        this.withMetricBasedScale(instanceCount, instanceCount, instanceCount);
        this.innerModel().withFixedDate(null);
        this.innerModel().withRecurrence(null);
        this.innerModel().withRules(new ArrayList<ScaleRuleInner>());
        return this;
    }

    @Override
    public AutoscaleProfileImpl withFixedDateSchedule(String timeZone, OffsetDateTime start, OffsetDateTime end) {
        this.innerModel().withFixedDate(new TimeWindow().withTimeZone(timeZone).withStart(start).withEnd(end));
        if (this.innerModel().recurrence() != null) {
            this.innerModel().withRecurrence(null);
        }
        return this;
    }

    @Override
    public AutoscaleProfileImpl withRecurrentSchedule(String scheduleTimeZone, String startTime, DayOfWeek... weekday) {
        if (startTime == null
            || startTime.isEmpty()
            || startTime.length() != 5
            || startTime.charAt(2) != ':'
            || !Character.isDigit(startTime.charAt(0))
            || !Character.isDigit(startTime.charAt(1))
            || !Character.isDigit(startTime.charAt(3))
            || !Character.isDigit(startTime.charAt(4))) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Start time should have format of 'hh:mm' where hh is in 24-hour clock (AM/PM times are not"
                    + " supported)."));
        }

        int hh = Integer.parseInt(startTime.substring(0, 2));
        int mm = Integer.parseInt(startTime.substring(3));
        if (hh > 23 || mm > 60) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Start time should have format of 'hh:mm' where hh is in 24-hour clock (AM/PM times are not"
                    + " supported)."));
        }

        this.innerModel().withRecurrence(new Recurrence());
        this.innerModel().recurrence().withFrequency(RecurrenceFrequency.WEEK);
        this.innerModel().recurrence().withSchedule(new RecurrentSchedule());
        this.innerModel().recurrence().schedule().withTimeZone(scheduleTimeZone);
        this.innerModel().recurrence().schedule().withHours(new ArrayList<Integer>());
        this.innerModel().recurrence().schedule().withMinutes(new ArrayList<Integer>());
        this.innerModel().recurrence().schedule().hours().add(hh);
        this.innerModel().recurrence().schedule().minutes().add(mm);
        this.innerModel().recurrence().schedule().withDays(new ArrayList<String>());

        for (DayOfWeek dof : weekday) {
            this.innerModel().recurrence().schedule().days().add(dof.toString());
        }

        this.innerModel().withFixedDate(null);
        return this;
    }

    @Override
    public ScaleRuleImpl defineScaleRule() {
        return new ScaleRuleImpl(new ScaleRuleInner(), this);
    }

    @Override
    public ScaleRuleImpl updateScaleRule(int ruleIndex) {
        ScaleRuleImpl srToUpdate = new ScaleRuleImpl(this.innerModel().rules().get(ruleIndex), this);
        return srToUpdate;
    }

    @Override
    public AutoscaleProfileImpl withoutScaleRule(int ruleIndex) {
        this.innerModel().rules().remove(ruleIndex);
        return this;
    }

    AutoscaleProfileImpl addNewScaleRule(ScaleRuleImpl scaleRule) {
        this.innerModel().rules().add(scaleRule.innerModel());
        return this;
    }
}
