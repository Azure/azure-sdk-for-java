// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

/**
 * Error thrown when an issue is found while generating a Feature Variant.
 */
public final class DynamicFeatureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of the DynamicFeatureException
     *
     * @param message the error message.
     * @param cause original issue caught
     */
    DynamicFeatureException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance of the DynamicFeatureException
     *
     * @param message the error message.
     */
    DynamicFeatureException(String message) {
        super(message);
    }

}
