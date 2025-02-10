// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * An expandable enum that describes Azure cloud environment.
 */
public final class AzureCloud extends ExpandableStringEnum<AzureCloud> {
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

    /**
     * Creates or finds an AzureCloud from its string representation.
     *
     * @param cloudName cloud name to look for
     * @return the corresponding AzureCloud
     */
    public static AzureCloud fromString(String cloudName) {
        return fromString(cloudName, AzureCloud.class);
    }

    /**
     * Creates a new instance of {@link AzureCloud} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link AzureCloud} which doesn't have a
     * String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public AzureCloud() {
    }
}
