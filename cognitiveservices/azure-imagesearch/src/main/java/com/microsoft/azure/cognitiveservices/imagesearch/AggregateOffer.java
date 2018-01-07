/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a list of offers from merchants that are related to the image.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = AggregateOffer.class)
@JsonTypeName("AggregateOffer")
public class AggregateOffer extends Offer {
    /**
     * A list of offers from merchants that have offerings related to the
     * image.
     */
    @JsonProperty(value = "offers", access = JsonProperty.Access.WRITE_ONLY)
    private List<Offer> offers;

    /**
     * Get the offers value.
     *
     * @return the offers value
     */
    public List<Offer> offers() {
        return this.offers;
    }

}
