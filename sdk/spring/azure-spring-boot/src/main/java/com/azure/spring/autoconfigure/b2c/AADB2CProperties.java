// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.spring.aad.AADAuthorizationGrantType.CLIENT_CREDENTIALS;

/**
 * Configuration properties for Azure Active Directory B2C.
 */
@Validated
@ConfigurationProperties(prefix = AADB2CProperties.PREFIX)
public class AADB2CProperties implements InitializingBean {

    public static final String DEFAULT_LOGOUT_SUCCESS_URL = "http://localhost:8080/login";

    public static final String PREFIX = "azure.activedirectory.b2c";

    private static final String TENANT_NAME_PART_REGEX = "([A-Za-z0-9]+\\.)";

    /**
     * The default user flow key 'sign-up-or-sign-in'.
     */
    protected static final String DEFAULT_KEY_SIGN_UP_OR_SIGN_IN = "sign-up-or-sign-in";

    /**
     * The default user flow key 'password-reset'.
     */
    protected static final String DEFAULT_KEY_PASSWORD_RESET = "password-reset";

    /**
     * The name of the b2c tenant.
     * @deprecated It's recommended to use 'baseUri' instead.
     */
    @Deprecated
    private String tenant;

    /**
     * The name of the b2c tenant id.
     */
    private String tenantId;

    /**
     * App ID URI which might be used in the <code>"aud"</code> claim of an token.
     */
    private String appIdUri;

    /**
     * Connection Timeout for the JWKSet Remote URL call.
     */
    private int jwtConnectTimeout = RemoteJWKSet.DEFAULT_HTTP_CONNECT_TIMEOUT; /* milliseconds */

    /**
     * Read Timeout for the JWKSet Remote URL call.
     */
    private int jwtReadTimeout = RemoteJWKSet.DEFAULT_HTTP_READ_TIMEOUT; /* milliseconds */

    /**
     * Size limit in Bytes of the JWKSet Remote URL call.
     */
    private int jwtSizeLimit = RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT; /* bytes */

    /**
     * The application ID that registered under b2c tenant.
     */
    @NotBlank(message = "client ID should not be blank")
    private String clientId;

    /**
     * The application secret that registered under b2c tenant.
     */
    private String clientSecret;

    @URL(message = "logout success should be valid URL")
    private String logoutSuccessUrl = DEFAULT_LOGOUT_SUCCESS_URL;

    private Map<String, Object> authenticateAdditionalParameters;

    /**
     * User name attribute name
     */
    private String userNameAttributeName;

    /**
     * Telemetry data will be collected if true, or disable data collection.
     */
    private boolean allowTelemetry = true;

    private String replyUrl = "{baseUrl}/login/oauth2/code/";

    /**
     * AAD B2C endpoint base uri.
     */
    @URL(message = "baseUri should be valid URL")
    private String baseUri;

    /**
     * Specify the primary sign in flow key.
     */
    private String loginFlow = DEFAULT_KEY_SIGN_UP_OR_SIGN_IN;

    private Map<String, String> userFlows = new HashMap<>();

    /**
     * Specify client configuration
     */
    private Map<String, AuthorizationClientProperties> authorizationClients = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        validateWebappProperties();
        validateCommonProperties();
    }

    /**
     * Validate web app scenario properties configuration when using user flows.
     */
    private void validateWebappProperties() {
        if (!CollectionUtils.isEmpty(userFlows)) {
            if (!StringUtils.hasText(tenant) && !StringUtils.hasText(baseUri)) {
                throw new AADB2CConfigurationException("'tenant' and 'baseUri' at least configure one item.");
            }
            if (!userFlows.keySet().contains(loginFlow)) {
                throw new AADB2CConfigurationException("Sign in user flow key '"
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
                                                   .map(authClient -> authClient.getAuthorizationGrantType())
                                                   .filter(client -> CLIENT_CREDENTIALS == client)
                                                   .count();
        if (credentialCount > 0 && !StringUtils.hasText(tenantId)) {
            throw new AADB2CConfigurationException("'tenant-id' must be configured "
                + "when using client credential flow.");
        }
    }

    protected String getPasswordReset() {
        Optional<String> keyOptional = userFlows.keySet()
                                                .stream()
                                                .filter(key -> key.equalsIgnoreCase(DEFAULT_KEY_PASSWORD_RESET))
                                                .findAny();
        return keyOptional.isPresent() ? userFlows.get(keyOptional.get()) : null;
    }

    public String getBaseUri() {
        // baseUri is empty and points to Global env by default
        if (StringUtils.hasText(tenant) && !StringUtils.hasText(baseUri)) {
            return String.format("https://%s.b2clogin.com/%s.onmicrosoft.com/", tenant, tenant);
        }
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    /**
     * Get tenant name for Telemetry
     * @return tenant name
     * @throws AADB2CConfigurationException resolve tenant name failed
     */
    @DeprecatedConfigurationProperty(
        reason = "Configuration updated to baseUri",
        replacement = "azure.activedirectory.b2c.base-uri")
    public String getTenant() {
        if (StringUtils.hasText(baseUri)) {
            Matcher matcher = Pattern.compile(TENANT_NAME_PART_REGEX).matcher(baseUri);
            if (matcher.find()) {
                String matched = matcher.group();
                return matched.substring(0, matched.length() - 1);
            }
            throw new AADB2CConfigurationException("Unable to resolve the 'tenant' name.");
        }
        return tenant;
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
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

    public void setAuthenticateAdditionalParameters(Map<String, Object> authenticateAdditionalParameters) {
        this.authenticateAdditionalParameters = authenticateAdditionalParameters;
    }

    @Deprecated
    @DeprecatedConfigurationProperty(
        reason = "Deprecate the telemetry endpoint and use HTTP header User Agent instead.")
    public boolean isAllowTelemetry() {
        return allowTelemetry;
    }

    public void setAllowTelemetry(boolean allowTelemetry) {
        this.allowTelemetry = allowTelemetry;
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

    public int getJwtConnectTimeout() {
        return jwtConnectTimeout;
    }

    public void setJwtConnectTimeout(int jwtConnectTimeout) {
        this.jwtConnectTimeout = jwtConnectTimeout;
    }

    public int getJwtReadTimeout() {
        return jwtReadTimeout;
    }

    public void setJwtReadTimeout(int jwtReadTimeout) {
        this.jwtReadTimeout = jwtReadTimeout;
    }

    public int getJwtSizeLimit() {
        return jwtSizeLimit;
    }

    public void setJwtSizeLimit(int jwtSizeLimit) {
        this.jwtSizeLimit = jwtSizeLimit;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Map<String, AuthorizationClientProperties> getAuthorizationClients() {
        return authorizationClients;
    }

    public void setAuthorizationClients(Map<String, AuthorizationClientProperties> authorizationClients) {
        this.authorizationClients = authorizationClients;
    }
}
