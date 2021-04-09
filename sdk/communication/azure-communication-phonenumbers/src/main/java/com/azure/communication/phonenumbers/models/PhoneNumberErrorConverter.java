// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.phonenumbers.implementation.models.CommunicationError} and
 * {@link PhoneNumberError}.
 */
public final class PhoneNumberErrorConverter {
    /**
     * Maps from {com.azure.communication.phonenumbrs.implementation.models.CommunicationError} to {@link PhoneNumberError}.
     */
    public static PhoneNumberError convert(com.azure.communication.phonenumbers.implementation.models.CommunicationError obj) {
        if (obj == null) {
            return null;
        }

        List<PhoneNumberError> details = new ArrayList<PhoneNumberError>();

        if (obj.getDetails() != null) {
            details = obj.getDetails()
                .stream()
                .map(detail -> convert(detail))
                .collect(Collectors.toList());
        }

        PhoneNumberError phoneNumberError = new PhoneNumberError(
            obj.getMessage(),
            obj.getCode(),
            obj.getTarget(),
            details,
            convert(obj.getInnerError())
        );

        return phoneNumberError;
    }

    private PhoneNumberErrorConverter() {
    }
}
