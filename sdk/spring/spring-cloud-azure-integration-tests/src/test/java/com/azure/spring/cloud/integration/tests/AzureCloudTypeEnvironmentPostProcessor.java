// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests;

import com.azure.spring.cloud.autoconfigure.keyvault.environment.KeyVaultEnvironmentPostProcessor;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class AzureCloudTypeEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String CLOUD_TYPE_PROPERTY_KEY = "spring.cloud.azure.profile.cloud-type";
    private final Log logger;

    public AzureCloudTypeEnvironmentPostProcessor(Log logger) {
        this.logger = logger;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String authorityHost = environment.getProperty("AZURE_AUTHORITY_HOST");
        if (!StringUtils.hasText(authorityHost)) {
            logger.info("No AZURE_AUTHORITY_HOST set.");
            return;
        }
        Map<String, Object> props = new HashMap<>();
        MapPropertySource mapPropertySource = new MapPropertySource("spring-cloud-azure-cloud-type", props);

        logger.info("The set AZURE_AUTHORITY_HOST is [" + authorityHost + "].");

        if (authorityHost.startsWith("https://login.microsoftonline.us")) {
            props.put(CLOUD_TYPE_PROPERTY_KEY, AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
            logger.info("US Gov environment set.");
        } else if (authorityHost.startsWith("https://login.chinacloudapi.cn")) {
            props.put(CLOUD_TYPE_PROPERTY_KEY, AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
            logger.info("China environment set.");
        } else if (authorityHost.startsWith("https://login.microsoftonline.com")) {
            props.put(CLOUD_TYPE_PROPERTY_KEY, AzureProfileOptionsProvider.CloudType.AZURE);
            logger.info("Azure Cloud environment set.");
        }

        environment.getPropertySources().addLast(mapPropertySource);
    }

    @Override
    public int getOrder() {
        return KeyVaultEnvironmentPostProcessor.ORDER - 1;
    }
}
