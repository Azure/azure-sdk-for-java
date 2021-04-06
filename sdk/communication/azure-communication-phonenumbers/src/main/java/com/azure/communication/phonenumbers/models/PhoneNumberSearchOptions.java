// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers.models;

import com.azure.core.annotation.Fluent;

/** The PhoneNumberSearchOptions model. */
@Fluent
public final class PhoneNumberSearchOptions {

    private String areaCode;
    private Integer quantity;

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
}
