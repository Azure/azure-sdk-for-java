// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2;

import com.azure.identity.v2.implementation.client.DevToolsClient;
import com.azure.identity.v2.implementation.models.DevToolsClientOptions;
import com.azure.identity.v2.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;

/**
 * <p>The Azure CLI is a command-line tool that allows users to manage Azure resources from their local machine or
 * terminal. It allows users to
 * <a href="https://learn.microsoft.com/cli/azure/authenticate-azure-cli">authenticate interactively</a> as a
 * user and/or a service principal against
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>.
 * The AzureCliCredential authenticates in a development environment and acquires a token on behalf of the
 * logged-in user or service principal in Azure CLI. It acts as the Azure CLI logged in user or service principal
 * and executes an Azure CLI command underneath to authenticate the application against Microsoft Entra ID.</p>
 *
 * <h2>Configure AzureCliCredential</h2>
 *
 * <p> To use this credential, the developer needs to authenticate locally in Azure CLI using one of the commands
 * below:</p>
 *
 * <ol>
 *     <li>Run "az login" in Azure CLI to authenticate as a user.</li>
 *     <li>Run "az login --service-principal --username {client ID} --password {client secret} --tenant {tenant ID}"
 *     to authenticate as a service principal.</li>
 * </ol>
 *
 * <p>You may need to repeat this process after a certain time period, depending on the refresh token validity in your
 * organization. Generally, the refresh token validity period is a few weeks to a few months. AzureCliCredential will
 * prompt you to sign in again.</p>
 *
 * <p><strong>Sample: Construct AzureCliCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link AzureCliCredential},
 * using the {@link AzureCliCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <pre>
 * TokenCredential azureCliCredential = new AzureCliCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.identity.v2
 * @see AzureCliCredentialBuilder
 */
public class AzureCliCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AzureCliCredential.class);

    private final DevToolsClient devToolslClient;

    /**
     * Creates an AzureCliSecretCredential with default identity client options.
     * @param devToolsClientOptions the options to configure the dev tools client
     */
    AzureCliCredential(DevToolsClientOptions devToolsClientOptions) {
        devToolslClient = new DevToolsClient(devToolsClientOptions);
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        try {
            AccessToken accessToken = devToolslClient.authenticateWithAzureCli(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (Exception ex) {
            LoggingUtil.logTokenError(LOGGER, request, ex);
            if (devToolslClient.getClientOptions().isChained()) {
                throw LOGGER.logThrowableAsError(new CredentialUnavailableException(ex.getMessage(), ex));
            }
            throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(ex.getMessage(), ex));
        }
    }
}
