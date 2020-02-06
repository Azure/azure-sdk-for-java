/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.mediaservices;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.*;
import static com.microsoft.azure.utils.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;

public class MediaServicesPropertiesTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MediaServicesAutoConfiguration.class))
            .withPropertyValues(propPair(TENANT_PROP, TENANT),
                    propPair(CLIENT_ID_PROP, CLIENT_ID),
                    propPair(CLIENT_SECRET_PROP, CLIENT_SECRET),
                    propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT));

    @Test
    public void canSetProperties() {
        contextRunner.run(context -> {
            final MediaServicesProperties properties = context.getBean(MediaServicesProperties.class);
            assertThat(properties).isNotNull();
            assertThat(properties.getTenant()).isEqualTo(TENANT);
            assertThat(properties.getClientId()).isEqualTo(CLIENT_ID);
            assertThat(properties.getClientSecret()).isEqualTo(CLIENT_SECRET);
            assertThat(properties.getRestApiEndpoint()).isEqualTo(REST_API_ENDPOINT);
        });
    }


    @Test
    public void emptyTenantNotAllowed() {
        contextRunner.withPropertyValues(propPair(TENANT_PROP, ""), propPair(CLIENT_ID_PROP, CLIENT_ID),
                propPair(CLIENT_SECRET_PROP, CLIENT_SECRET), propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT))
                .run(context -> {
                    try {
                        context.getBean(MediaServicesProperties.class);
                    } catch (IllegalStateException e) {
                        assertThat(e.getCause().getCause()).isInstanceOf(ConfigurationPropertiesBindException.class);
                    }
                });
    }

    @Test
    public void emptyClientIdtNotAllowed() {
        contextRunner.withPropertyValues(propPair(TENANT_PROP, TENANT), propPair(CLIENT_ID_PROP, ""),
                propPair(CLIENT_SECRET_PROP, CLIENT_SECRET), propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT))
                .run(context -> {
                    try {
                        context.getBean(MediaServicesProperties.class);
                    } catch (IllegalStateException e) {
                        assertThat(e.getCause().getCause()).isInstanceOf(ConfigurationPropertiesBindException.class);
                    }
                });
    }

    @Test
    public void emptyClientSecretNotAllowed() {
        contextRunner.withPropertyValues(propPair(TENANT_PROP, ""), propPair(CLIENT_ID_PROP, CLIENT_ID),
                propPair(CLIENT_SECRET_PROP, ""), propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT))
                .run(context -> {
                    try {
                        context.getBean(MediaServicesProperties.class);
                    } catch (IllegalStateException e) {
                        assertThat(e.getCause().getCause()).isInstanceOf(ConfigurationPropertiesBindException.class);
                    }
                });
    }

    @Test
    public void emptyRestApiEndpointNotAllowed() {
        contextRunner.withPropertyValues(propPair(TENANT_PROP, ""), propPair(CLIENT_ID_PROP, CLIENT_ID),
                propPair(CLIENT_SECRET_PROP, CLIENT_SECRET), propPair(REST_API_ENDPOINT_PROP, ""))
                .run(context -> {
                    try {
                        context.getBean(MediaServicesProperties.class);
                    } catch (IllegalStateException e) {
                        assertThat(e.getCause().getCause()).isInstanceOf(ConfigurationPropertiesBindException.class);
                    }
                });
    }
}

