// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;

/**
 * A converter for {@link AddParticipantRequest}
 */
public final class PhoneNumberIdentifierConverter {

    /**
     * Converts to {@link PhoneNumberIdentifierModel}.
     */
    public static PhoneNumberIdentifierModel convert(String phoneNumber) {

        PhoneNumberIdentifierModel phoneNumberIdentifierModel =
            (phoneNumber == null || phoneNumber.isEmpty()) ? null
                : CommunicationIdentifierConverter
                    .convert(new PhoneNumberIdentifier(phoneNumber)).getPhoneNumber();
        return phoneNumberIdentifierModel;
    }

    private PhoneNumberIdentifierConverter() {
    }
}
