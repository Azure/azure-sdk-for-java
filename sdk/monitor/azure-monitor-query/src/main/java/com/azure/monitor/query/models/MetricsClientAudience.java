// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.query.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The audience indicating the authorization scope of metrics clients.
 */
public class MetricsClientAudience extends ExpandableStringEnum<MetricsClientAudience> {

    /**
     * Static value for Azure Public Cloud.
     */
    public static final MetricsClientAudience AZURE_PUBLIC_CLOUD = fromString("https://metrics.monitor.azure.com//.default");

    /**
     * Static value for Azure US Government.
     */
    public static final MetricsClientAudience AZURE_US_GOVERNMENT = fromString("https://metrics.monitor.azure.us//.default");

    /**
     * Static value for Azure China.
     */
    public static final MetricsClientAudience AZURE_CHINA = fromString("https://metrics.monitor.azure.cn//.default");

    /**
     * @deprecated Creates an instance of MetricsClientAudience.
     */
    @Deprecated
    public MetricsClientAudience() {
    }

    /**
     * Creates an instance of MetricsClientAudience.
     *
     * @param name the string value.
     * @return the MetricsClientAudience.
     */
    public static MetricsClientAudience fromString(String name) {
        return fromString(name, MetricsClientAudience.class);
    }

    /**
     * Get the collection of MetricsClientAudience values.
     *
     * @return the collection of MetricsClientAudience values.
     */
    public static Collection<MetricsClientAudience> values() {
        return values(MetricsClientAudience.class);
    }
}
