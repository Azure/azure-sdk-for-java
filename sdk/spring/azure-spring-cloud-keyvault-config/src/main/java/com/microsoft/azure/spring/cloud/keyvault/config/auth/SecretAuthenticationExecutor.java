/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config.auth;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

import java.util.concurrent.Future;

public class SecretAuthenticationExecutor implements AuthenticationExecutor {
    private ClientCredential credential;

    public SecretAuthenticationExecutor(ClientCredential credential) {
        this.credential = credential;
    }

    @Override
    public Future<AuthenticationResult> acquireToken(AuthenticationContext context, String resource) {
        return context.acquireToken(resource, credential, null);
    }
}
