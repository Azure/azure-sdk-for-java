// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka.authentication;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.jaas.Jaas;
import com.azure.spring.cloud.service.implementation.jaas.JaasResolver;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils;
import com.azure.spring.cloud.service.implementation.kafka.KafkaOAuth2AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;
import static org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule.OAUTHBEARER_MECHANISM;
import static org.springframework.util.StringUtils.delimitedListToStringArray;

/**
 * OAuth2 authentication strategy for Azure Event Hubs Kafka using Microsoft Entra ID.
 * <p>
 * This strategy configures SASL/OAUTHBEARER authentication for Azure Event Hubs Kafka clients
 * using Microsoft Entra ID credentials (formerly Azure Active Directory).
 * </p>
 * <p>
 * Note: This is different from connection string-based authentication which is handled by
 * AzureEventHubsKafkaAutoConfiguration.
 * </p>
 *
 * @since 6.1.0
 */
public class KafkaOAuth2AuthenticationStrategy implements KafkaAuthenticationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOAuth2AuthenticationStrategy.class);
    private static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();
    
    public static final String SECURITY_PROTOCOL_CONFIG_SASL = SASL_SSL.name();
    public static final String SASL_MECHANISM_OAUTH = OAUTHBEARER_MECHANISM;
    public static final String AZURE_CONFIGURED_JAAS_OPTIONS_KEY = "azure.configured";
    public static final String AZURE_CONFIGURED_JAAS_OPTIONS_VALUE = "true";
    public static final String SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH =
        KafkaOAuth2AuthenticateCallbackHandler.class.getName();

    private static final Map<String, String> KAFKA_OAUTH_CONFIGS = Map.of(
        SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL,
        SASL_MECHANISM, SASL_MECHANISM_OAUTH,
        SASL_LOGIN_CALLBACK_HANDLER_CLASS, SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH
    );

    private static final String LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE = 
        "OAUTHBEARER authentication property {} will be configured as {} to support Azure Identity credentials.";
    private static final String LOG_OAUTH_AUTOCONFIGURATION_CONFIGURE = 
        "Spring Cloud Azure auto-configuration for Kafka OAUTHBEARER authentication will be loaded to configure your Kafka security and sasl properties to support Azure Identity credentials.";
    private static final String LOG_OAUTH_AUTOCONFIGURATION_RECOMMENDATION = 
        "Currently {} authentication mechanism is used, recommend to use Spring Cloud Azure auto-configuration for Kafka OAUTHBEARER authentication"
        + " which supports various Azure Identity credentials. To leverage the auto-configuration for OAuth2, you can just remove all your security, sasl and credential configurations of Kafka and Event Hubs."
        + " And configure Kafka bootstrap servers instead, which can be set as spring.kafka.boostrap-servers=EventHubsNamespacesFQDN:9093.";

    @Override
    public boolean shouldApply(Map<String, Object> kafkaProperties) {
        return meetAzureBootstrapServerConditions(kafkaProperties) 
            && meetSaslOAuthConditions(kafkaProperties);
    }

    @Override
    public void applyAuthentication(Map<String, Object> mergedProperties,
                                    Map<String, String> rawPropertiesMap,
                                    AzureGlobalProperties azureGlobalProperties) {
        resolveJaasForAzure(mergedProperties, azureGlobalProperties)
            .ifPresent(jaas -> {
                configJaasToKafkaRawProperties(jaas, rawPropertiesMap);
                logConfigureOAuthProperties();
            });
    }

    @Override
    public void clearAzureProperties(Map<String, String> rawPropertiesMap) {
        AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.getPropertyKeys()
            .forEach(rawPropertiesMap::remove);
    }

    private Optional<Jaas> resolveJaasForAzure(Map<String, Object> mergedProperties,
                                               AzureGlobalProperties azureGlobalProperties) {
        JaasResolver resolver = new JaasResolver();
        Jaas jaas = resolver.resolve((String) mergedProperties.get(SASL_JAAS_CONFIG))
            .orElse(new Jaas(OAuthBearerLoginModule.class.getName()));
        setAzurePropertiesToJaasOptionsIfAbsent(azureGlobalProperties, jaas);
        setKafkaPropertiesToJaasOptions(mergedProperties, jaas);
        jaas.getOptions().put(AZURE_CONFIGURED_JAAS_OPTIONS_KEY, AZURE_CONFIGURED_JAAS_OPTIONS_VALUE);
        return Optional.of(jaas);
    }

    private void configJaasToKafkaRawProperties(Jaas jaas, Map<String, String> rawPropertiesMap) {
        rawPropertiesMap.putAll(KAFKA_OAUTH_CONFIGS);
        rawPropertiesMap.put(SASL_JAAS_CONFIG, jaas.toString());
    }

    private void logConfigureOAuthProperties() {
        LOGGER.info(LOG_OAUTH_AUTOCONFIGURATION_CONFIGURE);
        LOGGER.debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        LOGGER.debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        LOGGER.debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_JAAS_CONFIG, 
            "***the value involves credentials and will not be logged***");
        LOGGER.debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_LOGIN_CALLBACK_HANDLER_CLASS,
            SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
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

    private boolean meetSaslOAuthConditions(Map<String, Object> sourceProperties) {
        String securityProtocol = (String) sourceProperties.get(SECURITY_PROTOCOL_CONFIG);
        String saslMechanism = (String) sourceProperties.get(SASL_MECHANISM);
        String jaasConfig = (String) sourceProperties.get(SASL_JAAS_CONFIG);
        
        if (meetSaslProtocolConditions(securityProtocol) 
            && meetSaslOAuth2MechanismConditions(saslMechanism)
            && meetJaasConditions(jaasConfig)) {
            return true;
        }
        LOGGER.info(LOG_OAUTH_AUTOCONFIGURATION_RECOMMENDATION, saslMechanism);
        return false;
    }

    private boolean meetSaslProtocolConditions(String securityProtocol) {
        return securityProtocol == null || SECURITY_PROTOCOL_CONFIG_SASL.equalsIgnoreCase(securityProtocol);
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

    private boolean meetAzureBootstrapServerConditions(Map<String, Object> sourceProperties) {
        Object bootstrapServers = sourceProperties.get(BOOTSTRAP_SERVERS_CONFIG);
        List<String> serverList;
        
        if (bootstrapServers instanceof String) {
            serverList = Arrays.asList(delimitedListToStringArray((String) bootstrapServers, ","));
        } else if (bootstrapServers instanceof Iterable<?>) {
            serverList = new java.util.ArrayList<>();
            for (Object obj : (Iterable<?>) bootstrapServers) {
                if (obj instanceof String) {
                    serverList.add((String) obj);
                } else {
                    LOGGER.debug("Kafka bootstrap server configuration doesn't meet passwordless requirements.");
                    return false;
                }
            }
        } else {
            LOGGER.debug("Kafka bootstrap server configuration doesn't meet passwordless requirements.");
            return false;
        }

        return serverList.size() == 1 && serverList.get(0).endsWith(":9093");
    }
}
