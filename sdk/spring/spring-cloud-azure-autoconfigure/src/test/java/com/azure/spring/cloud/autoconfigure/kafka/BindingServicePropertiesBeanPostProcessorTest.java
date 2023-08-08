// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.cloud.stream.config.BinderProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.autoconfigure.kafka.BindingServicePropertiesBeanPostProcessor.KAFKA_OAUTH2_SPRING_MAIN_SOURCES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SuppressWarnings("unchecked")
class BindingServicePropertiesBeanPostProcessorTest {

    private final BindingServicePropertiesBeanPostProcessor bpp = new BindingServicePropertiesBeanPostProcessor();

    @Test
    void testReadSpringMainPropertiesMapWithoutOriginalValues() {
        Map<String, Object> env = new LinkedHashMap<>();
        Map<String, Object> mainPropertiesMap = buildSpringMainPropertiesMap(env, null, null, null);
        assertSame(mainPropertiesMap, ((Map<String, Object>) env.get("spring")).get("main"));
    }

    @Test
    void testReadSpringMainPropertiesMapWithSpringProp() {
        Map<String, Object> env = new LinkedHashMap<>();
        Map<String, Object> mainPropertiesMap = buildSpringMainPropertiesMap(env, "profiles", "active", "dev");

        assertEquals("dev", ((Map<String, Map<String, Object>>) env.get("spring")).get("profiles").get("active"));
        assertSame(mainPropertiesMap, ((Map<String, Object>) env.get("spring")).get("main"));
    }

    @Test
    void testReadSpringMainPropertiesMapWithMainProp() {
        Map<String, Object> env = new LinkedHashMap<>();
        Map<String, Object> mainPropertiesMap = buildSpringMainPropertiesMap(env, "main", "banner-mode", "test");

        assertEquals("test", ((Map<String, Map<String, Object>>) env.get("spring")).get("main").get("banner-mode"));
        assertSame(mainPropertiesMap, ((Map<String, Object>) env.get("spring")).get("main"));
    }

    @Test
    void testReadSpringMainPropertiesMapWithSourcesProp() {
        Map<String, Object> env = new LinkedHashMap<>();
        Map<String, Object> mainPropertiesMap = buildSpringMainPropertiesMap(env, "main", "sources", "test");

        assertEquals("test", ((Map<String, Map<String, Object>>) env.get("spring")).get("main").get("sources"));
        assertSame(mainPropertiesMap, ((Map<String, Object>) env.get("spring")).get("main"));
    }

    @Test
    void testConfigureBinderSources() {
        Map<String, Object> env = new LinkedHashMap<>();
        Map<String, Object> mainPropertiesMap = buildSpringMainPropertiesMap(env, "main", "sources", "test");
        bpp.configureSpringMainSources(mainPropertiesMap);
        assertEquals(KAFKA_OAUTH2_SPRING_MAIN_SOURCES + ",test", ((Map<String, Map<String, Object>>) env.get("spring")).get("main").get("sources"));

        env.clear();
        mainPropertiesMap = buildSpringMainPropertiesMap(env, "main", "profiles", "active");
        bpp.configureSpringMainSources(mainPropertiesMap);
        assertEquals(KAFKA_OAUTH2_SPRING_MAIN_SOURCES, ((Map<String, Map<String, Object>>) env.get("spring")).get("main").get("sources"));
    }

    @Test
    void testBindKafkaByDefault() {
        BindingServiceProperties bindingServiceProperties = new BindingServiceProperties();
        bpp.postProcessBeforeInitialization(bindingServiceProperties, null);
        Map<String, Object> env = bindingServiceProperties.getBinders().get("kafka")
                .getEnvironment();
        assertEquals(KAFKA_OAUTH2_SPRING_MAIN_SOURCES, ((Map<String, Map<String, Object>>) env.get("spring")).get("main").get("sources"));

    }

    @Test
    void testBindKafkaByName() {
        BinderProperties binderProperties = new BinderProperties();
        Map<String, BinderProperties> binders = new HashMap<>();
        binders.put("kafka", binderProperties);
        BindingServiceProperties bindingServiceProperties = new BindingServiceProperties();
        bindingServiceProperties.setBinders(binders);

        bpp.postProcessBeforeInitialization(bindingServiceProperties, null);
        Map<String, Object> env = bindingServiceProperties.getBinders().get("kafka")
                .getEnvironment();
        assertEquals(KAFKA_OAUTH2_SPRING_MAIN_SOURCES, ((Map<String, Map<String, Object>>) env.get("spring")).get("main").get("sources"));
    }

    @Test
    void testBindKafkaByType() {
        BinderProperties binderProperties = new BinderProperties();
        Map<String, BinderProperties> binders = new HashMap<>();
        binders.put("test", binderProperties);
        binderProperties.setType("kafka");
        BindingServiceProperties bindingServiceProperties = new BindingServiceProperties();
        bindingServiceProperties.setBinders(binders);

        bpp.postProcessBeforeInitialization(bindingServiceProperties, null);
        Map<String, Object> env = bindingServiceProperties.getBinders().get("test")
                .getEnvironment();
        assertEquals(KAFKA_OAUTH2_SPRING_MAIN_SOURCES, ((Map<String, Map<String, Object>>) env.get("spring")).get("main").get("sources"));
    }

    private Map<String, Object> buildSpringMainPropertiesMap(Map<String, Object> env, String secondProperty, String thirdProperty, String value) {
        if (StringUtils.hasText(secondProperty)) {
            Map<String, Object> second = new LinkedHashMap<>();
            second.put(thirdProperty, value);
            Map<String, Object> first = new LinkedHashMap<>();
            first.put(secondProperty, second);
            env.put("spring", first);
        }
        return bpp.getOrCreateSpringMainPropertiesMap(env);
    }

}
