// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.core.util.configuration.BaseConfigurations;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * The base class for Managed Service Identity token based credentials.
 */
@Immutable
public final class ManagedIdentityCredential implements TokenCredential {
    private final AppServiceMSICredential appServiceMSICredential;
    private final VirtualMachineMSICredential virtualMachineMSICredential;

    /**
     * Creates an instance of the ManagedIdentityCredential.
     * @param clientId the client id of user assigned or system assigned identity
     * @param identityClientOptions the options for configuring the identity client.
     */
    ManagedIdentityCredential(String clientId, IdentityClientOptions identityClientOptions) {
        IdentityClient identityClient = new IdentityClientBuilder().clientId(clientId).identityClientOptions(identityClientOptions).build();
        Configuration configuration = ConfigurationManager.getConfiguration();
        if (configuration.contains(BaseConfigurations.MSI_ENDPOINT)) {
            appServiceMSICredential = new AppServiceMSICredential(clientId, identityClient);
            virtualMachineMSICredential = null;
        } else {
            virtualMachineMSICredential = new VirtualMachineMSICredential(clientId, identityClient);
            appServiceMSICredential = null;
        }
    }

    /**
     * @return the client id of user assigned or system assigned identity.
     */
    public String clientId() {
        return this.appServiceMSICredential != null ? this.appServiceMSICredential.clientId() : this.virtualMachineMSICredential.clientId();
    }

    /**
     * @return the endpoint from which token needs to be retrieved.
     */
    public String msiEndpoint() {
        return this.appServiceMSICredential == null ? null : this.appServiceMSICredential.msiEndpoint();
    }
    /**
     * @return the secret to use to retrieve the token.
     */
    public String msiSecret() {
        return this.appServiceMSICredential == null ? null : this.appServiceMSICredential.msiSecret();
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        return (appServiceMSICredential != null
            ? appServiceMSICredential.authenticate(scopes)
            : virtualMachineMSICredential.authenticate(scopes));
    }
}
