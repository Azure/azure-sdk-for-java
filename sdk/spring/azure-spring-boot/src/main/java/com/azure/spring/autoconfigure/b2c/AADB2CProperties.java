// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for Azure Active Directory B2C.
 */
@Validated
@ConfigurationProperties(prefix = AADB2CProperties.PREFIX)
public class AADB2CProperties implements InitializingBean {

    public static final String USER_FLOWS = "user-flows";

    /**
     * We do not use ${@link String#format(String, Object...)}
     * as it's not real constant, which cannot be referenced in annotation.
     */
    public static final String SIGN_IN_USER_FLOW = "sign-in-user-flow";

    public static final String DEFAULT_LOGOUT_SUCCESS_URL = "http://localhost:8080/login";

    public static final String PREFIX = "azure.activedirectory.b2c";

    /**
     * The name of the b2c tenant.
     */
    @NotBlank(message = "tenant name should not be blank")
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

    @URL(message = "reply URL should be valid URL")
    private String replyUrl;

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

    private Map<String, AuthorizationClientScopesProperties> authorizationClients = new HashMap<>();

    /**
     * The all user flows which is created under b2c tenant.
     */
    private List<String> userFlows = new ArrayList<>();

    /**
     * Specify the primary sign in flow name
     */
    @NotBlank(message = "The primary sign in flow name should not be blank.")
    private String signInUserFlow;

    @Override
    public void afterPropertiesSet() {
        if (!userFlows.isEmpty() && userFlows.contains(signInUserFlow)) {
            throw new AADB2CConfigurationException("Sign in user flow '" + signInUserFlow
                + "' does not need to be configured repeatedly.");
        }
    }

    public List<String> getUserFlows() {
        return userFlows;
    }

    public void setUserFlows(List<String> userFlows) {
        this.userFlows = userFlows;
    }

    public String getSignInUserFlow() {
        return signInUserFlow;
    }

    public void setSignInUserFlow(String signInUserFlow) {
        this.signInUserFlow = signInUserFlow;
    }

    private String getReplyURLPath(@URL String replyURL) {
        try {
            return new java.net.URL(replyURL).getPath();
        } catch (MalformedURLException e) {
            throw new AADB2CConfigurationException("Failed to get path of given URL.", e);
        }
    }

    @NonNull
    public String getLoginProcessingUrl() {
        return getReplyURLPath(replyUrl);
    }

    public Map<String, AuthorizationClientScopesProperties> getAuthorizationClients() {
        return authorizationClients;
    }

    public void setAuthorizationClients(Map<String, AuthorizationClientScopesProperties> authorizationClients) {
        this.authorizationClients = authorizationClients;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
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

    public String getReplyUrl() {
        return replyUrl;
    }

    public void setReplyUrl(String replyUrl) {
        this.replyUrl = replyUrl;
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
}
