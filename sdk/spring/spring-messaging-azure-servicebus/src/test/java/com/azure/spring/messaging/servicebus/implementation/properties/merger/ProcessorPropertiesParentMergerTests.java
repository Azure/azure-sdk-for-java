// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessorPropertiesParentMergerTests {
    private final ProcessorPropertiesParentMerger merger = new ProcessorPropertiesParentMerger();

    @Test
    void childNotProvidedShouldUseParent() {
        ProcessorProperties child = new ProcessorProperties();

        NamespaceProperties parent = new NamespaceProperties();
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);

        TestPropertiesInvocation parentProperties = new TestPropertiesInvocation(parent);
        parentProperties.addIgnoreMemberVariableNames("crossEntityTransactions");
        parentProperties.extractMethodsAndInvokeSetters();

        ProcessorProperties result = merger.merge(child, parent);

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
        ProcessorProperties child = new ProcessorProperties();
        child.getProxy().setHostname("child-hostname");
        child.getProfile().setCloudType(AZURE_CHINA);
        child.getClient().setTransportType(AmqpTransportType.AMQP);

        TestPropertiesInvocation childProperties = new TestPropertiesInvocation(child);
        childProperties.addIgnoreMemberVariableNames("crossEntityTransactions");
        childProperties.extractMethodsAndInvokeSetters();

        NamespaceProperties parent = new NamespaceProperties();
        parent.getProxy().setHostname("parent-hostname");
        parent.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        parent.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        TestPropertiesInvocation parentProperties = new TestPropertiesInvocation(parent);
        parentProperties.addIgnoreMemberVariableNames("crossEntityTransactions");
        parentProperties.extractMethodsAndInvokeSetters();

        ProcessorProperties result = merger.merge(child, parent);

        Map<String, Object> childMemberVariables = childProperties.getMemberVariables();
        Map<String, Object> parentMemberVariables = parentProperties.getMemberVariables();
        TestPropertiesInvocation resultProperties = new TestPropertiesInvocation(result);
        resultProperties.extractMethods();
        resultProperties.setTargetMemberVariables(parentMemberVariables);
        resultProperties.setTargetMemberVariables(childMemberVariables);
        resultProperties.assertTargetMemberVariablesValues();

        assertEquals(childMemberVariables.get("ConnectionString"), result.getConnectionString());
        assertEquals(childMemberVariables.get("PrefetchCount"), result.getPrefetchCount());
        assertEquals(childMemberVariables.get("MaxConcurrentCalls"), result.getMaxConcurrentCalls());
        assertEquals(childMemberVariables.get("DomainName"), result.getDomainName());
        assertEquals(childMemberVariables.get("CustomEndpointAddress"), result.getCustomEndpointAddress());
        assertEquals("child-hostname", result.getProxy().getHostname());
        assertEquals(AZURE_CHINA, result.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            result.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AmqpTransportType.AMQP, result.getClient().getTransportType());
    }

}
