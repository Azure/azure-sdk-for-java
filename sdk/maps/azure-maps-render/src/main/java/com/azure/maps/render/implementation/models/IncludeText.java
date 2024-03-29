// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.maps.render.implementation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for IncludeText. */
public final class IncludeText extends ExpandableStringEnum<IncludeText> {
    /** Static value yes for IncludeText. */
    public static final IncludeText YES = fromString("yes");

    /** Static value no for IncludeText. */
    public static final IncludeText NO = fromString("no");

    /**
     * Creates or finds a IncludeText from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding IncludeText.
     */
    @JsonCreator
    public static IncludeText fromString(String name) {
        return fromString(name, IncludeText.class);
    }

    /**
     * Gets known IncludeText values.
     *
     * @return known IncludeText values.
     */
    public static Collection<IncludeText> values() {
        return values(IncludeText.class);
    }
}
