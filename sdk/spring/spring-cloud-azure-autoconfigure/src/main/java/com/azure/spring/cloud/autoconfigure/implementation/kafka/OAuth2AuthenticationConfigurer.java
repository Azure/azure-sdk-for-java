// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.jaas.Jaas;
import com.azure.spring.cloud.service.implementation.jaas.JaasResolver;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.AZURE_CONFIGURED_JAAS_OPTIONS_KEY;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.AZURE_CONFIGURED_JAAS_OPTIONS_VALUE;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.SECURITY_PROTOCOL_CONFIG_SASL;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;

/**
 * Configures OAuth2 (OAUTHBEARER) authentication for Kafka using Azure Identity credentials.
 * This configurer handles Azure Event Hubs for Kafka scenarios with Microsoft Entra ID authentication.
 */
class OAuth2AuthenticationConfigurer extends AbstractKafkaAuthenticationConfigurer {

    private static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();
    private static final Map<String, String> KAFKA_OAUTH_CONFIGS = Map.of(
        SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL,
        SASL_MECHANISM, SASL_MECHANISM_OAUTH,
        SASL_LOGIN_CALLBACK_HANDLER_CLASS, SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH
    );

    private final AzureProperties azureProperties;

    OAuth2AuthenticationConfigurer(AzureProperties azureProperties, Logger logger) {
        super(logger);
        this.azureProperties = azureProperties;
    }

    @Override
    public void configure(Map<String, Object> mergedProperties, Map<String, String> rawProperties) {
        JaasResolver resolver = new JaasResolver();
        Jaas jaas = resolver.resolve((String) mergedProperties.get(SASL_JAAS_CONFIG))
            .orElse(new Jaas(OAuthBearerLoginModule.class.getName()));

        setAzurePropertiesToJaasOptionsIfAbsent(azureProperties, jaas);
        setKafkaPropertiesToJaasOptions(mergedProperties, jaas);
        jaas.getOptions().put(AZURE_CONFIGURED_JAAS_OPTIONS_KEY, AZURE_CONFIGURED_JAAS_OPTIONS_VALUE);

        rawProperties.putAll(KAFKA_OAUTH_CONFIGS);
        rawProperties.put(SASL_JAAS_CONFIG, jaas.toString());

        logConfiguration();
    }

    @Override
    protected boolean meetAuthenticationConditions(Map<String, Object> sourceProperties) {
        String securityProtocol = getSecurityProtocol(sourceProperties);
        String saslMechanism = getSaslMechanism(sourceProperties);
        String jaasConfig = getJaasConfig(sourceProperties);

        if (meetSaslProtocolConditions(securityProtocol)
            && meetSaslOAuth2MechanismConditions(saslMechanism)
            && meetJaasConditions(jaasConfig)) {
            return true;
        }

        logger.info("Currently {} authentication mechanism is used, recommend to use Spring Cloud Azure "
                + "auto-configuration for Kafka OAUTHBEARER authentication which supports various Azure Identity "
                + "credentials. To leverage the auto-configuration for OAuth2, you can just remove all your security, "
                + "sasl and credential configurations of Kafka and Event Hubs. And configure Kafka bootstrap servers "
                + "instead, which can be set as spring.kafka.boostrap-servers=EventHubsNamespacesFQDN:9093.",
            saslMechanism);
        return false;
    }

    private boolean meetSaslOAuth2MechanismConditions(String saslMechanism) {
        return saslMechanism == null || SASL_MECHANISM_OAUTH.equalsIgnoreCase(saslMechanism);
    }

    private boolean meetJaasConditions(String jaasConfig) {
        if (jaasConfig == null) {
            return true;
        }
        JaasResolver resolver = new JaasResolver();
        return resolver.resolve(jaasConfig)
            .map(jaas -> AZURE_CONFIGURED_JAAS_OPTIONS_VALUE.equals(
                jaas.getOptions().get(AZURE_CONFIGURED_JAAS_OPTIONS_KEY)))
            .orElse(false);
    }

    private void setKafkaPropertiesToJaasOptions(Map<String, ?> properties, Jaas jaas) {
        AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.getPropertyKeys()
            .forEach(k -> PROPERTY_MAPPER.from(properties.get(k)).to(p -> jaas.getOptions().put(k, (String) p)));
    }

    private void setAzurePropertiesToJaasOptionsIfAbsent(AzureProperties azureProperties, Jaas jaas) {
        convertAzurePropertiesToMap(azureProperties)
            .forEach((k, v) -> jaas.getOptions().putIfAbsent(k, v));
    }

    private Map<String, String> convertAzurePropertiesToMap(AzureProperties properties) {
        Map<String, String> configs = new HashMap<>();
        for (AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping m
            : AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.values()) {
            PROPERTY_MAPPER.from(m.getter().apply(properties)).to(p -> configs.put(m.propertyKey(), p));
        }
        return configs;
    }

    private void logConfiguration() {
        logger.info("Spring Cloud Azure auto-configuration for Kafka OAUTHBEARER authentication will be loaded to "
            + "configure your Kafka security and sasl properties to support Azure Identity credentials.");
        logger.debug("OAUTHBEARER authentication property {} will be configured as {} to support Azure Identity credentials.",
            SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        logger.debug("OAUTHBEARER authentication property {} will be configured as {} to support Azure Identity credentials.",
            SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        logger.debug("OAUTHBEARER authentication property {} will be configured as {} to support Azure Identity credentials.",
            SASL_JAAS_CONFIG, "***the value involves credentials and will not be logged***");
        logger.debug("OAUTHBEARER authentication property {} will be configured as {} to support Azure Identity credentials.",
            SASL_LOGIN_CALLBACK_HANDLER_CLASS, SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
    }
}
