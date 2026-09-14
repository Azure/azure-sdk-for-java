// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.AzureException;
import com.openai.core.ClientOptions;
import com.openai.core.LogLevel;
import com.openai.core.RequestOptions;
import com.openai.core.http.HttpClient;
import com.openai.core.http.HttpRequest;
import com.openai.core.http.HttpResponse;
import com.openai.credential.BearerTokenCredential;
import com.openai.credential.Credential;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

/**
 * Utility class used to forward token authentication to Stainless clients
 */
public final class TokenUtils {

    /**
     * Resolves the default Azure credential at the native async transport boundary.
     * Explicit native credential overrides bypass this adapter.
     */
    public static final class AsyncAuthentication {
        private final TokenCredential tokenCredential;
        private final String[] scopes;
        private final String marker = "azure-async-" + UUID.randomUUID();
        private final Credential credential = BearerTokenCredential.create(marker);

        /**
         * Creates authentication state for one native client.
         * @param tokenCredential Azure credential, required when default authentication is used.
         * @param scopes token scopes.
         */
        public AsyncAuthentication(TokenCredential tokenCredential, String... scopes) {
            this.tokenCredential = tokenCredential;
            this.scopes = scopes.clone();
        }

        /**
         * Gets the placeholder resolved by the authenticated transport before sending.
         * @return the native credential.
         */
        public Credential getCredential() {
            return credential;
        }

        /**
         * Wraps the final caller-selected transport after applying native options.
         * @param options native client options.
         * @return the authentication transport, before native client decorators are applied.
         */
        public HttpClient configure(ClientOptions.Builder options) {
            ClientOptions configured = options.build();
            if (configured.credential() != credential) {
                return configured.httpClient();
            }
            HttpClient transport = configured.toBuilder().maxRetries(0).logLevel(LogLevel.OFF).build().httpClient();
            HttpClient authenticatedTransport = new HttpClient() {
                @Override
                public HttpResponse execute(HttpRequest request, RequestOptions requestOptions) {
                    if (requiresToken(request)) {
                        request = authenticate(request, tokenCredential.getTokenSync(tokenContext()));
                    }
                    return transport.execute(request, requestOptions);
                }

                @Override
                public CompletableFuture<HttpResponse> executeAsync(HttpRequest request,
                    RequestOptions requestOptions) {
                    return Mono
                        .defer(() -> requiresToken(request)
                            ? tokenCredential.getToken(tokenContext())
                                .switchIfEmpty(
                                    Mono.error(new AzureException("The credential returned no access token.")))
                                .map(token -> authenticate(request, token))
                            : Mono.just(request))
                        .flatMap(authenticated -> Mono
                            .fromFuture(() -> transport.executeAsync(authenticated, requestOptions)))
                        .toFuture();
                }

                @Override
                public void close() {
                    transport.close();
                }
            };
            options.httpClient(authenticatedTransport);
            return authenticatedTransport;
        }

        private boolean requiresToken(HttpRequest request) {
            return request.headers().values("Authorization").contains("Bearer " + marker);
        }

        private TokenRequestContext tokenContext() {
            return new TokenRequestContext().setScopes(Arrays.asList(scopes));
        }

        private HttpRequest authenticate(HttpRequest request, AccessToken token) {
            return request.toBuilder().replaceHeaders("Authorization", "Bearer " + token.getToken()).build();
        }
    }

    /**
     * Utility authentication function.
     *
     * @param tokenCredential Token credential to be used
     * @param scopes for which the token credential authentication will be obtained
     * @return token supplier callback
     */
    public static Supplier<String> getBearerTokenSupplier(TokenCredential tokenCredential, String... scopes) {
        // Return a lazy supplier that fetches a fresh token on each invocation.
        // The Stainless OpenAI client calls this supplier to populate its Authorization header.
        return () -> {
            // Build a request context with the required scopes (e.g. "https://cognitiveservices.azure.com/.default")
            TokenRequestContext tokenRequestContext = new TokenRequestContext();
            tokenRequestContext.setScopes(Arrays.asList(scopes));

            // Obtain the token synchronously from the Azure credential (DefaultAzureCredential, etc.).
            // This delegates all caching and refresh logic to the credential implementation itself,
            // avoiding the need to construct an HttpPipeline or issue a dummy HTTP request.
            AccessToken accessToken = tokenCredential.getTokenSync(tokenRequestContext);
            return accessToken.getToken();
        };
    }
}
