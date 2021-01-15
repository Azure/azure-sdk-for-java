// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.exception;

import org.springframework.dao.DataAccessResourceFailureException;

public class GremlinQueryException extends DataAccessResourceFailureException {

    public GremlinQueryException(String msg) {
        super(msg);
    }

    public GremlinQueryException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
