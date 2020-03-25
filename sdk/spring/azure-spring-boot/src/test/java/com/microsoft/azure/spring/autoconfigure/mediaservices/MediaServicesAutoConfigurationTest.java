// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.mediaservices;

import com.microsoft.azure.spring.utils.TestUtils;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaContract;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.CLIENT_ID;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.CLIENT_ID_PROP;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.CLIENT_SECRET;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.CLIENT_SECRET_PROP;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.PROXY_HOST;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.PROXY_HOST_PROP;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.PROXY_PORT;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.PROXY_PORT_PROP;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.REST_API_ENDPOINT;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.REST_API_ENDPOINT_PROP;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.TENANT;
import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.TENANT_PROP;
import static org.assertj.core.api.Assertions.assertThat;

public class MediaServicesAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MediaServicesAutoConfiguration.class))
            .withPropertyValues(TestUtils.propPair(TENANT_PROP, TENANT),
                    TestUtils.propPair(CLIENT_ID_PROP, CLIENT_ID),
                    TestUtils.propPair(CLIENT_SECRET_PROP, CLIENT_SECRET),
                    TestUtils.propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT));

    @Test
    public void mediaContractBeanCanBeCreated() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(MediaContract.class));
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void byDefaultMediaContractBeanNotCreated() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MediaServicesAutoConfiguration.class))
                .withPropertyValues().run(context -> context.getBean(MediaContract.class));
    }

    @Test
    public void createMediaServiceAccountWithProxyHostMissing() {
        contextRunner.withPropertyValues(TestUtils.propPair(PROXY_PORT_PROP, PROXY_PORT)).run(context -> {
            try {
                context.getBean(MediaContract.class);
            } catch (IllegalStateException e) {
                assertThat(ExceptionUtils.indexOfThrowable(e, ServiceException.class)).isGreaterThan(-1);
            }
        });
    }

    @Test
    public void createMediaServiceAccountWithProxyPortMissing() {
        contextRunner.withPropertyValues(TestUtils.propPair(PROXY_HOST_PROP, PROXY_HOST)).run(context -> {
            try {
                context.getBean(MediaContract.class);
            } catch (IllegalStateException e) {
                assertThat(ExceptionUtils.indexOfThrowable(e, ServiceException.class)).isGreaterThan(-1);
            }
        });
    }
}
