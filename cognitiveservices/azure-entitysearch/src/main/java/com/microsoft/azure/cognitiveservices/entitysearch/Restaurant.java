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
 * The Restaurant model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("Restaurant")
public class Restaurant extends FoodEstablishment {
    /**
     * The acceptsReservations property.
     */
    @JsonProperty(value = "acceptsReservations", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean acceptsReservations;

    /**
     * The reservationUrl property.
     */
    @JsonProperty(value = "reservationUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String reservationUrl;

    /**
     * The servesCuisine property.
     */
    @JsonProperty(value = "servesCuisine", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> servesCuisine;

    /**
     * The menuUrl property.
     */
    @JsonProperty(value = "menuUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String menuUrl;

    /**
     * Get the acceptsReservations value.
     *
     * @return the acceptsReservations value
     */
    public Boolean acceptsReservations() {
        return this.acceptsReservations;
    }

    /**
     * Get the reservationUrl value.
     *
     * @return the reservationUrl value
     */
    public String reservationUrl() {
        return this.reservationUrl;
    }

    /**
     * Get the servesCuisine value.
     *
     * @return the servesCuisine value
     */
    public List<String> servesCuisine() {
        return this.servesCuisine;
    }

    /**
     * Get the menuUrl value.
     *
     * @return the menuUrl value
     */
    public String menuUrl() {
        return this.menuUrl;
    }

}
