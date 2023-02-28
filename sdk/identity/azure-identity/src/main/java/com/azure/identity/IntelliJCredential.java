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
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The IntelliJ credential authenticates in a development environment and acquires a token with the account in
 * Azure Toolkit for IntelliJ. It uses the logged in user information on the IntelliJ IDE and uses it to authenticate
 * the application against Azure Active Directory.
 *
 * <h2>Configure IntelliJCredential</h2>
 *
 * <p>Follow the steps outlined below:</p>
 *
 * <ol>
 *     <li>In your IntelliJ window, open File > Settings > Plugins.</li>
 *     <li>Search for "Azure Toolkit for IntelliJ" in the marketplace. Install and restart IDE.</li>
 *     <li>Find the new menu item Tools > Azure > Azure Sign In.</li>
 *     <li>Device Login will help you log in as a user account. Follow the instructions to log in on the
 *     login.microsoftonline.com website with the device code. IntelliJ will prompt you to select your subscriptions.
 *     Select the subscription with the resources that you want to access.</li>
 * </ol>
 *
 * <p> Once the developer has followed the steps above and authenticated successfully with
 * Azure Tools for IntelliJ plugin in the IntelliJ IDE then this credential can be used in the development code to
 * reuse the cached plugin credentials.</p>
 *
 * <p><strong>Sample: Construct IntelliJCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.intellijcredential.construct -->
 * <pre>
 * TokenCredential intelliJCredential = new IntelliJCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.intellijcredential.construct -->
 *
 * <p>The Azure SDK client builders consume TokenCredential for Azure Active Directory (AAD) based authentication.
 * The TokenCredential instantiated above can be passed into most of the Azure SDK client builders for
 * AAD authentication.</p>
 *
 * @see com.azure.identity
 * @see IntelliJCredentialBuilder
 */
@Immutable
public class IntelliJCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(IntelliJCredential.class);
    private static final String AZURE_TOOLS_FOR_INTELLIJ_CLIENT_ID = "61d65f5a-6e3b-468b-af73-a033f5098c5c";
    private final IdentityClient identityClient;
    private final AtomicReference<MsalToken> cachedToken;

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

        IntelliJAuthMethodDetails authMethodDetails;
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
                             .clientId(IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID)
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
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(),
                request, error));
    }
}
