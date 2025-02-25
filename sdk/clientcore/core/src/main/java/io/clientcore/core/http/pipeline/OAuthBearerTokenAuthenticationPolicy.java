// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.credentials.oauth.OAuthTokenCredential;
import io.clientcore.core.credentials.oauth.OAuthTokenRequestOptions;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.IOException;
import java.util.Objects;

/**
 * <p>The {@code OAuthBearerTokenAuthenticationPolicy} class is an implementation of the
 * {@link HttpCredentialPolicy}. This policy uses a {@link io.clientcore.core.credentials.oauth.OAuthTokenCredential}
 * to authenticate the request with a bearer token.</p>
 *
 * <p>This class is useful when you need to authorize requests with a bearer token. It ensures that the
 * requests are sent over HTTPS to prevent the token from being leaked.</p>
 *
 * @see io.clientcore.core.credentials.oauth.OAuthTokenCredential
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public class OAuthBearerTokenAuthenticationPolicy extends HttpCredentialPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(OAuthBearerTokenAuthenticationPolicy.class);
    private static final String BEARER = "Bearer";

    private final String[] scopes;
    private final OAuthTokenCredential credential;

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes of authentication the credential should get token for
     */
    public OAuthBearerTokenAuthenticationPolicy(OAuthTokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        this.credential = credential;
        this.scopes = scopes;
    }

    /**
     * Executed before sending the initial request and authenticates the request.
     *
     * @param httpRequest The request context.
     */
    public void authorizeRequest(HttpRequest httpRequest) {
        setAuthorizationHeaderHelper(httpRequest, new OAuthTokenRequestOptions().addScopes(scopes), false);
    }

    /**
     * Authorizes the request with the bearer token acquired using the specified {@code tokenRequestContext}
     *
     * @param request the HTTP request.
     * @param tokenRequestContext the token request context to be used for token acquisition.
     */
    protected void setAuthorizationHeader(HttpRequest request, OAuthTokenRequestOptions tokenRequestContext) {
        setAuthorizationHeaderHelper(request, tokenRequestContext, true);
    }

    private void setAuthorizationHeaderHelper(HttpRequest httpRequest, OAuthTokenRequestOptions tokenRequestContext,
        boolean checkToForceFetchToken) {
        AccessToken token = credential.getToken(tokenRequestContext);
        setAuthorizationHeader(httpRequest.getHeaders(), token.getToken());
    }

    private static void setAuthorizationHeader(HttpHeaders headers, String token) {
        headers.set(HttpHeaderName.AUTHORIZATION, BEARER + " " + token);
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        if (!"https".equals(httpRequest.getUri().getScheme())) {
            throw LOGGER.logThrowableAsError(
                new RuntimeException("Token credentials require a URL using the HTTPS protocol scheme"));
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

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication challenge
     * header is received after the initial request and returns appropriate {@link OAuthTokenRequestOptions} to be
     * used for re-authentication.
     *
     * <p>
     * The default implementation doesn't handle challenges. You can override and your implementation as needed.
     * </p>
     *
     * @param httpRequest The http request.
     * @param response The Http Response containing the authentication challenge header.
     * @return A boolean indicating if containing the {@link OAuthTokenRequestOptions} for re-authentication
     */
    public boolean authorizeRequestOnChallenge(HttpRequest httpRequest, Response<?> response) {
        return false;
    }
}
