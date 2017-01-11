/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class provides additional information about an http error response.
 */
public class CloudError {
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
    private List<CloudError> details;

    /**
     * Initializes a new instance of CloudError.
     */
    public CloudError() {
        this.details = new ArrayList<CloudError>();
    }

    /**
     * Gets the error code parsed from the body of the http error response.
     *
     * @return the error code.
     */
    public String code() {
        return code;
    }

    /**
     * Sets the error code parsed from the body of the http error response.
     *
     * @param code the error code.
     */
    public CloudError withCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Gets the error message parsed from the body of the http error response.
     *
     * @return the error message.
     */
    public String message() {
        return message;
    }

    /**
     * Sets the error message parsed from the body of the http error response.
     *
     * @param message the error message.
     */
    public CloudError withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Gets the target of the error.
     *
     * @return the target of the error.
     */
    public String target() {
        return target;
    }

    /**
     * Sets the target of the error.
     *
     * @param target the target of the error.
     */
    public CloudError withTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Gets the details for the error.
     *
     * @return the details for the error.
     */
    public List<CloudError> details() {
        return details;
    }
}
