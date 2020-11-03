// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.exception;

import org.springframework.dao.TypeMismatchDataAccessException;

public class GremlinUnexpectedSourceTypeException extends TypeMismatchDataAccessException {

    public GremlinUnexpectedSourceTypeException(String msg) {
        super(msg);
    }

    public GremlinUnexpectedSourceTypeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
