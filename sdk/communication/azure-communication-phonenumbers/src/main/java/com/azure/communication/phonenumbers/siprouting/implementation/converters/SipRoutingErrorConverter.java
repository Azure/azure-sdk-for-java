// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting.implementation.converters;

import com.azure.communication.phonenumbers.siprouting.implementation.models.CommunicationError;
import com.azure.communication.phonenumbers.siprouting.models.SipRoutingError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link CommunicationError} and
 * {@link SipRoutingError}.
 */
public final class SipRoutingErrorConverter {
    /**
     * Maps from {com.azure.communication.phonenumbers.siprouting.implementation.models.CommunicationError} to {@link SipRoutingError}.
     * @param communicationError The error to convert
     *
     * @return the converted SipRoutingError.
     */
    public static SipRoutingError convert(CommunicationError communicationError) {
        if (communicationError == null) {
            return null;
        }

        List<SipRoutingError> details = new ArrayList<>();

        if (communicationError.getDetails() != null) {
            details = communicationError.getDetails()
                .stream()
                .map(SipRoutingErrorConverter::convert)
                .collect(Collectors.toList());
        }

        return new SipRoutingError(
            communicationError.getCode(),
            communicationError.getMessage(),
            communicationError.getTarget(),
            details,
            convert(communicationError.getInnerError())
        );
    }

    private SipRoutingErrorConverter() {
    }
}
