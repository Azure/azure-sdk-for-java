// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.utils.ExpandableEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An expandable enum that describes Azure cloud environment.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class AzureCloud implements ExpandableEnum<String> {
    private static final Map<String, AzureCloud> VALUES = new ConcurrentHashMap<>();

    /**
     * Azure public cloud.
     */
    public static final AzureCloud AZURE_PUBLIC_CLOUD = fromString("AZURE_PUBLIC_CLOUD");
    /**
     * Azure China cloud.
     */
    public static final AzureCloud AZURE_CHINA_CLOUD = fromString("AZURE_CHINA_CLOUD");
    /**
     * Azure US government cloud.
     */
    public static final AzureCloud AZURE_US_GOVERNMENT_CLOUD = fromString("AZURE_US_GOVERNMENT");

    private final String cloudName;

    private AzureCloud(String cloudName) {
        this.cloudName = cloudName;
    }

    /**
     * Creates or finds an AzureCloud from its string representation.
     *
     * @param cloudName cloud name to look for
     * @return the corresponding AzureCloud
     */
    public static AzureCloud fromString(String cloudName) {
        if (cloudName == null) {
            return null;
        }
        return VALUES.computeIfAbsent(cloudName, AzureCloud::new);
    }

    @Override
    public String getValue() {
        return this.cloudName;
    }

    @Override
    public String toString() {
        return this.cloudName;
    }
}
