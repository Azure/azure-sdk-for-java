// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Communication Services Phone Numbers
 */
public final class PhoneNumberIdentifier extends CommunicationIdentifier {

    private static final String ANONYMOUS = "anonymous";

    private final String phoneNumber;
    private String assertedId;
    private boolean isAnonymous;

    /**
     * Creates a PhoneNumberIdentifier object
     *
     * @param phoneNumber the string identifier representing the PhoneNumber in E.164 format.
     *                    E.164 is a phone number formatted as +[CountryCode][AreaCode][LocalNumber] eg. "+18005555555"
     * @throws IllegalArgumentException thrown if phoneNumber parameter fail the validation.
     */
    public PhoneNumberIdentifier(String phoneNumber) {
        if (CoreUtils.isNullOrEmpty(phoneNumber)) {
            throw new IllegalArgumentException("The initialization parameter [phoneNumber] cannot be null to empty.");
        }
        this.phoneNumber = phoneNumber;
        this.setRawId(PHONE_NUMBER_PREFIX + phoneNumber);
    }

    /**
     * Gets the string identifier representing the object identity
     *
     * @return the string identifier representing the object identity
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Checks if the phone number is anonymous, e.g., used to represent a hidden caller ID.
     *
     * @return true if the phone number is anonymous, false otherwise.
     */
    public boolean isAnonymous() {
        return isAnonymous;
    }

    /**
     * Gets the asserted ID for the phone number, distinguishing it from other connections made through the same number.
     *
     * @return the string identifier representing the asserted ID for the phone number.
     */
    public String getAssertedId() {
        if (!CoreUtils.isNullOrEmpty(assertedId)) {
            return assertedId;
        }

        String[] segments = getRawId().substring(PHONE_NUMBER_PREFIX.length()).split("_");
        if (segments.length > 1) {
            assertedId = segments[segments.length - 1];
            return assertedId;
        }

        assertedId = "";
        return null;
    }

    /**
     * Set full id of the identifier
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full id of the identifier
     * @return PhoneNumberIdentifier object itself
     */
    @Override
    public PhoneNumberIdentifier setRawId(String rawId) {
        super.setRawId(rawId);
        this.isAnonymous = (PHONE_NUMBER_PREFIX + ANONYMOUS).equals(rawId);
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

        return getRawId() == null || phoneId.getRawId() == null || getRawId().equals(phoneId.getRawId());
    }

    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }
}
