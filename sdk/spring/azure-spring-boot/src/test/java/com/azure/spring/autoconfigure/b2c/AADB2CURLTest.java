// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class AADB2CURLTest {

    /**
     * Reference pattern see AUTHORIZATION_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetAuthorizationUrl() {
        final String expect = "https://fake-tenant.b2clogin.com/fake-tenant.onmicrosoft.com/oauth2/v2.0/authorize";

        assertThat(AADB2CURL.getAuthorizationUrl("fake-tenant")).isEqualTo(expect);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAuthorizationUrlException() {
        AADB2CURL.getAuthorizationUrl("");
    }

    /**
     * Reference pattern see TOKEN_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetTokenUrl() {
        final String expect = "https://fake-tenant.b2clogin.com/fake-tenant.onmicrosoft.com/oauth2/v2.0/token?p=fake-p";

        assertThat(AADB2CURL.getTokenUrl("fake-tenant", "fake-p")).isEqualTo(expect);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTokenUrlException() {
        AADB2CURL.getTokenUrl("", "");
    }

    /**
     * Reference pattern see JWKSET_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetJwkSetUrl() {
        final String expect = "https://new-tenant.b2clogin.com/new-tenant.onmicrosoft.com/discovery/v2.0/keys?p=new-p";

        assertThat(AADB2CURL.getJwkSetUrl("new-tenant", "new-p")).isEqualTo(expect);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetJwkSetUrlException() {
        AADB2CURL.getJwkSetUrl("", "");
    }

    /**
     * Reference pattern see END_SESSION_URL_PATTERN of ${@link AADB2CURL}.
     */
    @Test
    public void testGetEndSessionUrl() {
        final String expect = "https://my-tenant.b2clogin.com/my-tenant.onmicrosoft.com/oauth2/v2.0/logout?"
            + "post_logout_redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fhome&p=my-p";

        assertThat(AADB2CURL.getEndSessionUrl("my-tenant", "http://localhost:8080/home",
            "my-p")).isEqualTo(expect);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEndSessionUrlException() {
        AADB2CURL.getJwkSetUrl("", "");
    }
}
