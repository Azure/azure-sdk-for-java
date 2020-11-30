// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * An instance of this class provides additional information about an http error response.
 */
@Immutable
public class ManagementError {
    /**
     * Constructs a new {@link ManagementError} object.
     */
    public ManagementError() {
    }

    /**
     * Constructs a new {@link ManagementError} object.
     *
     * @param code the error code.
     * @param message the error message.
     */
    public ManagementError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * The error code parsed from the body of the http error response.
     */
    @JsonProperty(value = "code", access = JsonProperty.Access.WRITE_ONLY)
    private String code;

    /**
     * The error message parsed from the body of the http error response.
     */
    @JsonProperty(value = "message", access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * The target of the error.
     */
    @JsonProperty(value = "target", access = JsonProperty.Access.WRITE_ONLY)
    private String target;

    /**
     * Details for the error.
     */
    @JsonProperty(value = "details", access = JsonProperty.Access.WRITE_ONLY)
    private List<ManagementError> details;

    /**
     * Additional info for the error.
     */
    @JsonProperty(value = "additionalInfo", access = JsonProperty.Access.WRITE_ONLY)
    private List<AdditionalInfo> additionalInfo;

    /**
     * @return the error code parsed from the body of the http error response.
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the target of the error.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return the details for the error.
     */
    public List<? extends ManagementError> getDetails() {
        return details;
    }

    /**
     * @return the additional info for the error.
     */
    public List<AdditionalInfo> getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public String toString() {
        return message == null ? super.toString() : message;
    }
}
