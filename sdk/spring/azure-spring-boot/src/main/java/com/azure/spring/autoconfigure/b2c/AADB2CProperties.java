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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
     * The sign-up-or-sign-in user flow key name.
     * @deprecated Not limited to this name 'signUpOrSignIn' or 'sign-up-or-sign-in', users can use a custom name instead.
     */
    @Deprecated
    private static final String[] KEY_SIGN_UP_OR_SIGN_IN = {"signUpOrSignIn", "sign-up-or-sign-in"};

    /**
     * The profile-edit user flow key name.
     * @deprecated Not limited to this name 'profileEdit' or 'profile-edit', users can use a custom name instead.
     */
    @Deprecated
    private static final String[] KEY_PROFILE_EDIT = {"profileEdit", "profile-edit"};

    /**
     * The password-reset user flow key name.
     * @deprecated Not limited to this name 'passwordReset' or 'password-reset', users can use a custom name instead.
     */
    @Deprecated
    private static final String[] KEY_PASSWORD_RESET = {"passwordReset", "password-reset"};

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
     * Specify the primary sign in flow name
     */
    private String signInUserFlow;

    private Map<String, String> userFlows = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isEmpty(tenant) && StringUtils.isEmpty(baseUri)) {
            throw new AADB2CConfigurationException("'tenant' and 'baseUri' at least configure one item.");
        }

        if (StringUtils.hasText(signInUserFlow)) {
            if (userFlows.keySet().contains(signInUserFlow)) {
                throw new AADB2CConfigurationException("Sign in user flow '" + signInUserFlow
                    + "' does not need to be configured repeatedly.");
            }
        } else {
            // user flow 'signUpOrSignIn' will be the default flow for login.
            Map.Entry<String, String> signInEntry = getSignUpOrSignIn();
            if (null != signInEntry) {
                signInUserFlow = userFlows.remove(signInEntry.getKey());
            }
            if (StringUtils.isEmpty(signInUserFlow)) {
                throw new AADB2CConfigurationException("The primary sign in flow name "
                    + "'sign-in-user-flow' should not be blank.");
            }
        }
    }

    @DeprecatedConfigurationProperty(
        reason = "Compatible with old version 'signUpOrSignIn' usage",
        replacement = "Need to specify a specific key to get the mapping value")
    public Map.Entry<String, String> getSignUpOrSignIn() {
        return getUserFlowByKeys(KEY_SIGN_UP_OR_SIGN_IN).orElse(null);
    }

    @DeprecatedConfigurationProperty(
        reason = "Compatible with old version 'profileEdit' usage",
        replacement = "Need to specify a specific key to get the mapping value")
    public Map.Entry<String, String> getProfileEdit() {
        return getUserFlowByKeys(KEY_PROFILE_EDIT).orElse(null);
    }

    @DeprecatedConfigurationProperty(
        reason = "Compatible with old version 'passwordReset' usage",
        replacement = "Need to specify a specific key to get the mapping value")
    public Map.Entry<String, String> getPasswordReset() {
        return getUserFlowByKeys(KEY_PASSWORD_RESET).orElse(null);
    }

    private Optional<Map.Entry<String, String>> getUserFlowByKeys(final String[] reservedKeys) {
        Optional<String> keyOptional = userFlows.keySet()
                                                .stream()
                                                .filter(key -> Arrays.stream(reservedKeys)
                                                                     .anyMatch(reservedKey -> reservedKey.equalsIgnoreCase(key)))
                                                .findAny();
        return userFlows.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().equals(keyOptional.orElse(null)))
                        .findAny();
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

    public Map<String, String> getUserFlows() {
        return userFlows;
    }

    public void setUserFlows(Map<String, String> userFlows) {
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
