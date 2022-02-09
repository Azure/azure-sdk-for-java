// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** Result info class to be used to report result status for actions/operations. */
@Immutable
public final class ResultInfo {
    /*
     * The result code
     * For synchronous failures, this maps one-to-one with HTTP responses. For
     * asynchronous failures or messages, it is contextual.
     */
    private final Integer code;

    /*
     * The result subcode.
     * The subcode further classifies a failure. For example.
     */
    private final Integer subcode;

    /*
     * The message
     * The message is a detail explanation of subcode.
     */
    private final String message;

    /**
     * Get the code property: Gets the result code For synchronous failures, this maps one-to-one with HTTP
     * responses. For asynchronous failures or messages, it is contextual.
     *
     * @return the code value.
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Get the subcode property: Gets the result subcode. The subcode further classifies a failure.
     *
     * @return the subcode value.
     */
    public Integer getSubcode() {
        return subcode;
    }

    /**
     * Get the message property: Gets the message The message is a detail explanation of subcode.
     *
     * @return the message value.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Initializes a new instance of ResultInfo.
     *
     * @param code the code value.
     * @param subcode the subcode value.
     * @param message the message value.
     */
    public ResultInfo(Integer code, Integer subcode, String message) {
        this.code = code;
        this.subcode = subcode;
        this.message = message;
    }
}
