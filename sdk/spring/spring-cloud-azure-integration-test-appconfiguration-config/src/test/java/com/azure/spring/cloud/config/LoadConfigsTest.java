package com.azure.spring.cloud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
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

    @Autowired
    private MessageProperties properties;
    
    @Autowired
    private Environment env;

    @SuppressWarnings("null")
    @Test
    public void sampleTest() {
        assertEquals("Test", properties.getMessage());
        assertEquals("From Key Vault", properties.getSecret());
        assertTrue(env.getProperty("feature-management.Alpha", Boolean.class));
    }
}
