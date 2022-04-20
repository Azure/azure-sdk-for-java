package com.azure.spring.cloud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = LoadConfigsTest.class)
@TestPropertySource(properties = {
    "spring.cloud.azure.appconfiguration.stores[0].endpoint= ${STORE_NAME}",
    "spring.cloud.azure.appconfiguration.stores[0].feature-flags.enabled= true" })
@Configuration
@EnableConfigurationProperties(value = MessageProperties.class)
public class LoadConfigsTest {

    private final Logger log = LoggerFactory.getLogger(LoadConfigsTest.class);

    @Autowired
    private MessageProperties properties;
    
    @Autowired
    private Environment env;

    @Test
    public void sampleTest() {
        assertEquals("Test", properties.getMessage());
        assertEquals("From Key Vault", properties.getSecret());
        
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Boolean> map = env.getProperty("feature-management.featureManagement", LinkedHashMap.class);
        assertTrue(map.get("Alpha"));
    }
}
