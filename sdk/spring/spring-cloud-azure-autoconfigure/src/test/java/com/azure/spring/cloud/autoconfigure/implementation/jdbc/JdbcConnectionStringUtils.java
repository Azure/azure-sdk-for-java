// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class JdbcConnectionStringUtils {

    private static String buildEnhancedPropertiesOrderedString(Map<String, String> enhancedProperties, String queryDelimiter) {
        String enhancedPropertyString = new TreeMap<>(enhancedProperties).entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(queryDelimiter));
        return enhancedPropertyString;
    }

    public static String enhanceJdbcUrl(DatabaseType databaseType, String baseUrl, String... properties) {
        return enhanceJdbcUrl(databaseType, true, baseUrl, properties);
    }

    public static String enhanceJdbcUrl(DatabaseType databaseType, boolean hasOriginalProperties, String baseUrl, String... properties) {
        Map<String, String> enhancedProperties = new HashMap<>(databaseType.getDefaultEnhancedProperties());
        for (String property : properties) {
            String[] split = property.split("=");
            enhancedProperties.put(split[0], split[1]);
        }

        if (enhancedProperties.isEmpty()) {
            return baseUrl;
        }

        return baseUrl
            + (hasOriginalProperties ? databaseType.getQueryDelimiter() : databaseType.getPathQueryDelimiter())
            + buildEnhancedPropertiesOrderedString(enhancedProperties, databaseType.getQueryDelimiter());
    }

}
