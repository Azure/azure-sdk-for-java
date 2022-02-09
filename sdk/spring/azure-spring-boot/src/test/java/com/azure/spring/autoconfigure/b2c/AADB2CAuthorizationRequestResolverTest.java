// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class AADB2CAuthorizationRequestResolverTest {

    private WebApplicationContextRunner getContextRunner() {
        return new WebApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withConfiguration(AutoConfigurations.of(AbstractAADB2COAuth2ClientTestConfiguration.WebOAuth2ClientApp.class,
                AADB2CAutoConfiguration.class))
            .withPropertyValues(
                String.format("%s=%s", AADB2CConstants.TENANT_ID, AADB2CConstants.TEST_TENANT_ID),
                String.format("%s=%s", AADB2CConstants.BASE_URI, AADB2CConstants.TEST_BASE_URI),
                String.format("%s=%s", AADB2CConstants.CLIENT_ID, AADB2CConstants.TEST_CLIENT_ID),
                String.format("%s=%s", AADB2CConstants.CLIENT_SECRET, AADB2CConstants.TEST_CLIENT_SECRET),
                String.format("%s=%s", AADB2CConstants.LOGOUT_SUCCESS_URL, AADB2CConstants.TEST_LOGOUT_SUCCESS_URL),
                String.format("%s=%s", AADB2CConstants.LOGIN_FLOW, AADB2CConstants.TEST_KEY_SIGN_UP_OR_IN),
                String.format("%s.%s=%s", AADB2CConstants.USER_FLOWS,
                    AADB2CConstants.TEST_KEY_SIGN_UP_OR_IN, AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME),
                String.format("%s.%s=%s", AADB2CConstants.USER_FLOWS,
                    AADB2CConstants.TEST_KEY_PROFILE_EDIT, AADB2CConstants.TEST_PROFILE_EDIT_NAME),
                String.format("%s=%s", AADB2CConstants.CONFIG_PROMPT, AADB2CConstants.TEST_PROMPT),
                String.format("%s=%s", AADB2CConstants.CONFIG_LOGIN_HINT, AADB2CConstants.TEST_LOGIN_HINT)
            );
    }

    private HttpServletRequest getHttpServletRequest(String uri) {
        Assert.hasText(uri, "uri must contain text.");

        final MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.toString(), uri);

        request.setServletPath(uri);

        return request;
    }

    @Test
    public void testAutoConfigurationBean() {
        getContextRunner().run(c -> {
            String requestUri = "/fake-url";
            HttpServletRequest request = getHttpServletRequest(requestUri);
            final String registrationId = AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME;
            final AADB2CAuthorizationRequestResolver resolver = c.getBean(AADB2CAuthorizationRequestResolver.class);

            Assertions.assertNotNull(resolver);
            Assertions.assertNull(resolver.resolve(request));
            Assertions.assertNull(resolver.resolve(request, registrationId));

            requestUri = "/oauth2/authorization/" + AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME;
            request = getHttpServletRequest(requestUri);

            Assertions.assertNotNull(resolver.resolve(request));
            Assertions.assertNotNull(resolver.resolve(request, registrationId));

            Assertions.assertEquals(resolver.resolve(request).getAdditionalParameters().get("p"), AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME);
            Assertions.assertEquals(resolver.resolve(request).getAdditionalParameters().get(AADB2CConstants.PROMPT), AADB2CConstants.TEST_PROMPT);
            Assertions.assertEquals(resolver.resolve(request).getAdditionalParameters().get(AADB2CConstants.LOGIN_HINT), AADB2CConstants.TEST_LOGIN_HINT);
            Assertions.assertEquals((resolver.resolve(request).getClientId()), AADB2CConstants.TEST_CLIENT_ID);
            Assertions.assertEquals((resolver.resolve(request).getGrantType()), AuthorizationGrantType.AUTHORIZATION_CODE);
            Assertions.assertTrue(resolver.resolve(request).getScopes().containsAll(Arrays.asList("openid", AADB2CConstants.TEST_CLIENT_ID)));
        });
    }
}
