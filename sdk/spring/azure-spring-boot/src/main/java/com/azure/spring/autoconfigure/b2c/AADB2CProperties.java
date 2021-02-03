// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import java.net.MalformedURLException;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Azure Active Directory B2C.
 */
@Validated
@ConfigurationProperties(prefix = AADB2CProperties.PREFIX)
public class AADB2CProperties {

    private static final String USER_FLOWS = "user-flows";

    /**
     * We do not use ${@link String#format(String, Object...)} as it's not real constant, which cannot be referenced in
     * annotation.
     */
    public static final String USER_FLOW_PASSWORD_RESET = USER_FLOWS + ".password-reset";

    public static final String USER_FLOW_PROFILE_EDIT = USER_FLOWS + ".profile-edit";

    public static final String USER_FLOW_SIGN_UP_OR_SIGN_IN = USER_FLOWS + ".sign-up-or-sign-in";

    public static final String DEFAULT_LOGOUT_SUCCESS_URL = "http://localhost:8080/login";

    public static final String PREFIX = "azure.activedirectory.b2c";

    /**
     * The name of the b2c tenant name.
     */
    @NotBlank(message = "tenant name should not be blank")
    private String tenantName;

    /**
     * The name of the b2c tenant id.
     */
    @NotBlank(message = "tenant id should not be blank")
    private String tenantId;

    /**
     * App ID URI which might be used in the <code>"aud"</code> claim of an token.
     */
    private String AppIdUri;

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
     * Use OIDC ${@link OidcAuthorizationCodeAuthenticationProvider} by default. If set to false, will use Oauth2
     * ${@link OAuth2AuthorizationCodeAuthenticationProvider}.
     */
    private Boolean oidcEnabled = true;

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
     * The all user flows which is created under b2c tenant.
     */
    private UserFlows userFlows = new UserFlows();

    /**
     * Telemetry data will be collected if true, or disable data collection.
     */
    private boolean allowTelemetry = true;

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

    @Validated
    public static class UserFlows {

        protected UserFlows() {

        }

        /**
         * The sign-up-or-sign-in user flow which is created under b2c tenant.
         */
        @NotBlank(message = "sign-up-or-in value should not be blank")
        private String signUpOrSignIn;

        /**
         * The profile-edit user flow which is created under b2c tenant.
         */
        private String profileEdit;

        /**
         * The password-reset user flow which is created under b2c tenant.
         */
        private String passwordReset;

        public String getSignUpOrSignIn() {
            return signUpOrSignIn;
        }

        public void setSignUpOrSignIn(String signUpOrSignIn) {
            this.signUpOrSignIn = signUpOrSignIn;
        }

        public String getProfileEdit() {
            return profileEdit;
        }

        public void setProfileEdit(String profileEdit) {
            this.profileEdit = profileEdit;
        }

        public String getPasswordReset() {
            return passwordReset;
        }

        public void setPasswordReset(String passwordReset) {
            this.passwordReset = passwordReset;
        }
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Boolean getOidcEnabled() {
        return oidcEnabled;
    }

    public void setOidcEnabled(Boolean oidcEnabled) {
        this.oidcEnabled = oidcEnabled;
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

    public UserFlows getUserFlows() {
        return userFlows;
    }

    public void setUserFlows(UserFlows userFlows) {
        this.userFlows = userFlows;
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

    public String getAppIdUri() {
        return AppIdUri;
    }

    public void setAppIdUri(String appIdUri) {
        AppIdUri = appIdUri;
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
}
