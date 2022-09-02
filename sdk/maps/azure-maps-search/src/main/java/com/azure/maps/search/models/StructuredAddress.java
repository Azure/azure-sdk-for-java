// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

/**
 * StructuredAddress parameter for Search.
 */
public final class StructuredAddress {
    private String streetNumber;
    private String streetName;
    private String crossStreet;
    private String municipality;
    private String municipalitySubdivision;
    private String countryTertiarySubdivision;
    private String countrySecondarySubdivision;
    private String countrySubdivision;
    private String countryCode;
    private String postalCode;

    /**
     * Create a Structure Address with country code.
     * @param countryCode the country code, such as "US", "GB", "FR".
     */
    public StructuredAddress(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Returns the street number.
     * @return the street number
     */
    public String getStreetNumber() {
        return streetNumber;
    }

    /**
     * Returns the street name.
     * @return the street name
     */
    public String getStreetName() {
        return streetName;
    }

    /**
     * Returns the cross street name.
     * @return the cross street name
     */
    public String getCrossStreet() {
        return crossStreet;
    }

    /**
     * Returns the municipality.
     * @return the municipality
     */
    public String getMunicipality() {
        return municipality;
    }

    /**
     * Returns the municipality subdivision.
     * @return the municipality subdivision.
     */
    public String getMunicipalitySubdivision() {
        return municipalitySubdivision;
    }

    /**
     * Returns the country tertiary subdivision.
     * @return the country tertiary subdivision
     */
    public String getCountryTertiarySubdivision() {
        return countryTertiarySubdivision;
    }

    /**
     * Returns the country secondary subdivision.
     * @return the country secondary subdivision
     */
    public String getCountrySecondarySubdivision() {
        return countrySecondarySubdivision;
    }

    /**
     * Returns the country subdivision.
     * @return the country subdivision
     */
    public String getCountrySubdivision() {
        return countrySubdivision;
    }

    /**
     * Returns the country code.
     * @return the country code
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Returns the postal code.
     * @return the postal code
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the street number.
     *
     * @param streetNumber the street number
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
        return this;
    }

    /**
     * Sets the street name.
     * @param streetName the street name
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setStreetName(String streetName) {
        this.streetName = streetName;
        return this;
    }

    /**
     * Sets the cross street.
     * @param crossStreet the cross street name
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setCrossStreet(String crossStreet) {
        this.crossStreet = crossStreet;
        return this;
    }

    /**
     * Sets the municipality.
     * @param municipality the municipality
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setMunicipality(String municipality) {
        this.municipality = municipality;
        return this;
    }

    /**
     * Sets the municipality subdivision.
     * @param municipalitySubdivision the municipality subdivision
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setMunicipalitySubdivision(String municipalitySubdivision) {
        this.municipalitySubdivision = municipalitySubdivision;
        return this;
    }

    /**
     * Sets the country tertiary subdivision.
     * @param countryTertiarySubdivision the country tertiary subdivision
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setCountryTertiarySubdivision(String countryTertiarySubdivision) {
        this.countryTertiarySubdivision = countryTertiarySubdivision;
        return this;
    }

    /**
     * Sets the country secondary subdivision.
     * @param countrySecondarySubdivision the country secondary subdivision
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setCountrySecondarySubdivision(String countrySecondarySubdivision) {
        this.countrySecondarySubdivision = countrySecondarySubdivision;
        return this;
    }

    /**
     * Sets the country subdivision.
     * @param countrySubdivision the country subdivision
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setCountrySubdivision(String countrySubdivision) {
        this.countrySubdivision = countrySubdivision;
        return this;
    }

    /**
     * Sets the postal code.
     * @param postalCode the postal code.
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    /**
     * Sets the country code.
     * @param countryCode the country code
     * @return a reference to this {@code StructuredAddress}
     */
    public StructuredAddress setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }
}
