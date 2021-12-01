// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.converter;

import org.springframework.core.NestedRuntimeException;

/**
 * The conversion specific {@link NestedRuntimeException}.
 *
 * @author Warren Zhu
 */
public class ConversionException extends NestedRuntimeException {

    /**
     *
     * @param msg The message.
     */
    public ConversionException(String msg) {
        super(msg);
    }

    /**
     *
     * @param msg The message.
     * @param cause The cause of this exception.
     */
    public ConversionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
