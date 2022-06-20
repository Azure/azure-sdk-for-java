// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.models.CallingOperationResultDetailsDto;
import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;

/** The CallingOperationResultDetails model. */
@Fluent
@Immutable
public final class CallingOperationResultDetails {
    /*
     * The result code associated with the operation.
     */
    private final int code;

    /*
     * The subcode that further classifies the result.
     */
    private final int subcode;

    /*
     * The message is a detail explanation of subcode.
     */
    private final String message;

    /**
     * Constructor of the class
     *
     * @param callingOperationResultDetailsDto The calling operation result details
     */
    public CallingOperationResultDetails(CallingOperationResultDetailsDto callingOperationResultDetailsDto) {
        this.code = callingOperationResultDetailsDto.getCode();
        this.subcode = callingOperationResultDetailsDto.getSubcode();
        this.message = callingOperationResultDetailsDto.getMessage();
    }

    /**
     * Get the code property: The result code associated with the operation.
     *
     * @return the code value.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Get the subcode property: The subcode that further classifies the result.
     *
     * @return the subcode value.
     */
    public int getSubcode() {
        return this.subcode;
    }

    /**
     * Get the message property: The message is a detail explanation of subcode.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }
}
