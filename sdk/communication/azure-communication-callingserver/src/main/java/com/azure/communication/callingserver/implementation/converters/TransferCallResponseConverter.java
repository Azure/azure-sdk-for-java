// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.TransferCallResponseInternal;
import com.azure.communication.callingserver.models.TransferCallResponse;

/**
 * A converter for {@link TransferCallResponse}
 */
public final class TransferCallResponseConverter {

    /**
     * Converts for {@link TransferCallResponse}
     */
    public static TransferCallResponse convert(TransferCallResponseInternal transferCallResponseInternal) {
        return new TransferCallResponse(transferCallResponseInternal.getOperationId(),
            CallingOperationStatusConverter.convert(transferCallResponseInternal.getStatus()),
            transferCallResponseInternal.getOperationContext(),
            CallingOperationResultDetailsConverter.convert(transferCallResponseInternal.getResultDetails()));
    }

    private TransferCallResponseConverter() {

    }
}
