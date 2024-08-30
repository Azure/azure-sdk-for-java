// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.ValidationUtil;

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
 *
 * <!-- src_embed com.azure.identity.credential.interactivebrowsercredential.construct -->
 * <pre>
 * TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;
 *     .redirectUrl&#40;&quot;http:&#47;&#47;localhost:8765&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.interactivebrowsercredential.construct -->
 *
 * @see InteractiveBrowserCredential
 */
public class InteractiveBrowserCredentialBuilder extends AadCredentialBuilderBase<InteractiveBrowserCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(InteractiveBrowserCredentialBuilder.class);

    private Integer port;
    private boolean automaticAuthentication = true;
    private String redirectUrl;
    private String loginHint;

    /**
     * Constructs an instance of InteractiveBrowserCredentialBuilder.
     */
    public InteractiveBrowserCredentialBuilder() {
        super();
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
     * Sets the port for the local HTTP server, for which {@code http://localhost:{port}} must be
     * registered as a valid reply URL on the application.
     *
     * @deprecated Configure the redirect URL as {@code http://localhost:{port}} via
     * {@link InteractiveBrowserCredentialBuilder#redirectUrl(String)} instead.
     *
     * @param port the port on which the credential will listen for the browser authentication result
     * @return An updated instance of this builder with the port configured.
     */
    @Deprecated
    public InteractiveBrowserCredentialBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of this builder.
     */
    InteractiveBrowserCredentialBuilder allowUnencryptedCache() {
        this.identityClientOptions.setAllowUnencryptedCache(true);
        return this;
    }

    /**
     * Enables the shared token cache which is disabled by default. If enabled, the credential will store tokens
     * in a cache persisted to the machine, protected to the current user, which can be shared by other credentials
     * and processes.
     *
     * @return An updated instance of this builder with if the shared token cache enabled specified.
     */
    InteractiveBrowserCredentialBuilder enablePersistentCache() {
        this.identityClientOptions.enablePersistentCache();
        return this;
    }


    /**
     * Sets the {@link AuthenticationRecord} captured from a previous authentication.
     *
     * @param authenticationRecord The Authentication record to be configured.
     *
     * @return An updated instance of this builder with the configured authentication record.
     */
    public InteractiveBrowserCredentialBuilder authenticationRecord(AuthenticationRecord authenticationRecord) {
        this.identityClientOptions.setAuthenticationRecord(authenticationRecord);
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
    public InteractiveBrowserCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
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
        this.redirectUrl = redirectUrl;
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
        this.automaticAuthentication = false;
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
        this.loginHint = loginHint;
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
        identityClientOptions
            .setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants)));
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
        identityClientOptions.setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return this;
    }

    /**
     * Configures the options for customizing the browser for interactive authentication.
     * @param browserCustomizationOptions the browser customization options
     * @return An updated instance of this builder with the browser customization options configured.
     */
    public InteractiveBrowserCredentialBuilder browserCustomizationOptions(BrowserCustomizationOptions browserCustomizationOptions) {
        this.identityClientOptions.setBrowserCustomizationOptions(browserCustomizationOptions);
        return this;
    }

    /**
     * Creates a new {@link InteractiveBrowserCredential} with the current configurations.
     *
     * @return a {@link InteractiveBrowserCredential} with the current configurations.
     */
    public InteractiveBrowserCredential build() {
        ValidationUtil.validateInteractiveBrowserRedirectUrlSetup(port, redirectUrl, LOGGER);

        String clientId = this.clientId != null ? this.clientId : IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID;
        return new InteractiveBrowserCredential(clientId, tenantId, port, redirectUrl, automaticAuthentication,
            loginHint, identityClientOptions);
    }
}
