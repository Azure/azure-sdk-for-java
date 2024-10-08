// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Enum to determine the job tier.
 */
public final class JobTier extends ExpandableStringEnum<JobTier> {
    /**
     * Static value Null for JobTier.
     */
    public static final JobTier NULL = fromString("Null");

    /**
     * Static value Spot for JobTier.
     */
    public static final JobTier SPOT = fromString("Spot");

    /**
     * Static value Basic for JobTier.
     */
    public static final JobTier BASIC = fromString("Basic");

    /**
     * Static value Standard for JobTier.
     */
    public static final JobTier STANDARD = fromString("Standard");

    /**
     * Static value Premium for JobTier.
     */
    public static final JobTier PREMIUM = fromString("Premium");

    /**
     * Creates a new instance of JobTier value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public JobTier() {
    }

    /**
     * Creates or finds a JobTier from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding JobTier.
     */
    public static JobTier fromString(String name) {
        return fromString(name, JobTier.class);
    }

    /**
     * Gets known JobTier values.
     * 
     * @return known JobTier values.
     */
    public static Collection<JobTier> values() {
        return values(JobTier.class);
    }
}
