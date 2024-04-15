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
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

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
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.AzurePowerShellCredential},
 * using the {@link com.azure.identity.AzurePowerShellCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.azurepowershellcredential.construct -->
 * <pre>
 * TokenCredential powerShellCredential = new AzurePowerShellCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azurepowershellcredential.construct -->
 *
 * @see com.azure.identity
 * @see AzurePowerShellCredentialBuilder
 */
@Immutable
public class AzurePowerShellCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AzurePowerShellCredential.class);

    private final IdentityClient identityClient;

    AzurePowerShellCredential(String tenantId, IdentityClientOptions options) {
        identityClient = new IdentityClientBuilder()
            .identityClientOptions(options)
            .tenantId(tenantId)
            .build();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithAzurePowerShell(request)
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request,
                error))
            .onErrorMap(error -> {
                if (identityClient.getIdentityClientOptions().isChained()) {
                    return new CredentialUnavailableException(error.getMessage(), error);
                } else {
                    return error;
                }
            });
    }
}
