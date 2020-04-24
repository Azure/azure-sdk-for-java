// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class provides additional information about an http error response.
 */
@Immutable
public class CloudError {
    /**
     * The error code parsed from the body of the http error response.
     */
    private final String code;

    /**
     * The error message parsed from the body of the http error response.
     */
    private final String message;

    /**
     * The target of the error.
     */
    private final String target;

    /**
     * Details for the error.
     */
    private final List<CloudError> details;

    /**
     * Additional info for the error.
     */
    private final List<CloudErrorAdditionalInfo> additionalInfo;

    /**
     * Constructs a new {@link CloudError} object.
     *
     * @param code the error code parsed from the body of the http error response.
     * @param message the error message.
     * @param target the target of the error.
     * @param details the details for the error.
     * @param additionalInfo the additional info for the error.
     */
    @JsonCreator
    public CloudError(@JsonProperty("code") String code, @JsonProperty("message") String message,
                      @JsonProperty("target") String target,
                      @JsonProperty("details") List<CloudError> details,
                      @JsonProperty("additionalInfo") List<CloudErrorAdditionalInfo> additionalInfo) {
        this.code = code;
        this.message = message;
        this.target = target;
        this.details = details == null ? new ArrayList<>() : details;
        this.additionalInfo = additionalInfo == null ? new ArrayList<>() : additionalInfo;
    }

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
    public List<CloudError> getDetails() {
        return details;
    }

    /**
     * @return the additional info for the error.
     */
    public List<CloudErrorAdditionalInfo> getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public String toString() {
        return message == null ? super.toString() : message;
    }
}
