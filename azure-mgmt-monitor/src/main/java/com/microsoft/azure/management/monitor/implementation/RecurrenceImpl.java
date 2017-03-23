/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.Recurrence;
import com.microsoft.azure.management.monitor.RecurrenceFrequency;
import com.microsoft.azure.management.monitor.RecurrentSchedule;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation for Recurrence.
 */
@LangDefinition
class RecurrenceImpl extends
        WrapperImpl<RecurrenceInner>
        implements
            Recurrence,
            Recurrence.StandaloneDefinition,
            Recurrence.StandaloneUpdate,
            Recurrence.Definition,
            Recurrence.UpdateDefinition,
            Recurrence.Update {

    protected AutoscaleProfileImpl parentProfile;

    private RecurrenceImpl(RecurrenceInner inner) {
        super(inner);
        if(inner().schedule() == null) {
            inner().withSchedule(new RecurrentSchedule());
        }
    }

    RecurrenceImpl(AutoscaleProfileImpl parent, RecurrenceInner inner) {
        this(inner);
        parentProfile = parent;
    }

    RecurrenceImpl(AutoscaleProfileImpl parent) {
        this(parent, new RecurrenceInner());
    }

    @Override
    public RecurrenceFrequency frequency() {
        return inner().frequency();
    }

    @Override
    public String timeZone() {
        return inner().schedule().timeZone();
    }

    @Override
    public List<String> days() {
        return inner().schedule().days();
    }

    @Override
    public List<Integer> hours() {
        return inner().schedule().hours();
    }

    @Override
    public List<Integer> minutes() {
        return inner().schedule().minutes();
    }

    @Override
    public RecurrenceImpl withFrequency(RecurrenceFrequency frequency) {
        this.inner().withFrequency(frequency);
        return this;
    }

    @Override
    public RecurrenceImpl withScheduleTimeZone(String scheduleTimeZone) {
        this.inner().schedule().withTimeZone(scheduleTimeZone);
        return this;
    }

    @Override
    public RecurrenceImpl withScheduleDay(String day) {
        this.inner().schedule().withDays(Arrays.asList(day));
        return this;
    }

    @Override
    public RecurrenceImpl withScheduleDays(List<String> days) {
        this.inner().schedule().withDays(days);
        return this;
    }

    @Override
    public RecurrenceImpl withScheduleHour(int hour) {
        this.inner().schedule().withHours(Arrays.asList(hour));
        return this;
    }

    @Override
    public RecurrenceImpl withScheduleHours(List<Integer> hours) {
        this.inner().schedule().withHours(hours);
        return this;
    }

    @Override
    public RecurrenceImpl withScheduleMinute(int minute) {
        this.inner().schedule().withMinutes(Arrays.asList(minute));
        return this;
    }

    @Override
    public RecurrenceImpl withScheduleMinutes(List<Integer> minutes) {
        this.inner().schedule().withMinutes(minutes);
        return this;
    }

    @Override
    public AutoscaleProfileImpl parent() {
        return this.parentProfile;
    }

    @Override
    public AutoscaleProfileImpl attach() {
        return null;
    }

    @Override
    public RecurrenceImpl update() {
        return this;
    }

    @Override
    public RecurrenceImpl apply() {
        return this;
    }

    @Override
    public RecurrenceImpl create() {
        return this;
    }
}
