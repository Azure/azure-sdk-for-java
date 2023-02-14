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

import java.util.Objects;


/**
 * The DefaultAzureCredential is appropriate for most scenarios where the application ultimately runs in the Azure Cloud.
 * DefaultAzureCredential combines credentials that are commonly used to authenticate when deployed,
 * with credentials that are used to authenticate in a development environment. The DefaultAzureCredential will
 * attempt to authenticate via the following mechanisms in order.
 *
 * <ol>
 * <li>{@link EnvironmentCredential} - The DefaultAzureCredential will read account information specified via environment variables and use it to authenticate.</li>
 * <li>{@link ManagedIdentityCredential} - If the application deploys to an Azure host with Managed Identity enabled, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>{@link IntelliJCredential} - If you've authenticated via Azure Toolkit for IntelliJ, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>{@link AzureCliCredential} - If you've authenticated an account via the Azure CLI az login command, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>{@link AzurePowerShellCredential} - If you've authenticated an account via the Azure Power Shell Az Login command, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>Fails if none of the credentials above could be created.</li>
 * </ol>
 *
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/defaultazurecredential/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <H2>Configure DefaultAzureCredential</H2>
 * DefaultAzureCredential supports a set of configurations through setters on the DefaultAzureCredentialBuilder or environment variables.
 * <ol>
 *     <li>Setting the environment variables AZURE_CLIENT_ID, AZURE_CLIENT_SECRET/AZURE_CLIENT_CERTIFICATE_PATH, and AZURE_TENANT_ID configures the DefaultAzureCredential to authenticate as the service principal specified by the values.</li>
 *     <li>Setting {@link DefaultAzureCredentialBuilder#managedIdentityClientId(String)} on the builder or the environment variable AZURE_CLIENT_ID configures the DefaultAzureCredential to authenticate as a user-defined managed identity, while leaving them empty configures it to authenticate as a system-assigned managed identity.</li>
 *     <li>Setting .tenantId(String) on the builder or the environment variable AZURE_TENANT_ID configures the DefaultAzureCredential to authenticate to a specific tenant for Visual Studio Code, and IntelliJ IDEA.</li>
 * </ol>
 *
 * <p><strong>Sample: Construct DefaultAzureCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.construct -->
 * <pre>
 * DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.construct -->
 *
 * <p><strong>Sample: Construct DefaultAzureCredential with User Assigned Managed Identity </strong></p>
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity -->
 * <pre>
 * DefaultAzureCredential dacWithUserAssignedManagedIdentity = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .managedIdentityClientId&#40;&quot;&lt;Managed-Identity-Client-Id&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity -->
 *
 *
 * @see com.azure.identity.ManagedIdentityCredential
 * @see com.azure.identity.EnvironmentCredential
 * @see com.azure.identity.ClientSecretCredential
 * @see com.azure.identity.ClientCertificateCredential
 * @see com.azure.identity.UsernamePasswordCredential
 * @see com.azure.identity.AzureCliCredential
 * @see com.azure.identity.IntelliJCredential
 */


/**
 *
 * The ClientSecretCredential acquires an access token with a client secret for a service principal/registered AAD application.
 * The tenantId, clientId and clientSecret of the service principal are required for this credential to acquire an access token.
 * It can be used both in Azure hosted and local development environments for authentication.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/clientsecretcredential/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <p>As a pre-requisite, a service principal is required to use this authentication mechanism. If you don't have a service principal,
 * refer to <a href="https://aka.ms/azsdk/java/identity/serviceprincipal/create/docs">Create a service principal with Azure CLI.</a></p>
 *
 * <p><strong>Sample: Construct a simple ClientSecretCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.clientsecretcredential.construct -->
 * <pre>
 * ClientSecretCredential credential1 = new ClientSecretCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientSecret&#40;clientSecret&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientsecretcredential.construct -->
 *
 * <p><strong>Sample: Construct a ClientSecretCredential behind a proxy</strong></p>
 * <!-- src_embed com.azure.identity.credential.clientsecretcredential.constructwithproxy -->
 * <pre>
 * ClientSecretCredential credential2 = new ClientSecretCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientSecret&#40;clientSecret&#41;
 *     .proxyOptions&#40;new ProxyOptions&#40;Type.HTTP, new InetSocketAddress&#40;&quot;10.21.32.43&quot;, 5465&#41;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientsecretcredential.constructwithproxy -->
 */
@Immutable
public class ClientSecretCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ClientSecretCredential.class);

    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    /**
     * Creates a ClientSecretCredential with the given identity client options.
     *
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param clientSecret the secret value of the AAD application.
     * @param identityClientOptions the options for configuring the identity client
     */
    ClientSecretCredential(String tenantId, String clientId, String clientSecret,
                           IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(clientSecret, "'clientSecret' cannot be null.");
        Objects.requireNonNull(identityClientOptions, "'identityClientOptions' cannot be null.");
        IdentityClientBuilder builder =  new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .identityClientOptions(identityClientOptions);

        identityClient = builder.build();
        identitySyncClient = builder.buildSyncClient();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithConfidentialClientCache(request)
            .onErrorResume(t -> Mono.empty())
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithConfidentialClient(request)))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request,
                error));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClientCache(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) { }

        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClient(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
    }
}
