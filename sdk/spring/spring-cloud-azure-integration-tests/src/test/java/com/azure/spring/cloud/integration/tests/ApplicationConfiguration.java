// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests;

import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;


/**
 * {@link EnableAutoConfiguration} will enable the autoconfiguration classes
 * {@link SpringBootConfiguration} will enable find configuration classes with
 * {@link org.springframework.context.annotation.Configuration} and
 * {@link org.springframework.boot.test.context.TestConfiguration}
 */
@EnableAutoConfiguration
@SpringBootConfiguration
public class ApplicationConfiguration {

    @Bean
    static AzureGlobalPropertiesBeanPostProcessor azureGlobalPropertiesBeanPostProcessor() {
        return new AzureGlobalPropertiesBeanPostProcessor();
    }

    static class AzureGlobalPropertiesBeanPostProcessor implements BeanPostProcessor {

        private static final Logger LOGGER = LoggerFactory.getLogger(AzureGlobalPropertiesBeanPostProcessor.class);
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof AzureGlobalProperties) {
                AzureGlobalProperties azureGlobalProperties = (AzureGlobalProperties) bean;
                String authorityHost = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST);
                LOGGER.info(authorityHost);
                LOGGER.info(AzureAuthorityHosts.AZURE_GOVERNMENT);
                if (AzureAuthorityHosts.AZURE_GOVERNMENT.equals(authorityHost)) {
                    LOGGER.info("US Gov authority host set");
                    azureGlobalProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
                } else if (AzureAuthorityHosts.AZURE_CHINA.equals(authorityHost)) {
                    LOGGER.info("China authority host set");
                    azureGlobalProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
                } else {
                    LOGGER.info("Use Azure Global as the default cloud. ");
                    azureGlobalProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE);
                }
            }
            return bean;
        }

    }
}
