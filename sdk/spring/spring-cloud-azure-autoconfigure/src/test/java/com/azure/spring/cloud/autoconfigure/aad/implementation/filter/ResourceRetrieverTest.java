// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.filter;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceRetrieverTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AADAuthenticationFilterAutoConfiguration.class))
        .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
        .withPropertyValues(
            "spring.cloud.azure.active-directory.enabled=true",
            "spring.cloud.azure.active-directory.client-id=fake-client-id",
            "spring.cloud.azure.active-directory.client-secret=fake-client-secret"
        );

    @Test
    void resourceRetrieverDefaultConfig() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(ResourceRetriever.class);
                final ResourceRetriever retriever = context.getBean(ResourceRetriever.class);
                assertThat(retriever).isInstanceOf(DefaultResourceRetriever.class);

                final DefaultResourceRetriever defaultRetriever = (DefaultResourceRetriever) retriever;
                assertThat(defaultRetriever.getConnectTimeout()).isEqualTo(RemoteJWKSet.DEFAULT_HTTP_CONNECT_TIMEOUT);
                assertThat(defaultRetriever.getReadTimeout()).isEqualTo(RemoteJWKSet.DEFAULT_HTTP_READ_TIMEOUT);
                assertThat(defaultRetriever.getSizeLimit()).isEqualTo(RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT);
            });
    }

    @Test
    void resourceRetriverIsConfigurable() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.jwt-connect-timeout=1234",
                "spring.cloud.azure.active-directory.jwt-read-timeout=1234",
                "spring.cloud.azure.active-directory.jwt-size-limit=123400")
            .run(context -> {
                assertThat(context).hasSingleBean(ResourceRetriever.class);
                final ResourceRetriever retriever = context.getBean(ResourceRetriever.class);
                assertThat(retriever).isInstanceOf(DefaultResourceRetriever.class);

                final DefaultResourceRetriever defaultRetriever = (DefaultResourceRetriever) retriever;
                assertThat(defaultRetriever.getConnectTimeout()).isEqualTo(1234);
                assertThat(defaultRetriever.getReadTimeout()).isEqualTo(1234);
                assertThat(defaultRetriever.getSizeLimit()).isEqualTo(123400);
            });
    }
}
