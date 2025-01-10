// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.client.AmqpClientProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.properties.proxy.AmqpProxyProperties;
import com.azure.spring.cloud.core.properties.retry.AmqpRetryProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesComparer.isMergedPropertiesCorrect;
import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesValueInjectHelper.injectPseudoPropertyValues;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesMergerUsingInjectedValuesTests {

    @Test
    void allParentPropertiesWillBeMergedBySenderMerger() {
        // Arrange
        ProducerProperties child = new ProducerProperties();

        NamespaceProperties parent = new NamespaceProperties();
        injectPseudoPropertyValues(parent, List.of("cloudType"),"FullyQualifiedNamespace");

        // Action
        SenderPropertiesParentMerger merger = new SenderPropertiesParentMerger();
        ProducerProperties result = merger.merge(child, parent);

        // Assertion
        assertNotNull(parent.getConnectionString());
        assertNotNull(parent.getEntityName());

        assertAzureAmqpSdkProperties(parent);
        assertAzureAmqpSdkProperties(result);

        assertNotNull(result.getConnectionString());
        assertNotNull(result.getEntityName());

        assertTrue(isMergedPropertiesCorrect(parent, child, result));
    }

    @Test
    void allChildPropertiesWillBeMergedBySenderMerger() {
        // Arrange
        ProducerProperties child = new ProducerProperties();
        injectPseudoPropertyValues(child, List.of("cloudType"),"FullyQualifiedNamespace");

        NamespaceProperties parent = new NamespaceProperties();

        // Action
        SenderPropertiesParentMerger merger = new SenderPropertiesParentMerger();
        ProducerProperties result = merger.merge(child, parent);

        // Assertion
        assertNotNull(child.getConnectionString());
        assertNotNull(child.getEntityName());

        assertAzureAmqpSdkProperties(child);
        assertAzureAmqpSdkProperties(result);

        assertNotNull(result.getConnectionString());
        assertNotNull(result.getEntityName());

        assertTrue(isMergedPropertiesCorrect(child, child, result));
    }

    @Test
    void allParentPropertiesWillBeMergedByProcessorMerger() {
        // Arrange
        ProcessorProperties child = new ProcessorProperties();

        NamespaceProperties parent = new NamespaceProperties();
        injectPseudoPropertyValues(parent, List.of("cloudType"),"FullyQualifiedNamespace");

        // Action
        ProcessorPropertiesParentMerger merger = new ProcessorPropertiesParentMerger();
        ProcessorProperties result = merger.merge(child, parent);

        // Assertion
        assertNotNull(parent.getConnectionString());
        assertNotNull(parent.getEntityName());

        assertAzureAmqpSdkProperties(parent);
        assertAzureAmqpSdkProperties(result);

        assertNotNull(result.getConnectionString());
        assertNotNull(result.getEntityName());

        assertTrue(isMergedPropertiesCorrect(parent, child, result));
    }

    @Test
    void allChildPropertiesWillBeMergedByProcessorMerger() {
        // Arrange
        ProcessorProperties child = new ProcessorProperties();
        injectPseudoPropertyValues(child, List.of("cloudType"),"FullyQualifiedNamespace");

        NamespaceProperties parent = new NamespaceProperties();

        // Action
        ProcessorPropertiesParentMerger merger = new ProcessorPropertiesParentMerger();
        ProcessorProperties result = merger.merge(child, parent);

        // Assertion
        assertNotNull(child.getConnectionString());
        assertNotNull(child.getEntityName());

        assertAzureAmqpSdkProperties(child);
        assertAzureAmqpSdkProperties(result);

        assertNotNull(result.getConnectionString());
        assertNotNull(result.getEntityName());

        assertTrue(isMergedPropertiesCorrect(child, child, result));
    }

    private static void assertAzureAmqpSdkProperties(AzureAmqpSdkProperties properties) {
        assertBuiltInProperties(properties.getClient(), properties.getProxy(), properties.getRetry(), properties.getProfile(), properties.getCredential());
    }

    private static void assertBuiltInProperties(AmqpClientProperties client,
                                                AmqpProxyProperties proxy,
                                                AmqpRetryProperties retry,
                                                AzureProfileProperties profile,
                                                TokenCredentialProperties credential) {

        assertTrue(client.getApplicationId().startsWith("ApplicationId"));
        assertEquals(client.getTransportType(), AmqpTransportType.AMQP);

        assertTrue(proxy.getAuthenticationType().startsWith("AuthenticationType"));
        assertTrue(proxy.getHostname().startsWith("Hostname"));
        assertTrue(proxy.getPort() >= 100);
        assertTrue(proxy.getType().startsWith("Type"));
        assertTrue(proxy.getUsername().startsWith("Username"));
        assertTrue(proxy.getPassword().startsWith("Password"));

        assertTrue(retry.getTryTimeout().getSeconds() >= 100);
        assertEquals(retry.getMode(), RetryOptionsProvider.RetryMode.EXPONENTIAL);
        assertTrue(retry.getFixed().getMaxRetries() >= 100);
        assertTrue(retry.getFixed().getDelay().getSeconds() >= 100);
        assertTrue(retry.getExponential().getMaxRetries() >= 100);
        assertTrue(retry.getExponential().getBaseDelay().getSeconds() >= 100);
        assertTrue(retry.getExponential().getMaxDelay().getSeconds() >= 100);

        assertTrue(credential.getUsername().startsWith("Username"));
        assertTrue(credential.getPassword().startsWith("Password"));
        assertTrue(credential.getClientId().startsWith("ClientId"));
        assertTrue(credential.getPassword().startsWith("Password"));
        assertTrue(credential.getClientCertificatePath().startsWith("ClientCertificatePath"));
        assertTrue(credential.getClientCertificatePassword().startsWith("ClientCertificatePassword"));
        assertTrue(credential.getTokenCredentialBeanName().startsWith("TokenCredentialBeanName"));
        assertTrue(credential.isManagedIdentityEnabled());

        assertTrue(profile.getTenantId().startsWith("TenantId"));
        assertEquals(profile.getCloudType(), AZURE_CHINA);
        assertTrue(profile.getEnvironment().getActiveDirectoryEndpoint().startsWith("ActiveDirectoryEndpoint"));
        assertTrue(profile.getEnvironment().getServiceBusDomainName().startsWith("ServiceBusDomainName"));
    }
}
