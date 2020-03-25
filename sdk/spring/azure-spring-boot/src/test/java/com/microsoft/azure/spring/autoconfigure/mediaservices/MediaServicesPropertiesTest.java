// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.mediaservices;

import com.microsoft.azure.spring.utils.TestUtils;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.CLIENT_ID;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.CLIENT_ID_PROP;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.CLIENT_SECRET;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.CLIENT_SECRET_PROP;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.REST_API_ENDPOINT;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.REST_API_ENDPOINT_PROP;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.TENANT;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.TENANT_PROP;
import static org.assertj.core.api.Assertions.assertThat;

public class MediaServicesPropertiesTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MediaServicesAutoConfiguration.class))
            .withPropertyValues(TestUtils.propPair(TENANT_PROP, TENANT),
                    TestUtils.propPair(CLIENT_ID_PROP, CLIENT_ID),
                    TestUtils.propPair(CLIENT_SECRET_PROP, CLIENT_SECRET),
                    TestUtils.propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT));

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
        contextRunner.withPropertyValues(TestUtils.propPair(TENANT_PROP, ""), TestUtils.propPair(CLIENT_ID_PROP, CLIENT_ID),
                TestUtils.propPair(CLIENT_SECRET_PROP, CLIENT_SECRET), TestUtils.propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT))
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
        contextRunner.withPropertyValues(TestUtils.propPair(TENANT_PROP, TENANT), TestUtils.propPair(CLIENT_ID_PROP, ""),
                TestUtils.propPair(CLIENT_SECRET_PROP, CLIENT_SECRET), TestUtils.propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT))
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
        contextRunner.withPropertyValues(TestUtils.propPair(TENANT_PROP, ""), TestUtils.propPair(CLIENT_ID_PROP, CLIENT_ID),
                TestUtils.propPair(CLIENT_SECRET_PROP, ""), TestUtils.propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT))
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
        contextRunner.withPropertyValues(TestUtils.propPair(TENANT_PROP, ""), TestUtils.propPair(CLIENT_ID_PROP, CLIENT_ID),
                TestUtils.propPair(CLIENT_SECRET_PROP, CLIENT_SECRET), TestUtils.propPair(REST_API_ENDPOINT_PROP, ""))
                .run(context -> {
                    try {
                        context.getBean(MediaServicesProperties.class);
                    } catch (IllegalStateException e) {
                        assertThat(e.getCause().getCause()).isInstanceOf(ConfigurationPropertiesBindException.class);
                    }
                });
    }
}

