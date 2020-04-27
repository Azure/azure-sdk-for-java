// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for validating parameters.
 */
public final class ValidationUtil {
    public static void validate(String className, Map<String, Object> parameters) {
        ClientLogger logger = new ClientLogger(className);
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() == null) {
                missing.add(entry.getKey());
            }
        }
        if (missing.size() > 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Must provide non-null values for "
                + String.join(", ", missing) + " properties in " + className));
        }
    }

    public static void validateAuthHost(String className, String authHost) {
        ClientLogger logger = new ClientLogger(className);
        try {
            URI authUri = new URI(authHost);
        } catch (URISyntaxException e) {
            throw logger.logExceptionAsWarning(
                new IllegalArgumentException("Must provide a valid URI for authority host."));
        }
        if (!authHost.startsWith("https")) {
            throw logger.logExceptionAsWarning(
                new IllegalArgumentException("Authority host must use https scheme."));
        }
    }

    public static void validateCredentialId(String className, String id, String idName) {
        ClientLogger logger = new ClientLogger(className);
        if(!id.matches("^(?:[0-9]|[a-z]|-)+$")) {
            throw logger.logExceptionAsWarning(
                new IllegalArgumentException(
                    String.format("%s must have characters in the range of [0-9], [a-z], '-'")));
        }
    }
}
