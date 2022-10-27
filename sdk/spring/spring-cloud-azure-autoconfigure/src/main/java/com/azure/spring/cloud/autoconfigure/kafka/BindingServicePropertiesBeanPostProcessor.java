// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.stream.config.BinderProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
                configureBinderSources(readSpringMainPropertiesMap(kafkaBinderSourceProperty.getEnvironment()));

                Map<String, BinderProperties> kafkaBinderPropertyMap = new HashMap<>();
                kafkaBinderPropertyMap.put(KAKFA_BINDER_DEFAULT_NAME, kafkaBinderSourceProperty);

                bindingServiceProperties.setBinders(kafkaBinderPropertyMap);
            } else {
                for (Map.Entry<String, BinderProperties> entry : bindingServiceProperties.getBinders().entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null
                            && (KAKFA_BINDER_TYPE.equalsIgnoreCase(entry.getValue().getType())
                            || KAKFA_BINDER_DEFAULT_NAME.equalsIgnoreCase(entry.getKey()))) {
                        configureBinderSources(readSpringMainPropertiesMap(entry.getValue().getEnvironment()));
                    }
                }
            }
        }
        return bean;
    }

    void configureBinderSources(Map<String, Object> originalSources) {
        StringBuilder sources = new StringBuilder(AzureKafkaSpringCloudStreamConfiguration.AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS);
        if (StringUtils.hasText((String) originalSources.get("sources"))) {
            sources.append("," + originalSources.get("sources"));
        }
        originalSources.put("sources", sources.toString());
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> readSpringMainPropertiesMap(Map<String, Object> map) {
        if (map.containsKey("spring")) {
            Map<String, Object> spring = (Map<String, Object>) map.get("spring");
            if (spring.containsKey("main")) {
                return (Map<String, Object>) spring.get("main");
            } else {
                LinkedHashMap<String, Object> main = new LinkedHashMap<>();
                spring.put("main", main);
                return main;
            }
        } else {
            Map<String, Object> main = new LinkedHashMap<>();
            Map<String, Object> spring = new LinkedHashMap<>();
            spring.put("main", main);
            map.put("spring", spring);
            return main;
        }
    }
}
