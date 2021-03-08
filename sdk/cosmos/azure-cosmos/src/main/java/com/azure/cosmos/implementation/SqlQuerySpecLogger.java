// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlQuerySpecLogger {

    static final String LINE_SEPARATOR = System.getProperty("line.separator");
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
            StringBuilder queryLogBuilder = new StringBuilder(1000);
            queryLogBuilder.append(querySpec.getQueryText());
            querySpec.getParameters().forEach(p -> queryLogBuilder.append(LINE_SEPARATOR)
                .append(" > param: ")
                .append(p.getName())
                .append(" = ")
                .append(p.getValue(Object.class))
            );
            logger.debug(queryLogBuilder.toString());
        } else if (logger.isDebugEnabled()) {
            logger.debug(querySpec.getQueryText());
        }
    }

}
