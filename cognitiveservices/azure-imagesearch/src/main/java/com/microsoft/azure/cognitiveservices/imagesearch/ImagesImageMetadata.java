/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a count of the number of websites where you can shop or perform
 * other actions related to the image.
 */
public class ImagesImageMetadata {
    /**
     * The number of websites that offer goods of the products seen in the
     * image.
     */
    @JsonProperty(value = "shoppingSourcesCount", access = JsonProperty.Access.WRITE_ONLY)
    private Integer shoppingSourcesCount;

    /**
     * The number of websites that offer recipes of the food seen in the image.
     */
    @JsonProperty(value = "recipeSourcesCount", access = JsonProperty.Access.WRITE_ONLY)
    private Integer recipeSourcesCount;

    /**
     * A summary of the online offers of products found in the image. For
     * example, if the image is of a dress, the offer might identify the lowest
     * price and the number of offers found. Only visually similar products
     * insights include this field. The offer includes the following fields:
     * Name, AggregateRating, OfferCount, and LowPrice.
     */
    @JsonProperty(value = "aggregateOffer", access = JsonProperty.Access.WRITE_ONLY)
    private AggregateOffer aggregateOffer;

    /**
     * Get the shoppingSourcesCount value.
     *
     * @return the shoppingSourcesCount value
     */
    public Integer shoppingSourcesCount() {
        return this.shoppingSourcesCount;
    }

    /**
     * Get the recipeSourcesCount value.
     *
     * @return the recipeSourcesCount value
     */
    public Integer recipeSourcesCount() {
        return this.recipeSourcesCount;
    }

    /**
     * Get the aggregateOffer value.
     *
     * @return the aggregateOffer value
     */
    public AggregateOffer aggregateOffer() {
        return this.aggregateOffer;
    }

}
