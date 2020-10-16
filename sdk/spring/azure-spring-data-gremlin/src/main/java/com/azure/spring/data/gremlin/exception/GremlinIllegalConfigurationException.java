// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.exception;

import org.springframework.dao.InvalidDataAccessApiUsageException;

public class GremlinIllegalConfigurationException extends InvalidDataAccessApiUsageException {

    public GremlinIllegalConfigurationException(String msg) {
        super(msg);
    }

    public GremlinIllegalConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
