// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.implementation.converters;

import com.azure.communication.phonenumbers.implementation.models.CommunicationError;
import com.azure.communication.phonenumbers.models.PhoneNumberError;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link CommunicationError} and
 * {@link PhoneNumberError}.
 */
public final class PhoneNumberErrorConverter {
    /**
     * Maps from {com.azure.communication.phonenumbers.implementation.models.CommunicationError} to {@link PhoneNumberError}.
     * @param communicationError The error to convert
     * 
     * @return the converted PhoneNumberError.
     */
    public static PhoneNumberError convert(CommunicationError communicationError) {
        if (communicationError == null) {
            return null;
        }

        List<PhoneNumberError> details = new ArrayList<PhoneNumberError>();

        if (communicationError.getDetails() != null) {
            details = communicationError.getDetails()
                .stream()
                .map(detail -> convert(detail))
                .collect(Collectors.toList());
        }

        PhoneNumberError phoneNumberError = new PhoneNumberError(
            communicationError.getMessage(),
            communicationError.getCode(),
            communicationError.getTarget(),
            details
        );

        return phoneNumberError;
    }

    private PhoneNumberErrorConverter() {
    }
}
