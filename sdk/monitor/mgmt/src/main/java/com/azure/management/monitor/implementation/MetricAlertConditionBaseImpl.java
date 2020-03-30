/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.MetricAlertRuleTimeAggregation;
import com.azure.management.monitor.MetricDimension;
import com.azure.management.monitor.MultiMetricCriteria;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Base class for MetricAlertConditionImpl and MetricDynamicAlertConditionImpl.
 *
 * @param <InnerT> inner class, MetricCriteria or DynamicMetricCriteria
 * @param <SubclassT> subclass, i.e., MetricAlertConditionImpl or MetricDynamicAlertConditionImpl
 */
class MetricAlertConditionBaseImpl<InnerT extends MultiMetricCriteria, SubclassT extends MetricAlertConditionBaseImpl<InnerT, SubclassT>>
        extends WrapperImpl<InnerT> {

    protected final MetricAlertImpl parent;
    protected final TreeMap<String, MetricDimension> dimensions;

    protected MetricAlertConditionBaseImpl(String name, InnerT innerObject, MetricAlertImpl parent) {
        super(innerObject);
        this.inner().withName(name);
        this.parent = parent;
        this.dimensions = new TreeMap<>();
        if (this.inner().dimensions() != null) {
            for (MetricDimension md : this.inner().dimensions()) {
                dimensions.put(md.name(), md);
            }
        }
    }

    public String name() {
        return this.inner().name();
    }

    public String metricName() {
        return this.inner().metricName();
    }

    public String metricNamespace() {
        return this.inner().metricNamespace();
    }

    public MetricAlertRuleTimeAggregation timeAggregation() {
        return MetricAlertRuleTimeAggregation.fromString(this.inner().timeAggregation().toString());
    }

    public Collection<MetricDimension> dimensions() {
        return Collections.unmodifiableCollection(this.inner().dimensions());
    }

    public MetricAlertImpl parent() {
        this.inner().withDimensions(new ArrayList<>(this.dimensions.values()));
        return this.parent;
    }

    public SubclassT withMetricName(String metricName) {
        this.inner().withMetricName(metricName);
        return (SubclassT) this;
    }

    public SubclassT withMetricName(String metricName, String metricNamespace) {
        this.inner().withMetricNamespace(metricNamespace);
        return (SubclassT) this.withMetricName(metricName);
    }

    public SubclassT withDimension(String dimensionName, String... values) {
        if (this.dimensions.containsKey(dimensionName)) {
            dimensions.remove(dimensionName);
        }
        MetricDimension md = new MetricDimension();
        md.withName(dimensionName);
        md.withOperator("Include");
        md.withValues(Arrays.asList(values));
        dimensions.put(dimensionName, md);
        return (SubclassT) this;
    }

    public SubclassT withoutDimension(String dimensionName) {
        if (this.dimensions.containsKey(dimensionName)) {
            dimensions.remove(dimensionName);
        }
        return (SubclassT) this;
    }
}
