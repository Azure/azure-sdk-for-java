// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.ResultInfoInternal;
import com.azure.communication.callingserver.models.ResultInfo;

/**
 * A converter between {@link ResultInfoInternal} and {@link ResultInfo}.
 */
public final class ResultInfoConverter {

    /**
     * Maps from {@link ResultInfoInternal} to {@link ResultInfo}.
     */
    public static ResultInfo convert(ResultInfoInternal resultInfoInternal) {
        if (resultInfoInternal == null) {
            return null;
        }

        return new ResultInfo(
            resultInfoInternal.getCode(),
            resultInfoInternal.getSubcode(),
            resultInfoInternal.getMessage());
    }
}
