// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.credentials.TokenCredential;
import com.azure.identity.AccessToken;
import com.azure.identity.IdentityClient;
import com.azure.identity.IdentityClientOptions;
import com.azure.identity.implementation.AppServiceMSICredential;
import com.azure.identity.implementation.VirtualMachineMSICredential;
import reactor.core.publisher.Mono;

/**
 * The base class for Managed Service Identity token based credentials.
 */
public class ManagedIdentityCredential implements TokenCredential {
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
     * @return the principal id of user assigned or system assigned identity.
     */
    public String objectId() {
        return this.virtualMachineMSICredential == null ? null : this.virtualMachineMSICredential.objectId();
    }

    /**
     * @return the client id of user assigned or system assigned identity.
     */
    public String clientId() {
        return this.virtualMachineMSICredential == null ? null : this.virtualMachineMSICredential.clientId();
    }

    /**
     * @return the ARM resource id of the user assigned identity resource.
     */
    public String identityId() {
        return this.virtualMachineMSICredential == null ? null : this.virtualMachineMSICredential.identityId();
    }

    /**
     * specifies the principal id of user assigned or system assigned identity.
     *
     * @param objectId the object (principal) id
     * @return ManagedIdentityCredential
     */
    public ManagedIdentityCredential objectId(String objectId) {
        if (this.virtualMachineMSICredential != null) {
            this.virtualMachineMSICredential.objectId(objectId);
        }
        return this;
    }

    /**
     * Specifies the client id of user assigned or system assigned identity.
     *
     * @param clientId the client id
     * @return ManagedIdentityCredential
     */
    public ManagedIdentityCredential clientId(String clientId) {
        if (this.virtualMachineMSICredential != null) {
            this.virtualMachineMSICredential.clientId(clientId);
        }
        return this;
    }

    /**
     * Specifies the ARM resource id of the user assigned identity resource.
     *
     * @param identityId the identity ARM id
     * @return ManagedIdentityCredential
     */
    public ManagedIdentityCredential identityId(String identityId) {
        if (this.virtualMachineMSICredential != null) {
            this.virtualMachineMSICredential.identityId(identityId);
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

    /**
     * Specifies the Managed Service Identity endpoint for the App Service.
     * @param msiEndpoint the end point for acquiring a token on App Service.
     * @return ManagedIdentityCredential
     */
    public ManagedIdentityCredential msiEndpoint(String msiEndpoint) {
        if (this.appServiceMSICredential != null) {
            this.appServiceMSICredential.msiEndpoint(msiEndpoint);
        }
        return this;
    }

    /**
     * Specifies the Managed Service Identity secret for the App Service.
     * @param msiSecret the secret for acquiring a token on App Service.
     * @return ManagedIdentityCredential
     */
    public ManagedIdentityCredential msiSecret(String msiSecret) {
        if (this.appServiceMSICredential != null) {
            this.appServiceMSICredential.msiSecret(msiSecret);
        }
        return this;
    }

    @Override
    public Mono<String> getToken(String... scopes) {
        return (appServiceMSICredential != null ?
            appServiceMSICredential.authenticate(scopes) : virtualMachineMSICredential.authenticate(scopes))
            .map(AccessToken::token);
    }
}
