// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http.policy;

import com.azure.core.v2.credential.AccessToken;
import com.azure.core.v2.credential.TokenCredential;
import com.azure.core.v2.credential.TokenRequestContext;
import com.azure.core.v2.implementation.AccessTokenCache;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.util.ClientLogger;
import java.io.IOException;
import java.util.Objects;

/**
 * <p>The {@code BearerTokenAuthenticationPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface.
 * This policy uses a {@link TokenCredential} to authenticate the request with a bearer token.</p>
 *
 * <p>This class is useful when you need to authorize requests with a bearer token from Azure. It ensures that the
 * requests are sent over HTTPS to prevent the token from being leaked.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code BearerTokenAuthenticationPolicy} is created with a {@link TokenCredential} and a scope.
 * The policy can then added to the pipeline. The request sent via the pipeline will then include the
 * Authorization header with the bearer token.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.BearerTokenAuthenticationPolicy.constructor -->
 * <pre>
 * TokenCredential credential = new BasicAuthenticationCredential&#40;&quot;username&quot;, &quot;password&quot;&#41;;
 * BearerTokenAuthenticationPolicy policy = new BearerTokenAuthenticationPolicy&#40;credential,
 *     &quot;https:&#47;&#47;management.azure.com&#47;.default&quot;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.BearerTokenAuthenticationPolicy.constructor -->
 *
 * @see HttpPipelinePolicy
 * @see com.azure.core.v2.credential.TokenCredential
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public class BearerTokenAuthenticationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(BearerTokenAuthenticationPolicy.class);
    private static final String BEARER = "Bearer";

    private final String[] scopes;
    private final AccessTokenCache cache;

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes of authentication the credential should get token for
     */
    public BearerTokenAuthenticationPolicy(TokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        this.scopes = scopes;
        this.cache = new AccessTokenCache(credential);
    }

    /**
     * Synchronously executed before sending the initial request and authenticates the request.
     *
     * @param httpRequest The request context.
     */
    public void authorizeRequestSync(HttpRequest httpRequest) {
        setAuthorizationHeaderHelperSync(httpRequest, new TokenRequestContext().addScopes(scopes), false);
    }

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication challenge
     * header is received after the initial request and returns appropriate {@link TokenRequestContext} to be used for
     * re-authentication.
     *
     * @param httpRequest The request context.
     * @param response The Http Response containing the authentication challenge header.
     *
     * @return A boolean indicating if containing the {@link TokenRequestContext} for re-authentication
     */
    public boolean authorizeRequestOnChallengeSync(HttpRequest httpRequest, Response<?> response) {
        return false;
    }

    /**
     * Authorizes the request with the bearer token acquired using the specified {@code tokenRequestContext}
     *
     * @param request the HTTP request.
     * @param tokenRequestContext the token request context to be used for token acquisition.
     */
    public void setAuthorizationHeaderSync(HttpRequest request, TokenRequestContext tokenRequestContext) {
        setAuthorizationHeaderHelperSync(request, tokenRequestContext, true);
    }

    private void setAuthorizationHeaderHelperSync(HttpRequest httpRequest, TokenRequestContext tokenRequestContext,
        boolean checkToForceFetchToken) {
        AccessToken token = cache.getTokenSync(tokenRequestContext, checkToForceFetchToken);
        setAuthorizationHeader(httpRequest.getHeaders(), token.getToken());
    }

    private static void setAuthorizationHeader(HttpHeaders headers, String token) {
        headers.set(HttpHeaderName.AUTHORIZATION, BEARER + " " + token);
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        if (!"https".equals(httpRequest.getUrl().getProtocol())) {
            throw LOGGER.logThrowableAsError(
                new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }
        HttpPipelineNextPolicy nextPolicy = next.clone();

        authorizeRequestSync(httpRequest);
        Response<?> httpResponse = next.process();
        String authHeader = httpResponse.getHeaders().getValue(HttpHeaderName.WWW_AUTHENTICATE);
        if (httpResponse.getStatusCode() == 401 && authHeader != null) {
            if (authorizeRequestOnChallengeSync(httpRequest, httpResponse)) {
                // body needs to be closed or read to the end to release the connection
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return nextPolicy.process();
            } else {
                return httpResponse;
            }
        }
        return httpResponse;
    }
}
