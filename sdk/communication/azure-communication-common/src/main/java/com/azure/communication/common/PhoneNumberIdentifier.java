// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Communication Services Phone Numbers
 */
public class PhoneNumberIdentifier extends CommunicationIdentifier {

    private final String value;

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
        this.value = phoneNumber;
    }

    /**
     * @return the string identifier representing the object identity
     */
    public String getValue() {
        return value;
    }
}
