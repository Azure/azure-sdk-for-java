// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * A field of a feature flag resource that can be selected in a response.
 */
public final class FeatureFlagFields extends ExpandableStringEnum<FeatureFlagFields> {
    /** The feature flag name. */
    public static final FeatureFlagFields NAME = fromString("name");

    /** The enabled state. */
    public static final FeatureFlagFields ENABLED = fromString("enabled");

    /** The label. */
    public static final FeatureFlagFields LABEL = fromString("label");

    /** The description. */
    public static final FeatureFlagFields DESCRIPTION = fromString("description");

    /** The evaluation conditions. */
    public static final FeatureFlagFields CONDITIONS = fromString("conditions");

    /** The variant definitions. */
    public static final FeatureFlagFields VARIANTS = fromString("variants");

    /** The variant allocation. */
    public static final FeatureFlagFields ALLOCATION = fromString("allocation");

    /** The telemetry configuration. */
    public static final FeatureFlagFields TELEMETRY = fromString("telemetry");

    /** The resource tags. */
    public static final FeatureFlagFields TAGS = fromString("tags");

    /** The last modification time. */
    public static final FeatureFlagFields LAST_MODIFIED = fromString("last_modified");

    /** The entity tag. */
    public static final FeatureFlagFields ETAG = fromString("etag");

    /**
     * Creates a feature flag field.
     * @deprecated Use {@link #fromString(String)} instead.
     */
    @Deprecated
    public FeatureFlagFields() {
    }

    /**
     * Creates or finds a field from its wire value.
     * @param name The wire value.
     * @return The field.
     */
    public static FeatureFlagFields fromString(String name) {
        return fromString(name, FeatureFlagFields.class);
    }

    /**
     * Gets the known fields.
     * @return The known fields.
     */
    public static Collection<FeatureFlagFields> values() {
        return values(FeatureFlagFields.class);
    }
}
