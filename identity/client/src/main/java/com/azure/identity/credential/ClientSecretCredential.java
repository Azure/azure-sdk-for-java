package com.azure.identity.credential;

import com.azure.identity.AccessToken;
import com.azure.identity.IdentityClient;
import com.azure.identity.IdentityClientOptions;
import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * An AAD credential that acquires a token with a client secret for an AAD application.
 */
public class ClientSecretCredential extends AadCredential<ClientSecretCredential> {
    /* The client secret value. */
    private String clientSecret;
    private IdentityClient identityClient;

    /**
     * Creates a ClientSecretCredential with default AAD endpoint https://login.microsoftonline.com.
     */
    public ClientSecretCredential() {
        this(new IdentityClientOptions());
    }

    /**
     * Creates a ClientSecretCredential with default AAD endpoint https://login.microsoftonline.com.
     */
    public ClientSecretCredential(IdentityClientOptions identityClientOptions) {
        identityClient = new IdentityClient(identityClientOptions);
    }

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the AAD application.
     * @return the credential itself
     */
    public ClientSecretCredential clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    @Override
    public Mono<String> getToken(String... scopes) {
        return identityClient.activeDirectory().acquireTokenWithClientSecret(tenantId(), clientId(), clientSecret, scopes).map(AccessToken::token);
    }
}
