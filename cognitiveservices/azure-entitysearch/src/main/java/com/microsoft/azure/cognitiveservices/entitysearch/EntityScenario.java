/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for EntityScenario.
 */
public final class EntityScenario extends ExpandableStringEnum<EntityScenario> {
    /** Static value DominantEntity for EntityScenario. */
    public static final EntityScenario DOMINANT_ENTITY = fromString("DominantEntity");

    /** Static value DisambiguationItem for EntityScenario. */
    public static final EntityScenario DISAMBIGUATION_ITEM = fromString("DisambiguationItem");

    /** Static value ListItem for EntityScenario. */
    public static final EntityScenario LIST_ITEM = fromString("ListItem");

    /**
     * Creates or finds a EntityScenario from its string representation.
     * @param name a name to look for
     * @return the corresponding EntityScenario
     */
    @JsonCreator
    public static EntityScenario fromString(String name) {
        return fromString(name, EntityScenario.class);
    }

    /**
     * @return known EntityScenario values
     */
    public static Collection<EntityScenario> values() {
        return values(EntityScenario.class);
    }
}
