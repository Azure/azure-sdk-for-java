// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.hibernate.validator.constraints.URL;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * To get AAD B2C URLs for configuration.
 */
@Deprecated
public final class AADB2CURL {

    private AADB2CURL() {

    }

    private static final String AUTHORIZATION_URL_PATTERN = "oauth2/v2.0/authorize";

    private static final String TOKEN_URL_PATTERN = "oauth2/v2.0/token?p=";

    private static final String JWKSET_URL_PATTERN = "discovery/v2.0/keys?p=";

    private static final String END_SESSION_URL_PATTERN = "oauth2/v2.0/logout?post_logout_redirect_uri=%s&p=%s";

    private static final String AAD_TOKEN_URL_PATTERN = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
    private static final String AAD_JWKSET_URL_PATTERN = "https://login.microsoftonline.com/%s/discovery/v2.0/keys";

    public static String getAuthorizationUrl(String baseUri) {
        return addSlash(baseUri) + AUTHORIZATION_URL_PATTERN;
    }

    public static String getTokenUrl(String baseUri, String userFlow) {
        Assert.hasText(userFlow, "user flow should have text.");

        return addSlash(baseUri) + TOKEN_URL_PATTERN + userFlow;
    }

    public static String getAADTokenUrl(String tenantId) {
        Assert.hasText(tenantId, "tenantId should have text.");
        return String.format(AAD_TOKEN_URL_PATTERN, tenantId);
    }

    public static String getAADJwkSetUrl(String tenantId) {
        Assert.hasText(tenantId, "tenantId should have text.");
        return String.format(AAD_JWKSET_URL_PATTERN, tenantId);
    }

    public static String getJwkSetUrl(String baseUri, String userFlow) {
        Assert.hasText(userFlow, "user flow should have text.");

        return addSlash(baseUri) + JWKSET_URL_PATTERN + userFlow;
    }

    public static String getEndSessionUrl(String baseUri, String logoutUrl, String userFlow) {
        Assert.hasText(logoutUrl, "logoutUrl should have text.");
        Assert.hasText(userFlow, "user flow should have text.");

        return addSlash(baseUri) + String.format(END_SESSION_URL_PATTERN, getEncodedURL(logoutUrl), userFlow);
    }

    private static String getEncodedURL(String url) {
        Assert.hasText(url, "url should have text.");

        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new AADB2CConfigurationException("failed to encode url: " + url, e);
        }
    }

    private static String addSlash(@URL String uri) {
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
