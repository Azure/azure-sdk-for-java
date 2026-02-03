// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.PublicClientOptions;
import com.azure.v2.identity.implementation.util.IdentityConstants;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.identity.models.AuthenticationRecord;
import com.azure.v2.identity.models.BrowserCustomizationOptions;
import com.azure.v2.identity.models.TokenCachePersistenceOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link InteractiveBrowserCredential}.
 *
 * <p>Interactive browser authentication is a type of authentication flow offered by
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>
 * that enables users to sign in to applications and services using a web browser. This authentication method is
 * commonly used for web applications, where users enter their credentials directly into a web page.
 * With interactive browser authentication, the user navigates to a web application and is prompted to enter their
 * username and password credentials. The application then redirects the user to the Microsoft Entra ID sign-in page, where
 * they are prompted to enter their credentials again. After the user successfully authenticates, Microsoft Entra ID issues a
 * security token that the application can use to authorize the user's access to its resources.
 * The {@link InteractiveBrowserCredential} interactively authenticates a user and acquires a token with the default
 * system browser and offers a smooth authentication experience by letting a user use their own credentials to
 * authenticate the application. When authenticated, the oauth2 flow notifies the credential of the authentication
 * code through the reply URL. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/interactivebrowsercredential/docs">conceptual knowledge and
 * configuration details</a>.</p>
 *
 * <p><strong>Sample: Construct InteractiveBrowserCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link InteractiveBrowserCredential},
 * using the {@link InteractiveBrowserCredentialBuilder} to configure it. By default, the credential
 * targets a localhost redirect URL, to override that behaviour a
 * {@link InteractiveBrowserCredentialBuilder#redirectUrl(String)} can be optionally specified. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>

 * @see InteractiveBrowserCredential
 */
public class InteractiveBrowserCredentialBuilder
    extends EntraIdCredentialBuilderBase<InteractiveBrowserCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(InteractiveBrowserCredentialBuilder.class);
    private final PublicClientOptions publicClientOptions;

    /**
     * Constructs an instance of InteractiveBrowserCredentialBuilder.
     */
    public InteractiveBrowserCredentialBuilder() {
        super();
        publicClientOptions = new PublicClientOptions();
    }

    /**
     * Sets the client ID of the Microsoft Entra application that users will sign in to. It is recommended
     * that developers register their applications and assign appropriate roles. For more information,
     * visit this doc for <a href="https://aka.ms/identity/AppRegistrationAndRoleAssignment">app registration</a>.
     * If not specified, users will authenticate to an Azure development application, which is not recommended
     * for production scenarios.
     * @param clientId the client ID of the application.
     * @return An updated instance of this builder with the client id configured.
     */
    @Override
    public InteractiveBrowserCredentialBuilder clientId(String clientId) {
        return super.clientId(clientId);
    }

    /**
     * Sets the {@link AuthenticationRecord} captured from a previous authentication.
     *
     * @param authenticationRecord The Authentication record to be configured.
     *
     * @return An updated instance of this builder with the configured authentication record.
     */
    public InteractiveBrowserCredentialBuilder authenticationRecord(AuthenticationRecord authenticationRecord) {
        this.publicClientOptions.setAuthenticationRecord(authenticationRecord);
        return this;
    }

    /**
     * Configures the persistent shared token cache options and enables the persistent token cache which is disabled
     * by default. If configured, the credential will store tokens in a cache persisted to the machine, protected to
     * the current user, which can be shared by other credentials and processes.
     *
     * @param tokenCachePersistenceOptions the token cache configuration options
     * @return An updated instance of this builder with the token cache options configured.
     */
    public InteractiveBrowserCredentialBuilder
        tokenCachePersistenceOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
        this.publicClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Sets the Redirect URL where STS will callback the application with the security code. It is required if a custom
     * client id is specified via {@link InteractiveBrowserCredentialBuilder#clientId(String)} and must match the
     * redirect URL specified during the application registration.
     *
     * @param redirectUrl the redirect URL to listen on and receive security code.
     *
     * @return An updated instance of this builder with the configured redirect URL.
     */
    public InteractiveBrowserCredentialBuilder redirectUrl(String redirectUrl) {
        try {
            this.publicClientOptions.setRedirectUri(new URI(redirectUrl));
        } catch (URISyntaxException e) {
            throw LOGGER.throwableAtError().log(e, IllegalArgumentException::new);
        }
        return this;
    }

    /**
     * Disables the automatic authentication and prevents the {@link InteractiveBrowserCredential} from automatically
     * prompting the user. If automatic authentication is disabled a {@link AuthenticationRequiredException}
     * will be thrown from {@link InteractiveBrowserCredential#getToken(TokenRequestContext)} in the case that
     * user interaction is necessary. The application is responsible for handling this exception, and
     * calling {@link InteractiveBrowserCredential#authenticate()} or
     * {@link InteractiveBrowserCredential#authenticate(TokenRequestContext)} to authenticate the user interactively.
     *
     * @return An updated instance of this builder with automatic authentication disabled.
     */
    public InteractiveBrowserCredentialBuilder disableAutomaticAuthentication() {
        this.publicClientOptions.setAutomaticAuthentication(false);
        return this;
    }

    /**
     * Sets the username suggestion to pre-fill the login page's username/email address field. A user may still log in
     * with a different username.
     *
     * @param loginHint the username suggestion to pre-fill the login page's username/email address field.
     *
     * @return An updated instance of this builder with login hint configured.
     */
    public InteractiveBrowserCredentialBuilder loginHint(String loginHint) {
        this.publicClientOptions.setLoginHint(loginHint);
        return this;
    }

    /**
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant on which the application is installed.
     * If no value is specified for TenantId this option will have no effect, and the credential will
     * acquire tokens for any requested tenant.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    @Override
    public InteractiveBrowserCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
        publicClientOptions.setAdditionallyAllowedTenants(
            IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants)));
        return this;
    }

    /**
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant on which the application is installed.
     * If no value is specified for TenantId this option will have no effect, and the credential will
     * acquire tokens for any requested tenant.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    @Override
    public InteractiveBrowserCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        publicClientOptions
            .setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return this;
    }

    /**
     * Configures the options for customizing the browser for interactive authentication.
     * @param browserCustomizationOptions the browser customization options
     * @return An updated instance of this builder with the browser customization options configured.
     */
    public InteractiveBrowserCredentialBuilder
        browserCustomizationOptions(BrowserCustomizationOptions browserCustomizationOptions) {
        this.publicClientOptions.setBrowserCustomizationOptions(browserCustomizationOptions);
        return this;
    }

    /**
     * Creates a new {@link InteractiveBrowserCredential} with the current configurations.
     *
     * @return a {@link InteractiveBrowserCredential} with the current configurations.
     */
    public InteractiveBrowserCredential build() {
        String clientId = this.publicClientOptions.getClientId();

        publicClientOptions.setClientId(clientId != null ? clientId : IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID);
        return new InteractiveBrowserCredential(publicClientOptions);
    }

    @Override
    ClientOptions getClientOptions() {
        return publicClientOptions;
    }
}
