// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CallingOperationResultDetailsDto;
import com.azure.communication.callingserver.models.CallingOperationResultDetails;

/**
 * A converter for {@link CallingOperationResultDetails}
 */
public final class CallingOperationResultDetailsConverter {
    /**
     * Converts to {@link CallingOperationResultDetails}
     */
    public static CallingOperationResultDetails convert(CallingOperationResultDetailsDto callingOperationResultDetailsDto) {
        return new CallingOperationResultDetails(callingOperationResultDetailsDto.getCode(), callingOperationResultDetailsDto.getSubcode(),
            callingOperationResultDetailsDto.getMessage());
    }

    private CallingOperationResultDetailsConverter() {

    }
}
