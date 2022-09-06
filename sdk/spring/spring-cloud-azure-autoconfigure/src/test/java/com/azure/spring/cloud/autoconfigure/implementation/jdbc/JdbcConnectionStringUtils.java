// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.mysql.cj.conf.PropertyKey;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class JdbcConnectionStringUtils {

    public static String buildEnhancedPropertiesOrderedString(Map<String, String> enhancedProperties, String queryDelimiter) {
        String enhancedPropertyString = new TreeMap<>(enhancedProperties).entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(queryDelimiter));
        return enhancedPropertyString;
    }

    public static String enhanceMySqlJdbcUrl(String baseUrl, String... properties) {
        Map<String, String> enhancedProperties = new HashMap<>(DatabaseType.MYSQL.getDefaultEnhancedProperties());
        enhancedProperties.put(PropertyKey.connectionAttributes.getKeyName(), "_extension_version:" + AzureSpringIdentifier.AZURE_SPRING_MYSQL_OAUTH);
        for (String property : properties) {
            String[] split = property.split("=");
            enhancedProperties.put(split[0], split[1]);
        }
        return baseUrl
            + DatabaseType.MYSQL.getQueryDelimiter()
            + buildEnhancedPropertiesOrderedString(enhancedProperties, DatabaseType.MYSQL.getQueryDelimiter());
    }

}
