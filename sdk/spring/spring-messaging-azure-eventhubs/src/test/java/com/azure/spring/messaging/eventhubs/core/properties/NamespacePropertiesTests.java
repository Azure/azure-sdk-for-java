// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.messaging.eventhubs.core.properties;

import com.azure.spring.cloud.core.properties.profile.AzureEnvironmentProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamespacePropertiesTests {

    @Test
    void domainNameConfigureByDefault() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();

        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE, namespaceProperties.getProfile().getCloudType());
        assertEquals(AzureEnvironmentProperties.AZURE.getServiceBusDomainName(), namespaceProperties.getDomainName());
    }

    @Test
    void domainNameConfiguredAsCloud() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY);

        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY, namespaceProperties.getProfile().getCloudType());
        assertEquals(AzureEnvironmentProperties.AZURE_GERMANY.getServiceBusDomainName(), namespaceProperties.getDomainName());
    }

    @Test
    void domainNameOverrideCloud() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setDomainName("servicebus.chinacloudapi.cn");
        namespaceProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY);

        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY, namespaceProperties.getProfile().getCloudType());
        assertEquals(AzureEnvironmentProperties.AZURE_CHINA.getServiceBusDomainName(), namespaceProperties.getDomainName());
    }


}
