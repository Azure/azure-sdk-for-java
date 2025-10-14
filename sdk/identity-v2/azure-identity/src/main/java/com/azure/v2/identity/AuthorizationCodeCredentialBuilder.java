// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.PublicClientOptions;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * <p>Fluent credential builder for instantiating a {@link AuthorizationCodeCredential}.</p>
 *
 * <p>Authorization Code authentication in Azure is a type of authentication mechanism that allows users to
 * authenticate with <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>
 * and obtain an authorization code that can be used to request an access token to access
 * Azure resources. It is a widely used authentication mechanism and is supported by a wide range of Azure services
 * and applications. It provides a secure and scalable way to authenticate users and grant them access to Azure
 * resources.
 * The AuthorizationCodeCredential authenticates a user or an application and acquires a token with the configured
 * authorization code and the redirectURL where authorization code was received.</p>
 *
 * <p><strong>Sample: Construct AuthorizationCodeCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link AuthorizationCodeCredential},
 * using the {@link AuthorizationCodeCredentialBuilder} to configure it.
 * The {@code authorizationCode}, {@code redirectUrl} and {@code clientId} are required to be configured to create
 * {@link AuthorizationCodeCredential}. Once this credential is created, it may be passed into the builder of many of
 * the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential authorizationCodeCredential = new AuthorizationCodeCredentialBuilder&#40;&#41;.authorizationCode&#40;
 *         &quot;&#123;authorization-code-received-at-redirectURL&#125;&quot;&#41;
 *     .redirectUrl&#40;&quot;&#123;redirectUrl-where-authorization-code-is-received&#125;&quot;&#41;
 *     .clientId&#40;&quot;&#123;clientId-of-application-being-authenticated&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see AuthorizationCodeCredential
 */
public class AuthorizationCodeCredentialBuilder
    extends EntraIdCredentialBuilderBase<AuthorizationCodeCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(AuthorizationCodeCredentialBuilder.class);
    private static final String CLASS_NAME = AuthorizationCodeCredentialBuilder.class.getSimpleName();
    private String clientSecret;
    private final PublicClientOptions publicClientOptions;

    /**
     * Constructs an instance of AuthorizationCodeCredentialBuilder.
     */
    public AuthorizationCodeCredentialBuilder() {
        super();
        publicClientOptions = new PublicClientOptions();
    }

    /**
     * Sets the authorization code on the builder.
     *
     * @param authCode the authorization code acquired from user login
     * @return the AuthorizationCodeCredentialBuilder itself
     */
    public AuthorizationCodeCredentialBuilder authorizationCode(String authCode) {
        this.publicClientOptions.setAuthCode(authCode);
        return this;
    }

    /**
     * Sets redirect URL for the OAuth 2.0 login request, which must be
     * registered as a valid redirect URL on the application. The authorization code
     * will be sent to this URL, so it must be listening on this server and is able
     * to complete the {@link AuthorizationCodeCredential} construction from there.
     * This is also called Reply URLs in some contexts.
     *
     * @param redirectUrl the redirect URL to send the authorization code
     * @return the AuthorizationCodeCredentialBuilder itself
     */
    public AuthorizationCodeCredentialBuilder redirectUrl(String redirectUrl) {
        try {
            this.publicClientOptions.setRedirectUri(new URI(redirectUrl));
        } catch (URISyntaxException e) {
            throw LOGGER.throwableAtError().log(e, IllegalArgumentException::new);
        }
        return this;
    }

    /**
     * <p>Sets the client secret for the authentication. This is required for Microsoft Entra web apps.</p>
     *
     * <p>Do not set this for Microsoft Entra native apps.</p>
     *
     * @param clientSecret the secret value of the Microsoft Entra application.
     * @return An updated instance of this builder.
     */
    public AuthorizationCodeCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Creates a new {@link AuthorizationCodeCredential} with the current configurations.
     *
     * @return a {@link AuthorizationCodeCredential} with the current configurations.
     */
    public AuthorizationCodeCredential build() {
        ValidationUtil.validate(CLASS_NAME, LOGGER, "clientId", publicClientOptions.getClientId(), "authorizationCode",
            publicClientOptions.getAuthCode(), "redirectUrl", publicClientOptions.getRedirectUri());

        return new AuthorizationCodeCredential(clientSecret, publicClientOptions);
    }

    @Override
    ClientOptions getClientOptions() {
        return publicClientOptions;
    }
}
