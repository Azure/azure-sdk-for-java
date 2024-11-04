// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.Map;

class KafkaPropertiesBeanPostProcessor extends AbstractKafkaPropertiesBeanPostProcessor<KafkaProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPropertiesBeanPostProcessor.class);

    KafkaPropertiesBeanPostProcessor(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @SuppressWarnings("removal")
    @Override
    protected Map<String, Object> getMergedProducerProperties(KafkaProperties properties) {
        return properties.buildProducerProperties();
    }

    @Override
    protected Map<String, String> getRawProducerProperties(KafkaProperties properties) {
        return properties.getProducer().getProperties();
    }

    @SuppressWarnings("removal")
    @Override
    protected Map<String, Object> getMergedConsumerProperties(KafkaProperties properties) {
        return properties.buildConsumerProperties();
    }

    @Override
    protected Map<String, String> getRawConsumerProperties(KafkaProperties properties) {
        return properties.getConsumer().getProperties();
    }

    @SuppressWarnings("removal")
    @Override
    protected Map<String, Object> getMergedAdminProperties(KafkaProperties properties) {
        return properties.buildAdminProperties();

    }

    @Override
    protected Map<String, String> getRawAdminProperties(KafkaProperties properties) {
        return properties.getAdmin().getProperties();
    }

    @Override
    protected boolean needsPostProcess(Object bean) {
        return bean instanceof KafkaProperties;
    }

    @Override
    protected void customizeProcess(KafkaProperties properties) {
        clearAzureProperties(properties.getProperties());
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
