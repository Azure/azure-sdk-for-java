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
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.LoggingUtil;
import com.azure.identity.implementation.util.ValidationUtil;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Enables authentication to Microsoft Entra ID using GitHub Actions OIDC federated identity credentials.
 *
 * <p>This credential is designed for use in GitHub Actions workflows that have
 * {@code permissions: id-token: write} configured. It reads the following environment variables:</p>
 * <ul>
 *   <li>{@code AZURE_TENANT_ID} — The Microsoft Entra tenant ID.</li>
 *   <li>{@code AZURE_CLIENT_ID} — The client ID of the app registration with a federated identity credential.</li>
 *   <li>{@code ACTIONS_ID_TOKEN_REQUEST_URL} — Set automatically by the GitHub Actions runner.</li>
 *   <li>{@code ACTIONS_ID_TOKEN_REQUEST_TOKEN} — Set automatically by the GitHub Actions runner.</li>
 * </ul>
 *
 * <p>To construct an instance of this credential, use the {@link GitHubActionsCredentialBuilder}:</p>
 * <!-- src_embed com.azure.identity.credential.githubactionscredential.construct -->
 * <pre>
 * TokenCredential credential = new GitHubActionsCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.githubactionscredential.construct -->
 *
 * @see GitHubActionsCredentialBuilder
 */
@Immutable
public final class GitHubActionsCredential implements TokenCredential {
    static final String ACTIONS_ID_TOKEN_REQUEST_URL = "ACTIONS_ID_TOKEN_REQUEST_URL";
    static final String ACTIONS_ID_TOKEN_REQUEST_TOKEN = "ACTIONS_ID_TOKEN_REQUEST_TOKEN";
    private static final String TROUBLESHOOTING_GUIDE = "https://aka.ms/azsdk/java/identity/troubleshoot";

    private static final ClientLogger LOGGER = new ClientLogger(GitHubActionsCredential.class);

    private static final Map<String, String> AUDIENCE_BY_AUTHORITY_HOST;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("login.microsoftonline.com", "api://AzureADTokenExchange");
        map.put("login.chinacloudapi.cn", "api://AzureADTokenExchangeChina");
        map.put("login.microsoftonline.us", "api://AzureADTokenExchangeUSGov");
        map.put("login.sovcloud-identity.fr", "api://AzureADTokenExchangeFrance");
        map.put("login.sovcloud-identity.de", "api://AzureADTokenExchangeGermany");
        map.put("login.sovcloud-identity.sg", "api://AzureADTokenExchangeGovSG");
        AUDIENCE_BY_AUTHORITY_HOST = Collections.unmodifiableMap(map);
    }

    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    /**
     * Creates an instance of {@link GitHubActionsCredential}.
     *
     * @param clientId the client id of the service principal
     * @param tenantId the tenant id of the service principal
     * @param oidcRequestUrl the GitHub Actions OIDC request URL
     * @param oidcRequestToken the GitHub Actions OIDC request token
     * @param identityClientOptions the options for configuring the identity client
     * @throws CredentialUnavailableException if required configuration or environment variables are missing
     */
    GitHubActionsCredential(String clientId, String tenantId, String oidcRequestUrl, String oidcRequestToken,
        IdentityClientOptions identityClientOptions) {
        if (identityClientOptions == null) {
            identityClientOptions = new IdentityClientOptions();
        }

        if (CoreUtils.isNullOrEmpty(tenantId)) {
            throw LOGGER.logExceptionAsError(new CredentialUnavailableException(
                "GitHubActionsCredential: is unavailable. Set the AZURE_TENANT_ID environment variable to use this credential."));
        }
        if (CoreUtils.isNullOrEmpty(clientId)) {
            throw LOGGER.logExceptionAsError(new CredentialUnavailableException(
                "GitHubActionsCredential: is unavailable. Set the AZURE_CLIENT_ID environment variable to use this credential."));
        }
        if (CoreUtils.isNullOrEmpty(oidcRequestUrl) || CoreUtils.isNullOrEmpty(oidcRequestToken)) {
            List<String> missing = new ArrayList<>(2);
            if (CoreUtils.isNullOrEmpty(oidcRequestUrl)) {
                missing.add(ACTIONS_ID_TOKEN_REQUEST_URL);
            }
            if (CoreUtils.isNullOrEmpty(oidcRequestToken)) {
                missing.add(ACTIONS_ID_TOKEN_REQUEST_TOKEN);
            }
            throw LOGGER.logExceptionAsError(new CredentialUnavailableException(
                "GitHubActionsCredential: is unavailable. Ensure that you're running this task in a GitHub Actions workflow with 'permissions: id-token: write' so that the following missing system variable(s) can be defined: "
                    + String.join(", ", missing) + ". See the troubleshooting guide for more information: "
                    + TROUBLESHOOTING_GUIDE));
        }

        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);

        String authorityHost = identityClientOptions.getAuthorityHost();
        String audience = deriveAudience(authorityHost);

        IdentityClientBuilder builder = new IdentityClientBuilder().tenantId(tenantId)
            .clientId(clientId)
            .identityClientOptions(identityClientOptions)
            .clientAssertionSupplierWithHttpPipeline(httpPipeline -> {
                try {
                    UrlBuilder urlBuilder = UrlBuilder.parse(oidcRequestUrl);
                    urlBuilder.setQueryParameter("audience",
                        URLEncoder.encode(audience, StandardCharsets.UTF_8.name()));
                    HttpRequest request = new HttpRequest(HttpMethod.GET, urlBuilder.toUrl());
                    request.setHeader(HttpHeaderName.AUTHORIZATION, "Bearer " + oidcRequestToken);

                    try (HttpResponse response = httpPipeline.sendSync(request, Context.NONE)) {
                        return handleOidcResponse(response, LOGGER);
                    }
                } catch (ClientAuthenticationException e) {
                    throw e;
                } catch (Exception e) {
                    throw LOGGER.logExceptionAsError(
                        new ClientAuthenticationException("Failed to get the client assertion token", null, e));
                }
            });

        this.identitySyncClient = builder.buildSyncClient();
        this.identityClient = builder.build();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithConfidentialClientCache(request)
            .onErrorResume(t -> Mono.empty())
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithConfidentialClient(request)))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(
                error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, error));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClientCache(request);
            if (token != null) {
                LoggingUtil.logTokenSuccess(LOGGER, request);
                return token;
            }
        } catch (Exception e) {
            IdentityUtil.rethrowIfShutdownSignal(e);
        }

        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClient(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            // A cancellation was logged at verbose level above and must not also be logged as an error.
            IdentityUtil.rethrowIfShutdownSignal(e);
            // wrap the exception in a RuntimeException to avoid checked exception problems.
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Derives the OIDC audience from the authority host for sovereign cloud support.
     *
     * @param authorityHost the authority host
     * @return the OIDC exchange audience
     */
    static String deriveAudience(String authorityHost) {
        if (CoreUtils.isNullOrEmpty(authorityHost)) {
            throw LOGGER.logExceptionAsError(
                new CredentialUnavailableException("GitHubActionsCredential: is unavailable. The authority host \""
                    + authorityHost + "\" is not supported."));
        }
        String host = null;
        try {
            URI uri = new URI(authorityHost);
            host = uri.getHost();
            if (host == null) {
                URL url = new URL(authorityHost);
                host = url.getHost();
            }
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(
                new CredentialUnavailableException("GitHubActionsCredential: is unavailable. The authority host \""
                    + authorityHost + "\" is not supported.", e));
        }

        if (host == null) {
            throw LOGGER.logExceptionAsError(
                new CredentialUnavailableException("GitHubActionsCredential: is unavailable. The authority host \""
                    + authorityHost + "\" is not supported."));
        }

        String audience = AUDIENCE_BY_AUTHORITY_HOST.get(host.toLowerCase(Locale.ROOT));
        if (audience == null) {
            throw LOGGER.logExceptionAsError(
                new CredentialUnavailableException("GitHubActionsCredential: is unavailable. The authority host \""
                    + authorityHost + "\" is not supported."));
        }
        return audience;
    }

    /**
     * Parses the OIDC token response from GitHub's OIDC provider.
     *
     * @param response the HTTP response from GitHub's OIDC provider
     * @param logger the logger to log errors
     * @return the OIDC token JWT string
     */
    static String handleOidcResponse(HttpResponse response, ClientLogger logger) {
        BinaryData bodyBinaryData = response != null ? response.getBodyAsBinaryData() : null;
        String responseBody = (bodyBinaryData != null) ? bodyBinaryData.toString() : null;
        if (CoreUtils.isNullOrEmpty(responseBody)) {
            int statusCode = response != null ? response.getStatusCode() : 0;
            String message
                = "GitHubActionsCredential: Authentication Failed. Received null token from OIDC request. Status code: "
                    + statusCode + ". See the troubleshooting guide for more information: " + TROUBLESHOOTING_GUIDE;
            throw logger.logExceptionAsError(new ClientAuthenticationException(message, response));
        }

        if (response.getStatusCode() != 200) {
            String message = "GitHubActionsCredential: Authentication Failed. OIDC request returned status code "
                + response.getStatusCode() + ". See the troubleshooting guide for more information: "
                + TROUBLESHOOTING_GUIDE;
            throw logger.logExceptionAsError(new ClientAuthenticationException(message, response));
        }

        String tokenValue = null;
        try (JsonReader reader = JsonProviders.createReader(responseBody)) {
            if (reader.currentToken() == null) {
                reader.nextToken();
            }
            if (reader.currentToken() != JsonToken.START_OBJECT) {
                String message = "GitHubActionsCredential: Authentication Failed. Failed to parse OIDC response."
                    + " See the troubleshooting guide for more information: " + TROUBLESHOOTING_GUIDE;
                throw logger.logExceptionAsError(new ClientAuthenticationException(message, response));
            }
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("value".equals(fieldName)) {
                    tokenValue = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
        } catch (ClientAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            String message = "GitHubActionsCredential: Authentication Failed. Failed to parse OIDC response."
                + " See the troubleshooting guide for more information: " + TROUBLESHOOTING_GUIDE;
            throw logger.logExceptionAsError(new ClientAuthenticationException(message, response, e));
        }

        if (CoreUtils.isNullOrEmpty(tokenValue)) {
            String message
                = "GitHubActionsCredential: Authentication Failed. \"value\" field not detected in the response."
                    + " See the troubleshooting guide for more information: " + TROUBLESHOOTING_GUIDE;
            throw logger.logExceptionAsError(new ClientAuthenticationException(message, response));
        }

        return tokenValue;
    }
}
