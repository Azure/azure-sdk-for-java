/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.mediaservices;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaContract;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.microsoft.azure.spring.autoconfigure.mediaservices.Constants.*;
import static com.microsoft.azure.utils.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;

public class MediaServicesAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MediaServicesAutoConfiguration.class))
            .withPropertyValues(propPair(TENANT_PROP, TENANT),
                    propPair(CLIENT_ID_PROP, CLIENT_ID),
                    propPair(CLIENT_SECRET_PROP, CLIENT_SECRET),
                    propPair(REST_API_ENDPOINT_PROP, REST_API_ENDPOINT));

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
        contextRunner.withPropertyValues(propPair(PROXY_PORT_PROP, PROXY_PORT)).run(context -> {
            try {
                context.getBean(MediaContract.class);
            } catch (IllegalStateException e) {
                assertThat(ExceptionUtils.indexOfThrowable(e, ServiceException.class)).isGreaterThan(-1);
            }
        });
    }

    @Test
    public void createMediaServiceAccountWithProxyPortMissing() {
        contextRunner.withPropertyValues(propPair(PROXY_HOST_PROP, PROXY_HOST)).run(context -> {
            try {
                context.getBean(MediaContract.class);
            } catch (IllegalStateException e) {
                assertThat(ExceptionUtils.indexOfThrowable(e, ServiceException.class)).isGreaterThan(-1);
            }
        });
    }
}
