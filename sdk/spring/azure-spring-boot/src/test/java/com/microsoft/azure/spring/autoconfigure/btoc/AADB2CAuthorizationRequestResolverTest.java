// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.btoc;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

import static com.microsoft.azure.spring.autoconfigure.btoc.AADB2CConstants.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class AADB2CAuthorizationRequestResolverTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AADB2CAutoConfiguration.class))
            .withPropertyValues(
                    String.format("%s=%s", TENANT, TEST_TENANT),
                    String.format("%s=%s", CLIENT_ID, TEST_CLIENT_ID),
                    String.format("%s=%s", CLIENT_SECRET, TEST_CLIENT_SECRET),
                    String.format("%s=%s", REPLY_URL, TEST_REPLY_URL),
                    String.format("%s=%s", LOGOUT_SUCCESS_URL, TEST_LOGOUT_SUCCESS_URL),
                    String.format("%s=%s", SIGN_UP_OR_SIGN_IN, TEST_SIGN_UP_OR_IN_NAME)
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
            final String registrationId = TEST_SIGN_UP_OR_IN_NAME;
            final AADB2CAuthorizationRequestResolver resolver = c.getBean(AADB2CAuthorizationRequestResolver.class);

            assertThat(resolver).isNotNull();
            assertThat(resolver.resolve(request)).isNull();
            assertThat(resolver.resolve(request, registrationId)).isNull();

            requestUri = "/oauth2/authorization/" + TEST_SIGN_UP_OR_IN_NAME;
            request = getHttpServletRequest(requestUri);

            assertThat(resolver.resolve(request)).isNotNull();
            assertThat(resolver.resolve(request, registrationId)).isNotNull();

            assertThat(resolver.resolve(request).getAdditionalParameters().get("p")).isEqualTo(TEST_SIGN_UP_OR_IN_NAME);
            assertThat(resolver.resolve(request).getClientId()).isEqualTo(TEST_CLIENT_ID);
            assertThat(resolver.resolve(request).getRedirectUri()).isEqualTo(TEST_REPLY_URL);
            assertThat(resolver.resolve(request).getGrantType()).isEqualTo(AuthorizationGrantType.AUTHORIZATION_CODE);
            assertThat(resolver.resolve(request).getScopes()).contains("openid", TEST_CLIENT_ID);
        });
    }
}
