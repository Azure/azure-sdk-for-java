/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for Freshness.
 */
public final class Freshness extends ExpandableStringEnum<Freshness> {
    /** Static value Day for Freshness. */
    public static final Freshness DAY = fromString("Day");

    /** Static value Week for Freshness. */
    public static final Freshness WEEK = fromString("Week");

    /** Static value Month for Freshness. */
    public static final Freshness MONTH = fromString("Month");

    /**
     * Creates or finds a Freshness from its string representation.
     * @param name a name to look for
     * @return the corresponding Freshness
     */
    @JsonCreator
    public static Freshness fromString(String name) {
        return fromString(name, Freshness.class);
    }

    /**
     * @return known Freshness values
     */
    public static Collection<Freshness> values() {
        return values(Freshness.class);
    }
}
