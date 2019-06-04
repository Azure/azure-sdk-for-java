package com.azure.identity.implementation;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.identity.AccessToken;
import com.azure.identity.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential for App Service.
 */
public class AppServiceMSICredential {
    private String msiEndpoint;
    private String msiSecret;
    private final IdentityClient identityClient;

    /**
     * Creates an instance of AppServiceMSICredential.
     * @param identityClient the identity client to acquire a token with.
     */
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

    /**
     * Specifies the Managed Service Identity endpoint for the App Service.
     * @param msiEndpoint the end point for acquiring a token on App Service.
     * @return AppServiceMSICredential
     */
    public AppServiceMSICredential msiEndpoint(String msiEndpoint) {
        this.msiEndpoint = msiEndpoint;
        return this;
    }

    /**
     * Specifies the Managed Service Identity secret for the App Service.
     * @param msiSecret the secret for acquiring a token on App Service.
     * @return AppServiceMSICredential
     */
    public AppServiceMSICredential msiSecret(String msiSecret) {
        this.msiSecret = msiSecret;
        return this;
    }

    /**
     * Gets the token for a list of scopes.
     * @param scopes the scopes to get token for
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticate(String[] scopes) {
        return identityClient.managedIdentityClient().authenticateToManagedIdentityEnpoint(msiEndpoint, msiSecret, scopes);
    }
}
