package com.azure.identity.credential;

import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * An AAD credential that acquires a token with a client secret for an AAD application.
 */
public class ClientSecretCredential extends AadCredential<ClientSecretCredential> {
    /* A mapping from resource endpoint to its cached access token. */
    private final Map<String, AuthenticationResult> tokens = new HashMap<>();
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
    public Mono<String> getTokenAsync(String resource) {
        validate();
        if (clientSecret == null) {
            throw new IllegalArgumentException("Non-null value must be provided for clientSecret property in ClientSecretCredential");
        }
        AuthenticationResult authenticationResult = tokens.get(resource);
        if (authenticationResult != null && authenticationResult.getExpiresOnDate().after(new Date())) {
            return Mono.just(authenticationResult.getAccessToken());
        } else {
            return acquireAccessToken(resource)
                    .map(ar -> {
                        tokens.put(resource, ar);
                        return ar.getAccessToken();
                    });
        }
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
