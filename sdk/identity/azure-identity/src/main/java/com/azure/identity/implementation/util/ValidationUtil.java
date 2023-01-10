// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for validating parameters.
 */
public final class ValidationUtil {
    public static void validate(String className, ClientLogger logger, String param1Name, Object param1,
        String param2Name, Object param2) {
        String missing = "";

        if (param1 == null) {
            missing += param1Name;
        }

        if (param2 == null) {
            missing += missing.isEmpty() ? param2Name : ", " + param2Name;
        }

        if (!missing.isEmpty()) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Must provide non-null values for "
                +  missing + " properties in " + className));
        }
    }

    public static void validate(String className, ClientLogger logger, String param1Name, Object param1,
        String param2Name, Object param2, String param3Name, Object param3) {
        String missing = "";

        if (param1 == null) {
            missing += param1Name;
        }

        if (param2 == null) {
            missing += missing.isEmpty() ? param2Name : ", " + param2Name;
        }

        if (param3 == null) {
            missing += missing.isEmpty() ? param3Name : ", " + param3Name;
        }

        if (!missing.isEmpty()) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Must provide non-null values for "
                +  missing + " properties in " + className));
        }
    }

    public static void validateAuthHost(String authHost, ClientLogger logger) {
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

    public static void validateTenantIdCharacterRange(String id, ClientLogger logger) {
        if (id != null) {
            for (int i = 0; i < id.length(); i++) {
                if (!isValidTenantCharacter(id.charAt(i))) {
                    throw logger.logExceptionAsError(
                        new IllegalArgumentException(
                            "Invalid tenant id provided. You can locate your tenant id by following the instructions"
                                + " listed here: https://docs.microsoft.com/partner-center/find-ids-and-domain-names"));
                }
            }
        }
    }

    public static void validateInteractiveBrowserRedirectUrlSetup(Integer port, String redirectUrl,
        ClientLogger logger) {
        if (port != null && redirectUrl != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Port and Redirect URL cannot be configured at the same time. "
                                                 + "Port is deprecated now. Use the redirectUrl setter to specify"
                                                 + " the redirect URL on the builder."));
        }
    }

    private static boolean isValidTenantCharacter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '.') || (c == '-');
    }
}
