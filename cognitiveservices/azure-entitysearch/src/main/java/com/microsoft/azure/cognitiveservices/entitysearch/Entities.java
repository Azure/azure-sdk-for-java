/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines an entity answer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("Entities")
public class Entities extends SearchResultsAnswer {
    /**
     * The supported query scenario. This field is set to DominantEntity or
     * DisambiguationItem. The field is set to DominantEntity if Bing
     * determines that only a single entity satisfies the request. For example,
     * a book, movie, person, or attraction. If multiple entities could satisfy
     * the request, the field is set to DisambiguationItem. For example, if the
     * request uses the generic title of a movie franchise, the entity's type
     * would likely be DisambiguationItem. But, if the request specifies a
     * specific title from the franchise, the entity's type would likely be
     * DominantEntity. Possible values include: 'DominantEntity',
     * 'DominantEntityWithDisambiguation', 'Disambiguation', 'List',
     * 'ListWithPivot'.
     */
    @JsonProperty(value = "queryScenario", access = JsonProperty.Access.WRITE_ONLY)
    private EntityQueryScenario queryScenario;

    /**
     * A list of entities.
     */
    @JsonProperty(value = "value", required = true)
    private List<Thing> value;

    /**
     * Get the queryScenario value.
     *
     * @return the queryScenario value
     */
    public EntityQueryScenario queryScenario() {
        return this.queryScenario;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<Thing> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the Entities object itself.
     */
    public Entities withValue(List<Thing> value) {
        this.value = value;
        return this;
    }

}
