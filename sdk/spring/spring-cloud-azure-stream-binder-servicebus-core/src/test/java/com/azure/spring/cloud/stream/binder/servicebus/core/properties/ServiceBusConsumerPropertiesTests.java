// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.spring.cloud.core.properties.profile.AzureEnvironmentProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MAX_DURATION;
import static com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerPropertiesTests.CONNECTION_STRING_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceBusConsumerPropertiesTests {

    private ServiceBusConsumerProperties consumerProperties;

    @BeforeEach
    void beforeEach() {
        consumerProperties = new ServiceBusConsumerProperties();
    }

    @Test
    void autoCompleteDefaultTrue() {
        assertNull(consumerProperties.getAutoComplete());
    }

    @Test
    void customizeAutoComplete() {
        consumerProperties.setAutoComplete(false);
        assertFalse(consumerProperties.getAutoComplete());
    }

    @Test
    void requeueRejectedDefaultsToFalse() {
        assertFalse(consumerProperties.isRequeueRejected());
    }

    @Test
    void customRequeueRejected() {
        consumerProperties.setRequeueRejected(true);
        assertTrue(consumerProperties.isRequeueRejected());
    }

    @Test
    void maxConcurrentCallsDefaults() {
        assertNull(consumerProperties.getMaxConcurrentCalls());
    }

    @Test
    void customMaxConcurrentCalls() {
        consumerProperties.setMaxConcurrentCalls(10);
        assertEquals(10, consumerProperties.getMaxConcurrentCalls());
    }

    @Test
    void maxConcurrentSessionsDefaults() {
        assertNull(consumerProperties.getMaxConcurrentSessions());
    }

    @Test
    void customMaxConcurrentSessions() {
        consumerProperties.setMaxConcurrentSessions(10);
        assertEquals(10, consumerProperties.getMaxConcurrentSessions());
    }

    @Test
    void subQueueDefaults() {
        assertNull(consumerProperties.getSubQueue());
    }

    @Test
    void customSubQueue() {
        consumerProperties.setSubQueue(SubQueue.DEAD_LETTER_QUEUE);
        assertEquals(SubQueue.DEAD_LETTER_QUEUE, consumerProperties.getSubQueue());
    }

    @Test
    void receiveModeDefaults() {
        assertNull(consumerProperties.getReceiveMode());
    }

    @Test
    void customReceiveMode() {
        consumerProperties.setReceiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE);
        assertEquals(ServiceBusReceiveMode.RECEIVE_AND_DELETE, consumerProperties.getReceiveMode());
    }

    @Test
    void maxAutoLockRenewDurationDefaults() {
        assertNull(consumerProperties.getMaxAutoLockRenewDuration());
    }

    @Test
    void customMaxAutoLockRenewDuration() {
        Duration duration = Duration.ofMinutes(6);
        consumerProperties.setMaxAutoLockRenewDuration(duration);
        assertEquals(duration, consumerProperties.getMaxAutoLockRenewDuration());
    }

    @Test
    void domainNameDefaultsToNull() {
        assertNull(consumerProperties.getDomainName());
    }

    @Test
    void domainNameConfigureAsCloud() {
        consumerProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, consumerProperties.getProfile().getCloudType());
        assertEquals(AzureEnvironmentProperties.AZURE_US_GOVERNMENT.getServiceBusDomainName(), consumerProperties.getDomainName());
    }

    @Test
    void customDomainNameShouldSet() {
        consumerProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        consumerProperties.setDomainName("new.servicebus.windows.net");
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, consumerProperties.getProfile().getCloudType());
        assertEquals("new.servicebus.windows.net", consumerProperties.getDomainName());
    }

    @Test
    void getFqdnWhenNamespaceIsNullButConnectionStringIsNot() {
        consumerProperties.setConnectionString(String.format(CONNECTION_STRING_PATTERN, "test"));
        assertEquals("test.servicebus.windows.net", consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenNamespaceAndDomainNameAreNotNull() {
        consumerProperties.setNamespace("dev-namespace");
        consumerProperties.setDomainName("servicebus.windows.net");
        assertEquals("dev-namespace.servicebus.windows.net", consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenNamespaceAndDomainAreNull() {
        assertNull(consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenNamespaceIsNullButDomainNameIsNot() {
        consumerProperties.setDomainName("servicebus.windows.net");
        assertNull(consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenDomainNameIsNullButNamespaceIsNot() {
        consumerProperties.setNamespace("test");
        assertNull(consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnReturnNullWhenNamespaceAndConnectionStringAreNull() {
        assertNull(consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void amqpTransportTypeDefaultIsNull() {
        assertNull(consumerProperties.getClient().getTransportType());
    }

    @Test
    void defaultMaxSizeInMegabytes() {
        assertEquals(consumerProperties.getMaxSizeInMegabytes(), 1024L);
    }

    @Test
    void defaultMessageTimeToLive() {
        assertEquals(consumerProperties.getDefaultMessageTimeToLive(), MAX_DURATION);
    }

}
