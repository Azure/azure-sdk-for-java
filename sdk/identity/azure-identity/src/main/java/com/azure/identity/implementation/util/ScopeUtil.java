// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utilities to convert between scopes and resources for connecting to Azure Active Directory.
 */
public final class ScopeUtil {

    private static final String DEFAULT_SUFFIX = "/.default";
    private static final Pattern SCOPE_PATTERN = Pattern.compile("^[0-9a-zA-Z-.:/]+$");

    /**
     * Convert a list of scopes to a resource for Azure Active Directory.
     * @param scopes the list of scopes to authenticate to
     * @return the resource to authenticate with Azure Active Directory.
     * @throws IllegalArgumentException if scopes is empty or has more than 1 items
     */
    public static String scopesToResource(List<String> scopes) {
        Objects.requireNonNull(scopes);
        if (scopes.size() != 1) {
            throw new IllegalArgumentException(
                "To convert to a resource string the specified array must be exactly length 1");
        }

        if (!scopes.get(0).endsWith(DEFAULT_SUFFIX)) {
            return scopes.get(0);
        }

        return scopes.get(0).substring(0, scopes.get(0).lastIndexOf(DEFAULT_SUFFIX));
    }

    /**
     * Convert a resource to a list of scopes.
     * @param resource the resource for Azure Active Directory
     * @return the list of scopes
     */
    public static String[] resourceToScopes(String resource) {
        Objects.requireNonNull(resource);
        return new String[] { resource + DEFAULT_SUFFIX };
    }

    /**
     * Validate the format of the input scope.
     * @param scope the scope input
     * @throws IllegalArgumentException if a scope with invalid format is provided.
     */
    public static void validateScope(String scope) {
        boolean isScopeMatch = SCOPE_PATTERN.matcher(scope).matches();

        if (!isScopeMatch) {
            throw new IllegalArgumentException("The specified scope is not in expected format."
                                               + " Only alphanumeric characters, '.', '-', ':', and '/' are allowed");
        }
    }

    private ScopeUtil() {
    }
}
