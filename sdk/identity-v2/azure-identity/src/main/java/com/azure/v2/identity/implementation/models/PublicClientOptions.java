// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.models;

import com.azure.v2.identity.models.AuthenticationRecord;
import com.azure.v2.identity.models.BrowserCustomizationOptions;
import com.azure.v2.identity.models.DeviceCodeInfo;
import com.azure.v2.identity.InteractiveBrowserCredential;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Options to configure the IdentityClient.
 */
public class PublicClientOptions extends ClientOptions {
    private BrowserCustomizationOptions browserCustomizationOptions;
    private AuthenticationRecord authenticationRecord;
    private Consumer<DeviceCodeInfo> challengeConsumer;
    private boolean automaticAuthentication = true;
    private String authCode;
    private URI redirectUri;
    private String loginHint;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public PublicClientOptions() {
        super();
        this.browserCustomizationOptions = new BrowserCustomizationOptions();
    }

    /**
     * Creates a copy of public client options from provided client options instance.
     *
     * @param clientOptions the public client options to copy.
     */
    public PublicClientOptions(PublicClientOptions clientOptions) {
        super(clientOptions);
        this.browserCustomizationOptions = clientOptions.getBrowserCustomizationOptions();
        this.authenticationRecord = clientOptions.getAuthenticationRecord();
        this.challengeConsumer = clientOptions.getChallengeConsumer();
        this.automaticAuthentication = clientOptions.isAutomaticAuthentication();
        this.authCode = clientOptions.getAuthCode();
        this.redirectUri = clientOptions.getRedirectUri();
        this.loginHint = clientOptions.getLoginHint();
    }

    /**
     * Creates a copy of public client options from provided client options instance.
     *
     * @param clientOptions the client options to copy.
     */
    public PublicClientOptions(ClientOptions clientOptions) {
        super(clientOptions);
    }

    /**
     * Gets the configured client secret.
     *
     * @return the browser customization options
     */
    public BrowserCustomizationOptions getBrowserCustomizationOptions() {
        return this.browserCustomizationOptions;
    }

    /**
     * Sets the browser customization options for {@link InteractiveBrowserCredential} authentication.
     *
     * @param browserCustomizationOptions The browser customization options
     * @return the ConfidentialClientOptions itself.
     */
    public PublicClientOptions setBrowserCustomizationOptions(BrowserCustomizationOptions browserCustomizationOptions) {
        this.browserCustomizationOptions = browserCustomizationOptions;
        return this;
    }

    /**
     * Sets the {@link AuthenticationRecord} captured from a previous authentication.
     *
     * @param authenticationRecord The Authentication record to be configured.
     *
     * @return An updated instance of this builder with the configured authentication record.
     */
    public PublicClientOptions setAuthenticationRecord(AuthenticationRecord authenticationRecord) {
        this.authenticationRecord = authenticationRecord;
        return this;
    }

    /**
     * Get the configured {@link AuthenticationRecord}.
     *
     * @return {@link AuthenticationRecord}.
     */
    public AuthenticationRecord getAuthenticationRecord() {
        return authenticationRecord;
    }

    /**
     * Gets the challenge consumer.
     *
     * @return the challenge consumer
     */
    public Consumer<DeviceCodeInfo> getChallengeConsumer() {
        if (challengeConsumer == null) {
            challengeConsumer = deviceCodeInfo -> System.out.println(deviceCodeInfo.getMessage());
        }
        return challengeConsumer;
    }

    /**
     * Sets the challenge consumer for device code auth flow.
     *
     * @param challengeConsumer the challenge consumer
     * @return the updated options
     */
    public PublicClientOptions setChallengeConsumer(Consumer<DeviceCodeInfo> challengeConsumer) {
        this.challengeConsumer = challengeConsumer;
        return this;
    }

    /**
     * Checks whether automatic authentication is enabled for public client auth flows.
     *
     * @return A boolean indicating whether automatic authentication is enabled for public client auth flows.
     */
    public boolean isAutomaticAuthentication() {
        return automaticAuthentication;
    }

    /**
     * Configures the automatic authentication for public client auth flows.
     *
     * @param automaticAuthentication the boolean flag for automatic authentication
     * @return the updated options
     */
    public PublicClientOptions setAutomaticAuthentication(boolean automaticAuthentication) {
        this.automaticAuthentication = automaticAuthentication;
        return this;
    }

    /**
     * Get the authorization code for authorization code auth flow.
     *
     * @return the auth code
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Configures the authorization code for authorization code flow.
     *
     * @param authCode the authorization code
     * @return the updated options
     */
    public PublicClientOptions setAuthCode(String authCode) {
        this.authCode = authCode;
        return this;
    }

    /**
     * Gets the redirect URI.
     *
     * @return the redirect URI
     */
    public URI getRedirectUri() {
        return redirectUri;
    }

    /**
     * Configures the redirect URI.
     *
     * @param redirectUri the redirect URI
     * @return the updated options
     */
    public PublicClientOptions setRedirectUri(URI redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    @Override
    public PublicClientOptions clone() {
        PublicClientOptions clone = (PublicClientOptions) new PublicClientOptions()
            .setBrowserCustomizationOptions(browserCustomizationOptions)
            .setClientId(this.getClientId())
            .setTenantId(this.getTenantId())
            .setHttpPipelineOptions(this.getHttpPipelineOptions().clone())
            .setExecutorService(this.getExecutorService())
            .setAuthorityHost(this.getAuthorityHost())
            .setAdditionallyAllowedTenants(this.getAdditionallyAllowedTenants())
            .setTokenCacheOptions(this.getTokenCacheOptions());
        return clone;
    }

    /**
     * Sets the login hint for interactive browser auth flow.
     *
     * @param loginHint the login hint
     * @return the updated options
     */
    public PublicClientOptions setLoginHint(String loginHint) {
        this.loginHint = loginHint;
        return this;
    }

    /**
     * Gets the login hint
     *
     * @return the login hint
     */
    public String getLoginHint() {
        return this.loginHint;
    }
}
