/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a cooking recipe.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Recipe.class)
@JsonTypeName("Recipe")
public class Recipe extends CreativeWork {
    /**
     * The amount of time the food takes to cook. For example, PT25M. For
     * information about the time format, see
     * http://en.wikipedia.org/wiki/ISO_8601#Durations.
     */
    @JsonProperty(value = "cookTime", access = JsonProperty.Access.WRITE_ONLY)
    private String cookTime;

    /**
     * The amount of time required to prepare the ingredients. For example,
     * PT15M. For information about the time format, see
     * http://en.wikipedia.org/wiki/ISO_8601#Durations.
     */
    @JsonProperty(value = "prepTime", access = JsonProperty.Access.WRITE_ONLY)
    private String prepTime;

    /**
     * The total amount of time it takes to prepare and cook the recipe. For
     * example, PT45M. For information about the time format, see
     * http://en.wikipedia.org/wiki/ISO_8601#Durations.
     */
    @JsonProperty(value = "totalTime", access = JsonProperty.Access.WRITE_ONLY)
    private String totalTime;

    /**
     * Get the cookTime value.
     *
     * @return the cookTime value
     */
    public String cookTime() {
        return this.cookTime;
    }

    /**
     * Get the prepTime value.
     *
     * @return the prepTime value
     */
    public String prepTime() {
        return this.prepTime;
    }

    /**
     * Get the totalTime value.
     *
     * @return the totalTime value
     */
    public String totalTime() {
        return this.totalTime;
    }

}
