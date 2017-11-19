/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a postal address.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("PostalAddress")
public class PostalAddress extends StructuredValue {
    /**
     * The streetAddress property.
     */
    @JsonProperty(value = "streetAddress", access = JsonProperty.Access.WRITE_ONLY)
    private String streetAddress;

    /**
     * The city where the street address is located. For example, Seattle.
     */
    @JsonProperty(value = "addressLocality", access = JsonProperty.Access.WRITE_ONLY)
    private String addressLocality;

    /**
     * The addressSubregion property.
     */
    @JsonProperty(value = "addressSubregion", access = JsonProperty.Access.WRITE_ONLY)
    private String addressSubregion;

    /**
     * The state or province code where the street address is located. This
     * could be the two-letter code. For example, WA, or the full name ,
     * Washington.
     */
    @JsonProperty(value = "addressRegion", access = JsonProperty.Access.WRITE_ONLY)
    private String addressRegion;

    /**
     * The zip code or postal code where the street address is located. For
     * example, 98052.
     */
    @JsonProperty(value = "postalCode", access = JsonProperty.Access.WRITE_ONLY)
    private String postalCode;

    /**
     * The postOfficeBoxNumber property.
     */
    @JsonProperty(value = "postOfficeBoxNumber", access = JsonProperty.Access.WRITE_ONLY)
    private String postOfficeBoxNumber;

    /**
     * The country/region where the street address is located. This could be
     * the two-letter ISO code. For example, US, or the full name, United
     * States.
     */
    @JsonProperty(value = "addressCountry", access = JsonProperty.Access.WRITE_ONLY)
    private String addressCountry;

    /**
     * The two letter ISO code of this countr. For example, US.
     */
    @JsonProperty(value = "countryIso", access = JsonProperty.Access.WRITE_ONLY)
    private String countryIso;

    /**
     * The neighborhood where the street address is located. For example,
     * Westlake.
     */
    @JsonProperty(value = "neighborhood", access = JsonProperty.Access.WRITE_ONLY)
    private String neighborhood;

    /**
     * Region Abbreviation. For example, WA.
     */
    @JsonProperty(value = "addressRegionAbbreviation", access = JsonProperty.Access.WRITE_ONLY)
    private String addressRegionAbbreviation;

    /**
     * The complete address. For example, 2100 Westlake Ave N, Bellevue, WA
     * 98052.
     */
    @JsonProperty(value = "text", access = JsonProperty.Access.WRITE_ONLY)
    private String text;

    /**
     * Get the streetAddress value.
     *
     * @return the streetAddress value
     */
    public String streetAddress() {
        return this.streetAddress;
    }

    /**
     * Get the addressLocality value.
     *
     * @return the addressLocality value
     */
    public String addressLocality() {
        return this.addressLocality;
    }

    /**
     * Get the addressSubregion value.
     *
     * @return the addressSubregion value
     */
    public String addressSubregion() {
        return this.addressSubregion;
    }

    /**
     * Get the addressRegion value.
     *
     * @return the addressRegion value
     */
    public String addressRegion() {
        return this.addressRegion;
    }

    /**
     * Get the postalCode value.
     *
     * @return the postalCode value
     */
    public String postalCode() {
        return this.postalCode;
    }

    /**
     * Get the postOfficeBoxNumber value.
     *
     * @return the postOfficeBoxNumber value
     */
    public String postOfficeBoxNumber() {
        return this.postOfficeBoxNumber;
    }

    /**
     * Get the addressCountry value.
     *
     * @return the addressCountry value
     */
    public String addressCountry() {
        return this.addressCountry;
    }

    /**
     * Get the countryIso value.
     *
     * @return the countryIso value
     */
    public String countryIso() {
        return this.countryIso;
    }

    /**
     * Get the neighborhood value.
     *
     * @return the neighborhood value
     */
    public String neighborhood() {
        return this.neighborhood;
    }

    /**
     * Get the addressRegionAbbreviation value.
     *
     * @return the addressRegionAbbreviation value
     */
    public String addressRegionAbbreviation() {
        return this.addressRegionAbbreviation;
    }

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String text() {
        return this.text;
    }

}
