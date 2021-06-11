// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CancelAllMediaOperationsResultInternal;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResult;

public class CancelAllMediaOperationsResultConverter {
    public static CancelAllMediaOperationsResult convert(CancelAllMediaOperationsResultInternal cancelAllMediaOperationsResultInternal) {
        return new CancelAllMediaOperationsResult(
            cancelAllMediaOperationsResultInternal.getId(),
            cancelAllMediaOperationsResultInternal.getStatus(),
            cancelAllMediaOperationsResultInternal.getOperationContext(),
            ResultInfoConverter.convert(cancelAllMediaOperationsResultInternal.getResultInfo()));
    }
}

