// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The {@code EntityCertainty} model.
 */
@Immutable
public final class EntityCertainty extends ExpandableStringEnum<EntityCertainty> {
    /** Enum value Positive. */
    public static final EntityCertainty POSITIVE = fromString("Positive");

    /** Enum value Positive Possible. */
    public static final EntityCertainty POSITIVE_POSSIBLE = fromString("PositivePossible");

    /** Enum value Neutral Possible. */
    public static final EntityCertainty NEUTRAL_POSSIBLE = fromString("NeutralPossible");

    /** Enum value Negative Possible. */
    public static final EntityCertainty NEGATIVE_POSSIBLE = fromString("NegativePossible");

    /** Enum value Negative. */
    public static final EntityCertainty NEGATIVE = fromString("Negative");

    /**
     * Creates a new instance of {@code EntityCertainty} value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public EntityCertainty() {
    }

    /**
     * Creates or finds a {@code EntityCertainty} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@code EntityCertainty}.
     */
    public static EntityCertainty fromString(String name) {
        return fromString(name, EntityCertainty.class);
    }

    /**
     * All known EntityCertainty values.
     *
     * @return known EntityCertainty values.
     */
    public static Collection<EntityCertainty> values() {
        return values(EntityCertainty.class);
    }
}
