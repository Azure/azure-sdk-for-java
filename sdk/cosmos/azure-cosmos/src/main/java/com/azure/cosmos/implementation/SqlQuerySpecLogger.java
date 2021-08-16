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

    private static String toPrettyString(SqlQuerySpec query) {
        StringBuilder sb = new StringBuilder(1000);
        sb.append(query.getQueryText());
        query.getParameters().forEach(p -> sb.append(LINE_SEPARATOR)
                                            .append(" > param: ")
                                            .append(p.getName())
                                            .append(" = ")
                                            .append(p.getValue(Object.class))
        );

        return sb.toString();
    }

    public void logQuery(SqlQuerySpec querySpec) {
        if (logger.isTraceEnabled() && !querySpec.getParameters().isEmpty()) {
            logger.debug(toPrettyString(querySpec));
        } else if (logger.isDebugEnabled()) {
            logger.debug(querySpec.getQueryText());
        }
    }
}
