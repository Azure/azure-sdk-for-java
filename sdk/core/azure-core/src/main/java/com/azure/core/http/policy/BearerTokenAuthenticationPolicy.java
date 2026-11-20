// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.credential.AccessTokenCache;
import com.azure.core.implementation.http.policy.AuthorizationChallengeParser;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
 * <p>The Authorization header uses the {@code Bearer} scheme, except for proof-of-possession: when the
 * {@link TokenRequestContext} asks for a proof-of-possession token, or the token is bound to a TLS client certificate,
 * the policy uses the token's type as the scheme, such as {@code mtls_pop}. For a token bound to a certificate, the
 * policy also passes the certificate to the HTTP client with the request (see
 * {@link com.azure.core.util.Contexts#getClientCertificateBinding()}), so that the token is only sent over a TLS
 * connection that presents it. A token bound to a certificate isn't sent when a {@link RedirectPolicy} before this
 * policy redirects the request to another host.</p>
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.credential.TokenCredential
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
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
     * Executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     * @return A {@link Mono} containing {@link Void}
     */
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        if (this.scopes == null) {
            return Mono.empty();
        }
        return setAuthorizationHeaderHelper(context,
            new TokenRequestContext().addScopes(this.scopes).setCaeEnabled(true), false);
    }

    /**
     * Synchronously executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     */
    public void authorizeRequestSync(HttpPipelineCallContext context) {
        setAuthorizationHeaderHelperSync(context, new TokenRequestContext().addScopes(scopes).setCaeEnabled(true),
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
     * @param context The request context.
     * @param response The Http Response containing the authentication challenge header.
     * @return A {@link Mono} containing {@link TokenRequestContext}
     */
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        if (AuthorizationChallengeParser.isCaeClaimsChallenge(response)) {
            TokenRequestContext tokenRequestContext = getTokenRequestContextForCaeChallenge(response);
            if (tokenRequestContext != null) {
                return setAuthorizationHeader(context, tokenRequestContext).then(Mono.just(true));
            }
        }
        return Mono.just(false);
    }

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication challenge
     * header is received after the initial request and returns appropriate {@link TokenRequestContext} to be used for
     * re-authentication.
     * <p>
     * The default implementation will attempt to handle Continuous Access Evaluation (CAE) challenges.
     * </p>
     *
     * @param context The request context.
     * @param response The Http Response containing the authentication challenge header.
     * @return A boolean indicating if containing the {@link TokenRequestContext} for re-authentication
     */
    public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response) {
        if (AuthorizationChallengeParser.isCaeClaimsChallenge(response)) {
            TokenRequestContext tokenRequestContext = getTokenRequestContextForCaeChallenge(response);
            if (tokenRequestContext != null) {
                setAuthorizationHeaderSync(context, tokenRequestContext);
                return true;
            }
        }

        return false;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }
        HttpPipelineNextPolicy nextPolicy = next.clone();

        return authorizeRequest(context).then(Mono.defer(next::process)).flatMap(httpResponse -> {
            String authHeader = httpResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
            if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                return authorizeRequestOnChallenge(context, httpResponse).flatMap(authorized -> {
                    if (authorized) {
                        // body needs to be closed or read to the end to release the connection
                        httpResponse.close();
                        return nextPolicy.process();
                    } else {
                        return Mono.just(httpResponse);
                    }
                });
            }
            return Mono.just(httpResponse);
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }
        HttpPipelineNextSyncPolicy nextPolicy = next.clone();

        authorizeRequestSync(context);
        HttpResponse httpResponse = next.processSync();
        String authHeader = httpResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        if (httpResponse.getStatusCode() == 401 && authHeader != null) {
            if (authorizeRequestOnChallengeSync(context, httpResponse)) {
                // body needs to be closed or read to the end to release the connection
                httpResponse.close();
                return nextPolicy.processSync();
            } else {
                return httpResponse;
            }
        }
        return httpResponse;
    }

    /**
     * Authorizes the request with the access token acquired using the specified {@code tokenRequestContext}.
     * <p>
     * The class documentation describes the authorization scheme and the client certificate binding the request is
     * sent with.
     *
     * @param context the HTTP pipeline context.
     * @param tokenRequestContext the token request context to be used for token acquisition.
     * @return a {@link Mono} containing {@link Void}
     */
    public Mono<Void> setAuthorizationHeader(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        return setAuthorizationHeaderHelper(context, tokenRequestContext, true);
    }

    /**
     * Authorizes the request with the access token acquired using the specified {@code tokenRequestContext}.
     * <p>
     * The class documentation describes the authorization scheme and the client certificate binding the request is
     * sent with.
     *
     * @param context the HTTP pipeline context.
     * @param tokenRequestContext the token request context to be used for token acquisition.
     */
    public void setAuthorizationHeaderSync(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        setAuthorizationHeaderHelperSync(context, tokenRequestContext, true);
    }

    private Mono<Void> setAuthorizationHeaderHelper(HttpPipelineCallContext context,
        TokenRequestContext tokenRequestContext, boolean refreshOnContextChange) {
        return cache.getToken(tokenRequestContext, refreshOnContextChange).flatMap(token -> {
            setAuthorization(context, tokenRequestContext, token);
            return Mono.empty();
        });
    }

    private void setAuthorizationHeaderHelperSync(HttpPipelineCallContext context,
        TokenRequestContext tokenRequestContext, boolean refreshOnContextChange) {
        AccessToken token = cache.getTokenSync(tokenRequestContext, refreshOnContextChange);
        setAuthorization(context, tokenRequestContext, token);
    }

    private static void setAuthorization(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext,
        AccessToken token) {
        ClientCertificateBinding binding = token.getClientCertificateBinding();
        if (binding != null && RedirectPolicy.isRedirectedToAnotherHost(context)) {
            // A token bound to a certificate is only sent to the host it was requested for.
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            setClientCertificateBinding(context, null);
            return;
        }

        context.getHttpRequest()
            .getHeaders()
            .set(HttpHeaderName.AUTHORIZATION,
                getAuthorizationScheme(tokenRequestContext, token) + " " + token.getToken());
        setClientCertificateBinding(context, binding);
    }

    // Only proof-of-possession uses the token's type as the scheme, so other requests keep sending "Bearer" whatever
    // type a credential sets.
    private static String getAuthorizationScheme(TokenRequestContext tokenRequestContext, AccessToken token) {
        String tokenType = token.getTokenType();
        boolean proofOfPossession
            = tokenRequestContext.getProofOfPossessionOptions() != null || token.getClientCertificateBinding() != null;

        return !proofOfPossession || CoreUtils.isNullOrEmpty(tokenType) || BEARER.equalsIgnoreCase(tokenType)
            ? BEARER
            : tokenType;
    }

    /**
     * Sets the client certificate binding the request is sent with, or clears one set earlier, for example by an
     * earlier try of the request.
     *
     * @param context The request context.
     * @param binding The client certificate binding, or null to clear it.
     */
    static void setClientCertificateBinding(HttpPipelineCallContext context, ClientCertificateBinding binding) {
        if (binding != null || context.getData(HttpUtils.AZURE_CLIENT_CERTIFICATE_BINDING).isPresent()) {
            context.setData(HttpUtils.AZURE_CLIENT_CERTIFICATE_BINDING, binding);
        }
    }

    private TokenRequestContext getTokenRequestContextForCaeChallenge(HttpResponse response) {
        String decodedClaims = null;
        String encodedClaims
            = AuthorizationChallengeParser.getChallengeParameterFromResponse(response, "Bearer", "claims");

        if (!CoreUtils.isNullOrEmpty(encodedClaims)) {
            try {
                decodedClaims = new String(Base64.getDecoder().decode(encodedClaims), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                // We don't want to throw here, but we want to log this for future incident investigation.
                LOGGER.warning("Failed to decode the claims from the CAE challenge. Encoded claims: " + encodedClaims);
            }
        }

        if (decodedClaims == null) {
            return null;
        }

        return new TokenRequestContext().setClaims(decodedClaims).addScopes(scopes).setCaeEnabled(true);
    }
}
