// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.kafka.AzureEventHubsKafkaAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.kafka.properties.AzureEventHubsKafkaProperties;
import com.azure.spring.cloud.service.kafka.AzureKafkaConfigs;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static com.azure.core.management.AzureEnvironment.AZURE_GERMANY;
import static com.azure.core.management.AzureEnvironment.AZURE_US_GOVERNMENT;
import static com.azure.spring.cloud.autoconfigure.kafka.KafkaPropertiesBeanPostProcessor.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.kafka.KafkaPropertiesBeanPostProcessor.SECURITY_PROTOCOL_CONFIG_SASL;
import static com.azure.spring.cloud.autoconfigure.kafka.KafkaPropertiesBeanPostProcessor.SASL_JAAS_CONFIG_OAUTH;
import static com.azure.spring.cloud.autoconfigure.kafka.KafkaPropertiesBeanPostProcessor.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;


class AzureEventHubsKafkaOAuth2AutoConfigurationTests {

    static final String CONNECTION_STRING_FORMAT =
            "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaOAuth2AutoConfiguration.class,
                    AzureGlobalPropertiesAutoConfiguration.class, AzureTokenCredentialAutoConfiguration.class))
            .withBean(KafkaProperties.class, KafkaProperties::new);

    @Test
    void shouldNotConfigureWhenAzureEventHubsKafkaDisabled() {
        this.contextRunner
                .withPropertyValues("spring.cloud.azure.kafka.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class));
    }

    @Test
    void shouldNotConfigureWithoutKafkaTemplate() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader(KafkaTemplate.class))
                .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class));
    }

    @Test
    void azureGlobalPropertiesShouldBind() {
        this.contextRunner
                .withPropertyValues(
                        "spring.cloud.azure.credential.client-id=test-client-id",
                        "spring.cloud.azure.profile.tenant-id=test-tenant-id",
                        "spring.cloud.azure.profile.environment.active-directory-endpoint=" + AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint()
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(AzureGlobalProperties.class);
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaProperties.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                    assertEquals("test-client-id", azureGlobalProperties.getCredential().getClientId());
                    assertEquals("test-tenant-id", azureGlobalProperties.getProfile().getTenantId());
                    assertEquals(AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(), azureGlobalProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

                    AzureEventHubsKafkaProperties azureEventHubsKafkaProperties = context.getBean(AzureEventHubsKafkaProperties.class);
                    assertTrue(azureEventHubsKafkaProperties.isEnabled());
                    assertEquals("test-client-id", azureEventHubsKafkaProperties.getCredential().getClientId());
                    assertEquals("test-tenant-id", azureEventHubsKafkaProperties.getProfile().getTenantId());
                    assertNotEquals(AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(), azureEventHubsKafkaProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.CLIENT_ID_CONFIG));
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.TENANT_ID_CONFIG));
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.AAD_ENDPOINT_CONFIG));
                });
    }

    @Test
    void azureEventHubsKafkaPropertiesShouldBind() {
        this.contextRunner
                .withPropertyValues(
                        AzureKafkaConfigs.CLIENT_ID_CONFIG + "=test-client-id",
                        AzureKafkaConfigs.TENANT_ID_CONFIG + "=test-tenant-id",
                        AzureKafkaConfigs.AAD_ENDPOINT_CONFIG + "=" + AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint()
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaProperties.class);
                    assertThat(context).hasSingleBean(AzureGlobalProperties.class);

                    AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                    assertNull(azureGlobalProperties.getCredential().getClientId());
                    assertNull(azureGlobalProperties.getProfile().getTenantId());
                    assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(), azureGlobalProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

                    AzureEventHubsKafkaProperties azureEventHubsKafkaProperties = context.getBean(AzureEventHubsKafkaProperties.class);
                    assertTrue(azureEventHubsKafkaProperties.isEnabled());
                    assertEquals("test-client-id", azureEventHubsKafkaProperties.getCredential().getClientId());
                    assertEquals("test-tenant-id", azureEventHubsKafkaProperties.getProfile().getTenantId());
                    assertEquals(AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(), azureEventHubsKafkaProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.CLIENT_ID_CONFIG));
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.TENANT_ID_CONFIG));
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.AAD_ENDPOINT_CONFIG));
                });
    }

    @Test
    void azureEventHubsKafkaPropertiesShouldOverride() {
        this.contextRunner
                .withPropertyValues(
                        "spring.cloud.azure.credential.client-id=fake-client-id",
                        "spring.cloud.azure.profile.tenant-id=fake-tenant-id",
                        "spring.cloud.azure.profile.environment.active-directory-endpoint=" + AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
                        "spring.cloud.azure.kafka.enabled=true",
                        AzureKafkaConfigs.CLIENT_ID_CONFIG + "=test-client-id",
                        AzureKafkaConfigs.TENANT_ID_CONFIG + "=test-tenant-id",
                        AzureKafkaConfigs.AAD_ENDPOINT_CONFIG + "=" + AZURE_GERMANY.getActiveDirectoryEndpoint()
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaProperties.class);
                    assertThat(context).hasSingleBean(AzureGlobalProperties.class);

                    AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                    assertEquals("fake-client-id", azureGlobalProperties.getCredential().getClientId());
                    assertEquals("fake-tenant-id", azureGlobalProperties.getProfile().getTenantId());
                    assertEquals(AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(), azureGlobalProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

                    AzureEventHubsKafkaProperties azureEventHubsKafkaProperties = context.getBean(AzureEventHubsKafkaProperties.class);
                    assertTrue(azureEventHubsKafkaProperties.isEnabled());
                    assertEquals("test-client-id", azureEventHubsKafkaProperties.getCredential().getClientId());
                    assertEquals("test-tenant-id", azureEventHubsKafkaProperties.getProfile().getTenantId());
                    assertEquals(AZURE_GERMANY.getActiveDirectoryEndpoint(), azureEventHubsKafkaProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.CLIENT_ID_CONFIG));
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.TENANT_ID_CONFIG));
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.AAD_ENDPOINT_CONFIG));
                });
    }

    @Test
    void noAzurePropertiesShouldBind() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaProperties.class);
                    assertThat(context).hasSingleBean(AzureGlobalProperties.class);

                    AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                    assertNull(azureGlobalProperties.getCredential().getClientId());
                    assertNull(azureGlobalProperties.getProfile().getTenantId());
                    assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(), azureGlobalProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

                    AzureEventHubsKafkaProperties azureEventHubsKafkaProperties = context.getBean(AzureEventHubsKafkaProperties.class);
                    assertTrue(azureEventHubsKafkaProperties.isEnabled());
                    assertNull(azureEventHubsKafkaProperties.getCredential().getClientId());
                    assertNull(azureEventHubsKafkaProperties.getProfile().getTenantId());
                    assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(), azureEventHubsKafkaProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.CLIENT_ID_CONFIG));
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.TENANT_ID_CONFIG));
                    assertNull(kafkaProperties.getProperties().get(AzureKafkaConfigs.AAD_ENDPOINT_CONFIG));
                });
    }

    @Test
    void testConfigureKafkaProperties() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);

                    Map<String, Object> adminProperties = kafkaProperties.buildAdminProperties();
                    testOAuthKafkaPropertiesBind(adminProperties);

                    Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
                    testOAuthKafkaPropertiesBind(consumerProperties);

                    Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
                    testOAuthKafkaPropertiesBind(producerProperties);
                });
    }

    @Test
    void testConfigureSecurityProtocolProperties() {
        this.contextRunner
                .withPropertyValues(
                        "spring.kafka.admin.security.protocol=SSL",
                        "spring.kafka.producer.security.protocol=" + SECURITY_PROTOCOL_CONFIG_SASL
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);

                    Map<String, Object> adminProperties = kafkaProperties.buildAdminProperties();
                    testOAuthKafkaPropertiesNotBind(adminProperties);

                    Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
                    testOAuthKafkaPropertiesBind(consumerProperties);

                    Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
                    testOAuthKafkaPropertiesBind(producerProperties);
                });
    }

    @Test
    void testConfigureSaslProperties() {
        this.contextRunner
                .withPropertyValues(
                        "spring.kafka.properties.security.protocol=" + SECURITY_PROTOCOL_CONFIG_SASL,
                        "spring.kafka.admin.properties.sasl.mechanism=" + SASL_MECHANISM_OAUTH,
                        "spring.kafka.producer.properties.sasl.mechanism=PLAIN"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);

                    Map<String, Object> adminProperties = kafkaProperties.buildAdminProperties();
                    testOAuthKafkaPropertiesBind(adminProperties);

                    Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
                    testOAuthKafkaPropertiesBind(consumerProperties);

                    Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
                    assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, producerProperties.get(SECURITY_PROTOCOL_CONFIG));
                    assertNotEquals(SASL_MECHANISM_OAUTH, producerProperties.get(SASL_MECHANISM));
                    assertNotEquals(SASL_JAAS_CONFIG_OAUTH, producerProperties.get(SASL_JAAS_CONFIG));
                    assertNotEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH, producerProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));

                });
    }

    @Test
    void testConfigureJaasProperties() {
        this.contextRunner
                .withPropertyValues(
                        "spring.kafka.security.protocol=" + SECURITY_PROTOCOL_CONFIG_SASL,
                        "spring.kafka.properties.sasl.mechanism=" + SASL_MECHANISM_OAUTH,
                        "spring.kafka.admin.properties.sasl.jaas.config=fake-admin-custom",
                        "spring.kafka.producer.properties.login.callback.handler.class=fake-producer-custom"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);

                    Map<String, Object> adminProperties = kafkaProperties.buildAdminProperties();
                    testOAuthKafkaPropertiesBind(adminProperties);

                    Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
                    testOAuthKafkaPropertiesBind(consumerProperties);

                    Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
                    testOAuthKafkaPropertiesBind(producerProperties);
                });
    }

    private <T> void testOAuthKafkaPropertiesBind(Map<String, T> kafkaProperties) {
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, kafkaProperties.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, kafkaProperties.get(SASL_MECHANISM));
        assertEquals(SASL_JAAS_CONFIG_OAUTH, kafkaProperties.get(SASL_JAAS_CONFIG));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH, kafkaProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }

    private <T> void testOAuthKafkaPropertiesNotBind(Map<String, T> kafkaProperties) {
        assertNotEquals(SECURITY_PROTOCOL_CONFIG_SASL, kafkaProperties.get(SECURITY_PROTOCOL_CONFIG));
        assertNotEquals(SASL_MECHANISM_OAUTH, kafkaProperties.get(SASL_MECHANISM));
        assertNotEquals(SASL_JAAS_CONFIG_OAUTH, kafkaProperties.get(SASL_JAAS_CONFIG));
        assertNotEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH, kafkaProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }
}
