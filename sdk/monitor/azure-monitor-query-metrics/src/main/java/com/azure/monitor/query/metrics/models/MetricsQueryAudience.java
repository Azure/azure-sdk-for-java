// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics.models;

import java.util.Collection;

import com.azure.core.util.ExpandableStringEnum;

/**
 * The audience indicating the authorization scope of metrics query clients.
 */
public class MetricsQueryAudience extends ExpandableStringEnum<MetricsQueryAudience> {
    /**
     * Static value for Azure Public Cloud.
     */
    public static final MetricsQueryAudience AZURE_PUBLIC_CLOUD = fromString("https://metrics.monitor.azure.com");

    /**
     * Static value for Azure US Government.
     */
    public static final MetricsQueryAudience AZURE_US_GOVERNMENT = fromString("https://metrics.monitor.azure.us");

    /**
     * Static value for Azure China.
     */
    public static final MetricsQueryAudience AZURE_CHINA = fromString("https://metrics.monitor.azure.cn");

    /**
     * @deprecated Creates an instance of MetricsQueryAudience.
     */
    @Deprecated
    public MetricsQueryAudience() {
    }

    /**
     * Creates an instance of MetricsQueryAudience.
     *
     * @param name the string value.
     * @return the MetricsQueryAudience.
     */
    public static MetricsQueryAudience fromString(String name) {
        return fromString(name, MetricsQueryAudience.class);
    }

    /**
     * Get the collection of MetricsQueryAudience values.
     *
     * @return the collection of MetricsQueryAudience values.
     */
    public static Collection<MetricsQueryAudience> values() {
        return values(MetricsQueryAudience.class);
    }
}
