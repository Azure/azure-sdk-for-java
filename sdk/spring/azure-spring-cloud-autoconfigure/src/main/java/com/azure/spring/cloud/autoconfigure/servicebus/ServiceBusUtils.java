// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for Service Bus.
 */
public class ServiceBusUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusUtils.class);
    private static final String CONNECTION_STRING_PREFIX = "Endpoint=sb://";

    public static String getNamespace(String connectionString) {
        try {
            int start = connectionString.indexOf(CONNECTION_STRING_PREFIX) + CONNECTION_STRING_PREFIX.length();
            return connectionString.substring(start, connectionString.indexOf('.', start));
        } catch (Throwable e) {
            LOGGER.error("Fail to parse namespace from connection string", e);
            return null;
        }
    }

}
