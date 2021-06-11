// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

/** Result info class to be used to report result status for actions/operations. */
public final class ResultInfo {
    /*
     * Gets or sets the result code
     * For synchronous failures, this maps one-to-one with HTTP responses. For
     * asynchronous failures or messages, it is contextual.
     */
    private final Integer code;

    /*
     * Gets or sets the result subcode.
     * The subcode further classifies a failure. For example.
     */
    private final Integer subcode;

    /*
     * Gets or sets the message
     * The message is a detail explanation of subcode.
     */
    private final String message;

    /**
     * Get the code property: Gets or sets the result code For synchronous failures, this maps one-to-one with HTTP
     * responses. For asynchronous failures or messages, it is contextual.
     *
     * @return the code value.
     */
    public Integer getCode() {
        return this.code;
    }

    /**
     * Get the subcode property: Gets or sets the result subcode. The subcode further classifies a failure. For example.
     *
     * @return the subcode value.
     */
    public Integer getSubcode() {
        return this.subcode;
    }

    /**
     * Get the message property: Gets or sets the message The message is a detail explanation of subcode.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
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
