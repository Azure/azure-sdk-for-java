// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebFluxApplicationContextRunnerUtils.reactiveApplicationContextRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebFluxApplicationContextRunnerUtils.reactiveWebApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

public class AadReactiveAutoConfigurationConditionTests {

    private static final String SERVLET_WEB_APPLICATION_CLASS = "org.springframework.web.context.support.GenericWebApplicationContext";

    @Test
    void webFluxApplication() {
        reactiveWebApplicationContextRunner()
            .withPropertyValues(
                "spring.main.web-application-type=reactive",
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> assertThat(context).hasSingleBean(AadAuthenticationProperties.class));
    }

    @Test
    void nonWebFluxApplication() {
        reactiveApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .withClassLoader(new FilteredClassLoader(SERVLET_WEB_APPLICATION_CLASS))
            .run(context -> assertThat(context).doesNotHaveBean(AadAuthenticationProperties.class));
    }

}
