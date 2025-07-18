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
 * <p>The Azure Powershell is a command-line tool that allows users to manage Azure resources from their local machine
 * or terminal. It allows users to
 * <a href="https://learn.microsoft.com/powershell/azure/authenticate-azureps">authenticate interactively</a>
 * as a user and/or a service principal against
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>.
 * The AzurePowershellCredential authenticates in a development environment and acquires a token on behalf of the
 * logged-in user or service principal in Azure Powershell. It acts as the Azure Powershell logged in user or
 * service principal and executes an Azure Powershell command underneath to authenticate the application against
 * Microsoft Entra ID.</p>
 *
 * <h2>Configure AzurePowershellCredential</h2>
 *
 * <p> To use this credential, the developer needs to authenticate locally in Azure Powershell using one of the
 * commands below:</p>
 *
 * <ol>
 *     <li>Run "Connect-AzAccount" in Azure Powershell to authenticate as a user.</li>
 *     <li>Run "Connect-AzAccount -ServicePrincipal -ApplicationId {servicePrincipalId} -Tenant {tenantId}
 *     -CertificateThumbprint {thumbprint} to authenticate as a service principal."</li>
 * </ol>
 *
 * <p>You may need to repeat this process after a certain time period, depending on the refresh token validity in your
 * organization. Generally, the refresh token validity period is a few weeks to a few months. AzurePowershellCredential
 * will prompt you to sign in again.</p>
 *
 * <p><strong>Sample: Construct AzurePowershellCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link AzurePowerShellCredential},
 * using the {@link AzurePowerShellCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <pre>
 * TokenCredential powerShellCredential = new AzurePowerShellCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see AzurePowerShellCredentialBuilder
 */
public class AzurePowerShellCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AzurePowerShellCredential.class);

    private final DevToolsClient devToolslClient;

    AzurePowerShellCredential(DevToolsClientOptions options) {
        devToolslClient = new DevToolsClient(options);
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        try {
            AccessToken accessToken = devToolslClient.authenticateWithAzurePowerShell(request);
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
