// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlQuerySpecLogger {

    private static final SqlQuerySpecLogger INSTANCE = new SqlQuerySpecLogger(LoggerFactory.getLogger(SqlQuerySpecLogger.class));

    public static SqlQuerySpecLogger getInstance() {
        return INSTANCE;
    }

    private final Logger logger;

    SqlQuerySpecLogger(Logger logger) {
        this.logger = logger;
    }

    public void logQuery(SqlQuerySpec querySpec) {
        if (logger.isTraceEnabled() && !querySpec.getParameters().isEmpty()) {
            logger.debug(querySpec.toPrettyString());
        } else if (logger.isDebugEnabled()) {
            logger.debug(querySpec.getQueryText());
        }
    }
}
