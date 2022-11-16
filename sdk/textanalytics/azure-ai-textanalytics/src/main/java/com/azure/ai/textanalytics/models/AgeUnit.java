// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for AgeUnit. */
public final class AgeUnit extends ExpandableStringEnum<AgeUnit> {
    /** Static value Unspecified for AgeUnit. */
    public static final AgeUnit UNSPECIFIED = fromString("Unspecified");

    /** Static value Year for AgeUnit. */
    public static final AgeUnit YEAR = fromString("Year");

    /** Static value Month for AgeUnit. */
    public static final AgeUnit MONTH = fromString("Month");

    /** Static value Week for AgeUnit. */
    public static final AgeUnit WEEK = fromString("Week");

    /** Static value Day for AgeUnit. */
    public static final AgeUnit DAY = fromString("Day");

    /**
     * Creates or finds a AgeUnit from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AgeUnit.
     */
    public static AgeUnit fromString(String name) {
        return fromString(name, AgeUnit.class);
    }
}

