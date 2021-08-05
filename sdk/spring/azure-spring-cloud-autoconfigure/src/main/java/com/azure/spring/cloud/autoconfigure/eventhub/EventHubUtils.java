// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for Event Hub.
 */
public class EventHubUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubUtils.class);

    public static String getNamespace(String connectionString) {
        try {
            String prefix = "Endpoint=sb://";
            int start = connectionString.indexOf(prefix) + prefix.length();
            return connectionString.substring(start, connectionString.indexOf('.', start));
        } catch (Throwable e) {
            LOGGER.error("Fail to parse namespace from connection string", e);
            return null;
        }
    }

}
