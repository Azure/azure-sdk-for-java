// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.stream.binder.DefaultBinderFactory;
import org.springframework.cloud.stream.config.BinderProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link BeanPostProcessor} for {@link BindingServiceProperties} to support Azure Identity for Kafka Spring Cloud Stream Binder.
 * This BeanPostProcessor will put the {@link AzureKafkaSpringCloudStreamConfiguration} into Kafka binder's context.
 *
 * @since 4.4.0
 */
class BindingServicePropertiesBeanPostProcessor implements BeanPostProcessor {

    static final String SPRING_MAIN_SOURCES_PROPERTY = "spring.main.sources";
    private static final String KAKFA_BINDER_DEFAULT_NAME = "kafka";
    private static final String KAKFA_BINDER_TYPE = "kafka";

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof BindingServiceProperties) {
            BindingServiceProperties bindingServiceProperties = (BindingServiceProperties) bean;
            if (bindingServiceProperties.getBinders().isEmpty()) {
                BinderProperties kafkaBinderSourceProperty = new BinderProperties();
                configureBinderSources(kafkaBinderSourceProperty, AzureKafkaSpringCloudStreamConfiguration.AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS);

                Map<String, BinderProperties> kafkaBinderPropertyMap = new HashMap<>();
                kafkaBinderPropertyMap.put(KAKFA_BINDER_DEFAULT_NAME, kafkaBinderSourceProperty);

                bindingServiceProperties.setBinders(kafkaBinderPropertyMap);
            } else {
                for (Map.Entry<String, BinderProperties> entry : bindingServiceProperties.getBinders().entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null
                            && (KAKFA_BINDER_TYPE.equalsIgnoreCase(entry.getValue().getType())
                            || KAKFA_BINDER_DEFAULT_NAME.equalsIgnoreCase(entry.getKey()))) {
                        configureBinderSources(entry.getValue(), buildKafkaBinderSources(entry.getValue()));
                    }
                }
            }
        }
        return bean;
    }

    private String buildKafkaBinderSources(BinderProperties binderProperties) {
        Map<String, Object> flattenedProperties = new HashMap<>();
        flatten(null, binderProperties.getEnvironment(), flattenedProperties);
        
        StringBuilder sources = new StringBuilder(AzureKafkaSpringCloudStreamConfiguration.AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS);
        if (flattenedProperties.get(SPRING_MAIN_SOURCES_PROPERTY) != null) {
            sources.append("," + flattenedProperties.get(SPRING_MAIN_SOURCES_PROPERTY));
        }
        return sources.toString();
    }

    private void configureBinderSources(BinderProperties binderProperties, String sources) {
        binderProperties.getEnvironment().put(SPRING_MAIN_SOURCES_PROPERTY, sources);
    }

    /**
     * Ensures that nested properties are flattened (i.e., foo.bar=baz instead of
     * foo={bar=baz}). Copied from {@link DefaultBinderFactory}.
     * @param propertyName property name to flatten
     * @param value value that contains the property name
     * @param flattenedProperties map to which we'll add the falttened property
     */
    @SuppressWarnings("unchecked")
    private void flatten(String propertyName, Object value,
            Map<String, Object> flattenedProperties) {
        if (value instanceof Map) {
            ((Map<Object, Object>) value).forEach((k, v) -> flatten(
                (propertyName != null ? propertyName + "." : "") + k, v,
                flattenedProperties));
        }
        else {
            flattenedProperties.put(propertyName, value.toString());
        }
    }
}
