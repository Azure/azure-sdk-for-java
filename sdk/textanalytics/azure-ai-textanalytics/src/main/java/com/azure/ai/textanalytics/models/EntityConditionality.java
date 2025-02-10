// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The {@code EntityConditionality} model.
 */
@Immutable
public final class EntityConditionality extends ExpandableStringEnum<EntityConditionality> {
    /** Enum value Hypothetical. */
    public static final EntityConditionality HYPOTHETICAL = fromString("Hypothetical");

    /** Enum value Conditional. */
    public static final EntityConditionality CONDITIONAL = fromString("Conditional");

    /**
     * Creates a new instance of {@code EntityConditionality} value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public EntityConditionality() {
    }

    /**
     * Creates or finds a {@code EntityConditionality} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@code EntityConditionality}.
     */
    public static EntityConditionality fromString(String name) {
        return fromString(name, EntityConditionality.class);
    }

    /**
     * All known EntityConditionality values.
     *
     * @return known EntityConditionality values.
     */
    public static Collection<EntityConditionality> values() {
        return values(EntityConditionality.class);
    }
}
