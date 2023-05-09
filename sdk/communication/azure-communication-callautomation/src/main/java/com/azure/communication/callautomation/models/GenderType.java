// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for GenderType. */
public final class GenderType extends ExpandableStringEnum<GenderType> {
    /** Static value male for GenderType. */
    public static final GenderType MALE = fromString("male");

    /** Static value female for GenderType. */
    public static final GenderType FEMALE = fromString("female");

    /**
     * Creates or finds a GenderType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding GenderType.
     */
    @JsonCreator
    public static GenderType fromString(String name) {
        return fromString(name, GenderType.class);
    }

    /**
     * Gets known GenderType values.
     *
     * @return known GenderType values.
     */
    public static Collection<GenderType> values() {
        return values(GenderType.class);
    }
}
