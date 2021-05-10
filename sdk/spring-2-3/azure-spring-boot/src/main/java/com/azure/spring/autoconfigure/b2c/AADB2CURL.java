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
public final class AADB2CURL {

    private AADB2CURL() {

    }

    private static final String AUTHORIZATION_URL_PATTERN = "oauth2/v2.0/authorize";

    private static final String TOKEN_URL_PATTERN = "oauth2/v2.0/token?p=";

    private static final String JWKSET_URL_PATTERN = "discovery/v2.0/keys?p=";

    private static final String END_SESSION_URL_PATTERN = "oauth2/v2.0/logout?post_logout_redirect_uri=%s&p=%s";

    public static String getAuthorizationUrl(String baseUri) {
        return addSlash(baseUri) + AUTHORIZATION_URL_PATTERN;
    }

    public static String getTokenUrl(String baseUri, String userFlow) {
        Assert.hasText(userFlow, "user flow should have text.");

        return addSlash(baseUri) + TOKEN_URL_PATTERN + userFlow;
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
