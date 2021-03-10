// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AADB2CURLTest {

    static final String DEFAULT_BASE_URI = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/";
    static final String CHINA_BASE_URI = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/";

    /**
     * Reference pattern see AUTHORIZATION_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetGlobalAuthorizationUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/oauth2/v2.0/authorize";
        assertThat(AADB2CURL.getAuthorizationUrl(DEFAULT_BASE_URI)).isEqualTo(expect);
    }

    @Test
    public void testGetChinaAuthorizationUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/oauth2/v2.0/authorize";
        assertThat(AADB2CURL.getAuthorizationUrl(CHINA_BASE_URI)).isEqualTo(expect);
    }

    /**
     * Reference pattern see TOKEN_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetGlobalTokenUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/oauth2/v2.0/token?p=fake-p";
        assertThat(AADB2CURL.getTokenUrl(DEFAULT_BASE_URI, "fake-p")).isEqualTo(expect);
    }

    @Test
    public void testGetChinaTokenUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/oauth2/v2.0/token?p=fake-p";
        assertThat(AADB2CURL.getTokenUrl(CHINA_BASE_URI, "fake-p")).isEqualTo(expect);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTokenUrlException() {
        AADB2CURL.getTokenUrl("", "");
    }

    /**
     * Reference pattern see JWKSET_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetGlobalJwkSetUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/discovery/v2.0/keys?p=new-p";
        assertThat(AADB2CURL.getJwkSetUrl(DEFAULT_BASE_URI, "new-p")).isEqualTo(expect);
    }

    @Test
    public void testGetChinaJwkSetUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/discovery/v2.0/keys?p=new-p";
        assertThat(AADB2CURL.getJwkSetUrl(CHINA_BASE_URI, "new-p")).isEqualTo(expect);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetJwkSetUrlException() {
        AADB2CURL.getJwkSetUrl(DEFAULT_BASE_URI, "");
    }

    /**
     * Reference pattern see END_SESSION_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetGlobalEndSessionUrl() {
        final String expect = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/oauth2/v2.0/logout?"
            + "post_logout_redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fhome&p=my-p";

        assertThat(AADB2CURL.getEndSessionUrl(DEFAULT_BASE_URI, "http://localhost:8080/home",
            "my-p")).isEqualTo(expect);
    }

    @Test
    public void testGetChinaEndSessionUrl() {
        final String expect = "https://faketenant.b2clogin.cn/faketenant.partner.onmschina.cn/oauth2/v2.0/logout?"
            + "post_logout_redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fhome&p=my-p";

        assertThat(AADB2CURL.getEndSessionUrl(CHINA_BASE_URI, "http://localhost:8080/home",
            "my-p")).isEqualTo(expect);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEndSessionUrlException() {
        AADB2CURL.getJwkSetUrl(DEFAULT_BASE_URI, "");
    }
}
