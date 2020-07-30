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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for validating parameters.
 */
public final class ValidationUtil {
    private static Pattern identifierPattern = Pattern.compile("^(?:[0-9]|[a-z]|-)+$");


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
        HashMap<String, Object> parameter = new HashMap<>(1);
        parameter.put("Authority Host", authHost);
        validate(className, parameter);
        try {
            URI authUri = new URI(authHost);
        } catch (URISyntaxException e) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Must provide a valid URI for authority host.", e));
        }
        if (!authHost.startsWith("https")) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Authority host must use https scheme."));
        }
    }

    public static void validateCredentialId(String className, String id, String idName) {
        ClientLogger logger = new ClientLogger(className);
        HashMap<String, Object> parameter = new HashMap<>(1);
        parameter.put(idName, id);
        validate(className, parameter);
        if (!identifierPattern.matcher(id).matches()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(
                    String.format("%s must have characters in the range of [0-9], [a-z], '-'", idName)));
        }
    }

    public static void validateFilePath(String className, String filePath, String pathName) {
        ClientLogger logger = new ClientLogger(className);
        File file = new File(filePath);
        HashMap<String, Object> parameter = new HashMap<>(1);
        parameter.put(pathName, filePath);
        validate(className, parameter);
        if (!file.isAbsolute()) {
            Path absolutePath = Paths.get(file.getAbsolutePath());
            Path normalizedPath = absolutePath.normalize();
            if (!absolutePath.equals(normalizedPath)) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException(
                        String.format("%s is not valid. The path contains invalid characters `.` or `..`", pathName)));
            }
        }
    }
}
