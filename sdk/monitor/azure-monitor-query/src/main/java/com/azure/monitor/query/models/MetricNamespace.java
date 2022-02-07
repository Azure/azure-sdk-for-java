// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.monitor.query.implementation.metrics.models.MetricsHelper;

/**
 * Metric namespace class specifies the metadata for a metric namespace.
 */
public final class MetricNamespace {
    private NamespaceClassification classification;
    private String id;
    private String name;
    private String fullyQualifiedName;
    private String type;

    static {
        MetricsHelper.setMetricNamespaceAccessor(MetricNamespace::setMetricNamespaceProperties);
    }

    private void setMetricNamespaceProperties(NamespaceClassification classification, String id, String name,
                                              String fullyQualifiedName, String type) {
        this.classification = classification;
        this.id = id;
        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.type = type;
    }

    /**
     * Returns the kind of namespace.
     * @return the kind of namespace.
     */
    public NamespaceClassification getClassification() {
        return classification;
    }

    /**
     * Returns the ID of the metric namespace.
     * @return the ID of the metric namespace.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the escaped name of the namespace.
     * @return the escaped name of the namespace.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the fully qualified name of the metric namespace.
     * @return the fully qualified name of the metric namespace.
     */
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    /**
     * Returns the type of the namespace.
     * @return the type of the namespace.
     */
    public String getType() {
        return type;
    }
}
