// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.ResultInfoInternal;
import com.azure.communication.callingserver.models.ResultInfo;

public final class ResultInfoConverter {
    public static ResultInfo convert(ResultInfoInternal resultInfoInternal) {
        return resultInfoInternal == null ? null
            : new ResultInfo(
                resultInfoInternal.getCode(),
                resultInfoInternal.getSubcode(),
                resultInfoInternal.getMessage());
    }
}
