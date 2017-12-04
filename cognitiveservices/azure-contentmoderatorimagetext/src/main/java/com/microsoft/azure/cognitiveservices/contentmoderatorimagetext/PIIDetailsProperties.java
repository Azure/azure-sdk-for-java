/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Personal Identifier Information details.
 */
public class PIIDetailsProperties {
    /**
     * The email property.
     */
    @JsonProperty(value = "email")
    private List<EmailProperties> email;

    /**
     * The ipa property.
     */
    @JsonProperty(value = "ipa")
    private List<IPAProperties> ipa;

    /**
     * The phone property.
     */
    @JsonProperty(value = "phone")
    private List<PhoneProperties> phone;

    /**
     * The address property.
     */
    @JsonProperty(value = "address")
    private List<AddressProperties> address;

    /**
     * Get the email value.
     *
     * @return the email value
     */
    public List<EmailProperties> email() {
        return this.email;
    }

    /**
     * Set the email value.
     *
     * @param email the email value to set
     * @return the PIIDetailsProperties object itself.
     */
    public PIIDetailsProperties withEmail(List<EmailProperties> email) {
        this.email = email;
        return this;
    }

    /**
     * Get the ipa value.
     *
     * @return the ipa value
     */
    public List<IPAProperties> ipa() {
        return this.ipa;
    }

    /**
     * Set the ipa value.
     *
     * @param ipa the ipa value to set
     * @return the PIIDetailsProperties object itself.
     */
    public PIIDetailsProperties withIpa(List<IPAProperties> ipa) {
        this.ipa = ipa;
        return this;
    }

    /**
     * Get the phone value.
     *
     * @return the phone value
     */
    public List<PhoneProperties> phone() {
        return this.phone;
    }

    /**
     * Set the phone value.
     *
     * @param phone the phone value to set
     * @return the PIIDetailsProperties object itself.
     */
    public PIIDetailsProperties withPhone(List<PhoneProperties> phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Get the address value.
     *
     * @return the address value
     */
    public List<AddressProperties> address() {
        return this.address;
    }

    /**
     * Set the address value.
     *
     * @param address the address value to set
     * @return the PIIDetailsProperties object itself.
     */
    public PIIDetailsProperties withAddress(List<AddressProperties> address) {
        this.address = address;
        return this;
    }

}
