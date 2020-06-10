// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.models.LocalizableString;
import com.azure.resourcemanager.monitor.models.Metric;
import com.azure.resourcemanager.monitor.models.TimeSeriesElement;
import com.azure.resourcemanager.monitor.models.Unit;
import com.azure.resourcemanager.monitor.fluent.inner.MetricInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.List;

/** The Azure {@link Metric} wrapper class implementation. */
class MetricImpl extends WrapperImpl<MetricInner> implements Metric {
    private LocalizableString metricName;

    MetricImpl(MetricInner innerObject) {
        super(innerObject);
        this.metricName = (inner().name() == null) ? null : new LocalizableStringImpl(inner().name());
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String type() {
        return this.inner().type();
    }

    @Override
    public LocalizableString name() {
        return this.metricName;
    }

    @Override
    public Unit unit() {
        return this.inner().unit();
    }

    @Override
    public List<TimeSeriesElement> timeseries() {
        return this.inner().timeseries();
    }
}
