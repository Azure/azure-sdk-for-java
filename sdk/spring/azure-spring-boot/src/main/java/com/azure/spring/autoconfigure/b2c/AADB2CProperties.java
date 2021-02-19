// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration properties for Azure Active Directory B2C.
 */
@Validated
@ConfigurationProperties(prefix = AADB2CProperties.PREFIX)
public class AADB2CProperties implements InitializingBean {

    public static final String SIGN_IN_USER_FLOW = "sign-in-user-flow";

    public static final String DEFAULT_LOGOUT_SUCCESS_URL = "http://localhost:8080/login";

    public static final String PREFIX = "azure.activedirectory.b2c";

    private static final String TENANT_NAME_PART_REGEX = "([A-Za-z0-9]+\\.)";

    /**
     * The name of the b2c tenant.
     * @deprecated It's recommended to use 'baseUri' instead.
     */
    @Deprecated
    private String tenant;

    /**
     * The application ID that registered under b2c tenant.
     */
    @NotBlank(message = "client ID should not be blank")
    private String clientId;

    /**
     * The application secret that registered under b2c tenant.
     */
    @NotBlank(message = "client secret should not be blank")
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
     * The all user flows which is created under b2c tenant.
     */
    private Set<String> userFlows = new HashSet<>();

    /**
     * Specify the primary sign in flow name
     */
    @NotBlank(message = "The primary sign in flow name should not be blank.")
    private String signInUserFlow;

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isEmpty(tenant) && StringUtils.isEmpty(baseUri)) {
            throw new AADB2CConfigurationException("'tenant' and 'baseUri' at least configure one item.");
        }

        if (userFlows.contains(signInUserFlow)) {
            throw new AADB2CConfigurationException("Sign in user flow '" + signInUserFlow
                + "' does not need to be configured repeatedly.");
        }
    }

    public String getBaseUri() {
        // baseUri is empty and points to Global env by default
        if (StringUtils.hasText(tenant) && StringUtils.isEmpty(baseUri)) {
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

    public Set<String> getUserFlows() {
        return userFlows;
    }

    public void setUserFlows(Set<String> userFlows) {
        this.userFlows = userFlows;
    }

    public String getSignInUserFlow() {
        return signInUserFlow;
    }

    public void setSignInUserFlow(String signInUserFlow) {
        this.signInUserFlow = signInUserFlow;
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
}
