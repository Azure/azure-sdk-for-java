// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.properties.merger;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ProducerProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;

class ProducerPropertiesParentMergerTests {

    private final ProducerPropertiesParentMerger merger = new ProducerPropertiesParentMerger();

    @Test
    void childNotProvidedShouldUseParent() {
        ProducerProperties child = new ProducerProperties();

        NamespaceProperties parent = new NamespaceProperties();
        parent.setEventHubName("parent");
        parent.setConnectionString("parent-connection-str");
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        parent.setDomainName("parent-domain");
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        ProducerProperties result = merger.merge(child, parent);

        Assertions.assertEquals("parent", result.getEventHubName());
        Assertions.assertEquals("parent-connection-str", result.getConnectionString());
        Assertions.assertEquals("parent-hostname", result.getProxy().getHostname());
        Assertions.assertEquals(AZURE_US_GOVERNMENT, result.getProfile().getCloudType());
        Assertions.assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            result.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        Assertions.assertEquals("parent-domain", result.getDomainName());
        Assertions.assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, result.getClient().getTransportType());
    }

    @Test
    void childProvidedShouldUseChild() {
        ProducerProperties child = new ProducerProperties();
        child.setEventHubName("child");
        child.setConnectionString("child-connection-str");
        child.getProxy().setHostname("child-hostname");
        child.getProfile().setCloudType(AZURE_CHINA);
        child.setDomainName("child-domain");
        child.getClient().setTransportType(AmqpTransportType.AMQP);

        NamespaceProperties parent = new NamespaceProperties();
        parent.setEventHubName("parent");
        parent.setConnectionString("parent-connection-str");
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        parent.setDomainName("parent-domain");
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        ProducerProperties result = merger.merge(child, parent);

        Assertions.assertEquals("child", result.getEventHubName());
        Assertions.assertEquals("child-connection-str", result.getConnectionString());
        Assertions.assertEquals("child-hostname", result.getProxy().getHostname());
        Assertions.assertEquals(AZURE_CHINA, result.getProfile().getCloudType());
        Assertions.assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            result.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        Assertions.assertEquals(AmqpTransportType.AMQP, result.getClient().getTransportType());
    }

}
