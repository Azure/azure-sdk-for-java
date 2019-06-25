// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.util.configuration.BaseConfigurations;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.identity.IdentityClient;
import com.azure.identity.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * The base class for Managed Service Identity token based credentials.
 */
public final class ManagedIdentityCredential implements TokenCredential {
    private final AppServiceMSICredential appServiceMSICredential;
    private final VirtualMachineMSICredential virtualMachineMSICredential;

    /**
     * Creates an instance of the ManagedIdentityCredential with default identity client options.
     */
    public ManagedIdentityCredential() {
        this(new IdentityClientOptions());
    }

    /**
     * Creates an instance of the ManagedIdentityCredential.
     * @param identityClientOptions the options for configuring the identity client.
     */
    public ManagedIdentityCredential(IdentityClientOptions identityClientOptions) {
        IdentityClient identityClient = new IdentityClient(identityClientOptions);
        Configuration configuration = ConfigurationManager.getConfiguration();
        if (configuration.contains(BaseConfigurations.MSI_ENDPOINT) && configuration.contains(BaseConfigurations.MSI_SECRET)) {
            appServiceMSICredential = new AppServiceMSICredential(identityClient);
            virtualMachineMSICredential = null;
        } else {
            virtualMachineMSICredential = new VirtualMachineMSICredential(identityClient);
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
     * Specifies the client id of user assigned or system assigned identity.
     *
     * @param clientId the client id
     * @return ManagedIdentityCredential
     */
    public ManagedIdentityCredential clientId(String clientId) {
        if (this.appServiceMSICredential != null) {
            this.appServiceMSICredential.clientId(clientId);
        } else {
            this.virtualMachineMSICredential.clientId(clientId);
        }
        return this;
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
