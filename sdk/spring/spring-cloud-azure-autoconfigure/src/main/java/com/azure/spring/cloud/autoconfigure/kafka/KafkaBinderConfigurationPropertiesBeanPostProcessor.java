// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.properties.AzureThirdPartyServiceProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.configureOAuthProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.needConfigureSaslOAuth;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.convertAzurePropertiesToConfigMap;
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
            configureKafkaBinderProperties(mergedConsumerConfiguration, sourceConsumerProperties);

            Map<String, Object> mergedProducerConfiguration = binderConfigurationProperties.mergedProducerConfiguration();
            Map<String, String> sourceProducerProperties = binderConfigurationProperties.getProducerProperties();
            configureKafkaBinderProperties(mergedProducerConfiguration, sourceProducerProperties);

            //Should configure admin at last since the highest priority properties for admin is the binder configuration,
            //which is one of the property sources for consumer and producer binder properties,
            //thus if we change it then it might influence the final raw properties for consumer and producer.
            KafkaProperties kafkaProperties = binderConfigurationProperties.getKafkaProperties();
            Map<String, Object> adminProperties = kafkaProperties.buildAdminProperties();
            normalalizeBootPropsWithBinder(adminProperties, kafkaProperties, binderConfigurationProperties);
            configureKafkaBinderProperties(adminProperties, binderConfigurationProperties.getConfiguration());
        }
        return bean;
    }

    private void configureKafkaBinderProperties(Map<String, Object> mergedConfiguration, Map<String, String> sourceProperties) {
        if (needConfigureSaslOAuth(mergedConfiguration)) {
            AzureThirdPartyServiceProperties azureKafkaConsumerProperties =
                buildAzureProperties(mergedConfiguration, azureGlobalProperties);
            convertAzurePropertiesToConfigMap(azureKafkaConsumerProperties, sourceProperties);
            configureOAuthProperties(sourceProperties);
        }
    }

}
