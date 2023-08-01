package com.azure.spring.cloud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import com.azure.spring.cloud.feature.management.FeatureManager;
import com.azure.spring.cloud.feature.management.web.FeatureManagerSnapshot;

@SpringBootTest(classes = LoadConfigsTest.class)
@TestPropertySource(properties = {
    "spring.cloud.azure.appconfiguration.stores[0].endpoint= ${STORE_NAME}",
    "spring.cloud.azure.appconfiguration.stores[0].feature-flags.enabled= true" })
@Configuration
@EnableConfigurationProperties(value = MessageProperties.class)
@EnableAutoConfiguration
public class LoadConfigsTest {

    @Autowired
    private MessageProperties properties;

    @Autowired
    private Environment env;

    @Autowired
    private FeatureManager featureManager;

    @Autowired
    private FeatureManagerSnapshot featureManagerSnapshot;

    @Test
    public void sampleTest() {
        assertEquals("Test", properties.getMessage());
        assertEquals("From Key Vault", properties.getSecret());
        assertTrue(env.getProperty("feature-management.Alpha", Boolean.class));

        assertTrue(featureManager.isEnabled("Alpha"));
        Boolean random = featureManagerSnapshot.isEnabled("Random");
        for (int i = 0; i < 1000; i++) {
            assertEquals(random, featureManagerSnapshot.isEnabled("Random"));
        }
    }
}
