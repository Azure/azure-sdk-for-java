// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A connection string to describe the JDBC connection URL. The JDBC connection
 * string consists of the database type of this JDBC URL, the connection properties
 * such as credential properties, authentication plugins, or connection attributes.
 */
public final class JdbcConnectionString {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcConnectionString.class);

    public static final String INVALID_CONNECTION_STRING_FORMAT = "Invalid connection string: %s";
    public static final String UNSUPPORTED_DATABASE_TYPE_STRING_FORMAT = "The DatabaseType specified in : %s is not "
        + "supported to enhance authentication with Azure AD by Spring Cloud Azure.";
    public static final String INVALID_PROPERTY_PAIR_FORMAT = "Connection string has invalid key value pair: %s";
    private static final String TOKEN_VALUE_SEPARATOR = "=";
    private final String jdbcUrl;
    private final Map<String, String> properties = new HashMap<>();
    private DatabaseType databaseType = null;
    private String baseUrl = null;
    private final List<String> orderedPropertyKeys = new ArrayList<>();

    private JdbcConnectionString(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Resolve the database type, connection properties from the JDBC URL.
     * The supported database types are those in {@link DatabaseType}, if a URL of
     * any other database types is provided, an {@link AzureUnsupportedDatabaseTypeException}
     * will be thrown. If any illegal properties are detected, an {@link IllegalArgumentException}
     * will be thrown.
     */
    private void resolveSegments() {
        if (!StringUtils.hasText(this.jdbcUrl)) {
            LOGGER.warn("'connectionString' doesn't have text.");
            throw new IllegalArgumentException(String.format(INVALID_CONNECTION_STRING_FORMAT, this.jdbcUrl));
        }

        Optional<DatabaseType> optionalDatabaseType = Arrays.stream(DatabaseType.values())
                                                            .filter(databaseType -> this.jdbcUrl.startsWith(databaseType.getSchema() + ":"))
                                                            .findAny();
        this.databaseType = optionalDatabaseType.orElseThrow(() -> new AzureUnsupportedDatabaseTypeException(String.format(UNSUPPORTED_DATABASE_TYPE_STRING_FORMAT, this.jdbcUrl)));

        int pathQueryDelimiterIndex = this.jdbcUrl.indexOf(this.databaseType.getPathQueryDelimiter());

        if (pathQueryDelimiterIndex < 0) {
            this.baseUrl = jdbcUrl;
            return;
        }

        this.baseUrl = this.jdbcUrl.substring(0, pathQueryDelimiterIndex);
        String properties = this.jdbcUrl.substring(pathQueryDelimiterIndex + 1);

        final String[] tokenValuePairs = properties.split(this.databaseType.getQueryDelimiter());

        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPARATOR, 2);
            String key = pair[0];
            if (!StringUtils.hasText(pair[0])) {
                throw new IllegalArgumentException(String.format(INVALID_PROPERTY_PAIR_FORMAT, tokenValuePair));
            }
            if (pair.length < 2) {
                this.properties.put(key, null);
            } else {
                this.properties.put(key, pair[1]);
            }
            this.orderedPropertyKeys.add(key);
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    String getBaseUrl() {
        return baseUrl;
    }

    String getProperty(String key) {
        return properties.get(key);
    }

    Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    List<String> getOrderedPropertyKeys() {
        return Collections.unmodifiableList(this.orderedPropertyKeys);
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
