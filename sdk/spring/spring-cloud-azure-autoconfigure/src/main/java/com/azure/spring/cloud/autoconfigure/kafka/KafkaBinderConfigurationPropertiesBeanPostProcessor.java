// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.*;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.convertAzurePropertiesToConfigMap;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.convertAzurePropertiesToJaasProperty;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.springframework.cloud.stream.binder.kafka.provisioning.KafkaTopicProvisioner.normalalizeBootPropsWithBinder;

/**
 * {@link BeanPostProcessor} to apply {@link AzureGlobalProperties} and Kafka OAuth properties
 * to {@link KafkaBinderConfigurationProperties}.
 */
class KafkaBinderConfigurationPropertiesBeanPostProcessor implements BeanPostProcessor {

    private final AzureGlobalProperties azureGlobalProperties;

    KafkaBinderConfigurationPropertiesBeanPostProcessor(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof KafkaBinderConfigurationProperties) {
            KafkaBinderConfigurationProperties binderConfigurationProperties = (KafkaBinderConfigurationProperties) bean;
            Map<String, Object> mergedConsumerConfiguration = binderConfigurationProperties.mergedConsumerConfiguration();
            Map<String, String> sourceConsumerProperties = binderConfigurationProperties.getConsumerProperties();
            if (needConfigureSaslOAuth(mergedConsumerConfiguration)) {
                configureKafkaBinderProperties(mergedConsumerConfiguration, sourceConsumerProperties);
                configureKafkaUserAgent();
            }

            Map<String, Object> mergedProducerConfiguration = binderConfigurationProperties.mergedProducerConfiguration();
            Map<String, String> sourceProducerProperties = binderConfigurationProperties.getProducerProperties();
            if (needConfigureSaslOAuth(mergedProducerConfiguration)) {
                configureKafkaBinderProperties(mergedProducerConfiguration, sourceProducerProperties);
                configureKafkaUserAgent();
            }
            //Should configure admin at last since the highest priority properties for admin is the binder configuration,
            //which is one of the property sources for consumer and producer binder properties,
            //thus if we change it then it might influence the final raw properties for consumer and producer.
            KafkaProperties kafkaProperties = binderConfigurationProperties.getKafkaProperties();
            Map<String, Object> adminProperties = kafkaProperties.buildAdminProperties();
            normalalizeBootPropsWithBinder(adminProperties, kafkaProperties, binderConfigurationProperties);
            if (needConfigureSaslOAuth(adminProperties)) {
                configureKafkaBinderProperties(adminProperties, binderConfigurationProperties.getConfiguration());
                configureKafkaUserAgent();
            }
        }
        return bean;
    }

    void configureKafkaBinderProperties(Map<String, Object> mergedConfiguration, Map<String, String> sourceProperties) {
        configureOAuthProperties(sourceProperties);
        AzurePasswordlessProperties azurePasswordlessProperties =
            buildAzureProperties(mergedConfiguration, azureGlobalProperties);
        sourceProperties.put(SASL_JAAS_CONFIG,
            convertAzurePropertiesToJaasProperty(azurePasswordlessProperties, SASL_JAAS_CONFIG_OAUTH));
        logConfigureOAuthProperties();
    }

}
