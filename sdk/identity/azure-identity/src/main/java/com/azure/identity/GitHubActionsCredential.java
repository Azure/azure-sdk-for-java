// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.LoggingUtil;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

/**
 * Authenticates an application in GitHub Actions using an OIDC federated identity credential.
 *
 * <p>Configure a federated identity credential on the Microsoft Entra application that trusts the GitHub workflow's
 * issuer, subject, and audience. The workflow needs {@code permissions: id-token: write}. GitHub supplies the
 * OIDC request URL and bearer token. The client and tenant IDs default to {@code AZURE_CLIENT_ID} and
 * {@code AZURE_TENANT_ID}; they can also be set on {@link GitHubActionsCredentialBuilder}.</p>
 *
 * <p>The assertion audience is selected from the authority host. This credential supports Azure Public, China,
 * US Government, France Government, Germany Government, and Singapore Government. It is not included in
 * {@link DefaultAzureCredential}.</p>
 *
 * <!-- src_embed com.azure.identity.credential.githubactionscredential.construct -->
 * <pre>
 * GitHubActionsCredential credential = new GitHubActionsCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.githubactionscredential.construct -->
 *
 * @see GitHubActionsCredentialBuilder
 */
@Immutable
public class GitHubActionsCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(GitHubActionsCredential.class);
    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    GitHubActionsCredential(String clientId, String tenantId, String requestUrl, String requestToken,
        IdentityClientOptions options) {
        IdentityClientBuilder builder = new IdentityClientBuilder().clientId(clientId)
            .tenantId(tenantId)
            .identityClientOptions(options)
            .clientAssertionSupplierWithHttpPipeline(pipeline -> {
                HttpRequest request = new HttpRequest(HttpMethod.GET, requestUrl)
                    .setHeader(HttpHeaderName.AUTHORIZATION, "Bearer " + requestToken);
                try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
                    if (response.getStatusCode() != 200) {
                        throw authenticationError(
                            "GitHub OIDC request returned status code " + response.getStatusCode() + ".", response);
                    }
                    return readAssertion(response);
                }
            });
        identityClient = builder.build();
        identitySyncClient = builder.buildSyncClient();
    }

    /**
     * Asynchronously acquires an access token, using the token cache when possible.
     *
     * <p>GitHub OIDC failures can be retried without rebuilding the credential. Requests with a Continuous Access
     * Evaluation (CAE) claims challenge acquire a new token instead of reusing a cached token.</p>
     *
     * @param request the scopes, tenant, and authentication options for the token request.
     * @return a publisher containing the access token, or an authentication error.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        // The confidential-client assertion callback performs synchronous HTTP I/O.
        return Mono.defer(() -> identityClient.authenticateWithConfidentialClientCache(request)
            .onErrorResume(Exception.class, error -> {
                IdentityUtil.rethrowIfShutdownSignal(error);
                return Mono.empty();
            })
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithConfidentialClient(request))))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(
                error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, error));
    }

    /**
     * Synchronously acquires an access token, using the token cache when possible.
     *
     * <p>Requests with a Continuous Access Evaluation (CAE) claims challenge acquire a new token instead of reusing a
     * cached token.</p>
     *
     * @param request the scopes, tenant, and authentication options for the token request.
     * @return the access token.
     * @throws ClientAuthenticationException if the GitHub OIDC request fails or returns an invalid assertion response.
     */
    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClientCache(request);
            if (token != null) {
                LoggingUtil.logTokenSuccess(LOGGER, request);
                return token;
            }
        } catch (RuntimeException e) {
            IdentityUtil.rethrowIfShutdownSignal(e);
        }
        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClient(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (RuntimeException e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            IdentityUtil.rethrowIfShutdownSignal(e);
            throw LOGGER.logExceptionAsError(e);
        }
    }

    private static String readAssertion(HttpResponse response) {
        try (JsonReader reader = JsonProviders.createReader(response.getBodyAsBinaryData().toString())) {
            String assertion = reader.readObject(objectReader -> {
                String value = null;
                while (objectReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = objectReader.getFieldName();
                    objectReader.nextToken();
                    if ("value".equals(fieldName)) {
                        if (objectReader.currentToken() != JsonToken.STRING) {
                            throw authenticationError("GitHub OIDC response must contain a string 'value'.", response);
                        }
                        value = objectReader.getString();
                    } else {
                        objectReader.skipChildren();
                    }
                }
                return value;
            });
            if (CoreUtils.isNullOrEmpty(assertion) || reader.nextToken() != JsonToken.END_DOCUMENT) {
                throw authenticationError("GitHub OIDC response must contain a nonempty string 'value'.", response);
            }
            return assertion;
        } catch (IOException | IllegalStateException e) {
            throw LOGGER.logExceptionAsError(new ClientAuthenticationException(
                "GitHubActionsCredential authentication failed. Invalid GitHub OIDC response.", response,
                sanitizedCause(e)));
        }
    }

    // Parser and URI exception messages can contain secrets. Retain their type and stack trace, not their input.
    static RuntimeException sanitizedCause(Exception exception) {
        if (exception == null) {
            return null;
        }
        RuntimeException sanitized
            = new IllegalArgumentException(exception.getClass().getSimpleName() + " (input redacted)");
        sanitized.setStackTrace(exception.getStackTrace());
        return sanitized;
    }

    private static RuntimeException authenticationError(String message, HttpResponse response) {
        return LOGGER.logExceptionAsError(
            new ClientAuthenticationException("GitHubActionsCredential authentication failed. " + message, response));
    }
}
