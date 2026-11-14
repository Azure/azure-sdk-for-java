// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.guava25.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Lease configuration identity without persisting customer SQL.
 */
public final class ChangeFeedQueryFilter {
    public static final String PROPERTY_NAME = "queryFilterHash";

    private ChangeFeedQueryFilter() {
    }

    public static String fingerprint(String query) {
        return Strings.isNullOrEmpty(query) ? null
            : Hashing.sha256().hashString(query, StandardCharsets.UTF_8).toString();
    }

    public static void validate(String expected, String actual) {
        if (!Objects.equals(expected, actual)) {
            throw new QueryFilterMismatchException();
        }
    }

    public static Map<String, String> withFingerprint(Map<String, String> properties, String fingerprint) {
        if (properties != null && properties.containsKey(PROPERTY_NAME)) {
            validate(fingerprint, properties.get(PROPERTY_NAME));
        }
        if (fingerprint == null) {
            return properties;
        }
        Map<String, String> result = properties == null ? new HashMap<>() : new HashMap<>(properties);
        result.put(PROPERTY_NAME, fingerprint);
        return result;
    }

    public static void validateProperties(String expected, Map<String, String> properties) {
        validate(expected, properties == null ? null : properties.get(PROPERTY_NAME));
    }

    public static final class QueryFilterMismatchException extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        public QueryFilterMismatchException() {
            super("The change feed query does not match the existing lease configuration. "
                + "Build a new processor with a new lease prefix or lease container to change the query.");
        }
    }
}
