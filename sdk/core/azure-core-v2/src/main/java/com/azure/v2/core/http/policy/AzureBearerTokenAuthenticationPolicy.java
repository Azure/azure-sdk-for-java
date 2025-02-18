// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.policy;

import com.azure.v2.core.credentials.AzureTokenRequestContext;
import com.azure.v2.core.implementation.AccessTokenCache;
import com.azure.v2.core.implementation.http.policy.AuthorizationChallengeParser;
import com.azure.v2.core.utils.CoreUtils;
import io.clientcore.core.credentials.AccessToken;
import io.clientcore.core.credentials.TokenCredential;
import io.clientcore.core.credentials.TokenRequestContext;
import io.clientcore.core.http.models.*;
import io.clientcore.core.http.pipeline.*;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * <p>The {@code AzureBearerTokenAuthenticationPolicy} class is an implementation of the
 * {@link HttpPipelinePolicy} interface. This policy uses a {@link TokenCredential} to authenticate the request with
 * a bearer token.</p>
 *
 * <p>This class is useful when you need to authorize requests with a bearer token from Azure. It ensures that the
 * requests are sent over HTTPS to prevent the token from being leaked.</p>
 *
 * @see HttpPipelinePolicy
 * @see TokenCredential
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public class AzureBearerTokenAuthenticationPolicy extends BearerTokenAuthenticationPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(AzureBearerTokenAuthenticationPolicy.class);
    private static final String BEARER = "Bearer";

    private final String[] scopes;
    private final AccessTokenCache cache;

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes of authentication the credential should get token for
     */
    public AzureBearerTokenAuthenticationPolicy(TokenCredential credential, String... scopes) {
        super(credential, scopes);
        Objects.requireNonNull(credential);
        this.scopes = scopes;
        this.cache = new AccessTokenCache(credential);
    }

    /**
     * Synchronously executed before sending the initial request and authenticates the request.
     *
     * @param httpRequest The request context.
     */
    @Override
    public void authorizeRequest(HttpRequest httpRequest) {
        setAuthorizationHeaderHelper(httpRequest, new AzureTokenRequestContext().addScopes(scopes).setCaeEnabled(true),
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
    @Override
    public boolean authorizeRequestOnChallenge(HttpRequest httpRequest, Response<?> response) {
        if (AuthorizationChallengeParser.isCaeClaimsChallenge(response)) {
            AzureTokenRequestContext tokenRequestContext = getTokenRequestContextForCaeChallenge(response);
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
    @Override
    public void setAuthorizationHeader(HttpRequest request, TokenRequestContext tokenRequestContext) {
        setAuthorizationHeaderHelper(request, tokenRequestContext, true);
    }

    private void setAuthorizationHeaderHelper(HttpRequest httpRequest, TokenRequestContext tokenRequestContext,
        boolean checkToForceFetchToken) {
        AccessToken token = cache.getTokenSync(tokenRequestContext, checkToForceFetchToken);
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

    private AzureTokenRequestContext getTokenRequestContextForCaeChallenge(Response<?> response) {
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

        return new AzureTokenRequestContext().setClaims(decodedClaims).addScopes(scopes).setCaeEnabled(true);
    }
}
