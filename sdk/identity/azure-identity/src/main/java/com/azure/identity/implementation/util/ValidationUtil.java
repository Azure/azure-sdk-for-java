// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.util.logging.ClientLogger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for validating parameters.
 */
public final class ValidationUtil {
    private static Pattern clientIdentifierCharPattern = Pattern.compile("^(?:[A-Z]|[0-9]|[a-z]|-)+$");
    private static Pattern tenantIdentifierCharPattern = Pattern.compile("^(?:[A-Z]|[0-9]|[a-z]|-|.)+$");

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
            new URI(authHost);
        } catch (URISyntaxException e) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Must provide a valid URI for authority host.", e));
        }
        if (!authHost.startsWith("https")) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Authority host must use https scheme."));
        }
    }

    public static void validateClientIdCharacterRange(String className, String id) {
        ClientLogger logger = new ClientLogger(className);
        if (id != null) {
            if (!clientIdentifierCharPattern.matcher(id).matches()) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException(
                        "Client id must have characters in the range of [A-Z], [0-9], [a-z], '-'"));
            }
        }
    }

    public static void validateTenantIdCharacterRange(String className, String id) {
        ClientLogger logger = new ClientLogger(className);
        if (id != null) {
            if (!tenantIdentifierCharPattern.matcher(id).matches()) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException(
                        "Tenant id must have characters in the range of [A-Z], [0-9], [a-z], '-', '.'"));
            }
        }
    }

    public static void validateFilePath(String className, String filePath, String pathName) {
        ClientLogger logger = new ClientLogger(className);
        if (filePath != null) {
            File file = new File(filePath);
            if (!file.isAbsolute()) {
                Path absolutePath = Paths.get(file.getAbsolutePath());
                Path normalizedPath = absolutePath.normalize();
                if (!absolutePath.equals(normalizedPath)) {
                    throw logger.logExceptionAsError(
                        new IllegalArgumentException(
                            String.format(
                                "%s is not valid. The path contains invalid characters `.` or `..`", pathName)));
                }
            }
        }
    }

    public static void validateInteractiveBrowserRedirectUrlSetup(String className, Integer port, String redirecrUrl) {
        ClientLogger logger = new ClientLogger(className);
        if (port != null && redirecrUrl != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Port and Redirect URL cannot be configured at the same time. "
                                                 + "Port is deprecated now. Use the redirectUrl setter to specify"
                                                 + " the redirect URL on the builder."));
        }
    }
}
