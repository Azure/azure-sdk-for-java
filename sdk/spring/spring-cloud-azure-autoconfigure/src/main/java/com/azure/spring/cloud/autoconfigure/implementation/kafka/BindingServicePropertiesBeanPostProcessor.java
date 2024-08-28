// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

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

    static final String KAFKA_OAUTH2_SPRING_MAIN_SOURCES = String.join(",",
        AzureKafkaSpringCloudStreamConfiguration.class.getName(),
        AzureEventHubsKafkaOAuth2AutoConfiguration.class.getName());
    private static final String DEFAULT_KAFKA_BINDER_NAME = "kafka";
    private static final String KAFKA_BINDER_TYPE = "kafka";

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof BindingServiceProperties) {
            BindingServiceProperties bindingServiceProperties = (BindingServiceProperties) bean;
            if (bindingServiceProperties.getBinders().isEmpty()) {
                Map<String, Object> environment = new HashMap<>();
                Map<String, Object> springMainPropertiesMap = getOrCreateSpringMainPropertiesMap(environment);
                configureSpringMainSources(springMainPropertiesMap);

                BinderProperties defaultKafkaBinder = new BinderProperties();
                defaultKafkaBinder.setEnvironment(environment);
                defaultKafkaBinder.setDefaultCandidate(!StringUtils.hasText(bindingServiceProperties.getDefaultBinder()));

                Map<String, BinderProperties> binders = new HashMap<>();
                binders.put(DEFAULT_KAFKA_BINDER_NAME, defaultKafkaBinder);

                bindingServiceProperties.setBinders(binders);
            } else {
                for (Map.Entry<String, BinderProperties> entry : bindingServiceProperties.getBinders().entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        boolean isBinderTypeKafka = KAFKA_BINDER_TYPE.equalsIgnoreCase(entry.getValue().getType());
                        boolean isBinderNameKafka = DEFAULT_KAFKA_BINDER_NAME.equalsIgnoreCase(entry.getKey());
                        if (isBinderTypeKafka || isBinderNameKafka) {
                            Map<String, Object> environment = entry.getValue().getEnvironment();
                            Map<String, Object> springMainPropertiesMap = getOrCreateSpringMainPropertiesMap(environment);
                            configureSpringMainSources(springMainPropertiesMap);
                        }
                    }
                }
            }
        }
        return bean;
    }

    void configureSpringMainSources(Map<String, Object> springMainPropertiesMap) {
        String sources = KAFKA_OAUTH2_SPRING_MAIN_SOURCES;
        if (StringUtils.hasText((String) springMainPropertiesMap.get("sources"))) {
            sources += "," + springMainPropertiesMap.get("sources");
        }
        springMainPropertiesMap.put("sources", sources);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> getOrCreateSpringMainPropertiesMap(Map<String, Object> map) {
        Map<String, Object> spring = (Map<String, Object>) map.computeIfAbsent("spring", k -> new LinkedHashMap<String, Object>());
        return (Map<String, Object>) spring.computeIfAbsent("main", k -> new LinkedHashMap<String, Object>());
    }
}
