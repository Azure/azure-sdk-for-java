// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

/**
 * Util class for Event Hub.
 */
public class EventHubUtils {

    public static String getNamespace(String connectionString) {
        String prefix = "Endpoint=sb://";
        int start = connectionString.indexOf(prefix) + prefix.length();
        return connectionString.substring(start, connectionString.indexOf('.', start));
    }

}
