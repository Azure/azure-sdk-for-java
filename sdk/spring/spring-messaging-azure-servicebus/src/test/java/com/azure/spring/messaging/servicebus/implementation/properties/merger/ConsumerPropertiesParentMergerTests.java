// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.messaging.servicebus.core.properties.ConsumerProperties;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsumerPropertiesParentMergerTests {
    private final ConsumerPropertiesParentMerger merger = new ConsumerPropertiesParentMerger();
    
    @Test
    void childNotProvidedShouldUseParent() {
        ConsumerProperties child = new ConsumerProperties();
        child.setSessionEnabled(true);
        String customEndpoint = "https://test.address.com:443";
        NamespaceProperties parent = new NamespaceProperties();
        parent.setConnectionString("parent-connection-str");
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        parent.setDomainName("parent-domain");
        parent.setCustomEndpointAddress(customEndpoint);
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        ConsumerProperties result = merger.merge(child, parent);

        assertEquals("parent-connection-str", result.getConnectionString());
        assertEquals("parent-hostname", result.getProxy().getHostname());
        assertEquals(AZURE_US_GOVERNMENT, result.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            result.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", result.getDomainName());
        assertEquals(customEndpoint, result.getCustomEndpointAddress());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, result.getClient().getTransportType());
        assertTrue(result.getSessionEnabled());
    }

    @Test
    void childProvidedShouldUseChild() {
        ConsumerProperties child = new ConsumerProperties();
        child.setConnectionString("child-connection-str");
        child.getProxy().setHostname("child-hostname");
        child.setPrefetchCount(3);
        child.getProfile().setCloudType(AZURE_CHINA);
        child.setDomainName("child-domain");
        child.setCustomEndpointAddress("https://child.address.com:443");
        child.getClient().setTransportType(AmqpTransportType.AMQP);

        NamespaceProperties parent = new NamespaceProperties();
        parent.setConnectionString("parent-connection-str");
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        parent.setDomainName("parent-domain");
        parent.setCustomEndpointAddress("https://parent.address.com:443");
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        ConsumerProperties result = merger.merge(child, parent);

        assertEquals("child-connection-str", result.getConnectionString());
        assertEquals("child-hostname", result.getProxy().getHostname());
        assertEquals(3, result.getPrefetchCount());
        assertEquals("child-domain", result.getDomainName());
        assertEquals("https://child.address.com:443", result.getCustomEndpointAddress());
        assertEquals(AZURE_CHINA, result.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            result.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AmqpTransportType.AMQP, result.getClient().getTransportType());
    }
}
