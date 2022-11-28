// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils;
import com.azure.spring.cloud.service.implementation.jaas.Jaas;
import com.azure.spring.cloud.service.implementation.jaas.JaasResolver;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils;
import org.slf4j.Logger;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS_KEY;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS_VALUE;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.DEFAULT_SASL_JAAS_CONFIG_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.LOG_OAUTH_AUTOCONFIGURATION_CONFIGURE;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.SECURITY_PROTOCOL_CONFIG_SASL;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.configureKafkaUserAgent;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.needConfigureSaslOAuth;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;

abstract class AbstractKafkaPropertiesBeanPostProcessor<T> implements BeanPostProcessor {

    protected static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();

    private final AzureGlobalProperties azureGlobalProperties;

    AbstractKafkaPropertiesBeanPostProcessor(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (needsPostProcess(bean)) {
            T properties = (T) bean;

            replaceAzurePropertiesWithJaas(getMergedProducerProperties(properties), getRawProducerProperties(properties));
            replaceAzurePropertiesWithJaas(getMergedConsumerProperties(properties), getRawConsumerProperties(properties));
            replaceAzurePropertiesWithJaas(getMergedAdminProperties(properties), getRawAdminProperties(properties));
            customize(properties);
        }
        return bean;
    }

    /**
     * Create a map of the merged Kafka producer properties from the Kafka Spring properties.
     * @param properties the Kafka Spring properties
     * @return a Map containing all Kafka producer properties
     */
    protected abstract Map<String, Object> getMergedProducerProperties(T properties);

    /**
     * Get the raw {@link Map} object from the Kafka Spring properties that stores producer-specific properties.
     * @param properties the Kafka Spring properties
     * @return the map from Kafka properties storing producer-specific properties
     */
    protected abstract Map<String, String> getRawProducerProperties(T properties);

    /**
     * Create a map of the merged Kafka consumer properties from the Kafka Spring properties.
     * @param properties the Kafka Spring properties
     * @return a Map containing all Kafka consumer properties
     */
    protected abstract Map<String, Object> getMergedConsumerProperties(T properties);

    /**
     * Get the raw {@link Map} object from the Kafka Spring properties that stores consumer-specific properties.
     * @param properties the Kafka Spring properties
     * @return the map from Kafka properties storing consumer-specific properties
     */
    protected abstract Map<String, String> getRawConsumerProperties(T properties);

    /**
     * Create a map of the merged Kafka admin properties from the Kafka Spring properties.
     * @param properties the Kafka Spring properties
     * @return a Map containing all Kafka admin properties
     */
    protected abstract Map<String, Object> getMergedAdminProperties(T properties);

    /**
     * Get the raw {@link Map} object from the Kafka Spring properties that stores admin-specific properties.
     * @param properties the Kafka Spring properties
     * @return the map from Kafka properties storing admin-specific properties
     */
    protected abstract Map<String, String> getRawAdminProperties(T properties);

    protected abstract boolean needsPostProcess(Object bean);

    protected abstract Logger getLogger();

    /**
     * Process Kafka Spring properties for any customized operations.
     * @param properties the Kafka Spring properties
     */
    protected void customize(T properties) {
    }


    protected void clearAzureProperties(Map<String, String> properties) {
        AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.getPropertyKeys()
            .forEach(properties::remove);
    }

    /**
     * This method executes two operations:
     * <p>
     * 1. When this configuration meets Azure Kafka passwordless startup requirements, convert all Azure properties
     * in Kafka to {@link Jaas}, and configure the JAAS configuration back to Kafka.
     * </p>
     * <p>
     * 2. Clear any Azure properties in Kafka properties.
     * </p>
     * @param mergedProperties the merged Kafka properties which can contain Azure properties to resolve JAAS from
     * @param rawPropertiesMap the raw Kafka properties Map to configure JAAS to and remove Azure Properties from
     */
    private void replaceAzurePropertiesWithJaas(Map<String, Object> mergedProperties, Map<String, String> rawPropertiesMap) {
        resolveJaasForAzure(mergedProperties)
            .ifPresent(jaas -> {
                configJaasToKafkaRawProperties(jaas, rawPropertiesMap);
                logConfigureOAuthProperties();
                configureKafkaUserAgent();
            });
        clearAzureProperties(rawPropertiesMap);
    }

    private Optional<Jaas> resolveJaasForAzure(Map<String, Object> mergedProperties) {
        if (needConfigureSaslOAuth(mergedProperties)) {
            Jaas jaas = JaasResolver.resolve(getSaslJaasFromKafkaOrDefault(mergedProperties));
            setAzurePropertiesToJaasOptionsIfAbsent(azureGlobalProperties, jaas);
            setKafkaPropertiesToJaasOptions(mergedProperties, jaas);
            jaas.getOptions().put(AZURE_CONFIGURED_JAAS_OPTIONS_KEY, AZURE_CONFIGURED_JAAS_OPTIONS_VALUE);
            return Optional.of(jaas);
        } else {
            return Optional.empty();
        }
    }

    private String getSaslJaasFromKafkaOrDefault(Map<String, Object> mergedProperties) {
        String jaas = (String) mergedProperties.get(SASL_JAAS_CONFIG);
        return StringUtils.hasText(jaas) ? jaas : DEFAULT_SASL_JAAS_CONFIG_OAUTH;
    }

    private void configJaasToKafkaRawProperties(Jaas jaas, Map<String, String> rawPropertiesMap) {
        rawPropertiesMap.putAll(AzureKafkaConfigurationUtils.KAFKA_OAUTH_CONFIGS);
        rawPropertiesMap.put(SASL_JAAS_CONFIG, jaas.toString());
    }

    /**
     * Configure necessary OAuth properties for kafka properties and log for the changes.
     */
    private void logConfigureOAuthProperties() {
        getLogger().info(LOG_OAUTH_AUTOCONFIGURATION_CONFIGURE);
        getLogger().debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        getLogger().debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        getLogger().debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_JAAS_CONFIG, "***the value involves credentials and will not be logged***");
        getLogger().debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_LOGIN_CALLBACK_HANDLER_CLASS,
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
        for (AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping m : AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.values()) {
            PROPERTY_MAPPER.from(m.getter().apply(properties)).to(p -> configs.put(m.propertyKey(), p));
        }
        return configs;
    }

}
