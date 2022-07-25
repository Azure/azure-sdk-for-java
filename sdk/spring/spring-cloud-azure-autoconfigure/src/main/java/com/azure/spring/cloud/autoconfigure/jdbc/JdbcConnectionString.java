// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.NONE_VALUE;


class JdbcConnectionString {

    static final String INVALID_CONNECTION_STRING_FORMAT = "Invalid connection string: %s";
    static final String INVALID_PROPERTY_PAIR_FORMAT = "Connection string has invalid key value pair: %s";
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcConnectionString.class);
    private static final String TOKEN_VALUE_SEPARATOR = "=";
    private final String jdbcURL;
    private final Map<String, String> properties = new HashMap<>();
    private DatabaseType databaseType = null;

    JdbcConnectionString(String jdbcURL) {
        this.jdbcURL = jdbcURL;
        resolveSegments();
    }

    private void resolveSegments() {
        if (!StringUtils.hasText(this.jdbcURL)) {
            LOGGER.warn("'connectionString' doesn't have text.");
            throw new IllegalArgumentException(String.format(INVALID_CONNECTION_STRING_FORMAT, this.jdbcURL));
        }

        Optional<DatabaseType> optionalDatabaseType = Arrays.stream(DatabaseType.values())
                                                            .filter(databaseType -> this.jdbcURL.startsWith(databaseType.getSchema()))
                                                            .findAny();

            this.databaseType = optionalDatabaseType.orElseThrow(() -> new IllegalArgumentException(String.format(INVALID_CONNECTION_STRING_FORMAT, this.jdbcURL)));

        int pathQueryDelimiterIndex = this.jdbcURL.indexOf(this.databaseType.getPathQueryDelimiter());

        if (pathQueryDelimiterIndex < 0) {
            return;
        }

        String hostInfo = this.jdbcURL.substring(databaseType.getSchema().length() + 3, pathQueryDelimiterIndex);
        String[] hostInfoArray = hostInfo.split(":");
        if (hostInfoArray.length == 2) {
            this.properties.put("servername", hostInfoArray[0]);
            this.properties.put("port", hostInfoArray[1]);
        } else {
            this.properties.put("port", hostInfo);
        }

        String properties = this.jdbcURL.substring(pathQueryDelimiterIndex + 1);

        final String[] tokenValuePairs = properties.split(this.databaseType.getQueryDelimiter());

        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPARATOR, 2);
            String key = pair[0];
            if (!StringUtils.hasText(pair[0])) {
                throw new IllegalArgumentException(String.format(INVALID_PROPERTY_PAIR_FORMAT, tokenValuePair));
            }
            if (pair.length < 2) {
                this.properties.put(key, NONE_VALUE);
            } else {
                this.properties.put(key, pair[1]);
            }
        }
    }

    String enhanceConnectionString(Map<String, String> enhancedProperties) {
        if (enhancedProperties == null || enhancedProperties.isEmpty()) {
            return this.jdbcURL;
        }
        LOGGER.debug("Trying to enhance url for {}", databaseType);

        StringBuilder builder = new StringBuilder(this.jdbcURL);

        if (!this.hasProperties()) {
            builder.append(databaseType.getPathQueryDelimiter());
        } else {
            builder.append(databaseType.getQueryDelimiter());
        }

        for (Map.Entry<String, String> entry : enhancedProperties.entrySet()) {
            String key = entry.getKey(), value = entry.getValue();
            String valueProvidedInConnectionString = this.getProperty(key);

            if (valueProvidedInConnectionString == null) {
                builder.append(key)
                    .append("=")
                    .append(value)
                    .append(databaseType.getQueryDelimiter());
            } else if (!value.equals(valueProvidedInConnectionString)) {
                LOGGER.debug("The property {} is set to another value than default {}", key, value);
                throw new IllegalArgumentException("Inconsistent property detected");
            } else {
                LOGGER.debug("The property {} is already set", key);
            }
        }

        String enhancedUrl = builder.toString();
        return enhancedUrl.substring(0, enhancedUrl.length() - 1);
    }

    public String getProperty(String key) {
        return this.properties.get(key);
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public boolean hasProperties() {
        return !this.properties.isEmpty();
    }

}
