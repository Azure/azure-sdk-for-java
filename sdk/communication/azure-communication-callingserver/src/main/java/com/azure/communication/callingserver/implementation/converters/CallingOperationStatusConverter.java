// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CallingOperationStatusDto;
import com.azure.communication.callingserver.models.CallingOperationStatus;

/**
 * A converter for {@link CallingOperationStatus}
 */
public final class CallingOperationStatusConverter {
    /**
     * Converts to {@link CallingOperationStatus}
     */
    public static CallingOperationStatus convert(CallingOperationStatusDto callingOperationStatusDto) {
        return CallingOperationStatus.fromString(callingOperationStatusDto.toString());
    }

    private CallingOperationStatusConverter() {

    }
}
