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
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * The LocalBusiness model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("LocalBusiness")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "EntertainmentBusiness", value = EntertainmentBusiness.class),
    @JsonSubTypes.Type(name = "FoodEstablishment", value = FoodEstablishment.class),
    @JsonSubTypes.Type(name = "LodgingBusiness", value = LodgingBusiness.class)
})
public class LocalBusiness extends Place {
    /**
     * $$.
     */
    @JsonProperty(value = "priceRange", access = JsonProperty.Access.WRITE_ONLY)
    private String priceRange;

    /**
     * The panoramas property.
     */
    @JsonProperty(value = "panoramas", access = JsonProperty.Access.WRITE_ONLY)
    private List<ImageObject> panoramas;

    /**
     * The isPermanentlyClosed property.
     */
    @JsonProperty(value = "isPermanentlyClosed", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean isPermanentlyClosed;

    /**
     * The tagLine property.
     */
    @JsonProperty(value = "tagLine", access = JsonProperty.Access.WRITE_ONLY)
    private String tagLine;

    /**
     * Get the priceRange value.
     *
     * @return the priceRange value
     */
    public String priceRange() {
        return this.priceRange;
    }

    /**
     * Get the panoramas value.
     *
     * @return the panoramas value
     */
    public List<ImageObject> panoramas() {
        return this.panoramas;
    }

    /**
     * Get the isPermanentlyClosed value.
     *
     * @return the isPermanentlyClosed value
     */
    public Boolean isPermanentlyClosed() {
        return this.isPermanentlyClosed;
    }

    /**
     * Get the tagLine value.
     *
     * @return the tagLine value
     */
    public String tagLine() {
        return this.tagLine;
    }

}
