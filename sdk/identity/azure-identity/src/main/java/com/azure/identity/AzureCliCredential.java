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
 * The Azure CLI credential authenticates in a development environment and acquires a token with the enabled user
 * or service principal in Azure CLI. It uses the Azure CLI given a user that is already logged into it, and uses the
 * Azure CLI to authenticate the application against Azure Active Directory.
 *
 * <h2>Configure AzureCliCredential</h2>
 *
 * <p> To use this credential, the developer needs to authenticate locally in Azure CLI using one of the commands
 * below:</p>
 *
 * <ol>
 *     <li>Run "az login" in Azure CLI to authenticate as a user.</li>
 *     <li>Run "az login --service-principal --username <client ID> --password <client secret> --tenant <tenant ID>"
 *     to authenticate as a service principal.</li>
 * </ol>
 *
 * <p>You may need to repeat this process after a certain time period, depending on the refresh token validity in your
 * organization. Generally, the refresh token validity period is a few weeks to a few months. AzureCliCredential will
 * prompt you to sign in again.</p>
 *
 * <p><strong>Sample: Construct IntelliJCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.azureclicredential.construct -->
 * <pre>
 * TokenCredential azureCliCredential = new AzureCliCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azureclicredential.construct -->
 *
 * <p>The Azure SDK client builders consume TokenCredential for Azure Active Directory (AAD) based authentication.
 * The TokenCredential instantiated above can be passed into most of the Azure SDK client builders for
 * AAD authentication.</p>
 *
 * @see com.azure.identity
 * @see AzureCliCredentialBuilder
 */
@Immutable
public class AzureCliCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AzureCliCredential.class);

    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    /**
     * Creates an AzureCliSecretCredential with default identity client options.
     * @param tenantId the tenant id of the application
     * @param identityClientOptions the options to configure the identity client
     */
    AzureCliCredential(String tenantId, IdentityClientOptions identityClientOptions) {
        IdentityClientBuilder builder = new IdentityClientBuilder()
            .tenantId(tenantId)
            .identityClientOptions(identityClientOptions);

        identityClient = builder.build();
        identitySyncClient = builder.buildSyncClient();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithAzureCli(request)
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request,
                error));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        try {
            AccessToken accessToken = identitySyncClient.authenticateWithAzureCli(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
    }
}
