// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
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

    // TODO: Migrate to Configuration class in Core (https://github.com/Azure/azure-sdk-for-java/issues/14720)
    static final String PROPERTY_IDENTITY_ENDPOINT = "IDENTITY_ENDPOINT";
    static final String PROPERTY_IDENTITY_HEADER = "IDENTITY_HEADER";
    static final String PROPERTY_IMDS_ENDPOINT = "IMDS_ENDPOINT";

    /**
     * Creates an instance of the ManagedIdentityCredential.
     * @param clientId the client id of user assigned or system assigned identity
     * @param identityClientOptions the options for configuring the identity client.
     */
    ManagedIdentityCredential(String clientId, IdentityClientOptions identityClientOptions) {
        IdentityClient identityClient = new IdentityClientBuilder()
            .clientId(clientId)
            .identityClientOptions(identityClientOptions)
            .build();
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        if (configuration.contains(PROPERTY_IDENTITY_ENDPOINT)) {
            if (configuration.contains(PROPERTY_IDENTITY_HEADER)) {
                managedIdentityServiceCredential = new AppServiceMsiCredential(clientId, identityClient);
            } else if (configuration.contains(PROPERTY_IMDS_ENDPOINT)) {
                managedIdentityServiceCredential = new ArcIdentityCredential(clientId, identityClient);
            } else {
                managedIdentityServiceCredential = null;
            }
        } else if (configuration.contains(Configuration.PROPERTY_MSI_ENDPOINT)) {
            managedIdentityServiceCredential = new AppServiceMsiCredential(clientId, identityClient);
        } else {
            managedIdentityServiceCredential = new VirtualMachineMsiCredential(clientId, identityClient);
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
            .doOnSuccess((t -> logger.info(String.format("Azure Identity => Managed Identity environment: %s",
                    managedIdentityServiceCredential.getEnvironment()))))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(logger, request))
            .doOnError(error -> LoggingUtil.logTokenError(logger, request, error));
    }
}
