// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties.merger;

import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProcessorPropertiesParentMergerTest {

    private final ProcessorPropertiesParentMerger merger = new ProcessorPropertiesParentMerger();

    @Test
    void childNotProvidedShouldUseParent() {
        ProcessorProperties child = new ProcessorProperties();

        NamespaceProperties parent = new NamespaceProperties();
        parent.setEventHubName("parent");
        parent.setConnectionString("parent-connection-str");
        ProxyProperties proxy = new ProxyProperties();
        proxy.setHostname("parent-hostname");
        parent.setProxy(proxy);

        ProcessorProperties result = merger.mergeParent(child, parent);

        Assertions.assertEquals("parent", result.getEventHubName());
        Assertions.assertEquals("parent-connection-str", result.getConnectionString());
        Assertions.assertEquals("parent-hostname", result.getProxy().getHostname());
    }

    @Test
    void childProvidedShouldUseChild() {
        ProcessorProperties child = new ProcessorProperties();
        child.setEventHubName("child");
        child.setConnectionString("child-connection-str");
        ProxyProperties proxy = new ProxyProperties();
        proxy.setHostname("child-hostname");
        child.setProxy(proxy);
        child.setTrackLastEnqueuedEventProperties(true);
        child.setPrefetchCount(3);
        child.setConsumerGroup("default");

        NamespaceProperties parent = new NamespaceProperties();
        parent.setEventHubName("parent");
        parent.setConnectionString("parent-connection-str");
        ProxyProperties anotherProxy = new ProxyProperties();
        anotherProxy.setHostname("parent-hostname");
        parent.setProxy(anotherProxy);

        ProcessorProperties result = merger.mergeParent(child, parent);

        Assertions.assertEquals("child", result.getEventHubName());
        Assertions.assertEquals("child-connection-str", result.getConnectionString());
        Assertions.assertEquals("child-hostname", result.getProxy().getHostname());
        Assertions.assertEquals(true, result.getTrackLastEnqueuedEventProperties());
        Assertions.assertEquals("default", result.getConsumerGroup());
        Assertions.assertEquals(3, result.getPrefetchCount());
    }

}
