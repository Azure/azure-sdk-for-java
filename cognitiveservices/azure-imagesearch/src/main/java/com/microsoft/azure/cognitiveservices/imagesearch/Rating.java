/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * Defines a rating.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Rating.class)
@JsonTypeName("Rating")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "AggregateRating", value = AggregateRating.class)
})
public class Rating extends PropertiesItem {
    /**
     * The mean (average) rating. The possible values are 1.0 through 5.0.
     */
    @JsonProperty(value = "ratingValue", required = true)
    private double ratingValue;

    /**
     * The highest rated review. The possible values are 1.0 through 5.0.
     */
    @JsonProperty(value = "bestRating", access = JsonProperty.Access.WRITE_ONLY)
    private Double bestRating;

    /**
     * Get the ratingValue value.
     *
     * @return the ratingValue value
     */
    public double ratingValue() {
        return this.ratingValue;
    }

    /**
     * Set the ratingValue value.
     *
     * @param ratingValue the ratingValue value to set
     * @return the Rating object itself.
     */
    public Rating withRatingValue(double ratingValue) {
        this.ratingValue = ratingValue;
        return this;
    }

    /**
     * Get the bestRating value.
     *
     * @return the bestRating value
     */
    public Double bestRating() {
        return this.bestRating;
    }

}
