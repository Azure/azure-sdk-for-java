// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

/**
 * <p>Azure Developer CLI is a command-line interface tool that allows developers to create, manage, and deploy
 * resources in Azure. It's built on top of the Azure CLI and provides additional functionality specific
 * to Azure developers. It allows users to authenticate as a user and/or a service principal against
 * <a href="https://learn.microsoft.com/en-us/azure/active-directory/fundamentals/">Azure Active Directory (Azure AD)
 * </a>. The AzureDeveloperCliCredential authenticates in a development environment and acquires a token on behalf of
 * the logged-in user or service principal in Azure Developer CLI. It acts as the Azure Developer CLI logged in user or
 * service principal and executes an Azure CLI command underneath to authenticate the application against
 * Azure Active Directory.</p>
 *
 * <h2>Configure AzureDeveloperCliCredential</h2>
 *
 * <p> To use this credential, the developer needs to authenticate locally in Azure Developer CLI using one of the
 * commands below:</p>
 *
 * <ol>
 *     <li>Run "azd login" in Azure Developer CLI to authenticate interactively as a user.</li>
 *     <li>Run "azd login --client-id {@code clientID} --client-secret {@code clientSecret}
 *     --tenant-id {@code tenantID}" to authenticate as a service principal.</li>
 * </ol>
 *
 * <p>You may need to repeat this process after a certain time period, depending on the refresh token validity in your
 * organization. Generally, the refresh token validity period is a few weeks to a few months.
 * AzureDeveloperCliCredential will prompt you to sign in again.</p>
 *
 * <p><strong>Sample: Construct AzureDeveloperCliCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.AzureDeveloperCliCredential},
 * using the {@link com.azure.identity.AzureDeveloperCliCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.azuredeveloperclicredential.construct -->
 * <pre>
 * TokenCredential azureDevCliCredential = new AzureDeveloperCliCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azuredeveloperclicredential.construct -->
 *
 * @see com.azure.identity
 * @see AzureDeveloperCliCredentialBuilder
 */
@Immutable
public class AzureDeveloperCliCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AzureDeveloperCliCredential.class);

    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    /**
     * Creates an AzureDeveloperCliSecretCredential with default identity client options.
     * @param tenantId the tenant id of the application
     * @param identityClientOptions the options to configure the identity client
     */
    AzureDeveloperCliCredential(String tenantId, IdentityClientOptions identityClientOptions) {
        IdentityClientBuilder builder = new IdentityClientBuilder()
            .tenantId(tenantId)
            .identityClientOptions(identityClientOptions);

        identityClient = builder.build();
        identitySyncClient = builder.buildSyncClient();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithAzureDeveloperCli(request)
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request,
                error));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        try {
            AccessToken accessToken = identitySyncClient.authenticateWithAzureDeveloperCli(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
    }
}
