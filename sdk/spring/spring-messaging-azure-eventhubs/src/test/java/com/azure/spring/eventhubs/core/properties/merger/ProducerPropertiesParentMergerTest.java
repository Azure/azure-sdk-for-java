// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties.merger;

import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProducerProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProducerPropertiesParentMergerTest {

    private final ProducerPropertiesParentMerger merger = new ProducerPropertiesParentMerger();

    @Test
    void childNotProvidedShouldUseParent() {
        ProducerProperties child = new ProducerProperties();

        NamespaceProperties parent = new NamespaceProperties();
        parent.setEventHubName("parent");
        parent.setConnectionString("parent-connection-str");
        ProxyProperties proxy = new ProxyProperties();
        proxy.setHostname("parent-hostname");
        parent.setProxy(proxy);

        ProducerProperties result = merger.mergeParent(child, parent);

        Assertions.assertEquals("parent", result.getEventHubName());
        Assertions.assertEquals("parent-connection-str", result.getConnectionString());
        Assertions.assertEquals("parent-hostname", result.getProxy().getHostname());
    }

    @Test
    void childProvidedShouldUseChild() {
        ProducerProperties child = new ProducerProperties();
        child.setEventHubName("child");
        child.setConnectionString("child-connection-str");
        ProxyProperties proxy = new ProxyProperties();
        proxy.setHostname("child-hostname");
        child.setProxy(proxy);

        NamespaceProperties parent = new NamespaceProperties();
        parent.setEventHubName("parent");
        parent.setConnectionString("parent-connection-str");
        ProxyProperties anotherProxy = new ProxyProperties();
        anotherProxy.setHostname("parent-hostname");
        parent.setProxy(anotherProxy);

        ProducerProperties result = merger.mergeParent(child, parent);

        Assertions.assertEquals("child", result.getEventHubName());
        Assertions.assertEquals("child-connection-str", result.getConnectionString());
        Assertions.assertEquals("child-hostname", result.getProxy().getHostname());
    }

}
