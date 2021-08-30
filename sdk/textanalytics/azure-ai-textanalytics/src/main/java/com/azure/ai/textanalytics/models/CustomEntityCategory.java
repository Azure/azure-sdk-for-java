// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The custom entity category enum values.
 */
@Immutable
public final class CustomEntityCategory extends ExpandableStringEnum<CustomEntityCategory> {
    /**
     * Specifies that the entity contains a number or numeric quantity.
     */
    public static final CustomEntityCategory POLITICIAN = fromString("Politician");

    /**
     * Creates or finds a {@link CustomEntityCategory} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link CustomEntityCategory}.
     */
    @JsonCreator
    public static CustomEntityCategory fromString(String name) {
        return fromString(name, CustomEntityCategory.class);
    }
}
