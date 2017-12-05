/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines additional information about an entity such as type hints.
 */
public class EntitiesEntityPresentationInfo {
    /**
     * The supported scenario. Possible values include: 'DominantEntity',
     * 'DisambiguationItem', 'ListItem'.
     */
    @JsonProperty(value = "entityScenario", required = true)
    private EntityScenario entityScenario;

    /**
     * A list of hints that indicate the entity's type. The list could contain
     * a single hint such as Movie or a list of hints such as Place,
     * LocalBusiness, Restaurant. Each successive hint in the array narrows the
     * entity's type.
     */
    @JsonProperty(value = "entityTypeHints", access = JsonProperty.Access.WRITE_ONLY)
    private List<EntityType> entityTypeHints;

    /**
     * A display version of the entity hint. For example, if entityTypeHints is
     * Artist, this field may be set to American Singer.
     */
    @JsonProperty(value = "entityTypeDisplayHint", access = JsonProperty.Access.WRITE_ONLY)
    private String entityTypeDisplayHint;

    /**
     * Get the entityScenario value.
     *
     * @return the entityScenario value
     */
    public EntityScenario entityScenario() {
        return this.entityScenario;
    }

    /**
     * Set the entityScenario value.
     *
     * @param entityScenario the entityScenario value to set
     * @return the EntitiesEntityPresentationInfo object itself.
     */
    public EntitiesEntityPresentationInfo withEntityScenario(EntityScenario entityScenario) {
        this.entityScenario = entityScenario;
        return this;
    }

    /**
     * Get the entityTypeHints value.
     *
     * @return the entityTypeHints value
     */
    public List<EntityType> entityTypeHints() {
        return this.entityTypeHints;
    }

    /**
     * Get the entityTypeDisplayHint value.
     *
     * @return the entityTypeDisplayHint value
     */
    public String entityTypeDisplayHint() {
        return this.entityTypeDisplayHint;
    }

}
