// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.monitor.query.MetricsQueryClient;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Cloud audiences available for {@link MetricsQueryClient}.
 */
public final class MetricsQueryClientAudience extends ExpandableStringEnum<MetricsQueryClientAudience> {
    /** Static value AZURE_RESOURCE_MANAGER_CHINA for MetricsQueryClientAudience. */
    public static final MetricsQueryClientAudience AZURE_RESOURCE_MANAGER_CHINA = fromString("https://management.chinacloudapi.cn");

    /** Static value AZURE_RESOURCE_MANAGER_GERMANY for MetricsQueryClientAudience. */
    public static final MetricsQueryClientAudience AZURE_RESOURCE_MANAGER_GERMANY = fromString("https://management.microsoftazure.de");

    /** Static value AZURE_RESOURCE_MANAGER_GOVERNMENT for MetricsQueryClientAudience. */
    public static final MetricsQueryClientAudience AZURE_RESOURCE_MANAGER_GOVERNMENT = fromString("https://management.usgovcloudapi.net");

    /** Static value AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD for MetricsQueryClientAudience. */
    public static final MetricsQueryClientAudience AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD = fromString("https://management.azure.com");

    /** Static default audience that uses Azure public cloud. */
    public static final MetricsQueryClientAudience DEFAULT = fromString("https://management.azure.com");

    /**
     * Creates or finds a MetricsQueryClientAudience from its string representation.
     * @param name a name to look for.
     * @return the corresponding MetricsQueryClientAudience.
     */
    @JsonCreator
    public static MetricsQueryClientAudience fromString(String name) {
        return fromString(name, MetricsQueryClientAudience.class);
    }

    /** @return known MetricsQueryClientAudience values. */
    public static Collection<MetricsQueryClientAudience> values() {
        return values(MetricsQueryClientAudience.class);
    }
}
