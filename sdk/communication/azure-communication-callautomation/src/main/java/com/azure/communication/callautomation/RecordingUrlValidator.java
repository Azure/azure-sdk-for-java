// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Validator for recording URLs to prevent SSRF attacks by ensuring URLs point to legitimate
 * Azure Communication Services recording endpoints before credentials are attached.
 */
final class RecordingUrlValidator {
    private static final ClientLogger LOGGER = new ClientLogger(RecordingUrlValidator.class);

    /**
     * Allowed recording endpoint host suffixes.
     * These are the only domains permitted for recording URLs to prevent credential exfiltration.
     */
    private static final List<String> ALLOWED_HOST_SUFFIXES
        = Arrays.asList(".asm.skype.com", ".asyncgw.teams.microsoft.com");

    private RecordingUrlValidator() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates that a recording URL points to Azure Communication Services
     * or Azure Blob Storage endpoint before credentials are attached.
     * This prevents credential exfiltration via SSRF attacks.
     *
     * @param recordingUrl The recording URL to validate
     * @param parameterName The parameter name for exception messages
     * @throws IllegalArgumentException if the URL is invalid or potentially malicious
     */
    static void validateRecordingUrl(String recordingUrl, String parameterName) {
        if (recordingUrl == null || recordingUrl.trim().isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(parameterName + " cannot be null or empty"));
        }

        URL parsedUrl;
        try {
            parsedUrl = new URL(recordingUrl);
        } catch (MalformedURLException e) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException(parameterName + " must be a valid absolute URI.", e));
        }

        // Ensure the URL is absolute and uses HTTPS
        String protocol = parsedUrl.getProtocol();
        if (protocol == null || protocol.trim().isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(parameterName + " must be an absolute URI."));
        }

        if (!"https".equalsIgnoreCase(protocol)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(parameterName + " must use HTTPS scheme for security."));
        }

        String host = parsedUrl.getHost();
        if (host == null || host.trim().isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(parameterName + " must have a valid host."));
        }

        String hostLowerCase = host.toLowerCase(Locale.ROOT);

        // Check against allowed suffixes
        boolean isValidEndpoint = ALLOWED_HOST_SUFFIXES.stream().anyMatch(hostLowerCase::endsWith);

        if (!isValidEndpoint) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(
                "%s host '%s' is not a valid Azure Communication Services recording endpoint. "
                    + "Only URLs pointing to *.asm.skype.com, *.asyncgw.teams.microsoft.com are allowed.",
                parameterName, host)));
        }

        // Log successful validation
        LOGGER.verbose("Recording URL validated successfully: {}", recordingUrl);
    }
}
