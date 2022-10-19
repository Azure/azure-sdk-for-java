// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.converter;

import org.springframework.core.NestedRuntimeException;

/**
 * The conversion specific {@link NestedRuntimeException}.
 *
 */
public final class ConversionException extends NestedRuntimeException {

    /**
     * Construct a {@code ConversionException} with the specified detail message.
     * @param msg the specified detail message.
     */
    public ConversionException(String msg) {
        super(msg);
    }

    /**
     * Construct a {@code NestedRuntimeException} with the specified detail message and nested exception.
     * @param msg the specified detail message.
     * @param cause the nested exception.
     */
    public ConversionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
