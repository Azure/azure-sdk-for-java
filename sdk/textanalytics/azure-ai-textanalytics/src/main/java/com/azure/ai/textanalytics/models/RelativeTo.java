// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for RelativeTo. */
public final class RelativeTo extends ExpandableStringEnum<RelativeTo> {
    /** Static value Current for RelativeTo. */
    public static final RelativeTo CURRENT = fromString("Current");

    /** Static value End for RelativeTo. */
    public static final RelativeTo END = fromString("End");

    /** Static value Start for RelativeTo. */
    public static final RelativeTo START = fromString("Start");

    /**
     * Creates or finds a RelativeTo from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RelativeTo.
     */
    public static RelativeTo fromString(String name) {
        return fromString(name, RelativeTo.class);
    }
}
