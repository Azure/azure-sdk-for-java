// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AzureCloudTypeConfiguration {
    @Bean
    static AzureGlobalPropertiesBeanPostProcessor azureGlobalPropertiesBeanPostProcessor() {
        return new AzureGlobalPropertiesBeanPostProcessor();
    }

    static class AzureGlobalPropertiesBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

        private static final Logger LOGGER = LoggerFactory.getLogger(AzureGlobalPropertiesBeanPostProcessor.class);
        private Environment environment;


        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof AzureGlobalProperties) {
                AzureGlobalProperties azureGlobalProperties = (AzureGlobalProperties) bean;

                String authorityHost = this.environment.getProperty("AZURE_AUTHORITY_HOST");

                LOGGER.info("The set AZURE_AUTHORITY_HOST is [{}]", authorityHost);
                if (authorityHost.startsWith("https://login.microsoftonline.us")) {
                    azureGlobalProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
                    LOGGER.info("US Gov environment set.");
                } else if (authorityHost.startsWith("https://login.chinacloudapi.cn")) {
                    azureGlobalProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
                    LOGGER.info("China environment set.");
                } else if (authorityHost.startsWith("https://login.microsoftonline.com")) {
                    azureGlobalProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE);
                    LOGGER.info("Azure Cloud environment set.");
                } else {
                    azureGlobalProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE);
                    LOGGER.info("Use Azure Global as the default cloud. ");
                }
            }
            return bean;
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }
    }
}
