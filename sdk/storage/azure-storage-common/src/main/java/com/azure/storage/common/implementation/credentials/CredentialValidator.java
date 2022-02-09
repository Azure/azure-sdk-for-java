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
 *
 * RESERVED FOR INTERNAL USE.
 */
public class CredentialValidator {

    /**
     * Validates that only one credential has been provided.
     * @param storageSharedKeyCredential {@link StorageSharedKeyCredential} if present.
     * @param tokenCredential {@link TokenCredential} if present.
     * @param azureSasCredential {@link AzureSasCredential} if present.
     * @param sasToken {@link String} representing sas token if present.
     * @param logger {@ClientLogger}. Mandatory.
     */
    public static void validateSingleCredentialIsPresent(
        StorageSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String sasToken, ClientLogger logger) {
        List<Object> usedCredentials = Stream.of(
            storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken)
            .filter(Objects::nonNull).collect(Collectors.toList());
        if (usedCredentials.size() > 1) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Only one credential should be used. Credentials present: "
                    + usedCredentials.stream().map(c -> c instanceof String ? "sasToken" : c.getClass().getName())
                    .collect(Collectors.joining(","))
            ));
        }
    }
}
