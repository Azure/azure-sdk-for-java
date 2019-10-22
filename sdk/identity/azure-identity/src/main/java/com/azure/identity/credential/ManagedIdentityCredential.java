// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequest;
import com.azure.core.util.Configuration;
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
        IdentityClient identityClient = new IdentityClientBuilder()
            .clientId(clientId)
            .identityClientOptions(identityClientOptions)
            .build();
        Configuration configuration = Configuration.getGlobalConfiguration();
        if (configuration.contains(Configuration.PROPERTY_MSI_ENDPOINT)) {
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
    public String getClientId() {
        return this.appServiceMSICredential != null
            ? this.appServiceMSICredential.getClientId()
            : this.virtualMachineMSICredential.getClientId();
    }

    /**
     * @return the endpoint from which token needs to be retrieved.
     */
    public String getMsiEndpoint() {
        return this.appServiceMSICredential == null ? null : this.appServiceMSICredential.getMsiEndpoint();
    }
    /**
     * @return the secret to use to retrieve the token.
     */
    public String getMsiSecret() {
        return this.appServiceMSICredential == null ? null : this.appServiceMSICredential.getMsiSecret();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequest request) {
        return (appServiceMSICredential != null
            ? appServiceMSICredential.authenticate(request)
            : virtualMachineMSICredential.authenticate(request));
    }
}
