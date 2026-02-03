// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.credentials;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides helper methods for validation of credentials used in storage.
 * <br>
 * RESERVED FOR INTERNAL USE.
 */
public class CredentialValidator {

    /**
     * Validates that only one credential has been provided.
     * @param storageSharedKeyCredential {@link StorageSharedKeyCredential} if present.
     * @param tokenCredential {@link TokenCredential} if present.
     * @param azureSasCredential {@link AzureSasCredential} if present.
     * @param sasToken {@link String} representing sas token if present.
     * @param logger {@link ClientLogger}. Mandatory.
     */
    public static void validateSingleCredentialIsPresent(StorageSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String sasToken, ClientLogger logger) {
        List<Object> usedCredentials
            = Stream.of(storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (usedCredentials.size() > 1) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Only one credential should be used. Credentials present: " + usedCredentials.stream()
                    .map(c -> c instanceof String ? "sasToken" : c.getClass().getName())
                    .collect(Collectors.joining(","))));
        }
    }


    /**
     * Validates that the provided credentials do not form an ambiguous or unsupported combination.
     * <p>
     * This method is intended for scenarios where multiple credential types may be supplied and enforces
     * the following rules:
     * <ul>
     *     <li>A maximum of two credentials may be provided at once. Supplying three or more credentials
     *     results in an {@link IllegalStateException}.</li>
     *     <li>When exactly two credentials are provided, the only supported combinations are:
     *     <ul>
     *         <li>{@link TokenCredential} together with {@link AzureSasCredential}</li>
     *         <li>{@link TokenCredential} together with a SAS token string ({@code sasToken})</li>
     *     </ul>
     *     Any other two-credential combination (including {@link TokenCredential} with both SAS forms,
     *     or combinations that do not include {@link TokenCredential}) is considered invalid and results
     *     in an {@link IllegalStateException}.</li>
     * </ul>
     *
     * @param storageSharedKeyCredential {@link StorageSharedKeyCredential} if present; may be {@code null}.
     * @param tokenCredential {@link TokenCredential} if present; may be {@code null}.
     * @param azureSasCredential {@link AzureSasCredential} if present; may be {@code null}.
     * @param sasToken {@link String} representing a SAS token if present; may be {@code null}.
     * @param logger {@link ClientLogger} used to log and wrap thrown exceptions; must not be {@code null}.
     *
     * @throws IllegalStateException if more than two credentials are specified, or if two credentials are
     * provided in a combination other than {@link TokenCredential} plus either {@link AzureSasCredential}
     * or {@code sasToken}.
     */
    public static void validateCredentialsNotAmbiguous(StorageSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String sasToken, ClientLogger logger) {
        boolean hasSharedKey = storageSharedKeyCredential != null;
        boolean hasToken = tokenCredential != null;
        boolean hasAzureSas = azureSasCredential != null;
        boolean hasTokenSas = sasToken != null;

        int credentialCount
            = (hasSharedKey ? 1 : 0) + (hasToken ? 1 : 0) + (hasAzureSas ? 1 : 0) + (hasTokenSas ? 1 : 0);

        if (credentialCount >= 3) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Too many credentials specified. Only TokenCredential can be combined with a SAS credential "
                    + "(AzureSasCredential or sasToken). A maximum of two credentials is currently supported."));
        }

        if (credentialCount == 2) {
            // Only allow: tokenCredential + (azureSasCredential or sasToken)
            boolean validCombo = hasToken && (hasAzureSas ^ hasTokenSas);
            if (!validCombo) {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "Invalid combination of credentials. Only TokenCredential with either AzureSasCredential or sasToken is supported when two credentials are provided."));
            }
        }
    }
}
