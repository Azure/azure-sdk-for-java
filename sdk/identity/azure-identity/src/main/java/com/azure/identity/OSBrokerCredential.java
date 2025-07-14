// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityUtil;

import reactor.core.publisher.Mono;
import java.util.concurrent.atomic.AtomicReference;

class OSBrokerCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(OSBrokerCredential.class);
    private static final String BROKER_BUILDER_CLASS = "com.azure.identity.broker.InteractiveBrowserBrokerCredentialBuilder";
    private final String tenantId;
    private final AtomicReference<TokenCredential> cached = new AtomicReference<>();

    OSBrokerCredential(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        TokenCredential credential = cached.get();
        if (credential == null) {
            TokenCredential newCredential = createBrokerCredential();
            if (cached.compareAndSet(null, newCredential)) {
                credential = newCredential;
            } else {
                credential = cached.get();
            }
        }
        return credential.getToken(request);
    }

    private TokenCredential createBrokerCredential() {
        if (!IdentityUtil.isBrokerAvailable()) {
            throw LOGGER.logExceptionAsError(
                new CredentialUnavailableException("azure-identity-broker dependency is not available. "
                + "Ensure you have azure-identity-broker dependency added to your application."));
        }
        try {
            Class<?> builderClass = Class.forName(BROKER_BUILDER_CLASS);
            Object builder = builderClass.getConstructor().newInstance();
            builderClass.getMethod("setWindowHandle", long.class).invoke(builder, 0);
            builderClass.getMethod("useDefaultBrokerAccount").invoke(builder);
            com.azure.identity.InteractiveBrowserCredentialBuilder browserCredentialBuilder
                = (com.azure.identity.InteractiveBrowserCredentialBuilder) builder;
            if (!CoreUtils.isNullOrEmpty(tenantId)) {
                builderClass.getMethod("tenantId", String.class).invoke(builder, tenantId);
            }
            return browserCredentialBuilder.build();
        } catch (ClassNotFoundException e) {
            throw LOGGER.logExceptionAsError(
                new CredentialUnavailableException("InteractiveBrowserBrokerCredentialBuilder class not found. "
                + "Ensure you have azure-identity-broker dependency added to your application.", e));
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(
                new CredentialUnavailableException("Failed to create InteractiveBrowserBrokerCredential dynamically", e));
        }
    }
}
