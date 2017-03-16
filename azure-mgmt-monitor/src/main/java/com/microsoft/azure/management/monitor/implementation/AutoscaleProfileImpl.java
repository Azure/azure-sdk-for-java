/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.AutoscaleProfile;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.monitor.Recurrence;
import com.microsoft.azure.management.monitor.ScaleCapacity;
import com.microsoft.azure.management.monitor.ScaleRule;
import com.microsoft.azure.management.monitor.TimeWindow;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import org.joda.time.DateTime;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for AutoscaleProfile.
 */
@LangDefinition
class AutoscaleProfileImpl extends
        ExternalChildResourceImpl<AutoscaleProfile,
                AutoscaleProfileInner,
                AutoscaleSettingImpl,
                AutoscaleSetting>
        implements
            AutoscaleProfile,
            AutoscaleProfile.Definition,
            AutoscaleProfile.UpdateDefinition,
            AutoscaleProfile.Update {

    AutoscaleProfileImpl(String name, AutoscaleSettingImpl parent, AutoscaleProfileInner inner) {
        super(name, parent, inner);
        inner.withName(name);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public Observable<AutoscaleProfile> createAsync() {
        return null;
    }

    @Override
    public Observable<AutoscaleProfile> updateAsync() {
        return null;
    }

    @Override
    public Observable<Void> deleteAsync() {
        return null;
    }

    @Override
    protected Observable<AutoscaleProfileInner> getInnerAsync() {
        return null;
    }

    @Override
    public ScaleCapacity capacity() {
        return this.inner().capacity();
    }

    @Override
    public List<ScaleRule> rules() {
        return null;
    }

    @Override
    public TimeWindow fixedDate() {
        return this.inner().fixedDate();
    }

    @Override
    public Recurrence recurrence() {
        return new RecurrenceImpl(this, this.inner().recurrence());
    }

    @Override
    public AutoscaleSettingImpl attach() {
        return null;
    }

    @Override
    public AutoscaleProfileImpl withScaleCapacity(String capacityMinimum, String capacityMaximum, String capacityDefault) {
        this.inner().withCapacity( new ScaleCapacity()
                .withMinimum(capacityMinimum)
                .withMaximum(capacityMaximum)
                .withDefaultProperty(capacityDefault));
        return this;
    }

    @Override
    public AutoscaleProfileImpl withoutScaleRule(ScaleRule scaleRule) {
        return null;
    }

    @Override
    public ScaleRuleImpl updateScaleRule(ScaleRule scaleRule) {
        return null;
    }

    @Override
    public ScaleRuleImpl defineScaleRule() {
        return null;
    }

    @Override
    public AutoscaleProfileImpl withFixedDate(DateTime start, DateTime end) {
        return this.withFixedDate(start, end, null);
    }

    @Override
    public AutoscaleProfileImpl withFixedDate(DateTime start, DateTime end, String timeZone) {
        this.inner().withFixedDate( new TimeWindow()
                .withStart(start)
                .withEnd(end)
                .withTimeZone(timeZone));
        return this;
    }

    @Override
    public AutoscaleProfileImpl withoutFixedDate() {
        this.inner().withFixedDate(null);
        return this;
    }

    @Override
    public AutoscaleProfileImpl withoutRecurrence() {
        this.inner().withRecurrence(null);
        return this;
    }

    @Override
    public RecurrenceImpl updateRecurrence() {
        RecurrenceImpl recurrence = new RecurrenceImpl(this, this.inner().recurrence());
        return recurrence;
    }

    @Override
    public AutoscaleProfileImpl withRecurrence(Recurrence recurrence) {
        this.inner().withRecurrence(((RecurrenceImpl)recurrence).inner());
        return this;
    }

    @Override
    public RecurrenceImpl defineRecurrence() {
        RecurrenceImpl recurrence = new RecurrenceImpl(this);
        this.inner().withRecurrence(recurrence.inner());
        return recurrence;
    }

    @Override
    public AutoscaleProfileImpl withName(String name) {
        this.inner().withName(name);
        return this;
    }

    @Override
    public String id() {
        return this.name();
    }
}
