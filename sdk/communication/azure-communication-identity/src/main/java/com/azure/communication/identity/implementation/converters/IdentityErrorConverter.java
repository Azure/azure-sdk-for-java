// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity.implementation.converters;

import com.azure.communication.identity.implementation.models.CommunicationError;
import com.azure.communication.identity.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.identity.models.IdentityError;
import com.azure.communication.identity.models.IdentityErrorResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link CommunicationErrorResponseException} and
 * {@link IdentityErrorResponseException}.
 */
public final class IdentityErrorConverter {

    /**
     * Translate from {@link CommunicationErrorResponseException} to {@link IdentityErrorResponseException}.
     * @param exception The CommunicationErrorResponseException to translate
     *
     * @return the converted IdentityErrorResponseException.
     */
    public static IdentityErrorResponseException translateException(CommunicationErrorResponseException exception) {
        IdentityError error = null;
        if (exception.getValue() != null) {
            error = convert(exception.getValue().getError());
        }
        return new IdentityErrorResponseException(exception.getMessage(), exception.getResponse(), error);
    }

    /**
     * Maps fields from {@link CommunicationError} to {@link IdentityError}.
     * @param communicationError The error to convert
     *
     * @return the converted IdentityError.
     */
    public static IdentityError convert(CommunicationError communicationError) {
        if (communicationError == null) {
            return null;
        }

        List<IdentityError> details = new ArrayList<>();

        if (communicationError.getDetails() != null) {
            details = communicationError.getDetails()
                .stream()
                .map(IdentityErrorConverter::convert)
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
