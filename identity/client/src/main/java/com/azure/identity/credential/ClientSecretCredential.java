package com.azure.identity.credential;

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

    /**
     * Creates a ClientSecretCredential with default AAD endpoint https://login.microsoftonline.com.
     */
    public ClientSecretCredential() {
        super();
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
    public Mono<AuthenticationResult> authenticateAsync(String resource) {
        validate();
        if (clientSecret == null) {
            throw new IllegalArgumentException("Non-null value must be provided for clientSecret property in ClientSecretCredential");
        }
        return acquireAccessToken(resource);
    }

    private Mono<AuthenticationResult> acquireAccessToken(String resource) {
        String authorityUrl = this.aadEndpoint() + this.tenantId();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context;
        try {
            context = new AuthenticationContext(authorityUrl, false, executor);
        } catch (MalformedURLException mue) {
            executor.shutdown();
            throw Exceptions.propagate(mue);
        }
        return Mono.create((Consumer<MonoSink<AuthenticationResult>>) callback -> {
            context.acquireToken(
                    resource,
                    new ClientCredential(this.clientId(), this.clientSecret),
                    new AuthenticationCallback<AuthenticationResult>() {
                        @Override
                        public void onSuccess(AuthenticationResult o) {
                            callback.success(o);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            callback.error(throwable);
                        }
                    });
        }).doFinally(s -> executor.shutdown());
    }
}
