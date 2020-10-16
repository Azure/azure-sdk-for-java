// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.exception;

import org.springframework.dao.TypeMismatchDataAccessException;

public class GremlinEntityInformationException extends TypeMismatchDataAccessException {

    public GremlinEntityInformationException(String msg) {
        super(msg);
    }

    public GremlinEntityInformationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
