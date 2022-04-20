// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

/**
 * Test
 */
public class DynamicFeatureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of the DynamicFeatureException
     *
     * @param message the error message.
     * @param cause test
     */
    public DynamicFeatureException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance of the DynamicFeatureException
     *
     * @param message the error message.
     */
    public DynamicFeatureException(String message) {
        super(message);
    }

}
