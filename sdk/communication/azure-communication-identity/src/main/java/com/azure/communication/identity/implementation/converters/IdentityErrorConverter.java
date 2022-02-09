// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity.implementation.converters;

import com.azure.communication.identity.implementation.models.CommunicationError;
import com.azure.communication.identity.models.IdentityError;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link CommunicationError} and
 * {@link IdentityError}.
 */
public final class IdentityErrorConverter {
    /**
     * Maps from {com.azure.communication.identity.implementation.models.CommunicationError} to {@link IdentityError}.
     * @param communicationError The error to convert
     * 
     * @return the converted IdentityError.
     */
    public static IdentityError convert(CommunicationError communicationError) {
        if (communicationError == null) {
            return null;
        }

        List<IdentityError> details = new ArrayList<IdentityError>();

        if (communicationError.getDetails() != null) {
            details = communicationError.getDetails()
                .stream()
                .map(detail -> convert(detail))
                .collect(Collectors.toList());
        }

        IdentityError identityError = new IdentityError(
            communicationError.getMessage(),
            communicationError.getCode(),
            communicationError.getTarget(),
            details
        );

        return identityError;
    }

    private IdentityErrorConverter() {
    }
}
