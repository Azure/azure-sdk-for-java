// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.NONE_VALUE;

public final class JdbcConnectionString {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcConnectionString.class);

    public static final String INVALID_CONNECTION_STRING_FORMAT = "Invalid connection string: %s";
    public static final String UNSUPPORTED_DATABASE_TYPE_STRING_FORMAT = "The DatabaseType specified in : %s is not "
        + "supported to enhance authentication with Azure AD by Spring Cloud Azure.";
    public static final String INVALID_PROPERTY_PAIR_FORMAT = "Connection string has invalid key value pair: %s";
    private static final String TOKEN_VALUE_SEPARATOR = "=";
    private final String jdbcURL;
    private final Map<String, String> properties = new HashMap<>();
    private DatabaseType databaseType = null;

    private JdbcConnectionString(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    private void resolveSegments() {
        if (!StringUtils.hasText(this.jdbcURL)) {
            LOGGER.warn("'connectionString' doesn't have text.");
            throw new IllegalArgumentException(String.format(INVALID_CONNECTION_STRING_FORMAT, this.jdbcURL));
        }

        Optional<DatabaseType> optionalDatabaseType = Arrays.stream(DatabaseType.values())
                                                            .filter(databaseType -> this.jdbcURL.startsWith(databaseType.getSchema()))
                                                            .findAny();
        this.databaseType = optionalDatabaseType.orElseThrow(() -> new AzureUnsupportedDatabaseTypeException(String.format(UNSUPPORTED_DATABASE_TYPE_STRING_FORMAT, this.jdbcURL)));

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
            this.properties.put("servername", hostInfo);
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

    public String enhanceConnectionString(Map<String, String> enhancedProperties) {
        if (enhancedProperties == null || enhancedProperties.isEmpty()) {
            return this.jdbcURL;
        }
        LOGGER.debug("Trying to enhance jdbc url for {}", databaseType);

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

    public boolean addAttributeToProperty(String propertyKey, String attributeKey, String attributeValue,
                                       String attributeDelimiter, String attributeKeyValueDelimiter) {
        String attribute = attributeKey + attributeKeyValueDelimiter + attributeValue;
        if (this.properties.containsKey(propertyKey)) {
            String value = this.properties.get(propertyKey);
            String[] attributes = value.split(attributeDelimiter);
            for (String attributePair : attributes) {
                String[] split = attributePair.split(attributeKeyValueDelimiter);
                if (split.length > 1 && attributeKey.equals(split[0])) {
                    LOGGER.debug("The attribute {} in property {} is already set", attributeKey, propertyKey);
                    return false;
                }
            }
            this.properties.put(propertyKey, value + attributeDelimiter + attribute);
        } else {
            this.properties.put(propertyKey, attribute);
        }
        return true;
    }

    public boolean addProperty(String propertyKey, String value) {
        if (this.properties.containsKey(propertyKey)) {
            LOGGER.debug("The property {} is already set", propertyKey);
            return false;
        } else {
            this.properties.put(propertyKey, value);
            return true;
        }
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

    public static JdbcConnectionString resolve(String url) {
        JdbcConnectionString jdbcConnectionString = new JdbcConnectionString(url);
        try {
            jdbcConnectionString.resolveSegments();
        } catch (AzureUnsupportedDatabaseTypeException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }
        return jdbcConnectionString;
    }

}
