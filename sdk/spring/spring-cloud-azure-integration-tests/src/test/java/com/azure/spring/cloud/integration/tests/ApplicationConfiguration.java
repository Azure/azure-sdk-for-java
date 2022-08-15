// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
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
        AzureGlobalProperties azureGlobalProperties = new AzureGlobalProperties();
        AzureProfileConfigurationProperties azureProfileConfigurationProperties = azureGlobalProperties.getProfile();
        System.out.println("CloudType"+azureProfileConfigurationProperties.getCloudType());
        String azureAuthorityHost = System.getenv("AZURE_AUTHORITY_HOST");
        System.out.println("azureAuthorityHost"+azureAuthorityHost);
        if (usGovAuthorityHost.equals(azureAuthorityHost)) {
            System.out.println("US GOVERNMENT");
            azureProfileConfigurationProperties.setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        }
        if (chinaAuthorityHost.equals(azureAuthorityHost)) {
            System.out.println("CHINA");
            azureProfileConfigurationProperties.setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        }
    }
}
