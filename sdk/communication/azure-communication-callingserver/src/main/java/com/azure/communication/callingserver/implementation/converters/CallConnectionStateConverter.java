// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CallConnectionStateModel;
import com.azure.communication.callingserver.models.CallConnectionState;

/**
 * A converter for {@link CallConnectionState}
 */
public final class CallConnectionStateConverter {
    /**
     * Converts to {@link CallConnectionState}
     */
    public static CallConnectionState convert(CallConnectionStateModel callConnectionStateModel) {
        return CallConnectionState.fromString(callConnectionStateModel.toString());
    }

    private CallConnectionStateConverter() {

    }
}
