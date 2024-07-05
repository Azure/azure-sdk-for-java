// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.oauthClientAndResourceServerRunner;
import static org.assertj.core.api.Assertions.assertThat;

class AadAutoConfigurationServletConditionTests {

    private static final String SERVLET_WEB_APPLICATION_CLASS = "org.springframework.web.context.support.GenericWebApplicationContext";

    @Test
    void servletApplication() {
        oauthClientAndResourceServerRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> assertThat(context).hasSingleBean(AadAuthenticationProperties.class));
    }

    @Test
    void nonServletApplication() {
        oauthClientAndResourceServerRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .withClassLoader(new FilteredClassLoader(SERVLET_WEB_APPLICATION_CLASS))
            .run(context -> assertThat(context).doesNotHaveBean(AadAuthenticationProperties.class));
    }
}
