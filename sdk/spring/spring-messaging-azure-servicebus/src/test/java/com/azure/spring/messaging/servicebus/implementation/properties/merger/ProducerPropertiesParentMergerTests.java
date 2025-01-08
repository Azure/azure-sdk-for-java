// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProducerPropertiesParentMergerTests {
    private final SenderPropertiesParentMerger merger = new SenderPropertiesParentMerger();

    @Test
    void childNotProvidedShouldUseParent() {
        ProducerProperties child = new ProducerProperties();

        NamespaceProperties parent = new NamespaceProperties();
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        TestPropertiesInvocation parentProperties = new TestPropertiesInvocation(parent);
        // ignore the property 'crossEntityTransactions' since it does not support by ProcessorProperties class.
        parentProperties.addIgnoreMemberVariableNames("crossEntityTransactions");
        parentProperties.extractMethodsAndInvokeSetters();

        ProducerProperties result = merger.merge(child, parent);

        TestPropertiesInvocation resultProperties = new TestPropertiesInvocation(result);
        resultProperties.extractMethods();
        resultProperties.setTargetMemberVariables(parentProperties.getMemberVariables());
        resultProperties.assertTargetMemberVariablesValues();

        assertEquals("parent-hostname", result.getProxy().getHostname());
        assertEquals(AZURE_US_GOVERNMENT, result.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            result.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, result.getClient().getTransportType());
    }

    @Test
    void childProvidedShouldUseChild() {
        ProducerProperties child = new ProducerProperties();
        child.getProxy().setHostname("child-hostname");
        child.getProfile().setCloudType(AZURE_CHINA);
        child.getClient().setTransportType(AmqpTransportType.AMQP);

        TestPropertiesInvocation childProperties = new TestPropertiesInvocation(child);
        // ignore the property 'crossEntityTransactions' since it does not support by ProcessorProperties class.
        childProperties.addIgnoreMemberVariableNames("crossEntityTransactions");
        childProperties.extractMethodsAndInvokeSetters();

        NamespaceProperties parent = new NamespaceProperties();
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        TestPropertiesInvocation parentProperties = new TestPropertiesInvocation(parent);
        // ignore the property 'crossEntityTransactions' since it does not support by ProcessorProperties class.
        parentProperties.addIgnoreMemberVariableNames("crossEntityTransactions");
        parentProperties.extractMethodsAndInvokeSetters();

        ProducerProperties result = merger.merge(child, parent);

        Map<String, Object> childMemberVariables = childProperties.getMemberVariables();
        Map<String, Object> parentMemberVariables = parentProperties.getMemberVariables();
        TestPropertiesInvocation resultProperties = new TestPropertiesInvocation(result);
        resultProperties.extractMethods();
        resultProperties.setTargetMemberVariables(parentMemberVariables);
        resultProperties.setTargetMemberVariables(childMemberVariables);
        resultProperties.assertTargetMemberVariablesValues();

        assertEquals(childMemberVariables.get("ConnectionString"), result.getConnectionString());
        assertEquals("child-hostname", result.getProxy().getHostname());
        assertEquals(childMemberVariables.get("EntityName"), result.getEntityName());
        assertEquals(childMemberVariables.get("DomainName"), result.getDomainName());
        assertEquals(AZURE_CHINA, result.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            result.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AmqpTransportType.AMQP, result.getClient().getTransportType());
    }

}
