// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The {@code EntityAssociation} model.
 */
@Immutable
public final class EntityAssociation extends ExpandableStringEnum<EntityAssociation> {
    /**
     * Specifies if the entity is the subject of the text.
     */
    public static final EntityAssociation SUBJECT = fromString("Subject");

    /**
     * Specifies that the entity describes someone else.
     */
    public static final EntityAssociation OTHER = fromString("Other");

    /**
     * Creates a new instance of {@code EntityAssociation} value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public EntityAssociation() {
    }

    /**
     * Creates or finds a {@code EntityAssociation} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@code EntityAssociation}.
     */
    public static EntityAssociation fromString(String name) {
        return fromString(name, EntityAssociation.class);
    }

    /**
     * All known EntityAssociation values.
     *
     * @return known EntityAssociation values.
     */
    public static Collection<EntityAssociation> values() {
        return values(EntityAssociation.class);
    }
}
