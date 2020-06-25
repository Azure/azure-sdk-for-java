// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.IntelliJAuthMethodDetails;
import com.azure.identity.implementation.IntelliJCacheAccessor;
import com.azure.identity.implementation.MsalToken;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A credential provider that provides token credentials from Azure Tools for IntelliJ plugin credential cache.
 *
 * <p> If the developer has authenticated successfully with Azure Tools for IntelliJ plugin in the IntelliJ IDE then
 * this credential can be used in the development code to reuse the cached plugin credentials.</p>
 */
@Immutable
public class IntelliJCredential implements TokenCredential {
    private static final String AZURE_TOOLS_FOR_INTELLIJ_CLIENT_ID = "61d65f5a-6e3b-468b-af73-a033f5098c5c";
    private final IdentityClient identityClient;
    private final AtomicReference<MsalToken> cachedToken;
    private final ClientLogger logger = new ClientLogger(IntelliJCredential.class);

    /**
     * Creates an {@link IntelliJCredential} with default identity client options.
     * @param identityClientOptions the options to configure the identity client
     * @param tenantId the user specified tenant id.
     */
    IntelliJCredential(String tenantId, IdentityClientOptions identityClientOptions) {

        IdentityClientOptions options =
                identityClientOptions == null ? new IdentityClientOptions() : identityClientOptions;

        IntelliJCacheAccessor accessor =
                new IntelliJCacheAccessor(options.getIntelliJKeePassDatabasePath());

        IntelliJAuthMethodDetails authMethodDetails = null;
        try {
            authMethodDetails = accessor.getAuthDetailsIfAvailable();
        } catch (Exception e) {
            authMethodDetails = null;
        }

        if (CoreUtils.isNullOrEmpty(options.getAuthorityHost())) {
            String azureEnv = authMethodDetails != null ? authMethodDetails.getAzureEnv() : "";
            String cloudInstance = accessor.getAzureAuthHost(azureEnv);
            options.setAuthorityHost(cloudInstance);
        }

        String tenant = tenantId;

        if (tenant == null) {
            tenant = "common";
        }

        identityClient = new IdentityClientBuilder()
                             .identityClientOptions(options)
                             .tenantId(tenant)
                             .clientId(AZURE_TOOLS_FOR_INTELLIJ_CLIENT_ID)
                             .build();

        this.cachedToken = new AtomicReference<>();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.defer(() -> {
            if (cachedToken.get() != null) {
                return identityClient.authenticateWithPublicClientCache(request, cachedToken.get().getAccount())
                           .onErrorResume(t -> Mono.empty());
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(
            Mono.defer(() -> identityClient.authenticateWithIntelliJ(request)))
                   .map(msalToken -> {
                       cachedToken.set(msalToken);
                       return (AccessToken) msalToken;
                   })
            .doOnNext(token -> LoggingUtil.logTokenSuccess(logger, request))
            .doOnError(error -> LoggingUtil.logTokenError(logger, request, error));
    }
}
