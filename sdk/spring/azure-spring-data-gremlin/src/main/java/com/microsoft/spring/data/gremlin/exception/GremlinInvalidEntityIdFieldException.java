// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.exception;

public class GremlinInvalidEntityIdFieldException extends GremlinEntityInformationException {

    public GremlinInvalidEntityIdFieldException(String msg) {
        super(msg);
    }

    public GremlinInvalidEntityIdFieldException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
