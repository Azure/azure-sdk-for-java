// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The {@link EntityAssociation} model.
 */
@Immutable
public final class EntityAssociation extends ExpandableStringEnum<EntityAssociation> {
    /**
     * Specifies that the entity contains a number or numeric quantity.
     */
    public static final EntityAssociation SUBJECT = fromString("Subject");

    /**
     * Specifies that the entity association value: OTHER.
     */
    public static final EntityAssociation OTHER = fromString("Other");

    /**
     * Creates or finds a {@link EntityAssociation} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link EntityAssociation}.
     */
    @JsonCreator
    public static EntityAssociation fromString(String name) {
        return fromString(name, EntityAssociation.class);
    }
}
