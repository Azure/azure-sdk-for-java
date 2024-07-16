// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.azure.identity.implementation.util.IdentityUtil.isLinuxPlatform;
import static com.azure.identity.implementation.util.IdentityUtil.isWindowsPlatform;

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
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Must provide non-null values for "
                +  missing + " properties in " + className));
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
                                + " listed here: https://learn.microsoft.com/partner-center/find-ids-and-domain-names"));
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


    public static Path validateSecretFile(File file, ClientLogger logger) {

        Path path = file.toPath();
        if (isWindowsPlatform()) {
            String programData = System.getenv("ProgramData");
            if (CoreUtils.isNullOrEmpty(programData)) {
                throw logger.logExceptionAsError(new ClientAuthenticationException("The ProgramData environment"
                    + " variable is not set.", null));
            }
            String target = Paths.get(programData, "AzureConnectedMachineAgent", "Tokens").toString();
            if (!path.getParent().toString().equals(target)) {
                throw logger.logExceptionAsError(new ClientAuthenticationException("The secret key file is not"
                    + " located in the expected directory.", null));
            }
        } else if (isLinuxPlatform()) {
            Path target = Paths.get("/", "var", "opt", "azcmagent", "tokens");
            if (!path.getParent().equals(target)) {
                throw logger.logExceptionAsError(new ClientAuthenticationException("The secret key file is not"
                    + " located in the expected directory.", null));
            }
        } else {
            throw logger.logExceptionAsError(new ClientAuthenticationException("The platform is not supported"
                + " for Azure Arc Managed Identity Endpoint", null));
        }

        if (!path.toString().endsWith(".key")) {
            throw logger.logExceptionAsError(new ClientAuthenticationException("The secret key file does not"
                + " have the expected file extension", null));
        }



        if (file.length() > 4096) {
            throw logger.logExceptionAsError(new ClientAuthenticationException("The secret key file is too large"
                + " to be read from Azure Arc Managed Identity Endpoint", null));
        }

        return path;
    }
}
