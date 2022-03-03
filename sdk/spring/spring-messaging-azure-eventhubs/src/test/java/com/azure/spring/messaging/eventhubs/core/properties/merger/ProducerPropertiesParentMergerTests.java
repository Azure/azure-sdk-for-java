// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.properties.merger;

import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ProducerProperties;
import com.azure.spring.messaging.eventhubs.implementation.properties.merger.ProducerPropertiesParentMerger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProducerPropertiesParentMergerTests {

    private final ProducerPropertiesParentMerger merger = new ProducerPropertiesParentMerger();

    @Test
    void childNotProvidedShouldUseParent() {
        ProducerProperties child = new ProducerProperties();

        NamespaceProperties parent = new NamespaceProperties();
        parent.setEventHubName("parent");
        parent.setConnectionString("parent-connection-str");
        parent.getProxy().setHostname("parent-hostname");

        ProducerProperties result = merger.merge(child, parent);

        Assertions.assertEquals("parent", result.getEventHubName());
        Assertions.assertEquals("parent-connection-str", result.getConnectionString());
        Assertions.assertEquals("parent-hostname", result.getProxy().getHostname());
    }

    @Test
    void childProvidedShouldUseChild() {
        ProducerProperties child = new ProducerProperties();
        child.setEventHubName("child");
        child.setConnectionString("child-connection-str");
        child.getProxy().setHostname("child-hostname");

        NamespaceProperties parent = new NamespaceProperties();
        parent.setEventHubName("parent");
        parent.setConnectionString("parent-connection-str");
        parent.getProxy().setHostname("parent-hostname");

        ProducerProperties result = merger.merge(child, parent);

        Assertions.assertEquals("child", result.getEventHubName());
        Assertions.assertEquals("child-connection-str", result.getConnectionString());
        Assertions.assertEquals("child-hostname", result.getProxy().getHostname());
    }

}
