// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.TransferCallResultInternal;
import com.azure.communication.callingserver.models.TransferCallResult;

/**
 * A converter between {@link TransferCallResultInternal} and {@link TransferCallResult}.
 */
public final class TransferCallResultConverter {

    /**
     * Maps from {@link TransferCallResultInternal} to {@link TransferCallResult}.
     */
    public static TransferCallResult convert(TransferCallResultInternal transferCallResultInternal) {
        if (transferCallResultInternal == null) {
            return null;
        }

        return new TransferCallResult(
            transferCallResultInternal.getOperationId(),
            transferCallResultInternal.getStatus(),
            transferCallResultInternal.getOperationContext(),
            ResultDetailsConverter.convert(transferCallResultInternal.getResultDetails()));
    }
}
