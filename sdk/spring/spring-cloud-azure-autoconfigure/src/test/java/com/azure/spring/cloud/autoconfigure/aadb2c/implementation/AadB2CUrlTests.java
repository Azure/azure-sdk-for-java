// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AadB2CUrlTests {

    private static final String DEFAULT_BASE_URI = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/";
    private static final String CHINA_BASE_URI = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/";
    private static final String B2C_TENANT_ID = "fake-tenant-id";

    /**
     * Reference pattern see AUTHORIZATION_URL_PATTERN of ${@link AadB2CUrl}.
     */
    @Test
    void testGetGlobalAuthorizationUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/oauth2/v2.0/authorize";
        Assertions.assertEquals(AadB2CUrl.getAuthorizationUrl(DEFAULT_BASE_URI), expect);
    }

    @Test
    void testGetChinaAuthorizationUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/oauth2/v2.0/authorize";
        Assertions.assertEquals(AadB2CUrl.getAuthorizationUrl(CHINA_BASE_URI), expect);
    }

    /**
     * Reference pattern see TOKEN_URL_PATTERN of ${@link AadB2CUrl}.
     */
    @Test
    void testGetGlobalTokenUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/oauth2/v2.0/token?p=fake-p";
        Assertions.assertEquals(AadB2CUrl.getTokenUrl(DEFAULT_BASE_URI, "fake-p"), expect);
    }

    @Test
    void testGetChinaTokenUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/oauth2/v2.0/token?p=fake-p";
        Assertions.assertEquals(AadB2CUrl.getTokenUrl(CHINA_BASE_URI, "fake-p"), expect);
    }

    @Test
    void testGetTokenUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AadB2CUrl.getTokenUrl("", ""));
    }

    @Test
    void testGetAADTokenUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AadB2CUrl.getAADTokenUrl(""));
    }

    @Test
    void testGetAADTokenUrl() {
        final String expect = "https://login.microsoftonline.com/fake-tenant-id/oauth2/v2.0/token";
        Assertions.assertEquals(AadB2CUrl.getAADTokenUrl(B2C_TENANT_ID), expect);
    }

    /**
     * Reference pattern see JWKSET_URL_PATTERN of ${@link AadB2CUrl}.
     */
    @Test
    void testGetGlobalJwkSetUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/discovery/v2.0/keys?p=new-p";
        Assertions.assertEquals(AadB2CUrl.getJwkSetUrl(DEFAULT_BASE_URI, "new-p"), expect);
    }

    @Test
    void testGetChinaJwkSetUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/discovery/v2.0/keys?p=new-p";
        Assertions.assertEquals(AadB2CUrl.getJwkSetUrl(CHINA_BASE_URI, "new-p"), expect);
    }

    @Test
    void testGetJwkSetUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AadB2CUrl.getJwkSetUrl(DEFAULT_BASE_URI, ""));
    }

    @Test
    void testGetAADJwkSetUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AadB2CUrl.getAADJwkSetUrl(""));
    }

    @Test
    void testGetAADJwkSetUrl() {
        final String expect = "https://login.microsoftonline.com/fake-tenant-id/discovery/v2.0/keys";
        Assertions.assertEquals(AadB2CUrl.getAADJwkSetUrl(B2C_TENANT_ID), expect);
    }

    /**
     * Reference pattern see END_SESSION_URL_PATTERN of ${@link AadB2CUrl}.
     */
    @Test
    void testGetGlobalEndSessionUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/oauth2/v2.0/logout?"
            + "post_logout_redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fhome&p=my-p";

        Assertions.assertEquals(AadB2CUrl.getEndSessionUrl(DEFAULT_BASE_URI,
            "http://localhost:8080/home", "my-p"), expect);
    }

    @Test
    void testGetChinaEndSessionUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/oauth2/v2.0/logout?"
            + "post_logout_redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fhome&p=my-p";

        Assertions.assertEquals(AadB2CUrl.getEndSessionUrl(CHINA_BASE_URI,
            "http://localhost:8080/home", "my-p"), expect);
    }

    @Test
    void testGetEndSessionUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> AadB2CUrl.getJwkSetUrl(DEFAULT_BASE_URI, ""));
    }
}
