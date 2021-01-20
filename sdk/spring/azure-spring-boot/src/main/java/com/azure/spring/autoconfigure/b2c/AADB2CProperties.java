// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * Configuration properties for Azure Active Directory B2C.
 */
@Validated
@ConfigurationProperties(prefix = AADB2CProperties.PREFIX)
public class AADB2CProperties {

    private static final String USER_FLOWS = "user-flows";

    /**
     * We do not use ${@link String#format(String, Object...)}
     * as it's not real constant, which cannot be referenced in annotation.
     */
    public static final String USER_FLOW_PASSWORD_RESET = USER_FLOWS + ".password-reset";

    public static final String USER_FLOW_PROFILE_EDIT = USER_FLOWS + ".profile-edit";

    public static final String USER_FLOW_SIGN_UP_OR_SIGN_IN = USER_FLOWS + ".sign-up-or-sign-in";

    public static final String USER_FLOW_SIGN_UP = USER_FLOWS + ".sign-up";

    public static final String USER_FLOW_SIGN_IN = USER_FLOWS + ".sign-in";

    public static final String DEFAULT_LOGOUT_SUCCESS_URL = "http://localhost:8080/login";

    public static final String PREFIX = "azure.activedirectory.b2c";

    /**
     * The name of the b2c tenant.
     */
    @NotBlank(message = "tenant name should not be blank")
    private String tenant;

    /**
     * Use OIDC ${@link OidcAuthorizationCodeAuthenticationProvider} by default. If set to false,
     * will use Oauth2 ${@link OAuth2AuthorizationCodeAuthenticationProvider}.
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

    /**
     * UserFlows
     */
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

        /**
         * The sign-up user flow which is created under b2c tenant.
         */
        private String signUp;

        /**
         * The sign-in user flow which is created under b2c tenant.
         */
        private String signIn;

        public String getSignUp() {
            return signUp;
        }

        public void setSignUp(String signUp) {
            this.signUp = signUp;
        }

        public String getSignIn() {
            return signIn;
        }

        public void setSignIn(String signIn) {
            this.signIn = signIn;
        }

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

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
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
}
