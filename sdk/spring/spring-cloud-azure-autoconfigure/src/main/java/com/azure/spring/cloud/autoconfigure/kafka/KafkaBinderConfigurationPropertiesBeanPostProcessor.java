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

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.configureKafkaUserAgent;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.configureOAuthProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.logConfigureOAuthProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.needConfigureSaslOAuth;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.convertAzurePropertiesToConfigMap;
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

    private void configureKafkaBinderProperties(Map<String, Object> mergedConfiguration, Map<String, String> sourceProperties) {
        AzurePasswordlessProperties azurePasswordlessProperties =
            buildAzureProperties(mergedConfiguration, azureGlobalProperties);
        convertAzurePropertiesToConfigMap(azurePasswordlessProperties, sourceProperties);
        configureOAuthProperties(sourceProperties);
        logConfigureOAuthProperties();
    }

}
