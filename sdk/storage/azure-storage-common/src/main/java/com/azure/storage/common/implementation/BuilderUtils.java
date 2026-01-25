// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;

/**
 * This class provides helper methods for client builders.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class BuilderUtils {
    private BuilderUtils() {
    }

    public static RequestRetryPolicy createRetryPolicy(RequestRetryOptions retryOptions, RetryOptions coreRetryOptions,
        ClientLogger logger) {
        if (retryOptions != null && coreRetryOptions != null) {
            throw logger.logExceptionAsWarning(new IllegalStateException(
                "'retryOptions(RequestRetryOptions)' and 'retryOptions(RetryOptions)' cannot both be set"));
        }
        if (coreRetryOptions != null) {
            retryOptions = RequestRetryOptions.fromRetryOptions(coreRetryOptions, null, null);
        }
        if (retryOptions == null) {
            retryOptions = new RequestRetryOptions();
        }
        return new RequestRetryPolicy(retryOptions);
    }

    /**
     * Determines the authentication strategy based on provided credentials.
     *
     * @param storageSharedKeyCredential The storage shared key credential.
     * @param tokenCredential The token credential.
     * @param azureSasCredential The Azure SAS credential.
     * @param sasToken The SAS token.
     * @param logger The logger for error reporting.
     * @return The determined authentication strategy.
     * @throws IllegalStateException If an invalid combination of credentials is provided.
     */
    public static AuthenticationStrategy determineAuthenticationStrategy(
        StorageSharedKeyCredential storageSharedKeyCredential, TokenCredential tokenCredential,
        AzureSasCredential azureSasCredential, String sasToken, ClientLogger logger) {

        boolean hasSharedKey = storageSharedKeyCredential != null;
        boolean hasToken = tokenCredential != null;
        boolean hasSas = azureSasCredential != null || sasToken != null;

        // Count total credential types (SAS token and credential count as one type)
        int credentialCount = (hasSharedKey ? 1 : 0) + (hasToken ? 1 : 0) + (hasSas ? 1 : 0);

        if (credentialCount == 0) {
            return AuthenticationStrategy.ANONYMOUS;
        } else if (credentialCount == 1) {
            if (hasSharedKey) {
                return AuthenticationStrategy.SHARED_KEY;
            } else if (hasToken) {
                return AuthenticationStrategy.TOKEN;
            } else {
                return AuthenticationStrategy.SAS;
            }
        } else if (credentialCount == 2) {
            // Allow specific combinations for delegated user scenarios
            if (hasToken && hasSas) {
                return AuthenticationStrategy.TOKEN_WITH_SAS;
            } else {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "Invalid combination of credentials. Only token credential with SAS token/credential is supported for multi-credential scenarios."));
            }
        } else {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Too many credentials specified. A maximum of two credentials is supported for specific scenarios."));
        }
    }
}
