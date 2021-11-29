// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.TransferToCallRequest;

/**
 * A converter for {@link TransferToCallRequest}
 */
public final class TransferToCallRequestConverter {

    /**
     * Converts to {@link TransferToCallRequest}.
     */
    public static TransferToCallRequest convert(
        String targetCallConnectionId,
        String userToUserInformation,
        String operationContext) {

        return new TransferToCallRequest()
            .setUserToUserInformation(userToUserInformation)
            .setTargetCallConnectionId(targetCallConnectionId)
            .setOperationContext(operationContext);
    }

    private TransferToCallRequestConverter() {
    }
}
