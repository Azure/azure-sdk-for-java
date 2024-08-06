// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.IdentityUtil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent credential builder for instantiating a {@link DeviceCodeCredential}.
 *
 * <p>Device code authentication is a type of authentication flow offered by
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> that
 * allows users to sign in to applications on devices that don't have a web browser or a keyboard.
 * This authentication method is particularly useful for devices such as smart TVs, gaming consoles, and
 * Internet of Things (IoT) devices that may not have the capability to enter a username and password.
 * With device code authentication, the user is presented with a device code on the device that needs to be
 * authenticated. The user then navigates to a web browser on a separate device and enters the code on the
 * Microsoft sign-in page. After the user enters the code, Microsoft Entra ID verifies it and prompts the user to sign in
 * with their credentials, such as a username and password or a multi-factor authentication (MFA) method.
 * Device code authentication can be initiated using various Microsoft Entra-supported protocols, such as OAuth 2.0 and
 * OpenID Connect, and it can be used with a wide range of Microsoft Entra-integrated applications.
 * The DeviceCodeCredential interactively authenticates a user and acquires a token on devices with limited UI.
 * It works by prompting the user to visit a login URL on a browser-enabled machine when the application attempts to
 * authenticate. The user then enters the device code mentioned in the instructions along with their login credentials.
 * Upon successful authentication, the application that requested authentication gets authenticated successfully on the
 * device it's running on. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/devicecodecredential/docs">conceptual knowledge and configuration
 * details</a>.</p>
 *
 * <p>These steps will let the application authenticate, but it still won't have permission to log you into
 * Active Directory, or access resources on your behalf. To address this issue, navigate to API Permissions, and enable
 * Microsoft Graph and the resources you want to access, such as Azure Service Management, Key Vault, and so on.
 * You also need to be the admin of your tenant to grant consent to your application when you log in for the first time.
 * If you can't configure the device code flow option on your Active Directory, then it may require your app to
 * be multi- tenant. To make your app multi-tenant, navigate to the Authentication panel, then select Accounts in
 * any organizational directory. Then, select yes for Treat application as Public Client.</p>
 *
 * <p><strong>Sample: Construct DeviceCodeCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.DeviceCodeCredential},
 * using the {@link com.azure.identity.DeviceCodeCredentialBuilder} to configure it. By default, the credential
 * prints the device code challenge on the command line, to override that behaviours a {@code challengeConsumer}
 * can be optionally specified on the {@link com.azure.identity.DeviceCodeCredentialBuilder}. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.devicecodecredential.construct -->
 * <pre>
 * TokenCredential deviceCodeCredential = new DeviceCodeCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.devicecodecredential.construct -->
 *
 * @see DeviceCodeCredential
 */
public class DeviceCodeCredentialBuilder extends AadCredentialBuilderBase<DeviceCodeCredentialBuilder> {
    private Consumer<DeviceCodeInfo> challengeConsumer =
        deviceCodeInfo -> System.out.println(deviceCodeInfo.getMessage());

    private boolean automaticAuthentication = true;

    /**
     * Constructs an instance of DeviceCodeCredentialBuilder.
     */
    public DeviceCodeCredentialBuilder() {
        super();
    }

    /**
     * Sets the client ID of the Microsoft Entra application that users will sign in to. It is recommended
     * that developers register their applications and assign appropriate roles. For more information,
     * visit this doc for <a href="https://aka.ms/identity/AppRegistrationAndRoleAssignment">app registration</a>.
     * If not specified, users will authenticate to an Azure development application, which is not recommended
     * for production scenarios.
     *
     * @param clientId the client ID of the application.
     * @return An updated instance of this builder with the client ID configured.
     */
    @Override
    public DeviceCodeCredentialBuilder clientId(String clientId) {
        return super.clientId(clientId);
    }

    /**
     * Sets the consumer to meet the device code challenge. If not specified a default consumer is used which prints
     * the device code info message to stdout.
     *
     * @param challengeConsumer A method allowing the user to meet the device code challenge.
     * @return An updated instance of this builder with the challenge consumer configured.
     */
    public DeviceCodeCredentialBuilder challengeConsumer(
        Consumer<DeviceCodeInfo> challengeConsumer) {
        this.challengeConsumer = challengeConsumer;
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
    public DeviceCodeCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Sets the {@link AuthenticationRecord} captured from a previous authentication.
     *
     * @param authenticationRecord the authentication record to be configured.
     *
     * @return An updated instance of this builder with the configured authentication record.
     */
    public DeviceCodeCredentialBuilder authenticationRecord(AuthenticationRecord authenticationRecord) {
        this.identityClientOptions.setAuthenticationRecord(authenticationRecord);
        return this;
    }

    /**
     * Disables the automatic authentication and prevents the {@link DeviceCodeCredential} from automatically
     * prompting the user. If automatic authentication is disabled a {@link AuthenticationRequiredException}
     * will be thrown from {@link DeviceCodeCredential#getToken(TokenRequestContext)} in the case that
     * user interaction is necessary. The application is responsible for handling this exception, and
     * calling {@link DeviceCodeCredential#authenticate()} or
     * {@link DeviceCodeCredential#authenticate(TokenRequestContext)} to authenticate the user interactively.
     *
     * @return An updated instance of this builder with automatic authentication disabled.
     */
    public DeviceCodeCredentialBuilder disableAutomaticAuthentication() {
        this.automaticAuthentication = false;
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
    public DeviceCodeCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
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
    public DeviceCodeCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        identityClientOptions.setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return this;
    }

    /**
     * Creates a new {@link DeviceCodeCredential} with the current configurations.
     *
     * @return a {@link DeviceCodeCredential} with the current configurations.
     */
    public DeviceCodeCredential build() {
        String clientId = this.clientId != null ? this.clientId : IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID;
        return new DeviceCodeCredential(clientId, tenantId, challengeConsumer, automaticAuthentication,
                identityClientOptions);
    }
}
