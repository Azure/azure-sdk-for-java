// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.credentials.oauth.OAuthTokenCredential;
import io.clientcore.core.credentials.oauth.OAuthTokenRequestContext;
import io.clientcore.core.http.models.AuthMetadata;
import io.clientcore.core.http.models.AuthScheme;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Metadata(properties = MetadataProperties.IMMUTABLE)
public class OAuthBearerTokenAuthenticationPolicy extends HttpCredentialPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(OAuthBearerTokenAuthenticationPolicy.class);
    private static final String BEARER = "Bearer";

    // The default context contains all OAuth metadata specified in the tsp.
    private final OAuthTokenRequestContext context;
    private final OAuthTokenCredential credential;

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request.
     * @param context the default OAuth metadata to use for the token request.
     */
    public OAuthBearerTokenAuthenticationPolicy(OAuthTokenCredential credential, OAuthTokenRequestContext context) {
        Objects.requireNonNull(context);
        this.credential = credential;
        this.context = context;
    }

    /**
     * Executed before sending the initial request and authenticates the request.
     *
     * @param httpRequest The request context.
     * @param context the OAuth metadata to use for the token request.
     */
    public void authorizeRequest(HttpRequest httpRequest, OAuthTokenRequestContext context) {
        // Credential implementations are responsible for knowing what to do with the OAuth metadata.
        AccessToken token = credential.getToken(context);
        httpRequest.getHeaders().set(HttpHeaderName.AUTHORIZATION, BEARER + " " + token.getToken());
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException If the request is not using {@code HTTPS}.
     */
    @Override
    public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        if (!"https".equals(httpRequest.getUri().getScheme())) {
            throw LOGGER.throwableAtError()
                .log("Token credentials require a URL using the HTTPS protocol scheme", IllegalStateException::new);
        }

        HttpPipelineNextPolicy nextPolicy = next.copy();

        AuthMetadata authMetadata = (AuthMetadata) httpRequest.getContext().getMetadata(IO_CLIENTCORE_AUTH_METADATA);

        OAuthTokenRequestContext tokenRequestContext = context;
        if (authMetadata != null) {
            List<AuthScheme> authSchemes = authMetadata.getAuthSchemes();
            if (CoreUtils.isNullOrEmpty(authSchemes) || authSchemes.contains(AuthScheme.NO_AUTH)) {
                return next.process();
            } else {
                tokenRequestContext = mergeTokenRequestContext(authMetadata.getOAuthTokenRequestContext());
            }
        }

        authorizeRequest(httpRequest, mergeTokenRequestContext(tokenRequestContext));

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

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication challenge
     * header is received after the initial request and returns appropriate {@link OAuthTokenRequestContext} to be
     * used for re-authentication.
     *
     * <p>
     * The default implementation doesn't handle challenges. You can override and your implementation as needed.
     * </p>
     *
     * @param httpRequest The http request.
     * @param response The Http Response containing the authentication challenge header.
     * @return A boolean indicating if the request was authorized again via re-authentication
     */
    public boolean authorizeRequestOnChallenge(HttpRequest httpRequest, Response<BinaryData> response) {
        return false;
    }

    /**
     * Helper method to have per operation TRC override service level TRC.
     *
     * @param oAuthTokenRequestContext the incoming TRC
     * @return the merged TRC to use.
     */
    private OAuthTokenRequestContext mergeTokenRequestContext(OAuthTokenRequestContext oAuthTokenRequestContext) {
        if (oAuthTokenRequestContext != null) {
            // Merge scopes: Use incoming scopes if non-empty, else fallback to context's
            List<String> mergedScopes = CoreUtils.isNullOrEmpty(oAuthTokenRequestContext.getScopes())
                ? context.getScopes()
                : oAuthTokenRequestContext.getScopes();

            // Merge params: incoming overrides context's
            Map<String, Object> mergedParams = new HashMap<>();
            if (!CoreUtils.isNullOrEmpty(context.getParams())) {
                mergedParams.putAll(context.getParams());
            }
            if (!CoreUtils.isNullOrEmpty(oAuthTokenRequestContext.getParams())) {
                mergedParams.putAll(oAuthTokenRequestContext.getParams()); // incoming overrides existing
            }

            return new OAuthTokenRequestContext().setScopes(mergedScopes).setParams(mergedParams);
        }
        return context;
    }
}
