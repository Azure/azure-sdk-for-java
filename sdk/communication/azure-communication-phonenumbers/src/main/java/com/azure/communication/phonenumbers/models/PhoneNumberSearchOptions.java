// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers.models;

import com.azure.core.annotation.Fluent;

/** The PhoneNumberSearchOptions model. */
@Fluent
public final class PhoneNumberSearchOptions {

    private String areaCode;
    private Integer quantity;
    private String locality;
    private String administrativeDivision;

    /**
     * Sets the area code property.
     * @param areaCode the areaCode value to set.
     * @return the PhoneNumberSearchOptions object itself.
     */
    public PhoneNumberSearchOptions setAreaCode(String areaCode) {
        this.areaCode = areaCode;
        return this;
    }

    /**
     * Gets the area code property.
     * @return the area code.
     */
    public String getAreaCode() {
        return this.areaCode;
    }

    /**
     * Sets the quantity property.
     * @param quantity the quantity to set.
     * @return the PhoneNumberSearchOptions object itself.
     */
    public PhoneNumberSearchOptions setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * Gets the quantity property.
     * @return the quantity.
     */
    public Integer getQuantity() {
        return this.quantity;
    }

    /**
     * Sets the locality property.  Examples of a locality are city and town. This is applied only on geographic phone number searches.
     * @param locality the quantity to set.
     * @return the PhoneNumberSearchOptions object itself.
     */
    public PhoneNumberSearchOptions setLocality(String locality) {
        this.locality = locality;
        return this;
    }

    /**
     * Gets the locality property.  Examples of a locality are city and town.
     * @return the quantity.
     */
    public String getLocality() {
        return this.locality;
    }

    /**
     * Sets the administrative division property.  Examples of a administrative division are state and province. This is applied only on geographic phone number searches.
     * @param administrativeDivision the quantity to set.
     * @return the PhoneNumberSearchOptions object itself.
     */
    public PhoneNumberSearchOptions setAdministrativeDivision(String administrativeDivision) {
        this.administrativeDivision = administrativeDivision;
        return this;
    }

    /**
     * Gets the administrative division property.  Examples of a administrative division are state and province.
     * @return the quantity.
     */
    public String getAdministrativeDivision() {
        return this.administrativeDivision;
    }
}
