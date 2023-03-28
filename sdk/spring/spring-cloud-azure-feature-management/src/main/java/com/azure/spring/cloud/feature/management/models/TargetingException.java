// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

/**
 * This class defines a custom exception type for when an expected Filter is not found when checking if a Feature is
 * enabled. A FilterNotFoundException is only thrown when failfast is enabled, which is true by default.
 */
public final class TargetingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * TargetingException for the Targeting Filter.
     * @param message Exception error message
     */
    public TargetingException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of the FilterNotFoundException
     *
     * @param message the error message.
     * @param cause the original error thrown, typically of NoSuchBeanDefinitionException type.
     */
    public TargetingException(String message, Throwable cause) {
        super(message, cause);
    }

}
