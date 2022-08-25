// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import com.azure.core.annotation.Fluent;
import java.util.List;

/** The Rooms Services error. */
@Fluent
public final class RoomsError {

    private final String code;

    private final String message;

    private final String target;

    private final List<RoomsError> details;

    private final RoomsError innerError;

    /**
     * Constructs a new RoomsError
     * @param message The message of the original error
     * @param code The error code
     * @param target The target of the error
     * @param details Additional details
     * @param innerError The inner error
     */
    public RoomsError(String message, String code, String target, List<RoomsError> details, RoomsError innerError) {
        this.message = message;
        this.code = code;
        this.target = target;
        this.details = details;
        this.innerError = innerError;
    }

    /**
     * Get the code property: The error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the target property: The error target.
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the details property: Further details about specific errors that led to this error.
     *
     * @return the details value.
     */
    public List<RoomsError> getDetails() {
        return this.details;
    }

    /**
     * Get the innerError property: The inner error if any.
     *
     * @return the innerError value.
     */
    public RoomsError getInnerError() {
        return this.innerError;
    }

}
