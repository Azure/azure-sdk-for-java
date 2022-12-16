// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties;

import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.exception.AadB2cConfigurationException;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
     * Connection Timeout for the JWKSet Remote URL call.  Deprecated. If you want to configure this, please provide a RestOperations bean.
     */
    private Duration jwtConnectTimeout = Duration.ofMillis(RemoteJWKSet.DEFAULT_HTTP_CONNECT_TIMEOUT);

    /**
     * Read Timeout for the JWKSet Remote URL call.  Deprecated. If you want to configure this, please provide a RestOperations bean.
     */
    private Duration jwtReadTimeout = Duration.ofMillis(RemoteJWKSet.DEFAULT_HTTP_READ_TIMEOUT);

    /**
     * Size limit in Bytes of the JWKSet Remote URL call.  Deprecated. If you want to configure this, please provide a RestOperations bean.
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

    private void validateCommonProperties() {
        boolean usingClientCredentialFlow = authorizationClients.values()
                                                                .stream()
                                                                .map(AuthorizationClientProperties::getAuthorizationGrantType)
                                                                .anyMatch(CLIENT_CREDENTIALS::equals);
        if (usingClientCredentialFlow && !StringUtils.hasText(profile.getTenantId())) {
            throw new AadB2cConfigurationException("'tenant-id' must be configured "
                + "when using client credential flow.");
        }
    }

    private void validateURLProperties() {
        if (!isValidUrl(logoutSuccessUrl)) {
            throw new AadB2cConfigurationException("logout success should be valid URL.");
        }
        if (!isValidUrl(baseUri)) {
            throw new AadB2cConfigurationException("baseUri should be valid URL.");
        }
    }

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


    public String getPasswordReset() {
        return userFlows.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(DEFAULT_KEY_PASSWORD_RESET))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(null);
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public Map<String, String> getUserFlows() {
        return userFlows;
    }

    public void setUserFlows(Map<String, String> userFlows) {
        this.userFlows = userFlows;
    }

    public String getLoginFlow() {
        return loginFlow;
    }

    public void setLoginFlow(String loginFlow) {
        this.loginFlow = loginFlow;
    }

    public AadB2cCredentialProperties getCredential() {
        return credential;
    }

    public String getLogoutSuccessUrl() {
        return logoutSuccessUrl;
    }

    public void setLogoutSuccessUrl(String logoutSuccessUrl) {
        this.logoutSuccessUrl = logoutSuccessUrl;
    }

    public Map<String, Object> getAuthenticateAdditionalParameters() {
        return authenticateAdditionalParameters;
    }

    public String getUserNameAttributeName() {
        return userNameAttributeName;
    }

    public void setUserNameAttributeName(String userNameAttributeName) {
        this.userNameAttributeName = userNameAttributeName;
    }

    public String getReplyUrl() {
        return replyUrl;
    }

    public void setReplyUrl(String replyUrl) {
        this.replyUrl = replyUrl;
    }

    public String getAppIdUri() {
        return appIdUri;
    }

    public void setAppIdUri(String appIdUri) {
        this.appIdUri = appIdUri;
    }

    public Duration getJwtConnectTimeout() {
        return jwtConnectTimeout;
    }

    public void setJwtConnectTimeout(Duration jwtConnectTimeout) {
        this.jwtConnectTimeout = jwtConnectTimeout;
    }

    public Duration getJwtReadTimeout() {
        return jwtReadTimeout;
    }

    public void setJwtReadTimeout(Duration jwtReadTimeout) {
        this.jwtReadTimeout = jwtReadTimeout;
    }

    public int getJwtSizeLimit() {
        return jwtSizeLimit;
    }

    public void setJwtSizeLimit(int jwtSizeLimit) {
        this.jwtSizeLimit = jwtSizeLimit;
    }

    public AadB2cProfileProperties getProfile() {
        return profile;
    }

    public Map<String, AuthorizationClientProperties> getAuthorizationClients() {
        return authorizationClients;
    }
}
