// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlQuerySpecLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlQuerySpecLogger.class);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final SqlQuerySpecLogger INSTANCE = new SqlQuerySpecLogger();

    public static SqlQuerySpecLogger getInstance() {
        return INSTANCE;
    }

    public void logQuery(SqlQuerySpec querySpec) {
        if (LOGGER.isTraceEnabled() && querySpec.getParameters().size() > 0) {
            StringBuilder queryLogBuilder = new StringBuilder(1000);
            queryLogBuilder.append(querySpec.getQueryText());
            querySpec.getParameters().forEach(p -> queryLogBuilder.append(LINE_SEPARATOR)
                .append(" > param: ")
                .append(p.getName())
                .append(" = ")
                .append(p.getValue(Object.class))
            );
            LOGGER.debug(queryLogBuilder.toString());
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(querySpec.getQueryText());
        }
    }

}
