// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.converter;

import org.springframework.core.NestedRuntimeException;

/**
 * The conversion specific {@link NestedRuntimeException}.
 *
 * @author Warren Zhu
 */
public class ConversionException extends NestedRuntimeException {

    public ConversionException(String msg) {
        super(msg);
    }

    public ConversionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
