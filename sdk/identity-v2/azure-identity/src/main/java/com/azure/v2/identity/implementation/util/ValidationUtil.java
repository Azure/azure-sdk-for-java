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

    /**
     * Validates input parameters
     *
     * @param className the class invoking the API
     * @param logger the logger to be used for logging
     * @param names the list holding param names
     * @param values the list holding param values
     * @throws IllegalArgumentException if the validation fails
     */
    public static void validate(String className, ClientLogger logger, List<String> names, List<String> values) {
        String missing = "";

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == null) {
                missing += missing.isEmpty() ? names.get(i) : ", " + names.get(i);
            }
        }

        if (!missing.isEmpty()) {
            throw logger.throwableAtError()
                .addKeyValue("missingParameters", missing)
                .addKeyValue("className", className)
                .log("Some required parameters are not provided.", IllegalArgumentException::new);
        }
    }

    /**
     * Validates input parameters
     *
     * @param logger the logger to be used for logging
     * @param param1Name first parameter name
     * @param param1 first parameter value
     * @param param2Name second parameter name
     * @param param2 second parameter value
     * @throws IllegalArgumentException if the validation fails
     */
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
            throw logger.throwableAtError()
                .addKeyValue("missingParameters", missing)
                .log("Some required parameters are not provided.", IllegalArgumentException::new);
        }
    }

    /**
     * Validates input parameters
     *
     * @param logger the logger to be used for logging
     * @param param1Name first parameter name
     * @param param1 first parameter value
     * @param param2Name second parameter name
     * @param param2 second parameter value
     * @param param3Name third parameter name
     * @param param3 third parameter value
     * @throws IllegalArgumentException if the validation fails
     */
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
            throw logger.throwableAtError()
                .addKeyValue("missingParameters", missing)
                .log("Some required parameters are not provided.", IllegalArgumentException::new);
        }
    }

    /**
     * Validates authority host.
     *
     * @param authHost the authority host
     * @param logger the logger to be used for logging
     * @throws IllegalArgumentException if the validation fails
     */
    public static void validateAuthHost(String authHost, ClientLogger logger) {
        try {
            new URI(authHost);
        } catch (URISyntaxException e) {
            throw logger.throwableAtError()
                .log("Must provide a valid URI for authority host.", e, IllegalArgumentException::new);
        }
        if (!authHost.startsWith("https")) {
            throw logger.throwableAtError().log("Authority host must use https scheme.", IllegalArgumentException::new);
        }
    }

    /**
     * Validates the tenant id character range.
     *
     * @param id the tenant ID
     * @param logger the logger to be used for logging
     * @throws IllegalArgumentException if the validation fails.
     */
    public static void validateTenantIdCharacterRange(String id, ClientLogger logger) {
        if (id != null) {
            for (int i = 0; i < id.length(); i++) {
                if (!isValidTenantCharacter(id.charAt(i))) {
                    throw logger.throwableAtError()
                        .log(
                            "Invalid tenant id provided. You can locate your tenant id by following the instructions"
                                + " listed here: https://learn.microsoft.com/partner-center/find-ids-and-domain-names",
                            IllegalArgumentException::new);
                }
            }
        }
    }

    /**
     * Validates if the character is valid tenant character or not.
     *
     * @param c the input character
     * @return the boolean indicating character valid or not
     */
    private static boolean isValidTenantCharacter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '.') || (c == '-');
    }

    /**
     * Validates managed identity input parameters
     *
     * @param logger the logger to be used for logging
     * @param miClientOptions the managed identity client options
     * @throws IllegalStateException if the validation fails
     */
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
            throw logger.throwableAtError()
                .log("Only one of clientId, resourceId, or objectId can be specified.", IllegalStateException::new);
        }
    }

    /**
     * Validates input parameters
     *
     * @param className the class invoking the API
     * @param logger the logger to be used for logging
     * @param param1Name first parameter name
     * @param param1 first parameter value
     * @param param2Name second parameter name
     * @param param2 second parameter value
     * @param param3Name third parameter name
     * @param param3 third parameter value
     * @throws IllegalArgumentException if the validation fails
     */
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
            throw logger.throwableAtError()
                .addKeyValue("missingParameters", missing)
                .addKeyValue("className", className)
                .log("Some required parameters are not provided.", IllegalArgumentException::new);
        }
    }

    /**
     * Validates input parameters
     *
     * @param className the class invoking the API
     * @param logger the logger to be used for logging
     * @param param1Name first parameter name
     * @param param1 first parameter value
     * @param param2Name second parameter name
     * @param param2 second parameter value
     * @throws IllegalArgumentException if the validation fails
     */
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
            throw logger.throwableAtError()
                .addKeyValue("missingParameters", missing)
                .addKeyValue("className", className)
                .log("Some required parameters are not provided.", IllegalArgumentException::new);
        }
    }

    /**
     * Validates if the character is allowed in a subscription ID/Name or not.
     * @param c the input character
     * @return the boolean indicating validation result
     */
    private static boolean isValidSubscriptionCharacter(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || (c >= '0' && c <= '9')
            || (c == '.')
            || (c == '-')
            || (c == '_')
            || (c == ' ');
    }

    /**
     * Validates character range of subscription ID/Name.
     *
     * @param subscription the subscription ID/Name
     * @param logger the logger to be used for logging
     * @throws IllegalArgumentException if the validation fails
     */
    public static void validateSubscriptionCharacterRange(String subscription, ClientLogger logger) {
        if (subscription != null) {
            for (int i = 0; i < subscription.length(); i++) {
                if (!isValidSubscriptionCharacter(subscription.charAt(i))) {
                    throw logger.throwableAtError()
                        .addKeyValue("subscription", subscription)
                        .log(
                            "Invalid subscription if provided. If this is the name of a subscription, use its ID instead."
                                + " You can locate your subscription ID by following the instructions"
                                + " listed here: https://learn.microsoft.com/azure/azure-portal/get-subscription-tenant-id",
                            IllegalArgumentException::new);
                }
            }
        }
    }
}
