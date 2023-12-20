// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Class to represent the build errors.
 */
public class BuildError {
    @JsonIgnore
    private String message;
    @JsonProperty
    private BuildErrorCode code;
    @JsonProperty
    private BuildErrorLevel level;
    @JsonProperty
    private List<String> additionalDetails;

    /**
     * Creates an instance of {@link BuildError}.
     * @param message The error message.
     * @param code The {@link BuildErrorCode error code}.
     * @param level The {@link BuildErrorLevel error level}.
     */
    public BuildError(String message, BuildErrorCode code, BuildErrorLevel level) {
        this.message = message;
        this.code = code;
        this.level = level;
    }

    /**
     * Creates an instance of {@link BuildError}.
     * @param message The error message.
     * @param code The {@link BuildErrorCode error code}.
     * @param level The {@link BuildErrorLevel error level}.
     * @param additionalDetails Additional details about the error.
     */
    public BuildError(String message, BuildErrorCode code, BuildErrorLevel level, List<String> additionalDetails) {
        this.message = message;
        this.code = code;
        this.level = level;
        this.additionalDetails = additionalDetails;
    }

    /**
     * Returns the error message.
     * @return the error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the error code.
     * @return the error code.
     */
    public BuildErrorCode getCode() {
        return code;
    }

    /**
     * Returns the error severity level.
     * @return the error severity level.
     */
    public BuildErrorLevel getLevel() {
        return level;
    }

    /**
     * Returns additional details about this error.
     * @return additional details about this error.
     */
    public List<String> getAdditionalDetails() {
        return additionalDetails;
    }
}
