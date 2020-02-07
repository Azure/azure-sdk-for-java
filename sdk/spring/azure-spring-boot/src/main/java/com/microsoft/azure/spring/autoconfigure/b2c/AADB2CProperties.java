/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.b2c;

import lombok.*;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.net.MalformedURLException;

@Getter
@Setter
@Validated
@NoArgsConstructor
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

    @Getter
    @Setter
    @Validated
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    protected static class UserFlows {

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
    }
}
