// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security;

import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AadB2cProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AadB2cLogoutSuccessHandlerTests {

    private static final String BASE_URI = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com";

    private static final String TEST_LOGOUT_SUCCESS_URL = "http://localhost:8080/login";

    private static final String TEST_USER_FLOW_SIGN_UP_OR_IN = "my-sign-up-or-in";

    private AadB2cProperties properties;

    @BeforeAll
    void setUp() {
        properties = new AadB2cProperties();

        properties.setBaseUri(BASE_URI);
        properties.setLogoutSuccessUrl(TEST_LOGOUT_SUCCESS_URL);
        properties.getUserFlows().put(AadB2cProperties.DEFAULT_KEY_SIGN_UP_OR_SIGN_IN, TEST_USER_FLOW_SIGN_UP_OR_IN);
    }

    @Test
    void testDefaultTargetUrl() {
        final MyLogoutSuccessHandler handler = new MyLogoutSuccessHandler(properties);
        final String baseUri = properties.getBaseUri();
        final String url = properties.getLogoutSuccessUrl();
        final String userFlow = properties.getUserFlows().get(properties.getLoginFlow());

        assertThat(handler.getTargetUrl()).isEqualTo(AadB2cUrl.getEndSessionUrl(baseUri, url, userFlow));
    }

    private static class MyLogoutSuccessHandler extends AadB2cLogoutSuccessHandler {

        MyLogoutSuccessHandler(AadB2cProperties properties) {
            super(properties);
        }

        String getTargetUrl() {
            return super.getDefaultTargetUrl();
        }
    }
}
