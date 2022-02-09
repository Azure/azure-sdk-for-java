// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.exceptions;

import javax.annotation.Nonnull;


/**
 * Typed Exception thrown by a Data Store Accessor.
 *
 * Specific Data Stores extend this Exception class, and add the data store specific error details
 * to that extended exception class
 */
public class AccessorException extends Exception {
    public AccessorException(@Nonnull final String message, @Nonnull final Throwable e) {
        super(message, e);
    }
}
