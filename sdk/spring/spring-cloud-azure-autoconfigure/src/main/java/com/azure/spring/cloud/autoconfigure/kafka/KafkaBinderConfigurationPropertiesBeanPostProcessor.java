// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.configureKafkaOAuth2Properties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.needConfigureSaslOAuth;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.configureKafkaUserAgent;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.clearAzureProperties;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.copyAzureProperties;
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
            Map<String, Object> mergedConsumerConfiguration = new HashMap<>(binderConfigurationProperties.mergedConsumerConfiguration());
            // Since in the above call of KafkaBinderConfigurationProperties.mergedConsumerConfiguration, kafka only merge those kafka-defined configs,
            // then we need to manually copy azure configs from common kafka configs to the client specific configs.
            // For the priority: boot kafka client < binder kafka common < binder kafka client. While since we have moved all boot kafka configs to jaas,
            // then in the mergedConsumerConfiguration there will only be binder kafka client configs which has the highest priority. Thus the below call
            // should not override.
            copyAzureProperties(binderConfigurationProperties.getConfiguration(), mergedConsumerConfiguration);
            Map<String, String> sourceConsumerProperties = binderConfigurationProperties.getConsumerProperties();
            if (needConfigureSaslOAuth(mergedConsumerConfiguration)) {
                configureKafkaOAuth2Properties(mergedConsumerConfiguration, azureGlobalProperties, sourceConsumerProperties);
                configureKafkaUserAgent();
            }

            Map<String, Object> mergedProducerConfiguration = new HashMap<>(binderConfigurationProperties.mergedProducerConfiguration());
            copyAzureProperties(binderConfigurationProperties.getConfiguration(), mergedProducerConfiguration);
            Map<String, String> sourceProducerProperties = binderConfigurationProperties.getProducerProperties();
            if (needConfigureSaslOAuth(mergedProducerConfiguration)) {
                configureKafkaOAuth2Properties(mergedProducerConfiguration, azureGlobalProperties, sourceProducerProperties);
                configureKafkaUserAgent();
            }
            //Should configure admin at last since the highest priority properties for admin is the binder configuration,
            //which is one of the property sources for consumer and producer binder properties,
            //thus if we change it then it might influence the final raw properties for consumer and producer.
            KafkaProperties kafkaProperties = binderConfigurationProperties.getKafkaProperties();
            Map<String, Object> adminProperties = kafkaProperties.buildAdminProperties();
            normalalizeBootPropsWithBinder(adminProperties, kafkaProperties, binderConfigurationProperties);
            copyAzureProperties(binderConfigurationProperties.getConfiguration(), adminProperties);
            if (needConfigureSaslOAuth(adminProperties)) {
                configureKafkaOAuth2Properties(adminProperties, azureGlobalProperties, binderConfigurationProperties.getConfiguration());
                configureKafkaUserAgent();
            }
            clearAzurePropertiesInKafkaCustomizedProperties(binderConfigurationProperties);
        }
        return bean;
    }

    private void clearAzurePropertiesInKafkaCustomizedProperties(KafkaBinderConfigurationProperties properties) {
        clearAzureProperties(properties.getConfiguration());
        clearAzureProperties(properties.getConsumerProperties());
        clearAzureProperties(properties.getProducerProperties());
    }

}
