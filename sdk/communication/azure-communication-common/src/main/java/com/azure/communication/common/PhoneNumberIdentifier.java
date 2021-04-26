// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Communication Services Phone Numbers
 */
public final class PhoneNumberIdentifier extends CommunicationIdentifier {

    private final String phoneNumber;
    private String rawId;

    /**
     * Creates a PhoneNumberIdentifier object
     *
     * @param phoneNumber the string identifier representing the PhoneNumber in E.164 format.
     * E.164 is a phone number formatted as +[CountryCode][AreaCode][LocalNumber] eg. "+18005555555"
     * @throws IllegalArgumentException thrown if phoneNumber parameter fail the validation.
     */
    public PhoneNumberIdentifier(String phoneNumber) {
        if (CoreUtils.isNullOrEmpty(phoneNumber)) {
            throw new IllegalArgumentException("The initialization parameter [phoneNumber] cannot be null to empty.");
        }
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return the string identifier representing the object identity
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Get full id of the identifier. This id is optional.
     * @return full id of the identifier
     */
    public String getRawId() {
        return rawId;
    }

    /**
     * Set full id of the identifier
     * @param rawId full id of the identifier
     * @return PhoneNumberIdentifier object itself
     */
    public PhoneNumberIdentifier setRawId(String rawId) {
        this.rawId = rawId;
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof PhoneNumberIdentifier)) {
            return false;
        }

        PhoneNumberIdentifier phoneId = (PhoneNumberIdentifier) that;
        if (!phoneNumber.equals(phoneId.phoneNumber)) {
            return false;
        }

        return getRawId() == null
            || phoneId.getRawId() == null
            || getRawId().equals(phoneId.getRawId());
    }

    @Override
    public int hashCode() {
        return phoneNumber.hashCode();
    }
}
