// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

/**
 * The base class for Managed Service Identity token based credentials.
 */
@Immutable
public final class ManagedIdentityCredential implements TokenCredential {
    private final ManagedIdentityServiceCredential managedIdentityServiceCredential;
    private final ClientLogger logger = new ClientLogger(ManagedIdentityCredential.class);

    static final String PROPERTY_IMDS_ENDPOINT = "IMDS_ENDPOINT";
    static final String PROPERTY_IDENTITY_SERVER_THUMBPRINT = "IDENTITY_SERVER_THUMBPRINT";
    static final String TOKEN_FILE_PATH = "TOKEN_FILE_PATH";


    /**
     * Creates an instance of the ManagedIdentityCredential.
     * @param clientId the client id of user assigned or system assigned identity
     * @param identityClientOptions the options for configuring the identity client.
     */
    ManagedIdentityCredential(String clientId, IdentityClientOptions identityClientOptions) {
        IdentityClientBuilder clientBuilder = new IdentityClientBuilder()
            .clientId(clientId)
            .identityClientOptions(identityClientOptions);

        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        if (configuration.contains(Configuration.PROPERTY_MSI_ENDPOINT)) {
            managedIdentityServiceCredential = new AppServiceMsiCredential(clientId, clientBuilder.build());
        } else if (configuration.contains(Configuration.PROPERTY_IDENTITY_ENDPOINT)) {
            if (configuration.contains(Configuration.PROPERTY_IDENTITY_HEADER)) {
                if (configuration.get(PROPERTY_IDENTITY_SERVER_THUMBPRINT) != null) {
                    managedIdentityServiceCredential = new ServiceFabricMsiCredential(clientId, clientBuilder.build());
                } else {
                    managedIdentityServiceCredential = new VirtualMachineMsiCredential(clientId, clientBuilder.build());
                }
            } else if (configuration.get(PROPERTY_IMDS_ENDPOINT) != null) {
                managedIdentityServiceCredential = new ArcIdentityCredential(clientId, clientBuilder.build());
            } else {
                managedIdentityServiceCredential = new VirtualMachineMsiCredential(clientId, clientBuilder.build());
            }
        } else if (configuration.contains(Configuration.PROPERTY_AZURE_CLIENT_ID)
            && configuration.contains(Configuration.PROPERTY_AZURE_TENANT_ID)
            && configuration.get(TOKEN_FILE_PATH) != null) {
            clientBuilder.tenantId(configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID));
            clientBuilder.clientAssertionPath(configuration.get(TOKEN_FILE_PATH));
            managedIdentityServiceCredential = new ClientAssertionCredential(clientId, clientBuilder.build());

        } else {
            managedIdentityServiceCredential = new VirtualMachineMsiCredential(clientId, clientBuilder.build());
        }
        LoggingUtil.logAvailableEnvironmentVariables(logger, configuration);
    }

    /**
     * Gets the client ID of user assigned or system assigned identity.
     * @return the client ID of user assigned or system assigned identity.
     */
    public String getClientId() {
        return managedIdentityServiceCredential.getClientId();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (managedIdentityServiceCredential == null) {
            return Mono.error(logger.logExceptionAsError(
                new CredentialUnavailableException("ManagedIdentityCredential authentication unavailable. "
                   + "The Target Azure platform could not be determined from environment variables.")));
        }
        return managedIdentityServiceCredential.authenticate(request)
            .doOnSuccess(t -> logger.info("Azure Identity => Managed Identity environment: {}",
                    managedIdentityServiceCredential.getEnvironment()))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(logger, request))
            .doOnError(error -> LoggingUtil.logTokenError(logger, request, error));
    }
}


