/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Personal Identifier Information details.
 */
public class PII {
    /**
     * The email property.
     */
    @JsonProperty(value = "Email")
    private List<Email> email;

    /**
     * The iPA property.
     */
    @JsonProperty(value = "IPA")
    private List<IPA> iPA;

    /**
     * The phone property.
     */
    @JsonProperty(value = "Phone")
    private List<Phone> phone;

    /**
     * The address property.
     */
    @JsonProperty(value = "Address")
    private List<Address> address;

    /**
     * Get the email value.
     *
     * @return the email value
     */
    public List<Email> email() {
        return this.email;
    }

    /**
     * Set the email value.
     *
     * @param email the email value to set
     * @return the PII object itself.
     */
    public PII withEmail(List<Email> email) {
        this.email = email;
        return this;
    }

    /**
     * Get the iPA value.
     *
     * @return the iPA value
     */
    public List<IPA> iPA() {
        return this.iPA;
    }

    /**
     * Set the iPA value.
     *
     * @param iPA the iPA value to set
     * @return the PII object itself.
     */
    public PII withIPA(List<IPA> iPA) {
        this.iPA = iPA;
        return this;
    }

    /**
     * Get the phone value.
     *
     * @return the phone value
     */
    public List<Phone> phone() {
        return this.phone;
    }

    /**
     * Set the phone value.
     *
     * @param phone the phone value to set
     * @return the PII object itself.
     */
    public PII withPhone(List<Phone> phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Get the address value.
     *
     * @return the address value
     */
    public List<Address> address() {
        return this.address;
    }

    /**
     * Set the address value.
     *
     * @param address the address value to set
     * @return the PII object itself.
     */
    public PII withAddress(List<Address> address) {
        this.address = address;
        return this;
    }

}
