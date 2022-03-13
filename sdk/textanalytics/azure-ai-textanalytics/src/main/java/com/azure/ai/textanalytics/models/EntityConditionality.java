// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The {@link EntityConditionality} model.
 */
@Immutable
public final class EntityConditionality extends ExpandableStringEnum<EntityConditionality> {
    /** Enum value Hypothetical. */
    public static final EntityConditionality HYPOTHETICAL = fromString("Hypothetical");

    /** Enum value Conditional. */
    public static final EntityConditionality CONDITIONAL = fromString("Conditional");

    /**
     * Creates or finds a {@link EntityConditionality} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link EntityConditionality}.
     */
    @JsonCreator
    public static EntityConditionality fromString(String name) {
        return fromString(name, EntityConditionality.class);
    }
}
