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
 * Defines values for EntityQueryScenario.
 */
public final class EntityQueryScenario extends ExpandableStringEnum<EntityQueryScenario> {
    /** Static value DominantEntity for EntityQueryScenario. */
    public static final EntityQueryScenario DOMINANT_ENTITY = fromString("DominantEntity");

    /** Static value DominantEntityWithDisambiguation for EntityQueryScenario. */
    public static final EntityQueryScenario DOMINANT_ENTITY_WITH_DISAMBIGUATION = fromString("DominantEntityWithDisambiguation");

    /** Static value Disambiguation for EntityQueryScenario. */
    public static final EntityQueryScenario DISAMBIGUATION = fromString("Disambiguation");

    /** Static value List for EntityQueryScenario. */
    public static final EntityQueryScenario LIST = fromString("List");

    /** Static value ListWithPivot for EntityQueryScenario. */
    public static final EntityQueryScenario LIST_WITH_PIVOT = fromString("ListWithPivot");

    /**
     * Creates or finds a EntityQueryScenario from its string representation.
     * @param name a name to look for
     * @return the corresponding EntityQueryScenario
     */
    @JsonCreator
    public static EntityQueryScenario fromString(String name) {
        return fromString(name, EntityQueryScenario.class);
    }

    /**
     * @return known EntityQueryScenario values
     */
    public static Collection<EntityQueryScenario> values() {
        return values(EntityQueryScenario.class);
    }
}
