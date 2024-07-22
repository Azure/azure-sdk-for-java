// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import com.azure.spring.cloud.core.properties.profile.AzureEnvironmentProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MAX_DURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServiceBusProducerPropertiesTests {

    static final String CONNECTION_STRING_PATTERN = "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";
    private ServiceBusProducerProperties producerProperties;

    @BeforeEach
    void beforeEach() {
        producerProperties = new ServiceBusProducerProperties();
    }

    @Test
    void sendTimeoutDefaultsTo10000() {
        assertEquals(Duration.ofMillis(10000), producerProperties.getSendTimeout());
    }

    @Test
    void customSendTimeout() {
        Duration duration = Duration.ofSeconds(10);
        producerProperties.setSendTimeout(duration);
        assertEquals(duration, producerProperties.getSendTimeout());
    }

    @Test
    void syncDefaultsToFalse() {
        assertEquals(false, producerProperties.isSync());
    }

    @Test
    void customSync() {
        producerProperties.setSync(true);
        assertEquals(true, producerProperties.isSync());
    }

    @Test
    void domainNameDefaultsToNull() {
        assertNull(producerProperties.getDomainName());
    }

    @Test
    void domainNameConfigureAsCloud() {
        producerProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, producerProperties.getProfile().getCloudType());
        assertEquals(AzureEnvironmentProperties.AZURE_US_GOVERNMENT.getServiceBusDomainName(), producerProperties.getDomainName());
    }

    @Test
    void customDomainNameShouldSet() {
        producerProperties.setDomainName("new.servicebus.windows.net");
        producerProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, producerProperties.getProfile().getCloudType());
        assertEquals("new.servicebus.windows.net", producerProperties.getDomainName());
    }

    @Test
    void getFqdnWhenNamespaceIsNullButConnectionStringIsNot() {
        producerProperties.setConnectionString(String.format(CONNECTION_STRING_PATTERN, "test"));
        assertEquals("test.servicebus.windows.net", producerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenNamespaceAndDomainNameAreNotNull() {
        producerProperties.setNamespace("dev-namespace");
        producerProperties.setDomainName("servicebus.windows.net");
        assertEquals("dev-namespace.servicebus.windows.net", producerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenNamespaceAndDomainAreNull() {
        assertNull(producerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenNamespaceIsNullButDomainNameIsNot() {
        producerProperties.setDomainName("servicebus.windows.net");
        assertNull(producerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenDomainNameIsNullButNamespaceIsNot() {
        producerProperties.setNamespace("test");
        assertNull(producerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnReturnNullWhenNamespaceAndConnectionStringAreNull() {
        assertNull(producerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void amqpTransportTypeDefaultIsNull() {
        assertNull(producerProperties.getClient().getTransportType());
    }


    @Test
    void defaultMaxSizeInMegabytes() {
        assertEquals(producerProperties.getMaxSizeInMegabytes(), 1024L);
    }

    @Test
    void defaultMessageTimeToLive() {
        assertEquals(producerProperties.getDefaultMessageTimeToLive(), MAX_DURATION);
    }
}
