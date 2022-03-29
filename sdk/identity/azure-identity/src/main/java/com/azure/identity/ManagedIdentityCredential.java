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

import java.time.Duration;

/**
 * The base class for Managed Service Identity token based credentials.
 */
@Immutable
public final class ManagedIdentityCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ManagedIdentityCredential.class);

    final ManagedIdentityServiceCredential managedIdentityServiceCredential;
    private final IdentityClientOptions identityClientOptions;

    static final String PROPERTY_IMDS_ENDPOINT = "IMDS_ENDPOINT";
    static final String PROPERTY_IDENTITY_SERVER_THUMBPRINT = "IDENTITY_SERVER_THUMBPRINT";
    static final String AZURE_FEDERATED_TOKEN_FILE = "AZURE_FEDERATED_TOKEN_FILE";


    /**
     * Creates an instance of the ManagedIdentityCredential with the client ID of a
     * user-assigned identity, or app registration (when working with AKS pod-identity).
     * @param clientId the client id of user assigned identity or app registration (when working with AKS pod-identity).
     * @param resourceId the resource id of user assigned identity or registered application
     * @param identityClientOptions the options for configuring the identity client.
     */
    ManagedIdentityCredential(String clientId, String resourceId, IdentityClientOptions identityClientOptions) {
        IdentityClientBuilder clientBuilder = new IdentityClientBuilder()
            .clientId(clientId)
            .resourceId(resourceId)
            .identityClientOptions(identityClientOptions);
        this.identityClientOptions = identityClientOptions;

        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone() : identityClientOptions.getConfiguration();


        /*
         * Choose credential based on available environment variables in this order:
         *
         * Azure Arc: IDENTITY_ENDPOINT, IMDS_ENDPOINT
         * Service Fabric: IDENTITY_ENDPOINT, IDENTITY_HEADER, IDENTITY_SERVER_THUMBPRINT
         * App Service 2019-08-01: IDENTITY_ENDPOINT, IDENTITY_HEADER (MSI_ENDPOINT and MSI_SECRET will also be set.)
         * App Service 2017-09-01: MSI_ENDPOINT, MSI_SECRET
         * Cloud Shell: MSI_ENDPOINT
         * Pod Identity V2 (AksExchangeToken): AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_FEDERATED_TOKEN_FILE
         * IMDS/Pod Identity V1: No variables set.
         */

        if (configuration.contains(Configuration.PROPERTY_MSI_ENDPOINT)) {
            managedIdentityServiceCredential = new AppServiceMsiCredential(clientId, clientBuilder.build());
        } else if (configuration.contains(Configuration.PROPERTY_IDENTITY_ENDPOINT)) {
            if (configuration.contains(Configuration.PROPERTY_IDENTITY_HEADER)) {
                if (configuration.get(PROPERTY_IDENTITY_SERVER_THUMBPRINT) != null) {
                    managedIdentityServiceCredential = new ServiceFabricMsiCredential(clientId, clientBuilder.build());
                } else {
                    managedIdentityServiceCredential = new AppServiceMsiCredential(clientId, clientBuilder.build());
                }
            } else if (configuration.get(PROPERTY_IMDS_ENDPOINT) != null) {
                managedIdentityServiceCredential = new ArcIdentityCredential(clientId, clientBuilder.build());
            } else {
                managedIdentityServiceCredential = new VirtualMachineMsiCredential(clientId, clientBuilder.build());
            }
        } else if (configuration.contains(Configuration.PROPERTY_AZURE_TENANT_ID)
                && configuration.get(AZURE_FEDERATED_TOKEN_FILE) != null) {
            String clientIdentifier = clientId == null
                ? configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID) : clientId;
            clientBuilder.clientId(clientIdentifier);
            clientBuilder.tenantId(configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID));
            clientBuilder.clientAssertionPath(configuration.get(AZURE_FEDERATED_TOKEN_FILE));
            clientBuilder.clientAssertionTimeout(Duration.ofMinutes(5));
            managedIdentityServiceCredential = new AksExchangeTokenCredential(clientIdentifier, clientBuilder.build());
        } else {
            managedIdentityServiceCredential = new VirtualMachineMsiCredential(clientId, clientBuilder.build());
        }
        LoggingUtil.logAvailableEnvironmentVariables(LOGGER, configuration);
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
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, identityClientOptions,
                new CredentialUnavailableException("ManagedIdentityCredential authentication unavailable. "
                   + "The Target Azure platform could not be determined from environment variables."
                    + "To mitigate this issue, please refer to the troubleshooting guidelines here at"
                    + " https://aka.ms/azsdk/java/identity/managedidentitycredential/troubleshoot")));
        }
        return managedIdentityServiceCredential.authenticate(request)
            .doOnSuccess(t -> LOGGER.info("Azure Identity => Managed Identity environment: {}",
                    managedIdentityServiceCredential.getEnvironment()))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClientOptions, request, error));
    }
}


