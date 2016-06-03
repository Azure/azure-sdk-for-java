/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Configuration settings for the Azure App Service Authentication /
 * Authorization feature.
 */
public class SiteAuthSettingsInner {
    /**
     * Gets or sets a value indicating whether the Authentication /
     * Authorization feature is enabled for the current app.
     */
    private Boolean enabled;

    /**
     * Gets or sets the relative path prefix used by platform HTTP APIs.
     * Changing this value is not recommended except for
     * compatibility reasons.
     */
    private String httpApiPrefixPath;

    /**
     * Gets or sets the action to take when an unauthenticated client attempts
     * to access the app. Possible values include: 'RedirectToLoginPage',
     * 'AllowAnonymous'.
     */
    private UnauthenticatedClientAction unauthenticatedClientAction;

    /**
     * Gets or sets a value indicating whether to durably store
     * platform-specific security tokens
     * obtained during login flows. This capability is disabled by
     * default.
     */
    private Boolean tokenStoreEnabled;

    /**
     * Gets or sets a collection of external URLs that can be redirected to as
     * part of logging in
     * or logging out of the web app. Note that the query string
     * part of the URL is ignored.
     * This is an advanced setting typically only needed by
     * Windows Store application backends.
     * Note that URLs within the current domain are always
     * implicitly allowed.
     */
    private List<String> allowedExternalRedirectUrls;

    /**
     * Gets or sets the default authentication provider to use when multiple
     * providers are configured.
     * This setting is only needed if multiple providers are
     * configured and the unauthenticated client
     * action is set to "RedirectToLoginPage". Possible values
     * include: 'AzureActiveDirectory', 'Facebook', 'Google',
     * 'MicrosoftAccount', 'Twitter'.
     */
    private BuiltInAuthenticationProvider defaultProvider;

    /**
     * Gets or sets the number of hours after session token expiration that a
     * session token can be used to
     * call the token refresh API. The default is 72 hours.
     */
    private Double tokenRefreshExtensionHours;

    /**
     * Gets or sets the Client ID of this relying party application, known as
     * the client_id.
     * This setting is required for enabling OpenID Connection
     * authentication with Azure Active Directory or
     * other 3rd party OpenID Connect providers.
     * More information on OpenID Connect:
     * http://openid.net/specs/openid-connect-core-1_0.html.
     */
    private String clientId;

    /**
     * Gets or sets the Client Secret of this relying party application (in
     * Azure Active Directory, this is also referred to as the Key).
     * This setting is optional. If no client secret is
     * configured, the OpenID Connect implicit auth flow is used to
     * authenticate end users.
     * Otherwise, the OpenID Connect Authorization Code Flow is
     * used to authenticate end users.
     * More information on OpenID Connect:
     * http://openid.net/specs/openid-connect-core-1_0.html.
     */
    private String clientSecret;

    /**
     * Gets or sets the OpenID Connect Issuer URI that represents the entity
     * which issues access tokens for this application.
     * When using Azure Active Directory, this value is the URI of
     * the directory tenant, e.g. https://sts.windows.net/{tenant-guid}/.
     * This URI is a case-sensitive identifier for the token
     * issuer.
     * More information on OpenID Connect Discovery:
     * http://openid.net/specs/openid-connect-discovery-1_0.html.
     */
    private String issuer;

    /**
     * Gets or sets a list of allowed audience values to consider when
     * validating JWTs issued by
     * Azure Active Directory. Note that the
     * {Microsoft.Web.Hosting.Administration.SiteAuthSettings.ClientId} value
     * is always considered an
     * allowed audience, regardless of this setting.
     */
    private List<String> allowedAudiences;

    /**
     * Gets or sets a list of login parameters to send to the OpenID Connect
     * authorization endpoint when
     * a user logs in. Each parameter must be in the form
     * "key=value".
     */
    private List<String> additionalLoginParams;

    /**
     * The aadClientId property.
     */
    private String aadClientId;

    /**
     * The openIdIssuer property.
     */
    private String openIdIssuer;

    /**
     * Gets or sets the OpenID Connect Client ID for the Google web
     * application.
     * This setting is required for enabling Google Sign-In.
     * Google Sign-In documentation:
     * https://developers.google.com/identity/sign-in/web/.
     */
    private String googleClientId;

    /**
     * Gets or sets the client secret associated with the Google web
     * application.
     * This setting is required for enabling Google Sign-In.
     * Google Sign-In documentation:
     * https://developers.google.com/identity/sign-in/web/.
     */
    private String googleClientSecret;

    /**
     * Gets or sets the OAuth 2.0 scopes that will be requested as part of
     * Google Sign-In authentication.
     * This setting is optional. If not specified, "openid",
     * "profile", and "email" are used as default scopes.
     * Google Sign-In documentation:
     * https://developers.google.com/identity/sign-in/web/.
     */
    private List<String> googleOAuthScopes;

    /**
     * Gets or sets the App ID of the Facebook app used for login.
     * This setting is required for enabling Facebook Login.
     * Facebook Login documentation:
     * https://developers.facebook.com/docs/facebook-login.
     */
    private String facebookAppId;

    /**
     * Gets or sets the App Secret of the Facebook app used for Facebook
     * Login.
     * This setting is required for enabling Facebook Login.
     * Facebook Login documentation:
     * https://developers.facebook.com/docs/facebook-login.
     */
    private String facebookAppSecret;

    /**
     * Gets or sets the OAuth 2.0 scopes that will be requested as part of
     * Facebook Login authentication.
     * This setting is optional.
     * Facebook Login documentation:
     * https://developers.facebook.com/docs/facebook-login.
     */
    private List<String> facebookOAuthScopes;

    /**
     * Gets or sets the OAuth 1.0a consumer key of the Twitter application
     * used for sign-in.
     * This setting is required for enabling Twitter Sign-In.
     * Twitter Sign-In documentation:
     * https://dev.twitter.com/web/sign-in.
     */
    private String twitterConsumerKey;

    /**
     * Gets or sets the OAuth 1.0a consumer secret of the Twitter application
     * used for sign-in.
     * This setting is required for enabling Twitter Sign-In.
     * Twitter Sign-In documentation:
     * https://dev.twitter.com/web/sign-in.
     */
    private String twitterConsumerSecret;

    /**
     * Gets or sets the OAuth 2.0 client ID that was created for the app used
     * for authentication.
     * This setting is required for enabling Microsoft Account
     * authentication.
     * Microsoft Account OAuth documentation:
     * https://dev.onedrive.com/auth/msa_oauth.htm.
     */
    private String microsoftAccountClientId;

    /**
     * Gets or sets the OAuth 2.0 client secret that was created for the app
     * used for authentication.
     * This setting is required for enabling Microsoft Account
     * authentication.
     * Microsoft Account OAuth documentation:
     * https://dev.onedrive.com/auth/msa_oauth.htm.
     */
    private String microsoftAccountClientSecret;

    /**
     * Gets or sets the OAuth 2.0 scopes that will be requested as part of
     * Microsoft Account authentication.
     * This setting is optional. If not specified, "wl.basic" is
     * used as the default scope.
     * Microsoft Account Scopes and permissions documentation:
     * https://msdn.microsoft.com/en-us/library/dn631845.aspx.
     */
    private List<String> microsoftAccountOAuthScopes;

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the httpApiPrefixPath value.
     *
     * @return the httpApiPrefixPath value
     */
    public String httpApiPrefixPath() {
        return this.httpApiPrefixPath;
    }

    /**
     * Set the httpApiPrefixPath value.
     *
     * @param httpApiPrefixPath the httpApiPrefixPath value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withHttpApiPrefixPath(String httpApiPrefixPath) {
        this.httpApiPrefixPath = httpApiPrefixPath;
        return this;
    }

    /**
     * Get the unauthenticatedClientAction value.
     *
     * @return the unauthenticatedClientAction value
     */
    public UnauthenticatedClientAction unauthenticatedClientAction() {
        return this.unauthenticatedClientAction;
    }

    /**
     * Set the unauthenticatedClientAction value.
     *
     * @param unauthenticatedClientAction the unauthenticatedClientAction value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withUnauthenticatedClientAction(UnauthenticatedClientAction unauthenticatedClientAction) {
        this.unauthenticatedClientAction = unauthenticatedClientAction;
        return this;
    }

    /**
     * Get the tokenStoreEnabled value.
     *
     * @return the tokenStoreEnabled value
     */
    public Boolean tokenStoreEnabled() {
        return this.tokenStoreEnabled;
    }

    /**
     * Set the tokenStoreEnabled value.
     *
     * @param tokenStoreEnabled the tokenStoreEnabled value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withTokenStoreEnabled(Boolean tokenStoreEnabled) {
        this.tokenStoreEnabled = tokenStoreEnabled;
        return this;
    }

    /**
     * Get the allowedExternalRedirectUrls value.
     *
     * @return the allowedExternalRedirectUrls value
     */
    public List<String> allowedExternalRedirectUrls() {
        return this.allowedExternalRedirectUrls;
    }

    /**
     * Set the allowedExternalRedirectUrls value.
     *
     * @param allowedExternalRedirectUrls the allowedExternalRedirectUrls value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withAllowedExternalRedirectUrls(List<String> allowedExternalRedirectUrls) {
        this.allowedExternalRedirectUrls = allowedExternalRedirectUrls;
        return this;
    }

    /**
     * Get the defaultProvider value.
     *
     * @return the defaultProvider value
     */
    public BuiltInAuthenticationProvider defaultProvider() {
        return this.defaultProvider;
    }

    /**
     * Set the defaultProvider value.
     *
     * @param defaultProvider the defaultProvider value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withDefaultProvider(BuiltInAuthenticationProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
        return this;
    }

    /**
     * Get the tokenRefreshExtensionHours value.
     *
     * @return the tokenRefreshExtensionHours value
     */
    public Double tokenRefreshExtensionHours() {
        return this.tokenRefreshExtensionHours;
    }

    /**
     * Set the tokenRefreshExtensionHours value.
     *
     * @param tokenRefreshExtensionHours the tokenRefreshExtensionHours value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withTokenRefreshExtensionHours(Double tokenRefreshExtensionHours) {
        this.tokenRefreshExtensionHours = tokenRefreshExtensionHours;
        return this;
    }

    /**
     * Get the clientId value.
     *
     * @return the clientId value
     */
    public String clientId() {
        return this.clientId;
    }

    /**
     * Set the clientId value.
     *
     * @param clientId the clientId value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Get the clientSecret value.
     *
     * @return the clientSecret value
     */
    public String clientSecret() {
        return this.clientSecret;
    }

    /**
     * Set the clientSecret value.
     *
     * @param clientSecret the clientSecret value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Get the issuer value.
     *
     * @return the issuer value
     */
    public String issuer() {
        return this.issuer;
    }

    /**
     * Set the issuer value.
     *
     * @param issuer the issuer value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * Get the allowedAudiences value.
     *
     * @return the allowedAudiences value
     */
    public List<String> allowedAudiences() {
        return this.allowedAudiences;
    }

    /**
     * Set the allowedAudiences value.
     *
     * @param allowedAudiences the allowedAudiences value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withAllowedAudiences(List<String> allowedAudiences) {
        this.allowedAudiences = allowedAudiences;
        return this;
    }

    /**
     * Get the additionalLoginParams value.
     *
     * @return the additionalLoginParams value
     */
    public List<String> additionalLoginParams() {
        return this.additionalLoginParams;
    }

    /**
     * Set the additionalLoginParams value.
     *
     * @param additionalLoginParams the additionalLoginParams value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withAdditionalLoginParams(List<String> additionalLoginParams) {
        this.additionalLoginParams = additionalLoginParams;
        return this;
    }

    /**
     * Get the aadClientId value.
     *
     * @return the aadClientId value
     */
    public String aadClientId() {
        return this.aadClientId;
    }

    /**
     * Set the aadClientId value.
     *
     * @param aadClientId the aadClientId value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withAadClientId(String aadClientId) {
        this.aadClientId = aadClientId;
        return this;
    }

    /**
     * Get the openIdIssuer value.
     *
     * @return the openIdIssuer value
     */
    public String openIdIssuer() {
        return this.openIdIssuer;
    }

    /**
     * Set the openIdIssuer value.
     *
     * @param openIdIssuer the openIdIssuer value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withOpenIdIssuer(String openIdIssuer) {
        this.openIdIssuer = openIdIssuer;
        return this;
    }

    /**
     * Get the googleClientId value.
     *
     * @return the googleClientId value
     */
    public String googleClientId() {
        return this.googleClientId;
    }

    /**
     * Set the googleClientId value.
     *
     * @param googleClientId the googleClientId value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
        return this;
    }

    /**
     * Get the googleClientSecret value.
     *
     * @return the googleClientSecret value
     */
    public String googleClientSecret() {
        return this.googleClientSecret;
    }

    /**
     * Set the googleClientSecret value.
     *
     * @param googleClientSecret the googleClientSecret value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
        return this;
    }

    /**
     * Get the googleOAuthScopes value.
     *
     * @return the googleOAuthScopes value
     */
    public List<String> googleOAuthScopes() {
        return this.googleOAuthScopes;
    }

    /**
     * Set the googleOAuthScopes value.
     *
     * @param googleOAuthScopes the googleOAuthScopes value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withGoogleOAuthScopes(List<String> googleOAuthScopes) {
        this.googleOAuthScopes = googleOAuthScopes;
        return this;
    }

    /**
     * Get the facebookAppId value.
     *
     * @return the facebookAppId value
     */
    public String facebookAppId() {
        return this.facebookAppId;
    }

    /**
     * Set the facebookAppId value.
     *
     * @param facebookAppId the facebookAppId value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withFacebookAppId(String facebookAppId) {
        this.facebookAppId = facebookAppId;
        return this;
    }

    /**
     * Get the facebookAppSecret value.
     *
     * @return the facebookAppSecret value
     */
    public String facebookAppSecret() {
        return this.facebookAppSecret;
    }

    /**
     * Set the facebookAppSecret value.
     *
     * @param facebookAppSecret the facebookAppSecret value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withFacebookAppSecret(String facebookAppSecret) {
        this.facebookAppSecret = facebookAppSecret;
        return this;
    }

    /**
     * Get the facebookOAuthScopes value.
     *
     * @return the facebookOAuthScopes value
     */
    public List<String> facebookOAuthScopes() {
        return this.facebookOAuthScopes;
    }

    /**
     * Set the facebookOAuthScopes value.
     *
     * @param facebookOAuthScopes the facebookOAuthScopes value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withFacebookOAuthScopes(List<String> facebookOAuthScopes) {
        this.facebookOAuthScopes = facebookOAuthScopes;
        return this;
    }

    /**
     * Get the twitterConsumerKey value.
     *
     * @return the twitterConsumerKey value
     */
    public String twitterConsumerKey() {
        return this.twitterConsumerKey;
    }

    /**
     * Set the twitterConsumerKey value.
     *
     * @param twitterConsumerKey the twitterConsumerKey value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withTwitterConsumerKey(String twitterConsumerKey) {
        this.twitterConsumerKey = twitterConsumerKey;
        return this;
    }

    /**
     * Get the twitterConsumerSecret value.
     *
     * @return the twitterConsumerSecret value
     */
    public String twitterConsumerSecret() {
        return this.twitterConsumerSecret;
    }

    /**
     * Set the twitterConsumerSecret value.
     *
     * @param twitterConsumerSecret the twitterConsumerSecret value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withTwitterConsumerSecret(String twitterConsumerSecret) {
        this.twitterConsumerSecret = twitterConsumerSecret;
        return this;
    }

    /**
     * Get the microsoftAccountClientId value.
     *
     * @return the microsoftAccountClientId value
     */
    public String microsoftAccountClientId() {
        return this.microsoftAccountClientId;
    }

    /**
     * Set the microsoftAccountClientId value.
     *
     * @param microsoftAccountClientId the microsoftAccountClientId value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withMicrosoftAccountClientId(String microsoftAccountClientId) {
        this.microsoftAccountClientId = microsoftAccountClientId;
        return this;
    }

    /**
     * Get the microsoftAccountClientSecret value.
     *
     * @return the microsoftAccountClientSecret value
     */
    public String microsoftAccountClientSecret() {
        return this.microsoftAccountClientSecret;
    }

    /**
     * Set the microsoftAccountClientSecret value.
     *
     * @param microsoftAccountClientSecret the microsoftAccountClientSecret value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withMicrosoftAccountClientSecret(String microsoftAccountClientSecret) {
        this.microsoftAccountClientSecret = microsoftAccountClientSecret;
        return this;
    }

    /**
     * Get the microsoftAccountOAuthScopes value.
     *
     * @return the microsoftAccountOAuthScopes value
     */
    public List<String> microsoftAccountOAuthScopes() {
        return this.microsoftAccountOAuthScopes;
    }

    /**
     * Set the microsoftAccountOAuthScopes value.
     *
     * @param microsoftAccountOAuthScopes the microsoftAccountOAuthScopes value to set
     * @return the SiteAuthSettingsInner object itself.
     */
    public SiteAuthSettingsInner withMicrosoftAccountOAuthScopes(List<String> microsoftAccountOAuthScopes) {
        this.microsoftAccountOAuthScopes = microsoftAccountOAuthScopes;
        return this;
    }

}
