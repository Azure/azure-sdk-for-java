// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;

public class ProcessorPropertiesParentMergerTests {
    private final ProcessorPropertiesParentMerger merger = new ProcessorPropertiesParentMerger();

    @Test
    void childNotProvidedShouldUseParent() {
        ProcessorProperties child = new ProcessorProperties();

        NamespaceProperties parent = new NamespaceProperties();
        parent.setConnectionString("parent-connection-str");
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        parent.setDomainName("parent-domain");
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        ProcessorProperties result = merger.merge(child, parent);

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
        ProcessorProperties child = new ProcessorProperties();
        child.setConnectionString("child-connection-str");
        child.getProxy().setHostname("child-hostname");
        child.setPrefetchCount(3);
        child.setMaxConcurrentCalls(2);
        child.getProfile().setCloudType(AZURE_CHINA);
        child.setDomainName("child-domain");
        child.getClient().setTransportType(AmqpTransportType.AMQP);

        NamespaceProperties parent = new NamespaceProperties();
        parent.setConnectionString("parent-connection-str");
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        parent.setDomainName("parent-domain");
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        ProcessorProperties result = merger.merge(child, parent);

        Assertions.assertEquals("child-connection-str", result.getConnectionString());
        Assertions.assertEquals("child-hostname", result.getProxy().getHostname());
        Assertions.assertEquals(3, result.getPrefetchCount());
        Assertions.assertEquals(2, result.getMaxConcurrentCalls());
        Assertions.assertEquals("child-domain", result.getDomainName());
        Assertions.assertEquals(AZURE_CHINA, result.getProfile().getCloudType());
        Assertions.assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            result.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        Assertions.assertEquals(AmqpTransportType.AMQP, result.getClient().getTransportType());
    }

}
