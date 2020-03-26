/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.Metric;
import com.azure.management.monitor.models.MetricInner;
import com.azure.management.monitor.models.ResponseInner;
import com.azure.management.monitor.MetricCollection;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Azure {@link MetricCollection} wrapper class implementation.
 */
class MetricCollectionImpl
        extends WrapperImpl<ResponseInner> implements MetricCollection {

    MetricCollectionImpl(ResponseInner innerObject) {
        super(innerObject);
    }

    @Override
    public String namespace() {
        return this.inner().namespace();
    }

    @Override
    public String resourceRegion() {
        return this.inner().resourceregion();
    }

    @Override
    public Double cost() {
        return this.inner().cost().doubleValue();
    }

    @Override
    public String timespan() {
        return this.inner().timespan();
    }

    @Override
    public Duration interval() {
        return this.inner().interval();
    }

    @Override
    public List<Metric> metrics() {
        return this.inner().value().stream().map(MetricImpl::new).collect(Collectors.toList());
    }
}
