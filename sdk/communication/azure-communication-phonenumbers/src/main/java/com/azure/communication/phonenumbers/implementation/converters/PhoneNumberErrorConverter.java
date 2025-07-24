// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.implementation.converters;

import com.azure.communication.phonenumbers.implementation.models.CommunicationError;
import com.azure.communication.phonenumbers.models.PhoneNumberError;

import java.util.ArrayList;
import java.util.List;

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

        List<PhoneNumberError> details;
        if (communicationError.getDetails() != null) {
            details = new ArrayList<>(communicationError.getDetails().size());
            for (CommunicationError error : communicationError.getDetails()) {
                details.add(PhoneNumberErrorConverter.convert(error));
            }
        } else {
            details = new ArrayList<>();
        }

        return new PhoneNumberError(communicationError.getMessage(), communicationError.getCode(),
            communicationError.getTarget(), details);
    }

    private PhoneNumberErrorConverter() {
    }
}
