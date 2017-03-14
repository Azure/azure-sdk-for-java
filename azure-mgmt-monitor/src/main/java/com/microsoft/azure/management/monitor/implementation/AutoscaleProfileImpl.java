/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.AutoscaleNotification;
import com.microsoft.azure.management.monitor.AutoscaleProfile;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.monitor.Recurrence;
import com.microsoft.azure.management.monitor.RecurrenceFrequency;
import com.microsoft.azure.management.monitor.ScaleCapacity;
import com.microsoft.azure.management.monitor.ScaleRule;
import com.microsoft.azure.management.monitor.TimeWindow;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import org.joda.time.DateTime;
import rx.Observable;

import java.util.List;
import java.util.Map;

/**
 * Implementation for CdnProfile.
 */
@LangDefinition
class AutoscaleProfileImpl
        implements
            AutoscaleProfile,
            AutoscaleProfile.Definition,
            AutoscaleProfile.UpdateDefinition,
            AutoscaleProfile.Update {

    AutoscaleProfileImpl(String name) {
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public ScaleCapacity capacity() {
        return null;
    }

    @Override
    public List<ScaleRule> rules() {
        return null;
    }

    @Override
    public TimeWindow fixedDate() {
        return null;
    }

    @Override
    public Recurrence recurrence() {
        return null;
    }

    @Override
    public AutoscaleSettingImpl attach() {
        return null;
    }

    @Override
    public AutoscaleProfileImpl withScaleCapacity(String capacityMinimum, String capacityMaximum, String capacityDefault) {
        return null;
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
    public AutoscaleProfileImpl withTimeWindow(DateTime start, DateTime end) {
        return null;
    }

    @Override
    public AutoscaleProfileImpl withTimeWindow(DateTime start, DateTime end, String timeZone) {
        return null;
    }

    @Override
    public AutoscaleProfileImpl withoutTimeWindow() {
        return null;
    }

    @Override
    public AutoscaleProfileImpl withoutRecurrence() {
        return null;
    }

    @Override
    public RecurrenceImpl updateRecurrence() {
        return null;
    }

    @Override
    public AutoscaleProfileImpl withRecurrence(Recurrence recurrence) {
        return null;
    }

    @Override
    public RecurrenceImpl defineRecurrence() {
        return null;
    }

    @Override
    public AutoscaleSettingImpl parent() {
        return null;
    }

    @Override
    public AutoscaleProfileImpl withName(String Name) {
        return null;
    }
}
