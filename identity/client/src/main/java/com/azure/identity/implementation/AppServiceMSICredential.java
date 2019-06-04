package com.azure.identity.implementation;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.identity.AccessToken;
import com.azure.identity.IdentityClient;
import reactor.core.publisher.Mono;

public class AppServiceMSICredential {
    private String msiEndpoint;
    private String msiSecret;
    private final IdentityClient identityClient;

    public AppServiceMSICredential(IdentityClient identityClient) {
        Configuration configuration = ConfigurationManager.getConfiguration();
        if (configuration.contains(BaseConfigurations.MSI_ENDPOINT)) {
            msiEndpoint = configuration.get(BaseConfigurations.MSI_ENDPOINT);
        }
        if (configuration.contains(BaseConfigurations.MSI_SECRET)) {
            msiSecret = configuration.get(BaseConfigurations.MSI_SECRET);
        }
        this.identityClient = identityClient;
    }

    /**
     * @return the endpoint from which token needs to be retrieved.
     */
    public String msiEndpoint() {
        return this.msiEndpoint;
    }
    /**
     * @return the secret to use to retrieve the token.
     */
    public String msiSecret() {
        return this.msiSecret;
    }

    public AppServiceMSICredential msiEndpoint(String msiEndpoint) {
        this.msiEndpoint = msiEndpoint;
        return this;
    }

    public AppServiceMSICredential msiSecret(String msiSecret) {
        this.msiSecret = msiSecret;
        return this;
    }

    public Mono<AccessToken> authenticate(String[] scopes) {
        return identityClient.managedIdentityClient().authenticateToManagedIdentityEnpoint(msiEndpoint, msiSecret, scopes);
    }
}
