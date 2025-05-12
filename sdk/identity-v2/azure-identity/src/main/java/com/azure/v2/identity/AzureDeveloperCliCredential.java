// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.implementation.client.DevToolsClient;
import com.azure.v2.identity.implementation.models.DevToolsClientOptions;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

/**
 * <p>Azure Developer CLI is a command-line interface tool that allows developers to create, manage, and deploy
 * resources in Azure. It's built on top of the Azure CLI and provides additional functionality specific
 * to Azure developers. It allows users to authenticate as a user and/or a service principal against
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>.
 * The AzureDeveloperCliCredential authenticates in a development environment and acquires a token on behalf of
 * the logged-in user or service principal in Azure Developer CLI. It acts as the Azure Developer CLI logged in user or
 * service principal and executes an Azure CLI command underneath to authenticate the application against
 * Microsoft Entra ID.</p>
 *
 * <h2>Configure AzureDeveloperCliCredential</h2>
 *
 * <p> To use this credential, the developer needs to authenticate locally in Azure Developer CLI using one of the
 * commands below:</p>
 *
 * <ol>
 *     <li>Run "azd auth login" in Azure Developer CLI to authenticate interactively as a user.</li>
 *     <li>Run "azd auth login --client-id {@code clientID} --client-secret {@code clientSecret}
 *     --tenant-id {@code tenantID}" to authenticate as a service principal.</li>
 * </ol>
 *
 * <p>You may need to repeat this process after a certain time period, depending on the refresh token validity in your
 * organization. Generally, the refresh token validity period is a few weeks to a few months.
 * AzureDeveloperCliCredential will prompt you to sign in again.</p>
 *
 * <p><strong>Sample: Construct AzureDeveloperCliCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link AzureDeveloperCliCredential},
 * using the {@link AzureDeveloperCliCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <pre>
 * TokenCredential azureDevCliCredential = new AzureDeveloperCliCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see AzureDeveloperCliCredentialBuilder
 */
public class AzureDeveloperCliCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AzureDeveloperCliCredential.class);

    private final DevToolsClient devToolslClient;

    /**
     * Creates an AzureDeveloperClCredential with given dev tools client options.
     *
     * @param clientOptions the options to configure the dev tools client
     */
    AzureDeveloperCliCredential(DevToolsClientOptions clientOptions) {
        devToolslClient = new DevToolsClient(clientOptions);
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        try {
            AccessToken accessToken = devToolslClient.authenticateWithAzureDeveloperCli(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (RuntimeException ex) {
            throw logAndThrowTokenError(LOGGER, request, ex,
                devToolslClient.getClientOptions().isChained()
                    ? CredentialUnavailableException::new
                    : CredentialAuthenticationException::new);
        }
    }
}
