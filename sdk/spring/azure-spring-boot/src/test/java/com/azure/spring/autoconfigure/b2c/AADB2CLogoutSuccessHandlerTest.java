// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AADB2CLogoutSuccessHandlerTest {

    private static final String BASE_URI = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com";

    private static final String TEST_LOGOUT_SUCCESS_URL = "http://localhost:8080/login";

    private static final String TEST_USER_FLOW_SIGN_UP_OR_IN = "my-sign-up-or-in";

    private AADB2CProperties properties;

    @Before
    public void setUp() {
        properties = new AADB2CProperties();

        properties.setBaseUri(BASE_URI);
        properties.setLogoutSuccessUrl(TEST_LOGOUT_SUCCESS_URL);
        properties.getUserFlows().setSignUpOrSignIn(TEST_USER_FLOW_SIGN_UP_OR_IN);
    }

    @Test
    public void testDefaultTargetUrl() {
        final MyLogoutSuccessHandler handler = new MyLogoutSuccessHandler(properties);
        final String baseUri = properties.getBaseUri();
        final String url = properties.getLogoutSuccessUrl();
        final String userFlow = properties.getUserFlows().getSignUpOrSignIn();

        assertThat(handler.getTargetUrl()).isEqualTo(AADB2CURL.getEndSessionUrl(baseUri, url, userFlow));
    }

    private static class MyLogoutSuccessHandler extends AADB2CLogoutSuccessHandler {

        MyLogoutSuccessHandler(AADB2CProperties properties) {
            super(properties);
        }

        public String getTargetUrl() {
            return super.getDefaultTargetUrl();
        }
    }
}
