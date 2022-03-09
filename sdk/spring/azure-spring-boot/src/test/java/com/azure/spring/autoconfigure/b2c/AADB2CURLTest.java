// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AADB2CURLTest {

    private static final String DEFAULT_BASE_URI = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/";
    private static final String CHINA_BASE_URI = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/";
    private static final String B2C_TENANT_ID = "fake-tenant-id";

    /**
     * Reference pattern see AUTHORIZATION_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetGlobalAuthorizationUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/oauth2/v2.0/authorize";
        Assertions.assertEquals(AADB2CURL.getAuthorizationUrl(DEFAULT_BASE_URI), expect);
    }

    @Test
    public void testGetChinaAuthorizationUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/oauth2/v2.0/authorize";
        Assertions.assertEquals(AADB2CURL.getAuthorizationUrl(CHINA_BASE_URI), expect);
    }

    /**
     * Reference pattern see TOKEN_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetGlobalTokenUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/oauth2/v2.0/token?p=fake-p";
        Assertions.assertEquals(AADB2CURL.getTokenUrl(DEFAULT_BASE_URI, "fake-p"), expect);
    }

    @Test
    public void testGetChinaTokenUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/oauth2/v2.0/token?p=fake-p";
        Assertions.assertEquals(AADB2CURL.getTokenUrl(CHINA_BASE_URI, "fake-p"), expect);
    }

    @Test
    public void testGetTokenUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AADB2CURL.getTokenUrl("", ""));
    }

    @Test
    public void testGetAADTokenUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AADB2CURL.getAADTokenUrl(""));
    }

    @Test
    public void testGetAADTokenUrl() {
        final String expect = "https://login.microsoftonline.com/fake-tenant-id/oauth2/v2.0/token";
        Assertions.assertEquals(AADB2CURL.getAADTokenUrl(B2C_TENANT_ID), expect);
    }

    /**
     * Reference pattern see JWKSET_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetGlobalJwkSetUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/discovery/v2.0/keys?p=new-p";
        Assertions.assertEquals(AADB2CURL.getJwkSetUrl(DEFAULT_BASE_URI, "new-p"), expect);
    }

    @Test
    public void testGetChinaJwkSetUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/discovery/v2.0/keys?p=new-p";
        Assertions.assertEquals(AADB2CURL.getJwkSetUrl(CHINA_BASE_URI, "new-p"), expect);
    }

    @Test
    public void testGetJwkSetUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AADB2CURL.getJwkSetUrl(DEFAULT_BASE_URI, ""));
    }

    @Test
    public void testGetAADJwkSetUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AADB2CURL.getAADJwkSetUrl(""));
    }

    @Test
    public void testGetAADJwkSetUrl() {
        final String expect = "https://login.microsoftonline.com/fake-tenant-id/discovery/v2.0/keys";
        Assertions.assertEquals(AADB2CURL.getAADJwkSetUrl(B2C_TENANT_ID), expect);
    }

    /**
     * Reference pattern see END_SESSION_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetGlobalEndSessionUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/oauth2/v2.0/logout?"
            + "post_logout_redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fhome&p=my-p";

        Assertions.assertEquals(AADB2CURL.getEndSessionUrl(DEFAULT_BASE_URI,
            "http://localhost:8080/home", "my-p"), expect);
    }

    @Test
    public void testGetChinaEndSessionUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/oauth2/v2.0/logout?"
            + "post_logout_redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fhome&p=my-p";

        Assertions.assertEquals(AADB2CURL.getEndSessionUrl(CHINA_BASE_URI,
            "http://localhost:8080/home", "my-p"), expect);
    }

    @Test
    public void testGetEndSessionUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AADB2CURL.getJwkSetUrl(DEFAULT_BASE_URI, ""));
    }
}
