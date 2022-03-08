// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.factory;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.provider.connectionstring.ConnectionStringProvider;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.implementation.properties.AzureSdkProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractAzureServiceClientBuilderFactoryTests {

    @Test
    void emptyPropertiesShouldWork() {
        TestClientBuilderFactory factory = new TestClientBuilderFactory(new AzureTestProperties());
        factory.build();
    }

    @Test
    void connectionStringFromPropertiesShouldHavePriority() {
        AzureTestProperties testAzureProperties = new AzureTestProperties();
        testAzureProperties.setConnectionString("connection-string");

        TestClientBuilderFactory factory = new TestClientBuilderFactory(testAzureProperties);
        factory.setConnectionStringProvider(new StaticConnectionStringProvider<>("Test Service", "provider"));
        TestClientBuilder builder = factory.build();
        assertEquals("connection-string", builder.getConnectionString());
    }

    @Test
    void connectionStringFromPropertyShouldBeConfigured() {
        AzureTestProperties azureTestProperties = new AzureTestProperties();
        azureTestProperties.setConnectionString("connection-string");

        TestClientBuilderFactory factory = new TestClientBuilderFactory(azureTestProperties);
        TestClientBuilder builder = factory.build();
        assertEquals("connection-string", builder.getConnectionString());
    }

    @Test
    void connectionStringFromProviderShouldBeConfigured() {
        TestClientBuilderFactory factory = new TestClientBuilderFactory(new AzureTestProperties());
        factory.setConnectionStringProvider(new StaticConnectionStringProvider<>("Test Service", "provider"));
        TestClientBuilder builder = factory.build();
        assertEquals("provider", builder.getConnectionString());
    }

    @Test
    void springIdentifierShouldBeConfigured() {
        TestClientBuilderFactory factory = new TestClientBuilderFactory(new AzureTestProperties());
        factory.setSpringIdentifier("identifier");
        TestClientBuilder builder = factory.build();
        assertEquals("identifier", builder.getApplicationId());
    }

    @Test
    void applicationIdShouldBeConfigured() {
        AzureTestProperties azureTestProperties = new AzureTestProperties();
        azureTestProperties.getClient().setApplicationId("application-id");

        TestClientBuilderFactory factory = new TestClientBuilderFactory(azureTestProperties);
        TestClientBuilder builder = factory.build();
        assertEquals("application-id", builder.getApplicationId());
    }

    @Test
    void applicationIdAndSpringIdentifierShouldBeConfigured() {
        AzureTestProperties azureTestProperties = new AzureTestProperties();
        azureTestProperties.getClient().setApplicationId("application-id");
        TestClientBuilderFactory factory = new TestClientBuilderFactory(azureTestProperties);
        factory.setSpringIdentifier("identifier");
        TestClientBuilder builder = factory.build();
        assertEquals("application-ididentifier", builder.getApplicationId());
    }

    @Test
    void customizerShouldEffect() {
        TestBuilderCustomizer customizer = new TestBuilderCustomizer();

        TestClientBuilderFactory factory = new TestClientBuilderFactory(new AzureTestProperties());
        factory.addBuilderCustomizer(customizer);
        TestClientBuilder builder = factory.build();
        assertEquals(1, builder.getCustomizedTimes());

        TestClientBuilderFactory anotherFactory = new TestClientBuilderFactory(new AzureTestProperties());
        anotherFactory.addBuilderCustomizer(customizer);
        anotherFactory.addBuilderCustomizer(customizer);
        TestClientBuilder anotherBuilder = factory.build();
        assertEquals(2, anotherBuilder.getCustomizedTimes());
    }

    static class TestBuilderCustomizer implements AzureServiceClientBuilderCustomizer<TestClientBuilder> {

        @Override
        public void customize(TestClientBuilder builder) {
            builder.setCustomizedTimes(builder.getCustomizedTimes() + 1);
        }
    }

    static class TestClientBuilderFactory extends AbstractAzureServiceClientBuilderFactory<TestClientBuilder> {

        final AzureTestProperties properties;
        final TestClientBuilder builder = new TestClientBuilder();

        TestClientBuilderFactory(AzureTestProperties properties) {
            this.properties = properties;
        }

        @Override
        protected TestClientBuilder createBuilderInstance() {
            return this.builder;
        }

        @Override
        protected AzureProperties getAzureProperties() {
            return this.properties;
        }

        @Override
        protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(TestClientBuilder builder) {
            return Collections.emptyList();
        }

        @Override
        protected void configureProxy(TestClientBuilder builder) {

        }

        @Override
        protected void configureRetry(TestClientBuilder builder) {

        }

        @Override
        protected void configureService(TestClientBuilder builder) {

        }

        @Override
        protected BiConsumer<TestClientBuilder, String> consumeApplicationId() {
            return TestClientBuilder::setApplicationId;
        }

        @Override
        protected BiConsumer<TestClientBuilder, Configuration> consumeConfiguration() {
            return (a, b) -> { };
        }

        @Override
        protected BiConsumer<TestClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
            return (a, b) -> { };
        }

        @Override
        protected BiConsumer<TestClientBuilder, String> consumeConnectionString() {
            return TestClientBuilder::setConnectionString;
        }
    }

    static class TestClientBuilder {

        private String connectionString;
        private String applicationId;
        private int customizedTimes = 0;

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }

        public int getCustomizedTimes() {
            return customizedTimes;
        }

        public void setCustomizedTimes(int customizedTimes) {
            this.customizedTimes = customizedTimes;
        }
    }

    static class AzureTestProperties extends AzureSdkProperties implements ConnectionStringProvider, RetryOptionsProvider {
        private String connectionString;
        private final ClientProperties client = new ClientProperties();
        private final ProxyProperties proxy = new ProxyProperties();
        private final RetryProperties retry = new RetryProperties();

        @Override
        public ClientProperties getClient() {
            return this.client;
        }

        @Override
        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        @Override
        public ProxyProperties getProxy() {
            return proxy;
        }

        @Override
        public RetryProperties getRetry() {
            return retry;
        }
    }



}
