/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.LocalizableString;
import com.azure.management.monitor.Metric;
import com.azure.management.monitor.TimeSeriesElement;
import com.azure.management.monitor.Unit;
import com.azure.management.monitor.models.MetricInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.util.List;

/**
 * The Azure {@link Metric} wrapper class implementation.
 */
class MetricImpl
        extends WrapperImpl<MetricInner> implements Metric {
    private LocalizableString metricName;

    MetricImpl(MetricInner innerObject) {
        super(innerObject);
        this.metricName = (inner().name() == null) ? null : new LocalizableStringImpl(inner().name());
    }

    @Override
    public String id() {
        return this.inner().getId();
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
