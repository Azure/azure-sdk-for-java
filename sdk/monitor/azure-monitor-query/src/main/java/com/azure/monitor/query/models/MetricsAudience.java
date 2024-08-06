// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.query.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The audience indicating the authorization scope of metrics clients.
 */
public class MetricsAudience extends ExpandableStringEnum<MetricsAudience> {

    /**
     * Static value for Azure Public Cloud.
     */
    public static final MetricsAudience AZURE_PUBLIC_CLOUD = fromString("https://metrics.monitor.azure.com");

    /**
     * Static value for Azure US Government.
     */
    public static final MetricsAudience AZURE_US_GOVERNMENT = fromString("https://metrics.monitor.azure.us");

    /**
     * Static value for Azure China.
     */
    public static final MetricsAudience AZURE_CHINA = fromString("https://metrics.monitor.azure.cn");

    /**
     * @deprecated Creates an instance of MetricsAudience.
     */
    @Deprecated
    public MetricsAudience() {
    }

    /**
     * Creates an instance of MetricsAudience.
     *
     * @param name the string value.
     * @return the MetricsAudience.
     */
    public static MetricsAudience fromString(String name) {
        return fromString(name, MetricsAudience.class);
    }

    /**
     * Get the collection of MetricsAudience values.
     *
     * @return the collection of MetricsAudience values.
     */
    public static Collection<MetricsAudience> values() {
        return values(MetricsAudience.class);
    }
}
