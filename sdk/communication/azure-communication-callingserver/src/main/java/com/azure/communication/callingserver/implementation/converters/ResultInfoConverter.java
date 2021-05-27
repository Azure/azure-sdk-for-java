// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.models.ResultInfo;

/**
 * A converter between {@link com.azure.communication.callingserver.implementation.models.ResultInfoInternal} and
 * {@link ResultInfo}.
 */
public final class ResultInfoConverter {
    /**
     * Maps from {com.azure.communication.callingserver.implementation.models.ResultInfoInternal} to {@link ResultInfo}.
     */
    public static ResultInfo convert(com.azure.communication.callingserver.implementation.models.ResultInfoInternal obj) {
        if (obj == null) {
            return null;
        }

        ResultInfo resultInfo = new ResultInfo()
            .setCode(obj.getCode())
            .setSubcode(obj.getSubcode())
            .setMessage(obj.getMessage());

        return resultInfo;
    }

    private ResultInfoConverter() {
    }
}
