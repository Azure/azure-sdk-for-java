// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class AADB2CAuthorizationRequestResolverTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AADB2CAutoConfiguration.class))
            .withPropertyValues(
                    String.format("%s=%s", AADB2CConstants.BASE_URI, AADB2CConstants.TEST_BASE_URI),
                    String.format("%s=%s", AADB2CConstants.CLIENT_ID, AADB2CConstants.TEST_CLIENT_ID),
                    String.format("%s=%s", AADB2CConstants.CLIENT_SECRET, AADB2CConstants.TEST_CLIENT_SECRET),
                    String.format("%s=%s", AADB2CConstants.LOGOUT_SUCCESS_URL, AADB2CConstants.TEST_LOGOUT_SUCCESS_URL),
                    String.format("%s=%s", AADB2CConstants.SIGN_UP_OR_SIGN_IN, AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME),
                    String.format("%s=%s", AADB2CConstants.CONFIG_PROMPT, AADB2CConstants.TEST_PROMPT),
                    String.format("%s=%s", AADB2CConstants.CONFIG_LOGIN_HINT, AADB2CConstants.TEST_LOGIN_HINT)
            );

    private HttpServletRequest getHttpServletRequest(String uri) {
        Assert.hasText(uri, "uri must contain text.");

        final MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.toString(), uri);

        request.setServletPath(uri);

        return request;
    }

    @Test
    public void testAutoConfigurationBean() {
        this.contextRunner.run(c -> {
            String requestUri = "/fake-url";
            HttpServletRequest request = getHttpServletRequest(requestUri);
            final String registrationId = AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME;
            final AADB2CAuthorizationRequestResolver resolver = c.getBean(AADB2CAuthorizationRequestResolver.class);

            assertThat(resolver).isNotNull();
            assertThat(resolver.resolve(request)).isNull();
            assertThat(resolver.resolve(request, registrationId)).isNull();

            requestUri = "/oauth2/authorization/" + AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME;
            request = getHttpServletRequest(requestUri);

            assertThat(resolver.resolve(request)).isNotNull();
            assertThat(resolver.resolve(request, registrationId)).isNotNull();

            assertThat(resolver.resolve(request).getAdditionalParameters().get("p")).isEqualTo(AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME);
            assertThat(resolver.resolve(request).getAdditionalParameters().get(AADB2CConstants.PROMPT)).isEqualTo(AADB2CConstants.TEST_PROMPT);
            assertThat(resolver.resolve(request).getAdditionalParameters().get(AADB2CConstants.LOGIN_HINT)).isEqualTo(AADB2CConstants.TEST_LOGIN_HINT);
            assertThat(resolver.resolve(request).getClientId()).isEqualTo(AADB2CConstants.TEST_CLIENT_ID);
            assertThat(resolver.resolve(request).getGrantType()).isEqualTo(AuthorizationGrantType.AUTHORIZATION_CODE);
            assertThat(resolver.resolve(request).getScopes()).contains("openid", AADB2CConstants.TEST_CLIENT_ID);
        });
    }
}
