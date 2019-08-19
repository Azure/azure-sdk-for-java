// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class provides additional information about an http error response.
 */
public final class CloudError {
    /**
     * The error code parsed from the body of the http error response.
     */
    private String code;

    /**
     * The error message parsed from the body of the http error response.
     */
    private String message;

    /**
     * The target of the error.
     */
    private String target;

    /**
     * Details for the error.
     */
    private final List<CloudError> details;

    /**
     * Initializes a new instance of CloudError.
     */
    public CloudError() {
        this.details = new ArrayList<CloudError>();
    }

    /**
     * @return the error code parsed from the body of the http error response
     */
    public String code() {
        return code;
    }

    /**
     * Sets the error code parsed from the body of the http error response.
     *
     * @param code the error code
     * @return the CloudError object itself
     */
    public CloudError withCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * @return the error message
     */
    public String message() {
        return message;
    }

    /**
     * Sets the error message parsed from the body of the http error response.
     *
     * @param message the error message
     * @return the CloudError object itself
     */
    public CloudError withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * @return the target of the error
     */
    public String target() {
        return target;
    }

    /**
     * Sets the target of the error.
     *
     * @param target the target of the error
     * @return the CloudError object itself
     */
    public CloudError withTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * @return the details for the error
     */
    public List<CloudError> details() {
        return details;
    }
}
