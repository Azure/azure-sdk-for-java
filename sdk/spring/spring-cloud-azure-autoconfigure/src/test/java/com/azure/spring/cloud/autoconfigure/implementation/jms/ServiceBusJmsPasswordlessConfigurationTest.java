// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.ServiceBusJmsConnectionFactoryCustomizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class ServiceBusJmsPasswordlessConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(ServiceBusJmsPasswordlessTestConfig.class);

    @Test
    void testPropertyEnabledIsFalse() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getProfile().setCloudType(AZURE);
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");
        azureProperties.getProfile().getEnvironment().setActiveDirectoryGraphApiVersion("v2");

        this.contextRunner
            .withPropertyValues("spring.jms.servicebus.passwordless-enabled=false")
            .withPropertyValues("spring.jms.servicebus.connection-string=fake-connection-string")
            .withPropertyValues("spring.jms.servicebus.pricing-tier=standard")
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureServiceBusJmsCredentialSupplier.class);
                assertThat(context).doesNotHaveBean(ServiceBusJmsConnectionFactoryCustomizer.class);
            });
    }

    @Test
    void testPropertyEnabledIsTrue() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getProfile().setCloudType(AZURE);
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");
        azureProperties.getProfile().getEnvironment().setActiveDirectoryGraphApiVersion("v2");

        this.contextRunner
            .withPropertyValues("spring.jms.servicebus.passwordless-enabled=true")
            .withPropertyValues("spring.jms.servicebus.namespace=testnamespace")
            .withPropertyValues("spring.jms.servicebus.pricing-tier=standard")
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJmsCredentialSupplier.class);
                assertThat(context).hasSingleBean(ServiceBusJmsConnectionFactoryCustomizer.class);
            });
    }

    @Test
    void testServiceBusPasswordlessProperties() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getProfile().setCloudType(AZURE);
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");
        azureProperties.getProfile().getEnvironment().setActiveDirectoryGraphApiVersion("v2");
        this.contextRunner
            .withPropertyValues("spring.jms.servicebus.passwordless-enabled=true")
            .withPropertyValues("spring.jms.servicebus.namespace=testnamespace")
            .withPropertyValues("spring.jms.servicebus.pricing-tier=standard")
            .withPropertyValues("spring.jms.servicebus.scopes=scopes",
                "spring.jms.servicebus.profile.tenant-id=tenant-id",
                "spring.jms.servicebus.profile.subscription-id=subscription-id",
                "spring.jms.servicebus.profile.CloudType=AZURE_CHINA",
                "spring.jms.servicebus.credential.client-id=client-id",
                "spring.jms.servicebus.credential.client-secret=secret",
                "spring.jms.servicebus.credential.client-certificatePath=client-certificatePath",
                "spring.jms.servicebus.credential.client-certificatePassword=client-certificatePassword",
                "spring.jms.servicebus.credential.username=username",
                "spring.jms.servicebus.credential.password=password",
                "spring.jms.servicebus.credential.managed-identity-enabled=true",
                "spring.jms.servicebus.client.application-id=application-id",
                "spring.jms.servicebus.proxy.hostname=hostname",
                "spring.jms.servicebus.proxy.username=username",
                "spring.jms.servicebus.proxy.port=1111",
                "spring.jms.servicebus.proxy.password=password",
                "spring.jms.servicebus.proxy.type=type")
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJmsCredentialSupplier.class);
                assertThat(context).hasSingleBean(ServiceBusJmsConnectionFactoryCustomizer.class);
                AzureServiceBusJmsProperties properties = context.getBean(AzureServiceBusJmsProperties.class);
                assertThat(properties.getScopes()).isEqualTo("scopes");
                assertThat(properties.getProfile().getTenantId()).isEqualTo("tenant-id");
                assertThat(properties.getProfile().getSubscriptionId()).isEqualTo("subscription-id");
                assertThat(properties.getProfile().getCloudType()).isEqualTo(AZURE_CHINA);
                assertThat(properties.getCredential().getClientId()).isEqualTo("client-id");
                assertThat(properties.getCredential().getClientSecret()).isEqualTo("secret");
                assertThat(properties.getCredential().getClientCertificatePath()).isEqualTo("client-certificatePath");
                assertThat(properties.getCredential().getClientCertificatePassword()).isEqualTo("client-certificatePassword");
                assertThat(properties.getCredential().getUsername()).isEqualTo("username");
                assertThat(properties.getCredential().getPassword()).isEqualTo("password");
                assertThat(properties.getCredential().isManagedIdentityEnabled()).isTrue();

            });
    }

    @Test
    void testAzureServiceBusJmsCredentialSupplier() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getProfile().setCloudType(AZURE);
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");
        azureProperties.getProfile().getEnvironment().setActiveDirectoryGraphApiVersion("v2");

        this.contextRunner
            .withPropertyValues("spring.jms.servicebus.passwordless-enabled=true")
            .withBean(AzureGlobalProperties.class, () -> azureProperties);

        try (MockedConstruction<AzureServiceBusJmsCredentialSupplier> supplierMockedConstruction = mockConstruction(AzureServiceBusJmsCredentialSupplier.class,
            (azureServiceBusJmsCredentialSupplierMocker, mockerContext) -> {
                when(azureServiceBusJmsCredentialSupplierMocker.get()).thenReturn("fake-token");

                contextRunner.run(runnerContext -> {
                    assertThat(runnerContext).hasSingleBean(AzureServiceBusJmsCredentialSupplier.class);
                    assertThat(runnerContext).hasSingleBean(ServiceBusJmsConnectionFactoryCustomizer.class);
                    runnerContext.getBean(AzureServiceBusJmsCredentialSupplier.class).get().equals("fake-token");
                });

            })) {
            Assertions.assertNotNull(supplierMockedConstruction);
        }

    }

    @EnableConfigurationProperties
    @Import({ServiceBusJmsPasswordlessConfiguration.class})
    static class ServiceBusJmsPasswordlessTestConfig {

        @Bean
        AzureServiceBusJmsProperties jmsProperties() {
            return new AzureServiceBusJmsProperties();
        }
    }
}
