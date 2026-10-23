// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.ValidationUtil;

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
 * <!-- src_embed com.azure.identity.credential.githubactionscredential.construct -->
 * <pre>
 * GitHubActionsCredential credential = new GitHubActionsCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.githubactionscredential.construct -->
 *
 * @see GitHubActionsCredential
 */
public class GitHubActionsCredentialBuilder extends AadCredentialBuilderBase<GitHubActionsCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(GitHubActionsCredentialBuilder.class);
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
        identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
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
        Configuration configuration = identityClientOptions.getConfiguration();
        if (configuration == null) {
            configuration = Configuration.getGlobalConfiguration();
        }
        String resolvedClientId = required("AZURE_CLIENT_ID",
            CoreUtils.isNullOrEmpty(clientId) ? configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID) : clientId);
        String resolvedTenantId = required("AZURE_TENANT_ID",
            CoreUtils.isNullOrEmpty(tenantId) ? configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID) : tenantId);
        ValidationUtil.validateTenantIdCharacterRange(resolvedTenantId, LOGGER);
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
            throw LOGGER.logExceptionAsError(new CredentialUnavailableException("GitHubActionsCredential requires "
                + String.join(", ", missing) + ". Run in a GitHub Actions workflow with "
                + "'permissions: id-token: write'. For troubleshooting information, see "
                + "https://aka.ms/azsdk/java/identity/troubleshoot."));
        }
        String resolvedAuthority = authorityHost == null
            ? configuration.get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
            : authorityHost;
        String audience = audienceForAuthority(resolvedAuthority);
        IdentityClientOptions options = identityClientOptions.clone().setAuthorityHost(resolvedAuthority);
        return new GitHubActionsCredential(resolvedClientId, resolvedTenantId,
            requestUrlWithAudience(requestUrl, audience), requestToken, options);
    }

    private static String required(String name, String value) {
        if (CoreUtils.isNullOrEmpty(value)) {
            throw LOGGER.logExceptionAsError(new CredentialUnavailableException("GitHubActionsCredential requires "
                + name + ". Configure the Azure client and tenant IDs and run in a GitHub Actions workflow with "
                + "'permissions: id-token: write'."));
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

    private static RuntimeException unsupportedAuthority(Exception cause) {
        return LOGGER.logExceptionAsError(new CredentialUnavailableException(
            "GitHubActionsCredential does not support the configured authority host. "
                + "Use an Azure Public, China, US Government, France Government, Germany Government, "
                + "or Singapore Government authority.",
            GitHubActionsCredential.sanitizedCause(cause)));
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
            return UrlBuilder.parse(requestUrl)
                .setQueryParameter("audience", URLEncoder.encode(audience, StandardCharsets.UTF_8.name()))
                .toString();
        } catch (URISyntaxException | IOException e) {
            throw invalidRequestUrl(e);
        }
    }

    private static RuntimeException invalidRequestUrl(Exception cause) {
        return LOGGER.logExceptionAsError(new CredentialUnavailableException(
            "GitHubActionsCredential requires ACTIONS_ID_TOKEN_REQUEST_URL to be an absolute HTTPS URL "
                + "without user information or a fragment.",
            GitHubActionsCredential.sanitizedCause(cause)));
    }
}
