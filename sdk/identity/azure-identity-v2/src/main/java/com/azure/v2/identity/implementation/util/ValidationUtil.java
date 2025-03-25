// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.util;

import com.azure.v2.identity.implementation.models.ManagedIdentityClientOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Utility class for validating parameters.
 */
public final class ValidationUtil {

    public static void validate(String className, ClientLogger logger, List<String> names, List<String> values) {
        String missing = "";

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == null) {
                missing += missing.isEmpty() ? names.get(i) : ", " + names.get(i);
            }
        }

        if (!missing.isEmpty()) {
            throw logger.logThrowableAsWarning(new IllegalArgumentException(
                "Must provide non-null values for " + missing + " properties in " + className));
        }
    }

    public static void validate(ClientLogger logger, String param1Name, Object param1, String param2Name,
        Object param2) {
        String missing = "";

        if (param1 == null) {
            missing += param1Name;
        }

        if (param2 == null) {
            missing += missing.isEmpty() ? param2Name : ", " + param2Name;
        }

        if (!missing.isEmpty()) {
            throw logger.logThrowableAsWarning(
                new IllegalArgumentException("Must provide non-null values for " + missing + " properties."));
        }
    }

    public static void validate(ClientLogger logger, String param1Name, Object param1, String param2Name, Object param2,
        String param3Name, Object param3) {
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
            throw logger.logThrowableAsWarning(
                new IllegalArgumentException("Must provide non-null values for " + missing + " properties."));
        }
    }

    public static void validateAuthHost(String authHost, ClientLogger logger) {
        try {
            new URI(authHost);
        } catch (URISyntaxException e) {
            throw logger
                .logThrowableAsError(new IllegalArgumentException("Must provide a valid URI for authority host.", e));
        }
        if (!authHost.startsWith("https")) {
            throw logger.logThrowableAsError(new IllegalArgumentException("Authority host must use https scheme."));
        }
    }

    public static void validateTenantIdCharacterRange(String id, ClientLogger logger) {
        if (id != null) {
            for (int i = 0; i < id.length(); i++) {
                if (!isValidTenantCharacter(id.charAt(i))) {
                    throw logger.logThrowableAsError(new IllegalArgumentException(
                        "Invalid tenant id provided. You can locate your tenant id by following the instructions"
                            + " listed here: https://learn.microsoft.com/partner-center/find-ids-and-domain-names"));
                }
            }
        }
    }

    private static boolean isValidTenantCharacter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '.') || (c == '-');
    }

    public static void validateManagedIdentityIdParams(ManagedIdentityClientOptions miClientOptions,
        ClientLogger logger) {

        String clientId = miClientOptions.getClientId();
        String objectId = miClientOptions.getObjectId();
        String resourceId = miClientOptions.getResourceId();

        int nonNullIdCount = 0;

        if (clientId != null) {
            nonNullIdCount++;
        }
        if (resourceId != null) {
            nonNullIdCount++;
        }
        if (objectId != null) {
            nonNullIdCount++;
        }

        if (nonNullIdCount > 1) {
            throw logger.logThrowableAsError(
                new IllegalStateException("Only one of clientId, resourceId, or objectId can be specified."));
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
            throw logger.logThrowableAsWarning(new IllegalArgumentException(
                "Must provide non-null values for " + missing + " properties in " + className));
        }
    }

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
            throw logger.logThrowableAsWarning(new IllegalArgumentException(
                "Must provide non-null values for " + missing + " properties in " + className));
        }
    }

    private static boolean isValidSubscriptionCharacter(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || (c >= '0' && c <= '9')
            || (c == '.')
            || (c == '-')
            || (c == '_')
            || (c == ' ');
    }

    public static void validateSubscriptionCharacterRange(String subscription, ClientLogger logger) {
        if (subscription != null) {
            for (int i = 0; i < subscription.length(); i++) {
                if (!isValidSubscriptionCharacter(subscription.charAt(i))) {
                    throw logger.logThrowableAsError(new IllegalArgumentException("Invalid subscription: "
                        + subscription + " provided. If this is the name of a subscription, use its ID instead."
                        + " You can locate your subscription ID by following the instructions"
                        + " listed here: https://learn.microsoft.com/azure/azure-portal/get-subscription-tenant-id"));
                }
            }
        }
    }
}
