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
import com.microsoft.azure.management.monitor.ScaleAction;
import com.microsoft.azure.management.monitor.ScaleDirection;
import com.microsoft.azure.management.monitor.ScaleType;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for Recurrence.
 */
@LangDefinition
class RecurrenceImpl extends
        WrapperImpl<RecurrenceInner>
        implements
            Recurrence,
            Recurrence.Definition,
            Recurrence.Update {

    RecurrenceImpl(RecurrenceInner inner) {
        super(inner);
        if(inner().schedule() == null) {
            inner().withSchedule(new RecurrentSchedule());
        }

        /*if(inner.schedule().days() == null) {
            inner.schedule().withDays(new ArrayList<String>());
        }
        if(inner.schedule().hours() == null) {
            inner.schedule().withHours(new ArrayList<Integer>());
        }
        if(inner.schedule().minutes() == null) {
            inner.schedule().withMinutes(new ArrayList<Integer>());
        }*/
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
        if(this.inner().schedule().days() == null) {
            this.inner().schedule().withDays(new ArrayList<String>());
        }
        return this;
    }

    @Override
    public RecurrenceImpl withoutScheduleDay(String day) {
        return this;
    }

    @Override
    public RecurrenceImpl withScheduleDays(List day) {
        return this;
    }

    @Override
    public RecurrenceImpl withScheduleHour(int hour) {
        return null;
    }

    @Override
    public RecurrenceImpl withoutHour(int hour) {
        return null;
    }

    @Override
    public RecurrenceImpl withScheduleHours(List hours) {
        return null;
    }

    @Override
    public RecurrenceImpl withScheduleMinute(int minute) {
        return null;
    }

    @Override
    public RecurrenceImpl withoutScheduleMinute(int minute) {
        return null;
    }

    @Override
    public RecurrenceImpl withScheduleMinutes(List minutes) {
        return null;
    }

    @Override
    public AutoscaleProfileImpl apply() {
        return null;
    }
}
