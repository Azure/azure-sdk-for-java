/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config.auth;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;

import java.net.MalformedURLException;
import java.util.concurrent.*;

/**
 * Subclass of {@link KeyVaultCredentials}.
 */
public class AadKeyVaultCredentials extends KeyVaultCredentials {
    // TODO: make it configurable.
    private static final int TIMEOUT_IN_SECONDS = 15;
    private static final String AUTH_FAILED = "Failed to authenticate with Azure Key Vault.";

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private AuthenticationExecutor authExecutor;

    public AadKeyVaultCredentials(AuthenticationExecutor authExecutor) {
        this.authExecutor = authExecutor;
    }

    @Override
    public String doAuthenticate(String authorization, String resource, String scope) {

        try {
            AuthenticationContext context = new AuthenticationContext(authorization, false, service);
            return authExecutor.acquireToken(context, resource)
                    .get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .getAccessToken();
        } catch (InterruptedException | ExecutionException | TimeoutException | MalformedURLException ex) {
            throw new IllegalStateException(AUTH_FAILED, ex);
        }
    }
}
