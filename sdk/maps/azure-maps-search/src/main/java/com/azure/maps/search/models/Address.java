// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import java.util.List;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.GeoBoundingBox;
import com.azure.maps.search.implementation.helpers.AddressPropertiesHelper;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.implementation.models.AddressPrivate;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** The address of the result. */
@Immutable
public final class Address {
    private String buildingNumber;
    private String street;
    private String crossStreet;
    private String streetNumber;
    private List<String> routeNumbers;
    private String streetName;
    private String streetNameAndNumber;
    private String municipality;
    private String municipalitySubdivision;
    private String countryTertiarySubdivision;
    private String countrySecondarySubdivision;
    private String countrySubdivision;
    private String postalCode;
    private String extendedPostalCode;
    private String countryCode;
    private String country;
    private String countryCodeISO3;
    private String freeformAddress;
    private String countrySubdivisionName;
    private String localName;
    @JsonIgnore
    private GeoBoundingBox boundingBox;

    static {
        AddressPropertiesHelper.setAccessor(new AddressPropertiesHelper.AddressAccessor() {
            @Override
            public void setFromAddressPrivate(Address address, AddressPrivate addressPrivate) {
                address.setFromAddressPrivate(addressPrivate);
            }
        });
    }

    /**
     * Get the buildingNumber property: The building number on the street. DEPRECATED, use streetNumber instead.
     *
     * @return the buildingNumber value.
     */
    public String getBuildingNumber() {
        return this.buildingNumber;
    }

    /**
     * Get the street property: The street name. DEPRECATED, use streetName instead.
     *
     * @return the street value.
     */
    public String getStreet() {
        return this.street;
    }

    /**
     * Get the crossStreet property: The name of the street being crossed.
     *
     * @return the crossStreet value.
     */
    public String getCrossStreet() {
        return this.crossStreet;
    }

    /**
     * Get the streetNumber property: The building number on the street.
     *
     * @return the streetNumber value.
     */
    public String getStreetNumber() {
        return this.streetNumber;
    }

    /**
     * Get the routeNumbers property: The codes used to unambiguously identify the street.
     *
     * @return the routeNumbers value.
     */
    public List<String> getRouteNumbers() {
        return this.routeNumbers;
    }

    /**
     * Get the streetName property: The street name.
     *
     * @return the streetName value.
     */
    public String getStreetName() {
        return this.streetName;
    }

    /**
     * Get the streetNameAndNumber property: The street name and number.
     *
     * @return the streetNameAndNumber value.
     */
    public String getStreetNameAndNumber() {
        return this.streetNameAndNumber;
    }

    /**
     * Get the municipality property: City / Town.
     *
     * @return the municipality value.
     */
    public String getMunicipality() {
        return this.municipality;
    }

    /**
     * Get the municipalitySubdivision property: Sub / Super City.
     *
     * @return the municipalitySubdivision value.
     */
    public String getMunicipalitySubdivision() {
        return this.municipalitySubdivision;
    }

    /**
     * Get the countryTertiarySubdivision property: Named Area.
     *
     * @return the countryTertiarySubdivision value.
     */
    public String getCountryTertiarySubdivision() {
        return this.countryTertiarySubdivision;
    }

    /**
     * Get the countrySecondarySubdivision property: County.
     *
     * @return the countrySecondarySubdivision value.
     */
    public String getCountrySecondarySubdivision() {
        return this.countrySecondarySubdivision;
    }

    /**
     * Get the countrySubdivision property: State or Province.
     *
     * @return the countrySubdivision value.
     */
    public String getCountrySubdivision() {
        return this.countrySubdivision;
    }

    /**
     * Get the postalCode property: Postal Code / Zip Code.
     *
     * @return the postalCode value.
     */
    public String getPostalCode() {
        return this.postalCode;
    }

    /**
     * Get the extendedPostalCode property: Extended postal code (availability is dependent on the region).
     *
     * @return the extendedPostalCode value.
     */
    public String getExtendedPostalCode() {
        return this.extendedPostalCode;
    }

    /**
     * Get the countryCode property: Country (Note: This is a two-letter code, not a country name.).
     *
     * @return the countryCode value.
     */
    public String getCountryCode() {
        return this.countryCode;
    }

    /**
     * Get the country property: Country name.
     *
     * @return the country value.
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * Get the countryCodeISO3 property: ISO alpha-3 country code.
     *
     * @return the countryCodeISO3 value.
     */
    public String getCountryCodeISO3() {
        return this.countryCodeISO3;
    }

    /**
     * Get the freeformAddress property: An address line formatted according to the formatting rules of a Result's
     * country of origin, or in the case of a country, its full country name.
     *
     * @return the freeformAddress value.
     */
    public String getFreeformAddress() {
        return this.freeformAddress;
    }

    /**
     * Get the countrySubdivisionName property: The full name of a first level of country administrative hierarchy. This
     * field appears only in case countrySubdivision is presented in an abbreviated form. Only supported for USA,
     * Canada, and Great Britain.
     *
     * @return the countrySubdivisionName value.
     */
    public String getCountrySubdivisionName() {
        return this.countrySubdivisionName;
    }

    /**
     * Get the localName property: An address component which represents the name of a geographic area or locality that
     * groups a number of addressable objects for addressing purposes, without being an administrative unit. This field
     * is used to build the `freeformAddress` property.
     *
     * @return the localName value.
     */
    public String getLocalName() {
        return this.localName;
    }

    /**
     * Get the boundingBox property: The bounding box of the location.
     *
     * @return the boundingBox value.
     */
    public GeoBoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    // private setter for properties that need transformation
    private void setFromAddressPrivate(AddressPrivate addressPrivate) {
        this.buildingNumber = addressPrivate.getBuildingNumber();
        this.street = addressPrivate.getStreet();
        this.crossStreet = addressPrivate.getCrossStreet();
        this.streetNumber = addressPrivate.getStreetNumber();
        this.routeNumbers = addressPrivate.getRouteNumbers();
        this.streetName = addressPrivate.getStreetName();
        this.streetNameAndNumber = addressPrivate.getStreetNameAndNumber();
        this.municipality = addressPrivate.getMunicipality();
        this.municipalitySubdivision = addressPrivate.getMunicipalitySubdivision();
        this.countryTertiarySubdivision = addressPrivate.getCountryTertiarySubdivision();
        this.countrySecondarySubdivision = addressPrivate.getCountrySecondarySubdivision();
        this.countrySubdivision = addressPrivate.getCountrySubdivision();
        this.postalCode = addressPrivate.getPostalCode();
        this.extendedPostalCode = addressPrivate.getExtendedPostalCode();
        this.countryCode = addressPrivate.getCountryCode();
        this.country = addressPrivate.getCountry();
        this.freeformAddress = addressPrivate.getFreeformAddress();
        this.countrySubdivisionName = addressPrivate.getCountrySubdivisionName();
        this.localName = addressPrivate.getLocalName();

        // transform bounding box
        if (addressPrivate.getBoundingBox() != null) {
            this.boundingBox = Utility.toGeoBoundingBox(addressPrivate.getBoundingBox());
        }
    }
}
