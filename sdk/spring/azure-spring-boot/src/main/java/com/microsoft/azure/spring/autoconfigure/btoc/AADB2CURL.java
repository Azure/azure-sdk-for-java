// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.btoc;

import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * To get AAD B2C URLs for configuration.
 */
public final class AADB2CURL {

    private AADB2CURL() {

    }

    private static final String AUTHORIZATION_URL_PATTERN =
            "https://%s.b2clogin.com/%s.onmicrosoft.com/oauth2/v2.0/authorize";

    private static final String TOKEN_URL_PATTERN =
            "https://%s.b2clogin.com/%s.onmicrosoft.com/oauth2/v2.0/token?p=%s";

    private static final String JWKSET_URL_PATTERN =
            "https://%s.b2clogin.com/%s.onmicrosoft.com/discovery/v2.0/keys?p=%s";

    private static final String END_SESSION_URL_PATTERN =
            "https://%s.b2clogin.com/%s.onmicrosoft.com/oauth2/v2.0/logout?post_logout_redirect_uri=%s&p=%s";

    public static String getAuthorizationUrl(String tenant) {
        Assert.hasText(tenant, "tenant should have text.");

        return String.format(AUTHORIZATION_URL_PATTERN, tenant, tenant);
    }

    public static String getTokenUrl(String tenant, String userFlow) {
        Assert.hasText(tenant, "tenant should have text.");
        Assert.hasText(userFlow, "user flow should have text.");

        return String.format(TOKEN_URL_PATTERN, tenant, tenant, userFlow);
    }

    public static String getJwkSetUrl(String tenant, String userFlow) {
        Assert.hasText(tenant, "tenant should have text.");
        Assert.hasText(userFlow, "user flow should have text.");

        return String.format(JWKSET_URL_PATTERN, tenant, tenant, userFlow);
    }

    public static String getEndSessionUrl(String tenant, String logoutUrl, String userFlow) {
        Assert.hasText(tenant, "tenant should have text.");
        Assert.hasText(logoutUrl, "logoutUrl should have text.");
        Assert.hasText(userFlow, "user flow should have text.");

        return String.format(END_SESSION_URL_PATTERN, tenant, tenant, getEncodedURL(logoutUrl), userFlow);
    }

    private static String getEncodedURL(String url) {
        Assert.hasText(url, "url should have text.");

        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new AADB2CConfigurationException("failed to encode url: " + url, e);
        }
    }
}
