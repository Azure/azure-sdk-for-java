// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CancelAllMediaOperationsResultInternal;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResult;

/**
 * A converter between {@link CancelAllMediaOperationsResultInternal} and {@link CancelAllMediaOperationsResult}.
 */
public class CancelAllMediaOperationsResultConverter {

    /**
     * Maps from {@link CancelAllMediaOperationsResultInternal} to {@link CancelAllMediaOperationsResult}.
     */
    public static CancelAllMediaOperationsResult convert(CancelAllMediaOperationsResultInternal resultInternal) {
        if (resultInternal == null) {
            return null;
        }

        return new CancelAllMediaOperationsResult(
            resultInternal.getOperationId(),
            resultInternal.getStatus(),
            resultInternal.getOperationContext(),
            ResultInfoConverter.convert(resultInternal.getResultInfo()));
    }
}

