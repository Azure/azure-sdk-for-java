// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.cloud.stream.binder.kafka.provisioning.KafkaTopicProvisioner.normalalizeBootPropsWithBinder;

/**
 * {@link BeanPostProcessor} to apply {@link AzureGlobalProperties} and Kafka OAuth properties
 * to {@link KafkaBinderConfigurationProperties}.
 */
class KafkaBinderConfigurationPropertiesBeanPostProcessor extends AbstractKafkaPropertiesBeanPostProcessor<KafkaBinderConfigurationProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);

    KafkaBinderConfigurationPropertiesBeanPostProcessor(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Override
    protected Map<String, Object> getMergedProducerProperties(KafkaBinderConfigurationProperties properties) {
        return mergeNonAdminProperties(properties.mergedProducerConfiguration(), properties.getConfiguration());
    }

    @Override
    protected Map<String, String> getRawProducerProperties(KafkaBinderConfigurationProperties properties) {
        return properties.getProducerProperties();
    }

    @Override
    protected Map<String, Object> getMergedConsumerProperties(KafkaBinderConfigurationProperties properties) {
        return mergeNonAdminProperties(properties.mergedConsumerConfiguration(), properties.getConfiguration());
    }

    @Override
    protected Map<String, String> getRawConsumerProperties(KafkaBinderConfigurationProperties properties) {
        return properties.getConsumerProperties();
    }

    @Override
    protected Map<String, Object> getMergedAdminProperties(KafkaBinderConfigurationProperties properties) {
        return mergeAdminProperties(properties);
    }

    @Override
    protected Map<String, String> getRawAdminProperties(KafkaBinderConfigurationProperties properties) {
        return properties.getConfiguration();
    }

    @Override
    protected boolean needsPostProcess(Object bean) {
        return bean instanceof KafkaBinderConfigurationProperties;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private Map<String, Object> mergeNonAdminProperties(Map<String, Object> mergedPropertiesWithoutDefault, Map<String, String> defaultProperties) {
        Map<String, Object> merged = new HashMap<>(defaultProperties);
        merged.putAll(mergedPropertiesWithoutDefault);
        return merged;
    }

    @SuppressWarnings("removal")
    private Map<String, Object> mergeAdminProperties(KafkaBinderConfigurationProperties properties) {
        Map<String, Object> adminProperties = properties.getKafkaProperties().buildAdminProperties();
        normalalizeBootPropsWithBinder(adminProperties, properties.getKafkaProperties(), properties);
        AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.getPropertyKeys()
            .forEach(k -> PROPERTY_MAPPER.from(properties.getConfiguration().get(k)).to(v -> adminProperties.put(k, v)));
        return adminProperties;
    }

}
