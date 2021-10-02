// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.connectionstring.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.spring.core.connectionstring.ConnectionStringSegments.ENDPOINT;


class ConnectionString {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionString.class);

    private static final String TOKEN_VALUE_PAIR_DELIMITER = ";";
    private static final String TOKEN_VALUE_SEPARATOR = "=";
    private static final Map<ConnectionStringType, List<Set<String>>> TOKENS = initTokensMap();

    private final String str;
    private final ConnectionStringType type;
    private URI endpoint = null;
    private final Map<String, String> segments = new HashMap<>();

    ConnectionString(String str, @NonNull ConnectionStringType type) {
        this.str = str;
        this.type = type;
        resolveSegments();
    }

    private void resolveSegments() {
        if (!StringUtils.hasText(this.str)) {
            LOGGER.warn("'connectionString' doesn't have text.");
            return;
        }
        
        final String[] tokenValuePairs = this.str.split(TOKEN_VALUE_PAIR_DELIMITER);

        validateTokenValuePresents(tokenValuePairs);
        validateTokensAreLegal(collectTokens(tokenValuePairs));

        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPARATOR, 2);
            String token = pair[0], value = pair[1];

            if (ENDPOINT.equalsIgnoreCase(token)) {
                try {
                    this.endpoint = new URI(value);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(
                        String.format(Locale.US, "Invalid endpoint: %s", tokenValuePair), e);
                }
            }
            segments.put(token, value);
        }
    }

    public URI getEndpointUri() {
        return this.endpoint;
    }

    public String getSegment(String key) {
        return this.segments.get(key);
    }

    private static Map<ConnectionStringType, List<Set<String>>> initTokensMap() {
        final Map<ConnectionStringType, List<Set<String>>> tokensMap = new HashMap<>();
        for (ConnectionStringType type : ConnectionStringType.values()) {
            final List<Set<String>> tokensList = new ArrayList<>();

            for (String schema : type.getSchemas()) {
                tokensList.add(collectTokens(schema));
            }
            tokensMap.put(type, tokensList);
        }
        return tokensMap;
    }

    private void validateTokenValuePresents(String[] tokenValuePairs) {
        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPARATOR, 2);
            if (pair.length != 2 || !StringUtils.hasText(pair[1])) {
                throw new IllegalArgumentException(String.format(Locale.US,
                    "Connection string has invalid key value pair: %s", tokenValuePair));
            }
        }
    }

    private void validateTokensAreLegal(Set<String> tokens) {
        boolean isValid = false;

        List<Set<String>> validTokensList = TOKENS.get(this.type);
        validTokensList.sort((a, b) -> Integer.compare(b.size(), a.size()));

        for (Set<String> validTokens : validTokensList) {
            if (tokens.containsAll(validTokens)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Connection string '%s' is invalid, valid schemas are %s", this.str, this.type));
        }
    }

    private static Set<String> collectTokens(String str) {
        String[] tokenValuePairs = str.split(TOKEN_VALUE_PAIR_DELIMITER);
        return collectTokens(tokenValuePairs);
    }
    
    private static Set<String> collectTokens(String[] tokenValuePairs) {
        return Arrays.stream(tokenValuePairs)
                     .map(a -> a.split(TOKEN_VALUE_SEPARATOR, 2)[0])
                     .collect(Collectors.toSet());
    }

}
