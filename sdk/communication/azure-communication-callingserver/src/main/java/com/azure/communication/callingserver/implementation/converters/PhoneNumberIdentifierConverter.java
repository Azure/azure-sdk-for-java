// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.common.PhoneNumberIdentifier;

/**
 * A converter for {@link PhoneNumberIdentifier}
 */
public final class PhoneNumberIdentifierConverter {

    /**
     * Converts to {@link PhoneNumberIdentifierModel}.
     */
    public static PhoneNumberIdentifierModel convert(PhoneNumberIdentifier phoneNumberIdentifier) {

        PhoneNumberIdentifierModel phoneNumberIdentifierModel =
            (phoneNumberIdentifier == null || phoneNumberIdentifier.getPhoneNumber().isEmpty()) ? null
                : CommunicationIdentifierConverter
                    .convert(new PhoneNumberIdentifier(phoneNumberIdentifier.getPhoneNumber())).getPhoneNumber();
        return phoneNumberIdentifierModel;
    }

    private PhoneNumberIdentifierConverter() {
    }
}
