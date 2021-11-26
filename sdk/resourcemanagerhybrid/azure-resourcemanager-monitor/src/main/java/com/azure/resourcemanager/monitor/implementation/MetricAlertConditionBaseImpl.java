// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.models.MetricAlertRuleTimeAggregation;
import com.azure.resourcemanager.monitor.models.MetricDimension;
import com.azure.resourcemanager.monitor.models.MultiMetricCriteria;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
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
class MetricAlertConditionBaseImpl<
        InnerT extends MultiMetricCriteria, SubclassT extends MetricAlertConditionBaseImpl<InnerT, SubclassT>>
    extends WrapperImpl<InnerT> {

    protected final MetricAlertImpl parent;
    protected final TreeMap<String, MetricDimension> dimensions;

    protected MetricAlertConditionBaseImpl(String name, InnerT innerObject, MetricAlertImpl parent) {
        super(innerObject);
        this.innerModel().withName(name);
        this.parent = parent;
        this.dimensions = new TreeMap<>();
        if (this.innerModel().dimensions() != null) {
            for (MetricDimension md : this.innerModel().dimensions()) {
                dimensions.put(md.name(), md);
            }
        }
    }

    public String name() {
        return this.innerModel().name();
    }

    public String metricName() {
        return this.innerModel().metricName();
    }

    public String metricNamespace() {
        return this.innerModel().metricNamespace();
    }

    public MetricAlertRuleTimeAggregation timeAggregation() {
        return MetricAlertRuleTimeAggregation.fromString(this.innerModel().timeAggregation().toString());
    }

    public Collection<MetricDimension> dimensions() {
        return Collections.unmodifiableCollection(this.innerModel().dimensions());
    }

    public MetricAlertImpl parent() {
        this.innerModel().withDimensions(new ArrayList<>(this.dimensions.values()));
        return this.parent;
    }

    @SuppressWarnings("unchecked")
    public SubclassT withMetricName(String metricName) {
        this.innerModel().withMetricName(metricName);
        return (SubclassT) this;
    }

    public SubclassT withMetricName(String metricName, String metricNamespace) {
        this.innerModel().withMetricNamespace(metricNamespace);
        return this.withMetricName(metricName);
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    public SubclassT withoutDimension(String dimensionName) {
        if (this.dimensions.containsKey(dimensionName)) {
            dimensions.remove(dimensionName);
        }
        return (SubclassT) this;
    }
}
