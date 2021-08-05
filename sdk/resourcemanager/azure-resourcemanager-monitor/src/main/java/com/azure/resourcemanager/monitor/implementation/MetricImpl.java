// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.models.LocalizableString;
import com.azure.resourcemanager.monitor.models.Metric;
import com.azure.resourcemanager.monitor.models.Unit;
import com.azure.resourcemanager.monitor.models.TimeSeriesElement;
import com.azure.resourcemanager.monitor.fluent.models.MetricInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.List;

/** The Azure {@link Metric} wrapper class implementation. */
class MetricImpl extends WrapperImpl<MetricInner> implements Metric {
    private LocalizableString metricName;

    MetricImpl(MetricInner innerObject) {
        super(innerObject);
        this.metricName = (innerModel().name() == null) ? null : new LocalizableStringImpl(innerModel().name());
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String type() {
        return this.innerModel().type();
    }

    @Override
    public LocalizableString name() {
        return this.metricName;
    }

    @Override
    public Unit unit() {
        return this.innerModel().unit();
    }

    @Override
    public List<TimeSeriesElement> timeseries() {
        return this.innerModel().timeseries();
    }
}
