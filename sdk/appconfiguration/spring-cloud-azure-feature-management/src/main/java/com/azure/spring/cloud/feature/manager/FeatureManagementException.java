// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

/**
 * This class defines a custom exception type for when an expected Filter is not found when checking if a Feature is
 * enabled. A FilterNotFoundException is only thrown when failfast is enabled, which is true by default.
 */
public final class FeatureManagementException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * The error message of what caused the Feature Management Exception
     */
    private final String message;
    
    /**
     * Creates a new instance of the FilterNotFoundException
     *
     * @param message the error message.
     */
    FeatureManagementException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * Creates a new instance of the FilterNotFoundException
     *
     * @param message the error message.
     * @param cause   the original error thrown, typically of NoSuchBeanDefinitionException type.
     */
    FeatureManagementException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}
