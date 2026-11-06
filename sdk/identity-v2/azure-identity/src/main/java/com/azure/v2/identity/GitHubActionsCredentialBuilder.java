// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import com.azure.v2.identity.models.TokenCachePersistenceOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.UriBuilder;
import io.clientcore.core.utils.configuration.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fluent builder for {@link GitHubActionsCredential}.
 *
 * <p>Client and tenant IDs can be supplied explicitly or through {@code AZURE_CLIENT_ID} and
 * {@code AZURE_TENANT_ID}. GitHub supplies {@code ACTIONS_ID_TOKEN_REQUEST_URL} and
 * {@code ACTIONS_ID_TOKEN_REQUEST_TOKEN} to workflow steps with {@code permissions: id-token: write}.</p>
 *
 * <!-- src_embed com.azure.v2.identity.credential.githubactionscredential.construct -->
 * <pre>
 * GitHubActionsCredential credential = new GitHubActionsCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.identity.credential.githubactionscredential.construct -->
 *
 * @see GitHubActionsCredential
 */
public class GitHubActionsCredentialBuilder extends EntraIdCredentialBuilderBase<GitHubActionsCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(GitHubActionsCredentialBuilder.class);
    private final ConfidentialClientOptions confidentialClientOptions = new ConfidentialClientOptions();
    private String authorityHost;

    /**
     * Creates a builder for a GitHub Actions credential.
     */
    public GitHubActionsCredentialBuilder() {
    }

    @Override
    public GitHubActionsCredentialBuilder authorityHost(String authorityHost) {
        super.authorityHost(authorityHost);
        this.authorityHost = authorityHost;
        return this;
    }

    /**
     * Enables the persistent token cache with the specified options. Persistence is disabled by default.
     *
     * @param tokenCachePersistenceOptions the persistent token cache options.
     * @return this builder.
     */
    public GitHubActionsCredentialBuilder
        tokenCachePersistenceOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
        confidentialClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Builds a credential using the configured values and the GitHub Actions environment.
     *
     * <p>An explicit client ID, tenant ID, or authority host takes precedence over its environment variable.
     * The default authority is Azure Public. The assertion audience is derived from the authority host.
     * Missing GitHub runner variables are reported together with a link to troubleshooting guidance.</p>
     *
     * @return the configured credential.
     * @throws CredentialUnavailableException if required configuration is missing, the GitHub request URL is
     * invalid, or the authority host does not have a supported token-exchange audience.
     * @throws IllegalArgumentException if the tenant ID is invalid.
     */
    public GitHubActionsCredential build() {
        Configuration configuration = confidentialClientOptions.getConfiguration();
        if (configuration == null) {
            configuration = Configuration.getGlobalConfiguration();
        }
        String clientId = required("AZURE_CLIENT_ID",
            CoreUtils.isNullOrEmpty(confidentialClientOptions.getClientId())
                ? configuration.get("AZURE_CLIENT_ID")
                : confidentialClientOptions.getClientId());
        String tenantId = required("AZURE_TENANT_ID",
            CoreUtils.isNullOrEmpty(confidentialClientOptions.getTenantId())
                ? configuration.get("AZURE_TENANT_ID")
                : confidentialClientOptions.getTenantId());
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        String requestUrl = configuration.get("ACTIONS_ID_TOKEN_REQUEST_URL");
        String requestToken = configuration.get("ACTIONS_ID_TOKEN_REQUEST_TOKEN");
        List<String> missing = new ArrayList<>(2);
        if (CoreUtils.isNullOrEmpty(requestUrl)) {
            missing.add("ACTIONS_ID_TOKEN_REQUEST_URL");
        }
        if (CoreUtils.isNullOrEmpty(requestToken)) {
            missing.add("ACTIONS_ID_TOKEN_REQUEST_TOKEN");
        }
        if (!missing.isEmpty()) {
            String missingSettings = String.join(", ", missing);
            String message
                = "GitHubActionsCredential requires " + missingSettings + ". Run in a GitHub Actions workflow with "
                    + "'permissions: id-token: write'. For troubleshooting information, see "
                    + "https://aka.ms/azsdk/java/identity/troubleshoot.";
            throw LOGGER.throwableAtError()
                .addKeyValue("setting", missingSettings)
                .log("GitHubActionsCredential configuration is incomplete.",
                    ignored -> new CredentialUnavailableException(message));
        }
        String resolvedAuthority = authorityHost == null ? configuration.get("AZURE_AUTHORITY_HOST") : authorityHost;
        if (resolvedAuthority == null) {
            resolvedAuthority = AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        }
        String audience = audienceForAuthority(resolvedAuthority);
        ConfidentialClientOptions options = new ConfidentialClientOptions(confidentialClientOptions);
        options.setClientId(clientId).setTenantId(tenantId).setAuthorityHost(resolvedAuthority);
        return new GitHubActionsCredential(requestUrlWithAudience(requestUrl, audience), requestToken, options);
    }

    @Override
    ClientOptions getClientOptions() {
        return confidentialClientOptions;
    }

    private static String required(String name, String value) {
        if (CoreUtils.isNullOrEmpty(value)) {
            String message = "GitHubActionsCredential requires " + name
                + ". Configure the Azure client and tenant IDs and run in a GitHub Actions workflow with "
                + "'permissions: id-token: write'.";
            throw LOGGER.throwableAtError()
                .addKeyValue("setting", name)
                .log("GitHubActionsCredential configuration is incomplete.",
                    ignored -> new CredentialUnavailableException(message));
        }
        return value;
    }

    private static String audienceForAuthority(String authority) {
        try {
            URI uri = new URI(authority);
            if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null) {
                switch (uri.getHost().toLowerCase(Locale.ROOT)) {
                    case "login.microsoftonline.com":
                        return "api://AzureADTokenExchange";

                    case "login.chinacloudapi.cn":
                        return "api://AzureADTokenExchangeChina";

                    case "login.microsoftonline.us":
                        return "api://AzureADTokenExchangeUSGov";

                    case "login.sovcloud-identity.fr":
                        return "api://AzureADTokenExchangeFrance";

                    case "login.sovcloud-identity.de":
                        return "api://AzureADTokenExchangeGermany";

                    case "login.sovcloud-identity.sg":
                        return "api://AzureADTokenExchangeGovSG";

                    default:
                        break;
                }
            }
        } catch (URISyntaxException e) {
            throw unsupportedAuthority(e);
        }
        throw unsupportedAuthority(null);
    }

    private static CredentialUnavailableException unsupportedAuthority(Exception cause) {
        throw LOGGER.throwableAtError()
            .log(
                "GitHubActionsCredential does not support the configured authority host. "
                    + "Use an Azure Public, China, US Government, France Government, Germany Government, "
                    + "or Singapore Government authority.",
                GitHubActionsCredential.sanitizedCause(cause), CredentialUnavailableException::new);
    }

    private static String requestUrlWithAudience(String requestUrl, String audience) {
        try {
            URI uri = new URI(requestUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null
                || uri.getFragment() != null) {
                throw invalidRequestUrl(null);
            }
            return UriBuilder.parse(requestUrl)
                .setQueryParameter("audience", URLEncoder.encode(audience, StandardCharsets.UTF_8.name()))
                .toString();
        } catch (URISyntaxException | IOException e) {
            throw invalidRequestUrl(e);
        }
    }

    private static CredentialUnavailableException invalidRequestUrl(Exception cause) {
        throw LOGGER.throwableAtError()
            .log(
                "GitHubActionsCredential requires ACTIONS_ID_TOKEN_REQUEST_URL to be an absolute HTTPS URL "
                    + "without user information or a fragment.",
                GitHubActionsCredential.sanitizedCause(cause), CredentialUnavailableException::new);
    }
}
