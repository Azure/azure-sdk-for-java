// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests;

import com.azure.spring.cloud.autoconfigure.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * {@link EnableAutoConfiguration} will enable the autoconfiguration classes
 * {@link SpringBootConfiguration} will enable find configuration classes with
 * {@link org.springframework.context.annotation.Configuration} and
 * {@link org.springframework.boot.test.context.TestConfiguration}
 */
@EnableAutoConfiguration
@SpringBootConfiguration
public class ApplicationConfiguration {
    private static final String usGovAuthorityHost = "https://login.microsoftonline.us";
    private static final String chinaAuthorityHost = "https://login.chinacloudapi.cn";
    public static void ensureCloudType() {
        AzureProfileConfigurationProperties properties =
            new AzureProfileConfigurationProperties();
        String azureAuthorityHost = System.getenv("AZURE_AUTHORITY_HOST");
        if (usGovAuthorityHost.equals(azureAuthorityHost)) {
            properties.setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        }
        if (chinaAuthorityHost.equals(azureAuthorityHost)) {
           properties.setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        }
    }
}
