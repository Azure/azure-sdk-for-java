// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.pipeline;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.core.implementation.AccessTokenCache;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpCredentialPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.AuthUtils;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.clientcore.core.utils.CoreUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
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
     * @return A boolean indicating if the request was authorized again via re-authentication
     */
    public boolean authorizeRequestOnChallenge(HttpRequest httpRequest, Response<?> response) {
        if (isCaeClaimsChallenge(response)) {
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
    protected void setAuthorizationHeader(HttpRequest request, TokenRequestContext tokenRequestContext) {
        setAuthorizationHeaderHelper(request, tokenRequestContext, true);
    }

    private void setAuthorizationHeaderHelper(HttpRequest httpRequest, TokenRequestContext tokenRequestContext,
        boolean checkToForceFetchToken) {
        AccessToken token = cache.getToken(tokenRequestContext, checkToForceFetchToken);
        httpRequest.getHeaders().set(HttpHeaderName.AUTHORIZATION, BEARER + " " + token.getToken());
    }

    @Override
    public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        if (!"https".equals(httpRequest.getUri().getScheme())) {
            throw LOGGER.throwableAtError()
                .log("token credentials require a URL using the HTTPS protocol scheme", IllegalArgumentException::new);
        }

        HttpPipelineNextPolicy nextPolicy = next.copy();

        authorizeRequest(httpRequest);
        Response<BinaryData> httpResponse = next.process();
        String authHeader = httpResponse.getHeaders().getValue(HttpHeaderName.WWW_AUTHENTICATE);
        if (httpResponse.getStatusCode() == 401 && authHeader != null) {
            if (authorizeRequestOnChallenge(httpRequest, httpResponse)) {
                // body needs to be closed or read to the end to release the connection
                httpResponse.close();
                return nextPolicy.process();
            } else {
                return httpResponse;
            }
        }
        return httpResponse;
    }

    private TokenRequestContext getTokenRequestContextForCaeChallenge(Response<?> response) {
        String decodedClaims = null;
        String encodedClaims = getChallengeParameterFromResponse(response, "Bearer", "claims");

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

    /**
     * Examines a {@link Response} to see if it is a CAE challenge.
     * @param response The {@link Response} to examine.
     * @return True if the response is a CAE challenge, false otherwise.
     */
    static boolean isCaeClaimsChallenge(Response<?> response) {
        List<AuthenticateChallenge> authenticateChallengeList
            = AuthUtils.parseAuthenticateHeader(response.getHeaders().getValue(HttpHeaderName.WWW_AUTHENTICATE));

        for (AuthenticateChallenge authChallenge : authenticateChallengeList) {
            if (authChallenge.getScheme().equals("Bearer")) {

                String error = authChallenge.getParameters().get("error");
                String claims = authChallenge.getParameters().get("claims");
                return !CoreUtils.isNullOrEmpty(claims) && "insufficient_claims".equals(error);
            }
        }
        return false;
    }

    /**
     * Gets the specified challenge parameter from the challenge response.
     *
     * @param response the Http response with auth challenge
     * @param challengeScheme the challenge scheme to be checked
     * @param parameter the challenge parameter value to get
     *
     * @return the extracted value of the challenge parameter
     */
    static String getChallengeParameterFromResponse(Response<?> response, String challengeScheme, String parameter) {
        String challenge = response.getHeaders().getValue(HttpHeaderName.WWW_AUTHENTICATE);
        List<AuthenticateChallenge> authenticateChallengeList = AuthUtils.parseAuthenticateHeader(challenge);

        for (AuthenticateChallenge authChallenge : authenticateChallengeList) {
            if (authChallenge.getScheme().equals(challengeScheme)) {
                return authChallenge.getParameters().get(parameter);
            }
        }
        return null;
    }
}
