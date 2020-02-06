/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceRetrieverTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AADAuthenticationFilterAutoConfiguration.class))
            .withPropertyValues("azure.activedirectory.client-id=fake-client-id",
                    "azure.activedirectory.client-secret=fake-client-secret",
                    "azure.activedirectory.active-directory-groups=fake-group",
                    "azure.service.endpoints.global.aadKeyDiscoveryUri=http://fake.aad.discovery.uri");

    @Test
    public void resourceRetrieverDefaultConfig() {
        this.contextRunner.run(context -> {
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
    public void resourceRetriverIsConfigurable() {
        this.contextRunner.withPropertyValues("azure.activedirectory.jwt-connect-timeout=1234",
                "azure.activedirectory.jwt-read-timeout=1234",
                "azure.activedirectory.jwt-size-limit=123400",
                "azure.service.endpoints.global.aadKeyDiscoveryUri=http://fake.aad.discovery.uri")
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
