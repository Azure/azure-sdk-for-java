// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.policy;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.core.implementation.AccessTokenCache;
import com.azure.v2.core.implementation.http.policy.AuthorizationChallengeParser;
import com.azure.v2.core.utils.CoreUtils;
import io.clientcore.core.credentials.AccessToken;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpCredentialPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * <p>The {@code BearerTokenAuthenticationPolicy} class is an implementation of the
 * {@link HttpCredentialPolicy}. This policy uses a {@link TokenCredential} to authenticate the request with
 * a bearer token.</p>
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
 * <pre>
 * TokenCredential credential = new BasicAuthenticationCredential&#40;&quot;username&quot;, &quot;password&quot;&#41;;
 * BearerTokenAuthenticationPolicy policy = new BearerTokenAuthenticationPolicy&#40;credential,
 *     &quot;https:&#47;&#47;management.azure.com&#47;.default&quot;&#41;;
 * </pre>
 *
 * @see HttpPipelinePolicy
 * @see TokenCredential
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public class BearerTokenAuthenticationPolicy extends HttpCredentialPolicy {
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
     * Executed before sending the initial request and authenticates the request.
     *
     * @param httpRequest The request context.
     */
    public void authorizeRequest(HttpRequest httpRequest) {
        setAuthorizationHeaderHelper(httpRequest, new TokenRequestContext().addScopes(scopes).setCaeEnabled(true),
            false);
    }

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication challenge
     * header is received after the initial request and returns appropriate {@link TokenRequestContext} to be used for
     * re-authentication.
     * <p>
     * The default implementation will attempt to handle Continuous Access Evaluation (CAE) challenges.
     * </p>
     *
     * @param httpRequest The http request.
     * @param response The Http Response containing the authentication challenge header.
     * @return A boolean indicating if containing the {@link TokenRequestContext} for re-authentication
     */
    public boolean authorizeRequestOnChallenge(HttpRequest httpRequest, Response<?> response) {
        if (AuthorizationChallengeParser.isCaeClaimsChallenge(response)) {
            TokenRequestContext tokenRequestContext = getTokenRequestContextForCaeChallenge(response);
            if (tokenRequestContext != null) {
                setAuthorizationHeader(httpRequest, tokenRequestContext);
                return true;
            }
        }

        return false;
    }

    /**
     * Authorizes the request with the bearer token acquired using the specified {@code tokenRequestContext}
     *
     * @param request the HTTP request.
     * @param tokenRequestContext the token request context to be used for token acquisition.
     */
    public void setAuthorizationHeader(HttpRequest request, TokenRequestContext tokenRequestContext) {
        setAuthorizationHeaderHelper(request, tokenRequestContext, true);
    }

    private void setAuthorizationHeaderHelper(HttpRequest httpRequest, TokenRequestContext tokenRequestContext,
        boolean checkToForceFetchToken) {
        AccessToken token = cache.getToken(tokenRequestContext, checkToForceFetchToken);
        setAuthorizationHeader(httpRequest.getHeaders(), token.getToken());
    }

    private static void setAuthorizationHeader(HttpHeaders headers, String token) {
        headers.set(HttpHeaderName.AUTHORIZATION, BEARER + " " + token);
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        if (!"https".equals(httpRequest.getUri().getScheme())) {
            throw LOGGER.logThrowableAsError(
                new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }

        HttpPipelineNextPolicy nextPolicy = next.copy();

        authorizeRequest(httpRequest);
        Response<?> httpResponse = next.process();
        String authHeader = httpResponse.getHeaders().getValue(HttpHeaderName.WWW_AUTHENTICATE);
        if (httpResponse.getStatusCode() == 401 && authHeader != null) {
            if (authorizeRequestOnChallenge(httpRequest, httpResponse)) {
                // body needs to be closed or read to the end to release the connection
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    throw LOGGER.logThrowableAsError(new RuntimeException(e));
                }
                return nextPolicy.process();
            } else {
                return httpResponse;
            }
        }
        return httpResponse;
    }

    private TokenRequestContext getTokenRequestContextForCaeChallenge(Response<?> response) {
        String decodedClaims = null;
        String encodedClaims
            = AuthorizationChallengeParser.getChallengeParameterFromResponse(response, "Bearer", "claims");

        if (!CoreUtils.isNullOrEmpty(encodedClaims)) {
            try {
                decodedClaims = new String(Base64.getDecoder().decode(encodedClaims), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                // We don't want to throw here, but we want to log this for future incident investigation.
                LOGGER.atLevel(LogLevel.WARNING)
                    .log("Failed to decode the claims from the CAE challenge. Encoded claims: " + encodedClaims);
            }
        }

        if (decodedClaims == null) {
            return null;
        }

        return new TokenRequestContext().setClaims(decodedClaims).addScopes(scopes).setCaeEnabled(true);
    }
}
