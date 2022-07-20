// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.properties;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cConfigurationException;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

/**
 * Configuration properties for Azure Active Directory B2C.
 */
public class AadB2cProperties implements InitializingBean {

    /**
     * Default logout success URL.
     */
    public static final String DEFAULT_LOGOUT_SUCCESS_URL = "http://localhost:8080/login";

    /**
     * Prefix
     */
    public static final String PREFIX = "spring.cloud.azure.active-directory.b2c";

    /**
     * The default user flow key 'sign-up-or-sign-in'.
     */
    public static final String DEFAULT_KEY_SIGN_UP_OR_SIGN_IN = "sign-up-or-sign-in";

    /**
     * The default user flow key 'password-reset'.
     */
    protected static final String DEFAULT_KEY_PASSWORD_RESET = "password-reset";

    /**
     * Azure AD B2C profile information.
     */
    @NestedConfigurationProperty
    private final AadB2cProfileProperties profile = new AadB2cProfileProperties();

    /**
     * Azure AD B2C credential information.
     */
    @NestedConfigurationProperty
    private final AadB2cCredentialProperties credential = new AadB2cCredentialProperties();

    /**
     * App ID URI which might be used in the "aud" claim of a token.
     */
    private String appIdUri;

    /**
     * Connection Timeout for the JWKSet Remote URL call.
     */
    private Duration jwtConnectTimeout = Duration.ofMillis(RemoteJWKSet.DEFAULT_HTTP_CONNECT_TIMEOUT);

    /**
     * Read Timeout for the JWKSet Remote URL call.
     */
    private Duration jwtReadTimeout = Duration.ofMillis(RemoteJWKSet.DEFAULT_HTTP_READ_TIMEOUT);

    /**
     * Size limit in Bytes of the JWKSet Remote URL call.
     */
    private int jwtSizeLimit = RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT; /* bytes */

    /**
     * Redirect url after logout.
     */
    private String logoutSuccessUrl = DEFAULT_LOGOUT_SUCCESS_URL;

    /**
     * Additional parameters for authentication.
     */
    private final Map<String, Object> authenticateAdditionalParameters = new HashMap<>();

    /**
     * User name attribute name.
     */
    private String userNameAttributeName;

    /**
     * Reply url after get authorization code.
     */
    private String replyUrl = "{baseUrl}/login/oauth2/code/";

    /**
     * Azure AD B2C endpoint base uri.
     */
    private String baseUri;

    /**
     * Specify the primary sign-in flow key.
     */
    private String loginFlow = DEFAULT_KEY_SIGN_UP_OR_SIGN_IN;

    /**
     * User flows.
     */
    private Map<String, String> userFlows = new HashMap<>();

    /**
     * Specify client configuration.
     */
    private final Map<String, AuthorizationClientProperties> authorizationClients = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        validateURLProperties();
        validateWebappProperties();
        validateCommonProperties();
    }

    /**
     * Validate web app scenario properties configuration when using user flows.
     */
    private void validateWebappProperties() {
        if (!CollectionUtils.isEmpty(userFlows)) {
            if (!StringUtils.hasText(baseUri)) {
                throw new AadB2cConfigurationException("'baseUri' must be configured.");
            }
            if (!userFlows.containsKey(loginFlow)) {
                throw new AadB2cConfigurationException("Sign in user flow key '"
                    + loginFlow + "' is not in 'user-flows' map.");
            }
        }
    }

    /**
     * Validate common scenario properties configuration.
     */
    private void validateCommonProperties() {
        long credentialCount = authorizationClients.values()
                                                   .stream()
                                                   .map(AuthorizationClientProperties::getAuthorizationGrantType)
                                                   .filter(client -> CLIENT_CREDENTIALS.equals(client))
                                                   .count();
        if (credentialCount > 0 && !StringUtils.hasText(profile.getTenantId())) {
            throw new AadB2cConfigurationException("'tenant-id' must be configured "
                + "when using client credential flow.");
        }
    }

    /**
     * Validate URL properties configuration.
     */
    private void validateURLProperties() {
        if (!isValidUrl(logoutSuccessUrl)) {
            throw new AadB2cConfigurationException("logout success should be valid URL.");
        }
        if (!isValidUrl(baseUri)) {
            throw new AadB2cConfigurationException("baseUri should be valid URL.");
        }
    }

    /**
     * Used to validate uri, the uri is allowed to be empty.
     * @param uri the uri to be validated.
     * @return whether is uri is valid or not.
     */
    private static boolean isValidUrl(String uri) {
        if (!StringUtils.hasLength(uri)) {
            return true;
        }
        try {
            new java.net.URL(uri);
        } catch (MalformedURLException ex) {
            return false;
        }
        return true;
    }


    /**
     * Gets the password reset.
     *
     * @return the password reset
     */
    public String getPasswordReset() {
        Optional<String> keyOptional = userFlows.keySet()
                                                .stream()
                                                .filter(key -> key.equalsIgnoreCase(DEFAULT_KEY_PASSWORD_RESET))
                                                .findAny();
        return keyOptional.map(s -> userFlows.get(s)).orElse(null);
    }

    /**
     * Gets the base URI.
     *
     * @return the base URI
     */
    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Sets the base URI.
     *
     * @param baseUri the base URI
     */
    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    /**
     * Gets the user flows.
     *
     * @return the user flows
     */
    public Map<String, String> getUserFlows() {
        return userFlows;
    }

    /**
     * Sets the user flows.
     *
     * @param userFlows the user flows
     */
    public void setUserFlows(Map<String, String> userFlows) {
        this.userFlows = userFlows;
    }

    /**
     * Gets the login flow.
     *
     * @return the login flow
     */
    public String getLoginFlow() {
        return loginFlow;
    }

    /**
     * Sets the login flow.
     *
     * @param loginFlow the login flow
     */
    public void setLoginFlow(String loginFlow) {
        this.loginFlow = loginFlow;
    }

    /**
     * Gets the credential.
     *
     * @return the credential.
     */
    public AadB2cCredentialProperties getCredential() {
        return credential;
    }

    /**
     * Gets the logout success URL.
     *
     * @return the logout success URL
     */
    public String getLogoutSuccessUrl() {
        return logoutSuccessUrl;
    }

    /**
     * Sets the logout success URL.
     *
     * @param logoutSuccessUrl the logout success URL
     */
    public void setLogoutSuccessUrl(String logoutSuccessUrl) {
        this.logoutSuccessUrl = logoutSuccessUrl;
    }

    /**
     * Gets the additional authenticate parameters.
     *
     * @return the additional authenticate parameters
     */
    public Map<String, Object> getAuthenticateAdditionalParameters() {
        return authenticateAdditionalParameters;
    }

    /**
     * Gets the username attribute name.
     *
     * @return the username attribute name
     */
    public String getUserNameAttributeName() {
        return userNameAttributeName;
    }

    /**
     * Sets the username attribute name.
     *
     * @param userNameAttributeName the username attribute name
     */
    public void setUserNameAttributeName(String userNameAttributeName) {
        this.userNameAttributeName = userNameAttributeName;
    }

    /**
     * Gets the reply URL.
     *
     * @return the reply URL
     */
    public String getReplyUrl() {
        return replyUrl;
    }

    /**
     * Sets the reply URL.
     *
     * @param replyUrl the reply URL
     */
    public void setReplyUrl(String replyUrl) {
        this.replyUrl = replyUrl;
    }

    /**
     * Gets the app ID URI.
     *
     * @return the app ID URI
     */
    public String getAppIdUri() {
        return appIdUri;
    }

    /**
     * Sets the app ID URI.
     *
     * @param appIdUri the app ID URI.
     */
    public void setAppIdUri(String appIdUri) {
        this.appIdUri = appIdUri;
    }

    /**
     * Gets the JWT connect timeout.
     *
     * @return the JWT connect timeout
     */
    public Duration getJwtConnectTimeout() {
        return jwtConnectTimeout;
    }

    /**
     * Sets the JWT connect timeout.
     *
     * @param jwtConnectTimeout the JWT connect timeout
     */
    public void setJwtConnectTimeout(Duration jwtConnectTimeout) {
        this.jwtConnectTimeout = jwtConnectTimeout;
    }

    /**
     * Get the JWT read timeout.
     *
     * @return the JWT read timeout
     */
    public Duration getJwtReadTimeout() {
        return jwtReadTimeout;
    }

    /**
     * Sets the JWT read timeout.
     *
     * @param jwtReadTimeout the JWT read timeout
     */
    public void setJwtReadTimeout(Duration jwtReadTimeout) {
        this.jwtReadTimeout = jwtReadTimeout;
    }

    /**
     * Gets the JWT size limit.
     *
     * @return the JWT size limit
     */
    public int getJwtSizeLimit() {
        return jwtSizeLimit;
    }

    /**
     * Sets the JWT size limit.
     *
     * @param jwtSizeLimit the JWT size limit
     */
    public void setJwtSizeLimit(int jwtSizeLimit) {
        this.jwtSizeLimit = jwtSizeLimit;
    }

    /**
     * Gets the profile.
     *
     * @return the profile
     */
    public AadB2cProfileProperties getProfile() {
        return profile;
    }

    /**
     * Gets the authorization clients.
     *
     * @return the authorization clients
     */
    public Map<String, AuthorizationClientProperties> getAuthorizationClients() {
        return authorizationClients;
    }
}
